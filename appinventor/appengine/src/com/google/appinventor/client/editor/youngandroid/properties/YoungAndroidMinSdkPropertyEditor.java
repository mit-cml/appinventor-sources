package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

public class YoungAndroidMinSdkPropertyEditor extends ChoicePropertyEditor {
    public YoungAndroidMinSdkPropertyEditor() {
        super(buildChoices());
    }
    private static Choice[] buildChoices() {
        int min = ComponentConstants.APP_INVENTOR_MIN_SDK;
        int max = YaVersion.TARGET_SDK_VERSION;

        Choice[] choices = new Choice[max - min + 1];

        for (int i = min; i <= max; i++) {
            choices[i - min] = new Choice(String.valueOf(i), String.valueOf(i));
        }
        return choices;
    }
}