package net.mafiascum.web.sitechat.server.inboundpacket;



public abstract class SiteChatInboundPacket {

  public String command;
  
  public abstract SiteChatInboundPacketType getType();
}
