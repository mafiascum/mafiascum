package net.mafiascum.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.ThreadUtil;

import org.apache.log4j.Logger;

public class ConnectionPool {

  protected List<ConnectionInvocationHandler> availableConnections;
  protected Set<ConnectionInvocationHandler> openConnections;
  protected int totalConnections;
  protected final int maxConnections;
  protected boolean running;
  
  protected Thread allocatorThread;
  protected Logger logger = Logger.getLogger(ConnectionPool.class.getName());
  
  protected String mysqlUrl;
  protected String mysqlUsername;
  protected String mysqlPassword;
  
  public ConnectionPool(int maxConnections, String mysqlUrl, String mysqlUsername, String mysqlPassword) {

    this.maxConnections = maxConnections;
    this.mysqlPassword = mysqlPassword;
    this.mysqlUrl = mysqlUrl;
    this.mysqlUsername = mysqlUsername;
    this.totalConnections = 0;
  }
  
  public void setup() {
    
    availableConnections = new LinkedList<ConnectionInvocationHandler>();
    openConnections = new HashSet<ConnectionInvocationHandler>();
    running = true;
    
    allocatorThread = ThreadUtil.startThread(this, "Connection Pool Allocator", "AllocatorThreadMain", true);
  }
  
  public synchronized Connection openConnection() throws InterruptedException {
    
    ConnectionInvocationHandler connectionInvocationHandler = null;
    
    while(true) {
      while(availableConnections.size() <= 0) {

//      logger.debug("Waiting to allocate connection...\n" + MiscUtil.getPrintableStackTrace(Thread.currentThread().getStackTrace()));
        notifyAll();
        wait();
      }
    
      //Keep trying to find a connection that is still in a valid state, or if we run out, we will
      //break out and continue waiting for new ones to be allocated.
      while(availableConnections.size() > 0) {
        Iterator<ConnectionInvocationHandler> iterator = availableConnections.iterator();
        connectionInvocationHandler = iterator.next();
        iterator.remove();
        
        //Verify that the connection is still in an open state.
        boolean isConnectionValid = true;
        
        try {
          isConnectionValid = !connectionInvocationHandler.getConnectionSource().isClosed();
        }
        catch(SQLException sqlException) {
          
          isConnectionValid = false;
        }
        
        if(!isConnectionValid) {
          
          //This connection is no good. Lower our connection count and try again.
          logger.error("Tried allocating invalid connection. Discarding and trying again.");
          --totalConnections;
          continue;
        }
    
        openConnections.add(connectionInvocationHandler);
    
        connectionInvocationHandler.setLastOpenedDatetime(new Date());
    
        logger.debug("Connection allocated. Remaining: " + availableConnections.size() + ", Opened: " + openConnections.size());

        notifyAll();
        return connectionInvocationHandler.getConnectionProxy();
      }
    
      //If we get here then we did not find a valid connection. Go back to waiting.
    }
  }
  
  public synchronized void releaseConnection(ConnectionInvocationHandler connectionInvocationHandler) {
    
    logger.debug("Releasing Connection.");
    connectionInvocationHandler.setLastClosedDatetime(new Date());
    openConnections.remove(connectionInvocationHandler);
    availableConnections.add(connectionInvocationHandler);

    notifyAll();
  }
  
  public void AllocatorThreadMain() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    while(running) {

      try {
        synchronized(this) {
          
          while(totalConnections >= maxConnections) {
            wait();
          }
          
          try {
            
            while(totalConnections < maxConnections) {
              logger.info("Allocating Connection...");
              Connection connectionSource, connectionProxy;
              connectionSource = DriverManager.getConnection
              (
                mysqlUrl,
                mysqlUsername,
                mysqlPassword
              );
            
              connectionSource.setAutoCommit(false);
              ConnectionInvocationHandler connectionInvocationHandler = new ConnectionInvocationHandler(connectionSource);
              connectionProxy = (Connection)Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                connectionSource.getClass().getInterfaces(),
                connectionInvocationHandler
              );
              
              connectionInvocationHandler.setConnectionProxy(connectionProxy);
              connectionInvocationHandler.setCreatedDatetime(new Date());
              connectionInvocationHandler.setConnectionPool(this);
                
              availableConnections.add(connectionInvocationHandler);
              totalConnections += 1;
            }
          }
          catch(Exception exception) {
              
            logger.error("Could not allocate connection.");
            logger.error(MiscUtil.getPrintableStackTrace(exception));
          }
          
          this.notifyAll();
        }
      
        Thread.sleep(5000);
      }
      catch (InterruptedException interruptedException) {
        logger.error("AllocatorThreadMain:");
        logger.error(MiscUtil.getPrintableStackTrace(interruptedException));
      }
    }
  }
}