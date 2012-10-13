// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Tool to generate a list of the simple component types, and the permissions
 * required for each component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ComponentListGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String COMPONENT_LIST_OUTPUT_FILE_NAME = "simple_components.txt";
  private static final String COMPONENT_PERMISIONS_OUTPUT_FILE_NAME =
      "simple_components_permissions.json";
  private static final String COMPONENT_LIBRARIES_OUTPUT_FILE_NAME =
    "simple_components_libraries.json";

  @Override
  protected void outputResults() throws IOException {
    // Build the component list and the permissions simulataneously.
    StringBuilder componentList = new StringBuilder();
    StringBuilder componentPermissions = new StringBuilder();
    componentPermissions.append("[\n");
    StringBuilder componentLibraries = new StringBuilder();
    componentLibraries.append("[\n");
   

    // Components are already sorted.
    String listSeparator = "";
    String jsonSeparator = "";
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();

      componentList.append(listSeparator).append(component.name);
      listSeparator = "\n";

      componentPermissions.append(jsonSeparator);
      outputComponentPermissions(component, componentPermissions);
      
      componentLibraries.append(jsonSeparator);
      outputComponentLibraries(component, componentLibraries);
      
      jsonSeparator = ",\n";
     
    }

    componentPermissions.append("\n]");
    componentLibraries.append("\n]");

    FileObject src = createOutputFileObject(COMPONENT_LIST_OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    try {
      writer.write(componentList.toString());
      writer.flush();
    } finally {
      writer.close();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());

    src = createOutputFileObject(COMPONENT_PERMISIONS_OUTPUT_FILE_NAME);
    writer = src.openWriter();
    try {
      writer.write(componentPermissions.toString());
      writer.flush();
    } finally {
      writer.close();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
    
    src = createOutputFileObject(COMPONENT_LIBRARIES_OUTPUT_FILE_NAME);
    writer = src.openWriter();
    try {
      writer.write(componentLibraries.toString());
      writer.flush();
    } finally {
      writer.close();
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
    
  }

  private static void outputComponentPermissions(ComponentInfo component, StringBuilder sb) {
    sb.append("{\"name\": \"");
    sb.append(component.name);
    sb.append("\", \"permissions\": [");
    String separator = "";
    for (String permission : component.permissions) {
      sb.append(separator).append("\"").append(permission).append("\"");
      separator = ", ";
    }
    sb.append("]}");
  }
  
  private static void outputComponentLibraries(ComponentInfo component, StringBuilder sb) {
    sb.append("{\"name\": \"");
    sb.append(component.name);
    sb.append("\", \"libraries\": [");
    String separator = "";
    for (String library : component.libraries) {
      sb.append(separator).append("\"").append(library).append("\"");
      separator = ", ";
    }
    sb.append("]}");
  }
}
