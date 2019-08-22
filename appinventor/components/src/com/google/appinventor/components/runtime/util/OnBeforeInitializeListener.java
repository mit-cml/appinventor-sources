package com.google.appinventor.components.runtime.util;

/**
 * This interface contains a callback method onBeforeInitialize()
 * which will be invoked by the Form activity after the app is
 * initialized, but before the Screen Initialize event is dispatched.
 * Components can register for the callback by calling Form.registerForBeforeInitialize().
 */
public interface OnBeforeInitializeListener {
  public void onBeforeInitialize();
}
