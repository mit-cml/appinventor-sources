// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Builds the project overview context: metadata, screen list, assets,
 * extensions, and other-screen summaries (in ProjectEditor mode).
 *
 * <p>Reads from the client-provided {@code projectSnapshot} JSON rather
 * than StorageIo, so unsaved changes are reflected.
 */
public class ProjectModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(ProjectModule.class.getName());

  @Override
  public String build(ContextParams params) {
    String snapshotJson = params.getProjectSnapshot();
    String screenName = params.getScreenName();
    String mode = params.getMode();

    if (snapshotJson == null || snapshotJson.isEmpty()) {
      return buildFallback();
    }

    try {
      JSONObject snapshot = new JSONObject(snapshotJson);
      return buildFromSnapshot(snapshot, screenName, mode);
    } catch (Exception e) {
      LOG.warning("Failed to parse projectSnapshot: " + e.getMessage());
      return buildFallback();
    }
  }

  private String buildFromSnapshot(JSONObject snapshot, String screenName, String mode) {
    StringBuilder project = new StringBuilder();
    project.append("[Current project state — supersedes any previous project state]\n\n");
    project.append("## Project State\n\n");
    project.append(buildProjectOverview(snapshot)).append("\n");

    // Screen names
    JSONArray screenNamesArr = snapshot.optJSONArray("screenNames");
    List<String> screenNames = new ArrayList<>();
    if (screenNamesArr != null) {
      for (int i = 0; i < screenNamesArr.length(); i++) {
        screenNames.add(screenNamesArr.getString(i));
      }
    }
    project.append("### Screens: ").append(String.join(", ", screenNames)).append("\n\n");

    // Assets
    JSONArray assetsArr = snapshot.optJSONArray("assets");
    if (assetsArr != null && assetsArr.length() > 0) {
      List<String> assets = new ArrayList<>();
      for (int i = 0; i < assetsArr.length(); i++) {
        assets.add(assetsArr.getString(i));
      }
      project.append("### Assets: ").append(String.join(", ", assets)).append("\n\n");
    }

    // Extensions
    JSONArray extensionsArr = snapshot.optJSONArray("extensions");
    if (extensionsArr != null && extensionsArr.length() > 0) {
      List<String> extensions = new ArrayList<>();
      for (int i = 0; i < extensionsArr.length(); i++) {
        extensions.add(extensionsArr.getString(i));
      }
      project.append("### Extensions: ").append(String.join(", ", extensions)).append("\n\n");
    }

    // Screen summaries (ProjectEditor mode only)
    if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)) {
      JSONObject summaries = snapshot.optJSONObject("screenSummaries");
      if (summaries != null) {
        Iterator<String> keys = summaries.keys();
        while (keys.hasNext()) {
          String other = keys.next();
          if (!other.equals(screenName)) {
            project.append("### Screen: ").append(other).append(" (summary)\n");
            JSONObject summary = summaries.optJSONObject(other);
            if (summary != null) {
              int componentCount = summary.optInt("componentCount", 0);
              String title = summary.optString("title", other);
              project.append("Components: ").append(componentCount);
              project.append(", Title: \"").append(title).append("\"");
            }
            project.append("\n");
          }
        }
      }
    }

    return project.toString();
  }

  private String buildProjectOverview(JSONObject snapshot) {
    StringBuilder sb = new StringBuilder();
    sb.append("### Project Overview\n");

    String projectName = snapshot.optString("projectName", "Unknown");
    sb.append("- Name: ").append(projectName).append("\n");

    String appName = snapshot.optString("appName", projectName);
    sb.append("- App Name: ").append(appName).append("\n");

    String versionName = snapshot.optString("versionName", "");
    if (!versionName.isEmpty()) {
      sb.append("- Version: ").append(versionName).append("\n");
    }

    String theme = snapshot.optString("theme", "");
    if (!theme.isEmpty()) {
      sb.append("- Theme: ").append(theme).append("\n");
    }

    String sizing = snapshot.optString("sizing", "");
    if (!sizing.isEmpty()) {
      sb.append("- Sizing: ").append(sizing).append("\n");
    }

    String primaryColor = snapshot.optString("primaryColor", "");
    if (!primaryColor.isEmpty()) {
      sb.append("- Primary Color: ").append(primaryColor).append("\n");
    }

    String accentColor = snapshot.optString("accentColor", "");
    if (!accentColor.isEmpty()) {
      sb.append("- Accent Color: ").append(accentColor).append("\n");
    }

    return sb.toString();
  }

  private String buildFallback() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Current project state — supersedes any previous project state]\n\n");
    sb.append("## Project State\n\n");
    sb.append("### Project Overview\n");
    sb.append("- (project metadata unavailable)\n");
    return sb.toString();
  }
}
