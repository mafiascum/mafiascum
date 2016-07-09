package net.mafiascum.testcase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

import org.junit.Assert;


public class LoadHistoryMessagesTestCase extends TestCase {
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {
      
      testConversationAllExisting(connection);
      testConversationSomeMissing(connection);
      testConversationNoOldestMessage(connection);
      
      testPrivateAllExisting(connection);
      testPrivateSomeMissing(connection);
      testPrivateNoOldestMessage(connection);
    });
  }
  
  protected void testPrivateNoOldestMessage(Connection connection) throws SQLException {
    
    final int USER_ID_1 = 5932;
    final int USER_ID_2 = 2302;
    final int NUMBER_TO_LOAD = 5;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(connection, USER_ID_1, USER_ID_2, NUMBER_TO_LOAD, null);
    
    Assert.assertEquals(NUMBER_TO_LOAD, messages.size());
    Assert.assertEquals(44, messages.get(0).getId());
    Assert.assertEquals(43, messages.get(1).getId());
    Assert.assertEquals(42, messages.get(2).getId());
    Assert.assertEquals(41, messages.get(3).getId());
    Assert.assertEquals(40, messages.get(4).getId());
  }
  
  protected void testConversationNoOldestMessage(Connection connection) throws SQLException {
    
    final int NUMBER_TO_LOAD = 5;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForConversation(connection, LOBBY_CONVERSATION_ID, NUMBER_TO_LOAD, null);
    
    Assert.assertEquals(NUMBER_TO_LOAD, messages.size());
    Assert.assertEquals(30, messages.get(0).getId());
    Assert.assertEquals(29, messages.get(1).getId());
    Assert.assertEquals(28, messages.get(2).getId());
    Assert.assertEquals(27, messages.get(3).getId());
    Assert.assertEquals(26, messages.get(4).getId()); 
  }
  
  protected void testPrivateSomeMissing(Connection connection) throws SQLException {
    
    final int USER_ID_1 = 5932;
    final int USER_ID_2 = 2302;
    final int NUMBER_TO_LOAD = 5;
    final int OLDEST_MESSAGE_ID = 33;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(connection, USER_ID_1, USER_ID_2, NUMBER_TO_LOAD, OLDEST_MESSAGE_ID);
    
    Assert.assertEquals(2, messages.size());
    Assert.assertEquals(32, messages.get(0).getId());
    Assert.assertEquals(31, messages.get(1).getId());
  }
  
  protected void testPrivateAllExisting(Connection connection) throws SQLException {
    
    final int USER_ID_1 = 5932;
    final int USER_ID_2 = 2302;
    final int NUMBER_TO_LOAD = 5;
    final int OLDEST_MESSAGE_ID = 37;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(connection, USER_ID_1, USER_ID_2, NUMBER_TO_LOAD, OLDEST_MESSAGE_ID);
    
    Assert.assertEquals(NUMBER_TO_LOAD, messages.size());
    Assert.assertEquals(36, messages.get(0).getId());
    Assert.assertEquals(35, messages.get(1).getId());
    Assert.assertEquals(34, messages.get(2).getId());
    Assert.assertEquals(33, messages.get(3).getId());
    Assert.assertEquals(32, messages.get(4).getId());
  }
  
  protected void testConversationSomeMissing(Connection connection) throws SQLException {
    
    final int NUMBER_TO_LOAD = 5;
    final int OLDEST_MESSAGE_ID = 3;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForConversation(connection, LOBBY_CONVERSATION_ID, NUMBER_TO_LOAD, OLDEST_MESSAGE_ID);
    
    Assert.assertEquals(2, messages.size());
    Assert.assertEquals(2, messages.get(0).getId());
    Assert.assertEquals(1, messages.get(1).getId());   
  }
  
  protected void testConversationAllExisting(Connection connection) throws SQLException {
    
    final int NUMBER_TO_LOAD = 5;
    final int OLDEST_MESSAGE_ID = 14;
    
    List<SiteChatConversationMessage> messages = siteChatUtil.loadSiteChatConversationMessagesForConversation(connection, LOBBY_CONVERSATION_ID, NUMBER_TO_LOAD, OLDEST_MESSAGE_ID);
    
    Assert.assertEquals(NUMBER_TO_LOAD, messages.size());
    Assert.assertEquals(13, messages.get(0).getId());
    Assert.assertEquals(12, messages.get(1).getId());
    Assert.assertEquals(11, messages.get(2).getId());
    Assert.assertEquals(10, messages.get(3).getId());
    Assert.assertEquals( 9, messages.get(4).getId());    
  }
}
