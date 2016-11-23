package net.mafiascum.web.sitechat.async;

import java.util.List;

import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class SiteChatRefreshBansAsyncProcess extends SiteChatAsyncProcess {
  
  protected List<UserGroup> banUserGroups;
  
  public SiteChatRefreshBansAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(true, processor -> {
      QueryUtil.get().executeConnectionNoResult(processor.getProvider(), connection -> banUserGroups = SiteChatUtil.get().getBanUserGroups(connection));
    }));
    
    operations.add(new SiteChatAsyncProcessOperation(false, processor -> {
      processor.getBanManager().loadUserGroups(banUserGroups);
    }));
  }
}
