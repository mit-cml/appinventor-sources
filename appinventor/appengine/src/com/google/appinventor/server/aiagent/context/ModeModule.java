// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

/**
 * Builds mode instructions and editor view rules for the current request.
 */
public class ModeModule extends ContextModule {

  @Override
  public String build(ContextParams params) {
    return buildModeInstructions(params.getMode(), params.getCurrentView(),
        params.getLocale(), params.getLanguageDisplayName());
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
