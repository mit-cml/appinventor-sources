// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.PROJECT_DIRECTORY;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;

import com.google.appinventor.buildserver.Project;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProjectUtils {
  private static final Logger LOG = Logger.getLogger(ProjectUtils.class.getName());

  private static final String SEPARATOR = File.separator;
  public static final String PROJECT_PROPERTIES_FILE_NAME = PROJECT_DIRECTORY + SEPARATOR
      + "project.properties";

  private ProjectUtils() {
  }

  /**
   * Creates a new directory beneath the system's temporary directory (as
   * defined by the {@code java.io.tmpdir} system property), and returns its
   * name. The name of the directory will contain the current time (in millis),
   * and a random number.
   *
   * <p>This method assumes that the temporary volume is writable, has free
   * inodes and free blocks, and that it will not be called thousands of times
   * per second.
   *
   * @return the newly-created directory
   * @throws IllegalStateException if the directory could not be created
   */
  public static File createNewTempDir() {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    long random = (long) (Math.random() * Long.MAX_VALUE);
    random = Math.abs(random);
    String baseNamePrefix = System.currentTimeMillis() + "_" + random + "-";

    final int TEMP_DIR_ATTEMPTS = 10000;
    for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
      File tempDir = new File(baseDir, baseNamePrefix + counter);
      if (tempDir.exists()) {
        continue;
      }
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    throw new IllegalStateException("Failed to create directory within "
        + TEMP_DIR_ATTEMPTS + " attempts (tried "
        + baseNamePrefix + "0 to " + baseNamePrefix + (TEMP_DIR_ATTEMPTS - 1) + ')');
  }

  /**
   * Extracts the project represented by inputZip into the destination project root.
   *
   * @param inputZip the ZIP file containing an App Inventor project
   * @param projectRoot the destination directory for the extracted project
   * @return a list of files in the project
   * @throws IOException if the project cannot be extracted
   */
  public static List<String> extractProjectFiles(ZipFile inputZip, File projectRoot)
      throws IOException {
    // Make sure to skip returning any file not in the src/ directory, to avoid corrupted AIAs
    //   containing scm or bky in the assets' directory.
    String sourcePrefix = new File(projectRoot, SRC_FOLDER).getAbsolutePath() + SEPARATOR;

    List<String> projectSourceFileNames = new ArrayList<>();

    Enumeration<? extends ZipEntry> inputZipEnumeration = inputZip.entries();
    while (inputZipEnumeration.hasMoreElements()) {
      ZipEntry zipEntry = inputZipEnumeration.nextElement();
      final InputStream extractedInputStream = inputZip.getInputStream(zipEntry);
      File extractedFile = new File(projectRoot, zipEntry.getName());
      LOG.info("extracting " + extractedFile.getAbsolutePath() + " from input zip");
      Files.createParentDirs(extractedFile); // Do I need this?
      Files.copy(
          new InputSupplier<InputStream>() {
            public InputStream getInput() throws IOException {
              return extractedInputStream;
            }
          },
          extractedFile);

      String extractedFilePath = extractedFile.getPath();
      if (extractedFilePath.startsWith(sourcePrefix)) {
        projectSourceFileNames.add(extractedFile.getPath());
      }
    }

    return projectSourceFileNames;
  }

  /**
   * Loads the project properties file of a Young Android project.
   */
  public static Project getProjectProperties(File projectRoot) {
    return new Project(projectRoot.getAbsolutePath() + SEPARATOR + PROJECT_PROPERTIES_FILE_NAME);
  }
}
