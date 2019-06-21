package com.google.appinventor.components.runtime;

import android.os.Environment;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import android.Manifest;

@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
        description = "To be updated",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class CSVFile extends AndroidNonvisibleComponent {

    private YailList rows;
    private String sourceFile;

    /**
     * Creates a new CSVFile component.
     *
     * @param form the container that this component will be placed in
     */
    public CSVFile(Form form) {
        super(form);

        rows = new YailList();
    }

    private void parseCSVFromSource(final String filename) {
        form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                    try {
                        // TODO: Establish path properly (like in File class)
                        String path = Environment.getExternalStorageDirectory().getPath() + filename;;

                        byte[] bytes = FileUtil.readFile(path);
                        String csvTable = new String(bytes);

                        rows = CsvUtil.fromCsvTable(csvTable);
                    } catch (IOException e) {
                        e.printStackTrace();
                        rows = YailList.makeList(Collections.singletonList(e.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        rows = YailList.makeList(Collections.singletonList(e.getMessage()));
                    }
                } else {
                    form.dispatchPermissionDeniedEvent(CSVFile.this, "ReadFrom", permission);
                }
            }
        });
    }

    /**
     * Rows property getter method
     *
     * @return a YailList representing the list of strings to be picked from
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Elements() {
        return rows;
    }

    /**
     * Sets the source file to parse CSV from.
     *
     * @param source  Source file name
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public void SourceFile(String source) {
        this.sourceFile = source;

        // Parse CSV after setting source
        // TODO: Do not use in setter (?)
        // TODO: Convert to async?
        parseCSVFromSource(sourceFile);
    }
}
