package net.mafiascum.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Properties;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;

public class Provider {

  protected String mysqlUrl;
  protected String mysqlUsername;
  protected String mysqlPassword;
  protected String docRoot;
  protected boolean isWindows;
  
  protected Connection connection;
  protected Date connectionLastObtainedDatetime = null;
  
  protected final long MILLIS_BETWEEN_CONNECTION_RENEWAL = 300000;
  
  public Connection getConnection() throws Exception {

    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection connection = DriverManager.getConnection
      (
        mysqlUrl,
        mysqlUsername,
        mysqlPassword
      );
      
      connection.setAutoCommit(false);
      return connection;
  }
  catch(Throwable throwable) {
    
    throw new Exception("Could not create connection", throwable); 
  }
    /***
    try {
      
      if(connectionLastObtainedDatetime == null || new Date().getTime() - connectionLastObtainedDatetime.getTime() >= MILLIS_BETWEEN_CONNECTION_RENEWAL) {
      
        if(connection != null) {
          //Close the old database connection.
          
          QueryUtil.closeNoThrow(connection);
        }
        
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = DriverManager.getConnection
        (
          mysqlUrl,
          mysqlUsername,
          mysqlPassword
        );
        
        connection.setAutoCommit(false);
      }
      
      return connection;
    }
    catch(Throwable throwable) {
      
      throw new Exception("Could not create connection", throwable); 
    }
      ***/
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
  
  public void loadConfiguration(String configurationFilePath) throws Exception {
    
    Properties properties = MiscUtil.loadPropertiesResource(configurationFilePath);
    
    mysqlUrl = properties.getProperty("Mysql.Main.Url");
    mysqlUsername = properties.getProperty("Mysql.Main.Username");
    mysqlPassword = properties.getProperty("Mysql.Main.Password");
    isWindows = StringUtil.removeNull(properties.getProperty("IsWindows")).equals("true");
  }
}
