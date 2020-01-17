---
layout: documentation
title: Storage
---

[&laquo; Back to index](index.html)
# Storage

Table of Contents:

* [CloudDB](#CloudDB)
* [File](#File)
* [TinyDB](#TinyDB)
* [TinyWebDB](#TinyWebDB)

## CloudDB  {#CloudDB}

### Properties  {#CloudDB-Properties}

{:.properties}

{:id="CloudDB.DefaultRedisServer" .text .wo .do} *DefaultRedisServer*
: The Default Redis Server to use.

{:id="CloudDB.ProjectID" .text .ro} *ProjectID*
: Gets the ProjectID for this CloudDB project.

{:id="CloudDB.RedisPort" .number .ro} *RedisPort*
: The Redis Server port to use. Defaults to 6381

{:id="CloudDB.RedisServer" .text .ro} *RedisServer*
: The Redis Server to use to store data. A setting of "DEFAULT" means that the MIT server will be used.

{:id="CloudDB.Token" .text .ro .do} *Token*
: This field contains the authentication token used to login to the backed Redis server. For the "DEFAULT" server, do not edit this value, the system will fill it in for you. A system administrator may also provide a special value to you which can be used to share data between multiple projects from multiple people. If using your own Redis server, set a password in the server's config and enter it here.

{:id="CloudDB.UseSSL" .boolean .ro .do} *UseSSL*
: Set to true to use SSL to talk to CloudDB/Redis server. This should be set to True for the "DEFAULT" server.

### Events  {#CloudDB-Events}

{:.events}

{:id="CloudDB.CloudDBError"} CloudDBError(*message*{:.text})
: Indicates that an error occurred while communicating with the CloudDB Redis server.

{:id="CloudDB.DataChanged"} DataChanged(*tag*{:.text},*value*{:.any})
: Indicates that the data in the CloudDB project has changed.
 Launches an event with the tag and value that have been updated.

{:id="CloudDB.FirstRemoved"} FirstRemoved(*value*{:.any})
: Event triggered by the "RemoveFirstFromList" function. The argument "value" is the object that was the first in the list, and which is now removed.

{:id="CloudDB.GotValue"} GotValue(*tag*{:.text},*value*{:.any})
: Indicates that a GetValue request has succeeded.

{:id="CloudDB.TagList"} TagList(*value*{:.list})
: Event triggered when we have received the list of known tags. Used with the "GetTagList" Function.

### Methods  {#CloudDB-Methods}

{:.methods}

{:id="CloudDB.AppendValueToList" class="method"} <i/> AppendValueToList(*tag*{:.text},*itemToAdd*{:.any})
: Append a value to the end of a list atomically. If two devices use this function simultaneously, both will be appended and no data lost.

{:id="CloudDB.ClearTag" class="method"} <i/> ClearTag(*tag*{:.text})
: Remove the tag from CloudDB

{:id="CloudDB.CloudConnected" class="method returns boolean"} <i/> CloudConnected()
: returns True if we are on the network and will likely be able to connect to the CloudDB server.

{:id="CloudDB.GetTagList" class="method"} <i/> GetTagList()
: Get the list of tags for this application. When complete a "TagList" event will be triggered with the list of known tags.

{:id="CloudDB.GetValue" class="method"} <i/> GetValue(*tag*{:.text},*valueIfTagNotThere*{:.any})
: Get the Value for a tag, doesn't return the value but will cause a GotValue event to fire when the value is looked up.

{:id="CloudDB.RemoveFirstFromList" class="method"} <i/> RemoveFirstFromList(*tag*{:.text})
: Return the first element of a list and atomically remove it. If two devices use this function simultaneously, one will get the first element and the the other will get the second element, or an error if there is no available element. When the element is available, the "FirstRemoved" event will be triggered.

{:id="CloudDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Store a value at a tag.

## File  {#File}

### Events  {#File-Events}

{:.events}

{:id="File.AfterFileSaved"} AfterFileSaved(*fileName*{:.text})
: Event indicating that the contents of the file have been written.

{:id="File.GotText"} GotText(*text*{:.text})
: Event indicating that the contents from the file have been read.

### Methods  {#File-Methods}

{:.methods}

{:id="File.AppendToFile" class="method"} <i/> AppendToFile(*text*{:.text},*fileName*{:.text})
: Appends text to the end of a file storage, creating the file if it does not exist. See the help text under SaveFile for information about where files are written.

{:id="File.Delete" class="method"} <i/> Delete(*fileName*{:.text})
: Deletes a file from storage. Prefix the filename with / to delete a specific file in the SD card, for instance /myFile.txt. will delete the file /sdcard/myFile.txt. If the file does not begin with a /, then the file located in the programs private storage will be deleted. Starting the file with // is an error because assets files cannot be deleted.

{:id="File.ReadFrom" class="method"} <i/> ReadFrom(*fileName*{:.text})
: Reads text from a file in storage. Prefix the filename with / to read from a specific file on the SD card. for instance /myFile.txt will read the file /sdcard/myFile.txt. To read assets packaged with an application (also works for the Companion) start the filename with // (two slashes). If a filename does not start with a slash, it will be read from the applications private storage (for packaged apps) and from /sdcard/AppInventor/data for the Companion.

{:id="File.SaveFile" class="method"} <i/> SaveFile(*text*{:.text},*fileName*{:.text})
: Saves text to a file. If the filename begins with a slash (/) the file is written to the sdcard. For example writing to /myFile.txt will write the file to /sdcard/myFile.txt. If the filename does not start with a slash, it will be written in the programs private data directory where it will not be accessible to other programs on the phone. There is a special exception for the AI Companion where these files are written to /sdcard/AppInventor/data to facilitate debugging. Note that this block will overwrite a file if it already exists.

If you want to add content to a file use the append block.

## TinyDB  {#TinyDB}

### Properties  {#TinyDB-Properties}

{:.properties}

{:id="TinyDB.Namespace" .text} *Namespace*
: Namespace for storing data.

### Methods  {#TinyDB-Methods}

{:.methods}

{:id="TinyDB.ClearAll" class="method"} <i/> ClearAll()
: Clear the entire data store

{:id="TinyDB.ClearTag" class="method"} <i/> ClearTag(*tag*{:.text})
: Clear the entry with the given tag

{:id="TinyDB.GetTags" class="method returns any"} <i/> GetTags()
: Return a list of all the tags in the data store

{:id="TinyDB.GetValue" class="method returns any"} <i/> GetValue(*tag*{:.text},*valueIfTagNotThere*{:.any})
: Retrieve the value stored under the given tag.  If there's no such tag, then return valueIfTagNotThere.

{:id="TinyDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Store the given value under the given tag.  The storage persists on the
 phone when the app is restarted.

## TinyWebDB  {#TinyWebDB}

### Properties  {#TinyWebDB-Properties}

{:.properties}

{:id="TinyWebDB.ServiceURL" .text} *ServiceURL*
: Returns the URL of the web service database.

### Events  {#TinyWebDB-Events}

{:.events}

{:id="TinyWebDB.GotValue"} GotValue(*tagFromWebDB*{:.text},*valueFromWebDB*{:.any})
: Indicates that a GetValue server request has succeeded.

{:id="TinyWebDB.ValueStored"} ValueStored()
: Event indicating that a StoreValue server request has succeeded.

{:id="TinyWebDB.WebServiceError"} WebServiceError(*message*{:.text})
: Indicates that the communication with the Web service signaled an error

### Methods  {#TinyWebDB-Methods}

{:.methods}

{:id="TinyWebDB.GetValue" class="method"} <i/> GetValue(*tag*{:.text})
: GetValue asks the Web service to get the value stored under the given tag.
 It is up to the Web service what to return if there is no value stored
 under the tag.  This component just accepts whatever is returned.

{:id="TinyWebDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Asks the Web service to store the given value under the given tag