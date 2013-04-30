// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockVisibleComponent;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.json.ClientJsonString;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.user.client.Window;

import java.util.Map;

/**
 * A class that can upgrade a Young Android Form source file.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YoungAndroidFormUpgrader {
  static class LoadException extends IllegalStateException {
    LoadException(String message) {
      super(message);
    }
  }

  private static final SimpleComponentDatabase COMPONENT_DATABASE =
      SimpleComponentDatabase.getInstance();

  private YoungAndroidFormUpgrader() {
  }

  /**
   * Upgrades the given sourceProperties if necessary.
   *
   * @param sourceProperties the properties from the source file
   * @return true if the sourceProperties was upgraded, false otherwise
   */
  public static boolean upgradeSourceProperties(Map<String, JSONValue> sourceProperties) {
    StringBuilder upgradeDetails = new StringBuilder();
    try {
      int srcYaVersion = getSrcYaVersion(sourceProperties);
      if (needToUpgrade(srcYaVersion)) {
        Map<String, JSONValue> formProperties =
            sourceProperties.get("Properties").asObject().getProperties();
        upgradeComponent(srcYaVersion, formProperties, upgradeDetails);
        // The sourceProperties were upgraded. Update the version number.
        setSrcYaVersion(sourceProperties);
        if (upgradeDetails.length() > 0) {
          Window.alert(MESSAGES.projectWasUpgraded(upgradeDetails.toString()));
        }
        return true;
      }
    } catch (LoadException e) {
      // This shouldn't happen. If it does it's our fault, not the user's fault.
      Window.alert(MESSAGES.unexpectedProblem(e.getMessage()));
      OdeLog.xlog(e);
    }
    return false;
  }

  private static int getSrcYaVersion(Map<String, JSONValue> sourceProperties) {
    int srcYaVersion = 0;
    if (sourceProperties.containsKey("YaVersion")) {
      String version = sourceProperties.get("YaVersion").asString().getString();
      srcYaVersion = Integer.parseInt(version);
    }
    return srcYaVersion;
  }

  private static void setSrcYaVersion(Map<String, JSONValue> sourceProperties) {
    sourceProperties.put("YaVersion", new ClientJsonString("" + YaVersion.YOUNG_ANDROID_VERSION));
  }

  private static boolean needToUpgrade(int srcYaVersion) {
    // Compare the source file's YoungAndroid version with the system's YoungAndroid version.
    final int sysYaVersion = YaVersion.YOUNG_ANDROID_VERSION;
    if (srcYaVersion > sysYaVersion) {
      // The source file's version is newer than the system's version.
      // This can happen if the user is using (or in the past has used) a non-production version of
      // App Inventor.
      // This can also happen if the user is connected to a new version of App Inventor and then
      // later is connected to an old version of App Inventor.
      // We'll try to load the project but there may be compatibility issues if the project uses
      // future components or other features that the current system doesn't understand.
      Window.alert(MESSAGES.newerVersionProject());
      return false;
    }

    if (srcYaVersion == 0) {
      // The source file doesn't have a YoungAndroid version number.
      // There are two situations that cause this:
      // 1. The project may have been downloaded from alpha (androidblocks.googlelabs.com) and
      // uploaded to beta (appinventor.googlelabs.com), which is illegal.
      // 2. The project may have been created with beta (appinventor.googlelabs.com) before we
      // started putting version numbers into the source file, which is legal, and nothing
      // really changed between version 0 and version 1.
      //
      // For a limited time, we assume #2, show a warning, and proceed.
      // TODO(lizlooney) - after the limited time is up (when we think that all appinventor
      // projects have been upgraded), we may decide to refuse to load the project.
      Window.alert(MESSAGES.veryOldProject());
    }

    return (srcYaVersion < sysYaVersion);
  }

  /*
   * Parses the JSON properties and upgrades the component if necessary.
   * This method is called recursively for nested components.
   */
  private static void upgradeComponent(int srcYaVersion,
      Map<String, JSONValue> componentProperties, StringBuilder upgradeDetails) {

    String componentType = componentProperties.get("$Type").asString().getString();

    // Get the source component version from the componentProperties.
    int srcCompVersion = 0;
    if (componentProperties.containsKey("$Version")) {
      String version = componentProperties.get("$Version").asString().getString();
      srcCompVersion = Integer.parseInt(version);
    }

    if (srcYaVersion < 2) {
      // In YOUNG_ANDROID_VERSION 2, the Logger component was removed; Notifier should be used
      // instead.
      // Here we change the Logger component to a Notifier component automatically. Sweet!
      // (We need to do this upgrade here, not in the upgradeComponentProperties method. This is
      // because the code below calls COMPONENT_DATABASE.getComponentVersion() and that will fail
      // if componentType is "Logger" because "Logger" isn't a valid component type anymore.)
      if (componentType.equals("Logger")) {
        componentType = "Notifier";
        srcCompVersion = COMPONENT_DATABASE.getComponentVersion(componentType);
        componentProperties.put("$Type", new ClientJsonString(componentType));
        componentProperties.put("$Version", new ClientJsonString("" + srcCompVersion));
        upgradeDetails.append(MESSAGES.upgradeDetailLoggerReplacedWithNotifier(
            componentProperties.get("$Name").asString().getString()));
      }
    }

    // Get the system component version from the component database.
    final int sysCompVersion = COMPONENT_DATABASE.getComponentVersion(componentType);

    // Upgrade if necessary.
    upgradeComponentProperties(componentProperties, componentType, srcCompVersion, sysCompVersion);

    if (srcYaVersion < 26) {
      // Beginning with YOUNG_ANDROID_VERSION 26:
      // - In .scm files, values for asset, BluetoothClient, component, lego_nxt_sensor_port,
      // and string properties no longer contain leading and trailing quotes.
      unquotePropertyValues(componentProperties, componentType);
    }

    // Upgrade nested components
    if (componentProperties.containsKey("$Components")) {
      JSONArray componentsArray = componentProperties.get("$Components").asArray();
      for (JSONValue nestedComponent : componentsArray.getElements()) {
        upgradeComponent(srcYaVersion, nestedComponent.asObject().getProperties(), upgradeDetails);
      }
    }
  }

  private static void upgradeComponentProperties(Map<String, JSONValue> componentProperties,
      String componentType, int srcCompVersion, final int sysCompVersion) {
    // Compare the source file's component version with the system's component version.
    if (srcCompVersion == 0) {
      // The source file doesn't have a version number for this component.
      // There are two situations that cause this:
      // 1. The project may have been downloaded from alpha (androidblocks.googlelabs.com) and
      // uploaded to beta (appinventor.googlelabs.com), which is illegal.
      // 2. The project may have been created with beta (appinventor.googlelabs.com) before we
      // started putting version numbers into the source file, which is legal, and nothing
      // really changed between version 0 and version 1.
      //
      // For a limited time, we assume #2 and pretend that the source file said version 1.
      // TODO(lizlooney) - after the limited time is up (when we think that all appinventor
      // projects have been upgraded), we may decide to refuse to load the project.
      srcCompVersion = 1;
    }

    if (srcCompVersion > sysCompVersion) {
      // This shouldn't happen because we should have already detected that the project is a newer
      // version than the system and returned false in needToUpgrade.
      // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
      // incrementing YaVersion.YOUNG_ANDROID_VERSION each time a component's version number is
      // incremented.
      throw new LoadException(
          MESSAGES.newerVersionComponentException(componentType, srcCompVersion, sysCompVersion));
    }

    if (srcCompVersion < sysCompVersion) {
      // NOTE(lizlooney,user) - when a component changes, increment the component's version
      // number in com.google.appinventor.components.common.YaVersion and add code here to upgrade
      // properties as necessary.
      if (componentType.equals("AccelerometerSensor")){
        srcCompVersion = upgradeAccelerometerSensorProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("ActivityStarter")) {
        srcCompVersion = upgradeActivityStarterProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Ball")) {
        srcCompVersion = upgradeBallProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("BluetoothClient")) {
        srcCompVersion = upgradeBluetoothClientProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("BluetoothServer")) {
        srcCompVersion = upgradeBluetoothServerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Slider")) {
        srcCompVersion = upgradeSliderProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Button")) {
        srcCompVersion = upgradeButtonProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Canvas")) {
        srcCompVersion = upgradeCanvasProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("CheckBox")) {
        srcCompVersion = upgradeCheckBoxProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("ContactPicker")) {
        srcCompVersion = upgradeContactPickerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("EmailPicker")) {
        srcCompVersion = upgradeEmailPickerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Form")) {
        srcCompVersion = upgradeFormProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("FusiontablesControl")) {
        srcCompVersion = upgradeFusiontablesControlProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("HorizontalArrangement")) {
        srcCompVersion = upgradeHorizontalArrangementProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("ImagePicker")) {
        srcCompVersion = upgradeImagePickerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("ImageSprite")) {
        srcCompVersion = upgradeImageSpriteProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Label")) {
        srcCompVersion = upgradeLabelProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("ListPicker")) {
        srcCompVersion = upgradeListPickerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("LocationSensor")) {
        srcCompVersion = upgradeLocationSensorProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("OrientationSensor")) {
        srcCompVersion = upgradeOrientationSensorProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("PasswordTextBox")) {
        srcCompVersion = upgradePasswordTextBoxProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("PhoneNumberPicker")) {
        srcCompVersion = upgradePhoneNumberPickerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Player")) {
        srcCompVersion = upgradePlayerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Sound")) {
        srcCompVersion = upgradeSoundProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("TinyWebDB")) {
        srcCompVersion = upgradeTinyWebDBProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("VerticalArrangement")) {
        srcCompVersion = upgradeVerticalArrangementProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("VideoPlayer")) {
        srcCompVersion = upgradeVideoPlayerProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("TextBox")) {
        srcCompVersion = upgradeTextBoxProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Texting")) {
        srcCompVersion = upgradeTextingProperties(componentProperties, srcCompVersion);

      }  else if (componentType.equals("Notifier")) {
        srcCompVersion = upgradeNotifierProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Twitter")) {
        srcCompVersion = upgradeTwitterProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("Web")) {
        srcCompVersion = upgradeWebProperties(componentProperties, srcCompVersion);

      } else if (componentType.equals("WebViewer")) {
        srcCompVersion = upgradeWebViewerProperties(componentProperties, srcCompVersion);

      }

      if (srcCompVersion < sysCompVersion) {
        // If we got here, a component needed to be upgraded, but nothing handled it.
        // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
        // adding the appropriate code above to handle all component upgrades.
        throw new LoadException(
            MESSAGES.noUpgradeStrategyException(componentType, srcCompVersion, sysCompVersion));
      }

      // The component was upgraded. Update the $Version property.
      componentProperties.put("$Version", new ClientJsonString("" + srcCompVersion));

    }
  }

  private static void unquotePropertyValues(Map<String, JSONValue> componentProperties,
      String componentType) {
    // From the component database, get the map of property names and types for the component type.
    Map<String, String> propertyTypesByName =
        COMPONENT_DATABASE.getPropertyTypesByName(componentType);

    // Iterate through the component properties.
    for (String propertyName : componentProperties.keySet()) {
      // Get the property type.
      String propertyType = propertyTypesByName.get(propertyName);
      // In theory the check for propertyType == null shouldn't be necessary
      // but I have sometimes had a problem with it being null when running
      // with GWT debugging. Maybe it changes the timing somehow. Anyway,
      // this test for null should not hurt anything. -Sharon
      if (propertyType == null) {
        OdeLog.wlog("Couldn't find propertyType for property " + propertyName +
            " in component type " + componentType);
        continue;
      }
      // If the property type is one that was previously quoted, unquote the value.
      if (propertyType.equals("asset") ||
          propertyType.equals("BluetoothClient") ||
          propertyType.equals("component") ||
          propertyType.equals("lego_nxt_sensor_port") ||
          propertyType.equals("string")) {
        // Unquote the property value.
        JSONValue propertyValue = componentProperties.get(propertyName);
        String propertyValueString = propertyValue.asString().getString();
        propertyValueString = StringUtils.unquote(propertyValueString);
        componentProperties.put(propertyName, new ClientJsonString(propertyValueString));
      }
    }
  }

  private static int upgradeAccelerometerSensorProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
          // The AccelerometerSensor.MinimumInterval property was added.
          // No properties need to be modified to upgrade to version 2.
          srcCompVersion = 2;
    }
        return srcCompVersion;
  }

  private static int upgradeActivityStarterProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The ActivityStarter.DataType, ActivityStarter.ResultType, and ActivityStarter.ResultUri
      // properties were added.
      // The ActivityStarter.ResolveActivity method was added.
      // The ActivityStarter.ActivityError event was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The ActivityStarter.ActivityError event was marked userVisible false and is no longer
      // used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The ActivityStarter.StartActivity method was modified to provide the parent Form's
      // screen animation type.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradeBallProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Heading property was changed from int to double
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Z property was added
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The TouchUp, TouchDown, and Flung events were added. (for all sprites)
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
      }
    if (srcCompVersion < 5) {
      // The callback parameters speed and heading were added to Flung.
      srcCompVersion = 5;
    }
    return srcCompVersion;
  }

  private static int upgradeBluetoothClientProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The BluetoothClient.Enabled property was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The BluetoothClient.BluetoothError event was marked userVisible false and is no longer
      // used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The BluetoothClient.DelimiterByte property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The BluetoothClient.Secure property was added.
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    return srcCompVersion;
  }

  private static int upgradeBluetoothServerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The BluetoothServer.Enabled property was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The BluetoothServer.BluetoothError event was marked userVisible false and is no longer
      // used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The BluetoothServer.DelimiterByte property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The BluetoothServer.Secure property was added.
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    return srcCompVersion;
  }
  private static int upgradeSliderProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 1) {
      // Initial version. Placeholder for future upgrades
      srcCompVersion = 1;
    }

    return srcCompVersion;
  }

  private static int upgradeButtonProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The LongClick event was added.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Shape property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The ShowFeedback property was added.
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    return srcCompVersion;
  }

  private static int upgradeCanvasProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The LineWidth property was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The FontSize and TextAlignment properties and
      // the DrawText and DrawTextAtAngle methods were added.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // No properties need to be modified to upgrade to version 4.
      // The Save and SaveAs methods were added.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // No properties need to be modified to upgrade to version 5.
      // The methods GetBackgroundPixelColor, GetPixelColor, and
      // SetBackgroundPixelColor were added.
      srcCompVersion = 5;
    }
    if (srcCompVersion < 6) {
      // No properties need to be modified to upgrade to version 6.
      // The events TouchDown, TouchUp, and Flung were added.
      srcCompVersion = 6;
    }
    if (srcCompVersion < 7) {
      // The callback parameters speed and heading were added to Flung.
      srcCompVersion = 7;
    }
    return srcCompVersion;
  }

  private static int upgradeCheckBoxProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Value property was renamed to Checked.
      handlePropertyRename(componentProperties, "Value", "Checked");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeContactPickerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Open method was added.  No changes are needed.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Shape property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradeEmailPickerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeFormProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Screen.Scrollable property was added.
      // If the form contains a direct child component whose height is set to fill parent,
      // we set the Scrollable property value to false.
      if (componentProperties.containsKey("$Components")) {
        JSONArray componentsArray = componentProperties.get("$Components").asArray();
        for (JSONValue nestedComponent : componentsArray.getElements()) {
          Map<String, JSONValue> nestedComponentProperties =
              nestedComponent.asObject().getProperties();
          if (nestedComponentProperties.containsKey("Height")) {
            JSONValue heightValue = nestedComponentProperties.get("Height");
            String heightString = heightValue.asString().getString();
            try {
              int height = Integer.parseInt(heightString);
              if (height == MockVisibleComponent.LENGTH_FILL_PARENT) {
                // Set the Form's Scrollable property to false.
                componentProperties.put("Scrollable", new ClientJsonString("False"));
                break;
              }
            } catch (NumberFormatException e) {
              // Ignore this. If we throw an exception here, the project is unrecoverable.
            }
          }
        }
      }
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Screen.Icon property was added.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Screen.ErrorOccurred event was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The Screen.ScreenOrientation property and Screen.ScreenOrientationChanged event were
      // added.
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    if (srcCompVersion < 6) {
      // The SwitchForm and SwitchFormWithArgs methods were removed and the OtherScreenClosed event
      // was added.
      srcCompVersion = 6;
    }
    if (srcCompVersion < 7) {
      // The VersionCode and VersionName properties were added. No properties need to be modified
      // to update to version 7.
      srcCompVersion = 7;
    }

    if (srcCompVersion < 8) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrade to version 8.
      srcCompVersion = 8;
    }
    if (srcCompVersion < 9) {
      // The OpenScreenAnimation and CloseScreenAnimation properties were added. No blocks need
      // to be modified to upgrade to version 9.
      srcCompVersion = 9;
    }
    if (srcCompVersion < 10) {
      // The BackPressed event was added. No blocks need to be modified to upgrade to version 10.
      srcCompVersion = 10;
    }
    return srcCompVersion;
  }

  private static int upgradeFusiontablesControlProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // No properties need to be modified to upgrade to version 2.
      // The ApiKey property and the SendQuery and ForgetLogin methods were added.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeHorizontalArrangementProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrqde to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeImagePickerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Open method was added.  No changes are needed.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Shape property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The ImagePath property was renamed to Selection.
      handlePropertyRename(componentProperties, "ImagePath", "Selection");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 5;
    }
    return srcCompVersion;
  }

  private static int upgradeImageSpriteProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The SpriteComponent.Rotates property was added
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Heading property was changed from int to Double
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Z property was added
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The TouchUp, TouchDown, and Flung events were added. (for all sprites)
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    if (srcCompVersion < 6) {
      // The callback parameters speed and heading were added to Flung.
      srcCompVersion = 6;
    }
    return srcCompVersion;
  }

  private static int upgradeLabelProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeListPickerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The SelectionIndex property was added.  No changes are needed.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Open method was added.  No changes are needed.
      srcCompVersion = 4;
    }
    if (srcCompVersion < 5) {
      // The Shape property was added.
      // No properties need to be modified to upgrade to version 5.
      srcCompVersion = 5;
    }
    if (srcCompVersion < 6) {
      // The getIntent method was modified to add the parent Form's screen
      // animation type. No properties need to be modified to upgrade to
      // version 6.
      srcCompVersion = 6;
    }
    return srcCompVersion;
  }

  private static int upgradeLocationSensorProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The TimeInterval and DistanceInterval properties were added.
      // No properties need to be modified to upgrade to Version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeOrientationSensorProperties(
      Map<String, JSONValue> componentProperties, int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Yaw property was renamed to Azimuth.
      handlePropertyRename(componentProperties, "Yaw", "Azimuth");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradePasswordTextBoxProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradePhoneNumberPickerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Open method was added.  No changes are needed.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Shape property was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradePlayerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Player.PlayerError event was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Player.PlayerError event was marked userVisible false and is no longer used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The Looping and Volume properties were added.
      // The Completed Event was added.
      // The IsPlaying method was added.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradeSoundProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The Sound.SoundError event was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Sound.SoundError event was marked userVisible false and is no longer used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    return srcCompVersion;
  }

  private static int upgradeTinyWebDBProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The TinyWebDB.ShowAlert method was removed. Notifier.ShowAlert should be used instead.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeVerticalArrangementProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrqde to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeNotifierProperties(Map<String, JSONValue> componentProperties,
                                                  int srcCompVersion) {
    if (srcCompVersion < 2) {
      // A new boolean socket was added to allow canceling out of ShowChooseDialog
      // and ShowTextDialog
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeVideoPlayerProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The VideoPlayer.VideoPlayerError event was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The VideoPlayer.VideoPlayerError event was marked userVisible false and is no longer used.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    if (srcCompVersion < 4) {
      // The VideoPlayer.height and VideoPlayer.width getter and setters were marked as
      // visible to the user.
      // The FullScreen property was created.
      // No properties need to be modified to upgrade to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradeTwitterProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The designer properties ConsumerKey and ConsumerSecret were added
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    return srcCompVersion;
  }

  private static int upgradeTextingProperties(Map<String, JSONValue> componentProperties,
                                              int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The designer property GoogleVoiceEnabled was added
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      if (componentProperties.containsKey("ReceivingEnabled")) {
        JSONValue receivingEnabled = componentProperties.get("ReceivingEnabled");
        String receivingString = receivingEnabled.asString().getString();
        if (receivingString.equals("true")) {
          componentProperties.put("ReceivingEnabled", new ClientJsonString("2"));
        } else {
          componentProperties.put("ReceivingEnabled", new ClientJsonString("1"));
        }
      }
      srcCompVersion = 3;
    }

    return srcCompVersion;
  }

  private static int upgradeTextBoxProperties(Map<String, JSONValue> componentProperties,
      int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The property (and designer property) TextBox.NumbersOnly was added
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentProperties, "Alignment", "TextAlignment");
      // Properties related to this component have now been upgraded to version 3.
      srcCompVersion = 3;
    }

    if (srcCompVersion < 4) {
      // The MultiLine property was added.
      // The default for Multiline from now on is false, but up until now,
      // all text boxes have been multiline.
      // We need to set the MultiLine to true when we upgrade old projects.
      componentProperties.put("MultiLine", new ClientJsonString("True"));
      // Properties related to this component have now been upgraded to version 4.
      srcCompVersion = 4;
    }
    return srcCompVersion;
  }

  private static int upgradeWebProperties(Map<String, JSONValue> componentProperties,
                                          int srcCompVersion) {
    if (srcCompVersion < 2) {
      // The RequestHeaders and AllowCookies properties were added.
      // The BuildPostData and ClearCookies methods were added.
      // The existing PostText method was renamed to PostTextWithEncoding, and a new PostText
      // method was added.
      // No properties need to be modified to upgrade to version 2.
      srcCompVersion = 2;
    }
    if (srcCompVersion < 3) {
      // The methods PutText, PutTextWithEncoding, PutFile and Delete were added.
      // The method BuildPostData was renamed to BuildRequestData.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    return srcCompVersion;
  }

  private static int upgradeWebViewerProperties(Map<String, JSONValue> componentProperties,
                                                int srcCompVersion) {
    if (srcCompVersion < 3) {
      // The CanGoForward and CanGoBack methods were added.
      // No properties need to be modified to upgrade to version 2.
      // UsesLocation property added.
      // No properties need to be modified to upgrade to version 3.
      srcCompVersion = 3;
    }
    return srcCompVersion;
  }

  private static void handlePropertyRename(Map<String, JSONValue> componentProperties,
      String oldPropName, String newPropName) {
    if (componentProperties.containsKey(oldPropName)) {
      componentProperties.put(newPropName, componentProperties.remove(oldPropName));
    }
  }
}
