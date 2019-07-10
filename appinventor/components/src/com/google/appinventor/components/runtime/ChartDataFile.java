package com.google.appinventor.components.runtime;

import android.Manifest;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
        description = "To be updated",
        category = ComponentCategory.CHARTS,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class ChartDataFile extends AndroidViewComponent implements ComponentContainer {

    private String source = "";
    private Chart chartContainer;

    private YailList rows;
    private YailList columns;

    /**
     * Creates a new ChartDataFile component.
     *
     * @param container container, component will be placed in
     */
    public ChartDataFile(Chart container) {
        super(container);
        this.chartContainer = container;

        rows = new YailList();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
    public void Source(String source) {
        // Only set from the designer.
        this.source = source;
        parseCSVFromSource(source);
    }

    private void parseCSVFromSource(final String filename) {
        $form().askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                    try {
                        // Open asset file
                        final InputStream inputStream = $form().openAsset(filename);

                        readCSV(inputStream);

                        // TODO: Run asynchronously
                        // Read from the CSV file asynchronously
//                        AsynchUtil.runAsynchronously(new Runnable() {
//                            @Override
//                            public void run() {
//                                asyncReadCSV(inputStream);
//                            }
//                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    $form().dispatchPermissionDeniedEvent(ChartDataFile.this, "ReadFrom", permission);
                }
            }
        });
    }

    private void readCSV(InputStream inputStream) {
        try {
            // TODO: Taken form File.java. To be replaced to reduce redundancy.
            InputStreamReader input = new InputStreamReader(inputStream);
            StringWriter output = new StringWriter();
            char [] buffer = new char[4096];
            int offset = 0;
            int length = 0;
            while ((length = input.read(buffer, offset, 4096)) > 0) {
                output.write(buffer, 0, length);
            }

            final String result = output.toString().replaceAll("\r\n", "\n");

            // Parse rows from the result
            rows = CsvUtil.fromCsvTable(result);

            // Store columns separately (first row indicates column names)
            columns = (YailList)rows.getObject(0);

            // Import the data to referenced Data components
            // importData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Gets or sets the source URL used to load the Data from.")
    public String Source() {
        return source;
    }

    @SimpleProperty
    public YailList Rows() {
        return rows;
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
