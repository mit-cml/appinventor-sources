// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.CommandRegistry;
import com.google.appinventor.client.explorer.commands.ProjectNodeCommand;
import com.google.appinventor.client.widgets.ContextMenu;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * A context menu for a project node.
 *
 */
public final class ProjectNodeContextMenu {

  private ProjectNodeContextMenu() {
  }

  /**
   * Shows a context menu for a node.
   *
   * @param node  node for which to show the context menu
   * @param host  widget to anchor context menu to
   */
  public static void show(final ProjectNode node, Widget host) {

    List<CommandRegistry.Entry> entries = Ode.getCommandRegistry().get(node);
    if (entries.isEmpty()) {
      return;
    }

    final ContextMenu menu = new ContextMenu();
    // Position the context menu to the East of the host widget.
    menu.setPopupPosition(host.getAbsoluteLeft() + host.getOffsetWidth(),
        host.getAbsoluteTop());
    for (final CommandRegistry.Entry entry : entries) {
      final ProjectNodeCommand cmd = entry.getCommand();

      // Create the menu item.
      menu.addItem(cmd.getLabel(), new Command() {
        @Override
        public void execute() {
          menu.hide();
          cmd.execute(node);
        }
      });
    }

    menu.show();
  }
}
