package data;


public class ByteData implements Data {
  private byte[] bytes;
  
  public ByteData(byte[] bytes) {
    this.bytes = bytes;
  }
  
  public byte[] getBytes() {
    return bytes;
  }
  
  @Override
  public long getLength() {
    return bytes.length;
  }
}
