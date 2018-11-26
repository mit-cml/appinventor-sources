// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.appinventor.buildserver.util.AARLibrary;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.android.ide.common.internal.AaptCruncher;
import com.android.ide.common.internal.PngCruncher;
import com.android.sdklib.build.ApkBuilder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Main entry point for the YAIL compiler.
 *
 * <p>Supplies entry points for building Young Android projects.
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 *
 * [Will 2016/9/20, Refactored {@link #writeAndroidManifest(File)} to
 *   accomodate the new annotations for adding <activity> and <receiver>
 *   elements.]
 */
public final class Compiler {
  /**
   * reading guide:
   * Comp == Component, comp == component, COMP == COMPONENT
   * Ext == External, ext == external, EXT == EXTERNAL
   */

  public static int currentProgress = 10;

  // Kawa and DX processes can use a lot of memory. We only launch one Kawa or DX process at a time.
  private static final Object SYNC_KAWA_OR_DX = new Object();

  private static final String SLASH = File.separator;
  private static final String COLON = File.pathSeparator;

  public static final String RUNTIME_FILES_DIR = "/" + "files" + "/";

  // Build info constants. Used for permissions, libraries, assets and activities.
  // Must match ComponentProcessor.ARMEABI_V7A_SUFFIX
  private static final String ARMEABI_V7A_SUFFIX = "-v7a";
  // Must match ComponentProcessor.ARM64_V8A_SUFFIX
  private static final String ARM64_V8A_SUFFIX = "-v8a";
  // Must match ComponentProcessor.X86_64_SUFFIX
  private static final String X86_64_SUFFIX = "-x8a";
  // Must match Component.ASSET_DIRECTORY
  private static final String ASSET_DIRECTORY = "component";
  // Must match ComponentListGenerator.ASSETS_TARGET
  private static final String ASSETS_TARGET = "assets";
  // Must match ComponentListGenerator.ACTIVITIES_TARGET
  private static final String ACTIVITIES_TARGET = "activities";
  // Must match ComponentListGenerator.LIBRARIES_TARGET
  public static final String LIBRARIES_TARGET = "libraries";
  // Must match ComponentListGenerator.NATIVE_TARGET
  public static final String NATIVE_TARGET = "native";
  // Must match ComponentListGenerator.PERMISSIONS_TARGET
  private static final String PERMISSIONS_TARGET = "permissions";
  // Must match ComponentListGenerator.BROADCAST_RECEIVERS_TARGET
  private static final String BROADCAST_RECEIVERS_TARGET = "broadcastReceivers";
  // Must match ComponentListGenerator.ANDROIDMINSDK_TARGET
  private static final String ANDROIDMINSDK_TARGET = "androidMinSdk";
  
  // TODO(Will): Remove the following target once the deprecated
  //             @SimpleBroadcastReceiver annotation is removed. It should
  //             should remain for the time being because otherwise we'll break
  //             extensions currently using @SimpleBroadcastReceiver.
  //
  // Must match ComponentListGenerator.BROADCAST_RECEIVER_TARGET
  private static final String BROADCAST_RECEIVER_TARGET = "broadcastReceiver";

  // Native library directory names
  private static final String LIBS_DIR_NAME = "libs";
  private static final String ARMEABI_DIR_NAME = "armeabi";
  private static final String ARMEABI_V7A_DIR_NAME = "armeabi-v7a";
  private static final String ARM64_V8A_DIR_NAME = "arm64-v8a";
  private static final String X86_64_DIR_NAME = "x86_64";

  private static final String ASSET_DIR_NAME = "assets";
  private static final String EXT_COMPS_DIR_NAME = "external_comps";

  private static final String DEFAULT_APP_NAME = "";
  private static final String DEFAULT_ICON = RUNTIME_FILES_DIR + "ya.png";
  private static final String DEFAULT_VERSION_CODE = "1";
  private static final String DEFAULT_VERSION_NAME = "1.0";
  private static final String DEFAULT_MIN_SDK = "7";
  private static final String DEFAULT_THEME = "AppTheme.Light.DarkActionBar";

  /*
   * Resource paths to yail runtime, runtime library files and sdk tools.
   * To get the real file paths, call getResource() with one of these constants.
   */
  private static final String ACRA_RUNTIME =
      RUNTIME_FILES_DIR + "acra-4.4.0.jar";
  private static final String ANDROID_RUNTIME =
      RUNTIME_FILES_DIR + "android.jar";
  private static final String[] SUPPORT_JARS = new String[] {
      RUNTIME_FILES_DIR + "animated-vector-drawable.jar",
      RUNTIME_FILES_DIR + "appcompat-v7.jar",
      RUNTIME_FILES_DIR + "core-common.jar",
      RUNTIME_FILES_DIR + "lifecycle-common.jar",
      RUNTIME_FILES_DIR + "runtime.jar",
      RUNTIME_FILES_DIR + "support-annotations.jar",
      RUNTIME_FILES_DIR + "support-compat.jar",
      RUNTIME_FILES_DIR + "support-core-ui.jar",
      RUNTIME_FILES_DIR + "support-core-utils.jar",
      RUNTIME_FILES_DIR + "support-fragment.jar",
      RUNTIME_FILES_DIR + "support-media-compat.jar",
      RUNTIME_FILES_DIR + "support-v4.jar",
      RUNTIME_FILES_DIR + "support-vector-drawable.jar"
  };
  private static final String COMP_BUILD_INFO =
      RUNTIME_FILES_DIR + "simple_components_build_info.json";
  private static final String DX_JAR =
      RUNTIME_FILES_DIR + "dx.jar";
  private static final String KAWA_RUNTIME =
      RUNTIME_FILES_DIR + "kawa.jar";
  private static final String SIMPLE_ANDROID_RUNTIME_JAR =
      RUNTIME_FILES_DIR + "AndroidRuntime.jar";

  private static final String LINUX_AAPT_TOOL =
      "/tools/linux/aapt";
  private static final String LINUX_ZIPALIGN_TOOL =
      "/tools/linux/zipalign";
  private static final String MAC_AAPT_TOOL =
      "/tools/mac/aapt";
  private static final String MAC_ZIPALIGN_TOOL =
      "/tools/mac/zipalign";
  private static final String WINDOWS_AAPT_TOOL =
      "/tools/windows/aapt";
  private static final String WINDOWS_ZIPALIGN_TOOL =
      "/tools/windows/zipalign";

  @VisibleForTesting
  static final String YAIL_RUNTIME = RUNTIME_FILES_DIR + "runtime.scm";

  private final ConcurrentMap<String, Set<String>> assetsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> activitiesNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> broadcastReceiversNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> libsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> nativeLibsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> permissionsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> minSdksNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final Set<String> uniqueLibsNeeded = Sets.newHashSet();
  
  /**
   * Set of exploded AAR libraries.
   */
  private AARLibraries explodedAarLibs;

  /**
   * File where the compiled R resources are written.
   */
  private File appRJava;

  /**
   * The file containing the text version of the R resources map.
   */
  private File appRTxt;

  /**
   * Directory where the merged resource XML files are placed.
   */
  private File mergedResDir;

  // TODO(Will): Remove the following Set once the deprecated
  //             @SimpleBroadcastReceiver annotation is removed. It should
  //             should remain for the time being because otherwise we'll break
  //             extensions currently using @SimpleBroadcastReceiver.
  private final ConcurrentMap<String, Set<String>> componentBroadcastReceiver =
      new ConcurrentHashMap<String, Set<String>>();

  /**
   * Map used to hold the names and paths of resources that we've written out
   * as temp files.
   * Don't use this map directly. Please call getResource() with one of the
   * constants above to get the (temp file) path to a resource.
   */
  private static final ConcurrentMap<String, File> resources =
      new ConcurrentHashMap<String, File>();

  // TODO(user,lizlooney): i18n here and in lines below that call String.format(...)
  private static final String COMPILATION_ERROR =
      "Error: Your build failed due to an error when compiling %s.\n";
  private static final String ERROR_IN_STAGE =
      "Error: Your build failed due to an error in the %s stage, " +
      "not because of an error in your program.\n";
  private static final String ICON_ERROR =
      "Error: Your build failed because %s cannot be used as the application icon.\n";
  private static final String NO_USER_CODE_ERROR =
      "Error: No user code exists.\n";

  private final int childProcessRamMb;  // Maximum ram that can be used by a child processes, in MB.
  private final boolean isForCompanion;
  private final Project project;
  private final PrintStream out;
  private final PrintStream err;
  private final PrintStream userErrors;

  private File libsDir; // The directory that will contain any native libraries for packaging
  private String dexCacheDir;
  private boolean hasSecondDex = false; // True if classes2.dex should be added to the APK

  private JSONArray simpleCompsBuildInfo;
  private JSONArray extCompsBuildInfo;
  private Set<String> simpleCompTypes;  // types needed by the project
  private Set<String> extCompTypes; // types needed by the project

  /**
   * Mapping from type name to path in project to minimize tests against the file system.
   */
  private Map<String, String> extTypePathCache = new HashMap<String, String>();

  private static final Logger LOG = Logger.getLogger(Compiler.class.getName());

  private BuildServer.ProgressReporter reporter; // Used to report progress of the build

  /*
   * Generate the set of Android permissions needed by this project.
   */
  @VisibleForTesting
  void generatePermissions() {
    try {
      loadJsonInfo(permissionsNeeded, PERMISSIONS_TARGET);
      if (project != null) {    // Only do this if we have a project (testing doesn't provide one :-( ).
        LOG.log(Level.INFO, "usesLocation = " + project.getUsesLocation());
        if (project.getUsesLocation().equals("True")) { // Add location permissions if any WebViewer requests it
          Set<String> locationPermissions = Sets.newHashSet(); // via a Property.
          // See ProjectEditor.recordLocationSettings()
          locationPermissions.add("android.permission.ACCESS_FINE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_MOCK_LOCATION");
          permissionsNeeded.put("com.google.appinventor.components.runtime.WebViewer", locationPermissions);
        }
      }
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Permissions"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Permissions"));
    }

    int n = 0;
    for (String type : permissionsNeeded.keySet()) {
      n += permissionsNeeded.get(type).size();
    }

    System.out.println("Permissions needed, n = " + n);
  }

  // Just used for testing
  @VisibleForTesting
  Map<String,Set<String>> getPermissions() {
    return permissionsNeeded;
  }
  
  // Just used for testing
  @VisibleForTesting
  Map<String, Set<String>> getBroadcastReceivers() {
    return broadcastReceiversNeeded;
  }
  
  // Just used for testing
  @VisibleForTesting
  Map<String, Set<String>> getActivities() {
    return activitiesNeeded;
  }

  /*
   * Generate the set of Android libraries needed by this project.
   */
  @VisibleForTesting
  void generateLibNames() {
    try {
      loadJsonInfo(libsNeeded, LIBRARIES_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Libraries"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Libraries"));
    }

    int n = 0;
    for (String type : libsNeeded.keySet()) {
      n += libsNeeded.get(type).size();
    }

    System.out.println("Libraries needed, n = " + n);
  }

  /*
   * Generate the set of conditionally included libraries needed by this project.
   */
  @VisibleForTesting
  void generateNativeLibNames() {
    try {
      loadJsonInfo(nativeLibsNeeded, NATIVE_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Native Libraries"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Native Libraries"));
    }

    int n = 0;
    for (String type : nativeLibsNeeded.keySet()) {
      n += nativeLibsNeeded.get(type).size();
    }

    System.out.println("Native Libraries needed, n = " + n);
  }

  /*
   * Generate the set of conditionally included assets needed by this project.
   */
  @VisibleForTesting
  void generateAssets() {
    try {
      loadJsonInfo(assetsNeeded, ASSETS_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Assets"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Assets"));
    }

    int n = 0;
    for (String type : assetsNeeded.keySet()) {
      n += assetsNeeded.get(type).size();
    }

    System.out.println("Component assets needed, n = " + n);
  }

  /*
   * Generate the set of conditionally included activities needed by this project.
   */
  @VisibleForTesting
  void generateActivities() {
    try {
      loadJsonInfo(activitiesNeeded, ACTIVITIES_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Activities"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Activities"));
    }

    int n = 0;
    for (String type : activitiesNeeded.keySet()) {
      n += activitiesNeeded.get(type).size();
    }

    System.out.println("Component activities needed, n = " + n);
  }

  /*
   * Generate a set of conditionally included broadcast receivers needed by this project.
   */
  @VisibleForTesting
  void generateBroadcastReceivers() {
    try {
      loadJsonInfo(broadcastReceiversNeeded, BROADCAST_RECEIVERS_TARGET);
    }
    catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "BroadcastReceivers"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "BroadcastReceivers"));
    }
  }
  
  /*
   * TODO(Will): Remove this method once the deprecated @SimpleBroadcastReceiver
   *             annotation is removed. This should remain for the time being so
   *             that we don't break extensions currently using the
   *             @SimpleBroadcastReceiver annotation.
   */
  @VisibleForTesting
  void generateBroadcastReceiver() {
    try {
      loadJsonInfo(componentBroadcastReceiver, BROADCAST_RECEIVER_TARGET);
    }
    catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "BroadcastReceiver"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "BroadcastReceiver"));
    }
  }

  private void generateMinSdks() {
    try {
      loadJsonInfo(minSdksNeeded, ANDROIDMINSDK_TARGET);
    } catch (IOException|JSONException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "AndroidMinSDK"));
    }
  }

  // This patches around a bug in AAPT (and other placed in Android)
  // where an ampersand in the name string breaks AAPT.
  private String cleanName(String name) {
    return name.replace("&", "and");
  }

  private String cleanColor(String color, boolean makeOpaque) {
    String result = color;
    if (color.startsWith("&H") || color.startsWith("&h")) {
      result =  "#" + color.substring(2);
    }
    if (makeOpaque && result.length() == 9) {  // true for #AARRGGBB strings
      result = "#" + result.substring(3);  // remove any alpha value
    }
    return result;
  }

  /**
   * Write out a style definition customized with the given colors.
   *
   * @param out The writer the style will be written to.
   * @param name The name of the new style.
   * @param parent The parent style to inherit from.
   * @throws IOException if the writer cannot be written to.
   */
  private static void writeTheme(Writer out, String name, String parent, boolean holo) throws IOException {
    out.write("<style name=\"");
    out.write(name);
    out.write("\" parent=\"");
    out.write(parent);
    out.write("\">\n");
    out.write("<item name=\"colorPrimary\">@color/colorPrimary</item>\n");
    out.write("<item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>\n");
    out.write("<item name=\"colorAccent\">@color/colorAccent</item>\n");
    if (!parent.equals("android:Theme")) {
      out.write("<item name=\"windowActionBar\">true</item>\n");
      out.write("<item name=\"android:windowActionBar\">true</item>\n");  // Honeycomb ActionBar
      if (parent.contains("Holo") || holo) {
        out.write("<item name=\"android:actionBarStyle\">@style/AIActionBar</item>\n");
        out.write("<item name=\"actionBarStyle\">@style/AIActionBar</item>\n");
      }
      // Handles theme for Notifier
      out.write("<item name=\"android:dialogTheme\">@style/AIDialog</item>\n");
      out.write("<item name=\"dialogTheme\">@style/AIDialog</item>\n");
      out.write("<item name=\"android:cacheColorHint\">#000</item>\n");  // Fixes crash in ListPickerActivity
    }
    out.write("</style>\n");
  }

  private static void writeActionBarStyle(Writer out, String name, String parent,
      boolean blackText) throws IOException {
    out.write("<style name=\"");
    out.write(name);
    out.write("\" parent=\"");
    out.write(parent);
    out.write("\">\n");
    out.write("<item name=\"android:background\">@color/colorPrimary</item>\n");
    out.write("<item name=\"android:titleTextStyle\">@style/AIActionBarTitle</item>\n");
    out.write("</style>\n");
    out.write("<style name=\"AIActionBarTitle\" parent=\"android:TextAppearance.Holo.Widget.ActionBar.Title\">\n");
    out.write("<item name=\"android:textColor\">" + (blackText ? "#000" : "#fff") + "</item>\n");
    out.write("</style>\n");
  }

  private static void writeDialogTheme(Writer out, String name, String parent) throws IOException {
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

  /**
   * Create the default color and styling for the app.
   */
  private boolean createValuesXml(File valuesDir, String suffix) {
    String colorPrimary = project.getPrimaryColor() == null ? "#A5CF47" : project.getPrimaryColor();
    String colorPrimaryDark = project.getPrimaryColorDark() == null ? "#41521C" : project.getPrimaryColorDark();
    String colorAccent = project.getAccentColor() == null ? "#00728A" : project.getAccentColor();
    String theme = project.getTheme() == null ? "Classic" : project.getTheme();
    String actionbar = project.getActionBar();
    String parentTheme;
    boolean isClassicTheme = "Classic".equals(theme) || suffix.isEmpty();  // Default to classic theme prior to SDK 11
    boolean needsBlackTitleText = false;
    if (isClassicTheme) {
      parentTheme = "android:Theme";
    } else {
      if (suffix.equals("-v11")) {  // AppCompat needs SDK 14, so we explicitly name Holo for SDK 11 through 13
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
    colorPrimary = cleanColor(colorPrimary, true);
    colorPrimaryDark = cleanColor(colorPrimaryDark, true);
    colorAccent = cleanColor(colorAccent, true);
    File colorsXml = new File(valuesDir, "colors" + suffix + ".xml");
    File stylesXml = new File(valuesDir, "styles" + suffix + ".xml");
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(colorsXml), "UTF-8"));
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
      out.close();
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stylesXml), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<resources>\n");

      writeTheme(out, "AppTheme", parentTheme, suffix.equals("-v11"));
      if (!isClassicTheme) {
        if ("-v11".equals(suffix)) {  // Handle Holo
          if (parentTheme.contains("Light")) {
            writeActionBarStyle(out, "AIActionBar", "android:Widget.Holo.Light.ActionBar", needsBlackTitleText);
          } else {
            writeActionBarStyle(out, "AIActionBar", "android:Widget.Holo.ActionBar", needsBlackTitleText);
          }
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
      out.close();
    } catch(IOException e) {
      return false;
    }
    return true;
  }

  /*
   * Creates the provider_paths file which is used to setup a "Files" content
   * provider.
   */
  private boolean createProviderXml(File providerDir) {
    File paths = new File(providerDir, "provider_paths.xml");
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<paths xmlns:android=\"http://schemas.android.com/apk/res/android\">\n");
      out.write("   <external-path name=\"external_files\" path=\".\"/>\n");
      out.write("</paths>\n");
      out.close();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /*
   * Creates an AndroidManifest.xml file needed for the Android application.
   */
  private boolean writeAndroidManifest(File manifestFile) {
    // Create AndroidManifest.xml
    String mainClass = project.getMainClass();
    String packageName = Signatures.getPackageName(mainClass);
    String className = Signatures.getClassName(mainClass);
    String projectName = project.getProjectName();
    String vCode = (project.getVCode() == null) ? DEFAULT_VERSION_CODE : project.getVCode();
    String vName = (project.getVName() == null) ? DEFAULT_VERSION_NAME : cleanName(project.getVName());
    String aName = (project.getAName() == null) ? DEFAULT_APP_NAME : cleanName(project.getAName());
    LOG.log(Level.INFO, "VCode: " + project.getVCode());
    LOG.log(Level.INFO, "VName: " + project.getVName());

    // TODO(user): Use com.google.common.xml.XmlWriter
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(manifestFile), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      // TODO(markf) Allow users to set versionCode and versionName attributes.
      // See http://developer.android.com/guide/publishing/publishing.html for
      // more info.
      out.write("<manifest " +
          "xmlns:android=\"http://schemas.android.com/apk/res/android\" " +
          "package=\"" + packageName + "\" " +
          // TODO(markf): uncomment the following line when we're ready to enable publishing to the
          // Android Market.
         "android:versionCode=\"" + vCode +"\" " + "android:versionName=\"" + vName + "\" " +
          ">\n");

      // If we are building the Wireless Debugger (AppInventorDebugger) add the uses-feature tag which
      // is used by the Google Play store to determine which devices the app is available for. By adding
      // these lines we indicate that we use these features BUT THAT THEY ARE NOT REQUIRED so it is ok
      // to make the app available on devices that lack the feature. Without these lines the Play Store
      // makes a guess based on permissions and assumes that they are required features.
      if (isForCompanion) {
          out.write("  <uses-feature android:name=\"android.hardware.bluetooth\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.location\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.telephony\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.location.network\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.location.gps\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.microphone\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.touchscreen\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.camera\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.camera.autofocus\" android:required=\"false\" />\n");
          out.write("  <uses-feature android:name=\"android.hardware.wifi\" />\n"); // We actually require wifi
      }

      int minSdk = Integer.parseInt((project.getMinSdk() == null) ? DEFAULT_MIN_SDK : project.getMinSdk());
      if (!isForCompanion) {
        for (Set<String> minSdks : minSdksNeeded.values()) {
          for (String sdk : minSdks) {
            int sdkInt = Integer.parseInt(sdk);
            if (sdkInt > minSdk) {
              minSdk = sdkInt;
            }
          }
        }
      }

      // make permissions unique by putting them in one set
      Set<String> permissions = Sets.newHashSet();
      for (Set<String> compPermissions : permissionsNeeded.values()) {
        permissions.addAll(compPermissions);
      }

      for (String permission : permissions) {
        out.write("  <uses-permission android:name=\"" + permission + "\" />\n");
      }

      if (isForCompanion) {      // This is so ACRA can do a logcat on phones older then Jelly Bean
        out.write("  <uses-permission android:name=\"android.permission.READ_LOGS\" />\n");
        out.write("  <uses-permission android:name=\"android.permission.REQUEST_INSTALL_PACKAGES\" />\n");
      }

      // TODO(markf): Change the minSdkVersion below if we ever require an SDK beyond 1.5.
      // The market will use the following to filter apps shown to devices that don't support
      // the specified SDK version.  We right now support building for minSDK 4.
      // We might also want to allow users to specify minSdk version or targetSDK version.
      out.write("  <uses-sdk android:minSdkVersion=\"" + minSdk + "\" android:targetSdkVersion=\"26\" />\n");

      out.write("  <application ");

      // TODO(markf): The preparing to publish doc at
      // http://developer.android.com/guide/publishing/preparing.html suggests removing the
      // 'debuggable=true' but I'm not sure that our users would want that while they're still
      // testing their packaged apps.  Maybe we should make that an option, somehow.
      // TODONE(jis): Turned off debuggable. No one really uses it and it represents a security
      // risk for App Inventor App end-users.
      out.write("android:debuggable=\"false\" ");
      // out.write("android:debuggable=\"true\" "); // DEBUGGING
      if (aName.equals("")) {
        out.write("android:label=\"" + projectName + "\" ");
      } else {
        out.write("android:label=\"" + aName + "\" ");
      }
      out.write("android:icon=\"@drawable/ya\" ");
      if (isForCompanion) {              // This is to hook into ACRA
        out.write("android:name=\"com.google.appinventor.components.runtime.ReplApplication\" ");
      } else {
        out.write("android:name=\"com.google.appinventor.components.runtime.multidex.MultiDexApplication\" ");
      }
      // Write theme info if we are not using the "Classic" theme (i.e., no theme)
      if (true) {
//      if (!"Classic".equalsIgnoreCase(project.getTheme())) {
        out.write("android:theme=\"@style/AppTheme\" ");
      }
      out.write(">\n");

      for (Project.SourceDescriptor source : project.getSources()) {
        String formClassName = source.getQualifiedName();
        // String screenName = formClassName.substring(formClassName.lastIndexOf('.') + 1);
        boolean isMain = formClassName.equals(mainClass);

        if (isMain) {
          // The main activity of the application.
          out.write("    <activity android:name=\"." + className + "\" ");
        } else {
          // A secondary activity of the application.
          out.write("    <activity android:name=\"" + formClassName + "\" ");
        }

        // This line is here for NearField and NFC.   It keeps the activity from
        // restarting every time NDEF_DISCOVERED is signaled.
        // TODO:  Check that this doesn't screw up other components.  Also, it might be
        // better to do this programmatically when the NearField component is created, rather
        // than here in the manifest.
        if (simpleCompTypes.contains("com.google.appinventor.components.runtime.NearField") &&
            !isForCompanion && isMain) {
          out.write("android:launchMode=\"singleTask\" ");
        } else if (isMain && isForCompanion) {
          out.write("android:launchMode=\"singleTop\" ");
        }

        out.write("android:windowSoftInputMode=\"stateHidden\" ");

        // The keyboard option prevents the app from stopping when a external (bluetooth)
        // keyboard is attached.
        out.write("android:configChanges=\"orientation|screenSize|keyboardHidden|keyboard\">\n");


        out.write("      <intent-filter>\n");
        out.write("        <action android:name=\"android.intent.action.MAIN\" />\n");
        if (isMain) {
          out.write("        <category android:name=\"android.intent.category.LAUNCHER\" />\n");
        }
        out.write("      </intent-filter>\n");

        if (simpleCompTypes.contains("com.google.appinventor.components.runtime.NearField") &&
            !isForCompanion && isMain) {
          //  make the form respond to NDEF_DISCOVERED
          //  this will trigger the form's onResume method
          //  For now, we're handling text/plain only,but we can add more and make the Nearfield
          // component check the type.
          out.write("      <intent-filter>\n");
          out.write("        <action android:name=\"android.nfc.action.NDEF_DISCOVERED\" />\n");
          out.write("        <category android:name=\"android.intent.category.DEFAULT\" />\n");
          out.write("        <data android:mimeType=\"text/plain\" />\n");
          out.write("      </intent-filter>\n");
        }
        out.write("    </activity>\n");

        // Companion display a splash screen... define it's activity here
        if (isMain && isForCompanion) {
          out.write("    <activity android:name=\"com.google.appinventor.components.runtime.SplashActivity\" android:screenOrientation=\"behind\" android:configChanges=\"keyboardHidden|orientation\">\n");
          out.write("      <intent-filter>\n");
          out.write("        <action android:name=\"android.intent.action.MAIN\" />\n");
          out.write("      </intent-filter>\n");
          out.write("    </activity>\n");
        }
      }
      
      // Collect any additional <application> subelements into a single set.
      Set<Map.Entry<String, Set<String>>> subelements = Sets.newHashSet();
      subelements.addAll(activitiesNeeded.entrySet());
      subelements.addAll(broadcastReceiversNeeded.entrySet());
      
      
      // If any component needs to register additional activities or
      // broadcast receivers, insert them into the manifest here.
      if (!subelements.isEmpty()) {
        for (Map.Entry<String, Set<String>> componentSubElSetPair : subelements) {
          Set<String> subelementSet = componentSubElSetPair.getValue();
          for (String subelement : subelementSet) {
            out.write(subelement);
          }
        }
      }
  
      // TODO(Will): Remove the following legacy code once the deprecated
      //             @SimpleBroadcastReceiver annotation is removed. It should
      //             should remain for the time being because otherwise we'll break
      //             extensions currently using @SimpleBroadcastReceiver.
      
      // Collect any legacy simple broadcast receivers
      Set<String> simpleBroadcastReceivers = Sets.newHashSet();
      for (String componentType : componentBroadcastReceiver.keySet()) {
        simpleBroadcastReceivers.addAll(componentBroadcastReceiver.get(componentType));
      }
      
      // The format for each legacy Broadcast Receiver in simpleBroadcastReceivers is
      // "className,Action1,Action2,..." where the class name is mandatory, and
      // actions are optional (and as many as needed).
      for (String broadcastReceiver : simpleBroadcastReceivers) {
        String[] brNameAndActions = broadcastReceiver.split(",");
        if (brNameAndActions.length == 0) continue;
        out.write(
            "<receiver android:name=\"" + brNameAndActions[0] + "\" >\n");
        if (brNameAndActions.length > 1){
          out.write("  <intent-filter>\n");
          for (int i = 1; i < brNameAndActions.length; i++) {
            out.write("    <action android:name=\"" + brNameAndActions[i] + "\" />\n");
          }
          out.write("  </intent-filter>\n");
        }
        out.write("</receiver> \n");
      }

      // Add the FileProvider because in Sdk >=24 we cannot pass file:
      // URLs in intents (and in other contexts)

      out.write("      <provider\n");
      out.write("         android:name=\"android.support.v4.content.FileProvider\"\n");
      out.write("         android:authorities=\"" + packageName + ".provider\"\n");
      out.write("         android:exported=\"false\"\n");
      out.write("         android:grantUriPermissions=\"true\">\n");
      out.write("         <meta-data\n");
      out.write("            android:name=\"android.support.FILE_PROVIDER_PATHS\"\n");
      out.write("            android:resource=\"@xml/provider_paths\"/>\n");
      out.write("      </provider>\n");

      out.write("  </application>\n");
      out.write("</manifest>\n");
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "manifest"));
      return false;
    }

    return true;
  }

  /**
   * Builds a YAIL project.
   *
   * @param project  project to build
   * @param compTypes component types used in the project
   * @param out  stdout stream for compiler messages
   * @param err  stderr stream for compiler messages
   * @param userErrors stream to write user-visible error messages
   * @param keystoreFilePath
   * @param childProcessRam   maximum RAM for child processes, in MBs.
   * @return  {@code true} if the compilation succeeds, {@code false} otherwise
   * @throws JSONException
   * @throws IOException
   */
  public static boolean compile(Project project, Set<String> compTypes,
                                PrintStream out, PrintStream err, PrintStream userErrors,
                                boolean isForCompanion, String keystoreFilePath,
                                int childProcessRam, String dexCacheDir,
                                BuildServer.ProgressReporter reporter) throws IOException, JSONException {
    long start = System.currentTimeMillis();

    // Create a new compiler instance for the compilation
    Compiler compiler = new Compiler(project, compTypes, out, err, userErrors, isForCompanion,
                                     childProcessRam, dexCacheDir, reporter);

    compiler.generateAssets();
    compiler.generateActivities();
    compiler.generateBroadcastReceivers();
    compiler.generateLibNames();
    compiler.generateNativeLibNames();
    compiler.generatePermissions();
    compiler.generateMinSdks();
  
    // TODO(Will): Remove the following call once the deprecated
    //             @SimpleBroadcastReceiver annotation is removed. It should
    //             should remain for the time being because otherwise we'll break
    //             extensions currently using @SimpleBroadcastReceiver.
    compiler.generateBroadcastReceiver();

    // Create build directory.
    File buildDir = createDir(project.getBuildDirectory());

    // Prepare application icon.
    out.println("________Preparing application icon");
    File resDir = createDir(buildDir, "res");
    File drawableDir = createDir(resDir, "drawable");
    if (!compiler.prepareApplicationIcon(new File(drawableDir, "ya.png"))) {
      return false;
    }
    if (reporter != null) {
      reporter.report(15);        // Have to call directly because we are in a
    }                             // Static context

    // Create anim directory and animation xml files
    out.println("________Creating animation xml");
    File animDir = createDir(resDir, "anim");
    if (!compiler.createAnimationXml(animDir)) {
      return false;
    }

    // Create values directory and style xml files
    out.println("________Creating style xml");
    File styleDir = createDir(resDir, "values");
    File style11Dir = createDir(resDir, "values-v11");
    File style14Dir = createDir(resDir, "values-v14");
    File style21Dir = createDir(resDir, "values-v21");
    if (!compiler.createValuesXml(styleDir, "") ||
        !compiler.createValuesXml(style11Dir, "-v11") ||
        !compiler.createValuesXml(style14Dir, "-v14") ||
        !compiler.createValuesXml(style21Dir, "-v21")) {
      return false;
    }

    out.println("________Creating provider_path xml");
    File providerDir = createDir(resDir, "xml");
    if (!compiler.createProviderXml(providerDir)) {
      return false;
    }

    // Generate AndroidManifest.xml
    out.println("________Generating manifest file");
    File manifestFile = new File(buildDir, "AndroidManifest.xml");
    if (!compiler.writeAndroidManifest(manifestFile)) {
      return false;
    }
    if (reporter != null) {
      reporter.report(20);
    }

    // Insert native libraries
    out.println("________Attaching native libraries");
    if (!compiler.insertNativeLibs(buildDir)) {
      return false;
    }

    // Attach Android AAR Library dependencies
    out.println("________Attaching Android Archive (AAR) libraries");
    if (!compiler.attachAarLibraries(buildDir)) {
      return false;
    }

    // Add raw assets to sub-directory of project assets.
    out.println("________Attaching component assets");
    if (!compiler.attachCompAssets()) {
      return false;
    }

    // Invoke aapt to package everything up
    out.println("________Invoking AAPT");
    File deployDir = createDir(buildDir, "deploy");
    String tmpPackageName = deployDir.getAbsolutePath() + SLASH +
        project.getProjectName() + ".ap_";
    File srcJavaDir = createDir(buildDir, "generated/src");
    File rJavaDir = createDir(buildDir, "generated/symbols");
    if (!compiler.runAaptPackage(manifestFile, resDir, tmpPackageName, srcJavaDir, rJavaDir)) {
      return false;
    }
    if (reporter != null) {
      reporter.report(30);
    }

    // Create class files.
    out.println("________Compiling source files");
    File classesDir = createDir(buildDir, "classes");
    if (!compiler.generateRClasses(classesDir)) {
      return false;
    }
    if (!compiler.generateClasses(classesDir)) {
      return false;
    }
    if (reporter != null) {
      reporter.report(35);
    }

    // Invoke dx on class files
    out.println("________Invoking DX");
    // TODO(markf): Running DX is now pretty slow (~25 sec overhead the first time and ~15 sec
    // overhead for subsequent runs).  I think it's because of the need to dx the entire
    // kawa runtime every time.  We should probably only do that once and then copy all the
    // kawa runtime dx files into the generated classes.dex (which would only contain the
    // files compiled for this project).
    // Aargh.  It turns out that there's no way to manipulate .dex files to do the above.  An
    // Android guy suggested an alternate approach of shipping the kawa runtime .dex file as
    // data with the application and then creating a new DexClassLoader using that .dex file
    // and with the original app class loader as the parent of the new one.
    // TODONE(zhuowei): Now using the new Android DX tool to merge dex files
    // Needs to specify a writable cache dir on the command line that persists after shutdown
    // Each pre-dexed file is identified via its MD5 hash (since the standard Android SDK's
    // method of identifying via a hash of the path won't work when files
    // are copied into temporary storage) and processed via a hacked up version of
    // Android SDK's Dex Ant task
    File tmpDir = createDir(buildDir, "tmp");
    String dexedClassesDir = tmpDir.getAbsolutePath();
    if (!compiler.runDx(classesDir, dexedClassesDir, false)) {
      return false;
    }
    if (reporter != null) {
      reporter.report(85);
    }

    // Seal the apk with ApkBuilder
    out.println("________Invoking ApkBuilder");
    String apkAbsolutePath = deployDir.getAbsolutePath() + SLASH +
        project.getProjectName() + ".apk";
    if (!compiler.runApkBuilder(apkAbsolutePath, tmpPackageName, dexedClassesDir)) {
      return false;
    }
    if (reporter != null) {
      reporter.report(95);
    }

    // Sign the apk file
    out.println("________Signing the apk file");
    if (!compiler.runJarSigner(apkAbsolutePath, keystoreFilePath)) {
      return false;
    }

    // ZipAlign the apk file
    out.println("________ZipAligning the apk file");
    if (!compiler.runZipAlign(apkAbsolutePath, tmpDir)) {
      return false;
    }

    if (reporter != null) {
      reporter.report(100);
    }

    out.println("Build finished in " +
        ((System.currentTimeMillis() - start) / 1000.0) + " seconds");

    return true;
  }

  /*
   * Creates all the animation xml files.
   */
  private boolean createAnimationXml(File animDir) {
    // Store the filenames, and their contents into a HashMap
    // so that we can easily add more, and also to iterate
    // through creating the files.
    Map<String, String> files = new HashMap<String, String>();
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
      File file = new File(animDir, filename);
      if (!writeXmlFile(file, files.get(filename))) {
        return false;
      }
    }
    return true;
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
      return false;
    }
    return true;
  }

  /*
   * Runs ApkBuilder by using the API instead of calling its main method because the main method
   * can call System.exit(1), which will bring down our server.
   */
  private boolean runApkBuilder(String apkAbsolutePath, String zipArchive, String dexedClassesDir) {
    try {
      ApkBuilder apkBuilder =
          new ApkBuilder(apkAbsolutePath, zipArchive,
            dexedClassesDir + File.separator + "classes.dex", null, System.out);
      if (hasSecondDex) {
        apkBuilder.addFile(new File(dexedClassesDir + File.separator + "classes2.dex"),
          "classes2.dex");
      }
      if (nativeLibsNeeded.size() != 0) { // Need to add native libraries...
        apkBuilder.addNativeLibraries(libsDir);
      }
      apkBuilder.sealApk();
      return true;
    } catch (Exception e) {
      // This is fatal.
      e.printStackTrace();
      LOG.warning("YAIL compiler - ApkBuilder failed.");
      err.println("YAIL compiler - ApkBuilder failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "ApkBuilder"));
      return false;
    }
  }

  /**
   * Creates a new YAIL compiler.
   *
   * @param project  project to build
   * @param compTypes component types used in the project
   * @param out  stdout stream for compiler messages
   * @param err  stderr stream for compiler messages
   * @param userErrors stream to write user-visible error messages
   * @param childProcessMaxRam  maximum RAM for child processes, in MBs.
   */
  @VisibleForTesting
  Compiler(Project project, Set<String> compTypes, PrintStream out, PrintStream err,
           PrintStream userErrors, boolean isForCompanion,
           int childProcessMaxRam, String dexCacheDir, BuildServer.ProgressReporter reporter) {
    this.project = project;

    prepareCompTypes(compTypes);
    readBuildInfo();

    this.out = out;
    this.err = err;
    this.userErrors = userErrors;
    this.isForCompanion = isForCompanion;
    this.childProcessRamMb = childProcessMaxRam;
    this.dexCacheDir = dexCacheDir;
    this.reporter = reporter;

  }

  /*
   * Runs the Kawa compiler in a separate process to generate classes. Returns false if not able to
   * create a class file for every source file in the project.
   *
   * As a side effect, we generate uniqueLibsNeeded which contains a set of libraries used by
   * runDx. Each library appears in the set only once (which is why it is a set!). This is
   * important because when we Dex the libraries, a given library can only appear once.
   *
   */
  private boolean generateClasses(File classesDir) {
    try {
      List<Project.SourceDescriptor> sources = project.getSources();
      List<String> sourceFileNames = Lists.newArrayListWithCapacity(sources.size());
      List<String> classFileNames = Lists.newArrayListWithCapacity(sources.size());
      boolean userCodeExists = false;
      for (Project.SourceDescriptor source : sources) {
        String sourceFileName = source.getFile().getAbsolutePath();
        LOG.log(Level.INFO, "source file: " + sourceFileName);
        int srcIndex = sourceFileName.indexOf(File.separator + ".." + File.separator + "src" + File.separator);
        String sourceFileRelativePath = sourceFileName.substring(srcIndex + 8);
        String classFileName = (classesDir.getAbsolutePath() + "/" + sourceFileRelativePath)
          .replace(YoungAndroidConstants.YAIL_EXTENSION, ".class");

        // Check whether user code exists by seeing if a left parenthesis exists at the beginning of
        // a line in the file
        // TODO(user): Replace with more robust test of empty source file.
        if (!userCodeExists) {
          Reader fileReader = new FileReader(sourceFileName);
          try {
            while (fileReader.ready()) {
              int c = fileReader.read();
              if (c == '(') {
                userCodeExists = true;
                break;
              }
            }
          } finally {
            fileReader.close();
          }
        }
        sourceFileNames.add(sourceFileName);
        classFileNames.add(classFileName);
      }

      if (!userCodeExists) {
        userErrors.print(NO_USER_CODE_ERROR);
        return false;
      }

      // Construct the class path including component libraries (jars)
      StringBuilder classpath = new StringBuilder(getResource(KAWA_RUNTIME));
      classpath.append(COLON);
      classpath.append(getResource(ACRA_RUNTIME));
      classpath.append(COLON);
      classpath.append(getResource(SIMPLE_ANDROID_RUNTIME_JAR));
      classpath.append(COLON);

      for (String jar : SUPPORT_JARS) {
        classpath.append(getResource(jar));
        classpath.append(COLON);
      }

      // attach the jars of external comps
      Set<String> addedExtJars = new HashSet<String>();
      for (String type : extCompTypes) {
        String sourcePath = getExtCompDirPath(type) + SIMPLE_ANDROID_RUNTIME_JAR;
        if (!addedExtJars.contains(sourcePath)) {  // don't add multiple copies for bundled extensions
          classpath.append(sourcePath);
          classpath.append(COLON);
          addedExtJars.add(sourcePath);
        }
      }

      // Add component library names to classpath
      for (String type : libsNeeded.keySet()) {
        for (String lib : libsNeeded.get(type)) {
          String sourcePath = "";
          String pathSuffix = RUNTIME_FILES_DIR + lib;

          if (simpleCompTypes.contains(type)) {
            sourcePath = getResource(pathSuffix);
          } else if (extCompTypes.contains(type)) {
            sourcePath = getExtCompDirPath(type) + pathSuffix;
          } else {
            userErrors.print(String.format(ERROR_IN_STAGE, "Compile"));
            return false;
          }

          uniqueLibsNeeded.add(sourcePath);

          classpath.append(sourcePath);
          classpath.append(COLON);
        }
      }

      // Add dependencies for classes.jar in any AAR libraries
      for (File classesJar : explodedAarLibs.getClasses()) {
        if (classesJar != null) {  // true for optimized AARs in App Inventor libs
          final String abspath = classesJar.getAbsolutePath();
          uniqueLibsNeeded.add(abspath);
          classpath.append(abspath);
          classpath.append(COLON);
        }
      }
      if (explodedAarLibs.size() > 0) {
        classpath.append(explodedAarLibs.getOutputDirectory().getAbsolutePath());
        classpath.append(COLON);
      }

      classpath.append(getResource(ANDROID_RUNTIME));

      System.out.println("Libraries Classpath = " + classpath);

      String yailRuntime = getResource(YAIL_RUNTIME);
      List<String> kawaCommandArgs = Lists.newArrayList();
      int mx = childProcessRamMb - 200;
      Collections.addAll(kawaCommandArgs,
          System.getProperty("java.home") + "/bin/java",
          "-Dfile.encoding=UTF-8",
          "-mx" + mx + "M",
          "-cp", classpath.toString(),
          "kawa.repl",
          "-f", yailRuntime,
          "-d", classesDir.getAbsolutePath(),
          "-P", Signatures.getPackageName(project.getMainClass()) + ".",
          "-C");
      // TODO(lizlooney) - we are currently using (and have always used) absolute paths for the
      // source file names. The resulting .class files contain references to the source file names,
      // including the name of the tmp directory that contains them. We may be able to avoid that
      // by using source file names that are relative to the project root and using the project
      // root as the working directory for the Kawa compiler process.
      kawaCommandArgs.addAll(sourceFileNames);
      kawaCommandArgs.add(yailRuntime);
      String[] kawaCommandLine = kawaCommandArgs.toArray(new String[kawaCommandArgs.size()]);

      long start = System.currentTimeMillis();
      // Capture Kawa compiler stderr. The ODE server parses out the warnings and errors and adds
      // them to the protocol buffer for logging purposes. (See
      // buildserver/ProjectBuilder.processCompilerOutout.
      ByteArrayOutputStream kawaOutputStream = new ByteArrayOutputStream();
      boolean kawaSuccess;
      synchronized (SYNC_KAWA_OR_DX) {
        kawaSuccess = Execution.execute(null, kawaCommandLine,
            System.out, new PrintStream(kawaOutputStream));
      }
      if (!kawaSuccess) {
        LOG.log(Level.SEVERE, "Kawa compile has failed.");
      }
      String kawaOutput = kawaOutputStream.toString();
      out.print(kawaOutput);
      String kawaCompileTimeMessage = "Kawa compile time: " +
          ((System.currentTimeMillis() - start) / 1000.0) + " seconds";
      out.println(kawaCompileTimeMessage);
      LOG.info(kawaCompileTimeMessage);

      // Check that all of the class files were created.
      // If they weren't, return with an error.
      for (String classFileName : classFileNames) {
        File classFile = new File(classFileName);
        if (!classFile.exists()) {
          LOG.log(Level.INFO, "Can't find class file: " + classFileName);
          String screenName = classFileName.substring(classFileName.lastIndexOf('/') + 1,
              classFileName.lastIndexOf('.'));
          userErrors.print(String.format(COMPILATION_ERROR, screenName));
          return false;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Compile"));
      return false;
    }

    return true;
  }

  private boolean runJarSigner(String apkAbsolutePath, String keystoreAbsolutePath) {
    // TODO(user): maybe make a command line flag for the jarsigner location
    String javaHome = System.getProperty("java.home");
    // This works on Mac OS X.
    File jarsignerFile = new File(javaHome + SLASH + "bin" +
        SLASH + "jarsigner");
    if (!jarsignerFile.exists()) {
      // This works when a JDK is installed with the JRE.
      jarsignerFile = new File(javaHome + SLASH + ".." + SLASH + "bin" +
          SLASH + "jarsigner");
      if (System.getProperty("os.name").startsWith("Windows")) {
        jarsignerFile = new File(javaHome + SLASH + ".." + SLASH + "bin" +
            SLASH + "jarsigner.exe");
      }
      if (!jarsignerFile.exists()) {
        LOG.warning("YAIL compiler - could not find jarsigner.");
        err.println("YAIL compiler - could not find jarsigner.");
        userErrors.print(String.format(ERROR_IN_STAGE, "JarSigner"));
        return false;
      }
    }

    String[] jarsignerCommandLine = {
        jarsignerFile.getAbsolutePath(),
        "-digestalg", "SHA1",
        "-sigalg", "MD5withRSA",
        "-keystore", keystoreAbsolutePath,
        "-storepass", "android",
        apkAbsolutePath,
        "AndroidKey"
    };
    if (!Execution.execute(null, jarsignerCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - jarsigner execution failed.");
      err.println("YAIL compiler - jarsigner execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "JarSigner"));
      return false;
    }

    return true;
  }

  private boolean runZipAlign(String apkAbsolutePath, File tmpDir) {
    // TODO(user): add zipalign tool appinventor->lib->android->tools->linux and windows
    // Need to make sure assets directory exists otherwise zipalign will fail.
    createDir(project.getAssetsDirectory());
    String zipAlignTool;
    String osName = System.getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      zipAlignTool = MAC_ZIPALIGN_TOOL;
    } else if (osName.equals("Linux")) {
      zipAlignTool = LINUX_ZIPALIGN_TOOL;
    } else if (osName.startsWith("Windows")) {
      zipAlignTool = WINDOWS_ZIPALIGN_TOOL;
    } else {
      LOG.warning("YAIL compiler - cannot run ZIPALIGN on OS " + osName);
      err.println("YAIL compiler - cannot run ZIPALIGN on OS " + osName);
      userErrors.print(String.format(ERROR_IN_STAGE, "ZIPALIGN"));
      return false;
    }
    // TODO: create tmp file for zipaling result
    String zipAlignedPath = tmpDir.getAbsolutePath() + SLASH + "zipaligned.apk";
    // zipalign -f -v 4 infile.zip outfile.zip
    String[] zipAlignCommandLine = {
        getResource(zipAlignTool),
        "-f",
        "4",
        apkAbsolutePath,
        zipAlignedPath
    };
    long startZipAlign = System.currentTimeMillis();
    // Using System.err and System.out on purpose. Don't want to pollute build messages with
    // tools output
    if (!Execution.execute(null, zipAlignCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - ZIPALIGN execution failed.");
      err.println("YAIL compiler - ZIPALIGN execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "ZIPALIGN"));
      return false;
    }
    if (!copyFile(zipAlignedPath, apkAbsolutePath)) {
      LOG.warning("YAIL compiler - ZIPALIGN file copy failed.");
      err.println("YAIL compiler - ZIPALIGN file copy failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "ZIPALIGN"));
      return false;
    }
    String zipALignTimeMessage = "ZIPALIGN time: " +
        ((System.currentTimeMillis() - startZipAlign) / 1000.0) + " seconds";
    out.println(zipALignTimeMessage);
    LOG.info(zipALignTimeMessage);
    return true;
  }

  /*
   * Loads the icon for the application, either a user provided one or the default one.
   */
  private boolean prepareApplicationIcon(File outputPngFile) {
    String userSpecifiedIcon = Strings.nullToEmpty(project.getIcon());
    try {
      BufferedImage icon;
      if (!userSpecifiedIcon.isEmpty()) {
        File iconFile = new File(project.getAssetsDirectory(), userSpecifiedIcon);
        icon = ImageIO.read(iconFile);
        if (icon == null) {
          // This can happen if the iconFile isn't an image file.
          // For example, icon is null if the file is a .wav file.
          // TODO(lizlooney) - This happens if the user specifies a .ico file. We should fix that.
          userErrors.print(String.format(ICON_ERROR, userSpecifiedIcon));
          return false;
        }
      } else {
        // Load the default image.
        icon = ImageIO.read(Compiler.class.getResource(DEFAULT_ICON));
      }
      ImageIO.write(icon, "png", outputPngFile);
    } catch (Exception e) {
      e.printStackTrace();
      // If the user specified the icon, this is fatal.
      if (!userSpecifiedIcon.isEmpty()) {
        userErrors.print(String.format(ICON_ERROR, userSpecifiedIcon));
        return false;
      }
    }

    return true;
  }

  private boolean runDx(File classesDir, String dexedClassesDir, boolean secondTry) {
    List<File> libList = new ArrayList<File>();
    List<File> inputList = new ArrayList<File>();
    List<File> class2List = new ArrayList<File>();
    inputList.add(classesDir); //this is a directory, and won't be cached into the dex cache
    inputList.add(new File(getResource(SIMPLE_ANDROID_RUNTIME_JAR)));
    inputList.add(new File(getResource(KAWA_RUNTIME)));
    inputList.add(new File(getResource(ACRA_RUNTIME)));

    for (String jar : SUPPORT_JARS) {
      inputList.add(new File(getResource(jar)));
    }

    for (String lib : uniqueLibsNeeded) {
      libList.add(new File(lib));
    }

    // BEGIN DEBUG -- XXX --
    // System.err.println("runDx -- libraries");
    // for (File aFile : inputList) {
    //   System.err.println(" inputList => " + aFile.getAbsolutePath());
    // }
    // for (File aFile : libList) {
    //   System.err.println(" libList => " + aFile.getAbsolutePath());
    // }
    // END DEBUG -- XXX --

    // attach the jars of external comps to the libraries list
    Set<String> addedExtJars = new HashSet<String>();
    for (String type : extCompTypes) {
      String sourcePath = getExtCompDirPath(type) + SIMPLE_ANDROID_RUNTIME_JAR;
      if (!addedExtJars.contains(sourcePath)) {
        libList.add(new File(sourcePath));
        addedExtJars.add(sourcePath);
      }
    }

    int offset = libList.size();
    // Note: The choice of 12 libraries is arbitrary. We note that things
    // worked to put all libraries into the first classes.dex file when we
    // had 16 libraries and broke at 17. So this is a conservative number
    // to try.
    if (!secondTry) {           // First time through, try base + 12 libraries
      if (offset > 12)
        offset = 12;
    } else {
      offset = 0;               // Add NO libraries the second time through!
    }
    for (int i = 0; i < offset; i++) {
      inputList.add(libList.get(i));
    }

    if (libList.size() - offset > 0) { // Any left over for classes2?
      for (int i = offset; i < libList.size(); i++) {
        class2List.add(libList.get(i));
      }
    }

    DexExecTask dexTask = new DexExecTask();
    dexTask.setExecutable(getResource(DX_JAR));
    dexTask.setOutput(dexedClassesDir + File.separator + "classes.dex");
    dexTask.setChildProcessRamMb(childProcessRamMb);
    if (dexCacheDir == null) {
      dexTask.setDisableDexMerger(true);
    } else {
      createDir(new File(dexCacheDir));
      dexTask.setDexedLibs(dexCacheDir);
    }

    long startDx = System.currentTimeMillis();
    // Using System.err and System.out on purpose. Don't want to pollute build messages with
    // tools output
    boolean dxSuccess;
    synchronized (SYNC_KAWA_OR_DX) {
      setProgress(50);
      dxSuccess = dexTask.execute(inputList);
      if (dxSuccess && (class2List.size() > 0)) {
        setProgress(60);
        dexTask.setOutput(dexedClassesDir + File.separator + "classes2.dex");
        inputList = new ArrayList<File>();
        dxSuccess = dexTask.execute(class2List);
        setProgress(75);
        hasSecondDex = true;
      } else if (!dxSuccess) {  // The initial dx blew out, try more conservative
        LOG.info("DX execution failed, trying with fewer libraries.");
        if (secondTry) {        // Already tried the more conservative approach!
          LOG.warning("YAIL compiler - DX execution failed (secondTry!).");
          err.println("YAIL compiler - DX execution failed.");
          userErrors.print(String.format(ERROR_IN_STAGE, "DX"));
          return false;
        } else {
          return runDx(classesDir, dexedClassesDir, true);
        }
      }
    }
    if (!dxSuccess) {
      LOG.warning("YAIL compiler - DX execution failed.");
      err.println("YAIL compiler - DX execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "DX"));
      return false;
    }
    String dxTimeMessage = "DX time: " +
        ((System.currentTimeMillis() - startDx) / 1000.0) + " seconds";
    out.println(dxTimeMessage);
    LOG.info(dxTimeMessage);

    return true;
  }

  private boolean runAaptPackage(File manifestFile, File resDir, String tmpPackageName, File sourceOutputDir, File symbolOutputDir) {
    // Need to make sure assets directory exists otherwise aapt will fail.
    final File mergedAssetsDir = createDir(project.getBuildDirectory(), ASSET_DIR_NAME);
    String aaptTool;
    String osName = System.getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      aaptTool = MAC_AAPT_TOOL;
    } else if (osName.equals("Linux")) {
      aaptTool = LINUX_AAPT_TOOL;
    } else if (osName.startsWith("Windows")) {
      aaptTool = WINDOWS_AAPT_TOOL;
    } else {
      LOG.warning("YAIL compiler - cannot run AAPT on OS " + osName);
      err.println("YAIL compiler - cannot run AAPT on OS " + osName);
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT"));
      return false;
    }
    if (!mergeResources(resDir, project.getBuildDirectory(), aaptTool)) {
      LOG.warning("Unable to merge resources");
      err.println("Unable to merge resources");
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT"));
      return false;
    }
    List<String> aaptPackageCommandLineArgs = new ArrayList<String>();
    aaptPackageCommandLineArgs.add(getResource(aaptTool));
    aaptPackageCommandLineArgs.add("package");
    aaptPackageCommandLineArgs.add("-v");
    aaptPackageCommandLineArgs.add("-f");
    aaptPackageCommandLineArgs.add("-M");
    aaptPackageCommandLineArgs.add(manifestFile.getAbsolutePath());
    aaptPackageCommandLineArgs.add("-S");
    aaptPackageCommandLineArgs.add(mergedResDir.getAbsolutePath());
    aaptPackageCommandLineArgs.add("-A");
    aaptPackageCommandLineArgs.add(mergedAssetsDir.getAbsolutePath());
    aaptPackageCommandLineArgs.add("-I");
    aaptPackageCommandLineArgs.add(getResource(ANDROID_RUNTIME));
    aaptPackageCommandLineArgs.add("-F");
    aaptPackageCommandLineArgs.add(tmpPackageName);
    if (explodedAarLibs.size() > 0) {
      // If AARs are used, generate R.txt for later processing
      String packageName = Signatures.getPackageName(project.getMainClass());
      aaptPackageCommandLineArgs.add("-m");
      aaptPackageCommandLineArgs.add("-J");
      aaptPackageCommandLineArgs.add(sourceOutputDir.getAbsolutePath());
      aaptPackageCommandLineArgs.add("--custom-package");
      aaptPackageCommandLineArgs.add(packageName);
      aaptPackageCommandLineArgs.add("--output-text-symbols");
      aaptPackageCommandLineArgs.add(symbolOutputDir.getAbsolutePath());
      aaptPackageCommandLineArgs.add("--no-version-vectors");
      appRJava = new File(sourceOutputDir, packageName.replaceAll("\\.", "/") + "/R.java");
      appRTxt = new File(symbolOutputDir, "R.txt");
    }
    String[] aaptPackageCommandLine = aaptPackageCommandLineArgs.toArray(new String[aaptPackageCommandLineArgs.size()]);
    libSetup();                 // Setup /tmp/lib64 on Linux
    long startAapt = System.currentTimeMillis();
    // Using System.err and System.out on purpose. Don't want to pollute build messages with
    // tools output
    if (!Execution.execute(null, aaptPackageCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - AAPT execution failed.");
      err.println("YAIL compiler - AAPT execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT"));
      return false;
    }
    String aaptTimeMessage = "AAPT time: " +
        ((System.currentTimeMillis() - startAapt) / 1000.0) + " seconds";
    out.println(aaptTimeMessage);
    LOG.info(aaptTimeMessage);

    return true;
  }

  private boolean insertNativeLibs(File buildDir){
    /**
     * Native libraries are targeted for particular processor architectures.
     * Here, non-default architectures (ARMv5TE is default) are identified with suffixes
     * before being placed in the appropriate directory with their suffix removed.
     */
    libsDir = createDir(buildDir, LIBS_DIR_NAME);
    File armeabiDir = createDir(libsDir, ARMEABI_DIR_NAME);
    File armeabiV7aDir = createDir(libsDir, ARMEABI_V7A_DIR_NAME);
    File arm64V8aDir = createDir(libsDir, ARM64_V8A_DIR_NAME);
    File x8664Dir = createDir(libsDir, X86_64_DIR_NAME);

    try {
      for (String type : nativeLibsNeeded.keySet()) {
        for (String lib : nativeLibsNeeded.get(type)) {
          boolean isV7a = lib.endsWith(ARMEABI_V7A_SUFFIX);
          boolean isV8a = lib.endsWith(ARM64_V8A_SUFFIX);
          boolean isx8664 = lib.endsWith(X86_64_SUFFIX);

          String sourceDirName;
          File targetDir;
          if (isV7a) {
            sourceDirName = ARMEABI_V7A_DIR_NAME;
            targetDir = armeabiV7aDir;
            lib = lib.substring(0, lib.length() - ARMEABI_V7A_SUFFIX.length());
          } else if (isV8a) {
            sourceDirName = ARM64_V8A_DIR_NAME;
            targetDir = arm64V8aDir;
            lib = lib.substring(0, lib.length() - ARM64_V8A_SUFFIX.length());
          } else if (isx8664) {
            sourceDirName = X86_64_DIR_NAME;
            targetDir = x8664Dir;
            lib = lib.substring(0, lib.length() - X86_64_SUFFIX.length());
          } else {
            sourceDirName = ARMEABI_DIR_NAME;
            targetDir = armeabiDir;
          }

          String sourcePath = "";
          String pathSuffix = RUNTIME_FILES_DIR + sourceDirName + SLASH + lib;

          if (simpleCompTypes.contains(type)) {
            sourcePath = getResource(pathSuffix);
          } else if (extCompTypes.contains(type)) {
            sourcePath = getExtCompDirPath(type) + pathSuffix;
            targetDir = createDir(targetDir, EXT_COMPS_DIR_NAME);
            targetDir = createDir(targetDir, type);
          } else {
            userErrors.print(String.format(ERROR_IN_STAGE, "Native Code"));
            return false;
          }

          Files.copy(new File(sourcePath), new File(targetDir, lib));
        }
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Native Code"));
      return false;
    }
  }

  /**
   * Attach any AAR libraries to the build.
   *
   * @param buildDir Base directory of the build
   * @return true on success, otherwise false
   */
  private boolean attachAarLibraries(File buildDir) {
    final File explodedBaseDir = createDir(buildDir, "exploded-aars");
    final File generatedDir = createDir(buildDir, "generated");
    final File genSrcDir = createDir(generatedDir, "src");
    explodedAarLibs = new AARLibraries(genSrcDir);
    final Set<String> processedLibs = new HashSet<>();

    // walk components list for libraries ending in ".aar"
    try {
      for (Set<String> libs : libsNeeded.values()) {
        Iterator<String> i = libs.iterator();
        while (i.hasNext()) {
          String libname = i.next();
          if (libname.endsWith(".aar")) {
            i.remove();
            if (!processedLibs.contains(libname)) {
              // explode libraries into ${buildDir}/exploded-aars/<package>/
              AARLibrary aarLib = new AARLibrary(new File(getResource(RUNTIME_FILES_DIR + libname)));
              aarLib.unpackToDirectory(explodedBaseDir);
              explodedAarLibs.add(aarLib);
              processedLibs.add(libname);
            }
          }
        }
      }
      return true;
    } catch(IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Attach AAR Libraries"));
      return false;
    }
  }

  private boolean attachCompAssets() {
    createDir(project.getBuildDirectory()); // Needed to insert resources.
    try {
      // Gather non-library assets to be added to apk's Asset directory.
      // The assets directory have been created before this.
      File mergedAssetDir = createDir(project.getBuildDirectory(), ASSET_DIR_NAME);

      // Copy component/extension assets to build/assets
      for (String type : assetsNeeded.keySet()) {
        for (String assetName : assetsNeeded.get(type)) {
          File targetDir = mergedAssetDir;
          String sourcePath;

          if (simpleCompTypes.contains(type)) {
            String pathSuffix = RUNTIME_FILES_DIR + assetName;
            sourcePath = getResource(pathSuffix);
          } else if (extCompTypes.contains(type)) {
            final String extCompDir = getExtCompDirPath(type);
            sourcePath = getExtAssetPath(extCompDir, assetName);
            // If targetDir's location is changed here, you must update Form.java in components to
            // reference the new location. The path for assets in compiled apps is assumed to be
            // assets/EXTERNAL-COMP-PACKAGE/ASSET-NAME
            targetDir = createDir(targetDir, basename(extCompDir));
          } else {
            userErrors.print(String.format(ERROR_IN_STAGE, "Assets"));
            return false;
          }

          Files.copy(new File(sourcePath), new File(targetDir, assetName));
        }
      }

      // Copy project assets to build/assets
      File[] assets = project.getAssetsDirectory().listFiles();
      if (assets != null) {
        for (File asset : assets) {
          if (asset.isFile()) {
            Files.copy(asset, new File(mergedAssetDir, asset.getName()));
          }
        }
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Assets"));
      return false;
    }
  }

  /**
   * Merge XML resources from different dependencies into a single file that can be passed to AAPT.
   *
   * @param mainResDir Directory for resources from the application (i.e., not libraries)
   * @param buildDir Build directory path. Merged resources will be placed at $buildDir/intermediates/res/merged
   * @param aaptTool Path to the AAPT tool
   * @return true if the resources were merged successfully, otherwise false
   */
  private boolean mergeResources(File mainResDir, File buildDir, String aaptTool) {
    // these should exist from earlier build steps
    File intermediates = createDir(buildDir, "intermediates");
    File resDir = createDir(intermediates, "res");
    mergedResDir = createDir(resDir, "merged");
    PngCruncher cruncher = new AaptCruncher(getResource(aaptTool), null, null);
    return explodedAarLibs.mergeResources(mergedResDir, mainResDir, cruncher);
  }

  private boolean generateRClasses(File outputDir) {
    if (explodedAarLibs.size() == 0) {
      return true;  // nothing to see here
    }
    int error;
    try {
      error = explodedAarLibs.writeRClasses(outputDir, Signatures.getPackageName(project.getMainClass()), appRTxt);
    } catch (IOException|InterruptedException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Generate R Classes"));
      return false;
    }
    if (error != 0) {
      System.err.println("javac returned error code " + error);
      userErrors.print(String.format(ERROR_IN_STAGE, "Attach AAR Libraries"));
      return false;
    }
    return true;
  }

  /**
   * Writes out the given resource as a temp file and returns the absolute path.
   * Caches the location of the files, so we can reuse them.
   *
   * @param resourcePath the name of the resource
   */
  static synchronized String getResource(String resourcePath) {
    try {
      File file = resources.get(resourcePath);
      if (file == null) {
        String basename = PathUtil.basename(resourcePath);
        String prefix;
        String suffix;
        int lastDot = basename.lastIndexOf(".");
        if (lastDot != -1) {
          prefix = basename.substring(0, lastDot);
          suffix = basename.substring(lastDot);
        } else {
          prefix = basename;
          suffix = "";
        }
        while (prefix.length() < 3) {
          prefix = prefix + "_";
        }
        file = File.createTempFile(prefix, suffix);
        file.setExecutable(true);
        file.deleteOnExit();
        file.getParentFile().mkdirs();
        Files.copy(Resources.newInputStreamSupplier(Compiler.class.getResource(resourcePath)),
            file);
        resources.put(resourcePath, file);
      }
      return file.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * This code is only invoked on Linux. It copies libc++.so into /tmp/lib64. This
   * is needed on linux to run the aapt tool.
   */
  private void libSetup() {
    String osName = System.getProperty("os.name");
    if (!osName.equals("Linux")) {
      return;                   // Nothing to do (yet) for MacOS and Windows
    }
    try {
      File outFile = new File("/tmp/lib64/libc++.so");
      if (outFile.exists()) {    // Don't do it more then once!
        return;
      }
      File tmpLibDir = new File("/tmp/lib64");
      tmpLibDir.mkdirs();
      Files.copy(Resources.newInputStreamSupplier(Compiler.class.getResource("/tools/linux/lib64/libc++.so")),
        outFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   *  Loads permissions and information on component libraries and assets.
   */
  private void loadJsonInfo(ConcurrentMap<String, Set<String>> infoMap, String targetInfo)
      throws IOException, JSONException {
    synchronized (infoMap) {
      if (!infoMap.isEmpty()) {
        return;
      }

      JSONArray buildInfo = new JSONArray(
          "[" + simpleCompsBuildInfo.join(",") + "," +
          extCompsBuildInfo.join(",") + "]");

      for (int i = 0; i < buildInfo.length(); ++i) {
        JSONObject compJson = buildInfo.getJSONObject(i);
        JSONArray infoArray = null;
        String type = compJson.getString("type");
        try {
          infoArray = compJson.getJSONArray(targetInfo);
        } catch (JSONException e) {
          // Older compiled extensions will not have a broadcastReceiver
          // defined. Rather then require them all to be recompiled, we
          // treat the missing attribute as empty.
          if (e.getMessage().contains("broadcastReceiver")) {
            LOG.log(Level.INFO, "Component \"" + type + "\" does not have a broadcast receiver.");
            continue;
          } else if (e.getMessage().contains(ANDROIDMINSDK_TARGET)) {
            LOG.log(Level.INFO, "Component \"" + type + "\" does not specify a minimum SDK.");
            continue;
          } else {
            throw e;
          }
        }

        if (!simpleCompTypes.contains(type) && !extCompTypes.contains(type)) {
          continue;
        }

        Set<String> infoSet = Sets.newHashSet();
        for (int j = 0; j < infoArray.length(); ++j) {
          String info = infoArray.getString(j);
          if (!info.isEmpty()) {
            infoSet.add(info);
          }
        }

        if (!infoSet.isEmpty()) {
          infoMap.put(type, infoSet);
        }
      }
    }
  }

  /**
   * Copy one file to another. If destination file does not exist, it is created.
   *
   * @param srcPath absolute path to source file
   * @param dstPath absolute path to destination file
   * @return  {@code true} if the copy succeeds, {@code false} otherwise
   */
  private static Boolean copyFile(String srcPath, String dstPath) {
    try {
      FileInputStream in = new FileInputStream(srcPath);
      FileOutputStream out = new FileOutputStream(dstPath);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
    catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Creates a new directory (if it doesn't exist already).
   *
   * @param dir  new directory
   * @return  new directory
   */
  private static File createDir(File dir) {
    if (!dir.exists()) {
      dir.mkdir();
    }
    return dir;
  }

  /**
   * Creates a new directory (if it doesn't exist already).
   *
   * @param parentDir  parent directory of new directory
   * @param name  name of new directory
   * @return  new directory
   */
  private static File createDir(File parentDir, String name) {
    File dir = new File(parentDir, name);
    if (!dir.exists()) {
      dir.mkdir();
    }
    return dir;
  }

  private void setProgress(int increments) {
    LOG.info("The current progress is "
              + increments + "%");
    if (reporter != null) {
      reporter.report(increments);
    }
  }

  private void readBuildInfo() {
    try {
      simpleCompsBuildInfo = new JSONArray(Resources.toString(
          Compiler.class.getResource(COMP_BUILD_INFO), Charsets.UTF_8));

      extCompsBuildInfo = new JSONArray();
      Set<String> readComponentInfos = new HashSet<String>();
      for (String type : extCompTypes) {
        // .../assets/external_comps/com.package.MyExtComp/files/component_build_info.json
        File extCompRuntimeFileDir = new File(getExtCompDirPath(type) + RUNTIME_FILES_DIR);
        if (!extCompRuntimeFileDir.exists()) {
          // try extension package name for multi-extension files
          String path = getExtCompDirPath(type);
          path = path.substring(0, path.lastIndexOf('.'));
          extCompRuntimeFileDir = new File(path + RUNTIME_FILES_DIR);
        }
        File jsonFile = new File(extCompRuntimeFileDir, "component_build_infos.json");
        if (!jsonFile.exists()) {
          // old extension with a single component?
          jsonFile = new File(extCompRuntimeFileDir, "component_build_info.json");
          if (!jsonFile.exists()) {
            throw new IllegalStateException("No component_build_info.json in extension for " +
                type);
          }
        }
        if (readComponentInfos.contains(jsonFile.getAbsolutePath())) {
          continue;  // already read the build infos for this type (bundle extension)
        }

        String buildInfo = Resources.toString(jsonFile.toURI().toURL(), Charsets.UTF_8);
        JSONTokener tokener = new JSONTokener(buildInfo);
        Object value = tokener.nextValue();
        if (value instanceof JSONObject) {
          extCompsBuildInfo.put((JSONObject) value);
          readComponentInfos.add(jsonFile.getAbsolutePath());
        } else if (value instanceof JSONArray) {
          JSONArray infos = (JSONArray) value;
          for (int i = 0; i < infos.length(); i++) {
            extCompsBuildInfo.put(infos.getJSONObject(i));
          }
          readComponentInfos.add(jsonFile.getAbsolutePath());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void prepareCompTypes(Set<String> neededTypes) {
    try {
      JSONArray buildInfo = new JSONArray(Resources.toString(
          Compiler.class.getResource(COMP_BUILD_INFO), Charsets.UTF_8));

      Set<String> allSimpleTypes = Sets.newHashSet();
      for (int i = 0; i < buildInfo.length(); ++i) {
        JSONObject comp = buildInfo.getJSONObject(i);
        allSimpleTypes.add(comp.getString("type"));
      }

      simpleCompTypes = Sets.newHashSet(neededTypes);
      simpleCompTypes.retainAll(allSimpleTypes);

      extCompTypes = Sets.newHashSet(neededTypes);
      extCompTypes.removeAll(allSimpleTypes);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getExtCompDirPath(String type) {
    createDir(project.getAssetsDirectory());
    String candidate = extTypePathCache.get(type);
    if (candidate != null) {  // already computed the path
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + SLASH + EXT_COMPS_DIR_NAME +
        SLASH + type;
    if (new File(candidate).exists()) {  // extension has FCQN as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    candidate = project.getAssetsDirectory().getAbsolutePath() + SLASH +
        EXT_COMPS_DIR_NAME + SLASH + type.substring(0, type.lastIndexOf('.'));
    if (new File(candidate).exists()) {  // extension has package name as path element
      extTypePathCache.put(type, candidate);
      return candidate;
    }
    throw new IllegalStateException("Project lacks extension directory for " + type);
  }

  private static String basename(String path) {
    return new File(path).getName();
  }

  private static String getExtAssetPath(String extCompDir, String assetName) {
    return extCompDir + File.separator + ASSET_DIR_NAME + File.separator + assetName;
  }
}
