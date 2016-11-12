package net.mafiascum.web.sitechat.server.outboundpacket;

public class SiteChatOutboundDebugResultPacket extends SiteChatOutboundPacket {

  protected String id;
  protected String result;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getResult() {
    return result;
  }
  public void setResult(String result) {
    this.result = result;
  }
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.debugResult;
  }
}
