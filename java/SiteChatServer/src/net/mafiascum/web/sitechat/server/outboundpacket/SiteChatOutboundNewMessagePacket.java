package net.mafiascum.web.sitechat.server.outboundpacket;

public class SiteChatOutboundNewMessagePacket extends SiteChatOutboundPacket {

  protected int userId;
  protected int siteChatConversationId;
  protected int siteChatConversationMessageId;
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
  public int getSiteChatConversationMessageId() {
    return siteChatConversationMessageId;
  }
  public void setSiteChatConversationMessageId(int siteChatConversationMessageId) {
    this.siteChatConversationMessageId = siteChatConversationMessageId;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  
  public SiteChatOutboundPacketType getType() {
  
    return SiteChatOutboundPacketType.newMessage;
  }
}
