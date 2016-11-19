package net.mafiascum.web.sitechat.async;

import java.util.List;
import java.util.Map;

import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

public class SiteChatUserTableAsyncProcess extends SiteChatAsyncProcess {
  
  protected Map<Integer, SiteChatUser> siteChatUserMap;
  protected List<SiteChatUserSettings> userSettingsList;
  
  public SiteChatUserTableAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(true, processor -> {
      siteChatUserMap = QueryUtil.get().executeConnection(processor.getProvider(), connection -> SiteChatUtil.get().loadSiteChatUserMap(connection));
    }));
    
    operations.add(new SiteChatAsyncProcessOperation(true, processor -> {
      userSettingsList = QueryUtil.get().executeConnection(processor.getProvider(), connection -> SiteChatUtil.get().getSiteChatUserSettingsList(connection));
    }));
    
    operations.add(new SiteChatAsyncProcessOperation(false, processor -> {
      processor.getUserManager().loadUserMap(siteChatUserMap.values(), userSettingsList);
    }));
  }
}
