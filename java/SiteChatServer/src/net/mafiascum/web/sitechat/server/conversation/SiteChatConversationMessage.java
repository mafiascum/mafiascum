package net.mafiascum.web.sitechat.server.conversation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.jdbc.BatchInsertable;
import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="siteChatConversationMessage")
public class SiteChatConversationMessage extends DataObjectWithIntID implements DataObject, BatchInsertable, Cloneable {
  
  protected int userId;
  protected Date createdDatetime;
  protected Integer siteChatConversationId;
  protected Integer recipientUserId;
  protected String message;
  
  public SiteChatConversationMessage() {
    
    id = NEW;
  }

  public SiteChatConversationMessage(int id, int userId, Date createdDatetime, Integer siteChatConversationId, Integer recipientUserId, String message) {
    
    setId(id);
    setUserId(userId);
    setCreatedDatetime(createdDatetime);
    setSiteChatConversationId(siteChatConversationId);
    setRecipientUserId(recipientUserId);
    setMessage(message);
  }
  
  public SiteChatConversationMessage clone() {
    
    return new SiteChatConversationMessage(id, userId, createdDatetime, siteChatConversationId, recipientUserId, message);
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
    
    batchInsertStatement.putInteger(isNew() ? null : getId());
    batchInsertStatement.putInteger(getSiteChatConversationId());
    batchInsertStatement.putInt(getUserId());
    batchInsertStatement.putInteger(getRecipientUserId());
    batchInsertStatement.putDate(getCreatedDatetime());
    batchInsertStatement.putString(getMessage());
    
    batchInsertStatement.endEntry();
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    setId(resultSet.getInt("id"));
    setSiteChatConversationId(QueryUtil.get().getInteger(resultSet, "site_chat_conversation_id"));
    setUserId(resultSet.getInt("user_id"));
    setCreatedDatetime(resultSet.getTimestamp("created_datetime"));
    setMessage(resultSet.getString("message"));
    setRecipientUserId(QueryUtil.get().getInteger(resultSet, "recipient_user_id"));
  }
  
  public void store(Connection connection) throws SQLException {
    
    QueryUtil.get().executeStatement(connection, statement -> {
      
      StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));

      builder.put("site_chat_conversation_id", getSiteChatConversationId())
             .put("user_id", getUserId())
             .put("created_datetime", getCreatedDatetime())
             .put("message", getMessage())
             .put("recipient_user_id", getRecipientUserId())
             .putPrimaryKey("id", isNew() ? null : getId());
      
      builder.execute(statement, this);
      return null;
    });
  }
}
