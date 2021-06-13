package com.google.appinventor.client;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.views.projects.ProjectsExplorer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;

import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

public final class ModuleController {

  private static ModuleController INSTANCE = new ModuleController();

  private Widget legacyLayout;
  private DeckPanel rootPanel;
  private DeckPanel modulesPanel;
  private final int MODULES_PANEL_INDEX = 0;
  private final int LEGACY_PANEL_INDEX = 1;

  private ProjectsExplorer projectsExplorerModule;
  private int projectsExplorerIndex;

  private ModuleController() {
    rootPanel =  new DeckPanel();
    rootPanel.setAnimationEnabled(false);
    rootPanel.setStylePrimaryName("ode-ModuleControllerRoot");
    modulesPanel = new DeckPanel();
    modulesPanel.setAnimationEnabled(false);
    rootPanel.add(modulesPanel);
  }

  public static ModuleController get() {
    return INSTANCE;
  }

  public Widget wrapAroundLegacyLayout(Widget legacyLayout) {
    if(this.legacyLayout == null) {
      this.legacyLayout = legacyLayout;
    }
    rootPanel.add(legacyLayout);
    rootPanel.showWidget(LEGACY_PANEL_INDEX);
    return rootPanel;
  }

  public void displayLegacyLayout() {
    rootPanel.showWidget(LEGACY_PANEL_INDEX);
  }

  public void displayModularLayouts() {
    rootPanel.showWidget(MODULES_PANEL_INDEX);
  }

  public void switchToProjectsModule() {
    if(projectsExplorerModule == null) {
      GWT.runAsync(new RunAsyncCallback() {
        public void onFailure(Throwable caught) {
        }

        public void onSuccess() {
          initializeProjectsExplorerModule();
          modulesPanel.showWidget(projectsExplorerIndex);
        }
      });
    } else {
      modulesPanel.showWidget(projectsExplorerIndex);
    }
  }

  private void initializeProjectsExplorerModule() {
    projectsExplorerModule = new ProjectsExplorer();
    projectsExplorerIndex = modulesPanel.getWidgetCount();
    modulesPanel.add(projectsExplorerModule);
  }

  public void getProjectsModule(final OdeAsyncCallback<ProjectsExplorer> callback) {
    GWT.runAsync(new RunAsyncCallback() {
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess() {
        if(projectsExplorerModule == null) {
          initializeProjectsExplorerModule();
        }
        callback.onSuccess(projectsExplorerModule);
      }
    });
  }
}
