package net.mafiascum.web.sitechat.server.inboundpacket;


public class SiteChatInboundLoadMessagesPacket extends SiteChatInboundPacket {
  
  protected String conversationKey;
  protected Integer oldestMessageId;
  
  public SiteChatInboundLoadMessagesPacket(String conversationKey, Integer oldestMessageId) {
    setConversationKey(conversationKey);
    setOldestMessageId(oldestMessageId);
  }
  
  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.loadMessages;
  }

  public String getConversationKey() {
    return conversationKey;
  }

  public void setConversationKey(String conversationKey) {
    this.conversationKey = conversationKey;
  }

  public Integer getOldestMessageId() {
    return oldestMessageId;
  }

  public void setOldestMessageId(Integer oldestMessageId) {
    this.oldestMessageId = oldestMessageId;
  }
}
