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
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Allows for writing raw bytes directly to a stream.
 * Unlike ByteStreamWriter, it allows multiple segments via routing keys and provides framing.
 */
public abstract class FileOutputStream extends OutputStream {

    /**
     * Writes the provided data to buffers in preparation for appending to a Pravega stream.
     * Data is not made available to any readers until close() is called.
     *
     * It is intended that this method not block, but it may in the event that the server becomes
     * disconnected for sufficiently long or is sufficiently slow that that backlog of data to be
     * written becomes a memory issue.
     *
     * Implementation note:
     * Written bytes can be buffered on the client until they reach a threshold of 1 MB.
     * At that point, a transaction will be started and the bytes will be written to Pravega
     * (but not committed) by essentially calling EventStreamWriter.writeEvent multiple times.
     * If this FileOutputStream is closed before reaching the threshold, a transaction does
     * not need to be used.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }

    @Override
    public void write(int var1) throws IOException {
    }

    /**
     * Writes the end-of-file (EOF) marker, flushes all buffers, writes any events to Pravega,
     * commits the transaction (if open), and closes this FileOutputStream.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Returns a pointer object for the event read. The event pointer enables a random read of the
     * event at a future time.
     * This can only be called after close().
     * This may require an RPC to the server.
     *
     * @param wait If true, this method will block until subsequent calls to FileStreamReader#fetchEvent
     *             are guaranteed to succeed.
     *             This is important to have read-after-write consistency.
     *             If this guarantee is always provided, this parameter can be dropped.
     * @return Pointer to an event.
     */
    EventPointer getEventPointer(boolean wait) {
        throw new NotImplementedException("");
    }
}
