// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Tool to generate a list of the simple component types, and the permissions and libraries
 * (build info) required for each component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ComponentListGenerator extends ComponentProcessor {
  // Names of component information types to be output. Must match buildserver.compiler constants.
  private static final String PERMISSIONS_TARGET = "permissions";
  private static final String LIBRARIES_TARGET = "libraries";
  private static final String ASSETS_TARGET = "assets";
  private static final String NATIVE_TARGET = "native";
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

      componentList.append(listSeparator).append(component.name);
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
    sb.append("{\"name\": \"");
    sb.append(component.name + "\"");
    appendComponentInfo(sb, PERMISSIONS_TARGET, component.permissions);
    appendComponentInfo(sb, LIBRARIES_TARGET, component.libraries);
    appendComponentInfo(sb, NATIVE_TARGET, component.nativeLibraries);
    appendComponentInfo(sb, ASSETS_TARGET, component.assets);
    sb.append("}");
  }

  private static void appendComponentInfo(StringBuilder sb,
      String infoName, Set<String> infoEntries) {
    sb.append(", \"" + infoName + "\": [");
    String separator = "";
    for (String infoEntry : infoEntries) {
      sb.append(separator).append("\"").append(infoEntry).append("\"");
      separator = ", ";
    }
    sb.append("]");
  }
}
