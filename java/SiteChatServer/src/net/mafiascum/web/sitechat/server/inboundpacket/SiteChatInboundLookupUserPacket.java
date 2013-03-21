package net.mafiascum.web.sitechat.server.inboundpacket;


public class SiteChatInboundLookupUserPacket extends SiteChatInboundPacket {

  protected int userId;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.lookupUser;
  }
}
