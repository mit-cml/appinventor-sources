// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.allen_sauer.gwt.dnd.client.util.StringUtil;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.List;

public class ComponentImportWizard extends Wizard {

  final static String external_components = "assets/external_comps/";

  private static class ImportComponentCallback extends OdeAsyncCallback<ComponentImportResponse> {
    @Override
    public void onSuccess(ComponentImportResponse response) {
      if (response.getStatus() == ComponentImportResponse.Status.FAILED){
        Window.alert(MESSAGES.componentImportError());
        return;
      }
      else if (response.getStatus() != ComponentImportResponse.Status.IMPORTED &&
          response.getStatus() != ComponentImportResponse.Status.UPGRADED) {
        Window.alert(MESSAGES.componentImportError());
        return;
      }
      else if (response.getStatus() == ComponentImportResponse.Status.UNKNOWN_URL) {
        Window.alert(MESSAGES.componentImportUnknownURLError());
      }
      else if (response.getStatus() == ComponentImportResponse.Status.UPGRADED) {
        String componentName = SimpleComponentDatabase.getInstance().getComponentName(response.getComponentType());
        Window.alert(MESSAGES.componentUpgradedAlert() + componentName + " !");
      }

      List<ProjectNode> compNodes = response.getNodes();
      long destinationProjectId = response.getProjectId();
      long currentProjectId = ode.getCurrentYoungAndroidProjectId();
      if (currentProjectId != destinationProjectId) {
        return; // User switched project early!
      }
      Project project = ode.getProjectManager().getProject(destinationProjectId);
      if (project == null) {
        return; // Project does not exist!
      }
      if (response.getStatus() == ComponentImportResponse.Status.UPGRADED) {
        YoungAndroidComponentsFolder componentsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getComponentsFolder();
        YaProjectEditor projectEditor = (YaProjectEditor) ode.getEditorManager().getOpenProjectEditor(destinationProjectId);
        if (projectEditor == null) {
          return; // Project is not open!
        }
        for (ProjectNode node : compNodes) {
          project.addNode(componentsFolder, node);
          if (node.getName().equals("component.json") && StringUtils.countMatches(node.getFileId(), "/") == 3) {
            projectEditor.addComponent(node, null);
          }
        }

      } else if (response.getStatus() == ComponentImportResponse.Status.IMPORTED) {
        for (ProjectNode node : compNodes) {
          if (node.getName().equals("component.json") && StringUtils.countMatches(node.getFileId(), "/") == 3) {
            String fileId = node.getFileId();
            int start = fileId.indexOf(external_components) + external_components.length();
            int end = fileId.indexOf('/', start);
            String typeName = fileId.substring(start, end);
            new ComponentRenameWizard(typeName, destinationProjectId, compNodes).center();

          }
        }
      }



    }
  }

  private static int FROM_MY_COMPUTER_TAB = 0;
  private static int URL_TAB = 1;

  private static final String COMPONENT_ARCHIVE_EXTENSION = ".aix";

  private static final Ode ode = Ode.getInstance();

  public ComponentImportWizard() {
    super(MESSAGES.componentImportWizardCaption(), true, false);

    final CellTable compTable = createCompTable();
    final FileUpload fileUpload = createFileUpload();
    final Grid urlGrid = createUrlGrid();
    final TabPanel tabPanel = new TabPanel();
    tabPanel.add(fileUpload, "From my computer");
    tabPanel.add(urlGrid, "URL");
    tabPanel.selectTab(FROM_MY_COMPUTER_TAB);
    tabPanel.addStyleName("ode-Tabpanel");

    VerticalPanel panel = new VerticalPanel();
    panel.add(tabPanel);

    addPage(panel);

    getConfirmButton().setText("Import");

    setPagePanelHeight(150);
    setPixelSize(200, 150);
    setStylePrimaryName("ode-DialogBox");

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        final long projectId = ode.getCurrentYoungAndroidProjectId();
        final Project project = ode.getProjectManager().getProject(projectId);
        final YoungAndroidAssetsFolder assetsFolderNode =
            ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();

        if (tabPanel.getTabBar().getSelectedTab() == URL_TAB) {
          TextBox urlTextBox = (TextBox) urlGrid.getWidget(1, 0);
          String url = urlTextBox.getText();

          if (url.trim().isEmpty()) {
            Window.alert(MESSAGES.noUrlError());
            return;
          }

          ode.getComponentService().importComponentToProject(url, projectId,
              assetsFolderNode.getFileId(), new ImportComponentCallback());
        } else if (tabPanel.getTabBar().getSelectedTab() == FROM_MY_COMPUTER_TAB) {
          if (!fileUpload.getFilename().endsWith(COMPONENT_ARCHIVE_EXTENSION)) {
            Window.alert(MESSAGES.notComponentArchiveError());
            return;
          }

          String url = GWT.getModuleBaseURL() +
            ServerLayout.UPLOAD_SERVLET + "/" +
            ServerLayout.UPLOAD_COMPONENT + "/" +
            trimLeadingPath(fileUpload.getFilename());

          Uploader.getInstance().upload(fileUpload, url,
            new OdeAsyncCallback<UploadResponse>() {
              @Override
              public void onSuccess(UploadResponse uploadResponse) {
                String toImport = uploadResponse.getInfo();
                ode.getComponentService().importComponentToProject(toImport, projectId,
                    assetsFolderNode.getFileId(), new ImportComponentCallback());
              }
            });
          return;
        }
      }

      private String trimLeadingPath(String filename) {
        // Strip leading path off filename.
        // We need to support both Unix ('/') and Windows ('\\') separators.
        return filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
      }
    });
  }

  private CellTable createCompTable() {
    final SingleSelectionModel<Component> selectionModel =
        new SingleSelectionModel<Component>();

    CellTable<Component> compTable = new CellTable<Component>();
    compTable.setSelectionModel(selectionModel);

    Column<Component, Boolean> checkColumn =
        new Column<Component, Boolean>(new CheckboxCell(true, false)) {
          @Override
          public Boolean getValue(Component comp) {
            return selectionModel.isSelected(comp);
          }
        };
    Column<Component, String> nameColumn =
        new Column<Component, String>(new TextCell()) {
          @Override
          public String getValue(Component comp) {
            return comp.getName();
          }
        };
    Column<Component, Number> versionColumn =
        new Column<Component, Number>(new NumberCell()) {
          @Override
          public Number getValue(Component comp) {
            return comp.getVersion();
          }
        };

    compTable.addColumn(checkColumn);
    compTable.addColumn(nameColumn, "Component");
    compTable.addColumn(versionColumn, "Version");

    return compTable;
  }

  private Grid createUrlGrid() {
    TextBox urlTextBox = new TextBox();
    urlTextBox.setWidth("100%");
    Grid grid = new Grid(2, 1);
    grid.setWidget(0, 0, new Label("Url:"));
    grid.setWidget(1, 0, urlTextBox);
    return grid;
  }

  private FileUpload createFileUpload() {
    FileUpload upload = new FileUpload();
    upload.setName(ServerLayout.UPLOAD_COMPONENT_ARCHIVE_FORM_ELEMENT);
    return upload;
  }

}
