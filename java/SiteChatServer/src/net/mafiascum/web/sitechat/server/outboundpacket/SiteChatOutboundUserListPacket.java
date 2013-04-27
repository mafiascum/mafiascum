package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.List;

import net.mafiascum.web.sitechat.server.SiteChatUser;

public class SiteChatOutboundUserListPacket extends SiteChatOutboundPacket {

  protected List<SiteChatUser> siteChatUsers;

  public List<SiteChatUser> getSitechatUsers() {
    
    return siteChatUsers;
  }
  
  public void setSiteChatUsers(List<SiteChatUser> siteChatUsers) {
    
    this.siteChatUsers = siteChatUsers;
  }
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.userList;
  }
}
