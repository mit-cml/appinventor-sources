// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.graphics.SurfaceTexture;
import android.os.Environment;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Camera provides access to the UDOO's camera
 */
@DesignerComponent(version = YaVersion.UDOO_CAMERA_COMPONENT_VERSION,
   description = "A component to take a picture using the UDOO's camera. " +
        "After the picture is taken, the name of the file on the phone " +
        "containing the picture is available as an argument to the " +
        "AfterPicture event. The file name can be used, for example, to set " +
        "the Picture property of an Image component.",
   category = ComponentCategory.UDOO,
   nonVisible = true,
   iconName = "images/udooCamera.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class UdooCamera extends Camera {

  public UdooCamera(ComponentContainer container) {
    super(container);
  }

  /**
   * There is no front camera!
   */
  @Override
  public boolean UseFront() {
    return false;
  }
  
  /**
   * There is no front camera!
   */
  @Override
  public void UseFront(boolean front) {
  }
  
  private boolean useFront = false;
  
  /**
   * Takes a picture without user action, then raises the AfterPicture event.
   */
  @SimpleFunction
  public void SelfPicture() {
    SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    try {
        android.hardware.Camera camera = android.hardware.Camera.open(0);
        camera.setPreviewTexture(surfaceTexture);
        
        camera.startPreview();
        camera.takePicture(null,null,jpegCallback);
    } catch (RuntimeException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
  
  android.hardware.Camera.PictureCallback jpegCallback = new android.hardware.Camera.PictureCallback() {
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) 
    {
      FileOutputStream outStream = null;
      try {
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "/UDOO");
        imagesFolder.mkdirs(); 
        String fileName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".jpg";
        File output = new File(imagesFolder, fileName);
        outStream = new FileOutputStream(output);    
        outStream.write(data);
        outStream.close();
        AfterPicture(output.getAbsolutePath());
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        camera.stopPreview();
        camera.release();
        camera = null;
      }
    }
  };
}
