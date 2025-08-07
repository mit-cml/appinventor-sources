package com.google.appinventor.components.runtime.client;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(
    version = YaVersion.TEXTBOX_COMPONENT_VERSION,
    description = "A box for the user to enter text.",
    category = ComponentCategory.USERINTERFACE,
    iconName = "images/textbox.png")
@SimpleObject
public final class TextBox extends TextBoxBase {

    public TextBox(ComponentContainer container) {
        super(container, new com.google.gwt.user.client.ui.TextBox());
    }
}
