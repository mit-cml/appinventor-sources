// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
