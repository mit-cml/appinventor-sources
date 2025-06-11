// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.component.Component;
import com.google.appinventor.shared.rpc.project.UserProject;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Methods for importing project files.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface FileImporter {

  /**
   * Creates the project on the server and imports its content.
   *
   * @param userId the userId
   * @param projectName project name
   * @param uploadedFileStream project archive file
   * @return the UserProject
   * @throws FileImporterException if there is already a project named
   *         projectName or if uploadedFile is not a valid project archive
   * @throws IOException if any file operation fails
   */
  UserProject importProject(String userId, String projectName, InputStream uploadedFileStream)
      throws FileImporterException, IOException;

  /**
   * Creates the project on the server and imports its content. Sets the project
   * history with the provided {@code projectHistory} value.
   *
   * @param userId the userId
   * @param projectName
   * @param uploadedFileStream project archive file
   * @param projectHistory the optionally specially formatted project history
   *        string, or null
   * @return the UserProject
   * @throws FileImporterException if there is an error importing
   * @throws IOException if any file operation fails
   */
  UserProject importProject(String userId, String projectName,
                            InputStream uploadedFileStream, @Nullable String projectHistory)
      throws FileImporterException, IOException;

  /**
   * Adds the file to the project on the server and imports its content.
   *
   * @param userId the userId
   * @param projectId project id
   * @param uploadedFileStream uploaded source file
   * @param fileName uploaded file name
   * @return the modification time for the project
   * @throws FileImporterException if the file is too large
   * @throws IOException if any file operation fails
   */
  long importFile(String userId, long projectId, String fileName, InputStream uploadedFileStream)
      throws FileImporterException, IOException;

  /**
   * Adds the user file on the server and imports its content.
   *
   * @param userId the userId
   * @param fileName user file name
   * @param uploadedFileStream uploaded file
   * @throws IOException if any file operation fails
   */
  void importUserFile(String userId, String fileName, InputStream uploadedFileStream)
      throws IOException;

  /**
   * Returns the names of all the projects belonging to the user.
   *
   * @return The set of project names belonging to the provided {@code userId}.
   */
  Set<String> getProjectNames(final String userId);

  /**
   * importTempFile -- Given an input stream, creates a temporary
   * file from the content and returns its name
   *
   * @param inputstream the files data in an input stream
   */
  String importTempFile(InputStream inStream) throws IOException;

  /**
   * Imports a global asset for a user (e.g., an extension or other file not tied to a specific project).
   *
   * @param userId the user ID
   * @param assetName the name of the asset file
   * @param assetType the MIME type or general category of the asset (e.g., "application/zip", "text/java")
   * @param folder an optional folder for organization
   * @param uploadedStream the input stream of the asset file content
   * @return the ID of the stored global asset data
   * @throws FileImporterException if there is an error during import
   * @throws IOException if any file operation fails
   */
  long importGlobalAsset(String userId, String assetName, String assetType, String folder, InputStream uploadedStream)
      throws FileImporterException, IOException;

}
