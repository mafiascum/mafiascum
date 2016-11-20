package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetUserSettingsPacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import com.google.gson.Gson;

public class SiteChatInboundSetUserSettingsPacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetUserSettingsPacket setUserSettingsPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetUserSettingsPacket.class);
    processor.setUserSettings(user.getId(), setUserSettingsPacket.getCompact(), setUserSettingsPacket.getAnimateAvatars(), setUserSettingsPacket.getInvisible(), setUserSettingsPacket.getTimestamp());
  }
}
