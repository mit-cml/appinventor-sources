// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dialog;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;

import java.util.ArrayList;
import java.util.List;

/**
 * A command that renames one or more projects/folders.
 */
public final class RenameProjectsCommand implements Command {
  interface RenameProjectsCommandUiBinder extends UiBinder<Dialog, RenameProjectsCommand> {}
  private static final RenameProjectsCommandUiBinder UI_BINDER = GWT.create(RenameProjectsCommandUiBinder.class);

  @UiField Dialog renameDialog;
  @UiField Button renameButton;
  @UiField Button cancelButton;
  @UiField TextBox input;

  @UiField(provided=true)
  Resources.Style style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.styleDark() : Resources.INSTANCE.styleLight();

  private List<Project> projects;
  private List<Folder> folders;

  /**
   * Creates a new command for renaming projects
   */
  public RenameProjectsCommand(List<Project> projects, List<Folder> folders) {
    style.ensureInjected();
    UI_BINDER.createAndBindUi(this);

    this.projects = projects;
    this.folders = folders;
  }

  @UiHandler("cancelButton")
  void cancelRename(ClickEvent e) {
    renameDialog.hide();
  }

  @UiHandler("renameButton")
  void startRename(ClickEvent e) {
    renameDialog.hide();
    List<String> projectNames = new ArrayList<String>();
    List<Long> projectIds = new ArrayList<Long>();
    List<String> folderNames = new ArrayList<String>();
    List<Folder> foldersToRename = new ArrayList<Folder>();
    makeProjectNames(projectNames, projectIds);
    makeFolderNames(folderNames, foldersToRename);
    Ode.getInstance().getFolderManager().renameFolders(folderNames, foldersToRename);
    OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(MESSAGES.malformedProjectNameError()) {
      @Override
      public void onSuccess(Void result) {
        Window.Location.reload();
      }
    };

    if (projectNames.size() > 0) {
      Ode.getInstance().getProjectService().renameProjects(projectIds, projectNames, callback);
    } else {
      Window.Location.reload();
    }
  }

  @Override
  public void execute() {
    renameDialog.center();
  }

  private void makeProjectNames(List<String> newNames, List<Long> projectIds) {
    for (Project project : projects) {
      String nameCandidate = makeName(input.getValue(), project.getProjectName(), null);

      if (project.getProjectName().equals(nameCandidate)) {
        continue;
      }
      newNames.add(nameCandidate);
      projectIds.add(project.getProjectId());
    }
  }

  private void makeFolderNames(List<String> newNames, List<Folder> foldersToRename) {
    for (Folder folder : folders) {
      String nameCandidate = makeName(input.getValue(), folder.getName(), folder);

      if (folder.getName().equals(nameCandidate)) {
        continue;
      }
      newNames.add(nameCandidate);
      foldersToRename.add(folder);
    }
  }

  private String makeName(String nameCandidate, String currentName, Folder folder) {
    String[] nameCandidateSegments = nameCandidate.split("\\+");
    if (nameCandidateSegments.length == 2) {
      nameCandidate = nameCandidateSegments[0] + currentName + nameCandidateSegments[1];
    }

    int uniquifier = 0;
    if (folder == null) {
      while (TextValidators.checkNewProjectName(nameCandidate + (uniquifier == 0 ? "" : "_" + uniquifier), true)
          != TextValidators.ProjectNameStatus.SUCCESS) {
        uniquifier++;
      }
    } else {
      while (TextValidators.checkNewFolderName(nameCandidate + (uniquifier == 0 ? "" : "_" + uniquifier), folder.getParentFolder())
          != TextValidators.ProjectNameStatus.SUCCESS) {
        uniquifier++;
      }
    }
    return nameCandidate;
  }

  public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/light.css",
      "com/google/appinventor/client/explorer/commands/renameProjectsCommand.css"
    })
    Style styleLight();

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/dark.css",
      "com/google/appinventor/client/explorer/commands/renameProjectsCommand.css"
    })
    Style styleDark();

    public interface Style extends CssResource {
      String text();
      String actions();
      String input();
    }
  }
}
