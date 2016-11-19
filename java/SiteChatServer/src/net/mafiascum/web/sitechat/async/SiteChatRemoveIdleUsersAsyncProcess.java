package net.mafiascum.web.sitechat.async;

import java.util.Date;

public class SiteChatRemoveIdleUsersAsyncProcess extends SiteChatAsyncProcess {

  public SiteChatRemoveIdleUsersAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(false, processor -> processor.removeIdleUsers(new Date())));
  }
}
