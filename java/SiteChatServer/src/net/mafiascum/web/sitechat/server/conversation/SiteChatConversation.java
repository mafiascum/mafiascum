package net.mafiascum.web.sitechat.server.conversation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="siteChatConversation")
public class SiteChatConversation extends DataObjectWithIntID implements DataObject {

  protected Date createdDatetime;
  protected int createdByUserId;
  protected String name;
  protected String password;
  
  public SiteChatConversation() {
    
    id = NEW;
  }
  
  public Date getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Date createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public int getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(int createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    setId(resultSet.getInt("id"));
    setName(resultSet.getString("name"));
    setCreatedDatetime(resultSet.getTimestamp("created_datetime"));
    setCreatedByUserId(resultSet.getInt("created_by_user_id"));
    setPassword(resultSet.getString("password"));
  }
  
  public void store(Connection connection) throws SQLException {
    
    QueryUtil.get().executeStatement(connection, statement -> {
      
      StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));

      builder.put("name", getName())
             .put("created_datetime", getCreatedDatetime())
             .put("created_by_user_id", getCreatedByUserId())
             .put("password", getPassword())
             .putPrimaryKey("id", isNew() ? null : getId());
      
      builder.execute(statement, this);
      
      return null;
    });
  }
}
