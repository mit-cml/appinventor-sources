// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

    private String transport = "local";

    /**
     * Local or remote Arduino
     *
     * @return String
     */
    @SimpleProperty(description = "Connect to a local or remote Arduino")
    public String Transport() {
        return this.transport;
    }

    /**
     * Sets the transport property
     *
     * @param transport
     */
    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_TRANSPORTS,
        defaultValue = "local")
    @SimpleProperty
    public void Transport(String transport) {
        Log.d(TAG, "Transport set");
        if (transport.equals("local") || transport.equals("remote")) {
            this.transport = transport;
            RemoteAddress(this.remoteAddress);
            RemotePort(this.remotePort);
        }
    }
    
    private String remoteAddress;

    /**
     * Sets the remote IP address
     *
     * @param remoteAddress
     */
    @DesignerProperty()
    @SimpleProperty
    public void RemoteAddress(String remoteAddress) {
        Log.d(TAG, "Address set");
        this.remoteAddress = remoteAddress;
        UdooConnectionInterface transport = getTransport();
        if (transport instanceof UdooTcpRedirector) {
            ((UdooTcpRedirector)transport).setAddress(remoteAddress);
        }
    }
    
    private String remotePort;

    /**
     * Sets the remote TCP port number
     *
     * @param remotePort
     */
    @DesignerProperty()
    @SimpleProperty
    public void RemotePort(String remotePort) {
        Log.d(TAG, "Port set");
        this.remotePort = remotePort;
        UdooConnectionInterface transport = getTransport();
        if (transport instanceof UdooTcpRedirector) {
            ((UdooTcpRedirector)transport).setPort(remotePort);
        }
    }
    
    private String remoteSecret;

    /**
     * Sets the remote secret string for authentication
     *
     * @param remoteSecret
     */
    @DesignerProperty()
    @SimpleProperty
    public void RemoteSecret(String remoteSecret) {
        this.remoteSecret = remoteSecret;
    }
    
    public synchronized boolean isConnected()
    {
        boolean isc = getTransport().isConnected();
        if (!isc) {
            Log.d(TAG, "isConnected called, but disconnected!");
            getTransport().disconnect();
            getTransport().connect();
        }
        return isc;
    }

    public UdooArduinoSensor(Form form)
    {
        super(form);
        
        Log.d("UDOOLIFECYCLE", "UdooArduino");
        
        form.registerForOnResume(this);
        form.registerForOnDestroy(this);
        
        getTransport().registerComponent(this);
        getTransport().onCreate(form);
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
        
        getTransport().disconnect();
        getTransport().onDestroy();
    }

    
    

    @SimpleFunction
    public void ReadSensor(int pin)
    {
        if (this.isConnected()) {
            try {
                JSONObject response = getTransport().arduino().sensor(pin, "dht11");
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
    
    private UdooConnectionInterface getTransport()
    {
        if (this.transport.equals("local")) {
            return UdooAdkBroadcastReceiver.getInstance();
        } else {
            return UdooTcpRedirector.getInstance();
        }
    }
}
