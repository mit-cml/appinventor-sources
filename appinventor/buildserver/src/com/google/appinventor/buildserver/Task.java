package com.google.appinventor.buildserver;

public interface Task {
  /**
   * Main method to run the task
   *
   * @return TaskResult
   */
  TaskResult execute(CompilerContext context);
}
