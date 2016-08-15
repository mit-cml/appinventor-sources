// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.simple;

import com.google.appinventor.components.common.ComponentCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Database holding property and event information of Simple components.
 *
 * @author lizlooney@google.com (lizlooney)
 */
public interface ComponentDatabaseInterface {

  /**
   * Simple component information: component name, its properties
   */
  public static class ComponentDefinition {
    private final String name;
    private final int version;
    private final String type;
    private final boolean external;
    private final String categoryString;
    private final String helpString;
    private final boolean showOnPalette;
    private final String categoryDocUrlString;
    private final List<PropertyDefinition> properties;
    private final List<BlockPropertyDefinition> blockProperties;
    private final List<EventDefinition> events;
    private final List<MethodDefinition> methods;
    private final Map<String, String> propertiesTypesByName;
    private final boolean nonVisible;
    private final String iconName;
    private final String typeDescription;

    public ComponentDefinition(String name, int version, String type, boolean external, String categoryString, String helpString,
              boolean showOnPalette, boolean nonVisible, String iconName, String typeDescription) {
      this.name = name;
      this.version = version;
      this.type = type;
      this.external = external;
      this.categoryString = categoryString;
      this.helpString = helpString;
      this.showOnPalette = showOnPalette;
      this.categoryDocUrlString = ComponentCategory.valueOf(categoryString).getDocName();
      this.properties = new ArrayList<PropertyDefinition>();
      this.blockProperties = new ArrayList<BlockPropertyDefinition>();
      this.events = new ArrayList<EventDefinition>();
      this.methods = new ArrayList<MethodDefinition>();
      this.propertiesTypesByName = new HashMap<String, String>();
      this.nonVisible = nonVisible;
      this.iconName = iconName;
      this.typeDescription = typeDescription;
    }

    public void add(PropertyDefinition property) {
      properties.add(property);
      propertiesTypesByName.put(property.getName(), property.getEditorType());
    }

    public void add(BlockPropertyDefinition blockProperty) {
      blockProperties.add(blockProperty);
    }

    public void add(EventDefinition event) {
      events.add(event);
    }

    public void add(MethodDefinition method) {
      methods.add(method);
    }

    public String getName() {
      return name;
    }

    public int getVersion() {
      return version;
    }

    public String getType() {
      return type;
    }

    public boolean isExternal() {
      return external;
    }

    public String getCategoryString() {
      return categoryString;
    }

    public String getHelpString() {
      return helpString;
    }

    public boolean isShowOnPalette() {
      return showOnPalette;
    }

    public String getCategoryDocUrlString() {
      return categoryDocUrlString;
    }

    public List<PropertyDefinition> getProperties() {
      return properties;
    }

    public List<BlockPropertyDefinition> getBlockProperties() {
      return blockProperties;
    }

    public List<EventDefinition> getEvents() {
      return events;
    }

    public List<MethodDefinition> getMethods() {
      return methods;
    }

    public Map<String, String> getPropertiesTypesByName() {
      return propertiesTypesByName;
    }

    public boolean isNonVisible() {
      return nonVisible;
    }

    public String getIconName() {
      return iconName;
    }

    public String getTypeDescription() {
      return typeDescription;
    }
  }


  /**
   * Property definition: property name, property editor type and property
   * default value.
   */
  public static class PropertyDefinition {
    private final String name;
    private final String defaultValue;
    private final String caption;
    private final String editorType;

    public PropertyDefinition(String name, String defaultValue, String editorType) {
      this(name, defaultValue, name, editorType);
    }

    public PropertyDefinition(String name, String defaultValue, String caption, String editorType) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.caption = caption;
      this.editorType = editorType;
    }

    public String getName() {
      return name;
    }

    public String getCaption() {
      return caption;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public String getEditorType() {
      return editorType;
    }
  }

  /**
   * Block Property definition: property name, property description and property
   * type and read/write permission.
   */
  public static class BlockPropertyDefinition {
    private final String name;
    private final String description;
    private final String type;
    private final String rw;

    public BlockPropertyDefinition(String name, String description, String type) {
      this(name, description, type, "read-write");
    }

    public BlockPropertyDefinition(String name, String description,
        String type, String rw) {
      this.name = name;
      this.description = description;
      this.type = type;
      this.rw = rw;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public String getType() {
      return type;
    }

    public String getRW() {
      return rw;
    }
  }

  /**
   * Method and Event's parameter definition: parameter name and
   * parameter type.
   */
  public static class ParameterDefinition {
    private final String name;
    private final String type;

    public ParameterDefinition(String name, String type) {
      this.name = name;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }
  }

  /**
   * Event definition: event name, event description and event params.
   */
  public static class EventDefinition {
    private final String name;
    private final String description;
    private final boolean deprecated;
    // "params": [{ "name": "xAccel", "type": "number"},*]
    private final List<ParameterDefinition> params;

    public EventDefinition(String name, String description, boolean deprecated,
        List<ParameterDefinition> params) {
      this.name = name;
      this.description = description;
      this.deprecated = deprecated;
      this.params = params;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public boolean getDeprecated() { return deprecated; }

    public List<ParameterDefinition> getParam() {
      return params;
    }
  }

  /**
   * Method definition: method name, method description, method params,
   * and return type.
   */
  public static class MethodDefinition {
    private final String name;
    private final String description;
    private final boolean deprecated;
    // "params": [{ "name": "xAccel", "type": "number"},*]
    private List<ParameterDefinition> params;

    public MethodDefinition(String name, String description, boolean deprecated,
        List<ParameterDefinition> params) {
      this.name = name;
      this.description = description;
      this.deprecated = deprecated;
      this.params = params;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public boolean getDeprecated() { return deprecated; }

    public List<ParameterDefinition> getParam() {
      return params;
    }
  }

  /**
   * Returns the component names.
   */
  Set<String> getComponentNames();

  /**
   *Returns the type of a component
   *
   *@param componentName name of component to query
   *@return type of the component
   */
  String getComponentType(String componentName);

  /**
   *Returns the name of a component
   *
   *@param componentType type of component to query
   *@return name of the component
   */
  String getComponentName(String componentType);

  /**
   *
   *Returns the whether a component is external or not
   *
   *@param componentName name of component to query
   *@return true if componentName is external false otherwise
   */
  boolean getComponentExternal(String componentName);

  /**
   * Returns the version number of a component.
   *
   * @param componentName  name of component to query
   * @return  the component version number
   */
  int getComponentVersion(String componentName);

  /**
   * Returns the String version of a component's category.  Note that this
   * is the result of calling getString() on a ComponentCategory, not the
   * result of calling getName().  For example, for the ComponentCategory
   * EXPERIMENTAL, this returns "EXPERIMENTAL", not "Not ready for prime
   * time".  The procedure was written this way so that the result could
   * be converted back to a ComponentCategory with valueOf().
   *
   * @param componentName  name of component to query
   * @return  the component's category
   */
  String getCategoryString(String componentName);

  /**
   * Returns the appropriate piece of the URL in the goro docs for a
   * component's category (or null, if the mapping is not known).
   *
   * @param componentName  name of component to query
   * @return  the URL piece in the docs
   */
  String getCategoryDocUrlString(String componentName);

  /**
   * Returns a helpful message about the component.  This message
   * originates in the <code>designerHelpDescription</code> field of the
   * component's
   * {@link com.google.appinventor.components.annotations.DesignerComponent}
   * annotation, is written to the file "simple_component.json" by
   * {@link com.google.appinventor.components.scripts.ComponentDescriptorGenerator},
   * and is read back by
   * {@link com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel}.
   * It is not allowed to contain HTML or XML markup.
   *
   * @param componentName  name of component to query
   * @return helpful message about the component
   */
  String getHelpString(String componentName);

  /**
   * Returns whether the component with this name should be shown on the
   * palette.  As the time this is written (2/22/10), the only component
   * that should not be shown on the palette is Form/Screen.
   *
   * @param componentName  name of component to query
   * @return whether the component should be shown on the palette
   */
  boolean getShowOnPalette(String componentName);

  /**
   * Returns whether the component with this name is a "non-visible" component
   * (i.e., one that doesn't have a representation in the UI). Non-visible
   * components can be handled uniformly in the Designer and don't need
   * special MockComponent representations.
   *
   * @param componentName  name of component to query
   * @return whether the component is non-visible
   */
  boolean getNonVisible(String componentName);

  /**
   * Returns the name of the icon file (last part of the path name) for the
   * icon to be shown in the Designer
   */
  String getIconName(String componentName);

  /**
   * Returns a list of a component's property definitions.
   *
   * @param componentName  name of component to query
   * @return  list of property definition for the component
   */
  List<PropertyDefinition> getPropertyDefinitions(String componentName);

  /**
   * Returns a list of a component's block property definitions.
   *
   * @param componentName  name of component to query
   * @return  list of block property definition for the component
   */
  List<BlockPropertyDefinition> getBlockPropertyDefinitions(String componentName);

  /**
   * Returns a list of a component's event definitions.
   *
   * @param componentName  name of component to query
   * @return  list of event definition for the component
   */
  List<EventDefinition> getEventDefinitions(String componentName);

  /**
   * Returns a list of a component's method definitions.
   *
   * @param componentName  name of component to query
   * @return  list of method definition for the component
   */
  List<MethodDefinition> getMethodDefinitions(String componentName);

  /*
   * Returns a map of the property names and types for a component
   *
   * @param componentName  name of component to query
   * @return  map of property names and types
   */
  Map<String, String> getPropertyTypesByName(String componentName);

  /*
   * Returns the JSON string describing the component type for a component
   *
   * @param componentName  name of component to query
   */
  String getTypeDescription(String componentName);

   /*
   * Returns true if componentName matches some component
   *
   * @param componentName  name of component to query
   */
  boolean isComponent(String componentName);
}
