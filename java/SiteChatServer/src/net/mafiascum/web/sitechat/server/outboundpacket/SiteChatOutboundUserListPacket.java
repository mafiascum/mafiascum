package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.Date;
import java.util.List;
import java.util.Set;

import net.mafiascum.web.sitechat.server.conversation.SiteChatBarebonesConversation;
import net.mafiascum.web.sitechat.server.user.UserPacket;

public class SiteChatOutboundUserListPacket extends SiteChatOutboundPacket {

  protected List<UserPacket> siteChatUsers;
  protected List<SiteChatBarebonesConversation> siteChatConversations;
  protected Set<Integer> invisibleUserIds;
  protected Date packetSentDatetime;

  public List<UserPacket> getSitechatUsers() {
    
    return siteChatUsers;
  }
  
  public void setSiteChatUsers(List<UserPacket> siteChatUsers) {
    
    this.siteChatUsers = siteChatUsers;
  }
  
  public SiteChatOutboundPacketType getType() {
    return SiteChatOutboundPacketType.userList;
  }
  
  public Date getPacketSentDatetime() {
    
    return packetSentDatetime;
  }
  
  public void setPacketSentDatetime(Date packetSentDatetime) {
    
    this.packetSentDatetime = packetSentDatetime;
  }

  public List<SiteChatBarebonesConversation> getSiteChatConversations() {
    return siteChatConversations;
  }

  public void setSiteChatConversations(List<SiteChatBarebonesConversation> siteChatConversations) {
    this.siteChatConversations = siteChatConversations;
  }
  
  public Set<Integer> getInvisibleUserIds() {
    return invisibleUserIds;
  }
  
  public void setInvisibleUserIds(Set<Integer> invisibleUserIds) {
    this.invisibleUserIds = invisibleUserIds;
  }
}
