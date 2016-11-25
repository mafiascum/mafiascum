package net.mafiascum.web.sitechat.server.inboundpacket.operator.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLoadMessagesPacketOperator;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPacketType;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
    Descriptor descriptor = mock(Descriptor.class);
    SiteChatMessageProcessor processor = mock(SiteChatMessageProcessor.class);
    ArgumentCaptor<SiteChatOutboundLoadMessagesPacket> outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);

    doNothing().when(processor).sendToDescriptor(any(), outboundPacketCaptor.capture());
    when(processor.loadHistoricalMessages(anyInt(), any(), anyInt(), anyInt()))
           .thenReturn(Arrays.asList(new SiteChatConversationMessage(), new SiteChatConversationMessage(), new SiteChatConversationMessage()));
    
    operator.setSiteChatUtil(SiteChatUtil.get());
    operator.process(processor, userData, descriptor, new Gson().toJson(packet));
    
    verify(processor, times(1)).sendToDescriptor(any(), (SiteChatOutboundPacket)any());
    Assert.assertEquals(packet.getConversationKey(), outboundPacketCaptor.getValue().getConversationKey());
    Assert.assertEquals(SiteChatOutboundPacketType.loadMessages.getStandardName(), outboundPacketCaptor.getValue().getCommand());
    Assert.assertEquals(3, outboundPacketCaptor.getValue().getMessages().size());
    
    
    //Test site chat exception.
    outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);
    
    doNothing().when(processor).sendToDescriptor(any(), outboundPacketCaptor.capture());
    doThrow(new SiteChatException("message")).when(processor).loadHistoricalMessages(anyInt(), any(), anyInt(), anyInt());

    operator.process(processor, userData, descriptor, new Gson().toJson(packet));
    
    Assert.assertEquals("message", outboundPacketCaptor.getValue().getErrorMessage());
    
    
    //Test generic exception.
    outboundPacketCaptor = ArgumentCaptor.forClass(SiteChatOutboundLoadMessagesPacket.class);
    
    doNothing().when(processor).sendToDescriptor(any(), outboundPacketCaptor.capture());
    doThrow(new Exception("message")).when(processor).loadHistoricalMessages(anyInt(), any(), anyInt(), anyInt());

    operator.process(processor, userData, descriptor, new Gson().toJson(packet));
    
    Assert.assertEquals("An unknown error has occurred.", outboundPacketCaptor.getValue().getErrorMessage());
  }
}
