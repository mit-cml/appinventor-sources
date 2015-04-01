// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

/**
 * A component that interfaces with GPIOs in UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_GPIO_COMPONENT_VERSION,
    description = "A component that interfaces with GPIOs in UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udoo.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.ACCESS_SUPERUSER")
public class UdooGpio extends AndroidNonvisibleComponent
{
    public UdooGpio(Form form) {
        super(form);
    }

    
    private int pinNumber;

    /**
     * Returns the GPIO pin number
     *
     * @return integer
     */
    @SimpleProperty(description = "GPIO pin number")
    public int PinNumber() {
        return this.pinNumber;
    }

    /**
     * Sets the GPIO pin number
     *
     * @param pinNumber
     */
    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
        defaultValue = "1")
    @SimpleProperty
    public void PinNumber(int pinNumber) {
        this.pinNumber = pinNumber;
    }

    
    private String direction;

    /**
     * Returns the active GPIO direction
     *
     * @return String
     */
    @SimpleProperty(description = "GPIO direction")
    public String Direction() {
        return this.direction;
    }

    /**
     * Sets the GPIO direction
     *
     * @param direction
     */
    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_GPIO_DIRECTIONS,
        defaultValue = "in")
    @SimpleProperty
    public void Direction(String direction) {
        if (direction.equals("in") || direction.equals("out")) {
            this.direction = direction;
            this.sendShellCommand("echo " + direction + " > /sys/class/gpio/gpio" + this.pinNumber + "/direction");
        } else {
            throw new RuntimeException("Invalid direction `" + direction + "` for GPIO #" + this.pinNumber);
        }
    }
    
    
    @SimpleFunction
    public void SetValue(int newValue)
    {
        this.sendShellCommand("echo " + newValue + " > /sys/class/gpio/gpio" + this.pinNumber + "/value");
    }
    
    @SimpleFunction
    public int GetValue()
    {
        try {
            File file = new File("/sys/class/gpio/gpio" + this.pinNumber + "/value");
            InputStream in = new FileInputStream(file);
            byte ba[] = new byte[1];
            int read = in.read(ba, 0, 1);
            String string = new String(ba, 0, read);
            in.close();
            
            if (string.equals("0")) return 0;
            if (string.equals("1")) return 1;
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        throw new RuntimeException("Unknown value on GPIO #" + this.pinNumber);
    }

    private void sendShellCommand(String command)
    {
        DataOutputStream os = null;
        try {
            Process p;
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());  

            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");

            os.flush();           
        } catch (IOException e) {
            Log.w("UdooGpio", "Could not write to shell");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.d("UdooGpio", "Could not close process", e);
                }
            }
        }
    }
}
