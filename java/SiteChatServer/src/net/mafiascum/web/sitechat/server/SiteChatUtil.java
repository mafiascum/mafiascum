package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.util.SQLUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

public class SiteChatUtil {

  public static final int MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH = 255;
  public static final int MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH = 40;
  
  public static Map<Integer, SiteChatUser> loadSiteChatUserMap(Connection connection) throws SQLException {
    
    Statement statement = connection.createStatement();
    Map<Integer, SiteChatUser> siteChatUserMap = new HashMap<Integer, SiteChatUser>();
    String sql;
    ResultSet resultSet;
    int topUserId, offset = 0, fetchSize = 1000;
    
    sql = " SELECT MAX(user_id)"
        + " FROM phpbb_users";
    
    topUserId = QueryUtil.getSingleIntValueResult(statement, sql);
    
    while(offset <= topUserId) {

      sql = " SELECT"
          + "   user_id,"
          + "   username,"
          + "   user_avatar"
          + " FROM phpbb_users"
          + " WHERE user_id >= " + offset
          + " AND user_id < " + (offset + fetchSize);
      
      resultSet = statement.executeQuery(sql);
      
      while(resultSet.next()) {
        
        SiteChatUser siteChatUser = getSiteChatUser(resultSet);
        siteChatUserMap.put(siteChatUser.getId(), siteChatUser);
      }
      
      offset += fetchSize;
    }
    
    statement.close();
    
    return siteChatUserMap;
  }
  
  public static SiteChatUser getSiteChatUser(ResultSet resultSet) throws SQLException {
    
    SiteChatUser siteChatUser = new SiteChatUser();
    
    siteChatUser.setId(resultSet.getInt("user_id"));
    siteChatUser.setName(resultSet.getString("username"));
    siteChatUser.setAvatarUrl("http://forum.mafiascum.net/download/file.php?avatar=" + resultSet.getString("user_avatar"));
    
    return siteChatUser;
  }
  
  public static List<SiteChatConversation> getSiteChatConversations(Connection connection) throws SQLException {
    
    Statement statement = connection.createStatement();
    List<SiteChatConversation> siteChatConversations = new LinkedList<SiteChatConversation>();
    String sql;
    ResultSet resultSet;
    int topUserId, offset = 0, fetchSize = 1000;
    
    sql = " SELECT MAX(id)"
        + " FROM siteChatConversation";
    
    topUserId = QueryUtil.getSingleIntValueResult(statement, sql);
    
    while(offset <= topUserId) {

      sql = " SELECT *"
          + " FROM siteChatConversation"
          + " WHERE id >= " + offset
          + " AND id < " + (offset + fetchSize);
      
      resultSet = statement.executeQuery(sql);
      
      while(resultSet.next()) {
        
        SiteChatConversation siteChatConversation = getSiteChatConversation(resultSet);
        siteChatConversations.add(siteChatConversation);
      }
      
      offset += fetchSize;
    }
    
    statement.close();
    
    return siteChatConversations;
  }
  
  public static SiteChatConversation getSiteChatConversation(Connection connection, int siteChatConversationId) throws SQLException {
    
    ResultSet resultSet;
    SiteChatConversation siteChatConversation = null;
    String sql;
    
    sql = " SELECT *"
        + " FROM siteChatConversation"
        + " WHERE id = ?";
    
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    
    preparedStatement.setInt(1, siteChatConversationId);
    
    resultSet = preparedStatement.executeQuery();
    
    if(resultSet.next()) {
      
      siteChatConversation = getSiteChatConversation(resultSet);
    }
    
    preparedStatement.close();
    
    return siteChatConversation;
  }
  
  public static SiteChatConversation getSiteChatConversation(Connection connection, String siteChatConversationName) throws SQLException {
    
    ResultSet resultSet;
    SiteChatConversation siteChatConversation = null;
    String sql;
    
    sql = " SELECT *"
        + " FROM siteChatConversation"
        + " WHERE name = ?";
    
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    
    preparedStatement.setString(1, siteChatConversationName);
    
    resultSet = preparedStatement.executeQuery();
    
    if(resultSet.next()) {
      
      siteChatConversation = getSiteChatConversation(resultSet);
    }
    
    preparedStatement.close();
    
    return siteChatConversation;
  }
  
  public static SiteChatConversation getSiteChatConversation(ResultSet resultSet) throws SQLException {
    
    SiteChatConversation siteChatConversation = new SiteChatConversation();
    
    siteChatConversation.setId(resultSet.getInt("id"));
    siteChatConversation.setName(resultSet.getString("name"));
    siteChatConversation.setCreatedDatetime(resultSet.getTimestamp("created_datetime"));
    siteChatConversation.setCreatedByUserId(resultSet.getInt("created_by_user_id"));
    
    return siteChatConversation;
  }
  
  public static void putSiteChatConversation(Connection connection, SiteChatConversation siteChatConversation) throws SQLException {
    
    String sql;
    
    if(siteChatConversation.isNew()) {
      
      sql = " INSERT INTO siteChatConversation("
          + "   `name`,"
          + "   `created_datetime`,"
          + "   `created_by_user_id`"
          + " ) VALUES("
          + "   ?,"
          + "   ?,"
          + "   ?"
          + " )";
      
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      
      preparedStatement.setString(1, siteChatConversation.getName());
      preparedStatement.setTimestamp(2, SQLUtil.getTimestamp(siteChatConversation.getCreatedDatetime()));
      preparedStatement.setInt(3, siteChatConversation.getCreatedByUserId());
      
      preparedStatement.executeUpdate();
      
      siteChatConversation.setId(QueryUtil.getLastInsertedID(connection));
    }
    else {
      
      sql = " UPDATE siteChatConversation SET"
          + "   name = ?,"
          + "   created_datetime = ?,"
          + "   created_by_user_id = ?"
          + " WHERE id = ?";
      
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      
      preparedStatement.setString(1, siteChatConversation.getName());
      preparedStatement.setTimestamp(2, SQLUtil.getTimestamp(siteChatConversation.getCreatedDatetime()));
      preparedStatement.setInt(3, siteChatConversation.getCreatedByUserId());
      preparedStatement.setInt(4, siteChatConversation.getId());
      
      preparedStatement.executeUpdate();
    }
  }
  
  public static boolean authenticateUserLogin(Connection connection, int userId, String sessionId) throws SQLException {
    
    Statement statement = connection.createStatement();
    String sql;
    
    sql = " SELECT 1"
        + " FROM phpbb_sessions"
        + " WHERE session_id = " + SQLUtil.escapeQuoteString(sessionId)
        + " AND session_user_id = " + userId;
    
    return QueryUtil.hasAtLeastOneRow(statement, sql);
  }
  
  public static void putNewSiteChatConversationMessages(Connection connection, List<SiteChatConversationMessage> siteChatConversationMessages) throws SQLException {
    
    BatchInsertStatement batchInsertStatement = new BatchInsertStatement(connection, "siteChatConversationMessage", siteChatConversationMessages.size() + 1);

    batchInsertStatement.addField("id");
    batchInsertStatement.addField("site_chat_conversation_id");
    batchInsertStatement.addField("user_id");
    batchInsertStatement.addField("created_datetime");
    batchInsertStatement.addField("message");
    
    batchInsertStatement.start();
    
    for(SiteChatConversationMessage siteChatConversationMessage : siteChatConversationMessages) {
      
      batchInsertStatement.beginEntry();
      
      batchInsertStatement.putInt(siteChatConversationMessage.getId());
      batchInsertStatement.putInt(siteChatConversationMessage.getSiteChatConversationId());
      batchInsertStatement.putInt(siteChatConversationMessage.getUserId());
      batchInsertStatement.putDate(siteChatConversationMessage.getCreatedDatetime());
      batchInsertStatement.putString(siteChatConversationMessage.getMessage());
      
      batchInsertStatement.endEntry();
    }
    
    batchInsertStatement.finish();
  }
  
  public static int getTopSiteChatConversationMessageId(Connection connection) throws SQLException {
    
    String sql = " SELECT MAX(id) AS id"
               + " FROM siteChatConversationMessage";
    
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery(sql);
    int topId = 0;
    
    if(resultSet.next()) {
      
      topId = resultSet.getInt("id");
    }
    
    resultSet.close();
    statement.close();
    
    return topId;
  }
}
