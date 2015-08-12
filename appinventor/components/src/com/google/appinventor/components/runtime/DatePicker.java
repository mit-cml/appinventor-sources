// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.DatePickerDialog;
import android.os.Handler;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.Dates;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A button allowing a user to launch a DatePickerDialog. This component is
 * is based off the ButtonBase class instead of the base Picker class because
 * unlike the other pickers, the DatePicker does not need to launch a new
 * activity and get a result. The DatePicker is launched as a dialog.
 */
@DesignerComponent(version = YaVersion.DATEPICKER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "<p>A button that, when clicked on, launches a popup" +
        " dialog to allow the user to select a date.</p>")
@SimpleObject
public class DatePicker extends ButtonBase {

  private DatePickerDialog date;
  //month is the property that AI devs see, and it's always javaMonth + 1; month is 0-based in Java
  private int year, month, javaMonth, day;
  private Calendar instant;
  private String [] localizedMonths = new DateFormatSymbols().getMonths();
  private boolean customDate = false;
  private Form form;
  private Handler androidUIHandler;

  /**
   * Creates a new DatePicker component.
   * @param container the container in which the component will be placed in.
   */
  public DatePicker(ComponentContainer container) {
    super(container);
    form = container.$form();
    //Set the current date on creation
    final Calendar c = Calendar.getInstance();
    year = c.get(Calendar.YEAR);
    javaMonth = c.get(Calendar.MONTH);
    month = javaMonth + 1;
    day = c.get(Calendar.DAY_OF_MONTH);
    instant = Dates.DateInstant(year, month, day);
    date = new DatePickerDialog(this.container.$context(), datePickerListener, year, javaMonth,
        day);

    androidUIHandler = new Handler();

  }

  /**
   * Returns the Year that was last picked using the DatePicker.
   * @return the year in numeric format
   */
  @SimpleProperty(description = "the Year that was last picked using the DatePicker",
      category = PropertyCategory.APPEARANCE)
  public int Year() {
    return year;
  }

  /**
   * Returns the number of the Month that was last picked using the DatePicker.
   * @return the month in numeric format
   */
  @SimpleProperty(description = "the number of the Month that was last picked using the " +
      "DatePicker. Note that months start in 1 = January, 12 = December.",
      category = PropertyCategory.APPEARANCE)
  public int Month() {
    return month;
  }

  /**
   * Returns the name of the Month that was last picked using the DatePicker.
   * @return the month in textual format.
   */
  @SimpleProperty(description = "Returns the name of the Month that was last picked using the " +
      "DatePicker, in textual format.",
      category = PropertyCategory.APPEARANCE)
  public String MonthInText() {
    return localizedMonths[javaMonth];
  }

  /**
   * Returns the Day of the month that was last picked using the DatePicker.
   * @return the day in numeric format
   */
  @SimpleProperty(description = "the Day of the month that was last picked using the DatePicker.",
    category = PropertyCategory.APPEARANCE)
  public int Day() {
    return day;
  }

  /**
   * Returns instant of the date that was last picked using the DatePicker.
   * @return instant of the date
   */
  @SimpleProperty(description = "the instant of the date that was last picked using the DatePicker.",
    category = PropertyCategory.APPEARANCE)
  public Calendar Instant() {
    return instant;
  }

  @SimpleFunction(description = "Allows the user to set the date to be displayed when the date picker opens.\n" +
    "Valid values for the month field are 1-12 and 1-31 for the day field.\n")
  public void SetDateToDisplay(int year, int month, int day) {
    int jMonth = month - 1;
    try {
      GregorianCalendar cal = new GregorianCalendar(year, jMonth, day);
      cal.setLenient(false);
      cal.getTime();
    } catch (java.lang.IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, "SetDateToDisplay", ErrorMessages.ERROR_ILLEGAL_DATE);
    }
    date.updateDate(year, jMonth, day);
    instant = Dates.DateInstant(year, month, day);
    customDate = true;
  }

  @SimpleFunction(description = "Allows the user to set the date from the instant to be displayed when the date picker opens.")
  public void SetDateToDisplayFromInstant(Calendar instant) {
    int year = Dates.Year(instant);
    int month = Dates.Month(instant);
    int day = Dates.Day(instant);
    date.updateDate(year, month, day);
    instant = Dates.DateInstant(year, month, day);
    customDate = true;
  }

  @SimpleFunction(description="Launches the DatePicker popup.")
  public void LaunchPicker() {
    click();
  }

  /**
   * Overriding method from superclass to show the date picker dialog when the button is clicked
   */
  @Override
  public void click() {
    if (!customDate) {
      Calendar c = Calendar.getInstance();
      int year = c.get(Calendar.YEAR);
      int jMonth = c.get(Calendar.MONTH);
      int day = c.get(Calendar.DAY_OF_MONTH);
      date.updateDate(year, jMonth, day);
      instant = Dates.DateInstant(year, jMonth+1, day);
    } else {
      customDate = false;
    }
    date.show();
  }

  /**
   * Listener for the Dialog. It will update the fields after selection.
   */
  private DatePickerDialog.OnDateSetListener datePickerListener =
      new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker datePicker, int selectedYear,
                              int selectedMonth, int selectedDay) {
          if (datePicker.isShown()) {
            year = selectedYear;
            javaMonth = selectedMonth;
            month = javaMonth + 1;
            day = selectedDay;
            date.updateDate(year, javaMonth, day);
            instant = Dates.DateInstant(year, month, day);
            // We post an event to the Android handler to do the App Inventor
            // event dispatch. This way it gets called outside of the context
            // of the datepicker's event. This permits the App Inventor dispatch
            // handler to re-launch this date picker
            androidUIHandler.post(new Runnable() {
                public void run() {
                  AfterDateSet();
                }
              });
          }
        }
      };

  /**
   * Runs when the user sets the date in the Dialog.
   */
  @SimpleEvent(description = "Event that runs after the user chooses a Date in the dialog")
  public void AfterDateSet() {
    EventDispatcher.dispatchEvent(this, "AfterDateSet");
  }
}
