// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdooTcpRedirector implements UdooConnectionInterface
{
    private static final String TAG = "UdooTcpRedirector";
    private static UdooTcpRedirector instance = null;
    private Socket socket;
    private String address = null;
    private String port = null;
    private boolean connected = false;
    public UdooArduinoManager arduino;
    List<UdooConnectedInterface> connectedComponents = new ArrayList<UdooConnectedInterface>();
    private boolean waitForResponse = false;

    protected UdooTcpRedirector() {
    }
    public static UdooTcpRedirector getInstance() {
       if (instance == null) {
          instance = new UdooTcpRedirector();
       }
       return instance;
    }
    
    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException ex) {
                Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.connected = false;
    }

    @Override
    public UdooArduinoManager arduino() {
        return this.arduino;
    }

    @Override
    public void connect() {
        if (!this.waitForResponse) {
            Log.d(TAG, "connect() called " + this.address+ ":" + this.port);
            new CreateSocketTask().execute(this.address, this.port);
        }
    }

    @Override
    public void registerComponent(UdooConnectedInterface component) {
        this.connectedComponents.add(component);
    }

    @Override
    public void onCreate(ContextWrapper ctx) {
    }

    @Override
    public void onDestroy() {
        disconnect();
    }

    void setAddress(String address) {
        boolean changed = !address.equals(this.address);
        this.address = address;
        if (this.address != null && this.port != null && changed) {
            connect();
        }
    }
    
    void setPort(String port) {
        boolean changed = !address.equals(this.address);
        this.port = port;
        if (this.address != null && this.port != null && changed) {
            connect();
        }
    }
    
    void setArduino(InputStream in, OutputStream out) {
        this.arduino = new UdooArduinoManager(out, in, this);
        this.connected = true;
    }
    
    
    private class CreateSocketTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... args) {
            try {
                InetAddress serverAddr = InetAddress.getByName(args[0]);
                socket = new Socket(serverAddr, Integer.parseInt(args[1]));


                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                setArduino(in, out);

                Log.d(TAG, "Looks connected");
                waitForResponse = false;

                for (UdooConnectedInterface c : connectedComponents) {
                    Log.d(TAG, "notify " + c.toString());
                    c.Connected();
                }

                Log.d(TAG, "Components notified");

            } catch (UnknownHostException ex) {
                Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        protected void onPostExecute(Void a) {
        }
    }
}
