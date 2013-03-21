package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLogInPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLogInPacket;

import com.google.gson.Gson;

public class SiteChatInboundLogInPacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLogInPacket siteChatInboundLogInPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLogInPacket.class);
    
    System.out.println("User ID: " + siteChatInboundLogInPacket.getUserId());
    System.out.println("Session ID: " + siteChatInboundLogInPacket.getSessionId());
    System.out.println("Conversation ID Set: " + siteChatInboundLogInPacket.getConversationIdSet());
    
    SiteChatUser siteChatUser = siteChatServer.getSiteChatUser(siteChatInboundLogInPacket.getUserId());
    if(siteChatUser == null) {
      
      System.out.println("Non Existant User Attempted To Log In. User ID: " + siteChatInboundLogInPacket.getUserId());
      return;
    }
    
    boolean loginResult = siteChatServer.authenticateUserLogin(siteChatInboundLogInPacket.getUserId(), siteChatInboundLogInPacket.getSessionId());
    
    if(!loginResult) {
      
      System.out.println("Login authentication failed for user #" + siteChatUser.getId() + ". Session ID: " + siteChatInboundLogInPacket.getSessionId());
      return;
    }
    
    siteChatWebSocket.setSiteChatUser(siteChatUser);
    
    //Reconnect to conversations the user has been removed from.
    for(Integer siteChatConversationId : siteChatInboundLogInPacket.getConversationIdSet()) {
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatConversationId);
    
      if(siteChatConversationWithUserList == null) {
        
        System.out.println("User sent conversation ID that does not exist in system: " + siteChatConversationId);
      }
      else {
        
        if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatUser.getId())) {
          
          siteChatServer.attemptJoinConversation(siteChatUser.getId(), siteChatConversationId, false, true);
        }
      }
    }
    
    //Create the response
    SiteChatOutboundLogInPacket siteChatOutboundLogInPacket = new SiteChatOutboundLogInPacket();
    siteChatOutboundLogInPacket.setWasSuccessful(true);
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLogInPacket);
  }
}
