package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLeaveConversationPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLeaveConversationPacket;

import com.google.gson.Gson;

public class SiteChatInboundLeaveConversationPacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLeaveConversationPacket siteChatInboundLeaveConversationPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLeaveConversationPacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList;
    SiteChatUser siteChatUser = siteChatWebSocket.getSiteChatUser();
    
    if(siteChatUser == null) {
      //Not logged in.
      
      System.out.println("User trying to leave conversation without first logging in.");
      return;
    }
    
    siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatInboundLeaveConversationPacket.getSiteChatConversationId());
    
    if(siteChatConversationWithUserList == null) {
      
      return;
    }
    
    //Remove the user from the user ID cache.
    siteChatConversationWithUserList.getUserIdSet().remove(siteChatWebSocket.getSiteChatUser().getId());
    
    //Notify all other users in the conversation of the user's departure.
    SiteChatOutboundLeaveConversationPacket siteChatOutboundLeaveConversationPacket = new SiteChatOutboundLeaveConversationPacket();
    siteChatOutboundLeaveConversationPacket.setUserId(siteChatWebSocket.getSiteChatUser().getId());
    siteChatOutboundLeaveConversationPacket.setSiteChatConversationId(siteChatInboundLeaveConversationPacket.getSiteChatConversationId());
    
    siteChatServer.sendOutboundPacketToUsers(siteChatConversationWithUserList.getUserIdSet(), siteChatOutboundLeaveConversationPacket, null);
  }
}
