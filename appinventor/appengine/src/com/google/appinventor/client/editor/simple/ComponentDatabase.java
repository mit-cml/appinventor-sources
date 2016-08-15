// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
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
class ComponentDatabase implements ComponentDatabaseInterface {



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
  ComponentDatabase(JSONArray array) {
    components = new HashMap<String, ComponentDefinition>();
    List<String> newComponents = new ArrayList<String>();
    for (JSONValue component : array.getElements()) {
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
      throw new IllegalArgumentException();
    }

    return component;
  }

  @Override
  public int getComponentVersion(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getVersion();
  }

  @Override
  public String getComponentType(String componentName){
    ComponentDefinition component = components.get(componentName);
    if(component == null){
      throw new IllegalArgumentException();
    }

    return component.getType();
  }

  @Override
  public String getComponentName(String componentType) {
    for (String componentName : components.keySet()) {
      ComponentDefinition component = components.get(componentName);
      if (component.getType() == componentType) {
        return componentName;
      }
    }
    return "";
  }

  @Override
  public boolean getComponentExternal(String componentName){
    ComponentDefinition component = components.get(componentName);
    if(component == null){
      throw new IllegalArgumentException();
    }

    return component.isExternal();
  }

  @Override
  public String getCategoryString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getCategoryString();
  }

  @Override
  public String getCategoryDocUrlString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getCategoryDocUrlString();
  }

  @Override
  public String getHelpString(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getHelpString();
  }

  @Override
  public boolean getShowOnPalette(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.isShowOnPalette();
  }

  @Override
  public boolean getNonVisible(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }
    return component.isNonVisible();
  }

  @Override
  public String getIconName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }
    return component.getIconName();
  }

  @Override
  public List<PropertyDefinition> getPropertyDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getProperties();
  }

  @Override
  public List<BlockPropertyDefinition> getBlockPropertyDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getBlockProperties();
  }

  @Override
  public List<EventDefinition> getEventDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getEvents();
  }

  @Override
  public List<MethodDefinition> getMethodDefinitions(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getMethods();
  }

  @Override
  public Map<String, String> getPropertyTypesByName(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.getPropertiesTypesByName();
  }

  @Override
  public String getTypeDescription(String componentName) {
    ComponentDefinition component = components.get(componentName);
    if (component == null) {
      throw new IllegalArgumentException();
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
        properties.get("type").asString().getString(),
        Boolean.valueOf(properties.get("external").asString().getString()),
        properties.get("categoryString").asString().getString(),
        properties.get("helpString").asString().getString(),
        Boolean.valueOf(properties.get("showOnPalette").asString().getString()),
        Boolean.valueOf(properties.get("nonVisible").asString().getString()),
        properties.get("iconName").asString().getString(), componentNode.toJson());
    findComponentProperties(component, properties.get("properties").asArray());
    findComponentBlockProperties(component, properties.get("blockProperties").asArray());
    findComponentEvents(component, properties.get("events").asArray());
    findComponentMethods(component, properties.get("methods").asArray());
    components.put(component.getName(), component);
    return true;
  }

  /*
   * Enters property information into the component descriptor.
   */
  private void findComponentProperties(ComponentDefinition component, JSONArray propertiesArray) {
    for (JSONValue propertyValue : propertiesArray.getElements()) {
      Map<String, JSONValue> properties = propertyValue.asObject().getProperties();
      component.add(new PropertyDefinition(properties.get("name").asString().getString(),
          properties.get("defaultValue").asString().getString(), properties.get("editorType")
              .asString().getString()));
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
      listener.onResetDatabase();;
    }
  }

}
