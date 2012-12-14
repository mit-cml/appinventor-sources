// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.components.common.ComponentCategory;

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

  /*
   * Simple component information: component name, its properties
   */
  private static class Component {
    private final String name;
    private final int version;
    private final String categoryString;
    private final String helpString;
    private final boolean showOnPalette;
    private final String categoryDocUrlString;
    private final List<PropertyDefinition> properties;
    private final Map<String, String> propertiesTypesByName;
    private final boolean nonVisible;
    private final String iconName;
    private final String typeDescription;

    Component(String name, int version, String categoryString, String helpString,
              boolean showOnPalette, boolean nonVisible, String iconName,
              String typeDescription) {
      this.name = name;
      this.version = version;
      this.categoryString = categoryString;
      this.helpString = helpString;
      this.showOnPalette = showOnPalette;
      this.categoryDocUrlString = ComponentCategory.valueOf(categoryString).getDocName();
      this.properties = new ArrayList<PropertyDefinition>();
      this.propertiesTypesByName = new HashMap<String, String>();
      this.nonVisible = nonVisible;
      this.iconName = iconName;
      this.typeDescription = typeDescription;
    }

    void add(PropertyDefinition property) {
      properties.add(property);
      propertiesTypesByName.put(property.getName(), property.getEditorType());
    }
  }

  // Maps component names to component descriptors
  private final Map<String, Component> components;

  /**
   * Creates a new component database.
   *
   * @param array a JSONArray of components
   */
  ComponentDatabase(JSONArray array) {
    components = new HashMap<String, Component>();
    for (JSONValue component : array.getElements()) {
      initComponent(component.asObject());
    }
  }

  @Override
  public Set<String> getComponentNames() {
    return components.keySet();
  }

  @Override
  public int getComponentVersion(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.version;
  }

  @Override
  public String getCategoryString(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.categoryString;
  }

  @Override
  public String getCategoryDocUrlString(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.categoryDocUrlString;
  }

  @Override
  public String getHelpString(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.helpString;
  }

  @Override
  public boolean getShowOnPalette(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.showOnPalette;
  }

  @Override
  public boolean getNonVisible(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }
    return component.nonVisible;
  }

  @Override
  public String getIconName(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }
    return component.iconName;
  }

  @Override
  public List<PropertyDefinition> getPropertyDefinitions(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.properties;
  }

  @Override
  public Map<String, String> getPropertyTypesByName(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.propertiesTypesByName;
  }

  @Override
  public String getTypeDescription(String componentTypeName) {
    Component component = components.get(componentTypeName);
    if (component == null) {
      throw new IllegalArgumentException();
    }

    return component.typeDescription;
  }

  /*
   * Creates a component descriptor from the contents of the JSON file and puts
   * it in the components map.
   */
  private void initComponent(JSONObject componentNode) {
    Map<String, JSONValue> properties = componentNode.getProperties();
    Component component = new Component(
        properties.get("name").asString().getString(),
        Integer.parseInt(properties.get("version").asString().getString()),
        properties.get("categoryString").asString().getString(),
        properties.get("helpString").asString().getString(),
        Boolean.valueOf(properties.get("showOnPalette").asString().getString()),
        Boolean.valueOf(properties.get("nonVisible").asString().getString()),
        properties.get("iconName").asString().getString(),
        componentNode.toJson());
    findComponentProperties(component, properties.get("properties").asArray());
    components.put(component.name, component);
  }

  /*
   * Enters property information into the component descriptor.
   */
  private void findComponentProperties(Component component, JSONArray propertiesArray) {
    for (JSONValue propertyValue : propertiesArray.getElements()) {
      Map<String, JSONValue> properties = propertyValue.asObject().getProperties();
      component.add(new PropertyDefinition(properties.get("name").asString().getString(),
          properties.get("defaultValue").asString().getString(),
          properties.get("editorType").asString().getString()));
    }
  }
}
