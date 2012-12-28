package header;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Header {
  
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface HeaderField {
    int offset();
    int numBits();
  }
  
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TypeMapper {
    Class<?> value();
  }
  
  @TypeMapper(BigInteger.class)
  public static BigInteger makeBigInt(byte[] data) {
    return u(data);
  }
  
  @TypeMapper(long.class)
  public static long makeLong(byte[] data) {
    return u(ByteBuffer.wrap(data).getInt());
  }
  
  @TypeMapper(int.class)
  public static int makeInteger(byte[] data) {
    return u(ByteBuffer.wrap(data).getShort());
  }
  
  @TypeMapper(short.class)
  public static short makeShort(byte[] data) {
    return u(ByteBuffer.wrap(data).get());
  }
  
  @TypeMapper(byte.class)
  public static byte makeByte(byte[] data) {
    return ByteBuffer.wrap(data).get();
  }
  
  @TypeMapper(byte[].class)
  public static byte[] makeByteArray(byte[] data) {
    return data;
  }

  @TypeMapper(InetAddress.class)
  public static InetAddress makeInetAddress(byte[] bytes) {
    try {
      return InetAddress.getByAddress(bytes);
    } catch (UnknownHostException e) {
      return null;
    }
  }
  
  /**
   * @param signed an array of bytes that represents a number longer than a long.
   * @return a {@link BigInteger} whose value is {@code signed}, unsigned.
   */
  public static BigInteger u(byte[] signed) {
    return new BigInteger(1, signed);
  }
  
  /**
   * @param signed a signed integer
   * @return that integer, unsigned, held in a long
   */
  public static long u(int signed) {
    return (long) signed & 0xFFFFFFFFL;
  }
  
  /**
   * @param signed a signed short
   * @return that short, unsigned, held in an int
   */
  public static int u(short signed) {
    return signed & 0xFFFF;
  }
  
  /**
   * @param signed a signed byte
   * @return that byte, unsigned, held in a short
   */
  public static short u(byte signed) {
    return (short) (signed & 0xFF);
  }
  
  public static final String DEFAULT_BYTE_FORMAT = "%02x";
  
  public abstract Class<? extends Header> getDataPacketHeaderType();
  
  public ByteOrder getByteOrder() {
    return ByteOrder.BIG_ENDIAN;
  }
  
  public long getHeaderLength() {
    long length = 0;
    
    for (Field field : getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(HeaderField.class)) {
        length += field.getAnnotation(HeaderField.class).numBits();
      }
    }
    
    return (long) Math.ceil(length / 8L);
  }

  /**
   * Convenience function for {@link String#format(String, Object...)}.
   * 
   * @param string the string to format
   * @param args the format arguments
   * @return the formatted string
   */
  public String f(String string, Object...args) {
    return String.format(string, args);
  }
  
  /**
   * Convenience function for building a string from a list of lines.
   * 
   * @param lines the lines to be joined in a single string
   * @return a single string of all lines
   */
  public String makeText(String prefix, String...lines) {
    StringBuilder builder = new StringBuilder();
    for (String line : lines) {
      builder.append(String.format("%s: %s\n", prefix, line));
    }
    
    return builder.toString();
  }
}
