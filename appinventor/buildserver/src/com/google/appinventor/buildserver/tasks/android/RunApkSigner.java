// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.interfaces.AndroidTask;
import com.google.appinventor.buildserver.util.Execution;

/**
 * compiler.runApkSigner()
 */
@BuildType(apk = true)
public class RunApkSigner implements AndroidTask {
  @Override
  public TaskResult execute(AndroidCompilerContext context) {
    int mx = context.getChildProcessRam() - 200;
    /*
      apksigner sign\
      --ks <keystore file>\
      --ks-key-alias AndroidKey\
      --ks-pass pass:android\
      <APK>
    */
    String[] apksignerCommandLine = {
        System.getProperty("java.home") + "/bin/java", "-jar",
        "-mx" + mx + "M",
        context.getResources().getApksignerJar(), "sign",
        "-ks", context.getKeystoreFilePath(),
        "-ks-key-alias", "AndroidKey",
        "-ks-pass", "pass:android",
        context.getPaths().getDeployFile().getAbsolutePath()
    };

    if (!Execution.execute(null, apksignerCommandLine,
        System.out, System.err, Execution.Timeout.SHORT)) {
      context.getReporter().warn("We could not sign your app. In case you are using a custom keystore, please make " +
        "sure its password is set to 'android', and the key is set to 'androidkey'.");
      return TaskResult.generateError("Error while running ApkSigner tool");
    }

    return TaskResult.generateSuccess();
  }
}
