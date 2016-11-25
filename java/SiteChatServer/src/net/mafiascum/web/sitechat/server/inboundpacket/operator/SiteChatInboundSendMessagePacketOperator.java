package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSendMessagePacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundNewMessagePacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSendMessagePacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundSendMessagePacketOperator.class.getName());

  public SiteChatInboundSendMessagePacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {

    logger.trace("Processing SendChat Message...");
    SiteChatInboundSendMessagePacket sendMessagePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSendMessagePacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList = null;
    SiteChatUser siteChatRecipientUser = null;
    
    processor.updateUserActivity(user.getId());
    
    if(sendMessagePacket.getSiteChatConversationId() != null) {
      logger.debug("Site Chat Conversatin ID: " + sendMessagePacket.getSiteChatConversationId());
      siteChatConversationWithUserList = processor.getSiteChatConversationWithUserList(sendMessagePacket.getSiteChatConversationId());
    
      if(siteChatConversationWithUserList == null) {
      
        logger.error("No Site Chat Conversation could be found.");
        return;//Conversation does not exist.
      }
    
      if(!siteChatConversationWithUserList.getUserIdSet().contains(user.getId())) {
      
        logger.error("User not in chat.");
        return;//User is not in the conversation.
      }
    }
    else if(sendMessagePacket.getRecipientUserId() != null) {
      
      logger.debug("Recipient User ID: " + sendMessagePacket.getRecipientUserId());
      siteChatRecipientUser = processor.getSiteChatUser(sendMessagePacket.getRecipientUserId());
      if(siteChatRecipientUser == null) {
        
        logger.debug("Target user `" + sendMessagePacket.getRecipientUserId() + "` does not exist.");
        return;
      }
    }
    
    //Truncate long messages.
    if(sendMessagePacket.getMessage().length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH) {
      
      sendMessagePacket.setMessage(sendMessagePacket.getMessage().substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH));
    }
    
    //Remove out of range characters.
    sendMessagePacket.setMessage(stringUtil.removeNonBmp(sendMessagePacket.getMessage()));
    
    if(sendMessagePacket.getMessage().startsWith("/"))
      processor.processChannelCommand(descriptor, user.getUser(), sendMessagePacket.getMessage());
    else {
      SiteChatConversationMessage message = processor.recordSiteChatConversationMessage(user.getId(), sendMessagePacket.getSiteChatConversationId(), sendMessagePacket.getRecipientUserId(), sendMessagePacket.getMessage());
      message = message.clone();
      message.setMessage(stringUtil.escapeHTMLCharacters(message.getMessage()));
      
      sendOutboundMessage(siteChatConversationWithUserList, user.getUser(), processor, siteChatRecipientUser, message);
    }
  }
  
  protected void sendOutboundMessage(
      SiteChatConversationWithUserList siteChatConversationWithUserList,
      SiteChatUser siteChatUser,
      SiteChatMessageProcessor processor,
      SiteChatUser siteChatRecipientUser,
      SiteChatConversationMessage message
  ) throws IOException {
    
    Set<Integer> sendToUserIdSet;
    
    //Send the message to all users in the conversation(including the user who sent it).
    SiteChatOutboundNewMessagePacket siteChatOutboundNewMessagePacket = new SiteChatOutboundNewMessagePacket();
    siteChatOutboundNewMessagePacket.setSiteChatConversationMessage(message);
    
    //Build recipient user ID set.
    if(siteChatConversationWithUserList != null)
      sendToUserIdSet = siteChatConversationWithUserList.getUserIdSet();
    else {
      sendToUserIdSet = new HashSet<Integer>();
      sendToUserIdSet.add(siteChatRecipientUser.getId());
      sendToUserIdSet.add(siteChatUser.getId());
    }
    
    processor.sendOutboundPacketToUsers(sendToUserIdSet, siteChatOutboundNewMessagePacket, null);    
  }
}
