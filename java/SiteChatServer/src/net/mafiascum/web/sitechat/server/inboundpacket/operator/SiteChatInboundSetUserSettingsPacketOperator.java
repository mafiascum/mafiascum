package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetUserSettingsPacket;

import com.google.gson.Gson;

public class SiteChatInboundSetUserSettingsPacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetUserSettingsPacket setUserSettingsPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetUserSettingsPacket.class);
    siteChatServer.setUserSettings(siteChatUser.getId(), setUserSettingsPacket.getCompact(), setUserSettingsPacket.getAnimateAvatars(), setUserSettingsPacket.getTimestamp());
  }
}
