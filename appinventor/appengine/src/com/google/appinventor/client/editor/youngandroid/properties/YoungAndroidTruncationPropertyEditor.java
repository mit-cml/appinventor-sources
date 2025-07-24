// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the truction type set for the TextNode component.
 *
 * @author niclarke@mit.edu (Nichole Clarke)
 */
public class YoungAndroidTruncationPropertyEditor extends ChoicePropertyEditor {

    private static final Choice[] truncationTypes = new Choice[] {
            new Choice(MESSAGES.truncationTypeNone(), "1"),
            new Choice(MESSAGES.truncationTypeEnd(), "2"),
            new Choice(MESSAGES.truncationTypeMiddle(), "3"),
            new Choice(MESSAGES.truncationTypeStart(), "4")
    };

    public YoungAndroidTruncationPropertyEditor() {
        super(truncationTypes);
    }
}
