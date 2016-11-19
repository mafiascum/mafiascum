package net.mafiascum.web.sitechat.server.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;

public class UserData {

  protected SiteChatUser user;
  protected List<Descriptor> descriptors = new ArrayList<>();
  protected LocalDateTime lastActivityDatetime;
  protected LocalDateTime lastNetworkActivityDatetime;
  protected SiteChatUserSettings userSettings;
  
  public int getId() {
    return user.getId();
  }
  
  public SiteChatUser getUser() {
    return user;
  }
  public void setUser(SiteChatUser user) {
    this.user = user;
  }
  public List<Descriptor> getDescriptors() {
    return descriptors;
  }
  public void setDescriptors(List<Descriptor> descriptors) {
    this.descriptors = descriptors;
  }
  public LocalDateTime getLastActivityDatetime() {
    return lastActivityDatetime;
  }
  public void setLastActivityDatetime(LocalDateTime lastActivityDatetime) {
    this.lastActivityDatetime = lastActivityDatetime;
  }
  public LocalDateTime getLastNetworkActivityDatetime() {
    return lastNetworkActivityDatetime;
  }
  public void setLastNetworkActivityDatetime(LocalDateTime lastNetworkActivityDatetime) {
    this.lastNetworkActivityDatetime = lastNetworkActivityDatetime;
  }
  public SiteChatUserSettings getUserSettings() {
    return userSettings;
  }
  public void setUserSettings(SiteChatUserSettings userSettings) {
    this.userSettings = userSettings;
  }
  
  public UserPacket createUserPacket() {
    return new UserPacket(user, lastActivityDatetime);
  }
}
