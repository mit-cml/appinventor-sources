// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.appinventor.components.runtime.util.NougatUtil;

import java.io.File;

/**
 * Sharing is a non-visible component that enables sharing files and/or messages between your app
 * and other apps installed on a device. The component will display a list of the installed apps
 * that can handle the information provided, and will allow the user to choose one to share the
 * content with, for instance a mail app, a social network app, a texting app, and so on.
 *
 * The file path can be taken directly from other components such as the
 * [`Camera`](media.html#Camera) or the [`ImagePicker`](media.html#ImagePicker), but can also be
 * specified directly to read from storage. The default behaviour is to share files from the private
 * data directory associated with your app. If the file path starts with a slash (`/`), then the file
 * relative to `/` is shared.
 * 
 * Be aware that different devices treat storage differently, so a few things to try if, for
 * instance, you have a file called `arrow.gif` in the folder `Appinventor/assets`, would be:
 *
 * - `"file:///sdcard/Appinventor/assets/arrow.gif"`; or
 * - `"/storage/Appinventor/assets/arrow.gif"`
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
        "ImagePicker, but can also be specified directly to read from storage. The default " +
        "behaviour is to share files from the private data directory associated with your app. " +
        "If the file path starts with a slash (/), the the file relative to / is shared.<br>" +
        "Be aware that different devices treat storage differently, so a few things to try if, " +
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
   * @suppressdoc
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
   * @suppressdoc
   */
  @SimpleFunction(description = "Shares a file through any capable application "
      + "installed on the phone by displaying a list of the available apps and allowing the " +
      "user to choose one from the list. The selected app will open with the file inserted on it.")
  public void ShareFile(String file) {
    ShareFileWithMessage(file, "");
  }

  /**
   * Shares a file along with a message using Android' built-in sharing.
   * @suppressdoc
   */
  @SimpleFunction(description = "Shares both a file and a message through any capable application "
      + "installed on the phone by displaying a list of available apps and allowing the user to " +
      " choose one from the list. The selected app will open with the file and message inserted on it.")
  public void ShareFileWithMessage(String file, String message) {
    if (!file.startsWith("file://")) {
      if (!file.startsWith("/")) {
        // Files specified using a relative path should be resolved based on the form's default
        // file scope.
        file = form.getDefaultPath(file);
      } else {
        file = "file://" + file;
      }
    }

    Uri uri  = Uri.parse(file);
    File imageFile = new File(uri.getPath());
    if (imageFile.isFile()) {
      String fileExtension = file.substring(file.lastIndexOf(".")+1).toLowerCase();
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String type = mime.getMimeTypeFromExtension(fileExtension);
      if (type == null) {
        // Fix for #1701: We don't know what it is, but it's at least a sequence of bytes (we hope)
        type = "application/octet-stream";
      }

      Uri shareableUri = NougatUtil.getPackageUri(form, imageFile);
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, shareableUri);
      shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
