package main;

import header.EthernetHeader;
import header.EthernetHeader.EtherType;
import header.IpHeader;
import header.UdpHeader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import main.EthernetFrameList.Filter;
import data.Packet;

public class Main {
  public static void main(String[] args) 
      throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
    
    File file = new File("tracert.pcap");
    LibpcapParser parser = new LibpcapParser(file);
    
    EthernetFrameList packets = parser.parse().getAll(new Filter() {
      @SuppressWarnings("unchecked")
      @Override
      public boolean shouldUse(Packet<EthernetHeader> packet) {
        EthernetHeader header = packet.getHeader();
        if (header.getType() != EtherType.IP) return false;
        
        Packet<IpHeader> ipPacket = (Packet<IpHeader>) packet.getData();
        IpHeader ipHeader = ipPacket.getHeader();
        
        if (ipHeader.getProtocol() != IpHeader.Protocol.UDP) return false;
        
        Packet<UdpHeader> udpPacket = (Packet<UdpHeader>) ipPacket.getData();
        return udpPacket.getHeader().getProtocol() == UdpHeader.Protocol.DHCP;
      }
    });
    
    for (Packet<EthernetHeader> packet : packets) {
      System.out.println(packet);
    }
    
    System.out.println("/** Statistics **/");
    System.out.printf("Number of records processed: %d\n", packets.size());
    System.out.printf("Average packet length: %d bytes\n", packets.getAveragePacketLength());
  }
}
