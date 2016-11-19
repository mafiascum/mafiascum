package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

public abstract class SiteChatInboundSignedInPacketOperator extends SiteChatInboundPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundSignedInPacketOperator.class.getName());
  
  public abstract void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception;
  
  public void process(SiteChatMessageProcessor processor, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    UserData userData = processor.getUserManager().getUserByDescriptorId(descriptor.getId());
    if(userData == null) {
      //Not logged in.
      
      logger.error("User trying to leave conversation without first logging in.");
      return;
    }
    
    process(processor, userData, descriptor, siteChatInboundPacketJson);
  }
}
