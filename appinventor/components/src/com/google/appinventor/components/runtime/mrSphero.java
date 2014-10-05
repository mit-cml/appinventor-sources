// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;



import java.util.List;






import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.*;



/**
 * Multimedia component that plays sounds and optionally vibrates.  A
 * sound is specified via filename.  See also
 * {@link android.media.SoundPool}.
 *
 * @author sharon@google.com (Sharon Perl)
 */
@DesignerComponent(version = 1,
    description = "Component to control Sphero",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = true,
    iconName = "images/soundRecorder.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.BLUETOOTH_ADMIN, android.permission.BLUETOOTH")
@UsesLibraries(libraries = "RobotLibrary.jar")
@UsesNativeLibraries(libraries = "libachievement_manager.so" ,
                     v7aLibraries = "libachievement_manager.so-v7a")
public class mrSphero extends AndroidNonvisibleComponent
    implements Component{//, OnResumeListener, OnStopListener, OnDestroyListener, OnPauseListener, Deleteable {

  private Sphero mRobot;
  
  private boolean connected = false;



  public mrSphero(ComponentContainer container) {
    super(container.$form());
   
    RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {
        @Override
        public void onConnected(Robot robot) {
            mRobot = (Sphero) robot;
            mRobot.setColor(0, 0, 255);
            mRobot.rotate(180);
            mRobot.setBackLEDBrightness(1);
            //ConnectSphero();
            connected=true;
        }
 
        @Override
        public void onConnectionFailed(Robot sphero) {
       
        }

        @Override
        public void onDisconnected(Robot robot) {
           mRobot = null;
           connected=false;
        }
    }); 

    
    RobotProvider.getDefaultProvider().addDiscoveryListener(new DiscoveryListener() {
        @Override
        public void onBluetoothDisabled() {
       
        }

        @Override
        public void discoveryComplete(List<Sphero> spheros) {
        
        }

        @Override
        public void onFound(List<Sphero> sphero) {
          //The robot connects here but then companion app crashes? not sure why? 
          RobotProvider.getDefaultProvider().connect(sphero.iterator().next());
        }
    });

    RobotProvider.getDefaultProvider().startDiscovery(container.$context());
    
  }
  
  
  @SimpleFunction
  public void ColorRed() {
    if  (connected) {
     mRobot.setColor(255, 0, 0);
    }
  }
  
  @SimpleFunction
  public void ColorGreen() {
    if  (connected) {
     mRobot.setColor(0, 255, 0);
    }
  }
  
  @SimpleFunction
  public void ColorBlue() {
    if  (connected) {
     mRobot.setColor(0, 0, 255);
    }
  }
  
  @SimpleProperty
  public String RobotName() {
    if  (connected) {
       return  mRobot.isConnected().toString();
    }else{
      return "";
    }
      
  }
  
  
  @SimpleProperty
  public boolean isConnected() {
    return  connected;
  }
  
  
  
  
  
  //@Override
  //public void onResume() {
   
 // }

  // OnDestroyListener implementation

  //@Override
  //public void onDestroy() {

  //}

  // Deleteable implementation

  //@Override
  //public void onDelete() {
 
  //}

 


  //@Override
  //public void onStop() {
  // TODO Auto-generated method stub
  
  //}


//@Override
//public void onPause() {
  // TODO Auto-generated method stub
  
//}


}