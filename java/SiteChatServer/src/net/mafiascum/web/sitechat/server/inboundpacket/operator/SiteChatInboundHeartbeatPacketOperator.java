package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundConnectPacket;

import com.google.gson.Gson;

public class SiteChatInboundHeartbeatPacketOperator implements SiteChatInboundPacketOperator {

  public void process(SiteChatServer siteChatServer, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    SiteChatUser siteChatUser = siteChatWebSocket.getSiteChatUser();
    siteChatServer.updateUserActivity(siteChatUser.getId());
  }
}
