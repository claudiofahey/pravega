
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

```java
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

```java
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

For more samples, see [SampleUsage.java](SampleUsage.java).

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

## Implementation Ideas

### FileOutputStream.write

This method writes the provided data to buffers in preparation for appending to a Pravega stream.
Data is not made available to any readers until close() is called.

Written bytes can be buffered on the client until they reach a threshold of 1 MB.
At that point, a transaction will be started and the bytes will be written to Pravega
(but not committed) by effectively calling EventStreamWriter.writeEvent multiple times.
If this FileOutputStream is closed before reaching the threshold, a transaction does
not need to be used.

### Serialization to Segments

The simplest way to serialize events is to simply prefix the data with the length of the data.
This is currently done with the standard event API.

This can also be done with the file API but the complication is that the event size will not be
known until the `FileOutputStream.close` is called.

1. One possibility is to change the segment store
   commit logic so that it merges all standard (1 MB) events in the transaction.
   The length can then be calculated and written before the event. It is unclear if this is feasible.

2. An alternative is as follows. It requires no server-side changes.

   - Writer: When `FileOutputStream.write` has 1 MB in its internal buffer,
     it writes a standard event to an open transaction in Pravega. It repeats this as many times as necessary.
     When `FileOutputStream.close` is called, it will write a special end-of-file marker event.
     This can be something like an event with exactly one zero byte (with proper byte stuffing to handle
     real events with exactly one zero byte).
     After the EOF marker, it will commit the Pravega transaction.

   - Reader: `FileInputStream.read` will read bytes from standard events in the reader group and return them to the caller.
     When it reaches the end of the standard event, it will read the following event *in that segment*.
     If that event is the EOF marker, then it will return EOF to the caller.
     Otherwise, it will continue returning bytes to the caller.
     One critical difference between this behavior and the current event API behavior is that all
     reads through the EOF marker must come from the same segment. We don't want the reader
     to change to another segment in the middle of a `FileOutputStream`. This is a client-side change.

### getEventPointer from the writer

A nice feature would be to allow a writer to obtain the EventPointer for an event that has just been written and committed.
If this can be done efficiently, it should be available.
