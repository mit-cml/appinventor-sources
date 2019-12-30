package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;

public class MockVisibleExtension extends MockVisibleComponent {

    public MockVisibleExtension(SimpleEditor editor, String type) {
        this(editor, type, images.extension());
    }

    public MockVisibleExtension(SimpleEditor editor, String type, ImageResource icon) {
        super(editor, type, icon);
    }
}
