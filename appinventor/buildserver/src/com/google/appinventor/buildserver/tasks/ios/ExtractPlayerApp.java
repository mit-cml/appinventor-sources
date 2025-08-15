// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@BuildType(ipa = true, asc = true)
public class ExtractPlayerApp implements IosTask {
  private static final int BUFSIZE = 4096;

  @Override
  public TaskResult execute(IosCompilerContext context) {
    File buildDir = context.getPaths().getBuildDir();
    File payloadDir = ExecutorUtils.createDir(buildDir, "Payload");
    File appDir = ExecutorUtils.createDir(payloadDir,
        "PlayerApp.app"
    );
    byte[] buffer = new byte[BUFSIZE];
    try (InputStream is = ExtractPlayerApp.class.getResourceAsStream("/files/PlayerApp-unsigned.ipa");
         ZipInputStream zipInputStream = new ZipInputStream(is)) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        readIntoFile(buildDir, entry.getName(), zipInputStream, buffer);
      }
      context.getPaths().setPayloadDir(payloadDir);
      context.getPaths().setAppDir(appDir);
      context.getPaths().setFrameworkDir(new File(appDir, "Frameworks/"));
    } catch (IOException e) {
      e.printStackTrace();
      return TaskResult.generateError(e);
    }
    return TaskResult.generateSuccess();
  }

  private static void readIntoFile(File appDir, String name, InputStream is, byte[] buffer)
      throws IOException {
    File dir = appDir;
    int index;
    while ((index = name.indexOf('/')) >= 0) {
      dir = ExecutorUtils.createDir(dir, name.substring(0, index));
      name = name.substring(index + 1);
    }
    try (FileOutputStream out = new FileOutputStream(new File(dir, name))) {
      int read;
      while ((read = is.read(buffer, 0, buffer.length)) > 0) {
        out.write(buffer, 0, read);
      }
    }
  }
}
