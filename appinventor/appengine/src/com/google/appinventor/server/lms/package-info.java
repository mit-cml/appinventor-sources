// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Learning Management System (LMS) integrations for App Inventor.
 *
 * <p>This package is the server-side root for App Inventor's LMS integrations.
 * The first provider is Google Classroom; additional providers (Canvas and
 * Moodle via LTI 1.3) can be added later behind the same storage and servlet
 * layers.
 *
 * <p>It currently implements the Google Classroom sign-in flow, which connects a
 * user's account and stores the resulting refresh token:
 * <ul>
 *   <li>{@link com.google.appinventor.server.lms.LmsConnectServlet} starts the
 *       flow for the signed-in user, sealing the user id into an encrypted
 *       {@link com.google.appinventor.server.lms.LmsOAuthState} and redirecting
 *       to Google's consent screen.</li>
 *   <li>{@link com.google.appinventor.server.lms.LmsAuthCallbackServlet}
 *       completes the flow after Google's session-less redirect, recovering the
 *       user id from the state and exchanging the authorization code through
 *       {@link com.google.appinventor.server.lms.GoogleOAuthClient}.</li>
 *   <li>{@link com.google.appinventor.server.lms.LmsCredentialStore} encrypts the
 *       refresh token with Keyczar and persists it as a per-user file through
 *       {@code StorageIo}, the same mechanism used for the Android keystore and
 *       the App Store credentials. The plaintext token is never written to
 *       storage.</li>
 *   <li>{@link com.google.appinventor.server.lms.LmsOAuthConfig} reads the OAuth
 *       client configuration from flags and builds the authorization URL.</li>
 *   <li>{@link com.google.appinventor.server.lms.LmsHttp} holds the small HTTP
 *       and JSON helpers shared by the Google API clients.</li>
 * </ul>
 *
 * <p>It also provides the building blocks for exporting a project to the user's
 * own Drive, ahead of the endpoint that will call them:
 * <ul>
 *   <li>{@link com.google.appinventor.server.lms.GoogleDriveUploader} uploads file
 *       bytes to the user's Drive under the {@code drive.file} scope, and
 *       {@link com.google.appinventor.server.lms.GoogleOAuthClient} refreshes the
 *       stored credential into the access token that upload needs.</li>
 *   <li>{@link com.google.appinventor.server.lms.GoogleDriveProjectExporter} ties
 *       the export helper, the credential store, the refresh, and the upload into
 *       a single building block.</li>
 * </ul>
 *
 * <p>Planned additions: the teacher and student designer UI, and the rest of the
 * assignment lifecycle (list courses, distribute a template project, collect
 * submissions, set a grade).
 */
package com.google.appinventor.server.lms;
