package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidListViewRowOrientationChoicePropertyEditor extends ChoicePropertyEditor {

    private static final Choice[] layout = new Choice[] {
            new Choice(MESSAGES.horizontalOrientation(), "0"),
            new Choice(MESSAGES.verticalOrientation(), "1")
    };

    public YoungAndroidListViewRowOrientationChoicePropertyEditor() {
        super(layout);
    }
}
