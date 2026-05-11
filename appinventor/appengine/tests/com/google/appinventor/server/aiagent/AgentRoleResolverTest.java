// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.components.common.AIAgentMode;
import junit.framework.TestCase;

public class AgentRoleResolverTest extends TestCase {

  public void testAdvisorMapsToAdvisorRegardlessOfEnforcement() {
    for (EnforcementContext ec : EnforcementContext.values()) {
      assertEquals(
          "Advisor + " + ec,
          AgentRole.ADVISOR,
          AgentRoleResolver.resolve(AIAgentMode.Advisor, ec));
    }
  }

  public void testScreenEditorMapsToScreenEditorRegardlessOfEnforcement() {
    for (EnforcementContext ec : EnforcementContext.values()) {
      assertEquals(
          "ScreenEditor + " + ec,
          AgentRole.SCREEN_EDITOR,
          AgentRoleResolver.resolve(AIAgentMode.ScreenEditor, ec));
    }
  }

  public void testProjectEditorStandardMapsToProjectEditor() {
    assertEquals(
        AgentRole.PROJECT_EDITOR,
        AgentRoleResolver.resolve(
            AIAgentMode.ProjectEditor, EnforcementContext.STANDARD));
  }

  public void testProjectEditorExecutionMapsToProjectEditor() {
    assertEquals(
        AgentRole.PROJECT_EDITOR,
        AgentRoleResolver.resolve(
            AIAgentMode.ProjectEditor, EnforcementContext.EXECUTION));
  }

  public void testProjectEditorPlanningMapsToPlanner() {
    assertEquals(
        AgentRole.PROJECT_EDITOR_PLANNER,
        AgentRoleResolver.resolve(
            AIAgentMode.ProjectEditor, EnforcementContext.PLANNING));
  }

  public void testProjectEditorChildExecutionMapsToChild() {
    assertEquals(
        AgentRole.PROJECT_EDITOR_CHILD,
        AgentRoleResolver.resolve(
            AIAgentMode.ProjectEditor, EnforcementContext.CHILD_EXECUTION));
  }

  public void testOffModeFallsBackToAdvisor() {
    // Off is a permission-mode marker; the engine never instantiates an
    // agent in Off mode, but the resolver must still return a sane default.
    assertEquals(
        AgentRole.ADVISOR,
        AgentRoleResolver.resolve(AIAgentMode.Off, EnforcementContext.STANDARD));
  }

  public void testNullModeFallsBackToAdvisor() {
    assertEquals(
        AgentRole.ADVISOR,
        AgentRoleResolver.resolve(null, EnforcementContext.STANDARD));
  }

  public void testNullEnforcementTreatedAsStandard() {
    assertEquals(
        AgentRole.PROJECT_EDITOR,
        AgentRoleResolver.resolve(AIAgentMode.ProjectEditor, null));
  }
}
