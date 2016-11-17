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

  public static final String ID_COLUMN = "id";
  public static final String USER_ID_COLUMN = "user_id";
  public static final String CREATED_DATETIME_COLUMN = "created_datetime";
  public static final String SITE_CHAT_CONVERSATION_ID_COLUMN = "site_chat_conversation_id";
  public static final String RECIPIENT_USER_ID_COLUMN = "recipient_user_id";
  public static final String MESSAGE_COLUMN = "message";
  
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
    batchInsertStatement.addField(ID_COLUMN);
    batchInsertStatement.addField(SITE_CHAT_CONVERSATION_ID_COLUMN);
    batchInsertStatement.addField(USER_ID_COLUMN);
    batchInsertStatement.addField(RECIPIENT_USER_ID_COLUMN);
    batchInsertStatement.addField(CREATED_DATETIME_COLUMN);
    batchInsertStatement.addField(MESSAGE_COLUMN);
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
    
    setId(resultSet.getInt(ID_COLUMN));
    setSiteChatConversationId(QueryUtil.get().getInteger(resultSet, SITE_CHAT_CONVERSATION_ID_COLUMN));
    setUserId(resultSet.getInt(USER_ID_COLUMN));
    setCreatedDatetime(resultSet.getTimestamp(CREATED_DATETIME_COLUMN));
    setMessage(resultSet.getString(MESSAGE_COLUMN));
    setRecipientUserId(QueryUtil.get().getInteger(resultSet, RECIPIENT_USER_ID_COLUMN));
  }
  
  public void store(Connection connection) throws SQLException {
    
    StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));

    builder.put(SITE_CHAT_CONVERSATION_ID_COLUMN, getSiteChatConversationId())
           .put(USER_ID_COLUMN, getUserId())
           .put(CREATED_DATETIME_COLUMN, getCreatedDatetime())
           .put(MESSAGE_COLUMN, getMessage())
           .put(RECIPIENT_USER_ID_COLUMN, getRecipientUserId())
           .putPrimaryKey(ID_COLUMN, isNew() ? null : getId());
    
    builder.execute(connection, this);
  }
}
