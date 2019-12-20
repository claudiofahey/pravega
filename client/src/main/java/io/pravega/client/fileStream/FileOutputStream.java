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
 * Allows for writing to Pravega using the {@link OutputStream} interface.
 */
public abstract class FileOutputStream extends OutputStream implements AutoCloseable {

    @Override
    public abstract void write(byte[] b, int off, int len) throws IOException;

    public abstract void write(ByteBuffer src) throws IOException;

    @Override
    public abstract void close() throws IOException;

    /**
     * This must be called after all data has been written.
     * No data may be written after this is called.
     *
     * @return An {@link EventPointer} that
     *         can be used to immediately read the data written to this OutputStream.
     *         This implies that the event has been durably stored on the configured
     *         number of replicas, and is available for readers to see.
     */
    public abstract EventPointer seal() throws IOException;
}
