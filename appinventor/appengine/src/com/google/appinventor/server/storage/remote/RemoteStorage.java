// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.remote;

/**
 * Interface which abstracts the remote storage access patterns.
 * This allows AI2 to store different files, like build outputs, outside
 *   GCP, and provide a presigned URL to download and/or access them.
 */
public abstract class RemoteStorage {
  // We partition the bucket into different usage types, so specific lifecycle rules could
  //   be applied by prefix, if required.
  private final static String BUILD_OUTPUT_PREFIX = "build";
  private final static String PROJECT_EXPORT_PREFIX = "export";

  /**
   * Generates a, usually, presigned URL to upload the file to the remote
   *   storage server.
   *
   * @param objectKey object key in the remote storage server
   * @return (presigned) upload URL
   */
  public abstract String generateUploadUrl(final String objectKey);

  /**
   * Generates an external URL to access the object. Depending on the
   *   implementation, this may behave as a presigned URL or a "normal"
   *   URL (if using a CDN, for example).
   *
   * @param objectKey object key in the remote storage server
   * @return (presigned) get/retrieval URL
   */
  public abstract String generateRetrieveUrl(final String objectKey);

  /**
   * Generates a constant object key for a given specific project build output.
   *
   * @param target the type of target (Android)
   * @param userId the user ID owning the project
   * @param projectId the given project ID
   * @param projectName the name of the project to store
   * @param extensionName the extension name the build is on
   * @return build/userId/projectId/target/projectName.extensionName
   */
  public final String getBuildOutputObjectKey(final String target, final String userId, final Long projectId,
      final String projectName, final String extensionName) {
    final String fileName = projectName + "." + extensionName;
    return getBuildOutputObjectKey(target, userId, projectId, fileName);
  }

  /**
   * Generates a constant object key for a given specific project build output.
   *
   * @param target the type of target (Android)
   * @param userId the user ID owning the project
   * @param projectId the given project ID
   * @param fileName the file name to download
   * @return build/userId/projectId/target/projectName.extensionName
   */
  public final String getBuildOutputObjectKey(final String target, final String userId, final Long projectId,
      final String fileName) {
    final String filePath = userId + "/" + projectId + "/" + target;
    return BUILD_OUTPUT_PREFIX + "/" + filePath + "/" + fileName;
  }

  /**
   * Generates a constant object key for a given specific project export.
   *
   * @param userId the user ID owning the project
   * @param projectId the given project ID
   * @param projectName the name of the project to store
   * @return export/userId/projectId/projectName.aia
   */
  public final String getProjectExportObjectKey(final String userId, final Long projectId,
      final String projectName) {
    final String filePath = userId + "/" + projectId;
    final String fileName = projectName + ".aia";

    return PROJECT_EXPORT_PREFIX + "/" + filePath + "/" + fileName;
  }

}
