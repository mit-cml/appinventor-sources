// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import java.util.logging.Logger;

/**
 * Helper methods to deal with type names and signatures.
 *
 */
public final class Signatures {

  // Logging support
  private static final Logger LOG = Logger.getLogger(Signatures.class.getName());
  
  private Signatures() {
  }

  /*
   * Extracts the package name from a compound name (assuming the package name to be all but the
   * last component of the compound name).
   */
  private static String getPackageName(String name, char separator) {
    int index = name.lastIndexOf(separator);
    return index < 0 ? "" : name.substring(0, index);
  }

  /*
   * Extracts the class name from a compound name (assuming the class name to be the last component
   * of the compound name).
   */
  private static String getClassName(String name, char separator) {
    int index = name.lastIndexOf(separator);
    return index < 0 ? name : name.substring(index + 1);
  }

  /**
   * Returns the package name part of an dot-qualified class name.
   *
   * @param qualifiedName  qualified class name
   * @return  package name
   */
  public static String getPackageName(String qualifiedName) {
    return getPackageName(qualifiedName, '.');
  }

  /**
   * Returns the class name part of an dot-qualified class name.
   *
   * @param qualifiedName  qualified class name
   * @return  class name
   */
  public static String getClassName(String qualifiedName) {
    return getClassName(qualifiedName, '.');
  }

  /**
   * Returns the package name part of an internal name (according to the Java
   * VM specification).
   *
   * @param internalName  Java VM style internal name
   * @return  package name
   */
  public static String getInternalPackageName(String internalName) {
    return getPackageName(internalName, '/');
  }

  /**
   * Returns the class name part of an internal name (according to the Java
   * VM specification).
   *
   * @param internalName  Java VM style internal name
   * @return  class name
   */
  public static String getInternalClassName(String internalName) {
    return getClassName(internalName, '/');
  }
}
