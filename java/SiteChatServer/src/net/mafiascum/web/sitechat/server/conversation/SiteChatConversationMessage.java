package net.mafiascum.web.sitechat.server.conversation;

import java.util.Date;

import net.mafiascum.web.misc.DataObjectWithIntId;

public class SiteChatConversationMessage extends DataObjectWithIntId {
  
  protected int userId;
  protected Date createdDatetime;
  protected Integer siteChatConversationId;
  protected Integer recipientUserId;
  protected String message;
  
  public SiteChatConversationMessage() {
    
    id = NEW;
  }

  public SiteChatConversationMessage clone() {
    
    SiteChatConversationMessage siteChatConversationMessage = new SiteChatConversationMessage();
    
    siteChatConversationMessage.setId(id);
    siteChatConversationMessage.setUserId(userId);
    siteChatConversationMessage.setCreatedDatetime(createdDatetime);
    siteChatConversationMessage.setSiteChatConversationId(siteChatConversationId);
    siteChatConversationMessage.setRecipientUserId(recipientUserId);
    siteChatConversationMessage.setMessage(message);
    
    return siteChatConversationMessage;
  }
  
  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Date getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Date createdDatetime) {
    this.createdDatetime = createdDatetime;
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
}
