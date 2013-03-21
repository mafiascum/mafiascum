package net.mafiascum.web.sitechat.server.outboundpacket;

public abstract class SiteChatOutboundPacket {

  protected String command;
  
  public SiteChatOutboundPacket() {
    
    this.command = getType().getStandardName();
  }
  
  public String getCommand() {
    
    return command;
  }
  
  public void setCommand(String command) {
    
    this.command = command;
  }
  
  public abstract SiteChatOutboundPacketType getType();
}
