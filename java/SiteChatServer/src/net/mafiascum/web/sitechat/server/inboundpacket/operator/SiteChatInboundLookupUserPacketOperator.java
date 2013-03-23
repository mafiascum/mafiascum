package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLookupUserPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLookupUserPacket;

import com.google.gson.Gson;

public class SiteChatInboundLookupUserPacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLookupUserPacket siteChatInboundLookupUserPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLookupUserPacket.class);
    SiteChatUser siteChatUser = siteChatWebSocket.getSiteChatUser();
    MiscUtil.log("User ID: " + siteChatInboundLookupUserPacket.getUserId());
    
    if(siteChatUser == null) {
      //Not Logged In.
      
      MiscUtil.log("User not logged in, looking up user. Target User ID: " + siteChatInboundLookupUserPacket.getUserId());
      return;
    }
    
    
    //Create the response
    SiteChatOutboundLookupUserPacket siteChatOutboundLookupUserPacket = new SiteChatOutboundLookupUserPacket();
    siteChatOutboundLookupUserPacket.setUserId(siteChatInboundLookupUserPacket.getUserId());
    siteChatOutboundLookupUserPacket.setSiteChatUser(siteChatServer.getSiteChatUser(siteChatInboundLookupUserPacket.getUserId()));
    siteChatWebSocket.sendOutboundPacket(siteChatOutboundLookupUserPacket);
  }
}
