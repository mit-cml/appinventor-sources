package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = 1,
        description = "Testing component for creating property editor for transfer learning ml models",
        category = ComponentCategory.USERINTERFACE,
        nonVisible = true,
        iconName = "images/camera.png")
@SimpleObject
public class ClassifierModelsTest extends AndroidNonvisibleComponent {
    public ClassifierModelsTest(ComponentContainer container) {
        super(container.$form());
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_IMAGE_CLASSIFIER)
    @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
    public void Model(String model) {

    }
}
