package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundDebugResultPacket extends SiteChatInboundPacket {

  protected String id;
  protected String result;
  
  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.debugResult;
  }
  
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
}
