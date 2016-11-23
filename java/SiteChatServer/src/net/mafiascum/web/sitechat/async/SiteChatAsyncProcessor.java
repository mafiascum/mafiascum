package net.mafiascum.web.sitechat.async;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;

public class SiteChatAsyncProcessor {

  protected List<SiteChatAsyncProcess> asyncProcesses;
  
  public SiteChatAsyncProcessor() {
    asyncProcesses = new ArrayList<>();
  }
  
  public void addProcess(SiteChatAsyncProcess process) {
    asyncProcesses.add(process);
  }
  
  public void checkProcesses(SiteChatMessageProcessor processor) {
    
    LocalDateTime now = LocalDateTime.now();
    
    for(SiteChatAsyncProcess process : asyncProcesses)
      process.process(now, processor);
  }
}
