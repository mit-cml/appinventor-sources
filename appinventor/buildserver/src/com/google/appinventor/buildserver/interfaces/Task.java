// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.interfaces;

import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.TaskResult;

public interface Task {
  /**
   * Main method to run the task.
   *
   * @return TaskResult
   */
  TaskResult execute(CompilerContext context);
}
