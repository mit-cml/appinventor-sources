// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import com.google.appinventor.client.utils.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * A GWT wrapper for the JSZip library.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public final class JSZip {
  /**
   * A callback for iterating over the files in a ZIP.
   */
  @JsFunction
  public interface ForEachCallback {
    /**
     * Applies the callback to a file in the ZIP.
     *
     * @param name the name of the file
     * @param zipObject the file object
     */
    void apply(String name, ZipObject zipObject);
  }

  public JSZip() {}

  /**
   * Puts a file into the ZIP file, overwriting any other entries by the same name.
   *
   * @param name the name of the file being added
   * @param data the file content
   * @return the JSZip object for chaining
   */
  @JsMethod(name = "file")
  public native JSZip putFile(String name, Object data);

  public native JSZip file();

  /**
   * Asynchronously generates a ZIP file.
   *
   * @param options the options for generating the ZIP
   * @return a Promise for the ZIP contents
   * @param <T> the type of the ZIP contents, which depends on the {@link GenerateOptions#type}
   *           specified in the {@code options} parameter.
   */
  public native <T> Promise<T> generateAsync(GenerateOptions options);

  /**
   * Asynchronously loads a ZIP file.
   *
   * @param data the data to load
   * @param options the options for loading the ZIP
   * @return a Promise for the loaded ZIP
   */
  public native Promise<JSZip> loadAsync(Object data, LoadOptions options);

  /**
   * Iterates over the files in the ZIP.
   *
   * @param callback the callback to invoke for each file
   */
  public native void forEach(ForEachCallback callback);
}
