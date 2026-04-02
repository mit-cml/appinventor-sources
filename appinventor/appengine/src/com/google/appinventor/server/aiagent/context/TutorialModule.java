// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.aiagent.TutorialContentCache;

import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Builds tutorial context for the LLM when the project has an active
 * tutorial (TutorialURL is set). Loads pedagogical instructions from
 * {@code tutorial_instructions.md} and appends the fetched tutorial
 * page content.
 */
public class TutorialModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(TutorialModule.class.getName());

  private static volatile String cachedInstructions;

  private final TutorialContentCache cache;

  public TutorialModule(TutorialContentCache cache) {
    this.cache = cache;
  }

  @Override
  public String build(ContextParams params) {
    String snapshotJson = params.getProjectSnapshot();
    if (snapshotJson == null || snapshotJson.isEmpty()) {
      return null;
    }

    String url;
    try {
      JSONObject snapshot = new JSONObject(snapshotJson);
      url = snapshot.optString("tutorialURL", "");
    } catch (Exception e) {
      LOG.warning("Failed to parse projectSnapshot for tutorialURL: " + e.getMessage());
      return null;
    }

    if (url.isEmpty()) {
      return null;
    }

    String content = cache.get(url);
    if (content == null) {
      return null;
    }

    if (cachedInstructions == null) {
      cachedInstructions = ContextUtils.loadResource("tutorial_instructions.md");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[Active Tutorial Context]\n\n");
    sb.append("## Active Tutorial\n\n");
    sb.append(cachedInstructions).append("\n");
    sb.append("### Tutorial Content\n\n");
    sb.append(content);
    return sb.toString();
  }
}
