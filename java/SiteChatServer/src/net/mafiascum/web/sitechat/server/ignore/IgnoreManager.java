package net.mafiascum.web.sitechat.server.ignore;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mafiascum.phpbb.log.ForumLog;
import net.mafiascum.phpbb.log.ForumLogOperation;
import net.mafiascum.phpbb.log.ForumLogType;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.SiteChatIgnore;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.user.UserData;
import net.mafiascum.web.sitechat.server.user.UserManager;
import de.ailis.pherialize.Pherialize;

public class IgnoreManager {
  protected List<SiteChatIgnore> ignores;
  protected Map<Integer, List<SiteChatIgnore>> userIdToIgnoresMap;
  protected MiscUtil miscUtil = MiscUtil.get();
  protected SiteChatUtil siteChatUtil = SiteChatUtil.get();
  protected QueryUtil queryUtil = QueryUtil.get();
  protected UserManager userManager;
  protected Provider provider;
  
  public IgnoreManager(Provider provider, UserManager userManager) {
    ignores = new ArrayList<>();
    userIdToIgnoresMap = new HashMap<>();
    this.provider = provider;
    this.userManager = userManager;
  }
  
  public void reload() throws SQLException {
    this.ignores = new ArrayList<SiteChatIgnore>(queryUtil.executeConnection(provider, connection -> siteChatUtil.getSiteChatIgnores(connection)));
    this.userIdToIgnoresMap = miscUtil.createGroupedMap(ignores, SiteChatIgnore::getUserId, ignore -> ignore);
  }
  
  protected void addCachedIgnore(SiteChatIgnore ignore) {
    this.ignores.add(ignore);
    miscUtil.pushToListMap(ignore.getUserId(), ignore, userIdToIgnoresMap);
  }
  
  public void removeCachedIgnore(int userId, int ignoredUserId) {
    this.ignores.removeIf(entry -> entry.getUserId() == userId && entry.getIgnoredUserId() == ignoredUserId);
    List<SiteChatIgnore> userIgnores = userIdToIgnoresMap.get(userId);
    
    if(userIgnores == null)
      return;
    
    userIgnores.removeIf(entry -> entry.getUserId() == userId && entry.getIgnoredUserId() == ignoredUserId);
    
    if(userIgnores.isEmpty())
      userIdToIgnoresMap.remove(userId);
  }
  
  public List<SiteChatIgnore> getIgnores(int userId) {
    List<SiteChatIgnore> userIgnores = userIdToIgnoresMap.get(userId);
    return userIgnores == null ? new ArrayList<>() : userIgnores;
  }
  
  public SiteChatIgnore getIgnore(int userId, int ignoredUserId) {
    List<SiteChatIgnore> ignores = getIgnores(userId);
    for(SiteChatIgnore ignore : ignores)
      if(ignore.getIgnoredUserId() == ignoredUserId)
        return ignore;
    return null;
  }
  
  public SiteChatIgnore addIgnore(final int userId, final int ignoredUserId, final String ipAddress) throws SQLException {
    SiteChatIgnore ignore = getIgnore(userId, ignoredUserId);
    String addOrModify;
    
    if(ignore == null) {
      ignore = new SiteChatIgnore();
      ignore.setUserId(userId);
      ignore.setIgnoredUserId(ignoredUserId);
      ignore.setCreatedDatetime(LocalDateTime.now());
      addOrModify = "added";
    }
    else
      addOrModify = "modified";
    
    boolean added = ignore != null && ignore.isNew();
    
    final SiteChatIgnore ignoreFinal = ignore;
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatIgnore(connection, ignoreFinal));
    if(added)
      addCachedIgnore(ignore);
    
    addForumLog(userId, ignoredUserId, addOrModify, ipAddress);
    
    return ignore;
  }
  
  protected void addForumLog(int userId, int ignoredUserId, String addModifyOrDelete, String ipAddress) throws SQLException {
    UserData ignoredUser = userManager.getUser(ignoredUserId);
    List<String> forumLogDataList = new ArrayList<>(Arrays.asList(addModifyOrDelete, ignoredUser.getUser().getName()));
    
    queryUtil.executeConnectionNoResult(provider, connection -> createForumLog(connection, userId, ipAddress, Pherialize.serialize(forumLogDataList)));
  }
  
  public void createForumLog(Connection connection, int userId, String ipAddress, String data) throws SQLException {
    ForumLog log = new ForumLog(ForumLogType.users, userId, 0, 0, 0, ipAddress, System.currentTimeMillis() / 1000, ForumLogOperation.siteChatIgnore.getLanguageKey(), data);
    siteChatUtil.putForumLog(connection, log);
  }
  
  public void removeIgnore(String ipAddress, int userId, int ignoredUserId) throws SQLException {
    
    if(getIgnore(userId, ignoredUserId) == null)
      return;
    
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.removeSiteChatIgnore(connection, userId, ignoredUserId));
    removeCachedIgnore(userId, ignoredUserId);
    
    addForumLog(userId, ignoredUserId, "removed", ipAddress);
  }
}
