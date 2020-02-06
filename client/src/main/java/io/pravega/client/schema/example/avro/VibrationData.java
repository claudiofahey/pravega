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

@org.apache.avro.specific.AvroGenerated
public class VibrationData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 6960174017105936874L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"VibrationData\",\"namespace\":\"io.pravega.client.schema.example.avro\",\"fields\":[{\"name\":\"vibration1\",\"type\":\"double\"},{\"name\":\"vibration2\",\"type\":[\"null\",\"double\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<VibrationData> ENCODER =
      new BinaryMessageEncoder<VibrationData>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<VibrationData> DECODER =
      new BinaryMessageDecoder<VibrationData>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<VibrationData> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<VibrationData> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<VibrationData> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<VibrationData>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this VibrationData to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a VibrationData from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a VibrationData instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static VibrationData fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private double vibration1;
   private java.lang.Double vibration2;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public VibrationData() {}

  /**
   * All-args constructor.
   * @param vibration1 The new value for vibration1
   * @param vibration2 The new value for vibration2
   */
  public VibrationData(java.lang.Double vibration1, java.lang.Double vibration2) {
    this.vibration1 = vibration1;
    this.vibration2 = vibration2;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return vibration1;
    case 1: return vibration2;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: vibration1 = (java.lang.Double)value$; break;
    case 1: vibration2 = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'vibration1' field.
   * @return The value of the 'vibration1' field.
   */
  public double getVibration1() {
    return vibration1;
  }


  /**
   * Sets the value of the 'vibration1' field.
   * @param value the value to set.
   */
  public void setVibration1(double value) {
    this.vibration1 = value;
  }

  /**
   * Gets the value of the 'vibration2' field.
   * @return The value of the 'vibration2' field.
   */
  public java.lang.Double getVibration2() {
    return vibration2;
  }


  /**
   * Sets the value of the 'vibration2' field.
   * @param value the value to set.
   */
  public void setVibration2(java.lang.Double value) {
    this.vibration2 = value;
  }

  /**
   * Creates a new VibrationData RecordBuilder.
   * @return A new VibrationData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.VibrationData.Builder newBuilder() {
    return new io.pravega.client.schema.example.avro.VibrationData.Builder();
  }

  /**
   * Creates a new VibrationData RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new VibrationData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.VibrationData.Builder newBuilder(io.pravega.client.schema.example.avro.VibrationData.Builder other) {
    if (other == null) {
      return new io.pravega.client.schema.example.avro.VibrationData.Builder();
    } else {
      return new io.pravega.client.schema.example.avro.VibrationData.Builder(other);
    }
  }

  /**
   * Creates a new VibrationData RecordBuilder by copying an existing VibrationData instance.
   * @param other The existing instance to copy.
   * @return A new VibrationData RecordBuilder
   */
  public static io.pravega.client.schema.example.avro.VibrationData.Builder newBuilder(io.pravega.client.schema.example.avro.VibrationData other) {
    if (other == null) {
      return new io.pravega.client.schema.example.avro.VibrationData.Builder();
    } else {
      return new io.pravega.client.schema.example.avro.VibrationData.Builder(other);
    }
  }

  /**
   * RecordBuilder for VibrationData instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<VibrationData>
    implements org.apache.avro.data.RecordBuilder<VibrationData> {

    private double vibration1;
    private java.lang.Double vibration2;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.pravega.client.schema.example.avro.VibrationData.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.vibration1)) {
        this.vibration1 = data().deepCopy(fields()[0].schema(), other.vibration1);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.vibration2)) {
        this.vibration2 = data().deepCopy(fields()[1].schema(), other.vibration2);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
    }

    /**
     * Creates a Builder by copying an existing VibrationData instance
     * @param other The existing instance to copy.
     */
    private Builder(io.pravega.client.schema.example.avro.VibrationData other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.vibration1)) {
        this.vibration1 = data().deepCopy(fields()[0].schema(), other.vibration1);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.vibration2)) {
        this.vibration2 = data().deepCopy(fields()[1].schema(), other.vibration2);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'vibration1' field.
      * @return The value.
      */
    public double getVibration1() {
      return vibration1;
    }


    /**
      * Sets the value of the 'vibration1' field.
      * @param value The value of 'vibration1'.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.VibrationData.Builder setVibration1(double value) {
      validate(fields()[0], value);
      this.vibration1 = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'vibration1' field has been set.
      * @return True if the 'vibration1' field has been set, false otherwise.
      */
    public boolean hasVibration1() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'vibration1' field.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.VibrationData.Builder clearVibration1() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'vibration2' field.
      * @return The value.
      */
    public java.lang.Double getVibration2() {
      return vibration2;
    }


    /**
      * Sets the value of the 'vibration2' field.
      * @param value The value of 'vibration2'.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.VibrationData.Builder setVibration2(java.lang.Double value) {
      validate(fields()[1], value);
      this.vibration2 = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'vibration2' field has been set.
      * @return True if the 'vibration2' field has been set, false otherwise.
      */
    public boolean hasVibration2() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'vibration2' field.
      * @return This builder.
      */
    public io.pravega.client.schema.example.avro.VibrationData.Builder clearVibration2() {
      vibration2 = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public VibrationData build() {
      try {
        VibrationData record = new VibrationData();
        record.vibration1 = fieldSetFlags()[0] ? this.vibration1 : (java.lang.Double) defaultValue(fields()[0]);
        record.vibration2 = fieldSetFlags()[1] ? this.vibration2 : (java.lang.Double) defaultValue(fields()[1]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<VibrationData>
    WRITER$ = (org.apache.avro.io.DatumWriter<VibrationData>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<VibrationData>
    READER$ = (org.apache.avro.io.DatumReader<VibrationData>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeDouble(this.vibration1);

    if (this.vibration2 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeDouble(this.vibration2);
    }

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.vibration1 = in.readDouble();

      if (in.readIndex() != 1) {
        in.readNull();
        this.vibration2 = null;
      } else {
        this.vibration2 = in.readDouble();
      }

    } else {
      for (int i = 0; i < 2; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.vibration1 = in.readDouble();
          break;

        case 1:
          if (in.readIndex() != 1) {
            in.readNull();
            this.vibration2 = null;
          } else {
            this.vibration2 = in.readDouble();
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










