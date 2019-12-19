package io.pravega.client.fileStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.EventRead;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class SampleUsage {

    void SampleWriter() throws Exception {
        final FileStreamWriter writer = fileStreamClientFactory.createWriter(streamName, config);

        // Define our byte arrays. They can be up to 2 GB in size (maximum JVM array size).
        byte[] data1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        byte[] data2 = new byte[] { 10, 11, 12 };

        // Write byte arrays and commit.
        FileOutputStream os1 = writer.beginWriteEvent("routingKey1");
        // beginWriteEvent does not perform any Pravega RPCs.
        os1.write(data1);
        os1.write(data2);
        // No data is available to readers yet.
        os1.close();
        // The entire event may now be available to readers but this is not guaranteed.
        writer.flush();
        // Now the entire event is guaranteed to be available to readers.

        // We can also use the standard event API for events that fit in client memory.
        // Events can be of unlimited size.
        CompletableFuture<Void> future1 = writer.writeEvent("routingKey1", ByteBuffer.wrap(data1));

        // Copy large file from file system to the Pravega stream.
        // The size is unlimited (more than 2 GB).
        InputStream is2 = new java.io.FileInputStream("/tmp/file2");
        FileOutputStream os2 = writer.beginWriteEvent("routingKey2");
        IOUtils.copyLarge(is2, os2);
        is2.close();
        // Close the OutputStream and get the EventPointer so that we can read this specific event later.
        EventPointer ptr1 = os2.closeAndReturnEventPointer().get();
        // TODO: Serialize EventPointer and persist it.

        // Write files 3 and 4. Both files will be open at the same time.
        // They will be written as separate transactions and will NOT be interleaved.
        FileOutputStream os3 = writer.beginWriteEvent("routingKey3");
        FileOutputStream os4 = writer.beginWriteEvent("routingKey4");
        os3.write(data1);
        os4.write(data2);
        os3.write(data2);
        os4.write(data1);
        CompletableFuture<Void> future4 = os4.closeAndReturnReadabilityFuture();
        CompletableFuture<Void> future3 = os3.closeAndReturnReadabilityFuture();
        future4.get();
        future3.get();
    }

    /**
     * This demonstrates reading all of the files written by SampleWriter.
     * It will read files 1, 2, 4, 3.
     */
    void SampleReader() throws Exception {
        final long timeout = 1000;
        FileStreamReader reader = fileStreamClientFactory.createReader(readerId, readerGroup, config);
        for (int i = 0 ;; i++) {
            EventRead<FileInputStream> eventRead = reader.readNextEventAsStream(timeout);
            FileInputStream inputStream = eventRead.getEvent();
            if (inputStream != null) {
                // Copy event contents to a normal file.
                OutputStream outputStream = new java.io.FileOutputStream("/tmp/file" + i);
                IOUtils.copyLarge(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            }
        }
    }

    static class MyEventHeader {
        int metadata1;
        String metadata2;

        public MyEventHeader(int metadata1, String metadata2) {
            this.metadata1 = metadata1;
            this.metadata2 = metadata2;
        }
    }

    /**
     * This demonstrates how to write a JSON header followed by the body that will come from a FileInputStream.
     */
    void SampleHeaderBodyWriter() throws Exception {
        final FileStreamWriter writer = fileStreamClientFactory.createWriter(streamName, config);
        ObjectMapper jsonSerializer = new ObjectMapper();
        FileOutputStream os1 = writer.beginWriteEvent("routingKey1");
        MyEventHeader header1 = new MyEventHeader(123, "123");
        jsonSerializer.writeValue(os1, header1);
        InputStream is1 = new java.io.FileInputStream("/tmp/body1");
        IOUtils.copyLarge(is1, os1);
        is1.close();
        EventPointer ptr1 = os1.closeAndReturnEventPointer().get();
        writer.flush();
    }

    /**
     * This demonstrates how to read a JSON header followed by the body that will be written to a FileOutputStream.
     */
    void SampleHeaderBodyReader() throws Exception {
        final long timeout = 1000;
        FileStreamReader reader = fileStreamClientFactory.createReader(readerId, readerGroup, config);
        ObjectMapper jsonSerializer = new ObjectMapper();
        for (int i = 0 ;; i++) {
            EventRead<FileInputStream> eventRead = reader.readNextEventAsStream(timeout);
            FileInputStream inputStream = eventRead.getEvent();
            if (inputStream != null) {
                MyEventHeader header = jsonSerializer.readValue(inputStream, MyEventHeader.class);
                // Copy the rest of the event contents to a normal file.
                OutputStream outputStream = new java.io.FileOutputStream("/tmp/body" + header.metadata1);
                IOUtils.copyLarge(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            }
        }
    }

    /**
     * This demonstrates the ability to write events concurrently from multiple threads.
     */
    void SampleConcurrentWriter() throws Exception {
        final FileStreamWriter writer = fileStreamClientFactory.createWriter(streamName, config);

        // Copy 2 large files to the Pravega stream concurrently.
        final ExecutorService executor = new ForkJoinPool();
        executor.submit(() -> {
            InputStream is = new java.io.FileInputStream("/tmp/file5");
            FileOutputStream os = writer.beginWriteEvent("routingKey5");
            final long bytesCopied = IOUtils.copyLarge(is, os);
            is.close();
            os.close();
            return bytesCopied;
        });
        executor.submit(() -> {
            InputStream is = new java.io.FileInputStream("/tmp/file6");
            FileOutputStream os = writer.beginWriteEvent("routingKey6");
            final long bytesCopied = IOUtils.copyLarge(is, os);
            is.close();
            os.close();
            return bytesCopied;
        });
    }

    /**
     * This demonstrates the ability to read the content of multiple events concurrently from multiple threads.
     */
    void SampleConcurrentReader() throws Exception {
        final long timeout = 1000;
        final ExecutorService executor = new ForkJoinPool();
        FileStreamReader reader = fileStreamClientFactory.createReader(readerId, readerGroup, config);
        for (int i = 0 ;; i++) {
            EventRead<FileInputStream> eventRead = reader.readNextEventAsStream(timeout);
            FileInputStream inputStream = eventRead.getEvent();
            if (inputStream != null) {
                OutputStream outputStream = new java.io.FileOutputStream("/tmp/file" + i);
                // Submit a task to copy the event contents to a file.
                executor.submit(() -> {
                    final long bytesCopied = IOUtils.copyLarge(inputStream, outputStream);
                    inputStream.close();
                    outputStream.close();
                    return bytesCopied;
                });
            }
        }
    }

    /**
     * This demonstrates reading a single historical event using an EventPointer.
     */
    void SampleRandomReader() throws Exception {
        FileStreamReader reader = fileStreamClientFactory.createReader(readerId, readerGroup, config);
        byte[] serializedEventPointer = null;   // TODO: read serialized EventPointer
        EventPointer ptr1 = EventPointer.fromBytes(ByteBuffer.wrap(serializedEventPointer));
        FileInputStream inputStream = reader.fetchEventAsStream(ptr1);
        OutputStream outputStream = new java.io.FileOutputStream("/tmp/file1");
        IOUtils.copyLarge(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    void SampleTransactionalWriter() throws Exception {
        final TransactionalFileStreamWriter writer = fileStreamClientFactory.createTransactionalEventWriter(writerId, streamName, config);

        byte[] data1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        byte[] data2 = new byte[] { 10, 11, 12 };
        byte[] data3 = new byte[] { 13, 14 };

        FileTransaction tx1 = writer.beginTxn();
        TransactionalFileOutputStream os1 = tx1.beginWriteEvent("routingKey1");
        os1.write(data1);
        os1.write(data2);
        os1.close();
        // No data is available to readers yet.
        tx1.commit();
        // Now the entire event is guaranteed to be durably persisted. It may not be immediately available to readers.

        // We can also use the standard event API for events that fit in client memory.
        // Within a single transaction, writeEvent and beginWriteEvent can both be called any number of times.
        FileTransaction tx2 = writer.beginTxn();
        tx2.writeEvent("routingKey1", ByteBuffer.wrap(data1));
        TransactionalFileOutputStream os2 = tx2.beginWriteEvent("routingKey3");
        os2.write(data3);
        os2.close();
        tx2.commit();

        // We can also get EventPointers after committing the transaction.
        FileTransaction tx3 = writer.beginTxn();
        CompletableFuture<EventPointer> future3 = tx3.writeEventAndReturnPointer("routingKey1", ByteBuffer.wrap(data1));
        TransactionalFileOutputStream os3 = tx3.beginWriteEvent("routingKey3");
        os3.write(data2);
        CompletableFuture<EventPointer> future4 = os3.closeAndReturnEventPointer();
        tx3.commit();
        EventPointer ptr3 = future3.get();
        EventPointer ptr4 = future4.get();
    }

}
