package net.mafiascum.web.sitechat.server.outboundpacket;


public class SiteChatOutboundPasswordRequiredPacket extends SiteChatOutboundPacket {

  protected String conversationName;
  
  public String getConversationName() {
    return conversationName;
  }

  public void setConversationName(String conversationName) {
    this.conversationName = conversationName;
  }
  
  public SiteChatOutboundPacketType getType() {
  
    return SiteChatOutboundPacketType.passwordRequired;
  }
}
