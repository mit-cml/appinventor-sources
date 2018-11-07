// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//Component Created by Rachael T

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.YaVersion;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Environment;
import android.widget.FrameLayout;
import android.provider.MediaStore;
import android.util.Log;
import android.net.Uri;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;
import android.view.View;

/**
 * This component allows App inventor to support gamepad control by grabbing KeyEvents before they can reach the form's handler.
 */
@DesignerComponent(version = 1,
   description = "A component that allows Gamepad Controller support for your app",
   category = ComponentCategory.EXTENSION,
   nonVisible = true,
   iconName = "images/extension.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")

public class GamePad extends AndroidNonvisibleComponent
    implements Component, KeyEvent.Callback, android.view.View.OnGenericMotionListener {


  protected final Form form;


  // Keypad Input variables
  public boolean a;
  private boolean b;
  private boolean x;
  private boolean y;
  private boolean start;
  private boolean select;
  private boolean right_bumper;
  private boolean left_bumper;
  private boolean dpad_up;
  private boolean dpad_down;
  private boolean dpad_right;
  private boolean dpad_left;

  // Motion event input variables
  private float left_joystick_x;
  private float left_joystick_y;
  private float right_joystick_x;
  private float right_joystick_y;
  private float right_trigger;
  private float left_trigger;

  /**
   * Creates a Gamepad component.
   *
   * @param container container, component will be placed in
   */
  public GamePad(ComponentContainer container) {
    super(container.$form());
    this.form = container.$form();
  }

  /*This constructor is for testing purposes only */
  public GamePad(Form form){
    super(form);
    this.form = form;
  }


  /**
    * This method sorts out keycodes to register key presses on the gamepad
    */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
      if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
        a = true;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
        b = true;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
        x = true;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
        y = true;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
        right_bumper = true;
      } 
      if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
        left_bumper = true;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_START) {
        start = true;
      } 
      if (keyCode == KeyEvent.KEYCODE_BUTTON_SELECT) {
        select = true;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
        dpad_up = true;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
        dpad_down = true;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
        dpad_left = true;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
        dpad_right = true;
      }
    }
    //Report that the key press was handled
    return true;
  }

  /**
    * This method sorts out keycodes to register key releases on the gamepad
    */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event){
    if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
      if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
        a = false;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
        b = false;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
        x = false;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
        y = false;
      } 
      if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
        right_bumper = false;
      } 
      if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
        left_bumper = false;
      }
      if (keyCode == KeyEvent.KEYCODE_BUTTON_START) {
        start = false;
      } 
      if (keyCode == KeyEvent.KEYCODE_BUTTON_SELECT) {
        select = false;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
        dpad_up = false;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
        dpad_down = false;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
        dpad_left = false;
      }
      if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
        dpad_right = false;
      }
    }
    //Report that the key release was handled
    return true;
  }

  //Necessary to implement the Keycode.Callback interface, but not used by the method.
  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event){
    return true;
  }

  @Override
  public boolean onKeyMultiple(int keyCode, int count, KeyEvent event){
    return true;
  }

  //This method dispatches motion events to be processed one at a time
  @Override
  public boolean onGenericMotion(View v, MotionEvent event) {
    // Check that the event came from a game controller
    if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {
      // Process all historical movement samples in the batch
      final int historySize = event.getHistorySize();
      // Process the movements starting from the earliest historical position in the batch
      for (int i = 0; i < historySize; i++) {
        // Process the event at historical position i
        processJoystickInput(event, i);
      }
      // Process the current movement sample in the batch (position -1)
      processJoystickInput(event, -1);
    }
    return true;
  }

//This process motion events for the joystick and DPAD (some DPADs use keycodes, some use an axis)
public void processJoystickInput(MotionEvent event, int historyPos) {

    left_joystick_x = event.getAxisValue(MotionEvent.AXIS_X);;
    left_joystick_y = event.getAxisValue(MotionEvent.AXIS_Y);
    right_joystick_x = event.getAxisValue(12);
    right_joystick_y = event.getAxisValue(13);

    right_trigger = event.getAxisValue(MotionEvent.AXIS_RZ);
    left_trigger = event.getAxisValue(MotionEvent.AXIS_Z);

    float xaxis = event.getAxisValue(MotionEvent.AXIS_HAT_X);
    float yaxis = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

    if (Float.compare(xaxis, -1.0f) == 0) {
        dpad_left = true;
        dpad_right = false;
    } else if (Float.compare(xaxis, 1.0f) == 0) {
        dpad_right = true;
        dpad_left = false;
    } else {
        dpad_right = false;
        dpad_left = false;
    }

    if (Float.compare(yaxis, -1.0f) == 0) {
        dpad_up = true;
        dpad_down = false;
    } else if (Float.compare(yaxis, 1.0f) == 0) {
        dpad_down = true;
        dpad_up = false;
    } else {
        dpad_up = false;
        dpad_down = false;
    }
}

/**
   * Returns true if the A button is pressed on the gamepad
   *
   * @return {@code true} the A button is pressed {@code false} the A button is not pressed
   */
@SimpleProperty (description = "Is the A button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean AButton(){
      return a;
  }

/**
   * Returns true if the B button is pressed on the gamepad
   *
   * @return {@code true} the B button is pressed {@code false} the B button is not pressed
   */
@SimpleProperty (description = "Is the B button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean BButton(){
      return b;
  }

/**
   * Returns true if the X button is pressed on the gamepad
   *
   * @return {@code true} the X button is pressed {@code false} the X button is not pressed
   */
@SimpleProperty (description = "Is the X button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean XButton(){
      return x;
  }

/**
   * Returns true if the Y button is pressed on the gamepad
   *
   * @return {@code true} the Y button is pressed {@code false} the Y button is not pressed
   */
@SimpleProperty (description = "Is the Y button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean YButton(){
      return y;
  }
 
/**
   * Returns true if the Right Bumper is pressed on the gamepad
   *
   * @return {@code true} the Right Bumper is pressed {@code false} the Right Bumper is not pressed
   */
@SimpleProperty (description = "Is the Right bumper pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean RightBumper(){
    return right_bumper;
  }

/**
   * Returns true if the Left Bumper is pressed on the gamepad
   *
   * @return {@code true} the Left Bumper is pressed {@code false} the Left Bumper is not pressed
   */
@SimpleProperty (description = "Is the Left bumper pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean LeftBumper(){
      return left_bumper;
  }

/**
   * Returns true if the Start button is pressed on the gamepad
   *
   * @return {@code true} the Start button is pressed {@code false} the Start button is not pressed
   */
@SimpleProperty (description = "Is the Start button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean Start(){
      return start;
  }

/**
   * Returns true if the Select button is pressed on the gamepad
   *
   * @return {@code true} the Select button is pressed {@code false} the Select button is not pressed
   */
@SimpleProperty (description = "Is the Select button pressed?", category = PropertyCategory.BEHAVIOR)
  public boolean Select(){
      return select;
  }

/**
   * Returns true if the DPAD Up is pressed on the gamepad
   *
   * @return {@code true} the DPAD Up is pressed {@code false} the DPAD Up is not pressed
   */
@SimpleProperty (description = "Is the DPAD pressed up?", category = PropertyCategory.BEHAVIOR)
  public boolean DpadUp(){
      return dpad_up;
  }

/**
   * Returns true if the DPAD Down is pressed on the gamepad
   *
   * @return {@code true} the DPAD Down is pressed {@code false} the DPAD Down is not pressed
   */
@SimpleProperty (description = "Is the DPAD pressed down?", category = PropertyCategory.BEHAVIOR)
  public boolean DpadDown(){
      return dpad_down;
  }

/**
   * Returns true if the DPAD Left is pressed on the gamepad
   *
   * @return {@code true} the DPAD Left is pressed {@code false} the DPAD Left is not pressed
   */
@SimpleProperty (description = "Is the DPAD pressed left?", category = PropertyCategory.BEHAVIOR)
  public boolean DpadLeft(){
      return dpad_left;
  }

/**
   * Returns true if the DPAD Right is pressed on the gamepad
   *
   * @return {@code true} the DPAD Right is pressed {@code false} the DPAD Right is not pressed
   */
@SimpleProperty (description = "Is the DPAD pressed right?", category = PropertyCategory.BEHAVIOR)
  public boolean DpadRight(){
      return dpad_right;
  }

/**
   * Returns the position of the Left joystick X-axis on the gamepad
   *
   * @return the float value of its x-axis position between 1 and -1
   */
@SimpleProperty (description = "The Left Joystick X value", category = PropertyCategory.BEHAVIOR)
  public float LeftJoystickX(){
      return left_joystick_x;
  }

/**
   * Returns the position of the Left joystick Y-axis on the gamepad
   *
   * @return the float value of its y-axis position between 1 and -1
   */
@SimpleProperty (description = "The Left Joystick Y value", category = PropertyCategory.BEHAVIOR)
  public float LeftJoystickY(){
      return left_joystick_y;
  }

/**
   * Returns the position of the Right joystick X-axis on the gamepad
   *
   * @return the float value of its x-axis position between 1 and -1
   */
@SimpleProperty (description = "The Right Joystick X value", category = PropertyCategory.BEHAVIOR)
  public float RightJoystickX(){
      return right_joystick_x;
  }

/**
   * Returns the position of the Right joystick Y-axis on the gamepad
   *
   * @return the float value of its y-axis position between 1 and -1
   */
@SimpleProperty (description = "The Right Joystick Y value", category = PropertyCategory.BEHAVIOR)
  public float RightJoystickY(){
      return right_joystick_y;
  }

/**
   * Returns the position of the Right Trigger on its axis
   *
   * @return the float value of its axis position between 1 and -1
   */
@SimpleProperty (description = "The Right Trigger value", category = PropertyCategory.BEHAVIOR)
  public float RightTrigger(){
      return right_trigger;
  }

/**
   * Returns the position of the Left Trigger on its axis
   *
   * @return the float value of its axis position between 1 and -1
   */
@SimpleProperty (description = "The Left Trigger value", category = PropertyCategory.BEHAVIOR)
  public float LeftTrigger(){
      return left_trigger;
  }

/**
  *This function enables the gamepad, and must be called before keycodes can be grabbed. 
  */
@SimpleFunction
public void EnableGamepad(){
  form.dontGrabKeyEventsForComponent(this);
  form.dontGrabMotionEventsForComponent(this);
}

/**
  *This function disables the gamepad, leaving the EventDispatcher to deal with any key presses
  */
@SimpleFunction
public void DisableGamepad(){
  form.dontGrabKeyEventsForComponent(null);
  form.dontGrabMotionEventsForComponent(null);
}



}
