// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class UdooBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "UDOOBroadcastReceiver";
    private static final String ACTION_USB_PERMISSION = "com.google.appinventor.components.runtime.action.USB_PERMISSION";

    private ContextWrapper activity_;
    private UsbManager usbManager_;
    private PendingIntent pendingIntent_;
    private ParcelFileDescriptor fileDescriptor_;
    private FileInputStream inputStream_;
    private FileOutputStream outputStream_;
    public UdooArduinoManager arduino;
    private boolean connected = false;

    public void onCreate(ContextWrapper wrapper)
    {
        activity_ = wrapper;
        usbManager_ = (UsbManager)activity_.getSystemService(Context.USB_SERVICE);
        registerReceiver();
    }

    public void onDestroy()
    {
        unregisterReceiver();
    }

    @Override
    public synchronized void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            pendingIntent_ = null;
            if (intent.getBooleanExtra(usbManager_.EXTRA_PERMISSION_GRANTED, false)) {
                notifyAll();
            } else {
                Log.e(TAG, "Permission denied");
            }
        }
    }

    public synchronized void disconnect()
    {
        this.connected = false;
        notifyAll();

        if (fileDescriptor_ != null) {
            try {
                fileDescriptor_.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close file descriptor.", e);
            }
            fileDescriptor_ = null;
        }

        if (pendingIntent_ != null) {
            pendingIntent_.cancel();
            pendingIntent_ = null;
        }
    }
    
    public synchronized void connect()
    {
        this.connected = false;
        tryOpen();
        if (this.connected) {
            arduino.hi();
        }
    }

    public boolean tryOpen()
    {
        UsbAccessory[] accessories = usbManager_.getAccessoryList();
        UsbAccessory accessory = null;
        for (UsbAccessory iaccessory : accessories) {
            if (iaccessory != null && iaccessory.getManufacturer().equals("UDOO")) {
                accessory = iaccessory;
            }
        }
        
        if (accessory == null) {
            Log.v(TAG, "No accessory found.");
            return false;
        }

        if (!usbManager_.hasPermission(accessory)) {
            if (pendingIntent_ == null) {
                Log.v(TAG, "Requesting permission.");
                pendingIntent_ = PendingIntent.getBroadcast(activity_, 0, new Intent(
                        ACTION_USB_PERMISSION), 0);
                usbManager_.requestPermission(accessory, pendingIntent_);
            }
            return false;
        }

        boolean success = false;

        try {
            fileDescriptor_ = usbManager_.openAccessory(accessory);
            if (fileDescriptor_ == null) {
                Log.v(TAG, "Failed to open file descriptor.");
                return false;
            }

            try {
                FileDescriptor fd = fileDescriptor_.getFileDescriptor();
                inputStream_ = new FileInputStream(fd);
                outputStream_ = new FileOutputStream(fd);
                outputStream_.flush();
//
//                // We're going to block now. We're counting on the IOIO to
//                // write back a byte, or otherwise we're locked until
//                // physical disconnection. This is a known OpenAccessory
//                // bug:
//                // http://code.google.com/p/android/issues/detail?id=20545
//                while (inputStream_.read() != 1) {
//                    trySleep(1000);
//                }
                
                Log.d(TAG, "CONNECTED!!!");
                this.arduino = new UdooArduinoManager(outputStream_, inputStream_, this);
                this.connected = true;

                success = true;
                return true;
            } catch (IOException e) {
                this.connected = false;
                Log.v(TAG, "Failed to open streams", e);
                return false;
            } finally {
                if (!success) {
                    try {
                        fileDescriptor_.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close file descriptor.", e);
                    }
                    fileDescriptor_ = null;
                }
            }
        } finally {
            if (!success && pendingIntent_ != null) {
                pendingIntent_.cancel();
                pendingIntent_ = null;
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        activity_.registerReceiver(this, filter);
    }

    private void unregisterReceiver() {
        activity_.unregisterReceiver(this);
    }
    
    public boolean isConnected()
    {
        return this.connected;
    }
}
