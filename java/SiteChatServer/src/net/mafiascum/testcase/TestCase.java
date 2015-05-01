package net.mafiascum.testcase;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.mafiascum.arguments.CommandLineArguments;
import net.mafiascum.environment.Environment;
import net.mafiascum.provider.Provider;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;

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
  
  public static final int LOBBY_CONVERSATION_ID = 11;
  public static final int THE_HIVE_CONVERSATION_ID = 12;
  public static final int MAFIA_CONVERSATION_ID = 13;
  
  protected static String setupTestDatabase(Provider provider) throws SQLException {
    
    String databaseName = "mafiascum_test_" + System.currentTimeMillis();
    
    QueryUtil.get().executeStatement(provider, statement -> {
      
      statement.executeUpdate("CREATE DATABASE `" + databaseName + "`");
      
      for(String tableName : getTables(statement, null)) {
        
        statement.executeUpdate("CREATE TABLE `" + databaseName + "`.`" + tableName + "` LIKE `" + tableName + "`");
      }
      
      return null;
    });
    
    return databaseName;
  }
  
  public static List<String> getTables(Statement statement, String databaseName) throws SQLException {
    
    List<String> tables = new ArrayList<String>();
    ResultSet resultSet = statement.executeQuery("SHOW TABLES" + (databaseName == null ? "" : (" IN `" + databaseName + "`")));
    while(resultSet.next()) {
      tables.add(resultSet.getString(1));
    }
    resultSet.close();
    return tables;
  }
  
  public static void clearTestTables(Provider provider, String testDatabaseName) throws SQLException {
    
    QueryUtil queryUtil = QueryUtil.get();
    
    queryUtil.executeStatement(provider, statement -> {

      for(String tableName : getTables(statement, testDatabaseName)) {
        
        statement.executeUpdate("TRUNCATE TABLE `" + testDatabaseName + "`.`" + tableName + "`");
      }
      
      return null;
    });
  }
  
  public static void populateTestTables(Provider provider, String oldDatabaseName, String testDatabaseName) throws SQLException {
    
    logger.info("Populating Test Database.");
    QueryUtil queryUtil = QueryUtil.get();

    clearTestTables(provider, testDatabaseName);
    
    queryUtil.executeStatement(provider, statement -> {
      
      performInsertIntoTestDatabase(statement, "phpbb_users", oldDatabaseName, testDatabaseName, null);
      performInsertIntoTestDatabase(statement, "phpbb_groups", oldDatabaseName, testDatabaseName, null);

      List<SiteChatConversation> conversations = Arrays.asList(
          new SiteChatConversation(LOBBY_CONVERSATION_ID, new Date(), 5932, "Lobby", null),
          new SiteChatConversation(THE_HIVE_CONVERSATION_ID, new Date(), 5932, "The Hive", StringUtil.get().getSHA1("password")),
          new SiteChatConversation(MAFIA_CONVERSATION_ID, new Date(), 5932, "Mafia", null)
      );
      
      queryUtil.batchInsert(statement.getConnection(), conversations, queryUtil.getTableName(SiteChatConversation.class), 1000, false);
      
      List<SiteChatConversationMessage> messages = Arrays.asList(
          new SiteChatConversationMessage( 1, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 1"),
          new SiteChatConversationMessage( 2, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 2"),
          new SiteChatConversationMessage( 3, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 3"),
          new SiteChatConversationMessage( 4, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 4"),
          new SiteChatConversationMessage( 5, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 5"),
          new SiteChatConversationMessage( 6, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 6"),
          new SiteChatConversationMessage( 7, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 7"),
          new SiteChatConversationMessage( 8, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 8"),
          new SiteChatConversationMessage( 9, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 9"),
          new SiteChatConversationMessage(10, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 10"),
          new SiteChatConversationMessage(11, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 11"),
          new SiteChatConversationMessage(12, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 12"),
          new SiteChatConversationMessage(13, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 13"),
          new SiteChatConversationMessage(14, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 14"),
          new SiteChatConversationMessage(15, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 15"),
          new SiteChatConversationMessage(16, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 16"),
          new SiteChatConversationMessage(17, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 17"),
          new SiteChatConversationMessage(18, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 18"),
          new SiteChatConversationMessage(19, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 19"),
          new SiteChatConversationMessage(20, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 20"),
          new SiteChatConversationMessage(21, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 21"),
          new SiteChatConversationMessage(22, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 22"),
          new SiteChatConversationMessage(23, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 23"),
          new SiteChatConversationMessage(24, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 24"),
          new SiteChatConversationMessage(25, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 25"),
          new SiteChatConversationMessage(26, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 26"),
          new SiteChatConversationMessage(27, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 27"),
          new SiteChatConversationMessage(28, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 28"),
          new SiteChatConversationMessage(29, 2302, new Date(), LOBBY_CONVERSATION_ID, null, "Message 29"),
          new SiteChatConversationMessage(30, 5932, new Date(), LOBBY_CONVERSATION_ID, null, "Message 30"),
          
          new SiteChatConversationMessage(31, 5932, new Date(), null, 2302, "Private Message 1"),
          new SiteChatConversationMessage(32, 5932, new Date(), null, 2302, "Private Message 2"),
          new SiteChatConversationMessage(33, 5932, new Date(), null, 2302, "Private Message 3"),
          new SiteChatConversationMessage(34, 5932, new Date(), null, 2302, "Private Message 4"),
          new SiteChatConversationMessage(35, 2302, new Date(), null, 5932, "Private Message 5"),
          new SiteChatConversationMessage(36, 2302, new Date(), null, 5932, "Private Message 6"),
          new SiteChatConversationMessage(37, 2302, new Date(), null, 5932, "Private Message 7"),
          new SiteChatConversationMessage(38, 5932, new Date(), null, 2302, "Private Message 8"),
          new SiteChatConversationMessage(39, 2302, new Date(), null, 5932, "Private Message 9"),
          new SiteChatConversationMessage(40, 2302, new Date(), null, 5932, "Private Message 10"),
          new SiteChatConversationMessage(41, 5932, new Date(), null, 2302, "Private Message 11"),
          new SiteChatConversationMessage(42, 2302, new Date(), null, 5932, "Private Message 12"),
          new SiteChatConversationMessage(43, 5932, new Date(), null, 2302, "Private Message 13"),
          new SiteChatConversationMessage(44, 5932, new Date(), null, 2302, "Private Message 14")
      );
      
      queryUtil.batchInsert(statement.getConnection(), messages, queryUtil.getTableName(SiteChatConversationMessage.class), 1000, false);
      
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
      String mysqlUrl = provider.getMysqlUrl();
      String oldDatabaseName = provider.getDatabaseName();
      provider.setMysqlUrl(mysqlUrl.replace(oldDatabaseName, testDatabaseName));
      
      List<Class<?extends TestCase>> classes = new ArrayList<Class<?extends TestCase>>();
      classes.add(FirstTestCase.class);
      classes.add(ConversationsTestCase.class);
      classes.add(BatchInsertMessagesTestCase.class);
      classes.add(BannedUserIDSetTestCase.class);
      classes.add(LoadHistoryMessagesTestCase.class);
      classes.add(UserGroupsTestCase.class);
      
      for(Class<?extends TestCase> testCaseClass : classes) {
        
        try {
          
          provider.shutdownConnectionPool();
          provider.setupConnectionPool();
          
          populateTestTables(provider, oldDatabaseName, testDatabaseName);
          
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
