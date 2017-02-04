package net.mafiascum.web.sitechat.server;

import net.mafiascum.web.sitechat.server.event.SiteChatServerCloseEvent;
import net.mafiascum.web.sitechat.server.event.SiteChatServerMessageEvent;
import net.mafiascum.web.sitechat.server.event.SiteChatServerOpenEvent;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.common.WebSocketSession;

public class SiteChatWebSocket implements WebSocketListener {
  protected WebSocketSession connection;
  protected String id;
  protected SiteChatServer server;
  protected String forwardedFor;
  
  private static final Logger logger = Logger.getLogger(SiteChatWebSocket.class.getName());
  
  public SiteChatWebSocket(SiteChatServer server, String id, String forwardedFor) {
    this.server = server;
    this.id = id;
    this.forwardedFor = forwardedFor;
  }
  
  public WebSocketSession getConnection() {
    return connection;
  }
  
  public void onWebSocketBinary(byte[] data, int arg1, int arg2) {
    logger.info(this.getClass().getSimpleName() + "#onWebSocketBinary   arg1: " + arg1 + ", arg2: " + arg2 + ", Data: " + (data == null ? "<NULL>" : String.valueOf(data.length)) );
  }

  public void onWebSocketClose(int arg0, String data) {
    logger.trace(this.getClass().getSimpleName() + "#onWebSocketClose   arg0: " + arg0 + ", Data: " + data);
    
    server.queueEvent(new SiteChatServerCloseEvent(new Descriptor(id, getIpAddress())));
    server.removeWebSocket(id);
  }

  public void onWebSocketConnect(Session session) {
    logger.trace(this.getClass().getSimpleName() + "#onWebSocketConnect   " + getIpAddress());
    
    this.connection = (WebSocketSession)session;
    
    server.queueEvent(new SiteChatServerOpenEvent(new Descriptor(id, getIpAddress())));
  }
  
  public void onWebSocketError(Throwable throwable) {
    logger.trace(this.getClass().getSimpleName() + "#onWebSocketError   " + throwable);
  }

  public void onWebSocketText(String data) {
    logger.trace(this.getClass().getSimpleName() + "#onWebSocketText   " + data);
    
    server.queueEvent(new SiteChatServerMessageEvent(new Descriptor(id, getIpAddress()), data));
  }
  
  protected String getIpAddress() {
    if(forwardedFor != null)
      return forwardedFor;
    return connection.getRemoteAddress().getHostString();
  }
}