package net.mafiascum.web.sitechat.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.mafiascum.arguments.CommandLineArguments;
import net.mafiascum.json.DateUnixTimestampSerializer;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.async.SiteChatAsyncProcessor;
import net.mafiascum.web.sitechat.async.SiteChatRefreshBansAsyncProcess;
import net.mafiascum.web.sitechat.async.SiteChatRemoveIdleUsersAsyncProcess;
import net.mafiascum.web.sitechat.async.SiteChatUserListAsyncProcess;
import net.mafiascum.web.sitechat.async.SiteChatUserTableAsyncProcess;
import net.mafiascum.web.sitechat.server.conversation.SiteChatBarebonesConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.debug.DebugManager;
import net.mafiascum.web.sitechat.server.event.SiteChatServerCloseEvent;
import net.mafiascum.web.sitechat.server.event.SiteChatServerEvent;
import net.mafiascum.web.sitechat.server.event.SiteChatServerEventType;
import net.mafiascum.web.sitechat.server.event.SiteChatServerMessageEvent;
import net.mafiascum.web.sitechat.server.event.SiteChatServerOpenEvent;
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

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SiteChatMessageProcessor implements SignalHandler{
  
  protected Map<Integer, SiteChatConversationWithUserList> siteChatConversationWithMemberListMap = new HashMap<Integer, SiteChatConversationWithUserList>();
  protected Map<String, List<SiteChatConversationMessage>> siteChatPrivateConversationMessageHistoryMap = new HashMap<String, List<SiteChatConversationMessage>>();
  protected List<SiteChatConversationMessage> siteChatConversationMessagesToSave = new ArrayList<SiteChatConversationMessage>();
  protected Map<String, Descriptor> descriptorMap = new HashMap<>();

  protected SiteChatAsyncProcessor asyncProcessor;
  protected BanManager banManager;
  protected DebugManager debugManager;
  protected UserManager userManager;
  protected IgnoreManager ignoreManager;
  protected SiteChatServer server;
  protected Provider provider = null;
  
  protected SiteChatUtil siteChatUtil;
  protected QueryUtil queryUtil;
  protected StringUtil stringUtil;

  protected final int PULSES_PER_SECOND = 30;  
  protected final int MESSAGE_BATCH_SIZE = 1;
  protected int topSiteChatConversationMessageId;
  protected AtomicBoolean running;
  
  protected static Logger logger = Logger.getLogger(SiteChatMessageProcessor.class.getName());
  
  public SiteChatMessageProcessor() {
    setSiteChatUtil(SiteChatUtil.get());
    setQueryUtil(QueryUtil.get());
    setStringUtil(StringUtil.get());
  }
  
  public void setup() throws Exception {
    queryUtil.executeConnectionNoResult(provider, connection -> {
      //Add handler for interruption signal.
      Signal.handle(new Signal("INT"), this);
      Signal.handle(new Signal("TERM"), this);
      
      logger.info("Loading Site Chat Users...");
      this.userManager = new UserManager(provider, siteChatUtil);
      this.userManager.loadUserMap(siteChatUtil.loadSiteChatUserMap(connection).values(), siteChatUtil.getSiteChatUserSettingsList(connection), siteChatUtil.getUserGroups(connection));

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
      
      logger.info("Setting Up Async Processor.");
      asyncProcessor = new SiteChatAsyncProcessor();
      asyncProcessor.addProcess(new SiteChatUserTableAsyncProcess(1L * 60L * 1000L)); //Every minute.
      asyncProcessor.addProcess(new SiteChatRemoveIdleUsersAsyncProcess(1L * 60L * 1000L)); //Every minute.
      asyncProcessor.addProcess(new SiteChatUserListAsyncProcess(30L * 1000L)); //Every 30 seconds.
      asyncProcessor.addProcess(new SiteChatRefreshBansAsyncProcess(5L * 60L * 1000)); //Every 5 minutes.
    });
  }
  
  public void sendToDescriptor(String descriptorId, SiteChatOutboundPacket packet) throws Exception {
    String siteChatOutboundPacketJson = new GsonBuilder().registerTypeAdapter(Date.class, new DateUnixTimestampSerializer()).create().toJson(packet);
    sendToDescriptor(descriptorId, siteChatOutboundPacketJson);
  }
  
  public void sendToDescriptor(String descriptorId, String message) throws Exception {
    server.sendToDescriptor(descriptorId, message);
  }
  
  public void closeDescriptor(String descriptorId) throws Exception {
    server.closeDescriptor(descriptorId);
  }
  
  protected void buildUsernameToUserMap() {
    userManager.setupUsernameToUserMap();
  }
  
  public void refreshBannedUserList() throws SQLException {
    banManager.loadUserGroups(queryUtil.executeConnection(provider, connection -> siteChatUtil.getBanUserGroups(connection)));
  }
  
  public void printContainerSizes() {
    
    BigDecimal freeMemory = new BigDecimal(Runtime.getRuntime().freeMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal totalMemory = new BigDecimal(Runtime.getRuntime().totalMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    BigDecimal maxMemory = new BigDecimal(Runtime.getRuntime().maxMemory()).divide(new BigDecimal(1024*1024), BigDecimal.ROUND_HALF_DOWN);
    
    logger.debug("Descriptors: " + descriptorMap.size());
    logger.debug("Convos With Member list Map: " + siteChatConversationWithMemberListMap.size());
    logger.debug("Private Convo Map: " + siteChatPrivateConversationMessageHistoryMap.size());
    logger.debug("Messages To Save: " + siteChatConversationMessagesToSave.size());
    logger.debug("Total: " + totalMemory + "MB, Free: " + freeMemory + "MB, Max: " + maxMemory + "MB.");
  }
  
  public SiteChatConversationWithUserList getConversationWithUserList(String siteChatConversationName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

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
    return userManager.getSiteChatUserMap(userIdCollection);
  }
  
  public Map<Integer, UserData> getUserDataMap(Collection<Integer> userIdCollection) {
    return userManager.getUserDataMap(userIdCollection);
  }
  
  public Map<Integer, UserPacket> getUserPacketMap(Collection<Integer> userIdCollection) {
    Map<Integer, UserData> userDataMap;
    userDataMap = userManager.getUserDataMap(userIdCollection);
    
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
    return userManager.getUser(userId).getUser();
  }
  
  public SiteChatUser getSiteChatUser(String name) {
    return userManager.getUser(name).getUser();
  }
  
  public UserData getUserData(int userId) {
    return userManager.getUser(userId);
  }
  
  public UserData getUserData(String name) {
    return userManager.getUser(name);
  }

  public SiteChatConversationMessage recordSiteChatConversationMessage(int userId, Integer siteChatConversationId, Integer recipientUserId, String message) throws Exception {

    SiteChatConversationMessage siteChatConversationMessage = new SiteChatConversationMessage();
    siteChatConversationMessage.setMessage(message);
    siteChatConversationMessage.setCreatedDatetime(new Date());
    siteChatConversationMessage.setSiteChatConversationId(siteChatConversationId);
    siteChatConversationMessage.setRecipientUserId(recipientUserId);
    siteChatConversationMessage.setUserId(userId);
    siteChatConversationMessage.setId(++topSiteChatConversationMessageId);

    siteChatConversationMessagesToSave.add(siteChatConversationMessage);

    if(siteChatConversationMessagesToSave.size() >= MESSAGE_BATCH_SIZE) {

      saveSiteChatConversationMessages();
    }

    //Save to local conversation cache. Currently only being done for conversations.
    if(siteChatConversationMessage.getSiteChatConversationId() != null) {

      SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);

      if(siteChatConversationWithUserList.getSiteChatConversationMessages().size() >= siteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
        siteChatConversationWithUserList.getSiteChatConversationMessages().remove(0);
      }
      
      siteChatConversationWithUserList.getSiteChatConversationMessages().add(siteChatConversationMessage);
    }
    else {

      String privateConversationMapKey = siteChatUtil.getPrivateMessageHistoryKey(userId, recipientUserId);
      List<SiteChatConversationMessage> siteChatConversationMessages = null;

      siteChatConversationMessages = siteChatPrivateConversationMessageHistoryMap.get(privateConversationMapKey);

      if(siteChatConversationMessages == null) {

        siteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
        siteChatPrivateConversationMessageHistoryMap.put(privateConversationMapKey, siteChatConversationMessages);
      }

      if(siteChatConversationMessages.size() >= siteChatUtil.MAX_MESSAGES_PER_CONVERSATION_CACHE) {
        siteChatConversationMessages.remove(0);
      }
      
      siteChatConversationMessages.add(siteChatConversationMessage);
    }

    return siteChatConversationMessage;
  }

  public void updateConversationPassword(int userId, int conversationId, String password) throws Exception {

    SiteChatConversationWithUserList conversationWithUserList = getSiteChatConversationWithUserList(conversationId);

    if(conversationWithUserList == null) {

      throw new SiteChatException("Conversation not found.");
    }
    
    SiteChatConversation siteChatConversation = conversationWithUserList.getSiteChatConversation();
    
    if(siteChatConversation.getCreatedByUserId() != userId) {

      throw new SiteChatException("User is not conversation creator.");
    }

    siteChatConversation.setPassword(stringUtil.isNullOrEmptyTrimmedString(password) ? null : stringUtil.getSHA1(password));

    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putSiteChatConversation(connection, siteChatConversation));
  }

  public List<SiteChatConversationMessage> getMessageHistory(SiteChatConversationType siteChatConversationType, int lastReceivedSiteChatConversationId, int userId, int uniqueIdentifier) {

    List<SiteChatConversationMessage> messageHistoryToSendToUser = new LinkedList<SiteChatConversationMessage>();
    List<SiteChatConversationMessage> siteChatConversationMessages = null;
    
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
      
      if(siteChatConversationMessages.size() == 0) {

        return messageHistoryToSendToUser;
      }
    }
    
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

    return messageHistoryToSendToUser;
  }

  public void sendUserListToAllDescriptors() throws Exception {
    
    List<UserPacket> visibleUsers, allUsers, invisibleUsers;
    List<SiteChatBarebonesConversation> siteChatBarebonesConversations = new ArrayList<SiteChatBarebonesConversation>();
    
    visibleUsers = userManager.getClonedSiteChatUserList(userData -> userData.getLastActivityDatetime() != null && !userData.isInvisible());
    allUsers = userManager.getClonedSiteChatUserList(userData -> userData.getLastActivityDatetime() != null);
    invisibleUsers = userManager.getClonedSiteChatUserList(userData -> userData.getLastActivityDatetime() != null && userData.isInvisible());
    Set<Integer> invisibleUserIdSet = MiscUtil.get().transformToSet(invisibleUsers, userPacket -> userPacket.id);
    
    //Generate message for each user.
    for(UserPacket userPacket : allUsers) {
      
      int userId = userPacket.id;
      UserData userData = userManager.getUser(userId);
      boolean canSeeInvisibleUsers = userManager.isAdmin(userData) || userManager.isChatMod(userData);
      siteChatBarebonesConversations.clear();
      List<Descriptor> descriptors;
      
      descriptors = new ArrayList<>(userManager.getUser(userPacket.id).getDescriptors());
      
      if(descriptors.isEmpty())
        continue;
      
      for(int siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {

        SiteChatConversationWithUserList conversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
        SiteChatConversation conversation = conversationWithUserList.getSiteChatConversation();
        
        //No users in channel.
        if(conversationWithUserList.getUserIdSet().isEmpty())
          continue;
        
        //Password protected channel, user is not in channel.
        if(conversation.getPassword() != null && !conversationWithUserList.getUserIdSet().contains(userId))
            continue;

        //Copy all but the message array over. We do not need it for this.
        SiteChatBarebonesConversation barebonesConversation = new SiteChatBarebonesConversation();
        barebonesConversation.setId(conversationWithUserList.getSiteChatConversation().getId());
        barebonesConversation.setName(conversationWithUserList.getSiteChatConversation().getName());
        barebonesConversation.setUserIdSet(conversationWithUserList.getUserIdSet().stream().filter(channelUserId -> canSeeInvisibleUsers || !invisibleUserIdSet.contains(channelUserId)).collect(Collectors.toSet()));
        barebonesConversation.setCreatedByUserId(conversationWithUserList.getSiteChatConversation().getCreatedByUserId());
        
        siteChatBarebonesConversations.add(barebonesConversation);
      }
      
      SiteChatOutboundUserListPacket siteChatOutboundUserListPacket = new SiteChatOutboundUserListPacket();
      siteChatOutboundUserListPacket.setSiteChatUsers(canSeeInvisibleUsers ? allUsers : visibleUsers);
      siteChatOutboundUserListPacket.setSiteChatConversations(siteChatBarebonesConversations);
      siteChatOutboundUserListPacket.setInvisibleUserIds(canSeeInvisibleUsers ? invisibleUserIdSet : new HashSet<>());
      siteChatOutboundUserListPacket.setPacketSentDatetime(new Date());
      
      for(Descriptor descriptor : descriptors) {
        sendToDescriptor(descriptor.getId(), siteChatOutboundUserListPacket);
      }
    }
  }

  public void removeIdleUsers(Date contextDatetime) throws Exception {

    Set<Integer> idleUserIdSet;
    
    //Quickly grab a set of users that we will remove.
    idleUserIdSet = userManager.getIdleUserIdSet();
    
    logger.debug("Removing Inactive Users: " + idleUserIdSet.size());
    
    for(Integer userId : idleUserIdSet)
      removeUser(userId);
  }

  public void removeUser(int userId) {

    List<Descriptor> descriptorsToClose = new ArrayList<>();
    
    UserData userData = userManager.getUser(userId);
      
    if(userData == null)
      return;
    
    //TODO: The clearing portion should be moved into UserManager.
    descriptorsToClose.addAll(userData.getDescriptors());
    userData.getDescriptors().clear();
    userData.setLastActivityDatetime(null);
    userData.setLastNetworkActivityDatetime(null);
    
    for(Descriptor descriptor : descriptorsToClose) {
      
      try {
        closeDescriptor(descriptor.getId());
      }
      catch(Throwable throwable) {
        logger.error("Exception thrown while trying to disconnect web socket in removeUser() : " + throwable);
      }
    }
    
    for(Integer siteChatConversationId : siteChatConversationWithMemberListMap.keySet()) {

      SiteChatConversationWithUserList SiteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
      SiteChatConversationWithUserList.getUserIdSet().remove(userId);
    }
  }

  public void saveSiteChatConversationMessages() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> siteChatUtil.putNewSiteChatConversationMessages(connection, siteChatConversationMessagesToSave));
    siteChatConversationMessagesToSave.clear();
  }

  public void sendOutboundPacketToUsers(Set<Integer> userIdSet, SiteChatOutboundPacket siteChatOutboundPacket, Integer excludeUserId) throws IOException {

    for(int userId : userIdSet) {
      
      if(excludeUserId != null && excludeUserId.equals(userId))
        continue;
      
      UserData user = userManager.getUser(userId);
      
      if(user == null)
        continue;
      
      for(Descriptor descriptor : user.getDescriptors()) {
        try {
          sendToDescriptor(descriptor.getId(), siteChatOutboundPacket);
        }
        catch(Exception exception) {
          logger.error("Could not send outbound packet: ", exception);
        }
      }
    }
  }

  public List<SiteChatConversationMessage> loadHistoricalMessages(int siteChatUserId, SiteChatConversationType conversationType, int uniqueIdentifier, Integer oldestMessageId) throws Exception {
    
    if(conversationType.equals(SiteChatConversationType.Conversation)) {
      
      SiteChatConversationWithUserList conversationWithUserList;
      
      conversationWithUserList = siteChatConversationWithMemberListMap.get(uniqueIdentifier);
      
      if(conversationWithUserList == null)
        throw new SiteChatException("Conversation does not exist.");
      
      //Tell them the conversation doesn't exist. We do not want them to sniff by ID.
      if(!conversationWithUserList.getUserIdSet().contains(siteChatUserId))
        throw new SiteChatException("Conversation does not exist.");
      
      return queryUtil.executeConnection(provider, connection -> siteChatUtil.loadSiteChatConversationMessagesForConversation(connection, uniqueIdentifier, 25, oldestMessageId));
    }
    
    return queryUtil.executeConnection(provider, connection -> siteChatUtil.loadSiteChatConversationMessagesForPrivateConversation(connection, siteChatUserId, uniqueIdentifier, 25, oldestMessageId));
  }
  
  public void attemptJoinConversation(Descriptor descriptor, int siteChatUserId, int siteChatConversationId, boolean notifyUser, boolean notifyConversationMembers, String password, String authCode) throws Exception {

    UserData userData = userManager.getUser(siteChatUserId);
    SiteChatConversationWithUserList siteChatConversationWithUserList = siteChatConversationWithMemberListMap.get(siteChatConversationId);
    SiteChatConversation siteChatConversation = siteChatConversationWithUserList.getSiteChatConversation();
    Map<Integer, UserData> userDataMap = getUserDataMap(siteChatConversationWithUserList.getUserIdSet());

    if(siteChatConversation.getPassword() != null) {

      if(password == null) {

        if(authCode == null || !authCode.equals(siteChatUtil.generateConversationAuthCode(siteChatUserId, siteChatConversationId, siteChatConversation.getPassword()))) {
          SiteChatOutboundPasswordRequiredPacket siteChatOutboundPasswordRequiredPacket = new SiteChatOutboundPasswordRequiredPacket();
          siteChatOutboundPasswordRequiredPacket.setConversationName(siteChatConversation.getName());
          sendToDescriptor(descriptor.getId(), siteChatOutboundPasswordRequiredPacket);

          logger.debug("Password required!");
          return;
        }
      }
      else if(!stringUtil.getSHA1(password).equals(siteChatConversation.getPassword())) {

        SiteChatOutboundIncorrectPasswordPacket siteChatOutboundIncorrectPasswordPacket = new SiteChatOutboundIncorrectPasswordPacket();
        siteChatOutboundIncorrectPasswordPacket.setConversationName(siteChatConversation.getName());
        sendToDescriptor(descriptor.getId(), siteChatOutboundIncorrectPasswordPacket);

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

      for(Descriptor tempDescriptor : descriptorMap.values()) {
        
        UserData tempDescriptorUser = userManager.getUserByDescriptorId(tempDescriptor.getId());
        if(tempDescriptorUser != null && tempDescriptorUser.getId() == siteChatUserId) {

          try {
            sendToDescriptor(descriptor.getId(), siteChatOutboundConnectPacket);
          }
          catch(IOException ioException) {
            logger.error("Error attempting to send outbound packet. User ID: " + tempDescriptor.getId(), ioException);
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
  
  public void processChannelCommand(Descriptor descriptor, SiteChatUser user, String message) throws Exception {
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
      
      banManager.banUser(descriptor.getIpAddress(), user.getId(), targetUser);
    }
    else if(command.equals("unban")) {
      SiteChatUser targetUser = getSiteChatUser(remainder);
      
      if(!banManager.isBanAdmin(user.getId()))
        return;
      if(targetUser == null)
        return;//TODO: Notify.
      
      banManager.unbanUser(descriptor.getIpAddress(), user.getId(), targetUser);
    }
    else {
      //TODO: Notify.
    }
  }

  public void cleanup() throws Exception {

    logger.info("Saving queued conversation messages. Number in buffer: " + siteChatConversationMessagesToSave.size());

    saveSiteChatConversationMessages();
  }

  public boolean authenticateUserLogin(int userId, String sessionId) throws Exception {

    return queryUtil.executeConnection(provider, connection -> siteChatUtil.authenticateUserLogin(connection, userId, sessionId));
  }
  
  public void setup(SiteChatServer server, String... args) {
    try {
      
      this.server = server;
      
      int port=4241;
      String docRoot=".";

      CommandLineArguments commandLineArguments = new CommandLineArguments(args);

      if(commandLineArguments.getDocumentRoot() != null)
        docRoot = commandLineArguments.getDocumentRoot();
      if(commandLineArguments.getPort() != null)
        port = commandLineArguments.getPort();
      
      this.provider = new Provider();
      provider.setDocRoot(docRoot);
      logger.info("Loading Configuration.");
      provider.loadConfiguration(docRoot + "/" + "ServerConfig.txt");
      logger.info("Setting Up Connection Pool.");
      provider.setupConnectionPool();
      
      this.running = new AtomicBoolean(true);
      
      logger.info("Setting Up Message Processor.");
      setup();

      logger.info("Setting Up Site Chat Server.");
      server.setupServer(port);
      
      logger.info("Starting Site Chat Server.");
      server.start();
      
      run();
      
      server.stop();
      server.join();

      logger.info("Server has been stopped.\n");

      cleanup();

      System.exit(0);
    }
    catch (Exception e) {

      logger.error("Severe Exception: ", e);
    }
  }
  
  /**
   * 
   * Main loop with precise sleep intervals.
   * 
   */
  protected void run() {
    
    long nanoSecondsPerPulse = 1000000000 / PULSES_PER_SECOND;
    long nanoSecondsNext = System.nanoTime();
    
    while(running.get()) {
      
      try {
        long nanoSecondsStart = System.nanoTime();
        nanoSecondsNext = nanoSecondsStart + nanoSecondsPerPulse + (nanoSecondsNext - nanoSecondsStart);
        
        pulse();
        
        long nanoSecondsSleep = nanoSecondsNext - System.nanoTime();
        long miliSecondsSleep = nanoSecondsSleep / 1000000;
        int nanoFractionSleep = (int)(nanoSecondsSleep - miliSecondsSleep * 1000000);
        
        if(nanoSecondsSleep > 0)
          Thread.sleep(miliSecondsSleep, nanoFractionSleep);
      }
      catch(Exception exception) {
        logger.error("Error during message processor pulse.", exception);
      }
    }
  }
  
  protected void pulse() {
    processServerEvents();
    processAsyncEvents();
  }
  
  protected void processAsyncEvents() {
    asyncProcessor.checkProcesses(this);
  }
  
  protected void processServerEvents() {
    for(SiteChatServerEvent event : server.getAndFlushEvents()) {
      try {
        processServerEvent(event);
      }
      catch(Exception exception) {
        logger.error("Error while processing server event.", exception);
      }
    }
  }
  
  protected void processServerEvent(SiteChatServerEvent event) {
    logger.info("Processing event: " + event.getType().getStandardName());
    if(event.getType().equals(SiteChatServerEventType.open))
      processServerOpenEvent((SiteChatServerOpenEvent)event);
    else if(event.getType().equals(SiteChatServerEventType.message))
      processServerMessageEvent((SiteChatServerMessageEvent)event);
    else if(event.getType().equals(SiteChatServerEventType.close))
      processServerCloseEvent((SiteChatServerCloseEvent)event);
  }
  
  protected void processServerOpenEvent(SiteChatServerOpenEvent event) {
    descriptorMap.put(event.getDescriptor().getId(), event.getDescriptor());
  }
  
  protected void processServerMessageEvent(SiteChatServerMessageEvent event) {
    
    logger.info("Got text: " + event.getMessage());
    SiteChatInboundPacketSkeleton siteChatInboundPacketSkeleton = null;
    SiteChatInboundPacketType siteChatInboundPacketType = null;
    SiteChatInboundPacketOperator siteChatInboundPacketOperator = null;
    try {
      
      siteChatInboundPacketSkeleton = new Gson().fromJson(event.getMessage(), SiteChatInboundPacketSkeleton.class);
      siteChatInboundPacketType = SiteChatInboundPacketType.getEnumByStandardName(siteChatInboundPacketSkeleton.command);
      
      if(siteChatInboundPacketType == null) {
        
        throw new Exception("Could not find processor for command `" + siteChatInboundPacketSkeleton.command + "`");
      }
      
      siteChatInboundPacketOperator = siteChatInboundPacketType.getOperatorClass().newInstance();
      siteChatInboundPacketOperator.process(this, event.getDescriptor(), event.getMessage());
    }
    catch(Throwable throwable) {
      
      logger.error("Error processing inbound packet. Full data: " + event.getMessage(), throwable);
    }
  }
  
  protected void processServerCloseEvent(SiteChatServerCloseEvent event) {
    descriptorMap.remove(event.getDescriptor().getId());
    userManager.removeDescriptorFromUser(event.getDescriptor().getId());
  }

  public void handle(Signal signal) {

    try {

      logger.info("Signal received. Shutting down.");

      running.set(false);

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
  
  public void setUserSettings(int userId, boolean compact, boolean animateAvatars, boolean invisible, String timestampFormat) throws SQLException {
    userManager.setUserSettings(userId, compact, animateAvatars, invisible, timestampFormat);
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

  public void setSiteChatConversationWithMemberListMap(Map<Integer, SiteChatConversationWithUserList> siteChatConversationWithMemberListMap) {
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
  
  public void setProvider(Provider provider) {
    this.provider = provider;
  }
  
  public Provider getProvider() {
    return provider;
  }
}
