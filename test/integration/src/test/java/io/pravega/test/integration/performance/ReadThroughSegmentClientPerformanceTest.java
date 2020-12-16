/**
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.test.integration.performance;

import com.google.common.primitives.Longs;
import io.netty.buffer.Unpooled;
import io.pravega.client.ClientConfig;
import io.pravega.client.connection.impl.ConnectionPoolImpl;
import io.pravega.client.connection.impl.SocketConnectionFactoryImpl;
import io.pravega.client.control.impl.Controller;
import io.pravega.client.security.auth.DelegationTokenProviderFactory;
import io.pravega.client.segment.impl.EventSegmentReader;
import io.pravega.client.segment.impl.Segment;
import io.pravega.client.segment.impl.SegmentInputStreamFactoryImpl;
import io.pravega.client.segment.impl.SegmentOutputStream;
import io.pravega.client.segment.impl.SegmentOutputStreamFactoryImpl;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.PendingEvent;
import io.pravega.client.stream.mock.MockController;
import io.pravega.common.concurrent.Futures;
import io.pravega.common.util.BufferView;
import io.pravega.segmentstore.contracts.ReadResult;
import io.pravega.segmentstore.contracts.ReadResultEntry;
import io.pravega.segmentstore.contracts.SegmentType;
import io.pravega.segmentstore.contracts.StreamSegmentStore;
import io.pravega.segmentstore.contracts.tables.TableStore;
import io.pravega.segmentstore.server.host.handler.PravegaConnectionListener;
import io.pravega.segmentstore.server.store.ServiceBuilder;
import io.pravega.segmentstore.server.store.ServiceBuilderConfig;
import io.pravega.segmentstore.server.store.ServiceConfig;
import io.pravega.shared.protocol.netty.ByteBufWrapper;
import io.pravega.test.common.TestUtils;
import io.pravega.test.common.ThreadPooledTestSuite;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Benchmark historical read performance from the segment store cache
 * using the StreamSegmentStore interface.
 * To run this with more than 2 GiB of data, you must increase
 * the -Xmx parameter in build.gradle, project('test:integration').test.jvmArgs.
 */
@Slf4j
public class ReadThroughSegmentClientPerformanceTest extends ThreadPooledTestSuite {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(900);
    private ServiceBuilder serviceBuilder;
    private final Consumer<Segment> segmentSealedCallback = segment -> { };

    @Before
    public void setup() throws Exception {
        final ServiceBuilderConfig serviceBuilderConfig = ServiceBuilderConfig
                .builder()
                .include(ServiceConfig.builder()
                        .with(ServiceConfig.CONTAINER_COUNT, 1)
                        // Following must be large enough to hold the entire segment because reads from storage do not complete.
                        .with(ServiceConfig.CACHE_POLICY_MAX_SIZE, 24*1024*1024*1024L)
                )
                .build();
        this.serviceBuilder = ServiceBuilder.newInMemoryBuilder(serviceBuilderConfig);
        this.serviceBuilder.initialize();
    }

    @After
    public void teardown() {
        this.serviceBuilder.close();
    }

    /**
     * Write many events, then read them.
     */
    @Test
    public void testHistoricalReadThroughSegmentClient() throws Exception {
        log.info("testHistoricalReadThroughSegmentClient: BEGIN");
        final String segmentName = "testHistoricalReadDirectlyFromStore";
        final int eventSize = 2*1024*1024 - 16;
        final long desiredTotalBytes = (long) (16*1024*1024*1024L);
        final long numEvents = desiredTotalBytes / eventSize;
        final long totalBytes = numEvents * eventSize;
        final byte[] data = new byte[eventSize];
        new Random().nextBytes(data);

        final String endpoint = "localhost";
        final String scope = "scope";
        final String stream = "stream";
        int port = TestUtils.getAvailableListenPort();
        final StreamSegmentStore segmentStore = this.serviceBuilder.createStreamSegmentService();
        final TableStore tableStore = serviceBuilder.createTableStoreService();
        @Cleanup
        final PravegaConnectionListener server = new PravegaConnectionListener(false, port, segmentStore, tableStore,
                serviceBuilder.getLowPriorityExecutor());
        server.startListening();
        @Cleanup
        final SocketConnectionFactoryImpl clientCF = new SocketConnectionFactoryImpl(ClientConfig.builder().build());
        @Cleanup
        final ConnectionPoolImpl connectionPool = new ConnectionPoolImpl(ClientConfig.builder().build(), clientCF);
        @Cleanup
        final Controller controller = new MockController(endpoint, port, connectionPool, true);

        controller.createScope(scope);
        controller.createStream(scope, stream, StreamConfiguration.builder().build());

        final SegmentOutputStreamFactoryImpl segmentproducerClient = new SegmentOutputStreamFactoryImpl(controller, connectionPool);
        final SegmentInputStreamFactoryImpl segmentConsumerClient = new SegmentInputStreamFactoryImpl(controller, connectionPool);
        final Segment segment = Futures.getAndHandleExceptions(controller.getCurrentSegments(scope, stream), RuntimeException::new)
                .getSegments().iterator().next();

        @Cleanup
        final SegmentOutputStream segmentOutputStream = segmentproducerClient.createOutputStreamForSegment(
                segment,
                segmentSealedCallback,
                EventWriterConfig.builder().build(),
                DelegationTokenProviderFactory.createWithEmptyToken());
        @Cleanup
        final EventSegmentReader eventSegmentReader = segmentConsumerClient.createEventReaderForSegment(segment);

        // Write events.
        for (long eventNumber = 1; eventNumber <= numEvents; eventNumber++) {
            segmentOutputStream.write(PendingEvent.withHeader(null, ByteBuffer.wrap(data), new CompletableFuture<>()));
        }
        segmentOutputStream.flush();
        log.info("testHistoricalReadThroughSegmentClient: Done writing data.");

        // Read events.
        final long t0 = System.nanoTime();
        long offset = 0;
        while (offset < totalBytes) {
            final ByteBuffer result = eventSegmentReader.read();
            log.trace("testHistoricalReadThroughSegmentClient: read event of size {}", result.remaining());
            offset += result.remaining();
            // Note that the actual data bytes are not used.
        }
        assertEquals(totalBytes, offset);
        final double durationSec = (System.nanoTime() - t0) * 1e-9;
        final double megabytes = totalBytes * 1e-6;
        final double megabytesPerSec = megabytes / durationSec;
        log.info("testHistoricalReadThroughSegmentClient: durationSec={}, megabytes={}, megabytesPerSec={}",
                durationSec, megabytes, megabytesPerSec);
        log.info("testHistoricalReadThroughSegmentClient: END");
    }
}
