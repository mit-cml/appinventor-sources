// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.Dates;
import com.google.appinventor.components.runtime.util.TimerInternal;
import com.google.appinventor.components.runtime.util.ErrorMessages;


import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * ![Image of the Clock component](images/clock.png)
 *
 * Non-visible component that provides the instant in time using the internal clock on the phone.
 * It can fire a timer at regularly set intervals and perform time calculations, manipulations,
 * and conversions.
 *
 * Operations on dates and times, such as from [`DatePicker`](userinterface.html#DatePicker) and
 * [`TimePicker`](userinterface.html#TimePicker), are accomplished through methods in Clock. Date
 * and Time are represented as InstantInTime and Duration.
 *
 *  - **Instant**: consists of Year, Month, DayOfMnoth, Hour, Minute, and SEcond. An instant can be
 *    created using the {@link #MakeInstant(String)}, {@link #MakeInstantFromMillis(long)} and
 *    {@link #MakeInstantFromParts(int, int, int, int, int, int)} methods.
 *  - **Duration**: time in milliseconds elapsed between instants. Duration can be obtained by the
 *    {@link #Duration(Calendar, Calendar)} method.
 *
 * Instants are assumed to be in the device's local time zone. When they are converted to or from
 * milliseconds, the milliseconds for a given Instance are calculated from January 1, 1970 in UTC
 * (Greenwich Mean Time).
 *
 * Methods to convert an Instant to text are also available. Acceptable patterns are empty string,
 * `MM/dd/YYYY HH:mm:ss a`, or `MMM d, yyyy HH:mm`. The empty string will provide the default
 * format, which is `"MMM d, yyyy HH:mm:ss a"` for {@link #FormatDateTime(Calendar, String)},
 * `"MMM d, yyyy"` for {@link #FormatDate(Calendar, String)}. To see all possible formats, please
 * see [here](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html).
 *
 * A note on combining date and time: In order to combine the date from one Instant and the time
 * from another, for example from a [`DatePicker`](userinterface.html#DatePicker) and
 * [`TimePicker`](userinterface.html#TimePicker), extract the parts as text and use the text to
 * create a new Instant. For example:
 *
 * ![Example code blocks to combine date and time picker data](images/date_and_time_instant.png)
 */

@DesignerComponent(version = YaVersion.CLOCK_COMPONENT_VERSION,
    description = "<p>Non-visible component that provides the instant in time using the internal clock on th"
    + "e phone. It can fire a timer at regularly set intervals and perform time calculations, manipulations, and conversions.</p> "
    + "<p>Methods to convert an instant to text are also available. Acceptable patterns are empty string, MM/DD/YYYY HH:mm:ss a, or MMM d, yyyy"
    + "HH:mm. The empty string will provide the default format, which is \"MMM d, yyyy HH:mm:ss a\" for FormatDateTime \"MMM d, yyyy\" for FormatDate. "
    + "To see all possible format, please see <a href=\"https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html\" _target=\"_blank\">here</a>. </p> ",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/clock.png")
@SimpleObject
public class Clock extends AndroidNonvisibleComponent
    implements Component, AlarmHandler, OnStopListener, OnResumeListener, OnDestroyListener,
               Deleteable {
  private static final int DEFAULT_INTERVAL = 1000;  // ms
  private static final boolean DEFAULT_ENABLED = true;

  private TimerInternal timerInternal;
  private boolean timerAlwaysFires = true;
  private boolean onScreen = false;

  /**
   * Creates a new Clock component.
   *
   * @param container ignored (because this is a non-visible component)
   */
  public Clock(ComponentContainer container) {
    super(container.$form());
    timerInternal = new TimerInternal(this, DEFAULT_ENABLED, DEFAULT_INTERVAL);

    // Set up listeners
    form.registerForOnResume(this);
    form.registerForOnStop(this);
    form.registerForOnDestroy(this);

    if (form instanceof ReplForm) {
      // In REPL, if this Clock component was added to the project after the onResume call occurred,
      // then onScreen would be false, but the REPL app is, in fact, on screen.
      onScreen = true;
    }
  }

  // Only the above constructor should be used in practice.
  public Clock() {
    super(null);
    // To allow testing without Timer
  }

  /**
   * Default Timer event handler.
   *
   * @suppressdoc
   */
  @SimpleEvent(
      description = "The Timer event runs when the timer has gone off.")
  public void Timer() {
    if (timerAlwaysFires || onScreen) {
      EventDispatcher.dispatchEvent(this, "Timer");
    }
  }

  /**
   * Interval property getter method.
   *
   * @return timer interval in ms
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description ="Interval between timer events in ms")
  public int TimerInterval() {
    return timerInternal.Interval();
  }

  /**
   * Specifies the interval between subsequent {@link #Timer()} events.
   *
   *   **Note**: Drift may occur over time and that the system may not honor the
   * timing specified here if the app or another process on the phone is busy.
   *
   * @param interval timer interval in ms
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_INTERVAL + "")
  @SimpleProperty
  public void TimerInterval(int interval) {
    timerInternal.Interval(interval);
  }

  /**
   * Enabled property getter method.
   *
   * @return {@code true} indicates a running timer, {@code false} a stopped
   *         timer
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "Fires timer if true")
  public boolean TimerEnabled() {
    return timerInternal.Enabled();
  }

  /**
   * Specifies whether the {@link #Timer()} event should run.
   *
   * @param enabled {@code true} starts the timer, {@code false} stops it
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_ENABLED ? "True" : "False")
  @SimpleProperty
  public void TimerEnabled(boolean enabled) {
    timerInternal.Enabled(enabled);
  }

  /**
   * TimerAlwaysFires property getter method.
   *
   * @suppressdoc
   * @return {@code true} if the timer event will fire even if the application
   *   is not on the screen
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "Will fire even when application is not showing on the "
      + "screen if true")
  public boolean TimerAlwaysFires() {
    return timerAlwaysFires;
  }

  /**
   * TimerAlwaysFires property setter method: instructs when to disable
   *
   * @suppressdoc
   *  @param always {@code true} if the timer event should fire even if the
   *  application is not on the screen
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void TimerAlwaysFires(boolean always) {
    timerAlwaysFires = always;
  }

  // AlarmHandler implementation

  @Override
  public void alarm() {
    Timer();
  }

  /**
   * Returns the current system time in milliseconds.
   *
   * @return  current system time in milliseconds
   * @suppressdoc
   */
  @SimpleFunction (description = "Returns the phone's internal time.")
  public static long SystemTime() {
    return Dates.Timer();
  }

  @SimpleFunction(description = "Returns the current instant in time read from "
      + "phone's clock.")
  public static Calendar Now() {
    return Dates.Now();
  }

  /**
   * An instant in time specified by MM/DD/YYYY hh:mm:ss or MM/DD/YYYY or hh:mm
   * where MM is the month (01-12), DD the day (01-31), YYYY the year
   * (0000-9999), hh the hours (00-23), mm the minutes (00-59) and ss
   * the seconds (00-59).
   *
   * @param from  string to convert
   * @return  date
   * @suppressdoc
   */
  @SimpleFunction(
      description = "Returns an instant in time specified by MM/dd/YYYY hh:mm:ss or "
          + "MM/dd/YYYY or hh:mm.")
  public static Calendar MakeInstant(String from) {
    try {
      return Dates.DateValue(from);
    } catch (IllegalArgumentException e) {
      throw new YailRuntimeError(
          "Argument to MakeInstant should have form MM/dd/YYYY hh:mm:ss, or MM/dd/YYYY or hh:mm",
          "Sorry to be so picky.");
    }
  }

  /**
   * An instant in time specified by number year, number month, number day
   * @param year year integer
   * @param month month integer
   * @param day day integer
   * @return  Calendar instant
   * @suppressdoc
   */
  @SimpleFunction(description = "Returns an instant in time specified by year, month, date in "
      + "UTC.\nValid values for the month field are 1-12 and 1-31 for the day field.")
  public Calendar MakeDate(int year, int month, int day) {
    int jMonth = month - 1;
    try {
      GregorianCalendar cal = new GregorianCalendar(year, jMonth, day);
      cal.setLenient(false);

      // A non-lenient GregorianCalendar throws an exception upon 
      // calculating its time or calendar field values if any out-of-range field value has been set.
      cal.getTime();
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, "MakeDate", ErrorMessages.ERROR_ILLEGAL_DATE);
    }
    
    Calendar instant = Dates.DateInstant(year, month, day);
    return instant;
  }

  /**
   * An instant in time specified by integer hour, integer minute, and integer second
   * @param hour hour integer
   * @param minute minute integer
   * @param second second integer
   * @return  Calendar instant since 1/1/1970
   * @suppressdoc
   */
  @SimpleFunction(description = "Returns an instant in time specified by hour, minute, second in "
      + "UTC.")
  public Calendar MakeTime(int hour, int minute, int second) {
    Calendar instant = new GregorianCalendar();
    try {
      instant.set(Calendar.HOUR_OF_DAY, hour);
      instant.set(Calendar.MINUTE, minute);
      instant.set(Calendar.SECOND, second);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, "MakeTime", ErrorMessages.ERROR_ILLEGAL_DATE);
    }
    return instant;
  }

  /**
   * An instant in time specified by number year, number month, number day, number hour,
   * number minute, number second
   * @param year year integer
   * @param month month integer
   * @param day day integer
   * @param hour hour integer
   * @param minute minute integer
   * @param second minute integer
   * @return  Calendar instant
   * @suppressdoc
   */
  @SimpleFunction(
    description = "Returns an instant in time specified by year, month, date, hour, minute, "
        + "second in UTC.")
  public Calendar MakeInstantFromParts(int year, int month, int day, int hour, int minute, int second) {
    int jMonth = month - 1;
    Calendar instant = null;
    try {
      instant = new GregorianCalendar(year, jMonth, day);
      instant.setLenient(false);

      // A non-lenient GregorianCalendar throws an exception upon 
      // calculating its time or calendar field values if any out-of-range field value has been set.
      instant.getTime();
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, "MakeInstantFromParts", ErrorMessages.ERROR_ILLEGAL_DATE);
    }
    
    instant = Dates.DateInstant(year, month, day);
    
    try {
      instant.set(Calendar.HOUR_OF_DAY, hour);
      instant.set(Calendar.MINUTE, minute);
      instant.set(Calendar.SECOND, second);

    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, "MakeInstantFromParts", ErrorMessages.ERROR_ILLEGAL_DATE);
    }

    return instant;
    
  }

  /**
   * Create an Calendar from ms since 1/1/1970 00:00:00.0000
   * Probably should go in Calendar.
   *
   * @param millis raw millisecond number.
   * @suppressdoc
   */
  @SimpleFunction(description = "Returns an instant in time specified by the milliseconds since "
      + "1970 in UTC.")
  public static Calendar MakeInstantFromMillis(long millis) {
    Calendar instant = Dates.Now(); // just to get our hands on an instant
    instant.setTimeInMillis(millis);
    return instant;
  }

  /**
   * Calendar property getter method: gets the raw millisecond representation of
   *  a Calendar.
   * @param instant Calendar
   * @return milliseconds since 1/1/1970.
   * @suppressdoc
   */
  @SimpleFunction (description = "Returns the instant in time measured as milliseconds since 1970.")
  public static long GetMillis(Calendar instant) {
    return instant.getTimeInMillis();
  }

  @SimpleFunction(description = "Returns an instant in time some duration after the argument")
  public static Calendar AddDuration(Calendar instant, long quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAddInMillis(newInstant, quantity);
    return newInstant;
  }

  @SimpleFunction(description = "Returns an instant in time some seconds after the given instant.")
  public static Calendar AddSeconds(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.SECOND, quantity);
    return newInstant;
  }

  @SimpleFunction(description = "Returns an instant in time some minutes after the given instant.")
  public static Calendar AddMinutes(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.MINUTE, quantity);
    return newInstant;
  }

  @SimpleFunction(description = "Returns an instant in time some hours after the given instant.")
  public static Calendar AddHours(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.HOUR_OF_DAY, quantity);
    return newInstant;
  }

  @SimpleFunction(description = "Returns an instant in time some days after the given instant.")
  public static Calendar AddDays(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.DATE, quantity);
    return newInstant;
  }

  @SimpleFunction(description = "Returns An instant in time some weeks after the given instant.")
  public static Calendar AddWeeks(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.WEEK_OF_YEAR, quantity);
    return newInstant;
 }

  @SimpleFunction(description = "Returns an instant in time some months after the given instant.")
  public static Calendar AddMonths(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.MONTH, quantity);
    return newInstant;
 }

  @SimpleFunction(description = "Returns an instant in time some years after the given instant.")
  public static Calendar AddYears(Calendar instant, int quantity) {
    Calendar newInstant = (Calendar) instant.clone();
    Dates.DateAdd(newInstant, Calendar.YEAR, quantity);
    return newInstant;
  }

  /**
   * Returns the milliseconds by which end follows start (+ or -)
   *
   * @param start beginning instant
   * @param end ending instant
   * @return  milliseconds
   */
  @SimpleFunction (description = "Returns duration, which is milliseconds elapsed between "
      + "instants.")
  public static long Duration(Calendar start, Calendar end) {
    return end.getTimeInMillis() - start.getTimeInMillis();
  }

  /**
   * Returns the duration converted from milliseconds to seconds.
   *
   * @param duration time interval to convert
   * @return  duration in seconds
   */
  @SimpleFunction (description = "Converts the duration to the number of seconds.")
  public static long DurationToSeconds(long duration) {
          return Dates.ConvertDuration(duration, Calendar.SECOND);
  }

  /**
   * Returns the duration converted from milliseconds to minutes.
   *
   * @param duration time interval to convert
   * @return  duration in minutes
   */
  @SimpleFunction (description = "Converts the duration to the number of minutes.")
  public static long DurationToMinutes(long duration) {
          return Dates.ConvertDuration(duration, Calendar.MINUTE);
  }

  /**
   * Returns the duration converted from milliseconds to hours.
   *
   * @param duration time interval to convert
   * @return  duration in hours
   */
  @SimpleFunction (description = "Converts the duration to the number of hours.")
  public static long DurationToHours(long duration) {
          return Dates.ConvertDuration(duration, Calendar.HOUR_OF_DAY);
  }

  /**
   * Returns the duration converted from milliseconds to days.
   *
   * @param duration time interval to convert
   * @return  duration in days
   */
  @SimpleFunction (description = "Converts the duration to the number of days.")
  public static long DurationToDays(long duration) {
          return Dates.ConvertDuration(duration, Calendar.DATE);
  }

  /**
   * Returns the duration converted from milliseconds to weeks.
   *
   * @param duration time interval to convert
   * @return  duration in weeks
   */
  @SimpleFunction (description = "Converts the duration to the number of weeks.")
  public static long DurationToWeeks(long duration) {
          return Dates.ConvertDuration(duration, Calendar.WEEK_OF_YEAR);
  }

  /**
   * Returns the seconds for the given instant.
   *
   * @param instant  instant to use seconds of
   * @return  seconds (range 0 - 59)
   */
  @SimpleFunction (description = "Returns the second of the minute (0-59) from the instant.")
  public static int Second(Calendar instant) {
    return Dates.Second(instant);
  }

/**
   * Returns the minutes for the given date.
   *
   * @param instant instant to use minutes of
   * @return  minutes (range 0 - 59)
   */
  @SimpleFunction(description = "Returns the minute of the hour (0-59) from the instant.")
  public static int Minute(Calendar instant) {
    return Dates.Minute(instant);
  }

  /**
   * Returns the hours for the given date.
   *
   * @param instant Calendar to use hours of
   * @return  hours (range 0 - 23)
   */
  @SimpleFunction (description = "Returns the hour of the day (0-23) from the instant.")
  public static int Hour(Calendar instant) {
    return Dates.Hour(instant);
  }

  /**
   * Returns the day of the month.
   *
   * @param instant  instant to use day of the month of
   * @return  day: [1...31]
   */
  @SimpleFunction (description = "Returns the day of the month (1-31) from the instant.")
  public static int DayOfMonth(Calendar instant) {
    return Dates.Day(instant);
  }

  /**
   * Returns the weekday for the given instant.
   *
   * @param instant  instant to use day of week of
   * @return day of week: [1...7] starting with Sunday
   */
  @SimpleFunction (description = "Returns the day of the week represented as a "
      + "number from 1 (Sunday) to 7 (Saturday).")
  public static int Weekday(Calendar instant) {
    return Dates.Weekday(instant);
  }

  /**
   * Returns the name of the weekday for the given instant.
   *
   * @param instant  instant to use weekday of
   * @return  weekday, as a string.
   */
  @SimpleFunction (description = "Returns the name of the day of the week from the instant.")
  public static String WeekdayName(Calendar instant) {
    return Dates.WeekdayName(instant);
  }

  /**
   * Returns the number of the month for the given instant.
   *
   * @param instant  instant to use month of
   * @return  number of month
   */
  @SimpleFunction (description = "Returns the month of the year represented as a "
      + "number from 1 to 12).")
  public static int Month(Calendar instant) {
    return Dates.Month(instant) + 1;
  }

  /**
   * Returns the name of the month for the given instant.
   *
   * @param instant  instant to use month of
   * @return  name of month
   */
  @SimpleFunction (description = "Returns the name of the month from the instant, e.g., January, "
      + "February, March...")
  public static String MonthName(Calendar instant) {
    return Dates.MonthName(instant);
  }

 /**
   * Returns the year of the given instant.
   *
   * @param instant  instant to use year of
   * @return  year
   */
  @SimpleFunction(description = "The year")
  public static int Year(Calendar instant) {
    return Dates.Year(instant);
  }

  /**
   * Converts and formats an instant into a string of date and time with the specified pattern.
   * To learn more about valid patterns, please see
   * [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat).
   *
   * @param instant  instant to format
   * @param pattern format of the date and time e.g. MM/dd/YYYY hh:mm:ss a, MMM d, yyyy HH:mm
   * @return  formatted instant
   */
  @SimpleFunction (description = "Returns text representing the date and time of an"
      + " instant in the specified pattern")
  public static String FormatDateTime(Calendar instant, String pattern) {
    try {
      return Dates.FormatDateTime(instant, pattern);
    } catch (IllegalArgumentException e){
      throw new YailRuntimeError(
        "Illegal argument for pattern in Clock.FormatDateTime. Acceptable values are empty string, MM/dd/YYYY hh:mm:ss a, MMM d, yyyy HH:mm "
        + "For all possible patterns, see https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html",
        "Sorry to be so picky.");
    }
  }

  /**
   * Converts and formats an instant into a string of date with the specified pattern. To learn
   * more about valid patterns, please see
   * [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat).
   *
   * @param instant  instant to format
   * @param pattern format of the date e.g. MM/DD/YYYY or MMM d, yyyy
   * @return  formatted instant
   */
  @SimpleFunction (description = "Text representing the date of an instant in the specified pattern")
  public static String FormatDate(Calendar instant, String pattern) {
    try {
      return Dates.FormatDate(instant, pattern);
    } catch (IllegalArgumentException e){
      throw new YailRuntimeError(
        "Illegal argument for pattern in Clock.FormatDate. Acceptable values are empty string, MM/dd/YYYY, or MMM d, yyyy. "
        + "For all possible patterns, see https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"
        ,"Sorry to be so picky.");
    }
  }

  /**
   * Converts and formats the given instant into a string with the specified pattern. To learn
   * more about valid patterns, please see
   * [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat).
   *
   * @param instant  instant to format
   * @return  formatted instant
   */
  @SimpleFunction (description = "Text representing the time of an instant")
  public static String FormatTime(Calendar instant) {
    return Dates.FormatTime(instant);
  }

  @Override
  public void onStop() {
    onScreen = false;
  }

  @Override
  public void onResume() {
    onScreen = true;
  }

  @Override
  public void onDestroy() {
    timerInternal.Enabled(false);
  }

  @Override
  public void onDelete() {
    timerInternal.Enabled(false);
  }
}
