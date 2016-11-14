package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.user.UserPacket;

public class SiteChatOutboundLookupUserPacket extends SiteChatOutboundPacket {

  protected int userId;
  protected UserPacket siteChatUser;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
  
  public UserPacket getSiteChatUser() {
    return siteChatUser;
  }

  public void setSiteChatUser(UserPacket siteChatUser) {
    this.siteChatUser = siteChatUser;
  }

  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.lookupUser;
  }
}
