// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;

/**
 * Virtual file node used only to mount the Translation editor into the ProjectEditor deck.
 *
 * This does not represent a real persisted project file yet. Persistence will come later
 * when we decide whether i18n data lives in project settings, .scm, or a dedicated file.
 */
final class TranslationFileNode extends FileNode {
  private static final long serialVersionUID = 1L;

  private final ProjectRootNode projectRootNode;

  TranslationFileNode(ProjectRootNode projectRootNode) {
    super("Translations", projectRootNode.getProjectId() + "_translations");
    this.projectRootNode = projectRootNode;
  }

  @Override
  public ProjectRootNode getProjectRoot() {
    return projectRootNode;
  }

  @Override
  public long getProjectId() {
    return projectRootNode.getProjectId();
  }

  @Override
  public String getProjectType() {
    return projectRootNode.getProjectType();
  }
}
