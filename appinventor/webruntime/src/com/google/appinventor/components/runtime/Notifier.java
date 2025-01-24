package com.google.appinventor.components.runtime;

import android.app.Activity;

public class Notifier extends AndroidNonvisibleComponent {
  /**
   * Creates a new AndroidNonvisibleComponent.
   *
   * @param form the container that this component will be placed in
   */
  public Notifier(Form form) {
    super(form);
  }

  public void ShowAlert(String message) {
    // TODO(ewpatton): Real implementation
    System.out.println(message);
  }

  public void ShowMessageDialog(String message, String title, String buttonText) {
    // TODO(ewpatton): Real implementation
    System.out.println(message);
  }

  public static void twoButtonDialog(Activity activity, String message, String title,
      final String button1Text, final String button2Text, boolean cancelable,
      final Runnable positiveAction, final Runnable negativeAction, final Runnable cancelAction) {
    // TODO(ewpatton): Real implementation
    System.out.println(message);
  }

  public static void oneButtonAlert(Activity activity, String message, String title,
      String buttonText) {
    // TODO(ewpatton): Real implementation
    System.out.println(message);
  }
}
