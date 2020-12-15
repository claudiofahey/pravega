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
import io.pravega.common.util.BufferView;
import io.pravega.segmentstore.contracts.ReadResult;
import io.pravega.segmentstore.contracts.ReadResultEntry;
import io.pravega.segmentstore.contracts.ReadResultEntryType;
import io.pravega.segmentstore.contracts.SegmentType;
import io.pravega.segmentstore.contracts.StreamSegmentStore;
import io.pravega.segmentstore.server.store.ServiceBuilder;
import io.pravega.segmentstore.server.store.ServiceBuilderConfig;
import io.pravega.shared.protocol.netty.ByteBufWrapper;
import io.pravega.test.common.ThreadPooledTestSuite;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.InputStream;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ReadPerformanceTest extends ThreadPooledTestSuite {

    private static final int TIMEOUT_MILLIS = 300000;
    private ServiceBuilder serviceBuilder;
    @Rule
    public Timeout globalTimeout = Timeout.millis(TIMEOUT_MILLIS);

    @Before
    public void setup() throws Exception {
        this.serviceBuilder = ServiceBuilder.newInMemoryBuilder(ServiceBuilderConfig.getDefaultConfig());
        this.serviceBuilder.initialize();
    }

    @After
    public void teardown() {
        this.serviceBuilder.close();
    }

    @Test
    public void testReadDirectlyFromStore() throws Exception {
        log.info("testReadDirectlyFromStore: BEGIN");
        final String segmentName = "testReadFromStore";
        final int eventSize = 2*1024*1024;
        final long desiredTotalBytes = (long) (2.0 * 1024*1024*1024);
        final long numEvents = desiredTotalBytes / eventSize;
        final long totalBytes = numEvents * eventSize;
        final byte[] data = new byte[eventSize];
        new Random().nextBytes(data);
        final UUID clientId = UUID.randomUUID();

        final StreamSegmentStore segmentStore = serviceBuilder.createStreamSegmentService();

        fillStoreForSegment(segmentName, clientId, data, numEvents, segmentStore);

        final long t0 = System.nanoTime();
        long offset = 0;
        while (offset < totalBytes) {
            final int maxLength = (int) Longs.constrainToRange(totalBytes - offset, 0, 128*1024*1024);
            @Cleanup
            final ReadResult result = segmentStore.read(segmentName, offset, maxLength, Duration.ZERO).get();
            log.info("testReadDirectlyFromStore: ReadResult={}", result);
            while (result.hasNext()) {
                final ReadResultEntry entry = result.next();
                final BufferView contents = entry.getContent().get();
                log.info("testReadDirectlyFromStore: ReadResultEntry={}, contents.getLength={}",
                        entry, contents.getLength());
                offset += contents.getLength();
            }
        }
        assertEquals(totalBytes, offset);
        final double durationSec = (System.nanoTime() - t0) * 1e-9;
        final double megabytes = totalBytes * 1e-6;
        final double megabytesPerSec = megabytes / durationSec;
        log.info("testReadDirectlyFromStore: durationSec={}, megabytes={}, megabytesPerSec={}",
                durationSec, megabytes, megabytesPerSec);
        log.info("testReadDirectlyFromStore: END");
    }

    private void fillStoreForSegment(String segmentName, UUID clientId, byte[] data, long numEntries,
                                     StreamSegmentStore segmentStore) {
        log.info("fillStoreForSegment: BEGIN");
        try {
            segmentStore.createStreamSegment(segmentName, SegmentType.STREAM_SEGMENT, null, Duration.ZERO).get();
            for (long eventNumber = 1; eventNumber <= numEntries; eventNumber++) {
                segmentStore.append(segmentName, new ByteBufWrapper(Unpooled.wrappedBuffer(data)), null, Duration.ZERO).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info("fillStoreForSegment: END");
    }
}
