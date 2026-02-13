// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.ContextUtils;
import com.google.appinventor.server.aiagent.context.ProjectFiles;
import com.google.appinventor.server.aiagent.llm.ReadOnlyToolException;
import com.google.appinventor.server.aiagent.llm.ReadOnlyToolResolver;
import com.google.appinventor.server.storage.StorageIo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Logger;

/**
 * Resolves read-only tool calls (component lookups, screen lookups) during
 * LLM interactions.
 */
public class AIToolResolver {

  private static final Logger LOG = Logger.getLogger(AIToolResolver.class.getName());

  private static final String COMPONENT_DB_RESOURCE =
      "/com/google/appinventor/simple_components.json";

  // Cached component database JSON
  private static volatile String componentDbJson;

  private final AIContextBuilder contextBuilder;

  public AIToolResolver(AIContextBuilder contextBuilder) {
    this.contextBuilder = contextBuilder;
  }

  /**
   * Creates a {@link ReadOnlyToolResolver} bound to the given user and project.
   */
  public ReadOnlyToolResolver createResolver(String userId, long projectId) {
    return new ReadOnlyToolResolver() {
      @Override
      public boolean isReadOnly(String toolName) {
        return AIToolNames.LOOKUP_COMPONENT.equals(toolName)
            || AIToolNames.LOOKUP_SCREEN.equals(toolName);
      }

      @Override
      public String resolve(String toolName, String argsJson) throws ReadOnlyToolException {
        try {
          JSONObject args = new JSONObject(argsJson);
          switch (toolName) {
            case AIToolNames.LOOKUP_COMPONENT:
              return resolveLookupComponent(userId, projectId,
                  args.optString("component_type", ""));
            case AIToolNames.LOOKUP_SCREEN:
              return resolveLookupScreen(userId, projectId,
                  args.optString("screen_name", ""));
            default:
              throw new ReadOnlyToolException("Unknown read-only tool: " + toolName);
          }
        } catch (ReadOnlyToolException e) {
          throw e;
        } catch (Exception e) {
          throw new ReadOnlyToolException("Failed to resolve tool " + toolName
              + ": " + e.getMessage(), e);
        }
      }
    };
  }

  private String resolveLookupComponent(String userId, long projectId, String componentType)
      throws ReadOnlyToolException {
    if (componentType == null || componentType.isEmpty()) {
      throw new ReadOnlyToolException("component_type is required");
    }
    // Search built-in components first
    String db = getComponentDb();
    try {
      JSONArray components = new JSONArray(db);
      for (int i = 0; i < components.length(); i++) {
        JSONObject comp = components.getJSONObject(i);
        if (componentType.equals(comp.optString("name"))
            || componentType.equals(comp.optString("type"))) {
          return comp.toString(2);
        }
      }
    } catch (Exception e) {
      throw new ReadOnlyToolException("Failed to search component database: " + e.getMessage());
    }
    // Fallback: search extension components
    JSONObject extResult = searchExtensionComponents(userId, projectId, componentType);
    if (extResult != null) {
      return extResult.toString(2);
    }
    throw new ReadOnlyToolException("Component not found: " + componentType);
  }

  private JSONObject searchExtensionComponents(String userId, long projectId,
      String componentType) {
    JSONArray extComponents = getExtensionComponents(userId, projectId);
    for (int i = 0; i < extComponents.length(); i++) {
      JSONObject comp = extComponents.getJSONObject(i);
      if (componentType.equals(comp.optString("name"))
          || componentType.equals(comp.optString("type"))) {
        return comp;
      }
    }
    return null;
  }

  private JSONArray getExtensionComponents(String userId, long projectId) {
    JSONArray merged = new JSONArray();
    try {
      StorageIo storageIo = contextBuilder.getStorageIo();
      List<String> packages = ProjectFiles.listExtensions(userId, projectId, storageIo);
      for (String pkg : packages) {
        try {
          String filePath = "assets/external_comps/" + pkg + "/components.json";
          String content = storageIo.downloadFile(userId, projectId, filePath, "UTF-8");
          JSONArray arr = new JSONArray(content);
          for (int i = 0; i < arr.length(); i++) {
            merged.put(arr.getJSONObject(i));
          }
        } catch (Exception e) {
          LOG.warning("Failed to load extension " + pkg + ": " + e.getMessage());
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list extensions: " + e.getMessage());
    }
    return merged;
  }

  private String resolveLookupScreen(String userId, long projectId, String screenName)
      throws ReadOnlyToolException {
    if (screenName == null || screenName.isEmpty()) {
      throw new ReadOnlyToolException("screen_name is required");
    }
    try {
      return contextBuilder.buildScreenState(userId, projectId, screenName);
    } catch (Exception e) {
      throw new ReadOnlyToolException("Failed to look up screen " + screenName
          + ": " + e.getMessage());
    }
  }

  private static String getComponentDb() {
    if (componentDbJson == null) {
      String content = ContextUtils.loadResource(COMPONENT_DB_RESOURCE);
      componentDbJson = content.startsWith("(resource") ? "[]" : content;
    }
    return componentDbJson;
  }
}
