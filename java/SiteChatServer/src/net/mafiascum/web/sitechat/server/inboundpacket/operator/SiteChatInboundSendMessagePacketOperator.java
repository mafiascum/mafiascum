package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.Date;
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

public class SiteChatInboundSendMessagePacketOperator extends SiteChatInboundPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundSendMessagePacketOperator.class.getName());

  public SiteChatInboundSendMessagePacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {

    logger.trace("Processing SendChat Message...");
    SiteChatInboundSendMessagePacket siteChatInboundSendMessagePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSendMessagePacket.class);
    SiteChatUser siteChatUser = siteChatWebSocket.getSiteChatUser();
    Set<Integer> sendToUserIdSet;
    SiteChatConversationWithUserList siteChatConversationWithUserList = null;
    SiteChatUser siteChatRecipientUser = null;
    
    if(siteChatUser == null) {
      
      logger.error("User not logged in.");
      return;//User is not logged in.
    }
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    synchronized(siteChatUser) {
      
      siteChatUser.setLastActivityDatetime(new Date());
    }
    
    if(siteChatInboundSendMessagePacket.getSiteChatConversationId() != null) {
      logger.debug("Site Chat Conversatin ID: " + siteChatInboundSendMessagePacket.getSiteChatConversationId());
      siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatInboundSendMessagePacket.getSiteChatConversationId());
    
      if(siteChatConversationWithUserList == null) {
      
        logger.error("No Site Chat Conversation could be found.");
        return;//Conversation does not exist.
      }
    
      if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatWebSocket.getSiteChatUser().getId())) {
      
        logger.error("User not in chat.");
        return;//User is not in the conversation.
      }
    }
    else if(siteChatInboundSendMessagePacket.getRecipientUserId() != null) {
      
      logger.debug("Recipient User ID: " + siteChatInboundSendMessagePacket.getRecipientUserId());
      siteChatRecipientUser = siteChatServer.getSiteChatUser(siteChatInboundSendMessagePacket.getRecipientUserId());
      if(siteChatRecipientUser == null) {
        
        logger.debug("Target user `" + siteChatInboundSendMessagePacket.getRecipientUserId() + "` does not exist.");
        return;
      }
    }

    //Truncate long messages.
    if(siteChatInboundSendMessagePacket.getMessage().length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH) {
      
      siteChatInboundSendMessagePacket.setMessage(siteChatInboundSendMessagePacket.getMessage().substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH));
    }
    
    SiteChatConversationMessage siteChatConversationMessage = siteChatServer.recordSiteChatConversationMessage(siteChatUser.getId(), siteChatInboundSendMessagePacket.getSiteChatConversationId(), siteChatInboundSendMessagePacket.getRecipientUserId(), siteChatInboundSendMessagePacket.getMessage());
    siteChatConversationMessage = siteChatConversationMessage.clone();
    siteChatConversationMessage.setMessage(stringUtil.escapeHTMLCharacters(siteChatConversationMessage.getMessage()));
    
    //Send the message to all users in the conversation(including the user who sent it).
    SiteChatOutboundNewMessagePacket siteChatOutboundNewMessagePacket = new SiteChatOutboundNewMessagePacket();
    siteChatOutboundNewMessagePacket.setSiteChatConversationMessage(siteChatConversationMessage);
    
    //Build recipient user ID set.
    if(siteChatConversationWithUserList != null)
      sendToUserIdSet = siteChatConversationWithUserList.getUserIdSet();
    else {
      sendToUserIdSet = new HashSet<Integer>();
      sendToUserIdSet.add(siteChatRecipientUser.getId());
      sendToUserIdSet.add(siteChatUser.getId());
    }
    
    long timeBefore = System.currentTimeMillis();
    SiteChatServer.lagLogger.debug("Sending NewMessage Packets To Users.");
    siteChatServer.sendOutboundPacketToUsers(sendToUserIdSet, siteChatOutboundNewMessagePacket, null);
    long timeBetween = System.currentTimeMillis() - timeBefore;
    SiteChatServer.lagLogger.debug("NewMessage Packet Sent. Duration: " + timeBetween + " ms.");
  }
}
