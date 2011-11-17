// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Coercion result represents the result of a coercion attempt. It also contains
 * additional metadata about the coercion, such as an error message.
 *
 */
public class CoercionResult {

  private boolean result;
  private String errorMessage;

  private static final String COERCION_ERROR = "This block cannot plug into this socket";

  /**
   * Create a coercion result with a given result.
   * @param result the result of the coercion
   */
  public CoercionResult(boolean result) {
    this.result = result;
    this.errorMessage = "";
  }

  /**
   * Create a coercion result with a given result and optional error message.
   * @param result the result of the coercion
   * @param errorMessage the error message for this coercion, if any
   */
  public CoercionResult(boolean result, String errorMessage) {
    this.result = result;
    if (errorMessage == null || errorMessage.length() == 0) {
      this.errorMessage = "";
    } else {
      this.errorMessage = errorMessage;
    }
  }

  /**
   * Return the error message for this coercion.
   * @return error message
   */
  public String getErrorMessage() {
    return (errorMessage.length() > 0 ? COERCION_ERROR + " because " + errorMessage :
            COERCION_ERROR);
  }

  /**
   * Return the success of the coercion.
   * @return success of the coercion
   */
  public boolean getResult() {
    return result;
  }
}