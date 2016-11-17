package net.mafiascum.phpbb.log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="phpbb_log")
public class ForumLog extends DataObjectWithIntID implements DataObject {
  
  public static final String ID_COLUMN = "log_id";
  public static final String LOG_TYPE_COLUMN = "log_type";
  public static final String USER_ID_COLUMN = "user_id";
  public static final String FORUM_ID_COLUMN = "forum_id";
  public static final String TOPIC_ID_COLUMN = "topic_id";
  public static final String REPORTEE_ID_COLUMN = "reportee_id";
  public static final String LOG_IP_COLUMN = "log_ip";
  public static final String LOG_TIME_COLUMN = "log_time";
  public static final String LOG_OPERATION_COLUMN = "log_operation";
  public static final String LOG_DATA_COLUMN = "log_data";
  
  protected ForumLogType logType;
  protected int userId;
  protected int forumId;
  protected int topicId;
  protected int reporteeId;
  protected String logIp;
  protected long logTime;
  protected String logOperation;
  protected String logData;

  public ForumLog(ForumLogType logType, int userId, int forumId, int topicId, int reporteeId, String logIp, long logTime, String logOperation, String logData) {
    setLogType(logType);
    setUserId(userId);
    setForumId(forumId);
    setTopicId(topicId);
    setReporteeId(reporteeId);
    setLogIp(logIp);
    setLogTime(logTime);
    setLogOperation(logOperation);
    setLogData(logData);
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    setId(resultSet.getInt(ID_COLUMN));
    setLogType(ForumLogType.getEnum(resultSet.getInt(LOG_TYPE_COLUMN)));
    setUserId(resultSet.getInt(USER_ID_COLUMN));
    setForumId(resultSet.getInt(FORUM_ID_COLUMN));
    setTopicId(resultSet.getInt(TOPIC_ID_COLUMN));
    setReporteeId(resultSet.getInt(REPORTEE_ID_COLUMN));
    setLogIp(resultSet.getString(LOG_IP_COLUMN));
    setLogTime(resultSet.getLong(LOG_TIME_COLUMN));
    setLogOperation(resultSet.getString(LOG_OPERATION_COLUMN));
    setLogData(resultSet.getString(LOG_DATA_COLUMN));
  }

  public void store(Connection connection) throws SQLException {
    StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));
    
    builder
    .put(LOG_TYPE_COLUMN, getLogType())
    .put(USER_ID_COLUMN, getUserId())
    .put(FORUM_ID_COLUMN, getForumId())
    .put(TOPIC_ID_COLUMN, getTopicId())
    .put(REPORTEE_ID_COLUMN, getReporteeId())
    .put(LOG_IP_COLUMN, getLogIp())
    .put(LOG_TIME_COLUMN, getLogTime())
    .put(LOG_OPERATION_COLUMN, getLogOperation())
    .put(LOG_DATA_COLUMN, getLogData())
    .putPrimaryKey(ID_COLUMN, isNew() ? null : getId());
    
    builder.execute(connection, this);
  }

  public ForumLogType getLogType() {
    return logType;
  }

  public void setLogType(ForumLogType logType) {
    this.logType = logType;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getForumId() {
    return forumId;
  }

  public void setForumId(int forumId) {
    this.forumId = forumId;
  }

  public int getTopicId() {
    return topicId;
  }

  public void setTopicId(int topicId) {
    this.topicId = topicId;
  }

  public int getReporteeId() {
    return reporteeId;
  }

  public void setReporteeId(int reporteeId) {
    this.reporteeId = reporteeId;
  }

  public String getLogIp() {
    return logIp;
  }

  public void setLogIp(String logIp) {
    this.logIp = logIp;
  }

  public long getLogTime() {
    return logTime;
  }

  public void setLogTime(long logTime) {
    this.logTime = logTime;
  }

  public String getLogOperation() {
    return logOperation;
  }

  public void setLogOperation(String logOperation) {
    this.logOperation = logOperation;
  }

  public String getLogData() {
    return logData;
  }

  public void setLogData(String logData) {
    this.logData = logData;
  }
}
