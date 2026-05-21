// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;

import android.content.Intent;
import android.net.Uri;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.FileAction;
import com.google.appinventor.components.common.FileType;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * The <code>FilePicker</code> component is a button-like component that when clicked
 * by the user will prompt them to select a file from the system. The picker
 * can also be programmatically opened by calling its
 * <a href="/reference/components/media.html#FilePicker.Open" target="_blank">Open</a> method.
 * Using the FilePicker requires Android 4.4 or higher or iOS 11 or higher.
 *
 * @author ewpatton@mit.edu
 */
@DesignerComponent(
    version = YaVersion.FILEPICKER_COMPONENT_VERSION,
    iconName = "images/filepicker.png",
    category = ComponentCategory.MEDIA,
    androidMinSdk = 19
)
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public class FilePicker extends Picker {
  private FileAction action = FileAction.PickExistingFile;
  private String selection = "";
  private String mimeType = "*/*";

  public FilePicker(ComponentContainer container) {
    super(container);
  }

  /**
   * Sets the desired action for the FilePicker. One of:
   *
   *     - Pick Existing File: Open an existing file
   *     - Pick Directory: Open an existing directory
   *     - Pick New File: Create a new file for saving
   *
   * @param action the desired action
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES,
      defaultValue = "Pick Existing File",
      editorArgs = {"Pick Existing File", "Pick New File", "Pick Directory"})
  @SimpleProperty
  public void Action(FileAction action) {
    this.action = action;
  }

  // This version helps Kawa type-cast the designer property value to the FileAction form.
  @SuppressWarnings("checkstyle:MethodName")
  public void Action(String action) {
    Action(FileAction.fromUnderlyingValue(action));
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public FileAction Action() {
    return action;
  }

  /**
   * Sets the desired MIME type for picking a file.
   *
   * @param mimeType a valid MIME type specfiication
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "*/*")
  @SimpleProperty
  public void MimeType(@Options(FileType.class) String mimeType) {
    this.mimeType = mimeType;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String MimeType() {
    return mimeType;
  }

  /**
   * Returns the selected file, possibly as a content URI.
   *
   * @return the selected file
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String Selection() {
    return selection;
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (data != null) {
      Uri uri = data.getData();
      final int takeFlags = data.getFlags() & (
          Intent.FLAG_GRANT_READ_URI_PERMISSION
              | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
          );
      container.$form().getContentResolver().takePersistableUriPermission(uri, takeFlags);
      selection = uri.toString();
      AfterPicking();
    } else {
      container.$form().dispatchErrorOccurredEvent(this, "Open",
          ErrorMessages.ERROR_FILEPICKER_NO_URI_RETURNED);
    }
  }

  @Override
  protected Intent getIntent() {
    Intent intent = new Intent(actionToIntent(action));
    if (action == FileAction.PickExistingFile) {
      intent.addCategory(Intent.CATEGORY_OPENABLE);
    }
    if (action == FileAction.PickDirectory) {
      intent = Intent.createChooser(intent, "Test");
    } else {
      intent.setType(mimeType);
      int flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
          | Intent.FLAG_GRANT_READ_URI_PERMISSION;
      if (action == FileAction.PickExistingFile) {
        flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
      }
      intent.setFlags(flags);
    }
    return intent;
  }

  private static String actionToIntent(FileAction action) {
    switch (action) {
      case PickExistingFile:
        return ACTION_OPEN_DOCUMENT;
      case PickDirectory:
        return ACTION_OPEN_DOCUMENT_TREE;
      case PickNewFile:
        return ACTION_CREATE_DOCUMENT;
      default:
        throw new IllegalArgumentException("Unknown file action: " + action);
    }
  }
}
