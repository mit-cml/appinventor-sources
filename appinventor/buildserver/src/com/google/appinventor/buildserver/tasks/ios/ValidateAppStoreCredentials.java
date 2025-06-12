// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;

@BuildType(asc = true)
public class ValidateAppStoreCredentials implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    String username = context.getAppleId();
    String password = context.getAppSpecificPassword();
    if (username == null || username.isEmpty()) {
      return TaskResult.generateError("Missing username for App Store");
    }
    if (password == null || password.isEmpty()) {
      return TaskResult.generateError("Missing password for App Store");
    }
    return TaskResult.generateSuccess();
  }
}
