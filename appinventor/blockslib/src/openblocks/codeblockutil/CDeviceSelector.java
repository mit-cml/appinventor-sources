// Copyright 2011 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Provide a drop-down menu of devices from which the user can select. Devices
 * can be added and removed. When the list of devices is empty, clicking the
 * menu shows "No available devices". A device in the menu can be designated
 * as the "current device", which will cause it to have an icon next to its
 * name in the menu (a green arrow currently),
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class CDeviceSelector extends JMenuBar {
  private static final boolean DEBUG = true;
  
  private Color background = CGraphite.white;
  
  private volatile Map<String, JMenuItem> availableDevices;
  private volatile DeviceSelectedCallback callback = null;
  
  private JMenu menu;
  private JMenuItem noDevicesPlaceholder = new JMenuItem("No available devices");
  
  // Clicking this item does an ADB restart
  private JMenuItem resetConnections = new JMenuItem("Reset connections");
  
  private final ImageIcon connectedIcon;
  private JMenuItem currentDevice;
  
  public interface DeviceSelectedCallback {
    void onDeviceSelected(String device);
  }
  
  private AIDirectory aidir = new AIDirectory();
  
  public CDeviceSelector() {
    super();
    // Button text starts out as "Loading initial project"
    // Then changes to "Connect to Device" when the button is
    // first enabled
    menu = new JMenu("Loading initial project...");
    menu.setLayout(new BorderLayout());
    menu.setBackground(background);
    menu.setFont(new Font("Arial", Font.PLAIN, 13));
    menu.setOpaque(true);
    resetConnections.setEnabled(true);
    resetConnections.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Note(halabelson): I'd like to provide some visual feedback
        // to indicate that the reset is happening, but I can't figure 
        // out how to get Swing to do this.
        aidir.restartADB();
      }
    });
    menu.add(resetConnections);
    noDevicesPlaceholder.setEnabled(false);
    menu.add(noDevicesPlaceholder);
    add(menu);
    availableDevices = new HashMap<String, JMenuItem>();
    menu.setEnabled(false);  // start out disabled
    URL imageURL = CDeviceSelector.class.getResource("images/connected-arrow.png");
    String descriptor = "(connected) ";
    if (imageURL != null) {
      connectedIcon = new ImageIcon(imageURL, descriptor);
    } else {
      connectedIcon = new ImageIcon(descriptor);
    }
  }
  
  public void addCallback(DeviceSelectedCallback callback) {
    this.callback = callback;
  }
  
  public void addDevice(final String device) {
    if (DEBUG) {
      System.out.println("CDeviceSelector: adding device " + device);
    }
    synchronized(availableDevices) {
      if (!availableDevices.containsKey(device)) {
        JMenuItem item = new JMenuItem(device);
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (callback != null) {
              System.out.println("Device selected: " + device);
              callback.onDeviceSelected(device);
            }
          }
        });
        if (availableDevices.isEmpty()) {
          menu.remove(noDevicesPlaceholder);
        }
        availableDevices.put(device, item);
        menu.add(item);
      }
    }
  }
  
  public void removeDevice(String device) {
    if (DEBUG) {
      System.out.println("CDeviceSelector: removing device " + device);
    }
    synchronized(availableDevices) {
      if (availableDevices.containsKey(device)) {
        JMenuItem item = availableDevices.get(device);
        availableDevices.remove(device);
        menu.remove(item);
        if (currentDevice == item) {
          currentDevice = null;
        }
      }
      if (availableDevices.isEmpty()) {
        menu.add(resetConnections);
        menu.add(noDevicesPlaceholder);
      }
    }
  }
  
  public void setCurrentDevice(String device) {
    if (currentDevice != null) {
      currentDevice.setIcon(null);
      currentDevice = null;
    }
    if (device != null) {
      if (availableDevices.containsKey(device)) {
        JMenuItem item = availableDevices.get(device);
        item.setIcon(connectedIcon);
        currentDevice = item;
      }
    }
  }
  
  @Override
 public void setEnabled(boolean enabled) {
    menu.setText("Connect to Device...");
    menu.setEnabled(enabled);
  }

}
  
 
