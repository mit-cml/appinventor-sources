package com.google.appinventor.buildserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Class to report progress and information to the system or user
 */
public class Reporter {
  private BuildServer.ProgressReporter progress;
  private ByteArrayOutputStream systemAOS;
  private ByteArrayOutputStream userAOS;
  private PrintStream system;
  private PrintStream user;
  private ConsoleColors c;
  private String task;

  private static class ConsoleColors {
    public String RESET = "\u001B[0m";
    public String BLACK = "\u001B[30m";
    public String RED = "\u001B[31m";
    public String GREEN = "\u001B[32m";
    public String YELLOW = "\u001B[33m";
    public String BLUE = "\u001B[34m";
    public String PURPLE = "\u001B[35m";
    public String CYAN = "\u001B[36m";
    public String WHITE = "\u001B[37m";

    public ConsoleColors() {
    }
  }

  public Reporter(BuildServer.ProgressReporter reporter) {
    this.progress = reporter;
    this.systemAOS = new ByteArrayOutputStream();
    this.userAOS = new ByteArrayOutputStream();
    this.system = new PrintStream(this.systemAOS);
    this.user = new PrintStream(this.userAOS);
    this.c = new ConsoleColors();
  }

  public void setProgress(int progress) {
    if (this.progress != null) {
      this.progress.report(progress);
    }
  }

  private String task(String colorCode) {
    boolean color = colorCode != null && !colorCode.equals("");

    if (task != null && !task.equals("")) {
      return (color ? colorCode : "") + "[" + (color ? c.PURPLE : "") + task + (color ? c.RESET : "") + (color ? colorCode : "") + "] ";
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

  public void error(String message, boolean user) {
    String text = "ERROR: " + message;
    System.out.println(task(c.RED) + text + c.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + text);
    }
  }

  public void warn(String message) {
    this.warn(message, false);
  }

  public void warn(String message, boolean user) {
    String text = "WARN: " + message;
    System.out.println(task(c.YELLOW) + text + c.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + text);
    }
  }

  public void info(String message) {
    this.info(message, false);
  }

  public void info(String message, boolean user) {
    String text = "INFO: " + message;
    System.out.println(task(c.CYAN) + text + c.RESET);
    this.system.println(task(null) + text);
    if (user) {
      this.user.println(task(null) + message);
    }
  }

  public void log(String message) {
    this.log(message, false);
  }

  public void log(String message, boolean user) {
    System.out.println(task(c.WHITE) + message);
    this.system.println(task(null) + message);
    if (user) {
      this.user.println(task(null) + message);
    }
  }

  public void taskStart(String name) {
    this.task = name;
    System.out.println(this.task(c.BLUE) + "Starting Task" + c.RESET);
    this.system.println(this.task(null) + "Starting Task");
  }

  public void taskSuccess(double seconds) {
    System.out.println(this.task(c.GREEN) + "Task succeeded in " + c.PURPLE + seconds + c.GREEN + " seconds" + c.RESET);
    this.system.println(this.task(null) + "Task succeeded in " + seconds + " seconds");
    this.task = null;
  }

  public void taskError(double seconds) {
    System.out.print(this.task(c.RED) + "Task errored");
    this.system.print(this.task(null) + "Task errored");
    if (seconds > 0) {
      System.out.print(" in " + c.PURPLE + seconds + c.RED + " seconds");
      this.system.print(" in " + seconds + " seconds");
    }
    System.out.print(c.RESET + "\n");
    this.system.print("\n");
    this.task = null;
  }


  // AFTER FINISHING, CLOSE ALL PRINT STREAMS

  public void close() {
    if (this.system != null) {
      this.system.close();
    }
    if (this.user != null) {
      this.user.close();
    }
  }


  // CONVERT LOGS TO STRING

  public String getSystemOutput() {
    if (systemAOS == null) {
      return "";
    }

    try {
      return systemAOS.toString(PathUtil.DEFAULT_CHARSET);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }

  public String getUserOutput() {
    if (userAOS == null) {
      return "";
    }

    try {
      return userAOS.toString(PathUtil.DEFAULT_CHARSET);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }
}
