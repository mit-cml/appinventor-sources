// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.cellview.client.CellTable;



/**
 * Wizard for uploading previously archived (downloaded) projects.
 *
 */
public class ProjectUploadWizard extends Wizard {
  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".aia";
  private static final int FROM_MY_COMPUTER_TAB = 0;
  private static final int URL_TAB = 1;

  /**
   * Creates a new project upload wizard.
   */
  public ProjectUploadWizard() {
    super(MESSAGES.projectUploadWizardCaption(), true, false);

    // Initialize UI
    final CellTable compTable = createCompTable(); //new
    final FileUpload upload = new FileUpload();
    final Grid urlGrid = createUrlGrid(); //new
    final TabPanel tabPanel = new TabPanel(); //new
    tabPanel.add(upload, MESSAGES.componentImportFromComputer());//new
    tabPanel.add(urlGrid, MESSAGES.componentImportFromURL());//new
    tabPanel.selectTab(FROM_MY_COMPUTER_TAB);//new
    tabPanel.addStyleName("ode-Tabpanel");//new
    //new 
    //commented out 
    // upload.setName(ServerLayout.UPLOAD_PROJECT_ARCHIVE_FORM_ELEMENT);
    // upload.getElement().setAttribute("accept", PROJECT_ARCHIVE_EXTENSION);
    // setStylePrimaryName("ode-DialogBox"); 
    VerticalPanel panel = new VerticalPanel();
    

    //panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE); COMMENTED OUT
    panel.add(tabPanel);
    //panel.add(upload); COMMENTED OUT 
    addPage(panel);

    getConfirmButton().setText("Import");
    setPagePanelHeight(150);
    setPixelSize(200, 150);
    setStylePrimaryName("ode-DialogBox");
    
    // Create finish command (upload a project archive)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String filename = upload.getFilename();
        if (tabPanel.getTabBar().getSelectedTab() == URL_TAB) {
          TextBox urlTextBox = (TextBox) urlGrid.getWidget(1, 0);
          String url = urlTextBox.getText();

          if (url.trim().isEmpty()) {
            Window.alert(MESSAGES.noUrlError());
            return;
          }
          
          NewProjectCommand callbackCommand = new NewProjectCommand() {
            @Override
            public void execute(Project project) {
              Ode.getInstance().openYoungAndroidProjectInDesigner(project);
            }
          };

          new TemplateUploadWizard().openProjectFromTemplate(url, callbackCommand);
        } else {
          if (filename.endsWith(PROJECT_ARCHIVE_EXTENSION)) {
            // Strip extension and leading path off filename. We need to support both Unix ('/') and
            // Windows ('\\') path separators. File.pathSeparator is not available in GWT.
            filename = filename.substring(0, filename.length() - PROJECT_ARCHIVE_EXTENSION.length()).
                substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

            // Make sure the project name is legal and unique.
            if (!TextValidators.checkNewProjectName(filename)) {
              return;
            }

            String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
                ServerLayout.UPLOAD_PROJECT + "/" + filename;
            Uploader.getInstance().upload(upload, uploadUrl,
                new OdeAsyncCallback<UploadResponse>(
                    // failure message
                    MESSAGES.projectUploadError()) {
                  @Override
                  public void onSuccess(UploadResponse uploadResponse) {
                    switch (uploadResponse.getStatus()) {
                      case SUCCESS:
                        String info = uploadResponse.getInfo();
                        UserProject userProject = UserProject.valueOf(info);
                        Ode ode = Ode.getInstance();
                        Project uploadedProject = ode.getProjectManager().addProject(userProject);
                        ode.openYoungAndroidProjectInDesigner(uploadedProject);
                        break;
                      case NOT_PROJECT_ARCHIVE:
                        // This may be a "severe" error; but in the
                        // interest of reducing the number of red errors, the 
                        // line has been changed to report info not an error.
                        // This error is triggered when the user attempts to
                        // upload a zip file that is not a project.
                        ErrorReporter.reportInfo(MESSAGES.notProjectArchiveError());
                        break;
                      default:
                        ErrorReporter.reportError(MESSAGES.projectUploadError());
                        break;
                    }
                  }
                });
          } else {
            Window.alert(MESSAGES.notProjectArchiveError());
            center();
          }
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 320;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(40);
  }

  private Grid createUrlGrid() {
    TextBox urlTextBox = new TextBox();
    urlTextBox.setWidth("100%");
    Grid grid = new Grid(2, 1);
    grid.setWidget(0, 0, new Label("Url:"));
    grid.setWidget(1, 0, urlTextBox);
    return grid;
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
}
