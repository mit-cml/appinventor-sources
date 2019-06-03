---
layout: documentation
title: Experimental
---

[&laquo; Back to index](index.html)
# Experimental

Table of Contents:

* [FirebaseDB](#FirebaseDB)

## FirebaseDB  {#FirebaseDB}

### Properties  {#FirebaseDB-Properties}

{:.properties}

{:id="FirebaseDB.DefaultURL" .text .wo .do} *DefaultURL*
: 

{:id="FirebaseDB.DeveloperBucket" .text .do} *DeveloperBucket*
: Getter for the DeveloperBucket.

{:id="FirebaseDB.FirebaseToken" .text .do} *FirebaseToken*
: Getter for the FirebaseToken.

{:id="FirebaseDB.FirebaseURL" .text .do} *FirebaseURL*
: Gets the URL for this FirebaseDB.

{:id="FirebaseDB.Persist" .boolean .wo .do} *Persist*
: If true, variables will retain their values when off-line and the App exits. Values will be uploaded to Firebase the next time the App is run while connected to the network. This is useful for applications which will gather data while not connected to the network. Note: AppendValue and RemoveFirst will not work correctly when off-line, they require a network connection.<br/><br/> <i>Note</i>: If you set Persist on any Firebase component, on any screen, it makes all Firebase components on all screens persistent. This is a limitation of the low level Firebase library. Also be aware that if you want to set persist to true, you should do so before connecting the Companion for incremental development.

{:id="FirebaseDB.ProjectBucket" .text} *ProjectBucket*
: Gets the ProjectBucket for this FirebaseDB.

### Events  {#FirebaseDB-Events}

{:.events}

{:id="FirebaseDB.DataChanged"} DataChanged(*tag*{:.text},*value*{:.any})
: Indicates that the data in the Firebase has changed.
 Launches an event with the tag and value that have been updated.

{:id="FirebaseDB.FirebaseError"} FirebaseError(*message*{:.text})
: Indicates that the communication with the Firebase signaled an error.

{:id="FirebaseDB.FirstRemoved"} FirstRemoved(*value*{:.any})
: Event triggered by the "RemoveFirst" function. The argument "value" is the object that was the first in the list, and which is now removed.

{:id="FirebaseDB.GotValue"} GotValue(*tag*{:.text},*value*{:.any})
: Indicates that a GetValue request has succeeded.

{:id="FirebaseDB.TagList"} TagList(*value*{:.list})
: Event triggered when we have received the list of known tags. Used with the "GetTagList" Function.

### Methods  {#FirebaseDB-Methods}

{:.methods}

{:id="FirebaseDB.AppendValue" class="method"} <i/> AppendValue(*tag*{:.text},*valueToAdd*{:.any})
: Append a value to the end of a list atomically. If two devices use this function simultaneously, both will be appended and no data lost.

{:id="FirebaseDB.ClearTag" class="method"} <i/> ClearTag(*tag*{:.text})
: Remove the tag from Firebase

{:id="FirebaseDB.GetTagList" class="method"} <i/> GetTagList()
: Get the list of tags for this application. When complete a "TagList" event will be triggered with the list of known tags.

{:id="FirebaseDB.GetValue" class="method"} <i/> GetValue(*tag*{:.text},*valueIfTagNotThere*{:.any})
: GetValue asks Firebase to get the value stored under the given tag.
 It will pass valueIfTagNotThere to GotValue if there is no value stored
 under the tag.

{:id="FirebaseDB.RemoveFirst" class="method"} <i/> RemoveFirst(*tag*{:.text})
: Return the first element of a list and atomically remove it. If two devices use this function simultaneously, one will get the first element and the the other will get the second element, or an error if there is no available element. When the element is available, the "FirstRemoved" event will be triggered.

{:id="FirebaseDB.StoreValue" class="method"} <i/> StoreValue(*tag*{:.text},*valueToStore*{:.any})
: Asks Firebase to store the given value under the given tag.

{:id="FirebaseDB.Unauthenticate" class="method"} <i/> Unauthenticate()
: If you are having difficulty with the Companion and you are switching between different Firebase accounts, you may need to use this function to clear internal Firebase caches. You can just use the "Do It" function on this block in the blocks editor. Note: You should not normally need to use this block as part of an application.