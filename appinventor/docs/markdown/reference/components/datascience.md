---
layout: documentation
title: Data Science
---

[&laquo; Back to index](index.html)
# Data Science

Table of Contents:

* [Regression](#Regression)

## Regression  {#Regression}

A machine learning component to apply different ML models
 The component is attached directly to a Chart component by dragging it onto the Chart.



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

{:id="Regression.GetLineOfBestFitCorCoef" class="method returns number"} <i/> GetLineOfBestFitCorCoef(*xList*{:.list},*yList*{:.list})
: Gets the line of best fit correlation coefficient

{:id="Regression.GetLineOfBestFitSlope" class="method returns number"} <i/> GetLineOfBestFitSlope(*xList*{:.list},*yList*{:.list})
: Gets the line of best fit slope

{:id="Regression.GetLineOfBestFitYIntercept" class="method returns number"} <i/> GetLineOfBestFitYIntercept(*xList*{:.list},*yList*{:.list})
: Gets the line of best fit Intercept

{:id="Regression.ImportFromCloudDB" class="method"} <i/> ImportFromCloudDB(*cloudDB*{:.component},*tag*{:.text})
: Imports data from the specified CloudDB component by taking the value
 identified by the specified tag value.

   The expected CloudDB value is a list formatted in the same way as described in
 [`ImportFromList`](#Regression.ImportFromList).

   Does not overwrite any data.

{:id="Regression.ImportFromList" class="method"} <i/> ImportFromList(*list*{:.list})
: Imports the data from the specified list parameter to the data series.
 The list is expected to contain element which are also lists. Each
 list element is expected to have 2 values, the first one being
 the x value, and the second one being the y value.
 Invalid list entries are simply skipped. Existing data are not cleared.

{:id="Regression.ImportFromTinyDB" class="method"} <i/> ImportFromTinyDB(*tinyDB*{:.component},*tag*{:.text})
: Imports data from the specified TinyDB component by taking the value
 identified by the specified tag value.

   The expected TinyDB value is a list formatted in the same way as described in
 [`ImportFromList`](#Regression.ImportFromList).

   Does not overwrite any data.

{:id="Regression.RemoveDataSource" class="method"} <i/> RemoveDataSource()
: Removes the currently attached Data Source from the Chart Data component.
 Doing so will result in no more updates from the Data Source being sent, however,
 the current data will not be removed.
