// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import redis.clients.jedis.Jedis;

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

import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONException;


/**
 * The RedCloud component stores and retrieves information in the Cloud using Redis, an
 * open source library. The component has methods to store a value under a tag and to
 * retrieve the value associated with the tag. It also possesses a listener to fire events
 * when stored values are changed.
 *
 * @author manting@mit.edu (Natalie Lao)
 */

@DesignerComponent(version = 0,
    description = "Non-visible component that communicates with RedCloud server to store" +
        " and retrieve information.",
    designerHelpDescription = "Non-visible component that communicates with RedCloud " +
        "server to store and retrieve information.",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/redCloud.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "jedis.jar")
public class RedCloud extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "RedCloud";
  private boolean importProject = false;
  private String accountName = "";
  private String projectID = "";
  private boolean isPublic = false;
  // Note: The two variables below are static because the systems they
  // interact with within RedCloud are also static Note: Natalie check true
  private static boolean isInitialized = false;  // Whether we have made our first
                                                 // connection to Firebase
  private static boolean persist = false;        // Whether or not we are in persistant mode
                                                 // where variables are kept when an app exits
                                                 // when off-line
  private Handler androidUIHandler;
  private final Activity activity;
  private RedCloudJedisListener childListener;

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
   * Creates a new RedCloud component.
   * @param container the Form that this component is contained in.
   */
  public RedCloud(ComponentContainer container) {
    super(container.$form());
    // We use androidUIHandler when we set up operations that run asynchronously
    // in a separate thread, but which themselves want to cause actions
    // back in the UI thread.  They do this by posting those actions
    // to androidUIHandler.
    androidUIHandler = new Handler();
    this.activity = container.$context();

    accountName = ""; // set in Designer
    projectID = ""; // set in Designer
    
    // Retrieve new posts as they are added to the RedCloud.
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        jedis.psubscribe(new RedCloudJedisListener(RedCloud.this), "__key*__:*");
      }
    };
    t.start();
    
    //Natalie: Need to add auth
  }
  
  /**
   * Initialize: Do runtime intialization of RedCloud
   * We cannot make a connection to the server in the component
   * Constructor because we do not yet know the value of the
   * Persist property. The Persist property is used to set the
   * persistance flag in the Redisson static config. It must
   * be set prior to any connection happening and cannot be
   * changed after a connection (or reference) is made.
   * So we defer making a connection until we initialize. Initialize
   * is called from runtime.scm (via Form.java) after all components
   * and properties have been setup.
   */
  public void Initialize() {
    Log.i(LOG_TAG, "Initalize called!");
    isInitialized = true;
  }

  /**
   * Getter for the AccountName.
   *
   * @return the AccountName for this RedCloud project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the AccountName for this RedCloud project.")
  public String AccountName() {
    return accountName;
  }

  /**
   * Specifies the account name of this RedCloud project.
   *
   * @param usrname the user's account name
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "Sets the AccountName for this RedCloud project.")
  public void AccountName(String usrname) {
    if (!accountName.equals(usrname)) {
      accountName = usrname;
    }
  }
  
  /**
   * Getter for the ProjectID.
   *
   * @return the ProjectID for this RedCloud project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the ProjectID for this RedCloud project.")
  public String ProjectID() {
    return projectID;
  }
  
  /**
   * Specifies the ID of this RedCloud project.
   *
   * @param id the project ID
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "Sets the ProjectID for this RedCloud project.")
  public void ProjectID(String id) {
    if (!projectID.equals(id)) {
      projectID = id;
    }
  }

  /**
   * Asks RedCloud to store the given value under the given tag.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public void StoreValue(final String tag, Object valueToStore) {
    Log.i("RedCloud","StoreValue");

    try {
      if(valueToStore != null) {
        valueToStore = JsonUtil.getJsonRepresentation(valueToStore);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }
    
    final String value = (String) valueToStore;

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
   * GetValue asks RedCloud to get the value stored under the given tag.
   * It will pass valueIfTagNotThere to GotValue if there is no value stored
   * under the tag.
   *
   * @param tag The tag whose value is to be retrieved.
   * @param valueIfTagNotThere The value to pass to the event if the tag does
   *                           not exist.
   */
  @SimpleFunction
  public void GetValue(final String tag, final Object valueIfTagNotThere) {
    final AtomicReference<Object> value = new AtomicReference<Object>();

    // Set value to either the JSON from the RedCloud
    // or the JSON representation of valueIfTagNotThere
    Thread t = new Thread() {
      public void run() {
        Jedis jedis = getJedis();
        try {
          String returnValue = jedis.get(accountName+projectID+tag);
          if (returnValue != null) {
            value.set(returnValue);
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
  
  /**
   * Indicates that a GetValue request has succeeded.
   *
   * @param value the value that was returned. Can be any type of value
   *              (e.g. number, text, boolean or list).
   */
  @SimpleEvent
  public void GotValue(String tag, Object value) {
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
   * Asks RedCloud to forget (delete or set to "null") a given tag.
   *
   * @param tag The tag to remove
   */
  @SimpleFunction(description = "Remove the tag from Firebase")
  public void ClearTag(final String tag) {
    //Natalie: Should ClearTag become ClearTags? Jedis can delete a list of tags easily
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
   * GetTagList asks RedCloud to retrieve all the tags belonging to this project.
   *
   * The resulting list is returned in GotTagList
   */
  @SimpleFunction(description = "Get the list of tags for this application. " +
      "When complete a \"TagList\" event will be triggered with the list of " +
      "known tags.")
  public void GetTagList() {
    //Natalie: Need Listener here too!
    
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
    EventDispatcher.dispatchEvent(this, "TagList", value);
  }
  
  /**
   * Indicates that the data in the RedCloud project has changed.
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
        EventDispatcher.dispatchEvent(RedCloud.this, "DataChanged", parsedTag, tagValue);
      }
    });
  }
  
  /**
   * Indicates that the communication with the RedCloud signaled an error.
   *
   * @param message the error message
   */
  @SimpleEvent
  public void RedCloudError(String message) {
    // Log the error message for advanced developers
    Log.e(LOG_TAG, message);

    // Invoke the application's "RedCloudError" event handler
    boolean dispatched = EventDispatcher.dispatchEvent(this, "RedCloudError", message);
    if (!dispatched) {
      // If the handler doesn't exist, then put up our own alert
      Notifier.oneButtonAlert(form, message, "RedCloudError", "Continue");
    }
  }
  
  private Jedis getJedis(){
    Jedis jedis = new Jedis("128.52.179.76", 6379);
    jedis.auth("test6789");
    return jedis;
  }
  
}
