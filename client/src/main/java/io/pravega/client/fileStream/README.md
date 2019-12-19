
# Pravega File API (Draft Proposal)

This is a draft proposal for improvements to the Pravega client API.
Feedback is requested from all potential users of Pravega.

The proposed Pravega File API is very similar to the event API.
- `FileStreamWriter` extends `EventStreamWriter<ByteBuffer>` and has additional methods to write event content as a `java.io.OutputStream`.
- `FileStreamReader` extends `EventStreamReader<ByteBuffer>` and has additional methods to read event content as a `java.io.InputStream`.
- `TransactionalFileStreamWriter` is similar to `TransactionalEventStreamWriter<ByteBuffer>`
  and has additional methods to write event content as a `java.io.OutputStream`.

It has all the functionality of the non-transactional event API and byte stream API combined.
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
However, a new event can be started by calling `EventStreamWriter.beginWriteEvent`.

This shows how to use the file writer API.

```java
while (true) {
    // Initiate writing a new event.
    // beginWriteEvent does not perform any Pravega RPCs.
    FileOutputStream os1 = writer.beginWriteEvent("routingKey1");
    // Write some bytes to the event.
    os1.write(data1);
    // No data is available to readers yet.
    // It is either buffered in the client or written to an open Pravega transaction.
    // Write some more bytes to the same event.
    os1.write(data2);
    // Still no data is available to readers.
    // Indicate that we are done writing bytes to the events.
    os1.close();
    // The entire event may now be available to readers but this is not guaranteed.
    writer.flush();
    // Now the entire event is guaranteed to be available to readers.
    // Multiple events may be written to the same stream.
}
```

This shows how to use the file reader API.

```java
while (true) {
    EventRead<FileInputStream> eventRead = reader.readNextEventAsStream(timeout);
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

## Other Possible Features

Feedback is appreciated on the possible features below.

- Writer concurrency: A single writer can have multiple events "open" and write to them concurrently from
  different threads.

- Reader concurrency: A single reader can have multiple events "open" and read from them concurrently from
  different threads.

- Skip ahead: A reader may read just a few bytes of a very large event and decide to skip reading the rest of the event
  and move to the next event. This should be implemented efficiently.

- Random read: A reader may read a single event based on an `EventPointer`.

- A `FileStreamWriter` can choose to mix `writeEvent` and `beginWriteEvent` in any way.

- A `FileStreamReader` can choose to mix `readNextEvent` and `readNextEventAsStream` in any way.

- Streams written using the standard event API should be readable using the file API.

- Streams written using the file API should be readable using the standard event API (EventStreamReader).
  Events may exceed 1 MB and any necessary reassembly should be transparent to the caller.
  Readers should be able to set a maximum accepted event size (with 1 MB default) to avoid out-of-memory errors on the client.
  If EventStreamReader.readNextEvent encounters an event whose size exceeds this limit, it should
  throw an EventTooLargeException. Subsequent calls should skip the large event and return the following event.

- It is expected that the enhancements made to Pravega to implement the file API should be directly applicable to the standard event API,
  allowing the standard event API to accept events of unlimited size.

- The new method `CompletableFuture<EventPointer> writeEventAndReturnPointer(String routingKey, ByteBuffer... event)`
  will return a future to an `EventPointer`. The `EventPointer` can be persisted and used to read the event at any time
  in the future using `fetchEvent`. Similar methods will be available for all APIs that write events.

- The new method `CompletableFuture<Void> writeEvent(String routingKey, ByteBuffer... event)`
  accepts an unlimited sequence of ByteBuffers. These will be concatenated to build a single event.

## Implementation Ideas

See [Implementation Ideas](ImplementationIdeas.md).
