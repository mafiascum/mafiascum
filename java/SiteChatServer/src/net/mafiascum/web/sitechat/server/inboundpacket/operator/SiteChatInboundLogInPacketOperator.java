package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatIgnore;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUserSettings;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.ignore.IgnoreManager;
import net.mafiascum.web.sitechat.server.ignore.IgnorePacket;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundLogInPacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundLogInPacket;
import net.mafiascum.web.sitechat.server.user.UserData;
import net.mafiascum.web.sitechat.server.user.UserManager;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundLogInPacketOperator extends SiteChatInboundPacketOperator {

  private static final Logger logger = Logger.getLogger(SiteChatInboundLogInPacketOperator.class.getName());
  
  public SiteChatInboundLogInPacketOperator() {
    super();
  }
  
  public void process(SiteChatMessageProcessor processor, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundLogInPacket siteChatInboundLogInPacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundLogInPacket.class);
    int userId;
    
    UserData userData = processor.getUserData(siteChatInboundLogInPacket.getUserId());
    SiteChatUser siteChatUser = userData.getUser();
    if(siteChatUser == null) {
      
      logger.error("Non Existent User Attempted To Log In. User ID: " + siteChatInboundLogInPacket.getUserId());
      return;
    }
    
    processor.updateUserActivity(siteChatUser.getId());
    
    userId = siteChatUser.getId();
    
    logger.trace("Log In Packet. User ID: " + siteChatInboundLogInPacket.getUserId() + ", Session: " + siteChatInboundLogInPacket.getSessionId());
    
    //Before even authenticating, perform the cheaper check to see if the user is banned.
    if(processor.getBanManager().isUserBanned(siteChatUser.getId())) {
      
      logger.debug("Banned user #" + siteChatUser.getId() + " attempting to log in. Denied.");
      processor.closeDescriptor(descriptor.getId());
      return;
    }
    
    boolean loginResult = processor.authenticateUserLogin(siteChatInboundLogInPacket.getUserId(), siteChatInboundLogInPacket.getSessionId());
    
    if(!loginResult) {
      
      logger.debug("Login authentication failed for user #" + siteChatUser.getId() + ". Session ID: " + siteChatInboundLogInPacket.getSessionId());
      processor.closeDescriptor(descriptor.getId());
      return;
    }
    
    processor.getUserManager().associateDescriptorUser(userId, descriptor);
    
    Map<Integer, String> conversationIdToAuthCodeMap = siteChatInboundLogInPacket.getCoversationIdToAuthCodeMap();
    
    //Reconnect to conversations the user has been removed from.
    for(String siteChatConversationKey : siteChatInboundLogInPacket.getConversationKeySet()) {

      char symbol = siteChatUtil.getConversationSymbol(siteChatConversationKey);
      SiteChatConversationType siteChatConversationType = siteChatUtil.getSiteChatConversationTypeBySymbol(symbol);
      
      if(!siteChatConversationType.equals(SiteChatConversationType.Conversation)) {
        
        continue;
      }

      int siteChatConversationId = siteChatUtil.getConversationUniqueIdentifier(siteChatConversationKey);
      
      SiteChatConversationWithUserList siteChatConversationWithUserList = processor.getSiteChatConversationWithUserList(siteChatConversationId);
      
      if(siteChatConversationWithUserList == null) {
        
        logger.error("User sent conversation ID that does not exist in system: " + siteChatConversationId);
      }
      else {
        
        if(!siteChatConversationWithUserList.getUserIdSet().contains(siteChatUser.getId())) {
          
          processor.attemptJoinConversation(descriptor, siteChatUser.getId(), siteChatConversationId, false, true, null, conversationIdToAuthCodeMap == null ? null : conversationIdToAuthCodeMap.get(siteChatConversationId));
        }
      }
    }
    
    //Determine which messages the user has missed(if any, most of the time this should result in nothing).
    List<SiteChatConversationMessage> missedSiteChatConversationMessages = new LinkedList<SiteChatConversationMessage>();
    
    if(siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap() != null) {
      for(String siteChatConversationKey : siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap().keySet()) {
        
        char symbol = siteChatUtil.getConversationSymbol(siteChatConversationKey);
        int uniqueIdentifier = siteChatUtil.getConversationUniqueIdentifier(siteChatConversationKey);
        int mostRecentSiteChatConversationMessageId = siteChatInboundLogInPacket.getConversationKeyToMostRecentMessageIdMap().get(siteChatConversationKey);
        SiteChatConversationType siteChatConversationType = siteChatUtil.getSiteChatConversationTypeBySymbol(symbol);
        
        logger.trace("Conversation " + siteChatConversationKey + ", Last Message ID: " + mostRecentSiteChatConversationMessageId);
        
        if(siteChatConversationType == null) {
          
          logger.error("Unknown site chat conversation type. Symbol: " + symbol);
          continue;
        }
        
        List<SiteChatConversationMessage> siteChatConversationMessages = processor.getMessageHistory(siteChatConversationType, mostRecentSiteChatConversationMessageId, siteChatUser.getId(), uniqueIdentifier);
        
        if(siteChatConversationMessages == null || siteChatConversationMessages.isEmpty() == true) {
        
          continue;
        }
        else {
        
          missedSiteChatConversationMessages.addAll(siteChatConversationMessages);
        }
      }
    }
    
    Collections.sort(missedSiteChatConversationMessages, new Comparator<SiteChatConversationMessage>() {
      
      public int compare(SiteChatConversationMessage arg0, SiteChatConversationMessage arg1) {
        
        if(arg0.getId() < arg1.getId())
          return -1;
        else if(arg0.getId() > arg1.getId())
          return 1;
        return 0;
      }
    });
    
    //Create the response
    SiteChatOutboundLogInPacket siteChatOutboundLogInPacket = new SiteChatOutboundLogInPacket();
    siteChatOutboundLogInPacket.setWasSuccessful(true);
    siteChatOutboundLogInPacket.setMissedSiteChatConversationMessages(missedSiteChatConversationMessages);
    siteChatOutboundLogInPacket.setSettings(createSettingsMap(userData));
    siteChatOutboundLogInPacket.setIgnores(getIgnorePackets(siteChatUser, processor.getUserManager(), processor.getIgnoreManager()));
    processor.sendToDescriptor(descriptor.getId(), siteChatOutboundLogInPacket);
  }
  
  protected Map<String, Object> createSettingsMap(UserData userData) {
    Map<String, Object> settingsMap = new HashMap<>();
    SiteChatUserSettings userSettings = userData.getUserSettings();
    
    settingsMap.put("compact", userSettings == null ? false : userSettings.getCompact());
    settingsMap.put("animateAvatars", userSettings == null ? true : userSettings.getAnimateAvatars());
    settingsMap.put("timestamp", userSettings == null ? "" : userSettings.getTimestampFormat());
    settingsMap.put("invisible", userSettings == null ? false : userSettings.getInvisible());
    
    return settingsMap;
  }
  
  protected List<IgnorePacket> getIgnorePackets(SiteChatUser user, UserManager userManager, IgnoreManager ignoreManager) {
    List<IgnorePacket> ignorePackets = new ArrayList<>();
    
    for(SiteChatIgnore ignoreEntry : ignoreManager.getIgnores(user.getId())) {
      UserData ignoredUserData = userManager.getUser(ignoreEntry.getIgnoredUserId());
      
      if(ignoredUserData == null)
        continue;
      
      ignorePackets.add(ignoreEntry.createPacket(ignoredUserData.getUser()));
    }
    return ignorePackets;
  }
}
