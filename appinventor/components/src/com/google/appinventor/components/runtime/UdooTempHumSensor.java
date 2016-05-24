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
import com.google.appinventor.components.runtime.udoo.UdooBackgroundEventFirer;
import org.json.JSONObject;

/**
 * A component that interfaces with sensors connected to UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_TEMPERATURE_HUMIDITY_SENSOR_COMPONENT_VERSION,
    description = "A component that interfaces with temperature/humidity sensors connected to UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udooTemperature.png")
@SimpleObject
public class UdooTempHumSensor extends AndroidNonvisibleComponent
implements UdooConnectedInterface
{
  private UdooConnectionInterface connection = null;
  private final String TAG = "UdooTempHumSensor";
  private final String SENSOR_TYPE_DHT11 = "DHT11";
  private final String SENSOR_TYPE_DHT22 = "DHT22";

  public UdooTempHumSensor(Form form) {
    super(form);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_ARDUINO_CONNECTION,
    defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void UdooArduino(UdooArduino udooArduino) {
    this.connection = udooArduino.getTransport();
    this.connection.registerComponent(this, form);
  }
  
  private String sensor = SENSOR_TYPE_DHT11;

 /**
  * @param sensor 
  */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_TEMP_HUM_SENSORS,
      defaultValue = SENSOR_TYPE_DHT11)
  @SimpleProperty(
      description = "Select the temperature/humidity sensor connected to your board.",
      userVisible = false)
  public void Sensor(String sensor) {
    this.sensor = sensor;
  }

  @SimpleFunction
  public void ReadSensor(String pin)
  {
    if (this.isConnected()) {
      try {
        JSONObject response = getTransport().arduino().sensor(pin, this.sensor);
        this.DataReady(response.getDouble("temperature"), response.getDouble("humidity"));
      } catch (Exception ex) {
        Log.d(TAG, "Invalid JSON");
      }
    }
  }
  
  @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
  public void Connected()
  {
    UdooBackgroundEventFirer ef = new UdooBackgroundEventFirer();
    ef.setEventName("Connected");
    ef.execute(this);
  }
  
  @SimpleEvent(description = "Fires when the Arduino returns the temperature and humidity.")
  public void DataReady(double temperature, double humidity)
  {
    EventDispatcher.dispatchEvent(this, "DataReady", temperature, humidity);
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
