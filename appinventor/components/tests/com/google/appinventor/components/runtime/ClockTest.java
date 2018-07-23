// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import junit.framework.TestCase;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Tests Clock.java.
 *
 */

public class ClockTest extends TestCase {


  private final Calendar tc1 = Clock.MakeInstant("10/11/1941 09:30:00");
  private final Calendar tc2 = Clock.MakeInstant("10/25/1941 09:30:00"); // t1 + 14days
  private final Calendar tc3 = Clock.MakeInstant("10/11/1941 12:30:00"); // t1 + 3hours

  public void testNow() throws Exception {
    assertTrue(0 > Clock.Duration(Clock.Now(), tc1));
  }

  public void testDurationToDays() throws Exception {
    assertEquals(14, Clock.DurationToDays(Clock.Duration(tc1,tc2)));
  }

  public void testDurationToWeeks() throws Exception {
        assertEquals(2, Clock.DurationToWeeks(Clock.Duration(tc1,tc2)));
  }

  public void testDurationToHours() throws Exception {
        assertEquals(3, Clock.DurationToHours(Clock.Duration(tc1,tc3)));
  }

  public void testDurationToMinutes() throws Exception {
        assertEquals(180, Clock.DurationToMinutes(Clock.Duration(tc1,tc3)));
  }

  public void testMakeInstant() throws Exception {
    assertEquals("Oct 11, 1941 09:30:00 AM", Clock.FormatDateTime(tc1,""));
  }

  public void testMakeInstantFromMillis() throws Exception {
    assertEquals("Jan 1, 1970 12:00:00 AM",
        Clock.FormatDateTime(Clock.MakeInstantFromMillis(0
                                      - TimeZone.getDefault().getRawOffset()),""));
  }

  public void testGetMillis() throws Exception {
    assertEquals(-TimeZone.getDefault().getRawOffset(),
        Clock.GetMillis(Clock.MakeInstant("1/1/1970 00:00:00")));
  }

  public void testAddYears() throws Exception {
    assertEquals("Oct 11, 1943", Clock.FormatDate(Clock.AddYears(tc1, 2), "MMM d, yyyy"));
  }

  public void testAddYears2() throws Exception {
    assertEquals("10/11/1931 09:30 AM", Clock.FormatDateTime(Clock.AddYears(tc1, -10), "MM/dd/yyyy HH:mm a"));
  }

  public void testAddMonths() throws Exception {
    assertEquals("1941/12/11", Clock.FormatDate(Clock.AddMonths(tc1, 2), "yyyy/MM/dd"));
  }

  public void testAddMonths2() throws Exception {
    assertEquals("11/10/1942 09:30:00 AM", Clock.FormatDateTime(Clock.AddMonths(tc1, 12), "dd/MM/yyyy HH:mm:ss a"));
  }

  public void testAddWeeks() throws Exception {
    assertEquals("Sep 10, 2002 12:00:00 AM", Clock.FormatDateTime(
        Clock.AddWeeks(Clock.MakeInstant("9/11/2001 00:00:00"), 52), ""));
  }

  public void testAddWeeks2() throws Exception {
    assertEquals("Sep 18, 2001 12:00:00 AM", Clock.FormatDateTime(
        Clock.AddWeeks(Clock.MakeInstant("9/11/2001 00:00:00"), 1), ""));
  }

  public void testAddDays() throws Exception {
    assertEquals("Oct 9, 1941", Clock.FormatDate(Clock.AddDays(tc1, -2), ""));
  }

  public void testAddDays2() throws Exception {
    assertEquals("11/01/41", Clock.FormatDate(Clock.AddDays(tc1, 21), "MM/dd/yy"));
  }

  public void testAddHours() throws Exception {
    assertEquals("Oct 11, 1941 07:30:00 AM", Clock.FormatDateTime(Clock.AddHours(tc1, -2),""));
  }

  public void testAddHours2() throws Exception {
    assertEquals("10-12-1941 09:30:00 AM", Clock.FormatDateTime(Clock.AddHours(tc1, 24),"MM-dd-yyyy HH:mm:ss a"));
  }

  public void testAddMinutes() throws Exception {
    assertEquals("Oct 11, 1941 09:32:00 AM", Clock.FormatDateTime(Clock.AddMinutes(tc1, 2),""));
  }

  public void testAddSeconds() throws Exception {
    assertEquals("9:31:01 AM", Clock.FormatTime(Clock.AddSeconds(tc1, 61)));
  }

  public void testAddDuration() throws Exception {
        assertEquals("Oct 11, 1941 09:30:05 AM", Clock.FormatDateTime(Clock.AddDuration(tc1, 5000), "")); //5000ms = 5s
  }

  public void testAddDuration2() throws Exception {
        assertEquals("Oct 25, 1941", Clock.FormatDate(Clock.AddDuration(tc1, Clock.Duration(tc1, tc2)),""));
  }

  public void testSecond() throws Exception {
    assertEquals(0, Clock.Second(tc1));
  }

  public void testMinute() throws Exception {
    assertEquals(30, Clock.Minute(tc1));
  }

  public void testHour() throws Exception {
    assertEquals(9, Clock.Hour(tc1));
  }

  public void testWeekday() throws Exception {
    assertEquals(2, Clock.Weekday(Clock.MakeInstant("11/2/2009")));
  }

  public void testWeekdayName() throws Exception {
    assertEquals("Monday", Clock.WeekdayName(Clock.MakeInstant("11/2/2009")));
  }

  public void testDayOfMonth() throws Exception {
    assertEquals(11, Clock.DayOfMonth(tc1));
  }

  public void testMonth() throws Exception {
    assertEquals(10, Clock.Month(tc1));
  }

  public void testMonthName() throws Exception {
    assertEquals("October", Clock.MonthName(tc1));
  }

  public void testYear() throws Exception {
    assertEquals(1941, Clock.Year(tc1));
  }
}
