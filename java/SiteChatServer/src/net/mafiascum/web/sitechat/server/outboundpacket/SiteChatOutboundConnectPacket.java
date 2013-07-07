package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.Set;

import net.mafiascum.web.sitechat.server.SiteChatUser;

public class SiteChatOutboundConnectPacket extends SiteChatOutboundPacket {

  protected boolean wasSuccessful;
  protected int siteChatConversationId;
  protected String titleText;
  protected Set<SiteChatUser> users;
  protected int createdByUserId;
  
  public SiteChatOutboundConnectPacket() {
    
    super();
  }
  
  public boolean getWasSuccessful() {
    
    return wasSuccessful;
  }
  
  public void setWasSuccessful(boolean wasSuccessful) {
    
    this.wasSuccessful = wasSuccessful;
  }
  
  public SiteChatOutboundPacketType getType() {

    return SiteChatOutboundPacketType.connect;
  }

  public int getSiteChatConversationId() {
    return siteChatConversationId;
  }

  public void setSiteChatConversationId(int siteChatConversationId) {
    this.siteChatConversationId = siteChatConversationId;
  }

  public String getTitleText() {
    return titleText;
  }

  public void setTitleText(String titleText) {
    this.titleText = titleText;
  }
  
  public Set<SiteChatUser> getUsers() {
    return users;
  }
  
  public void setUsers(Set<SiteChatUser> users) {
    this.users = users;
  }
  
  public int getCreatedByUserId() {
    return createdByUserId;
  }
  
  public void setCreatedByUserId(int createdByUserId) {
    this.createdByUserId = createdByUserId;
  }
}
