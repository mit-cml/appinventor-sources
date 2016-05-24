// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing temperature/humidity sensors connected to the UDOO board.
 *
 * @author francesco.monte@gmail.com
 */
public class YoungAndroidUdooTempHumSensorsChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] sensors = new Choice[] {
    new Choice("DHT11", "DHT11"),
    new Choice("DHT22", "DHT22")
  };

  public YoungAndroidUdooTempHumSensorsChoicePropertyEditor() {
    super(sensors);
  }
}
