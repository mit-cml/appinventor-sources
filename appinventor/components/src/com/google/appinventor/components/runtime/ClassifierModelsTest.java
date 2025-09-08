package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
        description = "A component to take a picture using the device's camera. " +
                "After the picture is taken, the name of the file on the phone " +
                "containing the picture is available as an argument to the " +
                "AfterPicture event. The file name can be used, for example, to set " +
                "the Picture property of an Image component.",
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
