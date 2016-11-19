package net.mafiascum.web.sitechat.async;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;

public abstract class SiteChatAsyncProcess {
  
  protected List<SiteChatAsyncProcessOperation> operations;
  protected int currentOperationIndex;
  
  protected LocalDateTime lastRunDatetime;
  protected long miliSecondsBetweenRun;
  
  public SiteChatAsyncProcess(long miliSecondsBetweenRun) {
    this.miliSecondsBetweenRun = miliSecondsBetweenRun;
    operations = new ArrayList<>();
    
    currentOperationIndex = -1;
    
    fillOperations();
  }
  
  public void process(LocalDateTime now, SiteChatMessageProcessor processor) {
    if(currentOperationIndex == -1 && !readyToRun(now))
      return;
    
    while(true) {
      
      //Verify that current operation has completed.
      if(currentOperationIndex >= 0) {
        SiteChatAsyncProcessOperation currentOperation = operations.get(currentOperationIndex);
        
        if(!currentOperation.getIsFinished())
          break;
      }
      
      //Proceed to the next operation.
      if(++currentOperationIndex >= operations.size()) {
        currentOperationIndex = -1;
        lastRunDatetime = now;
        break;
      }
      
      SiteChatAsyncProcessOperation nextOperation = operations.get(currentOperationIndex);
      nextOperation.start(processor);
    }
  }
  
  public void start(LocalDateTime now) {
    lastRunDatetime = now;
    currentOperationIndex = -1;
  }
  
  public boolean readyToRun(LocalDateTime now) {
    if(lastRunDatetime == null)
      return true;
    
    return ChronoUnit.MILLIS.between(lastRunDatetime, now) >= miliSecondsBetweenRun;
  }
  
  protected abstract void fillOperations();
}
