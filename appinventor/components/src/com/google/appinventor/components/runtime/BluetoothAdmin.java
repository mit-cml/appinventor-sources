/**
Author: Sander Jochems
Version 1
**/

package com.google.appinventor.components.runtime;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@DesignerComponent(
	version = BluetoothAdmin.VERSION,
	description = "BluetoothAdmin component Created by Sander Jochems. Version: " + BluetoothAdmin.VERSION,
	category = ComponentCategory.CONNECTIVITY,
	nonVisible = true,
	iconName = "images/bluetooth.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.BLUETOOTH, android.permission.BLUETOOTH_ADMIN")

public class BluetoothAdmin extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "BluetoothAdmin";
    public static final int VERSION = 1;
    private ComponentContainer container;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
	
    private static final String NO_BLUETOOTH = "Device has no Bluetooth";
	
    private boolean useCodes = false;

    public BluetoothAdmin(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        this.context = container.$context();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(LOG_TAG, "BluetoothAdmin Created");
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Use codes instead of strings in returns for ScanMode and State")
    public boolean UseCodes() {
        return this.useCodes;
    }

    @DesignerProperty(defaultValue = "False", editorType = "boolean")
    @SimpleProperty
    public void UseCodes(boolean useCodes) {
        this.useCodes = useCodes;
    }

    @SimpleFunction(description = "Enable Bluetooth")
    public void Enable() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
        } else {
            mBluetoothAdapter.enable();
        }
    }

    @SimpleFunction(description = "Disable Bluetooth")
    public void Disable() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
        } else {
            mBluetoothAdapter.disable();
        }
    }

    @SimpleFunction(description = "Toggle Bluetooth")
    public void Toggle() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
        } else if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        } else {
            mBluetoothAdapter.enable();
        }
    }

    @SimpleFunction(description = "Returns if the device has Bluetooth")
    public boolean HasBluetooth() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return false;
        }
        return true;
    }

    @SimpleFunction(description = "Returns if the Bluetooth is enabled")
    public boolean IsEnabled() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
            return false;
        } else if (mBluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    @SimpleFunction(description = "Returns the Bluetooth Adapter Name")
    public String AdapterName() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.getName();
        }
        ErrorOccurred(NO_BLUETOOTH);
        return "UNKNOWN";
    }

    @SimpleFunction(description = "Returns the Bluetooth MacAdress")
    public String MacAddress() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.getAddress();
        }
        ErrorOccurred(NO_BLUETOOTH);
        return "UNKNOWN";
    }

    @SimpleFunction(description = "Retruns if the MacAddress is valid")
    public boolean ValidateMacAdress(String MacAdress) {
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            return BluetoothAdapter.checkBluetoothAddress(MacAdress);
        }
        ErrorOccurred(NO_BLUETOOTH);
        return false;
    }

    @SimpleFunction(description = "Retruns if the User MacAddress is valid")
    public boolean ValidateUserMacAdress() {
        if (mBluetoothAdapter != null) {
            return BluetoothAdapter.checkBluetoothAddress(mBluetoothAdapter.getAddress());
        }
        ErrorOccurred(NO_BLUETOOTH);
        return false;
    }

    @SimpleFunction(description = "Returns the state of the Bluetooth Adapter")
    public String State() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
            return "UNKNOWN";
        } else if (mBluetoothAdapter.getState() == 10) {
            if (this.useCodes) {
                return "10";
            }
            return "STATE_OFF";
        } else if (mBluetoothAdapter.getState() == 11) {
            if (this.useCodes) {
                return "11";
            }
            return "STATE_TURNING_ON";
        } else if (mBluetoothAdapter.getState() == 12) {
            if (this.useCodes) {
                return "12";
            }
            return "STATE_ON";
        } else if (mBluetoothAdapter.getState() != 13) {
            return "UNKNOWN";
        } else {
            if (this.useCodes) {
                return "13";
            }
            return "STATE_TURNING_OFF";
        }
    }

    @SimpleFunction(description = "Returns the scan mode of the Bluetooth Adapter")
    public String ScanMode() {
        if (mBluetoothAdapter == null) {
            ErrorOccurred(NO_BLUETOOTH);
            return "UNKNOWN";
        } else if (mBluetoothAdapter.getScanMode() == 20) {
            if (this.useCodes) {
                return "20";
            }
            return "SCAN_MODE_NONE";
        } else if (mBluetoothAdapter.getScanMode() == 21) {
            if (this.useCodes) {
                return "21";
            }
            return "SCAN_MODE_CONNECTABLE";
        } else if (mBluetoothAdapter.getScanMode() != 23) {
            return "UNKNOWN";
        } else {
            if (this.useCodes) {
                return "23";
            }
            return "SCAN_MODE_CONNECTABLE_DISCOVERABLE";
        }
    }

    @SimpleEvent(description = "Event triggers when there is an error")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", message);
    }
}
