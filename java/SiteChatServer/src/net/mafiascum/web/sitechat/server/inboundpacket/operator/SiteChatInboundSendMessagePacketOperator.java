package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSendMessagePacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundNewMessagePacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSendMessagePacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundSendMessagePacketOperator.class.getName());

  public SiteChatInboundSendMessagePacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {

    logger.trace("Processing SendChat Message...");
    SiteChatInboundSendMessagePacket sendMessagePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSendMessagePacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList = null;
    SiteChatUser siteChatRecipientUser = null;
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    if(sendMessagePacket.getSiteChatConversationId() != null) {
      logger.debug("Site Chat Conversatin ID: " + sendMessagePacket.getSiteChatConversationId());
      siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(sendMessagePacket.getSiteChatConversationId());
    
      if(siteChatConversationWithUserList == null) {
      
        logger.error("No Site Chat Conversation could be found.");
        return;//Conversation does not exist.
      }
    
      if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatWebSocket.getUserData().getId())) {
      
        logger.error("User not in chat.");
        return;//User is not in the conversation.
      }
    }
    else if(sendMessagePacket.getRecipientUserId() != null) {
      
      logger.debug("Recipient User ID: " + sendMessagePacket.getRecipientUserId());
      siteChatRecipientUser = siteChatServer.getSiteChatUser(sendMessagePacket.getRecipientUserId());
      if(siteChatRecipientUser == null) {
        
        logger.debug("Target user `" + sendMessagePacket.getRecipientUserId() + "` does not exist.");
        return;
      }
    }
    
    //Truncate long messages.
    if(sendMessagePacket.getMessage().length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH) {
      
      sendMessagePacket.setMessage(sendMessagePacket.getMessage().substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH));
    }
    
    if(sendMessagePacket.getMessage().startsWith("/"))
      siteChatServer.processChannelCommand(siteChatUser, sendMessagePacket.getMessage());
    else {
      SiteChatConversationMessage message = siteChatServer.recordSiteChatConversationMessage(siteChatUser.getId(), sendMessagePacket.getSiteChatConversationId(), sendMessagePacket.getRecipientUserId(), sendMessagePacket.getMessage());
      message = message.clone();
      message.setMessage(stringUtil.escapeHTMLCharacters(message.getMessage()));
      
      sendOutboundMessage(siteChatConversationWithUserList, siteChatUser, siteChatServer, siteChatRecipientUser, message);
    }
  }
  
  protected void sendOutboundMessage(
      SiteChatConversationWithUserList siteChatConversationWithUserList,
      SiteChatUser siteChatUser,
      SiteChatServer siteChatServer,
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
    
    //long timeBefore = System.currentTimeMillis();
    //SiteChatServer.lagLogger.debug("Sending NewMessage Packets To Users.");
    siteChatServer.sendOutboundPacketToUsers(sendToUserIdSet, siteChatOutboundNewMessagePacket, null);
    //long timeBetween = System.currentTimeMillis() - timeBefore;
    //SiteChatServer.lagLogger.debug("NewMessage Packet Sent. Duration: " + timeBetween + " ms.");    
  }
}
