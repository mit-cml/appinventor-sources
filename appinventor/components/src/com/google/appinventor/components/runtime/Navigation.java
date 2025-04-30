// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLatitude;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLongitude;
import static com.google.appinventor.components.runtime.util.YailDictionary.ALL;
import static java.util.Arrays.asList;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.TransportMethod;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeoJSONUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

/**
 * The Navigation component generates directions between two locations using a service called
 * [OpenRouteService](https://openrouteservice.org). You must provide a valid API key from that
 * service in order for this component to work.
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
@DesignerComponent(version = YaVersion.NAVIGATION_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Navigation",
    nonVisible = true,
    iconName = "images/navigation.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries({"osmdroid.jar"})
@SimpleObject
public class Navigation extends AndroidNonvisibleComponent implements Component {

  private static final String TAG = "Navigation";

  public static final String OPEN_ROUTE_SERVICE_URL =
      "https://api.openrouteservice.org/v2/directions/";
  private String apiKey;
  private GeoPoint startLocation;
  private GeoPoint endLocation;
  private TransportMethod method;
  private String serviceUrl = OPEN_ROUTE_SERVICE_URL;
  private String language = "en";
  private YailDictionary lastResponse = YailDictionary.makeDictionary();

  /**
   * Creates a Navigation component.
   *
   * @param container container, component will be placed in
   */
  public Navigation(ComponentContainer container) {
    super(container.$form());
    apiKey = "";
    startLocation = new GeoPoint(0.0, 0.0);
    endLocation = new GeoPoint(0.0, 0.0);
    method = TransportMethod.Foot;
  }

  /**
   * Request directions from the routing service using the values of {@link #StartLatitude()},
   * {@link #StartLongitude()}, {@link #EndLatitude()}, and {@link #EndLongitude()}. On success,
   * the {@link #GotDirections(YailList, YailList, double, double)} event block will run. If an
   * error occurs, the error will be reported via the
   * [`Screen's ErrorOccurred`](userinterface.html#Screen.ErrorOccurred) event.
   */
  @SimpleFunction(description = "Request directions from the routing service.")
  public void RequestDirections() {
    if (apiKey.equals("")) {
      form.dispatchErrorOccurredEvent(this, "Authorization", ErrorMessages.ERROR_INVALID_API_KEY);
      return;
    }
    final GeoPoint startLocation = this.startLocation;
    final GeoPoint endLocation = this.endLocation;
    final TransportMethod method = this.method;
    AsynchUtil.runAsynchronously(new Runnable() {
      @SuppressWarnings("TryWithIdenticalCatches")
      @Override
      public void run() {
        try {
          performRequest(startLocation, endLocation, method);
        } catch (IOException e) {
          form.dispatchErrorOccurredEvent(Navigation.this, "RequestDirections",
              ErrorMessages.ERROR_DEFAULT);
        } catch (JSONException je) {
          form.dispatchErrorOccurredEvent(Navigation.this, "RequestDirections",
              ErrorMessages.ERROR_DEFAULT);
        }
      }
    });
  }

  /**
   * Reserved for future use in case we decide to run our own service some day.
   */
  @SimpleProperty(userVisible = false)
  public void ServiceURL(String url) {
    this.serviceUrl = url;
  }

  /**
   * API Key for Open Route Service. Obtain an API key at
   * [https://openrouteservice.org](https://openrouteservice.org).
   *
   * @param key the API key to use for authentication requests
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(description = "API Key for Open Route Service.",
      category = PropertyCategory.BEHAVIOR)
  public void ApiKey(String key) {
    apiKey = key;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LATITUDE,
      defaultValue = "0.0")
  @SimpleProperty
  public void StartLatitude(double latitude) {
    if (isValidLatitude(latitude)) {
      startLocation.setLatitude(latitude);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "StartLatitude",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The latitude of the start location.")
  public double StartLatitude() {
    return startLocation.getLatitude();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE,
      defaultValue = "0.0")
  @SimpleProperty
  public void StartLongitude(double longitude) {
    if (isValidLongitude(longitude)) {
      startLocation.setLongitude(longitude);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "StartLongitude",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The longitude of the start location.")
  public double StartLongitude() {
    return startLocation.getLongitude();
  }

  @SimpleProperty(description = "Set the start location.")
  public void StartLocation(MapFeature feature) {
    GeoPoint point = feature.getCentroid();
    double latitude = point.getLatitude();
    double longitude = point.getLongitude();
    if (!isValidLatitude(latitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetStartLocation",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    } else if (!isValidLongitude(longitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetStartLocation",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    } else {
      startLocation.setCoords(latitude, longitude);
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LATITUDE,
      defaultValue = "0.0")
  @SimpleProperty
  public void EndLatitude(double latitude) {
    if (isValidLatitude(latitude)) {
      endLocation.setLatitude(latitude);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "EndLatitude",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The latitude of the end location.")
  public double EndLatitude() {
    return endLocation.getLatitude();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE,
      defaultValue = "0.0")
  @SimpleProperty
  public void EndLongitude(double longitude) {
    if (isValidLongitude(longitude)) {
      endLocation.setLongitude(longitude);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "EndLongitude",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The longitude of the end location.")
  public double EndLongitude() {
    return endLocation.getLongitude();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public @Options(TransportMethod.class) String TransportationMethod() {
    return TransportationMethodAbstract().toUnderlyingValue();
  }

  /**
   * Returns the current transportation method.
   */
  @SuppressWarnings("RegularMethodName")
  public TransportMethod TransportationMethodAbstract() {
    return method;
  }

  /**
   * Sets the current transportation method.
   */
  @SuppressWarnings("RegularMethodName")
  public void TransportationMethodAbstract(TransportMethod method) {
    this.method = method;
  }

  /**
   * The transportation method used for determining the route. Valid options are:
   *
   *  - `foot-walking`: Route based on walking paths
   *  - `driving-car`: Route based on vehicle paths
   *  - `cycling-regular`: Route based on bicycle paths
   *  - `wheelchair`: Route based on wheelchair accessible paths
   *
   * @param method the method to use
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NAVIGATION_METHOD,
      defaultValue = "foot-walking")
  @SimpleProperty(description = "The transportation method used for determining the route.")
  public void TransportationMethod(@Options(TransportMethod.class) String method) {
    TransportMethod t = TransportMethod.fromUnderlyingValue(method);
    if (t != null) {
      TransportationMethodAbstract(t);
    }
  }

  @SimpleProperty(description = "Set the end location.")
  public void EndLocation(MapFeature feature) {
    GeoPoint point = feature.getCentroid();
    double latitude = point.getLatitude();
    double longitude = point.getLongitude();
    if (!isValidLatitude(latitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetEndLocation",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    } else if (!isValidLongitude(longitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetEndLocation",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    } else {
      endLocation.setCoords(latitude, longitude);
    }
  }

  /**
   * The language to use for textual directions. Default is "en" for English.
   *
   * @param language the language to use for generating directions
   */
  @SimpleProperty(description = "The language to use for textual directions.",
      category = PropertyCategory.BEHAVIOR)
  @DesignerProperty(defaultValue = "en")
  public void Language(String language) {
    this.language = language;
  }

  @SimpleProperty
  public String Language() {
    return language;
  }

  /**
   * The raw response from the server. This can be used to access more details beyond what the
   * {@link #GotDirections(YailList, YailList, double, double)} event provides.
   *
   * @return the content of the response
   */
  @SimpleProperty(description = "Content of the last response as a dictionary.")
  public YailDictionary ResponseContent() {
    return lastResponse;
  }

  /**
   * Event indicating that a request has finished and has returned data. The following parameters
   * are provided:
   *
   *  - `directions`: A list of text directions, such as "Turn left at Massachusetts Avenue".
   *  - `points`: A list of (latitude, longitude) points that represent the path to take. This can
   *    be passed to {@link LineString#Points(YailList)} to draw the line on a {@link Map}.
   *  - `distance`: Estimated distance for the route, in meters.
   *  - `duration`: Estimated duration for the route, in seconds.
   *
   * @param directions a list of navigation statements
   * @param points a YailList containing the coordinates of a geojson LineString of the path
   * @param distance the distance of the route, in meters
   * @param duration the estimated duration to travel the route, in seconds
   */
  @SimpleEvent(description = "Event triggered when the Openrouteservice returns the directions.")
  public void GotDirections(YailList directions, YailList points, double distance,
      double duration) {
    Log.d(TAG, "GotDirections");
    EventDispatcher.dispatchEvent(this, "GotDirections", directions, points, distance, duration);
  }

  // MARK: Private implementation

  private void performRequest(GeoPoint start, GeoPoint end, TransportMethod method)
      throws IOException, JSONException {
    final String finalUrl = serviceUrl + method.toUnderlyingValue() + "/geojson";
    URL url = new URL(finalUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Authorization", apiKey);
    try {
      String coords = "{\"coordinates\": "
          + JsonUtil.getJsonRepresentation(getCoordinates(start, end)) + ", \"language\": \""
          + language + "\"}";
      byte[] postData = coords.getBytes("UTF-8");
      connection.setFixedLengthStreamingMode(postData.length);
      BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
      try {
        out.write(postData, 0, postData.length);
        out.flush();
      } finally {
        out.close();
      }

      if (connection.getResponseCode() != 200) {
        form.dispatchErrorOccurredEvent(this, "RequestDirections",
            ErrorMessages.ERROR_ROUTING_SERVICE_ERROR, connection.getResponseCode(),
            connection.getResponseMessage());
        return;
      }
      final String geoJson = getResponseContent(connection);
      Log.d(TAG, geoJson);
      final YailDictionary response = (YailDictionary) JsonUtil.getObjectFromJson(geoJson, true);
      YailList features = (YailList) response.get("features");
      if (features.size() > 0) {
        YailDictionary feature = (YailDictionary) features.getObject(0);
        YailDictionary summary =
            (YailDictionary) feature.getObjectAtKeyPath(asList("properties", "summary"));
        final double distance = (Double) summary.get("distance");
        final double duration = (Double) summary.get("duration");
        final YailList directions = YailList.makeList(getDirections(feature));
        final YailList coordinates = getLineStringCoords(feature);

        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            lastResponse = response;
            GotDirections(directions, coordinates, distance, duration);
          }
        });
      } else {
        // No response
        form.dispatchErrorOccurredEvent(this, "RequestDirections",
            ErrorMessages.ERROR_NO_ROUTE_FOUND);
      }
    } catch (Exception e) {
      form.dispatchErrorOccurredEvent(this, "RequestDirections",
          ErrorMessages.ERROR_UNABLE_TO_REQUEST_DIRECTIONS, e.getMessage());
      e.printStackTrace();
    } finally {
      connection.disconnect();
    }
  }

  private static String getResponseContent(HttpURLConnection connection) throws IOException {
    String encoding = connection.getContentEncoding();
    if (encoding == null) {
      encoding = "UTF-8";
    }
    Log.d(TAG, Integer.toString(connection.getResponseCode()));
    InputStreamReader reader = new InputStreamReader(connection.getInputStream(), encoding);
    try {
      int contentLength = connection.getContentLength();
      StringBuilder sb = (contentLength != -1)
          ? new StringBuilder(contentLength)
          : new StringBuilder();
      char[] buf = new char[1024];
      int read;
      while ((read = reader.read(buf)) != -1) {
        sb.append(buf, 0, read);
      }
      return sb.toString();
    } finally {
      reader.close();
    }
  }

  private Double[][] getCoordinates(GeoPoint startLocation, GeoPoint endLocation) {
    Double[][] coords = new Double[2][2];
    coords[0][0] = startLocation.getLongitude();
    coords[0][1] = startLocation.getLatitude();
    coords[1][0] = endLocation.getLongitude();
    coords[1][1] = endLocation.getLatitude();
    return coords;
  }

  private YailList getLineStringCoords(YailDictionary feature) {
    YailList coords =
        (YailList) feature.getObjectAtKeyPath(asList("geometry", "coordinates"));
    return GeoJSONUtil.swapCoordinates(coords);
  }

  private List<?> getDirections(YailDictionary feature) {
    return YailDictionary.walkKeyPath(feature,
        asList("properties", "segments", ALL, "steps", ALL, "instruction"));
  }
}
