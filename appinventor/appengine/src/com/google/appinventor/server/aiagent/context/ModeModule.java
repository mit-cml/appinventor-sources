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
 */
public class ModeModule extends ContextModule {

  @Override
  public String build(ContextParams params) {
    if (params.getEnforcementContext() == EnforcementContext.PLANNING) {
      return buildPlanningInstructions();
    }
    if (params.getEnforcementContext() == EnforcementContext.CHILD_EXECUTION) {
      return buildChildExecutionInstructions(params.getCurrentView());
    }
    return buildModeInstructions(params.getMode(), params.getCurrentView(),
        params.getLocale(), params.getLanguageDisplayName());
  }

  private String buildChildExecutionInstructions(String currentView) {
    return "You are a child agent executing one step of a multi-screen plan. "
        + "Your task is to implement the requested changes on THIS screen only.\n\n"
        + "IMPORTANT RULES:\n"
        + "- Execute the task COMPLETELY. Do not ask questions or wait for user input.\n"
        + "- Do not narrate or explain what you plan to do — just do it.\n"
        + "- You can ONLY modify the current screen. You cannot create, delete, "
        + "or switch to other screens.\n"
        + "- Use toggle_editor to switch between Designer and Blocks views as needed.\n"
        + "- The screen form component (the root) should be referenced by its screen name "
        + "for property changes (e.g., set_property with the screen name as component_name).\n\n"
        + "You are currently viewing the **" + currentView + "** editor.\n"
        + "- Designer operations (add_component, delete_component, set_property, "
        + "rename_component) require the Designer view.\n"
        + "- Block operations (write_block, delete_block) require the Blocks view.\n"
        + "- Use toggle_editor to switch when needed.\n\n"
        + "Complete ALL work for this screen — both Designer components and Blocks logic "
        + "— before finishing. Do not leave partial work.";
  }

  private String buildPlanningInstructions() {
    return "You are in Plan & Execute mode. Your task is to research the project and "
        + "propose a structured execution plan.\n\n"
        + "Available tools: lookup_component (research component specs), "
        + "lookup_screen (research screen state), propose_plan (submit your plan).\n\n"
        + "DO NOT attempt to add components, write blocks, or make any changes. Instead:\n"
        + "1. Use lookup_component and lookup_screen to understand the current project state.\n"
        + "2. Break the user's request into steps, each targeting a specific screen.\n"
        + "3. Use '__project__' as the screen for project-level operations "
        + "(creating screens, setting project properties).\n"
        + "4. Set depends_on when a step requires another to complete first "
        + "(e.g., a screen must be created before components can be added to it).\n"
        + "5. Call propose_plan with your complete plan.";
  }

  private String buildModeInstructions(String mode, String currentView,
      String locale, String languageDisplayName) {
    StringBuilder sb = new StringBuilder();
    sb.append("[Current mode and view — supersedes any previous mode instructions]\n\n");
    sb.append("## Mode: ").append(mode).append("\n\n");
    if (AI_AGENT_MODE_ADVISOR.equals(mode)) {
      sb.append("You are in Advisor mode. You can ONLY provide advice and answer questions. ")
          .append("You CANNOT modify the project — no write tools are available to you. ")
          .append("Use the lookup_component and lookup_screen tools to examine the project ")
          .append("when needed, then provide helpful guidance in your text response. ")
          .append("Your text response is your only way to communicate with the user.\n");
    } else if (AI_AGENT_MODE_SCREEN_EDITOR.equals(mode)) {
      sb.append("You are in ScreenEditor mode. You can modify the CURRENT screen only. ")
          .append("You cannot create, delete, or switch screens. You can toggle editor views. ")
          .append("To make changes, invoke the provided tools via function calling. ")
          .append("Always include a text response explaining what you are doing or ")
          .append("asking clarifying questions — do not return tool calls without ")
          .append("an accompanying explanation.\n");
    } else if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)) {
      sb.append("You are in ProjectEditor mode. You have full access to modify the project ")
          .append("including creating/deleting screens, modifying any screen, ")
          .append("and setting project-level properties. ")
          .append("To make changes, invoke the provided tools via function calling. ")
          .append("Always include a text response explaining what you are doing or ")
          .append("asking clarifying questions — do not return tool calls without ")
          .append("an accompanying explanation.\n");
    }

    if (!AI_AGENT_MODE_ADVISOR.equals(mode)) {
      sb.append("\n### Editor View Rules\n");
      sb.append("The user is currently viewing the **").append(currentView)
          .append("** editor.\n");
      sb.append("- **Designer operations** (`add_component`, `delete_component`, ")
          .append("`set_property`, `rename_component`) can ONLY be executed when the ")
          .append("user is viewing the **Designer** editor.\n");
      sb.append("- **Block operations** (`write_block`, `delete_block`) can ONLY be ")
          .append("executed when the user is viewing the **Blocks** editor.\n");
      sb.append("- To switch views, use the `toggle_editor` tool.\n");
      sb.append("- `toggle_editor` MUST be called **ALONE** — ")
          .append("never combine it with other tool calls in the same response.\n");
      sb.append("- After `toggle_editor` is confirmed, continue ")
          .append("with the operations that require the new view.\n");
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
