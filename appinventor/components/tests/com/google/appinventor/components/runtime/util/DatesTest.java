// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Tests for {@link Dates}.
 *
 */
public class DatesTest extends TestCase {

  public DatesTest(String testName) {
    super(testName);
  }

  /**
   * Tests {@link Dates#DateAdd(Calendar, int, int)}.
   */
  public void testDateAdd() {
    GregorianCalendar date = new GregorianCalendar(2004, Calendar.FEBRUARY, 29, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_YEAR, 1);
    assertEquals(2005, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2004, Calendar.FEBRUARY, 29, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_YEAR, -1);
    assertEquals(2003, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2004, Calendar.DECEMBER, 29, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_MONTH, 1);
    assertEquals(2005, date.get(GregorianCalendar.YEAR));
    assertEquals(29, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.JANUARY, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2004, Calendar.JANUARY, 29, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_MONTH, -1);
    assertEquals(2003, date.get(GregorianCalendar.YEAR));
    assertEquals(29, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.DECEMBER, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2100, Calendar.FEBRUARY, 28, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_DAY, 1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(1, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.MARCH, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2100, Calendar.MARCH, 1, 20, 01, 30);
    Dates.DateAdd(date, Dates.DATE_DAY, -1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));

    date = new GregorianCalendar(2100, Calendar.FEBRUARY, 28, 23, 01, 30);
    Dates.DateAdd(date, Dates.DATE_HOUR, 1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(1, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.MARCH, date.get(GregorianCalendar.MONTH));
    assertEquals(0, date.get(GregorianCalendar.HOUR_OF_DAY));

    date = new GregorianCalendar(2100, Calendar.MARCH, 1, 0, 01, 30);
    Dates.DateAdd(date, Dates.DATE_HOUR, -1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));
    assertEquals(23, date.get(GregorianCalendar.HOUR_OF_DAY));

    date = new GregorianCalendar(2100, Calendar.FEBRUARY, 28, 23, 59, 30);
    Dates.DateAdd(date, Dates.DATE_MINUTE, 1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(1, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.MARCH, date.get(GregorianCalendar.MONTH));
    assertEquals(0, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(0, date.get(GregorianCalendar.MINUTE));

    date = new GregorianCalendar(2100, Calendar.MARCH, 1, 0, 0, 30);
    Dates.DateAdd(date, Dates.DATE_MINUTE, -1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));
    assertEquals(23, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(59, date.get(GregorianCalendar.MINUTE));

    date = new GregorianCalendar(2100, Calendar.FEBRUARY, 28, 23, 59, 59);
    Dates.DateAdd(date, Dates.DATE_SECOND, 1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(1, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.MARCH, date.get(GregorianCalendar.MONTH));
    assertEquals(0, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(0, date.get(GregorianCalendar.MINUTE));
    assertEquals(0, date.get(GregorianCalendar.SECOND));

    date = new GregorianCalendar(2100, Calendar.MARCH, 1, 0, 0, 0);
    Dates.DateAdd(date, Dates.DATE_SECOND, -1);
    assertEquals(2100, date.get(GregorianCalendar.YEAR));
    assertEquals(28, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.FEBRUARY, date.get(GregorianCalendar.MONTH));
    assertEquals(23, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(59, date.get(GregorianCalendar.MINUTE));
    assertEquals(59, date.get(GregorianCalendar.SECOND));
  }

  /**
   * Tests {@link Dates#DateValue(String)}.
   */
  public void testDateValue() {
    // Check NullPointerException is thrown when the input string is null
    try {
      Calendar date = Dates.DateValue(null);
      fail();
    } catch (NullPointerException expected) {
    }

    // Empty string is an illegal argument
    try {
      Calendar date = Dates.DateValue("");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    // Another illegal argument
    try {
      Calendar date = Dates.DateValue("abc");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    // Input string with extra characters at the end is OK
    Dates.DateValue("04/21/2008 abc");
    Dates.DateValue("04/21/2008 09:29:48 abc");

    // Date only input string
    Calendar date = Dates.DateValue("04/21/2008");
    assertEquals(2008, date.get(GregorianCalendar.YEAR));
    assertEquals(21, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.APRIL, date.get(GregorianCalendar.MONTH));
    assertEquals(0, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(0, date.get(GregorianCalendar.MINUTE));
    assertEquals(0, date.get(GregorianCalendar.SECOND));

    // Date and time input string
    date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(2008, date.get(GregorianCalendar.YEAR));
    assertEquals(21, date.get(GregorianCalendar.DAY_OF_MONTH));
    assertEquals(Calendar.APRIL, date.get(GregorianCalendar.MONTH));
    assertEquals(9, date.get(GregorianCalendar.HOUR_OF_DAY));
    assertEquals(29, date.get(GregorianCalendar.MINUTE));
    assertEquals(48, date.get(GregorianCalendar.SECOND));
  }

  /**
   * Tests {@link Dates#Day(Calendar)}.
   */
  public void testDay() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(21, Dates.Day(date));
  }

  /**
   * Tests {@link Dates#FormatDate(Calendar)}.
   */
  public void testFormatDate() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals("Apr 21, 2008", Dates.FormatDate(date,""));
  }

  /**
   * Tests {@link Dates#Hour(Calendar)}.
   */
  public void testHour() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(9, Dates.Hour(date));
  }

  /**
   * Tests {@link Dates#Minute(Calendar)}.
   */
  public void testMinute() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(29, Dates.Minute(date));
  }

  /**
   * Tests {@link Dates#Month(Calendar)}.
   */
  public void testMonth() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(Dates.DATE_APRIL, Dates.Month(date));
  }

  /**
   * Tests {@link Dates#MonthName(Calendar)}.
   */
  public void testMonthName() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals("April", Dates.MonthName(date));
  }

  /**
   * Tests {@link Dates#Now()}.
   */
  public void testNow() {
    // Not much to test here, just making sure this doesn't crash
    assertNotNull(Dates.Now());
  }

  /**
   * Tests {@link Dates#Second(Calendar)}.
   */
  public void testSecond() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(48, Dates.Second(date));
  }

  /**
   * Tests {@link Dates#Timer()}.
   */
  public void testTimer() {
    final long sleepInterval = 1000;
    long start = Dates.Timer();
    try {
      Thread.sleep(sleepInterval);
    } catch (InterruptedException e) {
      fail();
    }
    long end = Dates.Timer();

    assertTrue(end - start >= sleepInterval);
  }

  /**
   * Tests {@link Dates#Weekday(Calendar)}.
   */
  public void testWeekday() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(Dates.DATE_MONDAY, Dates.Weekday(date));
  }

  /**
   * Tests {@link Dates#WeekdayName(Calendar)}.
   */
  public void testWeekdayName() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals("Monday", Dates.WeekdayName(date));
  }

  /**
   * Tests {@link Dates#Year(Calendar)}.
   */
  public void testYear() {
    Calendar date = Dates.DateValue("04/21/2008 09:29:48");
    assertEquals(2008, Dates.Year(date));
  }
}
