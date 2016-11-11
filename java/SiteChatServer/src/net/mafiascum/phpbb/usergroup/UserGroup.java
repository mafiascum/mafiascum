package net.mafiascum.phpbb.usergroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.IsNewDataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;

@Table(tableName="phpbb_user_group")
public class UserGroup implements DataObject, IsNewDataObject {

  public static final String GROUP_ID_COLUMN = "group_id";
  public static final String USER_ID_COLUMN = "user_id";
  public static final String GROUP_LEADER_COLUMN = "group_leader";
  public static final String USER_PENDING_COLUMN = "user_pending";
  public static final String AUTO_REMOVE_TIME_COLUMN = "auto_remove_time";
  
  protected int groupId;
  protected int userId;
  protected boolean groupLeader;
  protected boolean userPending;
  protected int autoRemoveTime;
  
  protected boolean isNew;
  
  public UserGroup() {
    this.isNew = true;
  }
  
  public UserGroup(boolean isNew, int groupId, int userId, boolean groupLeader, boolean userPending, int autoRemoveTime) {
    this();
    init(isNew, groupId, userId, groupLeader, userPending, autoRemoveTime);
  }
  
  public void init(boolean isNew, int groupId, int userId, boolean groupLeader, boolean userPending, int autoRemoveTime) {
    setIsNew(isNew);
    setGroupId(groupId);
    setUserId(userId);
    setGroupLeader(groupLeader);
    setUserPending(userPending);
    setAutoRemoveTime(autoRemoveTime);
  }
  
  public int getGroupId() {
    return groupId;
  }
  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public boolean getGroupLeader() {
    return groupLeader;
  }
  public void setGroupLeader(boolean groupLeader) {
    this.groupLeader = groupLeader;
  }
  public boolean getUserPending() {
    return userPending;
  }
  public void setUserPending(boolean userPending) {
    this.userPending = userPending;
  }
  public int getAutoRemoveTime() {
    return autoRemoveTime;
  }
  public void setAutoRemoveTime(int autoRemoveTime) {
    this.autoRemoveTime = autoRemoveTime;
  }
  public void setIsNew(boolean isNew) {
    this.isNew = isNew;
  }

  public boolean isNew() {
    return isNew;
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    init(false,
         resultSet.getInt(GROUP_ID_COLUMN),
         resultSet.getInt(USER_ID_COLUMN),
         QueryUtil.get().getIntBoolean(resultSet, GROUP_LEADER_COLUMN),
         QueryUtil.get().getIntBoolean(resultSet, USER_PENDING_COLUMN),
         resultSet.getInt(AUTO_REMOVE_TIME_COLUMN)
    );
  }
  
  public void store(Connection connection) throws SQLException {
    storeInsertIgnore(connection, false);
  }
  
  public void storeInsertIgnore(Connection connection, boolean insertIgnore) throws SQLException {
    QueryUtil.get().executeStatement(connection, statement -> {
      
      StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));
      
      builder.put(GROUP_ID_COLUMN, getGroupId())
             .put(USER_ID_COLUMN, getUserId())
             .put(GROUP_LEADER_COLUMN, getGroupLeader())
             .put(USER_PENDING_COLUMN, getUserPending())
             .put(AUTO_REMOVE_TIME_COLUMN, getAutoRemoveTime())
             .putPrimaryKey(GROUP_ID_COLUMN, getGroupId())
             .putPrimaryKey(USER_ID_COLUMN, getUserId());
      
      builder.setInsertIgnore(insertIgnore);
      builder.execute(statement, this);
      
      return null;
    });    
  }
}
