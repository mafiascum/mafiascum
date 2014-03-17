package net.mafiascum.web.sitechat.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.mafiascum.json.DateUnixTimestampSerializer;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatBarebonesConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundPacketType;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundConnectPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundHeartbeatPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLeaveConversationPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLogInPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundLookupUserPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundPacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSendMessagePacketOperator;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundSetPasswordPacketOperator;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundConnectPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundIncorrectPasswordPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPasswordRequiredPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundUserJoinPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundUserListPacket;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SiteChatServer extends Server implements SignalHandler {
  
  protected boolean _verbose;

  protected ServerConnector connector;
  protected WebSocketHandler webSocketHandler;
  protected ResourceHandler resourceHandler;

  protected Provider provider = null;
  
  protected volatile ConcurrentLinkedQueue<SiteChatWebSocket> descriptors = new ConcurrentLinkedQueue<SiteChatWebSocket>();
  protected volatile Map<Integer, SiteChatConversationWithUserList> siteChatConversationWithMemberListMap = new HashMap<Integer, SiteChatConversationWithUserList>();
  protected volatile Map<String, List<SiteChatConversationMessage>> siteChatPrivateConversationMessageHistoryMap = new HashMap<String, List<SiteChatConversationMessage>>();
  
  protected volatile Map<Integer, Date> userIdToLastNetworkActivityDatetime = new HashMap<Integer, Date>();
  protected volatile Map<Integer, SiteChatUser> siteChatUserMap = new HashMap<Integer, SiteChatUser>();
  protected volatile Map<Integer, List<SiteChatWebSocket>> userIdToSiteChatWebSocketsMap = new HashMap<Integer, List<SiteChatWebSocket>>();
  protected volatile List<SiteChatConversationMessage> siteChatConversationMessagesToSave = new LinkedList<SiteChatConversationMessage>();
  protected volatile Set<Integer> bannedUserIdSet = new HashSet<Integer>();
  protected SiteChatServerServiceThread serviceThread;
  
  protected final long MILLISECONDS_UNTIL_USER_IS_INACTIVE = (1000) * (60) * (5);
  protected final int MESSAGE_BATCH_SIZE = 25;
  protected volatile int topSiteChatConversationMessageId;
  
  protected static Logger logger = Logger.getLogger(SiteChatServer.class.getName());
  public static final Logger lagLogger = Logger.getLogger("LagMonitor");
  
  protected static final Map<SiteChatInboundPacketType, SiteChatInboundPacketOperator> siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap = new HashMap<SiteChatInboundPacketType, SiteChatInboundPacketOperator>();
  static {
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.connect, new SiteChatInboundConnectPacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.login, new SiteChatInboundLogInPacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.sendMessage, new SiteChatInboundSendMessagePacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.leaveConversation, new SiteChatInboundLeaveConversationPacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.lookupUser, new SiteChatInboundLookupUserPacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.heartbeat, new SiteChatInboundHeartbeatPacketOperator());
    siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.put(SiteChatInboundPacketType.setPassword, new SiteChatInboundSetPasswordPacketOperator());
  }
  
  public static class SiteChatInboundPacketSkeleton {
    
    public String command;
  }
  
  public SiteChatServer(int port, Provider provider) throws Exception {

    this.provider = provider;
    Connection connection = null;
    
    try {
      connection = provider.getConnection();
      
      //Add handler for interruption signal.
      Signal.handle(new Signal("INT"), this);
      Signal.handle(new Signal("TERM"), this);
      
      //Service thread.
      serviceThread = new SiteChatServerServiceThread(this);
      serviceThread.setName("SERVICE-THREAD");
      serviceThread.start();
      
      connector = new ServerConnector(this);
      connector.setPort(port);
      
      addConnector(connector);
      webSocketHandler = new WebSocketHandler()
      {
        /***
        public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
        {
          WebSocket webSocket = null;
          if(protocol != null && protocol.equals("site-chat"))
          {
            webSocket = new SiteChatWebSocket();
            descriptors.add((SiteChatWebSocket)webSocket);
          }
          
          return webSocket;
        }
        ***/

        public void configure(WebSocketServletFactory webSocketServletFactory) {
          
          webSocketServletFactory.setCreator(new WebSocketCreator() {

            public Object createWebSocket(UpgradeRequest upgradeRequest, UpgradeResponse upgradeResponse) {
              
              SiteChatWebSocket siteChatWebSocket = new SiteChatWebSocket();
              descriptors.add(siteChatWebSocket);
              
              upgradeResponse.setAcceptedSubProtocol("site-chat");
              
              return siteChatWebSocket;
            }
          });
        }
      };
  
      setHandler(webSocketHandler);
  
      logger.info("Loading Site Chat Users...");
      this.siteChatUserMap = SiteChatUtil.loadSiteChatUserMap(connection);
      
      logger.info("Loading Site Chat Conversations...");
      List<SiteChatConversation> siteChatConversationList = SiteChatUtil.getSiteChatConversations(connection);
      for(SiteChatConversation siteChatConversation : siteChatConversationList) {
        
        SiteChatConversationWithUserList siteChatConversationWithUserList = new SiteChatConversationWithUserList();
        siteChatConversationWithUserList.setSiteChatConversation(siteChatConversation);
        
        siteChatConversationWithMemberListMap.put(siteChatConversation.getId(), siteChatConversationWithUserList);
      }
      
      logger.info("Loading Top Site Chat Conversation Message ID...");
      topSiteChatConversationMessageId = SiteChatUtil.getTopSiteChatConversationMessageId(connection);
      
      logger.info("Loading Banned User ID Set...");
      refreshBanUserList();
      
      //resourceHandler=new ResourceHandler();
      //resourceHandler.setDirectoriesListed(false);
      //resourceHandler.setResourceBase("-");
      //webSocketHandler.setHandler(resourceHandler);
      
      connection.commit();
      connection.close();
      connection = null;
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public boolean isUseBanned(int userId) throws Exception {
    
    return bannedUserIdSet.contains(userId);
  }
  
  public void refreshBanUserList() throws Exception {

    Connection connection = null;
    try {
      
      Set<Integer> newBannedUserIdSet;
      connection = provider.getConnection();
      
      newBannedUserIdSet = SiteChatUtil.getBannedUserIdSet(connection);
      
      connection.commit();
      connection.close();
      connection = null;
      
      //Disconnect all newly banned users.
      for(Integer userId : newBannedUserIdSet) {
        
        if(!bannedUserIdSet.contains(userId)) {
          
          removeUser(userId);
        }
      }
      
      bannedUserIdSet.clear();
      bannedUserIdSet = newBannedUserIdSet;
      logger.debug("Banned User ID Set: " + bannedUserIdSet.size());
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public void printContainerSizes() {
    
    BigDecimal freeMemory = new BigDecimal(Runtime.getRuntime().freeMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal totalMemory = new BigDecimal(Runtime.getRuntime().totalMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal maxMemory = new BigDecimal(Runtime.getRuntime().maxMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    
    synchronized(descriptors) {
      logger.debug("Descriptors: " + descriptors.size());
    }
    synchronized(siteChatConversationWithMemberListMap) {
      logger.debug("Convos With Member list Map: " + siteChatConversationWithMemberListMap.size());
    }
    synchronized(siteChatPrivateConversationMessageHistoryMap) {
      logger.debug("Private Convo Map: " + siteChatPrivateConversationMessageHistoryMap.size());
    }
    synchronized(userIdToLastNetworkActivityDatetime) {
      logger.debug("User ID To Activity Map: " + userIdToLastNetworkActivityDatetime.size());
    }
    synchronized(siteChatUserMap) {
      logger.debug("User Map: " + siteChatUserMap.size());
    }
    synchronized(userIdToSiteChatWebSocketsMap) {
      logger.debug("User ID To Web Socket Map: " + userIdToSiteChatWebSocketsMap.size());
    }
    synchronized(siteChatConversationMessagesToSave) {
      logger.debug("Messages To Save: " + siteChatConversationMessagesToSave.size());
    }
    logger.debug("Total: " + totalMemory + "MB, Free: " + freeMemory + "MB, Max: " + maxMemory + "MB.");
  }
  
  public void associateWebSocketWithUser(int userId, SiteChatWebSocket siteChatWebSocket) {
    
    synchronized(siteChatConversationWithMemberListMap) {
      
      List<SiteChatWebSocket> siteChatWebSockets = userIdToSiteChatWebSocketsMap.get(userId);
      if(siteChatWebSockets == null) {
      
        siteChatWebSockets = new LinkedList<SiteChatWebSocket>();
        userIdToSiteChatWebSocketsMap.put(userId, siteChatWebSockets);
      }
    
      synchronized(siteChatWebSockets) {
        siteChatWebSockets.add(siteChatWebSocket);
      }
    }
  }
  
  public SiteChatConversationWithUserList getSiteChatConversationWithUserList(String siteChatConversationName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    
    SiteChatConversation siteChatConversation = null;
    for(Integer siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      siteChatConversation = siteChatConversationWithUserList.getSiteChatConversation();
      
      if(siteChatConversation.getName().equalsIgnoreCase(siteChatConversationName)) {
            
        return siteChatConversationWithUserList;
      }
    }
    
    return null;
  }
  
  public SiteChatConversationWithUserList getSiteChatConversationWithUserList(int siteChatConversationId) {
    
    return siteChatConversationWithMemberListMap.get(siteChatConversationId);
  }
  
  protected void addSiteChatUser(SiteChatUser siteChatUser) {
    
    synchronized(this.siteChatUserMap) {
      siteChatUserMap.put(siteChatUser.getId(), siteChatUser);
    }
  }
  
  public Map<Integer, SiteChatUser> getSiteChatUserMap(Collection<Integer> userIdCollection) {

    Map<Integer, SiteChatUser> siteChatUserMap = new HashMap<Integer, SiteChatUser>();
    synchronized(this.siteChatUserMap) {
      
      for(Integer userId : userIdCollection) {
        
        SiteChatUser siteChatUser = this.siteChatUserMap.get(userId);
        
        if(siteChatUser != null) {
          
          siteChatUserMap.put(siteChatUser.getId(), siteChatUser);
        }
        else{
          
          logger.error("getSiteChatUserMap() : Could not find user #" + userId + ".");
        }
      }
    }
    
    return siteChatUserMap;
  }
  
  public SiteChatConversationWithUserList createSiteChatConversation(String siteChatConversationName, int userId) throws Exception {
    
    Connection connection = null;
    try {
      
      SiteChatConversation siteChatConversation = new SiteChatConversation();
      siteChatConversation.setCreatedByUserId(userId);
      siteChatConversation.setCreatedDatetime(new Date());
      siteChatConversation.setName(siteChatConversationName);
      
      connection = provider.getConnection();
      
      SiteChatUtil.putSiteChatConversation(connection, siteChatConversation);
      
      connection.commit();
      connection.close();
      connection = null;
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = new SiteChatConversationWithUserList();
      siteChatConversationWithUserList.setSiteChatConversation(siteChatConversation);
      siteChatConversationWithUserList.getUserIdSet().add(userId);
      
      siteChatConversationWithMemberListMap.put(siteChatConversation.getId(), siteChatConversationWithUserList);
      
      return siteChatConversationWithUserList;
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public SiteChatUser getSiteChatUser(int userId) {
    
    synchronized(this.siteChatUserMap) {
      return siteChatUserMap.get(userId);
    }
  }
  
  public SiteChatConversationMessage recordSiteChatConversationMessage(int userId, Integer siteChatConversationId, Integer recipientUserId, String message) throws Exception {
    
    SiteChatConversationMessage siteChatConversationMessage = new SiteChatConversationMessage();
    siteChatConversationMessage.setMessage(message);
    siteChatConversationMessage.setCreatedDatetime(new Date());
    siteChatConversationMessage.setSiteChatConversationId(siteChatConversationId);
    siteChatConversationMessage.setRecipientUserId(recipientUserId);
    siteChatConversationMessage.setUserId(userId);
    siteChatConversationMessage.setId(++topSiteChatConversationMessageId);
    
    synchronized(siteChatConversationMessagesToSave) {
      siteChatConversationMessagesToSave.add(siteChatConversationMessage);
    
      if(siteChatConversationMessagesToSave.size() >= MESSAGE_BATCH_SIZE) {
      
        saveSiteChatConversationMessages();
      }
    }
    
    //Save to local conversation cache. Currently only being done for conversations.
    if(siteChatConversationMessage.getSiteChatConversationId() != null) {
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      
      synchronized(siteChatConversationWithUserList) {
        if(siteChatConversationWithUserList.getSiteChatConversationMessages().size() >= SiteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
          siteChatConversationWithUserList.getSiteChatConversationMessages().remove(0);
        }
        siteChatConversationWithUserList.getSiteChatConversationMessages().add(siteChatConversationMessage);
      }
    }
    else {
      
      String privateConversationMapKey = SiteChatUtil.getPrivateMessageHistoryKey(userId, recipientUserId);
      List<SiteChatConversationMessage> siteChatConversationMessages = null;
      
      synchronized(siteChatPrivateConversationMessageHistoryMap) {
        siteChatConversationMessages = siteChatPrivateConversationMessageHistoryMap.get(privateConversationMapKey);
      }
      
      if(siteChatConversationMessages == null) {

        siteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
        synchronized(siteChatPrivateConversationMessageHistoryMap) {
          siteChatPrivateConversationMessageHistoryMap.put(privateConversationMapKey, siteChatConversationMessages);
        }
      }
      
      synchronized(siteChatConversationMessages) {
        
        if(siteChatConversationMessages.size() >= SiteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
          siteChatConversationMessages.remove(0);
        }
        siteChatConversationMessages.add(siteChatConversationMessage);
      }
    }
    
    return siteChatConversationMessage;
  }
  
  public void updateConversationPassword(int userId, int conversationId, String password) throws Exception {
    
    Connection connection = null;
    
    try {
      SiteChatConversationWithUserList conversationWithUserList = getSiteChatConversationWithUserList(conversationId);
      
      if(conversationWithUserList == null) {
        
        throw new SiteChatException("Conversation not found.");
      }
      
      synchronized(conversationWithUserList) {
        
        SiteChatConversation siteChatConversation = conversationWithUserList.getSiteChatConversation();
        
        synchronized(siteChatConversation) {
        
          if(siteChatConversation.getCreatedByUserId() != userId) {
          
            throw new SiteChatException("User is not conversation creator.");
          }
          
          siteChatConversation.setPassword(StringUtil.isNullOrEmptyTrimmedString(password) ? null : StringUtil.getSHA1(password));
          
          connection = provider.getConnection();
          
          SiteChatUtil.putSiteChatConversation(connection, siteChatConversation);
          
          connection.commit();
          connection.close();
          connection = null;
        }
      }
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public List<SiteChatConversationMessage> getMessageHistory(SiteChatConversationType siteChatConversationType, int lastReceivedSiteChatConversationId, int userId, int uniqueIdentifier) {
    
    List<SiteChatConversationMessage> messageHistoryToSendToUser = new LinkedList<SiteChatConversationMessage>();
    List<SiteChatConversationMessage> siteChatConversationMessages = null;
    synchronized(siteChatConversationWithMemberListMap) {
      
      synchronized(siteChatPrivateConversationMessageHistoryMap) {
        if(siteChatConversationType.equals(SiteChatConversationType.Conversation)) {
          
          SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(uniqueIdentifier);
          if(siteChatConversationWithUserList == null) {
            
            return messageHistoryToSendToUser;
          }
          
          siteChatConversationMessages = siteChatConversationWithUserList.getSiteChatConversationMessages();
        }
        else {
          
          String messageHistoryKey = SiteChatUtil.getPrivateMessageHistoryKey(userId, uniqueIdentifier);
          siteChatConversationMessages = siteChatPrivateConversationMessageHistoryMap.get(messageHistoryKey);
          
          if(siteChatConversationMessages == null) {
            
            return messageHistoryToSendToUser;
          }
          
          synchronized(siteChatConversationMessages) {
            if(siteChatConversationMessages.size() == 0) {
            
              return messageHistoryToSendToUser;
            }
          }
        }
        
        
        synchronized(siteChatConversationMessages) {
          //Go through and get the message history.
          ListIterator<SiteChatConversationMessage> listIterator = siteChatConversationMessages.listIterator(siteChatConversationMessages.size());
        
          while(listIterator.hasPrevious()) {
          
            SiteChatConversationMessage siteChatConversationMessage = listIterator.previous();
            if(siteChatConversationMessage.getId() > lastReceivedSiteChatConversationId) {
              
              siteChatConversationMessage = siteChatConversationMessage.clone();
              siteChatConversationMessage.setMessage(StringUtil.escapeHTMLCharacters(siteChatConversationMessage.getMessage()));
              logger.trace("Adding missed message: " + siteChatConversationMessage.getId());
              messageHistoryToSendToUser.add(siteChatConversationMessage);
            }
          }
        }
      }
    }
    
    return messageHistoryToSendToUser;
  }
  
  public void refreshUserCache() throws Exception {
    
    Connection connection = null;
    try {
      connection = provider.getConnection();
      
      lagLogger.debug("Refreshing User Cache.");
      Map<Integer, SiteChatUser> siteChatUserMap = SiteChatUtil.loadSiteChatUserMap(connection);
      lagLogger.debug("User Cache Refreshed.");
      
      connection.commit();
      connection.close();
      connection = null;
      
      synchronized(this.siteChatUserMap) {

        SiteChatUser siteChatUser;
        for(int userId : this.siteChatUserMap.keySet()) {
          
          if( (siteChatUser = siteChatUserMap.get(userId)) != null ) {
            siteChatUser.setLastActivityDatetime(this.siteChatUserMap.get(userId).getLastActivityDatetime());
          }
        }
        this.siteChatUserMap = siteChatUserMap;
      }
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public void sendUserListToAllWebSockets() throws Exception {
    
    lagLogger.debug("Sending User List To All Web Sockets. START.");
    List<SiteChatUser> siteChatUserList = new LinkedList<SiteChatUser>();
    List<SiteChatBarebonesConversation> siteChatBarebonesConversations = new LinkedList<SiteChatBarebonesConversation>();
    
    synchronized(userIdToLastNetworkActivityDatetime) {
      for(int userId : userIdToLastNetworkActivityDatetime.keySet()) {
      
        synchronized(siteChatUserMap) {
          siteChatUserList.add(new SiteChatUser(siteChatUserMap.get(userId)));
        }
      }
    }
    
    //logger.debug("Generating user list packet for " + siteChatUserList.size() + " users.");

    //Generate message for each user.
    for(SiteChatUser siteChatUser : siteChatUserList) {
      
      int userId = siteChatUser.getId();
      //logger.debug("Considering Sending To User ID: " + userId);
      List<SiteChatWebSocket> siteChatWebSockets = userIdToSiteChatWebSocketsMap.get(userId);
      siteChatBarebonesConversations.clear();
      
      if(siteChatWebSockets == null) {
        
        continue;
      }
      
      synchronized(siteChatWebSockets) {
        if(siteChatWebSockets.isEmpty()) {
          
          continue;
        }
      }

      //logger.debug("Preparing user list packet for user #" + userId);

      for(int siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {
          
        SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
          
        synchronized(siteChatConversationWithUserList) {
          
          if(siteChatConversationWithUserList.getUserIdSet().isEmpty()) {
            
            continue;
          }
          if(siteChatConversationWithUserList.getSiteChatConversation().getPassword() != null) {
            
            if(!siteChatConversationWithUserList.getUserIdSet().contains(userId)) {
              
              continue;
            }
          }
            
          //Copy all but the message array over. We do not need it for this.
          //logger.debug("Creating barebones conversation.");
          SiteChatBarebonesConversation barebonesConversation = new SiteChatBarebonesConversation();
          barebonesConversation.setId(siteChatConversationWithUserList.getSiteChatConversation().getId());
          barebonesConversation.setName(siteChatConversationWithUserList.getSiteChatConversation().getName());
          barebonesConversation.setUserIdSet(siteChatConversationWithUserList.getUserIdSet());
          barebonesConversation.setCreatedByUserId(siteChatConversationWithUserList.getSiteChatConversation().getCreatedByUserId());
          //logger.debug("Barebones conversation created.");
          siteChatBarebonesConversations.add(barebonesConversation);
        }
      }
      
      //logger.debug("Creating outbound user list packet. Users: " + siteChatUserList.size() + ", Conversations: " + siteChatBarebonesConversations.size());
      SiteChatOutboundUserListPacket siteChatOutboundUserListPacket = new SiteChatOutboundUserListPacket();
      siteChatOutboundUserListPacket.setSiteChatUsers(siteChatUserList);
      siteChatOutboundUserListPacket.setSiteChatConversations(siteChatBarebonesConversations);
      siteChatOutboundUserListPacket.setPacketSentDatetime(new Date());
      
      synchronized(siteChatWebSockets) {
        for(SiteChatWebSocket siteChatWebSocket : siteChatWebSockets) {
          
          synchronized(siteChatWebSocket) {
            //logger.debug("Sending Message.");
            siteChatWebSocket.sendOutboundPacket(siteChatOutboundUserListPacket);
            //logger.debug("Message sent.");
          }
        }
      }
    }
    
  }
  
  public void removeIdleUsers(Date contextDatetime) throws Exception {

    lagLogger.debug("Remove Idle Users. START.");
    Set<Integer> inactiveUserIdSet = new HashSet<Integer>();
    long contextDatetimeMilliseconds = contextDatetime.getTime();
    
    //Quickly grab a set of users that we will remove.
    synchronized(userIdToLastNetworkActivityDatetime) {
      
      for(Integer userId : userIdToLastNetworkActivityDatetime.keySet()) {
        
        if(isUserActive(userId, contextDatetimeMilliseconds)) {
          inactiveUserIdSet.add(userId);
        }
      }
    }
    
    logger.debug("Removing Inactive Users: " + inactiveUserIdSet);
    for(Integer userId : inactiveUserIdSet) {
      
      removeUser(userId);
    }
    lagLogger.debug("Remove Idle Users. FINISHED.");
  }
  
  public boolean isUserActive(int userId, long contextDatetimeMilliseconds) {
    
    synchronized(userIdToLastNetworkActivityDatetime) {
      
      Date lastNetworkActivityDatetime;
      if( (lastNetworkActivityDatetime = userIdToLastNetworkActivityDatetime.get(userId)) == null)
        return false;
      
      return contextDatetimeMilliseconds - lastNetworkActivityDatetime.getTime() >= MILLISECONDS_UNTIL_USER_IS_INACTIVE;
    }
  }
  
  public void removeUser(int userId) {
    
    userIdToLastNetworkActivityDatetime.remove(userId);
    
    Iterator<SiteChatWebSocket> siteChatWebSocketIter = descriptors.iterator();
    while(siteChatWebSocketIter.hasNext()) {
      SiteChatWebSocket siteChatWebSocket = siteChatWebSocketIter.next();
        
      if(siteChatWebSocket.getSiteChatUser() != null && siteChatWebSocket.getSiteChatUser().getId() == userId) {
        
        try {
          
          siteChatWebSocket.getConnection().close();
          siteChatWebSocketIter.remove();
          
          synchronized(userIdToSiteChatWebSocketsMap) {
            List<SiteChatWebSocket> siteChatWebSockets = userIdToSiteChatWebSocketsMap.get(userId);
            if(siteChatWebSockets != null) {
            
              synchronized(siteChatWebSockets) {
              
                siteChatWebSockets.remove(siteChatWebSocket);
                if(siteChatWebSockets.size() == 0) {
                  userIdToSiteChatWebSocketsMap.remove(userId);
                }
              }
            }
          }
          
          break;
        }
        catch(Throwable throwable) {
          
          logger.error("Exception thrown while trying to disconnect web socket in removeUser() : " + throwable);
        }
      }
    }
    
    for(Integer siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {
        
      SiteChatConversationWithUserList SiteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      SiteChatConversationWithUserList.getUserIdSet().remove(userId);
    }
  }
  
  public void saveSiteChatConversationMessages() throws Exception {
    
    Connection connection = null;
    
    try {
      synchronized(siteChatConversationMessagesToSave) {
      
        if(siteChatConversationMessagesToSave.size() > 0) {
          
          connection = provider.getConnection();
          
          SiteChatUtil.putNewSiteChatConversationMessages(connection, siteChatConversationMessagesToSave);
          
          connection.commit();
          connection.close();
          connection = null;
    
          siteChatConversationMessagesToSave.clear();
        }
      }
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }

  public void processInboundDataPacket(SiteChatWebSocket webSocket, String data) {
    
    SiteChatInboundPacketSkeleton siteChatInboundPacketSkeleton = null;
    SiteChatInboundPacketType siteChatInboundPacketType = null;
    SiteChatInboundPacketOperator siteChatInboundPacketOperator = null;
    try {
      
      siteChatInboundPacketSkeleton = new Gson().fromJson(data, SiteChatInboundPacketSkeleton.class);
      siteChatInboundPacketType = SiteChatInboundPacketType.getEnumByStandardName(siteChatInboundPacketSkeleton.command);
      
      if(siteChatInboundPacketType == null) {
        
        throw new Exception("Could not find processor for command `" + siteChatInboundPacketSkeleton.command + "`");
      }
      
      logger.debug("Packet Type `" + siteChatInboundPacketType.getStandardName() + "`");
      
      siteChatInboundPacketOperator = siteChatInboundPacketTypeToSiteChatInboundPacketOperatorMap.get(siteChatInboundPacketType);
      
      siteChatInboundPacketOperator.process(this, webSocket, data);
    }
    catch(Throwable throwable) {

      logger.error("Error processing inbound packet. Full data: " + data, throwable);
    }
  }
  
  public void sendOutboundPacketToUsers(Set<Integer> userIdSet, SiteChatOutboundPacket siteChatOutboundPacket, Integer excludeUserId) throws IOException {
    
    synchronized(descriptors) {
      for(SiteChatWebSocket siteChatWebSocket : descriptors) {
        
        synchronized(siteChatWebSocket) {
        
          if(siteChatWebSocket.getSiteChatUser() != null) {
            
            for(Integer userId : userIdSet) {
              
              if(excludeUserId != null && excludeUserId == userId) {
                
                continue;
              }
              
              if(siteChatWebSocket.getSiteChatUser().getId() == userId) {
                
                try {
                  siteChatWebSocket.sendOutboundPacket(siteChatOutboundPacket);
                }
                catch(IOException ioException) {
                  
                  logger.error("Could not send outbound packet: ", ioException);
                }
              }
            }
          }
        }
      }
    }
  }
  
  public void attemptJoinConversation(SiteChatWebSocket siteChatWebSocket, int siteChatUserId, int siteChatConversationId, boolean notifyUser, boolean notifyConversationMembers, String password, String authCode) throws IOException {
    
    synchronized(this.siteChatUserMap) {
      SiteChatUser siteChatUser = siteChatUserMap.get(siteChatUserId);
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      SiteChatConversation siteChatConversation = siteChatConversationWithUserList.getSiteChatConversation();
      Map<Integer, SiteChatUser> siteChatUserMap = getSiteChatUserMap(siteChatConversationWithUserList.getUserIdSet());
      
      if(siteChatConversation.getPassword() != null) {
        
        if(password == null) {
          
          if(authCode == null || !authCode.equals(SiteChatUtil.generateConversationAuthCode(siteChatUserId, siteChatConversationId, siteChatConversation.getPassword()))) {
            SiteChatOutboundPasswordRequiredPacket siteChatOutboundPasswordRequiredPacket = new SiteChatOutboundPasswordRequiredPacket();
            siteChatOutboundPasswordRequiredPacket.setConversationName(siteChatConversation.getName());
            siteChatWebSocket.sendOutboundPacket(siteChatOutboundPasswordRequiredPacket);
          
            logger.debug("Password required!");
            return;
          }
        }
        else if(!StringUtil.getSHA1(password).equals(siteChatConversation.getPassword())) {
          
          SiteChatOutboundIncorrectPasswordPacket siteChatOutboundIncorrectPasswordPacket = new SiteChatOutboundIncorrectPasswordPacket();
          siteChatOutboundIncorrectPasswordPacket.setConversationName(siteChatConversation.getName());
          siteChatWebSocket.sendOutboundPacket(siteChatOutboundIncorrectPasswordPacket);
          
          logger.debug("Password did not match. Conversation `" + siteChatConversation.getName() + "`.");
          return;
        }
      }
      
      //Add user to user list.
      logger.debug("Adding user #" + siteChatUser.getId() + " to chat #" + siteChatConversationWithUserList.getSiteChatConversation().getId() + ".");
      siteChatConversationWithUserList.getUserIdSet().add(siteChatUser.getId());
      
      //Generate response packet to user.
      if(notifyUser) {
        SiteChatOutboundConnectPacket siteChatOutboundConnectPacket = new SiteChatOutboundConnectPacket();
        siteChatOutboundConnectPacket.setWasSuccessful(true);
        siteChatOutboundConnectPacket.setSiteChatConversationId(siteChatConversationWithUserList.getSiteChatConversation().getId());
        siteChatOutboundConnectPacket.setTitleText(StringUtil.escapeHTMLCharacters(siteChatConversation.getName()));
        siteChatOutboundConnectPacket.setCreatedByUserId(siteChatConversationWithUserList.getSiteChatConversation().getCreatedByUserId());
        
        if(siteChatConversation.getPassword() != null) {
          
          siteChatOutboundConnectPacket.setAuthCode(SiteChatUtil.generateConversationAuthCode(siteChatUser.getId(), siteChatConversation.getId(), siteChatConversation.getPassword()));
          
        }
        
        Set<SiteChatUser> siteChatUserSet = new HashSet<SiteChatUser>();
      
        for(Integer userId : siteChatUserMap.keySet()) {
        
          siteChatUserSet.add(siteChatUserMap.get(userId));
        }
        
        siteChatOutboundConnectPacket.setUsers(siteChatUserSet);
        
        for(SiteChatWebSocket tempSiteChatWebSocket : descriptors) {
          
          if(tempSiteChatWebSocket.getSiteChatUser() != null && tempSiteChatWebSocket.getSiteChatUser().getId() == siteChatUserId) {
            
            try {
              tempSiteChatWebSocket.sendOutboundPacket(siteChatOutboundConnectPacket);
            }
            catch(IOException ioException) {
              logger.error("Error attempting to send outbound packet. User ID: " + siteChatUserId, ioException);
            }
          }
        }
      }
    
      //Notify all users in the chat room that this user has joined.
      if(notifyConversationMembers) {
        SiteChatOutboundUserJoinPacket siteChatOutboundUserJoinPacket = new SiteChatOutboundUserJoinPacket();
        siteChatOutboundUserJoinPacket.setSiteChatConversationId(siteChatConversationWithUserList.getSiteChatConversation().getId());
        siteChatOutboundUserJoinPacket.setSiteChatUser(siteChatUser);
      
        sendOutboundPacketToUsers(siteChatUserMap.keySet(), siteChatOutboundUserJoinPacket, siteChatUser.getId());
      }
    }
  }
  
  public void cleanup() throws Exception {
    
    logger.info("Saving queued conversation messages. Number in buffer: " + siteChatConversationMessagesToSave.size());
    
    saveSiteChatConversationMessages();
  }
  
  public boolean authenticateUserLogin(int userId, String sessionId) throws Exception {
    
    Connection connection = null;
    
    try {
      connection = provider.getConnection();
      
      boolean result = SiteChatUtil.authenticateUserLogin(connection, userId, sessionId);
      
      connection.commit();
      connection.close();
      connection = null;
    
      return result;
    }
    finally {
      
      QueryUtil.closeNoThrow(connection);
    }
  }
  
  public boolean isVerbose()
  {
    return _verbose;
  }

  public void setVerbose(boolean verbose)
  {
    _verbose = verbose;
  }

  public void setResourceBase(String dir)
  {
    resourceHandler.setResourceBase(dir);
  }

  public String getResourceBase()
  {
    return resourceHandler.getResourceBase();
  }

  private static void usage()
  {
    logger.info("java -cp CLASSPATH " + SiteChatServer.class + " [ OPTIONS ]");
    logger.info("  -p|--port PORT  (default 4241)");
    logger.info("  -v|--verbose ");
    logger.info("  -d|--docroot file (default '.')");
    System.exit(1);
  }
  
  public static void main(String... args)
  {
    try {
      
      int port=4241;
      boolean verbose=false;
      String docRoot=".";
      
      for (int i=0;i<args.length;i++) {
        
        String a=args[i];
        if ("-p".equals(a)||"--port".equals(a))
          port=Integer.parseInt(args[++i]);
        else if ("-v".equals(a)||"--verbose".equals(a))
          verbose=true;
        else if ("-d".equals(a)||"--docroot".equals(a))
          docRoot=args[++i];
        else if (a.startsWith("-"))
          usage();
      }
      
      Provider provider = new Provider();
      provider.setDocRoot(docRoot);
      logger.info("Loading Configuration.");
      provider.loadConfiguration(docRoot + "/" + "ServerConfig.txt");
      logger.info("Setting Up Connection Pool.");
      provider.setupConnectionPool();
      
      logger.info("Setting Up Site Chat Server.");
      SiteChatServer server = new SiteChatServer(port, provider);
      server.setVerbose(verbose);
      
      server.setVerbose(true);
      
      //server.setResourceBase("-");
      logger.info("Starting Site Chat Server.");
      server.start();
      server.join();
      
      logger.info("Server has been stopped.\n");
      
      server.cleanup();
      
      System.exit(1);
    }
    catch (Exception e) {

      logger.error("Severe Exception: ", e);
    }
  }
  
  public class SiteChatWebSocket implements WebSocketListener
  {
    protected WebSocketSession connection;
    protected SiteChatUser siteChatUser;
    
    public SiteChatUser getSiteChatUser() {
      
      return siteChatUser;
    }
    
    public void setSiteChatUser(SiteChatUser siteChatUser) {
    
      this.siteChatUser = siteChatUser;
    }
    
    public WebSocketSession getConnection() {
      return connection;
    }
    
    public void sendOutboundPacket(SiteChatOutboundPacket siteChatOutboundPacket) throws IOException {
      
      synchronized(this.getConnection()) {
        if(this.getConnection().isOpen()) {
          
          String siteChatOutboundPacketJson = new GsonBuilder().registerTypeAdapter(Date.class, new DateUnixTimestampSerializer()).create().toJson(siteChatOutboundPacket);
          
          this.getConnection().getRemote().sendStringByFuture(siteChatOutboundPacketJson);
        }
      }
    }

    public void onWebSocketBinary(byte[] data, int arg1, int arg2) {
      
      if (_verbose)
        logger.trace(this.getClass().getSimpleName() + "#onWebSocketBinary   arg1: " + arg1 + ", arg2: " + arg2 + ", Data: " + (data == null ? "<NULL>" : String.valueOf(data.length)) );
    }

    public void onWebSocketClose(int arg0, String data) {

      if (_verbose)
        logger.trace(this.getClass().getSimpleName() + "#onWebSocketClose   arg0: " + arg0 + ", Data: " + data);
      
      descriptors.remove(this);
      
      if(this.getSiteChatUser() != null) {
        
        int userId = this.getSiteChatUser().getId();
        
        synchronized(userIdToSiteChatWebSocketsMap) {
          
          List<SiteChatWebSocket> siteChatWebSockets = userIdToSiteChatWebSocketsMap.get(userId);
          if(siteChatWebSockets != null) {
            
            synchronized(siteChatWebSockets) {
              siteChatWebSockets.remove(this);
          
              if(siteChatWebSockets.size() == 0) {
                userIdToSiteChatWebSocketsMap.remove(userId);
              }
            }
          }
        }
      }
    }

    public void onWebSocketConnect(Session session) {

      if (_verbose)
        logger.trace(this.getClass().getSimpleName() + "#onWebSocketConnect   " + session.getRemoteAddress().getHostString());
      
      this.connection = (WebSocketSession)session;
    }
    public void onWebSocketError(Throwable throwable) {

      if (_verbose)
        logger.trace(this.getClass().getSimpleName() + "#onWebSocketError   " + throwable);
    }

    public void onWebSocketText(String data) {
      
      {
        if (_verbose)
          logger.trace(this.getClass().getSimpleName() + "#onWebSocketText   " + data);
        
        try {
          
          processInboundDataPacket(this, data);
        }
        catch(Throwable throwable) {

          logger.error("Error in onWebSocketText()", throwable);
          return;
        }
      }
    }
  }

  public void handle(Signal signal) {
    
    try {
      
      logger.info("Signal received. Shutting down.");
      
      stop();
      
      logger.info("Attempting to shut down Web Socket Server...");
    }
    catch(Exception exception) {
      
      logger.error("Could not shut down Web Socket Server:", exception);
    }
    
  }
  public void updateUserActivity(int userId) {
    userIdToLastNetworkActivityDatetime.put(userId, new Date());
  }
}