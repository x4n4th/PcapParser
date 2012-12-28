package data;

import header.Header;


public class Packet<H extends Header> implements Data {
  private H header;
  private Data data;
  
  public Packet(H header, Data data) {
    this.header = header;
    this.data = data;
  }

  public H getHeader() {
    return header;
  }

  public Data getData() {
    return data;
  }
  
  public void setData(Data data) {
    this.data = data;
  }
  
  public boolean hasNestedPacket() {
    return data instanceof Packet;
  }
  
  public long getLength() {
    return header.getHeaderLength() + (data != null ? data.getLength() : 0);
  }
  
  @Override
  public String toString() {
    return header.toString() + '\n' + data;
  }
}
