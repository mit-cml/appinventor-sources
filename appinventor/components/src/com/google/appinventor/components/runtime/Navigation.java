// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.util.Log;

import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeoJSONUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureContainer;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.Web;

import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLatitude;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLongitude;

@DesignerComponent(version = YaVersion.NAVIGATION_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Navigation",
    nonVisible = true,
    iconName = "images/navigation.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject
public class Navigation extends AndroidNonvisibleComponent implements Component {

  private static final String TAG = "Navigation";

  public static final String OPEN_ROUTE_SERVICE_URL = "https://api.openrouteservice.org/v2/directions/%s/geojson/";
  private String apiKey;
  private GeoPoint startLocation;
  private GeoPoint endLocation;
  private TransportMethod method;
  private Web web;

  enum TransportMethod {
    DEFAULT ("foot-walking"),
    DRIVING ("driving-car"),
    CYCLING ("cycling-regular"),
    WALKING ("foot-walking"),
    WHEELCHAIR ("wheelchair");

    private final String method;
    
    TransportMethod(String method) {
      this.method = method;
    }
    
    private String method() { 
      return method;
    }
  }

  /**
   * Creates a Navigation component.
   *
   * @param container container, component will be placed in
   */
  public Navigation(ComponentContainer container) {
    super(container.$form());
    apiKey = "";
    startLocation = new GeoPoint(0, 0);
    endLocation = new GeoPoint(0, 0);
    method = TransportMethod.DEFAULT;
  }

  @SimpleFunction(description = "")
  public void RequestDirections() {
    if (apiKey.equals("")) {
      form.dispatchErrorOccurredEvent(this, "Authorization", ErrorMessages.ERROR_INVALID_API_KEY);
    }
    final GeoPoint startLocation = this.startLocation;
    final GeoPoint endLocation = this.endLocation;
    final TransportMethod method = this.method;
    AsynchUtil.runAsynchronously(new Runnable() {
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

  private void performRequest(GeoPoint start, GeoPoint end, TransportMethod method) throws IOException, JSONException {
    final String finalURL = String.format(OPEN_ROUTE_SERVICE_URL, method.method());
    URL url = new URL(finalURL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Authorization", apiKey);
    if (connection != null) {
      try {
        String coords = "{\"coordinates\": " + JsonUtil.getJsonRepresentation(getCoordinates(start, end)) + "}";
        byte[] postData = coords.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(postData.length);
        BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
        try {
          out.write(postData, 0, postData.length);
          out.flush();
        } finally {
          out.close();
        }
        
        final String geoJson = getResponseContent(connection);
        Log.d(TAG, geoJson);
        final List<YailList> geoJsonYail = GeoJSONUtil.getGeoJSONFeatures(TAG, geoJson);
        final YailList coordinates = getLineStringCoords(geoJsonYail.get(0));

        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotDirections(coordinates);
          }
        });
      } catch (Exception e) {
        form.dispatchErrorOccurredEvent(this, "RequestDirections",
              ErrorMessages.ERROR_DEFAULT);
      } finally {
        connection.disconnect();
      }
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

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
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

  @SimpleFunction(description = "Set the start location.")
  public void SetStartLocation(double latitude, double longitude) {
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

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NAVIGATION_METHOD,
      defaultValue = "foot-walking")
  @SimpleProperty
  public void TransportationMethod(String method) {
    for (TransportMethod t : TransportMethod.values()) {
      if (method.equals(t.method())) {
        this.method = t;
      }
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The longitude of the end location.")
  public String TransportationMethod() {
    return method.method();
  }

  @SimpleFunction(description = "Set the end location.")
  public void SetEndLocation(double latitude, double longitude) {
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

  private Double[][] getCoordinates(GeoPoint startLocation, GeoPoint endLocation) {
    Double[][] coords = new Double[2][2];
    coords[0][0] = startLocation.getLongitude();
    coords[0][1] = startLocation.getLatitude();
    coords[1][0] = endLocation.getLongitude();
    coords[1][1] = endLocation.getLatitude();
    return coords;
  }

  private YailList getLineStringCoords(YailList geoJson) {
    for (int i = 0; i < geoJson.size(); i++) {
      if (((YailList) geoJson.getObject(i)).getObject(0).equals("geometry")) {
        YailList geometry = (YailList) ((YailList) geoJson.getObject(i)).getObject(1);
        for (int j = 0; j < geometry.size(); j++) {
          if (((YailList) geometry.getObject(j)).getObject(0).equals("coordinates")) {
            YailList coordinates = (YailList) ((YailList) geometry.getObject(j)).getObject(1);
            return coordinates;
          }
        }
      }
    }
    return YailList.makeEmptyList();
  }

  /**
   * Event indicating that a request has finished and has returned data (directions).
   *
   * @param directions a YailList containing the coordinates of a geojson LineString of the path
   */
  @SimpleEvent(description = "Event triggered when the Openrouteservice returns the directions.")
  public void GotDirections(List<YailList> directions) {
    Log.d(TAG, "GotDirections");
    EventDispatcher.dispatchEvent(this, "GotDirections", directions);
  }
}