// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.storage.StorageIo;

import org.json.JSONObject;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Builds the project overview context: metadata, screen list, assets,
 * extensions, and other-screen summaries (in ProjectEditor mode).
 */
public class ProjectModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(ProjectModule.class.getName());

  @Override
  public String build(ContextParams params) {
    StorageIo storageIo = params.getStorageIo();
    String userId = params.getUserId();
    long projectId = params.getProjectId();
    String screenName = params.getScreenName();
    String mode = params.getMode();
    String packagePath = ProjectFiles.getPackagePath(userId, projectId, storageIo);
    List<String> screenNames = ProjectFiles.listScreenNames(userId, projectId,
        packagePath, storageIo);

    StringBuilder project = new StringBuilder();
    project.append("[Current project state — supersedes any previous project state]\n\n");
    project.append("## Project State\n\n");
    project.append(buildProjectOverview(userId, projectId, storageIo)).append("\n");
    project.append("### Screens: ").append(String.join(", ", screenNames)).append("\n\n");

    List<String> assets = ProjectFiles.listAssets(userId, projectId, storageIo);
    if (!assets.isEmpty()) {
      project.append("### Assets: ").append(String.join(", ", assets)).append("\n\n");
    }

    List<String> extensions = ProjectFiles.listExtensions(userId, projectId, storageIo);
    if (!extensions.isEmpty()) {
      project.append("### Extensions: ").append(String.join(", ", extensions)).append("\n\n");
    }

    if ("ProjectEditor".equals(mode)) {
      for (String other : screenNames) {
        if (!other.equals(screenName)) {
          project.append("### Screen: ").append(other).append(" (summary)\n");
          project.append(buildScreenSummary(userId, projectId, other, packagePath, storageIo));
        }
      }
    }

    return project.toString();
  }

  private String buildProjectOverview(String userId, long projectId, StorageIo storageIo) {
    StringBuilder sb = new StringBuilder();
    try {
      String propsContent = storageIo.downloadFile(
          userId, projectId, ProjectFiles.PROJECT_PROPERTIES_FILE, "UTF-8");
      Properties props = new Properties();
      props.load(new java.io.StringReader(propsContent));

      String projectName = storageIo.getProjectName(userId, projectId);
      sb.append("### Project Overview\n");
      sb.append("- Name: ").append(projectName).append("\n");

      String appName = props.getProperty("aname", projectName);
      sb.append("- App Name: ").append(appName).append("\n");

      String versionName = props.getProperty("versionname", "");
      if (!versionName.isEmpty()) {
        sb.append("- Version: ").append(versionName).append("\n");
      }

      String theme = props.getProperty("theme", "");
      if (!theme.isEmpty()) {
        sb.append("- Theme: ").append(theme).append("\n");
      }

      String sizing = props.getProperty("sizing", "");
      if (!sizing.isEmpty()) {
        sb.append("- Sizing: ").append(sizing).append("\n");
      }

      String primaryColor = props.getProperty("color.primary", "");
      if (!primaryColor.isEmpty()) {
        sb.append("- Primary Color: ").append(primaryColor).append("\n");
      }

      String accentColor = props.getProperty("color.accent", "");
      if (!accentColor.isEmpty()) {
        sb.append("- Accent Color: ").append(accentColor).append("\n");
      }
    } catch (Exception e) {
      sb.append("### Project Overview\n");
      sb.append("- Name: ").append(storageIo.getProjectName(userId, projectId)).append("\n");
    }
    return sb.toString();
  }

  private String buildScreenSummary(String userId, long projectId,
      String screenName, String packagePath, StorageIo storageIo) {
    StringBuilder sb = new StringBuilder();
    if (packagePath == null) {
      sb.append("(unknown)\n");
      return sb.toString();
    }

    String scmFileId = packagePath + "/" + screenName
        + com.google.appinventor.common.constants.YoungAndroidStructureConstants
            .FORM_PROPERTIES_EXTENSION;
    try {
      String scmContent = storageIo.downloadFile(userId, projectId, scmFileId, "UTF-8");
      String scmJson = ContextUtils.extractScmJson(scmContent);
      if (scmJson != null) {
        JSONObject props = new JSONObject(scmJson).optJSONObject("Properties");
        if (props != null) {
          int componentCount = ContextUtils.countComponents(props);
          sb.append("Components: ").append(componentCount);
          sb.append(", Title: \"").append(props.optString("Title", screenName)).append("\"");
        }
      }
    } catch (Exception e) {
      sb.append("(unable to read)");
    }
    sb.append("\n");
    return sb.toString();
  }
}
