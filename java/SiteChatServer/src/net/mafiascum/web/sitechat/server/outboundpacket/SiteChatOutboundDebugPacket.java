package net.mafiascum.web.sitechat.server.outboundpacket;

public class SiteChatOutboundDebugPacket extends SiteChatOutboundPacket {

  protected String id;
  protected String code;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.debug;
  }
}
