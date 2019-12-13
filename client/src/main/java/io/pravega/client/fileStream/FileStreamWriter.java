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

/**
 * A writer that can write files (an unlimited sequence of bytes) to a stream.
 * Based on EventStreamWriter.
 */
public interface FileStreamWriter extends AutoCloseable {
    /**
     * Initiate writing a file to the stream.
     * This method performs no I/O to Pravega.
     *
     * @param routingKey A free form string that is used to route messages to readers. Two events written with
     *        the same routingKey are guaranteed to be read in order. Two events with different routing keys
     *        may be read in parallel. 
     * @return A FileOutputStream that can be used to write the file contents.
     */
    FileOutputStream writeEvent(String routingKey);

    FileOutputStream writeEvent();

    void noteTime(long timestamp);

    @Override
    void close();
}
