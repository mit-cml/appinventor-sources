// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.properties.Property;

/**
 * Property for display in a {@link PropertiesPanel}.
 *
 * <p>Such properties have an associated {@link PropertyEditor},
 * hence the name of this class.
 *
 */
public final class EditableProperty extends Property {

  /**
   * Properties of this type will be persisted to the source file upon saving
   * and will be displayed in the properties panel.
   */
  public static final int TYPE_NORMAL = 0;

  /**
   * Properties of this type will not be persisted to the source file upon saving.
   */
  public static final int TYPE_NONPERSISTED = 0x01;

  /**
   * Properties of this type will not be displayed in the properties panel.
   */
  public static final int TYPE_INVISIBLE = 0x02;

  /**
   * Properties of this type will be included in a yail generation
   * even if they are not persisted. This is used for computed properties
   * which aren't stored, but which must be passed to a component both
   * in the Companion and when built. Example: The FirebaseToken and
   * deveoperId which are computed on the fly by MockFirebaseDB but
   * which are not persisted for security reasons. Yet we still need
   * to output them in the Yail.
   */

  public static final int TYPE_DOYAIL = 0x04;

  // EditableProperties that contains this EditableProperty
  private final EditableProperties properties;

  // Type of property
  private int type;

  // Property editor for use in properties panel
  private final PropertyEditor editor;

  // Property caption for use in properties panel
  private final String caption;

  // Editor type of the property (for creating new editors)
  private final String editorType;

  // Additional arguments for the editor
  private final String[] editorArgs;

  // Property description for use in properties panel help dialogs
  private final String description;

  /**
   * Creates a new property.
   *
   * @param properties  the EditableProperties that contains this
   *                    EditableProperty
   * @param name  property's name
   * @param defaultValue property's default value
   *                     (will also be its initial current value)
   * @param caption  property's caption for use in the UI
   * @param editor  property editor
   * @param type  type of property; see {@code TYPE_*} constants
   */
  public EditableProperty(EditableProperties properties, String name, String defaultValue,
      String caption, String category, String description, PropertyEditor editor, int type,
      String editorType, String[] editorArgs) {
    super(name, defaultValue, category);

    this.properties = properties;
    this.type = type;
    this.editor = editor;
    this.caption = caption;
    this.editorType = editorType;
    this.editorArgs = editorArgs;
    this.description = description;

    editor.setProperty(this);
  }

  public EditableProperty(EditableProperties properties, String name, String defaultValue,
      int type) {
    this(properties, name, defaultValue, name, null, null,
        new TextPropertyEditor(), type, "", null);
  }

  /**
   * Creates a new property.
   *
   * @param properties  the EditableProperties that contains this
   *                    EditableProperty
   * @param name  property's name (also used as the caption in the UI)
   * @param defaultValue property's default value
   *                     (will also be its initial current value)
   * @param type  type of property; see {@code TYPE_*} constants
   */
  public EditableProperty(EditableProperties properties, String name, String defaultValue,
      String category, int type, String editorType, String[] editorArgs) {
    this(properties, name, defaultValue, name, category, null, new TextPropertyEditor(), type, editorType, editorArgs);
  }

  /**
   * Returns whether this property is persisted.
   */
  @Override
  protected final boolean isPersisted() {
    return (type & TYPE_NONPERSISTED) == 0;
  }

  /**
   * Returns whether this property is displayed in the properties panel.
   */
  final boolean isVisible() {
    return (type & TYPE_INVISIBLE) == 0;
  }

  @Override
  protected final boolean isYail() {
    return (type & TYPE_DOYAIL) != 0;
  }

  /**
   * Sets the value of the property, optionally forcing the property to reset its value.
   *
   * @param value the value to set
   * @param force true if the property change should be forced, otherwise false
   * @see #setValue(String)
   */
  public void setValue(String value, boolean force) {
    if (!value.equals(getValue()) || force) {
      super.setValue(value);
      if (properties != null) {
        properties.firePropertyChangeEvent(getName(), value);
      }
      editor.updateValue();
    }
  }

  /**
   * {@inheritDoc}
   *
   * Also notifies any property listeners of the change.
   */
  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  /**
   * Returns the property editor widget for this property.
   *
   * @return  property editor
   */
  public final PropertyEditor getEditor() {
    return editor;
  }

  /**
   * Returns the caption for this property.
   *
   * @return  property caption
   */
  public String getCaption() {
    return caption;
  }

  public String getEditorType() {
    return editorType;
  }

  public String[] getEditorArgs() {
    return editorArgs;
  }


  /**
   * Called when this property is being orphaned.
   */
  final void orphan() {
    editor.orphan();
  }

  public int getType() {
    return type;
  }

  public void setType(int aType) {
    this.type = aType;
  }

  public String getDescription() {
    return description;
  }

  public EditableProperties getEditableProperties() {
    return properties;
  }
}
