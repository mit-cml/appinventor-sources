package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

public class YoungAndroidChartLabelValueTypeChoicePropertyEditor extends ChoicePropertyEditor {
    private static final Choice[] valueTypes = new Choice[] {
            new Choice(MESSAGES.labelDecimal(), "0"),
            new Choice(MESSAGES.labelInteger(), "1"),
            new Choice(MESSAGES.labelDate(), "2"),
            new Choice(MESSAGES.labelTime(), "3")
    };


    public YoungAndroidChartLabelValueTypeChoicePropertyEditor() {
        super(valueTypes);
    }
}
