package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;
import java.util.logging.Logger;

public class SwitchToFormEditorAction implements Command {
  private static final Logger LOG = Logger.getLogger(SwitchToFormEditorAction.class.getName());

  @Override
  public void execute() {
    final DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar.getCurrentProject() == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
          + "Ignoring SwitchToFormEditorAction.execute().");
      return;
    }
    if (toolbar.currentView != DesignToolbar.View.FORM) {
      // We are leaving a blocks editor, so take a screenshot
      Ode.getInstance().screenShotMaybe(new Runnable() {
        @Override
        public void run() {
          long projectId = Ode.getInstance().getCurrentYoungAndroidProjectRootNode().getProjectId();
          toolbar.switchToScreen(projectId, toolbar.getCurrentProject().currentScreen, DesignToolbar.View.FORM);
          toolbar.toggleEditor(false);      // Gray out the Designer button and enable the blocks button
          Ode.getInstance().getTopToolbar().updateFileMenuButtons(1);
        }
      }, false);
    }
  }
}
