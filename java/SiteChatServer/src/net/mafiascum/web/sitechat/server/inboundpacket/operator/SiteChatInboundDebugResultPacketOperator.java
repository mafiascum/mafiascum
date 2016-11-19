package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.debug.DebugEntry;
import net.mafiascum.web.sitechat.server.debug.DebugManager;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundDebugResultPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundDebugResultPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundDebugResultPacketOperator extends SiteChatInboundSignedInPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundDebugResultPacketOperator.class.getName());
  
  public SiteChatInboundDebugResultPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundDebugResultPacket resultPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundDebugResultPacket.class);
    logger.info("Debug Result. ID: " + resultPacket.getId() + ", Result: " + resultPacket.getResult() + ", User ID: " + user.getId());
    DebugManager debugManager = processor.getDebugManager();
    
    DebugEntry entry = debugManager.getEntry(resultPacket.getId());
    
    if(entry == null) {
      logger.error("Invalid debug result entry `" + resultPacket.getId() + "`");
      return;
    }
    
    SiteChatUser initiatingUser = processor.getSiteChatUser(entry.getInitiatingUserId());
    
    if(initiatingUser == null) {
      logger.error("Invalid initiating user `" + entry.getInitiatingUserId() + "`");
      return;
    }
    
    SiteChatOutboundDebugResultPacket outboundPacket = new SiteChatOutboundDebugResultPacket();
    
    outboundPacket.setId(entry.getId());
    outboundPacket.setResult(resultPacket.getResult());
    
    processor.sendOutboundPacketToUsers(miscUtil.makeHashSet(initiatingUser.getId()), outboundPacket, null);
  }
}
