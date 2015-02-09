package net.mafiascum.web.sitechat.server.conversation;

import java.util.Set;

import net.mafiascum.web.misc.DataObjectWithIntID;

public class SiteChatBarebonesConversation extends DataObjectWithIntID {
  
  protected String name;
  protected Set<Integer> userIdSet;
  protected int createdByUserId;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Set<Integer> getUserIdSet() {
    return userIdSet;
  }
  
  public void setUserIdSet(Set<Integer> userIdSet) {
    this.userIdSet = userIdSet;
  }
  
  public int getCreatedByUserId() {
    return createdByUserId;
  }
  
  public void setCreatedByUserId(int createdByUserId) {
    this.createdByUserId = createdByUserId;
  }
}
