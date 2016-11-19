package net.mafiascum.web.sitechat.server.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;

import org.junit.Assert;
import org.junit.Test;
public class SiteChatServerTest {

  protected final int CONVERSATION_ID = 11;
  protected final int USER_ID = 5932;
  protected Provider provider = mock(Provider.class);
  protected SiteChatUtil siteChatUtil = mock(SiteChatUtil.class);
  
  protected SiteChatMessageProcessor getProcessor() {
    SiteChatMessageProcessor processor = new SiteChatMessageProcessor();
    processor.setProvider(provider);
    processor.setSiteChatUtil(siteChatUtil);
    return processor;
  }
  
  @Test
  public void testLoadHistoricalMessagesForPrivateMessage() throws Exception {
    
    when(provider.getConnection()).thenReturn(mock(Connection.class));
    when(siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(any(), anyInt(), anyInt(), anyInt(), anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatMessageProcessor processor = getProcessor();
    
    List<SiteChatConversationMessage> messages;
    
    messages = processor.loadHistoricalMessages(USER_ID, SiteChatConversationType.Private, USER_ID, null);
    
    Assert.assertEquals(3, messages.size());
  }
  
  @Test
  public void testLoadHistoricalMessagesForConversationWithAccess() throws Exception {
    
    when(provider.getConnection()).thenReturn(mock(Connection.class));
    when(siteChatUtil.loadSiteChatConversationMessagesForConversation(any(), anyInt(), anyInt(), anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatMessageProcessor processor = getProcessor();
    
    SiteChatConversationWithUserList conversation = new SiteChatConversationWithUserList();
    conversation.setUserIdSet(MiscUtil.get().makeHashSet(USER_ID));
    Map<Integer, SiteChatConversationWithUserList> conversationMap = new HashMap<Integer, SiteChatConversationWithUserList>();
    conversationMap.put(CONVERSATION_ID, conversation);
    processor.setSiteChatConversationWithMemberListMap(conversationMap);
    
    List<SiteChatConversationMessage> messages;
    
    messages = processor.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
    
    Assert.assertEquals(3, messages.size());
  }
  
  @Test(expected=SiteChatException.class)
  public void testLoadHistoricalMessagesForConversationWithNoAccess() throws Exception {
    
    when(provider.getConnection()).thenReturn(mock(Connection.class));
    when(siteChatUtil.loadSiteChatConversationMessagesForConversation(any(), anyInt(), anyInt(), anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatMessageProcessor processor = getProcessor();
    
    SiteChatConversationWithUserList conversation = new SiteChatConversationWithUserList();
    conversation.setUserIdSet(MiscUtil.get().makeHashSet());
    Map<Integer, SiteChatConversationWithUserList> conversationMap = new HashMap<Integer, SiteChatConversationWithUserList>();
    conversationMap.put(CONVERSATION_ID, conversation);
    processor.setSiteChatConversationWithMemberListMap(conversationMap);
    
    processor.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
  }
  
  @Test(expected=SiteChatException.class)
  public void testLoadHistoricalMessagesForConversationWithInvalidConversation() throws Exception {
    
    when(provider.getConnection()).thenReturn(mock(Connection.class));
    when(siteChatUtil.loadSiteChatConversationMessagesForConversation(any(), anyInt(), anyInt(), anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatMessageProcessor processor = getProcessor();
    
    processor.setSiteChatConversationWithMemberListMap(new HashMap<Integer, SiteChatConversationWithUserList>());
    
    processor.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
  }
}
