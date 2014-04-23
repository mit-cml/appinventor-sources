// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.File;


/**
 * Component for sharing files and/or messages through Android's built-in sharing
 * functionality.
 *
 * @author victsou@gmail.com (Victor Silva) - Picked up on @cfromknecht's work
 * and fixed file support.
 */
@DesignerComponent(version = YaVersion.SHARING_COMPONENT_VERSION,
    description ="Sharing is a non-visible component that enables sharing files and/or " +
        "messages between your app and other apps installed on a device. The component " +
        "will display a list of the installed apps that can handle the information provided, " +
        "and will allow the user to choose one to share the content with, for instance a " +
        "mail app, a social network app, a texting app, and so on.<br>" +
        "The file path can be taken directly from other components such as the Camera or the " +
        "ImagePicker, but can also be specified directly to read from storage. Be aware that " +
        "different devices treat storage differently, so a few things to try if, " +
        "for instance, you have a file called arrow.gif in the folder " +
        "<code>Appinventor/assets</code>, would be: <ul>" +
        "<li><code>\"file:///sdcard/Appinventor/assets/arrow.gif\"</code></li> or " +
        "<li><code>\"/storage/Appinventor/assets/arrow.gif\"</code></li></ul>",
    category = ComponentCategory.SOCIAL,
    nonVisible = true, iconName = "images/sharing.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE")
public class Sharing extends AndroidNonvisibleComponent {

  public Sharing(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Shares a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a message through any capable " +
      "application installed on the phone by displaying a list of the available apps and " +
      "allowing the user to choose one from the list. The selected app will open with the " +
      "message inserted on it.")
  public void ShareMessage(String message) {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, message);
    shareIntent.setType("text/plain");

    // We cannot use Intent.createChooser(shareIntent, "Send using...") because it creates an
    // oversized pop up sharing window.
    this.form.startActivity(shareIntent);
  }

  /**
   * Shares a file using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a file through any capable application "
      + "installed on the phone by displaying a list of the available apps and allowing the " +
      "user to choose one from the list. The selected app will open with the file inserted on it.")
  public void ShareFile(String file) {
    ShareFileWithMessage(file, "");
  }

  /**
   * Shares a file along with a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares both a file and a message through any capable application "
      + "installed on the phone by displaying a list of available apps and allowing the user to " +
      " choose one from the list. The selected app will open with the file and message inserted on it.")
  public void ShareFileWithMessage(String file, String message) {

    if (!file.startsWith("file://"))
      file = "file://" + file;

    Uri uri  = Uri.parse(file);
    File imageFile = new File(uri.getPath());
    if (imageFile.isFile()) {
      String fileExtension = file.substring(file.lastIndexOf(".")+1).toLowerCase();
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String type = mime.getMimeTypeFromExtension(fileExtension);

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
      shareIntent.setType(type);
      if (message.length() > 0) {
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
      }
      // We cannot use Intent.createChooser(shareIntent, "Send using...") because it creates an
      // oversized pop up sharing window.
      this.form.startActivity(shareIntent);
    }
    else {
      String eventName = "ShareFile";
      if (message.equals(""))
        eventName = "ShareFileWithMessage";
      form.dispatchErrorOccurredEvent(Sharing.this, eventName,
          ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, file);
    }
  }
}
