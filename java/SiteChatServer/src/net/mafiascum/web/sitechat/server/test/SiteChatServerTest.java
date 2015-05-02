package net.mafiascum.web.sitechat.server.test;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SiteChatServerTest {

  protected final int CONVERSATION_ID = 11;
  protected final int USER_ID = 5932;
  protected Provider provider = Mockito.mock(Provider.class);
  protected SiteChatUtil siteChatUtil = Mockito.mock(SiteChatUtil.class);
  
  protected SiteChatServer getSiteChatServer() {
    SiteChatServer siteChatServer = new SiteChatServer(provider);
    siteChatServer.setSiteChatUtil(siteChatUtil);
    return siteChatServer;
  }
  
  @Test
  public void testLoadHistoricalMessagesForPrivateMessage() throws Exception {
    
    Mockito.when(provider.getConnection()).thenReturn(Mockito.mock(Connection.class));
    Mockito.when(siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatServer siteChatServer = getSiteChatServer();
    
    List<SiteChatConversationMessage> messages;
    
    messages = siteChatServer.loadHistoricalMessages(USER_ID, SiteChatConversationType.Private, USER_ID, null);
    
    Assert.assertEquals(3, messages.size());
  }
  
  @Test
  public void testLoadHistoricalMessagesForConversationWithAccess() throws Exception {
    
    Mockito.when(provider.getConnection()).thenReturn(Mockito.mock(Connection.class));
    Mockito.when(siteChatUtil.loadSiteChatConversationMessagesForConversation(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatServer siteChatServer = getSiteChatServer();
    
    SiteChatConversationWithUserList conversation = new SiteChatConversationWithUserList();
    conversation.setUserIdSet(MiscUtil.get().makeHashSet(USER_ID));
    Map<Integer, SiteChatConversationWithUserList> conversationMap = new HashMap<Integer, SiteChatConversationWithUserList>();
    conversationMap.put(CONVERSATION_ID, conversation);
    siteChatServer.setSiteChatConversationWithMemberListMap(conversationMap);
    
    List<SiteChatConversationMessage> messages;
    
    messages = siteChatServer.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
    
    Assert.assertEquals(3, messages.size());
  }
  
  @Test(expected=SiteChatException.class)
  public void testLoadHistoricalMessagesForConversationWithNoAccess() throws Exception {
    
    Mockito.when(provider.getConnection()).thenReturn(Mockito.mock(Connection.class));
    Mockito.when(siteChatUtil.loadSiteChatConversationMessagesForConversation(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatServer siteChatServer = getSiteChatServer();
    
    SiteChatConversationWithUserList conversation = new SiteChatConversationWithUserList();
    conversation.setUserIdSet(MiscUtil.get().makeHashSet());
    Map<Integer, SiteChatConversationWithUserList> conversationMap = new HashMap<Integer, SiteChatConversationWithUserList>();
    conversationMap.put(CONVERSATION_ID, conversation);
    siteChatServer.setSiteChatConversationWithMemberListMap(conversationMap);
    
    siteChatServer.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
  }
  
  @Test(expected=SiteChatException.class)
  public void testLoadHistoricalMessagesForConversationWithInvalidConversation() throws Exception {
    
    Mockito.when(provider.getConnection()).thenReturn(Mockito.mock(Connection.class));
    Mockito.when(siteChatUtil.loadSiteChatConversationMessagesForConversation(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    SiteChatServer siteChatServer = getSiteChatServer();
    
    siteChatServer.setSiteChatConversationWithMemberListMap(new HashMap<Integer, SiteChatConversationWithUserList>());
    
    siteChatServer.loadHistoricalMessages(USER_ID, SiteChatConversationType.Conversation, CONVERSATION_ID, null);
  }
}
