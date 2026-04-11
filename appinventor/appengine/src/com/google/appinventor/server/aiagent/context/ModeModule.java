// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.aiagent.EnforcementContext;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

/**
 * Builds mode instructions and editor view rules for the current request.
 *
 * <p>Prompt text is loaded from resource files in {@code resources/}:
 * <ul>
 *   <li>{@code mode_planning.md} — planning mode instructions
 *   <li>{@code mode_execution.md} — post-plan execution instructions (parent agent)
 *   <li>{@code mode_child_execution.md} — child agent execution instructions
 *   <li>{@code mode_advisor.md} — advisor mode instructions
 *   <li>{@code mode_screen_editor.md} — screen editor mode instructions
 *   <li>{@code mode_project_editor.md} — project editor mode instructions
 *   <li>{@code editor_view_rules.md} — editor view switching rules
 * </ul>
 *
 * <p>Dynamic values are substituted via {@code {{placeholder}}} tokens.
 */
public class ModeModule extends ContextModule {

  private static volatile String cachedPlanning;
  private static volatile String cachedExecution;
  private static volatile String cachedChildExecution;
  private static volatile String cachedAdvisor;
  private static volatile String cachedScreenEditor;
  private static volatile String cachedProjectEditor;
  private static volatile String cachedViewRules;

  @Override
  public String build(ContextParams params) {
    if (params.getEnforcementContext() == EnforcementContext.PLANNING) {
      return buildPlanningInstructions();
    }
    if (params.getEnforcementContext() == EnforcementContext.EXECUTION) {
      return buildExecutionInstructions(params.getCurrentView());
    }
    if (params.getEnforcementContext() == EnforcementContext.CHILD_EXECUTION) {
      return buildChildExecutionInstructions(params.getCurrentView());
    }
    return buildModeInstructions(params.getMode(), params.getCurrentView(),
        params.getLocale(), params.getLanguageDisplayName());
  }

  private String buildExecutionInstructions(String currentView) {
    if (cachedExecution == null) {
      cachedExecution = ContextUtils.loadResource("mode_execution.md");
    }
    return cachedExecution.replace("{{view}}", currentView);
  }

  private String buildChildExecutionInstructions(String currentView) {
    if (cachedChildExecution == null) {
      cachedChildExecution = ContextUtils.loadResource("mode_child_execution.md");
    }
    return cachedChildExecution.replace("{{view}}", currentView);
  }

  private String buildPlanningInstructions() {
    if (cachedPlanning == null) {
      cachedPlanning = ContextUtils.loadResource("mode_planning.md");
    }
    return cachedPlanning;
  }

  private String buildModeInstructions(String mode, String currentView,
      String locale, String languageDisplayName) {
    StringBuilder sb = new StringBuilder();
    sb.append("[Current mode and view — supersedes any previous mode instructions]\n\n");
    sb.append("## Mode: ").append(mode).append("\n\n");
    if (AI_AGENT_MODE_ADVISOR.equals(mode)) {
      if (cachedAdvisor == null) {
        cachedAdvisor = ContextUtils.loadResource("mode_advisor.md");
      }
      sb.append(cachedAdvisor);
    } else if (AI_AGENT_MODE_SCREEN_EDITOR.equals(mode)) {
      if (cachedScreenEditor == null) {
        cachedScreenEditor = ContextUtils.loadResource("mode_screen_editor.md");
      }
      sb.append(cachedScreenEditor);
    } else if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)) {
      if (cachedProjectEditor == null) {
        cachedProjectEditor = ContextUtils.loadResource("mode_project_editor.md");
      }
      sb.append(cachedProjectEditor);
    }

    if (!AI_AGENT_MODE_ADVISOR.equals(mode)) {
      if (cachedViewRules == null) {
        cachedViewRules = ContextUtils.loadResource("editor_view_rules.md");
      }
      sb.append("\n").append(cachedViewRules.replace("{{view}}", currentView));
      if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)) {
        sb.append("- `switch_screen` MUST also be called **ALONE** — ")
            .append("never combine it with other tool calls in the same response.\n");
        sb.append("- After `switch_screen` is confirmed, continue ")
            .append("with the operations that require the new screen.\n");
      }
    }

    // Language instruction
    if (locale != null && !locale.isEmpty()
        && !"en".equals(locale) && !"default".equals(locale)) {
      sb.append("\n### Language\n");
      sb.append("The user's interface language is ");
      if (languageDisplayName != null && !languageDisplayName.isEmpty()) {
        sb.append(languageDisplayName).append(" (").append(locale).append(")");
      } else {
        sb.append(locale);
      }
      sb.append(". By default, respond in this language. ")
          .append("However, if the user writes in a different language, ")
          .append("respond in the language they are using.\n");
    }

    return sb.toString();
  }
}
