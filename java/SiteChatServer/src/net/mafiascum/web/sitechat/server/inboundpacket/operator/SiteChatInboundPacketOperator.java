package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;

public interface SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatServer.SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception;
}
