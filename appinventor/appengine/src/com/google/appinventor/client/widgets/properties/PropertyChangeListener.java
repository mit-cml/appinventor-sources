// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

/**
 * Listener interface for receiving property value change events.
 *
 * <p>Classes interested in processing property value change events implement
 * this interface, and instances of that class are registered with the
 * {@link EditableProperties} in which that class is interested, using
 * {@link EditableProperties#addPropertyChangeListener(PropertyChangeListener)}
 * method. When the property value change event occurs, the listeners'
 * {@code onPropertyChange()} methods are invoked.
 *
 */
public interface PropertyChangeListener {
  /**
   * Invoked after the value of a property was changed.
   *
   * @param name  property name
   * @param newValue  new property value
   */
  void onPropertyChange(String name, String newValue);
}
