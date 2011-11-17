// Copyright 2010 Google Inc. All Rights Reserved.

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
  // - CANVAS_VERSION was incremented to 5.
  // - BLOCKS_LANGUAGE_VERSION was incremented to 16.

  public static final int YOUNG_ANDROID_VERSION = 51;

  // ............................... Blocks Language Version Number ...............................

  // NOTE(lizlooney,user) - when the blocks language changes:
  // 1. Increment YOUNG_ANDROID_VERSION above.
  // 2. Increment BLOCKS_LANGUAGE_VERSION here
  // 3. Add code in yacodeblocks.BlockSaveFile#upgradeLanguage to upgrade the .blk file contents
  // See also

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
  public static final int BLOCKS_LANGUAGE_VERSION = 16;

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

  public static final int ACCELEROMETERSENSOR_COMPONENT_VERSION = 1;

  // For ACTIVITYSTARTER_COMPONENT_VERSION 2:
  // - The ActivityStarter.DataType, ActivityStarter.ResultType, and ActivityStarter.ResultUri
  //   properties were added.
  // - The ActivityStarter.ResolveActivity method was added.
  // - The ActivityStarter.ActivityError event was added.
  // For ACTIVITYSTARTER_COMPONENT_VERSION 3:
  // - The ActivityStarter.ActivityError event was marked userVisible false and is no longer used.
  public static final int ACTIVITYSTARTER_COMPONENT_VERSION = 3;

  // For BALL_COMPONENT_VERSION 2:
  // - The PointTowards method was added (for all sprites)
  // - The heading property was changed from int to double (for all sprites
  // For BALL_COMPONENT_VERSION 3:
  // - The Z property was added (also for ImageSprite)
  public static final int BALL_COMPONENT_VERSION = 3;

  public static final int BARCODESCANNER_COMPONENT_VERSION = 1;

  // For BLUETOOTHCLIENT_COMPONENT_VERSION 2:
  // - The BluetoothClient.Enabled property was added.
  // For BLUETOOTHCLIENT_COMPONENT_VERSION 3:
  // - The BluetoothClient.BluetoothError event was marked userVisible false and is no longer used.
  // For BLUETOOTHCLIENT_COMPONENT_VERSION 4:
  // - The BluetoothClient.DelimiterByte propery was added.
  public static final int BLUETOOTHCLIENT_COMPONENT_VERSION = 4;

  // For BLUETOOTHSERVER_COMPONENT_VERSION 2:
  // - The BluetoothServer.Enabled property was added.
  // For BLUETOOTHSERVER_COMPONENT_VERSION 3:
  // - The BluetoothServer.BluetoothError event was marked userVisible false and is no longer used.
  // For BLUETOOTHSERVER_COMPONENT_VERSION 4:
  // - The BluetoothServer.DelimiterByte propery was added.
  public static final int BLUETOOTHSERVER_COMPONENT_VERSION = 4;

  // For BUTTON_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For BUTTON_COMPONENT_VERSION 3:
  // - The LongClick event was added.
  public static final int BUTTON_COMPONENT_VERSION = 3;

  public static final int CAMERA_COMPONENT_VERSION = 1;

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
  public static final int CANVAS_COMPONENT_VERSION = 5;

  // For CHECKBOX_COMPONENT_VERSION 2:
  // - The Value property was renamed to Checked.
  public static final int CHECKBOX_COMPONENT_VERSION = 2;

  public static final int CLOCK_COMPONENT_VERSION = 1;

  // For CONTACTPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For CONTACTPICKER_COMPONENT_VERSION 3:
  // - The method Open was added.
  public static final int CONTACTPICKER_COMPONENT_VERSION = 3;

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
  public static final int FORM_COMPONENT_VERSION = 6;

  public static final int FUSIONTABLESCONTROL_COMPONENT_VERSION = 1;

  public static final int GAMECLIENT_COMPONENT_VERSION = 1;

  public static final int HORIZONTALARRANGEMENT_COMPONENT_VERSION = 1;

  public static final int IMAGE_COMPONENT_VERSION = 1;

  // For IMAGEPICKER_COMPONENT_VERSION 2:
  // - The Alignment property was renamed to TextAlignment.
  // For IMAGEPICKER_COMPONENT_VERSION 3:
  // - The method Open was added.
  public static final int IMAGEPICKER_COMPONENT_VERSION = 3;

  // For IMAGESPRITE_COMPONENT_VERSION 2:
  // - The Rotates property was added.
  // For IMAGESPRITE_COMPONENT_VERSION 3:
  // - The PointTowards method was added (for all sprites)
  // - The heading property was changed from int to double (for all sprites)
  // For IMAGESPRITE_COMPONENT_VERSION 4:
  // - The Z property was added (also for Ball)
  public static final int IMAGESPRITE_COMPONENT_VERSION = 4;

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
  public static final int LISTPICKER_COMPONENT_VERSION = 4;

  public static final int LOCATIONSENSOR_COMPONENT_VERSION = 1;

  public static final int NOTIFIER_COMPONENT_VERSION = 1;

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
  public static final int PHONENUMBERPICKER_COMPONENT_VERSION = 3;

  // For PLAYER_COMPONENT_VERSION 2:
  // - The Player.PlayerError event was added.
  // For PLAYER_COMPONENT_VERSION 3:
  // - The Player.PlayerError event was marked userVisible false and is no longer used.
  public static final int PLAYER_COMPONENT_VERSION = 3;

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

  public static final int TEXTTOSPEECH_COMPONENT_VERSION = 1;

  public static final int TEXTING_COMPONENT_VERSION = 1;

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
  public static final int TWITTER_COMPONENT_VERSION = 2;

  public static final int VERTICALARRANGEMENT_COMPONENT_VERSION = 1;

  // For VIDEOPLAYER_COMPONENT_VERSION 2:
  // - The VideoPlayer.VideoPlayerError event was added.
  // For VIDEOPLAYER_COMPONENT_VERSION 3:
  // - The VideoPlayer.VideoPlayerError event was marked userVisible false and is no longer used.
  public static final int VIDEOPLAYER_COMPONENT_VERSION = 3;

  public static final int VOTING_COMPONENT_VERSION = 1;

  // For WEB_COMPONENT_VERSION 2:
  // - The RequestHeaders and AllowCookies properties were added.
  // - The BuildPostData and ClearCookies methods were added.
  // - The existing PostText method was renamed to PostTextWithEncoding, and a new PostText
  //   method was added.
  public static final int WEB_COMPONENT_VERSION = 2;

  // For WEBVIEWER_COMPONENT_VERSION 2:
  // - The CanGoForward and CanGoBack methods were added
  public static final int WEBVIEWER_COMPONENT_VERSION = 2;

}
