// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.Ev3BinaryParser;
import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.google.appinventor.components.common.YaVersion;

/**
 * A component that provides a high-level interface to mailbox communication with a
 * LEGO MINDSTORMS EV3 robot.
 *
 * @author tasosggps@gmail.com (Gkagkas Anastasios)
 */

@DesignerComponent(version = YaVersion.EV3_MAILBOX_COMPONENT_VERSION,
                   description = "A component that provides high level interfaces to a LEGO MINDSTORMS EV3 " +
                   "robot, with functions that can send messages to the Ev3 mailbox.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3Mailbox extends LegoMindstormsEv3Base {

  public Ev3Mailbox(ComponentContainer container) {
    super(container, "Ev3Mailbox");
  }

  @SimpleFunction(description = "Send a text message")
  public void SendMailboxText(String name, String message) {
    String functionName = "SendMailboxText";
    byte[] command = Ev3BinaryParser.encodeSystemCommand(Ev3Constants.SystemCommand.WRITEMAILBOX,
                                                         false,
                                                         (byte) (name.length()+1),
                                                         name,
                                                         (short) (message.length()+1),
                                                         message);
    sendCommand(functionName, command, false);
  }

  @SimpleFunction(description = "Send a numeric message")
  public void SendMailboxNumeric(String name, float message) {
    String functionName = "SendMailboxNumeric";
    int hexMessage = Float.floatToIntBits(message);
    byte[] command = Ev3BinaryParser.encodeSystemCommand(Ev3Constants.SystemCommand.WRITEMAILBOX,
                                                         false,
                                                         (byte) (name.length()+1),
                                                         name,
                                                         (short) 4,
                                                         hexMessage);
    sendCommand(functionName, command, false);
  }

  @SimpleFunction(description = "Send a boolean message")
  public void SendMailboxBoolean(String name, boolean message) {
    String functionName = "SendMailboxBoolean";
    int hexMessage = message ? 1:0;
    byte[] command = Ev3BinaryParser.encodeSystemCommand(Ev3Constants.SystemCommand.WRITEMAILBOX,
                                                         false,
                                                         (byte) (name.length()+1),
                                                         name,
                                                         (short) 1,
                                                         (byte) hexMessage);
    sendCommand(functionName, command, false);
  }
}
