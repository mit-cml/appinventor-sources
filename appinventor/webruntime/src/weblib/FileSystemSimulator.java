package weblib;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class FileSystemSimulator {
  public static native void storeFile(String filename, byte[] contents);
  public static native byte[] getFile(String filename);
  public static native void delete(String filename);
  public static native void copyFile(String input, String output);
}
