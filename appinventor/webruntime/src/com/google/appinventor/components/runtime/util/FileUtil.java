package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.errors.PermissionException;
import java.io.FileInputStream;
import java.io.IOException;
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

  public static String resolveFileName(Form form, String fileName, FileScope scope) {
    return fileName;
  }
}
