---
layout: documentation
title: Data Science
---

[&laquo; Back to index](index.html)
# Data Science

Table of Contents:

* [AnomalyDetection](#AnomalyDetection)
* [Regression](#Regression)

## AnomalyDetection  {#AnomalyDetection}

A data science component to apply different anomaly detection models.
 The component only needs a data source to apply the model on.

 The anomaly detection models only return a list of anomalies.
 ChartData2D component is needed to highlight the anomalies on a chart



### Properties  {#AnomalyDetection-Properties}

{:.properties}

{:id="AnomalyDetection.DataFileXColumn" .text .wo .do} *DataFileXColumn*
: Value used when importing data from a DataFile component [`Source`](#AnomalyDetection.Source). The
 value represents the column to use from the DataFile for the x entries
 of the Data Series. For instance, if a column's first value is "Time",
 and a column value of "Time" is specified, that column will be used
 for the x values. If a value here is not specified, default values for the
 x values will be generated instead.

{:id="AnomalyDetection.DataFileYColumn" .text .wo .do} *DataFileYColumn*
: Value used when importing data from a DataFile component [`Source`](#AnomalyDetection.Source). The
 value represents the column to use from the DataFile for the y entries
 of the Data Series. For instance, if a column's first value is "Temperature",
 and a column value of "Temperature" is specified, that column will be used
 for the y values. If a value here is not specified, default values for the
 y values will be generated instead.

{:id="AnomalyDetection.DataSourceKey" .text .wo .do} *DataSourceKey*
: Sets the Data Source key identifier for the value to import from the
 attached Data Source.

    An example is the tag of the TinyDB component, which identifies the value.

    The property is a Designer-only property, and should be changed after setting the
 Source component of the Chart Data component.

    A complete list of applicable values for each compatible source is as follows:

 * For TinyDB and CloudDB, this is the tag value.
 * For the AccelerometerSensor, the value should be one of the following: X Y or Z
 * For the GyroscopeSensor, the value should be one of the following: X Y or Z
 * For the LocationSensor, the value should be one of the following:
 latitude, longitude, altitude or speed
 * For the OrientationSensor, the value should be one of the following:
 pitch, azimuth or roll
 * For the Pedometer, the value should be one of the following:
 WalkSteps, SimpleSteps or Distance
 * For the ProximitySensor, the value should be distance.
 * For the BluetoothClient, the value represents the prefix to remove from the value.
 For instance, if values come in the format "t:12", the prefix can be specified as "t:",
 and the prefix will then be removed from the data. No value can be specified if purely
 numerical values are returned.

{:id="AnomalyDetection.ElementsFromPairs" .text .wo .do} *ElementsFromPairs*
: Comma separated list of Elements to use for the data series. Values are formatted
 as follows: x1,y1,x2,y2,x3,y3. Values are taken in pairs, and an entry is formed
 from the x and y values.

{:id="AnomalyDetection.Source" .component .wo .do} *Source*
: Sets the Source to use for the Data component. Valid choices
 include AccelerometerSensor, BluetoothClient, CloudDB, DataFile,
 GyroscopeSensor, LocationSesnro, OrientationSensor, Pedometer,
 ProximitySensor TinyDB and Web components. The Source value also requires
 valid DataSourceValue, WebColumn or DataFileColumn properties,
 depending on the type of the Source attached (the required properties
 show up in the Properties menu after the Source is changed).

    If the data identified by the [`DataSourceKey`](#AnomalyDetection.DataSourceKey) is updated
 in the attached Data Source component, then the data is also updated in
 the Chart Data component.

{:id="AnomalyDetection.SpreadsheetUseHeaders" .boolean .wo .do} *SpreadsheetUseHeaders*
: If checked, the first row of the spreadsheet will be used to interpret the x and y column
 values. Otherwise, the x and y columns should be a column reference, such as A or B.

{:id="AnomalyDetection.SpreadsheetXColumn" .text .wo .do} *SpreadsheetXColumn*
: Sets the column to parse from the attached Spreadsheet component for the x values. If a
 column is not specified, default values for the x values will be generated instead.

{:id="AnomalyDetection.SpreadsheetYColumn" .text .wo .do} *SpreadsheetYColumn*
: Sets the column to parse from the attached Spreadsheet component for the y values. If a
 column is not specified, default values for the y values will be generated instead.

{:id="AnomalyDetection.WebXColumn" .text .wo .do} *WebXColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the x entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Time" tag exists,
 the "Time" column value can be specified to use that array.

{:id="AnomalyDetection.WebYColumn" .text .wo .do} *WebYColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the y entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Temperature" tag exists,
 the "Temperature" column value can be specified to use that array.

### Events  {#AnomalyDetection-Events}

{:.events}
None


### Methods  {#AnomalyDetection-Methods}

{:.methods}

{:id="AnomalyDetection.ChangeDataSource" class="method"} <i/> ChangeDataSource(*source*{:.component},*keyValue*{:.text})
: Changes the Data Source of the component to the specified component Source with the
 specified key value. See the [`Source`](#AnomalyDetection.Source) property for
 applicable components. See the [`DataSourceKey`](#AnomalyDetection.DataSourceKey) property for the interpretation
 of the keyValue. In the case of the DataFile and Web components, the keyValue is expected to
 be a CSV formatted string, where the first value corresponds to the x column, and the second
 value corresponds to the y value.

{:id="AnomalyDetection.CleanData" class="method returns list"} <i/> CleanData(*anomaly*{:.list},*xList*{:.list},*yList*{:.list})
: Given a single anomaly: [(anomaly index, anomaly value)]

 1. Iterate over the xList and delete value at anomaly index
 2. Iterate over the yList and delete the value at anomaly index with the same value as anomaly
    value
 3. combine the xList and yList after modification in a list of x and y pairs

 We assume x and y lists are the same size and are ordered.

{:id="AnomalyDetection.Clear" class="method"} <i/> Clear()
: Removes all the entries from the Data Series.

{:id="AnomalyDetection.DetectAnomalies" class="method returns list"} <i/> DetectAnomalies(*dataList*{:.list},*threshold*{:.number})
: Calculates the mean and standard deviation of the data, and then checks each data point's
 Z-score against the threshold. If a data point's Z-score is greater than the threshold,
 the data point is labeled as anomaly.

{:id="AnomalyDetection.GetAllEntries" class="method returns list"} <i/> GetAllEntries()
: Returns all entries of the data series.
 The returned value is a list, where each element of the list
 is a list containing the values of the entry in order.

{:id="AnomalyDetection.GetEntriesWithXValue" class="method returns list"} <i/> GetEntriesWithXValue(*x*{:.text})
: Returns all entries of the data series matching the specified x value.
 For a description of the format of the returned List, see [`GetAllEntries`](#AnomalyDetection.GetAllEntries)

{:id="AnomalyDetection.GetEntriesWithYValue" class="method returns list"} <i/> GetEntriesWithYValue(*y*{:.text})
: Returns all entries of the data series matching the specified y value.
 For a description of the format of the returned List, see [`GetAllEntries`](#AnomalyDetection.GetAllEntries)

{:id="AnomalyDetection.ImportFromCloudDB" class="method"} <i/> ImportFromCloudDB(*cloudDB*{:.component},*tag*{:.text})
: Imports data from the specified CloudDB component by taking the value
 identified by the specified tag value.

    The expected CloudDB value is a list formatted in the same way as described in
 [`ImportFromList`](#AnomalyDetection.ImportFromList).

    Does not overwrite any data.

{:id="AnomalyDetection.ImportFromDataFile" class="method"} <i/> ImportFromDataFile(*dataFile*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified DataFile component by taking the specified x column
 for the x values, and the specified y column for the y values. The DataFile's source file
 is expected to be either a CSV or a JSON file.

    Passing in empty test for any of the column parameters will result in the usage of
 default values which are the indices of the entries. For the first entry, the default
 value would be the 1, for the second it would be 2, and so on.

{:id="AnomalyDetection.ImportFromList" class="method"} <i/> ImportFromList(*list*{:.list})
: Imports the data from the specified list parameter to the data series.
 The list is expected to contain element which are also lists. Each
 list element is expected to have 2 values, the first one being
 the x value, and the second one being the y value.
 Invalid list entries are simply skipped. Existing data are not cleared.

{:id="AnomalyDetection.ImportFromSpreadsheet" class="method"} <i/> ImportFromSpreadsheet(*sheet*{:.component},*xColumn*{:.text},*yColumn*{:.text},*useHeaders*{:.boolean})
: Imports data from the specified Spreadsheet component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Spreadsheet component's ReadSheet method has to be called to load the data. The usage of
 the GotSheet event in the Spreadsheet component is unnecessary.

    Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="AnomalyDetection.ImportFromTinyDB" class="method"} <i/> ImportFromTinyDB(*tinyDB*{:.component},*tag*{:.text})
: Imports data from the specified TinyDB component by taking the value
 identified by the specified tag value.

    The expected TinyDB value is a list formatted in the same way as described in
 [`ImportFromList`](#AnomalyDetection.ImportFromList).

    Does not overwrite any data.

{:id="AnomalyDetection.ImportFromWeb" class="method"} <i/> ImportFromWeb(*web*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified Web component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Web component's Get method has to be called to load the data. The usage of the gotValue
 event in the Web component is unnecessary.

    The expected response of the Web component is a JSON or CSV formatted
 file for this function to work.

    Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="AnomalyDetection.RemoveDataSource" class="method"} <i/> RemoveDataSource()
: Removes the currently attached Data Source from the Chart Data component.
 Doing so will result in no more updates from the Data Source being sent, however,
 the current data will not be removed.

## Regression  {#Regression}

A data science component to apply different regression models.
 The component only requires a data source to apply the model on.

 The component is only responsible for the statistical calculations and
 provides the following properties for line of best fit:
 "slope", "Yintercept", "correlation coefficient", and "predictions"

 To draw the line of best fit use the drawing block in ChartData2D component



### Properties  {#Regression-Properties}

{:.properties}

{:id="Regression.DataFileXColumn" .text .wo .do} *DataFileXColumn*
: Value used when importing data from a DataFile component [`Source`](#Regression.Source). The
 value represents the column to use from the DataFile for the x entries
 of the Data Series. For instance, if a column's first value is "Time",
 and a column value of "Time" is specified, that column will be used
 for the x values. If a value here is not specified, default values for the
 x values will be generated instead.

{:id="Regression.DataFileYColumn" .text .wo .do} *DataFileYColumn*
: Value used when importing data from a DataFile component [`Source`](#Regression.Source). The
 value represents the column to use from the DataFile for the y entries
 of the Data Series. For instance, if a column's first value is "Temperature",
 and a column value of "Temperature" is specified, that column will be used
 for the y values. If a value here is not specified, default values for the
 y values will be generated instead.

{:id="Regression.DataSourceKey" .text .wo .do} *DataSourceKey*
: Sets the Data Source key identifier for the value to import from the
 attached Data Source.

    An example is the tag of the TinyDB component, which identifies the value.

    The property is a Designer-only property, and should be changed after setting the
 Source component of the Chart Data component.

    A complete list of applicable values for each compatible source is as follows:

 * For TinyDB and CloudDB, this is the tag value.
 * For the AccelerometerSensor, the value should be one of the following: X Y or Z
 * For the GyroscopeSensor, the value should be one of the following: X Y or Z
 * For the LocationSensor, the value should be one of the following:
 latitude, longitude, altitude or speed
 * For the OrientationSensor, the value should be one of the following:
 pitch, azimuth or roll
 * For the Pedometer, the value should be one of the following:
 WalkSteps, SimpleSteps or Distance
 * For the ProximitySensor, the value should be distance.
 * For the BluetoothClient, the value represents the prefix to remove from the value.
 For instance, if values come in the format "t:12", the prefix can be specified as "t:",
 and the prefix will then be removed from the data. No value can be specified if purely
 numerical values are returned.

{:id="Regression.ElementsFromPairs" .text .wo .do} *ElementsFromPairs*
: Comma separated list of Elements to use for the data series. Values are formatted
 as follows: x1,y1,x2,y2,x3,y3. Values are taken in pairs, and an entry is formed
 from the x and y values.

{:id="Regression.Source" .component .wo .do} *Source*
: Sets the Source to use for the Data component. Valid choices
 include AccelerometerSensor, BluetoothClient, CloudDB, DataFile,
 GyroscopeSensor, LocationSesnro, OrientationSensor, Pedometer,
 ProximitySensor TinyDB and Web components. The Source value also requires
 valid DataSourceValue, WebColumn or DataFileColumn properties,
 depending on the type of the Source attached (the required properties
 show up in the Properties menu after the Source is changed).

    If the data identified by the [`DataSourceKey`](#Regression.DataSourceKey) is updated
 in the attached Data Source component, then the data is also updated in
 the Chart Data component.

{:id="Regression.SpreadsheetUseHeaders" .boolean .wo .do} *SpreadsheetUseHeaders*
: If checked, the first row of the spreadsheet will be used to interpret the x and y column
 values. Otherwise, the x and y columns should be a column reference, such as A or B.

{:id="Regression.SpreadsheetXColumn" .text .wo .do} *SpreadsheetXColumn*
: Sets the column to parse from the attached Spreadsheet component for the x values. If a
 column is not specified, default values for the x values will be generated instead.

{:id="Regression.SpreadsheetYColumn" .text .wo .do} *SpreadsheetYColumn*
: Sets the column to parse from the attached Spreadsheet component for the y values. If a
 column is not specified, default values for the y values will be generated instead.

{:id="Regression.WebXColumn" .text .wo .do} *WebXColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the x entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Time" tag exists,
 the "Time" column value can be specified to use that array.

{:id="Regression.WebYColumn" .text .wo .do} *WebYColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the y entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Temperature" tag exists,
 the "Temperature" column value can be specified to use that array.

### Events  {#Regression-Events}

{:.events}
None


### Methods  {#Regression-Methods}

{:.methods}

{:id="Regression.CalculateLineOfBestFitValue" class="method returns any"} <i/> CalculateLineOfBestFitValue(*xList*{:.list},*yList*{:.list},*value*{:.com.google.appinventor.components.common.LOBFValuesEnum})
: Returns one of the Line of Best Fit values.
 A value could be "slope", "Yintercept", "correlation coefficient", "predictions" or a
 dictionary with all values above if nothing specific provided.

{:id="Regression.ChangeDataSource" class="method"} <i/> ChangeDataSource(*source*{:.component},*keyValue*{:.text})
: Changes the Data Source of the component to the specified component Source with the
 specified key value. See the [`Source`](#Regression.Source) property for
 applicable components. See the [`DataSourceKey`](#Regression.DataSourceKey) property for the interpretation
 of the keyValue. In the case of the DataFile and Web components, the keyValue is expected to
 be a CSV formatted string, where the first value corresponds to the x column, and the second
 value corresponds to the y value.

{:id="Regression.Clear" class="method"} <i/> Clear()
: Removes all the entries from the Data Series.

{:id="Regression.GetAllEntries" class="method returns list"} <i/> GetAllEntries()
: Returns all entries of the data series.
 The returned value is a list, where each element of the list
 is a list containing the values of the entry in order.

{:id="Regression.GetEntriesWithXValue" class="method returns list"} <i/> GetEntriesWithXValue(*x*{:.text})
: Returns all entries of the data series matching the specified x value.
 For a description of the format of the returned List, see [`GetAllEntries`](#Regression.GetAllEntries)

{:id="Regression.GetEntriesWithYValue" class="method returns list"} <i/> GetEntriesWithYValue(*y*{:.text})
: Returns all entries of the data series matching the specified y value.
 For a description of the format of the returned List, see [`GetAllEntries`](#Regression.GetAllEntries)

{:id="Regression.ImportFromCloudDB" class="method"} <i/> ImportFromCloudDB(*cloudDB*{:.component},*tag*{:.text})
: Imports data from the specified CloudDB component by taking the value
 identified by the specified tag value.

    The expected CloudDB value is a list formatted in the same way as described in
 [`ImportFromList`](#Regression.ImportFromList).

    Does not overwrite any data.

{:id="Regression.ImportFromDataFile" class="method"} <i/> ImportFromDataFile(*dataFile*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified DataFile component by taking the specified x column
 for the x values, and the specified y column for the y values. The DataFile's source file
 is expected to be either a CSV or a JSON file.

    Passing in empty test for any of the column parameters will result in the usage of
 default values which are the indices of the entries. For the first entry, the default
 value would be the 1, for the second it would be 2, and so on.

{:id="Regression.ImportFromList" class="method"} <i/> ImportFromList(*list*{:.list})
: Imports the data from the specified list parameter to the data series.
 The list is expected to contain element which are also lists. Each
 list element is expected to have 2 values, the first one being
 the x value, and the second one being the y value.
 Invalid list entries are simply skipped. Existing data are not cleared.

{:id="Regression.ImportFromSpreadsheet" class="method"} <i/> ImportFromSpreadsheet(*sheet*{:.component},*xColumn*{:.text},*yColumn*{:.text},*useHeaders*{:.boolean})
: Imports data from the specified Spreadsheet component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Spreadsheet component's ReadSheet method has to be called to load the data. The usage of
 the GotSheet event in the Spreadsheet component is unnecessary.

    Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="Regression.ImportFromTinyDB" class="method"} <i/> ImportFromTinyDB(*tinyDB*{:.component},*tag*{:.text})
: Imports data from the specified TinyDB component by taking the value
 identified by the specified tag value.

    The expected TinyDB value is a list formatted in the same way as described in
 [`ImportFromList`](#Regression.ImportFromList).

    Does not overwrite any data.

{:id="Regression.ImportFromWeb" class="method"} <i/> ImportFromWeb(*web*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified Web component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Web component's Get method has to be called to load the data. The usage of the gotValue
 event in the Web component is unnecessary.

    The expected response of the Web component is a JSON or CSV formatted
 file for this function to work.

    Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="Regression.RemoveDataSource" class="method"} <i/> RemoveDataSource()
: Removes the currently attached Data Source from the Chart Data component.
 Doing so will result in no more updates from the Data Source being sent, however,
 the current data will not be removed.
