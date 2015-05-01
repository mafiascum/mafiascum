package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.Date;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundConnectPacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundConnectPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundConnectPacketOperator.class.getName());
  
  public SiteChatInboundConnectPacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundConnectPacket siteChatInboundConnectPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundConnectPacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList;
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    synchronized(siteChatUser) {
      
      siteChatUser.setLastActivityDatetime(new Date());
    }
    
    String siteChatConversationName = siteChatInboundConnectPacket.getSiteChatConversationName();
    
    //Truncate long conversation names.
    if(siteChatConversationName.length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH) {
      
      siteChatConversationName = siteChatConversationName.substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH);
    }
    
    siteChatConversationWithUserList = siteChatServer.getSiteChatConversationWithUserList(siteChatConversationName);
    
    if(siteChatConversationWithUserList == null) {
      
      siteChatConversationWithUserList = siteChatServer.createSiteChatConversation(siteChatInboundConnectPacket.getSiteChatConversationName(), siteChatUser.getId());
    }
    
    siteChatServer.attemptJoinConversation(siteChatWebSocket, siteChatUser.getId(), siteChatConversationWithUserList.getSiteChatConversation().getId(), true, true, siteChatInboundConnectPacket.getPassword(), null);
  }
}
