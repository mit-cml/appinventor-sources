// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtSensorType type used by the NxtDirectCommands component.
 * 
 * <p>Info attained from the "LEGO MINDSTORMS NXT Direct Command" document.
 * http://kio4.com/b4a/programas/Appendix%202-LEGO%20MINDSTORMS%20NXT%20Direct%20commands.pdf
 * http://www.ni.com/pdf/manuals/372574c.pdf
 * In combination with the documentation here:
 * https://www.mindstorms.rwth-aachen.de/documents/downloads/doc/version-4.07/help/NXT_SetInputMode.html
 */
public enum NxtSensorType implements OptionList<Integer> {
  NoSensor(0x00),
  Touch(0x01),
  LightOn(0x05),
  LightOff(0x06),
  SoundDB(0x07),  // Decibels.
  SoundDBA(0x08),  // A-weighted Decibels
  ColorFull(0x0D),
  ColorRed(0x0E),  // Light sensor mode w/ red light on.
  ColorGreen(0x0F),  // Light sensor mode w/ green light on.
  ColorBlue(0x10),  // Light sensor mode w/ blue light on.
  ColorNone(0x11),  // Light sensor mode w/ no light on.
  Digital12C(0x0A),
  Digital12C9V(0x0B),
  // These are old RCX sensors, but they are compatible with Nxt.
  RcxTemperature(0x02),
  RcxLight(0x03),
  RcxAngle(0x04);

  private final int value;

  NxtSensorType(int type) {
    this.value = type;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NxtSensorType> lookup = new HashMap<>();

  static {
    for (NxtSensorType type : NxtSensorType.values()) {
      lookup.put(type.toUnderlyingValue(), type);
    }
  }

  public static NxtSensorType fromUnderlyingValue(Integer type) {
    return lookup.get(type);
  }
}
