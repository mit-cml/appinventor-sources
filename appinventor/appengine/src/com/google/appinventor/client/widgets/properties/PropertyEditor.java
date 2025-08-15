// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.user.client.ui.Composite;

/**
 * Superclass for all property editors.
 *
 */
public abstract class PropertyEditor extends Composite {

  /**
   * Flag to indicate that the property editor is being used to edit multiple components.
   */
  private boolean multiselectMode = false;

  /**
   * Flag to indicate that the property editor's value is indeterminant due to conflicting values
   * for multiple selected components.
   */
  private boolean multiple = false;

  /**
   * Property being edited by this editor.
   */
  protected EditableProperty property;

  /**
   * Updates the property value shown in the editor.
   */
  protected abstract void updateValue();

  /**
   * Sets the property to be edited by this editor.
   *
   * @param property  property to be edited by this editor
   */
  public final void setProperty(EditableProperty property) {
    this.property = property;
    updateValue();
  }

  /**
   * Called when this property editor is being orphaned.
   *
   * If a property editor listens for events, it should override this method so
   * it will stop listening for events after it has been orphaned.
   */
  public void orphan() {
  }

  public void refresh() {
    this.updateValue();
  }

  /**
   * Sets whether the property editor is being used to edit multiple components.
   *
   * @param multiselect true if the editor is editing multiple properties, otherwise false.
   */
  public void setMultiselectMode(boolean multiselect) {
    multiselectMode = multiselect;
  }

  /**
   * Checks whether the editor is in multiple select mode.
   *
   * @return true if the editor is being used to edit multiple components, otherwise false.
   */
  public boolean inMultiselectMode() {
    return multiselectMode;
  }

  /**
   * Sets whether the property editor is editing multiple components with conflicting values.
   *
   * @param multiple true if the properties are in conflict, otherwise false.
   */
  public void setMultipleValues(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * Checks whether the editor is editing multiple conflicting values.
   *
   * @return true if there is a conflict, otherwise false.
   */
  public boolean isMultipleValues() {
    return multiple;
  }
}
