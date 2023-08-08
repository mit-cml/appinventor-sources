// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.buildserver.context.ComponentInfo;
import com.google.appinventor.buildserver.context.Paths;
import com.google.appinventor.buildserver.context.Resources;
import com.google.appinventor.buildserver.stats.StatReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;

public class CompilerContext {
  private Project project;
  private String ext;
  private Set<String> compTypes;
  private Map<String, Set<String>> compBlocks;
  /**
   * Maps Screen names to their orientation values to populate the
   * android:screenOrientation attribution in the &lt;activity&gt; element.
   */
  private Map<String, String> formOrientations;
  private Set<String> blockPermissions;
  private Reporter reporter;
  private StatReporter statReporter;
  private boolean isForCompanion;
  private boolean isForEmulator;
  private boolean includeDangerousPermissions;
  private String keystoreFilePath;
  private int childProcessRam;
  private String dexCacheDir;
  private String outputFileName;

  private JSONArray simpleCompsBuildInfo;
  private JSONArray extCompsBuildInfo;
  private JSONArray buildInfo;
  private Set<String> simpleCompTypes;  // types needed by the project
  private Set<String> extCompTypes; // types needed by the project

  private Map<String, String> extTypePathCache;

  private Paths paths;
  private Resources resources;
  private ComponentInfo componentInfo;

  public static class Builder {
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

    public Builder(Project project, String ext) {
      this.project = project;
      this.ext = ext;
    }

    public Builder withTypes(Set<String> compTypes) {
      this.compTypes = compTypes;
      return this;
    }

    public Builder withBlocks(Map<String, Set<String>> compBlocks) {
      this.compBlocks = compBlocks;
      return this;
    }

    public Builder withBlockPermissions(Set<String> permissions) {
      this.blockPermissions = permissions;
      return this;
    }

    public Builder withFormOrientations(Map<String, String> formOrientations) {
      this.formOrientations = formOrientations;
      return this;
    }

    public Builder withReporter(Reporter reporter) {
      this.reporter = reporter;
      return this;
    }

    public Builder withStatReporter(StatReporter statReporter) {
      this.statReporter = statReporter;
      return this;
    }

    public Builder withCompanion(boolean isForCompanion) {
      this.isForCompanion = isForCompanion;
      return this;
    }

    public Builder withEmulator(boolean isForEmulator) {
      this.isForEmulator = isForEmulator;
      return this;
    }

    public Builder withDangerousPermissions(boolean includeDangerousPermissions) {
      this.includeDangerousPermissions = includeDangerousPermissions;
      return this;
    }

    public Builder withKeystore(String keystoreFilePath) {
      this.keystoreFilePath = keystoreFilePath;
      return this;
    }

    public Builder withRam(int childProcessRam) {
      this.childProcessRam = childProcessRam;
      return this;
    }

    public Builder withCache(String dexCacheDir) {
      this.dexCacheDir = dexCacheDir;
      return this;
    }

    public Builder withOutput(String outputFileName) {
      this.outputFileName = outputFileName;
      return this;
    }

    /**
     * Construct a new CompilerContext using the builder's configuration.
     *
     * @return a new CompilerContext
     */
    public CompilerContext build() {
      CompilerContext context = new CompilerContext();
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

      final Paths paths;
      if (this.outputFileName != null) {
        paths = new Paths(this.outputFileName);
      } else if (project != null) {
        paths = new Paths(this.project.getProjectName() + "." + ext);
      } else {
        // For testing only
        paths = new Paths(null);
      }
      if (project != null) {
        paths.setBuildDir(ExecutorUtils.createDir(project.getBuildDirectory()));
        paths.setDeployDir(ExecutorUtils.createDir(paths.getBuildDir(), "deploy"));
        paths.setResDir(ExecutorUtils.createDir(paths.getBuildDir(), "res"));
        paths.setDrawableDir(ExecutorUtils.createDir(paths.getBuildDir(), "drawable"));
        paths.setTmpDir(ExecutorUtils.createDir(paths.getBuildDir(), "tmp"));
        paths.setLibsDir(ExecutorUtils.createDir(paths.getBuildDir(), "libs"));
        paths.setClassesDir(ExecutorUtils.createDir(paths.getBuildDir(), "classes"));
      }
      context.paths = paths;

      context.resources = new Resources();
      context.componentInfo = new ComponentInfo();

      context.extTypePathCache = new HashMap<>();

      System.out.println(this);

      return context;
    }
  }

  private CompilerContext() {
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

  public Paths getPaths() {
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

