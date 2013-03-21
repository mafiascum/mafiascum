package net.mafiascum.web.sitechat.server.inboundpacket;

import java.io.Serializable;


public abstract class SiteChatInboundPacket implements Serializable {

  public String command;
  
  public abstract SiteChatInboundPacketType getType();
}
