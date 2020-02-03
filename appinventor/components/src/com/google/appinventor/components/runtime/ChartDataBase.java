package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.OnBeforeInitializeListener;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@SimpleObject
public abstract class ChartDataBase implements Component, OnBeforeInitializeListener, ChartDataSourceChangeListener,
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

    // Properties used in Designer to import from DataFile.
    // Represents the names of the columns to use,
    // where each index corresponds to a single dimension.
    protected List<String> dataFileColumns;

    // Properties used in Designer to import from Web components.
    // Represents the names of the columns to use,
    // where each index corresponds to a single dimension.
    protected List<String> webColumns;

    // Property used in Designer to import from a Data Source.
    // Represents the key value of the value to use from the
    // attached Data Source.
    protected String dataSourceValue;

    private String label;
    private int color;
    private YailList colors;
    private int pointShape;
    private int lineType;

    private ChartDataSource dataSource; // Attached Chart Data Source

    // Currently imported observed Data Source value. This has to be
    // kept track of in order to remove old entries whenever the
    // value is updated.
    private Object currentDataSourceValue;

    private String elements; // Elements Designer property

    private boolean initialized = false; // Keep track whether the Screen has already been initialized

    private int t = 0;

    /**
     * Creates a new Chart Data component.
     */
    protected ChartDataBase(Chart chartContainer) {
        this.container = chartContainer;
        chartContainer.addDataComponent(this);

        // Set default properties and instantiate Chart Data Model
        initChartData();
        DataSourceValue("");

        threadRunner = Executors.newSingleThreadExecutor();
        container.$form().registerForOnBeforeInitialize(this);
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
     * Returns the Chart's colors as a List
     * @return  List of colors
     */
    @SimpleProperty(
        category = PropertyCategory.APPEARANCE
    )
    public YailList Colors() {
        return colors;
    }

    /**
     * Specifies the data series colors as a list of alpha-red-green-blue integers.
     *
     * TODO: Perhaps a Designer property selector could be devised here to select
     * TODO: the colors of the Chart.
     *
     * @param colors  List of argb values
     */
    @SimpleProperty
    public void Colors(YailList colors) {
        // Parse the entries of the YailList
        List<Integer> resultColors = new ArrayList<Integer>();

        for (int i = 0; i < colors.size(); ++i) {
            // Get the element of the YailList as a String
            String color = colors.getString(i);

            try {
                // Parse the color value and add it to the results List
                int colorValue = Integer.parseInt(color);
                resultColors.add(colorValue);
            } catch (NumberFormatException e) {
                // Skip invalid entry
            }
        }

        // Update the Colors YailList variable
        this.colors = YailList.makeList(resultColors);

        // Set the colors from the constructed List of colors
        // and refresh the Chart.
        chartDataModel.setColors(resultColors);
        refreshChart();
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
     * Changes the Point Shape of the Data Series, provided
     * that the Data Series is a Scatter Chart Data Series.
     *
     * @param shape  one of {@link ComponentConstants#CHART_POINT_STYLE_CIRCLE},
     *          {@link ComponentConstants#CHART_POINT_STYLE_SQUARE},
     *          {@link ComponentConstants#CHART_POINT_STYLE_TRIANGLE},
     *          {@link ComponentConstants#CHART_POINT_STYLE_CROSS} or
     *          {@link ComponentConstants#CHART_POINT_STYLE_X}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_POINT_SHAPE,
            defaultValue = ComponentConstants.CHART_POINT_STYLE_CIRCLE + "")
    @SimpleProperty(userVisible = false)
    public void PointShape(int shape) {
        this.pointShape = shape;

        // Only change the Point Shape if the Chart Data Model is a
        // ScatterChartDataModel (other models do not support changing
        // the Point Shape)
        if (chartDataModel instanceof ScatterChartDataModel) {
            ((ScatterChartDataModel)chartDataModel).setPointShape(shape);
        }
    }

    /**
     * Changes the Line Type of the Data Series, provided
     * that the Data Series is a Line based Data Series.
     *
     * @param type  one of {@link ComponentConstants#CHART_LINE_TYPE_LINEAR},
     *          {@link ComponentConstants#CHART_LINE_TYPE_CURVED} or
     *          {@link ComponentConstants#CHART_LINE_TYPE_STEPPED}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_LINE_TYPE,
        defaultValue = ComponentConstants.CHART_LINE_TYPE_LINEAR + "")
    @SimpleProperty(userVisible = false)
    public void LineType(int type) {
        this.lineType = type;

        // Only change the Line Type if the Chart Data Model is a
        // LineChartBaseDataModel (other models do not support changing
        // the Line Type)
        if (chartDataModel instanceof LineChartBaseDataModel) {
            ((LineChartBaseDataModel)chartDataModel).setLineType(type);
        }
    }

    /**
     * Specifies the elements of the entries that the Chart should have.
     * @param elements  Comma-separated values of Chart entries alternating between x and y values.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
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
     * Imports data from a Data File component, with the specified column names.
     * The method is ran asynchronously.
     *
     * @param dataFile  Data File component to import from
     * @param columns  list of column names to import from
     */
    protected void importFromDataFileAsync(final DataFile dataFile, YailList columns) {
        // Get the Future object representing the columns in the DataFile component.
        final Future<YailList> dataFileColumns = dataFile.getDataValue(columns);

        // Import the data from the Data file asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                YailList dataResult = null;

                try {
                    // Get the columns from the DataFile. The retrieval of
                    // the result is blocking, so it will first wait for
                    // the reading to be processed.
                    dataResult = dataFileColumns.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // Import from Data file with the specified parameters
                chartDataModel.importFromColumns(dataResult);

                // Refresh the Chart after import
                refreshChart();
            }
        });
    }


    /**
     * Imports data from a Web component, with the specified column names.
     * The method is ran asynchronously.
     *
     * @param webComponent  web component to import from
     * @param columns  list of column names to import from
     */
    protected void importFromWebAsync(final Web webComponent, final YailList columns) {
      // Get the Future object representing the columns in the Web component.
      final Future<YailList> webColumns = webComponent.getDataValue(columns);

        // Import the Data from the Web component asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
              // Get the data columns from the Web component. The retrieval of
              // the result is blocking, so it will first wait for
              // the retrieval to be processed in full.
              YailList dataColumns = null;

              try {
                dataColumns = webColumns.get();
              } catch (InterruptedException e) {
                e.printStackTrace();
              } catch (ExecutionException e) {
                e.printStackTrace();
              }

              if (webComponent == dataSource) {
                  updateCurrentDataSourceValue(dataSource, null, null);
              }

                // Import the data from the retrieved columns
                chartDataModel.importFromColumns(dataColumns);

                // Refresh the Chart after import
                refreshChart();
            }
        });
    }

    /**
     * Sets the Data column to parse data from the DataFile source for the x values.
     *
     * @param column  name of the column for the x values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_DATA_FILE_COLUMN, defaultValue = "")
    @SimpleProperty(description="Sets the column to parse from the attached Data File for the x values." +
        "If a column is not specified, default values for the x values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void DataFileXColumn(String column) {
        // The first element represents the x entries
        dataFileColumns.set(0, column);
    }

    /**
     * Sets the Data column to parse data from the Web component source for the x values.
     *
     * @param column  name of the column for the x values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "Sets the column to parse from the attached Web component for the x values." +
        "If a column is not specified, default values for the x values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void WebXColumn(String column) {
        // The first element represents the x entries
        webColumns.set(0, column);
    }

    /**
     * Sets the Data column to parse data from the DataFile source for the y values.
     *
     * @param column  name of the column for the y values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_DATA_FILE_COLUMN, defaultValue = "")
    @SimpleProperty(description="Sets the column to parse from the attached Data File for the y values." +
        "If a column is not specified, default values for the y values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void DataFileYColumn(String column) {
        // The second element represents the y entries
        dataFileColumns.set(1, column);
    }

    /**
     * Sets the Data column to parse data from the Web component source for the y values.
     *
     * @param column  name of the column for the y values
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "Sets the column to parse from the attached Web component for the y values." +
        "If a column is not specified, default values for the y values will be generated instead.",
        category = PropertyCategory.BEHAVIOR,
        userVisible = false)
    public void WebYColumn(String column) {
        // The second element represents the y entries
        webColumns.set(1, column);
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
     * @param dataSource  Data Source to use for the Chart data.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Sets the Data Source for the Data component. Accepted types " +
                "include DataFile, Web, TinyDB, CloudDB, AccelerometerSensor and BluetoothClient components.")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_DATA_SOURCE)
    public void Source(ChartDataSource dataSource) {
        // If the previous Data Source is an ObservableChartDataSource,
        // this Chart Data component must be removed from the observers
        // List of the Data Source.
        if (this.dataSource != dataSource && this.dataSource instanceof ObservableChartDataSource) {
            ((ObservableChartDataSource)this.dataSource).removeDataObserver(this);
        }

        this.dataSource = dataSource;

        // The data should only be imported after the Data component
        // is initialized, otherwise exceptions may be caused in case
        // of very small data files.
        if (initialized) {
          if (dataSource instanceof ObservableChartDataSource) {
            // Add this Data Component as an observer to the ObservableChartDataSource object
            ((ObservableChartDataSource)dataSource).addDataObserver(this);

            // No Data Source Value specified; Do not proceed with importing data
            if (dataSourceValue == null) {
                return;
            }
          }

            if (dataSource instanceof DataFile) {
                importFromDataFileAsync((DataFile)dataSource, YailList.makeList(dataFileColumns));
            } else if (dataSource instanceof TinyDB) {
                ImportFromTinyDB((TinyDB)dataSource, dataSourceValue);
            } else if (dataSource instanceof CloudDB) {
                ImportFromCloudDB((CloudDB)dataSource, dataSourceValue);
            } else if (dataSource instanceof Web) {
                importFromWebAsync((Web)dataSource, YailList.makeList(webColumns));
            }
        }
    }

    /**
     * Changes the attached Data Source of the Chart Data component to the
     * newly specified Source with the specified key value argument.
     *
     * In the case of DataFile and Web components, the keyValue is
     * expected to be a CSV-formatted String.
     *
     * @param source  Data Source to attach to the Data component
     * @param keyValue  Key value identifying the value to use from the Data Source
     */
    @SimpleFunction(description = "Changes the linked Data Source of the Data component, and " +
        "imports the data that matches the specified key value. Accepted Data Source types " +
        "include DataFile, Web, TinyDB, CloudDB, AccelerometerSensor and BluetoothClient components." +
        "If the data identified by the key is updated in the attached Data Source component, then " +
        "the data is also updated in the Chart Data component. In the case of DataFile and Web " +
        "components, the key value should be in CSV format, specified as follows: X,Y,Z\n" +
        "X, Y and Z correspond to column names respectively to use from the DataFile or Web " +
        "component upon importing.")
    public void ChangeDataSource(final ChartDataSource source, final String keyValue) {
        // To avoid interruptions to Data importing, the Chart Data Source should be
        // changed asynchronously.
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // DataFile and Web components require different handling for
                // changing the Data Source. The expected format is CSV values.
                if (source instanceof DataFile || source instanceof Web) {
                    YailList keyValues = new YailList();

                    try {
                        // Attempt to CSV Parse the specified String
                        keyValues = CsvUtil.fromCsvRow(keyValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Retrieve the List of columns to modify (DataFile columns if Data Source
                    // is a DataFile, and Web columns otherwise.
                    List<String> columnsList = (source instanceof DataFile) ? dataFileColumns : webColumns;

                    // Iterate through all the columns
                    for (int i = 0; i < columnsList.size(); ++i) {
                        // Default option: Set to blank
                        String columnValue = "";

                        // KeyValues has required column
                        if (keyValues.size() > i) {
                            // Update the column value of the current iteration to
                            // the parsed column value
                            columnValue = keyValues.getString(i);
                        }

                        // Update the i-th column value of the DataFileColumns/WebColumns
                        columnsList.set(i, columnValue);
                    }
                } else {
                    // All other Data Source components simply take
                    // the keyValue argument as the dataSourceValue.
                    dataSourceValue = keyValue;
                }

                // Reset Current Data Source Value to null
                currentDataSourceValue = null;

                // Change the Data Source
                Source(source);
            }
        });
    }

    /**
     * Removes the currently attached Data Source from the Chart Data component.
     */
    @SimpleFunction(description = "Un-links the currently associated Data Source component from the Chart.")
    public void RemoveDataSource() {
        // To avoid interruptions to Data importing, the Chart Data Source should be
        // changed asynchronously.
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Change the Chart Data Source to null
                Source(null);

                // Reset all DataSource related values to blank
                dataSourceValue = "";

                // Reset current Data Source Value to null
                currentDataSourceValue = null;

                for (int i = 0; i < dataFileColumns.size(); ++i) {
                    dataFileColumns.set(i, "");
                    webColumns.set(i, "");
                }
            }
        });
    }

    /**
     * Returns the entries of the Data Series the x values of which match
     * the provided value
     * @param x  x value to search for
     * @return  YailList of entries (represented as tuples)
     */
    @SimpleFunction(description = "Returns a List of entries with x values matching the specified x value." +
        "A single entry is represented as a List of values of the entry.")
    public YailList GetEntriesWithXValue(final String x) {
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

      // Undefined behavior: return empty List
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
    public YailList GetEntriesWithYValue(final String y) {
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
    @SimpleFunction(description = "Imports data from the specified TinyDB component, given the tag of the " +
        "value to use. The value is expected to be a YailList consisting of entries compatible with the " +
        "Data component.")
    public void ImportFromTinyDB(final TinyDB tinyDB, final String tag) {
        final List list = tinyDB.getDataValue(tag); // Get the List value from the TinyDB data

        // Update the current Data Source value (if appropriate)
        updateCurrentDataSourceValue(tinyDB, tag, list);

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
     * Imports data from the specified CloudDB component with the provided tag identifier.
     *
     * @param cloudDB  CloudDB component to import from
     * @param tag  the identifier of the value to import
     */
    @SimpleFunction(description = "Imports data from the specified CloudDB component, given the tag of the " +
        "value to use. The value is expected to be a YailList consisting of entries compatible with the " +
        "Data component.")
    public void ImportFromCloudDB(final CloudDB cloudDB, final String tag) {
        // Get the Future YailList object from the CloudDB data
        final Future<List> list = cloudDB.getDataValue(tag);

        // Import data asynchronously
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                final List listValue;

                try {
                    // Get the value from the Future object
                    listValue = list.get();

                    // Update the current Data Source value (if appropriate)
                    updateCurrentDataSourceValue(cloudDB, tag, listValue);

                    // Import the data and refresh the Chart
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
     * Refreshes the Chart View object with the current up to date
     * Data Series data.
     */
    protected void refreshChart() {
        // Update the Chart with the Chart Data Model's current
        // data and refresh the Chart itself.
        container.getChartView().Refresh(chartDataModel);

        // ORIGINAL SOLUTION (refreshing Chart directly via container.refresh())
        // ISSUE: Causes exceptions/crashes upon quicker data adding & refreshing.

        // Notify data set changes (needs to be invoked when changing
        // values of the Data Set directly, which occurs in add/remove
        // entry methods)
        // chartDataModel.getDataset().notifyDataSetChanged();

        // Refresh the Chart
        // container.refresh();

        // Newer solution to post a FutureTask and wait for it to finish
        // if the thread is an async thread;
        // ISSUE: Deadlocks can be caused (UI thread waits on async thread
        // to finish, while async thread waits on the UI thread to finish)

//        FutureTask<Void> task = new FutureTask<Void>(new Runnable() {
//            @Override
//            public void run() {
//                container.getChartView().Refresh(chartDataModel);
//            }
//        }, null);
//
//        container.getChartView().uiHandler.post(task);
//
//        if (Looper.getMainLooper() != Looper.myLooper()) {
//            try {
//                task.get();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        container.getChartView().Refresh(chartDataModel);
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
    public void onBeforeInitialize() {
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
    public void onDataSourceValueChange(final ChartDataSource component, final String key, final Object newValue) {
        if (component != dataSource // Calling component is not the attached Data Source. TODO: Un-observe?
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

                updateCurrentDataSourceValue(component, key, newValue);

                // New value is a List; Import the value
                if (currentDataSourceValue instanceof List) {
                    chartDataModel.importFromList((List)currentDataSourceValue);
                }

                // Refresh the Chart view
                refreshChart();
            }
        });
    }

    @Override
    public void onReceiveValue(RealTimeChartDataSource component, final String key, Object value) {
        // Calling component is not the actual Data Source
        if (component != dataSource) {
            return;
        }

        // Boolean to indicate whether data should be imported (conditions
        // for importing are satisfied)
        boolean importData = false;

        // BluetoothClient requires different handling due to value format
        // expected to be with a prefix (prefix||value)
        if (component instanceof BluetoothClient) {
            // Get the imported value as a String
            String valueString = (String) value;

            // Check whether the retrieved value starts with the local
            // dataSourceValue (which indicates the prefix)
            importData = valueString.startsWith(dataSourceValue);

            // Data should be imported (prefix match)
            if (importData) {
                // Extract the value from the retrieved prefix||value pair
                // The extraction is done by cutting off the prefix entirely.
                value = valueString.substring(dataSourceValue.length());
            }
        } else {
            // Check that the key of the value received matches the
            // Data Source value key
            importData = key == null || key.equals(dataSourceValue);
        }

        if (importData) {
            // Get value as final value to use for the runnable on UI thread
            final Object finalValue = value;

            // Import value in non-async (since this is a real-time value,
            // the update will come faster than running in async)
            // Importing the value asynchronously could cause more
            // race conditions between data series (as well as added tearing)
            container.$context().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Get the  t value synced across the entire Chart
                    // and update the synced value if necessary
                    t = container.getSyncedTValue(t);

                    // Create tuple from current t value and the received value
                    final YailList tuple = YailList.makeList(Arrays.asList(t, finalValue));

                    chartDataModel.addTimeEntry(tuple);
                    refreshChart();

                    // Increment t value
                    t++;
                }
            });
        }
    }

    /**
     * Updates the current observed Data Source value if the source and key matches
     * the attached Data Source & value
     * @param source  Source component
     * @param key  Key of the updated value
     * @param newValue  The updated value
     */
    private void updateCurrentDataSourceValue(ChartDataSource source, Object key, Object newValue) {
        if (source == dataSource // The source must be the same as the attached source
            && (key == null || key.equals(dataSourceValue))) { // The key should equal the local key (or null)
            if (source instanceof Web) {
                // Get the columns from the local webColumns properties
                YailList columns = ((Web)source).getColumns(YailList.makeList(webColumns));

                // Set the current Data Source Value to all the tuples from the columns.
                // This is needed to easily remove values later on when the value changes
                // again.
                currentDataSourceValue = chartDataModel.getTuplesFromColumns(columns);
            } else {
                // Update current Data Source value
                currentDataSourceValue = newValue;
            }
        }
    }

    /**
     * Changes the underlying Executor Service of the threadRunner.
     *
     * Primarily used for testing to inject test/mock ExecutorService
     * classes.
     *
     * @param service  new ExecutorService object to use..
     */
    public void setExecutorService(ExecutorService service) {
        threadRunner = service;
    }
}
