// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.WebServiceUtil;

import android.os.Handler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

// When the component is installed in App Inventor, the Javadoc
// comments will become included in the automatically-generated system
// documentation, except for lines starting with tags (such as @author).
/**
 * The TinyWebDB component communicates with a Web service to store
 * and retrieve information.  Although this component is usable, it is
 * very limited and meant primarily as a demonstration for people who
 * would like to create their own components that talk to the Web.
 * The accompanying Web service is at
 * (http://appinvtinywebdb.appspot.com).  The component has methods to
 * store a value under a tag and to retrieve the value associated with
 * the tag.  The interpretation of what "store" and "retrieve" means
 * is up to the Web service.  In this implementation, all tags and
 * values are strings (text).  This restriction may be relaxed in
 * future versions.
 *
 * @author halabelson@google.com (Hal Abelson)
 */


// The annotations here provide information to the compiler about
// integrating the component into App Inventor system.  The following
// three annotations stipulate that TinyWeb DB will appear in the
// designer, that it will be an object in the App Inventor language,
// and say what Android system permissions it requires.
//


@DesignerComponent(version = YaVersion.TINYWEBDB_COMPONENT_VERSION,
    description = "Non-visible component that communicates with a Web service to store and " +
    "retrieve information.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/tinyWebDB.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class TinyWebDB extends AndroidNonvisibleComponent implements Component {

  private static final String LOG_TAG = "TinyWebDB";
  private static final String STOREAVALUE_COMMAND = "storeavalue";
  private static final String TAG_PARAMETER = "tag";
  private static final String VALUE_PARAMETER = "value";
  private static final String GETVALUE_COMMAND = "getvalue";

  private String serviceURL;
  private Handler androidUIHandler;

  /**
   * Creates a new TinyWebDB component.
   *
   * @param container the Form that this component is contained in.
   */
  public TinyWebDB(ComponentContainer container) {
    super(container.$form());
    // We use androidUIHandler when we set up operations (like
    // postStoreVaue and getStoreValue) that run asynchronously in a
    // separate thread, but which themselves want to cause actions
    // back in the UI thread.  They do this by posting those actions
    // to androidUIHandler.
    androidUIHandler = new Handler();
    // We set the initial value of serviceURL to be the
    // demo Web service.
    serviceURL = "http://appinvtinywebdb.appspot.com/";
  }

  // The two procedures below give the getter and setter for the
  // TinyWebDB component's ServiceURL property.  Each one has
  // a @SimpleProperty annotation to indicate that it's a property in
  // the language (and blocks will be generated for it).  The setter
  // also has a @DesignerProperty that makes this property appear in the
  // Properties listed with the component in the designer.  Here we've
  // stipulated that the property should appear with a default value:
  // the URL of the App Inv Tiny DB demonstration Web service.  Note
  // that this default specifies what should be shown in the designer:
  // it does not automatically set the value of ServiceURL by itself,
  // which is why we explicitly set the variable serviceURL above
  // where the component is created.

  /**
   * Returns the URL of the web service database.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String ServiceURL() {
    return serviceURL;
  }

  /**
   * Specifies the URL of the  Web service.
   * The default value is the demo service running on App Engine.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "http://appinvtinywebdb.appspot.com")
  @SimpleProperty
  public void ServiceURL(String url) {
    serviceURL = url;
  }

  // StoreValue (and GetValue below) show how use the
  // event-driven style we recommend for operations that communicate
  // over the Web.  For each operation, there's (a) The function the
  // user calls (e.g., StoreValue); (b) A non-user visible function
  // that that runs asynchronously to do the actual communication,
  // wait for the result, and signal an event when the result is
  // obtained; (c) the event handler for that result (e.g., ValueStored)

  // Here's part (a):  The component function itself.  All it does is arrange
  // for part (b) to run in a separate thread.

  /**
   * Asks the Web service to store the given value under the given tag
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction
  // The @SimpleFunction annotation arranges for this to be a
  // function (StoreValue)  associated with the component.
  public void StoreValue(final String tag, final Object valueToStore) {
    final Runnable call = new Runnable() {
      public void run() { postStoreValue(tag, valueToStore); }};
      AsynchUtil.runAsynchronously(call);
  }

  // Here's part (b): The actual communication, which runs
  // asynchronously.  It uses postCommand, from the WebServiceUtil
  // library.  PostCommand here takes four arguments: (1) The URL of
  // the Web service; (2) The name of the command to be posted to the
  // Web service; (3) parameters for the command; (4) an
  // AsyncCallbackPair, which specifies an onSuccess callback and an
  // onFailure callback.

  // The onSuccess callback is called with the response from the Web
  // server.  Here, for postStoreValue, we ignore the response, and
  // simply signal a ValueStored event.

  // The onFailure callback is called with an error message.  It calls
  // WebServiceError, which will signal a WebServiceError event for the
  // application.

  private void postStoreValue(String tag, Object valueToStore) {
    // The commented-out Log.w command writes a message to the
    // AppInventor Web server log.  It's useful to include these
    // commands to aid in debugging while the component is being
    // developed, and then commenting them out when the component is
    // deployed.
    // Log.w(LOG_TAG, "postStoreValue: sending tag = " +
    // tag + " and value = " + valueToStore);
    // Here we define the AsyncCallbackPair, myCallback.
    AsyncCallbackPair<String> myCallback = new AsyncCallbackPair<String>() {
      public void onSuccess(String response) {
        // the result here will be the JSON-encoded list ["STORED", tag, value]
        // but the component ignores this
        // Log.w(LOG_TAG, "postStoreValue: got result " + result);
        androidUIHandler.post(new Runnable() {
            public void run() {
              // Signal an event to indicate that the value was
              // stored.  We post this to run in the Applcation's main
              // UI thread, rather than in the separate thread where
              // postStoreValue is running.
              ValueStored();
            }
          });
      }
      public void onFailure(final String message) {
        // Pass any failure message from the Web service command back
        // to the error handler.
        androidUIHandler.post(new Runnable() {
          public void run() {
            WebServiceError(message);
          }
        });
      }
    };
    try {
      WebServiceUtil.getInstance().postCommand(serviceURL,
                  STOREAVALUE_COMMAND,
                  Lists.<NameValuePair>newArrayList(
                      new BasicNameValuePair(TAG_PARAMETER, tag),
                      new BasicNameValuePair(VALUE_PARAMETER,
                                             JsonUtil.getJsonRepresentation(valueToStore))),
                  myCallback);
    } catch (JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }
  }

  // Here's part (c): The event that gets signaled when the Web service
  // replies that the store command succeeded.  The application writer
  // might want to specify a handler for this event, perhaps to show
  // a confirmation to the end user.

  /**
   * Event indicating that a StoreValue server request has succeeded.
   */
  @SimpleEvent
  public void ValueStored() {
    // invoke the application's "ValueStored" event handler.
    EventDispatcher.dispatchEvent(this, "ValueStored");
  }

  // The implementation of GetValue uses the same three-procedure
  // event-driven strategy as for StoreVale.  The main difference is
  // that where StoreVale uses postCommand, GetValue uses
  // postCommandReturningArray, which expects a JSON-encoded array as
  // the respose from the Web service.  For the Web service we're
  // using the response should be the two-element array ["VALUE", value] (i.e.,
  // the actual value tagged with "VALUE").

  // The onSuccess callback checks the response and signals an error
  // if it was null.  Otherwise it returns the second element of the
  // response (i.e., the value).  It also arranges to catch a JSON exception in
  // case the result coming back from the service was garbled.

  // The onFailure callback signals a WebServiceError, just as with
  // StoreValue.

  /**
   * GetValue asks the Web service to get the value stored under the given tag.
   * It is up to the Web service what to return if there is no value stored
   * under the tag.  This component just accepts whatever is returned.
   *
   * @param tag The tag whose value is to be retrieved.
   */
  @SimpleFunction
  public void GetValue(final String tag) {
    final Runnable call = new Runnable() { public void run() { postGetValue(tag); }};
    AsynchUtil.runAsynchronously(call);
  }

  private void postGetValue(final String tag) {
    // Log.w(LOG_TAG, "postGetValue: sending tag = " + tag);
    AsyncCallbackPair<JSONArray> myCallback = new AsyncCallbackPair<JSONArray>() {
      public void onSuccess(JSONArray result) {
        if (result == null) {
          // Signal a Web error event to indicate that there was no response
          // to this request for a value.
          androidUIHandler.post(new Runnable() {
              public void run() {
                WebServiceError("The Web server did not respond to the get value request " +
                                "for the tag " + tag + ".");
              }
            });
          return;
        } else {
          try {
            // Log.w(LOG_TAG, "postGetValue: got result " + result);
            // The Web service is designed to return the JSON encoded list ["VALUE", tag, value]
            final String tagFromWebDB = result.getString(1);
            String value = result.getString(2);
            // If there's no entry with tag as a key then return the empty string.
            final Object valueFromWebDB = (value.length() == 0) ? "" :
                JsonUtil.getObjectFromJson(value);
            androidUIHandler.post(new Runnable() {
              public void run() {
                // signal an event to indicate that a good value was returned.  Note
                // that the event handler takes the value as an argument.
                GotValue(tagFromWebDB, valueFromWebDB);
              }
            });
          } catch (JSONException e) {
            // Signal a Web error event to indicate the the server
            // returned a garbled value.  From the user's perspective, there may be no practical
            // difference between this and the "no response" error above, but application
            // writers can create handlers to use these events as they choose.
            androidUIHandler.post(new Runnable() {
              public void run() {
                WebServiceError("The Web server returned a garbled value " +
                    "for the tag " + tag + ".");
              }
            });
            return;
          }
        }
      }
      public void onFailure(final String message) {
        // Signal a Web error event to indicate that there was no response
        // to this request for a value.  Note that this needs to be posted to the UI
        // thread to avoid a subsequent UI event causing an exception.
        androidUIHandler.post(new Runnable() {
          public void run() {
            WebServiceError(message);
          }
        });
        return;
      }
    };
    WebServiceUtil.getInstance().postCommandReturningArray(
        serviceURL,
        GETVALUE_COMMAND,
        Lists.<NameValuePair>newArrayList(new BasicNameValuePair(TAG_PARAMETER, tag)),
        myCallback);
    return;
  }

  /**
   * Indicates that a GetValue server request has succeeded.
   *
   * @param valueFromWebDB the value that was returned. Can be any type of value
   * (e.g. number, text, boolean or list).
   */
  @SimpleEvent
  public void GotValue(String tagFromWebDB, Object valueFromWebDB) {
    // Invoke the application's "GotValue" event handler
    EventDispatcher.dispatchEvent(this, "GotValue", tagFromWebDB, valueFromWebDB);
  }

  /**
   * Indicates that the communication with the Web service signaled an error
   *
   * @param message the error message
   */
  @SimpleEvent
  public void WebServiceError(String message) {
    // Invoke the application's "WebServiceError" event handler
    // Log.w(LOG_TAG, "calling error event handler: " + message);
    EventDispatcher.dispatchEvent(this, "WebServiceError", message);
  }
}
