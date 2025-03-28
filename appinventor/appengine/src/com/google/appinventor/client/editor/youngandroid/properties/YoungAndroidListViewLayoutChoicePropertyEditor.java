// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * ListViewLayout property editor. A dropdown for selecting type of layout.
 */
public class YoungAndroidListViewLayoutChoicePropertyEditor extends ChoicePropertyEditor {
  // ListView layout choices
  private static final Choice[] layout = new Choice[] {
    new Choice(MESSAGES.singleTextLayout(), "0"),
    new Choice(MESSAGES.twoTextLayout(), "1"),
    new Choice(MESSAGES.twoTextLinearLayout(), "2"),
    new Choice(MESSAGES.imageSingleTextLayout(), "3"),
    new Choice(MESSAGES.imageTwoTextLayout(), "4"),
    new Choice(MESSAGES.imageTopTwoTextLayout(), "5")

  };

  public YoungAndroidListViewLayoutChoicePropertyEditor() {
        super(layout);
    }
}
