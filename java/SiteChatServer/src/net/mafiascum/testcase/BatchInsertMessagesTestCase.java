package net.mafiascum.testcase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

import org.junit.Assert;

public class BatchInsertMessagesTestCase extends TestCase {
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {

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
    });
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
