package main;

import header.EthernetHeader;
import header.Header;
import header.Header.HeaderField;
import header.Header.TypeMapper;
import header.RecordHeader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ByteData;
import data.Data;
import data.Packet;

public class LibpcapParser {
  private final long fileSize;
  private final InputStream input;
  private final Reader reader;
  private int offset;
  private Map<Class<? extends Header>, List<Field>> sortedFieldsMap;
  
  private static final int GLOBAL_HEADER_LENGTH = 24;
  
  /**
   * Turns an array of bytes into an array of bits.
   * 
   * @param byteBuffer the array of bytes
   * @return the corresponding array of bits
   */
  public static short[] toBitArray(ByteBuffer byteBuffer) {
    byte[] bytes = byteBuffer.array();
    short[] bits = new short[bytes.length * 8];
    
    int offset = 0;
    
    for (byte b : bytes) {
      for (int i = 7; i >= 0; --i) {
        bits[offset++] = (short) (((int) b >>> i) & 1);
      }
    }
    
    return bits;
  }
  
  public LibpcapParser(File libpcapFile) throws FileNotFoundException {
    fileSize = libpcapFile.length();
    input = new BufferedInputStream(new FileInputStream(libpcapFile));
    
    reader = new Reader();
    offset = 0;
    
    sortedFieldsMap = new HashMap<Class<? extends Header>, List<Field>>();
  }
  
  /**
   * Parses the LibPcap file into a list of ethernet packets.
   * @return a list of ethernet packets
   * @throws IOException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("unchecked")
  public EthernetFrameList parse() 
      throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
    
    reader.read(GLOBAL_HEADER_LENGTH);
    
    EthernetFrameList frameList = new EthernetFrameList();
    
    while (hasMoreData()) {
      Packet<RecordHeader> record = parse(RecordHeader.class);
      frameList.add((Packet<EthernetHeader>) record.getData());
      
      // The length should not include the meta-data in the record header.
      long bytesRead = record.getLength() - record.getHeader().getHeaderLength();
      long bytesNeeded = record.getHeader().getCapturedDataLength();
      
      // Consume any data in the last packet that wasn't already consumed.
      if (bytesNeeded > 0) {
        
        // Find the packet that had no data (meaning no sub-packet that we
        // knew how to parse).
        Packet<? extends Header> currentPacket = record;
        while (currentPacket.hasNestedPacket()) {
          currentPacket = (Packet<? extends Header>) currentPacket.getData();
        }

        // Set that packet's data to a byte buffer of the remaining bytes. These
        // bytes can very well be other packets, but header classes haven't been
        // defined for them yet so we just treat them as blobs.
        ByteBuffer bytes = reader.read((int)(bytesNeeded - bytesRead));
        ByteData byteData = new ByteData(bytes.array());
        
        currentPacket.setData(byteData);
      }
    }
    
    return frameList;
  }
  
  /**
   * Parses a given packet header by extracting the annotated header fields and using their
   * offsets/number of bits to dynamically extract the correct number of bytes and create
   * the corresponding value for that field.
   * 
   * @param type the type of header to parse
   * @return a packet with an instantiated version of the passed in header type as its header
   * 
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IOException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  public <H extends Header> Packet<H> parse(Class<H> type) 
      throws InstantiationException, IllegalAccessException, IOException, IllegalArgumentException, InvocationTargetException {
    
    List<Field> headerFields = getSortedFields(type);
    Map<Class<?>, Method> typeMap = getTypeMap(type);
  
    H header = type.newInstance();
    // Read all we need for the given header at once.
    ByteBuffer data = reader.read((int) header.getHeaderLength());
    
    short[] bits = toBitArray(data);
    
    for (Field headerField : headerFields) {
      headerField.setAccessible(true);
      
      HeaderField annotation = headerField.getAnnotation(HeaderField.class);
      int bitOffset = annotation.offset();
      int numBits = annotation.numBits();
      
      // Create the byte array for this field, padded as necessary.
      byte[] fieldBytes = new byte[(int) Math.ceil(numBits / 8.0)];
      int currentBytePos = 0;
      int currentBitPos = 0;
      int lastBitPos = bitOffset + numBits - 1;
      
      for (int i = lastBitPos; i >= bitOffset; --i) {
        currentBitPos = (lastBitPos - i) % 8;
        
        // Depending on the endianness that the header expects, we need
        // to fill the byte array from a given direction.
        currentBytePos = header.getByteOrder() == ByteOrder.LITTLE_ENDIAN ? 
            (lastBitPos - i) / 8 : (i - bitOffset) / 8;
        
        // Set the bit in the current bit position in the current byte
        int fieldByte = fieldBytes[currentBytePos];
        int currentBit = bits[i];
        fieldByte |= currentBit << currentBitPos;
        
        fieldBytes[currentBytePos] = (byte) fieldByte;
      }
      
      // Get the type mapping function for this field's type.
      Class<?> headerType = headerField.getType();
      if (!typeMap.containsKey(headerType)) throw new RuntimeException("Unrecognized type '" + headerType + "'");
      
      Method mapper = typeMap.get(headerType);
      
      Object value = mapper.invoke(null, fieldBytes);
      headerField.set(header, value);
    }
    
    // If the header has a sub-packet of some sort, recursively parse the header, else just assign
    // the data to null for now.
    Data packetData = header.getDataPacketHeaderType() != null ? 
        parse(header.getDataPacketHeaderType()) : null;
        
    return new Packet<H>(header, packetData);
  }
  
  /**
   * Builds a map of types to mapping functions that create those types from byte arrays.
   * 
   * @param type the type of header from which to extract the mapping functions
   * @return a map of types to mapping functions
   */
  public Map<Class<?>, Method> getTypeMap(Class<? extends Header> type) {
    Map<Class<?>, Method> typeMap = new HashMap<Class<?>, Method>();
    
    for (Method method : type.getMethods()) {
      method.setAccessible(true);
      
      if (method.isAnnotationPresent(TypeMapper.class)) {
        TypeMapper mapper = method.getAnnotation(TypeMapper.class);
        Class<?> mappedType = mapper.value();
        
        typeMap.put(mappedType, method);
      }
    }
    
    return typeMap;
  }
  
  /**
   * Gets the fields for the given header type sorted in ascending order based on their
   * byte-offset in the packet header.
   * 
   * @param type the type of the header from which to get the fields
   * @return a sorted list (ascending based on byte offset) of the packet header fields
   */
  public List<Field> getSortedFields(Class<? extends Header> type) {
    if (sortedFieldsMap.containsKey(type)) return sortedFieldsMap.get(type);
    
    Field[] fields = type.getDeclaredFields();
    List<Field> headerFields = new ArrayList<Field>();
    
    for (Field field : fields) {
      if (field.isAnnotationPresent(HeaderField.class)) {
        headerFields.add(field);
      }
    }
    
    Collections.sort(headerFields, new Comparator<Field>() {
      @Override
      public int compare(Field first, Field second) {
        return first.getAnnotation(HeaderField.class).offset() - 
            second.getAnnotation(HeaderField.class).offset();
      }
    });
    
    sortedFieldsMap.put(type, headerFields);
    
    return headerFields;
  }
  
  public boolean hasMoreData() {
    return offset < fileSize;
  }
  
  public Reader getReader() {
    return reader;
  }
  
  public int getOffset() {
    return offset;
  }
  
  /**
   * Class that aims to wrap reading from the input stream so that we can easily
   * track how much data has been read from the file.
   */
  public class Reader {
    /**
     * Reads {@code length} bytes from the file at the input stream's current
     * position.
     * 
     * @param length the amount of bytes to read
     * @return a {@link ByteBuffer} that holds the underlying byte array of the data
     *    that was read.
     * @throws IOException if there was an error while reading from the file
     */
    public ByteBuffer read(int length) throws IOException {
      return read(length, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Reads {@code length} bytes from the file at the input stream's current
     * position.
     * 
     * @param length the amount of bytes to read
     * @param order the endianness of the resulting {@link ByteBuffer}
     * @return a {@link ByteBuffer} that holds the underlying byte array of the data
     *    that was read.
     * @throws IOException if there was an error while reading from the file
     */
    public ByteBuffer read(int length, ByteOrder order) throws IOException {
      byte[] data = new byte[length];
      input.read(data, 0, length);
      
      offset += length;
      ByteBuffer buffer = ByteBuffer.wrap(data);
      buffer.order(order);
      return buffer;
    }
  }
}
