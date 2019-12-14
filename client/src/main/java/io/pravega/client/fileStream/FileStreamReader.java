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

import io.pravega.client.segment.impl.NoSuchEventException;
import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.TruncatedDataException;

import java.nio.ByteBuffer;

public interface FileStreamReader extends EventStreamReader<ByteBuffer> {

    /**
     * Similar to {@link #readNextEvent} but this returns a {@link FileInputStream}
     * that can be used to read the content of the next event.
     */
    EventRead<FileInputStream> readNextEventAsStream(long timeout) throws ReinitializationRequiredException, TruncatedDataException;

    /**
     * Similar to {@link #fetchEvent} but this returns a {@link FileInputStream}
     * that can be used to read the content of the next event.
     */
    FileInputStream fetchEventAsStream(EventPointer pointer) throws NoSuchEventException;
}
