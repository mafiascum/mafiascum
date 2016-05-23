package net.mafiascum.web.sitechat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.mysql.jdbc.Connection;

public class SiteChatUtilTest {

  protected SiteChatUtil siteChatUtil = SiteChatUtil.get();
  
  @Test
  public void testGenerateConversationAuthCode() {
    
    int siteChatUserId = 1;
    int siteChatConversationId = 25;
    String passwordSha1 = "6367c48dd193d56ea7b0baad25b19455e529f5ee";
    
    Assert.assertEquals("671fa6507853a21d76c2e2e7b64d8261a4e6b68f", siteChatUtil.generateConversationAuthCode(siteChatUserId, siteChatConversationId, passwordSha1));
  }
  
  @Test
  public void testGetPrivateMessageHistoryKey() {
    
    int userId1 = 5932;
    int userId2 = 7086;
    
    Assert.assertEquals("5932_7086", siteChatUtil.getPrivateMessageHistoryKey(userId1, userId2));
    Assert.assertEquals("5932_7086", siteChatUtil.getPrivateMessageHistoryKey(userId2, userId1));
  }
  
  @Test
  public void testGetSiteChatConversationTypeBySymbol() {
    
    Assert.assertEquals(SiteChatConversationType.Conversation, siteChatUtil.getSiteChatConversationTypeBySymbol('C'));
    Assert.assertEquals(SiteChatConversationType.Private, siteChatUtil.getSiteChatConversationTypeBySymbol('P'));
  }
  
  @Test
  public void testGetConversationSymbol() {
    
    Assert.assertEquals('P', siteChatUtil.getConversationSymbol("P1234"));
    Assert.assertEquals('C', siteChatUtil.getConversationSymbol("C5932"));
  }
  
  @Test
  public void testGetConversationUniqueIdentifier() {
    
    Assert.assertEquals(1234, siteChatUtil.getConversationUniqueIdentifier("P1234"));
    Assert.assertEquals(5932, siteChatUtil.getConversationUniqueIdentifier("C5932"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testPutNewSiteChatConversationMessages() throws SQLException {
    
    BatchInsertStatement batchInsertStatement = Mockito.mock(BatchInsertStatement.class);
    Connection connection = Mockito.mock(Connection.class);
    SiteChatUtil mockedSiteChatUtil = Mockito.spy(siteChatUtil);
    List<SiteChatConversationMessage> messages = new ArrayList<SiteChatConversationMessage>();
    
    Mockito.doNothing().when(mockedSiteChatUtil).putNewSiteChatConversationMessages((List<SiteChatConversationMessage>) Mockito.any(), (BatchInsertStatement)Mockito.any());
    
    mockedSiteChatUtil.putNewSiteChatConversationMessages(connection, messages);
    
    Mockito.verify(mockedSiteChatUtil, Mockito.times(1)).putNewSiteChatConversationMessages((List<SiteChatConversationMessage>) Mockito.any(), (BatchInsertStatement)Mockito.any());
    
    //Verify works with empty list.
    siteChatUtil.putNewSiteChatConversationMessages(messages, batchInsertStatement);
    
    //Verify with list entries.
    messages.add(Mockito.mock(SiteChatConversationMessage.class));
    messages.add(Mockito.mock(SiteChatConversationMessage.class));
    messages.add(Mockito.mock(SiteChatConversationMessage.class));
    
    siteChatUtil.putNewSiteChatConversationMessages(messages, batchInsertStatement);
    
    Mockito.verify(messages.get(0), Mockito.times(1)).setBatchInsertStatementColumns(batchInsertStatement);
    for(SiteChatConversationMessage message : messages) {
      Mockito.verify(message, Mockito.times(1)).addToBatchInsertStatement(batchInsertStatement);
    }
    Mockito.verify(batchInsertStatement, Mockito.times(1)).finish();
  }
  
  @Test
  public void testPutSiteChatConversation() throws SQLException {
    Connection connection = Mockito.mock(Connection.class);
    SiteChatConversation conversation = Mockito.mock(SiteChatConversation.class);
    
    siteChatUtil.putSiteChatConversation(connection, conversation);
    
    Mockito.verify(conversation, Mockito.times(1)).store(connection);
  }
}
