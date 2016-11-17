package net.mafiascum.web.sitechat.server.outboundpacket;

import java.util.List;
import java.util.Map;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.ignore.IgnorePacket;

public class SiteChatOutboundLogInPacket extends SiteChatOutboundPacket {

  protected boolean wasSuccessful;
  protected Map<String, Object> settings;
  protected List<SiteChatConversationMessage> missedSiteChatConversationMessages;
  protected List<IgnorePacket> ignores;
  
  public SiteChatOutboundLogInPacket() {
    
    super();
  }
  
  public boolean getWasSuccessful() {
    
    return wasSuccessful;
  }
  
  public void setWasSuccessful(boolean wasSuccessful) {
    
    this.wasSuccessful = wasSuccessful;
  }
  
  public List<SiteChatConversationMessage> getMissedSiteChatConversationMessages() {
    return missedSiteChatConversationMessages;
  }

  public void setMissedSiteChatConversationMessages(List<SiteChatConversationMessage> missedSiteChatConversationMessages) {
    this.missedSiteChatConversationMessages = missedSiteChatConversationMessages;
  }
  
  public Map<String, Object> getSettings() {
    return settings;
  }
  
  public void setSettings(Map<String, Object> settings) {
    this.settings = settings;
  }
  
  public List<IgnorePacket> getIgnores() {
    return ignores;
  }
  
  public void setIgnores(List<IgnorePacket> ignores) {
    this.ignores = ignores;
  }
  
  public SiteChatOutboundPacketType getType() {

    return SiteChatOutboundPacketType.login;
  }
}
