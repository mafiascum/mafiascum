package net.mafiascum.web.sitechat.server.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;

public class UserData {

  protected SiteChatUser user;
  protected List<Descriptor> descriptors = new ArrayList<>();
  protected LocalDateTime lastActivityDatetime;
  protected LocalDateTime lastNetworkActivityDatetime;
  protected SiteChatUserSettings userSettings;
  protected List<UserGroup> userGroups;
  
  public UserData() {
    userGroups = new ArrayList<>();
  }
  
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
  public List<UserGroup> getUserGroups() {
    return userGroups;
  }
  public void setUserGroups(List<UserGroup> userGroups) {
    this.userGroups = userGroups;
  }
  
  public UserPacket createUserPacket() {
    return new UserPacket(user, lastActivityDatetime);
  }
  
  public boolean isInvisible() {
    return userSettings != null && userSettings.getInvisible();
  }
  
  public boolean isInGroup(int groupId, Boolean leader, Boolean pending) {
    return userGroups.stream().filter(userGroup -> {
      return userGroup.getGroupId() == groupId
          && (leader == null || leader.equals(userGroup.getGroupLeader()))
          && (pending == null || pending.equals(userGroup.getUserPending()));
    }).findAny().isPresent();
  }
}
