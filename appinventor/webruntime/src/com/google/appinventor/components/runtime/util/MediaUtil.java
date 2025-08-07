package com.google.appinventor.components.runtime.util;

import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
import java.io.IOException;
import weblib.FileSystemSimulator;

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
    if (mediaPath == null || mediaPath.isEmpty()) {
      return null;
    }
    // TODO(ewpatton): Real implementation
    try {
      if (mediaPath.startsWith("file:") || mediaPath.startsWith("/")) {
        // Non-asset path
        String path;
        if (mediaPath.startsWith("file://")) {
          path = mediaPath.substring(7);
        } else if (mediaPath.startsWith("file:")) {
          path = mediaPath.substring(5);
        } else {
          path = mediaPath;
        }
        byte[] contents = FileSystemSimulator.getFile(path);
        if (contents == null) {
          throw new IOException("File not found: " + path);
        }
        return new BitmapDrawable(toDataUri(contents));
      }
      return new BitmapDrawable(AssetFetcher.getLoadedAsset(mediaPath));
    } catch (Exception e) {
      // If the asset is not loaded, we will try to load it from the file system.
      // This is a temporary workaround until we have a better way to handle assets.
      return null;
    }
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

  private static native String toDataUri(byte[] content) /*-{
    return "data:image/png;base64," + new Uint8Array(new Int8Array(content).buffer).toBase64();
  }-*/;
}
