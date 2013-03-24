package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

public class SiteChatOutboundNewMessagePacket extends SiteChatOutboundPacket {

  protected SiteChatConversationMessage siteChatConversationMessage;
  
  public SiteChatConversationMessage getSiteChatConversationMessage() {
    return siteChatConversationMessage;
  }

  public void setSiteChatConversationMessage(
      SiteChatConversationMessage siteChatConversationMessage) {
    this.siteChatConversationMessage = siteChatConversationMessage;
  }
  
  public SiteChatOutboundPacketType getType() {
  
    return SiteChatOutboundPacketType.newMessage;
  }
}
