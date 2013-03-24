package net.mafiascum.web.sitechat.server.inboundpacket;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.NameValueItemSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class SiteChatDataPacketTypeStatic {

  static final J5EnumSet<SiteChatInboundPacketType> enumSet = new J5EnumSet<SiteChatInboundPacketType>();

  static final NameValueItemSet standardNameValueItemSet = new NameValueItemSet();

  static synchronized void addEnum (SiteChatInboundPacketType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
    standardNameValueItemSet.addItem(name, enumRef);
  }
}

public enum SiteChatInboundPacketType implements VEnum {

  connect(0, "Connect"),
  login(1, "LogIn"),
  sendMessage(2, "SendMessage"),
  leaveConversation(3, "LeaveConversation"),
  lookupUser(4, "LookupUser"),
  heartbeat(5, "Heartbeat");
  

  private String standardName;

  private SiteChatInboundPacketType (int id, String standardName) {
    this.standardName = standardName;
    SiteChatDataPacketTypeStatic.addEnum(this, id, standardName);
  }

  public int value () { return SiteChatDataPacketTypeStatic.enumSet.getValue(this); }
  public String toString () { return SiteChatDataPacketTypeStatic.enumSet.toString(this); }

  public String getStandardName () { return standardName; }

  public static SiteChatInboundPacketType getEnum(int value) throws IndexOutOfBoundsException { return SiteChatDataPacketTypeStatic.enumSet.getEnum(value); }
  public static SiteChatInboundPacketType getEnumByStandardName(String standardName) {
    
    Iterator<SiteChatInboundPacketType> iter = getSetIterator();
    while(iter.hasNext()) {
      
      SiteChatInboundPacketType siteChatInboundPacketType = iter.next();
      
      if(siteChatInboundPacketType.getStandardName().equals(standardName)) {
        
        return siteChatInboundPacketType;
      }
    }
    
    return null;
  }
  public static Iterator<SiteChatInboundPacketType> getSetIterator () { return SiteChatDataPacketTypeStatic.enumSet.iterator(); }
  public static VEnumSet getSet () { return SiteChatDataPacketTypeStatic.enumSet; };

  public static NameValueItemSet getStandardNameValueItemSet () { return SiteChatDataPacketTypeStatic.standardNameValueItemSet; }
};
