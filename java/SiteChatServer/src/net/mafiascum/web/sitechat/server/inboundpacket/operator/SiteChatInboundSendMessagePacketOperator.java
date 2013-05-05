package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.HashSet;
import java.util.Set;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSendMessagePacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundNewMessagePacket;

import com.google.gson.Gson;

public class SiteChatInboundSendMessagePacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {

    MiscUtil.log("Processing SendChat Message...");
    SiteChatInboundSendMessagePacket siteChatInboundSendMessagePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSendMessagePacket.class);
    SiteChatUser siteChatUser = siteChatWebSocket.getSiteChatUser();
    Set<Integer> sendToUserIdSet;
    SiteChatConversationWithUserList siteChatConversationWithUserList = null;
    SiteChatUser siteChatRecipientUser = null;
    
    if(siteChatUser == null) {
      
      MiscUtil.log("User not logged in.");
      return;//User is not logged in.
    }
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    if(siteChatInboundSendMessagePacket.getSiteChatConversationId() != null) {
      MiscUtil.log("Site Chat Conversatin ID: " + siteChatInboundSendMessagePacket.getSiteChatConversationId());
      siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatInboundSendMessagePacket.getSiteChatConversationId());
    
      if(siteChatConversationWithUserList == null) {
      
        MiscUtil.log("No Site Chat Conversation could be found.");
        return;//Conversation does not exist.
      }
    
      if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatWebSocket.getSiteChatUser().getId())) {
      
        MiscUtil.log("User not in chat.");
        return;//User is not in the conversation.
      }
    }
    else if(siteChatInboundSendMessagePacket.getRecipientUserId() != null) {
      
      MiscUtil.log("Recipient User ID: " + siteChatInboundSendMessagePacket.getRecipientUserId());
      siteChatRecipientUser = siteChatServer.getSiteChatUser(siteChatInboundSendMessagePacket.getRecipientUserId());
      if(siteChatRecipientUser == null) {
        
        MiscUtil.log("Target user `" + siteChatInboundSendMessagePacket.getRecipientUserId() + "` does not exist.");
        return;
      }
    }

    //Truncate long messages.
    if(siteChatInboundSendMessagePacket.getMessage().length() > SiteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH) {
      
     siteChatInboundSendMessagePacket.setMessage(siteChatInboundSendMessagePacket.getMessage().substring(0, SiteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH));
    }
    
    SiteChatConversationMessage siteChatConversationMessage = siteChatServer.recordSiteChatConversationMessage(siteChatInboundSendMessagePacket.getUserId(), siteChatInboundSendMessagePacket.getSiteChatConversationId(), siteChatInboundSendMessagePacket.getRecipientUserId(), siteChatInboundSendMessagePacket.getMessage());
    siteChatConversationMessage = siteChatConversationMessage.clone();
    siteChatConversationMessage.setMessage(StringUtil.escapeHTMLCharacters(siteChatConversationMessage.getMessage()));
    
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
    
    siteChatServer.sendOutboundPacketToUsers(sendToUserIdSet, siteChatOutboundNewMessagePacket, null);
  }
}
