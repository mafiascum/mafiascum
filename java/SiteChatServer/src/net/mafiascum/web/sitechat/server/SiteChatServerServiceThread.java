package net.mafiascum.web.sitechat.server;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.util.MiscUtil;

public class SiteChatServerServiceThread extends Thread {

  protected SiteChatServer siteChatServer;
  protected volatile boolean terminated;
  protected Date lastRefreshUserCacheDatetime;
  protected Date lastInactiveUserRemovalDatetime;
  
  protected final long MILLISECONDS_PER_USER_TABLE_UPDATE = (1L * 60L * 1000L); //Every minute.
  protected final long MILLISECONDS_PER_INACTIVE_USER_REMOVAL = (1L * 60L * 1000L); //Every minute.
  
  public SiteChatServerServiceThread(SiteChatServer siteChatServer) {
    
    this.siteChatServer = siteChatServer;
    this.lastRefreshUserCacheDatetime = new Date();
    this.lastInactiveUserRemovalDatetime = new Date();
  }
  
  public void run() {
   
    try {
      while(!getTerminated()) {
        
        Date nowDatetime = new Date();
        
        //Refresh conversation list members.
        if(nowDatetime.getTime() - lastInactiveUserRemovalDatetime.getTime() >= MILLISECONDS_PER_INACTIVE_USER_REMOVAL) {
          
          try {
            siteChatServer.removeIdleUsers(nowDatetime);
          }
          catch(Throwable throwable) {
              
            MiscUtil.log("Could not remove idle users:\n");
            throwable.printStackTrace();
          }
          
          
          lastInactiveUserRemovalDatetime = nowDatetime;
        }
        
        //Refresh User Cache
        if(nowDatetime.getTime() - lastRefreshUserCacheDatetime.getTime() >= MILLISECONDS_PER_USER_TABLE_UPDATE) {
          
          try {
            siteChatServer.refreshUserCache();
          }
          catch(Throwable throwable) {
              
            MiscUtil.log("Could not refresh user cache:\n");
            throwable.printStackTrace();
          }
          
          lastRefreshUserCacheDatetime = nowDatetime;
        }
                
        Thread.sleep( 10 * 1000 );
      }
    }
    catch(Throwable throwable) {
      
      throwable.printStackTrace();
    }
  }
  
  public synchronized boolean getTerminated() {
    
    return terminated;
  }
  
  public synchronized void terminate() {
    
    this.terminated = true;
  }
}
