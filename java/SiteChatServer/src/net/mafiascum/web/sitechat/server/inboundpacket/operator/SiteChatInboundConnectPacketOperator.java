package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundConnectPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundConnectPacketOperator extends SiteChatInboundSignedInPacketOperator {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SiteChatInboundConnectPacketOperator.class.getName());
  
  public SiteChatInboundConnectPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundConnectPacket siteChatInboundConnectPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundConnectPacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList;
    
    processor.updateUserActivity(user.getId());
    
    String siteChatConversationName = siteChatInboundConnectPacket.getSiteChatConversationName();
    
    //Truncate long conversation names.
    if(siteChatConversationName.length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH) {
      
      siteChatConversationName = siteChatConversationName.substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH);
    }
    
    siteChatConversationWithUserList = processor.getConversationWithUserList(siteChatConversationName);
    
    if(siteChatConversationWithUserList == null) {
      
      siteChatConversationWithUserList = processor.createSiteChatConversation(siteChatInboundConnectPacket.getSiteChatConversationName(), user.getId());
    }
    
    processor.attemptJoinConversation(descriptor, user.getId(), siteChatConversationWithUserList.getSiteChatConversation().getId(), true, true, siteChatInboundConnectPacket.getPassword(), null);
  }
}
