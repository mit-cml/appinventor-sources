package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.gwt.user.client.Command;

public class ProjectPropertiesAction implements Command {
    @Override
    public void execute() {
      YaProjectEditor projectEditor = (YaProjectEditor)Ode.getInstance().getEditorManager()
      .getOpenProjectEditor(Ode.getInstance().getCurrentYoungAndroidProjectId());
      projectEditor.openProjectPropertyDialog();
    }
}
