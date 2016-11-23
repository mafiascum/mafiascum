package net.mafiascum.web.sitechat.async;

import java.util.List;
import java.util.Map;

import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class SiteChatUserTableAsyncProcess extends SiteChatAsyncProcess {
  
  protected Map<Integer, SiteChatUser> siteChatUserMap;
  protected List<SiteChatUserSettings> userSettingsList;
  protected List<UserGroup> userGroups;
  
  protected QueryUtil queryUtil;
  protected SiteChatUtil siteChatUtil;
  
  public SiteChatUserTableAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
    this.siteChatUtil = SiteChatUtil.get();
    this.queryUtil = QueryUtil.get();
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(true, processor -> {
      siteChatUserMap = queryUtil.executeConnection(processor.getProvider(), connection -> siteChatUtil.loadSiteChatUserMap(connection));
      userSettingsList = queryUtil.executeConnection(processor.getProvider(), connection -> siteChatUtil.getSiteChatUserSettingsList(connection));
      userGroups = queryUtil.executeConnection(processor.getProvider(), connection -> siteChatUtil.getUserGroups(connection));
    }));
    
    operations.add(new SiteChatAsyncProcessOperation(false, processor -> {
      processor.getUserManager().loadUserMap(siteChatUserMap.values(), userSettingsList, userGroups);
      
      siteChatUserMap = null;
      userSettingsList = null;
      userGroups = null;
    }));
  }
}
