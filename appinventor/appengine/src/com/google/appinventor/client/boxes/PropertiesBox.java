// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertiesPanel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Box implementation for properties panels.
 *
 */
public final class PropertiesBox extends Box {

  // Singleton properties box instance
  private static final Logger LOG = Logger.getLogger(PropertiesBox.class.getName());
  private static PropertiesBox INSTANCE = new PropertiesBox();
  private EditableProperties selectedProperties = null;
  private final PropertiesPanel designProperties = new PropertiesPanel();

  /**
   * Return the properties box.
   *
   * @return properties box
   */
  public static PropertiesBox getPropertiesBox() {
    return INSTANCE;
  }

  /**
   * Creates new properties box.
   */
  private PropertiesBox() {
    super(MESSAGES.propertiesBoxCaption(),
        200,    // height
        false,  // minimizable
        false,  // removable
        false,  // startMinimized
        false,  // bodyPadding
        false); // highlightCaption

    setContent(designProperties);
  }

  /*
   * Show the given component's properties in the properties panel.
   */
  public void show(YaFormEditor formEditor, boolean selected) {
    MockForm form = formEditor.getForm();
    List<MockComponent> components = form.getSelectedComponents();
    if (components == null || components.size() == 0) {
      throw new IllegalArgumentException("components must be a list of at least 1");
    }
    if (selectedProperties != null) {
      // TODO: figure out what this property change listener should now point to.
      selectedProperties.removePropertyChangeListener(formEditor);
    }
    if (components.size() == 1) {
      selectedProperties = components.get(0).getProperties();
    } else {
      EditableProperties newProperties = new EditableProperties(true);
      Map<String, EditableProperty> propertyMaps = new HashMap<>();
      boolean first = true;
      for (MockComponent component : components) {
        Set<String> properties = new HashSet<>();
        for (EditableProperty property : component.getProperties()) {
          String propertyName = property.getName();
          // Ignore UUID and NAME properties (can't be edited and always unique)
          if ("Uuid".equals(propertyName) || "Name".equals(propertyName)) {
            continue;
          }
          if (first) {
            propertyMaps.put(propertyName + ":" + property.getType(), property);
          } else {
            properties.add(propertyName + ":" + property.getType());
          }
        }
        if (properties.size() > 0) {
          propertyMaps.keySet().retainAll(properties);
        }
        first = false;
      }
      for (EditableProperty property : propertyMaps.values()) {
        String name = property.getName();
        newProperties.addProperty(
            name,
            property.getDefaultValue(),
            property.getCaption(),
            property.getCategory(),
            property.getDescription(),
            PropertiesUtil.createPropertyEditor(property.getEditorType(),
                property.getDefaultValue(), formEditor, property.getEditorArgs()),
            property.getType(),
            property.getEditorType(),
            property.getEditorArgs()
        );

        // Determine if all components have the same value and apply it
        String sharedValue = components.get(0).getPropertyValue(name);
        boolean collision = false;
        for (MockComponent component : components) {
          String propValue = component.getPropertyValue(name);
          if (!sharedValue.equals(propValue)) {
            sharedValue = "";
            collision = true;
            break;
          }
        }
        newProperties.getProperty(name).getEditor().setMultipleValues(collision);
        newProperties.getProperty(name).getEditor().setMultiselectMode(true);
        newProperties.getProperty(name).setValue(sharedValue);
      }
      selectedProperties = newProperties;
    }
    if (selected) {
      selectedProperties.addPropertyChangeListener(formEditor);
    }
    designProperties.setProperties(selectedProperties);
    if (components.size() > 1) {
      // TODO: Localize
      designProperties.setPropertiesCaption(MESSAGES.componentsSelected(components.size()));
    } else {
      // need to update the caption after the setProperties call, since
      // setProperties clears the caption!
      String componentType = components.get(0).getType();
      designProperties.setPropertiesCaption(components.get(0).getName() + " (" +
          ComponentTranslationTable.getComponentName(componentType.equals("Form")
              ? "Screen" : componentType) + ")");
    }
  }
}
