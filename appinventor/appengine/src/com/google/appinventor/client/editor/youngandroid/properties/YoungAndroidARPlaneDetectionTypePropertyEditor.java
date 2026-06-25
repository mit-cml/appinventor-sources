// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the AR configuration type set for the ARView3D component.
 *
 * @author niclarke@mit.edu (Nichole Clarke)
 */
public class YoungAndroidARPlaneDetectionTypePropertyEditor extends ChoicePropertyEditor {

    private static final Choice[] detectionTypes = new Choice[] {
            new Choice(MESSAGES.arPlaneDetectionTypeNone(), "1"),
            new Choice(MESSAGES.arPlaneDetectionTypeHorizontal(), "2"),
            new Choice(MESSAGES.arPlaneDetectionTypeVertical(), "3"),
            new Choice(MESSAGES.arPlaneDetectionTypeBoth(), "4")
    };

    public YoungAndroidARPlaneDetectionTypePropertyEditor() {
        super(detectionTypes);
    }
}
