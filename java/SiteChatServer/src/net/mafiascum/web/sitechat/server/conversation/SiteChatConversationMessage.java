package net.mafiascum.web.sitechat.server.conversation;

import java.sql.SQLException;
import java.util.Date;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.jdbc.BatchInsertable;
import net.mafiascum.jdbc.Table;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="siteChatConversationMessage")
public class SiteChatConversationMessage extends DataObjectWithIntID implements BatchInsertable, Cloneable {
  
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
  
  public void setBatchInsertStatementColumns(BatchInsertStatement batchInsertStatement) throws SQLException {
    batchInsertStatement.addField("id");
    batchInsertStatement.addField("site_chat_conversation_id");
    batchInsertStatement.addField("user_id");
    batchInsertStatement.addField("recipient_user_id");
    batchInsertStatement.addField("created_datetime");
    batchInsertStatement.addField("message");
  }
  
  public void addToBatchInsertStatement(BatchInsertStatement batchInsertStatement) throws SQLException {
    batchInsertStatement.beginEntry();
    
    batchInsertStatement.putInt(getId());
    batchInsertStatement.putInteger(getSiteChatConversationId());
    batchInsertStatement.putInt(getUserId());
    batchInsertStatement.putInteger(getRecipientUserId());
    batchInsertStatement.putDate(getCreatedDatetime());
    batchInsertStatement.putString(getMessage());
    
    batchInsertStatement.endEntry();
  }
}
