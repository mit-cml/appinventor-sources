package com.google.appinventor.components.runtime;

import android.app.Application;

public class ReplApplication extends Application {
  /**
   * Is the application fully loaded into the runtime? Always true in the web emulator
   */
  public static final boolean installed = true;
}
