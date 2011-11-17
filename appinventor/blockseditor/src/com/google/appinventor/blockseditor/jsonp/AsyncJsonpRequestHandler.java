// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.blockseditor.jsonp;

import com.google.appinventor.common.jsonp.JsonpConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A request handler base class for JSONP requests that must be handled
 * asynchronously.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class AsyncJsonpRequestHandler extends JsonpRequestHandler {
  private class Handler {
    private volatile Thread thread;
    private volatile String responseValue;
    private volatile Throwable exception;

    private Handler() {
    }

    private void startThread(String initialRequestId, final Map<String, String> parameters) {
      String threadName = "AsyncJsonRequestHandler-" + initialRequestId;
      thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            responseValue = getResponseValueAsync(parameters);
          } catch (Throwable e) {
            exception = e;
            if (HttpServer.LOG_TO_SYSTEM_OUT) {
              e.printStackTrace(System.out);
            }
          }
        }
      }, threadName);
      thread.start();
    }

    private boolean isThreadFinished() {
      return !thread.isAlive();
    }

    private String getResponseValue() throws Throwable {
      thread = null; // Don't need the thread anymore.
      // If an exception occurred, rethrow it.
      if (exception != null) {
        throw exception;
      }
      return responseValue;
    }
  }

  private final Map<String, Handler> handlers;

  /**
   * Creates an AsyncJsonpRequestHandler.
   *
   * @param server the server for this handler, used for logging messages
   */
  public AsyncJsonpRequestHandler(HttpServer server) {
    super(server);
    handlers = new ConcurrentHashMap<String, Handler>();
  }

  /**
   * Returns true if this is the initial request; false if this is a
   * subsequent request that is polling for the result of the initial
   * request.
   *
   * @param parameters the request parametere
   */
  private boolean isInitialRequest(Map<String, String> parameters) {
    return !parameters.containsKey(JsonpConstants.POLLING);
  }

  @Override
  public final String getResponseValue(final Map<String, String> parameters) throws Throwable {
    if (isInitialRequest(parameters)) {
      // This is the initial request. Start a new thread to handle the request.
      String initialRequestId = parameters.get(JsonpConstants.ID);
      Handler handler = new Handler();
      handlers.put(initialRequestId, handler);
      handler.startThread(initialRequestId, parameters);
      return JsonpConstants.NOT_FINISHED_YET;
    } else {
      // Polling for the result of the previous request.
      String initialRequestId = parameters.get(JsonpConstants.POLLING);
      Handler handler = handlers.get(initialRequestId);
      if (handler.isThreadFinished()) {
        // The thread has finished. We either have a responseValue or an exception.
        handlers.remove(initialRequestId); // Remove the handler info.
        return handler.getResponseValue();
      } else {
        // The thread hasn't finished yet, we don't have a response value yet.
        return JsonpConstants.NOT_FINISHED_YET;
      }
    }
  }

  /**
   * Returns the response value that will be passed to the javascript
   * callback. This method is called on a new thread.
   *
   * @param parameters the request parametere
   */
  public abstract String getResponseValueAsync(Map<String, String> parameters) throws Throwable;
}
