
# Pravega File API (Draft Proposal)

The Pravega File API is very similar to the event API.
`FileStreamWriter` extends `EventStreamWriter<ByteBuffer>` and has additional methods to write event content as a `java.io.OutputStream`.
`FileStreamReader` extends `EventStreamReader<ByteBuffer>` and has additional methods to read event content as a `java.io.InputStream`.

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
    FileOutputStream os1 = writer.beginWriteEvent("routingKey1");
    // writeEvent does not perform any Pravega RPCs.
    os1.write(data1);
    // No data is available to readers yet.
    // It is either buffered in the client or written to an open Pravega transaction.
    os1.write(data2);
    // Still no data is available to readers.
    os1.close();
    // Buffers have been flushed and the open transaction has been committed.
    // Now the entire event is available to readers.
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

## Other Features

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

## Implementation Ideas

### Method #1 - Chunking

It is believed that this method can be implemented with changes only to the Pravega client.

#### Encoding of Large Events to Segments

An event is currently written to a segment as the the length of the event (32-bit integer) followed by the serialized event.
This will be changed so that events can be written in chunks.
In general, chunks will be handled as events are today.
However the high-order bit of the length prefix will be used to store a “partial” flag.
It will be 1 if the event content continues to the following chunk.
Otherwise, the current chunk is the last one for a particular event.
This has the property that an event written using a prior version of Pravega will be decoded as an event with one chunk, providing backward compatibility.
Forward compatibility will exist for events up to 1 MB.

Below shows a small event that consists of the hex bytes 12345678. It's encoding would remain unchanged.
```
0000 0004 1234 5678
```

Below shows an event split into two chunks.
The logical event consists of the hex bytes 123456789012.
Note that there is no reason to chunk such as small event.
This small event is chunked only to illustrate the encoding.
```
f000 0004 1234 5678
0000 0002 9012
```


#### Writers

The underlying implementation of *all* Pravega writers
(`EventStreamWriter`, `IdempotentEventStreamWriter`, `TransactionalEventStreamWriter`, `FileStreamWriter`)
will be changed to allow an event to consist of multiple chunks.
If an event is 1 MB or less, there is no effective change.
For a larger event, it would be split into chunks and written to the segment in a transaction.
This ensures that all chunks for an event are contiguous.

When writing chunks, the writer does not need to know the total event size in advance.
It only needs to know if additional bytes can be written in which case it would set the partial flag.
It should be possible to write a final chunk with zero length (encoded as `0000 0000`).

Transactions can be implicit (controlled by the implementation) or explicit (controlled by the user).
If explicit, multiple large events can be written in the same transaction for improved throughput.

A useful feature would be to allow a writer to obtain the EventPointer for an event that has just been written.
If this can be done efficiently, it should be available.

#### Readers

The underlying implementation of all Pravega readers
(`EventStreamReader`, `FileStreamReader`)
will be changed to reassemble events from chunks. 
When a reader encounters a chunk with the partial flag set, it will know that it must continue reading the next chunk *in that segment*.
One critical difference between this behavior and the current event API behavior is that all reads through the final chunk must come from the same segment.
We don't want the reader to change to another segment in the middle of an event.

The `EventStreamReader.readNext` method will build a single byte array in memory for the entire event.
This will be convenient but could require lots of memory on the client.
The `FileStreamReader.readNextEventAsStream` method can be used to avoid using excessive amount of memory.
Regardless of the API, the event content is identical.

#### Skipping Events during Read

When a reading a large event as a `java.io.InputStream`, it is sometimes useful to skip reading
large number of bytes using `InputStream.skip`.
Additionally, the caller of `FileStreamReader.readNextEventAsStream` may decide to
get the next event before the current event has been read completely.
In either case, the implementation should be able to efficiently provide a way to
fast-forward through an event consisting of many chunks.

Consider the case in which there is a 1 GiB event written as 1024 1 MiB chunks.
A reader can skip the entire event as follows.
The reader reads the first 32-bit length field.
It would then skip the number of bytes specified in the length field.
If the partial flag was set, this process would repeat for the following chunk.
In total, there would be 1024 reads of 4 bytes each, totaling 4096 bytes, in order to skip 1 GiB of data.
If the data were read from disks, this might require 1024 4 KiB reads totaling 4 MiB.

### Method #2 - Chunking with reassembly during commit

This method uses Method #1's process to perform chunked writes to a transaction.
However, when a segment store commits a transaction, it will reassemble any chunked events.
The read process will remain identical to the current implementation, except that the event
length can be up to 2 GiB.
In particular, readers will not need to handle partial flags and the process for skipping events will be trivial.

It should be considered to increase the event size limit beyond a signed 32-bit integer (2 GiB).
A base 128 varint can encode values 0-127 with only 1 byte, making it very efficient for tiny messages.
It can also encode very large integers with 7/8 efficiency.
See https://developers.google.com/protocol-buffers/docs/encoding#varints.
