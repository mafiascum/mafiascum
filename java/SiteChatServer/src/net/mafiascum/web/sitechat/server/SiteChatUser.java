package net.mafiascum.web.sitechat.server;

import java.util.Date;

import net.mafiascum.web.misc.DataObjectWithIntId;

public class SiteChatUser extends DataObjectWithIntId {
  
  protected String name;
  protected String avatarUrl;
  protected Date lastActivityDatetime;
  
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
  
  public Date getLastActivityDatetime() {
    
    return lastActivityDatetime;
  }
  
  public void setLastActivityDatetime(Date lastActivityDatetime) {
    
    this.lastActivityDatetime = lastActivityDatetime;
  }
}
