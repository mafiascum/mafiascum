package net.mafiascum.web.sitechat.server.inboundpacket;

import java.util.Map;
import java.util.Set;

public class SiteChatInboundLogInPacket extends SiteChatInboundPacket {
  
  protected int userId;
  protected String sessionId;
  protected Set<String> conversationKeySet;
  protected Map<String, Integer> conversationKeyToMostRecentMessageIdMap;
  protected Map<Integer, String> coversationIdToAuthCodeMap;

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

  public Set<String> getConversationKeySet() {
    return conversationKeySet;
  }

  public void setConversationKeySet(Set<String> conversationKeySet) {
    this.conversationKeySet = conversationKeySet;
  }
  
  public SiteChatInboundPacketType getType() {
    return SiteChatInboundPacketType.login;
  }

  public Map<String, Integer> getConversationKeyToMostRecentMessageIdMap() {
    return conversationKeyToMostRecentMessageIdMap;
  }

  public void setConversationKeyToMostRecentMessageIdMap(Map<String, Integer> conversationKeyToMostRecentMessageIdMap) {
    this.conversationKeyToMostRecentMessageIdMap = conversationKeyToMostRecentMessageIdMap;
  }
  
  public Map<Integer, String> getCoversationIdToAuthCodeMap() {
    return coversationIdToAuthCodeMap;
  }
  
  public void setCoversationIdToAuthCodeMap(Map<Integer, String> coversationIdToAuthCodeMap) {
    this.coversationIdToAuthCodeMap = coversationIdToAuthCodeMap;
  }
}
