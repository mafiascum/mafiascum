package net.mafiascum.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** Represents a span of time (possibly crossing multiple days). */
public class TimeSpan implements Serializable {
  
  private static final long serialVersionUID = -5514308729128181756L;
  public Date startTime; // If not null, returned entries will occur at or after this time (inclusive).
  public Date endTime; // If not null, returned entries will occur before this time (exclusive).
  
  public TimeSpan () {
  }

  public TimeSpan (Date startTime, Date endTime) {
    this();
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static TimeSpan getTimeSpanForDay (Date date) {
    Calendar startOfDay = DateUtil.get().getCalendar(date);
    DateUtil.get().setToStartOfDay(startOfDay);

    Calendar startOfNextDay = (Calendar) startOfDay.clone();
    startOfNextDay.add(Calendar.DATE, 1);

    return new TimeSpan(startOfDay.getTime(), startOfNextDay.getTime());
  }

  public static TimeSpan getTimeSpanForCurrentMonth () {
    Date now = new Date();
    return getTimeSpanForMonth(now);
  }

  public static TimeSpan getTimeSpanForMonth (Date date) {
    Calendar startOfMonth = DateUtil.get().getCalendar(date);
    startOfMonth.set(startOfMonth.get(Calendar.YEAR), startOfMonth.get(Calendar.MONTH), 1);
    DateUtil.get().setToStartOfDay(startOfMonth);

    Calendar startOfNextMonth = (Calendar) startOfMonth.clone();
    startOfNextMonth.add(Calendar.MONTH, 1);

    return new TimeSpan(startOfMonth.getTime(), startOfNextMonth.getTime());
  }

  public static TimeSpan getTimeSpanForPastSoManyMonths (Date date, int number_of_months) {
    Calendar end_date = new GregorianCalendar();
    end_date.setTime(date);
    DateUtil.get().setToEndOfDay(end_date);

    Calendar start_date = (Calendar) end_date.clone();
    start_date.add(Calendar.MONTH, -number_of_months);

    return new TimeSpan(start_date.getTime(), end_date.getTime());
  }

  public static TimeSpan getTimeSpanForYear (Date date) {
    Calendar startOfYear = DateUtil.get().getCalendar(date);
    startOfYear.set(startOfYear.get(Calendar.YEAR), 0, 1);
    DateUtil.get().setToStartOfDay(startOfYear);

    Calendar startOfNextYear = (Calendar) startOfYear.clone();
    startOfNextYear.add(Calendar.YEAR, 1);

    return new TimeSpan(startOfYear.getTime(), startOfNextYear.getTime());
  }

  public static TimeSpan getTimeSpanForToday () {
    Date today = new Date();
    return getTimeSpanForDay(today);
  }

  public static TimeSpan getTimeSpanForRestOfToday () {
    Calendar now = new GregorianCalendar();

    Calendar startOfTomarrow = (Calendar) now.clone();
    startOfTomarrow.add(Calendar.DATE, 1);
    DateUtil.get().setToStartOfDay(startOfTomarrow);

    return new TimeSpan(now.getTime(), startOfTomarrow.getTime());
  }

  public static TimeSpan getTimeSpanForTomorrow () {
    Calendar tomorrow = DateUtil.get().getTomorrow();
    return getTimeSpanForDay(tomorrow.getTime());
  }

  public static TimeSpan getTimeSpanForYesterday () {
    Calendar yesterday = DateUtil.get().getYesterday();
    return getTimeSpanForDay(yesterday.getTime());
  }

  public static TimeSpan getTimeSpanForWeek (Date startDate) {
    Calendar startOfWeek = DateUtil.get().getCalendar(startDate);
    DateUtil.get().setToStartOfDay(startOfWeek);

    Calendar endOfWeek = (Calendar) startOfWeek.clone();
    endOfWeek.add(Calendar.DATE, 7);

    return new TimeSpan(startOfWeek.getTime(), endOfWeek.getTime());
  }

  public static TimeSpan getTimeSpanForAWeek () {
    return getTimeSpanForWeek(new Date());
  }

  public static TimeSpan getTimeSpanForThisWeek () {
    Calendar startOfWeek = new GregorianCalendar();
    DateUtil.get().setToStartOfWeek(startOfWeek);

    Calendar endOfWeek = (Calendar) startOfWeek.clone();
    endOfWeek.add(Calendar.DATE, 7);

    return new TimeSpan(startOfWeek.getTime(), endOfWeek.getTime());
  }

  public static TimeSpan getTimeSpanForPastWeek () {
    return getTimeSpanForPastSoManyDays(7);
  }

  public static TimeSpan getTimeSpanForPast2Weeks () {
    return getTimeSpanForPastSoManyDays(14);
  }

  public static TimeSpan getTimeSpanForPastMonth () {
    return getTimeSpanForPastSoManyDays(31);
  }

  public static TimeSpan getTimeSpanForPastSoManyDays (int numDays) {
    Calendar endOfWeek = new GregorianCalendar();
    endOfWeek.add(Calendar.DATE, 1);
    DateUtil.get().clearTime(endOfWeek);

    Calendar startOfWeek = (Calendar) endOfWeek.clone();
    startOfWeek.add(Calendar.DATE, -numDays + 1);

    return new TimeSpan(startOfWeek.getTime(), endOfWeek.getTime());
  }

  public static TimeSpan getTimeSpanForPastSoManyDaysFromNow (int numDays) {
    Calendar endDate = new GregorianCalendar();

    Calendar startDate = (Calendar) endDate.clone();
    startDate.add(Calendar.DATE, -numDays);

    return new TimeSpan(startDate.getTime(), endDate.getTime());
  }

  public static TimeSpan getTimeSpanSpanning (TimeSpan timeSpan1, TimeSpan timeSpan2) {
    TimeSpan spanningTimeSpan = new TimeSpan();
    spanningTimeSpan.startTime = timeSpan1.startTime.before(timeSpan2.startTime) ? timeSpan1.startTime : timeSpan2.startTime;
    spanningTimeSpan.endTime = timeSpan1.startTime.after(timeSpan2.endTime) ? timeSpan1.endTime : timeSpan2.endTime;
    return spanningTimeSpan;
  }

  public boolean equals (Object object) {
    return (object instanceof TimeSpan)
           && ((startTime != null && ((TimeSpan) object).startTime != null) && startTime.compareTo(((TimeSpan) object).startTime) == 0)
           && ((endTime != null && ((TimeSpan) object).endTime != null) && endTime.compareTo(((TimeSpan) object).endTime) == 0);
  }
}