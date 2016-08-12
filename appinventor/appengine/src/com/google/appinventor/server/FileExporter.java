// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * Methods for exporting project files.
 *
 */
public interface FileExporter {

  public static final String REMIX_INFORMATION_FILE_PATH = "youngandroidproject/remix_history";

  /**
   * Exports a project output file.
   *
   * @param userId the userId
   * @param projectId the project id belonging to the userId
   * @param target the output target platform, or null
   * @return RawFile with the name and content of the exported file
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (either no output file or too many output files)
   */
  RawFile exportProjectOutputFile(String userId, long projectId, @Nullable String target)
      throws IOException;

  /**
   * Exports the project source files as a zip.
   *
   * @param userId the userId
   * @param projectId the project id belonging to the userId
   * @param includeProjectHistory indicates whether to include a file
   *        containing the project's history in the zip
   * @param includeAndroidKeystore indicates whether to include the user's android.keystore file
   * @param zipName the desired name for the zip, or null for a name to be generated
   * @param fatalError set to true to cause missing GCS file to throw exception
   * @return the zip file, which includes a count of the number of zipped files
   *         and (indirectly) the name of the file and its contents
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (no source files)
   * @throws IOException if files cannot be written
   */
  ProjectSourceZip exportProjectSourceZip(String userId, long projectId,
    boolean includeProjectHistory,
    boolean includeAndroidKeystore, @Nullable String zipName,
    boolean includeYail,
    boolean includeScreenShots,
    boolean fatalError, boolean forGallery) throws IOException;

  /**
   * Exports all of the user's projects' source files as a zip of zips.
   *
   * @param userId the userId
   * @param zipName the desired name for the zip
   * @return the name, contents, and number of files in the zip
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (no projects)
   * @throws IOException if files cannot be written
   */
  ProjectSourceZip exportAllProjectsSourceZip(String userId, String zipName) throws IOException;

  /**
   * Exports a specific project file.
   *
   * @param userId the userId
   * @param projectId the project id belonging to the userId
   * @param filePath the full path of the file
   * @return RawFile with the name and content
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (file is not known)
   */
  RawFile exportFile(String userId, long projectId, String filePath) throws IOException;

  /**
   * Exports a specific user file.
   *
   * @param userId the userId
   * @param filePath the full path of the file
   * @return RawFile with the name and content
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (file is not known)
   */
  RawFile exportUserFile(String userId, String filePath) throws IOException;
}
