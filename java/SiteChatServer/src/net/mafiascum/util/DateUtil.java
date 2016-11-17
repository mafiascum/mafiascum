package net.mafiascum.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** Date/time utility methods. */
public class DateUtil extends MSUtil {
  
  private static DateUtil INSTANCE;
  
  private DateUtil() {
    
  }
  
  public static synchronized DateUtil get() {
    
    if(INSTANCE == null) {
      INSTANCE = new DateUtil();
      INSTANCE.init();
    }
    
    return INSTANCE;
  }

  public final GregorianCalendar getCalendar (Date time) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(time);
    return calendar;
  }

  public final GregorianCalendar getCalendar (long time) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(time);
    return calendar;
  }

  public int getCurrentYear () {
    GregorianCalendar now = new GregorianCalendar();
    return now.get(GregorianCalendar.YEAR);
  }

  public int getCurrentMonth () {
    GregorianCalendar now = new GregorianCalendar();
    return now.get(GregorianCalendar.MONTH);
  }

  public GregorianCalendar getYesterday () {
    GregorianCalendar tomarrow = new GregorianCalendar();
    tomarrow.add(Calendar.DATE, -1);
    return tomarrow;
  }

  public GregorianCalendar getTomorrow () {
    GregorianCalendar tomarrow = new GregorianCalendar();
    tomarrow.add(Calendar.DATE, 1);
    return tomarrow;
  }

  public final GregorianCalendar toCalendar (Date date) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    return calendar;
  }

  public void clearTime (Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  public void setToStartOfDay (Calendar calendar) {
    clearTime(calendar);
  }
  
  public void setToStartOfHour (Calendar calendar) {
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }
  
  public void setToEndOfHour (Calendar calendar) {
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
  }

  public void setToEndOfDay (Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
  }

  public Date startOfDay (Date date) {
    Calendar calendar = getCalendar(date);
    setToStartOfDay(calendar);
    return calendar.getTime();
  }

  public void setToStartOfWeek (Calendar calendar) {
    int day = calendar.get(GregorianCalendar.DAY_OF_WEEK);
    int diff = day - GregorianCalendar.SUNDAY;
    calendar.add(GregorianCalendar.DATE, -diff);
    clearTime(calendar);
  }

  public boolean onSameDay (Calendar cal1, Calendar cal2) {
    return (cal1.get(GregorianCalendar.YEAR) == cal2.get(GregorianCalendar.YEAR))
      && (cal1.get(GregorianCalendar.MONTH) == cal2.get(GregorianCalendar.MONTH)) 
      && (cal1.get(GregorianCalendar.DATE) == cal2.get(GregorianCalendar.DATE)); 
  }

  public boolean onSameDay (Date date1, Date date2) {
    return onSameDay(getCalendar(date1), getCalendar(date2));
  }

  public boolean isToday (Calendar calendar) {
    GregorianCalendar today = new GregorianCalendar();
    return onSameDay(calendar, today);
  }

  public boolean isTomorrow (Calendar calendar) {
    GregorianCalendar tomorrow = new GregorianCalendar();
    tomorrow.add(GregorianCalendar.DATE, 1);
    return onSameDay(calendar, tomorrow);
  }

  public boolean isYesterday (Calendar calendar) {
    GregorianCalendar yesterday = new GregorianCalendar();
    yesterday.add(GregorianCalendar.DATE, -1);
    return onSameDay(calendar, yesterday);
  }

  public Date combineDateAndTime (Date date, Date time) {
    GregorianCalendar calendar = getCalendar(date);
    GregorianCalendar timeCalendar = getCalendar(time);

    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
    calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
    calendar.set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND));

    return calendar.getTime();
  }

  public Date getStartOfNextDay (Date date) {
    GregorianCalendar calendar = getCalendar(date);
    clearTime(calendar);
    calendar.add(GregorianCalendar.DATE, +1);
    return calendar.getTime();
  }
  
  public Date getSameDayOfWeekLastYear(Date date) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    calendar.add(Calendar.YEAR, -1);
    calendar.add(Calendar.DATE, 1);
    
    if(calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) //Leap year.
      calendar.add(Calendar.DATE, 1);
    
    return calendar.getTime();
  }
  
  public Date getPreviousDay(Date date) {
    
    Calendar calendar = getCalendar(date);
    calendar.add(Calendar.DATE, -1);
    return calendar.getTime();
  }
  
  public Date getStartOfQuarter(Date date) {
    
    Calendar calendar = getCalendar(date);
    int quarter = getQuarter(date);
    
    if(quarter == 1)
      calendar.set(Calendar.MONTH, 0);
    else if(quarter == 2)
      calendar.set(Calendar.MONTH, 3);
    else if(quarter == 3)
      calendar.set(Calendar.MONTH, 6);
    else if(quarter == 4)
      calendar.set(Calendar.MONTH, 9);
    
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    clearTime(calendar);
    return calendar.getTime();
  }
  
  public Date getStartOfMonth(Date date) {
    
    Calendar calendar = getCalendar(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    clearTime(calendar);
    return calendar.getTime();
  }
  
  public Date getStartOfYear(Date date) {
    
    Calendar calendar = getCalendar(date);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    clearTime(calendar);
    return calendar.getTime();
  }
  
  public int getQuarter(Date date) {
    
    Calendar calendar = getCalendar(date);
    switch(calendar.get(Calendar.MONTH)) {
    case 0:
    case 1:
    case 2:
      return 1;
    case 3:
    case 4:
    case 5:
      return 2;
    case 6:
    case 7:
    case 8:
      return 3;
    case 9:
    case 10:
    case 11:
      return 4;
    }
    return 0;
  }
  
  public long daysBetween(Date startDate, Date endDate) {
    
    return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
  }
  
  public long currentTimeSeconds() {
    return System.currentTimeMillis() / 1000;
  }
}