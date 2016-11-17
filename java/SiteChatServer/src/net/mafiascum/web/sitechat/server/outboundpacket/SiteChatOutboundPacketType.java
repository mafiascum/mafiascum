package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.NameValueItemSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;

abstract class SiteChatDataPacketTypeStatic {

  static final J5EnumSet<SiteChatOutboundPacketType> enumSet = new J5EnumSet<SiteChatOutboundPacketType>();

  static final NameValueItemSet standardNameValueItemSet = new NameValueItemSet();

  static synchronized void addEnum (SiteChatOutboundPacketType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
    standardNameValueItemSet.addItem(name, enumRef);
  }
}

public enum SiteChatOutboundPacketType implements VEnum {

  connect(0, "Connect"),
  login(1, "LogIn"),
  userJoin(2, "UserJoin"),
  newMessage(3, "NewMessage"),
  leaveConversation(4, "LeaveConversation"),
  lookupUser(5, "LookupUser"),
  userList(6, "UserList"),
  passwordRequired(7, "PasswordRequired"),
  incorrectPassword(8, "IncorrectPassword"),
  setPassword(9, "SetPassword"),
  loadMessages(10, "LoadMessages"),
  debug(11, "Debug"),
  debugResult(12, "DebugResult"),
  setIgnore(13, "SetIgnore");

  private String standardName;

  private SiteChatOutboundPacketType (int id, String standardName) {
    this.standardName = standardName;
    SiteChatDataPacketTypeStatic.addEnum(this, id, standardName);
  }

  public int value () { return SiteChatDataPacketTypeStatic.enumSet.getValue(this); }
  public String toString () { return SiteChatDataPacketTypeStatic.enumSet.toString(this); }

  public String getStandardName () { return standardName; }

  public static SiteChatOutboundPacketType getEnum(int value) throws IndexOutOfBoundsException { return SiteChatDataPacketTypeStatic.enumSet.getEnum(value); }
  public static SiteChatOutboundPacketType getEnumByStandardName(String standardName) {
    
    Iterator<SiteChatOutboundPacketType> iter = getSetIterator();
    while(iter.hasNext()) {
      
      SiteChatOutboundPacketType SiteChatOutboundPacketType = iter.next();
      
      if(SiteChatOutboundPacketType.getStandardName().equals(standardName)) {
        
        return SiteChatOutboundPacketType;
      }
    }
    
    return null;
  }
  public static Iterator<SiteChatOutboundPacketType> getSetIterator () { return SiteChatDataPacketTypeStatic.enumSet.iterator(); }
  public static VEnumSet getSet () { return SiteChatDataPacketTypeStatic.enumSet; };

  public static NameValueItemSet getStandardNameValueItemSet () { return SiteChatDataPacketTypeStatic.standardNameValueItemSet; }
};
