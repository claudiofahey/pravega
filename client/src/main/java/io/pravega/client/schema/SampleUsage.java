package io.pravega.client.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.schema.example.avro.PressureData;
import io.pravega.client.schema.example.avro.SensorData;
import io.pravega.client.schema.example.avro.TemperatureData;
import io.pravega.client.schema.example.avro.User;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.Serializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.File;
import java.net.URI;
import java.time.Instant;

public class SampleUsage {
    final URI schemaRegistryURI = URI.create("https://schema-registry.example.com");

    /**
     * Jackson uses JSON.
     */
    void DynamicReaderUsingJackson() {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<ObjectNode> deserializer = serializerFactory.createJacksonObjectNodeDeserializer();
        final EventStreamReader<ObjectNode> reader = clientFactory.createReader("readerId", "readerGroup",
                deserializer, ReaderConfig.builder().build());
        final EventRead<ObjectNode> eventRead = reader.readNextEvent(1000);
        final ObjectNode objectNode = eventRead.getEvent();
        System.out.println(objectNode);
    }

    void DynamicWriterUsingJackson() {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<ObjectNode> serializer = serializerFactory.createJacksonObjectNodeSerializer(
                SerializerFactory.CompressionType.Snappy);
        final EventStreamWriter<ObjectNode> writer = clientFactory.createEventWriter("writerId", "streamName",
                serializer, EventWriterConfig.builder().build());
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode objectNode = mapper.createObjectNode();
        objectNode.set("field1", mapper.convertValue("value1", JsonNode.class));
        writer.writeEvent("routingKey", objectNode);
    }

    /**
     * See https://avro.apache.org/docs/current/gettingstartedjava.html
     */
    void DynamicReaderUsingAvro() {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<GenericRecord> deserializer = serializerFactory.createAvroGenericRecordDeserializer();
        final EventStreamReader<GenericRecord> reader = clientFactory.createReader("readerId", "readerGroup",
                deserializer, ReaderConfig.builder().build());
        final EventRead<GenericRecord> eventRead = reader.readNextEvent(1000);
        final GenericRecord genericRecord = eventRead.getEvent();
        System.out.println(genericRecord);
    }

    void DynamicWriterUsingAvro() throws Exception {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<GenericRecord> serializer = serializerFactory.createAvroGenericRecordSerializer(
                SerializerFactory.CompressionType.Snappy,
                SerializerFactory.CompatibilityStrategy.Full);
        final EventStreamWriter<GenericRecord> writer = clientFactory.createEventWriter("writerId", "streamName",
                serializer, EventWriterConfig.builder().build());

        // Write a user object.
        final Schema userSchema = new Schema.Parser().parse(new File("user.avsc"));
        final GenericRecord user1 = new GenericData.Record(userSchema);
        user1.put("name", "Alyssa");
        user1.put("favorite_number", 256);
        writer.writeEvent("routingKey", user1);

        // Write a machine object.
        final Schema machineSchema = new Schema.Parser().parse(new File("machine.avsc"));
        final GenericRecord machine1 = new GenericData.Record(machineSchema);
        user1.put("name", "machine1");
        writer.writeEvent("routingKey", machine1);

        // TODO: How can a static reader read multiple types of events?
        // TODO: How can a static writer write multiple types of events?
    }

    void StaticReaderUsingAvro() {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<User> deserializer = serializerFactory.createAvroDeserializer(User.class);
        final EventStreamReader<User> reader = clientFactory.createReader("readerId", "readerGroup",
                deserializer, ReaderConfig.builder().build());
        final EventRead<User> eventRead = reader.readNextEvent(1000);
        final User user = eventRead.getEvent();
        System.out.println(user);
    }

    void StaticWriterUsingAvro() throws Exception {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<User> serializer = serializerFactory.createAvroSerializer(
                User.class,
                SerializerFactory.CompressionType.Snappy,
                SerializerFactory.CompatibilityStrategy.Full);
        final EventStreamWriter<User> writer = clientFactory.createEventWriter("writerId", "streamName",
                serializer, EventWriterConfig.builder().build());
        final User user1 = new User("Ben", 7, "red");
        writer.writeEvent("routingKey", user1);
    }

    void StaticWriterUsingAvroUnion() throws Exception {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<SensorData> serializer = serializerFactory.createAvroSerializer(
                SensorData.class,
                SerializerFactory.CompressionType.Snappy,
                SerializerFactory.CompatibilityStrategy.Full);
        final EventStreamWriter<SensorData> writer = clientFactory.createEventWriter("writerId", "streamName",
                serializer, EventWriterConfig.builder().build());

        // Write TemperatureData
        final SensorData sensorData1 = SensorData.newBuilder()
                .setTimestamp(Instant.now())
                .setDeviceId("1234")
                .setData(TemperatureData.newBuilder()
                        .setTempCelsius(100.0)
                        .build())
                .build();
        writer.writeEvent(sensorData1.getDeviceId().toString(), sensorData1);

        // Write PressureData
        final SensorData sensorData2 = SensorData.newBuilder()
                .setTimestamp(Instant.now())
                .setDeviceId("1234")
                .setData(PressureData.newBuilder()
                        .setPressure(11.0)
                        .build())
                .build();
        writer.writeEvent(sensorData2.getDeviceId().toString(), sensorData2);
    }

    void StaticReaderUsingAvroUnion() {
        final EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope("scope",
                ClientConfig.builder().build());
        final SerializerFactory serializerFactory = new SerializerFactoryImpl(schemaRegistryURI);
        final Serializer<SensorData> deserializer = serializerFactory.createAvroDeserializer(SensorData.class);
        final EventStreamReader<SensorData> reader = clientFactory.createReader("readerId", "readerGroup",
                deserializer, ReaderConfig.builder().build());
        final EventRead<SensorData> eventRead = reader.readNextEvent(1000);
        final SensorData sensorData = eventRead.getEvent();
        final Object data = sensorData.getData();
        if (data instanceof TemperatureData) {
            final TemperatureData tempData = (TemperatureData) data;
            System.out.println(tempData.getTempCelsius());
        } else if (data instanceof PressureData) {
            final PressureData pressureData = (PressureData) data;
            System.out.println(pressureData.getPressure());
        }

        // TODO: Need to determine whether schemas are compatible when adding types to the union field.
    }


}
