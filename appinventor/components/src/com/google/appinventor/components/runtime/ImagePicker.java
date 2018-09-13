// -*- Mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

/**
 * Component enabling a user to select an image from the phone's gallery.
 *
 * @author halabelson@google.com (Hal Abelson)
 * @author nmdofficialhelp@gmail.com (Mika - Next Mobile Development[NMD])
 */
@DesignerComponent(version = YaVersion.IMAGEPICKER_COMPONENT_VERSION,
    description = "A special-purpose button. When the user taps an image picker, the " +
          "device's image gallery appears, and the user can choose an image. After an image is " +
          "picked, it is saved, and the <code>Selected</code> " +
          "property will be the name of the file where the image is stored.",
    category = ComponentCategory.MEDIA)

@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE")
@SimpleObject
public class ImagePicker extends Picker implements ActivityResultListener {

  private static final String LOG_TAG = "ImagePicker";
  private Context context;

  // The path to the saved image
  private String selectionSavedImage = "";

  /**
   * Create a new ImagePicker component.
   *
   * @param container the parent container.
   */
  public ImagePicker(ComponentContainer container) {
    super(container);
    context = container.$context();
    Log.d(LOG_TAG, "ImagePicker Created");
  }

  /**
   * Path to the file containing the image that was selected.
   */
  @SimpleProperty(description = "Path to the file containing the image that was selected.",
      category = PropertyCategory.BEHAVIOR)
  public String Selection() {
    return selectionSavedImage;
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
      selectionSavedImage = getImagePath(selectedImage);
      AfterPicking();
    }
  }

  private String getImagePath(Uri uri) {
    try {
      String[] data = {MediaStore.Audio.Media.DATA};
      CursorLoader loader = new CursorLoader(context, uri, data, null, null, null);
      Cursor cursor = loader.loadInBackground();
      int column_index = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA) : 0;
      if (cursor != null) {
        cursor.moveToFirst();
      }
      return cursor != null ? cursor.getString(column_index) : "ERROR";
    } catch (Exception e) {
      return "" + e.getMessage();
    }
  }
}
