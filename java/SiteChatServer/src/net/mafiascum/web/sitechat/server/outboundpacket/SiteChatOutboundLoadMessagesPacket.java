package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.List;
import java.util.Map;

import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

public class SiteChatOutboundLoadMessagesPacket extends SiteChatOutboundPacket {

  protected String conversationKey;
  protected List<SiteChatConversationMessage> messages;
  protected String errorMessage;
  protected Map<Integer, SiteChatUser> userMap;
  
  public SiteChatOutboundLoadMessagesPacket() {
    super();
  }
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.loadMessages;
  }

  public String getConversationKey() {
    return conversationKey;
  }

  public void setConversationKey(String conversationKey) {
    this.conversationKey = conversationKey;
  }

  public List<SiteChatConversationMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<SiteChatConversationMessage> messages) {
    this.messages = messages;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Map<Integer, SiteChatUser> getUserMap() {
    return userMap;
  }

  public void setUserMap(Map<Integer, SiteChatUser> userMap) {
    this.userMap = userMap;
  }
}
