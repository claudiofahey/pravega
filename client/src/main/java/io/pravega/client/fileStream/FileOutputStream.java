/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.client.fileStream;

import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.EventStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Allows for writing the content of a single event using the {@link OutputStream} interface.
 */
public abstract class FileOutputStream extends OutputStream implements AutoCloseable {

    /**
     * Writes the provided data to buffers in preparation for appending to a Pravega stream.
     * Data is not made available to any readers until {@link #close} is called.
     *
     * It is intended that this method not block, but it may in the event that the server becomes
     * disconnected for sufficiently long or is sufficiently slow that that backlog of data to be
     * written becomes a memory issue.
     */
    @Override
    public abstract void write(byte[] b, int off, int len) throws IOException;

    /**
     * Similar to {@link #write(byte[], int, int)}
     */
    public abstract void write(ByteBuffer src) throws IOException;

    /**
     * Indicates that the content of the entire event has been written entirely.
     * This method will generally return before data becomes durably stored.
     * When using this method, there is no guarantee of durability or readability until
     * {@link FileStreamWriter#flush} is called.
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Returns a future that will complete when the event becomes durably stored and readable.
     * This must be called before {@link #close}.
     *
     * @return A completableFuture that will complete when the event has been durably stored on the configured
     *         number of replicas, and is available for readers to see. This future may complete exceptionally
     *         if this cannot happen, however these exceptions are not transient failures. Failures that occur
     *         as a result of connection drops or host death are handled internally with multiple retires and
     *         exponential backoff. So there is no need to attempt to retry in the event of an exception.
     *         This future will complete sometime after {@link #close} is called.
     */
    public abstract CompletableFuture<Void> getReadabilityFuture() throws IOException;

    /**
     * Returns a future for the {@link EventPointer}.
     * This must be called before {@link #close}.
     *
     * @return A future that will complete when the {@link EventPointer} becomes available and
     *         can be used to immediately read the event.
     *         This implies that the event has been durably stored on the configured
     *         number of replicas, and is available for readers to see.
     *         This future will complete sometime after {@link #close} is called.
     */
    public abstract CompletableFuture<EventPointer> getEventPointerFuture() throws IOException;
}
