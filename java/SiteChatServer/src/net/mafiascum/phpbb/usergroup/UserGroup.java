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
         resultSet.getInt("group_id"),
         resultSet.getInt("user_id"),
         QueryUtil.get().getIntBoolean(resultSet, "group_leader"),
         QueryUtil.get().getIntBoolean(resultSet, "user_pending"),
         resultSet.getInt("auto_remove_time")
    );
  }
  
  public void store(Connection connection) throws SQLException {
    
    QueryUtil.get().executeStatement(connection, statement -> {
      
      StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));
      
      builder.put("group_id", getGroupId())
             .put("user_id", getUserId())
             .put("group_leader", getGroupLeader())
             .put("user_pending", getUserPending())
             .put("auto_remove_time", getAutoRemoveTime())
             .putPrimaryKey("group_id", getGroupId())
             .putPrimaryKey("user_id", getUserId());
      
      builder.execute(statement, this);
      
      return null;
    });
  }
}
