package java.util;

import com.google.gwt.core.client.JavaScriptObject;

public class Calendar {
  public static final int YEAR = 1;
  public static final int MONTH = 2;
  public static final int WEEK_OF_YEAR = 3;
  public static final int WEEK_OF_MONTH = 4;
  public static final int DATE = 5;
  public static final int DAY_OF_MONTH = 5;
  public static final int DAY_OF_WEEK = 7;
  public static final int HOUR_OF_DAY = 11;
  public static final int MINUTE = 12;
  public static final int SECOND = 13;
  public static final int MILLISECOND = 14;

  public static final int MONDAY = 2;
  public static final int TUESDAY = 3;
  public static final int WEDNESDAY = 4;
  public static final int THURSDAY = 5;
  public static final int FRIDAY = 6;
  public static final int SATURDAY = 7;
  public static final int SUNDAY = 1;

  public static final int JANUARY = 0;
  public static final int FEBRUARY = 1;
  public static final int MARCH = 2;
  public static final int APRIL = 3;
  public static final int MAY = 4;
  public static final int JUNE = 5;
  public static final int JULY = 6;
  public static final int AUGUST = 7;
  public static final int SEPTEMBER = 8;
  public static final int OCTOBER = 9;
  public static final int NOVEMBER = 10;
  public static final int DECEMBER = 11;

  protected JavaScriptObject dateTime;
  protected boolean isLenient = false;

  public Calendar() {
    dateTime = currentTime();
  }

  public Object clone() {
    Calendar copy = new Calendar();
    copy.dateTime = this.dateTime; // Shallow copy of the dateTime
    return copy;
  }

  public void setTimeInMillis(long millis) {
    setTimeInMillisNative((int) millis);
  }

  public void setLenient(boolean lenient) {
    isLenient = lenient;
  }

  public void add(int field, int amount) {
  }

  public int get(int field) {
    return 0;
  }

  public native void set(int field, int value) /*-{
    switch (field) {
      case 11:
        dateTime.setHours(value);
        break;
      case 12:
        dateTime.setMinutes(value);
        break;
      case 13:
        dateTime.setSeconds(value);
        break;
      default:
        console.warn("Unsupported Calendar field: " + field);
    }
  }-*/;

  public Date getTime() {
    return new Date(getTimeInMillis());
  }

  public void setTime(Date date) {
    if (date == null) {
      throw new NullPointerException("Date cannot be null");
    }
    setTimeInMillisNative((int) date.getTime());
  }

  public native int getTimeInMillis() /*-{
    return this.@java.util.Calendar::dateTime.getTime();
  }-*/;

  private static native JavaScriptObject currentTime() /*-{
    return new Date();
  }-*/;

  private static native JavaScriptObject copyDate(JavaScriptObject date) /*-{
    return new Date(date);
  }-*/;

  private native void setTimeInMillisNative(int millis) /*-{
    this.@java.util.Calendar::dateTime.setTime(millis);
  }-*/;
}
