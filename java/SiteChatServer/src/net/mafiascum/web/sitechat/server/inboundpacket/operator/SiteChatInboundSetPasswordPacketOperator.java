package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatException;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetPasswordPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundSetPasswordPacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSetPasswordPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundSetPasswordPacketOperator.class.getName());
  
  public SiteChatInboundSetPasswordPacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetPasswordPacket packet = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetPasswordPacket.class);
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    String error = null;
    
    try {
      siteChatServer.updateConversationPassword(siteChatUser.getId(), packet.getConversationId(), packet.getPassword());
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
    siteChatWebSocket.sendOutboundPacket(outboundPacket);
    
    logger.info("Password changed. Error: " + error);
  }
}
