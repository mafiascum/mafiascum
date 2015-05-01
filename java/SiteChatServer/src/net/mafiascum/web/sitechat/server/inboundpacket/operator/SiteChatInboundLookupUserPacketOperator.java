package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLookupUserPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLookupUserPacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLookupUserPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundLookupUserPacketOperator.class.getName());
  
  public SiteChatInboundLookupUserPacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLookupUserPacket siteChatInboundLookupUserPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLookupUserPacket.class);
    logger.debug("LookupUser Packet. User ID: " + siteChatInboundLookupUserPacket.getUserId());
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    //Create the response
    SiteChatOutboundLookupUserPacket siteChatOutboundLookupUserPacket = new SiteChatOutboundLookupUserPacket();
    siteChatOutboundLookupUserPacket.setUserId(siteChatInboundLookupUserPacket.getUserId());
    siteChatOutboundLookupUserPacket.setSiteChatUser(siteChatServer.getSiteChatUser(siteChatInboundLookupUserPacket.getUserId()));
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLookupUserPacket);
  }
}
