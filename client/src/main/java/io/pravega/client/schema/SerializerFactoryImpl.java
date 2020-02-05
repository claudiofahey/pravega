package io.pravega.client.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pravega.client.stream.Serializer;
import org.apache.avro.generic.GenericRecord;

import java.net.URI;

public class SerializerFactoryImpl implements SerializerFactory{
    public SerializerFactoryImpl(URI schemaRegistryURI) {
    }

    @Override
    public Serializer<ObjectNode> createJacksonObjectNodeSerializer(CompressionType compressionType) {
        return null;
    }

    @Override
    public Serializer<ObjectNode> createJacksonObjectNodeDeserializer() {
        return null;
    }

    @Override
    public Serializer<GenericRecord> createAvroGenericRecordSerializer(CompressionType compressionType, CompatibilityStrategy compatibility) {
        return null;
    }

    @Override
    public Serializer<GenericRecord> createAvroGenericRecordDeserializer() {
        return null;
    }

    @Override
    public <T> Serializer<T> createAvroSerializer(Class<T> c, CompressionType compressionType, CompatibilityStrategy compatibility) {
        return null;
    }

    @Override
    public <T> Serializer<T> createAvroDeserializer(Class<T> c) {
        return null;
    }
}
