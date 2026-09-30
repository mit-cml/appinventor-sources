// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 * Starts the Google Classroom sign-in flow by navigating the browser to the
 * server's {@code /lms/connect} endpoint. That endpoint runs behind the
 * authentication filter, mints the OAuth state for the signed-in user, and
 * redirects on to Google's consent screen; the callback returns to App Inventor.
 */
public class ConnectGoogleClassroomAction implements Command {
  private static final String CONNECT_URL = "/lms/connect";

  @Override
  public void execute() {
    // assign, not replace, so the user can press Back to return to the IDE if
    // they cancel Google's consent.
    Window.Location.assign(CONNECT_URL);
  }
}
