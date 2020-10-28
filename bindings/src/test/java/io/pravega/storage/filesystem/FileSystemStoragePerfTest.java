/**
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.storage.filesystem;

import io.pravega.common.io.FileHelpers;
import io.pravega.segmentstore.storage.AsyncStorageWrapper;
import io.pravega.segmentstore.storage.IdempotentStorageTestBase;
import io.pravega.segmentstore.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Random;

/**
 * Performance benchmarks for FileSystemStorage.
 */
@Slf4j
public class FileSystemStoragePerfTest extends IdempotentStorageTestBase {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(900);
    private File baseDir = null;
    private FileSystemStorageConfig adapterConfig;

    @Before
    public void setUp() throws Exception {
//        this.baseDir = Files.createTempDirectory("test_nfs").toFile().getAbsoluteFile();
        this.baseDir = new File("/mnt/lab9-isilon/home/faheyc/tmp");
        log.info("baseDir={}", baseDir);
        this.adapterConfig = FileSystemStorageConfig
                .builder()
                .with(FileSystemStorageConfig.ROOT, this.baseDir.getAbsolutePath())
                .with(FileSystemStorageConfig.REPLACE_ENABLED, true)
                .build();
    }

    @After
    public void tearDown() {
        FileHelpers.deleteFileOrDirectory(baseDir);
        baseDir = null;
    }

    /**
     * Benchmark the read() method.
     * This will write events to a segment and then read them.
     */
    @Test(timeout = 0)
    public void benchmarkRead() throws Exception {
        final String segmentName = "foo_write";
        final long eventCount = 10*1024;
        final int eventSize = 1024*1024;
        final long sleepNanos = 4*1000*1000;

        try (Storage s = createStorage()) {
            s.initialize(DEFAULT_EPOCH);
            s.create(segmentName, TIMEOUT).join();

            log.info("Writing");
            final val writeHandle = s.openWrite(segmentName).join();
            long offset = 0;
            final byte[] writeData = new byte[eventSize];
            new Random().nextBytes(writeData);
            for (long j = 0; j < eventCount; j++) {
                ByteArrayInputStream dataStream = new ByteArrayInputStream(writeData);
                s.write(writeHandle, offset, dataStream, writeData.length, TIMEOUT).join();
                offset += writeData.length;
            }

            log.info("Reading");
            offset = 0;
            final val readHandle = s.openRead(segmentName).join();
            final byte[] readBuffer = new byte[eventSize];
            final long t0 = System.currentTimeMillis();
            for (long j = 0; j < eventCount; j++) {
                int bytesRead = s.read(readHandle, offset, readBuffer, 0, readBuffer.length, TIMEOUT).join();
                Assert.assertEquals(String.format("Unexpected number of bytes read from offset %d.", offset),
                        eventSize, bytesRead);
                offset += bytesRead;
                // Simulate delay between reads. We use a busy wait to have precise control over the delay.
                if (sleepNanos > 0) {
                    final long sleepStart = System.nanoTime();
                    while (System.nanoTime() - sleepStart < sleepNanos);
                }
            }
            final double elapsedSec = (System.currentTimeMillis() - t0) / 1000.0;
            final double megabytesPerSec = (double) eventSize * eventCount / 1e6 / elapsedSec;
            log.info("Read rate: {} MB/sec", megabytesPerSec);
        }
    }

    @Override
    protected Storage createStorage() {
        return new AsyncStorageWrapper(new FileSystemStorage(this.adapterConfig), executorService());
    }
}
