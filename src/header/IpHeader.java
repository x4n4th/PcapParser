package header;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class IpHeader extends Header {
  public enum Protocol {
    ICMP(1),
    TCP(6),
    UDP(17),
    UNKNOWN;
    
    private final int code;
    
    private Protocol() {
      this(-1);
    }
    
    private Protocol(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return code;
    }
  }
  
  @SuppressWarnings("serial")
  private static final Map<Integer, Protocol> protocolMap = 
      new HashMap<Integer, Protocol>() {
    {
      put(1, Protocol.ICMP);
      put(6, Protocol.TCP);
      put(17, Protocol.UDP);
    }
  };
  
  @HeaderField(offset = 0, numBits = 4)
  private short version;
  
  @HeaderField(offset = 4, numBits = 4)
  private short headerLength;
  
  @HeaderField(offset = 8, numBits = 8)
  private short typeOfService;
  
  @HeaderField(offset = 16, numBits = 16)
  private int totalLength;
  
  @HeaderField(offset = 32, numBits = 16)
  private int datagramIdentifier;
  
  @HeaderField(offset = 48, numBits = 3)
  private short flags;
  
  @HeaderField(offset = 51, numBits = 13)
  private int fragmentOffset;
  
  @HeaderField(offset = 64, numBits = 8)
  private short timeToLive;
  
  @HeaderField(offset = 72, numBits = 8)
  private Protocol protocol;
  
  @HeaderField(offset = 80, numBits = 16)
  private int headerChecksum;
  
  @HeaderField(offset = 96, numBits = 32)
  private InetAddress sourceIpAddress;
  
  @HeaderField(offset = 128, numBits = 32)
  private InetAddress destIpAddress;
  
  @TypeMapper(Protocol.class)
  public static Protocol makeProtocol(byte[] bytes) {
    int code = (int) bytes[0];
    
    return protocolMap.containsKey(code) ? protocolMap.get(code) : Protocol.UNKNOWN;
  }
  
  /**
   * Parses a canonically formatted IPv4 address into a {@link InetAddress} object.
   * 
   * @param address the string address to parse
   * @return an {@link InetAddress} that represents the provided address
   */
  public static InetAddress parseIpV4Address(String address) {
    String[] parts = address.split("\\.");
    byte[] bytes = new byte[parts.length];
    
    for (int i = 0; i < parts.length; ++i) {
      bytes[i] = Integer.valueOf(parts[i]).byteValue();
    }
    
    try {
      return InetAddress.getByAddress(bytes);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
  public IpHeader() {}
  
  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    if (protocol == null) return null;
    
    switch (protocol) {
      case TCP:
        return TcpHeader.class;
      case UDP:
        return UdpHeader.class;
      default:
        return null;
    }
  }
 
  public long getIpHeaderLength() {
    return headerLength * 4;
  }

  public long getTotalLength() {
    return totalLength;
  }

  public int getVersion() {
    return version;
  }

  public int getTypeOfService() {
    return typeOfService;
  }

  public int getDatagramIdentifier() {
    return datagramIdentifier;
  }

  public int getFlags() {
    return flags;
  }

  public int getFragmentOffset() {
    return fragmentOffset;
  }

  public int getTimeToLive() {
    return timeToLive;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public int getHeaderChecksum() {
    return headerChecksum;
  }

  public InetAddress getSourceIpAddress() {
    return sourceIpAddress;
  }

  public InetAddress getDestIpAddress() {
    return destIpAddress;
  }
  
  @Override
  public String toString() {
    int reservedBit = flags & 1;
    int dontFragmentBit = (flags >> 1) & 1;
    int moreFragmentsBit = (flags >> 2) & 1;
    
    return makeText("IP",
        "----- IP Header -----",
        "",
        f("Version = %d", version),
        f("Header length = %d bytes", headerLength),
        f("Type of service = 0x%02x", typeOfService),
        f("Total length = %d bytes", totalLength),
        f("Identification: 0x%x (%d)", datagramIdentifier, datagramIdentifier),
        f("  Flags: 0x%02x", flags),
        f("    %d... = Reserved bit: %s", reservedBit, reservedBit != 0 ? "Set" : "Not set"),
        f("    .%d.. = Don't fragment: %s", dontFragmentBit, dontFragmentBit != 0 ? "Set" : "Not set"),
        f("    ..%d. = More fragments: %s", moreFragmentsBit, moreFragmentsBit != 0 ? "Set" : "Not set"),
        f("  Fragment Offset: %d", fragmentOffset),
        f("Time to live = %d seconds/hops", timeToLive),
        f("Protocol = %d (%s)", protocol.getCode(), protocol),
        f("Source IP address = %s", sourceIpAddress),
        f("Destination IP address = %s", destIpAddress));
  }
}
