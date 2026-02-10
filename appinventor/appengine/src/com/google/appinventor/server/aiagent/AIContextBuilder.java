// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.server.aiagent.llm.LLMTool;
import com.google.appinventor.server.storage.StorageIo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Builds the tiered LLM prompt (Layers 1-5) for the AI agent.
 *
 * <ul>
 *   <li>Layer 1: Static system prompt from appinventor_reference.md
 *   <li>Layer 2: Compact component catalog from simple_components.json
 *   <li>Layer 3: On-demand lookup tool definitions (mode-filtered)
 *   <li>Layer 4: Current app state (per-request, from project files)
 *   <li>Layer 5: Few-shot examples from few_shot_examples.json
 * </ul>
 *
 * <p>Static content (Layers 1, 2, 5) is cached on first use. Layer 4 is
 * built fresh per-request from the project state in storage.
 */
public class AIContextBuilder {

  private static final Logger LOG = Logger.getLogger(AIContextBuilder.class.getName());

  private static final String RESOURCE_BASE =
      "/com/google/appinventor/server/aiagent/resources/";
  private static final String EXTERNAL_COMPS_FOLDER = ASSETS_FOLDER + "/external_comps";
  private static final String PROJECT_DIRECTORY = "youngandroidproject";
  private static final String PROJECT_PROPERTIES_FILE =
      PROJECT_DIRECTORY + "/project.properties";

  // Cached static content (thread-safe: immutable once initialized)
  private static volatile String cachedReference;
  private static volatile String cachedCatalog;
  private static volatile String cachedExamples;
  private static volatile String cachedYailGrammar;

  private final StorageIo storageIo;

  public AIContextBuilder(StorageIo storageIo) {
    this.storageIo = storageIo;
  }

  // ---------- Public API ----------

  /**
   * Build the complete system prompt for an LLM request.
   *
   * @param userId     the authenticated user
   * @param projectId  the project being edited
   * @param screenName the currently active screen
   * @param mode       "Advisor", "ScreenEditor", or "ProjectEditor"
   * @param blocksYail client-generated YAIL for the current screen's blocks
   *                   (may be null or empty if unavailable)
   * @return the assembled system prompt string
   */
  public String build(String userId, long projectId, String screenName, String mode,
      String blocksYail) {
    StringBuilder sb = new StringBuilder();

    // Layer 1: Static reference
    String reference = getReference();
    sb.append(reference).append("\n\n");
    AIDebug.log(LOG, "Context Layer 1 (reference): " + reference.length() + " chars");

    // Layer 2: Component catalog
    String catalog = getCatalog();
    sb.append("## Component Catalog\n\n");
    sb.append(catalog).append("\n\n");
    AIDebug.log(LOG, "Context Layer 2 (catalog): " + catalog.length() + " chars");

    // YAIL grammar reference
    String grammar = getYailGrammar();
    sb.append("## YAIL Grammar\n\n");
    sb.append(grammar).append("\n\n");
    AIDebug.log(LOG, "Context (YAIL grammar): " + grammar.length() + " chars");

    // Layer 4: Current app state (per-request)
    String projectState = buildProjectState(userId, projectId, screenName, mode, blocksYail);
    sb.append("## Current Project State\n\n");
    sb.append(projectState);
    sb.append("\n\n");
    AIDebug.log(LOG, "Context Layer 4 (project state): " + projectState.length() + " chars");

    // Layer 5: Few-shot examples
    String examples = getExamples();
    sb.append("## Examples\n\n");
    sb.append(examples).append("\n\n");
    AIDebug.log(LOG, "Context Layer 5 (examples): " + examples.length() + " chars");

    // Mode instructions
    sb.append(buildModeInstructions(mode));

    return sb.toString();
  }

  /**
   * Build the list of LLM tools filtered by mode.
   *
   * @param mode "Advisor", "ScreenEditor", or "ProjectEditor"
   * @return tools available in the given mode
   */
  public List<LLMTool> buildTools(String mode) {
    List<LLMTool> tools = new ArrayList<>();

    // Read-only tools available in all modes
    tools.add(new LLMTool("lookup_component",
        "Look up full metadata for a component type from the component database. "
            + "Returns all properties, events, methods, and their types.",
        "{\"type\":\"object\",\"properties\":{\"component_type\":{\"type\":\"string\","
            + "\"description\":\"The component type name, e.g. Button, Label\"}},\"required\":[\"component_type\"]}"));
    tools.add(new LLMTool("lookup_screen",
        "Look up the current state of a screen including its component tree and blocks YAIL.",
        "{\"type\":\"object\",\"properties\":{\"screen_name\":{\"type\":\"string\","
            + "\"description\":\"The screen name, e.g. Screen1\"}},\"required\":[\"screen_name\"]}"));

    if ("Advisor".equals(mode)) {
      return tools;
    }

    // Write tools for ScreenEditor and ProjectEditor
    tools.add(new LLMTool("add_component",
        "Add a new component to the current screen.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"component_type\":{\"type\":\"string\",\"description\":\"Component type, e.g. Button\"},"
            + "\"name\":{\"type\":\"string\",\"description\":\"Instance name, e.g. Button1\"},"
            + "\"parent\":{\"type\":\"string\",\"description\":\"Parent container name (default: screen root)\"},"
            + "\"properties\":{\"type\":\"object\",\"description\":\"Initial property values\"}"
            + "},\"required\":[\"component_type\",\"name\"]}"));

    tools.add(new LLMTool("delete_component",
        "Delete a component from the current screen.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"name\":{\"type\":\"string\",\"description\":\"Component instance name\"}"
            + "},\"required\":[\"name\"]}"));

    tools.add(new LLMTool("set_property",
        "Set a property value on a component.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"component_name\":{\"type\":\"string\",\"description\":\"Component instance name\"},"
            + "\"property_name\":{\"type\":\"string\",\"description\":\"Property name\"},"
            + "\"value\":{\"description\":\"Property value (type depends on property)\"}"
            + "},\"required\":[\"component_name\",\"property_name\",\"value\"]}"));

    tools.add(new LLMTool("rename_component",
        "Rename a component instance.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"old_name\":{\"type\":\"string\",\"description\":\"Current component name\"},"
            + "\"new_name\":{\"type\":\"string\",\"description\":\"New component name\"}"
            + "},\"required\":[\"old_name\",\"new_name\"]}"));

    tools.add(new LLMTool("write_block",
        "Create or replace a top-level block (event handler, global variable, or procedure) "
            + "using YAIL code. The YAIL form head identifies the block type and target. "
            + "If a block with the same identity already exists, it is replaced (upsert semantics).",
        "{\"type\":\"object\",\"properties\":{"
            + "\"yail\":{\"type\":\"string\",\"description\":\"Complete YAIL S-expression for the "
            + "top-level block. Must be one of: (define-event ComponentName EventName ...), "
            + "(define-generic-event ComponentType EventName ...), "
            + "(def g$varName initialValue), (def (p$procName $param1 ...) body) for procedures "
            + "without return, or (def-return (p$procName $param1 ...) body) for procedures with return\"}"
            + "},\"required\":[\"yail\"]}"));

    tools.add(new LLMTool("delete_block",
        "Delete a top-level block (event handler, global variable, or procedure). "
            + "The identifier format matches the YAIL form head tokens.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"block\":{\"type\":\"string\",\"description\":\"Block identifier using YAIL head "
            + "tokens, e.g. 'define-event Button1 Click', 'define-generic-event Button Click', "
            + "'def g$score', 'def p$factorial', 'def-return p$myFunc'\"}"
            + "},\"required\":[\"block\"]}"));

    // Log tool list built so far before project-level tools
    if (AIDebug.enabled()) {
      StringBuilder toolList = new StringBuilder("Tools for mode " + mode + ":");
      for (LLMTool t : tools) {
        toolList.append(" ").append(t.getName());
      }
      AIDebug.log(LOG, toolList.toString());
    }

    // Project-level tools only for ProjectEditor
    if ("ProjectEditor".equals(mode)) {
      tools.add(new LLMTool("switch_screen",
          "Switch the active screen context.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"screen_name\":{\"type\":\"string\",\"description\":\"Screen name to switch to\"}"
              + "},\"required\":[\"screen_name\"]}"));

      tools.add(new LLMTool("create_screen",
          "Create a new screen in the project.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"screen_name\":{\"type\":\"string\",\"description\":\"New screen name\"}"
              + "},\"required\":[\"screen_name\"]}"));

      tools.add(new LLMTool("delete_screen",
          "Delete a screen from the project.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"screen_name\":{\"type\":\"string\",\"description\":\"Screen name to delete\"}"
              + "},\"required\":[\"screen_name\"]}"));

      tools.add(new LLMTool("set_project_property",
          "Set a project-level property (theme, colors, sizing, etc.).",
          "{\"type\":\"object\",\"properties\":{"
              + "\"property\":{\"type\":\"string\",\"description\":\"Property name\"},"
              + "\"value\":{\"type\":\"string\",\"description\":\"Property value\"}"
              + "},\"required\":[\"property\",\"value\"]}"));
    }

    return tools;
  }

  /**
   * Build the state of a single screen for a lookup_screen tool response.
   *
   * @param userId     the authenticated user
   * @param projectId  the project ID
   * @param screenName the screen to look up
   * @return formatted screen state
   */
  public String buildScreenState(String userId, long projectId, String screenName) {
    StringBuilder sb = new StringBuilder();
    String packagePath = getPackagePath(userId, projectId);
    if (packagePath == null) {
      sb.append("(Unable to determine package path)\n");
      return sb.toString();
    }

    String scmFileId = packagePath + "/" + screenName + FORM_PROPERTIES_EXTENSION;
    String bkyFileId = packagePath + "/" + screenName + BLOCKLY_SOURCE_EXTENSION;

    // Component tree
    sb.append("### Component Tree\n\n");
    try {
      String scmContent = storageIo.downloadFile(userId, projectId, scmFileId, "UTF-8");
      String scmJson = extractScmJson(scmContent);
      if (scmJson != null) {
        sb.append(buildComponentTree(new JSONObject(scmJson), 0));
      } else {
        sb.append("(empty screen)\n");
      }
    } catch (Exception e) {
      sb.append("(unable to read screen properties)\n");
    }

    // Blocks YAIL (from server-side .bky file as fallback only)
    sb.append("\n### Blocks (YAIL)\n\n");
    sb.append("(no blocks YAIL available from server — use client-provided YAIL)\n");

    return sb.toString();
  }

  /**
   * Build the state of the current screen using client-provided blocks YAIL
   * instead of server-side conversion from .bky files.
   */
  private String buildCurrentScreenState(String userId, long projectId,
      String screenName, String packagePath, String blocksYail) {
    StringBuilder sb = new StringBuilder();

    // Component tree from SCM file
    sb.append("#### Component Tree\n\n");
    if (packagePath != null) {
      String scmFileId = packagePath + "/" + screenName + FORM_PROPERTIES_EXTENSION;
      try {
        String scmContent = storageIo.downloadFile(userId, projectId, scmFileId, "UTF-8");
        String scmJson = extractScmJson(scmContent);
        if (scmJson != null) {
          sb.append(buildComponentTree(new JSONObject(scmJson), 0));
        } else {
          sb.append("(empty screen)\n");
        }
      } catch (Exception e) {
        sb.append("(unable to read screen properties)\n");
      }
    } else {
      sb.append("(unable to determine package path)\n");
    }

    // Blocks YAIL from client
    sb.append("\n#### Blocks (YAIL)\n\n");
    if (blocksYail != null && !blocksYail.trim().isEmpty()) {
      sb.append("```scheme\n");
      sb.append(blocksYail);
      sb.append("\n```\n");
    } else {
      sb.append("(no blocks)\n");
    }

    return sb.toString();
  }

  // ---------- Layer builders ----------

  private String buildProjectState(String userId, long projectId,
      String screenName, String mode, String blocksYail) {
    StringBuilder sb = new StringBuilder();

    // Project overview from project.properties
    sb.append(buildProjectOverview(userId, projectId));
    sb.append("\n");

    // Screen list
    String packagePath = getPackagePath(userId, projectId);
    List<String> screenNames = listScreenNames(userId, projectId, packagePath);
    sb.append("### Screens: ");
    sb.append(String.join(", ", screenNames));
    sb.append("\n\n");

    // Assets list
    List<String> assets = listAssets(userId, projectId);
    if (!assets.isEmpty()) {
      sb.append("### Assets: ");
      sb.append(String.join(", ", assets));
      sb.append("\n\n");
    }

    // Extension list
    List<String> extensions = listExtensions(userId, projectId);
    if (!extensions.isEmpty()) {
      sb.append("### Extensions: ");
      sb.append(String.join(", ", extensions));
      sb.append("\n\n");
    }

    // Current screen: component tree from SCM + blocks YAIL from client
    sb.append("### Current Screen: ").append(screenName).append("\n\n");
    sb.append(buildCurrentScreenState(userId, projectId, screenName, packagePath, blocksYail));

    // Other screens: summaries
    if ("ProjectEditor".equals(mode)) {
      for (String other : screenNames) {
        if (!other.equals(screenName)) {
          sb.append("\n### Screen: ").append(other).append(" (summary)\n");
          sb.append(buildScreenSummary(userId, projectId, other, packagePath));
        }
      }
    }

    return sb.toString();
  }

  private String buildProjectOverview(String userId, long projectId) {
    StringBuilder sb = new StringBuilder();
    try {
      String propsContent = storageIo.downloadFile(
          userId, projectId, PROJECT_PROPERTIES_FILE, "UTF-8");
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

  /**
   * Build an indented component tree from SCM JSON Properties object.
   */
  private String buildComponentTree(JSONObject properties, int depth) {
    StringBuilder sb = new StringBuilder();
    String indent = repeatIndent(depth);

    String type = properties.optString("$Type", "?");
    String name = properties.optString("$Name", "?");
    sb.append(indent).append(type).append(" (").append(name).append(")");

    // Collect non-default, non-internal properties
    List<String> propPairs = new ArrayList<>();
    for (Object keyObj : properties.keySet()) {
      String key = (String) keyObj;
      if (key.startsWith("$") || "Uuid".equals(key)) {
        continue;
      }
      propPairs.add(key + "=" + properties.get(key));
    }
    if (!propPairs.isEmpty()) {
      sb.append(" [").append(String.join(", ", propPairs)).append("]");
    }
    sb.append("\n");

    // Recurse into children
    JSONArray children = properties.optJSONArray("$Components");
    if (children != null) {
      for (int i = 0; i < children.length(); i++) {
        sb.append(buildComponentTree(children.getJSONObject(i), depth + 1));
      }
    }

    return sb.toString();
  }

  private String buildScreenSummary(String userId, long projectId,
      String screenName, String packagePath) {
    StringBuilder sb = new StringBuilder();
    if (packagePath == null) {
      sb.append("(unknown)\n");
      return sb.toString();
    }

    String scmFileId = packagePath + "/" + screenName + FORM_PROPERTIES_EXTENSION;
    try {
      String scmContent = storageIo.downloadFile(userId, projectId, scmFileId, "UTF-8");
      String scmJson = extractScmJson(scmContent);
      if (scmJson != null) {
        JSONObject props = new JSONObject(scmJson).optJSONObject("Properties");
        if (props != null) {
          int componentCount = countComponents(props);
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

  private int countComponents(JSONObject properties) {
    int count = 1;
    JSONArray children = properties.optJSONArray("$Components");
    if (children != null) {
      for (int i = 0; i < children.length(); i++) {
        count += countComponents(children.getJSONObject(i));
      }
    }
    return count;
  }

  private String buildModeInstructions(String mode) {
    StringBuilder sb = new StringBuilder();
    sb.append("## Mode: ").append(mode).append("\n\n");
    switch (mode) {
      case "Advisor":
        sb.append("You are in Advisor mode. You can ONLY provide advice and answer questions. ")
            .append("You CANNOT modify the project — no write tools are available to you. ")
            .append("Use the lookup_component and lookup_screen tools to examine the project ")
            .append("when needed, then provide helpful guidance in your text response. ")
            .append("Your text response is your only way to communicate with the user.\n");
        break;
      case "ScreenEditor":
        sb.append("You are in ScreenEditor mode. You can modify the CURRENT screen only. ")
            .append("You cannot create, delete, or switch screens. ")
            .append("To make changes, invoke the provided tools via function calling. ")
            .append("Always include a text response explaining what you are doing or ")
            .append("asking clarifying questions — do not return tool calls without ")
            .append("an accompanying explanation.\n");
        break;
      case "ProjectEditor":
        sb.append("You are in ProjectEditor mode. You have full access to modify the project ")
            .append("including creating/deleting screens, modifying any screen, ")
            .append("and setting project-level properties. ")
            .append("To make changes, invoke the provided tools via function calling. ")
            .append("Always include a text response explaining what you are doing or ")
            .append("asking clarifying questions — do not return tool calls without ")
            .append("an accompanying explanation.\n");
        break;
      default:
        break;
    }
    return sb.toString();
  }

  // ---------- Helper methods ----------

  /**
   * Extract the JSON content from an SCM file (strips the #| $JSON ... |# wrapper).
   */
  private String extractScmJson(String scmContent) {
    if (scmContent == null || scmContent.isEmpty()) {
      return null;
    }
    // SCM files are wrapped in #| $JSON ... |#
    int jsonStart = scmContent.indexOf("{");
    int jsonEnd = scmContent.lastIndexOf("}");
    if (jsonStart >= 0 && jsonEnd > jsonStart) {
      return scmContent.substring(jsonStart, jsonEnd + 1);
    }
    return null;
  }

  /**
   * Determine the source package path for the project.
   * Reads project.properties to find the main form and derives the package path.
   */
  private String getPackagePath(String userId, long projectId) {
    try {
      String propsContent = storageIo.downloadFile(
          userId, projectId, PROJECT_PROPERTIES_FILE, "UTF-8");
      Properties props = new Properties();
      props.load(new java.io.StringReader(propsContent));

      // main=com.example.myapp.Screen1
      String main = props.getProperty("main", "");
      if (main.isEmpty()) {
        return null;
      }
      int lastDot = main.lastIndexOf('.');
      if (lastDot < 0) {
        return null;
      }
      String packageName = main.substring(0, lastDot);
      return SRC_FOLDER + "/" + packageName.replace('.', '/');
    } catch (Exception e) {
      LOG.warning("Failed to read project properties: " + e.getMessage());
      return null;
    }
  }

  private List<String> listScreenNames(String userId, long projectId, String packagePath) {
    List<String> screens = new ArrayList<>();
    if (packagePath == null) {
      return screens;
    }
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(packagePath + "/") && fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
          String name = fileId.substring(fileId.lastIndexOf('/') + 1,
              fileId.length() - FORM_PROPERTIES_EXTENSION.length());
          screens.add(name);
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list screens: " + e.getMessage());
    }
    return screens;
  }

  private List<String> listAssets(String userId, long projectId) {
    List<String> assets = new ArrayList<>();
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(ASSETS_FOLDER + "/")
            && !fileId.startsWith(EXTERNAL_COMPS_FOLDER + "/")) {
          String name = fileId.substring(ASSETS_FOLDER.length() + 1);
          assets.add(name);
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list assets: " + e.getMessage());
    }
    return assets;
  }

  private List<String> listExtensions(String userId, long projectId) {
    List<String> extensions = new ArrayList<>();
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        if (fileId.startsWith(EXTERNAL_COMPS_FOLDER + "/")
            && fileId.endsWith("/components.json")) {
          String extPath = fileId.substring(EXTERNAL_COMPS_FOLDER.length() + 1);
          String extName = extPath.substring(0, extPath.indexOf('/'));
          if (!extensions.contains(extName)) {
            extensions.add(extName);
          }
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to list extensions: " + e.getMessage());
    }
    return extensions;
  }

  private static String repeatIndent(int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append("  ");
    }
    return sb.toString();
  }

  // ---------- Static resource loading ----------

  private static String getReference() {
    if (cachedReference == null) {
      cachedReference = loadResource("appinventor_reference.md");
    }
    return cachedReference;
  }

  private static String getCatalog() {
    if (cachedCatalog == null) {
      cachedCatalog = buildCompactCatalog();
    }
    return cachedCatalog;
  }

  /**
   * Build a compact component catalog from simple_components.json.
   *
   * <p>Groups components by category with names and brief descriptions only.
   * Excludes Form (documented inline in the system prompt) and INTERNAL
   * components. Only components with {@code showOnPalette=true} are included.
   */
  private static String buildCompactCatalog() {
    String json = loadResource("/com/google/appinventor/simple_components.json");
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

      // Skip Form (handled in system prompt), INTERNAL, and hidden components
      if ("Form".equals(name) || "INTERNAL".equals(categoryString) || !showOnPalette) {
        continue;
      }

      // Map category enum name to display name
      String displayCategory;
      try {
        displayCategory = ComponentCategory.valueOf(categoryString).getName();
      } catch (IllegalArgumentException e) {
        displayCategory = categoryString;
      }

      // Clean up description
      String desc = stripHtml(helpString);
      desc = truncateDescription(desc);

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

  /**
   * Strip HTML tags, decode common entities, and collapse whitespace.
   */
  private static String stripHtml(String html) {
    if (html == null || html.isEmpty()) {
      return "";
    }
    String text = html.replaceAll("<[^>]+>", " ");
    text = text.replace("&amp;", "&")
               .replace("&lt;", "<")
               .replace("&gt;", ">")
               .replace("&quot;", "\"")
               .replace("&#39;", "'")
               .replace("&nbsp;", " ");
    text = text.replaceAll("\\s+", " ").trim();
    return text;
  }

  /**
   * Truncate to the first sentence (period followed by whitespace or end),
   * capped at 150 characters.
   */
  private static String truncateDescription(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // Find first sentence boundary
    int end = -1;
    for (int i = 0; i < text.length() - 1; i++) {
      if (text.charAt(i) == '.' && (i + 1 >= text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
        end = i + 1; // include the period
        break;
      }
    }
    // If no sentence boundary found, check if text ends with period
    if (end < 0 && text.endsWith(".")) {
      end = text.length();
    }
    if (end < 0) {
      end = text.length();
    }
    String result = text.substring(0, end).trim();
    if (result.length() > 150) {
      result = result.substring(0, 147) + "...";
    }
    return result;
  }

  private static String getExamples() {
    if (cachedExamples == null) {
      cachedExamples = loadResource("few_shot_examples.json");
    }
    return cachedExamples;
  }

  private static String getYailGrammar() {
    if (cachedYailGrammar == null) {
      cachedYailGrammar = loadResource("yail_grammar.md");
    }
    return cachedYailGrammar;
  }

  private static String loadResource(String name) {
    String path = name.startsWith("/") ? name : RESOURCE_BASE + name;
    try (InputStream is = AIContextBuilder.class.getResourceAsStream(path)) {
      if (is == null) {
        LOG.warning("Resource not found: " + path);
        return "(resource not available)";
      }
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(is, StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString();
    } catch (IOException e) {
      LOG.warning("Failed to load resource " + name + ": " + e.getMessage());
      return "(resource not available)";
    }
  }
}
