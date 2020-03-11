package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

public class MockListViewRow extends MockHVArrangement {

    public static final String TYPE = "ListViewRow";
//    private int orientation;

    /**
     * Creates a new MockHVArrangement component.
     *
     * @param editor
     */
    public MockListViewRow(SimpleEditor editor) {
        super(editor, TYPE, images.horizontal(),
                ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
                ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
    }

//    private void setOrientation(String value) {
//        orientation = Integer.parseInt(value);
//        super.orientation = this.orientation;
//    }
//
//    @Override
//    public void onPropertyChange(String propertyName, String newValue) {
//        super.onPropertyChange(propertyName, newValue);
//        if(propertyName.equals(PROPERTY_NAME_LISTVIEWROW_ORIENTATION)) {
//            setOrientation(newValue);
//            refreshForm();
//        }
//    }
}
