// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Notifier component displays alert messages and creates Android log entries through
 * the following methods:
 * <ul>
 * <li> ShowMessageDialog: user must dismiss the message by pressing a button.
 * <li> ShowChooseDialog: displays two buttons to let the user choose one of two responses,
 *      for example, yes or no, after which the AfterChoosing event is raised.
 * <li> ShowTextDialog: lets the user enter text in response to the message, after
 *      which the AfterTextInput event is raised.
 * <li> ShowAlert: displays an alert that goes away by itself after
 *      a short time.
 * <li> LogError: logs an error message to the Android log.
 * <li> LogInfo: logs an info message to the Android log.
 * <li> LogWarning: logs a warning message to the Android log.
 * </ul>
 *
 * @author halabelson@google.com (Hal Abelson)
 */

//TODO(halabelson): Change the dialog methods to be synchronous and return values rather
// than signaling events; or at least to use one-shot events, when we implement those.

//TODO(halabelson): Figure out how/if these dialogs should deal with onPause.

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "The Notifier component displays alert messages and creates Android log entries " +
                "through the following methods: " +
        "<ul>" +
        "<li> ShowMessageDialog: user must dismiss the message by pressing a button.</li>" +
        "<li> ShowChooseDialog: displays two buttons to let the user choose one of two responses, " +
        "for example, yes or no, after which the AfterChoosing event is raised.</li>" +
        "<li> ShowTextDialog: lets the user enter text in response to the message, after " +
        "which the AfterTextInput event is raised. " +
        "<li> ShowAlert: displays an alert that goes away by itself after a short time.</li>" +
        "<li> LogError: logs an error message to the Android log. </li>" +
        "<li> LogInfo: logs an info message to the Android log.</li>" +
        "<li> LogWarning: logs a warning message to the Android log.</li>" +
        "</ul>",
    nonVisible = true,
    iconName = "images/notifier.png")
@SimpleObject

public final class Notifier extends AndroidNonvisibleComponent implements Component {

  private static final String LOG_TAG = "Notifier";
  private final Activity activity;
  private final Handler handler;

  //Length of Notifier message display
  private int notifierLength = Component.TOAST_LENGTH_LONG;

  // Notifier background color
  private int backgroundColor = Color.DKGRAY;

  // Notifier text color
  private int textColor = Color.WHITE;

  /**
   * Creates a new Notifier component.
   *
   * @param container the enclosing component
   */
  public Notifier (ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    handler = new Handler();
  }

  /**
   * Display an alert dialog with a single button that dismisses the alert.
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param buttonText the text on the button
   */
  @SimpleFunction
  public void ShowMessageDialog(String message, String title, String buttonText) {
    oneButtonAlert(message, title, buttonText);
  }

  private void oneButtonAlert(String message, String title, String buttonText) {
    Log.i(LOG_TAG, "One button alert " + message);
    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setMessage(message);
    alertDialog.setButton(buttonText, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
      }});
    alertDialog.show();
  }


  /**
   * Displays an alert with two buttons that have specified text, and additional button
   * marked CANCEL if cancelable is set.
   * Raises the AfterChoosing event when the choice has been made, and returns the text of
   * the button that was pressed.
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param button1Text the text on the left-hand button
   * @param button2Text the text on the right-hand button
   * @param cancelable indicates if additional CANCEL button should be added
   */
  @SimpleFunction
  public void ShowChooseDialog(String message, String title, String button1Text,
      String button2Text, boolean cancelable) {
    twoButtonAlert(message, title, button1Text, button2Text, cancelable);
  }

  private void twoButtonAlert(String message,  String title,
       final String button1Text,  final String button2Text, boolean cancelable) {
    Log.i(LOG_TAG, "ShowChooseDialog: " + message);

    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, button1Text,
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        AfterChoosing(button1Text);
      }
    });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, button2Text,
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        AfterChoosing(button2Text);
      }
    });

      //If cancelable is true, then a 3rd button, with text of Cancel will be added
      // and will raise AfterChoosing when pressed.
      if (cancelable)  {
            final String cancelButtonText="Cancel";
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancelButtonText,
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      AfterChoosing(cancelButtonText);
                  }
              });
      }

    alertDialog.show();
  }

  /**
   * Event after the user has made a selection for ShowChooseDialog.
   * @param choice is the text on the button the user pressed
   */
  @SimpleEvent
  public void AfterChoosing(String choice) {
    EventDispatcher.dispatchEvent(this, "AfterChoosing", choice);
  }

  /**
   * Shows a dialog box in which the user can enter text, after which the
   * AfterTextInput event is raised.
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param cancelable indicates whether the user should be able to cancel out of dialog.
   *                   When true, an additional CANCEL button will be added allowing user to cancel
   *                   out of dialog. On selection, will raise AfterTextInput with text of CANCEL.
   */
  @SimpleFunction
  public void ShowTextDialog(String message, String title, boolean cancelable) {
    textInputAlert(message, title, cancelable);
  }

  /**
   * Display an alert with a text entry. If cancelable is true, then also displays a "CANCEL"
   * button, allowing user to cancel out of dialog.
   * Raises the AfterTextInput event when the text has been entered and the user presses "OK".
   * Raises the AfterTextInput event when users presses "CANCEL", passing CANCEL to AfterTextInput
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param cancelable indicates whether the user should be able to cancel out of dialog.
   *                   When true, an additional CANCEL button will be added allowing user to cancel
   *                   out of dialog. On selection, will raise AfterTextInput with text of CANCEL.
   */
  private void textInputAlert(String message, String title, boolean cancelable) {
    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);
    alertDialog.setMessage(message);
    // Set an EditText view to get user input
    final EditText input = new EditText(activity);
    alertDialog.setView(input);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setButton("OK",
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        AfterTextInput(input.getText().toString());
      }
    });

      //If cancelable, then add the CANCEL button
      if (cancelable)  {
          final String cancelButtonText="CANCEL";
          alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancelButtonText,
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      //User pressed CANCEL. Raise AfterTextInput with CANCEL
                      AfterTextInput(cancelButtonText);
                  }
              });
      }

    alertDialog.show();
  }

  /**
   * Event raised after the user has responded to ShowTextDialog.
   * @param response is the text that was entered
   */
  @SimpleEvent
  public void AfterTextInput(String response) {
    EventDispatcher.dispatchEvent(this, "AfterTextInput", response);
  }

  /**
   * Display a temporary notification
   *
   * @param notice the text of the notification
   */
  @SimpleFunction
  public void ShowAlert(final String notice) {
    handler.post(new Runnable() {
      public void run() {
        toastNow(notice);
      }
    });
  }

  /**
   * Specifies the length of time that the alert is shown -- either "short" or "long".
   *
   * @param length  Length of time that an alert is visible
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_TOAST_LENGTH,
      defaultValue = Component.TOAST_LENGTH_LONG + "")
  @SimpleProperty(
      userVisible = false)
  public void NotifierLength(int length){
    notifierLength = length;
  }

  @SimpleProperty(
      description="specifies the length of time that the alert is shown -- either \"short\" or \"long\".",
      category = PropertyCategory.APPEARANCE)
  public int NotifierLength() {
    return notifierLength;
  }

  /**
   * Specifies the letr's background color.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
  @SimpleProperty(description="Specifies the alert's background color.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
  }

  /**
   * Returns the alert's text color.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty(description = "Specifies the alert's text color.",
      category = PropertyCategory.APPEARANCE)
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the alert's text color.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
    textColor = argb;
  }

  // show a toast using a TextView, which allows us to set the
  // font size.  The default toast is too small.
  private void toastNow (String message) {
    // The notifier font size for more recent releases seems too
    // small compared to early releases.
    // This sets the fontsize according to SDK level,  There is almost certainly
    // a better way to do this, with display metrics for example, but
    // I (hal) can't figure it out.
    int fontsize = (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH)
        ? 22 : 15;
    Toast toast = Toast.makeText(activity, message, notifierLength);
    toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
    TextView textView = new TextView(activity);
    textView.setBackgroundColor(backgroundColor);
    textView.setTextColor(textColor);
    textView.setTextSize(fontsize);
    Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    textView.setTypeface(typeface);
    textView.setPadding(10, 10, 10, 10);
    textView.setText(message);
    toast.setView(textView);
    toast.show();
  }

  /**
   * Log an error message.
   *
   * @param message the error message
   */
  @SimpleFunction(description = "Writes an error message to the Android log.")
  public void LogError(String message) {
    Log.e(LOG_TAG, message);
  }

  /**
   * Log a warning message.
   *
   * @param message the warning message
   */
  @SimpleFunction(description = "Writes a warning message to the Android log.")
  public void LogWarning(String message) {
    Log.w(LOG_TAG, message);
  }

  /**
   * Log an information message.
   *
   * @param message the information message
   */
  @SimpleFunction(description = "Writes an information message to the Android log.")
  public void LogInfo(String message) {
    Log.i(LOG_TAG, message);
  }
}
