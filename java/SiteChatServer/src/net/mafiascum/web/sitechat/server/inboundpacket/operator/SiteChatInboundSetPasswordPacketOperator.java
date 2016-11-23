package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetPasswordPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundSetPasswordPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSetPasswordPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundSetPasswordPacketOperator.class.getName());
  
  public SiteChatInboundSetPasswordPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetPasswordPacket packet = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetPasswordPacket.class);
    
    processor.updateUserActivity(user.getId());
    String error = null;
    
    try {
      processor.updateConversationPassword(user.getId(), packet.getConversationId(), packet.getPassword());
    }
    catch(SiteChatException siteChatException) {
      
      error = siteChatException.getMessage();
    }
    catch(Exception exception) {
      
      logger.error("Error updating conversation password:", exception);
      error = "An error has occurred.";
    }
    
    SiteChatOutboundSetPasswordPacket outboundPacket = new SiteChatOutboundSetPasswordPacket();
    outboundPacket.setErrorMessage(error);
    processor.sendToDescriptor(descriptor.getId(), outboundPacket);
    
    logger.info("Password changed. Error: " + error);
  }
}
