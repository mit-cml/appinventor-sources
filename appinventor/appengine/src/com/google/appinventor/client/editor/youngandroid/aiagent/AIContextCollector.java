// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;

import java.util.List;
import java.util.logging.Logger;

/**
 * Builds {@link AIAgentRequest} instances from the live editor state.
 *
 * <p>Reads the current project, screen, components, blocks, and settings
 * from {@link Ode} each time a request is built. No state is cached between
 * calls.</p>
 */
public class AIContextCollector {

  private static final Logger LOG = Logger.getLogger(AIContextCollector.class.getName());

  /**
   * Builds a fully-populated {@link AIAgentRequest} with client-side context.
   *
   * @param userMessage the user's message, or null for continuation/error requests
   */
  public AIAgentRequest buildRequest(String userMessage) {
    long projectId = getCurrentProjectId();
    String screenName = getCurrentScreenName();
    String blocksYail = getCurrentBlocksYail();
    String currentView = getCurrentViewString();
    String screenComponentsJson = buildScreenComponentsJson();
    String projectSnapshot = buildProjectSnapshot();
    return new AIAgentRequest(userMessage, projectId, screenName,
        blocksYail, currentView, screenComponentsJson, projectSnapshot);
  }

  /**
   * Returns the name of the currently active screen.
   */
  public String getCurrentScreenName() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        return project.currentScreen;
      }
    }
    return "Screen1";
  }

  /**
   * Returns the current screen's blocks editor, or null if unavailable.
   */
  public BlocksEditor<?, ?> getCurrentBlocksEditor() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        DesignToolbar.Screen screen = project.screens.get(project.currentScreen);
        if (screen != null && screen.blocksEditor instanceof BlocksEditor) {
          return (BlocksEditor<?, ?>) screen.blocksEditor;
        }
      }
    }
    return null;
  }

  /**
   * Returns the project ID of the currently open project, or 0 if none.
   */
  public long getCurrentProjectId() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        return project.getProjectId();
      }
    }
    return 0;
  }

  /**
   * Returns the current AIAgentMode from the project settings.
   * Defaults to "Off" if no project is open or the setting is missing.
   */
  public String getCurrentAIAgentMode() {
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(getCurrentProjectId());
    if (projectEditor == null) {
      return "Off";
    }
    String mode = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE);
    return (mode == null || mode.isEmpty()) ? "Off" : mode;
  }

  /**
   * Generates YAIL for the current screen's blocks using the client-side
   * Blockly YAIL generators. Returns only block-level YAIL (event handlers,
   * global variables, procedures) without form scaffolding.
   *
   * @return YAIL string, or empty string if the blocks editor is unavailable
   */
  private String getCurrentBlocksYail() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        DesignToolbar.Screen screen = project.screens.get(project.currentScreen);
        if (screen != null && screen.blocksEditor instanceof BlocksEditor) {
          try {
            return ((BlocksEditor<?, ?>) screen.blocksEditor).getBlocksYail();
          } catch (Exception e) {
            LOG.warning("Failed to generate blocks YAIL: " + e.getMessage());
          }
        }
      }
    }
    return "";
  }

  /**
   * Returns the current editor view as a string ("Designer" or "Blocks").
   */
  private String getCurrentViewString() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      return toolbar.getCurrentView() == DesignToolbar.View.BLOCKS ? "Blocks" : "Designer";
    }
    return "Designer";
  }

  /**
   * Builds the live component tree JSON from the current screen's designer.
   * Returns the inner Properties JSON (same structure as
   * {@code DesignerEditor.encodeComponentProperties()}).
   */
  private String buildScreenComponentsJson() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar == null) {
      return null;
    }
    DesignToolbar.DesignProject designProject = toolbar.getCurrentProject();
    if (designProject == null) {
      return null;
    }
    DesignToolbar.Screen screen = designProject.screens.get(designProject.currentScreen);
    if (screen == null || !(screen.designerEditor instanceof YaFormEditor)) {
      return null;
    }
    try {
      return ((YaFormEditor) screen.designerEditor).getPropertiesJson();
    } catch (Exception e) {
      LOG.warning("Failed to build screen components JSON: " + e.getMessage());
      return null;
    }
  }

  /**
   * Builds the project metadata snapshot JSON from client-side data.
   */
  private String buildProjectSnapshot() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar == null) {
      return null;
    }
    DesignToolbar.DesignProject designProject = toolbar.getCurrentProject();
    if (designProject == null) {
      return null;
    }

    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(designProject.getProjectId());
    if (projectEditor == null) {
      return null;
    }

    try {
      StringBuilder json = new StringBuilder("{");

      // Project name
      json.append("\"projectName\":").append(AIJsonUtils.jsonString(designProject.name));

      // App name
      String appName = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME);
      json.append(",\"appName\":").append(AIJsonUtils.jsonString(
          appName != null ? appName : designProject.name));

      // Version name
      String versionName = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME);
      if (versionName != null && !versionName.isEmpty()) {
        json.append(",\"versionName\":").append(AIJsonUtils.jsonString(versionName));
      }

      // Theme
      String theme = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME);
      if (theme != null && !theme.isEmpty()) {
        json.append(",\"theme\":").append(AIJsonUtils.jsonString(theme));
      }

      // Sizing
      String sizing = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
      if (sizing != null && !sizing.isEmpty()) {
        json.append(",\"sizing\":").append(AIJsonUtils.jsonString(sizing));
      }

      // Colors
      String primaryColor = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR);
      if (primaryColor != null && !primaryColor.isEmpty()) {
        json.append(",\"primaryColor\":").append(AIJsonUtils.jsonString(primaryColor));
      }

      String accentColor = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR);
      if (accentColor != null && !accentColor.isEmpty()) {
        json.append(",\"accentColor\":").append(AIJsonUtils.jsonString(accentColor));
      }

      // Screen names
      json.append(",\"screenNames\":[");
      String screenSep = "";
      for (String screenName : designProject.screens.keySet()) {
        json.append(screenSep).append(AIJsonUtils.jsonString(screenName));
        screenSep = ",";
      }
      json.append("]");

      // Assets (excluding external_comps/)
      YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode)
          Ode.getInstance().getProjectManager().getProject(designProject.getProjectId())
              .getRootNode();
      if (projectNode.getAssetsFolder() != null) {
        json.append(",\"assets\":[");
        String assetSep = "";
        for (ProjectNode child : projectNode.getAssetsFolder().getChildren()) {
          String fileId = child.getFileId();
          if (!fileId.contains("/external_comps/")) {
            json.append(assetSep).append(AIJsonUtils.jsonString(child.getName()));
            assetSep = ",";
          }
        }
        json.append("]");
      }

      // Extensions
      if (projectEditor instanceof YaProjectEditor) {
        List<String> extensions = ((YaProjectEditor) projectEditor).getExternalComponents();
        if (!extensions.isEmpty()) {
          json.append(",\"extensions\":[");
          String extSep = "";
          for (String ext : extensions) {
            json.append(extSep).append(AIJsonUtils.jsonString(ext));
            extSep = ",";
          }
          json.append("]");
        }
      }

      // Screen summaries (only in ProjectEditor mode)
      String mode = getCurrentAIAgentMode();
      if ("ProjectEditor".equals(mode) && projectEditor instanceof YaProjectEditor) {
        YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
        String currentScreen = designProject.currentScreen;
        json.append(",\"screenSummaries\":{");
        String sumSep = "";
        for (String screenName : designProject.screens.keySet()) {
          if (!screenName.equals(currentScreen)) {
            DesignerEditor<?, ?, ?, ?, ?> formEditor =
                yaProjectEditor.getFormFileEditor(screenName);
            if (formEditor != null) {
              MockForm form = (MockForm) formEditor.getRoot();
              if (form != null) {
                int count = countComponentsRecursive(form);
                String title = form.getPropertyValue("Title");
                if (title == null || title.isEmpty()) {
                  title = screenName;
                }
                json.append(sumSep).append(AIJsonUtils.jsonString(screenName)).append(":{");
                json.append("\"componentCount\":").append(count);
                json.append(",\"title\":").append(AIJsonUtils.jsonString(title));
                json.append("}");
                sumSep = ",";
              }
            }
          }
        }
        json.append("}");
      }

      json.append("}");
      return json.toString();
    } catch (Exception e) {
      LOG.warning("Failed to build project snapshot: " + e.getMessage());
      return null;
    }
  }

  /**
   * Recursively counts the number of components in a MockComponent tree.
   */
  private int countComponentsRecursive(MockComponent component) {
    int count = 1;
    for (MockComponent child : component.getChildren()) {
      count += countComponentsRecursive(child);
    }
    return count;
  }
}
