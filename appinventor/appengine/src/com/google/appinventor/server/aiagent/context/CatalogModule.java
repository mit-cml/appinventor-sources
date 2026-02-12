// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.components.common.ComponentCategory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Builds a compact component catalog from {@code simple_components.json}.
 * Cached on first call.
 */
public class CatalogModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(CatalogModule.class.getName());

  private static volatile String cached;

  @Override
  public String build(ContextParams params) {
    if (cached == null) {
      cached = buildCompactCatalog();
    }
    return cached;
  }

  private static String buildCompactCatalog() {
    String json = ContextUtils.loadResource("/com/google/appinventor/simple_components.json");
    if (json.startsWith("(resource")) {
      LOG.warning("Could not load simple_components.json for catalog");
      return json;
    }

    JSONArray components = new JSONArray(json);
    Map<String, List<String>> byCategory = new TreeMap<>();

    for (int i = 0; i < components.length(); i++) {
      JSONObject comp = components.getJSONObject(i);

      String name = comp.optString("name", "");
      String categoryString = comp.optString("categoryString", "");
      boolean showOnPalette = "true".equals(comp.optString("showOnPalette", "false"));
      boolean nonVisible = "true".equals(comp.optString("nonVisible", "false"));
      String helpString = comp.optString("helpString", "");

      if ("Form".equals(name) || "INTERNAL".equals(categoryString) || !showOnPalette) {
        continue;
      }

      String displayCategory;
      try {
        displayCategory = ComponentCategory.valueOf(categoryString).getName();
      } catch (IllegalArgumentException e) {
        displayCategory = categoryString;
      }

      String desc = ContextUtils.stripHtml(helpString);
      desc = ContextUtils.truncateDescription(desc);

      String entry = "- **" + name + "**";
      if (nonVisible) {
        entry += " (non-visible)";
      }
      if (!desc.isEmpty()) {
        entry += ": " + desc;
      }

      byCategory.computeIfAbsent(displayCategory, k -> new ArrayList<>()).add(entry);
    }

    StringBuilder sb = new StringBuilder();
    sb.append("The following component types are available. Use `lookup_component` to get\n");
    sb.append("full details (properties, events, methods) before using a component.\n");

    for (Map.Entry<String, List<String>> entry : byCategory.entrySet()) {
      sb.append("\n### ").append(entry.getKey()).append("\n");
      for (String line : entry.getValue()) {
        sb.append(line).append("\n");
      }
    }

    return sb.toString();
  }
}
