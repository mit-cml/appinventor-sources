// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

public class YoungAndroidFileScopePropertyEditor extends ChoicePropertyEditor {
  private static final Choice[] CHOICES = new Choice[] {
      new Choice(MESSAGES.fileScopeApp(), "App"),
      new Choice(MESSAGES.fileScopeAsset(), "Asset"),
      new Choice(MESSAGES.fileScopeCache(), "Cache"),
      new Choice(MESSAGES.fileScopeLegacy(), "Legacy"),
      new Choice(MESSAGES.fileScopePrivate(), "Private"),
      new Choice(MESSAGES.fileScopeShared(), "Shared")
  };

  public YoungAndroidFileScopePropertyEditor() {
    super(CHOICES);
  }
}
