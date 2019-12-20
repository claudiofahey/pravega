
# Pravega File API B - Header Stores Event Pointer to Body (Draft Proposal)

This is a draft proposal for improvements to the Pravega client API.
Feedback is requested from all potential users of Pravega.

## Event header and body

All use cases have events that can be considered to have a header and a body, much like an HTTP request.
The header is a small object that will always fit in memory and is generally under 1 MB.
It will generally be serialized to/from an in-memory data structure.
JSON or Avro are often used.
The body of an event is a potentially very large byte sequence that may not fit in memory.
Also, the body of an event may not be serialized at all.
An application may just want to pump bytes from an HTTP socket to the body of an event that will be written to Pravega.
Perhaps the body needs to be sent through a GZIPOutputStream and a CipherOutputStream before it gets written to Pravega.
Some types of events will have only a header or only a body.
In any case, it is natural for all use cases for the header to be serialized and the body to be handled as a stream of bytes (java.io.InputStream, OutputStream).

## File API B - Proposed method

There will be two lower level APIs.
One that writes binary data in a streaming way.
Similar to byte(input/output)Stream, but that is not a top level stream.
Rather it can only be read via a "pointer" to it, which is obtained when one calls seal.
(So it's in some ways like a transaction segment, but it never gets committed anywhere).
Then these pointers could be included in 'headers' which could be sent via a normal stream.

For samples, see [SampleUsage.java](SampleUsage.java).

# File API A compared with File API B

File API A is https://github.com/claudiofahey/pravega/tree/fileapi/client/src/main/java/io/pravega/client/fileStream.

File API B is this one.

- Body storage:

  File API A stores the header and body together in the same segment.
  File API B stores each body separately.

- Chunking:

  File API A uses chunking which is quite complicated to implement.
  File API B has no chunking at all and is much simpler.

- Reading of headers and bodies:

  File API A allows very efficient access and read-ahead can be very effective.

  File API B requires an RPC to Pravega to retrieve each body.
  This is suboptimal in Flink.
  There may need to be significant changes to the Pravega Flink connector to enable this ability
  or make it perform well.
  Read-ahead may be less effective.

- Reading of headers only:

  File API A must skip the bodies during read.
  This is difficult to implement and suboptimal.

  This is trivial with File API B.

- Transactions:

  File API A allows a single transaction to contain the header and body of multiple events.
  A transaction abort causes all headers and bodies to be deleted from storage.

  File API B only includes event headers in a transaction.
  A transaction abort will leave the body in storage.
  There is no simple way to remove such bodies from storage.

- Truncation:

  Truncation of a stream with File API A will truncate the header and body.

  Truncation with File API B will be more complicated since
  headers and bodies must be handled separately.
  This requires some investigation.

## Other Possible Features

Feedback is appreciated on the possible features below.

- Writer concurrency: A single writer can have multiple OutputStreams "open" and write to them concurrently from
  different threads.

- The new method `CompletableFuture<EventPointer> writeEventAndReturnPointer(String routingKey, ByteBuffer... event)`
  will return a future to an `EventPointer`. The `EventPointer` can be persisted and used to read the event at any time
  in the future using `fetchEvent`. Similar methods will be available for all APIs that write events.

- The new method `CompletableFuture<Void> writeEvent(String routingKey, ByteBuffer... event)`
  accepts an unlimited sequence of ByteBuffers. These will be concatenated to build a single event.

## Implementation Ideas

See [Implementation Ideas](ImplementationIdeas.md).
