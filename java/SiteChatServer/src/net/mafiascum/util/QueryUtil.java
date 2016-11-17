package net.mafiascum.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.mafiascum.functional.ConsumerWithException;
import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.jdbc.BatchInsertable;
import net.mafiascum.jdbc.ConnectionExecutor;
import net.mafiascum.jdbc.ConnectionExecutorNoResult;
import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StatementExecutor;
import net.mafiascum.jdbc.StatementExecutorNoResult;
import net.mafiascum.jdbc.Table;
import net.mafiascum.provider.Provider;

/** Utility methods for working with database queries. */
public class QueryUtil extends MSUtil {
  
  private static QueryUtil INSTANCE;
  
  private QueryUtil() {
    
  }
  
  public static synchronized QueryUtil get() {
    
    if(INSTANCE == null) {
      INSTANCE = new QueryUtil();
      INSTANCE.init();
    }
    
    return INSTANCE;
  }

  /**
   * Closes a connection hiding any SQLException thrown.  Use with caution.
   * Null connections are ignored.
   */
  public void closeNoThrow (Connection connection) {
    if (connection == null)
      return;

    try {
      connection.close();
    }
    catch (SQLException sqle) {
      // Ignore - as requested
    }
  }

  /**
   * Rolls-back a connection hiding any SQLException thrown.  Use with caution.
   * If an exception is thrown, the method tries to call closeNoThrow on the connection.  
   * Null connections are ignored.
   */
  public void rollbackNoThrow (Connection connection) {
    if (connection == null)
      return;

    try {
      connection.rollback();
    }
    catch (SQLException sqle) {
      // Try to close the connection and then ignore - as requested
      closeNoThrow(connection);
    }
  }

  /**
   * Closes a statement hiding any SQLException thrown.  Use with extreme caution.
   * Null statement are ignored.
   */
  public void closeNoThrow (Statement statement) {
    if (statement == null)
      return;

    try {
      statement.close();
    }
    catch (SQLException sqle) {
      // Ignore - as requested
    }
  }
  
  public void closeNoThrow (ResultSet resultSet) {
    
    if(resultSet == null)
      return;
    
    try {
      resultSet.close();
    }
    catch(SQLException sqlException) {
      //Ignore.
    }
  }

  /**
   * Execute the specified update statement on the specified connection.  This
   * method creates a statement, executes the sql using that statement, then
   * closes the statement.<br>
   * <br>
   * NOTE: This should not be used in situations where multiple SQL calls could
   * be made with a single Statement instance.
   */
  public int executeUpdate (Connection connection, String sql) throws SQLException {
    Statement statement = connection.createStatement();
    int result = statement.executeUpdate(sql);
    statement.close();
    return result;
  }

  /**
   * Used to check the results of an existance-query that depends on zero or
   * one rows being present in a result-set.  Generally used with queries
   * like 'SELECT 1 FROM table WHERE id = value' to see if the row with an
   * id that matches the specified value exists.<br>
   * <br>
   * NOTE: This method throws an exception if more than one rows exist.
   */
  public boolean hasSingleResultRow (Statement statement, String sql) throws SQLException {
    ResultSet rs = statement.executeQuery(sql);

    boolean hasResultRow = rs.next();

    if (rs.next())
      throw new SQLException("Invalid number of rows: multiple");

    return hasResultRow;
  }

  /**
   * Used to check the results of an existance-query that depends on zero or
   * one or more rows being present in a result-set.  Generally used with queries
   * like 'SELECT 1 FROM table WHERE id = value' to see if the row with an
   * id that matches the specified value exists.<br>
   */
  public boolean hasAtLeastOneRow (Statement statement, String sql) throws SQLException {
    ResultSet resultSet = null;
    
    try {
      resultSet = statement.executeQuery(sql);

      boolean hasResultRow = resultSet.next();
    
      resultSet.close();

      return hasResultRow;
    }
    finally {
      
      closeNoThrow(resultSet);
    }
  }

  /**
   * Performs the specified query and returns the result.  The query should
   * produce a single row containing a single integer value.  If the value
   * is null, zero will be returned.
   */
  public int getSingleIntValueResult (Statement statement, String sql) throws SQLException {
    ResultSet rs = statement.executeQuery(sql);

    if (!rs.next())
      throw new SQLException("Invalid number of rows: none");

    int value = rs.getInt(1);

    if (rs.next())
      throw new SQLException("Invalid number of rows: multiple");

    rs.close();

    return value;
  }
  
  public int getSingleIntValueResult(Connection connection, String sql) throws SQLException {
    return executeStatement(connection, statement -> getSingleIntValueResult(statement, sql));
  }

  /**
   * Performs the specified query and returns the result.  The query should
   * produce a single row containing a single long integer value.  If the value
   * is null, zero will be returned.
   */
  public long getSingleLongValueResult (Statement statement, String sql) throws SQLException {
    ResultSet rs = statement.executeQuery(sql);

    if (!rs.next())
      throw new SQLException("Invalid number of rows: none");

    long value = rs.getLong(1);

    if (rs.next())
      throw new SQLException("Invalid number of rows: multiple");

    rs.close();

    return value;
  }

  /**
   * Performs the specified query and returns the result.  The query should
   * produce a single row containing a single integer-boolean value.  If the
   * value is null, false will be returned.
   */
  public boolean getSingleIntBooleanValueResult (Statement statement, String sql) throws SQLException {
    ResultSet rs = statement.executeQuery(sql);

    if (!rs.next())
      throw new SQLException("Invalid number of rows: none");

    boolean value = getIntBoolean(rs, 1);

    if (rs.next())
      throw new SQLException("Invalid number of rows: multiple");

    rs.close();

    return value;
  }

  /** Retrieves the last inserted auto-increment id created within the current transaction. */
  public int getLastInsertedID (Connection connection) throws SQLException {
    Statement statement = null;
    try {
      statement = connection.createStatement();

      int id = getLastInsertedID(statement);

      statement.close();
      statement = null;

      return id;
    }
    finally {
      closeNoThrow(statement);
    }
  }


  /** Retrieves the last inserted auto-increment id created within the current transaction. */
  public int getLastInsertedID (Statement statement) throws SQLException {
    return getSingleIntValueResult(statement, "SELECT LAST_INSERT_ID()");
  }

  /** Retrieves the last inserted auto-increment id created within the current transaction. */
  public long getLastInsertedLongID (Statement statement) throws SQLException {
    return getSingleLongValueResult(statement, "SELECT LAST_INSERT_ID()");
  }

  /** Retrieves the last inserted auto-increment id created within the current transaction. */
  public int getCheckLastInsertedLongIDAsInt (Statement statement) throws SQLException {
    long longValue = getLastInsertedLongID(statement);

    int intValue = (int) longValue;
    if (intValue != longValue)
      throw new SQLException("Found long ID that cannot fit within int constraints");

    return intValue;
  }

  /** Retrieves the current date-time according to the database. */
  public Date getCurrentDatabaseDateTime (Connection connection) throws SQLException {
    Statement statement = connection.createStatement();
    Date date = getCurrentDatabaseDateTime(statement);
    statement.close();
    return date;
  }

  /** Retrieves the current date-time according to the database. */
  public Date getCurrentDatabaseDateTime (Statement statement) throws SQLException {
    ResultSet resultSet = statement.executeQuery(
      "SELECT NOW()"
    );
    resultSet.next();
    Date date = resultSet.getTimestamp(1);
    resultSet.close();
    return date;
  }

  public int getCheckLongIDAsInt (ResultSet rs, String fieldName) throws SQLException {
    long longValue = rs.getLong(fieldName);

    int intValue = (int) longValue;
    if (intValue != longValue)
      throw new SQLException("Found long ID that cannot fit within int constraints");

    return intValue;
  }

  /** Retrieves a possibly null integer values. */
  public Integer getInteger (ResultSet rs, String fieldName) throws SQLException {
    int value = rs.getInt(fieldName);
    return rs.wasNull() ? null : new Integer(value);
  }

  /** Retrieves a possibly null integer values. */
  public Integer getInteger (ResultSet rs, int fieldIndex) throws SQLException {
    int value = rs.getInt(fieldIndex);
    return rs.wasNull() ? null : new Integer(value);
  }

  /** Retrieves a possibly null short values. */
  public Short getShort (ResultSet rs, String fieldName) throws SQLException {
    short value = rs.getShort(fieldName);
    return rs.wasNull() ? null : new Short(value);
  }

  /** Retrieves a possibly null short values. */
  public Short getShort (ResultSet rs, int fieldIndex) throws SQLException {
    short value = rs.getShort(fieldIndex);
    return rs.wasNull() ? null : new Short(value);
  }

  /** Retrieves a possibly null long values. */
  public Long getLong (ResultSet rs, String fieldName) throws SQLException {
    long value = rs.getLong(fieldName);
    return rs.wasNull() ? null : new Long(value);
  }

  /** Retrieves a possibly null long values. */
  public Long getLong (ResultSet rs, int fieldIndex) throws SQLException {
    long value = rs.getLong(fieldIndex);
    return rs.wasNull() ? null : new Long(value);
  }

  /** Retrieves a BigDecimal value from a fixed (scale = 2) int field. */
  public BigDecimal getFixedIntBigDecimal (ResultSet rs, String fieldName) throws SQLException {
    int value = rs.getInt(fieldName);
    return BigDecimal.valueOf(value, 2);
  }

  /** Retrieves a BigDecimal value from a fixed (scale = 2) int field. */
  public BigDecimal getFixedIntegerBigDecimal (ResultSet rs, String fieldName) throws SQLException {
    Integer value = getInteger(rs, fieldName);
    if(value != null) {
      return BigDecimal.valueOf(value, 2);
    }
    else {
      return null;
    }
  }

  /** Retrieves a possibly null double values. */
  public Double getDouble (ResultSet rs, String fieldName) throws SQLException {
    double value = rs.getDouble(fieldName);
    return rs.wasNull() ? null : new Double(value);
  }

  /** Retrieves a possibly null boolean interpreted integer value. */
  public Boolean getPossiblyNullIntBoolean (ResultSet rs, String fieldName) throws SQLException {
    int value = rs.getInt(fieldName);
    return (rs.wasNull() ? null : value != 0);
  }

  /** Retrieves a possibly null boolean interpreted integer value. */
  public Boolean getPossiblyNullIntBoolean (ResultSet rs, int fieldIndex) throws SQLException {
    int value = rs.getInt(fieldIndex);
    return (rs.wasNull() ? null : value != 0);
  }
  
  /** Retrieves a boolean interpreted integer value. */
  public boolean getIntBoolean (ResultSet rs, String fieldName) throws SQLException {
    return (rs.getInt(fieldName) != 0);
  }

  /** Retrieves a boolean interpreted integer value. */
  public boolean getIntBoolean (ResultSet rs, int fieldIndex) throws SQLException {
    return (rs.getInt(fieldIndex) != 0);
  }

  /** Retrieves a possibly null boolean interpreted integer value. */
  public Boolean getIntBooleanObj (ResultSet rs, String fieldName) throws SQLException {
    boolean value = (rs.getInt(fieldName) != 0);
    return rs.wasNull() ? null : Boolean.valueOf(value);
  }

  /** Retrieves a possibly null enum calue. */
  public <Type extends Enum<Type>> Type getEnum (ResultSet rs, String fieldName, Class<Type> enumClass, boolean upperCaseStr) throws SQLException {
    String value = rs.getString(fieldName);

    if(upperCaseStr && value != null) {
      value = value.toUpperCase();
    }

    try {
      return (Type) ((value != null)
                       ? Enum.valueOf(enumClass, value)
                       : null);
    }
    catch (IllegalArgumentException iae) {
      SQLException sqle = new SQLException("Invalid value (\"" + value + "\") for enum " + enumClass.getName());
      sqle.initCause(iae);
      throw sqle;
    }
  }
  
  /** Performs the update, then checks to make sure the specified number of rows were affected. */
  public void updateCheckRowCount (Statement statement, String sql, int expectedRowCow) throws SQLException {
    executeUpdateCheckRowCount(statement, sql, expectedRowCow);
  }

  /** Performs the update, then checks to make sure the specified number of rows were affected. */
  public void executeUpdateCheckRowCount (Statement statement, String sql, int expectedRowCow) throws SQLException {
    int rowCount = statement.executeUpdate(sql);
    if (rowCount != expectedRowCow)
      throw new SQLException("Update row count does not match expected row count (" + rowCount + " != " + expectedRowCow + ")");
  }

  /**
   * Executes the SQL, checks that one-and-only-one row was inserted, then
   * retrieves and returns the last inserted ID.
   */
  public int executeInsertGetLastInsertedIntID (Statement statement, String sql) throws SQLException {
    updateCheckRowCount(statement, sql, 1);
    return getLastInsertedID(statement);
  }

  /**
   * Delete a set of rows in chunks - committing each chunk.<br>
   * <br>
   * NOTE: If the call fails the data may be left partially deleted.
   * 
   * @param connection The connection to use / commit.
   * @param deleteSQL The sql to use to perform the deletes - note: must allow a limit to be attached.
   * @param chunkSize The number of rows to delete at a time.
   */
  public void deleteChunkCommitLoop (Connection connection, String deleteSQL, int chunkSize) throws SQLException {
    while (true) {
      
      // Delete a chunk of rows and commit

      Statement statement = connection.createStatement();

      int numRowsAffected = statement.executeUpdate(
        deleteSQL + " LIMIT " + chunkSize
      );

      statement.close();
      connection.commit();

      // Exit if we deleted the last set of rows

      if (numRowsAffected < chunkSize)
        break;
    }
  }

  public void insertSelectChunkCommitLoop (Connection connection, String insertPortion, String selectPortion, String fromAndWherePortion, int chunkSize) throws SQLException {
    insertSelectChunkCommitLoop(connection, insertPortion, selectPortion, fromAndWherePortion, chunkSize, true);
  }

  public void insertSelectChunkCommitLoop (Connection connection, String insertPortion, String selectPortion, String fromAndWherePortion, int chunkSize, boolean commit) throws SQLException {

    Statement statement = connection.createStatement();
    int totalRows = getSingleIntValueResult(statement, "SELECT COUNT(*) " + fromAndWherePortion);
    statement.close();

    int startRow = 0;
    while (startRow < totalRows) {
      int updateCount = Math.min(totalRows - startRow, chunkSize);

      // Insert a chunk of rows and commit

      statement = connection.createStatement();

      updateCheckRowCount(
        statement,
        insertPortion
          + " " + selectPortion
          + " " + fromAndWherePortion
          + " LIMIT " + startRow + ", " + updateCount,
        updateCount
      );

      statement.close();
      
      if(commit) {
        connection.commit();
      }

      startRow += updateCount;
    }
  }

  public void keyedInsertSelectChunkCommitLoop (
    Connection connection,
    String keySelectKeyField, String keySelectTableList, String keySelectJoins, String keySelectWhereCriteria,
    String insertPortion, String selectPortion, String selectTables, String selectJoins, String whereCriteria, String selectKeyField, int chunkSize, String tempTableName) throws SQLException
  {
    // Setup

    Statement statement = connection.createStatement();

    statement.executeUpdate(
      "DROP TABLE IF EXISTS " + tempTableName
    );

    statement.executeUpdate(
      "SET @KeyRowIndex = -1"
    );

    statement.executeUpdate(
      "CREATE TABLE " + tempTableName + " AS"
      + " SELECT @KeyRowIndex := @KeyRowIndex + 1 tempInsertSelectKeyIndex_index, " + keySelectKeyField + " tempInsertSelectKeyIndex_key"
      + " FROM (" + keySelectTableList + ")"
      + ((keySelectJoins != null) ? " " + keySelectJoins : "")
      + ((keySelectWhereCriteria != null) ? " WHERE " + keySelectWhereCriteria : "")
    );

    int totalRows = getSingleIntValueResult(statement, "SELECT @KeyRowIndex") + 1;

    statement.executeUpdate(
      "CREATE INDEX orderIndex ON " + tempTableName + " (tempInsertSelectKeyIndex_index)"
    );

    statement.close();
    connection.commit();

    // Perform insert-select chunks

    int startKeyRow = 0;
    while (startKeyRow < totalRows) {
      int keyRowCount = Math.min(totalRows - startKeyRow, chunkSize);
      int limitKeyRow = startKeyRow + keyRowCount;

      executeUpdate(
        connection,
        insertPortion
          + " " + selectPortion
          + " FROM (" + tempTableName + ", " + selectTables + ")"
          + ((selectJoins != null) ? " " + selectJoins : "")
          + " WHERE (" + startKeyRow + " <= tempInsertSelectKeyIndex_index AND tempInsertSelectKeyIndex_index < " + limitKeyRow + ")"
          + " AND " + selectKeyField + " = tempInsertSelectKeyIndex_key"
          + " AND (" + whereCriteria + ")"
      );

      connection.commit();

      startKeyRow += keyRowCount;
    }

    // Cleanup

    executeUpdate(
      connection,
      "DROP TABLE " + tempTableName
    );

    connection.commit();
  }

  public void deleteChunkCommitLoop (Connection connection, String deleteTableList, String usingTableList, String whereCriteria, String keyField, int chunkSize, String tempTableName) throws SQLException {
    performUpdateChunkCommitLoop(
      connection, usingTableList, whereCriteria, keyField, chunkSize, tempTableName,
      "DELETE FROM " + deleteTableList
      + " USING " + tempTableName + ", " + usingTableList
    );
  }

  public int updateChunkCommitLoop (Connection connection, String tableList, String whereCriteria, String keyField, String updateAssignments, int chunkSize, String tempTableName) throws SQLException {
    return performUpdateChunkCommitLoop(
      connection, tableList, whereCriteria, keyField, chunkSize, tempTableName,
      "UPDATE " + tempTableName + ", " + tableList
      + " SET " + updateAssignments
    );
  }

  private int performUpdateChunkCommitLoop (Connection connection, String tableList, String whereCriteria, String keyField, int chunkSize, String tempTableName, String updatePrefixSQL) throws SQLException {

    // Setup an index table to drive the delete

    Statement statement = connection.createStatement();

    statement.executeUpdate(
      "DROP TABLE IF EXISTS " + tempTableName
    );

    statement.executeUpdate(
      "SET @RowIndex = -1"
    );

    statement.executeUpdate(
      "CREATE TABLE " + tempTableName + " AS"
      + " SELECT @RowIndex := @RowIndex + 1 tempUpdateIndex_index, " + keyField + " tempUpdateIndex_key"
      + " FROM " + tableList
      + " WHERE " + whereCriteria
    );

    int totalRows = getSingleIntValueResult(statement, "SELECT @RowIndex") + 1;

    statement.executeUpdate(
      "CREATE INDEX row_id ON " + tempTableName + " (tempUpdateIndex_index)"
    );

    statement.close();
    connection.commit();

    // Update chunks of rows, committing each set, until all the rows are updated

    int startRow = 0;
    while (startRow < totalRows) {
      int updateCount = Math.min(totalRows - startRow, chunkSize);
      int limitRow = startRow + updateCount;

      statement = connection.createStatement();

      updateCheckRowCount(
        statement,
        updatePrefixSQL
        + " WHERE (" + startRow + " <= tempUpdateIndex_index AND tempUpdateIndex_index < " + limitRow + ")"
        + " AND " + keyField + " = tempUpdateIndex_key"
        + " AND (" + whereCriteria + ")",
        updateCount
      );

      statement.close();
      connection.commit();

      startRow += updateCount;
    }

    // Cleanup

    statement = connection.createStatement();

    statement.executeUpdate(
      "DROP TABLE " + tempTableName
    );

    statement.close();
    connection.commit();

    return totalRows;
  }

  /** Checks if the specified exception represents a duplicate key error. */
  // NOTE: THIS METHOD MUST BE KEPT IN SYNC WITH THE JDBC DRIVER VERSION!
  public boolean isDuplicateKeyException (SQLException sqle) {
    String message = sqle.getMessage();
    return message.indexOf("Duplicate entry") >= 0
        && message.indexOf("for key") >= 0;
  }

  /** Checks if the specified exception represents a table exists error. */
  // NOTE: THIS METHOD MUST BE KEPT IN SYNC WITH THE JDBC DRIVER VERSION!
  public boolean isTableExistsException (SQLException sqle) {
    String message = sqle.getMessage();
    return message.indexOf("Table ") >= 0
           && message.indexOf(" already exists") >= 0;
  }

  /** Checks if the specified exception represents a lock-wait timeout. */
  // NOTE: THIS METHOD MUST BE KEPT IN SYNC WITH THE JDBC DRIVER VERSION!
  public boolean isLockWaitTimeoutException (SQLException sqle) {
    return sqle.getErrorCode() == 1205
        && sqle.getMessage().indexOf("Lock wait timeout") >= 0;
  }

  public List<String> buildListFromString (String value) {
    return stringUtil.buildListFromString(value, stringUtil.MYSQL_SEPERATOR_SEQUENCE_ESCAPED);
  }
  
  public String buildStringFromList(List<String> list) {
    return stringUtil.buildStringFromList(list, stringUtil.MYSQL_STORAGE_SEPERATOR_SEQUENCE);
  }
  
  public Set<Integer> getIntegerSet(Statement statement, String sql, String columnName) throws SQLException {
    
    Set<Integer> integerSet = new HashSet<Integer>();
    
    return getIntegerCollection(statement, sql, columnName, integerSet);
  }
  
  public Set<Integer> getIntegerSet(Connection connection, String sql, String columnName) throws SQLException {
    
    Set<Integer> integerSet = new HashSet<Integer>();
    
    return executeStatement(connection, statement -> getIntegerCollection(statement, sql, columnName, integerSet));
  }
  
  public ArrayList<Integer> getIntegerArrayList(Statement statement, String sql, String columnName) throws SQLException {
    
    ArrayList<Integer> integerSet = new ArrayList<Integer>();
    
    return getIntegerCollection(statement, sql, columnName, integerSet);
  }
  
  public <CollectionType extends Collection<Integer>> CollectionType getIntegerCollection(Statement statement, String sql, String columnName, CollectionType collection) throws SQLException {
    
    ResultSet resultSet = statement.executeQuery(sql);
    
    while(resultSet.next()) {
      
      collection.add(resultSet.getInt(columnName));
    }
    
    resultSet.close();
    
    return collection;
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectList(Connection connection, String criteria, Class<T> dbObjectClass) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectList(statement, criteria, dbObjectClass));
  }

  public <T extends DataObject> List<T> retrieveDataObjectList(Connection connection, String criteria, Class<T> dbObjectClass, boolean forUpdate) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectList(statement, criteria, null, null, dbObjectClass, forUpdate));
  }

  public <T extends DataObject> List<T> retrieveDataObjectList(Statement statement, String criteria, Class<T> dbObjectClass) throws SQLException {
    return retrieveDataObjectList(statement, criteria, null, null, dbObjectClass, false);
  }

  public <T extends DataObject> List<T> retrieveDataObjectList(Connection connection, String criteria, String orderBy, Class<T> dbObjectClass) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectList(statement, criteria, orderBy, null, dbObjectClass, false));
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectList(Connection connection, String criteria, String orderBy, String limit, Class<T> dbObjectClass, boolean forUpdate) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectList(statement, criteria, orderBy, limit, dbObjectClass, forUpdate));
  }

  public <T extends DataObject> List<T> retrieveDataObjectList(Statement statement, String criteria, String orderBy, Class<T> dbObjectClass) throws SQLException {
    return retrieveDataObjectList(statement, criteria, orderBy, null, dbObjectClass, false);
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectList(Statement statement, String criteria, String orderBy, String limit, Class<T> dbObjectClass, boolean forUpdate) throws SQLException {
    
    String escapedTableName = getEscapedTableName(dbObjectClass);
    
    if(escapedTableName == null) {
      
      throw new SQLException("No table name found for class `" + dbObjectClass.getName() + "`");
    }
    
    String sql = " SELECT *"
               + " FROM " + escapedTableName
               + " WHERE " + (criteria == null ? "1" : criteria)
               + (orderBy == null ? "" : (" ORDER BY " + orderBy))
               + (limit == null ? "" : (" LIMIT " + limit));
    
    return retrieveDataObjectListBySql(statement, sql, dbObjectClass);
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectListBySql(Connection connection, String sql, Class<T> dbObjectClass) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectListBySql(statement, sql, dbObjectClass));
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectListBySql(Statement statement, String sql, Class<T> dbObjectClass) throws SQLException {
    
    ResultSet resultSet = statement.executeQuery(sql);
    return retrieveDataObjectList(resultSet, dbObjectClass);
  }
  
  public <T extends DataObject> List<T> retrieveDataObjectList(ResultSet resultSet, Class<T> dbObjectClass) throws SQLException {
    
    List<T> dbObjectList = new ArrayList<T>();
    
    while(resultSet.next()) {
      
      dbObjectList.add(retrieveDataObject(resultSet, dbObjectClass));
    }
    
    resultSet.close();
    
    return dbObjectList;
  }
  
  public <KeyType, PassingType extends DataObject> Map<KeyType, PassingType> retrieveDataObjectMap(Connection connection, String criteria, Class<PassingType> passingTypeClass, Function<PassingType, KeyType> keyAccessor) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectMap(statement, criteria, passingTypeClass, keyAccessor));
  }
  
  public <KeyType, PassingType extends DataObject> Map<KeyType, PassingType> retrieveDataObjectMapBySql(Connection connection, String sql, Class<PassingType> passingTypeClass, Function<PassingType, KeyType> keyAccessor) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObjectMapBySql(statement, sql, passingTypeClass, keyAccessor));
  }
  
  public <KeyType, PassingType extends DataObject> Map<KeyType, PassingType> retrieveDataObjectMapBySql(Statement statement, String sql, Class<PassingType> passingTypeClass, Function<PassingType, KeyType> keyAccessor) throws SQLException {
    ResultSet resultSet = statement.executeQuery(sql);
    return retrieveDataObjectMap(resultSet, passingTypeClass, keyAccessor);
  }
  
  public <KeyType, PassingType extends DataObject> Map<KeyType, PassingType> retrieveDataObjectMap(Statement statement, String criteria, Class<PassingType> passingTypeClass, Function<PassingType, KeyType> keyAccessor) throws SQLException {
    
    String escapedTableName = getEscapedTableName(passingTypeClass);
    
    if(escapedTableName == null) {
      
      throw new SQLException("No table name found for class `" + passingTypeClass.getName() + "`");
    }
    
    String sql = " SELECT *"
               + " FROM " + escapedTableName
               + " WHERE " + (criteria == null ? "1" : criteria);

    return retrieveDataObjectMapBySql(statement, sql, passingTypeClass, keyAccessor);
  }
  
  public <KeyType, PassingType extends DataObject> Map<KeyType, PassingType> retrieveDataObjectMap(ResultSet resultSet, Class<PassingType> passingTypeClass, Function<PassingType, KeyType> keyAccessor) throws SQLException {
    
    Map<KeyType, PassingType> map = new HashMap<KeyType, PassingType>();
    
    while(resultSet.next()) {
    
      PassingType passingType = retrieveDataObject(resultSet, passingTypeClass);
      map.put(keyAccessor.apply(passingType), passingType);
    }
    
    resultSet.close();
  
    return map;
  }

  public <T extends DataObject> T retrieveDataObject(Connection connection, String criteria, Class<T> dbObjectClass) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObject(statement, criteria, dbObjectClass));
  }
  
  public <T extends DataObject> T retrieveDataObject(Connection connection, String criteria, Class<T> dbObjectClass, boolean forUpdate) throws SQLException {
    return executeStatement(connection, statement -> retrieveDataObject(statement, criteria, dbObjectClass, forUpdate));
  }
  
  public <T extends DataObject> T retrieveDataObject(Statement statement, String criteria, Class<T> dbObjectClass) throws SQLException {
    return retrieveDataObject(statement, criteria, dbObjectClass, false);
  }
  
  public <T extends DataObject> T retrieveDataObject(Statement statement, String criteria, Class<T> dbObjectClass, boolean forUpdate) throws SQLException {
    
    String escapedTableName = getEscapedTableName(dbObjectClass);
    
    if(escapedTableName == null) {
      throw new SQLException("No table name found for class `" + dbObjectClass.getName() + "`");
    }
    
    String sql = " SELECT *"
               + " FROM " + escapedTableName
               + " WHERE " + (criteria == null ? "1" : criteria);
    
    if(forUpdate) {
      sql += " FOR UPDATE";
    }
    
    return retrieveDataObjectBySql(statement, sql, dbObjectClass);
  }

  public <T extends DataObject> T retrieveDataObjectBySql(Connection connection, String sql, Class<T> dbObjectClass) throws SQLException {
    
    return executeStatement(connection, statement -> retrieveDataObjectBySql(statement, sql, dbObjectClass));
  }

  public <T extends DataObject> T retrieveDataObjectBySql(Statement statement, String sql, Class<T> dbObjectClass) throws SQLException {
    
    ResultSet resultSet = statement.executeQuery(sql);
    T dataObject = null;
    try {
    if(resultSet.next()) {
      
      dataObject = retrieveDataObject(resultSet, dbObjectClass);
    }
    }
    catch(NullPointerException sqlException) {
      sqlException.printStackTrace();
      throw sqlException;
    }
    
    resultSet.close();
    
    return dataObject;
  }

  public ResultSet retrieveResultSetBySql(Statement statement, String sql) throws SQLException {
    
    ResultSet resultSet = statement.executeQuery(sql);
    
    return resultSet;
  }
  
  public <T extends DataObject> T retrieveDataObject(ResultSet resultSet, Class<T> dbObjectClass) throws SQLException {
    
    try {
      T dbObject = dbObjectClass.newInstance();
    
      dbObject.loadFromResultSet(resultSet);

      return dbObject;
    }
    catch(Exception exception) {
      
      if(exception instanceof SQLException) {
        
        throw (SQLException)exception;
      }
      
      throw new SQLException(exception);
    }
  }
  
  public <T> T executeConnection(Provider provider, ConnectionExecutor<T> connectionExecutor) throws SQLException {
    
    Connection connection = null;
    
    try {
      
      connection = provider.getConnection();
      
      T result = connectionExecutor.execute(connection);
      
      connection.commit();
      connection.close();
      connection = null;
      
      return result;
    }
    catch(Exception exception) {
      
      rollbackNoThrow(connection);
      throw exception instanceof SQLException ? (SQLException)exception : new SQLException(exception);
    }
    finally {
      
      closeNoThrow(connection);
    }
  }
  
  public void executeConnectionNoResult(Provider provider, ConnectionExecutorNoResult connectionExecutor) throws SQLException {
    
    executeConnection(provider, connection -> {
      connectionExecutor.execute(connection);
      return null;
    });
  }
  
  public <T> T executeStatement(Provider provider, StatementExecutor<T> statementExecutor) throws SQLException {
    
    Connection connection = null;
    
    try {
      
      connection = provider.getConnection();
      
      T result = executeStatement(connection, statementExecutor);
      
      connection.commit();
      connection.close();
      connection = null;
      
      return result;
    }
    catch(Exception exception) {
      
      rollbackNoThrow(connection);
      throw exception instanceof SQLException ? (SQLException)exception : new SQLException(exception);
    }
    finally {
      
      closeNoThrow(connection);
    }
  }
  
  public void executeStatementNoResult(Provider provider, StatementExecutorNoResult statementExecutor) throws SQLException {
    
    executeStatement(provider, statement -> {
      
      statementExecutor.execute(statement);
      return null;
    });
  }
  
  public <T> T executeStatement(Connection connection, StatementExecutor<T> statementExecutor) throws SQLException {
    
    Statement statement = null;
    
    try {
      
      statement = connection.createStatement();
      
      T result = statementExecutor.execute(statement);
      
      statement.close();
      statement = null;
      
      return result;
    }
    catch(Exception exception) {
      throw exception instanceof SQLException ? (SQLException)exception : new SQLException(exception);
    }
    finally {
      
      closeNoThrow(statement);
    }
  }
  
  public void executeStatementNoResult(Connection connection, StatementExecutorNoResult statementExecutor) throws SQLException {
    
    executeStatement(connection, statement -> {
      
      statementExecutor.execute(statement);
      return null;
    });
  }
  
  public void forEachResultSet(Connection connection, String sql, ConsumerWithException<ResultSet> consumer) throws SQLException {
    
    executeStatementNoResult(connection, statement -> {
      
      ResultSet resultSet = statement.executeQuery(sql);
      while(resultSet.next())
        consumer.accept(resultSet);
      resultSet.close();
    });
  }
  
  public <T> String getEscapedTableName(Class<T> dbObjectClass) {
    Table table = dbObjectClass.getAnnotation(Table.class);
    if(table == null)
      return null;
    if(table.schema() != null && !table.schema().isEmpty())
      return sqlUtil.escapeQuoteColumnName(table.schema()) + "." + sqlUtil.escapeQuoteColumnName(table.tableName());
    return sqlUtil.escapeQuoteColumnName(table.tableName());    
  }
  
  public <T> String getTableName(Class<T> dbObjectClass) {
    
    Table table = dbObjectClass.getAnnotation(Table.class);
    return table == null ? null : table.tableName();    
  }
  
  public void batchInsert(Connection connection, Collection<?extends BatchInsertable> batchInsertableCollection, String tableName, int batchSize, boolean insertIgnore) throws SQLException {
    
    if(batchInsertableCollection.isEmpty())
      return;
    
    BatchInsertStatement batchInsertStatement = new BatchInsertStatement(connection, tableName, batchSize, insertIgnore);
    
    batchInsertableCollection.iterator().next().setBatchInsertStatementColumns(batchInsertStatement);
    batchInsertStatement.start();
    
    for(BatchInsertable batchInsertable : batchInsertableCollection) {
      
      batchInsertable.addToBatchInsertStatement(batchInsertStatement);
    }
    
    batchInsertStatement.finish();
  }
  
  public LocalDateTime getLocalDateTime(ResultSet resultSet, String columnName) throws SQLException {
    Timestamp timestamp = resultSet.getTimestamp(columnName);
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }
  
  public LocalDate getLocalDate(ResultSet resultSet, String columnName) throws SQLException {
    Timestamp timestamp = resultSet.getTimestamp(columnName);
    return timestamp == null ? null : timestamp.toLocalDateTime().toLocalDate();
  }
  
  public LocalTime getLocalTime(ResultSet resultSet, String columnName) throws SQLException {
    Time time = resultSet.getTime(columnName);
    return time == null ? null : time.toLocalTime();
  }
}