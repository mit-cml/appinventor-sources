package com.google.appinventor.buildserver.tasks;

import com.google.appinventor.buildserver.*;
import com.google.appinventor.components.common.ComponentDescriptorConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * compiler.generateAssets();
 * compiler.generateActivities();
 * compiler.generateBroadcastReceivers();
 * compiler.generateLibNames();
 * compiler.generateNativeLibNames();
 * compiler.generatePermissions();
 * compiler.generateMinSdks();
 * compiler.generateBroadcastReceiver();
 */
@BuildType(apk = true, aab = true)
public class LoadComponentInfo implements Task {
  CompilerContext context = null;
  private ConcurrentMap<String, Map<String, Map<String, Set<String>>>> conditionals;

  @Override
  public TaskResult execute(CompilerContext context) {
    this.context = context;
    this.conditionals = new ConcurrentHashMap<>();

    if (!this.generateAssets() || !this.generateActivities() || !this.generateBroadcastReceivers() ||
        !this.generateLibNames() || !this.generateNativeLibNames() || !this.generatePermissions() ||
        !this.generateMinSdks()) {
      return TaskResult.generateError("Could not extract info from the app");
    }

    // TODO(Will): Remove the following call once the deprecated
    //             @SimpleBroadcastReceiver annotation is removed. It should
    //             should remain for the time being because otherwise we'll break
    //             extensions currently using @SimpleBroadcastReceiver.
    if (!this.generateBroadcastReceiver()) {
      return TaskResult.generateError("Could not generate the broadcast receiver");
    }

    return TaskResult.generateSuccess();
  }

  /*
   * Generate the set of conditionally included assets needed by this project.
   */
  private boolean generateAssets() {
    context.getReporter().info("Generating assets...");
    try {
      loadJsonInfo(context.getComponentInfo().getAssetsNeeded(), ComponentDescriptorConstants.ASSETS_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Assets stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getAssetsNeeded().keySet()) {
      n += context.getComponentInfo().getAssetsNeeded().get(type).size();
    }

    context.getReporter().log("Component assets needed, n = " + n);
    return true;
  }

  /*
   * Generate the set of conditionally included activities needed by this project.
   */
  private boolean generateActivities() {
    context.getReporter().info("Generating activities...");
    try {
      loadJsonInfo(context.getComponentInfo().getActivitiesNeeded(), ComponentDescriptorConstants.ACTIVITIES_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Activities stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getActivitiesNeeded().keySet()) {
      n += context.getComponentInfo().getActivitiesNeeded().get(type).size();
    }

    context.getReporter().log("Component activities needed, n = " + n);
    return true;
  }

  /*
   * Generate a set of conditionally included broadcast receivers needed by this project.
   */
  private boolean generateBroadcastReceivers() {
    context.getReporter().info("Generating broadcast receivers...");
    try {
      loadJsonInfo(context.getComponentInfo().getBroadcastReceiversNeeded(), ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the BroadcastReceivers stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET), context.getComponentInfo().getBroadcastReceiversNeeded());

    // TODO: Output the number of broadcast receivers

    return true;
  }

  /*
   * Generate the set of Android libraries needed by this project.
   */
  private boolean generateLibNames() {
    context.getReporter().info("Generating libraries...");
    try {
      loadJsonInfo(context.getComponentInfo().getLibsNeeded(), ComponentDescriptorConstants.LIBRARIES_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Libraries stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getLibsNeeded().keySet()) {
      n += context.getComponentInfo().getLibsNeeded().get(type).size();
    }

    context.getReporter().log("Libraries needed, n = " + n);
    return true;
  }

  /*
   * Generate the set of conditionally included libraries needed by this project.
   */
  private boolean generateNativeLibNames() {
    context.getReporter().info("Generating native libraries...");
    if (context.isForEmulator()) {  // no libraries for emulator, so we return success
      return true;
    }
    try {
      loadJsonInfo(context.getComponentInfo().getNativeLibsNeeded(), ComponentDescriptorConstants.NATIVE_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Native Libraries stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getNativeLibsNeeded().keySet()) {
      n += context.getComponentInfo().getNativeLibsNeeded().get(type).size();
    }

    context.getReporter().log("Native Libraries needed, n = " + n);
    return true;
  }

  private boolean generatePermissions() {
    context.getReporter().info("Generating permissions...");
    try {
      loadJsonInfo(context.getComponentInfo().getPermissionsNeeded(), ComponentDescriptorConstants.PERMISSIONS_TARGET);
      if (context.getProject() != null) {    // Only do this if we have a project (testing doesn't provide one :-( ).
        context.getReporter().log("usesLocation = " + context.getProject().getUsesLocation());
        if (context.getProject().getUsesLocation().equals("True")) { // Add location permissions if any WebViewer requests it
          Set<String> locationPermissions = Sets.newHashSet(); // via a Property.
          // See ProjectEditor.recordLocationSettings()
          locationPermissions.add("android.permission.ACCESS_FINE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_MOCK_LOCATION");
          context.getComponentInfo().getPermissionsNeeded().put("com.google.appinventor.components.runtime.WebViewer", locationPermissions);
        }
      }
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Permissions stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.PERMISSIONS_TARGET), context.getComponentInfo().getPermissionsNeeded());

    int n = 0;
    for (String type : context.getComponentInfo().getPermissionsNeeded().keySet()) {
      n += context.getComponentInfo().getPermissionsNeeded().get(type).size();
    }

    context.getReporter().log("Permissions needed, n = " + n);
    return true;
  }

  private boolean generateMinSdks() {
    context.getReporter().info("Generating Android minimum SDK...");
    try {
      loadJsonInfo(context.getComponentInfo().getMinSdksNeeded(), ComponentDescriptorConstants.ANDROIDMINSDK_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Android Min SDK stage", true);
      return false;
    }

    return true;
  }

  /*
   * TODO(Will): Remove this method once the deprecated @SimpleBroadcastReceiver
   *             annotation is removed. This should remain for the time being so
   *             that we don't break extensions currently using the
   *             @SimpleBroadcastReceiver annotation.
   */
  private boolean generateBroadcastReceiver() {
    context.getReporter().info("Generating component broadcast receivers...");
    try {
      loadJsonInfo(context.getComponentInfo().getComponentBroadcastReceiver(), ComponentDescriptorConstants.BROADCAST_RECEIVER_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the BroadcastReceiver Generator stage", true);
      return false;
    }
    return true;
  }

  private void loadJsonInfo(ConcurrentMap<String, Set<String>> infoMap, String targetInfo) throws IOException, JSONException {
    synchronized (infoMap) {
      if (!infoMap.isEmpty()) {
        return;
      }

      JSONArray buildInfo = new JSONArray(
          "[" + context.getSimpleCompsBuildInfo().join(",") + "," +
              context.getExtCompsBuildInfo().join(",") + "]");

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
            context.getReporter().warn("Component \"" + type + "\" does not have a broadcast receiver.");
            continue;
          } else if (e.getMessage().contains(ComponentDescriptorConstants.ANDROIDMINSDK_TARGET)) {
            context.getReporter().warn("Component \"" + type + "\" does not specify a minimum SDK.");
            continue;
          } else {
            throw e;
          }
        }

        if (!context.getSimpleCompTypes().contains(type) && !context.getExtCompTypes().contains(type)) {
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

  /**
   * Processes the conditional info from simple_components_build_info.json into
   * a structure mapping annotation types to component names to block names to
   * values.
   *
   * @param compJson   Parsed component data from JSON
   * @param type       The name of the type being processed
   * @param targetInfo Name of the annotation target being processed (e.g.,
   *                   permissions). Any of: PERMISSIONS_TARGET,
   *                   BROADCAST_RECEIVERS_TARGET
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

  private void mergeConditionals(Map<String, Map<String, Set<String>>> conditionalMap, Map<String, Set<String>> infoMap) {
    if (conditionalMap != null) {
      if (context.isForCompanion()) {
        // For the companion, we take all of the conditionals
        for (Map.Entry<String, Map<String, Set<String>>> entry : conditionalMap.entrySet()) {
          for (Set<String> items : entry.getValue().values()) {
            ExecutorUtils.setOrMerge(infoMap, entry.getKey(), items);
          }
        }
        // If necessary, we can remove permissions at this point (e.g., Texting, PhoneCall)
      } else {
        // We walk the set of components and the blocks used in the project. If
        // any <component, block> combination is in the set of conditionals,
        // then we merge the associated set of values into the existing set. If
        // no existing set exists, we create one.
        for (Map.Entry<String, Set<String>> entry : context.getCompBlocks().entrySet()) {
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
}
