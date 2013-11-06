// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.jsonp;

import com.google.appinventor.common.jsonp.JsonpConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Extremely simple HTTP server.
 *
 * <p>Listens on a single port. Requests are handled one at a time, in the order
 * they are received. All replies must be content type text.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class HttpServer {
  /**
   * The Producer thread accepts connections and puts them in the connection
   * queue.
   */
  class Producer implements Runnable {
    private final ServerSocket serverSocket;
    private final BlockingQueue<Socket> connectionQueue;

    private Producer(ServerSocket serverSocket, BlockingQueue<Socket> connectionQueue) {
      this.serverSocket = serverSocket;
      this.connectionQueue = connectionQueue;
    }

    @Override
    public void run() {
      while (!shutDown) {
        Socket socket = null;
        try {
          socket = serverSocket.accept();
          socket.setSoTimeout(30 * 1000); // ??? why not 0 (infinite)?
        } catch (SocketException e) {
          // SocketException is thrown if the main thread calls ServerSocket.close().
          // We'll check the shutDown field at the top of while-loop to see if we are finished.
        } catch (IOException e) {
          if (LOG_TO_SYSTEM_OUT) {
            e.printStackTrace(System.out);
          }
        }

        if (socket != null) {
          cancelConnectivityTimerTask();
          setConnectivityStatus(true);

          try {
            connectionQueue.put(socket);
          } catch (InterruptedException e) {
            if (LOG_TO_SYSTEM_OUT) {
              e.printStackTrace(System.out);
            }
          }
        }
      }
    }
  }

  /**
   * The Consumer thread takes requests from the connection queue and handles
   * them.
   */
  class Consumer implements Runnable {
    private final BlockingQueue<Socket> connectionQueue;

    private Consumer(BlockingQueue<Socket> connectionQueue) {
      this.connectionQueue = connectionQueue;
    }

    @Override
    public void run() {
      while (!shutDown) {
        Socket socket = null;
        try {
          socket = connectionQueue.take();
        } catch (InterruptedException e) {
          if (LOG_TO_SYSTEM_OUT) {
            e.printStackTrace(System.out);
          }
        }
        if (socket != null) {
          // Handle the connection, and catch any exceptions that get thrown.
          try {
            processRequest(socket);
          } catch (Throwable e) {
            if (LOG_TO_SYSTEM_OUT) {
              e.printStackTrace(System.out);
            }
          }
          try {
            socket.close();
          } catch (IOException e) {
            if (LOG_TO_SYSTEM_OUT) {
              e.printStackTrace(System.out);
            }
          }
          scheduleConnectivityTimerTask();
        }
      }
    }

    /**
     * Process a request.
     */
    private void processRequest(Socket socket) throws IOException {
      Pair<String, String> protocolAndUri = readHttpRequest(socket);
      String protocol = protocolAndUri.getFirst();
      String uri = protocolAndUri.getSecond();
      log("processRequest: " + protocol + " " + uri + "\n");

      // Fail if the server's secret number has not be set yet.
      if (secret == 0) {
        reply(socket, "ERROR: service is unavailable at this time.<p>\n",
            ResponseCode.SERVICE_UNAV);
        return;
      }

      // Fail if the request is not coming from localhost.
      String addr = socket.getInetAddress().getHostAddress();
      if (!addr.equals("127.0.0.1")) {
        log("    rejecting request from unauthorized client\n");
        reply(socket, "ERROR: " + addr + " unauthorized to send request.<p>\n",
            ResponseCode.UNAUTHORIZED);
        return;
      }

      // Only allow GET requests.
      if (!protocol.equals("GET")) {
        log("    rejecting request of unsupported protocol " + protocol + "\n");
        reply(socket, "ERROR: " + protocol + " not implemented.<p>\n",
            ResponseCode.NOT_IMPL);
        return;
      }

      // Process the URI to get the handler key and the query parameters.
      Pair<String, Map<String, String>> pathAndParameters = parseUri(uri);
      String handlerKey = pathAndParameters.getFirst();
      Map<String, String> parameters = pathAndParameters.getSecond();

      // Validate the request parameters
      if (!validateRequestParameters(parameters, secret)) {
        reply(socket, "ERROR: request is bad.<p>\n", ResponseCode.BAD_REQUEST);
        return;
      }

      HttpRequestHandler handler = getHandlerForKey(handlerKey);
      if (handler != null) {
        reply(socket, handler.handleRequest(parameters), ResponseCode.REQUEST_OK);
      } else {
        reply(socket, "Page not found: " + uri + "\n", ResponseCode.NOT_FOUND);
      }
    }

    private void reply(Socket socket, String body, ResponseCode responseCode) throws IOException {
      byte[] utf8Bytes = body.getBytes("UTF-8");

      // Set headers.
      String outputHeaders = "HTTP/1.0 " + responseCode.getCode() + " "
          + responseCode.getDescription() + "\r\n"
          + "Content-Type: text/plain\r\n"
          + "Content-Length: " + utf8Bytes.length + "\r\n"
          + "Cache-Control: no-cache\r\n\r\n";

      // Write the headers.
      OutputStream outputStream = socket.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
      writer.write(outputHeaders);
      writer.flush();

      // Write the body.
      outputStream.write(utf8Bytes);
    }
  }

  /**
   * A class that represents a Pair of objects.
   *
   * <p>We don't use com.google.common.base.Pair because we don't want any
   * dependencies from outside of ODE. They bloat our deploy jar and they are
   * not compiled with the javac option to target 1.5.
   */
  static class Pair<A, B> {
    final A first;
    final B second;

    Pair(A first, B second) {
      this.first = first;
      this.second = second;
    }

    A getFirst() {
      return first;
    }

    B getSecond() {
      return second;
    }
  }

  private static final long NO_CONNECTIVITY_TIME_MILLIS = 5000;
  private static final long NO_CONNECTIVITY_TIME_NANO = NO_CONNECTIVITY_TIME_MILLIS * 1000000;

  public static final boolean LOG_TO_SYSTEM_OUT = false;

  /**
   * The secret that is required in all JSONP requests.
   */
  private int secret;

  /**
   * Map URI strings to HttpRequestHandler objects.
   */
  private final Map<String, HttpRequestHandler> handlers;

  private volatile boolean shutDown;

  // List of listeners for events.
  private final List<HttpServerEventListener> eventListeners;

  private volatile boolean connectivityStatus;
  private final Timer connectivityTimer;
  private volatile long lastRequestTime;  // in nano seconds
  private volatile TimerTask connectivityTimerTask;

  /**
   * Initialize the server, but don't start it up yet.
   */
  public HttpServer() {
    handlers = new HashMap<String, HttpRequestHandler>();
    eventListeners = new ArrayList<HttpServerEventListener>();
    connectivityStatus = true;
    connectivityTimer = new Timer("HttpServer-ConnectivityTimer");

    // Register some basic handlers.
    setHandler(JsonpConstants.CONTACT, new JsonpRequestHandler(this) {
      @Override
      public String getResponseValue(Map<String, String> parameters) {
        // Nothing to do.
        return null;
      }
    });

    setHandler(JsonpConstants.QUIT, new JsonpRequestHandler(this) {
      @Override
      public String getResponseValue(Map<String, String> parameters) {
        shutDown = true;
        return null;
      }
    });
  }

  /**
   * Sets the secret number that is required in all JSONP requests.
   *
   * @param secret the secret number
   */
  public void setSecret(int secret) {
    this.secret = secret;
  }

  /**
   * Register an HTTP request handler.
   * This will replace any existing handler for the specified request.
   *
   * @param request the request
   * @param handler the HTTP request handler to be registered.
   */
  public void setHandler(String request, HttpRequestHandler handler) {
    String key = getHandlerKey(request);
    synchronized (handlers) {
      handlers.put(key, handler);
    }
  }

  /**
   * Returns the current handler for the specified request, or null if one has
   * not been registered.
   */
  public HttpRequestHandler getHandler(String request) {
    return getHandlerForKey(getHandlerKey(request));
  }

  private HttpRequestHandler getHandlerForKey(String key) {
    synchronized (handlers) {
      return handlers.get(key);
    }
  }

  /**
   * Returns the handler key for the given request.
   */
  private static String getHandlerKey(String request) {
    return "/" + request;
  }

  /**
   * Logs the given message.
   */
  public void log(String message) {
    if (LOG_TO_SYSTEM_OUT) {
      System.out.println(message);
    }
  }

  /**
   * Adds an {@link HttpServerEventListener} to the listener list.
   *
   * @param listener  the listener to be added
   */
  public void addHttpServerEventListener(HttpServerEventListener listener) {
    eventListeners.add(listener);
  }

  /**
   * Removes an {@link HttpServerEventListener} from the listener list.
   *
   * @param listener  the listener to be removed
   */
  public void removeHttpServerEventListener(HttpServerEventListener listener) {
    eventListeners.remove(listener);
  }

  /**
   * Runs the HTTP server. This method doesn't return until the server has
   * been shut down.
   */
  public void runHttpServer() throws IOException {
    // Pick an available port.
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(null);
    int port = serverSocket.getLocalPort();
    firePortSelected(port);

    BlockingQueue<Socket> connectionQueue = new LinkedBlockingQueue<Socket>();
    Thread producerThread = new Thread(new Producer(serverSocket, connectionQueue),
        "HttpServer-Producer");
    Thread consumerThread = new Thread(new Consumer(connectionQueue), "HttpServer-Consumer");

    producerThread.start();
    consumerThread.start();

    // Because the consumerThread will be the one to set the shutDown field, it will be the first
    // to finish.
    try {
      consumerThread.join();
    } catch (InterruptedException e) {
      if (LOG_TO_SYSTEM_OUT) {
        e.printStackTrace(System.out);
      }
    }

    // After the consumerThread finishes, the producerThread will either finish
    // on its own after checking the shutDown field or it will be blocked inside the
    // ServerSocket.accept() method. By closing the server socket here, it will cause the
    // ServerSocket.accept() method to throw a SocketException.
    try {
      serverSocket.close();
    } catch (IOException e) {
      if (LOG_TO_SYSTEM_OUT) {
        e.printStackTrace(System.out);
      }
    }

    // Now we can wait for the producerThread to finish.
    try {
      producerThread.join();
    } catch (InterruptedException e) {
      if (LOG_TO_SYSTEM_OUT) {
        e.printStackTrace(System.out);
      }
    }
  }

  private void cancelConnectivityTimerTask() {
    synchronized (connectivityTimer) {
      if (connectivityTimerTask != null) {
        connectivityTimerTask.cancel();
        connectivityTimerTask = null;
      }
    }
  }

  private void scheduleConnectivityTimerTask() {
    synchronized (connectivityTimer) {
      // Cancel the previous timer task.
      cancelConnectivityTimerTask();

      lastRequestTime = System.nanoTime();

      connectivityTimerTask = new TimerTask() {
        @Override
        public void run() {
          // Make sure that we really have had no requests for a while.
          if (System.nanoTime() - lastRequestTime >= NO_CONNECTIVITY_TIME_NANO) {
            setConnectivityStatus(false);
          }
        }
      };
      connectivityTimer.schedule(connectivityTimerTask, NO_CONNECTIVITY_TIME_MILLIS);
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

  /**
   * Triggers a 'port selected' event to be sent to each listener.
   *
   * @param port the port that the server will listen on for HTTP requests
   */
  private void firePortSelected(int port) {
    for (HttpServerEventListener listener : eventListeners) {
      listener.onPortSelected(this, port);
    }
  }

  /**
   * Triggers a 'connectivity status change' event to be sent to each listener.
   */
  private void fireConnectivityStatusChange() {
    for (HttpServerEventListener listener : eventListeners) {
      listener.onConnectivityStatusChange(this, connectivityStatus);
    }
  }

  /**
   * Reads the HTTP request from the given socket and returns the protocol and
   * URI as a Pair.
   */
  private static Pair<String, String> readHttpRequest(Socket socket) throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream(), "UTF-8"));
    // Find the first non-blank line.
    String line;
    do {
      line = reader.readLine();
      if (line == null) {
        throw new IllegalStateException("readLine returned null");
      }
    } while (line.length() == 0);

    // Get the protocol and URI.
    String[] parts = line.split(" +", 3);
    if (parts.length < 2) {
      throw new IllegalStateException("Cannot find protocol and URI in HTTP request " + line);
    }
    String protocol = parts[0];
    String uri = parts[1];

    return new Pair<String, String>(protocol, uri);
  }

  /**
   * Parses the given URI and returns the path and the query parameters as a
   * Pair.
   */
  private static Pair<String, Map<String, String>> parseUri(String uri) throws IOException {
    String path;
    Map<String, String> parameters = new HashMap<String, String>();
    int questionMark = uri.indexOf('?');
    if (questionMark != -1) {
      path = uri.substring(0, questionMark);
      String query = uri.substring(questionMark + 1);
      String[] items = query.split("&");
      for (String item : items) {
        String[] keyAndValue = item.split("=");
        String key = keyAndValue[0];
        String value = (keyAndValue.length > 1)
            ? URLDecoder.decode(keyAndValue[1], "UTF-8")
            : "";
        parameters.put(key, value);
      }
    } else {
      path = uri;
    }

    return new Pair<String, Map<String, String>>(path, parameters);
  }

  /**
   * Validate the request parameters.
   *
   * @param parameters the request parametere
   * @param serverSecret the secret that the server requires
   * @returns true if the request parameters are valid, false otherwise
   */
  // VisibleForTesting
  static boolean validateRequestParameters(Map<String, String> parameters, int serverSecret) {
    // The request must have a "output" parameter whose value is "json".
    String output = parameters.get(JsonpConstants.OUTPUT);
    if (!JsonpConstants.REQUIRED_OUTPUT_VALUE.equals(output)) {
      return false;
    }

    // The request must have a "callback" parameter whose value is "jsonpcb".
    String callback = parameters.get(JsonpConstants.CALLBACK);
    if (!JsonpConstants.REQUIRED_CALLBACK_VALUE.equals(callback)) {
      return false;
    }

    // The request must have a "id" parameter whose value is not null or empty.
    String id = parameters.get(JsonpConstants.ID);
    if (id == null || id.length() == 0) {
      return false;
    }

    // The request must have a "secret" parameter whose value is a number that matches the server's
    // secret.
    try {
      int secret = Integer.parseInt(parameters.get(JsonpConstants.SECRET));
      if (secret != serverSecret) {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }

    return true;
  }
}
