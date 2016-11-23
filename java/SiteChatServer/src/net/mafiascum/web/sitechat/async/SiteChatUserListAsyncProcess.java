package net.mafiascum.web.sitechat.async;


public class SiteChatUserListAsyncProcess extends SiteChatAsyncProcess {
  
  public SiteChatUserListAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(false, server -> server.sendUserListToAllDescriptors()));
  }
}
