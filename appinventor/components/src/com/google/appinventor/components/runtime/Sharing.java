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
    description ="A non-visible component that enables sharing files and/or " +
        "messages through Android's built-in sharing.",
    category = ComponentCategory.SOCIAL,
    nonVisible = true, iconName = "images/sharing.png")
public class Sharing extends AndroidNonvisibleComponent {

  public Sharing(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Shares a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a message through any capable" +
      "application installed on the phone.")
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
      + "installed on the phone.")
  public void ShareFile(String file) {
    ShareFileWithMessage(file, "");
  }

  /**
   * Shares a file along with a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a file through any capable application "
      + "installed on the phone, along with a message.")
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