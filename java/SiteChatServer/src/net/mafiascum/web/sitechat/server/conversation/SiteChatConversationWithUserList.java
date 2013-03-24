package net.mafiascum.web.sitechat.server.conversation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SiteChatConversationWithUserList {

  protected Set<Integer> userIdSet = new HashSet<Integer>();
  protected SiteChatConversation siteChatConversation;
  protected List<SiteChatConversationMessage> siteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
  
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

  public List<SiteChatConversationMessage> getSiteChatConversationMessages() {
    return siteChatConversationMessages;
  }

  public void setSiteChatConversationMessages(
      List<SiteChatConversationMessage> siteChatConversationMessages) {
    this.siteChatConversationMessages = siteChatConversationMessages;
  }
}
