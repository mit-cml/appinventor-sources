// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.text.format.DateFormat;
import android.os.Handler;

import java.util.Calendar;


/**
 * A button that, when clicked on, opens a dialog to allow the user to select a time.
 *
 * Note: Date and time are manipulated using methods in the [`Clock`](sensors.html#Clock) component.
 *
 * @internaldoc
 * A button allowing a user to launch the TimePickerDialog. This component is
 * is based off the ButtonBase class instead of the base Picker class because
 * unlike the other pickers, the TimePicker does not need to launch a new
 * activity and get a result. The TimePicker is launched as a dialog.
 *
 * @author vedharaju@gmail.com
 */
@DesignerComponent(version = YaVersion.TIMEPICKER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "<p>A button that, when clicked on, launches  a popup" +
    " dialog to allow the user to select a time.</p>",
    iconName = "images/timePicker.png")
@SimpleObject
public class TimePicker extends ButtonBase {

  private int hour = 0;
  private int minute = 0;
  private TimePickerDialog time;
  private boolean customTime = false;
  private Form form;
  private Calendar instant;
  private Handler androidUIHandler;

  /**
   * Create a new TimePicker component.
   *
   * @param container the parent container.
   */
  public TimePicker(ComponentContainer container) {
    super(container);
    form = container.$form();
    final Calendar c = Calendar.getInstance();
    hour = c.get(Calendar.HOUR_OF_DAY);
    minute = c.get(Calendar.MINUTE);
    time = new TimePickerDialog(this.container.$context(),
        timePickerListener, hour, minute, DateFormat.is24HourFormat(this.container.$context()));

    instant = Dates.TimeInstant(hour, minute);
    androidUIHandler = new Handler();

  }


  /**
  * Returns the hour of the time that was last picked using the `TimePicker``.
  * The time returned is always in the 24hour format.
  *
  * @return hour in 24-hour format
  */
  @SimpleProperty(
      description = "The hour of the last time set using the time picker." +
      " The hour is in a 24 hour format. If the last time set was 11:53 pm" +
      ", this property will return 23.",
      category = PropertyCategory.APPEARANCE)
  public int Hour() {
    return hour;
  }

  /**
  * Returns the hour of the time that was last picked using the `TimePicker`.
  * The time returned is always in the 24hour format.
  *
  * @return hour in 24-hour format
  */
  @SimpleProperty(
      description = "The minute of the last time set using the time picker",
      category = PropertyCategory.APPEARANCE)
  public int Minute() {
    return minute;
  }

  /**
   * Returns the instant in time that was last picked using the `TimePicker`.
   * @return instant of the date
   */
  @SimpleProperty(
    description = "The instant of the last time set using the time picker",
    category = PropertyCategory.APPEARANCE)
  public Calendar Instant() {
    return instant;
  }

  /**
   * Allows the user to set the time to be displayed when the `TimePicker` opens. Valid values for
   * the hour field are 0-23 and 0-59 for the second field.
   * @param hour
   * @param minute
   */
  @SimpleFunction(description="Set the time to be shown in the Time Picker popup. Current time is shown by default.")
  public void SetTimeToDisplay(int hour, int minute) {
    if ((hour < 0) || (hour > 23)) {
      form.dispatchErrorOccurredEvent(this, "SetTimeToDisplay", ErrorMessages.ERROR_ILLEGAL_HOUR);
    } else if ((minute < 0) || (minute > 59)) {
      form.dispatchErrorOccurredEvent(this, "SetTimeToDisplay", ErrorMessages.ERROR_ILLEGAL_MINUTE);
    } else {
      time.updateTime(hour, minute);
      instant = Dates.TimeInstant(hour, minute);
      customTime = true;
    }
  }

  /**
   * Allows the instant to set the hour and minute to be displayed when the `TimePicker` opens.
   * Instants are used in [`Clock`](sensors.html#Clock), {@link DatePicker}, and {@link TimePicker}
   * components.
   * @param instant
   */
  @SimpleFunction(description="Set the time from the instant to be shown in the Time Picker dialog. " +
    "Current time is shown by default.")
  public void SetTimeToDisplayFromInstant(Calendar instant) {
    int hour = Dates.Hour(instant);
    int minute = Dates.Minute(instant);
    time.updateTime(hour, minute);
    instant = Dates.TimeInstant(hour, minute);
    customTime = true;
  }

  /**
   * Launches the `TimePicker` dialog.
   */
  @SimpleFunction(description="Launches the TimePicker dialog.")
  public void LaunchPicker(){
    click();
  }

  @Override
  public void click() {
    if (!customTime) {
      Calendar c = Calendar.getInstance();
      int h = c.get(Calendar.HOUR_OF_DAY);
      int m = c.get(Calendar.MINUTE);
      time.updateTime(h, m);
      instant = Dates.TimeInstant(hour, minute);
    } else {
      customTime = false;
    }
    time.show();
  }

  private TimePickerDialog.OnTimeSetListener timePickerListener =
      new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(android.widget.TimePicker view, int selectedHour,
            int selectedMinute) {
          if (view.isShown()) {
            hour = selectedHour;
            minute = selectedMinute;
            instant = Dates.TimeInstant(hour, minute);
            // We post an event to the Android handler to do the App Inventor
            // event dispatch. This way it gets called outside of the context
            // of the timepicker's event. This permits the App Inventor dispatch
            // handler to re-launch this timepicker
            androidUIHandler.post(new Runnable() {
                public void run() {
                  AfterTimeSet();
                }
              });
          }
        }
      };

  /**
  * This event is run when a user has set the time in the popup dialog.
  */
  @SimpleEvent(description="This event is run when a user has set the time in the popup dialog.")
  public void AfterTimeSet() {
    EventDispatcher.dispatchEvent(this, "AfterTimeSet");
  }
}
