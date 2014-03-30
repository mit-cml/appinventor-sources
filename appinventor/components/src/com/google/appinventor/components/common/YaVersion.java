// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.common;

/**
 * Contains constants related to Young Android system, blocks, and component
 * version numbers.
 *
 */
public class YaVersion {
  private YaVersion() {
  }

  // ............................ Young Android System Version Number .............................

  // YOUNG_ANDROID_VERSION must be incremented when either the blocks language or a component
  // changes.
  // TODO(lizlooney) - should this version number be generated so that it is automatically
  // incremented when the blocks language or a component changes?

  // For YOUNG_ANDROID_VERSION 2:
  // - The Logger component was removed. The Notifier component should be used instead.
  // - TINYWEBDB_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 3:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 4:
  // - The LegoNxtConnection component was added.
  // For YOUNG_ANDROID_VERSION 5:
  // - The Camera component was added.
  // For YOUNG_ANDROID_VERSION 6:
  // - FORM_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 7:
  // - The Bluetooth component was added.
  // For YOUNG_ANDROID_VERSION 8:
  // - PLAYER_COMPONENT_VERSION was incremented to 2.
  // - SOUND_COMPONENT_VERSION was incremented to 2.
  // - VIDEOPLAYER_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 9:
  // - The LegoNxtConnection component was removed without backwards compatibility.
  // - The LegoMindstormsNxtDirect component was added.
  // - The LegoMindstormsNxtDrive component was added.
  // - The Bluetooth component was removed without backwards compatibility.
  // - The BluetoothClient component was added.
  // - The BluetoothServer component was added.
  // For YOUNG_ANDROID_VERSION 10:
  // - ACTIVITYSTARTER_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 11:
  // - BLUETOOTHCLIENT_COMPONENT_VERSION was incremented to 2.
  // - BLUETOOTHSERVER_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION_12:
  // - TWITTER_COMPONENT_VERSION was incremented to 2.
  // - The Twitter component was changed to support OAuth authentication.
  // For YOUNG_ANDROID_VERSION 13:
  // - The LegoMindstormsNxtTouchSensor component was added.
  // - The LegoMindstormsNxtLightSensor component was added.
  // - The LegoMindstormsNxtSoundSensor component was added.
  // - The LegoMindstormsNxtUltrasonicSensor component was added.
  // For YOUNG_ANDROID_VERSION 14:
  // - LegoMindstormsNXT* components were renamed.
  // For YOUNG_ANDROID_VERSION 15:
  // - TEXTBOX_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 16:
  // - FORM_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 17:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 18:
  // - ACTIVITYSTARTER_COMPONENT_VERSION was incremented to 3.
  // - BLUETOOTHCLIENT_COMPONENT_VERSION was incremented to 3.
  // - BLUETOOTHSERVER_COMPONENT_VERSION was incremented to 3.
  // - FORM_COMPONENT_VERSION was incremented to 4.
  // - PLAYER_COMPONENT_VERSION was incremented to 3.
  // - SOUND_COMPONENT_VERSION was incremented to 3.
  // - VIDEOPLAYER_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 19:
  // - The NxtColorSensor component was added.
  // For YOUNG_ANDROID_VERSION 20:
  // - The SoundRecorder component was added.
  // For YOUNG_ANDROID_VERSION 21:
  // - BUTTON_COMPONENT_VERSION was incremented to 2.
  // - CHECKBOX_COMPONENT_VERSION was incremented to 2.
  // - CONTACTPICKER_COMPONENT_VERSION was incremented to 2.
  // - EMAILPICKER_COMPONENT_VERSION was incremented to 2.
  // - IMAGEPICKER_COMPONENT_VERSION was incremented to 2.
  // - LABEL_COMPONENT_VERSION was incremented to 2.
  // - LISTPICKER_COMPONENT_VERSION was incremented to 2.
  // - PASSWORDTEXTBOX_COMPONENT_VERSION was incremented to 2.
  // - PHONENUMBERPICKER_COMPONENT_VERSION was incremented to 2.
  // - TEXTBOX_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 22:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 4.
  // - CANVAS_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 23:
  // - IMAGESPRITE_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 24:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 25:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 6.
  // For YOUNG_ANDROID_VERSION 26:
  // - In .scm files, values for asset, BluetoothClient, component, lego_nxt_sensor_port,
  // and string properties no longer contain leading and trailing quotes.
  // For YOUNG_ANDROID_VERSION 27:
  // - BLUETOOTHCLIENT_COMPONENT_VERSION was incremented to 4.
  // - BLUETOOTHSERVER_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 28:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 7.
  // For YOUNG_ANDROID_VERSION 29:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 8.
  // For YOUNG_ANDROID_VERSION 30:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 9.
  // For YOUNG_ANDROID_VERSION 31:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 10.
  // For YOUNG_ANDROID_VERSION 32:
  // - LISTPICKER_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 33:
  // - CANVAS_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 34:
  // - IMAGESPRITE_COMPONENT_VERSION was incremented to 3.
  // - BALL_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 35:
  // - FORM_COMPONENT_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 36:
  // - FusiontablesControl component was added
  // - BLOCKS_LANGUAGE_VERSION was incremented to 11 (CSV-related list functions)
  // For YOUNG_ANDROID_VERSION 37:
  // - CANVAS_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 38:
  // - CONTACTPICKER_COMPONENT_VERSION was incremented to 3.
  // - IMAGEPICKER_COMPONENT_VERSION was incremented to 3.
  // - LISTPICKER_COMPONENT_VERSION was incremented to 4.
  // - PHONENUMBERPICKER_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 39:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 12
  // For YOUNG_ANDROID_VERSION 40:
  // - BUTTON_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 41:
  // - FORM_COMPONENT_VERSION was incremented to 6.
  // - BLOCKS_LANGUAGE_VERSION was incremented to 13
  // For YOUNG_ANDROID_VERSION 42:
  // - The Web component was added.
  // For YOUNG_ANDROID_VERSION 43:
  // - BALL_COMPONENT_VERSION was incremented to 3.
  // - IMAGESPRITE_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 44:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 14
  // For YOUNG_ANDROID_VERSION 45:
  // - ORIENTATIONSENSOR_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 46:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 15.
  // For YOUNG_ANDROID_VERSION 47:
  // - WebViewer component was added
  // For YOUNG_ANDROID_VERSION 48:
  // - WEB_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 49:
  // - WEBVIEWER_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 50:
  // - TEXTBOX_COMPONENT_VERSION was incremented to 4:
  // For YOUNG_ANDROID_VERSION 51:
  // - CANVAS_COMPONENT_VERSION was incremented to 5.
  // - BLOCKS_LANGUAGE_VERSION was incremented to 16.
  // For YOUNG_ANDROID_VERSION 52:
  // - BLOCKS_LANGUAGE_VERSION was incremented to 17.
  // For YOUNG_ANDROID_VERSION 53:
  // - BLUETOOTHCLIENT_COMPONENT_VERSION was incremented to 5.
  // - BLUETOOTHSERVER_COMPONENT_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 54:
  // - BUTTON_COMPONENT_VERSION was incremented to 4.
  // - CONTACTPICKER_COMPONENT_VERSION was incremented to 4.
  // - IMAGEPICKER_COMPONENT_VERSION was incremented to 4.
  // - LISTPICKER_COMPONENT_VERSION was incremented to 5.
  // - PHONENUMBERPICKER_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 55:
  // - ACCELEROMETERSENSOR_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 56
  // - LOCATIONSENSOR_COMPONENT_VERSION was incremented to 2
  // For YOUNG_ANDROID_VERSION 57:
  // - PLAYER_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 58:
  // - FORM_COMPONENT_VERSION was incremented to 7.
  // For YOUNG_ANDROID_VERION 59:
  // - The Camcorder component was added.
  // For YOUNG_ANDROID_VERSION 60:
  // - VIDEOPLAYER_COMPONENT_VERSION was incremented to 4.
  // For YOUNG_ANDROID_VERSION 61:
  // - HORIZONTALARRANGEMENT_COMPONENT_VERSION was incremented to 2
  // - VERTICALARRANGEMENT_COMPONENT_VERSION was incremented to 2
  // - FORM_COMPONENT_VERSION was incremented to 8
  // For YOUNG_ANDROID_VERSION 62:
  // - BALL_COMPONENT_VERSION was incremented to 4.
  // - CANVAS_COMPONENT_VERSION was incremented to 6.
  // - IMAGESPRITE_COMPONENT_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 63:
  // - ACTIVITYSTARTER_COMPONENT_VERSION was incremented to 4.
  // - FORM_COMPONENT_VERSION was incremented to 9.
  // - LISTPICKER_COMPONENT_VERSION was incremented to 6.
  // For YOUNG_ANDROID_VERSION 64:
  // - FUSIONTABLESCONTROL_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 65:
  // - BALL_COMPONENT_VERSION was incremented to 5.
  // - CANVAS_COMPONENT_VERSION was incremented to 7.
  // - IMAGESPRITE_COMPONENT_VERSION was incremented to 6.
  // For YOUNG_ANDROID_VERSION 66:
  // - FORM_COMPONENT_VERSION was incremented to 10.
  // For YOUNG_ANDROID_VERSION 67:
  // - TEXTING_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 68:
  // - Phone Status Block was added.
  // For YOUNG_ANDROID_VERSION 69:
  // - IMAGEPICKER_COMPONENT_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 70:
  // - TEXTING_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 71:
  // - NOTIFIER_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 72:
  // - WEBVIEWER_COMPONENT_VERSION was incremented to 3
  // For YOUNG_ANDROID_VERSION 73:
  // - BUTTON_COMPONENT_VERSION was incremented to 5.
  // For YOUNG_ANDROID_VERSION 74:
  // - SLIDER_COMPONENT_VERSION was incremented to 1.
  // For YOUNG_ANDROID_VERSION 75:
  // - WEBVIEWER_COMPONENT_VERSION was incremented to 3
  // For YOUNG_ANDROID_VERSION 76:
  // - PLAYER_COMPONENT_VERSION was incremented to 5
  // For YOUNG_ANDROID_VERSION 77:
  // - TWITTER_COMPONENT_VERSION was incremented to 3
  // For YOUNG_ANDROID_VERSION 78:
  // - NEARFIELD_COMPONENT_VERSION was incremented to 1
  // For YOUNG_ANDROID_VERSION 79:
  // - FORM_COMPONENT_VERSION was incremented to 11.
  // For YOUNG_ANDROID_VERSION 80:
  // - LISTPICKER_COMPONENT_VERSION was incremented to 7.
  // For YOUNG_ANDROID_VERSION 81:
  // - NOTIFIER_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 82:
  // - ACCELEROMETERSENSOR_COMPONENT_VERSION was incremented to 3.
  // For YOUNG_ANDROID_VERSION 83:
  // - LISTPICKER_COMPONENT_VERSION was incremented to 8.
  // For YOUNG_ANDROID_VERSION 84:
  // - FORM_COMPONENT_VERSION was incremented to 12.
  // For YOUNG_ANDROID_VERSION 85:
  // - CAMERA_COMPONENT_VERSION was incremented to 2.
  // For YOUNG_ANDROID_VERSION 86:
  // - VIDEOPLAYER_COMPONENT_VERSION was incremented to 5.
  // - The Sharing Component was added
  // For YOUNG_ANDROID_VERSION 87:
  // - WEBVIEWER_COMPONENT_VERSION was incremented to 4
  // For YOUNG_ANDROID_VERSION 88:
  // - SPINNER_COMPONENT_VERSION was incremented to 1
  // For YOUNG_ANDROID_VERSION 89:
  // - LISTVIEW_COMPONENT_VERSION was incremented to 1.
  // For YOUNG_ANDROID_VERSION 90:
  // - TEXTTOSPEECH_COMPONENT_VERSION was incremented to 2

    public static final int YOUNG_ANDROID_VERSION = 90;

  // ............................... Blocks Language Version Number ...............................

  // NOTE(lizlooney,user) - when the blocks language changes:
  // 1. Increment YOUNG_ANDROID_VERSION above.
  // 2. Increment BLOCKS_LANGUAGE_VERSION here
  // 3. ***Add code in yacodeblocks.BlockSaveFile#upgradeLanguage to upgrade the .blk file contents
  // 4. Add code in YoungAndroidFormUpgrader to upgrade the source file
  // *** BlockSaveFile is no longer used in App Inventor 2 (Feb. 2014)

  // For BLOCKS_LANGUAGE_VERSION 2:
  // - Allow arguments of different procedures and events to have the same names.
  // For BLOCKS_LANGUAGE_VERSION 3:
  // - Some String operations were added: text<, text=, text>, trim, upcase, downcase
  // For BLOCKS_LANGUAGE_VERSION 4:
  // Added: replace all, copy list, insert list item, for each in range
  // For BLOCKS_LANGUAGE_VERSION 5:
  // - The Math trigonometry functions' formal parameter names were changed, and two
  //   blocks (degrees-to-radians and radians-to-degrees) were added.
  // For BLOCKS_LANGUAGE_VERSION 6:
  // - Text blocks, comments, and complaints are encoded on save and decoded on load to
  // preserve international characters.
  // For BLOCKS_LANGUAGE_VERSION 7:
  // - Corrupted character sequences in comments are replaced with * when .blk files are upgraded.
  // For BLOCKS_LANGUAGE_VERSION 8:
  // - Socket labels of some text blocks were changed.
  // For BLOCKS_LANGUAGE_VERSION 9:
  // - Socket labels for degrees-to-radians and radians-to-degrees were fixed.
  // For BLOCKS_LANGUAGE_VERSION 10:
  // - Added not-equal block.  Add "as" descriptor to def block.
  // For BLOCKS_LANGUAGE_VERSION 11:
  // - CSV-related list functions were added (list to csv row, list to csv table,
  //   list from csv row, list from csv table)
  // For BLOCKS_LANGUAGE_VERSION 12:
  // - Changed multiply symbol from star to times; change subtract symbol from hyphen to minus
  // For BLOCKS_LANGUAGE_VERSION 13:
  // - Added open-screen and open-screen-with-start-text.
  // For BLOCKS_LANGUAGE_VERSION 14:
  // - Added generated blocks for component object methods and properties.
  // For BLOCKS_LANGUAGE_VERSION 15:
  // - Added "is text empty?" to Text drawer.
  // For BLOCKS_LANGUAGE_VERSION 16:
  // - Added make-color and split-color to Color drawer.
  // For BLOCKS_LANGUAGE_VERSION 17:
  // - Changed open-screen to open-another-screen
  // - Changed open-screen-with-start-text to open-another-screen-with-start-value
  // - Marked get-startup-text as a bad block
  // - Added get-start-value
  // - Added get-plain-start-text
  // - Marked close-screen-with-result as a bad block
  // - Added close-screen-with-value
  // - Added close-screen-with-plain-text

  public static final int BLOCKS_LANGUAGE_VERSION = 17;

  // ................................. Component Version Numbers ..................................

  // NOTE(lizlooney,user) - when a new component is added:
  // 1. Increment YOUNG_ANDROID_VERSION above.
  // 2. Add the version number for the new component below

  // NOTE(lizlooney,user) - when a component changes:
  // 1. Increment YOUNG_ANDROID_VERSION above.
  // 2. Increment the version number for that component below
  // 3. Add code in com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader#
  //    upgradeComponentProperties to upgrade the .scm file contents
  // 4. Add code in openblocks.yacodeblocks.BlockSaveFile#upgradeComponentBlocks to
  //    upgrade the .blk file contents

  //For ACCELEROMETERSENSOR_COMPONENT_VERSION 2:
  // - AccelerometerSensor.MinimumInterval property was added.
  // - AccelerometerSensor.AccelerationChanged method was modified to wait for
  //   the minimum interval to elapse before calling a shaking event when necessary.
  //For ACCELEROMETERSENSOR_COMPONENT_VERSION 3:
  // - AccelerometerSensor.Sensitivty property was added.
  public static final int ACCELEROMETERSENSOR_COMPONENT_VERSION = 3;

  // For ACTIVITYSTARTER_COMPONENT_VERSION 2:
  // - The ActivityStarter.DataType, ActivityStarter.ResultType, and ActivityStarter.ResultUri
  //   properties were added.
  // - The ActivityStarter.ResolveActivity method was added.
  // - The ActivityStarter.ActivityError event was added.
  // For ACTIVITYSTARTER_COMPONENT_VERSION 3:
  // - The ActivityStarter.ActivityError event was marked userVisible false and is no longer used.
  // For ACTIVITYSTARTER_COMPONENT_VERSION 4:
  // - The ActivityStarter.StartActivity was edited to use the parent Form's open screen
  //   animation to transition to next activity.
  public static final int ACTIVITYSTARTER_COMPONENT_VERSION = 4;

  // For BALL_COMPONENT_VERSION 2:
  // - The PointTowards method was added (for all sprites)
  // - The heading property was changed from int to double (for all sprites)
  // For BALL_COMPONENT_VERSION 3:
  // - The Z property was added (also for ImageSprite)
  // For BALL_COMPONENT_VERSION 4:
  // - The TouchUp, TouchDown, and Flung events were added. (for all sprites)
  // For BALL_COMPONENT_VERSION 5:
  // - Callback parameters speed and heading were added to Flung. (for all sprites)
  public static final int BALL_COMPONENT_VERSION = 5;

  public static final int BARCODESCANNER_COMPONENT_VERSION = 1;

  // For BLUETOOTHCLIENT_COMPONENT_VERSION 2:
  // - The BluetoothClient.Enabled property was added.
  // For BLUETOOTHCLIENT_COMPONENT_VERSION 3:
  // - The BluetoothClient.BluetoothError event was marked userVisible false and is no longer used.
  // For BLUETOOTHCLIENT_COMPONENT_VERSION 4:
  // - The BluetoothClient.DelimiterByte property was added.
  // For BLUETOOTHCLIENT_COMPONENT_VERSION 5:
  // - The BluetoothClient.Secure property was added.
  public static final int BLUETOOTHCLIENT_COMPONENT_VERSION = 5;

  // For BLUETOOTHSERVER_COMPONENT_VERSION 2:
  // - The BluetoothServer.Enabled property was added.
  // For BLUETOOTHSERVER_COMPONENT_VERSION 3:
  // - The BluetoothServer.BluetoothError event was marked userVisible false and is no longer used.
  // For BLUETOOTHSERVER_COMPONENT_VERSION 4:
  // - The BluetoothServer.DelimiterByte property was added.
  // For BLUETOOTHSERVER_COMPONENT_VERSION 5:
  // - The BluetoothServer.Secure property was added.
  public static final int BLUETOOTHSERVER_COMPONENT_VERSION = 5;

  // For BUTTON_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For BUTTON_COMPONENT_VERSION 3:
  // - The LongClick event was added.
  // For BUTTON_COMPONENT_VERSION 4:
  // - The Shape property was added.
  // For BUTTON_COMPONENT_VERSION 5:
  // - The ShowFeedback property was added.
  public static final int BUTTON_COMPONENT_VERSION = 5;

  public static final int CAMCORDER_COMPONENT_VERSION = 1;

  // For CAMERA_COMPONENT_VERSION 2:
  // - The UseFront property was added.
   public static final int CAMERA_COMPONENT_VERSION = 2;

  // For CANVAS_COMPONENT_VERSION 2:
  // - The LineWidth property was added.
  // For CANVAS_COMPONENT_VERSION 3:
  // - The FontSize property was added.
  // - The TextAlignment property was added.
  // - The DrawText method was added.
  // - The DrawTextAtAngle method was added.
  // For CANVAS_COMPONENT_VERSION 4:
  // - Added Save and SaveAs methods
  // For CANVAS_COMPONENT_VERSION 5:
  // - Added GetBackgroundPixelColor, GetPixelColor, and SetBackgroundPixelColor methods.
  // For CANVAS_COMPONENT_VERSION 6:
  // - Added TouchDown, TouchUp, and Flung events.
  // For CANVAS_COMPONENT_VERSION 7:
  // - Callback parameters speed and heading were added to Flung. (for all sprites)
  public static final int CANVAS_COMPONENT_VERSION = 7;

  // For CHECKBOX_COMPONENT_VERSION 2:
  // - The Value property was renamed to Checked.
  public static final int CHECKBOX_COMPONENT_VERSION = 2;

  public static final int CLOCK_COMPONENT_VERSION = 1;

  // For CONTACTPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For CONTACTPICKER_COMPONENT_VERSION 3:
  // - The method Open was added.
  // For CONTACTPICKER_COMPONENT_VERSION 4:
  // - The Shape property was added.
  public static final int CONTACTPICKER_COMPONENT_VERSION = 4;

  // For EMAILPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  public static final int EMAILPICKER_COMPONENT_VERSION = 2;

  // For FORM_COMPONENT_VERSION 2:
  // - The Screen.Scrollable property was added.
  // For FORM_COMPONENT_VERSION 3:
  // - The Screen.Icon property was added.
  // For FORM_COMPONENT_VERSION 4:
  // - The Screen.ErrorOccurred event was added.
  // For FORM_COMPONENT_VERSION 5:
  // - The Screen.ScreenOrientation property and Screen.ScreenOrientationChanged event were added.
  // For FORM_COMPONENT_VERSION 6:
  // - The SwitchForm and SwitchFormWithArgs methods were removed and the OtherScreenClosed event
  // was added.
  // For FORM_COMPONENT_VERSION 7:
  // - The VersionCode and VersionName properties were added.
  // For FROM_COMPONENT_VERSION 8:
  // - The AlignHorizontal property was added
  // - The AlignVertical property was added
  // For FORM_COMPONENT_VERSION 9:
  // - The OpenScreenAnimation property was added
  // - The CloseScreenAnimation property was added
  // For FORM_COMPONENT_VERSION 10:
  // - The BackPressed event was added.
  // For FORM_COMPONENT_VERSION 11:
  // - OpenScreenAnimation and CloseScreenAnimation are now properties.
  // For FORM_COMPONENT_VERSION 12:
  // - AboutScreen property was added
  public static final int FORM_COMPONENT_VERSION = 12;

  // For FUSIONTABLESCONTROL_COMPONENT_VERSION 2:
  // - The Fusiontables API was migrated from SQL to V1
  public static final int FUSIONTABLESCONTROL_COMPONENT_VERSION = 2;

  public static final int GAMECLIENT_COMPONENT_VERSION = 1;

  // For HORIZONTALARRANGEMENT_COMPONENT_VERSION 2:
  // - The AlignHorizontal property was added
  // - The AlignVertical property was added
    public static final int HORIZONTALARRANGEMENT_COMPONENT_VERSION = 2;

  public static final int IMAGE_COMPONENT_VERSION = 1;

  // For IMAGEPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For IMAGEPICKER_COMPONENT_VERSION 3:
  // - The method Open was added.
  // For IMAGEPICKER_COMPONENT_VERSION 4:
  // - The Shape property was added.
  // For IMAGEPICKER_COMPONENT_VERSION 5:
  // - The ImagePath property was changed to Selection, and now returns a file path to
  //   external storage

  public static final int IMAGEPICKER_COMPONENT_VERSION = 5;

  // For IMAGESPRITE_COMPONENT_VERSION 2:
  // - The Rotates property was added.
  // For IMAGESPRITE_COMPONENT_VERSION 3:
  // - The PointTowards method was added (for all sprites)
  // - The heading property was changed from int to double (for all sprites)
  // For IMAGESPRITE_COMPONENT_VERSION 4:
  // - The Z property was added (also for Ball)
  // For IMAGESPRITE_COMPONENT_VERSION 5:
  // - The TouchUp, TouchDown, and Flung events were added. (for all sprites)
  // For IMAGESPRITE_COMPONENT_VERSION 6:
  // - Callback parameters speed and heading were added to Flung. (for all sprites)
  public static final int IMAGESPRITE_COMPONENT_VERSION = 6;

  // For LABEL_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  public static final int LABEL_COMPONENT_VERSION = 2;

  public static final int NXT_COLORSENSOR_COMPONENT_VERSION = 1;

  public static final int NXT_DIRECT_COMMANDS_COMPONENT_VERSION = 1;

  public static final int NXT_DRIVE_COMPONENT_VERSION = 1;

  public static final int NXT_LIGHTSENSOR_COMPONENT_VERSION = 1;

  public static final int NXT_SOUNDSENSOR_COMPONENT_VERSION = 1;

  public static final int NXT_TOUCHSENSOR_COMPONENT_VERSION = 1;

  public static final int NXT_ULTRASONICSENSOR_COMPONENT_VERSION = 1;

  // For LISTPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For LISTPICKER_COMPONENT_VERSION 3:
  // - The SelectionIndex read-write property was added.
  // For LISTPICKER_COMPONENT_VERSION 4:
  // - The method Open was added.
  // For LISTPICKER_COMPONENT_VERSION 5:
  // - The Shape property was added.
  // For LISTPICKER_COMPONENT_VERSION 6:
  // - The getIntent method was modified to provide the ListPickerActivity
  //   with the parent Form's open screen animation.
  // For LISTPICKER_COMPONENT_VERSION 7:
  // - Added ShowFilterBar property
  // For LISTPICKER_COMPONENT_VERSION 8:
  // - Added title property

  public static final int LISTPICKER_COMPONENT_VERSION = 8;

  // For LOCATIONSENSOR_COMPONENT_VERSION 2:
  // - The TimeInterval and DistanceInterval properties were added.
  public static final int LOCATIONSENSOR_COMPONENT_VERSION = 2;

  // For NEARFIELD_COMPONENT_VERSION 1:
  public static final int NEARFIELD_COMPONENT_VERSION = 1;

  // For NOTIFIER_COMPONENT_VERSION 2:
  // - To ShowChooseDialog and ShowTextDialog, new arg was added to indicate if dialog is cancelable
  // For NOTIFIER_COMPONENT_VERSION 3:
  // - Added NotifierColor, TextColor and NotifierLength options
  public static final int NOTIFIER_COMPONENT_VERSION = 3;

  // For ORIENTATIONSENSOR_COMPONENT_VERSION = 2:
  // - The Yaw property was renamed to Azimuth.
  // - The yaw parameter to OrientationChanged was renamed to azimuth.
  public static final int ORIENTATIONSENSOR_COMPONENT_VERSION = 2;

  // For PASSWORDTEXTBOX_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  public static final int PASSWORDTEXTBOX_COMPONENT_VERSION = 2;

  public static final int PEDOMETER_COMPONENT_VERSION = 1;

  public static final int PHONECALL_COMPONENT_VERSION = 1;

  // For PHONENUMBERPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For PHONENUMBERPICKER_COMPONENT_VERSION 3:
  // - The method Open was added.
  // For PHONENUMBERPICKER_COMPONENT_VERSION 4:
  // - The Shape property was added.
  public static final int PHONENUMBERPICKER_COMPONENT_VERSION = 4;

  public static final int PHONESTATUS_COMPONENT_VERSION = 1;

  // For PLAYER_COMPONENT_VERSION 2:
  // - The Player.PlayerError event was added.
  // For PLAYER_COMPONENT_VERSION 3:
  // - The Player.PlayerError event was marked userVisible false and is no longer used.
  // For PLAYER_COMPONENT_VERSION 4:
  // - The Player.Completed event was added.
  // - The IsLooping property was added.
  // - The Volume property was added.
  // - The IsPlaying method was added.
  // For PLAYER_COMPONENT_VERSION 5:
  // - The IsLooping property was renamed to Loop.

  public static final int PLAYER_COMPONENT_VERSION = 5;

  public static final int SHARING_COMPONENT_VERSION = 1;

  // For SOUND_COMPONENT_VERSION 2:
  // - The Sound.SoundError event was added.
  // For SOUND_COMPONENT_VERSION 3:
  // - The Sound.SoundError event was marked userVisible false and is no longer used.
  public static final int SOUND_COMPONENT_VERSION = 3;

  public static final int SOUND_RECORDER_COMPONENT_VERSION = 1;

  public static final int SPEECHRECOGNIZER_COMPONENT_VERSION = 1;

  public static final int TABLEARRANGEMENT_COMPONENT_VERSION = 1;

  // For TEXTBOX_COMPONENT_VERSION 2:
  // - The TextBox.NumbersOnly property was added.
  // For TEXTBOX_COMPONENT_VERSION 3:
  // - The Alignment property was renamed to TextAlignment.
  // For TEXTBOX_COMPONENT_VERSION 4:
  // - The HideKeyboard method was added.
  // - The MultiLine property was added.
  public static final int TEXTBOX_COMPONENT_VERSION = 4;

  // For TEXTTOSPEECH_COMPONENT_VERSION, added speech pitch and rate
  public static final int TEXTTOSPEECH_COMPONENT_VERSION = 2;

  // For TEXTING_COMPONENT_VERSION 2:
  // Texting over Wifi was implemented using Google Voice
  // This works only on phones with 2.0 (Eclair) or higher.
  // It requires that the user has a Google Voice account
  // and has the mobile Voice app installed on the phone.
  // Access to Google Voice is controlled through OAuth 2.
  // For TEXTING_COMPONENT_VERISON 3:
  // - receivingEnabled is now an integer in the range 1-3
  //   instead of a boolean
  public static final int TEXTING_COMPONENT_VERSION = 3;

  public static final int TINYDB_COMPONENT_VERSION = 1;

  // For TINYWEBDB_COMPONENT_VERSION 2:
  // - The TinyWebDB.ShowAlert method was removed. Notifier.ShowAlert should be used instead.
  public static final int TINYWEBDB_COMPONENT_VERSION = 2;

  // For TWITTER_COMPONENT_VERSION 2:
  // - The Authorize method and IsAuthorized event handler were added to support
  //   OAuth authentication (now requred by Twitter). These
  //   should be used instead of Login and IsLoggedIn. Login is still there but
  //   calling Login pops up a notification to use Authorize. IsLoggedIn will
  //   be changed to IsAuthorized when the blocks file is upgraded.
  // - Added CheckAuthorized method to check whether the user is already
  //   logged in and call IsAuthorized if so. We save the accessToken across app
  //   invocations, so it is possible that the app already has authorization
  //   when it starts up.
  // - Added DeAuthorize method to forget authorization token (logout, effectively).
  // - Added ConsumerKey and ConsumerSecret designer properties (required for
  //   Authorize)
  // - Added Username read-only property that returns the Twitter username when
  //   the user is logged in.
  // - The friend timeline was changed to be a list of tuples (lists), where
  //   each sub-list is (username message). The old format was just a list
  //   of messages and didn't include the username associated with each message.
  // For TWITTER_COMPONENT_VERSION 3:
  // - The 'SetStatus' procedure has been changed to 'Tweet' to be more intuitive.
  // - Added 'TweetWithImage' which uploads an image to TwitPic and adds it to
  //   a tweet to allow a user to tweet with a picture. This requires a TwitPic_API_Key
  //   property.
  public static final int TWITTER_COMPONENT_VERSION = 3;

  // For VERTICALARRANGEMENT_COMPONENT_VERSION 2:
  // - The AlignHorizontal property was added
  // - The AlignVertical property was added
  public static final int VERTICALARRANGEMENT_COMPONENT_VERSION = 2;

  // For VIDEOPLAYER_COMPONENT_VERSION 2:
  // - The VideoPlayer.VideoPlayerError event was added.
  // For VIDEOPLAYER_COMPONENT_VERSION 3:
  // - The VideoPlayer.VideoPlayerError event was marked userVisible false and is no longer used.
  // For VIDEOPLAYER_COMPONENT_VERSION 4:
  // - The VideoPlayer.width and VideoPlayer.height variables were marked as user visible.
  // - The FullScreen property was added to the VideoPlayer.
  // For VIDEOPLAYER_COMPONENT_VERSION 5:
  // - The Volume property (setter only) was added to the VideoPlayer.
  public static final int VIDEOPLAYER_COMPONENT_VERSION = 5;

  public static final int VOTING_COMPONENT_VERSION = 1;

  // For WEB_COMPONENT_VERSION 2:
  // - The RequestHeaders and AllowCookies properties were added.
  // - The BuildPostData and ClearCookies methods were added.
  // - The existing PostText method was renamed to PostTextWithEncoding, and a new PostText
  //   method was added.
  // For WEB_COMPONENT_VERSION 3:
  // - PUT and DELETE Actions added (PutText, PutTextWithEncoding, PutFile, and Delete).
  public static final int WEB_COMPONENT_VERSION = 3;

  // For WEBVIEWER_COMPONENT_VERSION 2:
  // - The CanGoForward and CanGoBack methods were added
  // For WEBVIEWER_COMPONENT_VERSION 3:
  // - Add UsesLocation property to set location permissions
  // For WEBVIEWER_COMPONENT_VERSION 4:
  // - Add WebViewString
  public static final int WEBVIEWER_COMPONENT_VERSION = 4;

  // For SLIDER_COMPONENT_VERSION 1:
  // - Initial version.
  public static final int SLIDER_COMPONENT_VERSION = 1;

  // For SPINNER_COMPONENT_VERSION 1:
  public static final int SPINNER_COMPONENT_VERSION = 1;

  // For listView component Version
  public static final int LISTVIEW_COMPONENT_VERSION = 1;

  // Companion Versions and Update Information

  // The PREFERRED_COMPANION is displayed to the end-user if
  // they ask (via the Help->About menu) and if they are told
  // that they need to update their companion
  //
  // ACCEPTABLE_COMPANIONS is a list of Companion VersionNames
  // which are usable with this version of the system.
  //
  // COMPANION_UPDATE_URL is the URL used by the Companion
  // Update Mechanism to find the Companion to download.
  // Note: This new Companion needs to be signed by the same
  // key as the Companion it is replacing, as the Package Manager
  // is invoked from the running Companion.

  public static final String PREFERRED_COMPANION = "2.17ai2zx1";
  public static final String COMPANION_UPDATE_URL = "";
  public static final String [] ACCEPTABLE_COMPANIONS = { "2.15ai2", "2.15ai2zx1", "2.16ai2", "2.16ai2zx1", "2.17ai2", "2.17ai2zx1" };

  // Splash Screen Values
  public static final int SPLASH_SURVEY = 1;


}
