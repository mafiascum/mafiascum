package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLeaveConversationPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLeaveConversationPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLeaveConversationPacketOperator extends SiteChatInboundSignedInPacketOperator {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SiteChatInboundLeaveConversationPacketOperator.class.getName());
  
  public SiteChatInboundLeaveConversationPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLeaveConversationPacket siteChatInboundLeaveConversationPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLeaveConversationPacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList;
    
    processor.updateUserActivity(user.getId());
    
    siteChatConversationWithUserList = processor.getSiteChatConversationWithUserList(siteChatInboundLeaveConversationPacket.getSiteChatConversationId());
    
    if(siteChatConversationWithUserList == null) {
      
      return;
    }
    
    //Remove the user from the user ID cache.
    siteChatConversationWithUserList.getUserIdSet().remove(user.getId());
    
    //Notify all other users in the conversation of the user's departure.
    SiteChatOutboundLeaveConversationPacket siteChatOutboundLeaveConversationPacket = new SiteChatOutboundLeaveConversationPacket();
    siteChatOutboundLeaveConversationPacket.setUserId(user.getId());
    siteChatOutboundLeaveConversationPacket.setSiteChatConversationId(siteChatInboundLeaveConversationPacket.getSiteChatConversationId());
    
    processor.sendOutboundPacketToUsers(siteChatConversationWithUserList.getUserIdSet(), siteChatOutboundLeaveConversationPacket, null);
  }
}
