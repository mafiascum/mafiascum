package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundDebugPacket extends SiteChatInboundPacket {
  public String username;
  public String code;
  
  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.debug;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
