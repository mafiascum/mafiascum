package net.mafiascum.web.sitechat.server.inboundpacket;

import java.util.Map;
import java.util.Set;

public class SiteChatInboundLogInPacket extends SiteChatInboundPacket {

  protected int userId;
  protected String sessionId;
  protected Set<Integer> conversationIdSet;
  protected Map<Integer, Integer> conversationIdToMostRecentMessageIdMap;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
  
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Set<Integer> getConversationIdSet() {
    return conversationIdSet;
  }

  public void setConversationIdSet(Set<Integer> conversationIdSet) {
    this.conversationIdSet = conversationIdSet;
  }
  
  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.login;
  }

  public Map<Integer, Integer> getConversationIdToMostRecentMessageIdMap() {
    return conversationIdToMostRecentMessageIdMap;
  }

  public void setConversationIdToMostRecentMessageIdMap(
      Map<Integer, Integer> conversationIdToMostRecentMessageIdMap) {
    this.conversationIdToMostRecentMessageIdMap = conversationIdToMostRecentMessageIdMap;
  }
}
