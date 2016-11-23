package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import net.mafiascum.web.sitechat.server.Descriptor;
import net.mafiascum.web.sitechat.server.SiteChatIgnore;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetIgnorePacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundSetIgnorePacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSetIgnorePacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundSetIgnorePacketOperator.class.getName());
  
  public void process(SiteChatMessageProcessor processor, UserData user, Descriptor descriptor, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetIgnorePacket setIgnorePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetIgnorePacket.class);
    
    UserData ignoredUserData = null;
    
    if(setIgnorePacket.ignoredUserId != null)
      ignoredUserData = processor.getUserData(setIgnorePacket.ignoredUserId);
    else if(setIgnorePacket.ignoredName != null)
      ignoredUserData = processor.getUserData(setIgnorePacket.ignoredName);
    
    String operation = stringUtil.removeNull(setIgnorePacket.operation);
    SiteChatIgnore addedIgnore = null;
    
    if(operation.equalsIgnoreCase("SET")) {
      
      if(ignoredUserData == null) {
        logger.error("User " + user.getId() + " is attempting to ignore non-existent user " + setIgnorePacket.ignoredUserId + ".");
        sendErrorPacket(processor, descriptor, "Could not set ignore: the selected user could not be found.");
        return;
      }
      
      addedIgnore = processor.getIgnoreManager().addIgnore(user.getId(), ignoredUserData.getId(), descriptor.getIpAddress());
    }
    else if(operation.equalsIgnoreCase("REMOVE")) {
      
      if(ignoredUserData == null) {
        logger.error("User " + user.getId() + " is attempting to remove ignore for non-existent user " + setIgnorePacket.ignoredUserId + ".");
        sendErrorPacket(processor, descriptor, "Could not remove ignore: the selected user could not be found.");
        return;
      }
      
      processor.getIgnoreManager().removeIgnore(descriptor.getIpAddress(), user.getId(), ignoredUserData.getId());
    }
    else {
      logger.error("Invalid ignore operator `" + setIgnorePacket.operation + "` sent by user " + user.getId());
      return;
    }
    
    SiteChatOutboundSetIgnorePacket outboundPacket = new SiteChatOutboundSetIgnorePacket();
    
    outboundPacket.removed = addedIgnore == null;
    outboundPacket.ignore = addedIgnore == null ? null : addedIgnore.createPacket(ignoredUserData.getUser());
    outboundPacket.ignoredUserId = ignoredUserData.getId();

    processor.sendToDescriptor(descriptor.getId(), outboundPacket);
  }

  protected void sendErrorPacket(SiteChatMessageProcessor processor, Descriptor descriptor, String error) throws Exception {
    SiteChatOutboundSetIgnorePacket outboundPacket = new SiteChatOutboundSetIgnorePacket();
    outboundPacket.errors.add(error);
    processor.sendToDescriptor(descriptor.getId(), outboundPacket);
  }
}
