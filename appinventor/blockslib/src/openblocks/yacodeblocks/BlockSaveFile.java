// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockSaveFile is the saved form of the blocks workspace. This helper class
 * reads in the block save file and provides access to its version info.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author lizlooney@google.com (Liz Looney)
 */

public class BlockSaveFile {
  // Non-private so it can be accessed by BlockSaveFileTest.
  static final String BAD_BLOCK = "CompilerErrorMsg";

  private Document document;
  private Element documentRoot;
  private boolean wasUpgraded;

  public BlockSaveFile(Element langDefRoot, String contents) {
    final int sysYaVersion = WorkspaceUtils.getYoungAndroidVersion(langDefRoot);
    final int sysLangVersion = WorkspaceUtils.getBlocksLanguageVersion(langDefRoot);

    try {
      DocumentBuilder builder = WorkspaceUtils.newDocumentBuilder();
      document = builder.parse(new InputSource(new StringReader(contents)));
      documentRoot = document.getDocumentElement();

      if (needToUpgrade(getBlkYaVersion(), sysYaVersion)) {
        upgradeLanguage(getBlkLangVersion(), sysLangVersion);
        upgradeComponents();
        // The document was upgraded. Update the version number.
        setBlkYaVersion(sysYaVersion);
        wasUpgraded = true;
      }
    } catch (IllegalStateException e) {
      // Something (the blocks language or a component) needed to be upgraded, but no code handled
      // it.  If we correctly provided upgrade strategies and the user did not hack the source file,
      // this should not happen.
      e.printStackTrace();
      FeedbackReporter.showWarningMessage(
          "This project was saved in an older format, and cannot be upgraded at this time.");
    } catch (LoadException e) {
      // The blocks file is a newer version than the system.
      e.printStackTrace();
      FeedbackReporter.showWarningMessage(
          "This project was created with a newer version of the App Inventor system.");
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean wasUpgraded() {
    return wasUpgraded;
  }

  public Element getRoot() {
    return documentRoot;
  }

  private int getBlkYaVersion() {
    String version = documentRoot.getAttribute("ya-version");
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void setBlkYaVersion(int yaVersion) {
    documentRoot.setAttribute("ya-version", "" + yaVersion);
  }

  private int getBlkLangVersion() {
    String version = documentRoot.getAttribute("lang-version");
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private void setBlkLangVersion(int langVersion) {
    documentRoot.setAttribute("lang-version", "" + langVersion);
  }

  private boolean needToUpgrade(int blkYaVersion, final int sysYaVersion) {
    // Compare the .blk file's YoungAndroid version with the system's YoungAndroid version.
    if (blkYaVersion > sysYaVersion) {
      // The source file's version is newer than the system's version.
      // This can happen if the user is using (or in the past has used) a non-production version of
      // App Inventor.
      // This can also happen if the user is connected to a new version of App Inventor and then
      // later is connected to an old version of App Inventor.
      // We'll try to load the project but there may be compatibility issues if the project uses
      // future components or other features that the current system doesn't understand.
      FeedbackReporter.showWarningMessage(
          "This project was saved with a newer version of the App Inventor system. We\n" +
          "will attempt to load the project, but there may be compatibility issues.");
      return false;
    }

    if (blkYaVersion == 0) {
      // The .blk file doesn't have a YoungAndroid version number.
      // There are two situations that cause this:
      // 1. The project may have been downloaded from alpha (androidblocks.googlelabs.com) and
      // uploaded to beta (appinventor.googlelabs.com), which is illegal.
      // 2. The project may have been created with beta (appinventor.googlelabs.com) before we
      // started putting version numbers into the .blk file, which is legal, and nothing
      // really changed between version 0 and version 1.
      //
      // For a limited time, we assume #2 and proceed.
      // TODO(lizlooney) - after the limited time is up (when we think that all appinventor
      // projects have been upgraded), we may decide to refuse to load the project.
    }

    return (blkYaVersion < sysYaVersion);
  }

  private void upgradeLanguage(int blkLangVersion, final int sysLangVersion) throws LoadException {
    // Compare the .blk file's language version with the system's language version.
    if (blkLangVersion > sysLangVersion) {
      // This shouldn't happen because we should have already detected that the project is a newer
      // version than the system and returned false in needToUpgrade.
      // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
      // incrementing YaVersion.YOUNG_ANDROID_VERSION each time the blocks language version number
      // is incremented.
      throw new LoadException("Unable to load blocks file with blocks language version " +
          blkLangVersion + " (maximum known version is " + sysLangVersion + ").");
    }

    if (blkLangVersion == 0) {
      // The .blk file doesn't have a language version number.
      // There are two situations that cause this:
      // 1. The project may have been downloaded from alpha (androidblocks.googlelabs.com) and
      // uploaded to beta (appinventor.googlelabs.com), which is illegal.
      // 2. The project may have been created with beta (appinventor.googlelabs.com) before we
      // started putting version numbers into the .blk file, which is legal, and nothing
      // really changed between version 0 and version 1.
      //
      // For a limited time, we assume #2 and pretend that the .blk file said version 1.
      // TODO(lizlooney) - after the limited time is up (when we think that all appinventor
      // projects have been upgraded), we may decide to refuse to load the project.
      blkLangVersion = 1;
    }

    if (blkLangVersion < sysLangVersion) {
      // NOTE(lizlooney,user) - when the blocks language changes, increment
      // com.google.appinventor.components.common.YaVersion#BLOCKS_LANGUAGE_VERSION and add code
      // here to upgrade blocks as necessary.

      if (blkLangVersion < 2) {
        // In BLOCKS_LANGUAGE_VERSION 2, we allow arguments of different procedures and events
        // to have the same names.
        // No blocks need to be modified to upgrade to version 2.
        blkLangVersion = 2;
      }
      if (blkLangVersion < 3) {
        // In BLOCKS_LANGUAGE_VERSION 3, we added some string operations
        // No blocks need to be modified to upgrade to version 3.
        blkLangVersion = 3;
      }
      if (blkLangVersion < 4) {
        // In BLOCKS_LANGUAGE_VERSION 4, we added replace all, copy list,
        // insert list item, for range
        // No blocks need to be modified to upgrade to version 4.
        blkLangVersion = 4;
      }
      if (blkLangVersion < 5) {
        // In BLOCKS_LANGUAGE_VERSION 5, we changed some Math functions' formal parameter names.
        upgradeTrigBlocks();
        // Blocks have now been upgraded to language version 5.
        blkLangVersion = 5;
      }
      if (blkLangVersion < 6) {
        // Beginning in BLOCKS_LANGUAGE_VERSION 6, text blocks, comments, and complaints
        // are encoded on save and decoded on load to preserve international characters.
        encodeInternationalCharacters();
        // Blocks have now been upgraded to language version 6.
        blkLangVersion = 6;
      }
      if (blkLangVersion < 7) {
        // In BLOCKS_LANGUAGE_VERSION 7, corrupted character sequences in comments are replaced
        // with * when .blk files are upgraded.
        fixCorruptedComments();
        // Blocks have now been upgraded to language version 7.
        blkLangVersion = 7;
      }
      if (blkLangVersion < 8) {
        // In BLOCKS_LANGUAGE_VERSION 8, socket labels of some text blocks were changed.
        upgradeTextBlockSocketLabels();
        // Blocks have now been upgraded to language version 8.
        blkLangVersion = 8;
      }
      if (blkLangVersion < 9) {
        fixRadiansConversionBlocks();
        // Blocks have now been upgraded to language version 9.
        blkLangVersion = 9;
      }
      if (blkLangVersion < 10) {
        addAsLabelToDefBlocks();
        // Blocks have now been upgraded to language version 10.
        blkLangVersion = 10;
      }
      if (blkLangVersion < 11) {
        // In BLOCKS_LANGUAGE_VERSION 11, we added csv-related list operations.
        // No blocks need to be modified to upgrade to version 11.
        blkLangVersion = 11;
      }
      if (blkLangVersion < 12) {
        // In BLOCKS_LANGUAGE_VERSION 12, we changed the multiply
        // symbol from * star to times, and the subtract symbol
        // from hypen to minus
        changeStarAndHyphenToTimesAndMinusForMultiplyAndSubtractBlocks();
        // Blocks have now been upgraded to language version 12.
        blkLangVersion = 12;
      }
      if (blkLangVersion < 13) {
        // In BLOCKS_LANGUAGE_VERSION 13, we added open-screen and open-screen-with-start-text.
        // No blocks need to be modified to upgrade to version 13.
        blkLangVersion = 13;
      }
      if (blkLangVersion < 14) {
        // In BLOCKS_LANGUAGE_VERSION 14, we added property and method blocks for component objects.
        // No language blocks need to be modified to upgrade to version 14.
        blkLangVersion = 14;
      }
      if (blkLangVersion < 15) {
        // In BLOCKS_LANGUAGE_VERSION 15, we added "is text empty?" to
        // Text drawer.
        // No language blocks need to be modified to upgrade to version 15.
        blkLangVersion = 15;
      }
      if (blkLangVersion < 16) {
        // In BLOCKS_LANGUAGE_VERSION 16, we added make-color and split-color to the Color drawer.
        // No language blocks need to be modified to upgrade to version 16.
        blkLangVersion = 16;
      }
      if (blkLangVersion < 17) {
        // In BLOCKS_LANGUAGE_VERSION 17.
        // Changed open-screen to open-another-screen
        // Changed open-screen-with-start-text to open-another-screen-with-start-value
        // Marked get-startup-text as a bad block
        // Added get-start-value
        // Added get-plain-start-text
        // Marked close-screen-with-result as a bad block
        // Added close-screen-with-value
        // Added close-screen-with-plain-text
        changeGetStartTextAndOpenCloseScreenBlocks();
        blkLangVersion = 17;
      }

      if (blkLangVersion < sysLangVersion) {
        // If we got here, the blocks language needed to be upgraded, but nothing handled it.
        // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
        // adding the appropriate code above to handle all blocks language upgrades.
        throw new IllegalStateException("No upgrade strategy exists for blocks language" +
            " from version " + blkLangVersion + " to " + sysLangVersion + ".");
      }

      // The blocks language was upgraded. Update the version number.
      setBlkLangVersion(blkLangVersion);
    }
  }

  private void upgradeComponents() throws LoadException {
    int blkYaVersion = getBlkYaVersion();

    NodeList uuidEntryNodeList = document.getElementsByTagName("YoungAndroidUuidEntry");
    int length = uuidEntryNodeList.getLength();
    for (int i = 0; i < length; i++) {
      // All of our Nodes should also be Elements.  Skip any that aren't.
      if (uuidEntryNodeList.item(i) instanceof Element) {
        Element uuidEntry = (Element) uuidEntryNodeList.item(i);
        String genus = uuidEntry.getAttribute("component-genus");
        String componentName = uuidEntry.getAttribute("component-id");

        if (blkYaVersion < 2) {
          // In YOUNG_ANDROID_VERSION 2, the Logger component was removed;
          // Notifier should be used instead.
          // YoungAndroidFormUpgrader has changed all Logger components to
          // Notifier in the .scm file.
          if (genus.equals("Logger")) {
            // Call convertLoggerBlocks to change all Logger method calls
            // to Notifier method calls.
            convertLoggerBlocks(componentName);
            // Change the YoungAndroidUuidEntry attributes.
            genus = "Notifier";
            uuidEntry.setAttribute("component-genus", genus);
            uuidEntry.setAttribute("component-version",
                "" + WorkspaceUtils.getComponentVersion(genus));
            continue;
          }
        }

        if (blkYaVersion < 44) {
          // In YOUNG_ANDROID_VERSION 44, the Form component was renamed to Screen.
          handleFormToScreenConversion(componentName);
          // The genus of blocks related to the Screen now have "Screen-" as a prefix, not "Form-".
        }

        // Although Form was renamed to Screen in YOUNG_ANDROID_VERSION 44, the component-genus
        // attribute of YoungAndroidUuidEntry is still "Form".
        if (genus.equals("Form")) {
          // Here we are changing the local variable genus to "Screen" because we need to pass
          // "Screen" (not "Form") to upgradeComponentBlocks below. During the execution of
          // upgradeComponentBlocks, blocks that need to be upgraded are identified based on their
          // genus-name attribute. The genus of blocks related to the Screen now have "Screen-" as
          // a prefix, not "Form-".
          genus = "Screen";
        }

        String versionAttribute = uuidEntry.getAttribute("component-version");
        int blkCompVersion = (versionAttribute.length() > 0) ?
            Integer.parseInt(versionAttribute) : 0;
        final int sysCompVersion = WorkspaceUtils.getComponentVersion(genus);
        upgradeComponentBlocks(componentName, genus, blkCompVersion, sysCompVersion, uuidEntry);
      }
    }
  }

  private void handleFormToScreenConversion(String componentName) {
    // Change Form method and event blocks "Form-X" to "Screen-X".
    for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Form", "")) {
      String blockGenusName = block.getAttribute("genus-name");
      String methodOrEventName = blockGenusName.substring("Form".length());
      String newBlockGenusName = "Screen" + methodOrEventName;
      changeBlockGenusName(block, newBlockGenusName);
    }
  }

  private void upgradeComponentBlocks(String componentName, String genus, int blkCompVersion,
      final int sysCompVersion, Element uuidEntry) throws LoadException {

    // Compare the .blk file's component version with the system's component version.
    if (blkCompVersion > sysCompVersion) {
      // This shouldn't happen because we should have already detected that the project is a newer
      // version than the system and returned false in needToUpgrade.
      // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
      // incrementing YaVersion.YOUNG_ANDROID_VERSION each time a component's version number is
      // incremented.
      throw new LoadException("Unable to load blocks file with " + genus + " version " +
          blkCompVersion + " (maximum known version is " + sysCompVersion + ").");
    }

    if (blkCompVersion == 0) {
      // The .blk file doesn't have a version number for this component.
      // There are two situations that cause this:
      // 1. The project may have been downloaded from alpha (androidblocks.googlelabs.com) and
      // uploaded to beta (appinventor.googlelabs.com), which is illegal.
      // 2. The project may have been created with beta (appinventor.googlelabs.com) before we
      // started putting version numbers into the .blk file, which is legal, and nothing
      // really changed between version 0 and version 1.
      //
      // For a limited time, we assume #2 and pretend that the .blk file said version 1.
      // TODO(lizlooney) - after the limited time is up (when we think that all appinventor
      // projects have been upgraded), we may decide to refuse to load the project.
      blkCompVersion = 1;
    }

    if (blkCompVersion < sysCompVersion) {
      // NOTE(lizlooney,user) - when a component changes, increment the component's version
      // number in com.google.appinventor.components.common.YaVersion and add code here to upgrade blocks
      // as necessary.

      if (genus.equals("AccelerometerSensor")){
        blkCompVersion = upgradeAccelerometerSensorBlocks(blkCompVersion, componentName);

      } else if (genus.equals("ActivityStarter")) {
        blkCompVersion = upgradeActivityStarterBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Ball")) {
        blkCompVersion = upgradeBallBlocks(blkCompVersion, componentName);

      } else if (genus.equals("BluetoothClient")) {
        blkCompVersion = upgradeBluetoothClientBlocks(blkCompVersion, componentName);

      } else if (genus.equals("BluetoothServer")) {
        blkCompVersion = upgradeBluetoothServerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Slider")) {
        blkCompVersion = upgradeSliderBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Button")) {
        blkCompVersion = upgradeButtonBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Canvas")) {
        blkCompVersion = upgradeCanvasBlocks(blkCompVersion, componentName);

      } else if (genus.equals("CheckBox")) {
        blkCompVersion = upgradeCheckBoxBlocks(blkCompVersion, componentName);

      } else if (genus.equals("ContactPicker")) {
        blkCompVersion = upgradeContactPickerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("EmailPicker")) {
        blkCompVersion = upgradeEmailPickerBlocks(blkCompVersion, componentName);

        // Note that "Form" is converted to "Screen" above

      } else if (genus.equals("Screen")) {
        blkCompVersion = upgradeScreenBlocks(blkCompVersion, componentName);

      } else if (genus.equals("FusiontablesControl")) {
        blkCompVersion = upgradeFusiontablesControlBlocks(blkCompVersion, componentName);

      } else if (genus.equals("HorizontalArrangement")) {
        blkCompVersion = upgradeHorizontalArrangementBlocks(blkCompVersion, componentName);

      } else if (genus.equals("ImagePicker")) {
        blkCompVersion = upgradeImagePickerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("ImageSprite")) {
        blkCompVersion = upgradeImageSpriteBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Label")) {
        blkCompVersion = upgradeLabelBlocks(blkCompVersion, componentName);

      } else if (genus.equals("ListPicker")) {
        blkCompVersion = upgradeListPickerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("LocationSensor")) {
        blkCompVersion = upgradeLocationSensorBlocks(blkCompVersion, componentName);

      } else if (genus.equals("OrientationSensor")) {
        blkCompVersion = upgradeOrientationSensorBlocks(blkCompVersion, componentName);

      } else if (genus.equals("PasswordTextBox")) {
        blkCompVersion = upgradePasswordTextBoxBlocks(blkCompVersion, componentName);

      } else if (genus.equals("PhoneNumberPicker")) {
        blkCompVersion = upgradePhoneNumberPickerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Player")) {
        blkCompVersion = upgradePlayerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Sound")) {
        blkCompVersion = upgradeSoundBlocks(blkCompVersion, componentName);

      } else if (genus.equals("TextBox")) {
        blkCompVersion = upgradeTextBoxBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Texting")) {
        blkCompVersion = upgradeTextingBlocks(blkCompVersion, componentName);

      }  else if (genus.equals("Notifier")) {
        blkCompVersion = upgradeNotifierBlocks(blkCompVersion, componentName);

      } else if (genus.equals("TinyWebDB")) {
        blkCompVersion = upgradeTinyWebDBBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Twitter")) {
        blkCompVersion = upgradeTwitterBlocks(blkCompVersion, componentName);

      } else if (genus.equals("VerticalArrangement")) {
        blkCompVersion = upgradeVerticalArrangementBlocks(blkCompVersion, componentName);

      } else if (genus.equals("VideoPlayer")) {
        blkCompVersion = upgradeVideoPlayerBlocks(blkCompVersion, componentName);

      } else if (genus.equals("Web")) {
        blkCompVersion = upgradeWebBlocks(blkCompVersion, componentName);

      } else if (genus.equals("WebViewer")) {
        blkCompVersion = upgradeWebViewerBlocks(blkCompVersion, componentName);
      }

      if (blkCompVersion < sysCompVersion) {
        // If we got here, a component needed to be upgraded, but nothing handled it.
        // NOTE(lizlooney,user) - we need to make sure that this situation does not happen by
        // adding the appropriate code above to handle all component upgrades.
        throw new IllegalStateException("No block upgrade strategy exists for " + genus +
            " from version " + blkCompVersion + " to " + sysCompVersion + ".");
      }

      // This component was upgraded. Update the component-version attribute.
      uuidEntry.setAttribute("component-version", "" + blkCompVersion);
    }
  }

  private void convertLoggerBlocks(String componentName) {
    // Change Logger-Log method blocks for this component to Notifier-LogError.
    for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Logger", "Log")) {
      changeBlockGenusName(block, "Notifier-LogError");
      Node labelChild = getBlockLabelChild(block);
      String newLabel = componentName + ".LogError";
      labelChild.setNodeValue(newLabel);
      String oldConnectorLabel = "s";
      String newConnectorLabel = "message";
      changeFirstMatchingSocketBlockConnectorLabel(block, oldConnectorLabel, newConnectorLabel);
    }

    // Change Logger-Display method blocks for this component to Notifier-ShowAlert.
    for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Logger", "Display")) {
      changeBlockGenusName(block, "Notifier-ShowAlert");
      Node labelChild = getBlockLabelChild(block);
      String newLabel = componentName + ".ShowAlert";
      labelChild.setNodeValue(newLabel);
      String oldConnectorLabel = "s";
      String newConnectorLabel = "notice";
      changeFirstMatchingSocketBlockConnectorLabel(block, oldConnectorLabel, newConnectorLabel);
    }
  }

  private int upgradeAccelerometerSensorBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The AccelerometerSensor.MinimumInterval property was added.
          // No blocks need to be modified to upgrade to version 2.
          blkCompVersion = 2;
    }
        return blkCompVersion;
  }

  private int upgradeActivityStarterBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The ActivityStarter.DataType, ActivityStarter.ResultType, and ActivityStarter.ResultUri
      // properties were added.
      // The ActivityStarter.ResolveActivity method was added.
      // The ActivityStarter.ActivityError event was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The ActivityStarter.ActivityError event was marked userVisible false and is no longer
      // used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "ActivityStarter", "ActivityError")) {
        markBlockBad(block, "The ActivityStarter.ActivityError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The ActivityStarter.StartActivity method was modified to pull the parent Form's
      // screen animation type. No blockes need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }

  private int upgradeBallBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The PointTowards method was added (for all sprites).
      // The Heading property was changed from int to double (for all srites).
      // No blocks need to be modified to upgrade to version 2.
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Z property was added (also for ImageSprite)
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The TouchUp, TouchDown, and Flung events were added. (for all sprites)
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    
    if (blkCompVersion < 5) {
      // speed and hearing were added to the Flung event (for all sprites)
      // speed and heading were added to the Flung event
      final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
          "include speed and heading. Please change your program " +
          "by deleting this old version of the block and pick a new Flung block" +
          "from the drawer";
      for (Element block : getAllMatchingGenusBlocks("Ball-Flung")) {
        markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
      }
      blkCompVersion = 5;
    }
    return blkCompVersion;
  }

  private int upgradeBluetoothClientBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The BluetoothClient.Enabled property was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The BluetoothClient.BluetoothError event was marked userVisible false and is no longer
      // used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "BluetoothClient", "BluetoothError")) {
        markBlockBad(block, "The BluetoothClient.BluetoothError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      // Blocks related to this component have now been upgraded to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The BluetoothClient.DelimiterByte property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The BluetoothClient.Secure property was added.
      // No blocks need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }
    return blkCompVersion;
  }

  private int upgradeBluetoothServerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The BluetoothServer.Enabled property was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The BluetoothServer.BluetoothError event was marked userVisible false and is no longer
      // used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "BluetoothServer", "BluetoothError")) {
        markBlockBad(block, "The BluetoothServer.BluetoothError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      // Blocks related to this component have now been upgraded to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The BluetoothServer.DelimiterByte property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The BluetoothServer.Secure property was added.
      // No blocks need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }
    return blkCompVersion;
  }

  private int upgradeButtonBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The LongClick event was added.
      // No blocks need to be modified to upgrade to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Shape property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The ShowFeedback property was added.
      // No properties need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }

    return blkCompVersion;
  }

  private int upgradeSliderBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 1) {
      //This is initial version. Placeholder for future upgrades
      blkCompVersion = 1;
    }

    return blkCompVersion;
  }

  private int upgradeCanvasBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The LineWidth property was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The FontSize and TextAlignment properties and
      // the DrawText and DrawTextAtAngle methods were added.
      // No blocks need to be modified to upgrade to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // No blocks need to be modified to upgrade to version 4.
      // The Save and SaveAs methods were added.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // No blocks need to be modified to upgrade to version 5.
      // The GetBackgroundPixelColor, GetPixelColor, and
      // SetBackgroundPixelColor methods were added.
      blkCompVersion = 5;
    }
    if (blkCompVersion < 6) {
      // No blocks need to be modified to upgrade to version 6.
      // The TouchUp and TouchDown events were added.
      blkCompVersion = 6;
    }
    
    if (blkCompVersion < 7) {
      // speed and heading were added to the Flung event
      final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
          "include speed and heading. Please change your program " +
          "by deleting this old version of the block and pick a new Flung block" +
          "from the drawer";
      for (Element block : getAllMatchingGenusBlocks("Canvas-Flung")) {
        markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
      }    
      blkCompVersion = 7;
    }
    return blkCompVersion;
  }

  private int upgradeCheckBoxBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Value property was renamed to Checked.
      handlePropertyRename(componentName, "Value", "Checked");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeContactPickerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Open method was added, which does not require changes.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Shape property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }

  private int upgradeEmailPickerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeScreenBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Screen.Scrollable property was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Screen.Icon property was added.
      // No blocks need to be modified to upgrade to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Screen.ErrorOccurred event was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The Screen.ScreenOrientation property and Screen.ScreenOrientationChanged event were
      // added.
      // No blocks need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }
    if (blkCompVersion < 6) {
      // The SwitchForm and SwitchFormWithArgs methods were removed and the OtherScreenClosed event
      // was added.
      blkCompVersion = 6;
    }
    if (blkCompVersion < 7) {
      // The VersionCode and VersionName properties were added. No blocks need to be modified
      // to update to version 7.
      blkCompVersion = 7;
    }
    if (blkCompVersion < 8) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrade to version 8.
      blkCompVersion = 8;
    }
    if (blkCompVersion < 9) {
      // The OpenScreenAnimation and CloseScreenAnimation properties were added. No blocks need
      // to be modified to upgrade to version 9.
      blkCompVersion = 9;
    }
    if (blkCompVersion < 10) {
      // The BackPressed event was added. No blocks need to be modified to upgrade to version 10.
      blkCompVersion = 10;
    }
    return blkCompVersion;
  }

  private int upgradeFusiontablesControlBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // No changes required
      // The ApiKey property and the SendQuery and ForgetLogin methods were added.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }
  
  private int upgradeHorizontalArrangementBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrqde to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }


  private int upgradeImagePickerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Open method was added, which does not require changes.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Shape property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The ImagePath property was renamed to Selection.
      handlePropertyRename(componentName, "ImagePath", "Selection");
      // Blocks related to this component have now been upgraded to version 5.
      blkCompVersion = 5;
    } 
    return blkCompVersion;
  }

  private int upgradeImageSpriteBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The ImageSprite.Rotates property was added
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The PointTowards method was added.
      // The Heading property was changed from int to double
      // No blocks need to be modified to upgrade to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Z property was added (also for Ball)
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The TouchUp, TouchDown, and Flung events were added. (for all sprites)
      // No blocks need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }
    if (blkCompVersion < 6) {
        // speed and hearing were added to the Flung event (for all sprites)
        // speed and heading were added to the Flung event
        final String CHANGED_FLUNG_WARNING = "The %s block has been changed to " +
            "include speed and heading. Please change your program " +
            "by deleting this old version of the block and pick a new Flung block" +
            "from the drawer";
        for (Element block : getAllMatchingGenusBlocks("ImageSprite-Flung")) {
          markBlockBad(block, String.format(CHANGED_FLUNG_WARNING, "Flung"));
        }
      blkCompVersion = 6;
    }  
    
    
    return blkCompVersion;
  }

  private int upgradeLabelBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeListPickerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The SelectionIndex property was added, which does not require changes.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Open method was added, which does not require changes.
      blkCompVersion = 4;
    }
    if (blkCompVersion < 5) {
      // The Shape property was added.
      // No blocks need to be modified to upgrade to version 5.
      blkCompVersion = 5;
    }
    if (blkCompVersion < 6) {
      // The getIntent method was modified to add the parent Form's screen
      // animation type. No blocks need to be modified to upgrade to version 6.
      blkCompVersion = 6;
    }
    return blkCompVersion;
  }

  private int upgradeLocationSensorBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The TimeInterval and DistanceInterval properties were added.
      // No changes required.
      blkCompVersion = 2;
    }

    return blkCompVersion;
  }

  private int upgradeOrientationSensorBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Yaw property was renamed to Azimuth.
      handlePropertyRename(componentName, "Yaw", "Azimuth");
      // The yaw parameter to OrientationChanged was renamed to azimuth.
      for (Element block : getAllMatchingMethodOrEventBlocks(
               componentName, "OrientationSensor", "OrientationChanged")) {
        changeFirstMatchingSocketBlockConnectorLabel(block, "yaw", "azimuth");
      }

      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradePasswordTextBoxBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradePhoneNumberPickerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Open method was added, which does not require changes.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Shape property was added.
      // No blocks need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }

  private int upgradePlayerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Player.PlayerError event was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Player.PlayerError event was marked userVisible false and is no longer used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Player", "PlayerError")) {
        markBlockBad(block, "The Player.PlayerError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The Looping and Volume properties were added.
      // The Completed Event was added.
      // The IsPlaying method was added.
      // No properties need to be modified to upgrade to version 4.
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }

  private int upgradeSoundBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The Sound.SoundError event was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Sound.SoundError event was marked userVisible false and is no longer used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Sound", "SoundError")) {
        markBlockBad(block, "The Sound.SoundError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      blkCompVersion = 3;
    }
    return blkCompVersion;
  }

  private int upgradeTextBoxBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The TextBox.NumbersOnly property was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The Alignment property was renamed to TextAlignment.
      handlePropertyRename(componentName, "Alignment", "TextAlignment");
      // Blocks related to this component have now been upgraded to version 3.
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The TextBox.HideKeyboard method was added
      // The MultiLine property was added,
      // No blocks need to be modified to upgrade to version 4, although old
      // block need to have MultiLine explicitly set to true, since the new default
      // is false (see YoungAndroidFormUpgrade).
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }
  
  private int upgradeTextingBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // No changes required
      // The GoogleVoiceEnabled property was added.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      handlePropertyTypeChange(componentName, "ReceivingEnabled", "receivingEnabled is now an integer in the range 1-3 instead of a boolean");
      blkCompVersion = 3;
    }

    return blkCompVersion;
  }

  private int upgradeNotifierBlocks(int blkCompVersion, String componentName) {
      final String NEW_ARG_WARNING = "The %s block has been changed to " +
          "expect a new cancelable argument. Please replace this block by a new one from the Notifier drawer.";

    if (blkCompVersion < 2) {
        // Look for ShowChooseDialog method block for this component.
        for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Notifier",
          "ShowChooseDialog")) {
          // Mark the block bad.
          markBlockBad(block,NEW_ARG_WARNING );
      }

      // Look for ShowTextDialog method block for this component.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Notifier",
          "ShowTextDialog")) {
          // Mark the block bad.
          markBlockBad(block,NEW_ARG_WARNING );
      }

      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeTinyWebDBBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // Look for TinyWebDB-ShowAlert method blocks for this component.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "TinyWebDB",
          "ShowAlert")) {
        // Change the genus-name because TinyWebDB-ShowAlert doesn't exist anymore.
        changeBlockGenusName(block, "Notifier-ShowAlert");
        // Mark the block bad.
        markBlockBad(block,
            "TinyWebDB.ShowAlert has been removed. Please use Notifier.ShowAlert instead.");
      }
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeVerticalArrangementBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The AlignHorizontal and AlignVertical properties were added. No blocks need to be modified
      // to upgrade to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private int upgradeVideoPlayerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The VideoPlayer.VideoPlayerError event was added.
      // No blocks need to be modified to upgrade to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // The VideoPlayer.VideoPlayerError event was marked userVisible false and is no longer used.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "VideoPlayer", "VideoPlayerError")) {
        markBlockBad(block, "The VideoPlayer.VideoPlayerError event is no longer used. " +
            "Please use the Screen.ErrorOccurred event instead.");
      }
      blkCompVersion = 3;
    }
    if (blkCompVersion < 4) {
      // The VideoPlayer.height and VideoPlayer.width getter and setters were marked as
      // visible to the user
      blkCompVersion = 4;
    }
    return blkCompVersion;
  }

  private int upgradeTwitterBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // Change IsLoggedIn handlers to IsAuthorized. They are close enough
      // that this will probably work for most apps
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Twitter", "IsLoggedIn")) {
        changeBlockGenusName(block, "Twitter-IsAuthorized");
        Node labelChild = getBlockLabelChild(block);
        String newLabel = componentName + ".IsAuthorized";
        labelChild.setNodeValue(newLabel);
      }
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Twitter", "Login")) {
        markBlockBad(block, "Twitter.Login no longer works due to a change in " +
            "Twitter's APIs. Please use Authorize instead.");
      }
      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    return blkCompVersion;
  }

  private void upgradeTrigBlocks() {
    boolean foundTrig = false;

    final String CHANGED_ARG_WARNING = "The %s block has been changed to " +
        "expect degrees, rather than radians.  Please change your program " +
        "accordingly.  You may find the new radians-to-degrees block helpful.";
    for (Element block : getAllMatchingGenusBlocks("number-sin")) {
      foundTrig = true;
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "degrees");
      markBlockBad(block, String.format(CHANGED_ARG_WARNING, "sin"));
    }
    for (Element block : getAllMatchingGenusBlocks("number-cos")) {
      foundTrig = true;
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "degrees");
      markBlockBad(block, String.format(CHANGED_ARG_WARNING, "cos"));
    }
    for (Element block : getAllMatchingGenusBlocks("number-tan")) {
      foundTrig = true;
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "degrees");
      markBlockBad(block, String.format(CHANGED_ARG_WARNING, "tan"));
    }

    final String CHANGED_RESULT_WARNING = "The %s block has been changed " +
        "to provide radians rather than degrees.  Please change your " +
        "program accordingly.  You may find the new radians-to-degrees " +
        "block helpful.";
    for (Element block : getAllMatchingGenusBlocks("number-asin")) {
      foundTrig = true;
      markBlockBad(block, String.format(CHANGED_RESULT_WARNING, "asin"));
    }
    for (Element block : getAllMatchingGenusBlocks("number-acos")) {
      foundTrig = true;
      markBlockBad(block, String.format(CHANGED_RESULT_WARNING, "acos"));
    }
    for (Element block : getAllMatchingGenusBlocks("number-atan")) {
      foundTrig = true;
      markBlockBad(block, String.format(CHANGED_RESULT_WARNING, "atan"));
    }
    for (Element block : getAllMatchingGenusBlocks("number-atan2")) {
      foundTrig = true;
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "y");
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "x");
      markBlockBad(block, String.format(CHANGED_RESULT_WARNING, "atan2"));
    }

    if (foundTrig) {
      FeedbackReporter.showWarningMessage(
          "Since you last modified this project, App Inventor has changed\n" +
          "from using radians to degrees in its trigonometric functions.\n  " +
          "We apologize for the inconvenience and have added new Math\n" +
          "procedures radians-to-degrees and degrees-to-radians to help\n" +
          "with this transition.");
    }
  }

  private int upgradeWebBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 2) {
      // The RequestHeaders and AllowCookies properties were added.
      // The BuildPostData and ClearCookies methods were added.
      // The existing PostText method was renamed to PostTextWithEncoding, and a new PostText
      // method was added.

      // Look for Web-PostText method blocks for this component.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName, "Web", "PostText")) {
        // Change the method from PostText to PostTextWithEncoding.
        changeBlockGenusName(block, "Web-PostTextWithEncoding");
        changeBlockLabel(block, componentName + ".PostText",
            componentName + ".PostTextWithEncoding");
      }

      // Blocks related to this component have now been upgraded to version 2.
      blkCompVersion = 2;
    }
    if (blkCompVersion < 3) {
      // Change BuildPostData function to BuildRequestData.
      for (Element block : getAllMatchingMethodOrEventBlocks(componentName,
          "Web", "BuildPostData")) {
        changeBlockGenusName(block, "Web-BuildRequestData");
        Node labelChild = getBlockLabelChild(block);
        String newLabel = componentName + ".BuildRequestData";
        labelChild.setNodeValue(newLabel);
      }
      // Blocks related to this component have now been upgraded to version 3.
      blkCompVersion = 3;
    }
    return blkCompVersion;
  }

  private int upgradeWebViewerBlocks(int blkCompVersion, String componentName) {
    if (blkCompVersion < 3) {
      // The CanGoForward and CanGoBack methods were added
      // nothing needs to be changed to upgrade to version 2
      // UsesLocation property added.
      // No properties need to be modified to upgrade to version 3.
      blkCompVersion = 3;
    }
    return blkCompVersion;
  }

  private void encodeInternationalCharacters() {
    // Beginning in BLOCKS_LANGUAGE_VERSION 6, text blocks, comments, and complaints
    // are encoded on save and decoded on load to preserve international characters.

    // We need to encode all text blocks, because they will be decoded when the .blk file is
    // loaded.
    for (Element block : getAllMatchingGenusBlocks("text")) {
      Node labelChild = getBlockLabelChild(block);
      if (labelChild != null) {
        String value = labelChild.getNodeValue();
        value = Escapers.encodeInternationalCharacters(value);
        labelChild.setNodeValue(value);
      }
    }

    // We need to encode all comments and complaints, because they will be decoded when the .blk
    // file is loaded.
    String[] tagNames = { "Comment", "Complaint" };
    for (String tagName : tagNames) {
      NodeList nodeList = document.getElementsByTagName(tagName);
      int length = nodeList.getLength();
      for (int i = 0; i < length; i++) {
        Node node = nodeList.item(i);
        Node nodeTextChild = getNodeTextChild(node);
        if (nodeTextChild != null) {
          String value = nodeTextChild.getNodeValue();
          value = Escapers.encodeInternationalCharacters(value);
          nodeTextChild.setNodeValue(value);
        }
      }
    }
  }

  private void fixCorruptedComments() {
    NodeList nodeList = document.getElementsByTagName("Comment");
    int length = nodeList.getLength();
    for (int i = 0; i < length; i++) {
      Node node = nodeList.item(i);
      Node nodeTextChild = getNodeTextChild(node);
      if (nodeTextChild != null) {
        String value = nodeTextChild.getNodeValue();
        value = value.replaceAll("(\\\\u00d4\\\\u00f8\\\\u03a9)+", "*");
        nodeTextChild.setNodeValue(value);
      }
    }
  }

  private void upgradeTextBlockSocketLabels() {
    for (Element block : getAllMatchingGenusBlocks("string-less-than")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text1");
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text2");
    }
    for (Element block : getAllMatchingGenusBlocks("string-equal")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text1");
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text2");
    }
    for (Element block : getAllMatchingGenusBlocks("string-greater-than")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text1");
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text2");
    }
    for (Element block : getAllMatchingGenusBlocks("string-upcase")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text");
    }
    for (Element block : getAllMatchingGenusBlocks("string-downcase")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text");
    }
    for (Element block : getAllMatchingGenusBlocks("string-trim")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "text");
    }
    for (Element block : getAllMatchingGenusBlocks("string-replace-all")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "substring", "segment");
    }
  }

  private void fixRadiansConversionBlocks() {
    for (Element block : getAllMatchingGenusBlocks("number-degrees-to-radians")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "tangent", "degrees");
    }
    for (Element block : getAllMatchingGenusBlocks("number-radians-to-degrees")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "tangent", "radians");
    }
  }

  private void addAsLabelToDefBlocks() {
    for (Element block : getAllMatchingGenusBlocks("def")) {
      changeFirstMatchingSocketBlockConnectorLabel(block, "", "as");
    }
  }

  private void changeStarAndHyphenToTimesAndMinusForMultiplyAndSubtractBlocks() {
    String oldLabel = "*";
    String newLabel = "\\u00D7";
    for (Element block : getAllMatchingGenusBlocks("number-times")) {
      changeBlockLabel(block, oldLabel, newLabel);
    }
    oldLabel = "-";
    newLabel = "\\u2212";
    for (Element block : getAllMatchingGenusBlocks("number-minus")) {
      changeBlockLabel(block, oldLabel, newLabel);
    }
  }

  private void changeGetStartTextAndOpenCloseScreenBlocks() {
    String oldLabel = "open screen";
    String newLabel = "open another screen";
    for (Element block : getAllMatchingGenusBlocks("open-screen")) {
      changeBlockLabel(block, oldLabel, newLabel);
      changeBlockGenusName(block, "open-another-screen");
    }
    oldLabel = "open another screen with start text";
    newLabel = "open another screen with start value";
    for (Element block : getAllMatchingGenusBlocks("open-screen-with-start-text")) {
      changeBlockLabel(block, oldLabel, newLabel);
      changeBlockGenusName(block, "open-another-screen-with-start-value");
      changeFirstMatchingSocketBlockConnectorLabel(block, "startText", "startValue");
    }
    for (Element block : getAllMatchingGenusBlocks("get-startup-text")) {
      markBlockBad(block, "The get startup text block is no longer used. " +
          "Instead, please use get start value in multiple screen apps, " +
          "or get plain start text when starting from other apps.");
    }
    for (Element block : getAllMatchingGenusBlocks("close-screen-with-result")) {
      markBlockBad(block, "The close screen with result block is no longer used. " +
          "Instead, please use close screen with value in multiple screen apps, " +
          "or close screen with plain text for returning to other apps.");
    }
  }

  /*
   * Returns a list of the "Block" elements that are method or event blocks for
   * the given component name, genus, and method or event name.
   */
  private List<Element> getAllMatchingGenusBlocks(String blockGenus) {
    List<Element> matchingBlocks = new ArrayList<Element>();

    NodeList blockNodeList = document.getElementsByTagName("Block");
    int length = blockNodeList.getLength();
    for (int i = 0; i < length; i++) {
      // All of our Nodes should also be Elements.  Skip any that aren't.
      if (blockNodeList.item(i) instanceof Element) {
        Element block = (Element) blockNodeList.item(i);
        if (blockGenus.equals(block.getAttribute("genus-name"))) {
          matchingBlocks.add(block);
        }
      }
    }

    return matchingBlocks;
  }

  /*
   * Returns a list of the "Block" elements that are method or event blocks for
   * the given component name, genus, and method or event name.  An empty methodOrEventName
   * parameter will match all events and methods for that component name and genus.
   */
  private List<Element> getAllMatchingMethodOrEventBlocks(String componentName,
      String componentGenus, String methodOrEventName) {
    List<Element> matchingBlocks = new ArrayList<Element>();

    String blockGenus = componentGenus + "-" + methodOrEventName;

    NodeList blockNodeList = document.getElementsByTagName("Block");
    int length = blockNodeList.getLength();
    for (int i = 0; i < length; i++) {
      // All of our Nodes should also be Elements.  Skip any that aren't.
      if (blockNodeList.item(i) instanceof Element) {
        Element block = (Element) blockNodeList.item(i);
        String blockGenusName = block.getAttribute("genus-name");
        if (blockGenusName.equals(blockGenus) ||
            ((methodOrEventName.length() == 0) && blockGenusName.startsWith(blockGenus))) {
          // The block is the correct genus, but make sure it matches the componentName.
          // It could be for a different component of the same type.
          Node labelChild = getBlockLabelChild(block);
          String labelString = labelChild.getNodeValue();
          String labelComponentName = labelString.substring(0, labelString.indexOf('.'));
          if (labelComponentName.equals(componentName)) {
            matchingBlocks.add(block);
          }
        }
      }
    }

    return matchingBlocks;
  }

  /*
   * Marks the given block as a bad block by adding a CompilerErrorMsg child
   * node.
   */
  private void markBlockBad(Node block, String badMsg) {
    Element compilerErrorMsg = document.createElement(BAD_BLOCK);
    compilerErrorMsg.appendChild(document.createTextNode(badMsg));
    block.appendChild(compilerErrorMsg);
  }

  /*
   * Handles the renaming of a component property for the given componentName
   * from oldPropName to newPropName.
   */
  private void handlePropertyRename(String componentName, String oldPropName, String newPropName) {
    // Look at all BlockStub nodes.
    NodeList blockStubNodeList = document.getElementsByTagName("BlockStub");
    int length = blockStubNodeList.getLength();
    for (int i = 0; i < length; i++) {
      Node blockStub = blockStubNodeList.item(i);
      updateBlockStubForPropertyRename(blockStub, componentName, oldPropName, newPropName);
    }
  }

  /*
   * If the given "BlockStub" node is a component property block stub for the
   * given componentName and oldPropName, changes the property to newPropName.
   */
  private void updateBlockStubForPropertyRename(Node blockStub, String componentName,
      String oldPropName, String newPropName) {
    // A "BlockStub" node can be identified as a component property by its "StubParentName" node
    // value.
    // If identified, the "StubParentName" node value must be updated.
    // Also, a "BlockStub" node may contain a "Block" node that must be updated.
    NodeList blockStubChildren = blockStub.getChildNodes();
    int blockStubChildrenLength = blockStubChildren.getLength();
    for (int i = 0; i < blockStubChildrenLength; i++) {
      Node blockStubChild = blockStubChildren.item(i);
      String blockStubChildName = blockStubChild.getNodeName();
      if (blockStubChildName == null) {
        continue;
      }
      if (blockStubChildName.equals("StubParentName") && blockStubChild.hasChildNodes()) {
        Node stubParentName = blockStubChild;
        // The StubParentName node has one child - the text value.
        Node child = stubParentName.getChildNodes().item(0);
        // Check if the value is componentName + "." + oldPropName
        String value = child.getNodeValue();
        String oldValue = componentName + "." + oldPropName;
        if (value != null && value.equals(oldValue)) {
          // Change the value to componentName + "." + newPropName.
          String newValue = componentName + "." + newPropName;
          child.setNodeValue(newValue);
        }
      } else if (blockStubChildName.equals("Block") && blockStubChild instanceof Element) {
        Element block = (Element) blockStubChild;
        updateBlockForPropertyRename(block, componentName, oldPropName, newPropName);
      }
    }
  }

  /*
   * If the given "Block" node is a component property block for the given
   * componentName and oldPropName, changes the property to newPropName.
   */
  private void updateBlockForPropertyRename(Element block, String componentName,
      String oldPropName, String newPropName) {
    // A "Block" node can be identified as a component property block by its "genus-name" attribute
    // and its "Label" node value.
    // If identified, the "Label" node value must be updated.
    String genusName = block.getAttribute("genus-name");
    if (genusName.equals("componentSetter") || genusName.equals("componentGetter")) {
      String oldLabel = componentName + "." + oldPropName;
      String newLabel = componentName + "." + newPropName;
      changeBlockLabel(block, oldLabel, newLabel);
    }
  }

  private void changeBlockGenusName(Element block, String genus) {
    block.setAttribute("genus-name", genus);
  }

  /*
   * Changes the value of the "Label" for the given "Block" node to the
   * newLabel. If oldLabel parameter is not null, only change the
   * value if the current value of the "Label" equals oldLabel.
   */
  private void changeBlockLabel(Element block, String oldLabel, String newLabel) {
    Node labelChild = getBlockLabelChild(block);
    if (labelChild != null) {
      if (oldLabel != null) {
        // Check if the value is oldLabel.
        String value = labelChild.getNodeValue();
        if (!oldLabel.equals(value)) {
          // The current value doesn't match. Don't change it.
          return;
        }
      }
      // Change the value to newLabel.
      labelChild.setNodeValue(newLabel);
    }
  }

  /*
   * Returns the Node that is text of the "Label" of the given "Block" node.
   */
  // This is public and static because it is used in BlockSaveFileTest
  public static Node getBlockLabelChild(Element block) {
    // Look for the child node named "Label".
    NodeList blockChildren = block.getChildNodes();
    int blockChildrenLength = blockChildren.getLength();
    for (int i = 0; i < blockChildrenLength; i++) {
      Node blockChild = blockChildren.item(i);
      String blockChildName = blockChild.getNodeName();
      if (blockChildName == null) {
        continue;
      }
      if (blockChildName.equals("Label") && blockChild.hasChildNodes()) {
        // The Label node has one child - the text.
        return blockChild.getChildNodes().item(0);
      }
    }
    return null;
  }

  /*
   * Returns the Node that is the text of the "Text" of the given node.
   * <TheGivenNode>
   *   <Text>hello world</Text>
   * </TheGivenNode>
   */
  private static Node getNodeTextChild(Node node) {
    // Look at node's children for the child node named "Text".
    NodeList nodeChildren = node.getChildNodes();
    int nodeChildrenLength = nodeChildren.getLength();
    for (int i = 0; i < nodeChildrenLength; i++) {
      Node nodeChild = nodeChildren.item(i);
      String nodeChildName = nodeChild.getNodeName();
      if (nodeChildName == null) {
        continue;
      }
      if (nodeChildName.equals("Text") && nodeChild.hasChildNodes()) {
        // The Text node has one child - the text.
        return nodeChild.getChildNodes().item(0);
      }
    }
    return null;
  }

  /*
   * Looks within the given block's sockets for the first BlockConnector element
   * with the label attribute matching oldConnectorLabel and changes it to
   * newConnectorLabel.  No error is signalled if oldConnectorLabel is not found.
   */
  private void changeFirstMatchingSocketBlockConnectorLabel(Element block, String oldConnectorLabel,
      String newConnectorLabel) {
    // Look for the Sockets node.
    NodeList blockChildren = block.getChildNodes();
    int blockChildrenLength = blockChildren.getLength();
    for (int i = 0; i < blockChildrenLength; i++) {
      Node blockChild = blockChildren.item(i);
      String blockChildName = blockChild.getNodeName();
      if (blockChildName == null) {
        continue;
      }
      if (blockChildName.equals("Sockets") && blockChild.hasChildNodes()) {
        // Found it!
        Node sockets = blockChild;
        // Look for the BlockConnector node with the label that matches oldConnectorLabel.
        NodeList socketsChildren = sockets.getChildNodes();
        int socketsChildrenLength = socketsChildren.getLength();
        for (int j = 0; j < socketsChildrenLength; j++) {
          Node socketsChild = socketsChildren.item(j);
          String socketsChildName = socketsChild.getNodeName();
          if (socketsChildName == null) {
            continue;
          }
          if (socketsChildName.equals("BlockConnector") && socketsChild instanceof Element) {
            Element blockConnector = (Element) socketsChild;
            if (blockConnector.getAttribute("label").equals(oldConnectorLabel)) {
              // Found it!
              // Change the label.
              blockConnector.setAttribute("label", newConnectorLabel);
              return;
            }
          }
        }
      }
    }
  }

  /*
   * Converts the specified component's property from being a
   * read-write-property to a read-only-property.  If there are any
   * setter blocks, they will be marked bad, and true will be returned.  If
   * there are no setter blocks, false will be returned.
   */
  private boolean handlePropertyRemoveSetter(String componentName, String propName) {
    boolean broken = false;

    // We are targeting reads and writes of this qualified property name
    // (e.g., Label1.Text).
    String qualifiedPropName = componentName + "." + propName;

    // Find BlockStubs whose StubParentName is the qualified property name.
    NodeList blockStubNodeList = document.getElementsByTagName("BlockStub");
    int length = blockStubNodeList.getLength();
    for (int i = 0; i < length; i++) {
      // This try-catch will allow us to avoid explicitly checking for malformed
      // BlockSaveFiles, which could occur if the user munged their save file.
      // We want to ignore any munged nodes.
      try {
        Element blockStub = (Element) blockStubNodeList.item(i);
        String stubParentName = getChildTagValue(blockStub, "StubParentName");
        if (stubParentName.equals(qualifiedPropName)) {
          // Mark the property as being read-only.
          setChildTagValue(blockStub, "StubParentGenus", "read-only-property");

          // If it is a componentSetter, mark it as bad.
          Element block = (Element) ((Element) blockStub).getElementsByTagName("Block").item(0);
          if (block.getAttribute("genus-name").equals("componentSetter")) {
            markBlockBad(block, "This property is now read-only.");
            broken = true;
          }
        }
      } catch (NullPointerException e) {
        // This could occur in two situations, either of which we will ignore:
        // - getChildTagValue() returned null, indicating that a node lacked
        //   the expected child
        // - item(0) returned null, indicating that the blockStub had no
        //   elements with tag name "Block".
      } catch (IllegalArgumentException e) {
        // getChildTagValue() threw an IllegalArgumentException, indicating
        // that a node lacked the expected child.  Ignore it.
      } catch (ClassCastException e) {
        // A Node was not an Element.  Ignore it.
      }
    }
    return broken;
  }

  /*
   * When a property's type changes we use this routine to find any
   * component getters and setters and mark them as bad so the user corrects the
   * type.
   */
  private void handlePropertyTypeChange(String componentName, String propName, String changeMessage) {

    // We are targeting reads and writes of this qualified property name
    // (e.g., Label1.Text).
    String qualifiedPropName = componentName + "." + propName;

    // Find BlockStubs whose StubParentName is the qualified property name.
    NodeList blockStubNodeList = document.getElementsByTagName("BlockStub");
    int length = blockStubNodeList.getLength();
    for (int i = 0; i < length; i++) {
      // This try-catch will allow us to avoid explicitly checking for malformed
      // BlockSaveFiles, which could occur if the user munged their save file.
      // We want to ignore any munged nodes.
      try {
        Element blockStub = (Element) blockStubNodeList.item(i);
        String stubParentName = getChildTagValue(blockStub, "StubParentName");
        if (stubParentName.equals(qualifiedPropName)) {
          // If it is a componentSetter, mark it as bad.
          Element block = (Element) ((Element) blockStub).getElementsByTagName("Block").item(0);
          if (block.getAttribute("genus-name").equals("componentSetter") || block.getAttribute("genus-name").equals("componentGetter")) {
            markBlockBad(block, changeMessage);
          }
        }
      } catch (NullPointerException e) {
        // This could occur in two situations, either of which we will ignore:
        // - getChildTagValue() returned null, indicating that a node lacked
        //   the expected child
        // - item(0) returned null, indicating that the blockStub had no
        //   elements with tag name "Block".
      } catch (IllegalArgumentException e) {
        // getChildTagValue() threw an IllegalArgumentException, indicating
        // that a node lacked the expected child.  Ignore it.
      } catch (ClassCastException e) {
        // A Node was not an Element.  Ignore it.
      }
    }
  }

  /*
   * Finds the first child for this node that has the given tagName
   * and returns its value.
   *
   * For example, given this node:
   * <pre>
   * <BlockStub>
   *   <StubParentName>
   *      ListPicker1.Selection
   *   </StubParentName>
   *   ...
   * </BlockStub>
   * </pre>
   * and the tag "StubParentName", this would return "ListPicker1.Selection".
   *
   * If no such tag can be found, this throws an IllegalArgumentException.
   *
   * It is possible for this to return null, since null is a possible
   * (but not expected) return value from Node.getNodeValue().
   *
   * This is package-private so it can be used by BlockSaveFileTest.
   */
  static String getChildTagValue(Node node, String tagName)
      throws IllegalArgumentException {
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeName().equals(tagName) &&
          child.hasChildNodes()) {
        return child.getFirstChild().getNodeValue();
      }
    }
    throw new IllegalArgumentException("Could not find child " + tagName + " in node " + node);
  }

  /*
   * Sets the first child for this node with the given tagName to have the
   * value tagValue.
   *
   * For example, given this node:
   * <pre>
   * <BlockStub>
   *   <StubParentName>
   *      ListPicker1.Selection
   *   </StubParentName>
   *   <StubParentReadWriteProperty>
   *      read-write-property
   *   </StubParentReadWriteProperty>
   *   ...
   * </BlockStub>
   * </pre>
   * the tagName "StubParentName", and the tagValue "READ-ONLY-PROPERTY"
   * (capitalized for readability), this would mutate the node into:
   * <pre>
   * <BlockStub>
   *   <StubParentName>
   *      ListPicker1.Selection
   *   </StubParentName>
   *   <StubParentReadWriteProperty>
   *      READ-ONLY-PROPERTY
   *   </StubParentReadWriteProperty>
   *   ...
   * </BlockStub>
   * </pre>
   *
   * If no such tag can be found, this throws an IllegalArgumentException.
   */
  private static void setChildTagValue(Node node, String tagName, String tagValue)
      throws IllegalArgumentException {
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeName().equals(tagName) &&
          child.hasChildNodes()) {
        child.getFirstChild().setNodeValue(tagValue);
        return;
      }
    }
    throw new IllegalArgumentException("Could not find child " + tagName + " in node " + node);
  }
}
