
# Pravega File API (Draft Proposal)

The Pravega File API is very similar to the event API but the generic event type is a `java.io.InputStream` or
`java.io.OutputStream`. I called it the "file stream" API because I didn't want to call it the "stream stream" API.

Assuming this can be implemented, it has all the functionality of the non-transactional event API and byte stream API combined.
It supports routing keys, multiple segments, reader groups, events of unlimited size, and does not require the user to implement framing.

The intent is to have an unbounded sequence of events.
The events are always a bounded sequence of bytes but of unlimited size.
In effect, this is just the event API but with
1) unlimited size events
2) an API that allows reading/writing with small buffers.

A key difference between the file API and the byte stream API is that no data will be committed until the writer has closed
the `FileOutputStream` that is associated with a single event.
At that point, the event's content becomes fixed and it can be made available to readers, possibly by committing the transaction.
Once the `FileOutputStream` has been closed, no more bytes can be written to that event.
However, a new event can be started by calling `EventStreamWriter.writeEvent`.

This shows how to use the file writer API.

```
while (true) {
    FileOutputStream os1 = writer.writeEvent("routingKey1");
    // writeEvent does not perform any Pravega RPCs.
    os1.write(data1);
    // No data is available to readers yet. It is either buffered in the client or written to an open Pravega transaction.
    os1.write(data2);
    // Still no data is available to readers.
    os1.close();
    // Buffers have been flushed and the open transaction has been committed. Now the entire event is available to readers.
}
```

This shows how to use the file reader API.

```
while (true) {
    EventRead<FileInputStream> eventRead = reader.readNextEvent(timeout);
    FileInputStream inputStream = eventRead.getEvent();
    if (inputStream != null) {
        // Copy event contents to a normal file.
        OutputStream outputStream = new java.io.FileOutputStream("/tmp/file");
        IOUtils.copyLarge(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }
}
```

## Other Features

- Writer concurrency: A single writer can have multiple events "open" and write to them concurrently from
  different threads.

- Reader concurrency: A single reader can have multiple events "open" and read from them concurrently from
  different threads.

- Skip ahead: A reader may read just a few bytes of a very large event and decide to skip reading the entire event
  and move to the next event. This should be implemented efficiently.

- Random read: A reader may read a single event based on an `EventPointer`.

- Streams written using the standard event API should be readable using the file API.

- Streams written using the file API should be readable using the standard event API.
  Events that exceed the 1 MB event API limit should be handled as non-fatal conditions.

