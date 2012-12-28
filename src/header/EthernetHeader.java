package header;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class EthernetHeader extends Header {
  public enum EtherType {
    IP(0x0800),
    UNKNOWN;
    
    private final int code;
    
    private EtherType() {
      this(-1);
    }
    
    private EtherType(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return code;
    }
  }
  
  @SuppressWarnings("serial")
  private static final Map<Integer, EtherType> etherTypeMap = 
      new HashMap<Integer, EtherType>() {
    {
      put(0x0800, EtherType.IP);
    }
  };
  
  @HeaderField(offset = 0, numBits = 48)
  private byte[] sourceMacAddress;
  
  @HeaderField(offset = 48, numBits = 48)
  private byte[] destMacAddress;
  
  @HeaderField(offset = 96, numBits = 16)
  private EtherType type;
  
  @TypeMapper(EtherType.class)
  public static EtherType makeEtherType(byte[] data) {
    int code = u(ByteBuffer.wrap(data).getShort());
    return etherTypeMap.containsKey(code) ? etherTypeMap.get(code) : EtherType.UNKNOWN;
  }
  
  public static String formatMacAddress(byte[] macAddress) {
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < macAddress.length; ++i) {
      sb.append(String.format("%02x", macAddress[i]));
      
      if (i < macAddress.length - 1) sb.append(":");
    }
    
    return sb.toString();
  }
  
  public EthernetHeader() {}
  
  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    switch (type) {
      case IP:
        return IpHeader.class;
      default:
        return null;
    }
  }

  public static Map<Integer, EtherType> getEthertypemap() {
    return etherTypeMap;
  }

  public byte[] getSourceMacAddress() {
    return sourceMacAddress;
  }

  public byte[] getDestMacAddress() {
    return destMacAddress;
  }

  public EtherType getType() {
    return type;
  }
  
  @Override
  public String toString() {
    return makeText("ETHER",
        "----- Ether Header -----",
        "",
        f("Destination = %s", formatMacAddress(destMacAddress)),
        f("Source = %s", formatMacAddress(sourceMacAddress)),
        f("Ethertype = 0x%x (%s)", type.getCode(), type.toString()),
        "");
  }
}
