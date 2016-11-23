package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.List;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLoadMessagesPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLoadMessagesPacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundLoadMessagesPacketOperator.class.getName());
  
  public SiteChatInboundLoadMessagesPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLoadMessagesPacket packet = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLoadMessagesPacket.class);
    
    char symbol = siteChatUtil.getConversationSymbol(packet.getConversationKey());
    SiteChatConversationType type = siteChatUtil.getSiteChatConversationTypeBySymbol(symbol);
    int uniqueIdentifier = siteChatUtil.getConversationUniqueIdentifier(packet.getConversationKey());
    SiteChatOutboundLoadMessagesPacket outboundPacket = new SiteChatOutboundLoadMessagesPacket();
    
    List<SiteChatConversationMessage> messages = null;
    boolean hasError = true;
    
    try {
      messages = processor.loadHistoricalMessages(user.getId(), type, uniqueIdentifier, packet.getOldestMessageId());
      hasError = false;
    }
    catch(SiteChatException exception) {
      logger.error("Error.", exception);
      outboundPacket.setErrorMessage(exception.getMessage());
    }
    catch(Exception exception) {
      logger.error("Error.", exception);
      outboundPacket.setErrorMessage("An unknown error has occurred.");
    }
    
    if(!hasError) {
      
      outboundPacket.setMessages(messages);
      outboundPacket.setConversationKey(packet.getConversationKey());
      outboundPacket.setUserMap(processor.getUserPacketMap(miscUtil.transformToList(messages, SiteChatConversationMessage::getUserId)));
    }
    
    processor.sendToDescriptor(descriptor.getId(), outboundPacket);
  }
}
