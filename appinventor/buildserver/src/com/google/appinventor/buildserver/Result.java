// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import java.util.logging.Logger;

/**
 * Class used to report results of an external call or remote process.
 *
 * This class is designed to mimic the same information that would be
 * typically available when executing a separate process: an integer result
 * code and Strings for output and error.  The result value
 * (@link #SUCCESS} (0) should be used to indicate success; any other value
 * indicates failure.
 * <p>
 * While the interpretation of the text returned by {@link #getOutput()} and
 * {@link #getError()} is up to the clients of this class, some recommended
 * interpretations are:
 * <ul>
 * <li> The result of {@link #getOutput()} should be displayed to the user on
 *      success, and the result of {@link #getError()} on failure, or
 * <li> The result of {@link #getOutput()} should always be displayed to the
 *      user, and the result of {@link #getError()} on failure.
 * </ul>
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class Result {
  /**
   * The value of the {@code result} if the RPC was successful.  Any
   * other value indicates failure.
   */
  public static final int SUCCESS = 0;
  public static final int GENERAL_FAILURE = 1;
  public static final int YAIL_GENERATION_ERROR = 2;

  private int result;
  private String output;
  private String error;
  // The name of the form being built when an error occurred
  private String formName;
  
  // Logging support
  private static final Logger LOG = Logger.getLogger(Result.class.getName());

  /**
   * Creates a new Result object
   *
   * @param result the exit code
   * @param output an output string
   * @param error an error string
   * @param formName the name of the form being built when an error occurred
   */
  public Result(int result, String output, String error, String formName) {
    this.result = result;
    this.output = output;
    this.error = error;
    this.formName = formName;
  }

  /**
   * Creates a new Result object
   *
   * @param result the exit code
   * @param output an output string
   * @param error an error string
   */
  public Result(int result, String output, String error) {
    this.result = result;
    this.output = output;
    this.error = error;
  }

  /**
   * Creates a new Result object
   *
   * @param successful a flag indicating whether the call succeeded
   * @param output an output string
   * @param error an error string
   */
  public Result(boolean successful, String output, String error) {
    this.result = (successful ? SUCCESS : GENERAL_FAILURE);
    this.output = output;
    this.error = error;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Result() {
  }

  /**
   * Static constructor for a successful call.
   *
   * @param output the output string (possibly empty)
   * @param error the error string (possibly empty)
   *
   * @return information about a successful call
   */
  public static Result createSuccessfulResult(String output, String error) {
    return new Result(SUCCESS, output, error);
  }

  /**
   * Static constructor for an unsuccessful call.  (The name
   * {@code createFailingRpcResult} was chosen over the more standard
   * {@code createUnsuccessfulRpcResult} because the former is more visually
   * distinguishable from
   * {@link #createSuccessfulResult(String output, String error)}.
   *
   * @param output the output string (possibly empty)
   * @param error the error string
   *
   * @return information about a failing call
   */
  public static Result createFailingResult(String output, String error) {
    return new Result(GENERAL_FAILURE, output, error);
  }

  /**
   * Returns the result code.
   */
  public int getResult() {
    return result;
  }

  /**
   * Returns the output String.
   */
  public String getOutput() {
    return output;
  }

  /**
   * Returns the error String.  This is typically, but not necessarily,
   * displayed to the user.
   */
  public String getError() {
    return error;
  }

  /**
   * Return the name of the form where an error occurred
   */
  public String getFormName() {
    return formName;
  }

  /**
   * Indicates whether this succeeded
   *
   * @return {@code true} if the RPC call succeeded, {@code false} otherwise
   */
  public boolean succeeded() {
    return result == SUCCESS;
  }

  /**
   * Indicates whether this failed
   *
   * @return {@code true} if it failed, {@code false} if successful
   */
  public boolean failed() {
    return result != SUCCESS;
  }
}
