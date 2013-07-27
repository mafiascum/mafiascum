package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLogInPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLogInPacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLogInPacketOperator implements SiteChatInboundPacketOperator {

  protected Logger logger = Logger.getLogger(SiteChatInboundLogInPacketOperator.class.getName());
  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLogInPacket siteChatInboundLogInPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLogInPacket.class);
    int userId;
    
    SiteChatUser siteChatUser = siteChatServer.getSiteChatUser(siteChatInboundLogInPacket.getUserId());
    if(siteChatUser == null) {
      
      logger.error("Non Existant User Attempted To Log In. User ID: " + siteChatInboundLogInPacket.getUserId());
      return;
    }
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    synchronized(siteChatUser) {
      
      siteChatUser.setLastActivityDatetime(new Date());
      userId = siteChatUser.getId();
    }
    
    logger.trace("Log In Packet. User ID: " + siteChatInboundLogInPacket.getUserId() + ", Session: " + siteChatInboundLogInPacket.getSessionId());
    
    //Before even authenticating, perform the cheaper check to see if the user is banned.
    if(siteChatServer.isUseBanned(siteChatUser.getId())) {
      
      logger.debug("Banend user #" + siteChatUser.getId() + " attempting to log in. Denied.");
      siteChatWebSocket.getConnection().close();
      return;
    }
    
    boolean loginResult = siteChatServer.authenticateUserLogin(siteChatInboundLogInPacket.getUserId(), siteChatInboundLogInPacket.getSessionId());
    
    if(!loginResult) {
      
      logger.debug("Login authentication failed for user #" + siteChatUser.getId() + ". Session ID: " + siteChatInboundLogInPacket.getSessionId());
      siteChatWebSocket.getConnection().close();
      return;
    }
    
    logger.trace("Logged In. Last Activity: " + siteChatUser.getLastActivityDatetime());
    siteChatWebSocket.setSiteChatUser(siteChatUser);
    
    synchronized(siteChatWebSocket) {
      
      siteChatServer.associateWebSocketWithUser(userId, siteChatWebSocket);
    }
    
    //Reconnect to conversations the user has been removed from.
    for(String siteChatConversationKey : siteChatInboundLogInPacket.getConversationKeySet()) {

      char symbol = SiteChatUtil.getConversationSymbol(siteChatConversationKey);
      SiteChatConversationType siteChatConversationType = SiteChatUtil.getSiteChatConversationTypeBySymbol(symbol);
      
      if(!siteChatConversationType.equals(SiteChatConversationType.Conversation)) {
        
        continue;
      }

      int siteChatConversationId = SiteChatUtil.getConversationUniqueIdentifier(siteChatConversationKey);
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatConversationId);
      
      synchronized(siteChatConversationWithUserList) {
        if(siteChatConversationWithUserList == null) {
          
          logger.error("User sent conversation ID that does not exist in system: " + siteChatConversationId);
        }
        else {
          
          if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatUser.getId())) {
            
            siteChatServer.attemptJoinConversation(siteChatWebSocket, siteChatUser.getId(), siteChatConversationId, false, true, null);
          }
        }
      }
    }
    
    //Determine which messages the user has missed(if any, most of the time this should result in nothing).
    List<SiteChatConversationMessage> missedSiteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
    
    if(siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap() != null) {
      for(String siteChatConversationKey : siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap().keySet()) {
        
        char symbol = SiteChatUtil.getConversationSymbol(siteChatConversationKey);
        int uniqueIdentifier = SiteChatUtil.getConversationUniqueIdentifier(siteChatConversationKey);
        int mostRecentSiteChatConversationMessageId = siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap().get(siteChatConversationKey);
        SiteChatConversationType siteChatConversationType = SiteChatUtil.getSiteChatConversationTypeBySymbol(symbol);
        
        logger.trace("Conversation " + siteChatConversationKey + ", Last Message ID: " + mostRecentSiteChatConversationMessageId);
        
        if(siteChatConversationType == null) {
          
          logger.error("Unknown site chat conversation type. Symbol: " + symbol);
          continue;
        }
        
        List<SiteChatConversationMessage> siteChatConversationMessages = siteChatServer.getMessageHistory(siteChatConversationType, mostRecentSiteChatConversationMessageId, siteChatUser.getId(), uniqueIdentifier);
        
        synchronized(siteChatConversationMessages) {
          if(siteChatConversationMessages == null || siteChatConversationMessages.isEmpty() == true) {
          
            continue;
          }
          else {
          
            missedSiteChatConversationMessages.addAll(siteChatConversationMessages);
          }
        }
      }
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
    
    //Create the response
    SiteChatOutboundLogInPacket siteChatOutboundLogInPacket = new SiteChatOutboundLogInPacket();
    siteChatOutboundLogInPacket.setWasSuccessful(true);
    siteChatOutboundLogInPacket.setMissedSiteChatConversationMessages(missedSiteChatConversationMessages);
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLogInPacket);
  }
}
