// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.gwt.user.client.Command;
import java.util.logging.Logger;

public class SwitchToBlocksEditorAction implements Command {
  private static final Logger LOG = Logger.getLogger(SwitchToBlocksEditorAction.class.getName());

  @Override
  public void execute() {
    final DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar.getCurrentProject() == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
          + "Ignoring SwitchToBlocksEditorAction.execute().");
      return;
    }
    if (toolbar.getCurrentView() != DesignToolbar.View.BLOCKS) {
      long projectId = Ode.getInstance().getCurrentYoungAndroidProjectRootNode().getProjectId();
      toolbar.switchToScreen(projectId, toolbar.getCurrentProject().currentScreen, DesignToolbar.View.BLOCKS);
      toolbar.toggleEditor(true);       // Gray out the blocks button and enable the designer button
      Ode.getInstance().getTopToolbar().updateFileMenuButtons(1);
    }
  }
}
