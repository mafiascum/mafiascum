package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundSendMessagePacket extends SiteChatInboundPacket {

  protected Integer siteChatConversationId;
  protected Integer recipientUserId;
  protected String message;
  
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
