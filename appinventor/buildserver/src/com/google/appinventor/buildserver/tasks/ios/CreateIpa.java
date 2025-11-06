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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

@BuildType(ipa = true, asc = true)
public class CreateIpa implements IosTask {
  private static final Logger LOG = Logger.getLogger(CreateIpa.class.getName());

  @Override
  public TaskResult execute(IosCompilerContext context) {
    File basedir = context.getPaths().getPayloadDir();
    String outputFileName = context.getOutputFileName();
    if (outputFileName == null) {
      outputFileName = "PlayerApp.ipa";
    }
    File deploy = ExecutorUtils.createDir(context.getPaths().getBuildDir(), "deploy");
    File target = new File(deploy, outputFileName);
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target))) {
      recursivelyAddFiles(out, basedir, basedir.getParentFile());
      if (context.isForAppStore()) {
        recursivelyAddFiles(out, new File(basedir.getParentFile(), "SwiftSupport"),
            basedir.getParentFile());
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Exception creating IPA", e);
      return TaskResult.generateError(e);
    }
    if (!context.isForAppStore()) {
      context.getOutputFiles().add(target);
    }
    return TaskResult.generateSuccess();
  }

  protected void recursivelyAddFiles(ZipOutputStream out, File file, File base) throws IOException {
    File[] files = file.listFiles();
    if (files != null) {
      out.putNextEntry(new ZipEntry(file.getAbsolutePath()
          .replace(base.getAbsolutePath() + "/", "") + "/"));
      out.closeEntry();
      for (File child : files) {
        if (child.isDirectory()) {
          recursivelyAddFiles(out, child, base);
        } else {
          out.putNextEntry(new ZipEntry(child.getAbsolutePath()
              .replace(base.getAbsolutePath() + "/", "")));
          try (InputStream in = new FileInputStream(child)) {
            IOUtils.copy(in, out);
          }
          out.closeEntry();
        }
      }
    }
  }
}
