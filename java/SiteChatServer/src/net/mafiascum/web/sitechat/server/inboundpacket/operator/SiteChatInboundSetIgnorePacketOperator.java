package net.mafiascum.web.sitechat.server.inboundpacket.operator;

import java.io.IOException;

import net.mafiascum.web.sitechat.server.SiteChatIgnore;
import net.mafiascum.web.sitechat.server.SiteChatServer;
import net.mafiascum.web.sitechat.server.SiteChatServer.SiteChatWebSocket;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.inboundpacket.SiteChatInboundSetIgnorePacket;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundSetIgnorePacket;
import net.mafiascum.web.sitechat.server.user.UserData;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class SiteChatInboundSetIgnorePacketOperator extends SiteChatInboundSignedInPacketOperator {
  
  private static final Logger logger = Logger.getLogger(SiteChatInboundSetIgnorePacketOperator.class.getName());
  
  public void process(SiteChatServer siteChatServer, SiteChatUser siteChatUser, SiteChatWebSocket siteChatWebSocket, String siteChatInboundPacketJson) throws Exception {
    
    SiteChatInboundSetIgnorePacket setIgnorePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSetIgnorePacket.class);
    
    UserData ignoredUserData = null;
    
    if(setIgnorePacket.ignoredUserId != null)
      ignoredUserData = siteChatServer.getUserData(setIgnorePacket.ignoredUserId);
    else if(setIgnorePacket.ignoredName != null)
      ignoredUserData = siteChatServer.getUserData(setIgnorePacket.ignoredName);
    
    String operation = stringUtil.removeNull(setIgnorePacket.operation);
    SiteChatIgnore addedIgnore = null;
    
    if(operation.equalsIgnoreCase("SET")) {
      
      if(ignoredUserData == null) {
        logger.error("User " + siteChatUser.getId() + " is attempting to ignore non-existent user " + setIgnorePacket.ignoredUserId + ".");
        sendErrorPacket(siteChatWebSocket, "Could not set ignore: the selected user could not be found.");
        return;
      }
      
      addedIgnore = siteChatServer.addIgnore(siteChatWebSocket, siteChatUser.getId(), ignoredUserData.getId());
    }
    else if(operation.equalsIgnoreCase("REMOVE")) {
      
      if(ignoredUserData == null) {
        logger.error("User " + siteChatUser.getId() + " is attempting to remove ignore for non-existent user " + setIgnorePacket.ignoredUserId + ".");
        sendErrorPacket(siteChatWebSocket, "Could not remove ignore: the selected user could not be found.");
        return;
      }
      
      siteChatServer.removeIgnore(siteChatWebSocket, siteChatUser.getId(), ignoredUserData.getId());
    }
    else {
      logger.error("Invalid ignore operator `" + setIgnorePacket.operation + "` sent by user " + siteChatUser.getId());
      return;
    }
    
    SiteChatOutboundSetIgnorePacket outboundPacket = new SiteChatOutboundSetIgnorePacket();
    
    outboundPacket.removed = addedIgnore == null;
    outboundPacket.ignore = addedIgnore == null ? null : addedIgnore.createPacket(ignoredUserData.getUser());
    outboundPacket.ignoredUserId = ignoredUserData.getId();
    
    siteChatWebSocket.sendOutboundPacket(outboundPacket);
  }

  protected void sendErrorPacket(SiteChatWebSocket webSocket, String error) throws IOException {
    SiteChatOutboundSetIgnorePacket outboundPacket = new SiteChatOutboundSetIgnorePacket();
    outboundPacket.errors.add(error);
    webSocket.sendOutboundPacket(outboundPacket);
  }
}
