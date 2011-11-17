// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import com.google.appinventor.client.editor.ProjectEditorFactory;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

/**
 * Registry of project editor factories by project type.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ProjectEditorRegistry extends Registry<ProjectRootNode, ProjectEditorFactory> {

  public ProjectEditorRegistry() {
    super(ProjectRootNode.class);

    register(YoungAndroidProjectNode.class, YaProjectEditor.getFactory());
  }
}
