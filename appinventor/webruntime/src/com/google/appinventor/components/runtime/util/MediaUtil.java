package com.google.appinventor.components.runtime.util;

import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import android.os.Build;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MediaUtil {

  private enum MediaSource {
    ASSET,
    REPL_ASSET,
    SDCARD,
    FILE_URL,
    URL,
    CONTENT_URI,
    CONTACT_URI,
    PRIVATE_DATA
  }

  public static BitmapDrawable getBitmapDrawable(Form form, String mediaPath)
      throws IOException {
    // TODO(ewpatton): Real implementation
    return null;
  }

  public static int loadSoundPool(SoundPool soundPool, Form form, String mediaPath)
      throws IOException {
    MediaSource mediaSource = determineMediaSource(form, mediaPath);
    switch (mediaSource) {
      case REPL_ASSET:
        if (RUtil.needsFilePermission(form, mediaPath, null)) {
          form.assertPermission("android.permission.READ_EXTERNAL_STORAGE");
        }
        return soundPool.load(fileUrlToFilePath(form.getAssetPath(mediaPath)), 1);
    }

    throw new IOException("Unable to load audio " + mediaPath + ".");
  }

  private static MediaSource determineMediaSource(Form form, String mediaPath) {
    if (false
        || mediaPath.startsWith("/sdcard/")) {
      return MediaSource.SDCARD;

    } else if (mediaPath.startsWith("content://contacts/")) {
      return MediaSource.CONTACT_URI;

    } else if (mediaPath.startsWith("content://")) {
      return MediaSource.CONTENT_URI;
    } else if (mediaPath.startsWith("/data/")) {
      return MediaSource.PRIVATE_DATA;
    }

    /*
    try {
      URL url = new URL(mediaPath);
      // It's a well formed URL.
      if (mediaPath.startsWith("file:")) {
        if (url.getPath().startsWith("/android_asset/")) {
          return MediaSource.ASSET;
        }
        return MediaSource.FILE_URL;
      }

      return MediaSource.URL;

    } catch (MalformedURLException e) {
      // It's not a well formed URL!
    }
     */

    if (form instanceof ReplForm) {
      if (((ReplForm) form).isAssetsLoaded()) {
        return MediaSource.REPL_ASSET;
      } else {
        return MediaSource.ASSET;
      }
    }

    return MediaSource.ASSET;
  }


  static String fileUrlToFilePath(String mediaPath) throws IOException {
    try {
      return mediaPath;
    } catch (IllegalArgumentException e) {
      throw new IOException("Unable to determine file path of file url " + mediaPath);
    } catch (Exception e) {
      throw new IOException("Unable to determine file path of file url " + mediaPath);
    }
  }
}
