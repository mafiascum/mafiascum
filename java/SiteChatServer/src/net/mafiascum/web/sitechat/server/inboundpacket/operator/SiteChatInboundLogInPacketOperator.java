package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.util.MiscUtil;
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
    
    MiscUtil.log("User ID: " + siteChatInboundLogInPacket.getUserId());
    MiscUtil.log("Session ID: " + siteChatInboundLogInPacket.getSessionId());
    MiscUtil.log("Conversation ID Set: " + siteChatInboundLogInPacket.getConversationIdSet());
    
    SiteChatUser siteChatUser = siteChatServer.getSiteChatUser(siteChatInboundLogInPacket.getUserId());
    if(siteChatUser == null) {
      
      MiscUtil.log("Non Existant User Attempted To Log In. User ID: " + siteChatInboundLogInPacket.getUserId());
      return;
    }
    
    boolean loginResult = siteChatServer.authenticateUserLogin(siteChatInboundLogInPacket.getUserId(), siteChatInboundLogInPacket.getSessionId());
    
    if(!loginResult) {
      
      MiscUtil.log("Login authentication failed for user #" + siteChatUser.getId() + ". Session ID: " + siteChatInboundLogInPacket.getSessionId());
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
    
    //Create the response
    SiteChatOutboundLogInPacket siteChatOutboundLogInPacket = new SiteChatOutboundLogInPacket();
    siteChatOutboundLogInPacket.setWasSuccessful(true);
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLogInPacket);
  }
}
