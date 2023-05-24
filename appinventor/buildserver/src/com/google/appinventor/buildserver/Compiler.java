// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.android.ide.common.internal.AaptCruncher;
import com.android.ide.common.internal.PngCruncher;
import com.android.sdklib.build.ApkBuilder;
import com.google.appinventor.buildserver.stats.StatReporter;
import com.google.appinventor.buildserver.util.AARLibraries;
import com.google.appinventor.buildserver.util.AARLibrary;
import com.google.appinventor.buildserver.util.PermissionConstraint;
import com.google.appinventor.components.common.ComponentDescriptorConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

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
  private static final String SLASHREGEX = File.separatorChar == '\\' ? "\\\\" : "/";
  private static final String COLON = File.pathSeparator;
  private static final String ZIPSLASH = "/";

  public static final String RUNTIME_FILES_DIR = "/files/";
  public static final String RUNTIME_TOOLS_DIR = "/tools/";

  // Native library directory names
  private static final String LIBS_DIR_NAME = "libs";
  private static final String ARMEABI_DIR_NAME = "armeabi";
  private static final String ARMEABI_V7A_DIR_NAME = "armeabi-v7a";
  private static final String ARM64_V8A_DIR_NAME = "arm64-v8a";
  private static final String X86_64_DIR_NAME = "x86_64";

  private static final String ASSET_DIR_NAME = "assets";
  private static final String EXT_COMPS_DIR_NAME = "external_comps";

  private static final String DEFAULT_ICON = RUNTIME_FILES_DIR + "ya.png";

  /*
   * Resource paths to yail runtime, runtime library files and sdk tools.
   * To get the real file paths, call getResource() with one of these constants.
   */
  private static final String ACRA_RUNTIME =
      RUNTIME_FILES_DIR + "acra-4.4.0.jar";
  private static final String ANDROID_RUNTIME =
      RUNTIME_FILES_DIR + "android.jar";
  private static final String[] SUPPORT_JARS;
  private static final String[] SUPPORT_AARS;
  private static final String COMP_BUILD_INFO =
      RUNTIME_FILES_DIR + "simple_components_build_info.json";
  private static final String DX_JAR =
      RUNTIME_TOOLS_DIR + "dx.jar";
  private static final String KAWA_RUNTIME =
      RUNTIME_FILES_DIR + "kawa.jar";
  private static final String SIMPLE_ANDROID_RUNTIME_JAR =
      RUNTIME_FILES_DIR + "AndroidRuntime.jar";
  private static final String APKSIGNER_JAR =
      RUNTIME_TOOLS_DIR + "apksigner.jar";

  /*
   * Note for future updates: This list can be obtained from an Android Studio project running the
   * following command:
   *
   * ./gradlew :app:dependencies --configuration releaseRuntimeClasspath --console=plain | \
   *     awk 'BEGIN {FS="--- "} {print $2}' | cut -d : -f2 | sort -u
   */
  private static final Set<String> CRITICAL_JARS =
      new HashSet<>(Arrays.asList(
          // Minimum required for Android 4.x
          RUNTIME_FILES_DIR + "appcompat.jar",
          RUNTIME_FILES_DIR + "collection.jar",
          RUNTIME_FILES_DIR + "core.jar",
          RUNTIME_FILES_DIR + "core-common.jar",
          RUNTIME_FILES_DIR + "lifecycle-common.jar",
          RUNTIME_FILES_DIR + "vectordrawable.jar",
          RUNTIME_FILES_DIR + "vectordrawable-animated.jar",

          // Extras that may be pulled
          RUNTIME_FILES_DIR + "annotation.jar",
          RUNTIME_FILES_DIR + "asynclayoutinflater.jar",
          RUNTIME_FILES_DIR + "coordinatorlayout.jar",
          RUNTIME_FILES_DIR + "core-runtime.jar",
          RUNTIME_FILES_DIR + "cursoradapter.jar",
          RUNTIME_FILES_DIR + "customview.jar",
          RUNTIME_FILES_DIR + "documentfile.jar",
          RUNTIME_FILES_DIR + "drawerlayout.jar",
          RUNTIME_FILES_DIR + "fragment.jar",
          RUNTIME_FILES_DIR + "interpolator.jar",
          RUNTIME_FILES_DIR + "legacy-support-core-ui.jar",
          RUNTIME_FILES_DIR + "legacy-support-core-utils.jar",
          RUNTIME_FILES_DIR + "lifecycle-livedata.jar",
          RUNTIME_FILES_DIR + "lifecycle-livedata-core.jar",
          RUNTIME_FILES_DIR + "lifecycle-runtime.jar",
          RUNTIME_FILES_DIR + "lifecycle-viewmodel.jar",
          RUNTIME_FILES_DIR + "loader.jar",
          RUNTIME_FILES_DIR + "localbroadcastmanager.jar",
          RUNTIME_FILES_DIR + "print.jar",
          RUNTIME_FILES_DIR + "slidingpanelayout.jar",
          RUNTIME_FILES_DIR + "swiperefreshlayout.jar",
          RUNTIME_FILES_DIR + "versionedparcelable.jar",
          RUNTIME_FILES_DIR + "viewpager.jar"
      ));

  private static final String LINUX_AAPT_TOOL =
      RUNTIME_TOOLS_DIR + "linux/aapt";
  private static final String LINUX_ZIPALIGN_TOOL =
      RUNTIME_TOOLS_DIR + "linux/zipalign";
  private static final String MAC_AAPT_TOOL =
      RUNTIME_TOOLS_DIR + "mac/aapt";
  private static final String MAC_ZIPALIGN_TOOL =
      RUNTIME_TOOLS_DIR + "mac/zipalign";
  private static final String WINDOWS_AAPT_TOOL =
      RUNTIME_TOOLS_DIR + "windows/aapt";
  private static final String WINDOWS_PTHEAD_DLL =
      RUNTIME_TOOLS_DIR + "windows/libwinpthread-1.dll";
  private static final String WINDOWS_ZIPALIGN_TOOL =
      RUNTIME_TOOLS_DIR + "windows/zipalign";

  private static final String LINUX_AAPT2_TOOL =
      RUNTIME_TOOLS_DIR + "linux/aapt2";
  private static final String MAC_AAPT2_TOOL =
      RUNTIME_TOOLS_DIR + "mac/aapt2";
  private static final String WINDOWS_AAPT2_TOOL =
      RUNTIME_TOOLS_DIR + "windows/aapt2";
  private static final String BUNDLETOOL_JAR =
      RUNTIME_TOOLS_DIR + "bundletool.jar";

  @VisibleForTesting
  static final String YAIL_RUNTIME = RUNTIME_FILES_DIR + "runtime.scm";

  private final ConcurrentMap<String, Set<String>> assetsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> activitiesNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> metadataNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> activityMetadataNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> broadcastReceiversNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> queriesNeeded =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Set<String>> servicesNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> contentProvidersNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> libsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> nativeLibsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> permissionsNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  /**
   * Maps types to permissions to permission constraints.
   */
  private final ConcurrentMap<String, Map<String, Set<PermissionConstraint<?>>>>
      permissionConstraintsNeeded = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Set<String>> minSdksNeeded =
      new ConcurrentHashMap<String, Set<String>>();
  private final Set<String> uniqueLibsNeeded = Sets.newHashSet();
  private final ConcurrentMap<String, Map<String, Map<String, Set<String>>>> conditionals =
      new ConcurrentHashMap<>();
  /**
   * Maps types to blocks to permissions to permission constraints.
   */
  private final ConcurrentMap<String, Map<String, Map<String, Set<PermissionConstraint<?>>>>>
      conditionalPermissionConstraints = new ConcurrentHashMap<>();

  /**
   * Maps component type names to a set of blocks used in the project from the
   * named component. For example, Hello Purr might produce:
   *
   * <code>
   *   {
   *     "Button": {"Click", "Image", "Text"},
   *     "Screen": {"Title"},
   *     "Sound": {"Play", "Source", "Vibrate"}
   *   }
   * </code>
   */
  private final Map<String, Set<String>> compBlocks;

  /**
   * Maps Screen names to their orientation values to populate the
   * android:screenOrientation attribution in the &lt;activity&gt; element.
   */
  private final Map<String, String> formOrientations;

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

  /**
   * Zip file containing all compiled resources with AAPT2
   */
  private File resourcesZip;

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

  static {
    List<String> aars = new ArrayList<>();
    List<String> jars = new ArrayList<>();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(Compiler.class.getResourceAsStream(RUNTIME_FILES_DIR + "aars.txt")))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (!line.isEmpty()) {
          aars.add(line);
        } else {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Fatal error on startup reading aars.txt");
      e.printStackTrace();
      System.exit(1);
    }
    SUPPORT_AARS = aars.toArray(new String[0]);
    try (BufferedReader in = new  BufferedReader(new InputStreamReader(Compiler.class.getResourceAsStream(RUNTIME_FILES_DIR + "jars.txt")))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (!line.isEmpty()) {
          jars.add(RUNTIME_FILES_DIR + line);
        } else {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Fatal error on startup reading jars.txt");
      e.printStackTrace();
      System.exit(1);
    }
    SUPPORT_JARS = jars.toArray(new String[0]);
  }

  private final int childProcessRamMb;  // Maximum ram that can be used by a child processes, in MB.
  private final boolean isForCompanion;
  private final boolean isForEmulator;
  private final boolean includeDangerousPermissions;
  private final Project project;
  private final PrintStream out;
  private final PrintStream err;
  private final PrintStream userErrors;

  private File libsDir; // The directory that will contain any native libraries for packaging
  private String dexCacheDir;

  private JSONArray simpleCompsBuildInfo;
  private JSONArray extCompsBuildInfo;
  private JSONArray buildInfo;
  private Set<String> simpleCompTypes;  // types needed by the project
  private Set<String> extCompTypes; // types needed by the project

  /**
   * A list of the dex files created by {@link #runMultidex}.
   */
  private List<File> dexFiles = new ArrayList<>();

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
      loadJsonInfo(permissionsNeeded, ComponentDescriptorConstants.PERMISSIONS_TARGET);
      loadPermissionConstraints();
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

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.PERMISSIONS_TARGET), permissionsNeeded);
    mergeConditionalPermissionConstraints();

    int n = 0;
    for (String type : permissionsNeeded.keySet()) {
      n += permissionsNeeded.get(type).size();
    }

    System.out.println("Permissions needed, n = " + n);
  }

  /**
   * Merge the given {@code values} into the set at {@code key} in {@code map}.
   * If {@code key} is not set, then its value is treated as the empty set and
   * the key is set to a copy of {@code values}. {@code values} can be unmodifiable.
   * @param map A mapping of strings to sets of strings, representing component
   *            types to, e.g., permissions
   * @param key The key to evaluate, e.g., "Texting"
   * @param values The values associated with the key that need to be merged, e.g.,
   *               {"android.permission.SEND_SMS"}
   */
  private void setOrMerge(Map<String, Set<String>> map, String key, Set<String> values) {
    if (map.containsKey(key)) {
      map.get(key).addAll(values);
    } else {
      map.put(key, new HashSet<>(values));
    }
  }

  /**
   * Merge the conditionals from the given conditional map into the existing
   * map of required infos.
   * @param conditionalMap A map of component type names to maps of blocks to
   *                       sets of values (e.g., permission names)
   * @param infoMap A map of component type names to sets of values (e.g.,
   *                permission names)
   */
  private void mergeConditionals(Map<String, Map<String, Set<String>>> conditionalMap,
                                 Map<String, Set<String>> infoMap) {
    if (conditionalMap != null) {
      if (isForCompanion) {
        // For the companion, we take all of the conditionals
        for (Map.Entry<String, Map<String, Set<String>>> entry : conditionalMap.entrySet()) {
          for (Set<String> items : entry.getValue().values()) {
            setOrMerge(infoMap, entry.getKey(), items);
          }
        }
        // If necessary, we can remove permissions at this point (e.g., Texting, PhoneCall)
      } else {
        // We walk the set of components and the blocks used in the project. If
        // any <component, block> combination is in the set of conditionals,
        // then we merge the associated set of values into the existing set. If
        // no existing set exists, we create one.
        for (Map.Entry<String, Set<String>> entry : compBlocks.entrySet()) {
          if (conditionalMap.containsKey(entry.getKey())) {
            Map<String, Set<String>> blockPermsMap = conditionalMap.get(entry.getKey());
            for (String blockName : entry.getValue()) {
              Set<String> blockPerms = blockPermsMap.get(blockName);
              if (blockPerms != null) {
                Set<String> typePerms = infoMap.get(entry.getKey());
                if (typePerms != null) {
                  typePerms.addAll(blockPerms);
                } else {
                  infoMap.put(entry.getKey(), new HashSet<>(blockPerms));
                }
              }
            }
          }
        }
      }
    }
  }

  private void mergeConditionalPermissionConstraints() {
    if (isForCompanion) {
      return;  // We don't want to place additional constraints on the companion
    }
    for (String type : conditionalPermissionConstraints.keySet()) {
      String name = type.substring(type.lastIndexOf('.') + 1);
      Set<String> blocks = compBlocks.get(name);
      if (blocks == null) {  // this component wasn't used
        continue;
      }
      Map<String, Map<String, Set<PermissionConstraint<?>>>> blockPermsMap =
          conditionalPermissionConstraints.get(type);
      if (!permissionConstraintsNeeded.containsKey(type)) {
        permissionConstraintsNeeded.put(type,
            new HashMap<String, Set<PermissionConstraint<?>>>());
      }
      Map<String, Set<PermissionConstraint<?>>> permConstraints =
          permissionConstraintsNeeded.get(type);
      for (String blockName : blocks) {
        Map<String, Set<PermissionConstraint<?>>> blockPerms = blockPermsMap.get(blockName);
        if (blockPerms == null) {
          // No constraints specified by this block
          continue;
        }
        for (Map.Entry<String, Set<PermissionConstraint<?>>> entry1 : blockPerms.entrySet()) {
          String permName = entry1.getKey();
          Set<PermissionConstraint<?>> constraints = entry1.getValue();
          if (permConstraints.containsKey(permName)) {
            permConstraints.get(permName).addAll(constraints);
          } else {
            permConstraints.put(permName, new HashSet<>(constraints));
          }
        }
      }
    }
  }

  private void outputPermissionConstraints(Writer out,
      Multimap<String, PermissionConstraint<?>> permissionConstraints, String permission)
      throws IOException {
    Collection<PermissionConstraint<?>> constraints = permissionConstraints.get(permission);
    if (constraints == null) {
      return;
    }

    Multimap<String, PermissionConstraint<?>> aggregates = HashMultimap.create();
    for (PermissionConstraint<?> constraint : constraints) {
      aggregates.put(constraint.getAttribute(), constraint);
    }

    for (Map.Entry<String, Collection<PermissionConstraint<?>>> entry : aggregates.asMap().entrySet()) {
      String attribute = entry.getKey();
      // TODO(ewpatton): Figure out a more generic way of doing this.
      String value;
      if ("maxSdkVersion".equals(attribute)) {
        PermissionConstraint.Reducer<Integer> reducer = new PermissionConstraint.MaxReducer();
        for (PermissionConstraint<?> constraint : entry.getValue()) {
          constraint.as(Integer.class).apply(reducer);
        }
        value = reducer.toString();
      } else if ("usesPermissionFlags".equals(attribute)) {
        PermissionConstraint.Reducer<String> reducer = new PermissionConstraint.UnionReducer<>();
        for (PermissionConstraint<?> constraint : entry.getValue()) {
          constraint.as(String.class).apply(reducer);
        }
        value = reducer.toString();
      } else {
        throw new IllegalArgumentException("Unrecognized permission constraint: " + attribute);
      }
      out.write(" android:" + attribute + "=\"" + value + "\"");
    }
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

  @VisibleForTesting
  Map<String, Set<String>> getQueries() {
    return queriesNeeded;
  }

  // Just used for testing
  @VisibleForTesting
  Map<String, Set<String>> getServices() {
    return servicesNeeded;
  }

  // Just used for testing
  @VisibleForTesting
  Map<String, Set<String>> getContentProviders() {
    return contentProvidersNeeded;
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
      loadJsonInfo(libsNeeded, ComponentDescriptorConstants.LIBRARIES_TARGET);
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
    if (isForEmulator) {  // no libraries for emulator
      return;
    }
    try {
      loadJsonInfo(nativeLibsNeeded, ComponentDescriptorConstants.NATIVE_TARGET);
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
      loadJsonInfo(assetsNeeded, ComponentDescriptorConstants.ASSETS_TARGET);
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
      loadJsonInfo(activitiesNeeded, ComponentDescriptorConstants.ACTIVITIES_TARGET);
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

  /**
   * Generate a set of conditionally included metadata needed by this project.
   */
  @VisibleForTesting
  void generateMetadata() {
    try {
      loadJsonInfo(metadataNeeded, ComponentDescriptorConstants.METADATA_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Metadata"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Metadata"));
    }

    int n = 0;
    for (String type : metadataNeeded.keySet()) {
      n += metadataNeeded.get(type).size();
    }

    System.out.println("Component metadata needed, n = " + n);
  }

  /**
   * Generate a set of conditionally included activity metadata needed by this project.
   */
  @VisibleForTesting
  void generateActivityMetadata() {
    try {
      loadJsonInfo(activityMetadataNeeded, ComponentDescriptorConstants.ACTIVITY_METADATA_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Activity Metadata"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Activity Metadata"));
    }

    int n = 0;
    for (String type : activityMetadataNeeded.keySet()) {
      n += activityMetadataNeeded.get(type).size();
    }

    System.out.println("Component activity metadata needed, n = " + n);
  }

  /*
   * Generate a set of conditionally included broadcast receivers needed by this project.
   */
  @VisibleForTesting
  void generateBroadcastReceivers() {
    try {
      loadJsonInfo(broadcastReceiversNeeded, ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET);
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

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET), broadcastReceiversNeeded);
  }

  /*
   * Generate a set of conditionally included queries needed by this project.
   */
  @VisibleForTesting
  void generateQueries() {
    try {
      loadJsonInfo(queriesNeeded, ComponentDescriptorConstants.QUERIES_TARGET);
    } catch (IOException e) {
      // This is fatal.
      userErrors.print(String.format(ERROR_IN_STAGE, "Services"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      userErrors.print(String.format(ERROR_IN_STAGE, "Services"));
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.QUERIES_TARGET), queriesNeeded);
  }

  /*
   * Generate a set of conditionally included services needed by this project.
   */
  @VisibleForTesting
  void generateServices() {
    try {
      loadJsonInfo(servicesNeeded, ComponentDescriptorConstants.SERVICES_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Services"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Services"));
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.SERVICES_TARGET), servicesNeeded);
  }

  /*
   * Generate a set of conditionally included content providers needed by this project.
   */
  @VisibleForTesting
  void generateContentProviders() {
    try {
      loadJsonInfo(contentProvidersNeeded, ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET);
    } catch (IOException e) {
      // This is fatal.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Content Providers"));
    } catch (JSONException e) {
      // This is fatal, but shouldn't actually ever happen.
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "Content Providers"));
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET), contentProvidersNeeded);
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
      loadJsonInfo(componentBroadcastReceiver, ComponentDescriptorConstants.BROADCAST_RECEIVER_TARGET);
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
      loadJsonInfo(minSdksNeeded, ComponentDescriptorConstants.ANDROIDMINSDK_TARGET);
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
   * @param sdk The SDK version that the theme overlays
   * @throws IOException if the writer cannot be written to.
   */
  private static void writeTheme(Writer out, String name, String parent, int sdk) throws IOException {
    out.write("<style name=\"");
    out.write(name);
    out.write("\" parent=\"");
    out.write(parent);
    out.write("\">\n");
    out.write("<item name=\"colorPrimary\">@color/colorPrimary</item>\n");
    out.write("<item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>\n");
    out.write("<item name=\"colorAccent\">@color/colorAccent</item>\n");
    boolean holo = sdk >= 11 && sdk < 21;
    boolean needsClassicSwitch = false;
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
    } else {
      out.write("<item name=\"switchStyle\">@style/ClassicSwitch</item>\n");
      needsClassicSwitch = true;
    }
    out.write("</style>\n");
    if (needsClassicSwitch) {
      out.write("<style name=\"ClassicSwitch\" parent=\"Widget.AppCompat.CompoundButton.Switch\">\n");
      if (sdk == 23) {
        out.write("<item name=\"android:background\">@drawable/abc_control_background_material</item>\n");
      } else {
        out.write("<item name=\"android:background\">@drawable/abc_item_background_holo_light</item>\n");
      }
      out.write("</style>\n");
    }
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
    String colorPrimary = project.getPrimaryColor();
    String colorPrimaryDark = project.getPrimaryColorDark();
    String colorAccent = project.getAccentColor();
    String theme = project.getTheme();
    String actionbar = project.getActionBar();
    String parentTheme;
    boolean isClassicTheme = "Classic".equals(theme) || suffix.isEmpty();  // Default to classic theme prior to SDK 11
    boolean needsBlackTitleText = false;
    boolean holo = "-v11".equals(suffix) || "-v14".equals(suffix);
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

      writeTheme(out, "AppTheme", parentTheme,
          suffix.isEmpty() ? 7 : Integer.parseInt(suffix.substring(2)));
      if (!isClassicTheme) {
        if (holo) {  // Handle Holo
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

  private boolean createNetworkConfigXml(File configDir) {
    File networkConfig = new File(configDir, "network_security_config.xml");
    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(networkConfig)))) {
      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      out.println("<network-security-config>");
      out.println("<base-config cleartextTrafficPermitted=\"true\">");
      out.println("<trust-anchors>");
      out.println("<certificates src=\"system\"/>");
      out.println("</trust-anchors>");
      out.println("</base-config>");
      out.println("</network-security-config>");
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

  // Writes ic_launcher.xml to initialize adaptive icon
  private boolean writeICLauncher(File adaptiveIconFile, boolean isRound) {
    String mainClass = project.getMainClass();
    String packageName = Signatures.getPackageName(mainClass);
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(adaptiveIconFile), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<adaptive-icon " + "xmlns:android=\"http://schemas.android.com/apk/res/android\" " + ">\n");
      out.write("<background android:drawable=\"@color/ic_launcher_background\" />\n");
      out.write("<foreground android:drawable=\"@mipmap/ic_launcher_foreground\" />\n");
      out.write("</adaptive-icon>\n");
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "ic launcher"));
      return false;
    }
    return true;
  }

  // Writes ic_launcher_background.xml to indicate background color of adaptive icon
  private boolean writeICLauncherBackground(File icBackgroundFile) {
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(icBackgroundFile), "UTF-8"));
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      out.write("<resources>\n");
      out.write("<color name=\"ic_launcher_background\">#ffffff</color>\n");
      out.write("</resources>\n");
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      userErrors.print(String.format(ERROR_IN_STAGE, "ic launcher background"));
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
    String vCode = project.getVCode();
    String vName = cleanName(project.getVName());
    if (includeDangerousPermissions) {
      vName += "u";
    }
    String aName = cleanName(project.getAName());
    LOG.log(Level.INFO, "VCode: " + vCode);
    LOG.log(Level.INFO, "VName: " + vName);

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
        if (isForEmulator) {
          out.write("  <uses-feature android:name=\"android.hardware.wifi\" android:required=\"false\" />\n"); // We actually require wifi
        } else {
          out.write("  <uses-feature android:name=\"android.hardware.wifi\" />\n"); // We actually require wifi
        }
      }

      if (queriesNeeded.size() > 0) {
        out.write("  <queries>\n");
        for (Map.Entry<String, Set<String>> componentSubElSetPair : queriesNeeded.entrySet()) {
          Set<String> subelementSet = componentSubElSetPair.getValue();
          for (String subelement : subelementSet) {
            // replace %packageName% with the actual packageName
            out.write(subelement.replace("%packageName%", packageName));
          }
        }
        out.write("  </queries>\n");
      }
      int minSdk = Integer.parseInt(project.getMinSdk());
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
      if (usesLegacyFileAccess()) {
        permissions.add("android.permission.READ_EXTERNAL_STORAGE");
        permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
      }

      // Remove Google's Forbidden Permissions
      // This code is crude because we had to do this on short notice
      // List of permissions taken from
      // https://support.google.com/googleplay/android-developer/answer/9047303#intended
      if (isForCompanion && !includeDangerousPermissions) {
        // Default SMS handler
        permissions.remove("android.permission.READ_SMS");
        permissions.remove("android.permission.RECEIVE_MMS");
        permissions.remove("android.permission.RECEIVE_SMS");
        permissions.remove("android.permission.RECEIVE_WAP_PUSH");
        permissions.remove("android.permission.SEND_SMS");
        permissions.remove("android.permission.WRITE_SMS");
        // Default Phone handler
        permissions.remove("android.permission.PROCESS_OUTGOING_CALLS");
        permissions.remove("android.permission.CALL_PHONE");
        permissions.remove("android.permission.READ_CALL_LOG");
        permissions.remove("android.permission.WRITE_CALL_LOG");
      }

      Multimap<String, PermissionConstraint<?>> permissionConstraints = HashMultimap.create();
      for (Map<String, Set<PermissionConstraint<?>>> constraints : permissionConstraintsNeeded.values()) {
        for (Map.Entry<String, Set<PermissionConstraint<?>>> entry : constraints.entrySet()) {
          permissionConstraints.putAll(entry.getKey(), entry.getValue());
        }
      }

      for (String permission : permissions) {
        if ("android.permission.WRITE_EXTERNAL_STORAGE".equals(permission)) {
          out.write("  <uses-permission android:name=\"" + permission + "\"");

          // we don't need these permissions post KitKat, but we do need them for the companion
          if (!isForCompanion && !usesLegacyFileAccess() && minSdk < 29) {
            out.write(" android:maxSdkVersion=\"29\"");
          }

        } else {
          out.write("  <uses-permission android:name=\""
              // replace %packageName% with the actual packageName
              + permission.replace("%packageName%", packageName)
              + "\"");
        }
        outputPermissionConstraints(out, permissionConstraints, permission);
        out.write(" />");
      }

      if (isForCompanion) {      // This is so ACRA can do a logcat on phones older then Jelly Bean
        out.write("  <uses-permission android:name=\"android.permission.READ_LOGS\" />\n");
      }

      // TODO(markf): Change the minSdkVersion below if we ever require an SDK beyond 1.5.
      // The market will use the following to filter apps shown to devices that don't support
      // the specified SDK version.  We right now support building for minSDK 4.
      // We might also want to allow users to specify minSdk version or targetSDK version.
      out.write("  <uses-sdk android:minSdkVersion=\"" + minSdk + "\" android:targetSdkVersion=\"" +
          YaVersion.TARGET_SDK_VERSION + "\" />\n");

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
      out.write("android:networkSecurityConfig=\"@xml/network_security_config\" ");
      out.write("android:requestLegacyExternalStorage=\"true\" ");  // For SDK 29 (Android Q)
      if (YaVersion.TARGET_SDK_VERSION >= 30) {
        out.write("android:preserveLegacyExternalStorage=\"true\" ");  // For SDK 30 (Android R)
      }
      out.write("android:icon=\"@mipmap/ic_launcher\" ");
      out.write("android:roundIcon=\"@mipmap/ic_launcher\" ");
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

      out.write("<uses-library android:name=\"org.apache.http.legacy\" android:required=\"false\" />");

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
        // The line below is required for Android 12+
        out.write("android:exported=\"true\" ");
        out.write("android:screenOrientation=\"");
        out.write(formOrientations.get(source.getSimpleName()));
        out.write("\" ");

        out.write("android:windowSoftInputMode=\"stateHidden\" ");

        // The keyboard option prevents the app from stopping when a external (bluetooth)
        // keyboard is attached.
        out.write("android:configChanges=\"orientation|screenSize|keyboardHidden|keyboard|"
            + "screenLayout|smallestScreenSize\">\n");

        out.write("      <intent-filter>\n");
        out.write("        <action android:name=\"android.intent.action.MAIN\" />\n");
        if (isMain) {
          out.write("        <category android:name=\"android.intent.category.LAUNCHER\" />\n");
        }
        out.write("      </intent-filter>\n");
        if (isForCompanion) {
          out.write("<intent-filter>\n");
          out.write("<action android:name=\"android.intent.action.VIEW\" />\n");
          out.write("<category android:name=\"android.intent.category.DEFAULT\" />\n");
          out.write("<category android:name=\"android.intent.category.BROWSABLE\" />\n");
          out.write("<data android:scheme=\"aicompanion\" android:host=\"comp\" />\n");
          out.write("</intent-filter>\n");
        }

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

        Set<Map.Entry<String, Set<String>>> metadataElements = activityMetadataNeeded.entrySet();

        // If any component needs to register additional activity metadata,
        // insert them into the manifest here.
        if (!metadataElements.isEmpty()) {
          for (Map.Entry<String, Set<String>> metadataElementSetPair : metadataElements) {
            Set<String> metadataElementSet = metadataElementSetPair.getValue();
            for (String metadataElement : metadataElementSet) {
              out.write(
                metadataElement
                  .replace("%packageName%", packageName) // replace %packageName% with the actual packageName
              );
            }
          }
        }

        out.write("    </activity>\n");

        // Companion display a splash screen... define it's activity here
        if (isMain && isForCompanion) {
          out.write("    <activity android:name=\"com.google.appinventor.components.runtime.SplashActivity\" android:exported=\"false\" android:screenOrientation=\"behind\" android:configChanges=\"keyboardHidden|orientation\">\n");
          out.write("      <intent-filter>\n");
          out.write("        <action android:name=\"android.intent.action.MAIN\" />\n");
          out.write("      </intent-filter>\n");
          out.write("    </activity>\n");
        }
      }

      // Collect any additional <application> subelements into a single set.
      Set<Map.Entry<String, Set<String>>> subelements = Sets.newHashSet();
      subelements.addAll(activitiesNeeded.entrySet());
      subelements.addAll(metadataNeeded.entrySet());
      subelements.addAll(broadcastReceiversNeeded.entrySet());
      subelements.addAll(servicesNeeded.entrySet());
      subelements.addAll(contentProvidersNeeded.entrySet());


      // If any component needs to register additional activities,
      // broadcast receivers, services or content providers, insert
      // them into the manifest here.
      if (!subelements.isEmpty()) {
        for (Map.Entry<String, Set<String>> componentSubElSetPair : subelements) {
          Set<String> subelementSet = componentSubElSetPair.getValue();
          for (String subelement : subelementSet) {
            if (isForCompanion && !includeDangerousPermissions &&
                subelement.contains("android.provider.Telephony.SMS_RECEIVED")) {
              continue;
            }
            out.write(
              subelement
                .replace("%packageName%", packageName) // replace %packageName% with the actual packageName
            );
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
        if (brNameAndActions.length == 0) {
          continue;
        }
        // Remove the SMS_RECEIVED broadcast receiver if we aren't including dangerous permissions
        if (isForCompanion && !includeDangerousPermissions) {
          boolean skip = false;
          for (String action : brNameAndActions) {
            if (action.equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")) {
              skip = true;
              break;
            }
          }
          if (skip) {
            continue;
          }
        }
        out.write(
            "<receiver android:name=\"" + brNameAndActions[0] + "\" android:exported=\"true\">\n");
        if (brNameAndActions.length > 1) {
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
      out.write("         android:name=\"androidx.core.content.FileProvider\"\n");
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
   * @param compBlocks component type mapped to blocks used in project
   * @param formOrientations form names mapped to screen orientations
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
      Map<String, Set<String>> compBlocks,
      Map<String, String> formOrientations,
      PrintStream out, PrintStream err, PrintStream userErrors,
      boolean isForCompanion, boolean isForEmulator, boolean includeDangerousPermissions,
      String keystoreFilePath, int childProcessRam, String dexCacheDir, String outputFileName,
      BuildServer.ProgressReporter reporter, boolean isAab, StatReporter statReporter)
      throws IOException, JSONException {
    // Create a new compiler instance for the compilation
    Compiler compiler = new Compiler(project, compTypes, formOrientations, compBlocks, out, err,
        userErrors, isForCompanion, isForEmulator, includeDangerousPermissions, childProcessRam,
        dexCacheDir, reporter);

    return compileWithStats(compiler, project, isAab, keystoreFilePath, outputFileName, out,
        reporter, statReporter);
  }

  private static boolean compileWithStats(Compiler compiler, Project project, boolean isAab,
      String keystoreFilePath, String outputFileName, PrintStream out,
      BuildServer.ProgressReporter reporter, StatReporter statReporter) {
    boolean success = false;
    long start = System.currentTimeMillis();
    statReporter.startBuild(compiler);

    try {
      // Set initial progress to 0%
      if (reporter != null) {
        reporter.report(0);
      }

      if (!compiler.loadJsonInfo()) {
        return false;
      }
      statReporter.nextStage(compiler, "generateActivities");
      compiler.generateActivities();
      statReporter.nextStage(compiler, "generateActivityMetadata");
      compiler.generateActivityMetadata();
      statReporter.nextStage(compiler, "generateAssets");
      compiler.generateAssets();
      statReporter.nextStage(compiler, "generateBroadcastReceivers");
      compiler.generateBroadcastReceivers();
      statReporter.nextStage(compiler, "generateContentProviders");
      compiler.generateContentProviders();
      statReporter.nextStage(compiler, "generateLibNames");
      compiler.generateLibNames();
      statReporter.nextStage(compiler, "generateMetadata");
      compiler.generateMetadata();
      statReporter.nextStage(compiler, "generateMinSdks");
      compiler.generateMinSdks();
      statReporter.nextStage(compiler, "generateNativeLibNames");
      compiler.generateNativeLibNames();
      statReporter.nextStage(compiler, "generatePermissions");
      compiler.generatePermissions();
      statReporter.nextStage(compiler, "generateQueries");
      compiler.generateQueries();
      statReporter.nextStage(compiler, "generateServices");
      compiler.generateServices();

      // TODO(Will): Remove the following call once the deprecated
      //             @SimpleBroadcastReceiver annotation is removed. It should
      //             should remain for the time being because otherwise we'll break
      //             extensions currently using @SimpleBroadcastReceiver.
      statReporter.nextStage(compiler, "generateBroadcastReceiver");
      compiler.generateBroadcastReceiver();

      // Create build directory.
      File buildDir = createDir(project.getBuildDirectory());

      // Prepare application icon.
      out.println("________Preparing application icon");
      File resDir = createDir(buildDir, "res");
      File drawableDir = createDir(resDir, "drawable");

      // Create mipmap directories
      File mipmapHdpi = createDir(resDir,"mipmap-hdpi");
      File mipmapMdpi = createDir(resDir,"mipmap-mdpi");
      File mipmapXhdpi = createDir(resDir,"mipmap-xhdpi");
      File mipmapXxhdpi = createDir(resDir,"mipmap-xxhdpi");
      File mipmapXxxhdpi = createDir(resDir,"mipmap-xxxhdpi");

      // Create list of mipmaps for all icon types with respective sizes
      List<File> mipmapDirectoriesForIcons = Arrays.asList(mipmapMdpi, mipmapHdpi, mipmapXhdpi,
          mipmapXxhdpi, mipmapXxxhdpi);
      List<Integer> standardSizesForMipmaps = Arrays.asList(48,72,96,144,192);
      List<Integer> foregroundSizesForMipmaps = Arrays.asList(108,162,216,324,432);

      statReporter.nextStage(compiler, "prepareApplicationIcon");
      if (!compiler.prepareApplicationIcon(new File(drawableDir, "ya.png"),
          mipmapDirectoriesForIcons, standardSizesForMipmaps, foregroundSizesForMipmaps)) {
        return false;
      }
      if (reporter != null) {
        reporter.report(15);        // Have to call directly because we are in a
      }                             // Static context

      statReporter.nextStage(compiler, "createAnimationXml");
      // Create anim directory and animation xml files
      out.println("________Creating animation xml");
      File animDir = createDir(resDir, "anim");
      if (!compiler.createAnimationXml(animDir)) {
        return false;
      }

      statReporter.nextStage(compiler, "createValuesXml");
      // Create values directory and style xml files
      out.println("________Creating style xml");
      File styleDir = createDir(resDir, "values");
      File style11Dir = createDir(resDir, "values-v11");
      File style14Dir = createDir(resDir, "values-v14");
      File style21Dir = createDir(resDir, "values-v21");
      File style23Dir = createDir(resDir, "values-v23");
      if (!compiler.createValuesXml(styleDir, "")
          || !compiler.createValuesXml(style11Dir, "-v11")
          || !compiler.createValuesXml(style14Dir, "-v14")
          || !compiler.createValuesXml(style21Dir, "-v21")
          || !compiler.createValuesXml(style23Dir, "-v23")) {
        return false;
      }

      statReporter.nextStage(compiler, "createProviderXml");
      out.println("________Creating provider_path xml");
      File providerDir = createDir(resDir, "xml");
      if (!compiler.createProviderXml(providerDir)) {
        return false;
      }

      statReporter.nextStage(compiler, "createNetworkConfigXml");
      out.println("________Creating network_security_config xml");
      if (!compiler.createNetworkConfigXml(providerDir)) {
        return false;
      }

      statReporter.nextStage(compiler, "writeICLauncher");
      // Generate ic_launcher.xml
      out.println("________Generating adaptive icon file");
      File mipmapV26 = createDir(resDir, "mipmap-anydpi-v26");
      File icLauncher = new File(mipmapV26, "ic_launcher.xml");
      if (!compiler.writeICLauncher(icLauncher, false)) {
        return false;
      }

      // Generate ic_launcher_round.xml
      out.println("________Generating round adaptive icon file");
      File icLauncherRound = new File(mipmapV26, "ic_launcher_round.xml");
      if (!compiler.writeICLauncher(icLauncherRound, true)) {
        return false;
      }

      // Generate ic_launcher_background.xml
      out.println("________Generating adaptive icon background file");
      File icBackgroundColor = new File(styleDir, "ic_launcher_background.xml");
      if (!compiler.writeICLauncherBackground(icBackgroundColor)) {
        return false;
      }

      statReporter.nextStage(compiler, "writeAndroidManifest");
      // Generate AndroidManifest.xml
      out.println("________Generating manifest file");
      File manifestFile = new File(buildDir, "AndroidManifest.xml");
      if (!compiler.writeAndroidManifest(manifestFile)) {
        return false;
      }
      if (reporter != null) {
        reporter.report(20);
      }

      statReporter.nextStage(compiler, "insertNativeLibs");
      // Insert native libraries
      out.println("________Attaching native libraries");
      if (!compiler.insertNativeLibs(buildDir)) {
        return false;
      }

      statReporter.nextStage(compiler, "attachAarLibraries");
      // Attach Android AAR Library dependencies
      out.println("________Attaching Android Archive (AAR) libraries");
      if (!compiler.attachAarLibraries(buildDir)) {
        return false;
      }

      statReporter.nextStage(compiler, "attachCompAssets");
      // Add raw assets to sub-directory of project assets.
      out.println("________Attaching component assets");
      if (!compiler.attachCompAssets()) {
        return false;
      }

      // Invoke aapt to package everything up
      out.println("________Invoking AAPT");
      File deployDir = createDir(buildDir, "deploy");
      String tmpPackageName = deployDir.getAbsolutePath() + SLASH
          + project.getProjectName() + "." + (isAab ? "apk" : "ap_");
      File srcJavaDir = createDir(buildDir, "generated/src");
      File rconstJavaDir = createDir(buildDir, "generated/symbols");
      if (isAab) {
        statReporter.nextStage(compiler, "aapt2");
        if (!compiler.runAapt2Compile(resDir)) {
          return false;
        }
        if (!compiler.runAapt2Link(manifestFile, tmpPackageName, rconstJavaDir)) {
          return false;
        }
      } else {
        statReporter.nextStage(compiler, "aapt");
        if (!compiler.runAaptPackage(manifestFile, resDir, tmpPackageName, srcJavaDir,
            rconstJavaDir)) {
          return false;
        }
      }
      if (reporter != null) {
        reporter.report(30);
      }

      statReporter.nextStage(compiler, "generateClasses");
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

      statReporter.nextStage(compiler, "runMultidex");
      // Invoke dx on class files
      out.println("________Invoking DX");
      File tmpDir = createDir(buildDir, "tmp");
      String dexedClassesDir = tmpDir.getAbsolutePath();
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
      if (!compiler.runMultidex(classesDir, dexedClassesDir)) {
        return false;
      }
      if (reporter != null) {
        reporter.report(85);
      }

      if (isAab) {
        statReporter.nextStage(compiler, "bundletool");
        if (!compiler.bundleTool(buildDir, tmpPackageName, outputFileName, deployDir,
            keystoreFilePath, dexedClassesDir)) {
          return false;
        }
      } else {
        statReporter.nextStage(compiler, "runApkBuilder");
        // Seal the apk with ApkBuilder
        out.println("________Invoking ApkBuilder");
        String fileName = outputFileName;
        if (fileName == null) {
          fileName = project.getProjectName() + ".apk";
        }
        String apkAbsolutePath = deployDir.getAbsolutePath() + SLASH + fileName;
        if (!compiler.runApkBuilder(apkAbsolutePath, tmpPackageName, dexedClassesDir)) {
          return false;
        }
        if (reporter != null) {
          reporter.report(95);
        }

        // ZipAlign the apk file
        out.println("________ZipAligning the apk file");
        if (!compiler.runZipAlign(apkAbsolutePath, tmpDir)) {
          return false;
        }

        // Sign the apk file
        out.println("________Signing the apk file");
        if (!compiler.runApkSigner(apkAbsolutePath, keystoreFilePath)) {
          return false;
        }
      }

      if (reporter != null) {
        reporter.report(100);
      }

      out.println("Build finished in "
          + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");

      success = true;
    } finally {
      statReporter.stopBuild(compiler, success);
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
      if (dexFiles.size() > 1) {
        for (File f : dexFiles) {
          if (!f.getName().equals("classes.dex")) {
            apkBuilder.addFile(f, f.getName());
          }
        }
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
   * @param compBlocks component types mapped to blocks used in project
   * @param out  stdout stream for compiler messages
   * @param err  stderr stream for compiler messages
   * @param userErrors stream to write user-visible error messages
   * @param childProcessMaxRam  maximum RAM for child processes, in MBs.
   */
  @VisibleForTesting
  Compiler(Project project, Set<String> compTypes, Map<String, String> formOrientations,
      Map<String, Set<String>> compBlocks, PrintStream out, PrintStream err,
      PrintStream userErrors, boolean isForCompanion, boolean isForEmulator,
      boolean includeDangerousPermissions, int childProcessMaxRam, String dexCacheDir,
      BuildServer.ProgressReporter reporter) {
    this.project = project;
    this.compBlocks = compBlocks;
    this.formOrientations = formOrientations;

    prepareCompTypes(compTypes);
    readBuildInfo();

    this.out = out;
    this.err = err;
    this.userErrors = userErrors;
    this.isForCompanion = isForCompanion;
    this.isForEmulator = isForEmulator;
    this.includeDangerousPermissions = includeDangerousPermissions;
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
    // zipalign -f 4 infile.zip outfile.zip
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

  private boolean runApkSigner(String apkAbsolutePath, String keystoreAbsolutePath) {
    int mx = childProcessRamMb - 200;
    /*
      apksigner sign\
      --ks <keystore file>\
      --ks-key-alias AndroidKey\
      --ks-pass pass:android\
      <APK>
    */
    String[] apksignerCommandLine = {
      System.getProperty("java.home") + "/bin/java", "-jar",
      "-mx" + mx + "M",
      getResource(APKSIGNER_JAR), "sign",
      "-ks", keystoreAbsolutePath,
      "-ks-key-alias", "AndroidKey",
      "-ks-pass", "pass:android",
      apkAbsolutePath
    };

    long startApkSigner = System.currentTimeMillis();
    if (!Execution.execute(null, apksignerCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - apksigner execution failed.");
      err.println("YAIL compiler - apksigner execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "APKSIGNER"));
      return false;
    }
    String apkSignerTimeMessage = "APKSIGNER time: " + ((System.currentTimeMillis() - startApkSigner) / 1000.0) + " seconds";
    out.println(apkSignerTimeMessage);
    LOG.info(apkSignerTimeMessage);
    return true;
  }

  /*
   * Returns a resized image given a new width and height
   */
  private BufferedImage resizeImage(BufferedImage icon, int height, int width) {
    Image tmp = icon.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    BufferedImage finalResized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = finalResized.createGraphics();
    g2.drawImage(tmp, 0, 0, null);
    return finalResized;
  }

  /*
   * Creates the circle image of an icon
   */
  private BufferedImage produceRoundIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // Ratio of icon size to png image size for round icon is 0.80
    double iconWidth = imageWidth * 0.80;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int)Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    BufferedImage roundIcon = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = roundIcon.createGraphics();
    g2.setClip(new Ellipse2D.Float(marginWidth, marginWidth, intIconWidth, intIconWidth));
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return roundIcon;
  }

  /*
   * Creates the image of an icon with rounded corners
   */
  private BufferedImage produceRoundedCornerIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // Ratio of icon size to png image size for roundRect icon is 0.93
    double iconWidth = imageWidth * 0.93;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int)Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    // Corner radius of roundedCornerIcon needs to be 1/12 of width according to Android material guidelines
    float cornerRadius = intIconWidth / 12;
    BufferedImage roundedCornerIcon = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = roundedCornerIcon.createGraphics();
    g2.setClip(new RoundRectangle2D.Float(marginWidth, marginWidth, intIconWidth, intIconWidth, cornerRadius, cornerRadius));
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return roundedCornerIcon;
  }

  /*
   * Creates the foreground image of an icon
   */
  private BufferedImage produceForegroundImageIcon(BufferedImage icon) {
    int imageWidth = icon.getWidth();
    // According to the adaptive icon documentation, both layers are 108x108dp but only the inner
    // 72x72dp appears in the masked viewport, so we shrink down the size of the image accordingly.
    double iconWidth = imageWidth * 72.0 / 108.0;
    // Round iconWidth value to even int for a centered png
    int intIconWidth = ((int)Math.round(iconWidth / 2) * 2);
    Image tmp = icon.getScaledInstance(intIconWidth, intIconWidth, Image.SCALE_SMOOTH);
    int marginWidth = ((imageWidth - intIconWidth) / 2);
    BufferedImage foregroundImageIcon = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = foregroundImageIcon.createGraphics();
    g2.drawImage(tmp, marginWidth, marginWidth, null);
    return foregroundImageIcon;
  }

  /*
   * Loads the icon for the application, either a user provided one or the default one.
   */
  private boolean prepareApplicationIcon(File outputPngFile, List<File> mipmapDirectories, List<Integer> standardICSizes, List<Integer> foregroundICSizes) {
    String userSpecifiedIcon = Strings.nullToEmpty(project.getIcon());
    try {
      BufferedImage icon;
      if (!userSpecifiedIcon.isEmpty()) {
        File iconFile = new File(project.getAssetsDirectory(), userSpecifiedIcon);
        icon = ImageIO.read(iconFile);
        if (icon == null) {
          // This can happen if the iconFile isn't an image file.
          // For example, icon is null if the file is a .wav file.
          // TODO(lizlooney) - This happens if the user specifies a .ico file. We should
          // fix that.
          userErrors.print(String.format(ICON_ERROR, userSpecifiedIcon));
          return false;
        }
      } else {
        // Load the default image.
        icon = ImageIO.read(Compiler.class.getResource(DEFAULT_ICON));
      }

      BufferedImage roundIcon = produceRoundIcon(icon);
      BufferedImage roundRectIcon = produceRoundedCornerIcon(icon);
      BufferedImage foregroundIcon = produceForegroundImageIcon(icon);

      // For each mipmap directory, create all types of ic_launcher photos with respective mipmap sizes
      for(int i=0; i < mipmapDirectories.size(); i++){
        File mipmapDirectory = mipmapDirectories.get(i);
        Integer standardSize = standardICSizes.get(i);
        Integer foregroundSize = foregroundICSizes.get(i);

        BufferedImage round = resizeImage(roundIcon,standardSize,standardSize);
        BufferedImage roundRect = resizeImage(roundRectIcon,standardSize,standardSize);
        BufferedImage foreground = resizeImage(foregroundIcon,foregroundSize,foregroundSize);

        File roundIconPng = new File(mipmapDirectory,"ic_launcher_round.png");
        File roundRectIconPng = new File(mipmapDirectory,"ic_launcher.png");
        File foregroundPng = new File(mipmapDirectory,"ic_launcher_foreground.png");

        ImageIO.write(round, "png", roundIconPng);
        ImageIO.write(roundRect, "png", roundRectIconPng);
        ImageIO.write(foreground, "png", foregroundPng);
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

  /**
   * Processes recursively the directory pointed at by {@code dir} and adds any class files
   * encountered to the {@code classes} set.
   *
   * @param dir the directory to examine for class files
   * @param classes the Set used to record the classes
   * @param root the root path where the recursion started, which gets stripped from the file name
   *             to determine the class name
   */
  private void recordDirectoryForMainDex(File dir, Set<String> classes, String root) {
    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }
    for (File f : files) {
      if (f.isDirectory()) {
        recordDirectoryForMainDex(f, classes, root);
      } else if (f.getName().endsWith(".class")) {
        String className = f.getAbsolutePath().replace(root, "");
        className = className.substring(0, className.length() - 6);
        classes.add(className.replaceAll("/", "."));
      }
    }
  }

  /**
   * Processes the JAR file pointed at by {@code file} and adds the contained class names to
   * {@code classes}.
   *
   * @param file a File object pointing to a JAR file
   * @param classes the Set used to record the classes
   * @throws IOException if the input file cannot be read
   */
  private void recordJarForMainDex(File file, Set<String> classes) throws IOException {
    try (ZipInputStream is = new ZipInputStream(new FileInputStream(file))) {
      ZipEntry entry;
      while ((entry = is.getNextEntry()) != null) {
        String className = entry.getName();
        if (className.endsWith(".class")) {
          className = className.substring(0, className.length() - 6);
          classes.add(className.replaceAll("/", "."));
        }
      }
    }
  }

  /**
   * Examines the given file and records its classes for the main dex class list.
   *
   * @param file a File object pointing to a JAR file or a directory containing class files
   * @param classes the Set used to record the classes
   * @return the input file
   * @throws IOException if the input file cannot be read
   */
  private File recordForMainDex(File file, Set<String> classes) throws IOException {
    if (file.isDirectory()) {
      recordDirectoryForMainDex(file, classes, file.getAbsolutePath() + File.separator);
    } else if (file.getName().endsWith(".jar")) {
      recordJarForMainDex(file, classes);
    }
    return file;
  }

  /**
   * Writes out the class list for the main dex file. The format of this file is the pathname of
   * the class, including the .class extension, one per line.
   *
   * @param classesDir directory to place the main classes list
   * @param classes the set of classes to include in the main dex file
   * @return the path to the file containing the main classes list
   */
  private String writeClassList(File classesDir, Set<String> classes) {
    File target = new File(classesDir, "main-classes.txt");
    try (PrintStream out = new PrintStream(new FileOutputStream(target))) {
      for (String name : new TreeSet<>(classes)) {
        out.println(name.replaceAll("\\.", "/") + ".class");
      }
      return target.getAbsolutePath();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Compiles Java class files and JAR files into the Dex file format using dx.
   *
   * @param classesDir directory containing compiled App Inventor screens
   * @param dexedClassesDir output directory for classes.dex
   * @return true if successful or false if an error occurred
   */
  private boolean runMultidex(File classesDir, String dexedClassesDir) {
    Set<String> mainDexClasses = new HashSet<>();
    List<File> inputList = new ArrayList<>();
    boolean success;
    try {
      // Set up classes for main dex file
      inputList.add(recordForMainDex(classesDir, mainDexClasses));
      inputList.add(recordForMainDex(new File(getResource(SIMPLE_ANDROID_RUNTIME_JAR)),
          mainDexClasses));
      inputList.add(recordForMainDex(new File(getResource(KAWA_RUNTIME)), mainDexClasses));
      for (String jar : CRITICAL_JARS) {
        inputList.add(recordForMainDex(new File(getResource(jar)), mainDexClasses));
      }

      // Only include ACRA for the companion app
      if (isForCompanion) {
        inputList.add(recordForMainDex(new File(getResource(ACRA_RUNTIME)), mainDexClasses));
      }

      for (String jar : SUPPORT_JARS) {
        if (CRITICAL_JARS.contains(jar)) {  // already covered above
          continue;
        }
        inputList.add(new File(getResource(jar)));
      }

      // Add the rest of the libraries in any order
      for (String lib : uniqueLibsNeeded) {
        inputList.add(new File(lib));
      }

      // Add extension libraries
      Set<String> addedExtJars = new HashSet<>();
      for (String type : extCompTypes) {
        String sourcePath = getExtCompDirPath(type) + SIMPLE_ANDROID_RUNTIME_JAR;
        if (!addedExtJars.contains(sourcePath)) {
          inputList.add(new File(sourcePath));
          addedExtJars.add(sourcePath);
        }
      }

      // Run the dx utility
      DexExecTask dexTask = new DexExecTask();
      dexTask.setExecutable(getResource(DX_JAR));
      dexTask.setMainDexClassesFile(writeClassList(classesDir, mainDexClasses));
      dexTask.setOutput(dexedClassesDir);
      dexTask.setChildProcessRamMb(childProcessRamMb);
      if (dexCacheDir == null) {
        dexTask.setDisableDexMerger(true);
      } else {
        createDir(new File(dexCacheDir));
        dexTask.setDexedLibs(dexCacheDir);
      }
      String dxTimeMessage;
      synchronized (SYNC_KAWA_OR_DX) {
        setProgress(50);
        long startDx = System.currentTimeMillis();
        success = dexTask.execute(inputList);
        dxTimeMessage = String.format(Locale.getDefault(), "DX time: %f seconds",
            (System.currentTimeMillis() - startDx) / 1000.0);
        setProgress(75);
      }

      // Aggregate all of the classes.dex files output by dx
      File[] files = new File(dexedClassesDir).listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".dex");
        }
      });
      if (files == null) {
        throw new FileNotFoundException("Could not find classes.dex");
      }
      Collections.addAll(dexFiles, files);

      // Log status
      out.println(dxTimeMessage);
      LOG.info(dxTimeMessage);
    } catch (IOException e) {
      // Error will be reported below
      success = false;
    }
    if (!success) {
      LOG.warning("YAIL compiler - DX execution failed.");
      err.println("YAIL compiler - DX execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "DX"));
    }
    return success;
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
      appRJava = new File(sourceOutputDir, packageName.replaceAll("\\.", SLASHREGEX) + SLASH + "R.java");
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

  private boolean runAapt2Compile(File resDir) {
    resourcesZip = new File(resDir, "resources.zip");
    String aaptTool;
    String aapt2Tool;
    String osName = System.getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      aaptTool = MAC_AAPT_TOOL;
      aapt2Tool = MAC_AAPT2_TOOL;
    } else if (osName.equals("Linux")) {
      aaptTool = LINUX_AAPT_TOOL;
      aapt2Tool = LINUX_AAPT2_TOOL;
    } else if (osName.startsWith("Windows")) {
      aaptTool = WINDOWS_AAPT_TOOL;
      aapt2Tool = WINDOWS_AAPT2_TOOL;
    } else {
      LOG.warning("YAIL compiler - cannot run AAPT2 on OS " + osName);
      err.println("YAIL compiler - cannot run AAPT2 on OS " + osName);
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT2"));
      return false;
    }

    if (!mergeResources(resDir, project.getBuildDirectory(), aaptTool)) {
      LOG.warning("Unable to merge resources");
      err.println("Unable to merge resources");
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT"));
      return false;
    }
    
    libSetup();                 // Setup /tmp/lib64 on Linux

    List<String> aapt2CommandLine = new ArrayList<>();
    aapt2CommandLine.add(getResource(aapt2Tool));
    aapt2CommandLine.add("compile");
    aapt2CommandLine.add("--dir");
    aapt2CommandLine.add(mergedResDir.getAbsolutePath());
    aapt2CommandLine.add("-o");
    aapt2CommandLine.add(resourcesZip.getAbsolutePath());
    aapt2CommandLine.add("--no-crunch");
    aapt2CommandLine.add("-v");
    String[] aapt2CompileCommandLine = aapt2CommandLine.toArray(new String[0]);

    long startAapt2 = System.currentTimeMillis();
    if (!Execution.execute(null, aapt2CompileCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - AAPT2 compile execution failed.");
      err.println("YAIL compiler - AAPT2 compile execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT2 compile"));
      return false;
    }

    String aaptTimeMessage = "AAPT2 compile time: " + ((System.currentTimeMillis() - startAapt2) / 1000.0) + " seconds";
    out.println(aaptTimeMessage);
    LOG.info(aaptTimeMessage);
    return true;
  }

  private boolean runAapt2Link(File manifestFile, String tmpPackageName, File symbolOutputDir) {
    String aapt2Tool;
    String osName = System.getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      aapt2Tool = MAC_AAPT2_TOOL;
    } else if (osName.equals("Linux")) {
      aapt2Tool = LINUX_AAPT2_TOOL;
    } else if (osName.startsWith("Windows")) {
      aapt2Tool = WINDOWS_AAPT2_TOOL;
    } else {
      LOG.warning("YAIL compiler - cannot run AAPT2 on OS " + osName);
      err.println("YAIL compiler - cannot run AAPT2 on OS " + osName);
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT2"));
      return false;
    }
    appRTxt = new File(symbolOutputDir, "R.txt");

    List<String> aapt2CommandLine = new ArrayList<>();
    aapt2CommandLine.add(getResource(aapt2Tool));
    aapt2CommandLine.add("link");
    aapt2CommandLine.add("--proto-format");
    aapt2CommandLine.add("-o");
    aapt2CommandLine.add(tmpPackageName);
    aapt2CommandLine.add("-I");
    aapt2CommandLine.add(getResource(ANDROID_RUNTIME));
    aapt2CommandLine.add("-R");
    aapt2CommandLine.add(resourcesZip.getAbsolutePath());
    aapt2CommandLine.add("-A");
    aapt2CommandLine.add(createDir(project.getBuildDirectory(), ASSET_DIR_NAME).getAbsolutePath());
    aapt2CommandLine.add("--manifest");
    aapt2CommandLine.add(manifestFile.getAbsolutePath());
    aapt2CommandLine.add("--output-text-symbols");
    aapt2CommandLine.add(appRTxt.getAbsolutePath());
    aapt2CommandLine.add("--auto-add-overlay");
    aapt2CommandLine.add("--no-version-vectors");
    aapt2CommandLine.add("--no-auto-version");
    aapt2CommandLine.add("--no-version-transitions");
    aapt2CommandLine.add("--no-resource-deduping");
    aapt2CommandLine.add("-v");
    String[] aapt2LinkCommandLine = aapt2CommandLine.toArray(new String[0]);

    long startAapt2 = System.currentTimeMillis();
    if (!Execution.execute(null, aapt2LinkCommandLine, System.out, System.err)) {
      LOG.warning("YAIL compiler - AAPT2 link execution failed.");
      err.println("YAIL compiler - AAPT2 link execution failed.");
      userErrors.print(String.format(ERROR_IN_STAGE, "AAPT2 link"));
      return false;
    }

    String aaptTimeMessage = "AAPT2 link time: " + ((System.currentTimeMillis() - startAapt2) / 1000.0) + " seconds";
    out.println(aaptTimeMessage);
    LOG.info(aaptTimeMessage);
    return true;
  }

  private boolean bundleTool(File buildDir, String tmpPackageName,
                             String outputFileName, File deployDir, String keystoreFilePath, String dexedClassesDir) {
    try {
      String jarsignerTool = "jarsigner";
      String fileName = outputFileName;
      if (fileName == null) {
        fileName = project.getProjectName() + ".aab";
      }

      AabCompiler aabCompiler = new AabCompiler(out, buildDir, childProcessRamMb - 200)
            .setLibsDir(libsDir)
            .setProtoApk(new File(tmpPackageName))
            .setJarsigner(jarsignerTool)
            .setBundletool(getResource(BUNDLETOOL_JAR))
            .setDeploy(deployDir.getAbsolutePath() + SLASH + fileName)
            .setKeystore(keystoreFilePath)
            .setDexDir(dexedClassesDir);

      Future<Boolean> aab = Executors.newSingleThreadExecutor().submit(aabCompiler);
      return aab.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return false;
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
          boolean isV7a = lib.endsWith(ComponentDescriptorConstants.ARMEABI_V7A_SUFFIX);
          boolean isV8a = lib.endsWith(ComponentDescriptorConstants.ARM64_V8A_SUFFIX);
          boolean isx8664 = lib.endsWith(ComponentDescriptorConstants.X86_64_SUFFIX);

          String sourceDirName;
          File targetDir;
          if (isV7a) {
            sourceDirName = ARMEABI_V7A_DIR_NAME;
            targetDir = armeabiV7aDir;
            lib = lib.substring(0, lib.length() - ComponentDescriptorConstants.ARMEABI_V7A_SUFFIX.length());
          } else if (isV8a) {
            sourceDirName = ARM64_V8A_DIR_NAME;
            targetDir = arm64V8aDir;
            lib = lib.substring(0, lib.length() - ComponentDescriptorConstants.ARM64_V8A_SUFFIX.length());
          } else if (isx8664) {
            sourceDirName = X86_64_DIR_NAME;
            targetDir = x8664Dir;
            lib = lib.substring(0, lib.length() - ComponentDescriptorConstants.X86_64_SUFFIX.length());
          } else {
            sourceDirName = ARMEABI_DIR_NAME;
            targetDir = armeabiDir;
          }

          String sourcePath = "";
          String pathSuffix = RUNTIME_FILES_DIR + sourceDirName + ZIPSLASH + lib;

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

    // Attach the Android support libraries (needed by every app)
    libsNeeded.put("ANDROID", new HashSet<>(Arrays.asList(SUPPORT_AARS)));

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
    } catch (NullPointerException e) {
      throw new IllegalStateException("Unable to find required library: " + resourcePath, e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void ensureLib(String tempdir, String name, String resource) {
    try {
      File outFile = new File(tempdir, name);
      if (outFile.exists()) {
        return;
      }
      File tmpLibDir = new File(tempdir);
      tmpLibDir.mkdirs();
      Files.copy(Resources.newInputStreamSupplier(Compiler.class.getResource(resource)), outFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * This code extracts platform specific dynamic libraries needed by the build tools. These
   * libraries cannot be extracted using the usual mechanism as that assigns a random suffix,
   * causing dynamic linking to fail.
   */
  private void libSetup() {
    String osName = System.getProperty("os.name");
    if (osName.equals("Linux")) {
      ensureLib("/tmp/lib64", "libc++.so", RUNTIME_TOOLS_DIR + "linux/lib64/libc++.so");
    } else if (osName.startsWith("Windows")) {
      ensureLib(System.getProperty("java.io.tmpdir"), "libwinpthread-1.dll", WINDOWS_PTHEAD_DLL);
    }
  }

  private boolean loadJsonInfo() {
    try {
      buildInfo = new JSONArray(
          "[" + simpleCompsBuildInfo.join(",") + ","
              + extCompsBuildInfo.join(",") + "]"
      );
      return true;
    } catch (JSONException e) {
      e.printStackTrace();
      userErrors.printf(ERROR_IN_STAGE, "Loading Component Info");
      return false;
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

      if (buildInfo == null) {
        loadJsonInfo();
      }

      for (int i = 0; i < buildInfo.length(); ++i) {
        JSONObject compJson = buildInfo.getJSONObject(i);
        JSONArray infoArray = null;
        String type = compJson.getString("type");
        infoArray = compJson.optJSONArray(targetInfo);
        if (infoArray == null) {
          LOG.log(Level.INFO, "Component \"" + type + "\" does not specify " + targetInfo);
          // Continue to process other components
          continue;
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

        processConditionalInfo(compJson, type, targetInfo);
      }
    }
  }

  private void loadPermissionConstraints() throws JSONException {
    if (!permissionConstraintsNeeded.isEmpty()) {
      // Nothing to do here.
      return;
    }

    for (int i = 0; i < buildInfo.length(); i++) {
      JSONObject compJson = buildInfo.getJSONObject(i);
      String type = compJson.getString("type");
      if (!simpleCompTypes.contains(type) && !extCompTypes.contains(type)) {
        // Component type not used.
        continue;
      }

      JSONObject infoObject = compJson.optJSONObject(
          ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET);
      if (infoObject == null) {
        LOG.log(Level.INFO, "Component \"" + type + "\" does not specify "
            + ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET);
        continue;
      }

      // Handle declared constraints
      permissionConstraintsNeeded.put(type, processPermissionConstraints(infoObject));

      // Handle conditional constraints
      infoObject = compJson.optJSONObject(ComponentDescriptorConstants.CONDITIONALS_TARGET);
      if (infoObject == null) {
        continue;
      }
      infoObject = infoObject.optJSONObject(
          ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET);
      if (infoObject == null) {
        continue;
      }
      Map<String, Map<String, Set<PermissionConstraint<?>>>> blockConstraints = new HashMap<>();
      Iterator<?> it = infoObject.keys();
      while (it.hasNext()) {
        String blockName = (String) it.next();
        JSONObject constraints = infoObject.getJSONObject(blockName);
        blockConstraints.put(blockName, processPermissionConstraints(constraints));
      }
      conditionalPermissionConstraints.put(type, blockConstraints);
    }
  }

  private Map<String, Set<PermissionConstraint<?>>> processPermissionConstraints(JSONObject src)
      throws JSONException {
    Map<String, Set<PermissionConstraint<?>>> neededConstraints = new HashMap<>();
    Iterator<?> it = src.keys();
    while (it.hasNext()) {
      String permissionName = (String) it.next();
      Set<PermissionConstraint<?>> constraintSet = neededConstraints.get(permissionName);
      if (constraintSet == null) {
        constraintSet = new HashSet<>();
        neededConstraints.put(permissionName, constraintSet);
      }
      JSONObject constraints = src.getJSONObject(permissionName);
      Iterator<?> it2 = constraints.keys();
      while (it2.hasNext()) {
        String attribute = (String) it2.next();
        Object value = constraints.get(attribute);
        if (value instanceof Number) {
          constraintSet.add(new PermissionConstraint<>(permissionName, attribute,
              ((Number) value).intValue()));
        } else {
          constraintSet.add(new PermissionConstraint<>(permissionName, attribute,
              value.toString()));
        }
      }
    }
    return neededConstraints;
  }

  /**
   * Processes the conditional info from simple_components_build_info.json into
   * a structure mapping annotation types to component names to block names to
   * values.
   *
   * @param compJson Parsed component data from JSON
   * @param type The name of the type being processed
   * @param targetInfo Name of the annotation target being processed (e.g.,
   *                   permissions). Any of: PERMISSIONS_TARGET,
   *                   BROADCAST_RECEIVERS_TARGET, SERVICES_TARGET,
   *                   CONTENT_PROVIDERS_TARGET
   */
  private void processConditionalInfo(JSONObject compJson, String type, String targetInfo) {
    // Strip off the package name since SCM and BKY use unqualified names
    type = type.substring(type.lastIndexOf('.') + 1);

    JSONObject conditionals = compJson.optJSONObject(ComponentDescriptorConstants.CONDITIONALS_TARGET);
    if (conditionals != null) {
      JSONObject jsonBlockMap = conditionals.optJSONObject(targetInfo);
      if (jsonBlockMap != null) {
        if (!this.conditionals.containsKey(targetInfo)) {
          this.conditionals.put(targetInfo, new HashMap<String, Map<String, Set<String>>>());
        }
        Map<String, Set<String>> blockMap = new HashMap<>();
        this.conditionals.get(targetInfo).put(type, blockMap);
        for (String key : (List<String>) Lists.newArrayList(jsonBlockMap.keys())) {
          JSONArray data = jsonBlockMap.optJSONArray(key);
          HashSet<String> result = new HashSet<>();
          for (int i = 0; i < data.length(); i++) {
            result.add(data.optString(i));
          }
          blockMap.put(key, result);
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

  private boolean usesLegacyFileAccess() {
    return "Legacy".equals(project.getDefaultFileScope());
  }

  private static String basename(String path) {
    return new File(path).getName();
  }

  private static String getExtAssetPath(String extCompDir, String assetName) {
    return extCompDir + File.separator + ASSET_DIR_NAME + File.separator + assetName;
  }
}
