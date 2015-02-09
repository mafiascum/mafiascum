package net.mafiascum.testcase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

import org.apache.log4j.Logger;
import org.junit.Assert;

public class FirstTestCase extends TestCase {
  
  private static final Logger logger = Logger.getLogger(FirstTestCase.class.getName());
  
  public void execute() throws Exception {
    
    //SiteChatServer siteChatServer = new SiteChatServer(commandLineArguments.getPort(), provider);
    
    queryUtil.executeConnectionNoReturn(provider, connection -> {
      
      //Test getting user map.
      Map<Integer, SiteChatUser> siteChatUserMap = siteChatUtil.loadSiteChatUserMap(provider.getConnection());
      logger.info("Site Chat User Map Size: " + siteChatUserMap.size());
      siteChatUserMap = null;
      
      //Test creating new conversation.
      String conversationName = miscUtil.createRandomID(20);
      SiteChatConversation originalConversation = new SiteChatConversation();
      originalConversation.setCreatedByUserId(5932);
      originalConversation.setCreatedDatetime(new Date());
      originalConversation.setName(conversationName);
      originalConversation.setPassword(stringUtil.getSHA1("abc123"));
      
      Assert.assertTrue("Conversation not new.", originalConversation.isNew());
      
      siteChatUtil.putSiteChatConversation(connection, originalConversation);
      
      Assert.assertFalse("Conversation still new.", originalConversation.isNew());
      
      //Test loading conversation by ID.
      SiteChatConversation loadedConversation = siteChatUtil.getSiteChatConversation(connection, originalConversation.getId());
      
      Assert.assertEquals(originalConversation.getId(), loadedConversation.getId());
      
      //Test loading conversation by name.
      loadedConversation = siteChatUtil.getSiteChatConversation(connection, conversationName);
      
      Assert.assertEquals(originalConversation.getId(), loadedConversation.getId());

      //Test getting all conversations.
      List<SiteChatConversation> conversations = siteChatUtil.getSiteChatConversations(provider.getConnection());
      conversations = null;
      
      //Test batch inserting messages.
      testBatchInsertingMessages(connection);

      //Test getting banned user ID set.
      siteChatUtil.getBannedUserIdSet(connection);
      
      //Test authenticate session.
      //siteChatUtil.authenticateUserLogin(connection, 5932, "3a7e81d5b89eb66a9d36128920647fc8");
    });
  }
  
  protected void testBatchInsertingMessages(Connection connection) throws SQLException {
    
    final int NUMBER_OF_MESSAGES = 5;
    int maxMessageIdBefore = siteChatUtil.getTopSiteChatConversationMessageId(connection);
    int numberOfMessagesBefore = siteChatUtil.getNumberOfSiteChatConversationMessages(connection);
    
    List<SiteChatConversationMessage> messageList = new ArrayList<SiteChatConversationMessage>();
    for(int counter = 0;counter < NUMBER_OF_MESSAGES;++counter) {
      messageList.add(generateMessage(maxMessageIdBefore + counter + 1));
    }
    
    siteChatUtil.putNewSiteChatConversationMessages(connection, messageList);
    
    int maxMessageIdAfter = siteChatUtil.getTopSiteChatConversationMessageId(connection);
    int numberOfMessagesAfter = siteChatUtil.getNumberOfSiteChatConversationMessages(connection);
    
    Assert.assertEquals(maxMessageIdBefore + NUMBER_OF_MESSAGES, maxMessageIdAfter);
    Assert.assertEquals(numberOfMessagesBefore + NUMBER_OF_MESSAGES, numberOfMessagesAfter);
  }
  
  public SiteChatConversationMessage generateMessage(int id) {
    SiteChatConversationMessage message = new SiteChatConversationMessage();
    
    message.setId(id);
    message.setCreatedDatetime(new Date());
    message.setMessage("A message!");
    message.setRecipientUserId(null);
    message.setSiteChatConversationId(1);
    message.setUserId(5932);
    
    return message;
  }
}
