// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.blockseditor.jsonp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility methods commonly used in client-side HTTP servers.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author markf@google.com (Mark Friedman)
 */
public class Util {
  private static final boolean DEBUG = false;

  /*
   * This is a helper class to make it easy to call createTempFile
   * on an entry in a ZipInputStream. At most one ZipEntryInputStream
   * can exist for a given ZipInputStream at a time.
   */
  private static class ZipEntryInputStream extends InputStream {
    private ZipInputStream zipInputStream;

    ZipEntryInputStream(ZipInputStream zipInputStream) {
      this.zipInputStream = zipInputStream;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
      return zipInputStream.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
      byte[] b = new byte[1];
      if (-1 == zipInputStream.read(b, 0, 1)) {
        return -1;
      } else {
        return b[0];
      }
    }

    @Override
    public int available() throws IOException {
      return zipInputStream.available();
    }
  }

  private Util() {
  }

  /**
   * Downloads the file specified by the given url and saves it as a temp file
   * under {@code preferredName}, if possible. If it can't save it under
   * {@code preferredName} it uses the extension from {@code preferredName} with
   * the rest of the name constructed as it pleases.
   *
   * @param url the url for the file to be downloaded
   * @param preferredName
   * @return the full path of the temp file
   */
  public static String downloadFile(String url, String preferredName)
    throws FileNotFoundException, IOException, MalformedURLException {
    URLConnection urlConnection = new URL(url).openConnection();
      return createTempFile(new BufferedInputStream(urlConnection.getInputStream()),
          preferredName);
  }

  /**
   * Downloads the file using the given {@link URLConnection} and saves it to a
   * temp directory as a file with a supplied name unless it does not have
   * permission to do so, in which case it saves it to the temp directory
   * with whatever name it pleases, but with the extension of the supplied
   * name.
   *
   * @param fileName the base name (minus path) for the temp file that we hope
   *    to be able to rename to.
   * @return the full path of the temp file
   */
  public static String downloadFileToGivenNameElseExtension(URLConnection urlConnection,
      String fileName) throws IOException {
    return createTempFile(new BufferedInputStream(urlConnection.getInputStream()),
        fileName);
  }

 /**
   * Downloads a zip file from the server, separates the zip into individual
   * temp files and returns the temp file names. If possible, the
   * temp files will be named the same as they were in the zip file.
   * @param urlConnection
   * @return a list of pairs of file names. The first element of the pair is
   *   the name from the zip file and the second element is the local file names
   * @throws IOException
   */
  public static Map<String,String> downloadZipFile(URLConnection urlConnection)
    throws IOException {
    ZipInputStream zipInput = new ZipInputStream(urlConnection.getInputStream());
    Map<String,String> files = new HashMap<String,String>();
    ZipEntry nextEntry;
    while ((nextEntry = zipInput.getNextEntry()) != null) {
      String name = nextEntry.getName();
      files.put(name, createTempFile(new ZipEntryInputStream(zipInput), name));
      if (DEBUG) {
        System.out.println("Zip file entry named " + name + " stored in local file "
            + files.get(name));
      }
    }
    return files;
  }

  /*
   *  Creates a temp file from the contents of inputStream and returns its name. Tries
   *  to name the file preferredName if possible. preferredName should be a relative
   *  pathname but it may contain subdirectories.
   */
  private static String createTempFile(InputStream inputStream, String preferredName)
    throws IOException {
    String extension = "." + parseFileExtension(preferredName);
    // start by creating a temp file containing the contents of inputStream
    // and a name of File's choosing.
    File tempFile = File.createTempFile("aia", extension);
    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
    copy(inputStream, outputStream);
    inputStream.close();
    outputStream.flush();
    outputStream.close();
    tempFile.deleteOnExit();
    // try to renamed to preferredName
    File tempFileDirectory = tempFile.getParentFile();
    boolean success = true;  // assume the best
    if (preferredName.indexOf('/') != -1) {
      // may need to create subdirectories
      int basenameStart = preferredName.lastIndexOf('/') + 1;
      File newDirs = new File(tempFileDirectory, preferredName.substring(0, basenameStart));
      if (!newDirs.exists()) {
        success = newDirs.mkdirs();
      }
    }
    File renamedTempFile = new File(tempFileDirectory, preferredName);;
    if (success) {  // still good so far. try the rename
      success = tempFile.renameTo(renamedTempFile);
    }
    if (!success) {
      // We don't have permission to rename, so just fall back on tempFile name.
      if (DEBUG) {
        System.out.println("Attempt to rename " + tempFile.getPath() + " to " +
            renamedTempFile.getPath() + " failed");
      }
      return tempFile.getAbsolutePath();
    } else {
      return renamedTempFile.getAbsolutePath();
    }
  }

  private static String parseFileExtension(String path) {
    String extension = "";
    int dotPos = path.lastIndexOf('.');
    if (dotPos >= 0) {
      extension = path.substring(dotPos + 1);
    }
    return extension;
  }

  /**
   * Copies all bytes from the input stream to the output stream.
   * Does not close or flush either stream.
   *
   * @param from the InputStream to read from
   * @param to the OutputStream to write to
   * @return the number of bytes copied
   */
  private static long copy(InputStream from, OutputStream to) throws IOException {
    final int BUF_SIZE = 0x1000; // 4K
    byte[] buf = new byte[BUF_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }
}
