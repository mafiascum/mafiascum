package net.mafiascum.web.sitechat.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.mafiascum.functional.ConsumerWithException;
import net.mafiascum.web.sitechat.server.event.SiteChatServerEvent;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class SiteChatServer extends Server {

  protected ServerConnector connector;
  protected WebSocketHandler webSocketHandler;
  protected ResourceHandler resourceHandler;
  
  protected Map<String, SiteChatWebSocket> webSocketMap = new ConcurrentHashMap<>();
  protected List<SiteChatServerEvent> events = new ArrayList<>();
  
  private static final Logger logger = Logger.getLogger(SiteChatServer.class.getName());
  
  public List<SiteChatServerEvent> getAndFlushEvents() {
    List<SiteChatServerEvent> eventsToReturn = new ArrayList<>();
    
    synchronized(events) {
      eventsToReturn.addAll(events);
      events.clear();
    }
    
    return eventsToReturn;
  }
  
  protected void queueEvent(SiteChatServerEvent event) {
    synchronized(events) {
      events.add(event);
    }
  }
  
  protected void removeWebSocket(String id) {
    webSocketMap.remove(id);
  }
  
  public void sendToDescriptor(String descriptorId, String message) throws Exception {
    performConnectionOperation(descriptorId, connection -> connection.getRemote().sendStringByFuture(message));
  }
  
  public void closeDescriptor(String descriptorId) throws Exception {
    performConnectionOperation(descriptorId, connection -> connection.close());
  }
  
  public void performConnectionOperation(String descriptorId, ConsumerWithException<WebSocketSession> consumer) throws Exception {
    SiteChatWebSocket webSocket = webSocketMap.get(descriptorId);
    
    if(webSocket == null)
      return;
    
    WebSocketSession connection = null;
    
    synchronized(webSocket) {
      connection = webSocket.getConnection();
    }
   
    if(connection == null)
      return;
    
    synchronized(connection) {
      consumer.accept(connection);
    }
  }
  
  public static void main(String ... args) {
    new SiteChatServer().setup(args);
  }
  
  protected void setup(String[] args) {
    SiteChatMessageProcessor messageProcessor = new SiteChatMessageProcessor();
    messageProcessor.setup(this, args);
  }
  
  public void setupServer(int port) {
    logger.info(String.format("Setting up server on port %d.", port));
    connector = new ServerConnector(this);
    connector.setPort(port);
    
    addConnector(connector);
    webSocketHandler = new WebSocketHandler() {

      public void configure(WebSocketServletFactory webSocketServletFactory) {

        webSocketServletFactory.setCreator(new WebSocketCreator() {

          public Object createWebSocket(UpgradeRequest upgradeRequest, UpgradeResponse upgradeResponse) {
            
            String webSocketId = UUID.randomUUID().toString();
            SiteChatWebSocket siteChatWebSocket = new SiteChatWebSocket(SiteChatServer.this, webSocketId, upgradeRequest.getHeader("X-Forwarded-For"));
            
            webSocketMap.put(webSocketId, siteChatWebSocket);

            upgradeResponse.setAcceptedSubProtocol("site-chat");

            return siteChatWebSocket;
          }
        });
      }
    };

    setHandler(webSocketHandler);
  }
  
  public void setResourceBase(String dir) {
    resourceHandler.setResourceBase(dir);
  }

  public String getResourceBase() {
    return resourceHandler.getResourceBase();
  }
}