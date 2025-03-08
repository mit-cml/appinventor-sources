// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.common;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.interfaces.CommonTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import com.google.appinventor.buildserver.util.PermissionConstraint;
import com.google.appinventor.components.common.ComponentDescriptorConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;



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
public class LoadComponentInfo implements CommonTask {
  CompilerContext<?> context = null;
  private ConcurrentMap<String, Map<String, Map<String, Set<String>>>> conditionals;
  /**
   * Maps types to blocks to permissions to permission constraints.
   */
  private final ConcurrentMap<String, Map<String, Map<String, Set<PermissionConstraint<?>>>>>
      conditionalPermissionConstraints = new ConcurrentHashMap<>();

  @Override
  public TaskResult execute(CompilerContext<?> context) {
    this.context = context;
    this.conditionals = new ConcurrentHashMap<>();

    if (!loadJsonInfo()) {
      return TaskResult.generateError("Unable to load component information");
    }

    if (!this.generateAssets()
        || !this.generateActivities()
        || !this.generateActivityMetadata()
        || !this.generateBroadcastReceivers()
        || !this.generateContentProviders()
        || !this.generateLibNames()
        || !this.generateMetadata()
        || !this.generateMinSdks()
        || !this.generateNativeLibNames()
        || !this.generatePermissions()
        || !this.generateQueries()
        || !this.generateServices()
        || !this.generateXmls()) {
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
      loadJsonInfo(context.getComponentInfo().getAssetsNeeded(),
          ComponentDescriptorConstants.ASSETS_TARGET);
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
      loadJsonInfo(context.getComponentInfo().getActivitiesNeeded(),
          ComponentDescriptorConstants.ACTIVITIES_TARGET);
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

  /**
   * Generate a set of conditionally included metadata needed by this project.
   */
  private boolean generateMetadata() {
    try {
      loadJsonInfo(context.getComponentInfo().getMetadataNeeded(),
          ComponentDescriptorConstants.METADATA_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Metadata stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getMetadataNeeded().keySet()) {
      n += context.getComponentInfo().getMetadataNeeded().get(type).size();
    }

    context.getReporter().log("Component metadata needed, n = " + n);
    return true;
  }

  /**
   * Generate a set of conditionally included activity metadata needed by this project.
   */
  private boolean generateActivityMetadata() {
    try {
      loadJsonInfo(context.getComponentInfo().getActivityMetadataNeeded(),
          ComponentDescriptorConstants.ACTIVITY_METADATA_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Activity Metadata stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getActivityMetadataNeeded().keySet()) {
      n += context.getComponentInfo().getActivityMetadataNeeded().get(type).size();
    }

    context.getReporter().log("Component activity metadata needed, n = " + n);
    return true;
  }

  /*
   * Generate a set of conditionally included broadcast receivers needed by this project.
   */
  private boolean generateBroadcastReceivers() {
    context.getReporter().info("Generating broadcast receivers...");
    try {
      loadJsonInfo(context.getComponentInfo().getBroadcastReceiversNeeded(),
          ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the BroadcastReceivers stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET),
        context.getComponentInfo().getBroadcastReceiversNeeded());

    // TODO: Output the number of broadcast receivers

    return true;
  }

  /**
   * Generate a set of conditionally included queries needed by this project.
   */
  private boolean generateQueries() {
    try {
      loadJsonInfo(context.getComponentInfo().getQueriesNeeded(),
          ComponentDescriptorConstants.QUERIES_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Queries stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.QUERIES_TARGET),
        context.getComponentInfo().getQueriesNeeded());

    return true;
  }

  /**
   * Generate a set of conditionally included activity metadata needed by this project.
   */
  private boolean generateServices() {
    try {
      loadJsonInfo(context.getComponentInfo().getServicesNeeded(),
          ComponentDescriptorConstants.SERVICES_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Services stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.SERVICES_TARGET),
            context.getComponentInfo().getServicesNeeded());

    return true;
  }

  /**
   * Generate a set of conditionally included activity metadata needed by this project.
   */
  private boolean generateContentProviders() {
    try {
      loadJsonInfo(context.getComponentInfo().getContentProvidersNeeded(),
          ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Content Providers stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET),
            context.getComponentInfo().getContentProvidersNeeded());

    return true;
  }

  /**
   * Generate a set of conditionally included xml files needed by this project.
   */
  private boolean generateXmls() {
    try {
      loadJsonInfo(context.getComponentInfo().getXmlsNeeded(),
          ComponentDescriptorConstants.XMLS_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Xmls stage", true);
      return false;
    }

    int n = 0;
    for (String type : context.getComponentInfo().getXmlsNeeded().keySet()) {
      n += context.getComponentInfo().getXmlsNeeded().get(type).size();
    }

    context.getReporter().log("Component xmls needed, n = " + n);
    return true;
  }

  /*
   * Generate the set of Android libraries needed by this project.
   */
  private boolean generateLibNames() {
    context.getReporter().info("Generating libraries...");
    try {
      loadJsonInfo(context.getComponentInfo().getLibsNeeded(),
          ComponentDescriptorConstants.LIBRARIES_TARGET);
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
      loadJsonInfo(context.getComponentInfo().getNativeLibsNeeded(),
          ComponentDescriptorConstants.NATIVE_TARGET);
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
      loadJsonInfo(context.getComponentInfo().getPermissionsNeeded(),
          ComponentDescriptorConstants.PERMISSIONS_TARGET);
      loadPermissionConstraints();
      if (context.getProject() != null) {
        // Only do this if we have a project (testing doesn't provide one :-( ).
        context.getReporter().log("usesLocation = " + context.getProject().getUsesLocation());
        if (context.getProject().getUsesLocation().equals("True")) {
          // Add location permissions if any WebViewer requests it
          Set<String> locationPermissions = Sets.newHashSet(); // via a Property.
          // See ProjectEditor.recordLocationSettings()
          locationPermissions.add("android.permission.ACCESS_FINE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
          locationPermissions.add("android.permission.ACCESS_MOCK_LOCATION");
          context.getComponentInfo().getPermissionsNeeded()
              .put("com.google.appinventor.components.runtime.WebViewer", locationPermissions);
        }
      }
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the Permissions stage", true);
      return false;
    }

    mergeConditionals(conditionals.get(ComponentDescriptorConstants.PERMISSIONS_TARGET),
        context.getComponentInfo().getPermissionsNeeded());
    mergeConditionalPermissionConstraints();

    if (!context.getBlockPermissions().isEmpty()) {
      context.getComponentInfo().getPermissionsNeeded().put("<blocks>",
          context.getBlockPermissions());
    }

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
      loadJsonInfo(context.getComponentInfo().getMinSdksNeeded(),
          ComponentDescriptorConstants.ANDROIDMINSDK_TARGET);
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
      loadJsonInfo(context.getComponentInfo().getComponentBroadcastReceiver(),
          ComponentDescriptorConstants.BROADCAST_RECEIVER_TARGET);
    } catch (IOException | JSONException e) {
      // This is fatal.
      context.getReporter().error("There was an error in the BroadcastReceiver Generator stage",
          true);
      return false;
    }
    return true;
  }

  private boolean loadJsonInfo() {
    try {
      context.setBuildInfo(new JSONArray(
          "[" + context.getSimpleCompsBuildInfo().join(",") + ","
              + context.getExtCompsBuildInfo().join(",") + "]"));
      return true;
    } catch (JSONException e) {
      e.printStackTrace();
      context.getReporter().error("There was an error loading component info", true);
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

      if (context.getBuildInfo() == null) {
        loadJsonInfo();
      }

      final JSONArray buildInfo = context.getBuildInfo();

      for (int i = 0; i < buildInfo.length(); ++i) {
        JSONObject compJson = buildInfo.getJSONObject(i);
        JSONArray infoArray = null;
        String type = compJson.getString("type");
        infoArray = compJson.optJSONArray(targetInfo);
        if (infoArray == null) {
          context.getReporter().info("Component \"" + type + "\" does not specify " + targetInfo);
          // Continue to process other components
          continue;
        }

        if (!context.getSimpleCompTypes().contains(type)
            && !context.getExtCompTypes().contains(type)) {
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

    JSONObject conditionals = compJson.optJSONObject(
        ComponentDescriptorConstants.CONDITIONALS_TARGET);
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

  private void loadPermissionConstraints() throws JSONException {
    if (!context.getComponentInfo().getPermissionConstraintsNeeded().isEmpty()) {
      // Nothing to do here.
      return;
    }

    final JSONArray buildInfo = context.getBuildInfo();
    final Set<String> simpleCompTypes = context.getSimpleCompTypes();
    final Set<String> extCompTypes = context.getExtCompTypes();

    for (int i = 0; i < buildInfo.length(); i++) {
      JSONObject compJson = buildInfo.getJSONObject(i);
      String type = compJson.getString("type");
      if (!simpleCompTypes.contains(type) && !extCompTypes.contains(type)) {
        // Component type not used.
        continue;
      }

      JSONObject infoObject = compJson.optJSONObject(
          ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET);
      if (infoObject != null) {
        // Handle declared constraints
        context.getComponentInfo().getPermissionConstraintsNeeded()
            .put(type, processPermissionConstraints(infoObject));
      }

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

  private void mergeConditionals(Map<String, Map<String, Set<String>>> conditionalMap,
      Map<String, Set<String>> infoMap) {
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

  private void mergeConditionalPermissionConstraints() {
    if (context.isForCompanion()) {
      return;  // We don't want to place additional constraints on the companion
    }

    final Map<String, Set<String>> compBlocks = context.getCompBlocks();
    final Map<String, Map<String, Set<PermissionConstraint<?>>>> permissionConstraintsNeeded =
        context.getComponentInfo().getPermissionConstraintsNeeded();

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
}
