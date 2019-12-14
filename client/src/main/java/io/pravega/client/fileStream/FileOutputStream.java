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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Allows for writing the content of a single event using the {@link OutputStream} interface.
 */
public abstract class FileOutputStream extends OutputStream {

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
     * No more bytes may be written to this event after this call.
     * Writes the end-of-file (EOF) marker, flushes all buffers, writes any events to Pravega,
     * commits the transaction (if open), and closes this FileOutputStream.
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Same as {@link #close} but also returns the {@link EventPointer}.
     *
     * @return the {@link EventPointer} that can be used to immediately read the event.
     */
    public abstract EventPointer closeAndReturnEventPointer() throws IOException;
}
