package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mafiascum.phpbb.log.ForumLog;
import net.mafiascum.phpbb.log.ForumLogOperation;
import net.mafiascum.phpbb.log.ForumLogType;
import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.DateUtil;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import de.ailis.pherialize.Pherialize;

public class BanManager {
  protected Map<Integer, UserGroup> userIdToBanUserGroupMap = new HashMap<Integer, UserGroup>();
  protected SiteChatMessageProcessor processor;
  protected SiteChatUtil siteChatUtil;
  protected QueryUtil queryUtil;
  protected Provider provider;
  protected DateUtil dateUtil = DateUtil.get();
  
  public BanManager(SiteChatMessageProcessor processor, SiteChatUtil siteChatUtil, QueryUtil queryUtil, Provider provider) {
    this.processor = processor;
    this.siteChatUtil = siteChatUtil;
    this.queryUtil = queryUtil;
    this.provider = provider;
  }
  
  public void loadUserGroups(Collection<UserGroup> banUserGroups) {
    
    for(UserGroup newUserGroup : banUserGroups) {
      if(isUserBanned(newUserGroup) && !isUserBanned(newUserGroup.getUserId()))
        processor.removeUser(newUserGroup.getUserId());
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
  
  public void banUser(String ipAddress, int submittedByUserId, SiteChatUser bannedUser) throws SQLException {
    
    int userId = bannedUser.getId();
    if(isUserBanned(userId) || isBanAdmin(userId))
      return;
    
    userIdToBanUserGroupMap.put(userId, createBanUserGroup(userId));
    
    processor.removeUser(userId);
    
    long autoRemoveTime = dateUtil.currentTimeSeconds() + (60 * 60 * 24 * 2);//Two days.
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.addUserToUserGroup(connection, userId, siteChatUtil.BANNED_USERS_GROUP_ID, autoRemoveTime));
    queryUtil.executeConnectionNoResult(provider, connection -> createForumLog(connection, ipAddress, submittedByUserId, "added", "2 days", bannedUser));
  }
  
  public void unbanUser(final String ipAddress, final int submittedByUserId, final SiteChatUser bannedUser) throws SQLException {
    int userId = bannedUser.getId();
    if(!isUserBanned(userId))
      return;
    
    userIdToBanUserGroupMap.remove(userId);
    
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.deleteUserGroup(connection, userId, siteChatUtil.BANNED_USERS_GROUP_ID));
    queryUtil.executeConnectionNoResult(provider, connection -> createForumLog(connection, ipAddress, submittedByUserId, "removed", null, bannedUser));
  }
  
  protected void createForumLog(Connection connection, String ipAddress, int submittedByUserId, String addedOrRemoved, String durationDisplay, SiteChatUser bannedUser) throws SQLException {
    List<String> data = new ArrayList<>(Arrays.asList(addedOrRemoved, bannedUser.getName()));
    
    if(durationDisplay != null)
      data.add(durationDisplay);
    
    ForumLog log = new ForumLog(ForumLogType.mod, submittedByUserId, 0, 0, 0, ipAddress, System.currentTimeMillis() / 1000, ForumLogOperation.siteChatBan.getLanguageKey(), Pherialize.serialize(data));
    siteChatUtil.putForumLog(connection, log);
  }
}
