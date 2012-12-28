package main;

import header.EthernetHeader;

import java.util.ArrayList;

import data.Packet;

/**
 * Simple list of ethernet packets that supports filtering and some basic statistical
 * data.
 */
@SuppressWarnings("serial")
public class EthernetFrameList extends ArrayList<Packet<EthernetHeader>> {
  
  /**
   * @param filters the filters to run against the packets in this list
   * @return a subset of the list backed by this object that contains {@link Packet<EthernetHeader>}s
   *    that fulfill all of the {@link Filter}s in {@code filters}
   */
  public EthernetFrameList getAll(Filter...filters) {
    EthernetFrameList filteredList = new EthernetFrameList();
    
    for (Packet<EthernetHeader> packet : this) {
      if (passesAllFilters(packet, filters)) filteredList.add(packet);
    }
    
    return filteredList;
  }
  
  /**
   * @return the average length (in bytes) of all of the packets
   */
  public long getAveragePacketLength() {
    long lengthSum = 0;
    
    for (Packet<EthernetHeader> packet : this) {
      lengthSum += packet.getLength();
    }
    
    return lengthSum / size();
  }
  
  /**
   * @param packet the packet against which to run the filters
   * @param filters the filters to run
   * @return whether or not the packet passes all filters
   */
  protected boolean passesAllFilters(Packet<EthernetHeader> packet, Filter[] filters) {
    for (Filter filter : filters) {
      if (!filter.shouldUse(packet)) return false;
    }
    
    return true;
  }
  
  /**
   * Simple filter function interface for filtering packets from the list.
   */
  public static interface Filter {
    public boolean shouldUse(Packet<EthernetHeader> packet);
  }
}
