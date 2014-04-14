package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

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
        " and will allow the user to choose one to share the content with, for instance a " +
        "mail app, a social network app, a texting app, and so on.",
    category = ComponentCategory.SOCIAL,
    nonVisible = true, iconName = "images/sharing.png")
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

    Context cont = this.form.$context();
    cont.startActivity(Intent.createChooser(shareIntent,  "Send using..."));
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
    Activity act = (Activity) this.form.$context();

    Uri uri  = Uri.parse(file);
    File f = new File(uri.getPath());

    if (f.exists()) {
      String ext = file.substring(file.lastIndexOf(".")+1).toLowerCase();
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String type = mime.getMimeTypeFromExtension(ext);

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
      shareIntent.setType(type);

      if (message.length() > 0) {
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
      }

      act.startActivity(shareIntent);
    }
    else {
      Toast.makeText(act, file + " not found", Toast.LENGTH_SHORT).show();
    }
  }
}
