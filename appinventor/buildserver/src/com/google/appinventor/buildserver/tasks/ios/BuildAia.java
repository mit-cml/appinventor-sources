// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

@BuildType(ipa = true, asc = true)
public class BuildAia implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    File root = context.getPaths().getProjectRootDir();
    File assets = new File(root, "assets/");
    File src = new File(root, "src/");
    File target = new File(context.getPaths().getAppDir(), "appdata.aia");
    String base = root.getAbsolutePath() + "/";
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target))) {
      if (assets.exists()) {
        addToZip(out, assets, base);
      }
      addToZip(out, src, base);
    } catch (IOException e) {
      return TaskResult.generateError(e);
    }
    return TaskResult.generateSuccess();
  }

  private void addToZip(ZipOutputStream out, File file, String base) throws IOException {
    if (file.getName().endsWith(".mobileprovision")) {
      return;
    }
    if (file.isDirectory()) {
      out.putNextEntry(new ZipEntry(file.getAbsolutePath().replace(base, "") + "/"));
      out.closeEntry();
      File[] files = file.listFiles();
      if (files == null) {
        return;
      }
      for (File child : files) {
        String path = child.getAbsolutePath();
        if (path.endsWith(".scm") || path.endsWith(".bky")) {
          // Do not include the source files in the final AIA
          continue;
        }
        if (path.contains("/external_comps/")) {
          // Do not include extensions in the final AIA
          continue;
        }
        addToZip(out, child, base);
      }
    } else {
      out.putNextEntry(new ZipEntry(file.getAbsolutePath().replace(base, "")));
      try (FileInputStream in = new FileInputStream(file)) {
        IOUtils.copy(in, out);
      }
      out.closeEntry();
    }
  }
}
