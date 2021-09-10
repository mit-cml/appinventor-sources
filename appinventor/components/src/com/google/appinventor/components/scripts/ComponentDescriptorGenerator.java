// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.components.annotations.DesignerProperty;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Tool to generate simple component descriptors as JSON.
 *
 * The output is a sequence of component descriptions enclosed in square
 * brackets and separated by commas. Each component description has the
 * following format:
 * { "type": "COMPONENT-TYPE",
 *   "name": "COMPONENT-TYPE-NAME",
 *   "external": "true"|"false",
 *   "version": "VERSION",
 *   "categoryString": "PALETTE-CATEGORY",
 *   "helpString": “DESCRIPTION”,
 *   "showOnPalette": "true"|"false",
 *   "nonVisible": "true"|"false",
 *   "iconName": "ICON-FILE-NAME",
 *   "licenseName": "LICENSE-FILE-NAME",
 *   "androidMinSdk": "ANDROID-MIN-SDK",
 *   "conditionals": {
 *     "permissions": {
 *       "eventOrMethodName": [ "PERMISSION-NAME",+ ],+
 *     },
 *     "broadcastReceivers": {
 *       "eventOrMethodName": [ "BROADCAST-RECEIVER",+ ],+
 *     }
 *   }*,
 *   "properties": [
 *     { "name": "PROPERTY-NAME",
 *        "editorType": "EDITOR-TYPE",
 *        "defaultValue": "DEFAULT-VALUE"},*
 *    ],
 *   "blockProperties": [
 *     { "name": "PROPERTY-NAME",
 *        "description": "DESCRIPTION",
 *        "type": "YAIL-TYPE",
 *        "helper": {
 *          "type": HELPER-TYPE,
 *          "data": { ARBITRARY-DATA } 
 *        },
 *        "rw": "read-only"|"read-write"|"write-only"|"invisible"},*
 *   ],
 *   "events": [
 *     { "name": "EVENT-NAME",
 *       "description": "DESCRIPTION",
 *       "params": [
 *         { 
 *           "name": "PARAM-NAME",
 *           "type": "YAIL-TYPE"
 *           "helper": {
 *             "type": HELPER-TYPE,
 *             "data": { ARBITRARY-DATA } 
 *           }
 *         },*
 *       ]},+
 *   ],
 *   “methods”: [
 *     { "name": "METHOD-NAME",
 *       "description": "DESCRIPTION",
 *       "returnType": "YAIL-TYPE",
 *       "helper": {
 *         "type": HELPER-TYPE,
 *         "data": { ARBITRARY-DATA } 
 *       },
 *       "params": [
 *         {
 *           "name": "PARAM-NAME",
 *           "type": "YAIL-TYPE"
 *           "helper": {
 *             "type": HELPER-TYPE,
 *             "data": { ARBITRARY-DATA } 
 *           }
 *         },*
 *     ]},+
 *   ],
 *   ("assets": ["FILENAME",*])?
 * }
 * 
 * <p>A note on helper "ARBITRARY-DATA". The structure given above outlines a system where helper
 * data is duplicated every time that helper is used by a feature of a Component. Ideally this would
 * not be necessary and helper data could be stored in some kind of dictionary structure. The issue
 * is that this must export an array of objects to be compatible with extension .aia files which
 * have already been released, and adding more dictionaries or arrays to this structure would
 * require it to export an /object/ not an /array/. As such the simplest solution is to simply
 * duplicate data related to the helpers.
 * 
 * <p>It may make sense in the future to revisit this choice, but making this decision now would
 * only make it harder to support a more "lean" concept of data in the future, as we would still
 * have to deal with .aia files that use this duplicated format. So it is probably best to continue
 * duplicating data where necessary in the future.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author sharon@google.com (Sharon Perl) - added events, methods, non-designer
 *   properties (for use by browser-based blocks editor)
 */
public final class ComponentDescriptorGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "simple_components.json";

  private void outputComponent(ComponentInfo component, StringBuilder sb) {
    sb.append("{ \"type\": \"");
    sb.append(component.type);
    sb.append("\",\n  \"name\": \"");
    sb.append(component.name);
    sb.append("\",\n  \"external\": \"");
    sb.append(Boolean.toString(component.external));
    sb.append("\",\n  \"version\": \"");
    sb.append(component.getVersion());
    if (component.getVersionName() != null && !component.getVersionName().equals("")) {
      sb.append("\",\n  \"versionName\": \"");
      sb.append(component.getVersionName());
    }
    sb.append("\",\n  \"dateBuilt\": \"");
    sb.append(component.getDateBuilt());
    sb.append("\",\n  \"categoryString\": \"");
    sb.append(component.getCategoryString());
    sb.append("\",\n  \"helpString\": ");
    sb.append(formatDescription(component.getHelpDescription()));
    sb.append(",\n  \"helpUrl\": ");
    sb.append(formatDescription(component.getHelpUrl()));
    sb.append(",\n  \"showOnPalette\": \"");
    sb.append(component.getShowOnPalette());
    sb.append("\",\n  \"nonVisible\": \"");
    sb.append(component.getNonVisible());
    sb.append("\",\n  \"iconName\": \"");
    sb.append(component.getIconName());
    sb.append("\",\n  \"licenseName\": \"");
    sb.append(component.getLicenseName());
    sb.append("\",\n  \"androidMinSdk\": ");
    sb.append(component.getAndroidMinSdk());
    outputConditionalAnnotations(component, sb);
    sb.append(",\n  \"properties\": [");
    String separator = "";
    Set<String> alwaysSendProperties = new HashSet<String>();
    Map<String, String> defaultValues = new HashMap<String, String>();
    for (Map.Entry<String, DesignerProperty> entry : component.designerProperties.entrySet()) {
      String propertyName = entry.getKey();
      DesignerProperty dp = entry.getValue();
      sb.append(separator);
      if (dp.alwaysSend()) {
        alwaysSendProperties.add(propertyName);
        // We need to include the default value since it will be sent if no
        // value is specified (we don't write it in the .scm file).
        defaultValues.put(propertyName, dp.defaultValue());
      }
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
      outputBlockProperty(prop.name, prop, alwaysSendProperties.contains(prop.name), defaultValues.get(prop.name), sb);
      separator = ",\n    ";
    }
    sb.append("],\n  \"events\": [");
    separator = "";
    for (Event event : component.events.values()) {
      sb.append(separator);
      outputBlockEvent(event.name, event, sb, event.userVisible, event.deprecated);
      separator = ",\n    ";
    }
    sb.append("],\n  \"methods\": [");
    separator = "";
    for (Method method : component.methods.values()) {
      sb.append(separator);
      outputBlockMethod(method.name, method, sb, method.userVisible, method.deprecated);
      separator = ",\n    ";
    }
    sb.append("]");
    // Output assets for extensions (consumed by ExternalComponentGenerator and buildserver)
    if (component.external && component.assets.size() > 0) {
      sb.append(",\n  \"assets\": [");
      for (String asset : component.assets) {
        sb.append("\"");
        sb.append(asset.replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\""));
        sb.append("\",");
      }
      sb.setLength(sb.length() - 1);
      sb.append("]");
    }
    sb.append("}\n");
  }

  /**
   * Writes a multimap from string to string out as JSON to the given
   * StringBuilder {@code sb}. The multimap is realized as a JSON dictionary
   * mapping to an array of strings.
   *
   * @param sb The StringBuilder to receive the multimap.
   * @param indent The indent level for pretty printing. Must not be null.
   * @param map A mapping to output.
   */
  private static void outputMultimap(StringBuilder sb, String indent, Map<String, String[]> map) {
    boolean first = true;
    sb.append("{");
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      if (!first) sb.append(",");
      sb.append("\n");
      sb.append(indent);
      sb.append("  \"");
      sb.append(entry.getKey());
      sb.append("\": [\n");
      sb.append(indent);
      sb.append("    \"");
      StringUtils.join(sb, "\",\n" + indent + "    \"", entry.getValue());
      sb.append("\"\n");
      sb.append(indent);
      sb.append("  ]");
      first = false;
    }
    sb.append("\n");
    sb.append(indent);
    sb.append("}");
  }

  /**
   * Outputs the information of a component's conditional annotations (if any)
   * to the JSON component descriptor.
   *
   * @param component The component information being written.
   * @param sb The StringBuilder to receive the JSON descriptor.
   */
  private void outputConditionalAnnotations(ComponentInfo component, StringBuilder sb) {
    if (component.conditionalBroadcastReceivers.size()
        + component.conditionalContentProviders.size()
        + component.conditionalPermissions.size()
        + component.conditionalQueries.size()
        + component.conditionalServices.size() == 0) {
      return;
    }
    sb.append(",\n  \"conditionals\":{\n    ");
    boolean first = true;
    if (component.conditionalPermissions.size() > 0) {
      sb.append("\"permissions\": ");
      outputMultimap(sb, "    ", component.conditionalPermissions);
      first = false;
    }
    if (component.conditionalBroadcastReceivers.size() > 0) {
      if (!first) sb.append(",\n    ");
      sb.append("\"broadcastReceivers\": ");
      outputMultimap(sb, "    ", component.conditionalBroadcastReceivers);
      first = false;
    }
    if (component.conditionalQueries.size() > 0) {
      if (!first) {
        sb.append(",\n    ");
      }
      sb.append("\"queries\": ");
      outputMultimap(sb, "    ", component.conditionalQueries);
      first = false;
    }
    if (component.conditionalServices.size() > 0) {
      if (!first) sb.append(",\n    ");
      sb.append("\"services\": ");
      outputMultimap(sb, "    ", component.conditionalServices);
      first = false;
    }
    if (component.conditionalContentProviders.size() > 0) {
      if (!first) sb.append(",\n    ");
      sb.append("\"contentProviders\": ");
      outputMultimap(sb, "    ", component.conditionalContentProviders);
      first = false;
    }
    // Add other annotations here as needed
    sb.append("\n  }");
  }

  private void outputProperty(String propertyName, DesignerProperty dp, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(propertyName);
    sb.append("\", \"editorType\": \"");
    sb.append(dp.editorType());
    sb.append("\", \"defaultValue\": \"");
    sb.append(dp.defaultValue().replace("\"", "\\\""));

    sb.append("\", \"editorArgs\": ");
    String[] editorArgs = dp.editorArgs();
    for (int idx = 0; idx < editorArgs.length; idx += 1)
      editorArgs[idx] = "\"" + editorArgs[idx].replace("\"", "\\\"") + "\"";

    StringBuilder listLiteralBuilder = new StringBuilder();
    listLiteralBuilder.append("[");

    if (editorArgs.length > 0) {
      listLiteralBuilder.append(editorArgs[0]);

      for (int ind = 1; ind < editorArgs.length; ind += 1) {
        listLiteralBuilder.append(", ");
        listLiteralBuilder.append(editorArgs[ind]);
      }
    }

    listLiteralBuilder.append("]");

    sb.append(listLiteralBuilder.toString());
    if (dp.alwaysSend()) {
      sb.append(", \"alwaysSend\": true");
    }
    sb.append("}");
  }

  /**
   * Outputs the block description of a property.
   *
   * @param propertyName The property name
   * @param prop The property description
   * @param alwaysSend True if the block represents a DesignerProperty that is marked as always needing to be sent
   * @param defaultValue The default value of the property (only required if alwaysSend is true).
   * @param sb The StringBuilder to receive the output JSON
   */
  private void outputBlockProperty(String propertyName, Property prop, boolean alwaysSend, String defaultValue, StringBuilder sb) {
    sb.append("{ \"name\": \"");
    sb.append(propertyName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(prop.getDescription()));
    sb.append(", \"type\": \"");
    sb.append(prop.getYailType());
    sb.append("\"");
    outputHelper(prop.getHelperKey(), sb);
    sb.append(", \"rw\": \"");
    sb.append(prop.isUserVisible() ? prop.getRwString() : "invisible");
    // [lyn, 2015/12/20] Added deprecated field to JSON.
    // If we want to save space in simple-components.json,
    // we could include this field only when it is "true"
    sb.append("\", \"deprecated\": \"");
    sb.append(prop.isDeprecated());
    sb.append("\"");
    if (alwaysSend) {
      sb.append(", \"alwaysSend\": true, \"defaultValue\": \"");
      sb.append(defaultValue.replaceAll("\"", "\\\""));
      sb.append("\"");
    }
    sb.append("}");
  }

  private void outputBlockEvent(String eventName, Event event, StringBuilder sb,
                                boolean userVisible, boolean deprecated) {
    sb.append("{ \"name\": \"");
    sb.append(eventName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(event.description));
    // [lyn, 2015/12/20] Remove userVisible field from JSON, which is no longer used for events.
    // sb.append(", \"userVisible\": \"" + userVisible + "\"");
    // [lyn, 2015/12/20] Added deprecated field to JSON.
    // If we want to save space in simple-components.json,
    // we could include this field only when it is "true"
    sb.append(", \"deprecated\": \"" + deprecated + "\"");
    sb.append(", \"params\": ");
    outputParameters(event.parameters, sb);
    sb.append("}\n");
  }

  private void outputBlockMethod(String methodName, Method method, StringBuilder sb,
                                 boolean userVisible, boolean deprecated) {
    sb.append("{ \"name\": \"");
    sb.append(methodName);
    sb.append("\", \"description\": ");
    sb.append(formatDescription(method.description));
    // [lyn, 2015/12/20] Remove userVisible field from JSON, which is no longer used for methods.
    // sb.append(", \"userVisible\": \"" + userVisible + "\"");
    // [lyn, 2015/12/20] Added deprecated field to JSON.
    // If we want to save space in simple-components.json,
    // we could include this field only when it is "true"
    sb.append(", \"deprecated\": \"" + deprecated + "\"");
    sb.append(", \"params\": ");
    outputParameters(method.parameters, sb);
    if (method.getReturnType() != null) {
      sb.append(", \"returnType\": \"");
      sb.append(method.getYailReturnType());
      sb.append("\"");
    }
    if (method.isContinuation()) {
      sb.append(", \"continuation\": true");
    }
    outputHelper(method.getReturnHelperKey(), sb);
    sb.append("}");
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
      sb.append(p.getYailType());
      sb.append("\"");
      outputHelper(p.getHelperKey(), sb);
      sb.append("}");
      separator = ",";
    }
    sb.append("]");
  }

  /**
   * Outputs the json for the given helper key.
   */
  private void outputHelper(HelperKey helper, StringBuilder sb) {
    if (helper == null) {
      return;
    }
    sb.append(", \"helper\": {\n");
    sb.append("    \"type\": \"");
    sb.append(helper.getType());
    sb.append("\",\n");
    sb.append("    \"data\": {\n");
    switch (helper.getType()) {
      case OPTION_LIST:
        outputOptionList((String)helper.getKey(), sb);
        break;
      case ASSET:
        outputAsset((Integer)helper.getKey(), sb);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    sb.append("    }\n}");
  }

  /**
   * Outputs the json for the OptionList associated with the given key.
   */
  private void outputOptionList(String key, StringBuilder sb) {
    OptionList optList = optionLists.get(key);

    StringJoiner optsJoiner = new StringJoiner(",\n", "[\n", "\n      ]\n");
    for (Option opt : optList.asCollection()) {
      StringJoiner optJoiner = new StringJoiner(", ", "       { ", " }");
      optJoiner.add("\"name\": \"" +  opt.name + "\"")
          .add("\"value\": \"" +  opt.getValue() + "\"")
          .add("\"description\": " + formatDescription(opt.getDescription()))
          .add("\"deprecated\": \"" + opt.isDeprecated() + "\"");
      optsJoiner.add(optJoiner.toString());
    }

    StringJoiner sj = new StringJoiner(",\n      ", "      ", "");
    sj.add("\"className\": \"" + optList.getClassName() + "\"")
        .add("\"key\": \"" + key + "\"")
        .add("\"tag\": \"" + optList.getTagName() + "\"")
        .add("\"defaultOpt\": \"" + optList.getDefault() + "\"")
        .add("\"underlyingType\": \"" + optList.getUnderlyingType().toString() + "\"")
        .add("\"options\": " + optsJoiner.toString());

    sb.append(sj.toString());
  }

  private void outputAsset(int key, StringBuilder sb) {
    List<String> filter = filters.get(key);
    if (filter == null || filter.size() == 0) {
      return;
    }
    sb.append("      \"filter\": [ ");
    String prefix = "";
    for (String s : filter) {
      sb.append(prefix);
      sb.append("\"");
      sb.append(s);
      sb.append("\"");
      prefix = ", ";
    }
    sb.append(" ]\n");
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
