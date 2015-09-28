// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.udoo.UdooConnectionInterface;
import com.google.appinventor.components.runtime.udoo.UdooConnectedInterface;
import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import org.json.JSONObject;

/**
 * A component that interfaces with sensors connected to UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_COLOR_SENSOR_COMPONENT_VERSION,
    description = "A component that interfaces with color sensors connected to UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udooColor.png")
@SimpleObject
public class UdooColorSensor extends AndroidNonvisibleComponent
implements UdooConnectedInterface
{
  private UdooConnectionInterface connection = null;
  private final String TAG = "UdooColorSensor";
  private final String SENSOR_TYPE_TCS34725 = "TCS34725";

  public UdooColorSensor(Form form) {
    super(form);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_ARDUINO_CONNECTION,
    defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void UdooArduino(UdooArduino udooArduino) {
    this.connection = udooArduino.getTransport();
    this.connection.registerComponent(this, form);
  }
  
  private String sensor = SENSOR_TYPE_TCS34725;

 /**
  * @param sensor 
  */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_COLOR_SENSORS,
      defaultValue = SENSOR_TYPE_TCS34725)
  @SimpleProperty(
      description = "Select the color sensor tipology connected to your board.",
      userVisible = false)
  public void Sensor(String sensor) {
    this.sensor = sensor;
  }

  @SimpleFunction
  public void ReadSensor()
  {
    if (this.isConnected()) {
      try {
        JSONObject response = getTransport().arduino().sensor(this.sensor);
        this.DataReady(response.getInt("red"), response.getInt("green"), response.getInt("blue"));
      } catch (Exception ex) {
        Log.d(TAG, "Invalid JSON");
      }
    }
  }
  
  @SimpleEvent(description = "Fires when the Arduino returns the color.")
  public void DataReady(int red, int green, int blue)
  {
    EventDispatcher.dispatchEvent(this, "DataReady", red, green, blue);
  }
  
  @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
  public void Connected()
  {
    EventDispatcher.dispatchEvent(this, "Connected");
  }
  
  public synchronized boolean isConnected()
  {
    boolean isc = getTransport().isConnected();
    if (!isc) {
      if (!getTransport().isConnecting()) {
        getTransport().reconnect();
      }
    }
    return isc;
  }
  
  private UdooConnectionInterface getTransport()
  {
    return this.connection;
  }
}
