// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.component.ComponentInfo;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.CheckboxCell;

public class ComponentImportWizard extends Wizard {
  private final Ode ode = Ode.getInstance();

  public ComponentImportWizard() {
    super(MESSAGES.componentImportWizardCaption(), true, false);

    final CellTable compTable = createCompTable();

    ListDataProvider<ComponentInfo> dataProvider = new ListDataProvider<ComponentInfo>();
    for (ComponentInfo compInfo : ode.getComponentManager().getRetrivedComponentInfos()) {
      dataProvider.getList().add(compInfo);
    }
    dataProvider.addDataDisplay(compTable);

    VerticalPanel panel = new VerticalPanel();
    panel.add(compTable);

    addPage(panel);

    setPagePanelHeight(400);
    setPixelSize(400, 400);
    setStylePrimaryName("ode-DialogBox");

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        SingleSelectionModel<ComponentInfo> selectionModel =
            (SingleSelectionModel<ComponentInfo>) compTable.getSelectionModel();
        ComponentInfo toImport = selectionModel.getSelectedObject();

        if (toImport == null) {
          Window.alert(MESSAGES.noComponentSelectedError());
          center();
          return;
        }

        final long projectId = ode.getCurrentYoungAndroidProjectId();
        final Project project = ode.getProjectManager().getProject(projectId);
        final YoungAndroidAssetsFolder assetsFolderNode =
            ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();

        ode.getComponentService().importComponentToProject(
            toImport,
            projectId,
            assetsFolderNode.getFileId(),
            new OdeAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                // to be implemented
              }
            });
      }
    });
  }

  private CellTable createCompTable() {
    final SelectionModel<ComponentInfo> selectionModel =
        new SingleSelectionModel<ComponentInfo>();

    CellTable<ComponentInfo> compTable = new CellTable<ComponentInfo>();
    compTable.setSelectionModel(selectionModel);

    Column<ComponentInfo, Boolean> checkColumn = new Column<ComponentInfo, Boolean>(
        new CheckboxCell(true, false)) {
          @Override
          public Boolean getValue(ComponentInfo object) {
            return selectionModel.isSelected(object);
          }
        };
    Column<ComponentInfo, String> nameColumn = new Column<ComponentInfo, String>(
        new TextCell()) {
          @Override
          public String getValue(ComponentInfo compInfo) {
            return compInfo.getName();
          }
        };
    Column<ComponentInfo, Number> versionColumn = new Column<ComponentInfo, Number>(
        new NumberCell()) {
          @Override
          public Number getValue(ComponentInfo compInfo) {
            return compInfo.getVersion();
          }
        };

    compTable.addColumn(checkColumn);
    compTable.addColumn(nameColumn, "Component");
    compTable.addColumn(versionColumn, "Version");

    return compTable;
  }
}
