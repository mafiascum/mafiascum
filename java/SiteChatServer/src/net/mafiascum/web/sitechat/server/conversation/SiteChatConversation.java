package net.mafiascum.web.sitechat.server.conversation;

import java.util.Date;

import net.mafiascum.web.misc.DataObjectWithIntId;

public class SiteChatConversation extends DataObjectWithIntId {

  protected Date createdDatetime;
  protected int createdByUserId;
  protected String name;
  protected String password;
  
  public SiteChatConversation() {
    
    id = NEW;
  }
  
  public Date getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Date createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public int getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(int createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
}
