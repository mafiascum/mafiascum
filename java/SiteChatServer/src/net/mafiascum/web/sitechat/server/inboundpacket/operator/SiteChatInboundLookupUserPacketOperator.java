package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLookupUserPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLookupUserPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLookupUserPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundLookupUserPacketOperator.class.getName());
  
  public SiteChatInboundLookupUserPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLookupUserPacket siteChatInboundLookupUserPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLookupUserPacket.class);
    logger.debug("LookupUser Packet. User ID: " + siteChatInboundLookupUserPacket.getUserId());
    
    processor.updateUserActivity(user.getId());
    
    //Create the response
    SiteChatOutboundLookupUserPacket siteChatOutboundLookupUserPacket = new SiteChatOutboundLookupUserPacket();
    siteChatOutboundLookupUserPacket.setUserId(siteChatInboundLookupUserPacket.getUserId());
    siteChatOutboundLookupUserPacket.setSiteChatUser(processor.getUserData(siteChatInboundLookupUserPacket.getUserId()).createUserPacket());
    processor.sendToDescriptor(descriptor.getId(), siteChatOutboundLookupUserPacket);
  }
}
