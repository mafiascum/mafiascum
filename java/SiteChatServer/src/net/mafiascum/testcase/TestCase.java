package net.mafiascum.testcase;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.mafiascum.arguments.CommandLineArguments;
import net.mafiascum.environment.Environment;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.SiteChatUtil;

import org.apache.log4j.Logger;

public abstract class TestCase {

  public abstract void execute() throws Exception;
  
  private static final Logger logger = Logger.getLogger(TestCase.class.getName());
  
  protected Provider provider;
  protected CommandLineArguments commandLineArguments;
  protected SiteChatUtil siteChatUtil;
  protected QueryUtil queryUtil;
  protected StringUtil stringUtil;
  protected MiscUtil miscUtil;
  
  protected static String setupTestDatabase(Provider provider) throws SQLException {
    
    String databaseName = "mafiascum_test_" + System.currentTimeMillis();
    
    QueryUtil.get().executeStatement(provider, statement -> {
      
      statement.executeUpdate("CREATE DATABASE `" + databaseName + "`");
      
      ResultSet resultSet = statement.executeQuery("SHOW TABLES");
      List<String> tables = new ArrayList<String>();
      
      while(resultSet.next()) {
        
        tables.add(resultSet.getString(1));
      }
      
      resultSet.close();
      
      for(String tableName : tables) {
        
        statement.executeUpdate("CREATE TABLE " + databaseName + "." + tableName + " LIKE " + tableName);
      }
      
      return null;
    });
    
    return databaseName;
  }
  
  public static void populateTestTables(Provider provider, String oldDatabaseName, String testDatabaseName) throws SQLException {
    
    logger.info("Populating Test Database.");
    
    QueryUtil.get().executeStatement(provider, statement -> {
      
      performInsertIntoTestDatabase(statement, "phpbb_users", oldDatabaseName, testDatabaseName, null);
      performInsertIntoTestDatabase(statement, "siteChatConversation", oldDatabaseName, testDatabaseName, null);
      performInsertIntoTestDatabase(statement, "siteChatConversationMessage", oldDatabaseName, testDatabaseName, null);
      performInsertIntoTestDatabase(statement, "phpbb_user_group", oldDatabaseName, testDatabaseName, null);
      performInsertIntoTestDatabase(statement, "phpbb_groups", oldDatabaseName, testDatabaseName, null);
      
      return null;
    });
  }
  
  public static void performInsertIntoTestDatabase(Statement statement, String tableName, String oldDatabaseName, String testDatabaseName, String criteria) throws SQLException {
    
    String sql = " INSERT INTO " + testDatabaseName + "." + tableName
               + " SELECT *"
               + " FROM " + oldDatabaseName + "." + tableName
               + " WHERE " + (criteria == null ? "1" : criteria);
    
    statement.executeUpdate(sql);
  }
  
  public static void dropTestDatabase(Provider provider, String testDatabaseName) throws SQLException {
    
    QueryUtil.get().executeStatement(provider, statement -> {
      
      statement.executeUpdate("DROP DATABASE `" + testDatabaseName + "`");
      return null;
    });
  }
  
  public static void main(String[] args) throws Exception {
    
    CommandLineArguments commandLineArguments = new CommandLineArguments(args);
    Provider provider = new Provider();
    
    provider.loadConfiguration("file:" + new File(commandLineArguments.getDocumentRoot(), "ServerConfig.txt").getAbsolutePath());
    
    if(provider.getEnvironment().equals(Environment.prod)) {
      logger.warn("This script cannot be run in production environment. Aborting.");
      System.exit(1);
    }
    
    provider.setupConnectionPool();
    String testDatabaseName = setupTestDatabase(provider);

    try {
      provider.shutdownConnectionPool();
      String mysqlUrl = provider.getMysqlUrl();
      String oldDatabaseName = provider.getDatabaseName();
      provider.setMysqlUrl(mysqlUrl.replace(oldDatabaseName, testDatabaseName));
      provider.setupConnectionPool();
      
      populateTestTables(provider, oldDatabaseName, testDatabaseName);
      
      
      List<Class<?extends TestCase>> classes = new ArrayList<Class<?extends TestCase>>();
      classes.add(FirstTestCase.class);
      
      for(Class<?extends TestCase> testCaseClass : classes) {
        
        try {
          TestCase testCase = testCaseClass.newInstance();
          
          testCase.setProvider(provider);
          testCase.setCommandLineArguments(commandLineArguments);
          testCase.setStringUtil(StringUtil.get());
          testCase.setQueryUtil(QueryUtil.get());
          testCase.setSiteChatUtil(SiteChatUtil.get());
          testCase.setMiscUtil(MiscUtil.get());
          
          testCase.execute();
        }
        catch(Exception exception) {
          
          logger.error("Error while executing test case for `" + testCaseClass.getName() + "`", exception);
        }
      }
    }
    finally {
      
      provider.shutdownConnectionPool();
      provider.setupConnectionPool();
      dropTestDatabase(provider, testDatabaseName);
      
      logger.info("Shutdown Complete.");
    }
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public CommandLineArguments getCommandLineArguments() {
    return commandLineArguments;
  }

  public void setCommandLineArguments(CommandLineArguments commandLineArguments) {
    this.commandLineArguments = commandLineArguments;
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

  public MiscUtil getMiscUtil() {
    return miscUtil;
  }

  public void setMiscUtil(MiscUtil miscUtil) {
    this.miscUtil = miscUtil;
  }
}
