package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.ArrayList;
import java.util.List;

import net.mafiascum.web.sitechat.server.ignore.IgnorePacket;

public class SiteChatOutboundSetIgnorePacket extends SiteChatOutboundPacket {

  public IgnorePacket ignore;
  public List<String> errors = new ArrayList<>();
  public boolean removed;
  public Integer ignoredUserId;
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.setIgnore;
  }
}
