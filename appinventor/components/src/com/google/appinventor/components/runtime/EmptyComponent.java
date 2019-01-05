package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/*
Jacob Bashista 1/5/19

EmptyComponent is a component designed to contain no content
and act as a base for other components to be built off.
*/


@DesignerComponent(version = 1,
        description = "Empty Extension Component",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
public class EmptyComponent extends AndroidNonvisibleComponent
        implements Component {

    private final ComponentContainer container;

    public EmptyComponent(ComponentContainer container){
        super(container.$form());
        this.container = container;
    }
}
