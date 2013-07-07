package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

public class SiteChatOutboundIncorrectPasswordPacket extends SiteChatOutboundPacket {

  protected String conversationName;
  
  public String getConversationName() {
    return conversationName;
  }

  public void setConversationName(String conversationName) {
    this.conversationName = conversationName;
  }
  
  public SiteChatOutboundPacketType getType() {
  
    return SiteChatOutboundPacketType.incorrectPassword;
  }
}
