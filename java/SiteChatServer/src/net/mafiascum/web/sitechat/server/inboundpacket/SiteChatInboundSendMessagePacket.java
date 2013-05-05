package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundSendMessagePacket extends SiteChatInboundPacket {

  protected int userId;
  protected Integer siteChatConversationId;
  protected Integer recipientUserId;
  protected String message;
  
  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Integer getSiteChatConversationId() {
    return siteChatConversationId;
  }

  public void setSiteChatConversationId(Integer siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }
  
  public Integer getRecipientUserId() {
    return recipientUserId;
  }
  
  public void setRecipientUserId(Integer recipientUserId) {
    this.recipientUserId = recipientUserId;
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
