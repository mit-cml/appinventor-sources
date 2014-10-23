// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Listener interface for receiving form change events.
 *
 * <p>Classes interested in processing form change events implement this
 * interface, and instances of that class are registered with a form, using
 * the form's {@link MockForm#addFormChangeListener(FormChangeListener)}
 * method.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface FormChangeListener {

  /**
   * Invoked when a component property is changed.
   *
   * @param component  the component whose property was changed
   * @param propertyName the name of the property
   * @param propertyValue the value of the property
   */
  void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue);

  /**
   * Invoked when a component is removed.
   *
   * @param component  the component that was removed
   * @param permanentlyDeleted true if the component is being permanently
   *        deleted, false if the component is being moved from one container
   *        to another container
   */
  void onComponentRemoved(MockComponent component, boolean permanentlyDeleted);

  /**
   * Invoked when a component is added.
   *
   * @param component  the component that was added
   */
  void onComponentAdded(MockComponent component);

  /**
   * Invoked when a component is renamed.
   *
   * @param component  the component that was renamed
   * @param oldName the component's previous name
   */
  void onComponentRenamed(MockComponent component, String oldName);

  /**
   * Invoked when a selection change of the form or of one of its components
   * occurs.
   *
   * @param component  the component that was selected or unselected
   * @param selected  true if the component is selected, false if it is unselected
   */
  void onComponentSelectionChange(MockComponent component, boolean selected);
}
