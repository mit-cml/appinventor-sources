// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ComponentName;

import android.Manifest;

/**
 * Component for scanning a barcode and getting back the resulting string.
 *
 * @author sharon@google.com (Sharon Perl)
 */
@DesignerComponent(version = YaVersion.BARCODESCANNER_COMPONENT_VERSION,
    description = "Component for using the Barcode Scanner to read a barcode",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/barcodeScanner.png")
@SimpleObject
@UsesActivities(activities = {
    @ActivityElement(name = "com.google.zxing.client.android.AppInvCaptureActivity",
                     screenOrientation = "landscape",
                     stateNotNeeded = "true",
                     configChanges = "orientation|keyboardHidden",
                     theme = "@android:style/Theme.NoTitleBar.Fullscreen",
                     windowSoftInputMode = "stateAlwaysHidden")
})
@UsesPermissions(permissionNames = "android.permission.CAMERA")
@UsesLibraries(libraries = "Barcode.jar,core.jar")
public class BarcodeScanner extends AndroidNonvisibleComponent
  implements ActivityResultListener, Component {

  private static final String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
  private static final String LOCAL_SCAN = "com.google.zxing.client.android.AppInvCaptureActivity";
  private static final String SCANNER_RESULT_NAME = "SCAN_RESULT";
  private String result = "";
  private boolean useExternalScanner = true;
  private final ComponentContainer container;
  private boolean havePermission = false; // Do we have CAMERA permission?

  /* Used to identify the call to startActivityForResult. Will be passed back into the
  resultReturned() callback method. */
  private int requestCode;

  /**
   * Creates a Bar Code scanning component.
   *
   * @param container container, component will be placed in
   */
  public BarcodeScanner(ComponentContainer container) {
    super(container.$form());
    this.container = container;
  }

  /**
   * Result property getter method.
   */
  @SimpleProperty(description = "Text result of the previous scan.",
      category = PropertyCategory.BEHAVIOR)
  public String Result() {
    return result;
  }

  /**
   * Begins a barcode scan, using the camera. When the scan is complete, the
   * AfterScan event will be raised.
   */
  @SimpleFunction(description = "Begins a barcode scan, using the camera. When the scan " +
      "is complete, the AfterScan event will be raised.")
  public void DoScan() {
    Intent intent = new Intent(SCAN_INTENT);
    if (!useExternalScanner && (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)) {  // Should we attempt to use an internal scanner?
      // Make sure we have CAMERA permission
      if (!havePermission) {
        container.$form()
          .askPermission(Manifest.permission.CAMERA,
                         new PermissionResultHandler() {
                           @Override
                           public void HandlePermissionResponse(String permission, boolean granted) {
                             if (granted) {
                               BarcodeScanner.this.havePermission = true;
                               DoScan();
                             } else {
                               BarcodeScanner.this
                                 .container.$form()
                                 .dispatchErrorOccurredEvent(BarcodeScanner.this, "BarcodeScanner",
                                                             ErrorMessages.ERROR_NO_CAMERA_PERMISSION, "");
                             }
                           }
                         });
        return;
      }
      String packageName = container.$form().getPackageName();
      intent.setComponent(new ComponentName(packageName, "com.google.zxing.client.android.AppInvCaptureActivity"));
    }
    if (requestCode == 0) {
      requestCode = form.registerForActivityResult(this);
    }
    try {
      container.$context().startActivityForResult(intent, requestCode);
    } catch (ActivityNotFoundException e) {
      e.printStackTrace();
      container.$form().dispatchErrorOccurredEvent(this, "BarcodeScanner",
        ErrorMessages.ERROR_NO_SCANNER_FOUND, "");
    }
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      if (data.hasExtra(SCANNER_RESULT_NAME)) {
        result = data.getStringExtra(SCANNER_RESULT_NAME);
      } else {
        result = "";
      }
      AfterScan(result);
    }
  }

  /**
   * Indicates that the scanner has read a (text) result and provides the result 
   */
  @SimpleEvent
  public void AfterScan(String result) {
    EventDispatcher.dispatchEvent(this, "AfterScan", result);
  }

  /**
   * Gets whether or not you want to use an external scanning program to
   * scan barcodes.
   *
   * @return 'true' or 'false' depending on whether or not you want to use
   *         an external scanning program.
   */

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "If true App Inventor will look for and use an external scanning" +
    " program such as \"Bar Code Scanner.\"")
  public boolean UseExternalScanner() {
    return useExternalScanner;
  }

  /**
   * Set whether or not you wish to use an External Scanning program such as
   * Bar Code Scanner. If false a version of ZXing integrated into App Inventor
   * Will be used.
   *
   * @param useExternalScanner  Set true to use an external scanning program,
   *                            false to use internal copy of ZXing.
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty()
  public void UseExternalScanner(boolean useExternalScanner) {
    this.useExternalScanner = useExternalScanner;
  }

}
