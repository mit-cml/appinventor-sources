// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.components.common.ComponentDescriptorConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

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
    // Build the component list and build info simultaneously.
    StringBuilder componentList = new StringBuilder();
    StringBuilder componentBuildInfo = new StringBuilder();
    componentBuildInfo.append("[\n");

    // Components are already sorted.
    String listSeparator = "";
    String jsonSeparator = "";
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();

      componentList.append(listSeparator).append(component.type);
      listSeparator = "\n";

      componentBuildInfo.append(jsonSeparator);
      outputComponentBuildInfo(component, componentBuildInfo);
      jsonSeparator = ",\n";
    }

    componentBuildInfo.append("\n]");

    FileObject src = createOutputFileObject(COMPONENT_LIST_OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    try {
      writer.write(componentList.toString());
      writer.flush();
    } finally {
      writer.close();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());

    src = createOutputFileObject(COMPONENT_BUILD_INFO_OUTPUT_FILE_NAME);
    writer = src.openWriter();
    try {
      writer.write(componentBuildInfo.toString());
      writer.flush();
    } finally {
      writer.close();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }

  private static void outputComponentBuildInfo(ComponentInfo component, StringBuilder sb) {
    sb.append("{\"type\": \"");
    sb.append(component.type).append("\"");
    appendComponentInfo(sb, ComponentDescriptorConstants.PERMISSIONS_TARGET, component.permissions);
    appendComponentInfo(sb, ComponentDescriptorConstants.LIBRARIES_TARGET, component.libraries);
    appendComponentInfo(sb, ComponentDescriptorConstants.NATIVE_TARGET, component.nativeLibraries);
    appendComponentInfo(sb, ComponentDescriptorConstants.ASSETS_TARGET, component.assets);
    appendComponentInfo(sb, ComponentDescriptorConstants.ACTIVITIES_TARGET, component.activities);
    appendComponentInfo(sb, ComponentDescriptorConstants.METADATA_TARGET, component.metadata);
    appendComponentInfo(sb, ComponentDescriptorConstants.ACTIVITY_METADATA_TARGET, component.activityMetadata);
    appendComponentInfo(sb, ComponentDescriptorConstants.ANDROIDMINSDK_TARGET, Collections.singleton(Integer.toString(component.getAndroidMinSdk())));
    appendComponentInfo(sb, ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET, component.broadcastReceivers);
    appendComponentInfo(sb, ComponentDescriptorConstants.QUERIES_TARGET, component.queries);
    appendComponentInfo(sb, ComponentDescriptorConstants.SERVICES_TARGET, component.services);
    appendComponentInfo(sb, ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET, component.contentProviders);
    appendConditionalComponentInfo(component, sb);
    // TODO(Will): Remove the following call once the deprecated
    //             @SimpleBroadcastReceiver annotation is removed. It should
    //             should remain for the time being because otherwise we'll break
    //             extensions currently using @SimpleBroadcastReceiver.
    appendComponentInfo(sb, ComponentDescriptorConstants.BROADCAST_RECEIVER_TARGET, component.classNameAndActionsBR);
    sb.append("}");
  }

  /**
   * Adds information about conditional annotations (e.g., permissions) to the
   * component information dictionary.
   *
   * @param component The component info to process
   * @param sb Target StringBuilder to receive the conditional description
   */
  private static void appendConditionalComponentInfo(ComponentInfo component, StringBuilder sb) {
    if (component.conditionalBroadcastReceivers.size()
        + component.conditionalContentProviders.size()
        + component.conditionalPermissions.size()
        + component.conditionalQueries.size()
        + component.conditionalServices.size() == 0) {
      return;
    }
    sb.append(", \"" + ComponentDescriptorConstants.CONDITIONALS_TARGET + "\": { ");
    sb.append("\"" + ComponentDescriptorConstants.PERMISSIONS_TARGET + "\": ");
    appendMap(sb, component.conditionalPermissions);
    sb.append(", \"" + ComponentDescriptorConstants.BROADCAST_RECEIVERS_TARGET + "\": ");
    appendMap(sb, component.conditionalBroadcastReceivers);
    sb.append(", \"" + ComponentDescriptorConstants.QUERIES_TARGET + "\": ");
    appendMap(sb, component.conditionalQueries);
    sb.append(", \"" + ComponentDescriptorConstants.SERVICES_TARGET + "\": ");
    appendMap(sb, component.conditionalServices);
    sb.append(", \"" + ComponentDescriptorConstants.CONTENT_PROVIDERS_TARGET + "\": ");
    appendMap(sb, component.conditionalContentProviders);
    sb.append("}");
  }

  /**
   * Outputs a map to a StringBuilder.
   *
   * @param sb Target StringBuilder to receive the mapping
   * @param map Mapping of string to array of strings that should be output to
   *            the StringBuilder
   */
  private static void appendMap(StringBuilder sb, Map<String, String[]> map) {
    sb.append("{");
    boolean first = true;
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      if (!first) sb.append(", ");
      sb.append("\"");
      sb.append(entry.getKey());
      sb.append("\": [\"");
      StringUtils.join(sb, "\", \"", entry.getValue());
      sb.append("\"]");
      first = false;
    }
    sb.append("}");
  }

  private static void appendComponentInfo(StringBuilder sb,
      String infoName, Set<String> infoEntries) {
    sb.append(", \"").append(infoName).append("\": [");
    String separator = "";
    for (String infoEntry : infoEntries) {
      sb.append(separator).append("\"").append(infoEntry).append("\"");
      separator = ", ";
    }
    sb.append("]");
  }
}
