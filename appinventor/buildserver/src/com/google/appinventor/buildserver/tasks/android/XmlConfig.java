// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static com.google.appinventor.buildserver.TaskResult.generateError;
import static com.google.appinventor.buildserver.util.ExecutorUtils.createDir;

import com.google.appinventor.buildserver.AnimationXmlConstants;
import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * compiler.createValuesXml()
 * compiler.createProviderXml()
 * compiler.createNetworkConfigXml()
 * compiler.writeICLauncher()
 * compiler.writeICLauncher()
 * compiler.writeICLauncherBackground()
 *
 */
// createValuesXml
// createResXml
// GenerateXmlRes
@BuildType(apk = true, aab = true)
public class XmlConfig implements AndroidTask {
  AndroidCompilerContext context;

  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    this.context = context;

    // Create the "any" dpi dir
    context.getReporter().info("Creating animation xml");
    final File mipmapV26 = createDir(context.getPaths().getResDir(), "mipmap-anydpi-v26");

    // Create anim directory and animation xml files
    File animDir = createDir(context.getPaths().getResDir(), "anim");
    if (!this.createAnimationXml(animDir)) {
      return generateError("There was an error creating the animation xml");
    }

    // Create values directory and style xml files
    context.getReporter().info("Creating style xml");
    final File styleDir = createDir(context.getPaths().getResDir(), "values");
    List<String> standardStyleVersions = Arrays.asList("", "-v11", "-v14", "-v21", "-v23");
    for (String standardStyleVersion : standardStyleVersions) {
      File tmpStyleDir = createDir(context.getPaths().getResDir(), "values" + standardStyleVersion);
      if (!this.createValuesXml(tmpStyleDir, standardStyleVersion)) {
        return generateError("There was an error while generating the values"
            + standardStyleVersion + " style");
      }
    }

    context.getReporter().info("Creating provider_path xml");
    File providerDir = createDir(context.getPaths().getResDir(), "xml");
    if (!this.createProviderXml(providerDir)) {
      return generateError("There was an error creating the provider_path xml");
    }

    context.getReporter().info("Creating network_security_config xml");
    if (!this.createNetworkConfigXml(providerDir)) {
      return generateError("There was an error creating the network_security_config xml");
    }

    // Generate ic_launcher.xml
    context.getReporter().info("Generating adaptive icon file");
    File icLauncher = new File(mipmapV26, "ic_launcher.xml");
    if (!this.writeLauncher(icLauncher)) {
      return generateError("There was an error creating the adaptive icon file");
    }

    // Generate ic_launcher_round.xml
    context.getReporter().info("Generating round adaptive icon file");
    File icLauncherRound = new File(mipmapV26, "ic_launcher_round.xml");
    if (!this.writeLauncher(icLauncherRound)) {
      return generateError("There was an error creating the round adaptive icon file");
    }

    // Generate ic_launcher_background.xml
    context.getReporter().info("Generating adaptive icon background file");
    File icBackgroundColor = new File(styleDir, "ic_launcher_background.xml");
    if (!this.writeLauncherBackground(icBackgroundColor)) {
      return generateError("There was an error creating the adaptive icon background file");
    }

    // Generate custom xml files
    final Map<String, Set<String>> xmlsNeeded = context.getComponentInfo().getXmlsNeeded();
    if (!xmlsNeeded.isEmpty()) {
      context.getReporter().info("Generating custom xml files");
      if (!this.createXmls(xmlsNeeded)) {
        return generateError("There was an error creating the custom xml files");
      }
    }

    return TaskResult.generateSuccess();
  }


  /*
   * Writes the given string input to the provided file.
   */
  private boolean writeXmlFile(File file, String input) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(input);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      context.getReporter().error("Error writing to XML file " + file.getName());
      return false;
    }
    return true;
  }


  /*
   * Creates all the animation xml files.
   */
  private boolean createAnimationXml(File animDir) {
    // Store the filenames, and their contents into a HashMap
    // so that we can easily add more, and also to iterate
    // through creating the files.
    Map<String, String> files = new HashMap<>();
    files.put("fadein.xml", AnimationXmlConstants.FADE_IN_XML);
    files.put("fadeout.xml", AnimationXmlConstants.FADE_OUT_XML);
    files.put("hold.xml", AnimationXmlConstants.HOLD_XML);
    files.put("zoom_enter.xml", AnimationXmlConstants.ZOOM_ENTER);
    files.put("zoom_exit.xml", AnimationXmlConstants.ZOOM_EXIT);
    files.put("zoom_enter_reverse.xml", AnimationXmlConstants.ZOOM_ENTER_REVERSE);
    files.put("zoom_exit_reverse.xml", AnimationXmlConstants.ZOOM_EXIT_REVERSE);
    files.put("slide_exit.xml", AnimationXmlConstants.SLIDE_EXIT);
    files.put("slide_enter.xml", AnimationXmlConstants.SLIDE_ENTER);
    files.put("slide_exit_reverse.xml", AnimationXmlConstants.SLIDE_EXIT_REVERSE);
    files.put("slide_enter_reverse.xml", AnimationXmlConstants.SLIDE_ENTER_REVERSE);
    files.put("slide_v_exit.xml", AnimationXmlConstants.SLIDE_V_EXIT);
    files.put("slide_v_enter.xml", AnimationXmlConstants.SLIDE_V_ENTER);
    files.put("slide_v_exit_reverse.xml", AnimationXmlConstants.SLIDE_V_EXIT_REVERSE);
    files.put("slide_v_enter_reverse.xml", AnimationXmlConstants.SLIDE_V_ENTER_REVERSE);

    for (String filename : files.keySet()) {
      context.getReporter().log("Creating " + filename);
      File file = new File(animDir, filename);
      if (!writeXmlFile(file, files.get(filename))) {
        context.getReporter().error("Error writing animations XML file");
        return false;
      }
    }
    return true;
  }

  /** Create the custom xml files for the app. */
  private boolean createXmls(Map<String, Set<String>> xmlsNeeded) {
    for (Map.Entry<String, Set<String>> component : xmlsNeeded.entrySet()) {
      for (String xml : component.getValue()) {
        String[] parts = xml.split(":", 2);
        if (!parts[0].matches(
            "^(?:(layout|values|drawable|mipmap|xml|color|menu|animator|anim)[a-zA-Z0-9-+_]*/)?[a-z][a-zA-Z0-9-+_]*\\.xml$")) {
          context.getReporter().error("The file " + parts[0] + " being created has an invalid path or name.");
          return false;
        }
        File file =
            new File(
                createDir(context.getPaths().getResDir(), new File(parts[0]).getParent()),
                new File(parts[0]).getName());
        if (!writeXmlFile(file, parts[1])) {
          context.getReporter().error("Error writing custom XML file");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Create the default color and styling for the app.
   */
  private boolean createValuesXml(File valuesDir, String suffix) {
    String theme = context.getProject().getTheme();
    String actionbar = context.getProject().getActionBar();
    String parentTheme;
    // Default to classic theme prior to SDK 11
    boolean isClassicTheme = "Classic".equals(theme) || suffix.isEmpty();
    boolean needsBlackTitleText = false;
    boolean holo = "-v11".equals(suffix) || "-v14".equals(suffix);
    int sdk = suffix.isEmpty() ? 7 : Integer.parseInt(suffix.substring(2));
    if (isClassicTheme) {
      parentTheme = "android:Theme";
    } else {
      if (suffix.equals("-v11")) {
        // AppCompat needs SDK 14, so we explicitly name Holo for SDK 11 through 13
        parentTheme = theme.replace("AppTheme", "android:Theme.Holo");
        needsBlackTitleText = theme.contains("Light") && !theme.contains("DarkActionBar");
        if (theme.contains("Light")) {
          parentTheme = "android:Theme.Holo.Light";
        }
      } else {
        parentTheme = theme.replace("AppTheme", "Theme.AppCompat");
      }
      if (!"true".equalsIgnoreCase(actionbar)) {
        if (parentTheme.endsWith("DarkActionBar")) {
          parentTheme = parentTheme.replace("DarkActionBar", "NoActionBar");
        } else {
          parentTheme += ".NoActionBar";
        }
      }
    }
    String colorPrimary = context.getProject().getPrimaryColor();
    String colorPrimaryDark = context.getProject().getPrimaryColorDark();
    String colorAccent = context.getProject().getAccentColor();
    colorPrimary = cleanColor(colorPrimary);
    colorPrimaryDark = cleanColor(colorPrimaryDark);
    colorAccent = cleanColor(colorAccent);
    File colorsXml = new File(valuesDir, "colors" + suffix + ".xml");
    File stylesXml = new File(valuesDir, "styles" + suffix + ".xml");
    try (Writer out = new BufferedWriter(new OutputStreamWriter(
        Files.newOutputStream(colorsXml.toPath()), StandardCharsets.UTF_8))) {
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<resources>\n");
      out.write("<color name=\"colorPrimary\">");
      out.write(colorPrimary);
      out.write("</color>\n");
      out.write("<color name=\"colorPrimaryDark\">");
      out.write(colorPrimaryDark);
      out.write("</color>\n");
      out.write("<color name=\"colorAccent\">");
      out.write(colorAccent);
      out.write("</color>\n");
      out.write("</resources>\n");
    } catch (IOException e) {
      context.getReporter().error("Error writing values XML file");
      return false;
    }
    try (Writer out = new BufferedWriter(new OutputStreamWriter(
        Files.newOutputStream(stylesXml.toPath()), StandardCharsets.UTF_8))) {
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<resources>\n");

      // writeTheme >>>
      out.write("<style name=\"");
      out.write("AppTheme");
      out.write("\" parent=\"");
      out.write(parentTheme);
      out.write("\">\n");
      out.write("<item name=\"colorPrimary\">@color/colorPrimary</item>\n");
      out.write("<item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>\n");
      out.write("<item name=\"colorAccent\">@color/colorAccent</item>\n");
      boolean needsClassicSwitch = false;
      if (!parentTheme.equals("android:Theme")) {
        out.write("<item name=\"windowActionBar\">true</item>\n");
        out.write("<item name=\"android:windowActionBar\">true</item>\n");  // Honeycomb ActionBar
        if (parentTheme.contains("Holo") || holo) {
          out.write("<item name=\"android:actionBarStyle\">@style/AIActionBar</item>\n");
          out.write("<item name=\"actionBarStyle\">@style/AIActionBar</item>\n");
        }
        // Handles theme for Notifier
        out.write("<item name=\"android:dialogTheme\">@style/AIDialog</item>\n");
        out.write("<item name=\"dialogTheme\">@style/AIDialog</item>\n");
        // Fixes crash in ListPickerActivity
        out.write("<item name=\"android:cacheColorHint\">#000</item>\n");
      } else {
        out.write("<item name=\"switchStyle\">@style/ClassicSwitch</item>\n");
        needsClassicSwitch = true;
      }
      out.write("</style>\n");
      if (needsClassicSwitch) {
        out.write("<style name=\"ClassicSwitch\" "
            + "parent=\"Widget.AppCompat.CompoundButton.Switch\">\n");
        if (sdk == 23) {
          out.write("<item name=\"android:background\">"
              + "@drawable/abc_control_background_material</item>\n");
        } else {
          out.write("<item name=\"android:background\">"
              + "@drawable/abc_item_background_holo_light</item>\n");
        }
        out.write("</style>\n");
      }
      // <<< writeTheme

      if (!isClassicTheme) {
        if (holo) {  // Handle Holo
          // writeActionBarStyle >>>
          out.write("<style name=\"");
          out.write("AIActionBar");
          out.write("\" parent=\"");
          if (parentTheme.contains("Light")) {
            out.write("android:Widget.Holo.Light.ActionBar");
          } else {
            out.write("android:Widget.Holo.ActionBar");
          }
          out.write("\">\n");
          out.write("<item name=\"android:background\">@color/colorPrimary</item>\n");
          out.write("<item name=\"android:titleTextStyle\">@style/AIActionBarTitle</item>\n");
          out.write("</style>\n");
          out.write("<style name=\"AIActionBarTitle\" "
              + "parent=\"android:TextAppearance.Holo.Widget.ActionBar.Title\">\n");
          out.write("<item name=\"android:textColor\">"
              + (needsBlackTitleText ? "#000" : "#fff") + "</item>\n");
          out.write("</style>\n");
          // <<< writeActionBarStyle
        }
        if (parentTheme.contains("Light")) {
          writeDialogTheme(out, "AIDialog", "Theme.AppCompat.Light.Dialog");
          writeDialogTheme(out, "AIAlertDialog", "Theme.AppCompat.Light.Dialog.Alert");
        } else {
          writeDialogTheme(out, "AIDialog", "Theme.AppCompat.Dialog");
          writeDialogTheme(out, "AIAlertDialog", "Theme.AppCompat.Dialog.Alert");
        }
      }
      out.write("<style name=\"TextAppearance.AppCompat.Button\">\n");
      out.write("<item name=\"textAllCaps\">false</item>\n");
      out.write("</style>\n");
      out.write("</resources>\n");
    } catch (IOException e) {
      context.getReporter().error("Error writing values XML file");
      return false;
    }
    return true;
  }

  private String cleanColor(String color) {
    String result = color;
    if (color.startsWith("&H") || color.startsWith("&h")) {
      result = "#" + color.substring(2);
    }
    if (result.length() == 9) {  // true for #AARRGGBB strings
      result = "#" + result.substring(3);  // remove any alpha value
    }
    return result;
  }

  private void writeDialogTheme(Writer out, String name, String parent) throws IOException {
    out.write("<style name=\"");
    out.write(name);
    out.write("\" parent=\"");
    out.write(parent);
    out.write("\">\n");
    out.write("<item name=\"colorPrimary\">@color/colorPrimary</item>\n");
    out.write("<item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>\n");
    out.write("<item name=\"colorAccent\">@color/colorAccent</item>\n");
    if (parent.contains("Holo")) {
      // workaround for weird window border effect
      out.write("<item name=\"android:windowBackground\">@android:color/transparent</item>\n");
      out.write("<item name=\"android:gravity\">center</item>\n");
      out.write("<item name=\"android:layout_gravity\">center</item>\n");
      out.write("<item name=\"android:textColor\">@color/colorPrimary</item>\n");
    }
    out.write("</style>\n");
  }


  /*
   * Creates the provider_paths file which is used to setup a "Files" content
   * provider.
   */
  private boolean createProviderXml(File providerDir) {
    File paths = new File(providerDir, "provider_paths.xml");
    try (Writer out = new BufferedWriter(new OutputStreamWriter(
        Files.newOutputStream(paths.toPath()), StandardCharsets.UTF_8))) {
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<paths xmlns:android=\"http://schemas.android.com/apk/res/android\">\n");
      out.write("   <files-path name=\"internal_files\" path=\".\"/>\n");
      out.write("   <external-path name=\"external_files\" path=\".\"/>\n");
      out.write("</paths>\n");
    } catch (IOException e) {
      context.getReporter().error("Error writing provider_paths XML file");
      return false;
    }
    return true;
  }


  private boolean createNetworkConfigXml(File configDir) {
    File networkConfig = new File(configDir, "network_security_config.xml");
    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
        Files.newOutputStream(networkConfig.toPath()), StandardCharsets.UTF_8))) {
      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      out.println("<network-security-config>");
      out.println("<base-config cleartextTrafficPermitted=\"true\">");
      out.println("<trust-anchors>");
      out.println("<certificates src=\"system\"/>");
      out.println("</trust-anchors>");
      out.println("</base-config>");
      out.println("</network-security-config>");
    } catch (IOException e) {
      context.getReporter().error("Error writing network_config XML file");
      return false;
    }
    return true;
  }

  // Writes ic_launcher.xml to initialize adaptive icon
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean writeLauncher(File adaptiveIconFile) {
    try (Writer out = new BufferedWriter(new OutputStreamWriter(
        Files.newOutputStream(adaptiveIconFile.toPath()), StandardCharsets.UTF_8))) {
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<adaptive-icon " + "xmlns:android=\"http://schemas.android.com/apk/res/android\" " + ">\n");
      out.write("<background android:drawable=\"@color/ic_launcher_background\" />\n");
      out.write("<foreground android:drawable=\"@mipmap/ic_launcher_foreground\" />\n");
      out.write("</adaptive-icon>\n");
    } catch (IOException e) {
      context.getReporter().error("Error writing IC launcher file");
      return false;
    }
    return true;
  }


  // Writes ic_launcher_background.xml to indicate background color of adaptive icon
  private boolean writeLauncherBackground(File icBackgroundFile) {
    try (Writer out = new BufferedWriter(new OutputStreamWriter(
        Files.newOutputStream(icBackgroundFile.toPath()), StandardCharsets.UTF_8))) {
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<resources>\n");
      out.write("<color name=\"ic_launcher_background\">#ffffff</color>\n");
      out.write("</resources>\n");
    } catch (IOException e) {
      context.getReporter().error("Error writing IC launcher background file");
      return false;
    }
    return true;
  }
}
