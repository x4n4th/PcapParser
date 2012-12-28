package header;

import java.net.InetAddress;

public class DhcpHeader extends Header {
  public enum MessageType {
    DISCOVER,
    OFFER,
    REQUEST,
    DECLINE,
    ACK,
    NACK,
    RELEASE;
  }
  
  @SuppressWarnings("unused")
  @HeaderField(offset = 0, numBits = 96)
  private byte[] preamble;
  
  @HeaderField(offset = 96, numBits = 32)
  private InetAddress clientIpAddress;
  
  @HeaderField(offset = 128, numBits = 32)
  private InetAddress thisIpAddress;
  
  @HeaderField(offset = 160, numBits = 32)
  private InetAddress serverIpAddress;
  
  @HeaderField(offset = 192, numBits = 32)
  private InetAddress routerIpAddress;
  
  @SuppressWarnings("unused")
  @HeaderField(offset = 224, numBits = 1712)
  private byte[] ignored;  
  
  @HeaderField(offset = 1936, numBits = 8)
  private MessageType dhcpMessageType;
  
  @TypeMapper(MessageType.class)
  public static MessageType makeMessageType(byte[] bytes) {
    return MessageType.values()[bytes[0] - 1];
  }
  
  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    return null;
  }

  public InetAddress getClientIpAddress() {
    return clientIpAddress;
  }

  public InetAddress getThisIpAddress() {
    return thisIpAddress;
  }

  public InetAddress getServerIpAddress() {
    return serverIpAddress;
  }

  public InetAddress getRouterIpAddress() {
    return routerIpAddress;
  }

  public MessageType getDhcpMessageType() {
    return dhcpMessageType;
  }
  
  @Override
  public String toString() {
    return makeText("DHCP",
        "----- DHCP Header -----",
        f("Client IP address = %s", clientIpAddress),
        f("Your IP address = %s", thisIpAddress),
        f("Server IP address = %s",serverIpAddress),
        f("Router IP address = %s", routerIpAddress),
        f("Message type = %s (DHCP %s)", dhcpMessageType.ordinal() + 1, dhcpMessageType));
  }
}
