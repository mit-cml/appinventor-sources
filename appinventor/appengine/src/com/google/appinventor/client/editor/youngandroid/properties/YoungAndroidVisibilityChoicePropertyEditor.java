// Copyright 2012 MIT. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid.properties;


import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;


/**
 * Property editor for visibility: selects showing or hidden
 * 
 * @author hal@mit.edu (Hal Abelson)
 */
public class YoungAndroidVisibilityChoicePropertyEditor extends ChoicePropertyEditor {

  // TODO(hal): The values must be True and False. Lowercase true and false do not work here:
  // they are not transformed from strings to booleans when passed to the property.
  // Figure out why not and explain in a comment.
  
  private static final Choice[] visibility = new Choice[] {
    new Choice("showing", "True"),
    new Choice("hidden", "False"),
  };

  public YoungAndroidVisibilityChoicePropertyEditor() {
    super(visibility);
  }
}
