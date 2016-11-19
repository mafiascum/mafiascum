package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.debug.DebugEntry;
import net.mafiascum.web.sitechat.server.debug.DebugManager;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundDebugPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundDebugPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundDebugPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundDebugPacketOperator.class.getName());
  
  public SiteChatInboundDebugPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    SiteChatInboundDebugPacket debugPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundDebugPacket.class);
    SiteChatUser targetUser = processor.getSiteChatUser(debugPacket.getUsername());
    
    processor.updateUserActivity(user.getId());
    
    if(targetUser == null) {
      logger.error("Invalid user `" + debugPacket.getUsername() + "`.");
      return;
    }
    
    DebugManager debugManager = processor.getDebugManager();
    
    DebugEntry entry = debugManager.submitDebugEntry(user.getId(), targetUser.getId(), debugPacket.getCode());
    
    SiteChatOutboundDebugPacket outboundPacket = new SiteChatOutboundDebugPacket();
    
    outboundPacket.setId(entry.getId());
    outboundPacket.setCode(entry.getCode());
    
    processor.sendOutboundPacketToUsers(miscUtil.makeHashSet(targetUser.getId()), outboundPacket, null);
  }
}
