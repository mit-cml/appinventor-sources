// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Database holding information of Simple components and their properties.
 *
 */
public class ComponentDatabase implements ComponentDatabaseInterface {



  // Maps component names to component descriptors
  private final Map<String, ComponentDefinition> components;

  // Components in JSON String generated from internal components
  private final String internalComponentsJSONString;
  // Components in JSON String generated from components
  private String componentsJSONString;

  private final List<ComponentDatabaseChangeListener> componentDatabaseChangeListeners =
      new ArrayList<ComponentDatabaseChangeListener>();

  /**
   * Creates a new component database.
   *
   * @param array
   *          a JSONArray of components
   */
  public ComponentDatabase(JSONArray array) {
    components = new HashMap<String, ComponentDefinition>();
    List<String> newComponents = new ArrayList<String>();
    for (JSONValue component : array.getElements()) {
      if (component.asObject().get("external").asString().getString().equals("true")) {
        continue;
      }
      if (initComponent(component.asObject())) {
        newComponents.add(component.asObject().get("name").asString().getString());
      }
    }
    internalComponentsJSONString = generateComponentsJSON();
    componentsJSONString = internalComponentsJSONString;
    fireComponentsAdded(newComponents);
  }

  public boolean addComponent(JSONValue component) {
    if (!initComponent(component.asObject())) return false;
    List<String> newComponents = new ArrayList<String>();
    newComponents.add(component.asObject().get("name").asString().getString());
    componentsJSONString = generateComponentsJSON();
    fireComponentsAdded(newComponents);
    return true;
  }

  public int addComponents(JSONArray array) {
    int compsAdded = 0;
    List<String> newComponents = new ArrayList<String>();
    for ( JSONValue component : array.getElements()) {
      if(initComponent(component.asObject())) {
        newComponents.add(component.asObject().get("name").asString().getString());
        ++compsAdded;
      }
    }
    componentsJSONString = generateComponentsJSON();
    fireComponentsAdded(newComponents);
    return compsAdded;
  }

  public boolean removeComponent(String componentName) {
    List<String> removedComponents = new ArrayList<String>();
    removedComponents.add(componentName);
    Map<String, String> removedComponentsMap = new HashMap<String, String>();
    for (String componentType : removedComponents) {
      removedComponentsMap.put(componentType, getComponentType(componentType));
    }
    if (!fireBeforeComponentsRemoved(removedComponents)) {
      throw new IllegalStateException("Failed to remove Component!");
    }
    if (components.remove(componentName) != null) {
      componentsJSONString = generateComponentsJSON();
      fireComponentsRemoved(removedComponentsMap);
      return true;
    }
    return false;
  }
  /**
   * Resets the Component Database to include only Internal Components
   */
  public void resetDatabase() {
    components.clear();
    componentsJSONString = "";
    addComponents(new ClientJsonParser().parse(internalComponentsJSONString).asArray());
    fireResetDatabase();
  }

  @Override
  public Set<String> getComponentNames() {
    return components.keySet();
  }

  public ComponentDefinition getComponentDefinition(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component;
  }

  @Override
  public int getComponentVersion(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getVersion();
  }

  @Override
  public String getComponentVersionName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getVersionName();
  }

  @Override
  public String getComponentBuildDate(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getDateBuilt();
  }

  @Override
  public String getComponentType(String componentName){
    ComponentDefinition component = components.get(componentName);
    if(component == null){
      throw new ComponentNotFoundException(componentName);
    }

    return component.getType();
  }

  @Override
  public String getComponentName(String componentType) {
    for (String componentName : components.keySet()) {
      ComponentDefinition component = components.get(componentName);
      if (componentType.equals(component.getType())) {
        return componentName;
      }
    }
    return "";
  }

  @Override
  public boolean getComponentExternal(String componentName){
    ComponentDefinition component = components.get(componentName);
    if(component == null){
      throw new ComponentNotFoundException(componentName);
    }

    return component.isExternal();
  }

  @Override
  public String getCategoryString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getCategoryString();
  }

  @Override
  public String getCategoryDocUrlString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getCategoryDocUrlString();
  }

  @Override
  public String getHelpString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getHelpString();
  }

  @Override
  public String getHelpUrl(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getHelpUrl();
  }

  @Override
  public boolean getShowOnPalette(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.isShowOnPalette();
  }

  @Override
  public boolean getNonVisible(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }
    return component.isNonVisible();
  }

  @Override
  public String getIconName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }
    return component.getIconName();
  }

  @Override
  public String getLicenseName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }
    return component.getLicenseName();
  }

  @Override
  public List<PropertyDefinition> getPropertyDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getProperties();
  }

  @Override
  public List<BlockPropertyDefinition> getBlockPropertyDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getBlockProperties();
  }

  @Override
  public List<EventDefinition> getEventDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getEvents();
  }

  @Override
  public List<MethodDefinition> getMethodDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getMethods();
  }

  @Override
  public Map<String, String> getPropertyTypesByName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getPropertiesTypesByName();
  }

  @Override
  public String getTypeDescription(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new ComponentNotFoundException(componentName);
    }

    return component.getTypeDescription();
  }

  public String getComponentsJSONString() {
    return componentsJSONString;
  }

  /*
   * Creates a component descriptor from the contents of the JSON file and puts
   * it in the components map.
   */
  private boolean initComponent(JSONObject componentNode) {
    Map<String, JSONValue> properties = componentNode.getProperties();
    String name = properties.get("name").asString().getString();
    if(components.containsKey(name)) {
      // This must be a component upgrade! We remove existing entry
      components.remove(name);
    }
    ComponentDefinition component = new ComponentDefinition(name,
        Integer.parseInt(properties.get("version").asString().getString()),
        optString(properties.get("versionName"), ""),
        optString(properties.get("dateBuilt"), ""),
        properties.get("type").asString().getString(),
        Boolean.valueOf(properties.get("external").asString().getString()),
        properties.get("categoryString").asString().getString(),
        properties.get("helpString").asString().getString(),
        properties.containsKey("helpUrl") ? properties.get("helpUrl").asString().getString() : "",
        Boolean.valueOf(properties.get("showOnPalette").asString().getString()),
        Boolean.valueOf(properties.get("nonVisible").asString().getString()),
        properties.get("iconName").asString().getString(),
        properties.containsKey("licenseName") ? properties.get("licenseName").asString().getString() : "",
        componentNode.toJson());
    findComponentProperties(component, properties.get("properties").asArray(),
        properties.get("blockProperties").asArray());
    findComponentBlockProperties(component, properties.get("blockProperties").asArray());
    findComponentEvents(component, properties.get("events").asArray());
    findComponentMethods(component, properties.get("methods").asArray());
    components.put(component.getName(), component);
    return true;
  }

  /**
   * Extracts a string from the given value. If value is null, returns the defaultValue.
   * @param value JSON value to process
   * @param defaultValue Alternative value if {@code value} is not valid
   * @return A non-null String containing either the String version of {@code value} or
   * {@code defaultValue}
   */
  private String optString(JSONValue value, String defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return value.asString().getString();
  }

  /*
   * Enters property information into the component descriptor.
   */
  private void findComponentProperties(ComponentDefinition component,
      JSONArray propertiesArray, JSONArray blockPropertiesArray) {
    Map<String, String> descriptions = new HashMap<String, String>();
    Map<String, String> categoryMap = new HashMap<String, String>();
    for (JSONValue block : blockPropertiesArray.getElements()) {
      Map<String, JSONValue> properties = block.asObject().getProperties();
      String name = properties.get("name").asString().getString();
      // Extensions may not have a category set on the designer property, so we have to be
      // conservative here.
      categoryMap.put(name, optString(properties.get("category"), "Uncategorized"));
      descriptions.put(name, properties.get("description").asString().getString());
    }
    for (JSONValue propertyValue : propertiesArray.getElements()) {
      Map<String, JSONValue> properties = propertyValue.asObject().getProperties();

      // TODO Since older versions of extensions do not have the "editorArgs" key,
      // we check if "editorArgs" exists before parsing as a workaround. We may
      // need better approaches in future versions.
      List<String> editorArgsList = new ArrayList<String>();
      if (properties.containsKey("editorArgs")) {
        for (JSONValue val : properties.get("editorArgs").asArray().getElements())
          editorArgsList.add(val.asString().getString());
      }

      String name = properties.get("name").asString().getString();
      String category = categoryMap.get(name);
      if ( category == null ) {
        category = "Unspecified";
      }
      component.add(new PropertyDefinition(name,
          properties.get("defaultValue").asString().getString(),
          name, properties.get("editorType").asString().getString(),
          editorArgsList.toArray(new String[0]),
          category, descriptions.get(name)));
    }
  }

  /*
   * Enters block property information into the component descriptor.
   */
  private void findComponentBlockProperties(ComponentDefinition component, JSONArray blockPropertiesArray) {
    for (JSONValue blockPropertyValue : blockPropertiesArray.getElements()) {
      Map<String, JSONValue> blockProperties = blockPropertyValue.asObject().getProperties();
      component.add(new BlockPropertyDefinition(blockProperties.get("name").asString().getString(),
          blockProperties.get("description").asString().getString(), blockProperties.get("type")
              .asString().getString(), blockProperties.get("rw").asString().getString()));
    }
  }

  /*
   * Enters event information into the component descriptor.
   */
  private void findComponentEvents(ComponentDefinition component, JSONArray eventsArray) {
    for (JSONValue eventValue : eventsArray.getElements()) {
      Map<String, JSONValue> event = eventValue.asObject().getProperties();

      List<ParameterDefinition> paramList = new ArrayList<ParameterDefinition>();
      List<JSONValue> params = event.get("params").asArray().getElements();
      for (int i = 0; i < params.size(); ++i) {
        JSONValue paramValue = params.get(i);
        Map<String, JSONValue> param = paramValue.asObject().getProperties();
        paramList.add(new ParameterDefinition(param.get("name").asString().getString(), param
            .get("type").asString().getString()));
      }
      component.add(
          new EventDefinition(
              event.get("name").asString().getString(),
              event.get("description").asString().getString(),
              new Boolean(event.get("deprecated").asString().getString()),
              paramList));
    }
  }

  /*
   * Enters method information into the component descriptor.
   */
  private void findComponentMethods(ComponentDefinition component, JSONArray methodsArray) {
    for (JSONValue blockPropertyValue : methodsArray.getElements()) {
      Map<String, JSONValue> method = blockPropertyValue.asObject().getProperties();

      List<ParameterDefinition> paramList = new ArrayList<ParameterDefinition>();
      List<JSONValue> params = method.get("params").asArray().getElements();
      for (int i = 0; i < params.size(); ++i) {
        JSONValue paramValue = params.get(i);
        Map<String, JSONValue> param = paramValue.asObject().getProperties();
        paramList.add(new ParameterDefinition(param.get("name").asString().getString(), param
            .get("type").asString().getString()));
      }
      component.add(
          new MethodDefinition(
              method.get("name").asString().getString(),
              method.get("description").asString().getString(),
              new Boolean(method.get("deprecated").asString().getString()),
              paramList));
    }
  }

  private String generateComponentsJSON(){
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    String separator = "";
    for(Map.Entry<String, ComponentDefinition> comp : components.entrySet()){
      sb.append(separator).append(comp.getValue().getTypeDescription());
      separator=",";
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public boolean isComponent(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      return false;
    }

    return true;
  }

  public void addComponentDatabaseListener(ComponentDatabaseChangeListener listener) {
    componentDatabaseChangeListeners.add(listener);
  }

  public void removeComponentDatabaseListener(ComponentDatabaseChangeListener listener) {
    componentDatabaseChangeListeners.remove(listener);
  }

  private List<ComponentDatabaseChangeListener> copyComponentDatbaseChangeListeners() {
    return new ArrayList<ComponentDatabaseChangeListener>(componentDatabaseChangeListeners);
  }

  private void fireComponentsAdded(List<String> componentTypes) {
    for (ComponentDatabaseChangeListener listener : copyComponentDatbaseChangeListeners()) {
      listener.onComponentTypeAdded(componentTypes);
    }
  }

  private boolean fireBeforeComponentsRemoved(List<String> componentTypes) {
    boolean result = true;
    for (ComponentDatabaseChangeListener listener : copyComponentDatbaseChangeListeners()) {
      result = result & listener.beforeComponentTypeRemoved(componentTypes);
    }
    return result;
  }

  private void fireComponentsRemoved(Map<String, String> componentTypes) {
    for (ComponentDatabaseChangeListener listener : copyComponentDatbaseChangeListeners()) {
      listener.onComponentTypeRemoved(componentTypes);
    }
  }

  private void fireResetDatabase() {
    for (ComponentDatabaseChangeListener listener : copyComponentDatbaseChangeListeners()) {
      listener.onResetDatabase();
    }
  }

}
