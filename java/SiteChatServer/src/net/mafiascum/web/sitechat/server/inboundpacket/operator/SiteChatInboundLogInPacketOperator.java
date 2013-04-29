package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLogInPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLogInPacket;

import com.google.gson.Gson;

public class SiteChatInboundLogInPacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLogInPacket siteChatInboundLogInPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLogInPacket.class);
    
    MiscUtil.log("User ID: " + siteChatInboundLogInPacket.getUserId());
    MiscUtil.log("Session ID: " + siteChatInboundLogInPacket.getSessionId());
    MiscUtil.log("Conversation ID Set: " + siteChatInboundLogInPacket.getConversationIdSet());
    MiscUtil.log("Conversation To Message Map: " + siteChatInboundLogInPacket.getConversationIdToMostRecentMessageIdMap());
    
    SiteChatUser siteChatUser = siteChatServer.getSiteChatUser(siteChatInboundLogInPacket.getUserId());
    if(siteChatUser == null) {
      
      MiscUtil.log("Non Existant User Attempted To Log In. User ID: " + siteChatInboundLogInPacket.getUserId());
      return;
    }
    siteChatServer.updateUserActivity(siteChatUser.getId());
    boolean loginResult = siteChatServer.authenticateUserLogin(siteChatInboundLogInPacket.getUserId(), siteChatInboundLogInPacket.getSessionId());
    
    if(!loginResult) {
      
      MiscUtil.log("Login authentication failed for user #" + siteChatUser.getId() + ". Session ID: " + siteChatInboundLogInPacket.getSessionId());
      siteChatWebSocket.getConnection().close();
      return;
    }
    
    siteChatWebSocket.setSiteChatUser(siteChatUser);
    
    //Reconnect to conversations the user has been removed from.
    for(Integer siteChatConversationId : siteChatInboundLogInPacket.getConversationIdSet()) {
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatConversationId);
    
      if(siteChatConversationWithUserList == null) {
        
        MiscUtil.log("User sent conversation ID that does not exist in system: " + siteChatConversationId);
      }
      else {
        
        if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatUser.getId())) {
          
          siteChatServer.attemptJoinConversation(siteChatUser.getId(), siteChatConversationId, false, true);
        }
      }
    }
    
    //Determine which messages the user has missed(if any, most of the time this should result in nothing).
    List<SiteChatConversationMessage> missedSiteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
    if(siteChatInboundLogInPacket.getConversationIdToMostRecentMessageIdMap() != null) {
      for(Integer siteChatConversationId : siteChatInboundLogInPacket.getConversationIdToMostRecentMessageIdMap().keySet()) {
        
        int siteChatConversationMessageId = siteChatInboundLogInPacket.getConversationIdToMostRecentMessageIdMap().get(siteChatConversationId);
        SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatConversationId);
        List<SiteChatConversationMessage> siteChatConversationMessages = siteChatConversationWithUserList.getSiteChatConversationMessages();
        
        
        MiscUtil.log("Conversation " + siteChatConversationId + ", Last Message ID: " + siteChatConversationMessageId);
        
        if(siteChatConversationMessages.isEmpty() == false) {
          ListIterator<SiteChatConversationMessage> listIterator = siteChatConversationMessages.listIterator(siteChatConversationMessages.size());
          
          while(listIterator.hasPrevious()) {
            
            SiteChatConversationMessage siteChatConversationMessage = listIterator.previous();
            if(siteChatConversationMessage.getId() > siteChatConversationMessageId) {
              
              siteChatConversationMessage = siteChatConversationMessage.clone();
              siteChatConversationMessage.setMessage(StringUtil.escapeHTMLCharacters(siteChatConversationMessage.getMessage()));
              MiscUtil.log("Adding missed message: " + siteChatConversationMessage.getId());
              missedSiteChatConversationMessages.add(siteChatConversationMessage);
            }
          }
        }
  
        MiscUtil.log("Total Missed Messages: " + missedSiteChatConversationMessages.size());
      }
      
      Collections.sort(missedSiteChatConversationMessages, new Comparator<SiteChatConversationMessage>() {
        
        public int compare(SiteChatConversationMessage arg0, SiteChatConversationMessage arg1) {
          
          if(arg0.getId() < arg1.getId())
            return -1;
          else if(arg0.getId() > arg1.getId())
            return 1;
          return 0;
        }
      });
    }
    
    //Create the response
    SiteChatOutboundLogInPacket siteChatOutboundLogInPacket = new SiteChatOutboundLogInPacket();
    siteChatOutboundLogInPacket.setWasSuccessful(true);
    siteChatOutboundLogInPacket.setMissedSiteChatConversationMessages(missedSiteChatConversationMessages);
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLogInPacket);
  }
}
