package io.pravega.client.fileStream;

import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * This demonstrates various patterns for using serialization classes that
 * work on InputStreams and OutputStreams.
 * These patterns do not work well for data that is not in memory.
 */
public class SampleUsage2 {

    /**
     * A standard POJO class that must fit completely in memory.
     * This is easy and elegant to handle.
     */
    static class MyEventA {
        // Header
        int metadata1;
        int metadata2;
        // Body
        byte[] body;
    }

    /**
     * Serializers that use InputStream and OutputStream instead of ByteBuffer.
     */
    static class MyEventASerializer {
        public void serialize(OutputStream out, MyEventA value) throws IOException {
            out.write(value.metadata1);
            out.write(value.metadata2);
            out.write(value.body);
        }

        public MyEventA deserialize(InputStream src) throws IOException {
            MyEventA evt = new MyEventA();
            evt.metadata1 = src.read();
            evt.metadata2 = src.read();
            evt.body = IOUtils.toByteArray(src);
            return evt;
        }
    }

    /**
     * Sample writer application using MyEventA.
     */
    void SampleWriterA() throws Exception {
        EventStreamWriter<MyEventA> writer = clientFactory.createEventWriter(
                streamName,
                new MyEventASerializer(),
                config);
        MyEventA evt1 = new MyEventA();
        CompletableFuture<Void> future1 = writer.writeEvent("routingKey1", evt1);
        writer.close();
    }

    /**
     * Sample reader application using MyEventA.
     */
    void SampleReaderA() throws Exception {
        final long timeout = 1000;
        EventStreamReader<MyEventA> reader = clientFactory.createReader(readerId, readerGroup, new MyEventASerializer(), config);
        for (int i = 0 ;; i++) {
            EventRead<MyEventA> eventRead = reader.readNextEvent(timeout);
            if (eventRead.getEvent() != null) {
                System.out.print(eventRead.getEvent());
            }
        }
    }

    /**
     * Like MyEventA but the data is accessed as an InputStream or OutputStream.
     * The data does not need to fit in memory and can be a file, TCP socket, GZIPOutputStream, CipherOutputStream, etc.
     */
    static class MyEventB {
        // Header
        int metadata1;
        int metadata2;
        // Body
        // Ugly: We must use a different type depending on whether we intend to read or write the body.
        InputStream bodyIn;
        OutputStream bodyOut;
    }

    /**
     * The first attempt at a serializer and deserializer.
     */
    static class MyEventBSerializer1 {
        public void serialize(OutputStream out, MyEventB value) throws IOException {
            out.write(value.metadata1);
            out.write(value.metadata2);
            IOUtils.copyLarge(value.bodyIn, out);
        }

        /**
         * This deserializer doesn't work because we don't have enough information to create the OutputStream.
         */
        public MyEventB deserialize(InputStream src) throws IOException {
            MyEventB value = new MyEventB();
            value.metadata1 = src.read();
            value.metadata2 = src.read();
            // FATAL PROBLEM: What do we set bodyOut to?
            value.bodyOut = ???;
            IOUtils.copyLarge(src, value.bodyOut);
            return value;
        }
    }

    /**
     * Sample writer application using MyEventBSerializer1.
     */
    void SampleWriterB1() throws Exception {
        EventStreamWriter<MyEventB> writer = clientFactory.createEventWriter(
                streamName,
                new MyEventBSerializer1(),
                config);
        MyEventB evt2 = new MyEventB();
        evt2.metadata1 = 1;
        evt2.metadata2 = 2;
        evt2.bodyIn = new java.io.FileInputStream("/tmp/file2");
        CompletableFuture<Void> future2 = writer.writeEvent("routingKey1", evt2);
        evt2.bodyIn.close();
        writer.close();
    }

    /**
     * This deserializer works better but requires the output instance to be provided to deserialize().
     */
    static class MyEventBSerializer2 {
        public void deserialize(InputStream src, MyEventB value) throws IOException {
            value.metadata1 = src.read();
            value.metadata2 = src.read();
            IOUtils.copyLarge(src, value.bodyOut);
        }
    }

    /**
     * Sample reader application using MyEventBSerializer2.
     * We must pass an initialized instance of MyEventB to readNextEvent.
     */
    void SampleReaderB2() throws Exception {
        final long timeout = 1000;
        EventStreamReader<MyEventB> reader = clientFactory.createReader(readerId, readerGroup, new MyEventBSerializer2(), config);
        MyEventB event = new MyEventB();
        for (int i = 0 ;; i++) {
            event.bodyOut = new java.io.FileOutputStream("/tmp/file" + i);
            EventRead<MyEventB> eventRead = reader.readNextEvent(timeout, event);
            if (eventRead.getEvent() != null) {
                System.out.print(eventRead.getEvent());
            }
            event.bodyOut.close();
        }
    }

    /**
     * This deserializer uses an instance variable to store the event that will be returned.
     * This is not thread-safe.
     */
    static class MyEventBSerializer3 {
        public MyEventB event;
        public MyEventB deserialize(InputStream src) throws IOException {
            event.metadata1 = src.read();
            event.metadata2 = src.read();
            IOUtils.copyLarge(src, event.bodyOut);
            return event;
        }
    }

    /**
     * Sample reader application using MyEventBSerializer3.
     */
    void SampleReaderB3() throws Exception {
        final long timeout = 1000;
        MyEventBSerializer3 serializer = new MyEventBSerializer3();
        serializer.event = new MyEventB();
        EventStreamReader<MyEventB> reader = clientFactory.createReader(readerId, readerGroup, serializer, config);
        for (int i = 0 ;; i++) {
            serializer.event.bodyOut = new java.io.FileOutputStream("/tmp/file" + i);
            EventRead<MyEventB> eventRead = reader.readNextEvent(timeout);
            if (eventRead.getEvent() != null) {
                System.out.print(eventRead.getEvent());
            }
            serializer.event.bodyOut.close();
        }
    }
}
