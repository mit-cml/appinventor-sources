// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * Component for using the Barcode Scanner (which must be
 * pre-installed) to scan a barcode and get back the resulting string.
 *
 * @author sharon@google.com (Sharon Perl)
 */
@DesignerComponent(version = YaVersion.BARCODESCANNER_COMPONENT_VERSION,
    description = "Component for using the Barcode Scanner to read a barcode",
    category = ComponentCategory.MISC,
    nonVisible = true,
    iconName = "images/barcodeScanner.png")
@SimpleObject
public class BarcodeScanner extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component {

  private static final String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
  private static final String SCANNER_RESULT_NAME = "SCAN_RESULT";
  private String result = "";
  private final ComponentContainer container;

  /* Used to identify the call to startActivityForResult. Will be passed back into the
  resultReturned() callback method. */
  private int requestCode;

  /**
   * Creates a Phone Call component.
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
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String Result() {
    return result;
  }

  /**
   * Begins a barcode scan, using the camera. When the scan is complete, the
   * AfterScan event will be raised.
   */
  @SimpleFunction
  public void DoScan() {
    Intent intent = new Intent(SCAN_INTENT);
    if (requestCode == 0) {
      requestCode = form.registerForActivityResult(this);
    }
    container.$context().startActivityForResult(intent, requestCode);
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
   * Simple event to raise after the scanner activity has returned
   */
  @SimpleEvent
  public void AfterScan(String result) {
    EventDispatcher.dispatchEvent(this, "AfterScan", result);
  }

}
