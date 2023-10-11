// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Class to report progress and information to the system or user.
 */
public class Reporter {
  private final BuildServer.ProgressReporter progress;
  private final ByteArrayOutputStream systemBuffer;
  private final ByteArrayOutputStream userBuffer;
  private final PrintStream system;
  private final PrintStream user;
  private String task;

  private static class ConsoleColors {
    static final String RESET = "\u001B[0m";
    static final String BLACK = "\u001B[30m";
    static final String RED = "\u001B[31m";
    static final String GREEN = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE = "\u001B[34m";
    static final String PURPLE = "\u001B[35m";
    static final String CYAN = "\u001B[36m";
    static final String WHITE = "\u001B[37m";

    public ConsoleColors() {
    }
  }

  /**
   * Construct a new Reporter that will report any progress via the provided ProgressReporter.
   *
   * @param reporter a ProgressReporter that will receive updates on build progress
   */
  public Reporter(BuildServer.ProgressReporter reporter) {
    this.progress = reporter;
    this.systemBuffer = new ByteArrayOutputStream();
    this.userBuffer = new ByteArrayOutputStream();
    this.system = new PrintStream(this.systemBuffer);
    this.user = new PrintStream(this.userBuffer);
  }

  /**
   * Set the current build progress.
   *
   * @param progress build progress
   */
  public void setProgress(int progress) {
    if (this.progress != null) {
      this.progress.report(progress);
    }
  }

  private String task(String colorCode) {
    boolean color = colorCode != null && !colorCode.equals("");

    if (task != null && !task.equals("")) {
      return (color ? colorCode : "") + "[" + (color ? ConsoleColors.PURPLE : "") + task
          + (color ? ConsoleColors.RESET : "") + (color ? colorCode : "") + "] ";
    }
    return (color ? colorCode : "");
  }

  public PrintStream getSystemOut() {
    return system;
  }


  // UTILS TO REPORT MESSAGES

  public void error(String message) {
    this.error(message, false);
  }

  /**
   * Report an error in the build process. The user parameter indicates whether the error should
   * be reported to the user.
   *
   * @param message the error message
   * @param user true if the user should receive the error
   */
  public void error(String message, boolean user) {
    String text = "ERROR: " + message;
    System.out.println(task(ConsoleColors.RED) + text + ConsoleColors.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + text);
    }
  }

  public void warn(String message) {
    this.warn(message, false);
  }

  /**
   * Report a warning during the build process. The user parameter indicates whether the warning
   * should be sent to the user.
   *
   * @param message the warning message
   * @param user true if the user should receive the warning
   */
  public void warn(String message, boolean user) {
    String text = "WARN: " + message;
    System.out.println(task(ConsoleColors.YELLOW) + text + ConsoleColors.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + text);
    }
  }

  public void info(String message) {
    this.info(message, false);
  }

  /**
   * Report info during the build process. The user parameter indicates whether the info should
   * be sent to the user.
   *
   * @param message the info message
   * @param user true if the user should receive the information
   */
  public void info(String message, boolean user) {
    String text = "INFO: " + message;
    System.out.println(task(ConsoleColors.CYAN) + text + ConsoleColors.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + message);
    }
  }

  public void log(String message) {
    this.log(message, false);
  }

  /**
   * Report general logs during the build process. The user parameter indicates whether the log
   * should be sent to the user.
   *
   * @param message the log message
   * @param user true if the user should receive the log
   */
  public void log(String message, boolean user) {
    System.out.println(task(ConsoleColors.WHITE) + message);
    this.system.println(task(null) + message);
    if (user) {
      this.user.println(task(null) + message);
    }
  }

  /**
   * Log that a task has started.
   *
   * @param name the name of the task
   */
  public void taskStart(String name) {
    this.task = name;
    System.out.println(this.task(ConsoleColors.BLUE) + "Starting Task" + ConsoleColors.RESET);
    this.system.println(this.task(null) + "Starting Task");
  }

  /**
   * Log that the current task has succeeded.
   *
   * @param seconds the time in seconds to complete the task
   */
  public void taskSuccess(double seconds) {
    System.out.println(this.task(ConsoleColors.GREEN) + "Task succeeded in " + ConsoleColors.PURPLE
        + seconds + ConsoleColors.GREEN + " seconds" + ConsoleColors.RESET);
    this.system.println(this.task(null) + "Task succeeded in " + seconds + " seconds");
    this.task = null;
  }

  /**
   * Log that the current task has failed.
   *
   * @param seconds the time in seconds the task ran before failing
   */
  public void taskError(double seconds) {
    System.out.print(this.task(ConsoleColors.RED) + "Task errored");
    this.system.print(this.task(null) + "Task errored");
    if (seconds > 0) {
      System.out.print(" in " + ConsoleColors.PURPLE + seconds + ConsoleColors.RED + " seconds");
      this.system.print(" in " + seconds + " seconds");
    }
    System.out.print(ConsoleColors.RESET + "\n");
    this.system.print("\n");
    this.task = null;
  }


  // AFTER FINISHING, CLOSE ALL PRINT STREAMS

  public void close() {
    this.system.close();
    this.user.close();
  }


  // CONVERT LOGS TO STRING

  public String getSystemOutput() {
    return systemBuffer.toString();
  }

  public String getUserOutput() {
    return userBuffer.toString();
  }
}
