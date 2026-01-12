---
layout: documentation
title: Charts
---

[&laquo; Back to index](index.html)
# Charts

Table of Contents:

* [Chart](#Chart)
* [ChartData2D](#ChartData2D)
* [Trendline](#Trendline)

## Chart  {#Chart}

The Chart component plots data originating from it's attached Data components. Five different
 Chart types are available, including Line, Area, Scatter, Bar and Pie, which can be changed by
 the [`Type`](#Chart.Type) property.
 The Chart component itself has various other properties that change the appearance
 of the Chart, such as {[`Description`](#Chart.Description), [`GridEnabled`](#Chart.GridEnabled),



### Properties  {#Chart-Properties}

{:.properties}

{:id="Chart.AxesTextColor" .color} *AxesTextColor*
: Specifies the chart's axes text color as an alpha-red-green-blue
 integer.

{:id="Chart.BackgroundColor" .color} *BackgroundColor*
: Specifies the chart's background color as an alpha-red-green-blue
 integer.

{:id="Chart.Description" .text} *Description*
: Specifies the text displayed by the description label inside the Chart.
 Specifying an empty string ("") will not display any label.

{:id="Chart.GridEnabled" .boolean} *GridEnabled*
: Changes the visibility of the Chart's grid, if the
 Chart Type is set to a Chart with an Axis (applies for Area, Bar, Line,
 Scatter Chart types).

{:id="Chart.Height" .number .bo} *Height*
: Specifies the `Chart`'s vertical height, measured in pixels.

{:id="Chart.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `Chart`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="Chart.Labels" .list .bo} *Labels*
: Changes the Chart's X axis labels to the specified List,
 if the Chart's Type is set to a Chart with an Axis.

   The first entry of the List corresponds to the minimum x value of the data,
 the second to the min x value + 1, and so on.

   If a label is not specified for an x value, a default value
 is used (the x value of the axis tick at that location).

{:id="Chart.LabelsFromString" .text .wo .do} *LabelsFromString*
: Specifies the labels to set to the Chart's X Axis, provided the current
 view is a Chart with an X Axis. The labels are specified as a single comma-separated
 values String (meaning each value is separated by a comma). See [`Labels`](#Chart.Labels)
 for more details on how the Labels are applied to the Chart.

{:id="Chart.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="Chart.LegendEnabled" .boolean} *LegendEnabled*
: Changes the visibility of the Chart's Legend.

{:id="Chart.PieRadius" .number .wo .do} *PieRadius*
: Sets the Pie Radius of the Chart. If the current type is
 not the Pie Chart, the value has no effect.

{:id="Chart.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="Chart.Type" .com.google.appinventor.components.common.ChartTypeEnum .ro} *Type*
: Specifies the type of the Chart, which determines how to visualize the data.

{:id="Chart.ValueFormat" .number .do} *ValueFormat*
: Specifies the format for X axis labels and point values.

{:id="Chart.Visible" .boolean} *Visible*
: Specifies whether the `Chart` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Chart` is showing and `false`{:.logic.block} if hidden.

{:id="Chart.Width" .number .bo} *Width*
: Specifies the horizontal width of the `Chart`, measured in pixels.

{:id="Chart.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `Chart` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

{:id="Chart.XFromZero" .boolean} *XFromZero*
: Determines whether the X axis origin is set at 0 or the minimum X value
 across all data series.

{:id="Chart.YFromZero" .boolean} *YFromZero*
: Determines whether the Y axis origin is set at 0 or the minimum y value
 across all data series.

### Events  {#Chart-Events}

{:.events}

{:id="Chart.EntryClick"} EntryClick(*series*{:.component},*x*{:.any},*y*{:.number})
: Indicates that the user clicked on a data entry in the `Chart`. The specific series, along
 with its x and y values, are reported.

### Methods  {#Chart-Methods}

{:.methods}

{:id="Chart.ExtendDomainToInclude" class="method"} <i/> ExtendDomainToInclude(*x*{:.number})
: Extends the domain of the chart to include the provided x value. If x is already within the
 bounds of the domain, this method has no effect.

{:id="Chart.ExtendRangeToInclude" class="method"} <i/> ExtendRangeToInclude(*y*{:.number})
: Extends the range of the chart to include the provided y value. If y is already within the
 bounds of the range, this method has no effect.

{:id="Chart.ResetAxes" class="method"} <i/> ResetAxes()
: Resets the axes of the chart to their original bounds.

{:id="Chart.SetDomain" class="method"} <i/> SetDomain(*minimum*{:.number},*maximum*{:.number})
: Sets the minimum and maximum for the domain of the X axis.

{:id="Chart.SetRange" class="method"} <i/> SetRange(*minimum*{:.number},*maximum*{:.number})
: Sets the minimum and maximum for the range of the Y axis.

## ChartData2D  {#ChartData2D}

A ChartData2D component represents a single two-dimensional Data Series in the Chart component,
 for example, a single Line in the case of a Line Chart, or a single Bar in the case of a Bar
 Chart. The Data component is responsible for handling all the data of the Chart. The entries
 of the Data component correspond of an x and a y value.
 The component is attached directly to a Chart component by dragging it onto the Chart.



### Properties  {#ChartData2D-Properties}

{:.properties}

{:id="ChartData2D.Color" .color} *Color*
: Specifies the data series color as an alpha-red-green-blue integer.

{:id="ChartData2D.Colors" .list .bo} *Colors*
: Specifies the data series colors as a list of alpha-red-green-blue integers.

   If there is more data than there are colors, the colors will be alternated
 in order. E.g. if there are two colors Red and Blue, the colors will be applied
 in the order: Red, Blue, Red, Blue, ...

{:id="ChartData2D.DataFileXColumn" .text .wo .do} *DataFileXColumn*
: Value used when importing data from a DataFile component [`Source`](#ChartData2D.Source). The
 value represents the column to use from the DataFile for the x entries
 of the Data Series. For instance, if a column's first value is "Time",
 and a column value of "Time" is specified, that column will be used
 for the x values. If a value here is not specified, default values for the
 x values will be generated instead.

{:id="ChartData2D.DataFileYColumn" .text .wo .do} *DataFileYColumn*
: Value used when importing data from a DataFile component [`Source`](#ChartData2D.Source). The
 value represents the column to use from the DataFile for the y entries
 of the Data Series. For instance, if a column's first value is "Temperature",
 and a column value of "Temperature" is specified, that column will be used
 for the y values. If a value here is not specified, default values for the
 y values will be generated instead.

{:id="ChartData2D.DataLabelColor" .color} *DataLabelColor*
: Specifies the data points label color as an alpha-red-green-blue integer.

{:id="ChartData2D.DataSourceKey" .text .wo .do} *DataSourceKey*
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

{:id="ChartData2D.ElementsFromPairs" .text .wo .do} *ElementsFromPairs*
: Comma separated list of Elements to use for the data series. Values are formatted
 as follows: x1,y1,x2,y2,x3,y3. Values are taken in pairs, and an entry is formed
 from the x and y values.

{:id="ChartData2D.Label" .text} *Label*
: Specifies the text for the data series label.

{:id="ChartData2D.LineType" .com.google.appinventor.components.common.LineTypeEnum .wo .do} *LineType*
: Changes the Line Type of the Data Series, provided that the
 Data component is attached to a Chart that has the type set to
 a line-based Chart(applies to area and line Chart types).
    Valid types include linear, curved or stepped.

{:id="ChartData2D.PointShape" .com.google.appinventor.components.common.PointStyleEnum .wo .do} *PointShape*
: Changes the Point Shape of the Data Series, provided that the
 Data component is attached to a Chart that has the type set to
 the Scatter Chart. Valid types include circle, square, triangle, cross, x.

{:id="ChartData2D.Source" .component .wo .do} *Source*
: Sets the Source to use for the Data component. Valid choices
 include AccelerometerSensor, BluetoothClient, CloudDB, DataFile,
 GyroscopeSensor, LocationSesnro, OrientationSensor, Pedometer,
 ProximitySensor TinyDB and Web components. The Source value also requires
 valid DataSourceValue, WebColumn or DataFileColumn properties,
 depending on the type of the Source attached (the required properties
 show up in the Properties menu after the Source is changed).

   If the data identified by the [`DataSourceKey`](#ChartData2D.DataSourceKey) is updated
 in the attached Data Source component, then the data is also updated in
 the Chart Data component.

{:id="ChartData2D.SpreadsheetUseHeaders" .boolean .wo .do} *SpreadsheetUseHeaders*
: If checked, the first row of the spreadsheet will be used to interpret the x and y column
 values. Otherwise, the x and y columns should be a column reference, such as A or B.

{:id="ChartData2D.SpreadsheetXColumn" .text .wo .do} *SpreadsheetXColumn*
: Sets the column to parse from the attached Spreadsheet component for the x values. If a
 column is not specified, default values for the x values will be generated instead.

{:id="ChartData2D.SpreadsheetYColumn" .text .wo .do} *SpreadsheetYColumn*
: Sets the column to parse from the attached Spreadsheet component for the y values. If a
 column is not specified, default values for the y values will be generated instead.

{:id="ChartData2D.WebXColumn" .text .wo .do} *WebXColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the x entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Time" tag exists,
 the "Time" column value can be specified to use that array.

{:id="ChartData2D.WebYColumn" .text .wo .do} *WebYColumn*
: Value used when importing data from a Web component Source. The
 value represents the column to use from the Web for the y entries
 of the Data Series. For instance, if the contents of the Web are
 retrieved in JSON format, and an array with the "Temperature" tag exists,
 the "Temperature" column value can be specified to use that array.

### Events  {#ChartData2D-Events}

{:.events}

{:id="ChartData2D.EntryClick"} EntryClick(*x*{:.any},*y*{:.number})
: Indicates that the user tapped on a data point in the chart. The x and y values of the
 tapped entry are reported.

### Methods  {#ChartData2D-Methods}

{:.methods}

{:id="ChartData2D.AddEntry" class="method"} <i/> AddEntry(*x*{:.text},*y*{:.text})
: Adds an entry with the specified x and y value. Values can be specified as text,
 or as numbers. For Line, Scatter, Area and Bar Charts, both values should represent a number.
 For Bar charts, the x value is rounded to the nearest integer.
 For Pie Charts, the x value is a text value.

{:id="ChartData2D.ChangeDataSource" class="method"} <i/> ChangeDataSource(*source*{:.component},*keyValue*{:.text})
: Changes the Data Source of the component to the specified component Source with the
 specified key value. See the [`Source`](#ChartData2D.Source) property for
 applicable components. See the [`DataSourceKey`](#ChartData2D.DataSourceKey) property for the interpretation
 of the keyValue. In the case of the DataFile and Web components, the keyValue is expected to
 be a CSV formatted string, where the first value corresponds to the x column, and the second
 value corresponds to the y value.

{:id="ChartData2D.Clear" class="method"} <i/> Clear()
: Removes all the entries from the Data Series.

{:id="ChartData2D.DoesEntryExist" class="method returns boolean"} <i/> DoesEntryExist(*x*{:.text},*y*{:.text})
: Returns a boolean value specifying whether an entry with the specified x and y
 values exists. The boolean value of true is returned if the value exists,
 and a false value otherwise. See [`AddEntry`](#ChartData2D.AddEntry)
 for an explanation of the valid entry values.

{:id="ChartData2D.GetAllEntries" class="method returns list"} <i/> GetAllEntries()
: Returns all entries of the data series.
 The returned value is a list, where each element of the list
 is a list containing the values of the entry in order.

{:id="ChartData2D.GetEntriesWithXValue" class="method returns list"} <i/> GetEntriesWithXValue(*x*{:.text})
: Returns all entries of the data series matching the specified x value.
 For a description of the format of the returned List, see [`GetAllEntries`](#ChartData2D.GetAllEntries)

{:id="ChartData2D.GetEntriesWithYValue" class="method returns list"} <i/> GetEntriesWithYValue(*y*{:.text})
: Returns all entries of the data series matching the specified y value.
 For a description of the format of the returned List, see [`GetAllEntries`](#ChartData2D.GetAllEntries)

{:id="ChartData2D.HighlightDataPoints" class="method"} <i/> HighlightDataPoints(*dataPoints*{:.list},*color*{:.number})
: Highlights all given data points on the Chart in the color of choice.

{:id="ChartData2D.ImportFromCloudDB" class="method"} <i/> ImportFromCloudDB(*cloudDB*{:.component},*tag*{:.text})
: Imports data from the specified CloudDB component by taking the value
 identified by the specified tag value.

   The expected CloudDB value is a list formatted in the same way as described in
 [`ImportFromList`](#ChartData2D.ImportFromList).

   Does not overwrite any data.

{:id="ChartData2D.ImportFromDataFile" class="method"} <i/> ImportFromDataFile(*dataFile*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified DataFile component by taking the specified x column
 for the x values, and the specified y column for the y values. The DataFile's source file
 is expected to be either a CSV or a JSON file.

   Passing in empty text for any of the column parameters will result in the usage of
 default values which are the indices of the entries. For the first entry, the default
 value would be the 1, for the second it would be 2, and so on.

{:id="ChartData2D.ImportFromList" class="method"} <i/> ImportFromList(*list*{:.list})
: Imports the data from the specified list parameter to the data series.
 The list is expected to contain element which are also lists. Each
 list element is expected to have 2 values, the first one being
 the x value, and the second one being the y value.
 Invalid list entries are simply skipped. Existing data are not cleared.

{:id="ChartData2D.ImportFromSpreadsheet" class="method"} <i/> ImportFromSpreadsheet(*spreadsheet*{:.component},*xColumn*{:.text},*yColumn*{:.text},*useHeaders*{:.boolean})
: Imports data from the specified Spreadsheet component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Spreadsheet component's ReadSheet method has to be called to load the data. The usage of
 the GotSheet event in the Spreadsheet component is unnecessary.

   Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="ChartData2D.ImportFromTinyDB" class="method"} <i/> ImportFromTinyDB(*tinyDB*{:.component},*tag*{:.text})
: Imports data from the specified TinyDB component by taking the value
 identified by the specified tag value.

   The expected TinyDB value is a list formatted in the same way as described in
 [`ImportFromList`](#ChartData2D.ImportFromList).

   Does not overwrite any data.

{:id="ChartData2D.ImportFromWeb" class="method"} <i/> ImportFromWeb(*web*{:.component},*xValueColumn*{:.text},*yValueColumn*{:.text})
: Imports data from the specified Web component by taking the specified x column
 for the x values, and the specified y column for the y values. Prior to calling this function,
 the Web component's Get method has to be called to load the data. The usage of the gotValue
 event in the Web component is unnecessary.

   The expected response of the Web component is a JSON or CSV formatted
 file for this function to work.

   Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, 3, ...).

{:id="ChartData2D.RemoveDataSource" class="method"} <i/> RemoveDataSource()
: Removes the currently attached Data Source from the Chart Data component.
 Doing so will result in no more updates from the Data Source being sent, however,
 the current data will not be removed.

{:id="ChartData2D.RemoveEntry" class="method"} <i/> RemoveEntry(*x*{:.text},*y*{:.text})
: Removes an entry with the specified x and y value, provided it exists.
 See [`AddEntry`](#ChartData2D.AddEntry) for an explanation of the valid entry values.

## Trendline  {#Trendline}

The Trendline component can be used to visualize the trend of a data series represented by a
 ChartData2D component. It must be added to a Chart component. To associate a ChartData2D
 instance, either set the ChartData property in the design view of the app or use the setter
 block. The Trendline will update automatically if its associated ChartData2D is changed.

 There are four models available for the Trendline: Linear, Quadratic, Logarithmic, and
 Exponential. Depending on which model you use, certain properties of the Trendline component
 will provide relevant values.

   * Linear: y = m*x + b, where m is LinearCoefficient and b is YIntercept
   * Quadratic: y = a\*x<sup>2</sup> + b*x + c, where a is QuadraticCoefficient, b is
     LinearCoefficient, and c is YIntercept
   * Logarithmic: y = a + b*ln(x), where a is LogarithmConstant and b is LogarithmCoefficient
   * Exponential: y = a*b<sup>x</sup>, where a is the ExponentialCoefficient and b is the
     ExponentialBase

 For all models, the r<sup>2</sup> correlation will be reported through the RSquared property
 block.



### Properties  {#Trendline-Properties}

{:.properties}

{:id="Trendline.ChartData" .component .wo} *ChartData*
: The data series for which to compute the line of best fit.

{:id="Trendline.Color" .color} *Color*
: The color of the line of best fit.

{:id="Trendline.CorrelationCoefficient" .number .ro .bo} *CorrelationCoefficient*
: The correlation coefficient of the trendline to the data.

{:id="Trendline.ExponentialBase" .number .ro .bo} *ExponentialBase*
: The base of the exponential term in the equation y = a*b^x.

{:id="Trendline.ExponentialCoefficient" .number .ro .bo} *ExponentialCoefficient*
: The coefficient of the exponential term in the equation y = a*b^x.

{:id="Trendline.Extend" .boolean} *Extend*
: Whether to extend the line of best fit beyond the data.

{:id="Trendline.LinearCoefficient" .number .ro .bo} *LinearCoefficient*
: The coefficient of the linear term in the trendline.

{:id="Trendline.LogarithmCoefficient" .number .ro .bo} *LogarithmCoefficient*
: The coefficient of the logarithmic term in the equation y = a + b*ln(x).

{:id="Trendline.LogarithmConstant" .number .ro .bo} *LogarithmConstant*
: The constant term in the logarithmic equation y = a + b*ln(x).

{:id="Trendline.Model" .com.google.appinventor.components.common.BestFitModelEnum} *Model*
: The model to use for the line of best fit.

{:id="Trendline.Predictions" .list .ro .bo} *Predictions*
: The predictions for the trendline.

{:id="Trendline.QuadraticCoefficient" .number .ro .bo} *QuadraticCoefficient*
: The coefficient of the quadratic term in the trendline, if any.

{:id="Trendline.RSquared" .number .ro .bo} *RSquared*
: The r-squared coefficient of determination for the trendline.

{:id="Trendline.Results" .dictionary .ro .bo} *Results*
: Obtain a copy of the most recent values computed by the line of best fit.

{:id="Trendline.StrokeStyle" .com.google.appinventor.components.common.StrokeStyleEnum} *StrokeStyle*
: The style of the best fit line.

{:id="Trendline.StrokeWidth" .number} *StrokeWidth*
: The width of the best fit line.

{:id="Trendline.Visible" .boolean} *Visible*
: Whether the line of best fit is visible.

{:id="Trendline.XIntercepts" .any .ro .bo} *XIntercepts*
: The X-intercepts of the trendline (where the line crosses the X-axis), if any. Possible
 values are NaN (no intercept), a single value (one intercept), or a list of values.

{:id="Trendline.YIntercept" .number .ro .bo} *YIntercept*
: The Y-intercept of the trendline (constant term).

### Events  {#Trendline-Events}

{:.events}

{:id="Trendline.Updated"} Updated(*results*{:.dictionary})
: Event indicating that the line of best fit has been updated.

### Methods  {#Trendline-Methods}

{:.methods}

{:id="Trendline.DisconnectFromChartData" class="method"} <i/> DisconnectFromChartData()
: Disconnect the Trendline from a previously associated ChartData2D.

{:id="Trendline.GetResultValue" class="method returns any"} <i/> GetResultValue(*value*{:.text})
: Get the field of the most recent values computed by the line of best fit. The available
 values vary based on the model used. For example, a linear model will have slope and
 Yintercept fields whereas a quadratic model will have x^2, slope, and intercept fields.
