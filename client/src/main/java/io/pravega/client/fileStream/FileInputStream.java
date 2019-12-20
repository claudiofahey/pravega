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

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousChannel;
import java.util.concurrent.CompletableFuture;

/**
 * Allows for reading raw bytes from an {@link EventPointer} using the {@link InputStream} interface.
 *
 * This class is designed such that it can be used with
 * or without blocking. To avoid blocking use the {@link #onDataAvailable()} method to make sure to
 * only call {@link #read(byte[])} when there is data {@link #available()}.
 *
 * It is safe to invoke methods on this class from multiple threads, but doing so will not increase
 * performance.
 */
@ThreadSafe
public abstract class FileInputStream extends InputStream implements AsynchronousChannel, AutoCloseable {

    /**
     * Returns the number of bytes that can be read without blocking. If the number returned is greater than 0
     * then a call to {@link #read(byte[])} will return data from memory without blocking. If the
     * number returned is 0 then {@link #read(byte[])} will block. If -1 is returned this indicates
     * the end of the stream has been reached and a call to {@link #read(byte[])} will return -1.
     *
     * @see InputStream#available()
     * @return the number of bytes that can be read without blocking.
     */
    @Override
    public abstract int available();

    /**
     * If {@link #available()} is non-zero, this method will read bytes from an in-memory buffer into the
     * provided array. If {@link #available()} is zero will wait for additional data to arrive and
     * then fill the provided array. This method will only block if {@link #available()} is 0. In
     * which case it will block until some data arrives and return that. (Which may or may not fill
     * the provided buffer)
     *
     * See {@link InputStream#read(byte[], int, int)}
     *
     * @return The number of bytes copied into the provided buffer. Or -1 if the end of the file
     * has been reached.
     *
     */
    @Override
    public abstract int read(byte[] b, int off, int len) throws IOException;

    /**
     * This method attempts to skip forward by the provided number of bytes. If it is not possible
     * to skip forward `n` bytes (because there are less than `n` bytes remaining, it will skip as
     * many as possible and return the number skipped.
     *
     * This method is not affected by truncation.
     * @param n number of bytes to skip.
     * @return number of bytes skipped.
     * @throws IOException Thrown if an IOError occurs while attempting to obtain the length of the
     *             stream.
     */
    @Override
    public abstract long skip(long n) throws IOException;

    /**
     * Closes the reader.
     * This may block on an ongoing {@link #read()} request if there is one.
     * See {@link InputStream#close()}
     */
    @Override
    public abstract void close();

    /**
     * Returns a future that will be completed when there is data available to be read. The Integer
     * in the result will be the number of bytes {@link #available()} or -1 if the reader has
     * reached the end of the event.
     *
     * @return A the number of bytes {@link #available()}
     */
    public abstract CompletableFuture<Integer> onDataAvailable();

}
