package net.mafiascum.web.sitechat.server.inboundpacket.operator.test;

import java.util.Arrays;

import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLoadMessagesPacketOperator;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPacketType;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.Gson;

public class SiteChatInboundLoadMessagesPacketOperatorTest {
  
  @Test
  public void testProcess() throws Exception {
    
    SiteChatInboundLoadMessagesPacket packet = new SiteChatInboundLoadMessagesPacket("C11", 10);
    
    UserData userData = new UserData();
    SiteChatUser user = new SiteChatUser();
    
    userData.setUser(user);
    user.setId(5932);

    SiteChatInboundLoadMessagesPacketOperator operator = new SiteChatInboundLoadMessagesPacketOperator();
    SiteChatWebSocket siteChatWebSocket = Mockito.mock(SiteChatWebSocket.class);
    SiteChatServer siteChatServer = Mockito.mock(SiteChatServer.class);
    ArgumentCaptor<SiteChatOutboundLoadMessagesPacket> outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);

    Mockito.doNothing().when(siteChatWebSocket).sendOutboundPacket(outboundPacketCaptor.capture());
    Mockito.when(siteChatWebSocket.getUserData()).thenReturn(userData);
    Mockito.when(siteChatServer.loadHistoricalMessages(Mockito.anyInt(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    
    operator.setSiteChatUtil(SiteChatUtil.get());
    operator.process(siteChatServer, siteChatWebSocket, new Gson().toJson(packet));
    
    Mockito.verify(siteChatWebSocket, Mockito.times(1)).sendOutboundPacket(Mockito.any());
    Assert.assertEquals(packet.getConversationKey(), outboundPacketCaptor.getValue().getConversationKey());
    Assert.assertEquals(SiteChatOutboundPacketType.loadMessages.getStandardName(), outboundPacketCaptor.getValue().getCommand());
    Assert.assertEquals(3, outboundPacketCaptor.getValue().getMessages().size());
    
    
    //Test site chat exception.
    outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);
    
    Mockito.doNothing().when(siteChatWebSocket).sendOutboundPacket(outboundPacketCaptor.capture());
    Mockito.when(siteChatWebSocket.getUserData()).thenReturn(userData);
    Mockito.doThrow(new SiteChatException("message")).when(siteChatServer).loadHistoricalMessages(Mockito.anyInt(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());

    operator.process(siteChatServer, siteChatWebSocket, new Gson().toJson(packet));
    
    Assert.assertEquals("message", outboundPacketCaptor.getValue().getErrorMessage());
    
    
    //Test generic exception.
    outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);
    
    Mockito.doNothing().when(siteChatWebSocket).sendOutboundPacket(outboundPacketCaptor.capture());
    Mockito.when(siteChatWebSocket.getUserData()).thenReturn(userData);
    Mockito.doThrow(new Exception("message")).when(siteChatServer).loadHistoricalMessages(Mockito.anyInt(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());

    operator.process(siteChatServer, siteChatWebSocket, new Gson().toJson(packet));
    
    Assert.assertEquals("An unknown error has occurred.", outboundPacketCaptor.getValue().getErrorMessage());
  }
}
