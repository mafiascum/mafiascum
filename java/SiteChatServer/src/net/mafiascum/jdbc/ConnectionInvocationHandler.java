package net.mafiascum.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Date;

import org.apache.log4j.Logger;

class ConnectionInvocationHandler implements InvocationHandler {

  protected Connection connectionSource;
  protected Connection connectionProxy;
  protected ConnectionPool connectionPool;
  protected Date createdDatetime;
  protected Date lastOpenedDatetime;
  protected Date lastAccessedDatetime;
  protected Date lastClosedDatetime;
  
  protected Logger logger = Logger.getLogger(ConnectionInvocationHandler.class.getName());
  
  public ConnectionInvocationHandler(Connection connectionSource) {
    
    this.connectionSource = connectionSource;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  
    setLastAccessedDatetime(new Date());
    if(method.getName().equals("close")) {
      
      connectionPool.releaseConnection(this);
      return null;
    }
    else {
      return method.invoke(connectionSource, args);
    }
  }
  
  public Connection getConnectionSource() {
    return connectionSource;
  }

  public void setConnectionSource(Connection connectionSource) {
    this.connectionSource = connectionSource;
  }

  public Date getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Date createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public Date getLastOpenedDatetime() {
    return lastOpenedDatetime;
  }

  public void setLastOpenedDatetime(Date lastOpenedDatetime) {
    this.lastOpenedDatetime = lastOpenedDatetime;
  }

  public Date getLastAccessedDatetime() {
    return lastAccessedDatetime;
  }

  public void setLastAccessedDatetime(Date lastAccessedDatetime) {
    this.lastAccessedDatetime = lastAccessedDatetime;
  }

  public Date getLastClosedDatetime() {
    return lastClosedDatetime;
  }

  public void setLastClosedDatetime(Date lastClosedDatetime) {
    this.lastClosedDatetime = lastClosedDatetime;
  }

  public Connection getConnectionProxy() {
    return connectionProxy;
  }
  
  public void setConnectionProxy(Connection connectionProxy) {
    this.connectionProxy = connectionProxy;
  }
  
  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }
  
  public void setConnectionPool(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }
}
