package net.mafiascum.testcase;

import java.util.Date;
import java.util.List;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;

import org.apache.log4j.Logger;
import org.junit.Assert;

public class ConversationsTestCase extends TestCase {
  
  private static final Logger logger = Logger.getLogger(ConversationsTestCase.class.getName());
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {
      
      logger.info("Testing conversations.");
      
      //Test getting all conversations.
      List<SiteChatConversation> conversations = siteChatUtil.getSiteChatConversations(connection);
      Assert.assertEquals(3, conversations.size());
      
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
    });
  }
}
