package io.pravega.client.fileStream;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventPointer;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.Serializer;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public class SampleUsage {

    /**
     * Sample event with a header and an EventPointer to a body.
     */
    static class MyEvent {
        // Header
        int metadata1;
        int metadata2;
        // Body
        EventPointer bodyEventPointer;

        public MyEvent(int metadata1, int metadata2, EventPointer bodyEventPointer) {
            this.metadata1 = metadata1;
            this.metadata2 = metadata2;
            this.bodyEventPointer = bodyEventPointer;
        }
    }

    /**
     * Sample writer application using MyEvent.
     */
    void SampleWriter() throws Exception {
        final String scope = "scope1";
        final String streamName = "stream1";
        final ClientConfig clientConfig = ClientConfig.builder().build();
        final EventWriterConfig eventWriterConfig = EventWriterConfig.builder().build();
        // TODO: JSON serializer needs to handle EventPointer.
        final Serializer<MyEvent> serializer = new JsonSerializer(MyEvent.class);
        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
             EventStreamWriter<MyEvent> writer = clientFactory.createEventWriter(streamName, serializer, eventWriterConfig)) {
            for (int i = 0 ; i < 10 ; i++) {
                String routingKey = "routingKey" + i;
                // Write body to Pravega using an OutputStream..
                EventPointer bodyPtr;
                try (InputStream is = new java.io.FileInputStream("/tmp/file" + i);
                     FileOutputStream os = writer.createFile(routingKey)) {  // NEW API HERE
                    IOUtils.copyLarge(is, os);
                    // Get EventPointer to body.
                    bodyPtr = os.seal();
                }
                // Write event (header) containing the EventPointer to the body.
                MyEvent evt = new MyEvent(i, 123, bodyPtr);
                CompletableFuture<Void> future1 = writer.writeEvent(routingKey, evt);
            }
        }
    }

    /**
     * Sample reader application using MyEvent.
     */
    void SampleReader() throws Exception {
        final String scope = "scope1";
        final String streamName = "stream1";
        final ClientConfig clientConfig = ClientConfig.builder().build();
        final long timeout = 1000;
        final String readerId = "1";
        final String readerGroup = "1";
        final Serializer<MyEvent> serializer = new JsonSerializer(MyEvent.class);
        final ReaderConfig readerConfig = ReaderConfig.builder().build();
        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
             EventStreamReader<MyEvent> reader = clientFactory.createReader(readerId, readerGroup, serializer, readerConfig)) {
            for (;;) {
                final EventRead<MyEvent> eventRead = reader.readNextEvent(timeout);
                final MyEvent evt = eventRead.getEvent();
                if (evt != null) {
                    try (FileInputStream is = clientFactory.readFile(evt.bodyEventPointer);  // NEW API HERE
                         OutputStream os = new java.io.FileOutputStream("/tmp/file" + evt.metadata1)) {
                        IOUtils.copyLarge(is, os);
                    }
                }
            }
        }
    }
}
