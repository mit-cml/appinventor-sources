// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import java.util.ArrayList;
import java.util.List;

/**
 * The palette helper for the LEGO MINDSTORMS component category.
 */
class LegoPaletteHelper extends OrderedPaletteHelper {
  private static final List<String> legoComponentNames = new ArrayList<String>();
  static {
    // NXT actulators
    legoComponentNames.add("NxtDrive");

    // NXT sensors
    legoComponentNames.add("NxtColorSensor");
    legoComponentNames.add("NxtLightSensor");
    legoComponentNames.add("NxtSoundSensor");
    legoComponentNames.add("NxtTouchSensor");
    legoComponentNames.add("NxtUltrasonicSensor");

    // NXT low level components
    legoComponentNames.add("NxtDirectCommands");

    // EV3 components
    legoComponentNames.add("Ev3Motors");
    legoComponentNames.add("Ev3ColorSensor");
    legoComponentNames.add("Ev3GyroSensor");
    legoComponentNames.add("Ev3TouchSensor");
    legoComponentNames.add("Ev3UltrasonicSensor");
    legoComponentNames.add("Ev3Sound");
    legoComponentNames.add("Ev3UI");
    legoComponentNames.add("Ev3Commands");
  }

  LegoPaletteHelper() {
    super(legoComponentNames);
  }
}
