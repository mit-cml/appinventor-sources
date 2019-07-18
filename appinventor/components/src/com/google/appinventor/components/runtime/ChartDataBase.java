package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.Arrays;
import java.util.concurrent.*;

@SimpleObject
public abstract class ChartDataBase implements Component, OnInitializeListener {
    protected Chart container;
    protected ChartDataModel chartDataModel;

    private String label;
    private int color;

    private YailList csvColumns;
    private CSVFile dataSource;
    private ExecutorService threadRunner;

    private boolean initialized = false; // Keep track whether the Screen has already been initialized

    /**
     * Creates a new Chart Data component.
     */
    protected ChartDataBase(Chart chartContainer) {
        this.container = chartContainer;
        chartContainer.addDataComponent(this);
        initChartData();

        threadRunner = Executors.newSingleThreadExecutor();

        container.$form().registerForOnInitialize(this);
    }

    /**
     * Returns the data series color as an alpha-red-green-blue integer.
     *
     * @return  background RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public int Color() {
        return color;
    }

    /**
     * Specifies the data series color as an alpha-red-green-blue integer.
     *
     * @param argb  background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void Color(int argb) {
        color = argb;
        chartDataModel.setColor(color);
        refreshChart();
    }

    /**
     * Returns the label text of the data series.
     *
     * @return  label text
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public String Label() {
        try {
            label = (String) threadRunner.submit(new Callable<Object>() {
               @Override
               public String call() {
                   return chartDataModel.getDataset().getEntryCount() + " entries";
               }
            }).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return label;
    }

    /**
     * Specifies the text for the data series label.
     *
     * @param text  label text
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty
    public void Label(String text) {
        this.label = text;
        chartDataModel.setLabel(text);
        refreshChart();
    }

    /**
     * Specifies the elements of the entries that the Chart should have.
     * @param elements  Comma-separated values of Chart entries alternating between x and y values.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="To be done (non-functional for now)",  category = PropertyCategory.BEHAVIOR)
    public void ElementsFromPairs(String elements) {
        // Base case:  nothing to add
        if (elements.equals("")) {
            return;
        }

        chartDataModel.setElements(elements);
        refreshChart();
    }

    /**
     * Initializes the Chart Data object by setting
     * the default properties and initializing the
     * corresponding ChartDataModel object instance.
     */
    public void initChartData() {
        // Creates a ChartDataModel based on the current
        // Chart type being used.
        chartDataModel = container.createChartModel();

        // Set default values
        Color(Component.COLOR_BLACK);
        Label("");
    }

    /**
     * Adds elements to the Data component from a specified List of tuples.
     *
     * @param list  YailList of tuples.
     */
    @SimpleFunction(description = "Imports data from a list of entries" +
      "Data is not overwritten.")
    public void ImportFromList(YailList list) {
        chartDataModel.importFromList(list);
        refreshChart();
    }

    /**
     * Removes all the entries from the Data Series.
     */
    @SimpleFunction(description = "Clears all of the data.")
    public void Clear() {
        // Run clear entries asynchronously in the queued Thread runner.
        // Queuing ensures that values are cleared only after all the
        // async reading is processed.
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                chartDataModel.clearEntries();
                refreshChart();
            }
        });
    }

    /**
     * Imports data from a CSV file component, with the specified column names.
     * The method is ran asynchronously.
     *
     * @param csvFile  CSV File component to import from
     * @param columns  list of column names to import from
     */
    protected void importFromCSVAsync(final CSVFile csvFile, YailList columns) {
        // Get the Future object representing the columns in the CSVFile component,
        final Future<YailList> csvFileColumns = csvFile.getColumns(columns);

        // Import the data from the CSV file asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                YailList csvResult = null;

                try {
                    // Get the columns from the CSVFile. The retrieval of
                    // the result is blocking, so it will first wait for
                    // the reading to be processed.
                    // The expected format is a (rowCount, columns) List.
                    csvResult = csvFileColumns.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // Undefined behavior (exceptions thrown)
                if (csvResult == null) {
                    return;
                }

                // Get the contents of the result
                final int rowSize = (Integer)csvResult.getObject(0);
                final YailList csvColumns = (YailList)csvResult.getObject(1);

                // Import from CSV file with the specified parameters
                chartDataModel.importFromCSV(csvColumns, rowSize);

                // Refresh the Chart after import
                refreshChart();
            }
        });
    }

    /**
     * Sets the CSV columns to parse data from the CSV source.
     *
     * @param columns  CSV representation of the column names (e.g. A,B will
     *                 use A for the x values, and B for the y values)
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="Sets the columns of the CSV file to parse data from." +
            "The columns must be specified in a CSV format, e.g. A,B will will use " +
            "A for the x values, and B for the y values.",
            category = PropertyCategory.BEHAVIOR,
                userVisible = false)
    public void CsvColumns(String columns) {
        try {
            this.csvColumns = CsvUtil.fromCsvRow(columns);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the Data Source for the Chart data component. The data
     * is then automatically imported.
     *
     * TODO: Modify description to include more data sources
     * TODO: Support for more Data Sources (so not only limited to CSVFile)
     * @param dataSource  Data Source to use for the Chart data.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Sets the Data Source for the Data component. Accepted types " +
                    "include CSVFiles.")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_DATA_SOURCE)
    public void Source(final CSVFile dataSource) {
        this.dataSource = dataSource;

        if (initialized) {
            importFromCSVAsync(dataSource, csvColumns);
        }
    }

    /**
     * Refreshes the Chart view object.
     */
    protected void refreshChart() {
        // To avoid exceptions, refresh the Chart on the UI thread.
        container.$context().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.refresh();
            }
        });
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }

    /**
     * Links the Data Source component with the Data component, if
     * the Source component has been defined earlier.
     *
     * The reason this is done is because otherwise exceptions
     * are thrown if the Data is being imported before the component
     * is fully initialized.
     */
    @Override
    public void onInitialize() {
        initialized = true;

        // Data Source should only be imported after the Screen
        // has been initialized, otherwise some exceptions may occur
        // on small data sets with regards to Chart refreshing.
        if (dataSource != null) {
            Source(dataSource);
        }
    }

}
