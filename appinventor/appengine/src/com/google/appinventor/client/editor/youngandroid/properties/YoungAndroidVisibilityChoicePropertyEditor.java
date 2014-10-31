// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;


import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;


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
    new Choice(MESSAGES.VisibilityShowingProperties(), "True"),
    new Choice(MESSAGES.VisibilityHiddenProperties(), "False"),
  };

  public YoungAndroidVisibilityChoicePropertyEditor() {
    super(visibility);
  }
}
