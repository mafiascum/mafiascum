package net.mafiascum.web.sitechat.server.inboundpacket;

import java.util.Iterator;

import net.mafiascum.enumerator.J5EnumSet;
import net.mafiascum.enumerator.NameValueItemSet;
import net.mafiascum.enumerator.VEnum;
import net.mafiascum.enumerator.VEnumSet;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundConnectPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundDebugPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundDebugResultPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundHeartbeatPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLeaveConversationPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLoadMessagesPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLogInPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLookupUserPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSendMessagePacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSetIgnorePacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSetPasswordPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSetUserSettingsPacketOperator;

abstract class SiteChatDataPacketTypeStatic {

  static final J5EnumSet<SiteChatInboundPacketType> enumSet = new J5EnumSet<SiteChatInboundPacketType>();

  static final NameValueItemSet standardNameValueItemSet = new NameValueItemSet();

  static synchronized void addEnum (SiteChatInboundPacketType enumRef, int enumID, String name) {
    enumSet.registerEnum(enumRef, enumID);
    standardNameValueItemSet.addItem(name, enumRef);
  }
}

public enum SiteChatInboundPacketType implements VEnum {

  connect(0, "Connect", SiteChatInboundConnectPacketOperator.class),
  login(1, "LogIn", SiteChatInboundLogInPacketOperator.class),
  sendMessage(2, "SendMessage", SiteChatInboundSendMessagePacketOperator.class),
  leaveConversation(3, "LeaveConversation", SiteChatInboundLeaveConversationPacketOperator.class),
  lookupUser(4, "LookupUser", SiteChatInboundLookupUserPacketOperator.class),
  heartbeat(5, "Heartbeat", SiteChatInboundHeartbeatPacketOperator.class),
  setPassword(6, "SetPassword", SiteChatInboundSetPasswordPacketOperator.class),
  loadMessages(7, "LoadMessages", SiteChatInboundLoadMessagesPacketOperator.class),
  debug(8, "Debug", SiteChatInboundDebugPacketOperator.class),
  debugResult(9, "DebugResult", SiteChatInboundDebugResultPacketOperator.class),
  setUserSettings(10, "SetUserSettings", SiteChatInboundSetUserSettingsPacketOperator.class),
  setIgnore(11, "SetIgnore", SiteChatInboundSetIgnorePacketOperator.class);
  

  private String standardName;
  private Class<?extends SiteChatInboundPacketOperator> operatorClass;

  private SiteChatInboundPacketType (int id, String standardName, Class<?extends SiteChatInboundPacketOperator> operatorClass) {
    this.standardName = standardName;
    this.operatorClass = operatorClass;
    SiteChatDataPacketTypeStatic.addEnum(this, id, standardName);
  }

  public int value () { return SiteChatDataPacketTypeStatic.enumSet.getValue(this); }
  public String toString () { return SiteChatDataPacketTypeStatic.enumSet.toString(this); }

  public String getStandardName () { return standardName; }
  public Class<?extends SiteChatInboundPacketOperator> getOperatorClass() { return operatorClass; }

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
