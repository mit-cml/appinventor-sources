// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.Ev3BinaryParser;
import com.google.appinventor.components.runtime.util.Ev3Constants;

/**
 * ![EV3 component icon](images/legoMindstormsEv3.png)
 *
 * A component that provides a high-level interface to a LEGO MINDSTORMS EV3
 * robot, which provides sound functionalities.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@DesignerComponent(version = YaVersion.EV3_SOUND_COMPONENT_VERSION,
                   description = "A component that provides a high-level interface to " +
                                 "sound functionalities on LEGO MINDSTORMS EV3 robot.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3Sound extends LegoMindstormsEv3Base {
  /**
   * Creates a new Ev3Sound component.
   */
  public Ev3Sound(ComponentContainer container) {
    super(container, "Ev3Sound");
  }

  /**
   * Make the robot play a tone.
   */
  @SimpleFunction(description = "Make the robot play a tone.")
  public void PlayTone(int volume, int frequency, int milliseconds) {
    String functionName = Thread.currentThread().getStackTrace()[1].getMethodName();

    if (volume < 0 || volume > 100 || frequency < 250 || frequency > 10000 || milliseconds < 0 || milliseconds > 0xffff) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.SOUND,
                                                         true,
                                                         0,
                                                         0,
                                                         "cccc",
                                                         (byte) Ev3Constants.SoundSubcode.TONE,
                                                         (byte) volume,
                                                         (short) frequency,
                                                         (short) milliseconds);
    sendCommand(functionName, command, true);
  }

  /**
   * Stop any sound on the robot.
   */
  @SimpleFunction(description = "Stop any sound on the robot.")
  public void StopSound() {
    String functionName = Thread.currentThread().getStackTrace()[1].getMethodName();
    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.SOUND,
                                                         false,
                                                         0,
                                                         0,
                                                         "c",
                                                         Ev3Constants.SoundSubcode.BREAK);
    sendCommand(functionName, command, false);
  }
}
