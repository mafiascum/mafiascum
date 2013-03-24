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
  
  protected final long MILLISECONDS_PER_USER_TABLE_UPDATE = (1L * 60L * 1000L);
  
  public SiteChatServerServiceThread(SiteChatServer siteChatServer) {
    
    this.siteChatServer = siteChatServer;
    this.lastRefreshUserCacheDatetime = new Date();
  }
  
  public void run() {
   
    try {
      while(!getTerminated()) {
        
        Date nowDatetime = new Date();
        
        //refresh conversation list members.
        Iterator<Map.Entry<Integer, SiteChatConversationWithUserList>> it = siteChatServer.siteChatConversationWithMemberListMap.entrySet().iterator();
        Map<Integer, Date> userActivity = siteChatServer.userIdToLastActivityDatetime;
        int currentUser = 0;
        while(it.hasNext()){
          Set<Integer> userIdSet = it.next().getValue().getUserIdSet();
          Iterator<Integer> currentUsers = userIdSet.iterator();
          while(currentUsers.hasNext()){
            currentUser = currentUsers.next();
            if (nowDatetime.getTime() - userActivity.get(currentUser).getTime() >5000){
              userIdSet.remove(currentUser);
            }
          }
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
