package java.io;

import java.net.URI;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileWriteOperation;
import com.google.appinventor.components.runtime.util.ScopedFile;

import weblib.FileSystemSimulator;

public class File {
  private String path = "";

  public File(File parent, String name) {
    if (parent.getAbsolutePath().endsWith("/")) {
      path = parent.getAbsolutePath() + name;
    } else {
      path = parent.getAbsolutePath() + "/" + name;
    }
  }

  public File(URI uri) {
    String path = uri.toString();
    if (path.startsWith("file://")) {
      this.path = path.substring(7); // Remove "file://"
    } else if (path.startsWith("file:")) {
      this.path = path.substring(5); // Remove "file:"
    } else {
      this.path = path; // Use the URI as is if it doesn't start with "file://"
    }
  }

  public File(String pathname) {
    path = pathname;
  }

  public String getAbsolutePath() {
    return path;
  }

  public static File createTempFile(String prefix, String suffix, File parent) {
    // This is a placeholder for actual temp file creation logic.
    // In a real implementation, this would create a temporary file and return it.
    return new File(parent, prefix + Math.abs(Math.round(Integer.MAX_VALUE * Math.random())) + suffix);
  }

  public String toString() {
    return path;
  }

  public boolean delete() {
    try {
      if (FileSystemSimulator.getFile(path) == null) {
        // File does not exist
        return false;
      }
      FileSystemSimulator.delete(path);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getPath() {
    return path;
  }

  public boolean mkdirs() {
    return false;
  }

  public File[] listFiles() {
    return new File[]{new File(path)};
  }

  public long lastModified() {
    return 0;
  }

}
