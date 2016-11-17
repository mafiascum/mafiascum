package net.mafiascum.web.sitechat.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mafiascum.arguments.CommandLineArguments;
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
import net.mafiascum.web.sitechat.server.debug.DebugManager;
import net.mafiascum.web.sitechat.server.ignore.IgnoreManager;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundPacketSkeleton;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundPacketType;
import net.mafiascum.web.sitechat.server.inboundpacket.operator.SiteChatInboundPacketOperator;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundConnectPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundIncorrectPasswordPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundPasswordRequiredPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundUserJoinPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundUserListPacket;
import net.mafiascum.web.sitechat.server.user.UserData;
import net.mafiascum.web.sitechat.server.user.UserManager;
import net.mafiascum.web.sitechat.server.user.UserPacket;

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
  
  protected ServerConnector connector;
  protected WebSocketHandler webSocketHandler;
  protected ResourceHandler resourceHandler;

  protected Provider provider = null;

  protected volatile ConcurrentLinkedQueue<SiteChatWebSocket> descriptors = new ConcurrentLinkedQueue<SiteChatWebSocket>();
  protected volatile Map<Integer, SiteChatConversationWithUserList> siteChatConversationWithMemberListMap = new HashMap<Integer, SiteChatConversationWithUserList>();
  protected volatile Map<String, List<SiteChatConversationMessage>> siteChatPrivateConversationMessageHistoryMap = new HashMap<String, List<SiteChatConversationMessage>>();
  
  protected volatile List<SiteChatConversationMessage> siteChatConversationMessagesToSave = new LinkedList<SiteChatConversationMessage>();
  
  protected SiteChatServerServiceThread serviceThread;
  protected BanManager banManager;
  protected DebugManager debugManager;
  protected UserManager userManager;
  protected IgnoreManager ignoreManager;
  
  protected final int MESSAGE_BATCH_SIZE = 1;
  protected volatile int topSiteChatConversationMessageId;

  protected static Logger logger = Logger.getLogger(SiteChatServer.class.getName());
  //public static final Logger lagLogger = Logger.getLogger("LagMonitor");

  protected SiteChatUtil siteChatUtil;
  protected QueryUtil queryUtil;
  protected StringUtil stringUtil;

  public SiteChatServer(Provider provider) {

    this.provider = provider;

    setSiteChatUtil(SiteChatUtil.get());
    setQueryUtil(QueryUtil.get());
    setStringUtil(StringUtil.get());
  }
  
  public void setup(int port) throws Exception {
    queryUtil.executeConnectionNoResult(provider, connection -> {
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
      webSocketHandler = new WebSocketHandler() {

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
      this.userManager = new UserManager(provider, siteChatUtil);
      this.userManager.loadUserMap(siteChatUtil.loadSiteChatUserMap(connection).values(), siteChatUtil.getSiteChatUserSettingsList(connection));

      logger.info("Loading Site Chat Ignores...");
      this.ignoreManager = new IgnoreManager(provider, userManager);
      this.ignoreManager.reload();
      
      logger.info("Loading Site Chat Conversations...");
      List<SiteChatConversation> siteChatConversationList = siteChatUtil.getSiteChatConversations(connection);
      for(SiteChatConversation siteChatConversation : siteChatConversationList) {

        SiteChatConversationWithUserList siteChatConversationWithUserList = new SiteChatConversationWithUserList();
        siteChatConversationWithUserList.setSiteChatConversation(siteChatConversation);

        siteChatConversationWithMemberListMap.put(siteChatConversation.getId(), siteChatConversationWithUserList);
      }

      logger.info("Loading Top Site Chat Conversation Message ID...");
      topSiteChatConversationMessageId = siteChatUtil.getTopSiteChatConversationMessageId(connection);

      logger.info("Loading Banned User ID Set...");
      this.banManager = new BanManager(this, siteChatUtil, queryUtil, provider);
      refreshBannedUserList();
      
      this.debugManager = new DebugManager();
    });
  }
  
  protected void buildUsernameToUserMap() {
    synchronized(userManager) {
      userManager.setupUsernameToUserMap();
    }
  }
  
  public void refreshBannedUserList() throws SQLException {
    banManager.loadUserGroups(queryUtil.executeConnection(provider, connection -> siteChatUtil.getBanUserGroups(connection)));
  }

  public void printContainerSizes() {

    BigDecimal freeMemory = new BigDecimal(Runtime.getRuntime().freeMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal totalMemory = new BigDecimal(Runtime.getRuntime().totalMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal maxMemory = new BigDecimal(Runtime.getRuntime().maxMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);

    logger.debug("Descriptors: " + descriptors.size());
    synchronized(siteChatConversationWithMemberListMap) {
      logger.debug("Convos With Member list Map: " + siteChatConversationWithMemberListMap.size());
    }
    synchronized(siteChatPrivateConversationMessageHistoryMap) {
      logger.debug("Private Convo Map: " + siteChatPrivateConversationMessageHistoryMap.size());
    }
    /***
    synchronized(userIdToLastNetworkActivityDatetime) {
      logger.debug("User ID To Activity Map: " + userIdToLastNetworkActivityDatetime.size());
    }
    synchronized(siteChatUserMap) {
      logger.debug("User Map: " + siteChatUserMap.size());
    }
    synchronized(userIdToSiteChatWebSocketsMap) {
      logger.debug("User ID To Web Socket Map: " + userIdToSiteChatWebSocketsMap.size());
    }
    ***/
    synchronized(siteChatConversationMessagesToSave) {
      logger.debug("Messages To Save: " + siteChatConversationMessagesToSave.size());
    }
    logger.debug("Total: " + totalMemory + "MB, Free: " + freeMemory + "MB, Max: " + maxMemory + "MB.");
  }

  public void associateWebSocketWithUser(int userId, SiteChatWebSocket webSocket) {
    synchronized(userManager) {
      userManager.associateWebSocketWithUser(userId, webSocket);
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

  public Map<Integer, SiteChatUser> getSiteChatUserMap(Collection<Integer> userIdCollection) {
    synchronized(userManager) {
      return userManager.getSiteChatUserMap(userIdCollection);
    }
  }
  
  public Map<Integer, UserData> getUserDataMap(Collection<Integer> userIdCollection) {
    synchronized(userManager) {
      return userManager.getUserDataMap(userIdCollection);
    }
  }
  
  public Map<Integer, UserPacket> getUserPacketMap(Collection<Integer> userIdCollection) {
    Map<Integer, UserData> userDataMap;
    synchronized(userManager) {
      userDataMap = userManager.getUserDataMap(userIdCollection);
    }
    
    List<UserPacket> userPackets = MiscUtil.get().transformToList(userDataMap.values(), UserData::createUserPacket);
    return MiscUtil.get().map(userPackets, packet -> packet.id);
  }

  public SiteChatConversationWithUserList createSiteChatConversation(String siteChatConversationName, int userId) throws Exception {

    SiteChatConversation siteChatConversation = new SiteChatConversation();
    siteChatConversation.setCreatedByUserId(userId);
    siteChatConversation.setCreatedDatetime(new Date());
    siteChatConversation.setName(siteChatConversationName);

    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatConversation(connection, siteChatConversation));

    SiteChatConversationWithUserList siteChatConversationWithUserList = new SiteChatConversationWithUserList();
    siteChatConversationWithUserList.setSiteChatConversation(siteChatConversation);
    siteChatConversationWithUserList.getUserIdSet().add(userId);

    siteChatConversationWithMemberListMap.put(siteChatConversation.getId(), siteChatConversationWithUserList);

    return siteChatConversationWithUserList;
  }

  public SiteChatUser getSiteChatUser(int userId) {
    synchronized(userManager) {
      return userManager.getUser(userId).getUser();
    }
  }
  
  public SiteChatUser getSiteChatUser(String name) {
    synchronized(userManager) {
      return userManager.getUser(name).getUser();
    }
  }
  
  public UserData getUserData(int userId) {
    synchronized(userManager) {
      return userManager.getUser(userId);
    }
  }
  
  public UserData getUserData(String name) {
    synchronized(userManager) {
      return userManager.getUser(name);
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
        if(siteChatConversationWithUserList.getSiteChatConversationMessages().size() >= siteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
          siteChatConversationWithUserList.getSiteChatConversationMessages().remove(0);
        }
        siteChatConversationWithUserList.getSiteChatConversationMessages().add(siteChatConversationMessage);
      }
    }
    else {

      String privateConversationMapKey = siteChatUtil.getPrivateMessageHistoryKey(userId, recipientUserId);
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

        if(siteChatConversationMessages.size() >= siteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
          siteChatConversationMessages.remove(0);
        }
        siteChatConversationMessages.add(siteChatConversationMessage);
      }
    }

    return siteChatConversationMessage;
  }

  public void updateConversationPassword(int userId, int conversationId, String password) throws Exception {

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

        siteChatConversation.setPassword(stringUtil.isNullOrEmptyTrimmedString(password) ? null : stringUtil.getSHA1(password));

        queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatConversation(connection, siteChatConversation));
      }
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

          String messageHistoryKey = siteChatUtil.getPrivateMessageHistoryKey(userId, uniqueIdentifier);
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
              siteChatConversationMessage.setMessage(stringUtil.escapeHTMLCharacters(siteChatConversationMessage.getMessage()));
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

    //lagLogger.debug("Refreshing User Cache.");
    Map<Integer, SiteChatUser> siteChatUserMap = queryUtil.executeConnection(provider, connection -> siteChatUtil.loadSiteChatUserMap(connection));
    List<SiteChatUserSettings> userSettingsList = queryUtil.executeConnection(provider, connection -> siteChatUtil.getSiteChatUserSettingsList(connection));
    //lagLogger.debug("User Cache Refreshed.");

    synchronized(userManager) {
      userManager.loadUserMap(siteChatUserMap.values(), userSettingsList);
    }
  }

  public void sendUserListToAllWebSockets() throws Exception {

    //lagLogger.debug("Sending User List To All Web Sockets. START.");
    List<UserPacket> siteChatUserList;
    List<SiteChatBarebonesConversation> siteChatBarebonesConversations = new LinkedList<SiteChatBarebonesConversation>();
    
    synchronized(userManager) {
      siteChatUserList = userManager.getClonedSiteChatUserList(userData -> userData.getLastActivityDatetime() != null);
    }
    
    //Generate message for each user.
    for(UserPacket userPacket : siteChatUserList) {

      int userId = userPacket.id;
      //logger.debug("Considering Sending To User ID: " + userId);
      siteChatBarebonesConversations.clear();
      List<SiteChatWebSocket> webSockets;

      synchronized(userManager) {
        webSockets = new ArrayList<>(userManager.getUser(userPacket.id).getWebSockets());
      }
      
      if(webSockets.isEmpty())
        continue;

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
        
      for(SiteChatWebSocket webSocket : webSockets) {

        synchronized(webSocket) {
          webSocket.sendOutboundPacket(siteChatOutboundUserListPacket);
        }
      }
    }
  }

  public void removeIdleUsers(Date contextDatetime) throws Exception {

    //lagLogger.debug("Remove Idle Users. START.");
    Set<Integer> idleUserIdSet;
    
    //Quickly grab a set of users that we will remove.
    synchronized(userManager) {
      idleUserIdSet = userManager.getIdleUserIdSet();
    }
    
    logger.debug("Removing Inactive Users: " + idleUserIdSet.size());
    
    for(Integer userId : idleUserIdSet)
      removeUser(userId);
    
    //lagLogger.debug("Remove Idle Users. FINISHED.");
  }

  public void removeUser(int userId) {

    List<SiteChatWebSocket> webSocketsToClose = new ArrayList<>();
    synchronized(userManager) {
      UserData userData = userManager.getUser(userId);
      
      if(userData == null)
        return;
      
      webSocketsToClose.addAll(userData.getWebSockets());
      userData.getWebSockets().clear();
      userData.setLastActivityDatetime(null);
      userData.setLastNetworkActivityDatetime(null);
    }
    
    for(SiteChatWebSocket webSocket : webSocketsToClose) {
      
      try {
        webSocket.getConnection().close();
      }
      catch(Throwable throwable) {
        logger.error("Exception thrown while trying to disconnect web socket in removeUser() : " + throwable);
      }
    }

    synchronized(siteChatConversationWithMemberListMap) {
      for(Integer siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {
  
        SiteChatConversationWithUserList SiteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
        SiteChatConversationWithUserList.getUserIdSet().remove(userId);
      }
    }
  }

  public void saveSiteChatConversationMessages() throws Exception {

    synchronized(siteChatConversationMessagesToSave) {

      queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putNewSiteChatConversationMessages(connection, siteChatConversationMessagesToSave));
      siteChatConversationMessagesToSave.clear();
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

      siteChatInboundPacketOperator = siteChatInboundPacketType.getOperatorClass().newInstance();
      siteChatInboundPacketOperator.process(this, webSocket, data);
    }
    catch(Throwable throwable) {

      logger.error("Error processing inbound packet. Full data: " + data, throwable);
    }
  }

  public void sendOutboundPacketToUsers(Set<Integer> userIdSet, SiteChatOutboundPacket siteChatOutboundPacket, Integer excludeUserId) throws IOException {

    for(SiteChatWebSocket siteChatWebSocket : descriptors) {

      synchronized(siteChatWebSocket) {

        if(siteChatWebSocket.getUserData() != null) {

          for(Integer userId : userIdSet) {

            if(excludeUserId != null && excludeUserId.equals(userId)) {

              continue;
            }

            if(siteChatWebSocket.getUserData().getUser().getId() == userId) {

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

  public List<SiteChatConversationMessage> loadHistoricalMessages(int siteChatUserId, SiteChatConversationType conversationType, int uniqueIdentifier, Integer oldestMessageId) throws Exception {
    
    if(conversationType.equals(SiteChatConversationType.Conversation)) {
      
      SiteChatConversationWithUserList conversationWithUserList;
      synchronized(this.siteChatConversationWithMemberListMap) {
        
        conversationWithUserList = siteChatConversationWithMemberListMap.get(uniqueIdentifier);
        
        if(conversationWithUserList == null)
          throw new SiteChatException("Conversation does not exist.");
      }
      
      synchronized(conversationWithUserList) {
        
        //Tell them the conversation doesn't exist. We do not want them to sniff by ID.
        if(!conversationWithUserList.getUserIdSet().contains(siteChatUserId))
          throw new SiteChatException("Conversation does not exist.");
      }
      
      return queryUtil.executeConnection(provider, connection -> siteChatUtil.loadSiteChatConversationMessagesForConversation(connection, uniqueIdentifier, 25, oldestMessageId));
    }
    
    return queryUtil.executeConnection(provider, connection -> siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(connection, siteChatUserId, uniqueIdentifier, 25, oldestMessageId));
  }
  
  public void attemptJoinConversation(SiteChatWebSocket siteChatWebSocket, int siteChatUserId, int siteChatConversationId, boolean notifyUser, boolean notifyConversationMembers, String password, String authCode) throws IOException {

    synchronized(userManager) {
      UserData userData = userManager.getUser(siteChatUserId);
      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      SiteChatConversation siteChatConversation = siteChatConversationWithUserList.getSiteChatConversation();
      Map<Integer, UserData> userDataMap = getUserDataMap(siteChatConversationWithUserList.getUserIdSet());

      if(siteChatConversation.getPassword() != null) {

        if(password == null) {

          if(authCode == null || !authCode.equals(siteChatUtil.generateConversationAuthCode(siteChatUserId, siteChatConversationId, siteChatConversation.getPassword()))) {
            SiteChatOutboundPasswordRequiredPacket siteChatOutboundPasswordRequiredPacket = new SiteChatOutboundPasswordRequiredPacket();
            siteChatOutboundPasswordRequiredPacket.setConversationName(siteChatConversation.getName());
            siteChatWebSocket.sendOutboundPacket(siteChatOutboundPasswordRequiredPacket);

            logger.debug("Password required!");
            return;
          }
        }
        else if(!stringUtil.getSHA1(password).equals(siteChatConversation.getPassword())) {

          SiteChatOutboundIncorrectPasswordPacket siteChatOutboundIncorrectPasswordPacket = new SiteChatOutboundIncorrectPasswordPacket();
          siteChatOutboundIncorrectPasswordPacket.setConversationName(siteChatConversation.getName());
          siteChatWebSocket.sendOutboundPacket(siteChatOutboundIncorrectPasswordPacket);

          logger.debug("Password did not match. Conversation `" + siteChatConversation.getName() + "`.");
          return;
        }
      }

      //Add user to user list.
      logger.debug("Adding user #" + userData.getUser().getId() + " to chat #" + siteChatConversationWithUserList.getSiteChatConversation().getId() + ".");
      siteChatConversationWithUserList.getUserIdSet().add(userData.getUser().getId());

      //Generate response packet to user.
      if(notifyUser) {
        SiteChatOutboundConnectPacket siteChatOutboundConnectPacket = new SiteChatOutboundConnectPacket();
        siteChatOutboundConnectPacket.setWasSuccessful(true);
        siteChatOutboundConnectPacket.setSiteChatConversationId(siteChatConversationWithUserList.getSiteChatConversation().getId());
        siteChatOutboundConnectPacket.setTitleText(siteChatConversation.getName());
        siteChatOutboundConnectPacket.setCreatedByUserId(siteChatConversationWithUserList.getSiteChatConversation().getCreatedByUserId());

        if(siteChatConversation.getPassword() != null) {

          siteChatOutboundConnectPacket.setAuthCode(siteChatUtil.generateConversationAuthCode(userData.getUser().getId(), siteChatConversation.getId(), siteChatConversation.getPassword()));

        }

        Set<UserPacket> userPacketSet = MiscUtil.get().transformToSet(userDataMap.values(), UserData::createUserPacket);

        siteChatOutboundConnectPacket.setUsers(userPacketSet);

        for(SiteChatWebSocket tempSiteChatWebSocket : descriptors) {

          if(tempSiteChatWebSocket.getUserData() != null && tempSiteChatWebSocket.getUserData().getUser().getId() == siteChatUserId) {

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
        siteChatOutboundUserJoinPacket.setSiteChatUser(userData.createUserPacket());

        sendOutboundPacketToUsers(userDataMap.keySet(), siteChatOutboundUserJoinPacket, userData.getUser().getId());
      }
    }
  }
  
  public void processChannelCommand(SiteChatWebSocket webSocket, SiteChatUser user, String message) throws Exception {
    Pattern pattern = Pattern.compile("^/(\\w+)\\s+(.*?)$");
    Matcher matcher = pattern.matcher(message);
    
    if(!matcher.find())
      return;//TODO: Notify user of invalid command.
    
    String command = matcher.group(1).toLowerCase();
    String remainder = matcher.group(2);
    
    if(command.equals("ban")) {
      
      SiteChatUser targetUser = getSiteChatUser(remainder);
      
      if(!banManager.isBanAdmin(user.getId()))
        return;
      if(targetUser == null)
        return;//TODO: Notify.
      
      banManager.banUser(webSocket.getConnection().getRemoteAddress().getHostString(), user.getId(), targetUser);
    }
    else if(command.equals("unban")) {
      SiteChatUser targetUser = getSiteChatUser(remainder);
      
      if(!banManager.isBanAdmin(user.getId()))
        return;
      if(targetUser == null)
        return;//TODO: Notify.
      
      banManager.unbanUser(webSocket.getConnection().getRemoteAddress().getHostString(), user.getId(), targetUser);
    }
    else {
      //TODO: Notify.
    }
  }
  
  public SiteChatIgnore addIgnore(SiteChatWebSocket webSocket, int userId, int ignoredUserId) throws SQLException {
    String ipAddress = webSocket.getConnection().getRemoteAddress().getHostString();
    synchronized(ignoreManager) {
      return ignoreManager.addIgnore(userId, ignoredUserId, ipAddress);
    }
  }
  
  public void removeIgnore(SiteChatWebSocket webSocket, int userId, int ignoredUserId) throws SQLException {
    String ipAddress = webSocket.getConnection().getRemoteAddress().getHostString();
    synchronized(ignoreManager) {
      ignoreManager.removeIgnore(ipAddress, userId, ignoredUserId);
    }
  }

  public void cleanup() throws Exception {

    logger.info("Saving queued conversation messages. Number in buffer: " + siteChatConversationMessagesToSave.size());

    saveSiteChatConversationMessages();
  }

  public boolean authenticateUserLogin(int userId, String sessionId) throws Exception {

    return queryUtil.executeConnection(provider, connection -> siteChatUtil.authenticateUserLogin(connection, userId, sessionId));
  }
  
  public void setResourceBase(String dir) {
    resourceHandler.setResourceBase(dir);
  }

  public String getResourceBase() {
    return resourceHandler.getResourceBase();
  }

  public static void main(String... args) {
    try {

      int port=4241;
      String docRoot=".";

      CommandLineArguments commandLineArguments = new CommandLineArguments(args);

      if(commandLineArguments.getDocumentRoot() != null)
        docRoot = commandLineArguments.getDocumentRoot();
      if(commandLineArguments.getPort() != null)
        port = commandLineArguments.getPort();
      
      Provider provider = new Provider();
      provider.setDocRoot(docRoot);
      logger.info("Loading Configuration.");
      provider.loadConfiguration(docRoot + "/" + "ServerConfig.txt");
      logger.info("Setting Up Connection Pool.");
      provider.setupConnectionPool();

      logger.info("Setting Up Site Chat Server.");
      SiteChatServer server = new SiteChatServer(provider);
      server.setup(port);
      
      logger.info("Starting Site Chat Server.");
      server.start();
      server.join();

      logger.info("Server has been stopped.\n");

      server.cleanup();

      System.exit(0);
    }
    catch (Exception e) {

      logger.error("Severe Exception: ", e);
    }
  }

  public class SiteChatWebSocket implements WebSocketListener {
    protected WebSocketSession connection;
    protected UserData userData;

    public UserData getUserData() {

      return userData;
    }

    public void setUserData(UserData userData) {

      this.userData = userData;
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
      
      logger.trace(this.getClass().getSimpleName() + "#onWebSocketBinary   arg1: " + arg1 + ", arg2: " + arg2 + ", Data: " + (data == null ? "<NULL>" : String.valueOf(data.length)) );
    }

    public void onWebSocketClose(int arg0, String data) {

      logger.trace(this.getClass().getSimpleName() + "#onWebSocketClose   arg0: " + arg0 + ", Data: " + data);
      descriptors.remove(this);

      if(this.getUserData() != null) {
        
        synchronized(userManager) {
          userManager.removeWebSocketFromUser(userData.getUser().getId(), this);
        }
      }
    }

    public void onWebSocketConnect(Session session) {
      logger.trace(this.getClass().getSimpleName() + "#onWebSocketConnect   " + session.getRemoteAddress().getHostString());
      this.connection = (WebSocketSession)session;
    }
    public void onWebSocketError(Throwable throwable) {

      logger.trace(this.getClass().getSimpleName() + "#onWebSocketError   " + throwable);
    }

    public void onWebSocketText(String data) {

      logger.info("User Data: " + userData);
      logger.trace(this.getClass().getSimpleName() + "#onWebSocketText   " + data);
      try {
        logger.info("Got text: " + data);
        processInboundDataPacket(this, data);
      }
      catch(Throwable throwable) {

        logger.error("Error in onWebSocketText()", throwable);
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
    userManager.updateUserActivity(userId);
  }
  
  public void updateUserNetworkActivity(int userId) {
    userManager.updateUserNetworkActivity(userId);
  }
  
  public void setUserSettings(int userId, boolean compact, boolean animateAvatars, String timestampFormat) throws SQLException {
    synchronized(userManager) {
      userManager.setUserSettings(userId, compact, animateAvatars, timestampFormat);
    }
  }

  public SiteChatUtil getSiteChatUtil() {
    return siteChatUtil;
  }

  public void setSiteChatUtil(SiteChatUtil siteChatUtil) {
    this.siteChatUtil = siteChatUtil;
  }

  public QueryUtil getQueryUtil() {
    return queryUtil;
  }

  public void setQueryUtil(QueryUtil queryUtil) {
    this.queryUtil = queryUtil;
  }

  public StringUtil getStringUtil() {
    return stringUtil;
  }

  public void setStringUtil(StringUtil stringUtil) {
    this.stringUtil = stringUtil;
  }

  public Map<Integer, SiteChatConversationWithUserList> getSiteChatConversationWithMemberListMap() {
    return siteChatConversationWithMemberListMap;
  }

  public void setSiteChatConversationWithMemberListMap(
      Map<Integer, SiteChatConversationWithUserList> siteChatConversationWithMemberListMap) {
    this.siteChatConversationWithMemberListMap = siteChatConversationWithMemberListMap;
  }
  
  public BanManager getBanManager() {
    return banManager;
  }
  
  public DebugManager getDebugManager() {
    return debugManager;
  }
  
  public UserManager getUserManager() {
    return userManager;
  }
  
  public IgnoreManager getIgnoreManager() {
    return ignoreManager;
  }
}