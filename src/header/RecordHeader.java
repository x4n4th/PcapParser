package header;

import java.math.BigInteger;
import java.nio.ByteOrder;

public class RecordHeader extends Header {
  @HeaderField(offset = 0, numBits = 64)
  private BigInteger timestamp;
  
  @HeaderField(offset = 64, numBits = 32)
  private long capturedDataLength;
  
  @HeaderField(offset = 96, numBits = 32)
  private long frameLength;
  
  public RecordHeader() {}
  
  @Override
  public ByteOrder getByteOrder() {
    return ByteOrder.LITTLE_ENDIAN;
  }

  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    return EthernetHeader.class;
  }

  public BigInteger getTimestamp() {
    return timestamp;
  }

  public long getCapturedDataLength() {
    return capturedDataLength;
  }

  public long getFrameLength() {
    return frameLength;
  }
}
