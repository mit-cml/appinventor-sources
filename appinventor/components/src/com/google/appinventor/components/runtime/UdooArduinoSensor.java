// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A component that interfaces with sensors connected to UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_ARDUINO_SENSOR_COMPONENT_VERSION,
    description = "A component that interfaces with sensors connected to UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udoo.png")
@SimpleObject
public class UdooArduinoSensor extends AndroidNonvisibleComponent
implements OnResumeListener, OnDestroyListener, UdooConnectedInterface
{
    private String TAG = "UDOOUsbActivity";
    private UdooBroadcastReceiver usbReceiver = UdooBroadcastReceiver.getInstance();

    public synchronized boolean isConnected()
    {
        boolean isc = usbReceiver.isConnected();
        if (!isc) {
            Log.d(TAG, "isConnected called, but disconnected!");
            usbReceiver.disconnect();
            usbReceiver.connect();
        }
        return isc;
    }

    public UdooArduinoSensor(Form form)
    {
        super(form);
        
        Log.d("UDOOLIFECYCLE", "UdooArduino");
        
        form.registerForOnResume(this);
        form.registerForOnDestroy(this);
        
        usbReceiver.setComponent(this);
        usbReceiver.onCreate(form);
    }
    
    @Override
    public void onResume()
    {
        Log.d("UDOOLIFECYCLE", "onResume");
        
        this.isConnected(); //connects, if disconnected
    }

    @Override
    public void onDestroy()
    {
        Log.d("UDOOLIFECYCLE", "onDestroy");
        
        usbReceiver.disconnect();
        usbReceiver.onDestroy();
    }

    
    

    @SimpleFunction
    public void ReadSensor(int pin)
    {
        if (this.isConnected()) {
            try {
                JSONObject response = usbReceiver.arduino.sensor(pin, "dht11");
                this.DataReady(response.getInt("temperature"), response.getInt("humidity"));
            } catch (Exception ex) {
                Log.d(TAG, "Invalid JSON");
            }
        }
    }
    
    @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
    public void Connected()
    {
        Log.d(TAG, "Connected EVENT");
        EventDispatcher.dispatchEvent(this, "Connected");
    }
    
    @SimpleEvent(description = "Fires when the Arduino returns the temperature and humidity.")
    public void DataReady(int temperature, int humidity)
    {
        Log.d(TAG, "Data ready");
        
        EventDispatcher.dispatchEvent(this, "DataReady", temperature, humidity);
    }
}
