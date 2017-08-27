// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

//Natalie: Package should be different for an extension
package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import com.evernote.android.job.JobManager;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesBroadcastReceivers;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.SyncJob;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.CloudDBCache;
import com.google.appinventor.components.runtime.util.CloudDBCacheHelper;
import com.google.appinventor.components.runtime.util.CloudDBJedisListener;
import org.json.JSONArray;
import org.json.JSONException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The CloudDB component stores and retrieves information in the Cloud using Redis, an
 * open source library. The component has methods to store a value under a tag and to
 * retrieve the value associated with the tag. It also possesses a listener to fire events
 * when stored values are changed. It also posseses a sync capability which helps CloudDB
 * to sync with data collected offline.
 *
 * @author manting@mit.edu (Natalie Lao)
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */

@DesignerComponent(version = 1,
    description = "Non-visible component that communicates with CloudDB server to store" +
        " and retrieve information.",
    designerHelpDescription = "Non-visible component that communicates with CloudDB " +
        "server to store and retrieve information.",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/cloudDB.png")
@UsesBroadcastReceivers(receivers = {
        /*@ReceiverElement(name = "com.google.appinventor.components.runtime.util.AddReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "com.evernote.android.job.ADD_JOB_CREATOR")})
                },
                exported = "false"),*/
        @ReceiverElement(name = "com.evernote.android.job.v14.PlatformAlarmReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "com.evernote.android.job.v14.RUN_JOB"),
                                @ActionElement(name = "net.vrallev.android.job.v14.RUN_JOB")})
                },
                exported = "false"),
        @ReceiverElement(name = "com.evernote.android.job.JobBootReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "android.intent.action.BOOT_COMPLETED"),
                                @ActionElement(name = "android.intent.action.QUICKBOOT_POWERON"),
                                @ActionElement(name = "com.htc.intent.action.QUICKBOOT_POWERON"),
                                @ActionElement(name = "android.intent.action.MY_PACKAGE_REPLACED")})
                },
                exported = "false")
})
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
                 "android.permission.ACCESS_NETWORK_STATE," +
                 "android.permission.RECEIVE_BOOT_COMPLETED," +
                 "android.permission.WAKE_LOCK")
@UsesLibraries(libraries = "jedis.jar")
public final class CloudDB extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "CloudDB";
  private static final String BINFILE_DIR = "/AppInventorBinaries";
  private boolean importProject = false;
  private String projectID = "";
  private String token = "";
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

  private Jedis INSTANCE = null;
  private String redisServer;
  private int redisPort;
  private volatile boolean LISTENERSTOPPING = false;
  private JobManager jobManager;

  // To avoid blocking the UI thread, we do most Jedis operations in the background.
  // Rather then spawning a new thread for each request, we use an ExcutorService with
  // a single background thread to perform all the Jedis work. Using a single thread
  // also means that we can share a single Jedis connection and not worry about thread
  // synchronization.

  private ExecutorService background = Executors.newSingleThreadExecutor();

  //added by Joydeep Mitra
  private boolean sync = false;
  private long syncPeriod = 9_00_000;
  private ConnectivityManager cm;
  //private CloudDBCacheHelper cloudDBCacheHelper;
  //-------------------------

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
    Log.d(LOG_TAG, "Static: androidUIHandler = " + androidUIHandler);
    this.activity = container.$context();
    //Defaults set in MockCloudDB.java in appengine/src/com/google/appinventor/client/editor/simple/components
    projectID = ""; // set in Designer
    token = ""; //set in Designer

    redisServer = "jis.csail.mit.edu";
    redisPort = 9001;

    Log.d(LOG_TAG, "JobManager created");

    cm = (ConnectivityManager) form.$context().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
  }

  /**
   * Initialize: Do runtime initialization of CloudDB
   */
  public void Initialize() {
    Log.i(LOG_TAG, "Initalize called!");
    isInitialized = true;
    startListener();
  }

  private void stopListener() {
    // We do this on the UI thread to make sure it is complete
    // before we repoint the redis server (or port)
    Log.i(LOG_TAG, "Listener stopping!");
    LISTENERSTOPPING = true;
    Jedis jedis = getJedis();
    try {
      jedis.psubscribe(new CloudDBJedisListener(CloudDB.this), "__key*__:*");
    } catch (Exception e) {
      Log.e(LOG_TAG, "in stop listener", e);
    }
  }

  private void startListener() {
    // Retrieve new posts as they are added to the CloudDB.
    // Note: We use a real thread here rather then the background executor
    // because this thread will run effectively forever
    LISTENERSTOPPING = false;
    Log.i(LOG_TAG, "Listener starting!");
    Thread t = new Thread() {
        public void run() {
          while (true) {
            Jedis jedis = getJedis(true);
            if (jedis != null) {
              try {
                jedis.psubscribe(new CloudDBJedisListener(CloudDB.this), "__key*__:*");
              } catch (Exception e) {
                Log.e(LOG_TAG, "Error in listener thread", e);
              }
            } else {
              // Could not connect to the Redis server. Sleep for
              // a minute and try again. Note: We can sleep because
              // we are in a separate thread.
              Log.i(LOG_TAG, "Cannot connect to Redis server, sleeping 1 minute...");
              try {
                Thread.sleep(60*1000);
              } catch (InterruptedException e) {
                // XXX
              }
            }
            if (LISTENERSTOPPING) {
              break;
            }
          }
          Log.d(LOG_TAG, "Listener existing");
        }
      };
    t.start();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "jis.csail.mit.edu")
  public void RedisServer(String servername) {
    stopListener();
    redisServer = servername;
    if (INSTANCE != null) {
      INSTANCE.quit();
      INSTANCE = null;
    }
    startListener();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The Redis Server to use.")
  public String RedisServer() {
    return redisServer;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
    defaultValue = "9001")
  public void RedisPort(int port) {
    stopListener();
    redisPort = port;
    if (INSTANCE != null) {
      INSTANCE.quit();
      INSTANCE = null;
    }
    startListener();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The Redis Server port to use.")
  public int RedisPort() {
    return redisPort;
  }

  /**
   * Indicates whether CloudDB needs to be synced.
   * @param sync
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "false")
  @SimpleProperty(description = "Specifies if data stored in CloudDB needs to be synced", userVisible = false)
  public void Sync(boolean sync){
    Log.d(CloudDB.LOG_TAG,"Sync called with sync = " + sync);
    this.sync = sync;
    Log.d(LOG_TAG, "About to add job creator");
    JobManager.instance().getConfig().setAllowSmallerIntervalsForMarshmallow(true); // For debugging
    Log.d(CloudDB.LOG_TAG,"JobManager for SyncJob added...");
    SyncJob.scheduleSync(this.syncPeriod);

  }

  /**
   * Specifies the periodic interval in ms in which the sync job will run.
   * @param period
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
          defaultValue = "900000")
  public void SyncPeriod(long period){
    Log.d(CloudDB.LOG_TAG,"SyncPeriod called with period = " + sync);
    this.syncPeriod = period;
    SyncJob.scheduleSync(this.syncPeriod);
  }

  /**
   * Getter for the sync period.
   *
   * @return the syncPeriod for the Sync Job.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
          description = "Gets the sync period of the SyncJob.")
  public long SyncPeriod() {
    return this.syncPeriod;
  }

  /**
   * Getter for the ProjectID.
   *
   * @return the ProjectID for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the ProjectID for this CloudDB project.")
  public String ProjectID() {
    checkProjectIDNotBlank();
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
      throw new RuntimeException("CloudDB ProjectID property cannot be blank.");
    }
  }

  /**
   * Specifies the Token Signature of this CloudDB project.
   *
   * @param authToken for CloudDB server
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
          defaultValue = "")
  public void Token(String authToken) {
    if (!token.equals(authToken)) {
      token = authToken;
      saveToken(token);
    }
    if (token.equals("")){
      throw new RuntimeException("CloudDB Token property cannot be blank.");
    }
  }

  /**
   * Getter for the authTokenSignature.
   *
   * @return the authTokenSignature for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
          description = "Gets the token signature for this CloudDB project.")
  public String Token() {
    checkProjectIDNotBlank();
    return token;
  }

  @SimpleFunction
  public void PerformSyncNow(){
    SyncJob.scheduleSync();
  }

  /**
   * Asks CloudDB to store the given value under the given tag.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public void StoreValue(final String tag, final Object valueToStore) {
    Log.i("CloudDB","StoreValue");
    checkProjectIDNotBlank();
    Log.i("CloudDB","PASSSSS");

    final String value;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();

    try {
      if (valueToStore != null) {
        String strval = valueToStore.toString();
        if (strval.startsWith("file:///") || strval.startsWith("/storage")) {
          value = JsonUtil.getJsonRepresentation(readFile(strval));
        } else {
          value = JsonUtil.getJsonRepresentation(valueToStore);
        }
      } else {
        value = "";
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    //Natalie: perform the store operation
    //valueToStore is always converted to JSON (String);
    if(isConnected){
      Log.i("CloudDB","Device is online...");
      background.submit(new Runnable() {
          public void run() {
            Jedis jedis = getJedis();
            Log.i("CloudDB", "Before set is called...");
            long statusCodeReply = jedis.zadd(projectID+tag, System.currentTimeMillis(),value);
            Log.i("CloudDB", "Jedis Key = " + projectID+tag);
            Log.i("CloudDB", "Jedis TS = " + System.currentTimeMillis());
            Log.i("CloudDB", "Jedis Val = " + value);
          }
        });
    }
    else if(sync){
      Log.i("CloudDB","Device is offline...");
      Log.i("CloudDB","Proceed to cache data locally...");
      sendValueTocache(tag,value);
    }
    Log.i("CloudDB", "End of StoreValue...");
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
    checkProjectIDNotBlank();
    Log.d(CloudDB.LOG_TAG,"getting value ...");
    final AtomicReference<Object> value = new AtomicReference<Object>();
    Cursor cursor = null;
    SQLiteDatabase db = null;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();

    if(!isConnected && sync){
      /*
      read from cache
       */
      Log.d(CloudDB.LOG_TAG,"reading from cache ...");
      try {
        //CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(form.$context());
        db = CloudDBCacheHelper.getInstance(form.$context()).getWritableDatabase();

        String[] projection = {CloudDBCache.Table1.COLUMN_NAME_VALUE, CloudDBCache.Table1.COLUMN_TIMESTAMP};
        String selection = CloudDBCache.Table1.COLUMN_NAME_KEY + " = ? ";
        String[] selectionArgs = {this.projectID+tag};
        String orderby = CloudDBCache.Table1.COLUMN_TIMESTAMP + " DESC";
        cursor = db.query(CloudDBCache.Table1.TABLE_NAME, projection, selection, selectionArgs, null, null, orderby);
        String val;
        if (cursor != null && cursor.moveToNext()) {
          Log.d(CloudDB.LOG_TAG,"cursor has values");
          val = cursor.getString(cursor.getColumnIndex(CloudDBCache.Table1.COLUMN_NAME_VALUE));
          Log.d(CloudDB.LOG_TAG,"value retrieved = " + val + " from cache");
          String jsonVal = this.getJsonRepresenationIfValueFileName(val);
          if(jsonVal != null) value.set(jsonVal);
          else value.set(val);
        }
        else {
          Log.d(CloudDB.LOG_TAG,"cursor has no values");
          value.set(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
        }
        Log.d(CloudDB.LOG_TAG,"value set successfully");
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that the value was
            // Signal an event to indicate that the value was
            // received.  We post this to run in the Application's main
            // UI thread.
            GotValue(tag, value.get());
          }
        });
      } catch (Exception e) {
        //Log.d(CloudDB.LOG_TAG, "Error occurred while reading from cache...");
        Log.e(CloudDB.LOG_TAG,"Error occurred while reading from cache",e);
        //e.printStackTrace();
      }
      finally {
        if(cursor != null) cursor.close();
        if(db != null) db.close();
      }
    }
    else if(isConnected){
      // Set value to either the JSON from the CloudDB
      // or the JSON representation of valueIfTagNotThere
      background.submit(new Runnable() {
          public void run() {
            Jedis jedis = getJedis();
            try {
              Log.d(CloudDB.LOG_TAG,"reading from Redis ...");
              Set<String> returnValues = jedis.zrange(projectID+tag,0,-1);
              Log.d(CloudDB.LOG_TAG,"zrange success ...");
              String returnValue = null;
              if(returnValues != null && !returnValues.isEmpty()){
                returnValue = returnValues.toArray()[returnValues.size()-1].toString();
              }
              Log.d(CloudDB.LOG_TAG,"Device is online = " + returnValue);
              if (returnValue != null) {
                String val = getJsonRepresenationIfValueFileName(returnValue);
                if(val != null) value.set(val);
                else value.set(returnValue);
              }
              else {
                Log.d(CloudDB.LOG_TAG,"Value retrieved is null");
                value.set(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
              }
            } catch(JSONException e) {
              throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
            }
            catch(NullPointerException e){
              Log.d(CloudDB.LOG_TAG,"error while zrange...");
              throw new YailRuntimeError("zrange threw a runtime exception.", "Redis runtime exception.");
            }

            androidUIHandler.post(new Runnable() {
                public void run() {
                  // Signal an event to indicate that the value was
                  // received.  We post this to run in the Application's main
                  // UI thread.
                  GotValue(tag, value.get());
                }
              });
          }
        });
    }
  }

  /**
   * GetValues asks CloudDB to get a list of values stored under the given tag.
   * It will pass valueIfTagNotThere to GotValue if there is no value stored
   * under the tag.
   *
   * @param tag The tag whose values is to be retrieved.
   * @param valueIfTagNotThere The value to pass to the event if the tag does
   *                           not exist.
   */
  @SimpleFunction
  public void GetValues(final String tag, final Object valueIfTagNotThere) {
    checkProjectIDNotBlank();
    Log.d(CloudDB.LOG_TAG,"getting value ...");
    final AtomicReference<Object> value = new AtomicReference<Object>();
    Cursor cursor = null;
    SQLiteDatabase db = null;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();

    if(!isConnected && sync){
      /*
      read from cache
       */
      Log.d(CloudDB.LOG_TAG,"reading from cache ...");
      try {
        //CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(form.$context());
        db = CloudDBCacheHelper.getInstance(form.$context()).getWritableDatabase();
        final List<String> values = new ArrayList<>();

        String[] projection = {CloudDBCache.Table1.COLUMN_NAME_VALUE, CloudDBCache.Table1.COLUMN_TIMESTAMP};
        String selection = CloudDBCache.Table1.COLUMN_NAME_KEY + " = ? ";
        String[] selectionArgs = {this.projectID+tag};
        String orderby = CloudDBCache.Table1.COLUMN_TIMESTAMP + " DESC";
        cursor = db.query(CloudDBCache.Table1.TABLE_NAME, projection, selection, selectionArgs, null, null, orderby);
        if (cursor != null){
          while(cursor.moveToNext()) {
            Log.d(CloudDB.LOG_TAG, "cursor has values");
            String val = cursor.getString(cursor.getColumnIndex(CloudDBCache.Table1.COLUMN_NAME_VALUE));
            values.add(val);
            Log.d(CloudDB.LOG_TAG, "value retrieved = " + val);
          }
        }
        else {
          Log.d(CloudDB.LOG_TAG,"cursor has no values");
          values.add(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
        }
        Log.d(CloudDB.LOG_TAG,"value set successfully");
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that the value was
            // received.  We post this to run in the Application's main
            // UI thread.
            GotValue(tag, values);
          }
        });
      } catch (Exception e) {
        //Log.d(CloudDB.LOG_TAG, "Error occurred while reading from cache...");
        Log.e(CloudDB.LOG_TAG,"Error occurred while reading from cache",e);
        //e.printStackTrace();
      }
      finally {
        if(cursor != null) cursor.close();
        if(db != null) db.close();
      }
    }
    else if(isConnected){
      // Set value to either the JSON from the CloudDB
      // or the JSON representation of valueIfTagNotThere
      background.submit(new Runnable() {
          public void run() {
            Jedis jedis = getJedis();
            final List<String> returnValuesList;
            try {
              Log.d(CloudDB.LOG_TAG,"reading from Redis ...");
              Set<String> returnValues = jedis.zrange(projectID+tag,0,-1);
              Log.d(CloudDB.LOG_TAG,"zrange success ...");

              if(returnValues != null && !returnValues.isEmpty()){
                returnValuesList = new ArrayList<>(returnValues);
              }
              else{
                returnValuesList = new ArrayList<>();
                returnValuesList.add(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
              }
              androidUIHandler.post(new Runnable() {
                  public void run() {
                    // Signal an event to indicate that the value was
                    // received.  We post this to run in the Application's main
                    // UI thread.
                    GotValue(tag, returnValuesList);
                  }
                });
            }
            catch(JSONException e){
              Log.e(CloudDB.LOG_TAG,"error while converting to JSON...",e);
            }
            catch(NullPointerException e){
              Log.e(CloudDB.LOG_TAG,"error while zrange...",e);
              throw new YailRuntimeError("zrange threw a runtime exception.", "Redis runtime exception.");
            }
            catch(Exception e){
              Log.e(CloudDB.LOG_TAG,"error while making list...",e);
            }

            finally {
              if(jedis != null) jedis.close();
            }
          }
        });
    }
  }

  @SimpleEvent(description = "Event triggered by the \"RemoveFirstFromList\" function. The " +
    "argument \"value\" is the object that was the first in the list, and which is now " +
    "removed.")
  public void FirstRemoved(Object value) {
    checkProjectIDNotBlank();
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
    checkProjectIDNotBlank();

    final String key = projectID + tag;

    background.submit(new Runnable() {
        public void run() {
          Jedis jedis = getJedis();
          try {
            FirstRemoved(jedis.eval(POP_FIRST_SCRIPT, 1, key));
          } catch(JedisException e) {

          } finally {
            jedis.close();
          }
        }
      });
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
    checkProjectIDNotBlank();

    Object itemObject = new Object();
    try {
      if(itemToAdd != null) {
        itemObject = JsonUtil.getJsonRepresentation(itemToAdd);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    final String item = (String) itemObject;
    final String key = projectID + tag;

    background.submit(new Runnable() {
        public void run() {
          Jedis jedis = getJedis();
          try {
            jedis.eval(APPEND_SCRIPT, 1, key, item);
          } catch(JedisException e) {

          } finally {
            jedis.close();
          }
        }
      });
  }

  /**
   * Indicates that a GetValue request has succeeded.
   *
   * @param value the value that was returned. Can be any type of value
   *              (e.g. number, text, boolean or list).
   */
  @SimpleEvent
  public void GotValue(String tag, Object value) {
    Log.d(CloudDB.LOG_TAG, "GotValue: tag = " + tag + " value = " + (String) value);
    checkProjectIDNotBlank();

    try {
      Log.d(LOG_TAG, "GotValue: Class of value = " + value.getClass().getName());
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
    checkProjectIDNotBlank();
    Jedis jedis = getJedis();
    jedis.del(projectID+tag);
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
    checkProjectIDNotBlank();

    Jedis jedis = getJedis();

    Set<String> value = jedis.keys(projectID+"*");
    final List<String> listValue = new ArrayList<String>(value);

    for(int i = 0; i < listValue.size(); i++){
      listValue.set(i, listValue.get(i).substring((projectID).length()));
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
    // Natalie: Why is this not called "GotTagList"? Also need to only
    // show tag without or projectID
    checkProjectIDNotBlank();
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
            System.out.println(tagValue);
          }
        } catch(JSONException e) {
          throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
        }

        String parsedTag = tag.substring(projectID.length()+9); // 9 is for debugging

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

  private void checkProjectIDNotBlank(){
    if (projectID.equals("")){
      throw new RuntimeException("CloudDB ProjectID property cannot be blank.");
    }
    if(token.equals("")){
      throw new RuntimeException("CloudDB Token property cannot be blank");
    }
  }

  private Jedis getJedis(boolean createNew) {
    Jedis jedis;
    try {
      jedis = new Jedis(redisServer, redisPort);
      jedis.auth("test6789");
    } catch (JedisConnectionException e) {
      return null;
    }
    return jedis;
  }

  public Jedis getJedis(){
    if (INSTANCE == null) {
      INSTANCE = getJedis(true);
    }
    return INSTANCE;
  }

 /**
   * Accepts a file name and returns a Yail List with two
   * elements. the first element is the file's extension (example:
   * jpg, gif, etc.). The second element is the base64 encoded
   * contents of the file. This function is suitable for reading
   * binary files such as sounds and images. The base64 contents can
   * then be stored with mechanisms oriented around text, such as
   * tinyDB, Fusion tables and Firebase.
   *
   * Written by Jeff Schiller (jis) for the BinFile Extension
   *
   * @param fileName
   * @returns YailList the list of the file extension and contents
   */
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
      results[0] = "." + extension;
      results[1] = encodedContent;
      return YailList.makeList(results);
    } catch (FileNotFoundException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    } catch (IOException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    }
  }

  /**
   * Accepts a base64 encoded string and a file extension (which must be three characters).
   * Decodes the string into a binary and saves it to a file on external storage and returns
   * the filename assigned.
   *
   * Written by Jeff Schiller (jis) for the BinFile Extension
   *
   * @param input Base64 input string
   * @param fileExtension three character file extension
   * @return the name of the created file
   */
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

  // keep only the last N files, where N = maxSavedFiles
  // Written by Jeff Schiller (jis) for the BinFile Extension
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

  // Utility to get the file extension from a filename
  // Written by Jeff Schiller (jis) for the BinFile Extension
  private String getFileExtension(String fullName) {
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf(".");
    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }

  /*
  Written by joymitro@gmail.com (Joydeep Mitra)
  saves data to a local SQLiteDb when device is offline
   */
  public void sendValueTocache(final String tag, final String value) {
    Cursor cursor = null;
    SQLiteDatabase db = null;

      try {
          //CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(form.$context());
          db = CloudDBCacheHelper.getInstance(form.$context()).getWritableDatabase();

          //insert new key
          ContentValues contentValues = new ContentValues();
          contentValues.put(CloudDBCache.Table1.COLUMN_NAME_KEY, this.projectID + tag);
          contentValues.put(CloudDBCache.Table1.COLUMN_NAME_VALUE, value);
          contentValues.put(CloudDBCache.Table1.COLUMN_UPLOAD_FLAG,0);
          contentValues.put(CloudDBCache.Table1.COLUMN_TIMESTAMP,System.currentTimeMillis());
          db.insert(CloudDBCache.Table1.TABLE_NAME, null, contentValues);

      } catch (Exception e) {
          Log.d("CloudDB", "Error occurred while caching data locally...");
          e.printStackTrace();
      }
      finally {
        if(cursor !=null) cursor.close();
        if(db != null) db.close();
      }

  }

  /*
  Written by joymitro@gmail.com (Joydeep Mitra)
  saves the token to a local SQLiteDb
   */
  private void saveToken(final String token) {
    Cursor cursor = null;
    SQLiteDatabase db = null;
    Log.d(CloudDB.LOG_TAG,"saving token in cache ...");
    try {
      //CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(form.$context());
      db = CloudDBCacheHelper.getInstance(form.$context()).getWritableDatabase();

      String[] projection = {CloudDBCache.Table2.COLUMN_TOKEN};
      cursor = db.query(CloudDBCache.Table2.TABLE_NAME, projection, null, null, null, null, null);
      if (cursor != null && cursor.getCount() == 1) {
        //update existing token
        ContentValues values = new ContentValues();
        values.put(CloudDBCache.Table2.COLUMN_TOKEN, token);
        db.update(CloudDBCache.Table2.TABLE_NAME, values, null, null);
      }
      else {
        //insert new key
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudDBCache.Table2.COLUMN_TOKEN, token);
        db.insert(CloudDBCache.Table2.TABLE_NAME, null, contentValues);
      }
    } catch (Exception e) {
      Log.d("CloudDB", "Error occurred while caching token locally...");
      e.printStackTrace();
    }
    finally {
      if(cursor !=null) cursor.close();
      if(db != null) db.close();
    }

  }

  /*
  * Written by joymitro@gmail.com (Joydeep Mitra)
  * This method converts a file path to a JSON representation.
  * The code in the method was part of GetValue. For better modularity and reusability
  * the logic is now part of this method, which can be invoked from wherever and
  * whenever required.
  *
  * @param file path
  * @return JSON representation
  */
  private String getJsonRepresenationIfValueFileName(String value){
    try {
      JSONArray valueJsonList = new JSONArray(value);
      List<String> valueList = JsonUtil.getStringListFromJsonArray(valueJsonList);
      if (valueList.size() == 2) {
        if (valueList.get(0).startsWith(".")) {
          String filename = writeFile(valueList.get(1), valueList.get(0).substring(1));
          System.out.println("Filename Written: " + filename);
          filename = filename.replace("file:/", "file:///");
          return JsonUtil.getJsonRepresentation(filename);
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch(JSONException e) {
      return null;
    }
  }
}


/*private String getToken(){
    SQLiteDatabase db = null;
    Cursor cursor = null;
    Log.d(CloudDB.LOG_TAG,"getting token from cache...");
    try {
      db = CloudDBCacheHelper.getInstance(form.$context()).getWritableDatabase();
      String[] projection = {CloudDBCache.Table2.COLUMN_TOKEN};
      cursor = db.query(CloudDBCache.Table2.TABLE_NAME, projection, null, null, null, null, null);
      String val;
      if (cursor != null && cursor.moveToNext()) {
        Log.d(CloudDB.LOG_TAG,"Token found");
        val = cursor.getString(cursor.getColumnIndex(CloudDBCache.Table2.COLUMN_TOKEN));
        Log.d(CloudDB.LOG_TAG,"token retrieved = " + val + " from cache");
        return val;
      }
      else {
        Log.d(CloudDB.LOG_TAG,"token not found in cache");
        return null;
      }
    } catch (Exception e) {
      //Log.d(CloudDB.LOG_TAG, "Error occurred while reading from cache...");
      Log.e(CloudDB.LOG_TAG,"Error occurred while reading token from cache",e);
      return null;
      //e.printStackTrace();
    }
    finally {
      if(cursor !=null) cursor.close();
      if(db != null) db.close();
    }
  }*/
