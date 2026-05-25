// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.components.common.AIAgentMode;

/**
 * Resolves the {@link AgentRole} that should handle a given request based
 * on the user-facing AI mode and the server-side enforcement context.
 *
 * <p>Pure function. No I/O, no flags. Tested exhaustively in
 * {@code AgentRoleResolverTest}.
 */
public final class AgentRoleResolver {

  private AgentRoleResolver() {}

  public static AgentRole resolve(AIAgentMode mode, EnforcementContext ec) {
    if (mode == null) {
      return AgentRole.ADVISOR;
    }
    EnforcementContext effective = ec == null ? EnforcementContext.STANDARD : ec;
    switch (mode) {
      case ScreenEditor:
        return AgentRole.SCREEN_EDITOR;
      case ProjectEditor:
        switch (effective) {
          case PLANNING:
            return AgentRole.PROJECT_EDITOR_PLANNER;
          case CHILD_EXECUTION:
            return AgentRole.PROJECT_EDITOR_CHILD;
          case STANDARD:
          case EXECUTION:
          default:
            return AgentRole.PROJECT_EDITOR;
        }
      case Advisor:
      case Off:
      default:
        return AgentRole.ADVISOR;
    }
  }
}
