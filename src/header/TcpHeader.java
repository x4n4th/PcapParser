package header;

public class TcpHeader extends Header {
  @HeaderField(offset = 0, numBits = 16)
  private int sourcePort;
  
  @HeaderField(offset = 16, numBits = 16)
  private int destPort;
  
  @HeaderField(offset = 32, numBits = 32)
  private long sequenceNumber;
  
  @HeaderField(offset = 64, numBits = 32)
  private long ackNumber;
  
  @HeaderField(offset = 96, numBits = 4)
  private short headerLength;
  
  @HeaderField(offset = 100, numBits = 3)
  private byte reserved;
  
  @HeaderField(offset = 103, numBits = 9)
  private short flags;
  
  @HeaderField(offset = 112, numBits = 16)
  private int advertizedWindowSize;

  @HeaderField(offset = 128, numBits = 16)
  private int checksum;
  
  @HeaderField(offset = 144, numBits = 16)
  private int urgentPointer;
  
  @Override
  public Class<? extends Header> getDataPacketHeaderType() {
    return null;
  }

  public int getSourcePort() {
    return sourcePort;
  }

  public int getDestPort() {
    return destPort;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public long getAckNumber() {
    return ackNumber;
  }

  public long getTcpHeaderLength() {
    return headerLength;
  }

  public byte getReserved() {
    return reserved;
  } 

  public short getFlags() {
    return flags;
  }

  public int getAdvertizedWindowSize() {
    return advertizedWindowSize;
  }

  public int getChecksum() {
    return checksum;
  }

  public int getUrgentPointer() {
    return urgentPointer;
  }
  
  @Override
  public String toString() {
    int urgentPointerBit = flags >>> 5 & 1;
    int ackBit = flags >>> 4 & 1;
    int pushBit = flags >>> 3 & 1;
    int resetBit = flags >>> 2 & 1;
    int synBit = flags >>> 1 & 1;
    int finBit = flags & 1;
    
    return makeText("TCP",
        "----- TCP Header -----",
        "",
        f("Source port = %d", sourcePort),
        f("Destination port = %d", destPort),
        f("Sequence number = %d", sequenceNumber),
        f("Acknowledgement number = %d", ackNumber),
        f("Header length = %d", headerLength),
        f("Flags = 0x%x", flags),
        f("  ..%d..... = %s", urgentPointerBit, urgentPointerBit == 1 ? "Urgent pointer" : "No urgent pointer"),
        f("  ...%d.... = %s", ackBit, ackBit == 1 ? "Ackowledgement" : "No acknowledgement"),
        f("  ....%d... = %s", pushBit, pushBit == 1 ? "Push" : "No push"),
        f("  .....%d.. = %s", resetBit, resetBit == 1 ? "Reset" : "No reset"),
        f("  ......%d. = %s", synBit, synBit == 1 ? "Syn" : "No syn"),
        f("  .......%d = %s", finBit, finBit == 1 ? "Fin" : "No fin"),
        f("Window size = %d", advertizedWindowSize));
  }
}
