package io.pravega.client.fileStream;

import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.Transaction;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface FileTransaction extends Transaction<ByteBuffer> {
    /**
     * Initiate writing an event to the stream.
     * This will return a {@link TransactionalFileOutputStream} that can be used to write the event contents.
     * The event will not be made available to readers until {@link FileOutputStream#close} is called.
     * This method performs no I/O to Pravega.
     *
     * For a single transaction, only one FileOutputStream may be open for writing.
     *
     * @return A {@link TransactionalFileOutputStream} that can be used to write the event content.
     */
    TransactionalFileOutputStream beginWriteEvent(String routingKey);
    TransactionalFileOutputStream beginWriteEvent();

    /**
     * Write a single event whose content will be the concatenation of one or more ByteBuffers.
     *
     * @return A future that will complete when the {@link EventPointer} becomes available and
     * can be used to immediately read the event.
     * This will occur only after {@link FileTransaction#commit} is called.
     */
    CompletableFuture<EventPointer> writeEventAndReturnPointer(String routingKey, ByteBuffer... event);
    CompletableFuture<EventPointer> writeEventAndReturnPointer(ByteBuffer... event);
}
