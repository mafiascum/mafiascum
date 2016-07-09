package net.mafiascum.web.sitechat.server;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;

public class BanManager {
  protected Map<Integer, UserGroup> userIdToBanUserGroupMap = new HashMap<Integer, UserGroup>();
  protected SiteChatServer server;
  protected SiteChatUtil siteChatUtil;
  protected QueryUtil queryUtil;
  protected Provider provider;
  
  public BanManager(SiteChatServer server, SiteChatUtil siteChatUtil, QueryUtil queryUtil, Provider provider) {
    this.server = server;
    this.siteChatUtil = siteChatUtil;
    this.queryUtil = queryUtil;
    this.provider = provider;
  }
  
  public void loadUserGroups(Collection<UserGroup> banUserGroups) {
    
    for(UserGroup newUserGroup : banUserGroups) {
      if(isUserBanned(newUserGroup) && !isUserBanned(newUserGroup.getUserId()))
        server.removeUser(newUserGroup.getUserId());
    }
    
    this.userIdToBanUserGroupMap = MiscUtil.get().map(banUserGroups, UserGroup::getUserId);
  }
  
  public UserGroup createBanUserGroup(int userId) {
    return new UserGroup(true, siteChatUtil.BANNED_USERS_GROUP_ID, userId, false, false, 0);
  }
  
  public boolean isUserBanned(int userId) {
    return isUserBanned(userIdToBanUserGroupMap.get(userId));
  }
  
  protected boolean isUserBanned(UserGroup userGroup) {
    return userGroup != null && !userGroup.getGroupLeader();
  }
  
  public boolean isBanAdmin(int userId) {
    return isBanAdmin(userIdToBanUserGroupMap.get(userId));
  }
  
  protected boolean isBanAdmin(UserGroup userGroup) {
    return userGroup != null && userGroup.getGroupLeader();
  }
  
  public void banUser(int userId) throws SQLException {
    
    if(isUserBanned(userId) || isBanAdmin(userId))
      return;
    
    userIdToBanUserGroupMap.put(userId, createBanUserGroup(userId));
    
    server.removeUser(userId);
    
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.addUserToUserGroup(connection, userId, siteChatUtil.BANNED_USERS_GROUP_ID));
  }
  
  public void unbanUser(int userId) throws SQLException {
    if(!isUserBanned(userId))
      return;
    
    userIdToBanUserGroupMap.remove(userId);
    
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.deleteUserGroup(connection, userId, siteChatUtil.BANNED_USERS_GROUP_ID));
  }
}
