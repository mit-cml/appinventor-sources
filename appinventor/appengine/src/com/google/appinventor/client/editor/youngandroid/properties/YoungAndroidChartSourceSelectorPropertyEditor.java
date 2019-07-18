package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

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
        // Keep track of property changes to the SourceFile property. In the case of
        // such a change, the Source should be un-attached (by setting the value to Hone)
        if (property.getValue().equals(component.getName()) // Check that this is the attached component
                && propertyName.equals("SourceFile")) { // Check for SourceFile changes
            property.setValue(""); // Change the Source value to None
        }
    }
}
