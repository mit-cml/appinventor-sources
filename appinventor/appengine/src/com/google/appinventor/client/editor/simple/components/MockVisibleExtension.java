package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
public class MockVisibleExtension extends MockVisibleComponent {

    public MockVisibleExtension(SimpleEditor editor, String type) {
        super(editor, type, new Image(images.extension()));
    }

    public void initComponent(Element component) {
        initComponent(HTMLPanel.wrap(component));
    }

    public String getName() {
        return super.getName();
    }
}
