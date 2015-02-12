// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.components.annotations.DesignerProperty;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Tool to generate simple component descriptors as JSON.
 * 
 * The output is a sequence of component descriptions enclosed in square 
 * brackets and separated by commas. Each component description has the 
 * following format:
 * { "name": "COMPONENT-TYPE-NAME",
 *   "version": "VERSION",
 *   "categoryString": "PALETTE-CATEGORY",
 *   "helpString": “DESCRIPTION”,
 *   "showOnPalette": "true"|"false",
 *   "nonVisible": "true"|"false",
 *   "iconName": "ICON-FILE-NAME",
 *   "properties": [
 *     { "name": "PROPERTY-NAME", 
 *        "editorType": "EDITOR-TYPE", 
 *        "defaultValue": "DEFAULT-VALUE"},*
 *    ],
 *   "blockProperties": [
 *     { "name": "PROPERTY-NAME", 
 *        "description": "DESCRIPTION", 
 *        "type": "YAIL-TYPE",
 *        "rw": "read-only"|"read-write"|"write-only"|"invisible"},*
 *   ],
 *   "events": [
 *     { "name": "EVENT-NAME", 
 *       "description": "DESCRIPTION", 
 *       "params": [
 *         { "name": "PARAM-NAME", 
 *           "type": "YAIL-TYPE"},*
 *       ]},+
 *   ],
 *   “methods”: [
 *     { "name": "METHOD-NAME", 
 *       "description": "DESCRIPTION", 
 *       "params": [
 *         { "name": "PARAM-NAME", 
 *       "type": "YAIL-TYPE"},*
 *     ]},+
 *   ]
 * }
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author sharon@google.com (Sharon Perl) - added events, methods, non-designer
 *   properties (for use by browser-based blocks editor)
 */
public final class ComponentDescriptorGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "simple_components.json";

  private void outputComponent(ComponentInfo component, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(component.name);
    sb.append("\",\n  \"version\": \"");
    sb.append(component.getVersion());
    sb.append("\",\n  \"categoryString\": \"");
    sb.append(component.getCategoryString());
    sb.append("\",\n  \"helpString\": ");
    sb.append(formatDescription(component.getHelpDescription()));
    sb.append(",\n  \"showOnPalette\": \"");
    sb.append(component.getShowOnPalette());
    sb.append("\",\n  \"nonVisible\": \"");
    sb.append(component.getNonVisible());
    sb.append("\",\n  \"iconName\": \"");
    sb.append(component.getIconName());
    sb.append("\",\n  \"properties\": [");
    String separator = "";
    for (Map.Entry<String, DesignerProperty> entry : component.designerProperties.entrySet()) {
      String propertyName = entry.getKey();
      DesignerProperty dp = entry.getValue();
      sb.append(separator);
      outputProperty(propertyName, dp, sb);
      separator = ",\n";
    }
    // We need additional information about properties in the blocks editor,
    // and we need all of them, not just the Designer properties. We output
    // the entire set separately for use by the blocks editor to keep things simple.
    sb.append("],\n  \"blockProperties\": [");
    separator = "";
    for (Property prop : component.properties.values()) {
      sb.append(separator);
      // Output properties that are not user-visible, but mark them as invisible
      // Note: carrying this over from the old Java blocks editor. I'm not sure
      // that we'll actually do anything with invisible properties in the blocks
      // editor. (sharon@google.com)
      outputBlockProperty(prop.name, prop, sb);
      separator = ",\n    ";
    }
    sb.append("],\n  \"events\": [");
    separator = "";
    for (Event event : component.events.values()) {
      sb.append(separator);
      outputBlockEvent(event.name, event, sb, !event.userVisible);
      separator = ",\n    ";
    }
    sb.append("],\n  \"methods\": [");
    separator = "";
    for (Method method : component.methods.values()) {
      sb.append(separator);
      outputBlockMethod(method.name, method, sb, !method.userVisible);
      separator = ",\n    ";
    }
    sb.append("]}\n");
  }

  private void outputProperty(String propertyName, DesignerProperty dp, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(propertyName);
    sb.append("\", \"editorType\": \"");
    sb.append(dp.editorType());
    sb.append("\", \"defaultValue\": \"");
    sb.append(dp.defaultValue().replace("\"", "\\\""));
    sb.append("\"}");
  }

  private void outputBlockProperty(String propertyName, Property prop, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(propertyName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(prop.getDescription()));
    sb.append(", \"type\": \"");
    sb.append(javaTypeToYailType(prop.getType()));
    sb.append("\", \"rw\": \"");
    sb.append(prop.isUserVisible() ? prop.getRwString() : "invisible");
    sb.append("\"}");
  }
  
  private void outputBlockEvent(String eventName, Event event, StringBuilder sb,
                                boolean deprecated) {
    sb.append("{ \"name\": \"");
    sb.append(eventName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(event.description));
    sb.append(", \"deprecated\": \"" + deprecated + "\"");
    sb.append(", \"params\": ");
    outputParameters(event.parameters, sb);
    sb.append("}\n");
  }
  
  private void outputBlockMethod(String methodName, Method method, StringBuilder sb,
                                 boolean deprecated) {
    sb.append("{ \"name\": \"");
    sb.append(methodName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(method.description));
    sb.append(", \"deprecated\": \"" + deprecated + "\"");
    sb.append(", \"params\": ");
    outputParameters(method.parameters, sb);
    if (method.getReturnType() != null) {
      sb.append(", \"returnType\": \"");
      sb.append(javaTypeToYailType(method.getReturnType()));
      sb.append("\"}");
    } else {
      sb.append("}");
    }
  }
  
  /*
   *  Output a parameter list (including surrounding [])
   */
  private void outputParameters(List<Parameter> params, StringBuilder sb) {
    sb.append("[");
    String separator = "";
    for (Parameter p : params) {
      sb.append(separator);
      sb.append("{ \"name\": \"");
      sb.append(p.name);
      sb.append("\", \"type\": \"");
      sb.append(javaTypeToYailType(p.type));
      sb.append("\"}");
      separator = ",";
    }
    sb.append("]");
  }
  
  @Override
  protected void outputResults() throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append('[');
    String separator = "";

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      sb.append(separator);
      outputComponent(component, sb);
      separator = ",\n";
    }

    sb.append(']');

    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(sb.toString());
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }
  
  /*
   * Format a description string as a json string. Note that the returned value
   * include surrounding double quotes.
   */
  private static String formatDescription(String description) {
    return StringUtils.toJson(description);
  }
}
