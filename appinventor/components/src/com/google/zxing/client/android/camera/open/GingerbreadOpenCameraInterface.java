// -*- mode: java; c-basic-offset: 2; -*-
/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.camera.open;

/* import android.annotation.TargetApi; */
import android.hardware.Camera;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Implementation for Android API 9 (Gingerbread) and later. This opens up the possibility of accessing
 * front cameras, and rotated cameras.
 */
/* @TargetApi(9) */
public final class GingerbreadOpenCameraInterface implements OpenCameraInterface {

  private static final String TAG = "GingerbreadOpenCamera";

  /**
   * Opens a rear-facing camera with {@link Camera#open(int)}, if one exists, or opens camera 0.
   */
  @Override
  public Camera open() {

    // We have to use reflection because we are currently linked against Froyo (2.2).

    try {

      Class AndroidCamera;
      AndroidCamera = Class.forName("android.hardware.Camera");

      int numCameras = ((Integer) invokeStaticMethod(getMethod(AndroidCamera, "getNumberOfCameras"))).intValue();
      if (numCameras == 0) {
        Log.w(TAG, "No cameras!");
        return null;
      }

      int index = 0;
      Class CameraInfo = Class.forName("android.hardware.Camera$CameraInfo");

      Field facingField = CameraInfo.getField("facing");
      while (index < numCameras) {
        Object cameraInfo = CameraInfo.getConstructor().newInstance();
        AndroidCamera.getMethod("getCameraInfo", int.class, CameraInfo).invoke(null, index, cameraInfo);
        if (facingField.getInt(cameraInfo) == CameraInfo.getDeclaredField("CAMERA_FACING_BACK").getInt(CameraInfo))
          break;
        index++;
      }

      if (index < numCameras) {
        Log.i(TAG, "Opening camera #" + index);
      } else {
        Log.i(TAG, "No camera facing back; returning camera #0");
        index = 0;
      }

      Camera camera = (Camera) getMethod(AndroidCamera, "open", int.class).invoke(null, index);
      return camera;

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  // Reflection helper methods

  private static Method getMethod(Class clazz, String name) {
    try {
      return clazz.getMethod(name, new Class[0]);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Method getMethod(Class clazz, String name, Class<?>... parameterTypes) {
    try {
      return clazz.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokeStaticMethod(Method method) {
    try {
      return method.invoke(null);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  private static Object invokeMethod(Method method, Object thisObject, Object... args) {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  private static Object invokeMethodThrowsIllegalArgumentException(Method method,
    Object thisObject, Object... args) throws IllegalArgumentException {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  private static Object invokeMethodThrowsIOException(Method method, Object thisObject,
    Object... args) throws IOException {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

}
