// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ComponentListBox;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.shared.rpc.component.Component;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.List;

/**
 * The component toolbar houses command buttons in the Component tab.
 *
 */
public class ComponentToolbar extends Toolbar {
  private static final String WIDGET_NAME_DELETE_COMPONENT = "DeleteComponent";

  public ComponentToolbar() {
    addButton(new ToolbarItem(WIDGET_NAME_DELETE_COMPONENT,
        MESSAGES.deleteComponentButton(), new DeleteComponentAction()));
  }

  public void updateButtons() {
    ComponentList compList = ComponentListBox.getInstance().getComponentList();
    int numSelectedComponents = compList.getNumSelectedComponents();
    setButtonEnabled(WIDGET_NAME_DELETE_COMPONENT, numSelectedComponents > 0);
  }

  private static class DeleteComponentAction implements Command {
    @Override
    public void execute() {
      List<Component> selectedComps = ComponentListBox.getInstance().
          getComponentList().getSelectedComponents();

      if (!deleteConfirmation(selectedComps)) {
        return;
      }

      for (final Component comp : selectedComps) {
        Ode.getInstance().getComponentService().deleteComponent(comp,
            new OdeAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                Ode.getInstance().getComponentManager().removeComponent(comp);
              }
            });
      }
    }

    private boolean deleteConfirmation(List<Component> comps) {
      StringBuilder compNames = new StringBuilder();
      String separator = "";
      for (Component comp : comps) {
        compNames.append(separator).append(comp.getName());
        separator = ", ";
      }

      return Window.confirm(MESSAGES.confirmDeleteComponents(compNames.toString()));
    }
  }
}
