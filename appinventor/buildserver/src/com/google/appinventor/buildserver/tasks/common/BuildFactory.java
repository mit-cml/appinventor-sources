// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.common;

import com.google.appinventor.buildserver.Compiler;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.context.Paths;

import java.util.HashMap;
import java.util.Map;

public abstract class BuildFactory<P extends Paths, T extends CompilerContext<P>> {
  private static final Map<String, BuildFactory<?, ? extends CompilerContext<?>>> FACTORIES
      = new HashMap<>();
  private final String extension;

  protected static void register(String extension, BuildFactory<?, ?> factory) {
    FACTORIES.put(extension, factory);
  }

  @SuppressWarnings("unchecked")
  public static <P extends Paths, T extends CompilerContext<P>> BuildFactory<P, T> get(
      String extension) {
    return (BuildFactory<P, T>) FACTORIES.get(extension);
  }

  protected BuildFactory(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  /**
   * Creates a new Compiler that builds apps in accordance with this factory's target platform.
   *
   * @param context the context under which the compiler will run
   * @return a new compiler
   */
  public final Compiler<P, T> makeCompiler(T context) {
    Compiler<P, T> compiler = new Compiler.Builder<P, T>()
        .withContext(context)
        .withType(extension)
        .build();
    prepareBuild(compiler);
    prepareAppIcon(compiler);
    prepareMetadata(compiler);
    attachLibraries(compiler);
    processAssets(compiler);
    compileSources(compiler);
    createAppPackage(compiler);
    signApp(compiler);
    createOutputBundle(compiler);
    return compiler;
  }

  protected void prepareBuild(Compiler<P, T> compiler) {
    compiler.add(ReadBuildInfo.class);
    compiler.add(LoadComponentInfo.class);
  }

  protected void prepareAppIcon(Compiler<P, T> compiler) {
  }

  protected void prepareMetadata(Compiler<P, T> compiler) {
  }

  protected void attachLibraries(Compiler<P, T> compiler) {
  }

  protected void processAssets(Compiler<P, T> compiler) {
  }

  protected void compileSources(Compiler<P, T> compiler) {
  }

  protected void createAppPackage(Compiler<P, T> compiler) {
  }

  protected void signApp(Compiler<P, T> compiler) {
  }

  protected void createOutputBundle(Compiler<P, T> compiler) {
  }

  public abstract Class<T> getContextClass();
}
