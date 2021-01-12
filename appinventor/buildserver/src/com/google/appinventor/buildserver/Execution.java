// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
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

  /*
   * Input stream handler and string buffer recorder used for stdout/stderr redirection and to return results in StringBuffer
   */
  private static class RedirectStreamHandleAndgetBuffer extends Thread {
    // Streams to redirect from and to
    private final InputStream input;
    private final PrintWriter output;
    private final StringBuffer outputbuffer;

    RedirectStreamHandleAndgetBuffer(PrintWriter output, InputStream input, StringBuffer outputbuffer) {
      this.input = Preconditions.checkNotNull(input);
      this.output = Preconditions.checkNotNull(output);
      this.outputbuffer = Preconditions.checkNotNull(outputbuffer);
      start();
    }

    @Override
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
          output.println(line);
          outputbuffer.append(line).append("\n");
        }
      } catch (IOException ioe) {
        // OK to ignore...
        LOG.log(Level.WARNING, "____I/O Redirection failure: ", ioe);
      }
    }
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

  /*
   * Input stream handler used for stdout and stderr redirection to StringBuffer
   */
  private static class RedirectStreamToStringBuffer extends Thread {
    // Streams to redirect from and to
    private final InputStream input;
    private final StringBuffer output;

    RedirectStreamToStringBuffer(StringBuffer output, InputStream input) {
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
          output.append(line).append("\n");
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
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */
  public static boolean execute(File workingDir, String[] command, PrintStream out,
      PrintStream err) {
    LOG.log(Level.INFO, "____Executing " + joiner.join(command));
    if (System.getProperty("os.name").startsWith("Windows")){
    	for(int i =0; i < command.length; i++){
    		command[i] = command[i].replace("\"", "\\\"");
    	}
    }
    try {
      Process process = Runtime.getRuntime().exec(command, null, workingDir);
      new RedirectStreamHandler(new PrintWriter(out, true), process.getInputStream());
      new RedirectStreamHandler(new PrintWriter(err, true), process.getErrorStream());
      return process.waitFor() == 0;
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
   * @return  String Array with three arguments [true/false-String,Out-String,Err-String]
   */
  public static String[] executeandget(File workingDir, String[] command, PrintStream out,
                                       PrintStream err) {
    LOG.log(Level.INFO, "____Executing " + joiner.join(command));
    if (System.getProperty("os.name").startsWith("Windows")){
      for(int i =0; i < command.length; i++){
        command[i] = command[i].replace("\"", "\\\"");
      }
    }
    try {
      Process process = Runtime.getRuntime().exec(command, null, workingDir);
      StringBuffer strBufferOut=new StringBuffer();
      StringBuffer strBufferErr=new StringBuffer();
      new RedirectStreamHandleAndgetBuffer(new PrintWriter(out, true), process.getInputStream(),strBufferOut);
      new RedirectStreamHandleAndgetBuffer(new PrintWriter(err, true), process.getErrorStream(),strBufferErr);
      return new String[]{Boolean.toString(process.waitFor() == 0),strBufferOut.toString(),strBufferErr.toString()};
    } catch (Exception e) {
      LOG.log(Level.WARNING, "____Execution failure: ", e);
      return new String[]{"false","",""}; //Must have size 3
    }
  }

  /**
   * Executes a command, redirects standard output and standard error to
   * string buffers, and returns the process's exit code.
   *
   * @param workingDir  working directory for the command
   * @param command  command to execute and its arguments
   * @param out  standard output stream to redirect to
   * @param err  standard error stream to redirect to
   * @return  the exit code of the process
   */
  public static int execute(File workingDir, String[] command, StringBuffer out,
      StringBuffer err) throws IOException {
    LOG.log(Level.INFO, "____Executing " + joiner.join(command));
    Process process = Runtime.getRuntime().exec(command, null, workingDir);
    Thread outThread = new RedirectStreamToStringBuffer(out, process.getInputStream());
    Thread errThread = new RedirectStreamToStringBuffer(err, process.getErrorStream());
    try {
      process.waitFor();
      outThread.join();
      errThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return process.exitValue();
  }
}
