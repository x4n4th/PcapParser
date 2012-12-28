package header;

import java.util.HashMap;
import java.util.Map;

public class UdpHeader extends Header {
  public enum Protocol {
    DHCP("BOOTP"),
    DNS(""),
    UNKNOWN("");
    
    private String name;
    
    private Protocol(String name) {
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
  }
  
  @SuppressWarnings("serial")
  private static final Map<Integer, Protocol> portProtocolMap = 
      new HashMap<Integer, Protocol>() {
    {
      put(67, Protocol.DHCP);
      put(53, Protocol.DNS);
    }
  };
  
  @SuppressWarnings("serial")
  private static final Map<Integer, String> portNamesMap = 
      new HashMap<Integer, String>() {
    {
      put(67, "BOOTP Server");
      put(68, "BOOTP Client");
    }
  };
  
  @HeaderField(offset = 0, numBits = 16)
  private int sourcePort;
  
  @HeaderField(offset = 16, numBits = 16)
  private int destPort;
  
  @HeaderField(offset = 32, numBits = 16)
  private int length;
  
  @HeaderField(offset = 48, numBits = 16)
  private int checksum;
  
  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    switch (getProtocol()) {
      case DHCP:
        return DhcpHeader.class;
      default:
        return null;  
    }
  }
  
  public Protocol getProtocol() {
    if (portProtocolMap.containsKey(sourcePort)) {
      return portProtocolMap.get(sourcePort);
    } else if (portProtocolMap.containsKey(destPort)) {
      return portProtocolMap.get(destPort);
    } else {
      return Protocol.UNKNOWN;
    }
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public int getDestPort() {
    return destPort;
  }

  public int getLength() {
    return length;
  }

  public int getChecksum() {
    return checksum;
  }
  
  @Override
  public String toString() {
    String sourcePortName = portNamesMap.get(sourcePort);
    String destPortName = portNamesMap.get(destPort);
    
    String sourcePortTag = sourcePortName != null ?
        "(" + sourcePortName + ")" : "";
    
    String destPortTag = destPortName != null ?
        "(" + destPortName + ")" : "";
    
    return makeText("UDP",
        "----- UDP Header -----",
        "",
        f("Source port = %d %s", sourcePort, sourcePortTag),
        f("Destination port = %d %s", destPort, destPortTag),
        f("Length = %d", length),
        f("Checksum = 0x%x", checksum));
  }
}
