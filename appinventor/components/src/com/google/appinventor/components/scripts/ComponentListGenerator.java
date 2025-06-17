// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.PermissionConstraint;

import com.google.appinventor.components.common.ComponentDescriptorConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tool to generate a list of the simple component types, permissions, libraries, activities,
 * Broadcast Receivers, Services and Content Providers  (build info) required for each component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ComponentListGenerator extends ComponentProcessor {
  // Where to write results.  Build Info is the collection of permissions, asset and library info.
  private static final String COMPONENT_LIST_OUTPUT_FILE_NAME = "simple_components.txt";
  private static final String COMPONENT_BUILD_INFO_OUTPUT_FILE_NAME =
      "simple_components_build_info.json";

  @Override
  protected void outputResults() throws IOException {
    JSONArray componentBuildInfo = new JSONArray();
    // Build the component list and build info simultaneously.
    StringBuilder componentList = new StringBuilder();

    // Components are already sorted.
    String listSeparator = "";
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();

      componentList.append(listSeparator).append(component.type);
      listSeparator = "\n";

      componentBuildInfo.put(outputComponentBuildInfo(component));
    }

    FileObject src = createOutputFileObject(COMPONENT_LIST_OUTPUT_FILE_NAME);
    try (Writer writer = src.openWriter()) {
      writer.write(componentList.toString());
      writer.flush();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());

    src = createOutputFileObject(COMPONENT_BUILD_INFO_OUTPUT_FILE_NAME);
    try (Writer writer = src.openWriter()) {
      writer.write(componentBuildInfo.toString());
      writer.flush();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }

  private static JSONObject outputComponentBuildInfo(ComponentInfo component) {
    JSONObject json = new JSONObject();
    json.put("type", component.type);
    appendComponentInfo(json, ComponentDescriptorConstants.PERMISSIONS_TARGET,
        component.permissions);
    appendPermissionConstraints(json, ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET,
        component.permissionConstraints);
    appendComponentInfo(json, ComponentDescriptorConstants.LIBRARIES_TARGET, component.libraries);
    appendComponentInfo(json, ComponentDescriptorConstants.NATIVE_TARGET,
        component.nativeLibraries);
    appendComponentInfo(json, ComponentDescriptorConstants.ASSETS_TARGET, component.assets);
    appendComponentInfo(json, ComponentDescriptorConstants.ACTIVITIES_TARGET, component.activities);
    appendComponentInfo(json, ComponentDescriptorConstants.METADATA_TARGET, component.metadata);
    appendComponentInfo(json, ComponentDescriptorConstants.ACTIVITY_METADATA_TARGET,
        component.activityMetadata);
    appendComponentInfo(json, ComponentDescriptorConstants.ANDROIDMINSDK_TARGET,
        Collections.singleton(Integer.toString(component.getAndroidMinSdk())));
    appendComponentInfo(json, ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET,
        component.broadcastReceivers);
    appendComponentInfo(json, ComponentDescriptorConstants.QUERIES_TARGET, component.queries);
    appendComponentInfo(json, ComponentDescriptorConstants.SERVICES_TARGET, component.services);
    appendComponentInfo(json, ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET,
        component.contentProviders);
    appendComponentInfo(json, ComponentDescriptorConstants.XMLS_TARGET, component.xmls);
    appendConditionalComponentInfo(component, json);
    // TODO(Will): Remove the following call once the deprecated
    //             @SimpleBroadcastReceiver annotation is removed. It should
    //             should remain for the time being because otherwise we'll break
    //             extensions currently using @SimpleBroadcastReceiver.
    appendComponentInfo(json, ComponentDescriptorConstants.BROADCAST_RECEIVER_TARGET,
        component.classNameAndActionsBR);
    return json;
  }

  /**
   * Adds information about conditional annotations (e.g., permissions) to the
   * component information dictionary.
   *
   * @param component The component info to process
   * @param parent Target JSONObject to receive the conditional description
   */
  private static void appendConditionalComponentInfo(ComponentInfo component, JSONObject parent) {
    if (component.conditionalBroadcastReceivers.size()
        + component.conditionalContentProviders.size()
        + component.conditionalPermissions.size()
        + component.conditionalQueries.size()
        + component.conditionalServices.size() == 0) {
      return;
    }
    JSONObject json = new JSONObject();
    appendMap(json, ComponentDescriptorConstants.PERMISSIONS_TARGET,
        component.conditionalPermissions);
    appendMultimap(json, component.conditionalPermissionConstraints);
    appendMap(json, ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET,
        component.conditionalBroadcastReceivers);
    appendMap(json, ComponentDescriptorConstants.QUERIES_TARGET,
        component.conditionalQueries);
    appendMap(json, ComponentDescriptorConstants.SERVICES_TARGET,
        component.conditionalServices);
    appendMap(json, ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET,
        component.conditionalContentProviders);
    parent.put(ComponentDescriptorConstants.CONDITIONALS_TARGET, json);
  }

  /**
   * Outputs a map to a StringBuilder.
   *
   * @param parent Target JSONObject to receive the mapping
   * @param key Key of {@code parent} which will hold the result
   * @param map Mapping of string to array of strings that should be output to
   *            the StringBuilder
   */
  private static void appendMap(JSONObject parent, String key, Map<String, String[]> map) {
    JSONObject json = new JSONObject();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      json.put(entry.getKey(), new JSONArray(entry.getValue()));
    }
    parent.put(key, json);
  }

  private static void appendMultimap(JSONObject parent,
      Map<String, Map<String, PermissionConstraint>> map) {
    JSONObject json = new JSONObject();
    for (Map.Entry<String, Map<String, PermissionConstraint>> entry : map.entrySet()) {
      appendPermissionConstraints(json, entry.getKey(), entry.getValue());
    }
    parent.put(ComponentDescriptorConstants.PERMISSION_CONSTRAINTS_TARGET, json);
  }

  private static void appendComponentInfo(JSONObject parent,
      String infoName, Set<String> infoEntries) {
    parent.put(infoName, new JSONArray(infoEntries));
  }

  /**
   * Inserts the set of permission constraints into the {@code parent} object at the given key
   * {@code infoName}. If a particular field is not set, it will not be output. If no entries
   * are provided, then {2code parent} will not be modified. Produces the following structure:
   * <code><pre>
   *   {
   *     infoName: {
   *       "permissionName": {
   *         "maxSdkVersion": number,
   *         "usesPermissionFlags": string
   *       }, ...
   *     }
   *   }
   * </pre></code>
   *
   * @param parent the JSON object receiving the encoded permission constraints
   * @param infoName the insertion point in the data structure for the constraints
   * @param entries the permission name-constraint mappings to encode
   */
  private static void appendPermissionConstraints(JSONObject parent, String infoName,
      Map<String, PermissionConstraint> entries) {
    if (entries == null || entries.isEmpty()) {
      return;
    }
    JSONObject json = new JSONObject();
    for (Map.Entry<String, PermissionConstraint> entry : entries.entrySet()) {
      JSONObject child = new JSONObject();
      PermissionConstraint constraint = entry.getValue();
      if (constraint.maxSdkVersion() > 0) {
        child.put("maxSdkVersion", constraint.maxSdkVersion());
      }
      if (!constraint.usesPermissionFlags().isEmpty()) {
        child.put("usesPermissionFlags", constraint.usesPermissionFlags());
      }
      json.put(entry.getKey(), child);
    }
    parent.put(infoName, json);
  }
}
