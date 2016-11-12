package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.debug.DebugEntry;
import net.mafiascum.web.sitechat.server.debug.DebugManager;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundDebugPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundDebugPacket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundDebugPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundDebugPacketOperator.class.getName());
  
  public SiteChatInboundDebugPacketOperator() {
    super();
  }
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    SiteChatInboundDebugPacket debugPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundDebugPacket.class);
    SiteChatUser targetUser = siteChatServer.getSiteChatUser(debugPacket.getUsername());
    
    siteChatServer.updateUserActivity(siteChatUser.getId());
    
    if(targetUser == null) {
      logger.error("Invalid user `" + debugPacket.getUsername() + "`.");
      return;
    }
    
    DebugManager debugManager = siteChatServer.getDebugManager();
    
    DebugEntry entry = debugManager.submitDebugEntry(siteChatUser.getId(), targetUser.getId(), debugPacket.getCode());
    
    SiteChatOutboundDebugPacket outboundPacket = new SiteChatOutboundDebugPacket();
    
    outboundPacket.setId(entry.getId());
    outboundPacket.setCode(entry.getCode());
    
    siteChatServer.sendOutboundPacketToUsers(miscUtil.makeHashSet(targetUser.getId()), outboundPacket, null);
  }
}
