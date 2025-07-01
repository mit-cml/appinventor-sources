package java.util;

import com.google.gwt.core.client.JavaScriptObject;

public class GregorianCalendar extends Calendar {
  public GregorianCalendar() {
    super();
  }

  public GregorianCalendar(int year, int month, int dayOfMonth) {
    setTimeInMillis(new Date(year, month, dayOfMonth).getTime());
  }
}
