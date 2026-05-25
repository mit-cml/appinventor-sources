// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;

import com.google.appinventor.server.storage.StorageIo;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builds the current screen context: component tree and blocks YAIL.
 *
 * <p>For the current screen, uses the client-provided
 * {@code screenComponentsJson} so unsaved designer changes are reflected.
 *
 * <p>Also provides {@link #buildScreenState} for the read-only
 * {@code lookup_screen} tool, which still reads from StorageIo.
 */
public class ScreenModule extends ContextModule {

  @Override
  public String build(ContextParams params) {
    String screenName = params.getScreenName();
    String blocksYail = params.getBlocksYail();
    String screenComponentsJson = params.getScreenComponentsJson();
    String blockWarnings = params.getBlockWarnings();

    StringBuilder screen = new StringBuilder();
    screen.append("[Current screen state — supersedes any previous screen state]\n\n");
    screen.append("## Current Screen: ").append(screenName).append("\n\n");
    screen.append(buildCurrentScreenState(screenName, blocksYail,
        screenComponentsJson, blockWarnings));
    return screen.toString();
  }

  /**
   * Build the state of a single screen for a {@code lookup_screen} tool
   * response. Reads from StorageIo (server-saved data).
   */
  public String buildScreenState(String userId, long projectId, String screenName,
      StorageIo storageIo) {
    StringBuilder sb = new StringBuilder();
    String packagePath = ProjectFiles.getPackagePath(userId, projectId, storageIo);
    if (packagePath == null) {
      sb.append("(Unable to determine package path)\n");
      return sb.toString();
    }

    String scmFileId = packagePath + "/" + screenName + FORM_PROPERTIES_EXTENSION;

    sb.append("### Component Tree\n\n");
    try {
      String scmContent = storageIo.downloadFile(userId, projectId, scmFileId, "UTF-8");
      String scmJson = ContextUtils.extractScmJson(scmContent);
      if (scmJson != null) {
        sb.append(ContextUtils.buildComponentTree(new JSONObject(scmJson), 0));
      } else {
        sb.append("(empty screen)\n");
      }
    } catch (Exception e) {
      sb.append("(unable to read screen properties)\n");
    }

    sb.append("\n### Blocks (YAIL)\n\n");
    sb.append("(no blocks YAIL available from server — use client-provided YAIL)\n");

    return sb.toString();
  }

  private String buildCurrentScreenState(String screenName, String blocksYail,
      String screenComponentsJson, String blockWarnings) {
    StringBuilder sb = new StringBuilder();

    sb.append("#### Component Tree\n\n");
    if (screenComponentsJson != null && !screenComponentsJson.isEmpty()) {
      try {
        JSONObject props = new JSONObject(screenComponentsJson);
        sb.append(ContextUtils.buildComponentTree(props, 0));
      } catch (Exception e) {
        sb.append("(unable to parse screen components JSON)\n");
      }
    } else {
      sb.append("(screen component tree unavailable)\n");
    }

    sb.append("\n#### Blocks (YAIL)\n\n");
    if (blocksYail != null && !blocksYail.trim().isEmpty()) {
      sb.append("```scheme\n");
      sb.append(blocksYail);
      sb.append("\n```\n");
    } else {
      sb.append("(no blocks)\n");
    }

    // Block warnings and errors section
    String warningsSection = formatBlockWarnings(blockWarnings);
    if (warningsSection != null) {
      sb.append("\n").append(warningsSection);
    }

    return sb.toString();
  }

  /**
   * Parses the block warnings JSON and formats it as human-readable text.
   * Returns null if there are no warnings or errors.
   */
  private String formatBlockWarnings(String blockWarningsJson) {
    if (blockWarningsJson == null || blockWarningsJson.isEmpty()) {
      return null;
    }
    try {
      JSONObject json = new JSONObject(blockWarningsJson);
      int errorCount = json.optInt("errorCount", 0);
      int warningCount = json.optInt("warningCount", 0);
      if (errorCount == 0 && warningCount == 0) {
        return null;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("#### Block Warnings and Errors\n\n");

      if (errorCount > 0) {
        JSONArray errors = json.optJSONArray("errors");
        sb.append("Errors (").append(errorCount).append("):\n");
        if (errors != null) {
          for (int i = 0; i < errors.length(); i++) {
            JSONObject entry = errors.getJSONObject(i);
            sb.append("- ").append(entry.optString("block", "unknown block"))
                .append(": ").append(entry.optString("message", "")).append("\n");
          }
        }
        sb.append("\n");
      }

      if (warningCount > 0) {
        JSONArray warnings = json.optJSONArray("warnings");
        sb.append("Warnings (").append(warningCount).append("):\n");
        if (warnings != null) {
          for (int i = 0; i < warnings.length(); i++) {
            JSONObject entry = warnings.getJSONObject(i);
            sb.append("- ").append(entry.optString("block", "unknown block"))
                .append(": ").append(entry.optString("message", "")).append("\n");
          }
        }
        sb.append("\n");
      }

      return sb.toString();
    } catch (Exception e) {
      return null;
    }
  }
}
