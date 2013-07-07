package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundSetPasswordPacket extends SiteChatInboundPacket {

  protected int conversationId;
  protected String password;
  
  public int getConversationId() {
    return conversationId;
  }
  
  public void setConversationId(int conversationId) {
    this.conversationId = conversationId;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public SiteChatInboundPacketType getType() {
    
    return SiteChatInboundPacketType.setPassword;
  }
}
