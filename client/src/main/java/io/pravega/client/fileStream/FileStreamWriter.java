/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
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

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * An {@link EventStreamWriter} that can write byte sequences of unlimited size to a Pravega stream.
 */
public interface FileStreamWriter extends EventStreamWriter<ByteBuffer> {
    /**
     * Initiate writing an event to the stream.
     * This will return a {@link FileOutputStream} that can be used to write the event contents.
     * The event will not be made available to readers until {@link FileOutputStream#close} is called.
     * This method performs no I/O to Pravega.
     *
     * @return A FileOutputStream that can be used to write the event contents.
     */
    FileOutputStream beginWriteEvent(String routingKey);
    FileOutputStream beginWriteEvent();

    /**
     * Write a single event whose content will be the concatenation of one or more ByteBuffers.
     */
    CompletableFuture<Void> writeEvent(String routingKey, ByteBuffer... event);
    CompletableFuture<Void> writeEvent(ByteBuffer... event);

    /**
     * Write a single event whose content will be the concatenation of one or more ByteBuffers.
     *
     * @return A CompletableFuture that will receive the {@link EventPointer} that can be used
     * to immediately read the event.
     */
    CompletableFuture<EventPointer> writeEventAndReturnPointer(String routingKey, ByteBuffer... event);
    CompletableFuture<EventPointer> writeEventAndReturnPointer(ByteBuffer... event);
}
