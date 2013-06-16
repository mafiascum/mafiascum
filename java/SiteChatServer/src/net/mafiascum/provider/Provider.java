package net.mafiascum.provider;

import java.sql.Connection;
import java.util.Properties;

import net.mafiascum.jdbc.ConnectionPool;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;

public class Provider {

  protected String mysqlUrl;
  protected String mysqlUsername;
  protected String mysqlPassword;
  protected String docRoot;
  protected boolean isWindows;
  protected int maxConnections;
  
  protected ConnectionPool connectionPool = null;
  
  protected final long MILLIS_BETWEEN_CONNECTION_RENEWAL = 300000;
  
  public Connection getConnection() throws Exception {

    return connectionPool.openConnection();
  }
  
  public void setupConnectionPool() {
    
    connectionPool = new ConnectionPool(getMaxConnections(), mysqlUrl, mysqlUsername, mysqlPassword);
    connectionPool.setup();
  }
  
  public void loadConfiguration(String configurationFilePath) throws Exception {
    
    Properties properties = MiscUtil.loadPropertiesResource(configurationFilePath);
    
    mysqlUrl = properties.getProperty("Mysql.Main.Url");
    mysqlUsername = properties.getProperty("Mysql.Main.Username");
    mysqlPassword = properties.getProperty("Mysql.Main.Password");
    isWindows = StringUtil.removeNull(properties.getProperty("IsWindows")).equals("true");
    maxConnections = Integer.valueOf(properties.getProperty("Mysql.Main.MaxConnections"));
  }
  
  public String getDocRoot() {
    
    return docRoot;
  }
  
  public void setDocRoot(String docRoot) {
    
    this.docRoot = docRoot;
  }
  
  public boolean getIsWindows() {
    
    return isWindows;
  }
  
  public void setIsWindows(boolean isWindows) {
    
    this.isWindows = isWindows;
  }
  
  public int getMaxConnections() {
    
    return maxConnections;
  }
  
  public void setMaxConnections(int maxConnections) {
    
    this.maxConnections = maxConnections;
  }
}
