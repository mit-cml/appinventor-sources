// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.Compiler;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.AndroidPaths;
import com.google.appinventor.buildserver.tasks.common.BuildFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The AndroidBuildFactory is responsible for setting up the sequence of tasks
 * needed to compile an Android app from an App Inventor project. The factory
 * supports creating both Android Packages (APKs) and Android App Bundles (AABs).
 */
public class AndroidBuildFactory extends BuildFactory<AndroidPaths, AndroidCompilerContext> {
  private static final Logger LOG = Logger.getLogger(AndroidBuildFactory.class.getName());
  private static final boolean USE_D8;

  static {
    double version = 1.8;
    try {
      version = Double.parseDouble(System.getProperty("java.specification.version"));
    } catch (NumberFormatException e) {
      // In theory this shouldn't happen, but we will assume Java 1.8 or earlier if it does
      LOG.log(Level.SEVERE, "Unable to determine Java version", e);
    }
    USE_D8 = version >= 9;
  }

  private final boolean isAab;

  public static void install() {
    register(BuildType.APK_EXTENSION, new AndroidBuildFactory(false));
    register(BuildType.AAB_EXTENSION, new AndroidBuildFactory(true));
  }

  protected AndroidBuildFactory(boolean isAab) {
    super(isAab ? "aab" : "apk");
    this.isAab = isAab;
  }

  @Override
  protected void prepareAppIcon(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.prepareAppIcon(compiler);
    compiler.add(PrepareAppIcon.class);
  }

  @Override
  protected void prepareMetadata(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.prepareMetadata(compiler);
    compiler.add(XmlConfig.class);
    compiler.add(CreateManifest.class);
  }

  @Override
  protected void attachLibraries(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.attachLibraries(compiler);
    compiler.add(AttachNativeLibs.class);
    compiler.add(AttachAarLibs.class);
    compiler.add(AttachCompAssets.class);
  }

  @Override
  protected void processAssets(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.processAssets(compiler);
    compiler.add(MergeResources.class);
    compiler.add(SetupLibs.class);
    compiler.add(isAab ? RunAapt2.class : RunAapt.class);
  }

  @Override
  protected void compileSources(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.compileSources(compiler);
    compiler.add(GenerateClasses.class);
    compiler.add(USE_D8 ? RunD8.class : RunMultidex.class);
  }

  @Override
  protected void createAppPackage(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.createAppPackage(compiler);
    compiler.add(isAab ? RunBundletool.class : RunApkBuilder.class);
  }

  @Override
  protected void signApp(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.signApp(compiler);
    if (!isAab) {
      compiler.add(RunZipAlign.class);
      compiler.add(RunApkSigner.class);
    }
  }

  @Override
  protected void createOutputBundle(Compiler<AndroidPaths, AndroidCompilerContext> compiler) {
    super.createOutputBundle(compiler);
  }

  @Override
  public Class<AndroidCompilerContext> getContextClass() {
    return AndroidCompilerContext.class;
  }
}
