// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Experimental LTI 1.3 tool support, so a Learning Management System (for
 * example Moodle or Canvas) can assign App Inventor work, launch students into
 * the IDE, and receive submissions and grades back.
 *
 * <p>The flow, in assignment order:
 *
 * <ol>
 *   <li>An administrator registers this tool once in the LMS with three URLs
 *       served here, {@code /lti/login}, {@code /lti/launch}, and
 *       {@code /lti/jwks} ({@link com.google.appinventor.server.lti.LtiLoginServlet},
 *       {@link com.google.appinventor.server.lti.LtiLaunchServlet},
 *       {@link com.google.appinventor.server.lti.LtiJwksServlet}). No LMS side
 *       code is involved.
 *   <li>A teacher adds an App Inventor assignment in the LMS. With Deep Linking
 *       the teacher can pick one of their own projects as the template
 *       ({@code /lti/deeplink/select},
 *       {@link com.google.appinventor.server.lti.LtiDeepLinkingSelectServlet}).
 *   <li>A student opens the assignment. The launch is validated
 *       ({@link com.google.appinventor.server.lti.LtiJwt}), the student is
 *       signed in by reusing the existing App Inventor session cookie, is given
 *       their own copy of the template (or a blank project), and the IDE opens
 *       that project.
 *   <li>The student submits from the IDE Project menu, which calls
 *       {@code /lti/submit} ({@link com.google.appinventor.server.lti.LtiSubmitServlet})
 *       and posts a submitted state to the LMS gradebook over Assignment and
 *       Grade Services ({@link com.google.appinventor.server.lti.LtiAgs}).
 *   <li>The teacher grades in the LMS and the student views the grade there.
 * </ol>
 *
 * <p>The assignment to project link and the grade passback target are stored
 * durably per user ({@link com.google.appinventor.server.lti.LtiResourceLinks},
 * {@link com.google.appinventor.server.lti.LtiGradeContext}), so relaunches are
 * idempotent across activity renames and submissions work across server
 * restarts.
 *
 * <p>This package is an exploration spike. The in flight OIDC state is held in
 * memory ({@link com.google.appinventor.server.lti.LtiState}), so a launch must
 * finish on the same server instance that began it, which suits a single
 * instance development server. The platform
 * registry, the account link, the assignment to project link, the grade
 * context, the one time launch nonces, and the tool key pair are kept in the
 * datastore ({@link com.google.appinventor.server.lti.LtiConfig} seeds the
 * registry from flags on first use), so several platforms can share one server
 * and can register through Dynamic Registration.
 */
package com.google.appinventor.server.lti;
