package net.mafiascum.web.sitechat.server;

import java.util.Date;

import net.mafiascum.util.MiscUtil;

import org.apache.log4j.Logger;

public class SiteChatServerServiceThread extends Thread {

  protected SiteChatServer siteChatServer;
  protected volatile boolean terminated;
  protected Date lastRefreshUserCacheDatetime;
  protected Date lastInactiveUserRemovalDatetime;
  protected Date lastUserListDatetime;
  protected Date lastBannedUserListLoadedDatetime;
  
  protected static final long MILLISECONDS_PER_USER_TABLE_UPDATE = (1L * 60L * 1000L); //Every minute.
  protected static final long MILLISECONDS_PER_INACTIVE_USER_REMOVAL = (1L * 60L * 1000L); //Every minute.
  protected static final long MILLISECONDS_PER_USER_LIST = (30L * 1000L); //Every 30 seconds.
  protected static final long MILLISECONDS_PER_BAN_USER_GROUP_REFRESH = (5L * 60L * 1000); //Every 5 minutes.
  
  protected Logger logger = Logger.getLogger(SiteChatServerServiceThread.class.getName());
  
  public SiteChatServerServiceThread(SiteChatServer siteChatServer) {
    
    this.siteChatServer = siteChatServer;
    this.lastRefreshUserCacheDatetime = new Date();
    this.lastInactiveUserRemovalDatetime = new Date();
    this.lastUserListDatetime = new Date();
    this.lastBannedUserListLoadedDatetime = new Date();
  }
  
  public void run() {
   
    try {
      while(!getTerminated()) {
        
        Date nowDatetime = new Date();
        
        
        if(nowDatetime.getTime() - lastUserListDatetime.getTime() >= MILLISECONDS_PER_USER_LIST) {
          
          try {
            logger.debug("Sending user list to all web sockets.");
            siteChatServer.sendUserListToAllWebSockets();
            logger.debug("User list sent to all web sockets.");
            
            siteChatServer.printContainerSizes();
          }
          catch(Throwable throwable) {
            
            logger.error("Could not send user list:", throwable);
          }
          
          lastUserListDatetime = nowDatetime;
        }
        //Refresh conversation list members.
        if(nowDatetime.getTime() - lastInactiveUserRemovalDatetime.getTime() >= MILLISECONDS_PER_INACTIVE_USER_REMOVAL) {
          
          try {
            
            logger.debug("Removing idle users.");
            siteChatServer.removeIdleUsers(nowDatetime);
            logger.debug("Idle users removed.");
          }
          catch(Throwable throwable) {
              
            logger.error("Could not remove idle users:", throwable);
          }
          
          
          lastInactiveUserRemovalDatetime = nowDatetime;
        }
        
        //Refresh banned user ID set.
        if(nowDatetime.getTime() - lastBannedUserListLoadedDatetime.getTime() >= MILLISECONDS_PER_BAN_USER_GROUP_REFRESH) {
          
          try {
            
            logger.debug("Refreshing ban user list.");
            siteChatServer.refreshBanUserList();
            logger.debug("Ban user list refreshed.");
          }
          catch(Throwable throwable) {
            
            logger.error("Could not load banned user ID set:", throwable);
          }
          
          
          lastBannedUserListLoadedDatetime = nowDatetime;
        }
        
        //Refresh User Cache
        if(nowDatetime.getTime() - lastRefreshUserCacheDatetime.getTime() >= MILLISECONDS_PER_USER_TABLE_UPDATE) {
          
          try {
            
            logger.debug("Refreshing user cache.");
            siteChatServer.refreshUserCache();
            logger.debug("User cache refreshed.");
          }
          catch(Throwable throwable) {
              
            logger.error("Could not refresh user cache:\n", throwable);
          }
          
          lastRefreshUserCacheDatetime = nowDatetime;
        }
        
        Thread.sleep( 10 * 1000 );
      }
    }
    catch(Throwable throwable) {
      
      logger.error("Critical error in service thread.", throwable);
    }
    
    logger.debug("Service thread terminating.");
  }
  
  public synchronized boolean getTerminated() {
    
    return terminated;
  }
  
  public synchronized void terminate() {
    
    this.terminated = true;
  }
}
