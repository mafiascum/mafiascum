package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.user.UserPacket;

public class SiteChatOutboundUserJoinPacket extends SiteChatOutboundPacket {

  protected int siteChatConversationId;
  protected UserPacket siteChatUser;
  
  public SiteChatOutboundUserJoinPacket() {
    
    super();
  }
  
  public int getSiteChatConversationId() {
    return siteChatConversationId;
  }

  public void setSiteChatConversationId(int siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }

  public UserPacket getSiteChatUser() {
    return siteChatUser;
  }

  public void setSiteChatUser(UserPacket siteChatUser) {
    this.siteChatUser = siteChatUser;
  }

  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.userJoin;
  }
}
