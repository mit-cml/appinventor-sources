package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.Icon;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class MockVisibleExtension extends MockVisibleComponent {

    private Icon iconWidget;

    public MockVisibleExtension(SimpleEditor editor, String type) {
        this(editor, type, new Image(images.extension()));
    }

    public MockVisibleExtension(SimpleEditor editor, String type, Image icon) {
        super(editor, type, icon);
        iconWidget = new Icon(icon);

        initComponent(iconWidget);
    }
}
