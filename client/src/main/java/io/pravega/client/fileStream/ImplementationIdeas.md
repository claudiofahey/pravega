# Pravega File API (Draft Proposal)

## Implementation Ideas

This section provides some thoughts on how the Pravega File API might be implemented.

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

Since skipping requires interpreting the events, we want to avoid this occurring on the segment store.
Instead, the skipping logic should be completely handled on the client.
When the client is on a remote network, a simple implementation of this will cause significant latency between each chunk that is skipped.
To reduce this effect, the client can use a simple predictive read-ahead algorithm to read multiple byte ranges from the segment store.
This will be effective as long as all chunks except the last are the same size.

### Method #2 - Chunking with reassembly during commit

This method uses Method #1's process to perform chunked writes to a transaction.
However, when a segment store commits a transaction, it will reassemble any chunked events.
The read process will remain identical to the current implementation, except that the event
length can be up to 2 GiB.
In particular, readers will not need to handle partial flags and the process for skipping events will be trivial.

Cons: This requires the segment store to interpret the byte stream.
This is contrary to the design of the segment store and should be avoided.

It should be considered to increase the event size limit beyond a signed 32-bit integer (2 GiB).
A base 128 varint can encode values 0-127 with only 1 byte, making it very efficient for tiny messages.
It can also encode very large integers with 7/8 efficiency.
See https://developers.google.com/protocol-buffers/docs/encoding#varints.

### Method #3 - Chunking with patching during commit

When an event larger than 1 MB is written, it will be serialized to a transactional segment with a
placeholder length prefix. The client will note the offset to the placeholder length prefix.
Additional data chunks will be written to transactional segments until the entire event has been written.
At that point, the client will note the final event length and record a PATCH record
consisting of {segment ID, offset to length prefix, length of length prefix (4), 32-bit integer length of event}.
Additional events may be written to the same transaction and PATCH records will be created
for any events larger than 1 MB.
Prior to flush or commit, the list of PATCH records will be sent to the Pravega server.
Upon commit, the PATCH instructions will be applied to the segments, ensuring that length-prefixes are correct.

This avoids having the segment store interpret bytes.

The read process will remain identical to the current implementation, except that the event
length can be up to 2 GiB.
In particular, readers will not need to handle partial flags and the process for skipping events will be trivial.

Optional: Consider using a combination of variable-length and fixed-length integer encoding for the length prefixes.
If done correctly, we could have just 1-byte overhead for very small events, increasing to 6 byte overhead for 1 TB events.
