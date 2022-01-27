// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for background image repeat mode.
 * 
 * @author devarshmavani19@gmail.com (Devarsh Mavani)
 */
public class YoungAndroidBackgroundImageRepeatModePropertyEditor extends ChoicePropertyEditor {
    public static final Choice[] repeatModes = new Choice[] {
        new Choice(MESSAGES.backgroundImageRepeatModeNone(), "0"),
        new Choice(MESSAGES.backgroundImageRepeatModeXY(), "1"),
    };

    public YoungAndroidBackgroundImageRepeatModePropertyEditor() {
        super(repeatModes);
    }

}