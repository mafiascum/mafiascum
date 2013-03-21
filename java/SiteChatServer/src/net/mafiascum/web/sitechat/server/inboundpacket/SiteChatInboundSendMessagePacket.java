package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundSendMessagePacket extends SiteChatInboundPacket {

  protected int userId;
  protected int siteChatConversationId;
  protected String message;
  
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public SiteChatInboundPacketType getType() {
    
    return SiteChatInboundPacketType.sendMessage;
  }

}
