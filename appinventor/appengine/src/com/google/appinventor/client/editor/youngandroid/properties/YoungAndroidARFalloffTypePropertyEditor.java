// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the AR falloff type set for the ARLight components.
 *
 * @author niclarke@mit.edu (Nichole Clarke)
 */
public class YoungAndroidARFalloffTypePropertyEditor extends ChoicePropertyEditor {

    private static final Choice[] falloffTypes = new Choice[] {
            new Choice(MESSAGES.arFalloffTypeNone(), "0"),
            new Choice(MESSAGES.arFalloffTypeLinear(), "1"),
            new Choice(MESSAGES.arFalloffTypeQuadratic(), "2")
    };

    public YoungAndroidARFalloffTypePropertyEditor() {
        super(falloffTypes);
    }
}
