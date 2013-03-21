package net.mafiascum.web.sitechat.server.conversation;

import java.util.HashSet;
import java.util.Set;

public class SiteChatConversationWithUserList {

  protected Set<Integer> userIdSet = new HashSet<Integer>();
  protected SiteChatConversation siteChatConversation;
  
  public Set<Integer> getUserIdSet() {
    return userIdSet;
  }
  
  public void setUserIdSet(Set<Integer> userIdSet) {
    this.userIdSet = userIdSet;
  }
  
  public SiteChatConversation getSiteChatConversation() {
    return siteChatConversation;
  }
  
  public void setSiteChatConversation(SiteChatConversation siteChatConversation) {
    this.siteChatConversation = siteChatConversation;
  }
}
