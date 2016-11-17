package net.mafiascum.jdbc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.mafiascum.enumerator.VEnum;
import net.mafiascum.util.SQLUtil;
import net.mafiascum.web.misc.Dual;

public class StoreSQLBuilder {
  protected List<Dual<String, String>> columns;
  protected List<Dual<String, String>> primaryKeys;
  protected String tableName;
  protected boolean insertIgnore;
  
  public StoreSQLBuilder(String tableName) {
    
    setColumns(new ArrayList<Dual<String, String>>());
    setPrimaryKeys(new ArrayList<Dual<String, String>>());
    this.tableName = tableName;
    setInsertIgnore(false);
  }
  
  public void setColumns(List<Dual<String, String>> columns) {
    
    this.columns = columns;
  }
  
  public void setPrimaryKeys(List<Dual<String, String>> primaryKeys) {
    
    this.primaryKeys = primaryKeys;
  }
  
  public StoreSQLBuilder putEscapedString(String columnName, String escapedString, List<Dual<String, String>> columns) {
    
    columns.add(new Dual<String, String>(columnName, escapedString));
    return this;
  }
  
  public StoreSQLBuilder putEscapedString(String columnName, String escapedString) {
    
    return putEscapedString(columnName, escapedString, columns);
  }
  
  public StoreSQLBuilder put(String columnName, String value) {
    
    return putEscapedString(columnName, SQLUtil.get().escapeQuoteString(value));
  }
  
  public StoreSQLBuilder put(String columnName, Integer value) {
    
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value));
  }
  
  public StoreSQLBuilder put(String columnName, int value) {
    
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public StoreSQLBuilder put(String columnName, boolean value) {
    
    return putEscapedString(columnName, SQLUtil.get().encodeBooleanInt(value));
  }

  public StoreSQLBuilder put(String columnName, Boolean value) {
    
    return putEscapedString(columnName, SQLUtil.get().encodeBooleanInt(value));
  }
  
  public StoreSQLBuilder put(String columnName, Long value) {
    
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value));
  }

  public StoreSQLBuilder put(String columnName, long value) {
    
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public StoreSQLBuilder put(String columnName, BigDecimal value) {
    
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public StoreSQLBuilder putMoney(String columnName, BigDecimal moneyValue) {
    
    return putEscapedString(columnName, moneyValue == null ? "NULL" : String.valueOf(moneyValue.movePointRight(2).intValue()));
  }
  
  public StoreSQLBuilder put(String columnName, java.sql.Date value) {
    return putEscapedString(columnName, (value != null) ? SQLUtil.get().encodeQuoteTimestamp(value) : "NULL");
  }
  
  public StoreSQLBuilder put(String columnName, Date value) {
    return putEscapedString(columnName, (value != null) ? SQLUtil.get().encodeQuoteDate(value) : "NULL");
  }
  
  public StoreSQLBuilder put(String columnName, LocalDateTime value) {
    return putEscapedString(columnName, (value != null) ? SQLUtil.get().encodeQuoteDate(value) : "NULL");
  }
  
  public StoreSQLBuilder put(String columnName, LocalDate value) {
    return putEscapedString(columnName, (value != null) ? SQLUtil.get().encodeQuoteDate(value) : "NULL");
  }
  
  public StoreSQLBuilder put(String columnName, LocalTime value) {
    return putEscapedString(columnName, (value != null) ? SQLUtil.get().encodeQuoteTime(value) : "NULL");
  }
  
  public StoreSQLBuilder put(String columnName, VEnum vEnum) {
    return putEscapedString(columnName, vEnum == null ? null : SQLUtil.get().escapeQuoteString(vEnum.toString()));
  }
  
  public StoreSQLBuilder putPrimaryKey(String columnName, Integer value) {
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value), primaryKeys);
  }
  
  public StoreSQLBuilder putPrimaryKey(String columnName, String value) {
    return putEscapedString(columnName, value == null ? "NULL" : SQLUtil.get().escapeQuoteString(value), primaryKeys);
  }
  
  public StoreSQLBuilder setInsertIgnore(boolean insertIgnore) {
    this.insertIgnore = insertIgnore;
    return this;
  }
  
  public String generateInsert() {
    
    StringBuilder stringBuilder = new StringBuilder();
    
    StringBuilder columnNameStringBuilder = new StringBuilder();
    StringBuilder columnValueStringBuilder = new StringBuilder();
    stringBuilder.append("INSERT ").append(insertIgnore ? "IGNORE " : "").append("INTO `").append(tableName.replace("`", "")).append("`(");
    
    boolean isFirst = true;
    for(Dual<String, String> columnDual : columns) {
      
      columnNameStringBuilder.append(isFirst ? "" : ",").append("`").append(columnDual.getObject1()).append("`");
      columnValueStringBuilder.append(isFirst ? "" : ",").append(columnDual.getObject2());
      isFirst = false;
    }
    
    stringBuilder.append(columnNameStringBuilder.toString())
                 .append(")VALUES(")
                 .append(columnValueStringBuilder.toString())
                 .append(")");
    
    return stringBuilder.toString();
  }
  
  public String generateUpdate() throws SQLException {
    
    return generateUpdate(generateUpdateWhereClause());
  }
  
  public String generateUpdate(String whereClause) {
    
    StringBuilder stringBuilder = new StringBuilder();
    
    stringBuilder.append("UPDATE `" + tableName.replace("`", "") + "` SET ");
    
    boolean isFirst = true;
    for(Dual<String, String> columnDual : columns) {
      
      stringBuilder.append(isFirst ? "" : ",").append("`").append(columnDual.getObject1()).append("`=").append(columnDual.getObject2());
      isFirst = false;
    }
    
    stringBuilder.append(" WHERE " + (whereClause == null ? "1" : whereClause));
    return stringBuilder.toString();
  }
  
  public String generateUpdateWhereClause() throws SQLException {
    
    if(primaryKeys == null || primaryKeys.isEmpty()) {
      
      throw new SQLException("No primary key(s) set.");
    }
    
    StringBuilder stringBuilder = new StringBuilder();
    Iterator<Dual<String, String>> columnIter = primaryKeys.iterator();
    while(columnIter.hasNext()) {
      
      Dual<String, String> column = columnIter.next();
      
      stringBuilder.append('`').append(column.getObject1()).append('`');
      
      if(column.getObject2().equals("NULL"))
        stringBuilder.append("IS NULL");
      else
        stringBuilder.append('=').append(column.getObject2());
      
      if(columnIter.hasNext()) {
        stringBuilder.append(" AND ");
      }
    }
    
    return stringBuilder.toString();
  }

  public boolean isInsertIgnore() {
    return insertIgnore;
  }
}