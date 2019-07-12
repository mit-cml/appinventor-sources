package com.google.appinventor.components.runtime;

import android.os.Environment;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.*;
import java.util.*;

import android.Manifest;

@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
        description = "To be updated",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class CSVFile extends AndroidNonvisibleComponent {

    private String sourceFile = "";
    private YailList rows;
    private YailList columns;
    private YailList columnNames;

    private boolean readingDone = false;

    private ArrayList<ChartDataBase> dataComponents;

    /**
     * Creates a new CSVFile component.
     *
     * @param form the container that this component will be placed in
     */
    public CSVFile(Form form) {
        super(form);

        rows = new YailList();
        columns = new YailList();
        dataComponents = new ArrayList<ChartDataBase>();
    }

    // Reads from stored file. To be integrated
//    private void parseCSVFromSource(final String filename) {
//        form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
//            @Override
//            public void HandlePermissionResponse(String permission, boolean granted) {
//                if (granted) {
//                    try {
//                        String path = Environment.getExternalStorageDirectory().getPath() + filename;;
//
//                        byte[] bytes = FileUtil.readFile(path);
//                        String csvTable = new String(bytes);
//
//                        rows = CsvUtil.fromCsvTable(csvTable);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        rows = YailList.makeList(Collections.singletonList(e.getMessage()));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        rows = YailList.makeList(Collections.singletonList(e.getMessage()));
//                    }
//                } else {
//                    form.dispatchPermissionDeniedEvent(CSVFile.this, "ReadFrom", permission);
//                }
//            }
//        });
//    }

    private void parseCSVFromSource(final String filename) {
        form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                    try {
                        // TODO: Establish path properly (like in File class)

                        // Open asset file
                        final InputStream inputStream = form.openAsset(filename);

                        AsynchUtil.runAsynchronously(new Runnable() {
                            @Override
                            public void run() {
                                readCSV(inputStream);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    form.dispatchPermissionDeniedEvent(CSVFile.this, "ReadFrom", permission);
                }
            }
        });
    }

    private void readCSV(InputStream inputStream) {
        readingDone = false;
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

            // Construct column lists from rows
            constructColumnsFromRows();

            // TODO: Notify data reading done (for async race condition)
            readingDone = true;

            for (ChartDataBase dataComponent : dataComponents) {
                dataComponent.importFromCSVAsync();
            }

            dataComponents.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Rows property getter method
     *
     * @return a YailList representing the list of strings to be picked from
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Rows() {
        return rows;
    }


    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Columns() {
        return columns;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList ColumnNames() {
        return columnNames;
    }

    /**
     * Sets the source file to parse CSV from.
     *
     * @param source  Source file name
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
    public void SourceFile(String source) {
        this.sourceFile = source;

        // Parse CSV after setting source
        parseCSVFromSource(sourceFile);
    }

    public boolean isReadingDone() {
        return readingDone;
    }

    public void importDataComponent(ChartDataBase dataComponent) {
        if (isReadingDone()) {
            dataComponent.importFromCSV();
        } else {
            dataComponents.add(dataComponent);
        }
    }

    /**
     * Gets the specified column's elements as a YailList.
     *
     * @param column  name of column
     * @return YailList of elements in the column
     */
    public YailList getColumn(String column) {
        // Get the index of the column (first row - column names)
        int index = columnNames.indexOf(column) - 1;

        // Column not found
        if (index < 0) {
            return null;
        }

        return (YailList)columns.getObject(index);
    }

    private void constructColumnsFromRows() {
        // Store columns separately (first row indicates column names)
        columnNames = (YailList)rows.getObject(0);

        int rowSize = columnNames.size();

        ArrayList<YailList> columnList = new ArrayList<YailList>();

        for (int i = 0; i < rowSize; ++i) {
            columnList.add(getColumn(i));
        }

        columns = YailList.makeList(columnList);
    }

    private YailList getColumn(int index) {
        List<String> entries = new ArrayList<String>();

        for (int i = 0; i < rows.size(); ++i) {
            // Get row
            YailList row = (YailList) rows.getObject(i); // Safe cast

            // index-th entry in the row is the required column value
            entries.add((row.getString(index)));
        }

        return YailList.makeList(entries);
    }
}
