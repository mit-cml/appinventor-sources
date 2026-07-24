// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;

/**
 * Exports a user's App Inventor project to their own Google Drive, wiring
 * together pieces that already exist: the source-zip export
 * ({@link FileExporter}), the stored Google credential
 * ({@link LmsCredentialStore}), a refresh of that credential into a short-lived
 * access token ({@link GoogleOAuthClient}), and the Drive upload
 * ({@link GoogleDriveUploader}).
 *
 * <p>This is a building block with a documented contract and no inline access
 * control. It trusts that its caller has already confirmed the requesting user
 * may export the project, the same arrangement the export helper relies on for
 * the download path. The owner check belongs in the calling endpoint, not here.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class GoogleDriveProjectExporter {

  private final FileExporter fileExporter;
  private final LmsCredentialStore credentialStore;

  /** Creates an exporter backed by the default file exporter and credential store. */
  public GoogleDriveProjectExporter() {
    this(new FileExporterImpl(), new LmsCredentialStore());
  }

  @VisibleForTesting
  GoogleDriveProjectExporter(FileExporter fileExporter, LmsCredentialStore credentialStore) {
    this.fileExporter = fileExporter;
    this.credentialStore = credentialStore;
  }

  /**
   * Exports the given project to the user's Google Drive and returns a browser
   * link to the new file. The caller must have already confirmed that the user
   * owns the project.
   *
   * @param userId the App Inventor user id, also the owner of the target Drive
   * @param projectId the project to export
   * @return a browser link (the Drive {@code webViewLink}) to the newly created file
   * @throws IllegalStateException if the user has no stored Google credential, so
   *     the caller should route them through the connect flow first
   * @throws IOException if the token refresh, the export, or the upload fails,
   *     including an {@code invalid_grant} error when the stored credential has
   *     been revoked or has expired
   * @throws EncryptionException if the stored credential cannot be decrypted
   */
  public String exportProjectToDrive(String userId, long projectId)
      throws IOException, EncryptionException {
    String refreshToken = credentialStore.getGoogleRefreshToken(userId);
    if (refreshToken == null) {
      throw new IllegalStateException("No stored Google credential for the user");
    }
    String tokenJson = GoogleOAuthClient.refreshAccessToken(
        LmsOAuthConfig.clientId(), LmsOAuthConfig.clientSecret(), refreshToken);
    String accessToken = LmsHttp.jsonField(tokenJson, "access_token");
    if (accessToken == null) {
      throw new IOException("The Google token refresh returned no access token");
    }
    ProjectSourceZip zip = fileExporter.exportProjectSourceZip(userId, projectId);
    return GoogleDriveUploader.uploadFile(accessToken, zip.getFileName(), zip.getContent());
  }
}
