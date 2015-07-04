// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jsonp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.appinventor.common.jsonp.JsonpConstants;
import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used for communicating with an HTTP server via JSONP requests.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class JsonpConnection {
  private static final int CONTACT_REQUEST_TIMER_DELAY = 1000;
  private static final int MINIMUM_UNRESPONSIVE_TIME_LIMIT = 2000;

  private static int jsonpRequestCounter = 0;
  private static final Map<String, AsyncCallback<String>> CALLBACKS =
      new HashMap<String, AsyncCallback<String>>();

  // Functions that will be used to transform the server's responses to the appropriate types.
  // Those that don't need the jsonParser can be static.
  protected static final Function<String, String> stringResponseDecoder =
      Functions.<String>identity();

  protected static final Function<String, Void> voidResponseDecoder =
      new Function<String, Void>() {
    public Void apply(String response) {
      return null;
    }
  };

  @VisibleForTesting
  public static final Function<String, Boolean> booleanResponseDecoder =
      new Function<String, Boolean>() {
    public Boolean apply(String response) {
      return Boolean.valueOf(response);
    }
  };

  @VisibleForTesting
  public static final Function<String, Integer> integerResponseDecoder =
      new Function<String, Integer>() {
    public Integer apply(String response) {
      return Integer.valueOf(response);
    }
  };

  @VisibleForTesting
  public final Function<String, String[]> stringArrayResponseDecoder;

  /**
   * The connection information for the JSONP server.
   */
  private final JsonpConnectionInfo connInfo;

  /**
   * The list of {@link ConnectivityListener}.
   */
  private final List<ConnectivityListener> connectivityListeners;

  /**
   * A timer for sending a contact request periodically to see if the server
   * is still alive.
   */
  private Timer contactTimer;

  /**
   * The time that last contact request was sent.
   */
  private long contactRequestTime;

  /**
   * The time limit (in milliseconds) within which we expect to have received a
   * response to the contact request.
   */
  private int unresponsiveTimeLimit;

  /**
   * A timer used to recognize that the server is unresponsive.
   */
  private Timer unresponsiveConnectionTimer;

  /**
   * The connectivity status. If false, the server might be dead.
   */
  private boolean connectivityStatus;

  /**
   * The JSON parser used to decode responses.
   */
  private final JSONParser jsonParser;

  /**
   * A {@link Function} used to escape URL query parameters.
   */
  private final Function<String, String> escapeQueryParameterFunction;

  /**
   * Creates a JsonpConnection object with the given connection information,
   * JSON parser, and escape function.
   *
   * @param connInfo the JSONP connection information
   * @param jsonParser the JSON parser
   * @param escapeQueryParameterFunction the escape function
   */
  public JsonpConnection(JsonpConnectionInfo connInfo, final JSONParser jsonParser,
      Function<String, String> escapeQueryParameterFunction) {
    this.connInfo = connInfo;
    this.jsonParser = jsonParser;
    this.escapeQueryParameterFunction = escapeQueryParameterFunction;

    stringArrayResponseDecoder = new Function<String, String[]>() {
      public String[] apply(String response) {
        if (response != null) {
          JSONValue jsonValue = jsonParser.parse(response);
          if (jsonValue != null) {
            JSONArray jsonArray = jsonValue.asArray();
            int size = jsonArray.size();
            String[] strings = new String[size];
            for (int i = 0; i < size; i++) {
              JSONValue element = jsonArray.get(i);
              strings[i] = (element != null) ? element.asString().getString() : null;
            }
            return strings;
          }
        }
        return null;
      }
    };

    connectivityListeners = new ArrayList<ConnectivityListener>();
    connectivityStatus = true;

    unresponsiveTimeLimit = MINIMUM_UNRESPONSIVE_TIME_LIMIT;
  }

  /**
   * Sends a JSONP request by embedding a script tag into the document.
   * Generates a new id for the request.
   *
   * @param request the request to be made
   * @param parameters the parameters for the request
   * @param function a function that transforms the response into the type
   *        that the callback needs
   * @param callback the callback that should be called with the transformed
   *        response
   */
  public <T> void sendJsonpRequest(String request, Map<String, Object> parameters,
      Function<String, ? extends T> function, AsyncCallback<T> callback) {
    String id = getNextRequestId();
    sendJsonpRequest(id, request, parameters, function, callback);
  }

  /**
   * Returns the next request id;
   */
  @VisibleForTesting
  public String getNextRequestId() {
    return "jr_" + (jsonpRequestCounter++);
  }

  /**
   * Sends a JSONP request by embedding a SCRIPT tag into the document.
   *
   * @param id the id used for the script tag and to identify the callback
   * @param request the request to be made
   * @param parameters the parameters for the request
   * @param function a function that transforms the response into the type
   *        that the callback needs
   * @param callback the callback that should be called with the transformed
   *        response
   */
  private <T> void sendJsonpRequest(String id, String request, Map<String, Object> parameters,
      final Function<String, ? extends T> function, final AsyncCallback<T> callback) {
    Preconditions.checkNotNull(id);

    // Prepare an intermediate callback that converts the String result to T.
    if (callback != null) {
      Preconditions.checkNotNull(function);
      CALLBACKS.put(id, new AsyncCallback<String>() {
        @Override
        public void onSuccess(String jsonResult) {
          T result;
          try {
            result = function.apply(jsonResult);
          } catch (RuntimeException e) {
            callback.onFailure(e);
            return;
          }
          callback.onSuccess(result);
        }

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
      });
    }

    // Insert a script tag into the document.
    Document document = Document.get();
    ScriptElement script = document.createScriptElement();
    String uri = makeURI(request, parameters, id);
    script.setSrc(uri);
    script.setId(id);
    Element bodyElement = document.getElementsByTagName("body").getItem(0);
    Element previous = document.getElementById(id);
    if (previous != null) {
      bodyElement.replaceChild(script, previous);
    } else {
      bodyElement.appendChild(script);
    }
  }

  /**
   * Sends a JSONP request by embedding a SCRIPT tag into the document and
   * then polls for the result by resending the request with the parameter
   * {@link JsonpConstants#POLLING}. We continue polling until we receive a
   * response that is not {@link JsonpConstants#NOT_FINISHED_YET}.
   *
   * @param request the request to be made
   * @param p the parameters for the request
   * @param function a function that transforms the response into the type
   *        that the callback needs
   * @param callback the callback that should be called with the transformed
   *        response
   */
  public <T> void sendJsonpRequestAndPoll(final String request, Map<String, Object> p,
      final Function<String, ? extends T> function, final AsyncCallback<T> callback) {
    // Make a new parameters Map so we can add the POLLING parameter below.
    final Map<String, Object> parameters = new HashMap<String, Object>();
    if (p != null) {
      parameters.putAll(p);
    }
    final String initialRequestId = getNextRequestId();

    AsyncCallback<String> pollingCallback = new AsyncCallback<String>() {
      @Override
      public void onSuccess(String response) {
        if (JsonpConstants.NOT_FINISHED_YET.equals(response)) {
          // No response is available yet, create a timer to try again in a second.
          parameters.put(JsonpConstants.POLLING, initialRequestId);

          final AsyncCallback<String> pollingCallback = this;
          Timer timer = new Timer() {
            @Override
            public void run() {
              sendJsonpRequest(request, parameters, stringResponseDecoder, pollingCallback);
            }
          };
          timer.schedule(1000); // schedule the timer
        } else {
          // The response is available, convert it to the correct type T.
          T result;
          try {
            result = function.apply(response);
          } catch (RuntimeException e) {
            callback.onFailure(e);
            return;
          }
          callback.onSuccess(result);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }
    };

    sendJsonpRequest(initialRequestId, request, parameters, stringResponseDecoder, pollingCallback);
  }

  /**
   * Returns the URI for a JSONP request.
   *
   * @param request the request to be made
   * @param parameters the parameters for the request
   * @param id the id for the request
   */
  @VisibleForTesting
  public String makeURI(String request, Map<String, Object> parameters, String id) {
    StringBuilder sb = new StringBuilder();
    sb.append("http://127.0.0.1:").append(connInfo.getPort()).append("/").append(request);
    sb.append("?").append(JsonpConstants.OUTPUT).append("=").append(
        JsonpConstants.REQUIRED_OUTPUT_VALUE);
    sb.append("&").append(JsonpConstants.CALLBACK).append("=").append(
        JsonpConstants.REQUIRED_CALLBACK_VALUE);
    sb.append("&").append(JsonpConstants.ID).append("=").append(
        escapeQueryParameterFunction.apply(id));
    sb.append("&").append(JsonpConstants.SECRET).append("=").append(connInfo.getSecret());
    if (parameters != null) {
      for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
        sb.append("&").append(parameter.getKey()).append("=").append(
            escapeQueryParameterFunction.apply(parameter.getValue().toString()));
      }
    }
    return sb.toString();
  }

  /**
   * Define the jsonpcb method that is called from javascript for all JSONP
   * requests that we make.
   */
  public static native void defineBridgeMethod() /*-{
    $wnd.jsonpcb = function(id, success, s) {
      if (s != null) {
        s = decodeURIComponent(s);
      }
      @com.google.appinventor.client.jsonp.JsonpConnection::jsonpcb(Ljava/lang/String;ZLjava/lang/String;)
      (id, success, s);
    }
  }-*/;

  /**
   * Called from the native jsonpcb method defined above, for all JSONP
   * requests that we make.
   *
   * @param id the id of the script tag that made the JSONP request
   * @param success whether the request was successful or not
   * @param response the response produced by the JSONP request if successful,
   * or the exception message if not successful
   */
  public static void jsonpcb(String id, boolean success, String response) {
    // Remove the script tag from the document.
    removeScriptTag(id);

    // Remove the callback from the CALLBACKS map.
    AsyncCallback<String> callback = CALLBACKS.remove(id);

    // Call the callback's onSuccess or onFailure method.
    if (callback != null) {
      if (success) {
        callback.onSuccess(response);
      } else {
        callback.onFailure(new RuntimeException(response));
      }
    }
  }

  /**
   * Removes the script tag with the given id from the document.
   */
  private static void removeScriptTag(String id) {
    Document document = Document.get();
    Element element = document.getElementById(id);
    if (element != null) {
      document.getElementsByTagName("body").getItem(0).removeChild(element);
    }
  }

  /**
   * Removes the JSONP request with the given id.
   */
  public void removeJsonpRequest(String id) {
    removeScriptTag(id);
    CALLBACKS.remove(id);
  }

  /**
   * Sends a JSONP request to the HTTP server telling it to quit.
   */
  public void quit() {
    sendJsonpRequest(JsonpConstants.QUIT, null, null, null);
  }

  /**
   * Registers the given @{link ConnectivityListener}.
   */
  public void addConnectivityListener(ConnectivityListener listener) {
    connectivityListeners.add(listener);
    if (connectivityListeners.size() == 1) {
      createConnectivityTimers();
      contactTimer.schedule(CONTACT_REQUEST_TIMER_DELAY);
    }
  }

  /**
   * Unregisters the given @{link ConnectivityListener}.
   */
  public void removeConnectivityListener(ConnectivityListener listener) {
    connectivityListeners.remove(listener);
    if (connectivityListeners.size() == 0) {
      contactTimer.cancel();
    }
  }

  private void createConnectivityTimers() {
    // We create these GWT timers lazily.
    // We must not create them during a java test because they will cause test failures.

    if (contactTimer == null) {
      // Create (but don't schedule) a timer to send a contact request.
      contactTimer = new Timer() {
        @Override
        public void run() {
          sendContactRequest();
        }
      };
    }
    if (unresponsiveConnectionTimer == null) {
      unresponsiveConnectionTimer = new Timer() {
        @Override
        public void run() {
          handleUnresponsiveConnection();
        }
      };
    }
  }

  private void sendContactRequest() {
    contactRequestTime = System.currentTimeMillis();
    unresponsiveConnectionTimer.schedule(unresponsiveTimeLimit);
    sendJsonpRequest(JsonpConstants.CONTACT, null, voidResponseDecoder, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void response) {
        receivedContactResponse();
      }

      @Override
      public void onFailure(Throwable caught) {
        // We got a response, even if it is a failure response.
        receivedContactResponse();
      }
    });
  }

  private void receivedContactResponse() {
    int elapsedTime = (int) (System.currentTimeMillis() - contactRequestTime);

    contactRequestTime = 0;
    unresponsiveConnectionTimer.cancel();

    setConnectivityStatus(true);

    // Adjust the unresponsiveTimeLimit.
    unresponsiveTimeLimit = Math.max(MINIMUM_UNRESPONSIVE_TIME_LIMIT, elapsedTime);

    if (connectivityListeners.size() >= 1) {
      contactTimer.schedule(CONTACT_REQUEST_TIMER_DELAY);
    }
  }

  private void handleUnresponsiveConnection() {
    // Just in case the unresponsiveConnectionTimer still goes off *after* we've canceled it, check
    // that contactRequestTime is not 0 before calling setConnectivityStatus.
    if (contactRequestTime != 0) {
      setConnectivityStatus(false);
    }
  }

  private void setConnectivityStatus(boolean newConnectivityStatus) {
    // This method may be called repeatedly with true (or false). We only need to do something when
    // the connectivity status changes.
    if (connectivityStatus != newConnectivityStatus) {
      connectivityStatus = newConnectivityStatus;
      fireConnectivityStatusChange();
    }
  }

  private void fireConnectivityStatusChange() {
    // Since listeners may choose to remove themselves during their callback, we use a copy in the
    // for loop here.
    List<ConnectivityListener> listenersCopy =
        new ArrayList<ConnectivityListener>(connectivityListeners);
    for (ConnectivityListener listener : listenersCopy) {
      listener.onConnectivityStatusChange(this, connectivityStatus);
    }
  }
}
