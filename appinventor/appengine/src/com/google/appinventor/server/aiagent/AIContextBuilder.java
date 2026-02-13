// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.CatalogModule;
import com.google.appinventor.server.aiagent.context.ContextParams;
import com.google.appinventor.server.aiagent.context.ExamplesModule;
import com.google.appinventor.server.aiagent.context.GrammarModule;
import com.google.appinventor.server.aiagent.context.ModeModule;
import com.google.appinventor.server.aiagent.context.ProjectModule;
import com.google.appinventor.server.aiagent.context.ReferenceModule;
import com.google.appinventor.server.aiagent.context.ScreenModule;
import com.google.appinventor.server.aiagent.llm.LLMTool;
import com.google.appinventor.server.storage.StorageIo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Builds the LLM system prompt and tool definitions for the AI agent.
 *
 * <p>The <strong>system prompt</strong> is fully static and assembled by
 * {@link #build()} from:
 * <ul>
 *   <li>Layer 1: Static reference and rules from appinventor_reference.md
 *   <li>Layer 2: Compact component catalog from simple_components.json
 *   <li>Layer 3: YAIL grammar reference from yail_grammar.md
 *   <li>Layer 4: Few-shot examples from few_shot_examples.json
 * </ul>
 *
 * <p>Per-request <strong>context messages</strong> are built by
 * {@link #buildContextMessages} and sent as separate user messages before
 * the user's actual message:
 * <ol>
 *   <li>Mode and view: current mode instructions and editor view rules
 *   <li>Project overview: metadata, screen list, assets, extensions
 *   <li>Current screen: component tree and blocks YAIL
 * </ol>
 *
 * <p>Tool definitions are built separately by {@link #buildTools} and passed
 * via each provider's native tool/function-calling API parameter, filtered
 * by mode and current editor view.
 *
 * <p>All system prompt layers are cached on first use. Context messages
 * are built fresh per-request.
 */
public class AIContextBuilder {

  private static final Logger LOG = Logger.getLogger(AIContextBuilder.class.getName());

  // Context modules
  private final ReferenceModule referenceModule = new ReferenceModule();
  private final CatalogModule catalogModule = new CatalogModule();
  private final GrammarModule grammarModule = new GrammarModule();
  private final ExamplesModule examplesModule = new ExamplesModule();
  private final ModeModule modeModule = new ModeModule();
  private final ProjectModule projectModule = new ProjectModule();
  private final ScreenModule screenModule = new ScreenModule();

  private final StorageIo storageIo;

  public AIContextBuilder(StorageIo storageIo) {
    this.storageIo = storageIo;
  }

  StorageIo getStorageIo() {
    return storageIo;
  }

  // ---------- Public API ----------

  /**
   * Build the system prompt for an LLM request.
   *
   * <p>All layers are cached on first use.
   *
   * @return the assembled system prompt string
   */
  public String build() {
    StringBuilder sb = new StringBuilder();

    // Layer 1: Static reference
    String reference = referenceModule.build(null);
    sb.append(reference).append("\n\n");
    AIDebug.log(LOG, "Context Layer 1 (reference): " + reference.length() + " chars");

    // Layer 2: Component catalog
    String catalog = catalogModule.build(null);
    sb.append("## Component Catalog\n\n");
    sb.append(catalog).append("\n\n");
    AIDebug.log(LOG, "Context Layer 2 (catalog): " + catalog.length() + " chars");

    // Layer 3: YAIL grammar reference
    String grammar = grammarModule.build(null);
    sb.append("## YAIL Grammar\n\n");
    sb.append(grammar).append("\n\n");
    AIDebug.log(LOG, "Context Layer 3 (YAIL grammar): " + grammar.length() + " chars");

    // Layer 4: Few-shot examples
    String examples = examplesModule.build(null);
    sb.append("## Examples\n\n");
    sb.append(examples).append("\n\n");
    AIDebug.log(LOG, "Context Layer 4 (examples): " + examples.length() + " chars");

    return sb.toString();
  }

  /**
   * Build the per-request context messages sent as separate user messages
   * before the user's actual message.
   *
   * <p>Returns three context messages:
   * <ol>
   *   <li>Mode and view: current mode instructions and editor view rules
   *   <li>Project overview: project metadata, screen list, assets,
   *       extensions, and other screen summaries
   *   <li>Current screen: component tree and blocks YAIL
   * </ol>
   *
   * @param userId               the authenticated user
   * @param projectId            the project being edited
   * @param screenName           the currently active screen
   * @param mode                 "Advisor", "ScreenEditor", or "ProjectEditor"
   * @param blocksYail           client-generated YAIL for the current screen's blocks
   *                             (may be null or empty if unavailable)
   * @param currentView          the active editor view ("Designer" or "Blocks")
   * @param screenComponentsJson live component tree JSON from the client
   * @param projectSnapshot      project metadata JSON from the client
   * @param blockWarnings        JSON with block warnings/errors from the client
   *                             (may be null or empty)
   * @return list of context message strings
   */
  public List<String> buildContextMessages(String userId, long projectId, String screenName,
      String mode, String blocksYail, String currentView,
      String screenComponentsJson, String projectSnapshot,
      String blockWarnings) {
    ContextParams params = new ContextParams(userId, projectId, screenName, mode,
        blocksYail, currentView, screenComponentsJson, projectSnapshot, blockWarnings);
    List<String> messages = new ArrayList<>();

    // Message 1: Mode and view
    String modeCtx = modeModule.build(params);
    messages.add(modeCtx);
    AIDebug.log(LOG, "Context message 1 (mode): " + modeCtx.length() + " chars");

    // Message 2: Project overview
    String projectCtx = projectModule.build(params);
    messages.add(projectCtx);
    AIDebug.log(LOG, "Context message 2 (project): " + projectCtx.length() + " chars");

    // Message 3: Current screen
    String screenCtx = screenModule.build(params);
    messages.add(screenCtx);
    AIDebug.log(LOG, "Context message 3 (screen): " + screenCtx.length() + " chars");

    return messages;
  }

  /**
   * Build the list of LLM tools filtered by mode and current editor view.
   *
   * @param mode        "Advisor", "ScreenEditor", or "ProjectEditor"
   * @param currentView the active editor view ("Designer" or "Blocks")
   * @return tools available in the given mode and view
   */
  public List<LLMTool> buildTools(String mode, String currentView) {
    List<LLMTool> tools = new ArrayList<>();

    // Read-only tools available in all modes
    tools.add(new LLMTool(AIToolNames.LOOKUP_COMPONENT,
        "Look up full metadata for a component type from the component database. "
            + "Returns all properties, events, methods, and their types.",
        "{\"type\":\"object\",\"properties\":{\"component_type\":{\"type\":\"string\","
            + "\"description\":\"The component type name, e.g. Button, Label\"}},\"required\":[\"component_type\"]}"));
    tools.add(new LLMTool(AIToolNames.LOOKUP_SCREEN,
        "Look up the saved state of a screen including its component tree. "
            + "Note: this reads from the server's last-saved data, which may not "
            + "reflect unsaved changes. For the current screen, the component tree "
            + "in the context messages above is the authoritative source. "
            + "Use this tool only for non-current screens.",
        "{\"type\":\"object\",\"properties\":{\"screen_name\":{\"type\":\"string\","
            + "\"description\":\"The screen name, e.g. Screen1\"}},\"required\":[\"screen_name\"]}"));

    if ("Advisor".equals(mode)) {
      return tools;
    }

    // Designer tools: only available when viewing Designer
    if (!"Blocks".equals(currentView)) {
      tools.add(new LLMTool(AIToolNames.ADD_COMPONENT,
          "Add a new component to the current screen.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"component_type\":{\"type\":\"string\",\"description\":\"Component type, e.g. Button\"},"
              + "\"name\":{\"type\":\"string\",\"description\":\"Instance name, e.g. Button1\"},"
              + "\"parent\":{\"type\":\"string\",\"description\":\"Parent container name (default: screen root)\"},"
              + "\"properties\":{\"type\":\"object\",\"description\":\"Initial property values\"}"
              + "},\"required\":[\"component_type\",\"name\"]}"));

      tools.add(new LLMTool(AIToolNames.DELETE_COMPONENT,
          "Delete a component from the current screen.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"name\":{\"type\":\"string\",\"description\":\"Component instance name\"}"
              + "},\"required\":[\"name\"]}"));

      tools.add(new LLMTool(AIToolNames.SET_PROPERTY,
          "Set a property value on a component.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"component_name\":{\"type\":\"string\",\"description\":\"Component instance name\"},"
              + "\"property_name\":{\"type\":\"string\",\"description\":\"Property name\"},"
              + "\"value\":{\"description\":\"Property value (type depends on property)\"}"
              + "},\"required\":[\"component_name\",\"property_name\",\"value\"]}"));

      tools.add(new LLMTool(AIToolNames.RENAME_COMPONENT,
          "Rename a component instance.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"old_name\":{\"type\":\"string\",\"description\":\"Current component name\"},"
              + "\"new_name\":{\"type\":\"string\",\"description\":\"New component name\"}"
              + "},\"required\":[\"old_name\",\"new_name\"]}"));
    }

    // Blocks tools: only available when viewing Blocks
    if (!"Designer".equals(currentView)) {
      tools.add(new LLMTool(AIToolNames.WRITE_BLOCK,
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

      tools.add(new LLMTool(AIToolNames.DELETE_BLOCK,
          "Delete a top-level block (event handler, global variable, or procedure). "
              + "The identifier format matches the YAIL form head tokens.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"block\":{\"type\":\"string\",\"description\":\"Block identifier using YAIL head "
              + "tokens, e.g. 'define-event Button1 Click', 'define-generic-event Button Click', "
              + "'def g$score', 'def p$factorial', 'def-return p$myFunc'\"}"
              + "},\"required\":[\"block\"]}"));
    }

    // Navigation tool: toggle editor view (ScreenEditor and ProjectEditor)
    tools.add(new LLMTool(AIToolNames.TOGGLE_EDITOR,
        "Switch the editor view between Designer and Blocks. "
            + "This tool MUST be called ALONE — do not combine it with any other tools "
            + "in the same response. After the toggle is confirmed, continue with "
            + "the operations that require the new view.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"view\":{\"type\":\"string\",\"enum\":[\"Designer\",\"Blocks\"],"
            + "\"description\":\"The editor view to switch to\"}"
            + "},\"required\":[\"view\"]}"));

    // Log tool list built so far before project-level tools
    if (AIDebug.enabled()) {
      StringBuilder toolList = new StringBuilder("Tools for mode " + mode + ":");
      for (LLMTool t : tools) {
        toolList.append(" ").append(t.getName());
      }
      AIDebug.log(LOG, toolList.toString());
    }

    // Navigation tool: switch screen (ScreenEditor and ProjectEditor)
    tools.add(new LLMTool(AIToolNames.SWITCH_SCREEN,
        "Switch the active screen context. "
            + "This tool MUST be called ALONE — do not combine it with any other tools "
            + "in the same response. After the switch is confirmed, continue with "
            + "the operations that require the new screen.",
        "{\"type\":\"object\",\"properties\":{"
            + "\"screen_name\":{\"type\":\"string\",\"description\":\"Screen name to switch to\"}"
            + "},\"required\":[\"screen_name\"]}"));

    // Project-level tools only for ProjectEditor
    if ("ProjectEditor".equals(mode)) {
      tools.add(new LLMTool(AIToolNames.CREATE_SCREEN,
          "Create a new screen in the project.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"screen_name\":{\"type\":\"string\",\"description\":\"New screen name\"}"
              + "},\"required\":[\"screen_name\"]}"));

      tools.add(new LLMTool(AIToolNames.DELETE_SCREEN,
          "Delete a screen from the project.",
          "{\"type\":\"object\",\"properties\":{"
              + "\"screen_name\":{\"type\":\"string\",\"description\":\"Screen name to delete\"}"
              + "},\"required\":[\"screen_name\"]}"));

      tools.add(new LLMTool(AIToolNames.SET_PROJECT_PROPERTY,
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
    return screenModule.buildScreenState(userId, projectId, screenName, storageIo);
  }
}
