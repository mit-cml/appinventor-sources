// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

public class TaskResult {
  private final int exitCode;
  private Exception error;

  private TaskResult(int code) {
    this.exitCode = code;
  }

  private TaskResult(int code, Exception error) {
    this.exitCode = code;
    this.error = error;
  }

  /**
   * Generates a success object for the Compiler.
   *
   * @return TaskResult(0)
   */
  public static TaskResult generateSuccess() {
    return new TaskResult(TaskExecutionCodes.SUCCESS_EXIT_CODE);
  }

  /**
   * Generates an error object for the Compiler.
   *
   * @return TaskResult(1, Exception)
   */
  public static TaskResult generateError(String error) {
    return TaskResult.generateError(new Exception(error));
  }

  public static TaskResult generateError(Exception error) {
    return new TaskResult(TaskExecutionCodes.ERROR_EXIT_CODE, error);
  }

  /**
   * Checks if the result object contains a success code.
   *
   * @return boolean
   */
  public boolean isSuccess() {
    return TaskResult.isSuccess(this.getExitCode());
  }

  /**
   * Checks if the given TaskResult was exited with success.
   *
   * @param result TaskResult
   * @return boolean
   */
  public static boolean isSuccess(TaskResult result) {
    return TaskResult.isSuccess(result.getExitCode());
  }

  /**
   * Checks if the given number is the same as the success code.
   *
   * @param result int
   * @return boolean
   */
  public static boolean isSuccess(int result) {
    return result == TaskExecutionCodes.SUCCESS_EXIT_CODE;
  }

  /**
   * Returns the exit code of the task.
   *
   * @return int
   */
  public int getExitCode() {
    return this.exitCode;
  }

  /**
   * Optional exception that is present when exit code is not success.
   *
   * @return Exception
   */
  public Exception getError() {
    return this.error;
  }
}
