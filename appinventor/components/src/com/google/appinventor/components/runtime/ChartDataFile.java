package com.google.appinventor.components.runtime;

import android.view.View;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
        description = "To be updated",
        category = ComponentCategory.CHARTS,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class ChartDataFile implements Component {

    private String source = "";
    private ChartBase chartContainer;

    /**
     * Creates a new ChartDataFile component.
     *
     * @param container container, component will be placed in
     */
    public ChartDataFile(ChartBase container) {
        this.chartContainer = container;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    public void Source(String source) {
        // Only set from the designer.
        this.source = source;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Gets or sets the source URL used to load the Data from.")
    public String Source() {
        return source;
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return chartContainer.$form();
    }
}
