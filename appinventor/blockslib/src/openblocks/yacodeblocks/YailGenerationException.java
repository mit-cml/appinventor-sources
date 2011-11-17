//Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for exceptions that occur during yail
 * code generation
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class YailGenerationException extends CodeblocksException {

  // The name of the form being built when an error occurred
  private String formName;

  public YailGenerationException(String message, String formName) {
    super(message);
    this.formName = formName;
  }

  public YailGenerationException(String message) {
    super(message);
  }

  public YailGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Return the name of the form that yail generation failed on.
   */
  public String getFormName() {
    return formName;
  }
}
