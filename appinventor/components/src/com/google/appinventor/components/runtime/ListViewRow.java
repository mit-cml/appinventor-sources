package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.LISTVIEWROW_COMPONENT_VERSION,
        description = "Component to build custom layout for each row of ListView.",
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class ListViewRow extends HVArrangement {

//    private int orientation;

    public ListViewRow (ComponentContainer container) {
        // need to change orientation parameter on the basis of value from ListView component
        super(container, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
                ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
//        ListViewRowOrientation(ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL);
    }

//    /**
//     * Returns type of orientation for the contents of the ListViewRow
//     *
//     * @return orientation as integer value
//     */
//    @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
//    public int ListViewRowOrientation() {
//        return orientation;
//    }
//
//    /**
//     * Specifies type of orientation for contents of a ListViewRow.
//     *
//     * @param value integer value to determine type of ListView layout
//     */
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEWROW_ORIENTATION,
//            defaultValue = Component.LAYOUT_ORIENTATION_HORIZONTAL+"")
//    @SimpleProperty(userVisible = true)
//    public void ListViewRowOrientation(int value) {
//        orientation = value;
//        setOrientation();
//    }
}
