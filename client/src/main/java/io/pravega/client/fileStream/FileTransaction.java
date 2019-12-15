package io.pravega.client.fileStream;

import io.pravega.client.stream.Transaction;

import java.nio.ByteBuffer;

public interface FileTransaction extends Transaction<ByteBuffer> {
    FileOutputStream beginWriteEvent(String routingKey);
    FileOutputStream beginWriteEvent();
    // TODO: How can caller get EventPointers for all events committed in this transaction?
}
