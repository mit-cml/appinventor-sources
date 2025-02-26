---
layout: documentation
title: Storage
---

[&laquo; Back to index](index.html)
# Storage

Table of Contents:

* [CloudDB](#CloudDB)
* [DataFile](#DataFile)
* [File](#File)
* [Spreadsheet](#Spreadsheet)
* [TinyDB](#TinyDB)
* [TinyWebDB](#TinyWebDB)

## CloudDB  {#CloudDB}

The `CloudDB` component is a Non-visible component that allows you to store data on a Internet
 connected database server (using Redis software). This allows the users of your App to share
 data with each other. By default data will be stored in a server maintained by MIT, however you
 can setup and run your own server. Set the [`RedisServer`](#CloudDB.RedisServer) property and
 [`RedisPort`](#CloudDB.RedisPort) property to access your own server.



### Properties  {#CloudDB-Properties}

{:.properties}

{:id="CloudDB.ProjectID" .text .ro} *ProjectID*
: Gets the ProjectID for this CloudDB project.

{:id="CloudDB.RedisPort" .number .ro} *RedisPort*
: The Redis Server port to use. Defaults to 6381

{:id="CloudDB.RedisServer" .text .ro} *RedisServer*
: The Redis Server to use to store data. A setting of "DEFAULT" means that the MIT server will be used.

{:id="CloudDB.Token" .text .ro .do} *Token*
: This field contains the authentication token used to login to the backed Redis server. For the
 "DEFAULT" server, do not edit this value, the system will fill it in for you. A system
 administrator may also provide a special value to you which can be used to share data between
 multiple projects from multiple people. If using your own Redis server, set a password in the
 server's config and enter it here.

{:id="CloudDB.UseSSL" .boolean .ro .do} *UseSSL*
: Set to true to use SSL to talk to CloudDB/Redis server. This should be set to True for the "DEFAULT" server.

### Events  {#CloudDB-Events}

{:.events}

{:id="CloudDB.CloudDBError"} CloudDBError(*message*{:.text})
: Indicates that an error occurred while communicating with the CloudDB Redis server.

{:id="CloudDB.DataChanged"} DataChanged(*tag*{:.text},*value*{:.any})
: Indicates that the data in the CloudDB project has changed. Launches an event with the
 `tag`{:.text.block} that has been updated and the `value`{:.variable.block} it now has.

{:id="CloudDB.FirstRemoved"} FirstRemoved(*value*{:.any})
: Event triggered by the [`RemoveFirstFromList`](#CloudDB.RemoveFirstFromList) function. The argument
 `value`{:.variable.block} is the object that was the first in the list, and which is now
 removed.

{:id="CloudDB.GotValue"} GotValue(*tag*{:.text},*value*{:.any})
: Indicates that a [`GetValue`](#CloudDB.GetValue) request has succeeded.

{:id="CloudDB.TagList"} TagList(*value*{:.list})
: Event triggered when we have received the list of known tags. Run in response to a call to the
 [`GetTagList`](#CloudDB.GetTagList) function.

{:id="CloudDB.UpdateDone"} UpdateDone(*tag*{:.text},*operation*{:.text})
: Indicates that operations that store data to CloudDB have completed.

### Methods  {#CloudDB-Methods}

{:.methods}

{:id="CloudDB.AppendValueToList" class="method"} <i/> AppendValueToList(*tag*{:.text},*itemToAdd*{:.any})
: Append a value to the end of a list atomically. If two devices use this function simultaneously, both will be appended and no data lost.

{:id="CloudDB.ClearTag" class="method"} <i/> ClearTag(*tag*{:.text})
: Remove the tag from CloudDB.

{:id="CloudDB.CloudConnected" class="method returns boolean"} <i/> CloudConnected()
: Returns `true`{:.logic.block} if we are on the network and will likely be able to connect to
 the `CloudDB` server.

{:id="CloudDB.GetTagList" class="method"} <i/> GetTagList()
: Asks `CloudDB` to retrieve all the tags belonging to this project. The
 resulting list is returned in the event [`TagList`](#CloudDB.TagList).

{:id="CloudDB.GetValue" class="method"} <i/> GetValue(*tag*{:.text},*valueIfTagNotThere*{:.any})
: `GetValue` asks `CloudDB` to get the value stored under the given tag.
 It will pass the result to the [`GotValue`](#CloudDB.GotValue) will be given.

{:id="CloudDB.RemoveFirstFromList" class="method"} <i/> RemoveFirstFromList(*tag*{:.text})
: Obtain the first element of a list and atomically remove it. If two devices use this function
 simultaneously, one will get the first element and the the other will get the second element,
 or an error if there is no available element. When the element is available, the
 [`FirstRemoved`](#CloudDB.FirstRemoved) event will be triggered.

{:id="CloudDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Asks `CloudDB` to store the given `value`{:.variable.block} under the given
 `tag`{:.text.block}.

## DataFile  {#DataFile}

Component that allows reading CSV and JSON data. The DataFile contains functionality relevant to accessing CSV or
 JSON parsed data in the form of rows or columns. Can be used together with the ChartData2D component to import data
 directly from a file to the Chart. The component may also be dragged and dropped on a Chart after a file has been
 selected and parsed successfully to create ChartData components automatically from the file onto the Chart.



### Properties  {#DataFile-Properties}

{:.properties}

{:id="DataFile.ColumnNames" .list .ro .bo} *ColumnNames*
: Retrieve the column names of the currently loaded Source file.
 For CSV files, this will return a List of entries in the first row.
 For JSON files, this will return a List of keys in the JSON object.

{:id="DataFile.Columns" .list .ro .bo} *Columns*
: Retrieve a List of columns of the currently loaded Source file.

{:id="DataFile.DefaultScope" .com.google.appinventor.components.common.FileScopeEnum .wo .do} *DefaultScope*
: Specifies the default scope for files accessed using the File component. The App scope should
 work for most apps. Legacy mode can be used for apps that predate the newer constraints in
 Android on app file access.

{:id="DataFile.Rows" .list .ro .bo} *Rows*
: Retrieve a List of rows of the currently loaded Source file.

{:id="DataFile.SourceFile" .text .wo .do} *SourceFile*
: Sets the source file to parse data from, and then parses the
 file asynchronously. The results are stored in the [`Columns`](#DataFile.Columns),
 [`Rows`](#DataFile.Rows) and [`ColumnNames`](#DataFile.ColumnNames) properties.
 The expected formatting of the file is either the CSV or JSON format.

### Events  {#DataFile-Events}

{:.events}
None


### Methods  {#DataFile-Methods}

{:.methods}

{:id="DataFile.ReadFile" class="method"} <i/> ReadFile(*fileName*{:.text})
: Indicates source file to load data from. The expected format of the contents of the file
 are either CSV or JSON. Prefix the `fileName`{:.text.block} with `/` to read from a
 specific file on the SD card (for example, `/myFile.txt` will read the file
 `/sdcard/myFile.txt`). To read assets packaged with an application (also works for the
 Companion) start the `fileName`{:.text.block} with `//` (two slashes). If a
 `fileName`{:.text.block} does not start with a slash, it will be read from the application's
 private storage (for packaged apps) and from `/sdcard/AppInventor/data` for the Companion.

## File  {#File}

Non-visible component for storing and retrieving files. Use this component to write or read files
 on the device.

 The exact location where external files are placed is a function of the value of the
 [`Scope`](#File.Scope) property, whether the app is running in the Companion or compiled,
 and which version of Android the app is running on.

   Because newer versions of Android require files be stored in app-specific directories, the
 `DefaultScope` is set to `App`. If you are using an older version of Android and need access to
 the legacy public storage, change the `DefaultScope` property to `Legacy`. You can also change
 the `Scope` using the blocks.

   Below we briefly describe each scope type:

   - App: Files will be read from and written to app-specific storage on Android 2.2 and
     higher. On earlier versions of Android, files will be written to legacy storage.
   - Asset: Files will be read from the app assets. It is an error to attempt to write to app
     assets as they are contained in read-only storage.
   - Cache: Files will be read from and written to the app's cache directory. Cache is useful for
     temporary files that can be recreated as it allows the user to clear temporary files to get
     back storage space.
   - Legacy: Files will be read from and written to the file system using the App Inventor rules
     prior to release nb187. That is, file names starting with a single `/` will be read from and
     written to the root of the external storage directory, e.g., `/sdcard/`. Legacy functionality
     ***will not work*** on Android 11 or later.
   - Private: Files will be read from and written to the app's private directory. Use this scope
     to store information that shouldn't be visible to other applications, such as file
     management apps.
   - Shared: Files will be read from and written to the device's shared media directories, such
     as `Pictures`.

 Note 1: In Legacy mode, file names can take one of three forms:

  - Private files have no leading `/` and are written to app private storage (e.g., "file.txt")
  - External files have a single leading `/` and are written to public storage (e.g., "/file.txt")
  - Bundled app assets have two leading `//` and can only be read (e.g., "//file.txt")

 Note 2: In all scopes, a file name beginning with two slashes (`//`) will be interpreted as an
 asset name.



### Properties  {#File-Properties}

{:.properties}

{:id="File.DefaultScope" .com.google.appinventor.components.common.FileScopeEnum .wo .do} *DefaultScope*
: Specifies the default scope for files accessed using the File component. The App scope should
 work for most apps. Legacy mode can be used for apps that predate the newer constraints in
 Android on app file access.

{:id="File.ReadPermission" .boolean .wo .do} *ReadPermission*
: A designer-only property that can be used to enable read access to file storage outside of the
 app-specific directories.

{:id="File.Scope" .com.google.appinventor.components.common.FileScopeEnum .bo} *Scope*
: Indicates the current scope for operations such as ReadFrom and SaveFile.

{:id="File.WritePermission" .boolean .wo .do} *WritePermission*
: A designer-only property that can be used to enable write access to file storage outside of the
 app-specific directories.

### Events  {#File-Events}

{:.events}

{:id="File.AfterFileSaved"} AfterFileSaved(*fileName*{:.text})
: Event indicating that the contents of the file have been written.

{:id="File.GotText"} GotText(*text*{:.text})
: Event indicating that the contents from the file have been read.

### Methods  {#File-Methods}

{:.methods}

{:id="File.AppendToFile" class="method"} <i/> AppendToFile(*text*{:.text},*fileName*{:.text})
: Appends text to the end of a file. Creates the file if it does not already exist. See the help
 text under [`SaveFile`](#File.SaveFile) for information about where files are written.
 On success, the [`AfterFileSaved`](#File.AfterFileSaved) event will run.

{:id="File.CopyFile" class="method returns boolean"} <i/> CopyFile(*fromScope*{:.com.google.appinventor.components.common.FileScopeEnum},*fromFileName*{:.text},*toScope*{:.com.google.appinventor.components.common.FileScopeEnum},*toFileName*{:.text})
: Copy the contents from the first file to the second file.

{:id="File.Delete" class="method"} <i/> Delete(*fileName*{:.text})
: Deletes a file from storage. Prefix the `fileName`{:.text.block} with `/` to delete a specific
 file in the SD card (for example, `/myFile.txt` will delete the file `/sdcard/myFile.txt`).
 If the `fileName`{:.text.block} does not begin with a `/`, then the file located in the
 program's private storage will be deleted. Starting the `fileName`{:.text.block} with `//` is
 an error because asset files cannot be deleted.

{:id="File.Exists" class="method returns boolean"} <i/> Exists(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*path*{:.text})
: Tests whether the path exists in the given scope.

{:id="File.IsDirectory" class="method returns boolean"} <i/> IsDirectory(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*path*{:.text})
: Tests whether the path named in the given scope is a directory.

{:id="File.ListDirectory" class="method returns list"} <i/> ListDirectory(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*directoryName*{:.text})
: Get a list of files and directories in the given directory.

{:id="File.MakeDirectory" class="method returns boolean"} <i/> MakeDirectory(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*directoryName*{:.text})
: Create a new directory for storing files. The semantics of this method are such that it will
 return true if the directory exists at its completion. This can mean that the directory already
 existed prior to the call.

{:id="File.MakeFullPath" class="method returns text"} <i/> MakeFullPath(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*path*{:.text})
: Converts the scope and path into a single string for other components.

{:id="File.MoveFile" class="method returns boolean"} <i/> MoveFile(*fromScope*{:.com.google.appinventor.components.common.FileScopeEnum},*fromFileName*{:.text},*toScope*{:.com.google.appinventor.components.common.FileScopeEnum},*toFileName*{:.text})
: Move a file from one location to another.

{:id="File.ReadFrom" class="method"} <i/> ReadFrom(*fileName*{:.text})
: Reads text from a file in storage. Prefix the `fileName`{:.text.block} with `/` to read from a
 specific file on the SD card (for example, `/myFile.txt` will read the file
 `/sdcard/myFile.txt`). To read assets packaged with an application (also works for the
 Companion) start the `fileName`{:.text.block} with `//` (two slashes). If a
 `fileName`{:.text.block} does not start with a slash, it will be read from the application's
 private storage (for packaged apps) and from `/sdcard/AppInventor/data` for the Companion.

{:id="File.RemoveDirectory" class="method returns boolean"} <i/> RemoveDirectory(*scope*{:.com.google.appinventor.components.common.FileScopeEnum},*directoryName*{:.text},*recursive*{:.boolean})
: Remove a directory from the file system. If recursive is true, then everything is removed. If
 recursive is false, only the directory is removed and only if it is empty.

{:id="File.SaveFile" class="method"} <i/> SaveFile(*text*{:.text},*fileName*{:.text})
: Saves text to a file. If the `fileName`{:.text.block} begins with a slash (`/`) the file is
 written to the sdcard (for example, writing to `/myFile.txt` will write the file to
 `/sdcard/myFile.txt`). If the `fileName`{:.text.block} does not start with a slash, it will be
 written in the program's private data directory where it will not be accessible to other
 programs on the phone. There is a special exception for the AI Companion where these files are
 written to `/sdcard/AppInventor/data` to facilitate debugging.

   Note that this block will overwrite a file if it already exists. If you want to add content
 to an existing file use the [`AppendToFile`](#File.AppendToFile) method.

## Spreadsheet  {#Spreadsheet}

Spreadsheet is a non-visible component for storing and receiving data from
 a Google Sheets document using the Google Sheets API.

 In order to utilize this component, one must first have a Google Developer
 Account. Then, one must create a new project under that Google Developer
 Account, enable the Google Sheets API on that project, and finally create a
 Service Account for the Sheets API.

 Instructions on how to create the Service Account, as well as where to find
 other relevant information for using the Google Sheets Component, can be
 found <a href='/reference/other/googlesheets-api-setup.html'>here</a>.

 Row and column numbers are 1-indexed.



### Properties  {#Spreadsheet-Properties}

{:.properties}

{:id="Spreadsheet.ApplicationName" .text .do} *ApplicationName*
: The name of your application, used when making API calls.

{:id="Spreadsheet.CredentialsJson" .text} *CredentialsJson*
: The JSON File with credentials for the Service Account

{:id="Spreadsheet.SpreadsheetID" .text} *SpreadsheetID*
: The ID for the Google Sheets file you want to edit. You can find the spreadsheetID in the URL of the Google Sheets file.

### Events  {#Spreadsheet-Events}

{:.events}

{:id="Spreadsheet.ErrorOccurred"} ErrorOccurred(*errorMessage*{:.text})
: Triggered whenever an API call encounters an error. Details about the error are in `errorMessage`.

{:id="Spreadsheet.FinishedAddColumn"} FinishedAddColumn(*columnNumber*{:.number})
: The callback event for the [`AddColumn`](#Spreadsheet.AddColumn) block, called once the
 values on the table have been updated. Additionally, this returns the
 column number for the new column.

{:id="Spreadsheet.FinishedAddRow"} FinishedAddRow(*rowNumber*{:.number})
: The callback event for the [`AddRow`](#Spreadsheet.AddRow) block, called once the
 values on the table have been updated. Additionally, this returns the
 row number for the new row.

{:id="Spreadsheet.FinishedAddSheet"} FinishedAddSheet(*sheetName*{:.text})
: The callback event for the addSheet block, called once the values on the table have been updated.

{:id="Spreadsheet.FinishedClearRange"} FinishedClearRange()
: The callback event for the [`ClearRange`](#Spreadsheet.ClearRange) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedDeleteSheet"} FinishedDeleteSheet(*sheetName*{:.text})
: The callback event for the DeleteSheet block, called once the values on the table have been updated.

{:id="Spreadsheet.FinishedRemoveColumn"} FinishedRemoveColumn()
: The callback event for the [`RemoveColumn`](#Spreadsheet.RemoveColumn) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedRemoveRow"} FinishedRemoveRow()
: The callback event for the [`RemoveRow`](#Spreadsheet.RemoveRow) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedWriteCell"} FinishedWriteCell()
: The callback event for the [`WriteCell`](#Spreadsheet.WriteCell) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedWriteColumn"} FinishedWriteColumn()
: The callback event for the [`WriteColumn`](#Spreadsheet.WriteColumn) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedWriteRange"} FinishedWriteRange()
: The callback event for the [`WriteRange`](#Spreadsheet.WriteRange) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.FinishedWriteRow"} FinishedWriteRow()
: The callback event for the [`WriteRow`](#Spreadsheet.WriteRow) block, called once the
 values on the table have been updated.

{:id="Spreadsheet.GotCellData"} GotCellData(*cellData*{:.text})
: The callback event for the [`ReadCell`](#Spreadsheet.ReadCell) block. The `cellData` is
 the text value in the cell.

{:id="Spreadsheet.GotColumnData"} GotColumnData(*columnData*{:.list})
: The callback event for the [`ReadColumn`](#Spreadsheet.ReadColumn) block. The `columnData` is a
 list of text cell-values in order of increasing row number.

{:id="Spreadsheet.GotRangeData"} GotRangeData(*rangeData*{:.list})
: The callback event for the [`ReadRange`](#Spreadsheet.ReadRange) block. The `rangeData` is
 a list of rows, where the dimensions are the same as the rangeReference.

{:id="Spreadsheet.GotRowData"} GotRowData(*rowDataList*{:.list})
: The callback event for the [`ReadRow`](#Spreadsheet.ReadRow) block. The `rowDataList` is a
 list of text cell-values in order of increasing column number.

{:id="Spreadsheet.GotSheetData"} GotSheetData(*sheetData*{:.list})
: The callback event for the [`ReadSheet`](#Spreadsheet.ReadSheet) block. The `sheetData` is a
 list of rows.

### Methods  {#Spreadsheet-Methods}

{:.methods}

{:id="Spreadsheet.AddColumn" class="method"} <i/> AddColumn(*sheetName*{:.text},*data*{:.list})
: Given a list of values as `data`, appends the values to the next empty
 column of the sheet. It will always start from the top row and continue
 downwards. Once complete, it triggers the [`FinishedAddColumn`](#Spreadsheet.FinishedAddColumn)
 callback event.

{:id="Spreadsheet.AddRow" class="method"} <i/> AddRow(*sheetName*{:.text},*data*{:.list})
: Given a list of values as `data`, appends the values to the next
 empty row of the sheet. It will always start from the left most column and
 continue to the right. Once complete, it triggers the [`FinishedAddRow`](#Spreadsheet.FinishedAddRow)
 callback event. Additionally, this returns the row number for the new row.

{:id="Spreadsheet.AddSheet" class="method"} <i/> AddSheet(*sheetName*{:.text})
: Adds a new sheet inside the Spreadsheet.

{:id="Spreadsheet.ClearRange" class="method"} <i/> ClearRange(*sheetName*{:.text},*rangeReference*{:.text})
: Empties the cells in the given range. Once complete, this block triggers
 the [`FinishedClearRange`](#Spreadsheet.FinishedClearRange) callback event.

{:id="Spreadsheet.DeleteSheet" class="method"} <i/> DeleteSheet(*sheetName*{:.text})
: Deletes the specified sheet inside the Spreadsheet.

{:id="Spreadsheet.GetCellReference" class="method returns text"} <i/> GetCellReference(*row*{:.number},*column*{:.number})
: Converts the integer representation of rows and columns to A1-Notation used
 in Google Sheets for a single cell. For example, row 1 and col 2
 corresponds to the string \"B1\".

{:id="Spreadsheet.GetRangeReference" class="method returns text"} <i/> GetRangeReference(*row1*{:.number},*column1*{:.number},*row2*{:.number},*column2*{:.number})
: Converts the integer representation of rows and columns for the corners of
 the range to A1-Notation used in Google Sheets. For example, selecting the
 range from row 1, col 2 to row 3, col 4 corresponds to the string "B1:D3".

{:id="Spreadsheet.ReadCell" class="method"} <i/> ReadCell(*sheetName*{:.text},*cellReference*{:.text})
: On the page with the provided sheetName, reads the cell at the given
 cellReference and triggers the [`GotCellData`](#Spreadsheet.GotCellData) callback event. The
 cellReference can be either a text block with A1-Notation, or the result of
 the [`GetCellReference`](#Spreadsheet.GetCellReference) block.

{:id="Spreadsheet.ReadColumn" class="method"} <i/> ReadColumn(*sheetName*{:.text},*column*{:.text})
: On the page with the provided sheetName, reads the column at the given
 colNumber and triggers the [`GotColumnData`](#Spreadsheet.GotColumnData) callback event.

{:id="Spreadsheet.ReadRange" class="method"} <i/> ReadRange(*sheetName*{:.text},*rangeReference*{:.text})
: On the page with the provided sheetName, reads the cells at the given
 rangeReference and triggers the [`GotRangeData`](#Spreadsheet.GotRangeData) callback event. The
 rangeReference can be either a text block with A1-Notation, or the result
 of the [`GetRangeReference`](#Spreadsheet.GetRangeReference) block.

{:id="Spreadsheet.ReadRow" class="method"} <i/> ReadRow(*sheetName*{:.text},*rowNumber*{:.number})
: On the page with the provided sheetName, reads the row at the given
 rowNumber and triggers the [`GotRowData`](#Spreadsheet.GotRowData) callback event.

{:id="Spreadsheet.ReadSheet" class="method"} <i/> ReadSheet(*sheetName*{:.text})
: Reads the <b>entire</b> Google Sheets document and triggers the
 [`GotSheetData`](#Spreadsheet.GotSheetData) callback event.

{:id="Spreadsheet.ReadWithExactFilter" class="method"} <i/> ReadWithExactFilter(*sheetName*{:.text},*colID*{:.number},*value*{:.text})
: Filters a Google Sheet for rows where the given column number matches the provided value.

{:id="Spreadsheet.ReadWithPartialFilter" class="method"} <i/> ReadWithPartialFilter(*sheetName*{:.text},*colID*{:.number},*value*{:.text})
: Filters a Google Sheet for rows where the given column number contains the provided value string.

{:id="Spreadsheet.RemoveColumn" class="method"} <i/> RemoveColumn(*sheetName*{:.text},*column*{:.text})
: Deletes the column with the given column number from the table. This does
 not clear the column, but removes it entirely. The sheet's grid id can be
 found at the end of the url of the Google Sheets document, right after the
 "gid=". Once complete, it triggers the [`FinishedRemoveColumn`](#Spreadsheet.FinishedRemoveColumn)
 callback event.

{:id="Spreadsheet.RemoveRow" class="method"} <i/> RemoveRow(*sheetName*{:.text},*rowNumber*{:.number})
: Deletes the row with the given row number (1-indexed) from the table. This
 does not clear the row, but removes it entirely. The sheet's grid id can be
 found at the end of the url of the Google Sheets document, right after the
 "gid=". Once complete, it triggers the [`FinishedRemoveRow`](#Spreadsheet.FinishedRemoveRow)
 callback event.

{:id="Spreadsheet.WriteCell" class="method"} <i/> WriteCell(*sheetName*{:.text},*cellReference*{:.text},*data*{:.any})
: Given text or a number as `data`, writes the value to the cell. It will
 override any existing data in the cell with the one provided. Once complete,
 it triggers the [`FinishedWriteCell`](#Spreadsheet.FinishedWriteCell) callback event.

{:id="Spreadsheet.WriteColumn" class="method"} <i/> WriteColumn(*sheetName*{:.text},*column*{:.text},*data*{:.list})
: Given a list of values as `data`, writes the values to the column with the
 given column number, overriding existing values from top down. (Note: It
 will not erase the entire column.) Once complete, it triggers the
 [`FinishedWriteColumn`](#Spreadsheet.FinishedWriteColumn) callback event.

{:id="Spreadsheet.WriteRange" class="method"} <i/> WriteRange(*sheetName*{:.text},*rangeReference*{:.text},*data*{:.list})
: Given list of lists as `data`, writes the values to cells in the range. The
 number of rows and columns in the range must match the dimensions of your
 data. This method will override existing data in the range. Once complete,
 it triggers the [`FinishedWriteRange`](#Spreadsheet.FinishedWriteRange) callback event.

{:id="Spreadsheet.WriteRow" class="method"} <i/> WriteRow(*sheetName*{:.text},*rowNumber*{:.number},*data*{:.list})
: Given a list of values as `data`, writes the values to the row  with the
 given row number, overriding existing values from left to right. (Note: It
 will not erase the entire row.) Once complete, it triggers the
 [`FinishedWriteRow`](#Spreadsheet.FinishedWriteRow) callback event.

## TinyDB  {#TinyDB}

`TinyDB` is a non-visible component that stores data for an app.

 Apps created with App Inventor are initialized each time they run. This means that if an app
 sets the value of a variable and the user then quits the app, the value of that variable will
 not be remembered the next time the app is run. In contrast, TinyDB is a persistent data store
 for the app. The data stored in a `TinyDB` will be available each time the app is run. An
 example might be a game that saves the high score and retrieves it each time the game is played.

 Data items consist of tags and values. To store a data item, you specify the tag it should be
 stored under. The tag must be a text block, giving the data a name. Subsequently, you can
 retrieve the data that was stored under a given tag.

 You cannot use the `TinyDB` to pass data between two different apps on the phone, although you
 can use the `TinyDB` to share data between the different screens of a multi-screen app.

 When you are developing apps using the AI Companion, all the apps using that Companion will
 share the same `TinyDB`. That sharing will disappear once the apps are packaged and installed on
 the phone. During development you should be careful to clear the Companion app's data each time
 you start working on a new app.



### Properties  {#TinyDB-Properties}

{:.properties}

{:id="TinyDB.Namespace" .text} *Namespace*
: Namespace for storing data.

### Events  {#TinyDB-Events}

{:.events}
None


### Methods  {#TinyDB-Methods}

{:.methods}

{:id="TinyDB.ClearAll" class="method"} <i/> ClearAll()
: Clear the entire data store.

{:id="TinyDB.ClearTag" class="method"} <i/> ClearTag(*tag*{:.text})
: Clear the entry with the given `tag`{:.text.block}.

{:id="TinyDB.GetEntries" class="method returns dictionary"} <i/> GetEntries()
: Method to get all data in form of Dictionary

{:id="TinyDB.GetTags" class="method returns any"} <i/> GetTags()
: Return a list of all the tags in the data store.

{:id="TinyDB.GetValue" class="method returns any"} <i/> GetValue(*tag*{:.text},*valueIfTagNotThere*{:.any})
: Retrieve the value stored under the given `tag`{:.text.block}.  If there's no such tag, then
 return `valueIfTagNotThere`{:.variable.block}.

{:id="TinyDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Store the given `valueToStore`{:.variable.block} under the given `tag`{:.text.block}.
 The storage persists on the phone when the app is restarted.

## TinyWebDB  {#TinyWebDB}

The `TinyWebDB` component communicates with a Web service to store
 and retrieve information.  Although this component is usable, it is
 very limited and meant primarily as a demonstration for people who
 would like to create their own components that talk to the Web.
 The accompanying Web service is at
 (http://tinywebdb.appinventor.mit.edu).  The component has methods to
 [store a value](#TinyWebDB.StoreValue) under a tag and to
 [retrieve the value](#TinyWebDB.GetValue) associated with
 the tag.  The interpretation of what "store" and "retrieve" means
 is up to the Web service.  In this implementation, all tags and
 values are strings (text).  This restriction may be relaxed in
 future versions.



### Properties  {#TinyWebDB-Properties}

{:.properties}

{:id="TinyWebDB.ServiceURL" .text} *ServiceURL*
: Specifies the URL of the  Web service.
 The default value is the demo service running on App Engine.

### Events  {#TinyWebDB-Events}

{:.events}

{:id="TinyWebDB.GotValue"} GotValue(*tagFromWebDB*{:.text},*valueFromWebDB*{:.any})
: Indicates that a [`GetValue`](#TinyWebDB.GetValue) server request has succeeded.

{:id="TinyWebDB.ValueStored"} ValueStored()
: Event indicating that a [`StoreValue`](#TinyWebDB.StoreValue)  server request has succeeded.

{:id="TinyWebDB.WebServiceError"} WebServiceError(*message*{:.text})
: Indicates that the communication with the Web service signaled an error.

### Methods  {#TinyWebDB-Methods}

{:.methods}

{:id="TinyWebDB.GetValue" class="method"} <i/> GetValue(*tag*{:.text})
: `GetValue` asks the Web service to get the value stored under the given `tag`{:.text.block}.
 It is up to the Web service what to return if there is no value stored under the
 `tag`{:.text.block}.  This component just accepts whatever is returned. The
 [`GotValue`](#TinyWebDB.GotValue) event will be run on completion.

{:id="TinyWebDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Sends a request to the Web service to store the given `valueToStore`{:.variable.block} under
 the given `tag`{:.text.block}. The [`ValueStored`](#TinyWebDB.ValueStored) event will be run on completion.
