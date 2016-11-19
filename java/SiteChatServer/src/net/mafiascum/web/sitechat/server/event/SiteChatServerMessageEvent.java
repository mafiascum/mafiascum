package net.mafiascum.web.sitechat.server.event;

import net.mafiascum.web.sitechat.server.Descriptor;

public class SiteChatServerMessageEvent extends SiteChatServerEvent {
  
  protected Descriptor descriptor;
  protected String message;
  
  public SiteChatServerMessageEvent(Descriptor descriptor, String message) {
    setMessage(message);
    setDescriptor(descriptor);
  }
  
  public Descriptor getDescriptor() {
    return descriptor;
  }
  
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public SiteChatServerEventType getType() {
    return SiteChatServerEventType.message;
  }
}
