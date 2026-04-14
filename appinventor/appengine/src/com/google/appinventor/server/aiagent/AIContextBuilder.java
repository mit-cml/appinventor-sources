// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.CatalogModule;
import com.google.appinventor.server.aiagent.context.CompanionModule;
import com.google.appinventor.server.aiagent.context.ContextParams;
import com.google.appinventor.server.aiagent.context.ContextUtils;
import com.google.appinventor.server.aiagent.context.ExamplesModule;
import com.google.appinventor.server.aiagent.context.GrammarModule;
import com.google.appinventor.server.aiagent.context.ModeModule;
import com.google.appinventor.server.aiagent.context.ProjectModule;
import com.google.appinventor.server.aiagent.context.ReferenceModule;
import com.google.appinventor.server.aiagent.context.ScreenModule;
import com.google.appinventor.server.aiagent.context.TutorialModule;
import com.google.appinventor.server.aiagent.llm.LLMTool;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.settings.SettingsConstants;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;

import org.json.JSONObject;

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
 * <p>Tool definitions are loaded from {@code tool_definitions.json} and
 * filtered by mode and current editor view via {@link #buildTools}.
 *
 * <p>All system prompt layers are cached on first use. Context messages
 * are built fresh per-request.
 */
public class AIContextBuilder {

  private static final Logger LOG = Logger.getLogger(AIContextBuilder.class.getName());

  /**
   * Controls whether tutorial context is included in LLM requests.
   * When {@code true}, if the project has a TutorialURL set, the tutorial
   * page content is fetched and included as an additional context message.
   */
  private static final Flag<Boolean> INCLUDE_TUTORIAL_CONTEXT =
      Flag.createFlag("ai.agent.features.tutorial-context", true);

  /**
   * Controls whether Companion runtime state is included in LLM requests.
   * When {@code true}, if the client attaches a non-null companionSnapshot
   * to the request, that state is rendered into a context message and the
   * companion read tools are exposed to the LLM.
   */
  private static final Flag<Boolean> INCLUDE_COMPANION_CONTEXT =
      Flag.createFlag("ai.agent.features.companion-context", true);

  /** Cached parsed tool definitions from {@code tool_definitions.json}. */
  private static volatile JSONObject cachedToolDefs;

  // Context modules
  private final ReferenceModule referenceModule = new ReferenceModule();
  private final CatalogModule catalogModule = new CatalogModule();
  private final GrammarModule grammarModule = new GrammarModule();
  private final ExamplesModule examplesModule = new ExamplesModule();
  private final ModeModule modeModule = new ModeModule();
  private final ProjectModule projectModule = new ProjectModule();
  private final ScreenModule screenModule = new ScreenModule();
  private final CompanionModule companionModule = new CompanionModule();
  private final TutorialContentCache tutorialContentCache;
  private final TutorialModule tutorialModule;

  private final StorageIo storageIo;

  public AIContextBuilder(StorageIo storageIo) {
    this.storageIo = storageIo;
    this.tutorialContentCache = new TutorialContentCache(storageIo);
    this.tutorialModule = new TutorialModule(tutorialContentCache);
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
   * <p>Returns up to five context messages (mode, project, screen, companion,
   * tutorial — the last two only when enabled and applicable).
   * <ol>
   *   <li>Mode and view: current mode instructions and editor view rules
   *   <li>Project overview: project metadata, screen list, assets,
   *       extensions, and other screen summaries
   *   <li>Current screen: component tree and blocks YAIL
   *   <li>Companion runtime state (if enabled and snapshot present)
   *   <li>Tutorial content (if enabled and project has a TutorialURL)
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
   * @param locale               the user's interface locale code (e.g. "es_ES"),
   *                             may be null
   * @param languageDisplayName  the native display name (e.g. "Español"),
   *                             may be null
   * @param companionSnapshot    JSON snapshot of live Companion state from the client
   *                             (may be null when not sharing)
   * @param enforcementContext   controls which operations are allowed
   *                             (e.g. PLANNING suppresses the normal mode instructions)
   * @return list of context message strings
   */
  public List<String> buildContextMessages(String userId, long projectId, String screenName,
      String mode, String blocksYail, String currentView,
      String screenComponentsJson, String projectSnapshot,
      String blockWarnings, String locale, String languageDisplayName,
      String companionSnapshot, EnforcementContext enforcementContext) {
    ContextParams params = new ContextParams(userId, projectId, screenName, mode,
        blocksYail, currentView, screenComponentsJson, projectSnapshot, blockWarnings,
        locale, languageDisplayName, companionSnapshot, enforcementContext);
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

    // Message 4: Companion runtime state (if enabled and snapshot present)
    if (INCLUDE_COMPANION_CONTEXT.get()) {
      String companionCtx = companionModule.build(params);
      if (companionCtx != null) {
        messages.add(companionCtx);
        AIDebug.log(LOG, "Context message 4 (companion): " + companionCtx.length() + " chars");
      }
    }

    // Message 5: Tutorial context (if enabled and active)
    if (INCLUDE_TUTORIAL_CONTEXT.get()) {
      String tutorialCtx = tutorialModule.build(params);
      if (tutorialCtx != null) {
        messages.add(tutorialCtx);
        AIDebug.log(LOG, "Context message 5 (tutorial): " + tutorialCtx.length() + " chars");
      }
    }

    return messages;
  }

  /**
   * Build the list of LLM tools filtered by mode and current editor view.
   *
   * <p>Tool definitions are loaded from {@code tool_definitions.json} and
   * cached on first use.
   *
   * @param mode              "Advisor", "ScreenEditor", or "ProjectEditor"
   * @param currentView       the active editor view ("Designer" or "Blocks")
   * @param context           the enforcement context controlling which tools are exposed
   * @param companionSharing  {@code true} when the client has attached a non-null
   *                          companionSnapshot to the request; exposes companion read tools
   * @return tools available in the given mode, view, and enforcement context
   */
  public List<LLMTool> buildTools(String mode, String currentView, EnforcementContext context,
      boolean companionSharing) {
    JSONObject defs = getToolDefinitions();
    List<LLMTool> tools = new ArrayList<>();

    // Planning mode: only expose read-only tools and propose_plan
    if (context == EnforcementContext.PLANNING) {
      tools.add(toolFromDefs(defs, AIToolNames.LOOKUP_COMPONENT));
      tools.add(toolFromDefs(defs, AIToolNames.LOOKUP_SCREEN));
      tools.add(toolFromDefs(defs, AIToolNames.PROPOSE_PLAN));
      return tools;
    }

    // Read-only tools available in all modes
    tools.add(toolFromDefs(defs, AIToolNames.LOOKUP_COMPONENT));
    tools.add(toolFromDefs(defs, AIToolNames.LOOKUP_SCREEN));

    // Companion runtime reads: available in all non-PLANNING modes when the
    // client has attached a companion snapshot and the feature flag is on.
    // Advisor + ScreenEditor + ProjectEditor all get them.
    if (INCLUDE_COMPANION_CONTEXT.get() && companionSharing) {
      tools.add(toolFromDefs(defs, AIToolNames.READ_COMPONENT_PROPERTY));
      tools.add(toolFromDefs(defs, AIToolNames.READ_VARIABLE));
      tools.add(toolFromDefs(defs, AIToolNames.READ_RECENT_LOGS));
    }

    if (AI_AGENT_MODE_ADVISOR.equals(mode)) {
      return tools;
    }

    // Designer tools: only available when viewing Designer
    if (!"Blocks".equals(currentView)) {
      tools.add(toolFromDefs(defs, AIToolNames.ADD_COMPONENT));
      tools.add(toolFromDefs(defs, AIToolNames.DELETE_COMPONENT));
      tools.add(toolFromDefs(defs, AIToolNames.SET_PROPERTY));
      tools.add(toolFromDefs(defs, AIToolNames.RENAME_COMPONENT));
    }

    // Blocks tools: only available when viewing Blocks
    if (!"Designer".equals(currentView)) {
      tools.add(toolFromDefs(defs, AIToolNames.WRITE_BLOCK));
      tools.add(toolFromDefs(defs, AIToolNames.DELETE_BLOCK));
    }

    // Navigation tool: toggle editor view (ScreenEditor and ProjectEditor)
    tools.add(toolFromDefs(defs, AIToolNames.TOGGLE_EDITOR));

    // Log tool list built so far before project-level tools
    if (AIDebug.enabled()) {
      StringBuilder toolList = new StringBuilder("Tools for mode " + mode + ":");
      for (LLMTool t : tools) {
        toolList.append(" ").append(t.getName());
      }
      AIDebug.log(LOG, toolList.toString());
    }

    // Project-level tools only for ProjectEditor (not for child agents)
    if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)
        && context != EnforcementContext.CHILD_EXECUTION) {
      tools.add(toolFromDefs(defs, AIToolNames.SWITCH_SCREEN));
      tools.add(toolFromDefs(defs, AIToolNames.CREATE_SCREEN));
      tools.add(toolFromDefs(defs, AIToolNames.DELETE_SCREEN));
      tools.add(toolFromDefs(defs, AIToolNames.SET_PROJECT_PROPERTY));
    }

    // In EXECUTION phase, also expose propose_plan so the parent agent can
    // choose between direct edits and proposing a new plan for complex work.
    if (context == EnforcementContext.EXECUTION) {
      tools.add(toolFromDefs(defs, AIToolNames.PROPOSE_PLAN));
    }

    return tools;
  }

  // ---------- Private helpers ----------

  private static JSONObject getToolDefinitions() {
    if (cachedToolDefs == null) {
      String json = ContextUtils.loadResource("tool_definitions.json");
      cachedToolDefs = new JSONObject(json);
    }
    return cachedToolDefs;
  }

  private static LLMTool toolFromDefs(JSONObject defs, String toolName) {
    JSONObject def = defs.getJSONObject(toolName);
    return new LLMTool(
        toolName,
        def.getString("description"),
        def.getJSONObject("parameters").toString());
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
