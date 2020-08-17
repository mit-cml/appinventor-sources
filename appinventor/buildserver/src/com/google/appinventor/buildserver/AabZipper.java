package com.google.appinventor.buildserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AabZipper {
  public static boolean zipBundle(File src, File dest, String root) {
    try {
      FileOutputStream fos = new FileOutputStream(dest);
      ZipOutputStream zipOut = new ZipOutputStream(fos);

      zipFile(src, src.getName(), zipOut, root);
      zipOut.close();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, String root) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    String zipFileName = fileName;
    if (zipFileName.startsWith(root)) {
      zipFileName = zipFileName.substring(root.length());
    }

    boolean windows = !File.separator.equals("/");
    if (windows) {
      zipFileName = zipFileName.replace(File.separator, "/");
    }

    if (fileToZip.isDirectory()) {
      if (zipFileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(zipFileName));
      } else {
        zipOut.putNextEntry(new ZipEntry(zipFileName + "/"));
      }
      zipOut.closeEntry();
      File[] children = fileToZip.listFiles();
      assert children != null;
      for (File childFile : children) {
        zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut, root);
      }
      return;
    }

    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(zipFileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }
}
