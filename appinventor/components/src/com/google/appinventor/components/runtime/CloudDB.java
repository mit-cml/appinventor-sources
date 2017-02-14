// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import android.os.Environment;

import android.util.Base64;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.YailList;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * The CloudDB component stores and retrieves information in the Cloud using Redis, an
 * open source library. The component has methods to store a value under a tag and to
 * retrieve the value associated with the tag. It also possesses a listener to fire events
 * when stored values are changed.
 *
 * @author manting@mit.edu (Natalie Lao)
 */

@DesignerComponent(version = 0,
    description = "Non-visible component that communicates with CloudDB server to store" +
        " and retrieve information.",
    designerHelpDescription = "Non-visible component that communicates with CloudDB " +
        "server to store and retrieve information.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/cloudDB.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "jedis.jar")
public class CloudDB extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "CloudDB";
  private static final String BINFILE_DIR = "/AppInventorBinaries";
  private boolean importProject = false;
  private String accountName = "";
  private String projectID = "";
  private boolean isPublic = false;
  // Note: The two variables below are static because the systems they
  // interact with within CloudDB are also static Note: Natalie check true
  private static boolean isInitialized = false;  // Whether we have made our first
                                                 // connection to Firebase
  private static boolean persist = false;        // Whether or not we are in persistant mode
                                                 // where variables are kept when an app exits
                                                 // when off-line
  private Handler androidUIHandler;
  private final Activity activity;
  private CloudDBJedisListener childListener;

  // ReturnVal -- Holder which can be used as a final value but whose content
  //              remains mutable.
  private static class ReturnVal {
    String err;                 // Holder for any errors
    Object retval;              // Returned value

    Object getRetval() {
      return retval;
    }

  }

  //Natalie: What does this do?
  private abstract static class Transactional {
    final Object arg1;
    final Object arg2;
    final ReturnVal retv;

    Transactional(Object arg1, Object arg2, ReturnVal retv) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.retv = retv;
    }

    ReturnVal getResult() {
      return retv;
    }
  }

  /**
   * Creates a new CloudDB component.
   * @param container the Form that this component is contained in.
   */
  public CloudDB(ComponentContainer container) {
    super(container.$form());
    // We use androidUIHandler when we set up operations that run asynchronously
    // in a separate thread, but which themselves want to cause actions
    // back in the UI thread.  They do this by posting those actions
    // to androidUIHandler.
    androidUIHandler = new Handler();
    this.activity = container.$context();
    //Defaults set in MockCloudDB.java in appengine/src/com/google/appinventor/client/editor/simple/components
    accountName = ""; // set in Designer
    projectID = ""; // set in Designer
    
    // Retrieve new posts as they are added to the CloudDB.
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        jedis.psubscribe(new CloudDBJedisListener(CloudDB.this), "__key*__:*");
      }
    };
    t.start();
    
    //Natalie: Need to add auth
  }
  
  /**
   * Initialize: Do runtime initialization of CloudDB
   */
  public void Initialize() {
    Log.i(LOG_TAG, "Initalize called!");
    isInitialized = true;
  }

  /**
   * Getter for the AccountName.
   *
   * @return the AccountName for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the AccountName for this CloudDB project.")
  public String AccountName() {
    checkAccountNameProjectIDNotBlank();
    return accountName;
  }

  /**
   * Specifies the account name of this CloudDB project.
   *
   * @param usrname the user's account name
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  public void AccountName(String usrname) {    
    if (!accountName.equals(usrname)) {
      accountName = usrname;
    }
    if (accountName.equals("")){
      throw new RuntimeException("CloudDB AccountName property cannot be blank.");
    }
  }
  
  /**
   * Getter for the ProjectID.
   *
   * @return the ProjectID for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the ProjectID for this CloudDB project.")
  public String ProjectID() {
    checkAccountNameProjectIDNotBlank();
    return projectID;
  }
  
  /**
   * Specifies the ID of this CloudDB project.
   *
   * @param id the project ID
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  public void ProjectID(String id) {
    if (!projectID.equals(id)) {
      projectID = id;
    }
    if (projectID.equals("")){
      throw new RuntimeException("CloudDB AccountName property cannot be blank.");
    }
  }

  /**
   * Asks CloudDB to store the given value under the given tag.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public void StoreValue(final String tag, String valueToStore) {
    Log.i("CloudDB","StoreValue");
    checkAccountNameProjectIDNotBlank();
    Log.i("CloudDB","PASSSSS");
    
    try {
      if (valueToStore != null) {
        if (valueToStore.startsWith("file://")) {
          valueToStore = JsonUtil.getJsonRepresentation(readFile(valueToStore));
        } else {
          valueToStore = JsonUtil.getJsonRepresentation(valueToStore);
        }
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    final String value = valueToStore;

    //Natalie: perform the store operation
    //valueToStore is always converted to JSON (String);
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        try {
          String statusCodeReply = jedis.set(accountName+projectID+tag, value.toString());
        } finally {
          if (jedis != null) {
            jedis.close();
          }
        }
      }
    };
    t.start();
  }
  
  /**
   * GetValue asks CloudDB to get the value stored under the given tag.
   * It will pass valueIfTagNotThere to GotValue if there is no value stored
   * under the tag.
   *
   * @param tag The tag whose value is to be retrieved.
   * @param valueIfTagNotThere The value to pass to the event if the tag does
   *                           not exist.
   */
  @SimpleFunction
  public void GetValue(final String tag, final Object valueIfTagNotThere) {
    checkAccountNameProjectIDNotBlank();
    
    final AtomicReference<Object> value = new AtomicReference<Object>();

    // Set value to either the JSON from the CloudDB
    // or the JSON representation of valueIfTagNotThere
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        try {
          String returnValue = jedis.get(accountName+projectID+tag);
          if (returnValue != null) {
            try {
              JSONArray valueJsonList = new JSONArray(returnValue);
              List<String> valueList = JsonUtil.getStringListFromJsonArray(valueJsonList);
              if (valueList.size() == 2) {
                String filename = writeFile(valueList.get(1), valueList.get(0));
                filename = filename.replace("file:/", "file:///");
                value.set(JsonUtil.getJsonRepresentation(filename));
              } else {
                value.set(returnValue);
              }
            } catch(JSONException e) {
              value.set(returnValue);
            }
          } else {
            value.set(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
          }
        } catch(JSONException e) {
          throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
        }
        
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that the value was
            // received.  We post this to run in the Application's main
            // UI thread.
            GotValue(tag, value.get());
          }
        });
        
        jedis.close();
      }
    };
    t.start();
  }

  @SimpleEvent(description = "Event triggered by the \"RemoveFirstFromList\" function. The " +
    "argument \"value\" is the object that was the first in the list, and which is now " +
    "removed.")
  public void FirstRemoved(Object value) {
    checkAccountNameProjectIDNotBlank();
    EventDispatcher.dispatchEvent(this, "FirstRemoved", value);
  }

  private static final String POP_FIRST_SCRIPT =
      "local key = KEYS[1];" +
      "local currentValue = redis.call('get', key);" +
      "local decodedValue = cjson.decode(currentValue);" +
      "if (type(decodedValue) == 'table') then " +
      "  local removedValue = table.remove(decodedValue, 1);" +
      "  local newValue = cjson.encode(decodedValue);" +
      "  redis.call('set', key, newValue);" +
      "  return removedValue;" +
      "else " +
      "  return error('You can only remove elements from a list');" +
      "end";

  @SimpleFunction(description = "Return the first element of a list and atomically remove it. " +
    "If two devices use this function simultaneously, one will get the first element and the " +
    "the other will get the second element, or an error if there is no available element. " +
    "When the element is available, the \"FirstRemoved\" event will be triggered.")
  public void RemoveFirstFromList(final String tag) {
    checkAccountNameProjectIDNotBlank();
    
    final String key = accountName + projectID + tag;

    Thread t = new Thread() {
      public void run() {
      Jedis jedis = getJedis();
        try {
          FirstRemoved(jedis.eval(POP_FIRST_SCRIPT, 1, key));
        } catch(JedisException e) {

        } finally {
          jedis.close();
        }
      }
    };
    t.start();
  }

  private static final String APPEND_SCRIPT =
      "local key = KEYS[1];" +
      "local toAppend = ARGV[1];" +
      "local currentValue = redis.call('get', key);" +
      "local newTable;" +
      "if (currentValue == false) then " +
      "  newTable = {};" +
      "else " +
      "  newTable = cjson.decode(currentValue);" +
      "  if not (type(newTable) == 'table') then " +
      "    return error('You can only append to a list');" +
      "  end " +
      "end " +
      "table.insert(newTable, toAppend);" +
      "local newValue = cjson.encode(newTable);" +
      "redis.call('set', key, newValue);" +
      "return redis.call('get', key);";

  @SimpleFunction(description = "Append a value to the end of a list atomically. " +
    "If two devices use this function simultaneously, both will be appended and no " +
    "data lost.")
  public void AppendValueToList(final String tag, final Object itemToAdd) {
    checkAccountNameProjectIDNotBlank();
    
    Object itemObject = new Object();
    try {
      if(itemToAdd != null) {
        itemObject = JsonUtil.getJsonRepresentation(itemToAdd);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }
    
    final String item = (String) itemObject;
    final String key = accountName + projectID + tag;

    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        try {
          jedis.eval(APPEND_SCRIPT, 1, key, item);
        } catch(JedisException e) {

        } finally {
          jedis.close();
        }
      }
    };
    t.start();
  }
  
  /**
   * Indicates that a GetValue request has succeeded.
   *
   * @param value the value that was returned. Can be any type of value
   *              (e.g. number, text, boolean or list).
   */
  @SimpleEvent
  public void GotValue(String tag, Object value) {
    checkAccountNameProjectIDNotBlank();
    
    try {
      if(value != null && value instanceof String) {
        value = JsonUtil.getObjectFromJson((String) value);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
    }

    // Invoke the application's "GotValue" event handler
    EventDispatcher.dispatchEvent(this, "GotValue", tag, value);
  }
  
  /**
   * Asks CloudDB to forget (delete or set to "null") a given tag.
   *
   * @param tag The tag to remove
   */
  @SimpleFunction(description = "Remove the tag from Firebase")
  public void ClearTag(final String tag) {
    //Natalie: Should we also add ClearTagsList? Jedis can delete a list of tags easily
    checkAccountNameProjectIDNotBlank();
    
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        jedis.del(accountName+projectID+tag);
        jedis.close();
      }
    };
    t.start();
  }
  
  /**
   * GetTagList asks CloudDB to retrieve all the tags belonging to this project.
   *
   * The resulting list is returned in GotTagList
   */
  @SimpleFunction(description = "Get the list of tags for this application. " +
      "When complete a \"TagList\" event will be triggered with the list of " +
      "known tags.")
  public void GetTagList() {
    //Natalie: Need Listener here too!
    checkAccountNameProjectIDNotBlank();
    
    Jedis jedis = getJedis();
    
    Set<String> value = jedis.keys(accountName+projectID+"*");    
    final List<String> listValue = new ArrayList<String>(value);
    
    for(int i = 0; i < listValue.size(); i++){
      listValue.set(i, listValue.get(i).substring((accountName+projectID).length()));
    }
    
    androidUIHandler.post(new Runnable() {
      @Override
      public void run() {
        TagList(listValue);
      }
    });
    
    jedis.close();
  }
  
  /**
   * Indicates that a GetTagList request has succeeded.
   *
   * @param value the list of tags that was returned.
   */
  @SimpleEvent(description = "Event triggered when we have received the list of known tags. " +
      "Used with the \"GetTagList\" Function.")
  public void TagList(List<String> value) {
    //Natalie: Why is this not called "GotTagList"? Also need to only show tag without 
    //accountName or projectID
    checkAccountNameProjectIDNotBlank();
    EventDispatcher.dispatchEvent(this, "TagList", value);
  }
  
  /**
   * Indicates that the data in the CloudDB project has changed.
   * Launches an event with the tag and value that have been updated.
   *
   * @param tag the tag that has changed.
   * @param value the new value of the tag.
   */
  @SimpleEvent
  public void DataChanged(final String tag, final Object value) {    
    androidUIHandler.post(new Runnable() {
      public void run() {
        Object tagValue = "";
        try {
          if(value != null && value instanceof String) {
            tagValue = JsonUtil.getObjectFromJson((String) value);
          }
        } catch(JSONException e) {
          throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
        }
        
        String parsedTag = tag.substring(accountName.length()+projectID.length());
        
        // Invoke the application's "DataChanged" event handler
        EventDispatcher.dispatchEvent(CloudDB.this, "DataChanged", parsedTag, tagValue);
      }
    });
  }
  
  /**
   * Indicates that the communication with the CloudDB signaled an error.
   *
   * @param message the error message
   */
  @SimpleEvent
  public void CloudDBError(String message) {
    // Log the error message for advanced developers
    Log.e(LOG_TAG, message);

    // Invoke the application's "CloudDBError" event handler
    boolean dispatched = EventDispatcher.dispatchEvent(this, "CloudDBError", message);
    if (!dispatched) {
      // If the handler doesn't exist, then put up our own alert
      Notifier.oneButtonAlert(form, message, "CloudDBError", "Continue");
    }
  }
  
  private void checkAccountNameProjectIDNotBlank(){
    if (accountName.equals("")){
      throw new RuntimeException("CloudDB AccountName property cannot be blank.");
    }
    if (projectID.equals("")){
      throw new RuntimeException("CloudDB ProjectID property cannot be blank.");
    }
  }

  private String getFileExtension(String fullName) {
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf(".");
    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }
  
  private Jedis getJedis(){
    Jedis jedis = new Jedis("128.52.179.76", 6379);
    jedis.auth("test6789");
    return jedis;
  }

  private YailList readFile(String fileName) {
    try {
      String originalFileName = fileName;
      // Trim off file:// part if present
      if (fileName.startsWith("file://")) {
        fileName = fileName.substring(7);
      }
      if (!fileName.startsWith("/")) {
        throw new YailRuntimeError("Invalid fileName, was " + originalFileName, "ReadFrom");
      }
      File inputFile = new File(fileName);
      if (!inputFile.isFile()) {
        throw new YailRuntimeError("Cannot find file", "ReadFrom");
      }
      String extension = getFileExtension(fileName);
      FileInputStream inputStream = new FileInputStream(inputFile);
      byte [] content = new byte[(int)inputFile.length()];
      int bytesRead = inputStream.read(content);
      if (bytesRead != inputFile.length()) {
        throw new YailRuntimeError("Did not read complete file!", "Read");
      }
      inputStream.close();
      String encodedContent = Base64.encodeToString(content, Base64.DEFAULT);
      Object [] results = new Object[2];
      results[0] = extension;
      results[1] = encodedContent;
      return YailList.makeList(results);
    } catch (FileNotFoundException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    } catch (IOException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    }
  }

  private void trimDirectory(int maxSavedFiles, File directory) {

    File [] files = directory.listFiles();

    Arrays.sort(files, new Comparator<File>(){
      public int compare(File f1, File f2)
      {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      } });

    int excess = files.length - maxSavedFiles;
    for (int i = 0; i < excess; i++) {
      files[i].delete();
    }

  }

  private String writeFile(String input, String fileExtension) {
    try {
      if (fileExtension.length() != 3) {
        throw new YailRuntimeError("File Extension must be three characters", "Write Error");
      }
      byte [] content = Base64.decode(input, Base64.DEFAULT);
      String fullDirName = Environment.getExternalStorageDirectory() + BINFILE_DIR;
      File destDirectory = new File(fullDirName);
      destDirectory.mkdirs();
      File dest = File.createTempFile("BinFile", "." + fileExtension, destDirectory);
      FileOutputStream outStream = new FileOutputStream(dest);
      outStream.write(content);
      outStream.close();
      String retval = dest.toURI().toASCIIString();
      trimDirectory(20, destDirectory);
      return retval;
    } catch (Exception e) {
      throw new YailRuntimeError(e.getMessage(), "Write");
    }
  }
  
}
