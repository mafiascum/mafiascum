package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundConnectPacket extends SiteChatInboundPacket {
  
  protected String siteChatConversationName;
  protected String password;
  
  public SiteChatInboundPacketType getType() {
    
    return SiteChatInboundPacketType.connect;
  }

  public String getSiteChatConversationName() {
    return siteChatConversationName;
  }

  public void setSiteChatConversationName(String siteChatConversationName) {
    this.siteChatConversationName = siteChatConversationName;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
}
