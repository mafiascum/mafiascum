package net.mafiascum.web.sitechat.server.outboundpacket;


public class SiteChatOutboundSetPasswordPacket extends SiteChatOutboundPacket {

  protected String errorMessage;
  
  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  public SiteChatOutboundPacketType getType() {
  
    return SiteChatOutboundPacketType.setPassword;
  }
}
