package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundLeaveConversationPacket extends SiteChatInboundPacket {

  protected int siteChatConversationId;

  public int getSiteChatConversationId() {
    return siteChatConversationId;
  }

  public void setSiteChatConversationId(int siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }

  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.leaveConversation;
  }
}
