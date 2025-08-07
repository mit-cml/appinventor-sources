// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for command execution and I/O redirection.
 *
 */
public final class Execution {

  // Logging support
  private static final Logger LOG = Logger.getLogger(Execution.class.getName());
  private static final Joiner joiner = Joiner.on(" ");

  private static boolean DISABLE_TIMEOUTS = false;

  public enum Timeout {
    SHORT(5), // 5 seconds
    MEDIUM(30), // 30 seconds
    LONG(120); // 2 minutes

    private final int seconds;

    Timeout(int seconds) {
      this.seconds = seconds;
    }

    public int getSeconds() {
      return seconds;
    }
  }

  public static void disableTimeouts() {
    DISABLE_TIMEOUTS = true;
  }

  /*
   * Input stream handler used for stdout and stderr redirection.
   */
  private static class RedirectStreamHandler extends Thread {
    // Streams to redirect from and to
    private final InputStream input;
    private final PrintWriter output;

    RedirectStreamHandler(PrintWriter output, InputStream input) {
      this.input = Preconditions.checkNotNull(input);
      this.output = Preconditions.checkNotNull(output);
      start();
    }

    @Override
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            output.println(line);
        }
      } catch (IOException ioe) {
        // OK to ignore...
        LOG.log(Level.WARNING, "____I/O Redirection failure: ", ioe);
      }
    }
  }

  private Execution() {
  }

  /**
   * Executes a command in a command shell.
   *
   * @param workingDir  working directory for the command
   * @param command  command to execute and its arguments
   * @param out  standard output stream to redirect to
   * @param err  standard error stream to redirect to
   * @param timeoutSeconds  timeout in seconds for the command execution
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */
  public static boolean execute(File workingDir, String[] command, PrintStream out,
      PrintStream err, int timeoutSeconds) {
    LOG.log(Level.INFO, "____Executing " + joiner.join(command));
    if (System.getProperty("os.name").startsWith("Windows")){
      for(int i =0; i < command.length; i++){
        command[i] = command[i].replace("\"", "\\\"");
      }
    }

    try {
      Process process = Runtime.getRuntime().exec(command, null, workingDir);
      // Prevent any interactive shell from waiting for input
      process.getOutputStream().close();
      new RedirectStreamHandler(new PrintWriter(out, true), process.getInputStream());
      new RedirectStreamHandler(new PrintWriter(err, true), process.getErrorStream());

      if (timeoutSeconds <= 0 || DISABLE_TIMEOUTS) {
        return process.waitFor() == 0;
      }

      if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        err.println("Process had to be forcibly terminated due to timeout");
        return false;
      }

      return process.exitValue() == 0;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "____Execution failure: ", e);
      return false;
    }
  }

  /**
   * Executes a command in a command shell.
   *
   * @param workingDir  working directory for the command
   * @param command  command to execute and its arguments
   * @param out  standard output stream to redirect to
   * @param err  standard error stream to redirect to
   * @param timeout  timeout for the command execution
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */
  public static boolean execute(File workingDir, String[] command, PrintStream out,
      PrintStream err, Timeout timeout) {
    return execute(workingDir, command, out, err, timeout.getSeconds());
  }

  /**
   * Executes a command in a command shell.
   *
   * @param workingDir  working directory for the command
   * @param command  command to execute and its arguments
   * @param out  standard output stream to redirect to
   * @param err  standard error stream to redirect to
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */
  public static boolean execute(File workingDir, String[] command, PrintStream out,
      PrintStream err) {
    return execute(workingDir, command, out, err, Timeout.SHORT);
  }
}
