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
 * robot, which provides graphic functionalities.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@DesignerComponent(version = YaVersion.EV3_UI_COMPONENT_VERSION,
                   description = "A component that provides a high-level interface to a LEGO MINDSTORMS EV3 " +
                                 "robot, with functions to draw graphs on EV3 screen.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3UI extends LegoMindstormsEv3Base {

  /**
   * Creates a new Ev3UI component.
   */
  public Ev3UI(ComponentContainer container) {
    super(container, "Ev3UI");
  }

  /**
   * Draw a point on the screen.
   */
  @SimpleFunction(description = "Draw a point on the screen.")
  public void DrawPoint(int color, int x, int y) {
    String functionName = "DrawPoint";

    if (color != 0 && color != 1) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccc",
                                                         Ev3Constants.UIDrawSubcode.PIXEL,
                                                         (byte) color,
                                                         (short) x,
                                                         (short) y);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }

  /**
   * Draw a built-in icon on screen.
   */
  @SimpleFunction(description = "Draw a built-in icon on screen.")
  public void DrawIcon(int color, int x, int y, int type, int no) {
    String functionName = "DrawIcon";

    if (color != 0 && color != 1) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccccc",
                                                         Ev3Constants.UIDrawSubcode.ICON,
                                                         (byte) color,
                                                         (short) x,
                                                         (short) y,
                                                         type,
                                                         no);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }

  /**
   * Draw a line on the screen.
   */
  @SimpleFunction(description = "Draw a line on the screen.")
  public void DrawLine(int color, int x1, int y1, int x2, int y2) {
    String functionName = "DrawLine";

    if (color != 0 && color != 1) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccccc",
                                                         Ev3Constants.UIDrawSubcode.LINE,
                                                         (byte) color,
                                                         (short) x1,
                                                         (short) y1,
                                                         (short) x2,
                                                         (short) y2);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }

  /**
   * Draw a rectangle on the screen.
   */
  @SimpleFunction(description = "Draw a rectangle on the screen.")
  public void DrawRect(int color, int x, int y, int width, int height, boolean fill) {
    String functionName = "DrawRect";

    if (color != 0 && color != 1) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccccc",
                                                         fill ? Ev3Constants.UIDrawSubcode.FILLRECT : Ev3Constants.UIDrawSubcode.RECT,
                                                         (byte) color,
                                                         (short) x,
                                                         (short) y,
                                                         (short) width,
                                                         (short) height);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }

  /**
   * Draw a circle on the screen.
   */
  @SimpleFunction(description = "Draw a circle on the screen.")
  public void DrawCircle(int color, int x, int y, int radius, boolean fill) {
    String functionName = "DrawCircle";

    if (color != 0 && color != 1 || radius < 0) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccccc",
                                                         fill ? Ev3Constants.UIDrawSubcode.FILLCIRCLE : Ev3Constants.UIDrawSubcode.CIRCLE,
                                                         (byte) color,
                                                         (short) x,
                                                         (short) y,
                                                         (short) radius);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }

  /**
   * Fill the screen with a color.
   */
  @SimpleFunction(description = "Fill the screen with a color.")
  public void FillScreen(int color) {
    String functionName = "FillScreen";

    if (color != 0 && color != 1) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return;
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccc",
                                                         Ev3Constants.UIDrawSubcode.FILLWINDOW,
                                                         (byte) color,
                                                         (short) 0,
                                                         (short) 0);
    sendCommand(functionName, command, false);

    command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.UI_DRAW,
                                                  false,
                                                  0,
                                                  0,
                                                  "c",
                                                  Ev3Constants.UIDrawSubcode.UPDATE);
    sendCommand(functionName, command, false);
  }
}
