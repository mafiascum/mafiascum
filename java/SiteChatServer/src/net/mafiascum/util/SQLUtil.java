package net.mafiascum.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.mafiascum.enumerator.VEnum;

/** SQL / JDBC utility methods. */
public class SQLUtil extends MSUtil {
  
  private static SQLUtil INSTANCE;
  
  private SQLUtil() {
    
  }
  
  public static synchronized SQLUtil get() {
    
    if(INSTANCE == null) {
      INSTANCE = new SQLUtil();
      INSTANCE.init();
    }
    
    return INSTANCE;
  }
  
  /** SQL representing the true boolean-int value. */
  public final String TRUE_BOOLINTSQL = "1";

  /** SQL representing the false boolean-int value. */
  public final String FALSE_BOOLINTSQL = "0";

  /** Escapes a string for use in an SQL statement. */
  public String escapeString (String text) {
    return text.replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
  }

  /** 
   * Escapes and quotes a string for use in an SQL statement.
   * If the string is null, "NULL" is returned instead.
   */
  public String escapeQuoteString (String text) {
    return (text != null) ? "'" + escapeString(text) + "'" : "NULL";
  }

  /** Encodes and quotes the timestamp for use in an SQL statement. */
  public String encodeQuoteTimestamp (java.util.Date timestamp) {
    return encodeQuoteDate(timestamp);
  }

  /** Encodes and quotes the timestamp for use in an SQL statement. */
  public String encodeQuoteDate (java.util.Date timestamp) {
    return (timestamp != null)
                ? new SimpleDateFormat("''yyyyMMddHHmmss''").format(timestamp)
                : "NULL";
  }
  
  /** Encodes and quotes the timestamp for use in an SQL statement. */
  public String encodeQuoteTime (java.util.Date timestamp) {
    return (timestamp != null)
                ? new SimpleDateFormat("''HH:mm:ss''").format(timestamp)
                : "NULL";
  }

  /** Encodes and quotes the day portion only of a date for use in an SQL statement. */
  public String encodeQuoteDate_DayPortionOnly (java.util.Date timestamp) {
    return (timestamp != null)
                ? new SimpleDateFormat("''yyyyMMdd''").format(timestamp)
                : "NULL";
  }
  
  public String encodeQuoteDate (LocalDateTime timestamp) {
    return timestamp != null ? timestamp.format(DateTimeFormatter.ofPattern("''yyyyMMddHHmmss''")) : "NULL";
  }
  
  public String encodeQuoteDate (LocalDate timestamp) {
    return timestamp != null ? timestamp.format(DateTimeFormatter.ofPattern("''yyyyMMdd''")) : "NULL";
  }
  
  public String encodeQuoteTime (LocalTime time) {
    return time == null ? "NULL" : time.format(DateTimeFormatter.ofPattern("''HH:mm:ss''"));
  }

  /** Encodes a boolean value as an SQL integer value. */
  public String encodeBooleanInt (boolean value) {
    return value ? TRUE_BOOLINTSQL : FALSE_BOOLINTSQL;
  }

  /**
   * Encodes a boolean value as an SQL integer value.
   * If value is null then NULL is returned.
   */
  public String encodeBooleanInt (Boolean value) {
    if (value == null)
      return "NULL";

    return encodeBooleanInt(value.booleanValue());
  }
  
  public String putFixedIntegerBigDecimal (BigDecimal value) {
    if (value == null)
      return "NULL";

    return value.movePointRight(2).toString();
  }

  /** Encodes and quotes the enum for use in an SQL statement. */
  public String encodeQuoteEnum (@SuppressWarnings("rawtypes") Enum value) {
    return (value != null)
             ? "'" + value + "'"
             : "NULL";
  }

  /** Encodes the venum for use in an SQL statement. */
  public String encodeVEnum (VEnum vEnum) {
    return (vEnum != null)
             ? String.valueOf(vEnum.value())
             : "NULL";
  }

  /**
   * Builds an SQL expression representing the specified date criteria.
   * 
   * @param timeSpan The time span to be represented by the date criteria.
   * 
   * @return SQL representing the date criteria.
   */
  public String getTimeSpanCriteriaSQL (String dateField, TimeSpan timeSpan) {

    String startDataCriteria = (timeSpan != null && timeSpan.startTime != null)
                                  ? encodeQuoteTimestamp(timeSpan.startTime) + " <= " + dateField
                                  : null;
    String endDataCriteria = (timeSpan != null && timeSpan.endTime != null)
                                ? dateField + " < " + encodeQuoteTimestamp(timeSpan.endTime)
                                : null;

    if (startDataCriteria != null) {
      if (endDataCriteria != null) {
        return "(" + startDataCriteria + " AND " + endDataCriteria + ")";
      }
      else {
        return startDataCriteria;
      }
    }
    else {
      if (endDataCriteria != null) {
        return endDataCriteria;
      }
      else {
        return "1 = 1";
      }
    }
  }

  /**
   * Builds an SQL expression representing date-criteria that limits selection 
   * entries with dates occurring today
   */
  public String getTodayTimeSpanCriteriaSQL (String dateField) {
    TimeSpan timeSpan = TimeSpan.getTimeSpanForToday();
    return getTimeSpanCriteriaSQL(dateField, timeSpan);
  }

  /**
   * Builds an SQL expression representing date-criteria that limits selection 
   * entries with dates occuring between now and the end of today.
   */
  public String getRestOfTodayTimeSpanCriteriaSQL (String dateField) {
    TimeSpan timeSpan = TimeSpan.getTimeSpanForRestOfToday();
    return getTimeSpanCriteriaSQL(dateField, timeSpan);
  }

  /**
   * Creates an SQL list from the items in the provided list.
   * 
   * @param set The source item collection.
   * @param quoteElements Whether the items should be enclosed in single quotes.
   * @param onEmptyAddNull If true and the source list is empty, a list containing only null will be returned. 
   * 
   * @return An SQL list representing the source item list. 
   */
  public <T> String buildListSQL (Collection<T> set, boolean quoteElements, boolean onEmptyAddNull) {
    if (onEmptyAddNull && set.isEmpty())
      return "(null)";

    StringBuilder strBuf = new StringBuilder("(");
    Iterator<T> iter = set.iterator();
    boolean firstPass = true;
    while (iter.hasNext()) {
      if (firstPass)
        firstPass = false;
      else
        strBuf.append(',');

      Object element = iter.next();
      if (element == null)
        strBuf.append("NULL");
      else if (quoteElements)
        strBuf.append(escapeQuoteString(element.toString()));
      else
        strBuf.append(element);
    }
    strBuf.append(')');
      return strBuf.toString();
  }

  /**
   * Builds SQL WHERE-criteria for the specified field based on the provided IP
   * address spec.  The IP address spec can be a full IP address or one set with
   * a component replaced by '*' (ex: "141.238.30.*"; ex: "141.*").
   */
  public String buildIPAddressCriteria (String ipField, String ipMatchSpec) {

    Short[] ipComponents = miscUtil.parseIPAddress(ipMatchSpec, true);

    if (ipComponents == null)
      return "1 = 1";

    String ipValue = "";
    boolean exactMatch = true;
    for (int i = 0; i < ipComponents.length; i++) {
      if (i != 0) {
        ipValue += '.';
      }
      if (ipComponents[i] == null) {
        ipValue += '%';
        exactMatch = false;
        break;
      }
      ipValue += ipComponents[i];
    }

    return exactMatch
             ? ipField + " = " + escapeQuoteString(ipValue)
             : ipField + " LIKE " + escapeQuoteString(ipValue);
  }
  
  public String buildStringFromList (List<String> values) {
    return stringUtil.buildStringFromList(values, stringUtil.MYSQL_STORAGE_SEPERATOR_SEQUENCE);
  }
  
  public Timestamp getTimestamp(java.util.Date date) {
    return new Timestamp(date.getTime());
  }
  
  public String generateLimitClause(Integer offset, Integer fetchSize, boolean preceedingSpace) {
    
    if(offset == null && fetchSize == null)
      return "";
    if(offset == null)
      return (preceedingSpace ? " " : "") + "LIMIT " + String.valueOf(fetchSize);
    if(fetchSize == null)
      return (preceedingSpace ? " " : "") + "LIMIT " + offset + "," + Integer.MAX_VALUE;
    return (preceedingSpace ? " " : "") + "LIMIT " + offset + "," + fetchSize;
  }
  
  public <T> String escapeQuoteColumnName(Class<T> dbObjectClass, String columnName) {
    
    String tableName = queryUtil.getTableName(dbObjectClass);
    return escapeQuoteColumnName(tableName, columnName);
  }
  
  public <T> String escapeQuoteColumnName(String tableName, String columnName) {
    if(tableName == null)
      return escapeQuoteColumnName(columnName);
    return escapeQuoteColumnName(tableName) + "." + escapeQuoteColumnName(columnName);
  }
  
  public String escapeQuoteColumnName(String columnName) {
    return "`" + columnName.replaceAll("`", "") + "`";
  }
}