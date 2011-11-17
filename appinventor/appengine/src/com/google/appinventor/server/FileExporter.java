// Copyright 2010 Google Inc. All Rights Reserved.

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
   * @return the zip file, which includes a count of the number of zipped files
   *         and (indirectly) the name of the file and its contents
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (no source files)
   * @throws IOException if files cannot be written
   */
  ProjectSourceZip exportProjectSourceZip(String userId, long projectId,
                                          boolean includeProjectHistory,
                                          boolean includeAndroidKeystore, @Nullable String zipName)
      throws IOException;

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
   * Exports a specific file.
   *
   * @param userId the userId
   * @param projectId the project id belonging to the userId
   * @param filePath the full path of the file
   * @return RawFile with the name and content
   * @throws IllegalArgumentException if download request cannot be fulfilled
   *         (file is not known)
   */
  RawFile exportFile(String userId, long projectId, String filePath) throws IOException;
}
