package io.pravega.client.fileStream;

import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.Transaction;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface FileTransaction extends Transaction<ByteBuffer> {
    FileOutputStream beginWriteEvent(String routingKey);
    FileOutputStream beginWriteEvent();
    CompletableFuture<EventPointer> writeEventAndReturnPointer(String routingKey, ByteBuffer... event);
    CompletableFuture<EventPointer> writeEventAndReturnPointer(ByteBuffer... event);
}
