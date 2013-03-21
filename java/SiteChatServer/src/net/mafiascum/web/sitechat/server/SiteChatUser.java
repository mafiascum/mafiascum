package net.mafiascum.web.sitechat.server;

import net.mafiascum.web.misc.DataObjectWithIntId;

public class SiteChatUser extends DataObjectWithIntId {
  
  protected String name;
  protected String avatarUrl;
  
  public SiteChatUser() {
    
    id = NEW;
  }
  
  public String getName() {
    
    return name;
  }
  
  public void setName(String name) {
    
    this.name = name;
  }
  
  public String getAvatarUrl() {
    
    return avatarUrl;
  }
  
  public void setAvatarUrl(String avatarUrl) {
    
    this.avatarUrl = avatarUrl;
  }
}
