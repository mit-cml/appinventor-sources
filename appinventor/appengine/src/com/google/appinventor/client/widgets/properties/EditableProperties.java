// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.properties.Properties;

import java.util.ArrayList;
import java.util.List;


/**
 * A collection of properties with associated {@link PropertyEditor}s.
 *
 * <p>Interested classes can register listeners to receive events about value
 * changes of any property in the collection.
 *
 */
public class EditableProperties extends Properties<EditableProperty> {

  // List of listeners for any property value changes
  private final List<PropertyChangeListener> changeListeners;

  // Fire change events after adding a new property
  private final boolean changeEventOnAdd;

  /**
   * Creates a new properties collection.
   *
   * @param changeEventOnAdd  if {@code true}, a change event will be fired
   *                          after adding a new property
   */
  public EditableProperties(boolean changeEventOnAdd) {
    this.changeEventOnAdd = changeEventOnAdd;

    changeListeners = new ArrayList<PropertyChangeListener>();
  }

  /**
   * Adds a new property.
   *
   * @param name  property name
   * @param defaultValue  default value of property
   * @param caption property caption for use in the ui
   * @param editor  property editor
   * @param type  type of property; see {@code TYPE_*} constants in {@link EditableProperty}
   */

  public void addProperty(String name, String defaultValue, String caption,
      PropertyEditor editor, int type) {
    addProperty(new EditableProperty(this, name, defaultValue, caption, editor, type));
  }

  @Override
  protected void addProperty(EditableProperty property) {
    super.addProperty(property);

    if (changeEventOnAdd) {
      firePropertyChangeEvent(property.getName(), property.getValue());
    }
  }

  /**
   * Adds a {@link PropertyChangeListener} to the listener list.
   *
   * @param listener  the {@code PropertyChangeListener} to be added
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    changeListeners.add(listener);
  }

  /**
   * Removes a {@link PropertyChangeListener} from the listener list.
   *
   * @param listener  the {@code PropertyChangeListener} to be removed
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    changeListeners.remove(listener);
  }

  /**
   * Triggers a change event to be sent to the listener on the listener list.
   *
   * @param name  property name
   * @param newValue  new property value
   */
  public void firePropertyChangeEvent(String name, String newValue) {
    for (PropertyChangeListener listener : changeListeners) {
      listener.onPropertyChange(name, newValue);
    }
  }

  /**
   * Adds the properties from this collection to a properties panel for display.
   *
   * @param panel  properties panel
   */
  void addToPropertiesPanel(PropertiesPanel panel) {
    for (EditableProperty property : this) {
      if (property.isVisible()) {
        panel.addProperty(property);
      }
    }
  }

  /**
   * Orphans all properties and then deletes them from this collection.
   */
  public void clear() {
    for (EditableProperty property : this) {
      property.orphan();
    }
    deleteAllProperties();
  }
}
