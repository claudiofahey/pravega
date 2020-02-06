package io.pravega.client.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pravega.client.stream.Serializer;
import io.pravega.client.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.Map;

/**
 * TODO: Consider splitting this class so that large dependencies (Jackson, Avro, Protobuf) can be optionally loaded.
 */

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

    enum ReaderSchemaStrategy {
        /**
         * The first version of the schema in the schema registry will be used.
         * */
        UseOldestWriterSchema,
        /**
         * The most recent version of the schema in the schema registry will be used.
         * */
        UseNewestWriterSchema,
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
     *
     * Each event will be deserialized using the Avro schema that the event was written with.
     * Schemas returned by different readNextEvent calls may vary.
     */
    Serializer<GenericRecord> createAvroGenericRecordDeserializer();

    /**
     * Get a matching Avro schema from the schema registry.
     */
    Schema getAvroSchemaFromRegistry(
            Stream stream,
            String eventType,
            ReaderSchemaStrategy readerSchemaStrategy);

    /**
     * This will return a Serializer that can deserialize the following types of events:
     *  - Events encoded as plain UTF-8 JSON objects
     *  - Events with a schema registry header and SchemaType JSON
     *  - Events with a schema registry header and SchemaType Avro
     * (Some conversions may not be implemented.)
     *
     * Each event will be deserialized using the provided Avro reader schema
     * Schemas returned by different readNextEvent calls will always be the same.
     */
    Serializer<GenericRecord> createAvroGenericRecordDeserializer(
            Schema readerSchema);

    /**
     * This will return a Serializer that will serialize events with a schema registry header and SchemaType Avro.
     */
    <T> Serializer<T> createAvroSerializer(
            Class<T> c,
            Stream stream,
            String eventType,
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
