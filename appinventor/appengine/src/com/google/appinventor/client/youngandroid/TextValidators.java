// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.user.client.Window;

/**
 */
public final class TextValidators {

  // This class should never be instantiated.
  private TextValidators() {}

  /**
   * Determines whether the given project name is valid, displaying an alert
   * if it is not.  In order to be valid, the project name must satisfy
   * {@link #isValidIdentifier(String)} and not be a duplicate of an existing
   * project name for the same user.
   *
   * @param projectName the project name to validate
   * @return {@code true} if the project name is valid, {@code false} otherwise
   */
  public static boolean checkNewProjectName(String projectName) {

    // Check the format of the project name
    if (!isValidIdentifier(projectName)) {
      Window.alert(MESSAGES.malformedProjectNameError());
      return false;
    }

    // Check that project does not already exist
    if (Ode.getInstance().getProjectManager().getProject(projectName) != null) {
      Window.alert(MESSAGES.duplicateProjectNameError(projectName));
      return false;
    }

    return true;
  }

  /**
   * Checks whether the argument is a legal identifier, specifically,
   * a non-empty string starting with a letter and followed by any number of
   * (unaccented English) letters, digits, or underscores.
   *
   * @param text the proposed identifier
   * @return {@code true} if the argument is a legal identifier, {@code false}
   *         otherwise
   */
  public static boolean isValidIdentifier(String text) {
    return text.matches("^[a-zA-Z]\\w*$");
  }
}
