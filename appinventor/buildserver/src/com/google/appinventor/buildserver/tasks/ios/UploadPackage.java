// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.Execution;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BuildType(asc = true)
public class UploadPackage implements IosTask {
  private static final String ITMS_TRANSPORTER = "/usr/local/itms/bin/iTMSTransporter";
  private static final Pattern SHORT_NAME = Pattern.compile("^\\d+.*\\s+(\\w+)$");

  @Override
  public TaskResult execute(IosCompilerContext context) {
    final String username = context.getAppleId();
    final String password = context.getAppSpecificPassword();
    StringBuffer outBuffer = new StringBuffer();
    StringBuffer errBuffer = new StringBuffer();
    try {
      Execution.execute(context.getWorkDir(), new String[]{
          ITMS_TRANSPORTER,
          "-m",
          "provider",
          "-u",
          username,
          "-p",
          password
      }, outBuffer, errBuffer);
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    String shortName = context.getShortName();
    if (shortName == null) {
      try (BufferedReader in = new BufferedReader(new StringReader(outBuffer.toString()))) {
        String line;
        while ((line = in.readLine()) != null) {
          Matcher matcher = SHORT_NAME.matcher(line);
          if (matcher.find()) {
            shortName = matcher.group(1);
            break;
          }
        }
      } catch (IOException e) {
        return TaskResult.generateError(e);
      }
      if (shortName == null) {
        return TaskResult.generateError("Unable to determine the App Store Connect short name.");
      }
    }
    outBuffer = new StringBuffer();
    errBuffer = new StringBuffer();
    try {
      if (Execution.execute(context.getWorkDir(), new String[]{
          ITMS_TRANSPORTER,
          "-m",
          "upload",
          "-assetFile",
          context.getPaths().getDeployFile().getAbsolutePath(),
          "-u",
          username,
          "-p",
          password,
          "-asc_provider",
          shortName
      }, outBuffer, errBuffer) == 0) {
        return TaskResult.generateSuccess();
      }
      String error = errBuffer.toString();
      if (error.contains("already been successfully delivered")) {
        return TaskResult.generateError("Build number already in use. Increase your Version Code "
            + "and resubmit your app.");
      }
      return TaskResult.generateError(errBuffer.toString());
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
  }
}
