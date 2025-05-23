// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@BuildType(ipa = true, asc = true)
public class CreateEntitlements implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    String filename = context.getProject().getProjectName() + ".app.xcent";
    File entitlements = new File(context.getPaths().getTmpDir(), filename);
    context.setEntitlements(entitlements);
    NSDictionary root = new NSDictionary();
    root.put("application-identifier", context.getAppId());
    root.put("com.apple.developer.team-identifier", context.getTeamId());
    root.put("get-task-allow", false);
    NSArray keychainGroups = new NSArray(
        NSObject.fromJavaObject(context.getAppId())
    );
    root.put("keychain-access-groups", keychainGroups);
    try (PrintStream out = new PrintStream(new FileOutputStream(entitlements))) {
      out.print(root.toXMLPropertyList());
      return TaskResult.generateSuccess();
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
  }
}
