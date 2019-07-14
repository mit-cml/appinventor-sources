package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.output.OdeLog;

import java.util.Set;

public class YoungAndroidChartSourceSelectorPropertyEditor extends YoungAndroidComponentSelectorPropertyEditor {
    public YoungAndroidChartSourceSelectorPropertyEditor(YaFormEditor editor) {
        super(editor);
    }

    public YoungAndroidChartSourceSelectorPropertyEditor(YaFormEditor editor, Set<String> componentTypes) {
        super(editor, componentTypes);
    }

    @Override
    public void onComponentPropertyChanged(MockComponent component,
                                           String propertyName, String propertyValue) {
        if (property.getValue().equals(component.getName()) && propertyName.equals("SourceFile")) {
            property.setValue("");
        }
    }
}
