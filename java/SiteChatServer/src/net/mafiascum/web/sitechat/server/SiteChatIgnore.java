package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.misc.DataObjectWithIntID;
import net.mafiascum.web.sitechat.server.ignore.IgnorePacket;

@Table(tableName="siteChatIgnore")
public class SiteChatIgnore extends DataObjectWithIntID implements DataObject {

  public static final String ID_COLUMN = "id";
  public static final String USER_ID_COLUMN = "user_id";
  public static final String IGNORED_USER_ID_COLUMN = "ignored_user_id";
  public static final String CREATED_DATETIME_COLUMN = "created_datetime";
  
  protected int userId;
  protected int ignoredUserId;
  protected LocalDateTime createdDatetime;
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    QueryUtil queryUtil = QueryUtil.get();
    
    setId(resultSet.getInt(ID_COLUMN));
    setUserId(resultSet.getInt(USER_ID_COLUMN));
    setIgnoredUserId(resultSet.getInt(IGNORED_USER_ID_COLUMN));
    setCreatedDatetime(queryUtil.getLocalDateTime(resultSet, CREATED_DATETIME_COLUMN));
  }
  
  public void store(Connection connection) throws SQLException {
    StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));
    
    builder
    .put(USER_ID_COLUMN, getUserId())
    .put(IGNORED_USER_ID_COLUMN, getIgnoredUserId())
    .put(CREATED_DATETIME_COLUMN, getCreatedDatetime())
    .putPrimaryKey(ID_COLUMN, isNew() ? null : getId());

    builder.execute(connection, this);
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getIgnoredUserId() {
    return ignoredUserId;
  }

  public void setIgnoredUserId(int ignoredUserId) {
    this.ignoredUserId = ignoredUserId;
  }

  public LocalDateTime getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(LocalDateTime createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public IgnorePacket createPacket(SiteChatUser ignoredUser) {
    IgnorePacket ignorePacket = new IgnorePacket();
    ignorePacket.userId = getUserId();
    ignorePacket.ignoredUser = ignoredUser;
    return ignorePacket;
  }
}
