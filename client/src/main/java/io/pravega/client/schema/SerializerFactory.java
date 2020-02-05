package io.pravega.client.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pravega.client.stream.Serializer;
import org.apache.avro.generic.GenericRecord;

import java.util.Map;

public interface SerializerFactory {
    enum CompressionType {
        None,
        Snappy,
        Gzip
    }

    enum CompatibilityStrategy {
        Forward,
        Full
    }

    /**
     * This will return a Serializer that will serialize events with a schema registry header and SchemaType JSON.
     */
    Serializer<ObjectNode> createJacksonObjectNodeSerializer(
            CompressionType compressionType);

    /**
     * This will return a Serializer that can deserialize the following types of events:
     *  - Events encoded as plain UTF-8 JSON objects
     *  - Events with a schema registry header and SchemaType JSON
     *  - Events with a schema registry header and SchemaType Avro
     * (Some conversions may not be implemented.)
     */
    Serializer<ObjectNode> createJacksonObjectNodeDeserializer();

    /**
     * This will return a Serializer that will serialize events with a schema registry header and SchemaType Avro.
     */
    Serializer<GenericRecord> createAvroGenericRecordSerializer(
            CompressionType compressionType,
            CompatibilityStrategy compatibility);

    /**
     * This will return a Serializer that can deserialize the following types of events:
     *  - Events encoded as plain UTF-8 JSON objects
     *  - Events with a schema registry header and SchemaType JSON
     *  - Events with a schema registry header and SchemaType Avro
     * (Some conversions may not be implemented.)
     */
    Serializer<GenericRecord> createAvroGenericRecordDeserializer();

    /**
     * This will return a Serializer that will serialize events with a schema registry header and SchemaType Avro.
     */
    <T> Serializer<T> createAvroSerializer(
            Class<T> c,
            CompressionType compressionType,
            CompatibilityStrategy compatibility);

    /**
     * This will return a Serializer that can deserialize the following types of events:
     *  - Events with a schema registry header and SchemaType Avro
     * The Avro schema used to write the event must be compatible with the Avro
     * schema used to generate the class T.
     */
    <T> Serializer<T> createAvroDeserializer(Class<T> c);
}
