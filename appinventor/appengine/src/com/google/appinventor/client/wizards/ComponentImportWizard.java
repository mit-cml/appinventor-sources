// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.component.ComponentInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public class ComponentImportWizard extends Wizard {
  static class ComponentCell extends AbstractCell<ComponentInfo> {
    @Override
    public void render(Context context, ComponentInfo value, SafeHtmlBuilder sb) {
      sb.appendHtmlConstant("<table>");
      sb.appendHtmlConstant("<tr>");
      sb.appendEscaped(value.getName());
      sb.appendHtmlConstant("</tr>");
      sb.appendHtmlConstant("<tr>");
      sb.appendEscaped(new Long(value.getVersion()).toString());
      sb.appendHtmlConstant("</tr>");
      sb.appendHtmlConstant("</table>");
    }
  }

  public ComponentImportWizard() {
    super(MESSAGES.componentImportWizardCaption(), true, false);

    CellList<ComponentInfo> cellList = new CellList<ComponentInfo>(new ComponentCell());
    final SingleSelectionModel<ComponentInfo> selectionModel = new SingleSelectionModel<ComponentInfo>();
    cellList.setSelectionModel(selectionModel);

    ListDataProvider<ComponentInfo> dataProvider = new ListDataProvider<ComponentInfo>();
    for (ComponentInfo compInfo : Ode.getInstance().getComponentManager().getRetrivedComponentInfos()) {
      dataProvider.getList().add(compInfo);
    }
    dataProvider.addDataDisplay(cellList);

    VerticalPanel panel = new VerticalPanel();
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.add(cellList);

    addPage(panel);

    // todo: improve the ui
    setPagePanelHeight(40);
    setStylePrimaryName("ode-DialogBox");

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        ComponentInfo toImport = selectionModel.getSelectedObject();

        if (toImport == null) {
          Window.alert(MESSAGES.noComponentSelectedError());
          center();
          return;
        }

        final Ode ODE = Ode.getInstance();
        final long projectId = ODE.getCurrentYoungAndroidProjectId();
        ODE.getComponentService().importComponentToProject(toImport, projectId,
            new OdeAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                // todo: make use of the files in assets to do the importing
              }
        });
      }
    });
  }
}
