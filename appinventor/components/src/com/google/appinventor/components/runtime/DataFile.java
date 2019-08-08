package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@DesignerComponent(version = 1,
        description = "To be updated",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class DataFile extends FileBase implements ChartDataSource<YailList, Future<YailList>> {
    private String sourceFile;

    private YailList rows;
    private YailList columns;
    private YailList columnNames; // Elements of the first column

    private ExecutorService threadRunner; // Used to queue & execute asynchronous tasks

    /**
     * Creates a new DataFile component.
     * @param container the Form that this component is contained in.
     */
    public DataFile(ComponentContainer container) {
        super(container);

        rows = new YailList();
        columns = new YailList();
        columnNames = new YailList();

        threadRunner = Executors.newSingleThreadExecutor();
    }

    /**
     * Rows property getter method
     *
     * @return a YailList representing the parsed rows of the Data file.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns a list of rows corresponding" +
            " to the Data File's content.")
    public YailList Rows() {
        return getYailListPropertyHelper(new Callable<YailList>() {
            @Override
            public YailList call() throws Exception {
                return rows;
            }
        });
    }


    /**
     * Columns property getter method
     *
     * @return a YailList representing the parsed columns of the Data file.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns a list of columns corresponding" +
            " to the Data File's content.")
    public YailList Columns() {
        return getYailListPropertyHelper(new Callable<YailList>() {
            @Override
            public YailList call() throws Exception {
                return columns;
            }
        });
    }

    /**
     * Column names property getter method.
     * The intended use case of the method is for Data Files which contain
     * the column names in the first row.
     *
     * @return  a YailList containing the elements of the first row.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the elements of the first row" +
            " of the Data File's contents.")
    public YailList ColumnNames() {
        return getYailListPropertyHelper(new Callable<YailList>() {
            @Override
            public YailList call() throws Exception {
                return columnNames;
            }
        });
    }

    /**
     * Helper method for retrieving YailList properties using blocking
     * calls. Used for Rows, Columns and ColumnNames properties.
     *
     * The property to return has to be wrapped in a Callable and passed
     * to this method in order to return the updated variable (post-reading).
     * Passing in the property would not work as expected because the
     * variable would have to be final.
     *
     * @param propertyCallable  Callable that returns the required YailList property
     * @return  YailList property
     */
    private YailList getYailListPropertyHelper(Callable<YailList> propertyCallable) {
        // Since reading might be in progress, the task of
        // getting a DataFile property should be queued so that the
        // thread is blocked until the reading is finished.
        try {
            return  threadRunner
                    .submit(propertyCallable) // Run the callable async (and queued)
                    .get(); // Get the property (blocks thread until previous threads finish)
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new YailList(); // Return empty list (default option)
    }

    /**
     * Sets the source file to parse data from, and then parses the
     * file asynchronously.
     * The results are stored in the Columns, Rows and ColumnNames properties.
     *
     * @param source  Source file name
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
        defaultValue = "")
    public void SourceFile(String source) {
        // The SourceFile property is only set from the Designer, so the
        // only possibility is a media file. Since media file paths are
        // distinguished by double slashes at the beginning, they need
        // to be added.
        ReadFile("//" + source);
    }

    @SimpleFunction(description = "Indicates source file to load data from." +
        "The expected format of the contents of the file are either CSV or JSON." +
        "Prefix the filename with / to read from a specific file on the SD card. " +
        "for instance /myFile.txt will read the file /sdcard/myFile.txt. To read " +
        "assets packaged with an application (also works for the Companion) start " +
        "the filename with // (two slashes). If a filename does not start with a " +
        "slash, it will be read from the applications private storage (for packaged " +
        "apps) and from /sdcard/AppInventor/data for the Companion." +
        "The results of the reading are stored in the Rows, Columns " +
        "and ColumnNames properties of the component.")
    public void ReadFile(String source) {
        this.sourceFile = source;

        readFromFile(sourceFile);
    }

    /**
     * Gets the specified column's elements as a YailList.
     *
     * @param column  name of column
     * @return YailList of elements in the column
     */
    public YailList getColumn(String column) {
        // Get the index of the column (first row - column names)
        // 1 is subtracted from the index since YailList indexOf
        // returns an index that is 1-based.
        int index = columnNames.indexOf(column) - 1;

        // Column not found
        if (index < 0) {
            return new YailList();
        }

        return (YailList)columns.getObject(index);
    }

    @Override
    protected void AsyncRead(final InputStream inputStream, final String fileName) {
        // Add runnable to the Single Thread runner to read File asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Parse InputStream to String
                    final String result = readFromInputStream(inputStream);

                    // First character is a curly bracket; Assume JSON
                    // TODO: When fetching columns and rows, in the case of
                    // TODO: colums/rows being uneven lengths, the final rows and columns
                    // TODO: objects will differ (the transpose will fill missing entries
                    // TODO: with blank empty String entries, while the original List will
                    // TODO: have uneven sized Lists. For consistency, this should be
                    // TODO: handled, but currently there is a bit too much overhead in doing
                    // TODO: so due to YailLists not supporting the add() operation)
                    if (result.charAt(0) == '{') {
                        // Parse columns from the result
                        columns = JsonUtil.getColumnsFromJSON(result);

                        // Construct row lists from columns
                        rows = ChartDataSourceUtil.getTranspose(columns);
                    } else { // Assume CSV otherwise
                        // Parse rows from the result
                        rows = CsvUtil.fromCsvTable(result);

                        // Construct column lists from rows
                        columns = ChartDataSourceUtil.getTranspose(rows);
                    }

                    // If rows size is non-zero, set column names to first row. Otherwise,
                    // set it to an empty List.
                    columnNames = (rows.size() > 0) ? ((YailList)rows.getObject(0)) : new YailList();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns a Future object which holds the DataFile columns at the point
     * of invoking the method.
     *
     * If reading is in progress, the method blocks until reading is done
     * before returning the result.
     *
     * The method should be called asynchronously to prevent freezing of
     * the main thread.
     *
     * The row size is contained in the method to create default values for the
     * CharDataModel in case of an absence of columns.
     *
     * @param columns  List of columns to retrieve (String object entries expected)
     * @return  Future object containing YailList of format (rowCount, columns)
     */
    @Override
    public Future<YailList> getDataValue(final YailList columns) {
        // Submit a callable which constructs the results.
        // The callable is only executed after all the previous
        // tasks have been completed.
        return threadRunner.submit(new Callable<YailList>() {
            @Override
            public YailList call() {
                ArrayList<YailList> resultingColumns = new ArrayList<YailList>();

                // Iterate over the specified column names
                for (int i = 0; i < columns.size(); ++i) {
                    // Get and add the specified column to the resulting columns list
                    String columnName = columns.getString(i);
                    YailList column = getColumn(columnName);
                    resultingColumns.add(column);
                }

                // Convert result to a YailList and return it
                YailList csvColumns = YailList.makeList(resultingColumns);
                return csvColumns;
            }
        });
    }
}
