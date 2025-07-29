package edu.mit.appinventor.webemu;

import android.AndroidManifest;
import android.Theme;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.res.Resources;
import com.google.appinventor.components.runtime.ListPickerActivity;
import com.google.appinventor.components.runtime.ReplApplication;
import com.google.appinventor.components.runtime.Form;

/**
 * Entry point for the Web-based App Inventor emulator.
 */
public class WebEmulator extends AndroidManifest {
  static {
    Theme.colorPrimary = "#3F51B5";
  }
  @Override
  public int getIcon() {
    return 0;
  }

  @Override
  public Application getApplication() {
    return new ReplApplication();
  }

  @Override
  public Resources getResources() {
    return null;
  }

  @Override
  public Class<?> getDefaultActivityClass() {
    return Screen1.class;
  }

  @Override
  public Activity getActivity(Class clazz) {
    if (Screen1.class.equals(clazz)) {
      return new Screen1();
    } else if (ListPickerActivity.class.equals(clazz)) {
      return new ListPickerActivity();
    }
    return null;
  }

  @Override
  public Service getService(Class clazz) {
    return null;
  }
}
