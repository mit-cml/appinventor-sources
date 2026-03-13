package com.google.appinventor.client.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

public abstract class RestService {
  protected static final String REST_BASE = "/rest/v1";
  private static final Map<Class<?>, ResponseParser> RESPONSE_PARSERS = new HashMap<>();
  static {
    RESPONSE_PARSERS.put(Config.class, (json) -> new Config(json));
  }

  public static void head(String url, AsyncCallback<Boolean> callback) {
    sendHeadRequest(url, callback);
  }

  public static <T> void get(String url, Class<T> respClass, AsyncCallback<T> callback) {
    sendObjectRequest("GET", url, null, respClass, callback);
  }

  public static <T> void getList(String url, Class<T> respClass, AsyncCallback<List<T>> callback) {
    sendListRequest("GET", url, null, respClass, callback);
  }

  public static <T> void put(String url, Object reqObj, Class<T> respClass,
      AsyncCallback<T> callback) {
    sendObjectRequest("PUT", url, reqObj, respClass, callback);
  }

  public static <T> void patch(String url, Object reqObj, Class<T> respClass,
      AsyncCallback<T> callback) {
    sendObjectRequest("PATCH", url, reqObj, respClass, callback);
  }

  public static <T> void delete(String url, Class<T> respClass, AsyncCallback<T> callback) {
    sendObjectRequest("DELETE", url, null, respClass, callback);
  }

  private static void sendHeadRequest(String url, AsyncCallback<Boolean> callback) {
    sendHttpRequest("HEAD", url, null,
        new HashSet<>(Arrays.asList(Response.SC_OK, Response.SC_NOT_FOUND)),
        (statusCode, responseText) -> {
          callback.onSuccess(statusCode == Response.SC_OK ? Boolean.TRUE : Boolean.FALSE);
        }, callback);
  }

  private static <T> void sendObjectRequest(String method, String url, Object reqObj,
      Class<T> respClass, AsyncCallback<T> callback) {
    sendHttpRequest(method, url, reqObj, new HashSet<>(Arrays.asList(Response.SC_OK)),
        (statusCode, respText) -> {
          final ResponseParser respParser = RESPONSE_PARSERS.get(respClass);
          final Object respJSON = !respText.isEmpty() ? JSON.parse(respText) : null;
          @SuppressWarnings("unchecked")
          final T respObj = respParser != null ? (T) respParser.parse(respJSON) : (T) respJSON;
          callback.onSuccess(respObj);
        }, callback);
  }

  private static <T> void sendListRequest(String httpMethod, String url, Object reqObj,
      Class<T> respClass, AsyncCallback<List<T>> callback) {
    sendHttpRequest(httpMethod, url, reqObj, new HashSet<>(Arrays.asList(Response.SC_OK)),
        (statusCode, respText) -> {
          final ResponseParser respParser = RESPONSE_PARSERS.get(respClass);
          @SuppressWarnings("unchecked")
          final List<T> respJSONList = Arrays.asList((T[]) JSON.parse(respText));
          @SuppressWarnings("unchecked")
          final List<T> respObjList = respParser != null ? respJSONList.stream()
              .map(respJSON -> (T) respParser.parse(respJSON)).collect(Collectors.toList())
              : respJSONList;
          callback.onSuccess(respObjList);
        }, callback);
  }

  private static <T> void sendHttpRequest(String method, String url, Object reqObj,
      Set<Integer> successStatusCodes, SuccessHandler successHandler, AsyncCallback<T> callback) {
    final String reqJSON = reqObj != null ? JSON.stringify(reqObj) : null;
    final HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder(method, URL.encode(url));
    if (reqJSON != null) {
      httpRequestBuilder.setHeader("Content-Type", "application/json; charset=utf-8");
    }

    try {
      httpRequestBuilder.sendRequest(reqJSON, new RequestCallback() {
        @Override
        public void onResponseReceived(Request req, Response resp) {
          final int statusCode = resp.getStatusCode();
          if (successStatusCodes.contains(statusCode)) {
            successHandler.handleSuccess(statusCode, resp.getText());
          } else {
            callback.onFailure(
                new StatusCodeException(statusCode, resp.getStatusText(), resp.getText()));
          }
        }

        @Override
        public void onError(Request req, Throwable e) {
          callback.onFailure(e);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }


  private static class HttpRequestBuilder extends RequestBuilder {
    private HttpRequestBuilder(String httpMethod, String url) {
      super(httpMethod, url);
    }
  }

  @FunctionalInterface
  private static interface SuccessHandler {
    void handleSuccess(int statusCode, String respText);
  }

  @FunctionalInterface
  private static interface ResponseParser {
    Object parse(Object json);
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  private static class JSON {
    private static native String stringify(Object obj);

    private static native Object parse(String json);
  }
}
