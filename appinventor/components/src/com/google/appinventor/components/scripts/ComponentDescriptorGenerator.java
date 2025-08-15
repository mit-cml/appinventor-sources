// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.FileObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tool to generate simple component descriptors as JSON.
 *
 * <p>The output is a sequence of component descriptions enclosed in square
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
 *     properties (for use by browser-based blocks editor)
 */
public final class ComponentDescriptorGenerator extends ComponentProcessor {
  // Where to write results.
  private static final String OUTPUT_FILE_NAME = "simple_components.json";

  private void outputComponent(ComponentInfo component, JSONArray parent) {
    JSONObject json = new JSONObject();
    json.put("type", component.type);
    json.put("name", component.name);
    json.put("external", Boolean.toString(component.external));
    json.put("version", Integer.toString(component.getVersion()));
    if (component.getVersionName() != null && !component.getVersionName().isEmpty()) {
      json.put("versionName", component.getVersionName());
    }
    json.put("dateBuilt", component.getDateBuilt());
    json.put("categoryString", component.getCategoryString());
    json.put("helpString", component.getHelpDescription());
    json.put("helpUrl", component.getHelpUrl());
    json.put("showOnPalette", Boolean.toString(component.getShowOnPalette()));
    json.put("nonVisible", Boolean.toString(component.getNonVisible()));
    json.put("iconName", component.getIconName());
    json.put("licenseName", component.getLicenseName());
    json.put("androidMinSdk", Integer.toString(component.getAndroidMinSdk()));
    outputConditionalAnnotations(component, json);
    outputProperties(component, json);
    outputEvents(component, json);
    outputMethods(component, json);
    if (component.external && component.assets.size() > 0) {
      outputAssets(component, json);
    }
    outputProviderModels(component, json);
    outputProvider(component, json);
    parent.put(json);
  }

  private void outputProperties(ComponentInfo component, JSONObject parent) {
    JSONArray json = new JSONArray();
    Set<String> alwaysSendProperties = new HashSet<>();
    Map<String, String> defaultValues = new HashMap<>();
    for (Map.Entry<String, DesignerProperty> entry : component.designerProperties.entrySet()) {
      String propertyName = entry.getKey();
      DesignerProperty dp = entry.getValue();
      if (dp.alwaysSend()) {
        alwaysSendProperties.add(propertyName);
        // We need to include the default value since it will be sent if no
        // value is specified (we don't write it in the .scm file).
        defaultValues.put(propertyName, dp.defaultValue());
      }
      json.put(outputProperty(propertyName, dp));
    }
    parent.put("properties", json);
    json = new JSONArray();
    // We need additional information about properties in the blocks editor,
    // and we need all of them, not just the Designer properties. We output
    // the entire set separately for use by the blocks editor to keep things simple.
    for (Property prop : component.properties.values()) {
      // Output properties that are not user-visible, but mark them as invisible
      // Note: carrying this over from the old Java blocks editor. I'm not sure
      // that we'll actually do anything with invisible properties in the blocks
      // editor. (sharon@google.com)
      json.put(outputBlockProperty(prop, component.name,
          component.designerProperties.get(prop.name),
          alwaysSendProperties.contains(prop.name),
          defaultValues.get(prop.name)));
    }
    parent.put("blockProperties", json);
  }

  private void outputEvents(ComponentInfo component, JSONObject parent) {
    JSONArray json = new JSONArray();
    for (Event event : component.events.values()) {
      json.put(outputBlockEvent(event));
    }
    parent.put("events", json);
  }

  private void outputMethods(ComponentInfo component, JSONObject parent) {
    JSONArray json = new JSONArray();
    for (Method method : component.methods.values()) {
      json.put(outputBlockMethod(method));
    }
    parent.put("methods", json);
  }

  private void outputAssets(ComponentInfo component, JSONObject parent) {
    parent.put("assets", new JSONArray(component.assets));
  }

  private void outputProviderModels(ComponentInfo component, JSONObject parent) {
    parent.put("providermodel", new JSONArray());
  }

  private void outputProvider(ComponentInfo component, JSONObject parent) {
    parent.put("provider", new JSONArray());
  }

  /**
   * Writes a multimap from string to string out as JSON to the given
   * StringBuilder {@code sb}. The multimap is realized as a JSON dictionary
   * mapping to an array of strings.
   *
   * @param map A mapping to output.
   */
  private static JSONObject outputMultimap(Map<String, String[]> map) {
    JSONObject json = new JSONObject();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      json.put(entry.getKey(), new JSONArray(entry.getValue()));
    }
    return json;
  }

  /**
   * Outputs the information of a component's conditional annotations (if any)
   * to the JSON component descriptor.
   *
   * @param component The component information being written.
   * @param parent The JSONObject to receive the JSON descriptor.
   */
  private void outputConditionalAnnotations(ComponentInfo component, JSONObject parent) {
    if (component.conditionalBroadcastReceivers.size()
        + component.conditionalContentProviders.size()
        + component.conditionalPermissions.size()
        + component.conditionalQueries.size()
        + component.conditionalServices.size() == 0) {
      return;
    }
    JSONObject json = new JSONObject();
    if (component.conditionalPermissions.size() > 0) {
      json.put("permissions", outputMultimap(component.conditionalPermissions));
    }
    if (component.conditionalBroadcastReceivers.size() > 0) {
      json.put("broadcastReceivers", outputMultimap(component.conditionalBroadcastReceivers));
    }
    if (component.conditionalQueries.size() > 0) {
      json.put("queries", outputMultimap(component.conditionalQueries));
    }
    if (component.conditionalServices.size() > 0) {
      json.put("services", outputMultimap(component.conditionalServices));
    }
    if (component.conditionalContentProviders.size() > 0) {
      json.put("contentProviders", outputMultimap(component.conditionalContentProviders));
    }
    // Add other annotations here as needed
    parent.put("conditionals", json);
  }

  private JSONObject outputProperty(String propertyName, DesignerProperty dp) {
    JSONObject json = new JSONObject();
    json.put("name", propertyName);
    json.put("editorType", dp.editorType());
    json.put("defaultValue", dp.defaultValue());
    json.put("editorArgs", new JSONArray(dp.editorArgs()));
    if (dp.alwaysSend()) {
      json.put("alwaysSend", true);
    }
    return json;
  }

  /**
   * Outputs the block description of a property.
   *
   * @param prop The property description
   * @param alwaysSend True if the block represents a DesignerProperty that is marked as always
   *                   needing to be sent
   * @param defaultValue The default value of the property (only required if alwaysSend is true).
   */
  private JSONObject outputBlockProperty(Property prop, String componentName,
      DesignerProperty designProp, boolean alwaysSend, String defaultValue) {
    if (prop.getCategory() == PropertyCategory.UNSET && designProp != null) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "Property " + componentName + "." + prop.name + " has no category.");
    }
    JSONObject json = new JSONObject();
    json.put("name", prop.name);
    json.put("description", prop.getDescription());
    json.put("type", prop.getYailType());
    outputHelper(prop.getHelperKey(), json);
    json.put("rw", prop.isUserVisible() ? prop.getRwString() : "invisible");
    json.put("deprecated", Boolean.toString(prop.isDeprecated()));
    json.put("category", prop.getCategory().getName());
    if (alwaysSend) {
      json.put("alwaysSend", true);
      json.put("defaultValue", defaultValue);
    }
    return json;
  }

  private JSONObject outputBlockEvent(Event event) {
    JSONObject json = new JSONObject();
    json.put("name", event.name);
    json.put("description", event.description);
    json.put("deprecated", Boolean.toString(event.deprecated));
    json.put("params", outputParameters(event.parameters));
    return json;
  }

  private JSONObject outputBlockMethod(Method method) {
    JSONObject json = new JSONObject();
    json.put("name", method.name);
    json.put("description", method.description);
    json.put("deprecated", Boolean.toString(method.deprecated));
    json.put("params", outputParameters(method.parameters));
    if (method.getReturnType() != null) {
      json.put("returnType", method.getYailReturnType());
    }
    if (method.isContinuation()) {
      json.put("continuation", true);
    }
    outputHelper(method.getReturnHelperKey(), json);
    return json;
  }

  /*
   *  Output a parameter list (including surrounding [])
   */
  private JSONArray outputParameters(List<Parameter> params) {
    JSONArray json = new JSONArray();
    for (Parameter p : params) {
      JSONObject param = new JSONObject();
      param.put("name", p.name);
      param.put("type", p.getYailType());
      outputHelper(p.getHelperKey(), param);
      json.put(param);
    }
    return json;
  }

  /**
   * Outputs the json for the given helper key.
   */
  private void outputHelper(HelperKey helper, JSONObject parent) {
    if (helper == null) {
      return;
    }
    JSONObject json = new JSONObject();
    json.put("type", helper.getType());
    switch (helper.getType()) {
      case OPTION_LIST:
        json.put("data", outputOptionList((String) helper.getKey()));
        break;
      case ASSET:
        json.put("data", outputAsset((Integer) helper.getKey()));
        break;
      case PROVIDER_MODEL:
        json.put("data", outputProviderModel((Integer) helper.getKey()));
        break;
      case PROVIDER:
        json.put("data", outputProvider((Integer) helper.getKey()));
        break;
      default:
        throw new UnsupportedOperationException();
    }
    parent.put("helper", json);
  }

  /**
   * Outputs the json for the OptionList associated with the given key.
   */
  private JSONObject outputOptionList(String key) {
    OptionList optList = optionLists.get(key);
    JSONObject json = new JSONObject();
    json.put("className", optList.getClassName());
    json.put("key", key);
    json.put("tag", optList.getTagName());
    json.put("defaultOpt", optList.getDefault());
    json.put("underlyingType", optList.getUnderlyingType().toString());
    JSONArray options = new JSONArray();
    for (Option opt : optList.asCollection()) {
      JSONObject option = new JSONObject();
      option.put("name", opt.name);
      option.put("value", opt.getValue());
      option.put("description", opt.getDescription());
      option.put("deprecated", Boolean.toString(opt.isDeprecated()));
      options.put(option);
    }
    json.put("options", options);
    return json;
  }

  private JSONObject outputAsset(int key) {
    JSONObject json = new JSONObject();
    List<String> filter = filters.get(key);
    if (filter != null && filter.size() != 0) {
      json.put("filter", new JSONArray(filter));
    }
    return json;
  }

  private JSONObject outputProviderModel(int key) {
    JSONObject json = new JSONObject();
    List<String> filter = filters.get(key);
    if (filter != null && filter.size() != 0) {
      json.put("filter", new JSONArray(filter));
    }
    return json;
  }

  private JSONObject outputProvider(int key) {
    JSONObject json = new JSONObject();
    List<String> filter = filters.get(key);
    if (filter != null && filter.size() != 0) {
      json.put("filter", new JSONArray(filter));
    }
    return json;
  }

  @Override
  protected void outputResults() throws IOException {
    JSONArray collection = new JSONArray();

    // Components are already sorted.
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      outputComponent(entry.getValue(), collection);
    }

    FileObject src = createOutputFileObject(OUTPUT_FILE_NAME);
    Writer writer = src.openWriter();
    writer.write(collection.toString());
    writer.flush();
    writer.close();
    messager.printMessage(Diagnostic.Kind.NOTE, "Wrote file " + src.toUri());
  }
}
