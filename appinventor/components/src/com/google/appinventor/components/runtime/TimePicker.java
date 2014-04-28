// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.app.Dialog;
import android.app.TimePickerDialog;
import java.util.Calendar;


/**
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
    " dialog to allow the user to select a time.</p>")
@SimpleObject
public class TimePicker extends ButtonBase {

  private int hour = 0;
  private int minute = 0;
  private TimePickerDialog time;

  /**
   * Create a new TimePicker component.
   *
   * @param container the parent container.
   */
  public TimePicker(ComponentContainer container) {
    super(container);
    final Calendar c = Calendar.getInstance();
    hour = c.get(Calendar.HOUR_OF_DAY);
    minute = c.get(Calendar.MINUTE);
    time = new TimePickerDialog(this.container.$context(),
        timePickerListener, hour, minute, false);
  }


  /**
  * Returns the hour of the time that was last picked using the timepicker.
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
  * Returns the hour of the time that was last picked using the timepicker.
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

  @Override
  public void click() {
    time.show();
  }

  private TimePickerDialog.OnTimeSetListener timePickerListener =
      new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(android.widget.TimePicker view, int selectedHour,
            int selectedMinute) {
              hour = selectedHour;
              minute = selectedMinute;
              time.updateTime(hour, minute);
              AfterTimeSet();
        }
      };

  /**
  * Indicates the user has set the time.
  */
  @SimpleEvent(description="This event is run when a user has set the time in the popup dialog.")
  public void AfterTimeSet() {
    EventDispatcher.dispatchEvent(this, "AfterTimeSet");
  }
}
