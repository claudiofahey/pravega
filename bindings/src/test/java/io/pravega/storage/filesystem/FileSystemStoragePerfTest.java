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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

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
//        this.baseDir = new File("/mnt/lab9-isilon/home/faheyc/tmp");
        this.baseDir = new File("/mnt/isilon1/tmp/FileSystemStoragePerfTest");
        log.info("baseDir={}", baseDir);
        this.adapterConfig = FileSystemStorageConfig
                .builder()
                .with(FileSystemStorageConfig.ROOT, this.baseDir.getAbsolutePath())
                .with(FileSystemStorageConfig.REPLACE_ENABLED, true)
                .build();
    }

    @After
    public void tearDown() {
//        FileHelpers.deleteFileOrDirectory(baseDir);
        baseDir = null;
    }

    /**
     * Benchmark the read() method.
     * This will write events to a segment and then read them.
     */
    @Test(timeout = 0)
    public void benchmarkRead() throws Exception {
        final double totalGiB = 200.0;
        final int eventSize = 64*1024*1024;
        final long eventCount = (long) (totalGiB * 1024 * 1024 * 1024 / eventSize);
        log.info("totalGiB={}, eventSize={}, eventCount={}", totalGiB, eventSize, eventCount);
        final String segmentName = String.format("benchmarkRead-%dMiB", eventCount * eventSize / 1024 / 1024);
        log.info("segmentName={}", segmentName);
        final long sleepNanos = 0*1000*1000;
        final int permits = 10;

        try (Storage s = createStorage()) {
            s.initialize(DEFAULT_EPOCH);

            if (s.exists(segmentName, TIMEOUT).join()) {
                log.info("Segment already exists. Skipping write.");
            } else {
                log.info("Writing");
                s.create(segmentName, TIMEOUT).join();
                final val writeHandle = s.openWrite(segmentName).join();
                final byte[] writeData = new byte[eventSize];
                new Random().nextBytes(writeData);
                long offset = 0;
                for (long j = 0; j < eventCount; j++) {
                    ByteArrayInputStream dataStream = new ByteArrayInputStream(writeData);
                    s.write(writeHandle, offset, dataStream, writeData.length, TIMEOUT).join();
                    offset += writeData.length;
                }
            }

            log.info("Reading");
            long offset = 0;
            final val readHandle = s.openRead(segmentName).join();
            final byte[] readBuffer = new byte[eventSize];
            final long t0 = System.currentTimeMillis();
            final Semaphore sem = new Semaphore(permits);
            for (long j = 0; j < eventCount; j++) {
                sem.acquire();
                final CompletableFuture<Integer> future = s.read(readHandle, offset, readBuffer, 0, readBuffer.length, TIMEOUT);
                future.thenAccept(bytesRead -> {
                    Assert.assertEquals("Unexpected number of bytes read.", eventSize, (long) bytesRead);
                    // Simulate delay between reads. We use a busy wait to have precise control over the delay.
                    if (sleepNanos > 0) {
                        final long sleepStart = System.nanoTime();
                        while (System.nanoTime() - sleepStart < sleepNanos) ;
                    }
                    sem.release();
                });
                offset += eventSize;
            }
            sem.acquire(permits);
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
