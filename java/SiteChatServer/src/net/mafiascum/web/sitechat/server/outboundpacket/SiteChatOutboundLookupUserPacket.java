package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.SiteChatUser;

public class SiteChatOutboundLookupUserPacket extends SiteChatOutboundPacket {

  protected int userId;
  protected SiteChatUser siteChatUser;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
  
  public SiteChatUser getSiteChatUser() {
    return siteChatUser;
  }

  public void setSiteChatUser(SiteChatUser siteChatUser) {
    this.siteChatUser = siteChatUser;
  }

  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.lookupUser;
  }
}
