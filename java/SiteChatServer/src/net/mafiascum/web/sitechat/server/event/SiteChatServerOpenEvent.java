package net.mafiascum.web.sitechat.server.event;

import net.mafiascum.web.sitechat.server.Descriptor;

public class SiteChatServerOpenEvent extends SiteChatServerEvent {

  protected Descriptor descriptor;
  
  public SiteChatServerOpenEvent(Descriptor descriptor) {
    setDescriptor(descriptor);
  }
  
  public Descriptor getDescriptor() {
    return descriptor;
  }
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }
  
  public SiteChatServerEventType getType() {
    return SiteChatServerEventType.open;
  }
}
