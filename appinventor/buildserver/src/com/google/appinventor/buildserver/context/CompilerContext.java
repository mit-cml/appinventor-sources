// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.Reporter;
import com.google.appinventor.buildserver.stats.StatReporter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;

public class CompilerContext<P extends Paths> {
  Project project;
  String ext;
  Set<String> compTypes;
  Map<String, Set<String>> compBlocks;
  /**
   * Maps Screen names to their orientation values to populate the
   * android:screenOrientation attribution in the &lt;activity&gt; element.
   */
  Map<String, String> formOrientations;
  Set<String> blockPermissions;
  Reporter reporter;
  StatReporter statReporter;
  boolean isForCompanion;
  boolean isForEmulator;
  boolean includeDangerousPermissions;
  String keystoreFilePath;
  int childProcessRam;
  String dexCacheDir;
  String outputFileName;

  JSONArray simpleCompsBuildInfo;
  JSONArray extCompsBuildInfo;
  JSONArray buildInfo;
  Set<String> simpleCompTypes;  // types needed by the project
  Set<String> extCompTypes; // types needed by the project

  Map<String, String> extTypePathCache;

  final P paths;
  Resources resources;
  ComponentInfo componentInfo;

  public static class Builder<R extends Paths, T extends CompilerContext<? extends R>> {
    private final Project project;
    private final String ext;
    private Set<String> compTypes;
    private Map<String, Set<String>> compBlocks;
    private Map<String, String> formOrientations;
    private Set<String> blockPermissions;
    private Reporter reporter;
    private StatReporter statReporter;
    private boolean isForCompanion = false;
    private boolean isForEmulator = false;
    private boolean includeDangerousPermissions = false;
    private String keystoreFilePath;
    private int childProcessRam = 2048;
    private String dexCacheDir = null;
    private String outputFileName = null;

    private Class<? extends T> clazz;

    public Builder(Project project, String ext) {
      this.project = project;
      this.ext = ext;
    }

    public Builder<R, T> withTypes(Set<String> compTypes) {
      this.compTypes = compTypes;
      return this;
    }

    public Builder<R, T> withBlocks(Map<String, Set<String>> compBlocks) {
      this.compBlocks = compBlocks;
      return this;
    }

    public Builder<R, T> withBlockPermissions(Set<String> permissions) {
      this.blockPermissions = permissions;
      return this;
    }

    public Builder<R, T> withFormOrientations(Map<String, String> formOrientations) {
      this.formOrientations = formOrientations;
      return this;
    }

    public Builder<R, T> withReporter(Reporter reporter) {
      this.reporter = reporter;
      return this;
    }

    public Builder<R, T> withStatReporter(StatReporter statReporter) {
      this.statReporter = statReporter;
      return this;
    }

    public Builder<R, T> withCompanion(boolean isForCompanion) {
      this.isForCompanion = isForCompanion;
      return this;
    }

    public Builder<R, T> withEmulator(boolean isForEmulator) {
      this.isForEmulator = isForEmulator;
      return this;
    }

    public Builder<R, T> withDangerousPermissions(boolean includeDangerousPermissions) {
      this.includeDangerousPermissions = includeDangerousPermissions;
      return this;
    }

    public Builder<R, T> withKeystore(String keystoreFilePath) {
      this.keystoreFilePath = keystoreFilePath;
      return this;
    }

    public Builder<R, T> withRam(int childProcessRam) {
      this.childProcessRam = childProcessRam;
      return this;
    }

    public Builder<R, T> withCache(String dexCacheDir) {
      this.dexCacheDir = dexCacheDir;
      return this;
    }

    public Builder<R, T> withOutput(String outputFileName) {
      this.outputFileName = outputFileName;
      return this;
    }

    public <E extends T> Builder<R, T> withClass(Class<E> clazz) {
      this.clazz = clazz;
      return this;
    }

    /**
     * Construct a new CompilerContext using the builder's configuration.
     *
     * @return a new CompilerContext
     */
    public T build() {
      T context;
      try {
        context = this.clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      if (project == null) {
        System.out.println("[WARN] CompilerContext.Builder needs a Project");
      } else if (compTypes == null) {
        throw new IllegalStateException("CompilerContext.Builder needs CompTypes");
      } else if (compBlocks == null) {
        throw new IllegalStateException("CompilerContext.Builder needs CompBlocks");
      } else if (reporter == null) {
        throw new IllegalStateException("CompilerContext.Builder needs a Reporter");
      } else if (keystoreFilePath == null) {
        throw new IllegalStateException("CompilerContext.Builder needs the KeystoreFilePath");
      }
      context.project = project;
      context.ext = ext;
      context.compTypes = compTypes;
      context.compBlocks = compBlocks;
      context.formOrientations = formOrientations;
      context.blockPermissions = blockPermissions;
      context.reporter = reporter;
      context.statReporter = statReporter;
      context.isForCompanion = isForCompanion;
      context.isForEmulator = isForEmulator;
      context.includeDangerousPermissions = includeDangerousPermissions;
      context.keystoreFilePath = keystoreFilePath;
      context.dexCacheDir = dexCacheDir;
      context.outputFileName = outputFileName;
      context.childProcessRam = childProcessRam;

      context.paths.setOutputFileName(outputFileName);
      if (project != null) {  // For testing only!
        context.paths.setProjectRootDir(new File(project.getProjectDir()).getParentFile());
        context.paths.mkdirs(project.getBuildDirectory());
      }

      context.resources = new Resources();
      context.componentInfo = new ComponentInfo();

      context.extTypePathCache = new HashMap<>();

      System.out.println(this);

      return context;
    }
  }

  protected CompilerContext(P paths) {
    this.paths = paths;
  }

  public Project getProject() {
    return project;
  }

  public String getExt() {
    return ext;
  }

  public Set<String> getBlockPermissions() {
    return blockPermissions;
  }

  public Set<String> getCompTypes() {
    return compTypes;
  }

  public Map<String, Set<String>> getCompBlocks() {
    return compBlocks;
  }

  public Map<String, String> getFormOrientations() {
    return formOrientations;
  }

  public Reporter getReporter() {
    return reporter;
  }

  public StatReporter getStatReporter() {
    return statReporter;
  }

  public boolean isForCompanion() {
    return isForCompanion;
  }

  public boolean isForEmulator() {
    return isForEmulator;
  }

  public boolean isIncludeDangerousPermissions() {
    return includeDangerousPermissions;
  }

  public String getKeystoreFilePath() {
    return keystoreFilePath;
  }

  public int getChildProcessRam() {
    return childProcessRam;
  }

  public String getDexCacheDir() {
    return dexCacheDir;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public JSONArray getBuildInfo() {
    return buildInfo;
  }

  public void setBuildInfo(JSONArray buildInfo) {
    this.buildInfo = buildInfo;
  }

  public JSONArray getSimpleCompsBuildInfo() {
    return simpleCompsBuildInfo;
  }

  public JSONArray getExtCompsBuildInfo() {
    return extCompsBuildInfo;
  }

  public Set<String> getSimpleCompTypes() {
    return simpleCompTypes;
  }

  public Set<String> getExtCompTypes() {
    return extCompTypes;
  }

  public P getPaths() {
    return paths;
  }

  public void setSimpleCompsBuildInfo(JSONArray simpleCompsBuildInfo) {
    this.simpleCompsBuildInfo = simpleCompsBuildInfo;
  }

  public void setExtCompsBuildInfo(JSONArray extCompsBuildInfo) {
    this.extCompsBuildInfo = extCompsBuildInfo;
  }

  public void setSimpleCompTypes(Set<String> simpleCompTypes) {
    this.simpleCompTypes = simpleCompTypes;
  }

  public void setExtCompTypes(Set<String> extCompTypes) {
    this.extCompTypes = extCompTypes;
  }

  public String getResource(String resource) {
    return resources.getResource(resource);
  }

  public ComponentInfo getComponentInfo() {
    return this.componentInfo;
  }

  public Resources getResources() {
    return resources;
  }

  public Map<String, String> getExtTypePathCache() {
    return this.extTypePathCache;
  }

  public boolean usesLegacyFileAccess() {
    return "Legacy".equals(project.getDefaultFileScope());
  }

  public boolean usesSharedFileAccess() {
    return "Shared".equals(project.getDefaultFileScope());
  }

  @Override
  public String toString() {
    return "ExecutorContext{"
        + "project=" + project
        + ", ext='" + ext + '\''
        + ", compTypes=" + compTypes
        + ", compBlocks=" + compBlocks
        + ", reporter=" + reporter
        + ", isForCompanion=" + isForCompanion
        + ", isForEmulator=" + isForEmulator
        + ", includeDangerousPermissions=" + includeDangerousPermissions
        + ", keystoreFilePath='" + keystoreFilePath + '\''
        + ", childProcessRam=" + childProcessRam
        + ", dexCacheDir='" + dexCacheDir + '\''
        + ", outputFileName='" + outputFileName + '\''
        + ", simpleCompsBuildInfo=" + simpleCompsBuildInfo
        + ", extCompsBuildInfo=" + extCompsBuildInfo
        + ", simpleCompTypes=" + simpleCompTypes
        + ", extCompTypes=" + extCompTypes
        + ", extTypePathCache=" + extTypePathCache
        + ", paths=" + paths
        + ", resources=" + resources
        + '}';
  }
}

