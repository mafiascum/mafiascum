package net.mafiascum.web.sitechat.server.user;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class UserManager {
  protected Map<Integer, UserData> userIdToUserMap = new HashMap<>();
  protected Map<String, UserData> usernameToUserMap = new HashMap<>();
  protected Map<String, Integer> descriptorIdToUserIdMap = new HashMap<>();
  
  protected Provider provider;
  protected SiteChatUtil siteChatUtil;
  
  protected final long MILLISECONDS_UNTIL_USER_IS_INACTIVE = (1000) * (60) * (5);
  
  public UserManager(Provider provider, SiteChatUtil siteChatUtil) {
    this.provider = provider;
    this.siteChatUtil = siteChatUtil;
  }
  
  protected String getUsernameMapKey(String username) {
    return username.toLowerCase();
  }
  
  public UserData getUser(String username) {
    return usernameToUserMap.get(getUsernameMapKey(username));
  }
  
  public UserData getUser(int userId) {
    return userIdToUserMap.get(userId);
  }
  
  public UserData getUserByDescriptorId(String descriptorId) {
    Integer userId = descriptorIdToUserIdMap.get(descriptorId);
    
    if(userId == null)
      return null;
    
    return getUser(userId);
  }
  
  public void updateUserActivity(int userId) {
    UserData userData = getUser(userId);
    
    if(userData == null)
      return;
    
    userData.setLastActivityDatetime(LocalDateTime.now());
  }

  public void updateUserNetworkActivity(int userId) {
    UserData userData = getUser(userId);
    
    if(userData == null)
      return;
    
    userData.setLastNetworkActivityDatetime(LocalDateTime.now());
  }
  
  public Map<Integer, SiteChatUser> getSiteChatUserMap(Collection<Integer> userIds) {
    Map<Integer, SiteChatUser> siteChatUserMap = new HashMap<>();
    
    for(Integer userId : userIds) {
      
      UserData userData = getUser(userId);
      
      if(userData != null)
        siteChatUserMap.put(userId, userData.getUser());
    }
    
    return siteChatUserMap;
  }
  
  public Map<Integer, UserData> getUserDataMap(Collection<Integer> userIds) {
    Map<Integer, UserData> userDataMap = new HashMap<>();
    
    for(Integer userId : userIds) {
      
      UserData userData = getUser(userId);
      
      if(userData != null)
        userDataMap.put(userId, userData);
    }
    
    return userDataMap;
  }
  
  public void setupUsernameToUserMap() {
    this.usernameToUserMap = MiscUtil.get().map(userIdToUserMap.values(), user -> getUsernameMapKey(user.getUser().getName()));
  }
  
  public void loadUserMap(Collection<SiteChatUser> users, Collection<SiteChatUserSettings> settingsList, Collection<UserGroup> userGroups) {
    for(SiteChatUser user : users) {
      UserData userData = getUser(user.getId());
      
      if(userData == null)
        userData = new UserData();
      
      userData.setUser(user);
      userData.getUserGroups().clear();
      
      userIdToUserMap.put(user.getId(), userData);
    }
    
    for(SiteChatUserSettings userSettings : settingsList) {
      UserData userData = getUser(userSettings.getUserId());
      
      if(userData != null)
        userData.setUserSettings(userSettings);
    }
    
    for(UserGroup userGroup : userGroups) {
      UserData userData = getUser(userGroup.getUserId());
      
      if(userData != null) 
        userData.getUserGroups().add(userGroup);
    }
    
    setupUsernameToUserMap();
  }
  
  public void associateDescriptorUser(int userId, Descriptor descriptor) {
    getUser(userId).getDescriptors().add(descriptor);
    descriptorIdToUserIdMap.put(descriptor.getId(), userId);
  }
  
  public void removeDescriptorFromUser(String descriptorId) {
    Integer userId = descriptorIdToUserIdMap.get(descriptorId);
    
    if(userId == null)
      return;
    
    descriptorIdToUserIdMap.remove(descriptorId);
    getUser(userId).getDescriptors().removeIf(descriptor -> descriptor.getId().equals(descriptorId));
  }
  
  public Set<Integer> getIdleUserIdSet() {
    Set<Integer> idleUserIdSet = new HashSet<>();
    long contextDatetimeMilliseconds = System.currentTimeMillis();
    
    for(UserData userData : userIdToUserMap.values()) {
      if(isUserInactive(userData, contextDatetimeMilliseconds))
        idleUserIdSet.add(userData.getUser().getId());
    }
    
    return idleUserIdSet;
  }
  
  protected boolean isUserInactive(UserData userData, long contextDatetimeMilliseconds) {
    LocalDateTime lastNetworkActivityDatetime = userData.getLastNetworkActivityDatetime();
    if(lastNetworkActivityDatetime == null)
      return false;
    
    return (contextDatetimeMilliseconds - lastNetworkActivityDatetime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) >= MILLISECONDS_UNTIL_USER_IS_INACTIVE;
  }
  
  public List<UserPacket> getClonedSiteChatUserList(Predicate<UserData> predicate) {
    List<UserPacket> userList = new ArrayList<>();
    
    for(UserData userData : userIdToUserMap.values())
      if(predicate.test(userData))
        userList.add(userData.createUserPacket());
    
    return userList;
  }
  
  public void setUserSettings(int userId, boolean compact, boolean animateAvatars, boolean invisible, String timestampFormat) throws SQLException {
    QueryUtil.get().executeConnectionNoResult(provider, connection -> {
        
      UserData userData = getUser(userId);
      SiteChatUserSettings settings = userData.getUserSettings();
      
      if(settings == null)
        settings = siteChatUtil.getSiteChatUserSettings(connection, userId);
      if(settings == null)
        settings = new SiteChatUserSettings();
      
      if(settings.isNew()) {
        settings.setUserId(userId);
        userData.setUserSettings(settings);
      }
      
      settings.setCompact(compact);
      settings.setAnimateAvatars(animateAvatars);
      settings.setTimestampFormat(timestampFormat);
      settings.setInvisible(invisible);
      
      saveUserSettings(settings);
    });
  }
  
  protected void saveUserSettings(SiteChatUserSettings settings) throws SQLException {
    QueryUtil.get().executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatUserSettings(connection, settings));
  }
  
  public int getAdminGroupId() {
    return 13637;
  }
  
  public int getBanGroupId() {
    return 13662;
  }
  
  public boolean isAdmin(UserData userData) {
    return userData.isInGroup(getAdminGroupId(), null, false);
  }
  
  public boolean isChatMod(UserData userData) {
    return userData.isInGroup(getBanGroupId(), true, false);
  }
}
