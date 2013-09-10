// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.simple;

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
   * Returns the component names.
   */
  Set<String> getComponentNames();

  /**
   * Returns the version number of a component.
   *
   * @param componentTypeName  name of component to query
   * @return  the component version number
   */
  int getComponentVersion(String componentTypeName);

  /**
   * Returns the String version of a component's category.  Note that this
   * is the result of calling getString() on a ComponentCategory, not the
   * result of calling getName().  For example, for the ComponentCategory
   * EXPERIMENTAL, this returns "EXPERIMENTAL", not "Not ready for prime
   * time".  The procedure was written this way so that the result could
   * be converted back to a ComponentCategory with valueOf().
   *
   * @param componentTypeName  name of component to query
   * @return  the component's category
   */
  String getCategoryString(String componentTypeName);

  /**
   * Returns the appropriate piece of the URL in the goro docs for a
   * component's category (or null, if the mapping is not known).
   *
   * @param componentTypeName  name of component to query
   * @return  the URL piece in the docs
   */
  String getCategoryDocUrlString(String componentTypeName);

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
   * @param componentTypeName  name of component to query
   * @return helpful message about the component
   */
  String getHelpString(String componentTypeName);

  /**
   * Returns whether the component with this name should be shown on the
   * palette.  As the time this is written (2/22/10), the only component
   * that should not be shown on the palette is Form/Screen.
   *
   * @param componentTypeName  name of component to query
   * @return whether the component should be shown on the palette
   */
  boolean getShowOnPalette(String componentTypeName);

  /**
   * Returns whether the component with this name is a "non-visible" component
   * (i.e., one that doesn't have a representation in the UI). Non-visible
   * components can be handled uniformly in the Designer and don't need
   * special MockComponent representations.
   *
   * @param componentTypeName  name of component to query
   * @return whether the component is non-visible
   */
  boolean getNonVisible(String componentTypeName);

  /**
   * Returns the name of the icon file (last part of the path name) for the
   * icon to be shown in the Designer
   */
  String getIconName(String componentTypeName);

  /**
   * Returns a list of a component's property definitions.
   *
   * @param componentTypeName  name of component to query
   * @return  list of property definition for the component
   */
  List<PropertyDefinition> getPropertyDefinitions(String componentTypeName);

  /*
   * Returns a map of the property names and types for a component
   *
   * @param componentTypeName  name of component to query
   * @return  map of property names and types
   */
  Map<String, String> getPropertyTypesByName(String componentTypeName);
  
  /*
   * Returns the JSON string describing the component type for a component
   * 
   * @param componentTypeName  name of component to query
   */
  String getTypeDescription(String componentTypeName);
}
