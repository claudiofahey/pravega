package io.pravega.client.fileStream;

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

        //
        // Write byte arrays and commit.
        //
        FileOutputStream os1 = writer.beginWriteEvent("routingKey1");
        // beginWriteEvent does not perform any Pravega RPCs.
        os1.write(data1);
        // No data is available to readers yet.
        os1.write(data2);
        // No data is available to readers yet.
        os1.close();
        // Now the entire event is available to readers.

        // We can also using the standard event API for events that fit in client memory.
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
        os4.close();    // this commits file 4 to the Pravega stream
        os3.close();    // this commits file 3 to the Pravega stream
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
     * This demonstrates the ability to read event contents concurrently from multiple threads.
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

}
