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
import io.pravega.client.stream.Transaction;
import io.pravega.client.stream.TxnFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Allows for writing the content of a single event using the {@link OutputStream} interface.
 */
public abstract class TransactionalFileOutputStream extends OutputStream {

    /**
     * Writes the provided data to buffers in preparation for appending to a Pravega stream.
     * Data is not made available to any readers until {@link #close} and
     * {@link FileTransaction#commit} are called.
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
     * The event will not be visible to anyone until {@link FileTransaction#commit} is called.
     *
     * @throws TxnFailedException The Transaction is no longer in state {@link Transaction.Status#OPEN}
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Returns a future for the {@link EventPointer}.
     * This must be called before {@link #close}.
     *
     * @return A future that will complete when the {@link EventPointer} becomes available and
     *         can be used to immediately read the event.
     *         This implies that the event has been durably stored on the configured
     *         number of replicas, and is available for readers to see.
     *         This future will complete sometime after both {@link #close} and {@link FileTransaction#commit} are called.
     */
    public abstract CompletableFuture<EventPointer> getEventPointerFuture() throws IOException;
}
