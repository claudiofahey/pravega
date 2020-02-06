/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.pravega.client.schema.example.avro;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

/** Demonstrates a union type. An event must contain exactly one of TemperatureData, VibrationData, or PressureData. */
@org.apache.avro.specific.AvroGenerated
public class SensorData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 6255266828930615164L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SensorData\",\"namespace\":\"io.pravega.client.schema.example.avro\",\"doc\":\"Demonstrates a union type. An event must contain exactly one of TemperatureData, VibrationData, or PressureData.\",\"fields\":[{\"name\":\"timestamp\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"},\"doc\":\"Event time\"},{\"name\":\"device_id\",\"type\":\"string\",\"doc\":\"This is used as the routing key for the stream so it must exist for all types.\"},{\"name\":\"data\",\"type\":[{\"type\":\"record\",\"name\":\"TemperatureData\",\"fields\":[{\"name\":\"temp_celsius\",\"type\":\"double\"}]},{\"type\":\"record\",\"name\":\"VibrationData\",\"fields\":[{\"name\":\"vibration1\",\"type\":\"double\"},{\"name\":\"vibration2\",\"type\":[\"null\",\"double\"]}]},{\"type\":\"record\",\"name\":\"PressureData\",\"fields\":[{\"name\":\"pressure\",\"type\":\"double\"}]}]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();
static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.data.TimeConversions.TimestampMillisConversion());
  }

  private static final BinaryMessageEncoder<SensorData> ENCODER =
      new BinaryMessageEncoder<SensorData>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<SensorData> DECODER =
      new BinaryMessageDecoder<SensorData>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<SensorData> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<SensorData> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<SensorData> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<SensorData>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this SensorData to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a SensorData from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a SensorData instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static SensorData fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** Event time */
   private java.time.Instant timestamp;
  /** This is used as the routing key for the stream so it must exist for all types. */
   private java.lang.CharSequence device_id;
   private java.lang.Object data;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public SensorData() {}

  /**
   * All-args constructor.
   * @param timestamp Event time
   * @param device_id This is used as the routing key for the stream so it must exist for all types.
   * @param data The new value for data
   */
  public SensorData(java.time.Instant timestamp, java.lang.CharSequence device_id, java.lang.Object data) {
    this.timestamp = timestamp.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
    this.device_id = device_id;
    this.data = data;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return timestamp;
    case 1: return device_id;
    case 2: return data;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  private static final org.apache.avro.Conversion<?>[] conversions =
      new org.apache.avro.Conversion<?>[] {
      new org.apache.avro.data.TimeConversions.TimestampMillisConversion(),
      null,
      null,
      null
  };

  @Override
  public org.apache.avro.Conversion<?> getConversion(int field) {
    return conversions[field];
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: timestamp = (java.time.Instant)value$; break;
    case 1: device_id = (java.lang.CharSequence)value$; break;
    case 2: data = value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'timestamp' field.
   * @return Event time
   */
  public java.time.Instant getTimestamp() {
    return timestamp;
  }


  /**
   * Sets the value of the 'timestamp' field.
   * Event time
   * @param value the value to set.
   */
  public void setTimestamp(java.time.Instant value) {
    this.timestamp = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
  }

  /**
   * Gets the value of the 'device_id' field.
   * @return This is used as the routing key for the stream so it must exist for all types.
   */
  public java.lang.CharSequence getDeviceId() {
    return device_id;
  }


  /**
   * Sets the value of the 'device_id' field.
   * This is used as the routing key for the stream so it must exist for all types.
   * @param value the value to set.
   */
  public void setDeviceId(java.lang.CharSequence value) {
    this.device_id = value;
  }

  /**
   * Gets the value of the 'data' field.
   * @return The value of the 'data' field.
   */
  public java.lang.Object getData() {
    return data;
  }


  /**
   * Sets the value of the 'data' field.
   * @param value the value to set.
   */
  public void setData(java.lang.Object value) {
    this.data = value;
  }

  /**
   * Creates a new SensorData RecordBuilder.
   * @return A new SensorData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.SensorData.Builder newBuilder() {
    return new io.pravega.client.schema.example.avro.SensorData.Builder();
  }

  /**
   * Creates a new SensorData RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new SensorData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.SensorData.Builder newBuilder(io.pravega.client.schema.example.avro.SensorData.Builder other) {
    if (other == null) {
      return new io.pravega.client.schema.example.avro.SensorData.Builder();
    } else {
      return new io.pravega.client.schema.example.avro.SensorData.Builder(other);
    }
  }

  /**
   * Creates a new SensorData RecordBuilder by copying an existing SensorData instance.
   * @param other The existing instance to copy.
   * @return A new SensorData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.SensorData.Builder newBuilder(io.pravega.client.schema.example.avro.SensorData other) {
    if (other == null) {
      return new io.pravega.client.schema.example.avro.SensorData.Builder();
    } else {
      return new io.pravega.client.schema.example.avro.SensorData.Builder(other);
    }
  }

  /**
   * RecordBuilder for SensorData instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<SensorData>
    implements org.apache.avro.data.RecordBuilder<SensorData> {

    /** Event time */
    private java.time.Instant timestamp;
    /** This is used as the routing key for the stream so it must exist for all types. */
    private java.lang.CharSequence device_id;
    private java.lang.Object data;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.pravega.client.schema.example.avro.SensorData.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[0].schema(), other.timestamp);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.device_id)) {
        this.device_id = data().deepCopy(fields()[1].schema(), other.device_id);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.data)) {
        this.data = data().deepCopy(fields()[2].schema(), other.data);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
    }

    /**
     * Creates a Builder by copying an existing SensorData instance
     * @param other The existing instance to copy.
     */
    private Builder(io.pravega.client.schema.example.avro.SensorData other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[0].schema(), other.timestamp);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.device_id)) {
        this.device_id = data().deepCopy(fields()[1].schema(), other.device_id);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.data)) {
        this.data = data().deepCopy(fields()[2].schema(), other.data);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'timestamp' field.
      * Event time
      * @return The value.
      */
    public java.time.Instant getTimestamp() {
      return timestamp;
    }


    /**
      * Sets the value of the 'timestamp' field.
      * Event time
      * @param value The value of 'timestamp'.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder setTimestamp(java.time.Instant value) {
      validate(fields()[0], value);
      this.timestamp = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'timestamp' field has been set.
      * Event time
      * @return True if the 'timestamp' field has been set, false otherwise.
      */
    public boolean hasTimestamp() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'timestamp' field.
      * Event time
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder clearTimestamp() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'device_id' field.
      * This is used as the routing key for the stream so it must exist for all types.
      * @return The value.
      */
    public java.lang.CharSequence getDeviceId() {
      return device_id;
    }


    /**
      * Sets the value of the 'device_id' field.
      * This is used as the routing key for the stream so it must exist for all types.
      * @param value The value of 'device_id'.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder setDeviceId(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.device_id = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'device_id' field has been set.
      * This is used as the routing key for the stream so it must exist for all types.
      * @return True if the 'device_id' field has been set, false otherwise.
      */
    public boolean hasDeviceId() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'device_id' field.
      * This is used as the routing key for the stream so it must exist for all types.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder clearDeviceId() {
      device_id = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'data' field.
      * @return The value.
      */
    public java.lang.Object getData() {
      return data;
    }


    /**
      * Sets the value of the 'data' field.
      * @param value The value of 'data'.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder setData(java.lang.Object value) {
      validate(fields()[2], value);
      this.data = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'data' field has been set.
      * @return True if the 'data' field has been set, false otherwise.
      */
    public boolean hasData() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'data' field.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.SensorData.Builder clearData() {
      data = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SensorData build() {
      try {
        SensorData record = new SensorData();
        record.timestamp = fieldSetFlags()[0] ? this.timestamp : (java.time.Instant) defaultValue(fields()[0]);
        record.device_id = fieldSetFlags()[1] ? this.device_id : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.data = fieldSetFlags()[2] ? this.data :  defaultValue(fields()[2]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<SensorData>
    WRITER$ = (org.apache.avro.io.DatumWriter<SensorData>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<SensorData>
    READER$ = (org.apache.avro.io.DatumReader<SensorData>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










