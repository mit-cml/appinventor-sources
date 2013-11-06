// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components.utils;

import com.google.appinventor.client.editor.simple.components.MockForm;
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
 * Provides utility functions for dealing with the UI for component properties
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
  // getExstingPropertyEditor below
  
  // retrieve the property editor for Horizontal Alignment
  public static  YoungAndroidHorizontalAlignmentChoicePropertyEditor
  getHAlignmentEditor(EditableProperties properties) 
      throws BadPropertyEditorException {

    PropertyEditor propEditor= null;
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_HORIZONTAL_ALIGNMENT);
    if (! (prop == null)) {
      if (prop instanceof EditableProperty) {
        propEditor = ((EditableProperty) prop).getEditor();
        if (! (propEditor == null)) {
          if (! (propEditor instanceof YoungAndroidHorizontalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }
    return (YoungAndroidHorizontalAlignmentChoicePropertyEditor)propEditor;
  }

  // retrieve the property editor for Vertical Alignment
  public static  YoungAndroidVerticalAlignmentChoicePropertyEditor
  getVAlignmentEditor(EditableProperties properties) 
      throws BadPropertyEditorException {

    PropertyEditor propEditor= null;
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_VERTICAL_ALIGNMENT);
    if (! (prop == null)) {
      if (prop instanceof EditableProperty) {
        propEditor = ((EditableProperty) prop).getEditor();
        if (! (propEditor == null)) {
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
