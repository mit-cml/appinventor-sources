// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;

import com.android.io.StreamException;
import com.android.xml.AndroidManifest;

/**
 * AARLibrary encapsulates important information about Android Archive (AAR) files so that they
 * can be used as part of the App Inventor build process.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class AARLibrary {
  private static final String MANIFEST = "AndroidManifest.xml";
  private static final String CLASSES = "classes.jar";
  private static final String R_TEXT = "R.txt";
  private static final String RES_DIR = "res/";
  private static final String ASSET_DIR = "assets/";
  private static final String LIBS_DIR = "libs/";
  private static final String JNI_DIR = "jni/";

  /**
   * Path to the AAR file modeled by the AARLibrary.
   */
  private final File aarPath;

  /**
   * Name of the simple name for the library based on its file name.
   */
  private final String name;

  /**
   * The package name for the library based on its AndroidManifest.
   */
  private String packageName;

  /**
   * Base directory where the archive is unpacked.
   */
  private File basedir;

  /**
   * Resource directory location after unpacking.
   */
  private File resdir = null;

  // derived from https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/aar-format
  /**
   * Manifest file location after unpacking.
   */
  private File manifest;

  /**
   * Classes.dex file location after unpacking.
   */
  private File classes;

  /**
   * R.txt file location after unpacking.
   */
  private File rtxt;

  /**
   * Set of all descendants of the resources hierarchy.
   */
  private Set<File> resources = new HashSet<>();

  /**
   * Set of all assets found under the assets/ directory.
   */
  private Set<File> assets = new HashSet<>();

  /**
   * Set of all JAR files found under the libs/ directory.
   */
  private Set<File> libs = new HashSet<>();

  /**
   * Set of all dynamically linked libraries found under the jni/ directory.
   */
  private Set<File> jni = new HashSet<>();

  /**
   * File wrapper around a zip stream to allow extracting the package name from the AndroidManifest.
   */
  private static class ZipEntryWrapper extends BaseFileWrapper {
    private final InputStream stream;

    ZipEntryWrapper(InputStream stream) {
      this.stream = stream;
    }

    @Override
    public InputStream getContents() throws StreamException {
      return this.stream;
    }
  }

  /**
   * Constructs a new AARLibrary.
   *
   * @param aar the file representation of the archive (a .aar file).
   */
  public AARLibrary(final File aar) {
    aarPath = aar;
    String temp = aar.getName();
    name = temp.substring(0, temp.length()-4);
  }

  public File getFile() {
    return aarPath;
  }

  public String getSimpleName() {
    return name;
  }

  public String getPackageName() {
    return packageName;
  }

  public File getDirectory() {
    return basedir;
  }

  public File getResDirectory() {
    return resdir;
  }

  public File getManifest() {
    return manifest;
  }

  public File getClassesJar() {
    return classes;
  }

  public File getRTxt() {
    return rtxt;
  }

  public Set<File> getResources() {
    return resources;
  }

  public Set<File> getAssets() {
    return assets;
  }

  public Set<File> getLibraries() {
    return libs;
  }

  public Set<File> getNatives() {
    return jni;
  }

  /**
   * Extracts the package name from the Android Archive without needing to unzip it to a location
   * in the file system
   *
   * @param zip the input stream reading from the Android Archive.
   * @return the package name declared in the archive's AndroidManifest.xml.
   * @throws IOException if reading the input stream fails.
   */
  private String extractPackageName(ZipFile zip) throws IOException {
    ZipEntry entry = zip.getEntry("AndroidManifest.xml");
    if (entry == null) {
      throw new IllegalArgumentException(zip.getName() + " does not contain AndroidManifest.xml");
    }
    try {
      ZipEntryWrapper wrapper = new ZipEntryWrapper(zip.getInputStream(entry));
      // the following call will automatically close the input stream opened above
      return AndroidManifest.getPackage(wrapper);
    } catch(StreamException|XPathExpressionException e) {
      throw new IOException("Exception processing AndroidManifest.xml", e);
    }
  }

  /**
   * Catalogs the file extracted from the Android Archive based on its file name.
   *
   * @param file the file name of an extracted file.
   */
  private void catalog(File file) {
    if (MANIFEST.equals(file.getName())) {
      manifest = file;
    } else if (CLASSES.equals(file.getName())) {
      classes = file;
    } else if (R_TEXT.equals(file.getName())) {
      rtxt = file;
    } else if (file.getPath().startsWith(RES_DIR)) {
      resources.add(file);
    } else if (file.getPath().startsWith(ASSET_DIR)) {
      assets.add(file);
    } else if (file.getPath().startsWith(LIBS_DIR)) {
      libs.add(file);
    } else if (file.getPath().startsWith(JNI_DIR)) {
      jni.add(file);
    }
  }

  /**
   * Unpacks the Android Archive to a directory in the file system. The unpacking operation will
   * create a new directory named with the archive's package name to prevent collisions with
   * other Android Archives.
   * @param path the path to where the archive will be unpacked.
   * @throws IOException if any error occurs attempting to read the archive or write new files to
   *                     the file system.
   */
  public void unpackToDirectory(final File path) throws IOException {
    ZipFile zip = null;
    try {
      zip = new ZipFile(aarPath);
      packageName = extractPackageName(zip);
      basedir = new File(path, packageName);
      if (!basedir.exists() && !basedir.mkdirs()) {
        throw new IOException("Unable to create directory for AAR package: " + basedir);
      }
      InputStream input = null;
      OutputStream output = null;
      Enumeration<? extends ZipEntry> i = zip.entries();
      while (i.hasMoreElements()) {
        ZipEntry entry = i.nextElement();
        File target = new File(basedir, entry.getName());
        if (entry.isDirectory() && !target.exists() && !target.mkdirs()) {
          throw new IOException("Unable to create directory " + path.getAbsolutePath());
        } else if (!entry.isDirectory()) {
          try {
            // Need to make sure the parent directory is present. Files can appear
            // in a ZIP (AAR) file without an explicit directory object
            File parentDir = target.getParentFile();
            if (!parentDir.exists()) {
              parentDir.mkdirs();
            }
            output = new FileOutputStream(target);
            input = zip.getInputStream(entry);
            IOUtils.copy(input, output);
          } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
          }
          catalog(target);
        }
      }
      resdir = new File(basedir, "res");
      if (!resdir.exists()) {
        resdir = null;
      }
    } finally {
      IOUtils.closeQuietly(zip);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null) {
      return false;
    } else if (getClass() != o.getClass()) {
      return false;
    } else {
      return getFile().equals(((AARLibrary) o).getFile());
    }
  }

  @Override
  public int hashCode() {
    return aarPath.hashCode();
  }
}
