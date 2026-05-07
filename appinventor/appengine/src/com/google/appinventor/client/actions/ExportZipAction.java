// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.jzip.GenerateOptions;
import com.google.appinventor.client.jzip.JSZip;
import com.google.appinventor.client.jzip.Type;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Command;

/**
 * Exports the current project as a zip file.
 */
public class ExportZipAction implements Command {
  @Override
  public void execute() {
    Project project = Ode.getCurrentProject();
    JSZip zip = new JSZip();
    YoungAndroidProjectNode node = (YoungAndroidProjectNode) project.getRootNode();
    writeNode(node, zip);
    zip.generateAsync(new GenerateOptions(Type.ARRAY_BUFFER)).then(
        buffer -> {
          log(buffer);
          return null;
        });
  }

  private void writeNode(ProjectNode node, JSZip zip) {
    if (node instanceof FolderNode) {
      for (ProjectNode child : node.getChildren()) {
        writeNode(child, zip);
      }
    } else {
      zip.putFile(node.getFileId(), node.getFileId());
    }
  }

  private native void log(Object o)/*-{
    console.log(o);
  }-*/;
}
