package net.mafiascum.web.sitechat.async;

import java.util.concurrent.atomic.AtomicBoolean;

import net.mafiascum.functional.ConsumerWithException;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;

import org.apache.log4j.Logger;

public class SiteChatAsyncProcessOperation {

  protected boolean async;
  protected AtomicBoolean hasStarted;
  protected AtomicBoolean isFinished;
  protected ConsumerWithException<SiteChatMessageProcessor> consumer;

  private static final Logger logger = Logger.getLogger(SiteChatAsyncProcessOperation.class.getName());
  
  public SiteChatAsyncProcessOperation(boolean async, ConsumerWithException<SiteChatMessageProcessor> consumer) {
    this.consumer = consumer;
    this.async = async;
    this.hasStarted = new AtomicBoolean(false);
    this.isFinished = new AtomicBoolean(false);
  }
  
  public boolean getHasStarted() {
    return hasStarted.get();
  }
  
  public boolean getIsFinished() {
    return isFinished.get();
  }
  
  public void reset() {
    this.hasStarted.set(false);
    this.isFinished.set(false);
  }
  
  public void start(SiteChatMessageProcessor processor) {
    this.hasStarted.set(true);
    this.isFinished.set(false);
    
    Runnable innerRunnable = () -> {
      try {
        consumer.accept(processor);
      }
      catch(Exception exception) {
        logger.error("Error running async operation.", exception);
      }
      finally {
        isFinished.set(true);
      }
    };
    
    if(async) {
      Thread thread = new Thread(innerRunnable);
      thread.setDaemon(true);
      thread.start();
    }
    else {
      innerRunnable.run();
    }
  }
}
