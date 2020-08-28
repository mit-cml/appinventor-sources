package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock SidebarHeader component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public class MockSidebarHeader extends MockHVArrangement {
    public static final String TYPE = "SidebarHeader";

    /**
     * Creates a new MockHVArrangement component.
     *
     * @param editor
     */
   public MockSidebarHeader(SimpleEditor editor) {
        super(editor, TYPE, images.sidebarHeader(), ComponentConstants.LAYOUT_ORIENTATION_VERTICAL, ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
   }

    @Override
    public boolean isSidebarHeader() {
        return true;
    }
}
