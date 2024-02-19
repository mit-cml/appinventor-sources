// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Data Source that is given to the Data Science component for analysis or directly to the chart
 * component for visualisation.
 */
@SuppressWarnings({"TryWithIdenticalCatches", "checkstyle:JavadocParagraph"})
@SimpleObject
public abstract class DataCollection<C extends ComponentContainer, M extends DataModel<?>>
    implements Component, DataSource<Object, List<?>>, DataSourceChangeListener {
  protected final Set<DataSourceChangeListener> listeners = new HashSet<>();
  protected final C container;
  protected M dataModel;

  /**
   * Used to queue & execute asynchronous tasks while ensuring
   * order of method execution (ExecutorService should be a Single Thread runner)
   * In the case of methods which return values and where
   * the result depends on the state of the data, blocking get
   * calls are used to ensure that all the previous async tasks
   * finish before the data is returned.
   */
  protected ExecutorService threadRunner;

  /**
   * Properties used in Designer to import from DataFile.
   * Represents the names of the columns to use,
   * where each index corresponds to a single dimension.
   */
  protected List<String> dataFileColumns;

  protected boolean useSheetHeaders;

  protected List<String> sheetsColumns;

  /**
   * Properties used in Designer to import from Web components.
   * Represents the names of the columns to use,
   * where each index corresponds to a single dimension.
   */
  protected List<String> webColumns;

  /**
   * Property used in Designer to import from a Data Source.
   * Represents the key value of the value to use from the
   * attached Data Source.
   */
  protected String dataSourceKey;

  private DataSource<?, ?> dataSource; // Attached Chart Data Source

  /**
   * Last seen observed Data Source value. This has to be
   * kept track of in order to remove old entries whenever the
   * value is updated.
   */
  private Object lastDataSourceValue;

  private String elements; // Elements Designer property

  private boolean initialized = false; // Keep track whether the Screen has already been initialized

  private int tick = 0;

  /**
   * Creates a new Chart Data component.
   */
  public DataCollection(C container) {
    this.container = container;

    // Set default properties
    DataSourceKey("");
    threadRunner = Executors.newSingleThreadExecutor();
    // Construct default dataFileColumns list with 2 entries
    dataFileColumns = Arrays.asList("", "");
    sheetsColumns = Arrays.asList("", "");
    webColumns = Arrays.asList("", ""); // Construct default webColumns list with 2 entries
  }

  /**
   * Changes the underlying Executor Service of the threadRunner.
   *
   *    Primarily used for testing to inject test/mock ExecutorService
   * classes.
   *
   * @param service new ExecutorService object to use..
   */
  public void setExecutorService(ExecutorService service) {
    threadRunner = service;
  }

  public void addDataSourceChangeListener(DataSourceChangeListener listener) {
    listeners.add(listener);
    // Trigger the listener's event
    listener.onDataSourceValueChange(this, null, null);
  }

  public void removeDataSourceChangeListener(DataSourceChangeListener listener) {
    listeners.remove(listener);
  }

  /* DataSource implementation */

  @Override
  public List<?> getDataValue(Object key) {
    return dataModel.entries;
  }

  /**
   * Refreshes the Chart View object with the current up to date
   * Data Series data.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public abstract void onDataChange();

  /*
   * SimpleProperties
   */


  /**
   * Comma separated list of Elements to use for the data series. Values are formatted
   * as follows: x1,y1,x2,y2,x3,y3. Values are taken in pairs, and an entry is formed
   * from the x and y values.
   *
   * @param elements Comma-separated values of Chart entries alternating between x and y values.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void ElementsFromPairs(final String elements) {
    // TODO: Ideally, this should be set in the thread runner.
    // TODO: However, usually this does not make the difference
    // TODO: As this property is only set from the Designer and
    // TODO: there is virtually no room for conflict.
    this.elements = elements;

    // If the specified String is empty, ignore import.
    // If the Data component is not initialized, then ignore
    // the importing (because if there is a Source property specified,
    // ElementsFromPairs should not take effect to prevent data overriding)
    if (elements == null || elements.equals("") || !initialized) {
      return;
    }

    // Import the specified data asynchronously
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        dataModel.setElements(elements);
        onDataChange();
      }
    });
  }

  /**
   * If checked, the first row of the spreadsheet will be used to interpret the x and y column
   * values. Otherwise, the x and y columns should be a column reference, such as A or B.
   *
   * @param useHeaders true if the first row of the spreadsheet should be interpreted as a header
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void SpreadsheetUseHeaders(boolean useHeaders) {
    useSheetHeaders = useHeaders;
  }

  /**
   * Value used when importing data from a DataFile component {@link #Source(DataSource)}. The
   * value represents the column to use from the DataFile for the x entries
   * of the Data Series. For instance, if a column's first value is "Time",
   * and a column value of "Time" is specified, that column will be used
   * for the x values. If a value here is not specified, default values for the
   * x values will be generated instead.
   *
   * @param column name of the column for the x values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_DATA_FILE_COLUMN)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void DataFileXColumn(String column) {
    // The first element represents the x entries
    dataFileColumns.set(0, column);
  }

  /**
   * Value used when importing data from a Web component Source. The
   * value represents the column to use from the Web for the x entries
   * of the Data Series. For instance, if the contents of the Web are
   * retrieved in JSON format, and an array with the "Time" tag exists,
   * the "Time" column value can be specified to use that array.
   *
   * @param column name of the column for the x values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(description = "Sets the column to parse from the attached Web component for "
      + "the x values. If a column is not specified, default values for the x values will be "
      + "generated instead.",
      category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void WebXColumn(String column) {
    // The first element represents the x entries
    webColumns.set(0, column);
  }

  /**
   * Sets the column to parse from the attached Spreadsheet component for the x values. If a
   * column is not specified, default values for the x values will be generated instead.
   *
   * @param column the name of the column to use for X values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public void SpreadsheetXColumn(String column) {
    sheetsColumns.set(0, column);
  }

  /**
   * Value used when importing data from a DataFile component {@link #Source(DataSource)}. The
   * value represents the column to use from the DataFile for the y entries
   * of the Data Series. For instance, if a column's first value is "Temperature",
   * and a column value of "Temperature" is specified, that column will be used
   * for the y values. If a value here is not specified, default values for the
   * y values will be generated instead.
   *
   * @param column name of the column for the y values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_DATA_FILE_COLUMN)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void DataFileYColumn(String column) {
    // The second element represents the y entries
    dataFileColumns.set(1, column);
  }

  /**
   * Value used when importing data from a Web component Source. The
   * value represents the column to use from the Web for the y entries
   * of the Data Series. For instance, if the contents of the Web are
   * retrieved in JSON format, and an array with the "Temperature" tag exists,
   * the "Temperature" column value can be specified to use that array.
   *
   * @param column name of the column for the y values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(description = "Sets the column to parse from the attached Web component for the "
      + "y values. If a column is not specified, default values for the y values will be "
      + "generated instead.",
      category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void WebYColumn(String column) {
    // The second element represents the y entries
    webColumns.set(1, column);
  }

  /**
   * Sets the column to parse from the attached Spreadsheet component for the y values. If a
   * column is not specified, default values for the y values will be generated instead.
   *
   * @param column the name of the column to use for Y values
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public void SpreadsheetYColumn(String column) {
    sheetsColumns.set(1, column);
  }

  /**
   * Sets the Data Source key identifier for the value to import from the
   * attached Data Source.
   *
   *   An example is the tag of the TinyDB component, which identifies the value.
   *
   *   The property is a Designer-only property, and should be changed after setting the
   * Source component of the Chart Data component.
   *
   *   A complete list of applicable values for each compatible source is as follows:
   *
   *     * For TinyDB and CloudDB, this is the tag value.
   *     * For the AccelerometerSensor, the value should be one of the following: X Y or Z
   *     * For the GyroscopeSensor, the value should be one of the following: X Y or Z
   *     * For the LocationSensor, the value should be one of the following:
   *       latitude, longitude, altitude or speed
   *     * For the OrientationSensor, the value should be one of the following:
   *       pitch, azimuth or roll
   *     * For the Pedometer, the value should be one of the following:
   *       WalkSteps, SimpleSteps or Distance
   *     * For the ProximitySensor, the value should be distance.
   *     * For the BluetoothClient, the value represents the prefix to remove from the value.
   *       For instance, if values come in the format "t:12", the prefix can be specified as "t:",
   *       and the prefix will then be removed from the data. No value can be specified if purely
   *       numerical values are returned.
   *
   * @param key new key value
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public void DataSourceKey(String key) {
    this.dataSourceKey = key;
  }


  /**
   * Sets the Source to use for the Data component. Valid choices
   * include AccelerometerSensor, BluetoothClient, CloudDB, DataFile,
   * GyroscopeSensor, LocationSesnro, OrientationSensor, Pedometer,
   * ProximitySensor TinyDB and Web components. The Source value also requires
   * valid DataSourceValue, WebColumn or DataFileColumn properties,
   * depending on the type of the Source attached (the required properties
   * show up in the Properties menu after the Source is changed).
   *
   *   If the data identified by the {@link #DataSourceKey(String)} is updated
   * in the attached Data Source component, then the data is also updated in
   * the Chart Data component.
   *
   * @param dataSource Data Source to use for the Chart data.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_DATA_SOURCE)
  public <K, V> void Source(DataSource<K, V> dataSource) {
    // If the previous Data Source is an ObservableDataSource,
    // this Chart Data component must be removed from the observers
    // List of the Data Source.
    if (this.dataSource != dataSource && this.dataSource instanceof ObservableDataSource) {
      ((ObservableDataSource<?, ?>) this.dataSource).removeDataObserver(this);
    }

    this.dataSource = dataSource;

    // The data should only be imported after the Data component
    // is initialized, otherwise exceptions may be caused in case
    // of very small data files.
    if (initialized) {
      if (dataSource instanceof ObservableDataSource) {
        // Add this Data Component as an observer to the ObservableDataSource object
        ((ObservableDataSource<?, ?>) dataSource).addDataObserver(this);

        // No Data Source Value specified; Do not proceed with importing data
        if (dataSourceKey == null) {
          return;
        }
      }

      if (dataSource instanceof DataFile) {
        importFromDataFileAsync((DataFile) dataSource, YailList.makeList(dataFileColumns));
      } else if (dataSource instanceof TinyDB) {
        ImportFromTinyDB((TinyDB) dataSource, dataSourceKey);
      } else if (dataSource instanceof CloudDB) {
        ImportFromCloudDB((CloudDB) dataSource, dataSourceKey);
      } else if (dataSource instanceof Spreadsheet) {
        importFromSpreadsheetAsync((Spreadsheet) dataSource, YailList.makeList(sheetsColumns),
            useSheetHeaders);
      } else if (dataSource instanceof Web) {
        importFromWebAsync((Web) dataSource, YailList.makeList(webColumns));
      }
    }
  }

  /*
   * SimpleFunctions
   */

  /**
   * Imports the data from the specified list parameter to the data series.
   * The list is expected to contain element which are also lists. Each
   * list element is expected to have 2 values, the first one being
   * the x value, and the second one being the y value.
   * Invalid list entries are simply skipped. Existing data are not cleared.
   *
   * @param list list of tuples.
   */
  @SimpleFunction()
  public void ImportFromList(final YailList list) {
    // TODO: JavaDoc description has to be updated if we ever
    // TODO: Generalize to more than 2 dimensions.

    // Import the specified data asynchronously
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        dataModel.importFromList(list);
        onDataChange();
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
        dataModel.clearEntries();
        onDataChange();
      }
    });
  }

  /**
   * Changes the Data Source of the component to the specified component Source with the
   * specified key value. See the {@link #Source(DataSource)} property for
   * applicable components. See the {@link #DataSourceKey(String)} property for the interpretation
   * of the keyValue. In the case of the DataFile and Web components, the keyValue is expected to
   * be a CSV formatted string, where the first value corresponds to the x column, and the second
   * value corresponds to the y value.
   *
   * @param source   Data Source to attach to the Data component
   * @param keyValue Key value identifying the value to use from the Data Source
   */
  @SimpleFunction()
  public <K, V> void ChangeDataSource(final DataSource<K, V> source, final String keyValue) {
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
            Log.e(this.getClass().getName(), e.getMessage());
          }

          // Retrieve the List of columns to modify (DataFile columns if Data Source
          // is a DataFile, and Web columns otherwise.
          List<String> columnsList;
          if (source instanceof DataFile) {
            columnsList = dataFileColumns;
          } else if (source instanceof Spreadsheet) {
            columnsList = sheetsColumns;
          } else if (source instanceof Web) {
            columnsList = webColumns;
          } else {
            throw new IllegalArgumentException(source + " is not an expected DataSource");
          }

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
          // the keyValue argument as the dataSourceKey.
          dataSourceKey = keyValue;
        }

        // Reset Current Data Source Value to null
        lastDataSourceValue = null;

        // Change the Data Source
        Source(source);
      }
    });
  }

  /**
   * Removes the currently attached Data Source from the Chart Data component.
   * Doing so will result in no more updates from the Data Source being sent, however,
   * the current data will not be removed.
   */
  @SimpleFunction(description = "Un-links the currently associated Data Source component from "
      + "the Chart.")
  public void RemoveDataSource() {
    // To avoid interruptions to Data importing, the Chart Data Source should be
    // changed asynchronously.
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        // Change the Chart Data Source to null
        Source(null);

        // Reset all DataSource related values to blank
        dataSourceKey = "";

        // Reset current Data Source Value to null
        lastDataSourceValue = null;

        for (int i = 0; i < dataFileColumns.size(); ++i) {
          dataFileColumns.set(i, "");
          sheetsColumns.set(i, "");
          webColumns.set(i, "");
        }
      }
    });
  }

  /**
   * Returns all entries of the data series matching the specified x value.
   * For a description of the format of the returned List, see {@link #GetAllEntries()}
   *
   * @param x x value to search for
   * @return YailList of entries (represented as tuples)
   */
  @SuppressWarnings("TryWithIdenticalCatches")
  @SimpleFunction(description = "Returns a List of entries with x values matching the specified "
      + "x value. A single entry is represented as a List of values of the entry.")
  public YailList GetEntriesWithXValue(final String x) {
    try {
      return threadRunner.submit(new Callable<YailList>() {
        @Override
        public YailList call() {
          // Use X Value as criterion to filter entries
          return dataModel.findEntriesByCriterion(x, DataModel.EntryCriterion.XValue);
        }
      }).get();
    } catch (InterruptedException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    } catch (ExecutionException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    }

    // Undefined behavior: return empty List
    return new YailList();
  }

  /**
   * Returns all entries of the data series matching the specified y value.
   * For a description of the format of the returned List, see {@link #GetAllEntries()}
   *
   * @param y y value to search for
   * @return YailList of entries (represented as tuples)
   */
  @SuppressWarnings("TryWithIdenticalCatches")
  @SimpleFunction(description = "Returns a List of entries with y values matching the specified "
      + "y value. A single entry is represented as a List of values of the entry.")
  public YailList GetEntriesWithYValue(final String y) {
    try {
      return threadRunner.submit(new Callable<YailList>() {
        @Override
        public YailList call() {
          // Use YValue as criterion to filter entries
          return dataModel.findEntriesByCriterion(y, DataModel.EntryCriterion.YValue);
        }
      }).get();
    } catch (InterruptedException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    } catch (ExecutionException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    }

    return new YailList();
  }

  /**
   * Returns all entries of the data series.
   * The returned value is a list, where each element of the list
   * is a list containing the values of the entry in order.
   *
   * @return YailList of all the entries of the Data Series
   */
  @SuppressWarnings("TryWithIdenticalCatches")
  @SimpleFunction(description = "Returns all the entries of the Data Series. "
      + "A single entry is represented as a List of values of the entry.")
  public YailList GetAllEntries() {
    try {
      return threadRunner.submit(new Callable<YailList>() {
        @Override
        public YailList call() {
          return dataModel.getEntriesAsTuples();
        }
      }).get();
    } catch (InterruptedException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    } catch (ExecutionException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    }

    return new YailList();
  }

  /**
   * Imports data from the specified TinyDB component by taking the value
   * identified by the specified tag value.
   *
   *   The expected TinyDB value is a list formatted in the same way as described in
   * {@link #ImportFromList(YailList)}.
   *
   *   Does not overwrite any data.
   *
   * @param tinyDB TinyDB component to import from
   * @param tag    the identifier of the value to import
   */
  @SimpleFunction()
  public void ImportFromTinyDB(final TinyDB tinyDB, final String tag) {
    final List<?> list = tinyDB.getDataValue(tag); // Get the List value from the TinyDB data

    // Update the current Data Source value (if appropriate)
    updateCurrentDataSourceValue(tinyDB, tag, list);

    // Import the specified data asynchronously
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        dataModel.importFromList(list);
        onDataChange();
      }
    });
  }

  /**
   * Imports data from the specified CloudDB component by taking the value
   * identified by the specified tag value.
   *
   *   The expected CloudDB value is a list formatted in the same way as described in
   * {@link #ImportFromList(YailList)}.
   *
   *   Does not overwrite any data.
   *
   * @param cloudDB CloudDB component to import from
   * @param tag     the identifier of the value to import
   */
  @SimpleFunction()
  public void ImportFromCloudDB(final CloudDB cloudDB, final String tag) {
    // Get the Future YailList object from the CloudDB data
    final Future<YailList> list = cloudDB.getDataValue(tag);

    // Import data asynchronously
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        final YailList listValue;

        try {
          // Get the value from the Future object
          listValue = list.get();

          // Update the current Data Source value (if appropriate)
          updateCurrentDataSourceValue(cloudDB, tag, listValue);

          // Import the data and refresh the Chart
          dataModel.importFromList(listValue);
          onDataChange();
        } catch (InterruptedException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        }
      }
    });
  }

  /*
   * Helper methods & overrides
   */

  /**
   * Imports data from a Data File component, with the specified column names.
   * The method is run asynchronously.
   *
   * @param dataFile Data File component to import from
   * @param columns  list of column names to import from
   */
  protected void importFromDataFileAsync(final DataFile dataFile, YailList columns) {
    // Get the Future object representing the columns in the DataFile component.
    final Future<YailList> dataFileColumns = dataFile.getDataValue(columns);

    // Import the data from the Data file asynchronously
    threadRunner.execute(new Runnable() {
      @SuppressWarnings("TryWithIdenticalCatches")
      @Override
      public void run() {
        YailList dataResult = null;

        try {
          // Get the columns from the DataFile. The retrieval of
          // the result is blocking, so it will first wait for
          // the reading to be processed.
          dataResult = dataFileColumns.get();
        } catch (InterruptedException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        }

        // Import from Data file with the specified parameters
        dataModel.importFromColumns(dataResult, true);
        // Refresh the Chart after import
        onDataChange();
      }
    });
  }


  protected void importFromSpreadsheetAsync(final Spreadsheet sheets, final YailList columns,
                                            final boolean useHeaders) {
    final Future<YailList> sheetColumns = sheets.getDataValue(columns, useHeaders);

    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        YailList dataColumns = null;

        try {
          dataColumns = sheetColumns.get();
        } catch (InterruptedException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        }

        if (sheets == dataSource) {
          updateCurrentDataSourceValue(dataSource, null, null);
        }

        dataModel.importFromColumns(dataColumns, useHeaders);
        onDataChange();

      }
    });
  }


  /**
   * Imports data from a Web component, with the specified column names.
   * The method is ran asynchronously.
   *
   * @param webComponent web component to import from
   * @param columns      list of column names to import from
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
          Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        }

        if (webComponent == dataSource) {
          updateCurrentDataSourceValue(dataSource, null, null);
        }

        // Import the data from the retrieved columns
        dataModel.importFromColumns(dataColumns, true);
        onDataChange();

      }
    });
  }

  /**
   * Imports data from the specified DataFile component by taking the specified x column
   * for the x values, and the specified y column for the y values. The DataFile's source file
   * is expected to be either a CSV or a JSON file.
   *
   *   Passing in empty text for any of the column parameters will result in the usage of
   * default values which are the indices of the entries. For the first entry, the default
   * value would be the 1, for the second it would be 2, and so on.
   *
   * @param dataFile     Data File component to import from
   * @param xValueColumn x-value column name
   * @param yValueColumn y-value column name
   */
  @SuppressWarnings("checkstyle:ParameterName")
  @SimpleFunction()
  public void ImportFromDataFile(final DataFile dataFile, String xValueColumn,
      String yValueColumn) {
    // Construct a YailList of columns from the specified parameters
    YailList columns = YailList.makeList(Arrays.asList(xValueColumn, yValueColumn));

    importFromDataFileAsync(dataFile, columns);
  }

  /**
   * Imports data from the specified Spreadsheet component by taking the specified x column
   * for the x values, and the specified y column for the y values. Prior to calling this function,
   * the Spreadsheet component's ReadSheet method has to be called to load the data. The usage of
   * the GotSheet event in the Spreadsheet component is unnecessary.
   *
   *   Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).
   *
   * @param spreadsheet  Spreadsheet component to import from
   * @param xColumn      x-value column name
   * @param yColumn      y-value column name
   * @param useHeaders   use the first row of values to interpret the column names
   */
  @SimpleFunction
  public void ImportFromSpreadsheet(final Spreadsheet spreadsheet, String xColumn, String yColumn,
      boolean useHeaders) {
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    importFromSpreadsheetAsync(spreadsheet, columns, useHeaders);
  }

  /**
   * Imports data from the specified Web component by taking the specified x column
   * for the x values, and the specified y column for the y values. Prior to calling this function,
   * the Web component's Get method has to be called to load the data. The usage of the gotValue
   * event in the Web component is unnecessary.
   *
   *   The expected response of the Web component is a JSON or CSV formatted
   * file for this function to work.
   *
   *   Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).
   *
   * @param web          Web component to import from
   * @param xValueColumn x-value column name
   * @param yValueColumn y-value column name
   */
  @SimpleFunction(description = "Imports data from the specified Web component, given the names "
      + "of the X and Y value columns. Empty columns are filled with default values "
      + "(1, 2, 3, ... for Entry 1, 2, ...). In order for the data importing to be successful, "
      + "to load the data, and then this block should be used on that Web component. The usage "
      + "of the gotValue event in the Web component is unnecessary.")
  public void ImportFromWeb(final Web web, String xValueColumn, String yValueColumn) {
    // Construct a YailList of columns from the specified parameters
    YailList columns = YailList.makeList(Arrays.asList(xValueColumn, yValueColumn));

    importFromWebAsync(web, columns);
  }

  /**
   * Links the Data Source component with the Data component, if
   * the Source component has been defined earlier.
   *
   *    The reason this is done is because otherwise exceptions
   * are thrown if the Data is being imported before the component
   * is fully initialized.
   */
  public void Initialize() {
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

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return container.$form();
  }

  /**
   * Event called when the value of the observed DataSource component changes.
   *
   *    If the key matches the dataSourceKey of the Data Component, the specified
   * new value is processed and imported, while the old data part of the Data
   * Source is removed.
   *
   *    A key value of null is interpreted as a change of all the values, so it would
   * change the imported data.
   *
   * @param component component that triggered the event
   * @param key       key of the value that changed
   * @param newValue  the new value of the observed value
   */
  @Override
  public void onDataSourceValueChange(final DataSource<?, ?> component, final String key,
                                      final Object newValue) {
    // Calling component is not the attached Data Source. TODO: Un-observe?
    if (component != dataSource
        || (!isKeyValid(key))) { // The changed value is not the observed value
      return;
    }

    // Run data operations asynchronously
    threadRunner.execute(new Runnable() {
      @Override
      public void run() {
        // Old value originating from the Data Source exists and is of type List
        if (lastDataSourceValue instanceof List) {
          // Remove the old values
          dataModel.removeValues((List<?>) lastDataSourceValue);
        }

        updateCurrentDataSourceValue(component, key, newValue);

        // New value is a List; Import the value
        if (lastDataSourceValue instanceof List) {
          dataModel.importFromList((List<?>) lastDataSourceValue);
        }
        onDataChange();
      }
    });
  }

  @Override
  public void onReceiveValue(RealTimeDataSource<?, ?> component, final String key, Object value) {
    // Calling component is not the actual Data Source
    if (component != dataSource) {
      return;
    }

    // Boolean to indicate whether data should be imported (conditions
    // for importing are satisfied)
    boolean importData;

    // BluetoothClient requires different handling due to value format
    // expected to be with a prefix (prefix||value)
    if (component instanceof BluetoothClient) {
      // Get the imported value as a String
      String valueString = (String) value;

      // Check whether the retrieved value starts with the local
      // dataSourceKey (which indicates the prefix)
      importData = valueString.startsWith(dataSourceKey);

      // Data should be imported (prefix match)
      if (importData) {
        // Extract the value from the retrieved prefix||value pair
        // The extraction is done by cutting off the prefix entirely.
        value = valueString.substring(dataSourceKey.length());
      }
    } else {
      // Check that the key of the value received matches the
      // Data Source value key
      importData = isKeyValid(key);
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
          if (container instanceof Chart) {
            tick = ((Chart) container).getSyncedTValue(tick);

            // Create tuple from current t value and the received value
            final YailList tuple = YailList.makeList(Arrays.asList(tick, finalValue));

            dataModel.addTimeEntry(tuple);
            onDataChange();

            // Increment t value
            tick++;
          }
        }
      });

    }
  }

  /**
   * Updates the current observed Data Source value if the source and key matches
   * the attached Data Source and value.
   *
   * @param source   Source component
   * @param key      Key of the updated value
   * @param newValue The updated value
   */
  private void updateCurrentDataSourceValue(DataSource<?, ?> source, String key, Object newValue) {
    // The source must be the same as the attached source & the key must
    // be valid in order to process the update.
    if (source == dataSource && isKeyValid(key)) {
      if (source instanceof Web) {
        // Get the columns from the local webColumns properties
        YailList columns = ((Web) source).getColumns(YailList.makeList(webColumns));

        // Set the current Data Source Value to all the tuples from the columns.
        // This is needed to easily remove values later on when the value changes
        // again.
        lastDataSourceValue = dataModel.getTuplesFromColumns(columns, true);
      } else if (source instanceof Spreadsheet) {
        YailList columns = ((Spreadsheet) source).getColumns(YailList.makeList(sheetsColumns),
            useSheetHeaders);
        lastDataSourceValue = dataModel.getTuplesFromColumns(columns, useSheetHeaders);
      } else {
        // Update current Data Source value
        lastDataSourceValue = newValue;
      }
    }
  }

  /**
   * Checks whether the provided key is compatible based on the current set
   * Data Source key.
   *
   * @param key Key to check
   * @return True if the key is equivalent to the current Data Source key
   */
  private boolean isKeyValid(String key) {
    // The key should either be equal to the local key, or null.
    return (key == null || key.equals(dataSourceKey));
  }


  /**
   * Casts list items to doubles.
   */
  public static List<Double> castToDouble(List<?> list) {
    List<Double> listDoubles = new ArrayList<>();
    for (Object o : list) {
      if (o instanceof Number) {
        listDoubles.add(((Number) o).doubleValue());
      } else {
        try {
          listDoubles.add(Double.parseDouble(o.toString()));
        } catch (NumberFormatException e) {
          // Do nothing (value already false)
        }
      }
    }
    return listDoubles;
  }
}
