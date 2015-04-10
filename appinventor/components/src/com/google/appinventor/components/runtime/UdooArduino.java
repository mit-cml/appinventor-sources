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


/**
 * A component that interfaces with GPIOs in UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_ARDUINO_COMPONENT_VERSION,
    description = "A component that interfaces with the Arduino CPU in UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udoo.png")
@SimpleObject
public class UdooArduino extends AndroidNonvisibleComponent
implements OnResumeListener, OnDestroyListener
{
    private String TAG = "UDOOUsbActivity";
    private UdooBroadcastReceiver usbReceiver = new UdooBroadcastReceiver();

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

    public UdooArduino(Form form)
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
    public void pinMode(int pin, String mode)
    {
        if (this.isConnected()) {
            usbReceiver.arduino.pinMode(pin, mode);
        }
    }
    
    @SimpleFunction
    public void digitalWrite(int pin, String value)
    {
        if (this.isConnected()) {
            usbReceiver.arduino.digitalWrite(pin, value);
        }
    }
    
    @SimpleFunction
    public int digitalRead(int pin) throws Exception
    {
        if (this.isConnected()) {
            return usbReceiver.arduino.digitalRead(pin);
        }
        
        throw new Exception("Not connected");
    }
    
    @SimpleFunction
    public void analogWrite(int pin, int value)
    {
        if (this.isConnected()) {
            usbReceiver.arduino.analogWrite(pin, value);
        }
    }
    
    @SimpleFunction
    public int analogRead(int pin) throws Exception
    {
        Log.d(TAG, "chiamata analog read");
        if (this.isConnected()) {
            Log.d(TAG, "chiamo metodo");
            return usbReceiver.arduino.analogRead(pin);
        }
        
        Log.d(TAG, "non connesso..");
        
        throw new Exception("Not connected");
    }
    
    @SimpleFunction
    public void delay(int ms) throws Exception
    {
        if (this.isConnected()) {
            usbReceiver.arduino.delay(ms);
        }
    }
    
    @SimpleFunction
    public int map(int value, int fromLow, int fromHigh, int toLow, int toHigh) throws Exception
    {
        if (this.isConnected()) {
            return usbReceiver.arduino.map(value, fromLow, fromHigh, toLow, toHigh);
        }
        
        throw new Exception("Not connected");
    }
    
    @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
    public void Connected()
    {
        Log.d(TAG, "Connected EVENT");
        EventDispatcher.dispatchEvent(this, "Connected");
    }
}
