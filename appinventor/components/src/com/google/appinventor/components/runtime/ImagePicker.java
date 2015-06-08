// -*- Mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;

/**
 * Component enabling a user to select an image from the phone's gallery.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.IMAGEPICKER_COMPONENT_VERSION,
    description = "A special-purpose button. When the user taps an image picker, the " +
          "device's image gallery appears, and the user can choose an image. After an image is " +
          "picked,the <code>Selected</code> property will be the path to the file containing the image that was selected",
    category = ComponentCategory.MEDIA)

@SimpleObject
public class ImagePicker extends Picker implements ActivityResultListener {

  private static final String LOG_TAG = "ImagePicker";

  // The path to the selected image
  private String selectedImage = "";

  /**
   * Create a new ImagePicker component.
   *
   * @param container the parent container.
   */
  public ImagePicker(ComponentContainer container) {
    super(container);
  }

  /**
   * Path to the file containing the image that was selected.
   */
  @SimpleProperty(description = "Path to the file containing the image that was selected.",
      category = PropertyCategory.BEHAVIOR)
  public String Selection() {
    return selectedImage;
  }

  @Override
  protected Intent getIntent() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    return intent;
  }

  /**
   * Callback method to get the result returned by the image picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the image's content URI.
   */
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      selectedImage = data.getDataString();
      AfterPicking();
    }
  }


}
