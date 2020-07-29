package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

public class MockSidebarHeader extends MockHVArrangement {
    public static final String TYPE = "SidebarHeader";

    /**
     * Creates a new MockHVArrangement component.
     *
     * @param editor
     */
   public MockSidebarHeader(SimpleEditor editor) {
        super(editor, TYPE, images.sidebar(), ComponentConstants.LAYOUT_ORIENTATION_VERTICAL, ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
        finishConfiguration();
   }

    @Override
    public boolean isSidebarHeader() {
        return true;
    }
}
