package net.mafiascum.web.sitechat.server.outboundpacket;

import net.mafiascum.web.sitechat.server.SiteChatUser;

public class SiteChatOutboundUserJoinPacket extends SiteChatOutboundPacket {

  protected int siteChatConversationId;
  protected SiteChatUser siteChatUser;
  
  public SiteChatOutboundUserJoinPacket() {
    
    super();
  }
  
  public int getSiteChatConversationId() {
    return siteChatConversationId;
  }

  public void setSiteChatConversationId(int siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }

  public SiteChatUser getSiteChatUser() {
    return siteChatUser;
  }

  public void setSiteChatUser(SiteChatUser siteChatUser) {
    this.siteChatUser = siteChatUser;
  }

  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.userJoin;
  }
}
