// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components.utils;

import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.editor.
       youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.
       YoungAndroidVerticalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.properties.Property;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;

/**
 * Provides utility functions for dealing with the UI for component properties.
 *
 * These functions are called by descendants of MockComponent with its protected
 * field "properties" as an argument. This EditableProperties object is populated
 * by {@link YoungAndroidPalettePanel#configureComponent(MockComponent)}, which
 * calls {@link MockComponent#addProperty(String, String, String, PropertyEditor)}
 * for each of the MockComponent's properties. The addProperty method in turn calls
 * {@link EditableProperties#addProperty(String, String, String, PropertyEditor, int)},
 * adding the property to "properties".
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class PropertiesUtil {

  /**
   * Prevent instantiation.
   */
  private PropertiesUtil() {
  }

  // Use individual methods for each property since we can't write the generic
  // getExstingPropertyEditor due to type safety issues - see below.

  /**
   * Retrieves the property editor for Horizontal Alignment.
   *
   * @param  properties
   * @return the property editor for Horizontal Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidHorizontalAlignmentChoicePropertyEditor
  getHAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;

    // Get the property with the specified name from the properties
    // object. This works because an EditableProperties contains a
    // TreeMap<String, T extends Property> that maps a property's name to its instance.
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_HORIZONTAL_ALIGNMENT);

    if (prop != null) {
      if (prop instanceof EditableProperty) {
        // Cast prop to an EditableProperty and get its editor
        propEditor = ((EditableProperty) prop).getEditor();

        // Make sure we have the right editor
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidHorizontalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }

    // Cast propEditor to the appropriate type
    return (YoungAndroidHorizontalAlignmentChoicePropertyEditor)propEditor;
  }

  /**
   * Retrieves the property editor for Vertical Alignment.
   * Note that this is almost identical to {@link #getHAlignmentEditor(EditableProperties)}
   * in terms of implementation.
   *
   * @param  properties
   * @return the property editor for Vertical Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidVerticalAlignmentChoicePropertyEditor
  getVAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_VERTICAL_ALIGNMENT);
    if (prop != null) {
      if (prop instanceof EditableProperty) {
        propEditor = ((EditableProperty) prop).getEditor();
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidVerticalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }
    return (YoungAndroidVerticalAlignmentChoicePropertyEditor)propEditor;
  }

  // TODO(hal):  The individual property methods above should be replaced by a single generic
  // method getExistingPropertyEditor that
  // retrieves the property editor for the named property and checks its type.  Here's how the
  // code should go.  While this compiles in Java, it sadly
  // does not compile in GWT, which has not yet implemented type-safe generic casting.
  // Sigh.  Well, maybe someday.

// /**
//  * Returns the existing property editor for the given name.
//  *
//  * @throws IllegalStateException  if no such property editor exists, or if
//  * the editor does not have the required class
//  * @param name  property name
//  * @param C the required class of class of the property editor
//  */

// public <T extends PropertyEditor>T getExistingPropertyEditor(String name, Class<T> C)
//     throws BadPropertyEditorException {
//   PropertyEditor propEditor= null;
//   Property prop = getProperty(name);
//   if (! (prop == null)) {
//     if (prop instanceof EditableProperty) {
//       propEditor = ((EditableProperty) prop).getEditor();
//       if (! (propEditor == null)) {
//         if (! (C.isAssignableFrom(propEditor.getClass()))) {
//           throw new BadPropertyEditorException("Bad property editor");
//         }
//       }
//     }
//   }
//   return C.cast(propEditor);
// }


}
