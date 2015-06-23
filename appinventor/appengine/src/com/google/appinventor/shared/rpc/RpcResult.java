// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Class used as the result of an RPC call.
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
public class RpcResult implements IsSerializable {
  /**
   * The value of the {@code result} if the RPC was successful.  Any
   * other value indicates failure.
   */
  public static final int SUCCESS = 0;

  private int result;
  private String output;
  private String error;
  // Extra info that might be passed back (e.g. a json object)
  private String extra;

  /**
   * Creates a new RpcResult object
   *
   * @param result the RPC exit code
   * @param output an output string
   * @param error an error string
   * @param extra an extra string
   */
  public RpcResult(int result, String output, String error, String extra) {
    this.result = result;
    this.output = output;
    this.error = error;
    this.extra = extra;
  }

  /**
   * Creates a new RpcResult object
   *
   * @param result the RPC exit code
   * @param output an output string
   * @param error an error string
   */
  public RpcResult(int result, String output, String error) {
    this.result = result;
    this.output = output;
    this.error = error;
  }

  /**
   * Creates a new RpcResult object
   *
   * @param successful a flag indicating whether the RPC succeeded
   * @param output an output string
   * @param error an error string
   */
  public RpcResult(boolean successful, String output, String error) {
    this.result = (successful ? SUCCESS : ~SUCCESS);
    this.output = output;
    this.error = error;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private RpcResult() {
  }

  /**
   * Static constructor for a successful RPC call.
   *
   * @param output the output string (possibly empty)
   * @param error the error string (possibly empty)
   *
   * @return information about a successful RPC call
   */
  public static RpcResult createSuccessfulRpcResult(String output, String error) {
    return new RpcResult(SUCCESS, output, error);
  }

  /**
   * Static constructor for an unsuccessful RPC call.  (The name
   * {@code createFailingRpcResult} was chosen over the more standard
   * {@code createUnsuccessfulRpcResult} because the former is more visually
   * distinguishable from
   * {@link #createSuccessfulRpcResult(String output, String error)}.
   *
   * @param output the output string (possibly empty)
   * @param error the error string
   *
   * @return information about a failing RPC call
   */
  public static RpcResult createFailingRpcResult(String output, String error) {
    return new RpcResult(~SUCCESS, output, error);
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
   * Returns the error String.
   * This is typically an exception message that may be confusing to the user.
   */
  public String getError() {
    return error;
  }

  /**
   * Returns the extra String.
   */
  public String getExtra() {
    return extra;
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
