// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Component enabling a user to select an image from the phone's gallery.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
@DesignerComponent(version = YaVersion.IMAGEPICKER_COMPONENT_VERSION,
    description = "<p>A special-purpose button. When the user taps an image picker, the " +
          "device's image gallery appears, and the user can choose an image. After the user " +
          "picks an image, the property <code>ImagePath</code> is set to a name that " +
          "designates the image.</p>",
    category = ComponentCategory.MEDIA)
@SimpleObject
public class ImagePicker extends Picker implements ActivityResultListener {

  private String imagePath;

  /**
   * Create a new ImagePicker component.
   *
   * @param container the parent container.
   */
  public ImagePicker(ComponentContainer container) {
    super(container);
  }

  /**
   * Path for the image that was selected.
   */
  @SimpleProperty(
      // This doesn't fit exactly into any category.
      category = PropertyCategory.BEHAVIOR)
  public String ImagePath() {
    return imagePath;
  }

  @Override
  protected Intent getIntent() {
    return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
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
      Uri selectedImage = data.getData();
      imagePath = selectedImage.toString();
      Log.i("ImagePicker", "Image imagePath = " + imagePath);
      AfterPicking();
    }
  }
}
