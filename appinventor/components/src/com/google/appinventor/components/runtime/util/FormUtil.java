package com.google.appinventor.components.runtime.util;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FormUtil {
  private static final String LOG_TAG = "FormUtil";

  public static void callInitialize(Object component) throws Throwable {
    Method method;
    try {
      method = component.getClass().getMethod("Initialize", (Class<?>[]) null);
    } catch (SecurityException e) {
      Log.i(LOG_TAG, "Security exception " + e.getMessage());
      return;
    } catch (NoSuchMethodException e) {
      //This is OK.
      return;
    }
    try {
      Log.i(LOG_TAG, "calling Initialize method for Object " + component.toString());
      method.invoke(component, (Object[]) null);
    } catch (InvocationTargetException e){
      Log.i(LOG_TAG, "invoke exception: " + e.getMessage());
      throw e.getTargetException();
    }
  }

  public static String getPackageName(Object component) {
    return component.getClass().getPackage().getName();
  }
}
