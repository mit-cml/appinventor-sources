package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.OdeMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Composite;

public class ProjectsExplorer extends Composite {
  interface ProjectsExplorerUiBinder extends UiBinder<FlowPanel, ProjectsExplorer> {}
  private static final ProjectsExplorerUiBinder UI_BINDER = GWT.create(ProjectsExplorerUiBinder.class);

  public ProjectsExplorer() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }
}
