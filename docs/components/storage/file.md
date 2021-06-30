<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# File

Non-visible component for storing and retrieving files. Use this component to write or read files on your device. The default behaviour is to write files to the private data directory associated with your App. The Companion is special cased to write files to /sdcard/AppInventor/data to facilitate debugging. If the file path starts with a slash (/), then the file is created relative to /sdcard. For example writing a file to /myFile.txt will write the file in /sdcard/myFile.txt.

---

## Designer Properties

---

### LegacyMode

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   LegacyMode  |   boolean   |     False     |

## Events

---

### AfterFileSaved

<div block-type = "component_event" component-selector = "File" event-selector = "AfterFileSaved" id = "file-afterfilesaved"></div>

Event indicating that the contents of the file have been written.

| Param Name | IO Type |
| :--------: | :-----: |
|  fileName  |   text  |

### GotText

<div block-type = "component_event" component-selector = "File" event-selector = "GotText" id = "file-gottext"></div>

Event indicating that the contents from the file have been read.

| Param Name | IO Type |
| :--------: | :-----: |
|    text    |   text  |

## Methods

---

### AppendToFile

<div block-type = "component_method" component-selector = "File" method-selector = "AppendToFile" id = "file-appendtofile"></div>

Return Type : No Return Value

Appends text to the end of a file storage, creating the file if it does not exist. See the help text under SaveFile for information about where files are written.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |
|  fileName  |    text    |

### Delete

<div block-type = "component_method" component-selector = "File" method-selector = "Delete" id = "file-delete"></div>

Return Type : No Return Value

Deletes a file from storage. Prefix the filename with / to delete a specific file in the SD card, for instance /myFile.txt. will delete the file /sdcard/myFile.txt. If the file does not begin with a /, then the file located in the programs private storage will be deleted. Starting the file with // is an error because assets files cannot be deleted.

| Param Name | Input Type |
| :--------: | :--------: |
|  fileName  |    text    |

### ReadFrom

<div block-type = "component_method" component-selector = "File" method-selector = "ReadFrom" id = "file-readfrom"></div>

Return Type : No Return Value

Reads text from a file in storage. Prefix the filename with / to read from a specific file on the SD card. for instance /myFile.txt will read the file /sdcard/myFile.txt. To read assets packaged with an application (also works for the Companion) start the filename with // (two slashes). If a filename does not start with a slash, it will be read from the applications private storage (for packaged apps) and from /sdcard/AppInventor/data for the Companion.

| Param Name | Input Type |
| :--------: | :--------: |
|  fileName  |    text    |

### SaveFile

<div block-type = "component_method" component-selector = "File" method-selector = "SaveFile" id = "file-savefile"></div>

Return Type : No Return Value

Saves text to a file. If the filename begins with a slash (/) the file is written to the sdcard. For example writing to /myFile.txt will write the file to /sdcard/myFile.txt. If the filename does not start with a slash, it will be written in the programs private data directory where it will not be accessible to other programs on the phone. There is a special exception for the AI Companion where these files are written to /sdcard/AppInventor/data to facilitate debugging. Note that this block will overwrite a file if it already exists. If you want to add content to a file use the append block.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |
|  fileName  |    text    |

## Block Properties

---

### LegacyMode

<div block-type = "component_set_get" component-selector = "File" property-selector = "LegacyMode" property-type = "get" id = "get-file-legacymode"></div>

<div block-type = "component_set_get" component-selector = "File" property-selector = "LegacyMode" property-type = "set" id = "set-file-legacymode"></div>

Allows app to access files from the root of the external storage directory (legacy mode).

| Param Name | IO Type |
| :--------: | :-----: |
| LegacyMode | boolean |

## Component

---

### File

<div block-type = "component_component_block" component-selector = "File" id = "component-file"></div>

Return Type : component

Component File

