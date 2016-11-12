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

import org.apache.log4j.Logger;

import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class UserManager {
  protected Map<Integer, UserData> userIdToUserMap = new HashMap<Integer, UserData>();
  protected Map<String, UserData> usernameToUserMap = new HashMap<String, UserData>();
  
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
  
  public void updateUserActivity(int userId) {
    UserData userData = getUser(userId);
    
    if(userData == null)
      return;
    
    userData.setLastActivityDatetime(LocalDateTime.now());
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
  
  public void setupUsernameToUserMap() {
    this.usernameToUserMap = MiscUtil.get().map(userIdToUserMap.values(), user -> getUsernameMapKey(user.getUser().getName()));
  }
  
  public void loadUserMap(Collection<SiteChatUser> users, Collection<SiteChatUserSettings> settingsList) {
    for(SiteChatUser user : users) {
      UserData userData = getUser(user.getId());
      
      if(userData == null)
        userData = new UserData();
      
      userData.setUser(user);
      
      userIdToUserMap.put(user.getId(), userData);
    }
    
    for(SiteChatUserSettings userSettings : settingsList) {
      UserData userData = getUser(userSettings.getUserId());
      
      if(userData != null)
        userData.setUserSettings(userSettings);
    }
    
    setupUsernameToUserMap();
  }
  
  public void associateWebSocketWithUser(int userId, SiteChatWebSocket webSocket) {
    getUser(userId).getWebSockets().add(webSocket);
  }
  
  public void removeWebSocketFromUser(int userId, SiteChatWebSocket webSocket) {
    getUser(userId).getWebSockets().remove(webSocket);
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
    LocalDateTime lastNetworkActivityDatetime = userData.getLastActivityDatetime();
    if(lastNetworkActivityDatetime == null)
      return false;
    
    return (contextDatetimeMilliseconds - lastNetworkActivityDatetime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) >= MILLISECONDS_UNTIL_USER_IS_INACTIVE;
  }
  
  public List<SiteChatUser> getClonedSiteChatUserList(Predicate<UserData> predicate) {
    List<SiteChatUser> userList = new ArrayList<>();
    
    for(UserData userData : userIdToUserMap.values())
      if(predicate.test(userData))
        userList.add(new SiteChatUser(userData.getUser()));
    
    return userList;
  }
  
  private static final Logger logger = Logger.getLogger(UserManager.class.getName());
  
  public void setUserSettings(int userId, boolean compact, boolean animateAvatars, String timestampFormat) throws SQLException {
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
      
      saveUserSettings(settings);
    });
  }
  
  protected void saveUserSettings(SiteChatUserSettings settings) throws SQLException {
    QueryUtil.get().executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatUserSettings(connection, settings));
  }
}
