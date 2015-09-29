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
class NxtPaletteHelper extends OrderedPaletteHelper {
  private static final List<String> nxtComponentNames = new ArrayList<String>();
  static {
    // First, high-level component to drive robot..
    nxtComponentNames.add("NxtDrive");
    // Then, sensors.
    nxtComponentNames.add("NxtColorSensor");
    nxtComponentNames.add("NxtLightSensor");
    nxtComponentNames.add("NxtSoundSensor");
    nxtComponentNames.add("NxtTouchSensor");
    nxtComponentNames.add("NxtUltrasonicSensor");
    // Then, low level components.
    nxtComponentNames.add("NxtDirectCommands");
  }

  NxtPaletteHelper() {
    super(nxtComponentNames);
  }
}
