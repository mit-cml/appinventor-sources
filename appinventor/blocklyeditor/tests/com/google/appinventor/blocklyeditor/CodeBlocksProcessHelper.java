// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.blocklyeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for command execution and I/O redirection.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public final class CodeBlocksProcessHelper {

  private static final boolean DEBUG = false;

  /*
   * Input stream handler used for stdout and stderr redirection.
   */
  private static class RedirectStreamHandler extends Thread {
    // Streams to redirect from and to
    private final InputStream input;
    private final StringBuffer output;

    RedirectStreamHandler(InputStream input, StringBuffer output) {
      this.input = input;
      this.output = output;
      start();
    }

    @Override
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
          if (output != null) {
            output.append(line).append("\n");
          }
        }
        reader.close();
      } catch (IOException ioe) {
        // OK to ignore...
      }
    }
  }

  private CodeBlocksProcessHelper() {
  }

  /**
   * Executes a command as a separate process.
   *
   * @param workingDir  working directory for the command, can be null
   * @param command  command to execute and its arguments
   * @param out  where to redirect standard output, can be null
   * @param err  where to redirect standard error, can be null
   * @param waitForOutput whether to wait for the output stream process
   * @return  {@code true} if the command succeeds, {@code false} otherwise
   */

  private synchronized static boolean exec(File workingDir, String[] command, StringBuffer out,
      StringBuffer err, boolean waitForOutput) {
   
    try {
      if (DEBUG) {
        System.out.println("CodeBlocksProcessHelper exec private method on command");
      }
      
      Process process = Runtime.getRuntime().exec(command, null, workingDir);
      // We create the out and err stream handlers even in the case where we don't wait
      // for (or want) the output.  The reason is that on (some?) Windows, if you
      // don't handle stdout and stderr, a process that writes more than about
      // 128 bytes will hang.
      
      Thread outHandler = new RedirectStreamHandler(process.getInputStream(), out);
      Thread errHandler = new RedirectStreamHandler(process.getErrorStream(), err);
      int exitCode =  process.waitFor();
      if (DEBUG) {
        System.out.println("exit code = " + exitCode);
      }
      
      if (waitForOutput) {
        outHandler.join();
        errHandler.join();
      }
      process.exitValue();
      return exitCode == 0;
    } catch (Exception e) {
      System.out.println("CodeBlocksProcessHelper: " +
          "Error trying to exec command: " + e.getMessage());
      return false;
    }
  }

  /**
   * Executes a command as a separate process.
   *
   * @param command  command to execute and its arguments
   * @param waitForOutput whether to wait for the output stream process
   * @return the process output.  NOTE: The returned value and
   * the thrown error message may be incorrect if waitForOutput is false
   */
  public synchronized static String exec(String[] command, boolean waitForOutput) throws IOException {
    StringBuffer out = null;
    StringBuffer err = null;
    if (DEBUG) {
      System.out.println("CodeBlocksProcessHelper exec public method on command");
    }
    out = new StringBuffer();
    err = new StringBuffer();
    boolean result = exec(null, command, out, err, waitForOutput);
    if (DEBUG) {
      printIfNotEmpty(out.toString(), "command process response");
      printIfNotEmpty(err.toString(), "command process error");
      printIfNotEmpty(Boolean.toString(result), "command process result");
    }
    if (!result) {
      throw new IOException(err.toString());
    }
    return out.toString();
  }

  static private void printIfNotEmpty(String output, String prefix) {
    if (output.length() != 0) {
      System.out.println(prefix + ": " + output);
    }
  }

  // We can't just send a bare command string for Windows to exec.  We need to
  // explicitly invoke the command processor with cmd.exe
  public static String execOnWindows(String commandString, boolean waitForOutput)
      throws IOException {
    String[] command = new String[] {"cmd.exe", "/C", commandString};
    if (DEBUG) {
      System.out.println("CodeBlocksProcessHelper execOnWindows: " + commandString);
    }
    return exec(command, waitForOutput);
  }
}
