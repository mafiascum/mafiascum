package net.mafiascum.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class ConnectionInvocationHandler implements InvocationHandler {

  protected Connection connection;
  
  public ConnectionInvocationHandler(Connection connection) {
    
    this.connection = connection;
  }
  
  @Override
  public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
    
    System.out.println("INVOKE!");
    
    return null;
  }
  
  public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    
    Connection connection;
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    connection = DriverManager.getConnection
    (
      "jdbc:mysql://localhost/dev?useUnicode=yes&characterEncoding=UTF-8&jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&rewriteBatchedStatements=true",
      "root",
      ""
    );
    
    connection.setAutoCommit(false);
    
    ConnectionInvocationHandler connectionInvocationHandler = new ConnectionInvocationHandler(connection);
    
    Connection connectionProxy = (Connection) Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        connection.getClass().getInterfaces(),
        connectionInvocationHandler
      );
    
    connectionProxy.clearWarnings();
    
    System.out.println("Closing Main");
  }
}
