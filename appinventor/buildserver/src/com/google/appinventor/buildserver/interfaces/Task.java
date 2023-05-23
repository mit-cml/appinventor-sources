package com.google.appinventor.buildserver.interfaces;

import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.TaskResult;

public interface Task {
  /**
   * Main method to run the task
   *
   * @return TaskResult
   */
  TaskResult execute(CompilerContext context);
}
