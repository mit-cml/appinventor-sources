package com.google.appinventor.components.runtime.util;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.errors.RuntimeError;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class FileUtil {
  public static FileInputStream openFile(Form form, String fileName) throws IOException,
      PermissionException {
    return null;
  }

  public static FileInputStream openFile(Form form, URI fileUri) throws IOException,
      PermissionException {
    return null;
  }

  /**
   * Gets an external file {@code fileName} on the external storage.
   *
   * <p>The {@code form} is used as a context for asking for permissions and generating an
   * app-specific directory based on the {@code accessMode} provided.
   *
   * @param form The form to use as the context for the operation(s)
   * @param fileName The filename to read/write/append
   * @param scope The permission model to apply to determine the file location
   * @return The File representing the filename on the external storage. The exact location depends
   *     on the value of {@code accessMode}.
   * @throws FileException if the external storage is not writeable
   * @throws PermissionException if the app doesn't have the necessary permissions to write the file
   */
  public static File getExternalFile(Form form, String fileName, FileScope scope)
      throws FileException, PermissionException {
    return new File(URI.create(resolveFileName(form, fileName, scope)));
  }

  /**
   * Given a file name of the form "name", "/name", or "//name", resolves the name to an absolute
   * file URI.
   *
   * @param form the Form object to use as a Context and to ask for permissions, if needed
   * @param fileName the name of the file to be accessed, using the File semantics
   * @param scope permission mode to use for locating the file and asking permissions
   * @return a String of the form "file://..." with the full path to the file based on the
   *     permission mode in effect
   */
  public static String resolveFileName(Form form, String fileName, FileScope scope) {
    File path;
    if (fileName.startsWith("//")) {  // Asset files in legacy mode
      path = new File(form.getAssetPath(fileName.substring(2)).substring(7));
    } else if (fileName.startsWith("content:")) {
      return fileName;
    } else if (scope == FileScope.App && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
      path = new File(form.getExternalFilesDir(""), fileName);
    } else if (scope == FileScope.Asset) {
      path = new File(form.getAssetPath(fileName).substring(7));
    } else if (scope == FileScope.Cache) {
      path = new File(form.getCachePath(fileName).substring(7));
    } else if ((scope == FileScope.Legacy || scope == null) && fileName.startsWith("/")) {
      path = new File(QUtil.getExternalStorageDir(form, false, true), fileName.substring(1));
    } else if (scope == FileScope.Private) {
      path = new File(form.getPrivatePath(fileName).substring(7));
    } else if (scope == FileScope.Shared) {
      path = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
    } else if (!fileName.startsWith("/")) {  // Private files in legacy mode
      path = new File(form.getPrivatePath(fileName).substring(7));
    } else {
      /* Starting with nb186, files will be placed in different locations when the file name starts
       * with a single "/" character. For Android Q and later, this is the app-specific directory
       * on external storage. For Android versions prior to Q, it will be the root of the external
       * storage, such as /sdcard or /storage/external/0/.
       */
      path = getExternalFile(form, fileName.substring(1), scope);
    }
    return Uri.fromFile(path).toString();
  }

  public static boolean needsWritePermission(ScopedFile file) {
    return false;
  }

  public static void copyFile(String file, String file2) {
    
  }


  /**
   * Exception class for reporting back media-related error numbers from
   * ErrorMessages, which the caller can in turn pass to
   * Form.dispatchErrorOccurredEvent if needed.
   */
  public static class FileException extends RuntimeError {
    private final int msgNumber;
    public FileException(int errorMsgNumber) {
      msgNumber = errorMsgNumber;
    }

    public int getErrorMessageNumber() {
      return msgNumber;
    }
  }
}
