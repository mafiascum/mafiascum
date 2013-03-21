package net.mafiascum.web.sitechat.server.outboundpacket;

public class SiteChatOutboundLeaveConversationPacket extends SiteChatOutboundPacket {

  protected int userId;
  protected int siteChatConversationId;
  
  public SiteChatOutboundLeaveConversationPacket() {
    
    super();
  }
  
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getSiteChatConversationId() {
    return siteChatConversationId;
  }
  public void setSiteChatConversationId(int siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }

  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.leaveConversation;
  }
}
