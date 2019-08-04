package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@SimpleObject
public abstract class ChartDataBase implements Component, OnInitializeListener, ChartDataSourceChangeListener,
    ChartDataSourceGetValueListener {
    protected Chart container;
    protected ChartDataModel chartDataModel;

    /* Used to queue & execute asynchronous tasks while ensuring
     * order of method execution (ExecutorService should be a Single Thread runner)
     * In the case of methods which return values and where
     * the result depends on the state of the data, blocking get
     * calls are used to ensure that all the previous async tasks
     * finish before the data is returned. */
    protected ExecutorService threadRunner;

    // Properties used in Designer to import from CSV.
    // Represents the names of the columns to use,
    // where each index corresponds to a single dimension.
    protected List<String> csvColumns;

    // Property used in Designer to import from a Data Source.
    // Represents the key value of the value to use from the
    // attached Data Source.
    protected String dataSourceValue;

    private String label;
    private int color;

    private ChartDataSource dataSource; // Attached Chart Data Source

    // Currently imported observed Data Source value. This has to be
    // kept track of in order to remove old entries whenever the
    // value is updated.
    private Object currentDataSourceValue;

    private String elements; // Elements Designer property

    private boolean initialized = false; // Keep track whether the Screen has already been initialized

    private int t = 1;

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
    @SimpleProperty(description="To be done (non-functional for now)",  category = PropertyCategory.BEHAVIOR,
                    userVisible = false)
    public void ElementsFromPairs(String elements) {
        this.elements = elements;

        // If the specified String is empty, ignore import.
        // If the Data component is not initialized, then ignore
        // the importing (because if there is a Source property specified,
        // ElementsFromPairs should not take effect to prevent data overriding)
        if (elements == null || elements.equals("") || !initialized) {
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
    public void ImportFromList(final YailList list) {
        // Import the specified data asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                chartDataModel.importFromList(list);
                refreshChart();
            }
        });
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
        final Future<YailList> csvFileColumns = csvFile.getDataValue(columns);

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

                // Import from CSV file with the specified parameters
                chartDataModel.importFromCSV(csvResult);

                // Refresh the Chart after import
                refreshChart();
            }
        });
    }

    /**
     * Sets the CSV column to parse data from the CSV source for the x values.
     *
     * @param column  name of the column for the x values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CSV_COLUMN, defaultValue = "")
    @SimpleProperty(description="Sets the column to parse from the attached CSV file for the x values." +
        "If a column is not specified, default values for the x values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void CsvXColumn(String column) {
        // The first element represents the x entries
        csvColumns.set(0, column);
    }

    /**
     * Sets the CSV column to parse data from the CSV source for the y values.
     *
     * @param column  name of the column for the y values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CSV_COLUMN, defaultValue = "")
    @SimpleProperty(description="Sets the column to parse from the attached CSV file for the y values." +
        "If a column is not specified, default values for the y values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void CsvYColumn(String column) {
        // The second element represents the y entries
        csvColumns.set(1, column);
    }

    /**
     * Sets the Data Source key identifier for the value to import from the
     * attached Data Source.
     *
     * An example is the tag of the TinyDB component, which identifies the value.
     *
     * The property is a Designer-only property, to be changed after setting the
     * Source component of the Chart Data component.
     * @param value  new (key) value
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="Sets the value identifier for the data value to import from the " +
        "attached Data Source.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void DataSourceValue(String value) {
        this.dataSourceValue = value;
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
    public void Source(ChartDataSource dataSource) {
        this.dataSource = dataSource;

        // The data should only be imported after the Data component
        // is initialized, otherwise exceptions may be caused in case
        // of very small data files.
        if (initialized) {
          if (dataSource instanceof ObservableChartDataSource) {
            // Add this Data Component as an observer to the ObservableChartDataSource object
            ((ObservableChartDataSource)dataSource).addDataSourceObserver(this);

            // No Data Source Value specified; Do not proceed with importing data
            if (dataSourceValue == null) {
                return;
            }
          }

            if (dataSource instanceof CSVFile) {
                importFromCSVAsync((CSVFile)dataSource, YailList.makeList(csvColumns));
            } else if (dataSource instanceof TinyDB) {
              ImportFromTinyDB((TinyDB)dataSource, dataSourceValue);
            } else if (dataSource instanceof CloudDB) {
                ImportFromCloudDB((CloudDB)dataSource, dataSourceValue);
            }
        }
    }

    /**
     * Returns the entries of the Data Series the x values of which match
     * the provided value
     * @param x  x value to search for
     * @return  YailList of entries (represented as tuples)
     */
    @SimpleFunction(description = "Returns a List of entries with x values matching the specified x value." +
        "A single entry is represented as a List of values of the entry.")
    public YailList GetEntriesWithXValue(final float x) {
      try {
        return threadRunner.submit(new Callable<YailList>() {
          @Override
          public YailList call() {
            // Use X Value as criterion to filter entries
            return chartDataModel.findEntriesByCriterion(x, ChartDataModel.EntryCriterion.XValue);
          }
        }).get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }

      // Undefined behavior: return emtpy List
      return new YailList();
    }

    /**
     * Returns the entries of the Data Series the y values of which match
     * the provided value
     * @param y  y value to search for
     * @return  YailList of entries (represented as tuples)
     */
    @SimpleFunction(description = "Returns a List of entries with y values matching the specified y value." +
        "A single entry is represented as a List of values of the entry.")
    public YailList GetEntriesWithYValue(final float y) {
      try {
        return threadRunner.submit(new Callable<YailList>() {
          @Override
          public YailList call() {
            // Use YValue as criterion to filter entries
            return chartDataModel.findEntriesByCriterion(y, ChartDataModel.EntryCriterion.YValue);
          }
        }).get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }

      return new YailList();
    }

    /**
     * Returns all the entries of the Data Series.
     * @return  YailList of all the entries of the Data Series
     */
    @SimpleFunction(description = "Returns all the entries of the Data Series." +
        "A single entry is represented as a List of values of the entry.")
    public YailList GetAllEntries() {
        try {
            return threadRunner.submit(new Callable<YailList>() {
                @Override
                public YailList call() {
                    return chartDataModel.getEntriesAsTuples();
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new YailList();
    }

    /**
     * Imports data from the specified TinyDB component with the provided tag identifier.
     *
     * @param tinyDB  TinyDB component to import from
     * @param tag  the identifier of the value to import
     */
    @SimpleFunction(description = "Imports data from the specified TinyDB component, given the names of the " +
        "value to use. The value is expected to be a YailList consisting of entries compatible with the " +
        "Data component.")
    public void ImportFromTinyDB(final TinyDB tinyDB, final String tag) {
        final List list = tinyDB.getDataValue(tag); // Get the List value from the TinyDB data

        // Update the current Data Source value (if appropriate)
        updateCurrentDataSourceValue(tinyDB, tag, currentDataSourceValue);

        // Import the specified data asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                chartDataModel.importFromList(list);
                refreshChart();
            }
        });
    }

    /**
     * TO BE FILLED
     */
    @SimpleFunction(description = "Imports data from the specified CloudDB component, given the names of the " +
        "value to use. The value is expected to be a YailList consisting of entries compatible with the " +
        "Data component.")
    public void ImportFromCloudDB(final CloudDB cloudDB, final String value) {
        // Get the Future YailList object from the CloudDB data
        final Future<List> list = cloudDB.getDataValue(value);

        threadRunner.submit(new Runnable() {
            @Override
            public void run() {
                List listValue;

                try {
                    listValue = list.get();

                    // Update the current Data Source value (if appropriate)
                    updateCurrentDataSourceValue(cloudDB, value, currentDataSourceValue);

                    chartDataModel.importFromList(listValue);
                    refreshChart();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Refreshes the Chart view object.
     */
    protected void refreshChart() {
        // In case of the LineChartBaseDataModel being used, the Data Set
        // of the model has to manually notify the changes (since entries
        // are added directly to the Data Set in the case of the
        // LineChartBase Data Model
        // TODO: In case the addEntryOrdered method is ever used instead,
        // TODO: this line could then be removed.
        if (chartDataModel instanceof LineChartBaseDataModel) {
            chartDataModel.getDataset().notifyDataSetChanged();
        }

        container.refresh();
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
        } else {
            // If no Source is specified, the ElementsFromPairs
            // property can be set instead. Otherwise, this is not
            // set to prevent data overriding.
            ElementsFromPairs(elements);
        }
    }

    /**
     * Event called when the value of the observed ChartDataSource component changes.
     *
     * If the key matches the dataSourceValue of the Data Component, the specified
     * new value is processed and imported, while the old data part of the Data
     * Source is removed.
     *
     * A key value of null is interpreted as a change of all the values, so it would
     * change the imported data.
     *
     * @param component  component that triggered the event
     * @param key  key of the value that changed
     * @param newValue  the new value of the observed value
     */
    @Override
    public void onDataSourceValueChange(ChartDataSource component, String key, final Object newValue) {
        if (!component.equals(dataSource) // Calling component is not the attached Data Source. TODO: Un-observe?
            || (key != null && !key.equals(dataSourceValue))) { // The changed value is not the observed value
            return;
        }

        // Run data operations asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Old value originating from the Data Source exists and is of type List
                if (currentDataSourceValue instanceof List) {
                    // Remove the old values
                    chartDataModel.removeValues((List)currentDataSourceValue);
                }

                // Update current Data Source value
                currentDataSourceValue = newValue;

                // New value is a List; Import the value
                if (newValue instanceof List) {
                    chartDataModel.importFromList((List)newValue);
                }

                // Refresh the Chart view
                refreshChart();
            }
        });
    }

    @Override
    public void onReceiveValue(String key, Object value) {
        // Check that the key of the value received matches the
        // Data Source value key
        if (key.equals(dataSourceValue)) {
            // Construct and add tuple with t value and the data value
            YailList tuple = YailList.makeList(Arrays.asList(t, value));
            chartDataModel.addTimeEntry(tuple);

            refreshChart();
            t++;
        }
    }

    /**
     * Updates the current observed Data Source value if the source and key matches
     * the attached Data Source & value
     * @param source  Source component
     * @param key  Key of the updated value
     * @param newValue  The updated value
     */
    private void updateCurrentDataSourceValue(ObservableChartDataSource source, Object key, Object newValue) {
        if (source == dataSource // The source must be the same as the attached source
            && key != null // The key must be non-null
            && key.equals(dataSourceValue)) { // The key should equal the local key
            currentDataSourceValue = newValue;
        }
    }
}
