package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.user.UserData;

public class SiteChatInboundHeartbeatPacketOperator extends SiteChatInboundSignedInPacketOperator {

  public SiteChatInboundHeartbeatPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    processor.updateUserNetworkActivity(user.getId());
  }
}
