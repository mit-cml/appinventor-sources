// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Config;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

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

import org.json.JSONException;


/**
 * The Firebase component communicates with a Web service to store
 * and retrieve information.  The component has methods to
 * store a value under a tag and to retrieve the value associated with
 * the tag. It also possesses a listener to fire events when stored
 * values are changed.
 *
 * @author kasmus@mit.edu (Kristin Asmus)
 * @author will2596@gmail.com (William Byrne) (default Firebase partitioning and user authentication)
 * @author jis@mit.edu (Jeffrey I. Schiller) (defaultURL setup at runtime, other cleanup)
 */

@DesignerComponent(version = YaVersion.FIREBASE_COMPONENT_VERSION,
    description = "Non-visible component that communicates with Firebase to store and " +
    "retrieve information.",
    designerHelpDescription = "Non-visible component that communicates with a Firebase" +
        " to store and retrieve information.",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    androidMinSdk = 10,
    iconName = "images/firebaseDB.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "firebase.jar")
public class FirebaseDB extends AndroidNonvisibleComponent implements Component {

  private static final String LOG_TAG = "Firebase";
  private String firebaseURL = null;
  private String defaultURL = null;
  private boolean useDefault = true;
  private String developerBucket;
  private String projectBucket;
  private String firebaseToken;
  // Note: The two variables below are static because the systems they
  // interact with within Firebase are also static
  private static boolean isInitialized = false;  // Whether we have made our first
                                                 // connection to Firebase
  private static boolean persist = false;        // Whether or not we are in persistant mode
                                                 // where variables are kept when an app exits
                                                 // when off-line
  private Handler androidUIHandler;
  private final Activity activity;
  private Firebase myFirebase;
  private ChildEventListener childListener;
  private Firebase.AuthStateListener authListener;

  // ReturnVal -- Holder which can be used as a final value but whose content
  //              remains mutable.
  private static class ReturnVal {
    String err;                 // Holder for any errors
    Object retval;              // Returned value

    Object getRetval() {
      return retval;
    }

  }

  private abstract static class Transactional {
    final Object arg1;
    final Object arg2;
    final ReturnVal retv;

    Transactional(Object arg1, Object arg2, ReturnVal retv) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.retv = retv;
    }

    abstract Transaction.Result run(MutableData currentData);

    ReturnVal getResult() {
      return retv;
    }

  }

  /**
   * Creates a new Firebase component.
   *
   * @param container the Form that this component is contained in.
   */
  public FirebaseDB(ComponentContainer container) {
    super(container.$form());
    // We use androidUIHandler when we set up operations that run asynchronously
    // in a separate thread, but which themselves want to cause actions
    // back in the UI thread.  They do this by posting those actions
    // to androidUIHandler.
    androidUIHandler = new Handler();
    this.activity = container.$context();
    Firebase.setAndroidContext(activity);

    developerBucket = ""; // set dynamically in the Designer
    projectBucket = ""; // given a dynamic default value in the Designer
    firebaseToken = ""; // set dynamically in the Designer

    childListener = new ChildEventListener() {
      // Retrieve new posts as they are added to the Firebase.
      @Override
      public void onChildAdded(final DataSnapshot snapshot, String previousChildKey) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that the child data was changed.
            // We post this to run in the Application's main UI thread.
            DataChanged(snapshot.getKey(), snapshot.getValue());
          }
        });
      }

      @Override
      public void onCancelled(final FirebaseError error) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that an error occurred.
            // We post this to run in the Application's main UI thread.
            FirebaseError(error.getMessage());
          }
        });
      }

      @Override
      public void onChildChanged(final DataSnapshot snapshot, String previousChildKey) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that the child data was changed.
            // We post this to run in the Application's main UI thread.
            DataChanged(snapshot.getKey(), snapshot.getValue());
          }
        });
      }

      @Override
      public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
      }

      @Override
      public void onChildRemoved(final DataSnapshot snapshot) {
        Log.i(LOG_TAG, "onChildRemoved: " + snapshot.getKey() + " removed.");
        // We do *NOT* run the code below because triggering an event
        // with a null argument causes problems in App Inventor programs
        // If people need to know when a child is removed, we should add
        // a new event which only takes the tag as a parameter
        // androidUIHandler.post(new Runnable() {
        //   public void run() {
        //     // Signal an event to indicate that the child data was changed.
        //     // We post this to run in the Application's main UI thread.
        //     DataChanged(snapshot.getKey(), null);
        //   }
        // });
      }
    };

    authListener = new Firebase.AuthStateListener() {
      @Override
      public void onAuthStateChanged(AuthData data) {
        Log.i(LOG_TAG, "onAuthStateChanged: data = " + data);
        if (data == null) {
          myFirebase.authWithCustomToken(firebaseToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
              Log.i(LOG_TAG, "Auth Successful.");
            }

            @Override
            public void onAuthenticationError(FirebaseError error) {
              Log.e(LOG_TAG, "Auth Failed: " + error.getMessage());
            }
          });
        }
      }
    };
  }

  /**
   * Initialize: Do runtime intiailization of FirebaseDB
   * We cannot make a connection to Firebase in the component
   * Constructor because we do not yet know the value of the
   * Persist property. The Persist property is used to set the
   * persistance flag in the Firebase static config. It must
   * be set prior to any connection happening and cannot be
   * changed after a Firebase connection (or reference) is made.
   * So we defer making a connection until we initialize. Initialize
   * is called from runtime.scm (via Form.java) after all components
   * and properties have been setup.
   */
  public void Initialize() {
    Log.i(LOG_TAG, "Initalize called!");
    isInitialized = true;
    resetListener();
  }

  /**
   * Getter for the Firebase URL.
   *
   * @return the URL for this Firebase
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "Gets the URL for this FirebaseDB.",
    userVisible = false)
  public String FirebaseURL() {
    if (useDefault) {
      return "DEFAULT";
    } else {
      return firebaseURL;
    }
  }

  /**
   * Specifies the URL for the Firebase.
   *
   * The default value is currently my private Firebase URL, but this will
   * eventually changed once the App Inventor Candle plan is activated.
   *
   * @param url the URL for the Firebase
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FIREBASE_URL,
    defaultValue = "DEFAULT")
  @SimpleProperty(description = "Sets the URL for this FirebaseDB.")
  public void FirebaseURL(String url) {
    if (url.equals("DEFAULT")) {
      if (!useDefault) {        // If we weren't setup for the default
        useDefault = true;
        if (defaultURL == null) { // Not setup yet
          Log.d(LOG_TAG, "FirebaseURL called before DefaultURL (should not happen!)");
        } else {
          firebaseURL = defaultURL;
          resetListener();
        }
      } else {
        firebaseURL = defaultURL; // Should already be the case
      }
    } else {
      useDefault = false;
      url = url + (url.endsWith("/") ? "" : "/");

      if (firebaseURL.equals(url)) {
        return;                 // Nothing to do
      } else {
        firebaseURL = url;
        useDefault = false;
        resetListener();
      }
    }
  }

  /**
   * Getter for the DeveloperBucket.
   *
   * @return the DeveloperBucket for this Firebase
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String DeveloperBucket() {
    return developerBucket;
  }

  /**
   * Specifies the unique developer path of the Firebase. This is set programmatically
   * in {@link com.google.appinventor.client.editor.simple.components.MockFirebaseDB}
   * and consists of the current App Inventor user's email.
   *
   * @param bucket the name of the developer's bucket
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty
  public void DeveloperBucket(String bucket) {
    developerBucket = bucket;
    resetListener();
  }

  /**
   * Getter for the ProjectBucket.
   *
   * @return the ProjectBucket for this Firebase
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the ProjectBucket for this FirebaseDB.")
  public String ProjectBucket() {
    return projectBucket;
  }

  /**
   * Specifies the path for the project bucket of the Firebase.
   *
   * @param bucket the name of the project's bucket
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "Sets the ProjectBucket for this FirebaseDB.")
  public void ProjectBucket(String bucket) {
    if (!projectBucket.equals(bucket)) {
      projectBucket = bucket;
      resetListener();
    }
  }

  /**
   * Getter for the FirebaseToken.
   *
   * @return the JWT used to authenticate users on the default Firebase
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String FirebaseToken() {
    return firebaseToken;
  }

  /**
   * Specifies the JWT for the default Firebase.
   *
   * @param JWT the JSON Web Token (JWT) used to authenticate on the
   *            default Firebase
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty
  public void FirebaseToken(String JWT) {
    firebaseToken = JWT;
    resetListener();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  @SimpleProperty(userVisible = false,
    description = "If true, variables will retain their values when off-line and the App " +
    "exits. Values will be uploaded to Firebase the next time the App is " +
    "run while connected to the network. This is useful for applications " +
    "which will gather data while not connected to the network. Note: " +
    "AppendValue and RemoveFirst will not work correctly when off-line, " +
    "they require a network connection.<br/><br/> " +
    "<i>Note</i>: If you set Persist on any Firebase component, on any " +
    "screen, it makes all Firebase components on all screens persistent. " +
    "This is a limitation of the low level Firebase library. Also be " +
    "aware that if you want to set persist to true, you should do so " +
    "before connecting the Companion for incremental development.")
  public void Persist(boolean value) {
    Log.i(LOG_TAG, "Persist Called: Value = " + value);
    if (persist != value) {     // We are making a change
      if (isInitialized) {
        throw new RuntimeException("You cannot change the Persist value of Firebase " +
          "after Application Initialization, this includes the Companion");
      }
      Config config = Firebase.getDefaultConfig();
      config.setPersistenceEnabled(value);
      Firebase.setDefaultConfig(config);
      persist = value;
      resetListener();
    }
  }

  private void resetListener() {
    // If Firebase has not been inialized, then
    // we do nothing.
    if (!isInitialized) {
      return;
    }
    // remove listeners from the old Firebase path
    if (myFirebase != null) {
      myFirebase.removeEventListener(childListener);
      myFirebase.removeAuthStateListener(authListener);
    }

    myFirebase = null;
    connectFirebase();          // Reconnect to Firebase with new parameters
  }

  /*
     TODO (William Byrne): Implement Transactions

     As things stand, any operation performed on a tag that depends on the
     existing data at the tag is vulnerable to concurrent modification bugs.
     This is caused by the inherent non-atomicity of such an operation using
     the current component blocks. One way to solve this problem would be to
     use the Firebase#runTransaction(Transaction.Handler) method to run such an
     operation atomically. However, that entails either creating a RunTransaction
     block that accepts both an operation to perform on the cloud variable and
     additional data or creating individual blocks performing commonly needed
     operations on cloud variables (e.g. increment, decrement, append to list, etc)
     atomically. Since both of those solutions require involved implementations,
     this issue is being left for Version 2.

     Additional Documentation relating to Firebase Transactions can be found below:

     https://www.firebase.com/docs/android/guide/saving-data.html#section-transactions

     https://www.firebase.com/docs/java-api/javadoc/com/firebase/client/Transaction.html,

     https://www.firebase.com/docs/java-api/javadoc/com/firebase/client/Transaction.Handler.html,

     https://www.firebase.com/docs/java-api/javadoc/com/firebase/client/Firebase.html#runTransaction-com.firebase.client.Transaction.Handler-
   */

  /**
   * Asks Firebase to forget (delete or set to "null") a given tag.
   *
   * @param tag The tag to remove
   */

  @SimpleFunction(description = "Remove the tag from Firebase")
  public void ClearTag(final String tag) {
    this.myFirebase.child(tag).removeValue();
  }

  /**
   * Asks Firebase to store the given value under the given tag.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  public void StoreValue(final String tag, Object valueToStore) {
    try {
      if(valueToStore != null) {
        valueToStore = JsonUtil.getJsonRepresentation(valueToStore);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    // perform the store operation
    this.myFirebase.child(tag).setValue(valueToStore);
  }

  /**
   * GetValue asks Firebase to get the value stored under the given tag.
   * It will pass valueIfTagNotThere to GotValue if there is no value stored
   * under the tag.
   *
   * @param tag The tag whose value is to be retrieved.
   * @param valueIfTagNotThere The value to pass to the event if the tag does
   *                           not exist.
   */
  @SimpleFunction
  public void GetValue(final String tag, final Object valueIfTagNotThere) {
    this.myFirebase.child(tag).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(final DataSnapshot snapshot) {
        final AtomicReference<Object> value = new AtomicReference<Object>();

        // Set value to either the JSON from the Firebase
        // or the JSON representation of valueIfTagNotThere
        try {
          if (snapshot.exists()) {
            value.set(snapshot.getValue());
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
      }

      @Override
      public void onCancelled(final FirebaseError error) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            // Signal an event to indicate that an error occurred.
            // We post this to run in the Application's main
            // UI thread.
            FirebaseError(error.getMessage());
          }
        });
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
   * Indicates that the data in the Firebase has changed.
   * Launches an event with the tag and value that have been updated.
   *
   * @param tag the tag that has changed.
   * @param value the value that has changed.
   */
  @SimpleEvent
  public void DataChanged(String tag, Object value) {
    try {
      if(value != null && value instanceof String) {
        value = JsonUtil.getObjectFromJson((String) value);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
    }

    // Invoke the application's "DataChanged" event handler
    EventDispatcher.dispatchEvent(this, "DataChanged", tag, value);
  }

  /**
   * Indicates that the communication with the Firebase signaled an error.
   *
   * @param message the error message
   */
  @SimpleEvent
  public void FirebaseError(String message) {
    // Log the error message for advanced developers
    Log.e(LOG_TAG, message);

    // Invoke the application's "FirebaseError" event handler
    boolean dispatched = EventDispatcher.dispatchEvent(this, "FirebaseError", message);
    if (!dispatched) {
      // If the handler doesn't exist, then put up our own alert
      Notifier.oneButtonAlert(form, message, "FirebaseError", "Continue");
    }
  }

  private void connectFirebase() {
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_GINGERBREAD_MR1) {
      Notifier.oneButtonAlert(activity, "The version of Android on this device is too old to use Firebase.",
        "Android Too Old", "OK");
      return;
    }
    if(useDefault) {
      myFirebase = new Firebase(firebaseURL + "developers/" + developerBucket + projectBucket);
    } else {
      myFirebase = new Firebase(firebaseURL + projectBucket);
    }
    // add listeners to the new Firebase path
    myFirebase.addChildEventListener(childListener);
    myFirebase.addAuthStateListener(authListener);
  }

  /**
   * Unauthenticate from Firebase.
   *
   * Firebase keeps track of credentials in a cache in shared_prefs
   * It will re-use these credentials as long as they are valid. Given
   * That we retrieve a FirebaseToken with a version long life, this will
   * effectively be forever. Shared_prefs survive an application update
   * and depending on how backup is configured on a device, it might survive
   * an application removal and reinstallation.
   *
   * Normally this is not a problem, however if we change the credentials
   * used, for example the App author is switching from one Firebase account
   * to another, or invalided their firebase.secret, this cached credential
   * is invalid, but will continue to be used, which results in errors.
   *
   * This function permits us to unauthenticate, which tosses the cached
   * credentials. The next time authentication is needed we will use our
   * current FirebaseToken and get fresh credentials.
   */

  @SimpleFunction(description = "If you are having difficulty with the Companion and you " +
    "are switching between different Firebase accounts, you may need to use this function " +
    "to clear internal Firebase caches. You can just use the \"Do It\" function on this block " +
    "in the blocks editor. Note: You should not normally need to use this block as part of " +
    "an application.")
  public void Unauthenticate() {
    if (myFirebase == null) {
      connectFirebase();
    }
    myFirebase.unauth();
  }

  // This is a non-documented property because it is hidden in the
  // UI. Its purpose in life is to transmit the default firebase URL
  // from the system into the Companion or packaged app. The Default
  // URL is set in appengine-web.xml (the firebase.url property). It
  // is sent to the client from the server via the system config call
  // and sent hear from MockFirebaseDB.java

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    userVisible = false)
  public void DefaultURL(String url) {
    defaultURL = url;
    if (useDefault) {
      firebaseURL = defaultURL;
      resetListener();
    }
  }

  @SimpleFunction(description = "Return the first element of a list and atomically remove it. " +
    "If two devices use this function simultaneously, one will get the first element and the " +
    "the other will get the second element, or an error if there is no available element. " +
    "When the element is available, the \"FirstRemoved\" event will be triggered.")
  public void RemoveFirst(final String tag) {
    final ReturnVal result = new ReturnVal();
    Firebase firebaseChild = myFirebase.child(tag);
    Transactional toRun = new Transactional(null, null, result) {
        @Override
        Transaction.Result run(MutableData currentData) {
          Object value = currentData.getValue();
          if (value == null) {
            result.err = "Previous value was empty.";
            return Transaction.abort();
          }
          try {
            if (value instanceof String) {
              value = JsonUtil.getObjectFromJson((String) value);
            } else {
              result.err = "Invalid JSON object in database (shouldn't happen!)";
              return Transaction.abort();
            }
          } catch (JSONException e) {
            result.err = "Invalid JSON object in database (shouldn't happen!)";
            return Transaction.abort();
          }
          if (value instanceof List) {
            if (((List)value).isEmpty()) {
              result.err = "The list was empty";
              return Transaction.abort();
            }
            result.retval = ((List)value).remove(0);
            try {
              value = JsonUtil.getJsonRepresentation(YailList.makeList((List)value));
            } catch (JSONException e) {
              result.err = "Could not convert value to JSON.";
              return Transaction.abort();
            }
            currentData.setValue(value);
            return Transaction.success(currentData);
          } else {
            result.err = "You can only remove elements from a list.";
            return Transaction.abort();
          }
        }
      };
    firebaseTransaction(toRun, firebaseChild, new Runnable() {
        @Override
        public void run() {
          FirstRemoved(result.getRetval());
        }
      });
  }

  @SimpleFunction(description = "Get the list of tags for this application. " +
    "When complete a \"TagList\" event will be triggered with the list of " +
    "known tags.")
  public void GetTagList() {
    Firebase zFireBase = myFirebase.child(""); // Does this really clone the parent?
    zFireBase.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot data) {
          Object value = data.getValue();
          if (value instanceof HashMap) {
            value = new ArrayList(((HashMap)value).keySet());
            final List listValue = (List) value;
            androidUIHandler.post(new Runnable() {
                @Override
                public void run() {
                  TagList(listValue);
                }
              });
          }
        }
        @Override
        public void onCancelled(FirebaseError error) {
          // Do Nothing
        }
      });
  }

  @SimpleEvent(description = "Event triggered when we have received the list of known tags. " +
    "Used with the \"GetTagList\" Function.")
  public void TagList(List value) {
    EventDispatcher.dispatchEvent(this, "TagList", value);
  }

  @SimpleEvent(description = "Event triggered by the \"RemoveFirst\" function. The " +
    "argument \"value\" is the object that was the first in the list, and which is now " +
    "removed.")
  public void FirstRemoved(Object value) {
    EventDispatcher.dispatchEvent(this, "FirstRemoved", value);
  }

  @SimpleFunction(description = "Append a value to the end of a list atomically. " +
    "If two devices use this function simultaneously, both will be appended and no " +
    "data lost.")
  public void AppendValue(final String tag, final Object valueToAdd) {
    final ReturnVal result = new ReturnVal();
    Firebase firebaseChild = myFirebase.child(tag);
    Transactional toRun = new Transactional(null, null, result) {
        @Override
        Transaction.Result run(MutableData currentData) {
          Object value = currentData.getValue();
          if (value == null) {
            result.err = "Previous value was empty.";
            return Transaction.abort();
          }
          try {
            if (value instanceof String) {
              value = JsonUtil.getObjectFromJson((String) value);
            } else {
              result.err = "Invalid JSON object in database (shouldn't happen!)";
              return Transaction.abort();
            }
          } catch (JSONException e) {
            result.err = "Invalid JSON object in database (shouldn't happen!)";
            return Transaction.abort();
          }
          if (value instanceof List) {
            ((List)value).add(valueToAdd);
            try {
              value = JsonUtil.getJsonRepresentation((List)value);
            } catch (JSONException e) {
              result.err = "Could not convert value to JSON.";
              return Transaction.abort();
            }
            currentData.setValue(value);
            return Transaction.success(currentData);
          } else {
            result.err = "You can only append to a list.";
            return Transaction.abort();
          }
        }
      };
    firebaseTransaction(toRun, firebaseChild, null);
  }

  private void firebaseTransaction(final Transactional toRun, Firebase firebase, final Runnable whenDone) {
    final ReturnVal result = toRun.getResult();
    firebase.runTransaction(new Transaction.Handler() {
        @Override
        public Transaction.Result doTransaction(MutableData currentData) {
          return toRun.run(currentData);
        }

        @Override
        public void onComplete(final FirebaseError firebaseError, boolean committed,
          DataSnapshot currentData) {
          if (firebaseError != null) {
            androidUIHandler.post(new Runnable() {
                @Override
                public void run() {
                  Log.i(LOG_TAG, "AppendValue(onComplete): firebase: " + firebaseError.getMessage());
                  Log.i(LOG_TAG, "AppendValue(onComplete): result.err: " + result.err);
                  FirebaseError(firebaseError.getMessage());
                }
              });
            return;
          }
          if (!committed) {
            androidUIHandler.post(new Runnable() {
                @Override
                public void run() {
                  Log.i(LOG_TAG, "AppendValue(!committed): result.err: " + result.err);
                  FirebaseError(result.err);
                }
              });
          } else {
            if (whenDone != null) {
              androidUIHandler.post(whenDone);
            }
          }
          return;
        }
      });
  }
}
