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
 * <p>This package is an exploration spike. The OIDC state and the grade
 * passback context are held in memory
 * ({@link com.google.appinventor.server.lti.LtiState},
 * {@link com.google.appinventor.server.lti.LtiGradeContext}), configuration is
 * read from flags ({@link com.google.appinventor.server.lti.LtiConfig}), and
 * role and deployment checks are lenient. A production version would move that
 * state and the platform registrations into the datastore.
 */
package com.google.appinventor.server.lti;
