// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for checking the startup form before compilation.
 *
 */
public class YoungAndroidCheckStartupFormCommand extends ChainableCommand {
  /**
   * Creates a check startup form command, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the check has finished
   */
  public YoungAndroidCheckStartupFormCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    ProjectSettings projectSettings = getProject(node).getSettings();
    Settings settings =
        projectSettings.getSettings(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
    String startupForm =
        settings.getPropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_STARTUP_FORM);
    String[] formValues = getFormNodes(node.getProjectRoot());

    // Make sure current startup form actually exists
    if (!StringUtils.contains(formValues, startupForm)) {
      startupForm = "";
    }

    // If there is no startup form already selected, select one.
    if (startupForm.isEmpty()) {
      // If there are any forms, automatically select the first one, otherwise report error.
      if (formValues.length > 0) {
        settings.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_STARTUP_FORM,
                                     formValues[0]);
        projectSettings.saveSettings(new Command() {
          @Override
          public void execute() {
            executeNextCommand(node);
          }
        });
      } else {
        // No forms at all (WTF?), abort mission.
        executionFailedOrCanceled();
      }
    } else {
      // Build project
      executeNextCommand(node);
    }
  }

  private static String[] getFormNodes(ProjectRootNode rootNode) {
    List<String> formChoices = new ArrayList<String>();
    for (ProjectNode sourceNode : rootNode.getAllSourceNodes()) {
      if (sourceNode instanceof YoungAndroidFormNode) {
        String qualifiedName = ((YoungAndroidFormNode) sourceNode).getQualifiedName();
        // If the form is Screen1, put it at the beginning of the list.
        if (qualifiedName.endsWith(".Screen1")) {
          formChoices.add(0, qualifiedName);
        } else {
          formChoices.add(qualifiedName);
        }
      }
    }
    return formChoices.toArray(new String[formChoices.size()]);
  }
}
