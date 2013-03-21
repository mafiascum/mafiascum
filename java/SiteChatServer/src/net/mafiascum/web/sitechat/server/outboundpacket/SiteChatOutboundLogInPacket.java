package net.mafiascum.web.sitechat.server.outboundpacket;

public class SiteChatOutboundLogInPacket extends SiteChatOutboundPacket {

  protected boolean wasSuccessful;
  
  public SiteChatOutboundLogInPacket() {
    
    super();
  }
  
  public boolean getWasSuccessful() {
    
    return wasSuccessful;
  }
  
  public void setWasSuccessful(boolean wasSuccessful) {
    
    this.wasSuccessful = wasSuccessful;
  }
  
  public SiteChatOutboundPacketType getType() {

    return SiteChatOutboundPacketType.login;
  }
}
