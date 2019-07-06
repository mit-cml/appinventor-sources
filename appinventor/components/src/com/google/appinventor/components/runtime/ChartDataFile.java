package com.google.appinventor.components.runtime;

import android.app.Activity;
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
public class ChartDataFile extends AndroidViewComponent implements ComponentContainer {

    private String source = "";
    private ChartBase chartContainer;

    /**
     * Creates a new ChartDataFile component.
     *
     * @param container container, component will be placed in
     */
    public ChartDataFile(ChartBase container) {
        super(container);
        this.chartContainer = container;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_GEOJSON_TYPE)
    public void Source(String source) {
        // Only set from the designer.
        this.source = source;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Gets or sets the source URL used to load the Data from.")
    public String Source() {
        return source;
    }

    public ChartBase getChartContainer() {
        return chartContainer;
    }

    @Override
    public Activity $context() {
        return chartContainer.$context();
    }

    @Override
    public Form $form() {
        return chartContainer.$form();
    }

    @Override
    public void $add(AndroidViewComponent component) {
        throw new UnsupportedOperationException("ChartBase.$add() called");
    }

    @Override
    public void setChildWidth(AndroidViewComponent component, int width) {
        throw new UnsupportedOperationException("ChartBase.setChildWidth called");
    }

    @Override
    public void setChildHeight(AndroidViewComponent component, int height) {
        throw new UnsupportedOperationException("ChartBase.setChildHeight called");
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public int Width() {
        return 0;
    }

    @Override
    public int Height() {
        return 0;
    }
}
