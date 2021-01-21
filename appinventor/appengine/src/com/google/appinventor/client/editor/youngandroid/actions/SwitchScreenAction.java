package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class SwitchScreenAction implements Command {
  private final long projectId;
  private final String name;  // screen name

  public SwitchScreenAction(long projectId, String screenName) {
    this.projectId = projectId;
    this.name = screenName;
  }

  @Override
  public void execute() {
    // If we are in the blocks view, we should take a screenshot
    // of the blocks as we switch to a different screen
    final DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar.getCurrentView() == DesignToolbar.View.BLOCKS) {
      Ode.getInstance().screenShotMaybe(new Runnable() {
        @Override
        public void run() {
          toolbar.switchToScreen(projectId, name, toolbar.getCurrentView());
        }
      }, false);
    } else {
      toolbar.switchToScreen(projectId, name, toolbar.getCurrentView());
    }
  }
}
