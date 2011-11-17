// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.ProjectEditorFactory;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Project editor for Young Android projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YaProjectEditor extends ProjectEditor implements ProjectChangeListener {
  /**
   * Returns a project editor factory for {@code YaProjectEditor}s.
   *
   * @return a project editor factory for {@code YaProjectEditor}s.
   */
  public static ProjectEditorFactory getFactory() {
    return new ProjectEditorFactory() {
      @Override
      public ProjectEditor createProjectEditor(ProjectRootNode projectRootNode) {
        return new YaProjectEditor(projectRootNode);
      }
    };
  }

  public YaProjectEditor(ProjectRootNode projectRootNode) {
    super(projectRootNode);
    project.addProjectChangeListener(this);
  }

  @Override
  public void loadProject() {
    for (ProjectNode source : projectRootNode.getAllSourceNodes()) {
      if (source instanceof YoungAndroidFormNode) {
        addFormEditor((YoungAndroidFormNode) source);
      }
    }
  }

  @Override
  protected void onShow() {
    AssetListBox.getAssetListBox().getAssetList().refreshAssetList(projectId);

    // The superclass will call onShow for the selected file editor (a YaFormEditor).
    super.onShow();
  }

  @Override
  protected void onHide() {
    AssetListBox.getAssetListBox().getAssetList().refreshAssetList(0);

    // The superclass will call onHide for the selected file editor (a YaFormEditor).
    super.onHide();
  }

  /**
   * Returns a list of the YaFormEditors in this YaProjectEditor.
   */
  public List<YaFormEditor> getYaFormEditors() {
    List<YaFormEditor> list = new ArrayList<YaFormEditor>();
    for (FileEditor fileEditor : openFileEditors.values()) {
      list.add((YaFormEditor) fileEditor);
    }
    return list;
  }

  // ProjectChangeListener methods

  @Override
  public void onProjectLoaded(Project project) {
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidFormNode) {
      if (getFileEditor(node.getFileId()) == null) {
        addFormEditor((YoungAndroidFormNode) node);
      }
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
  }

  // Private methods

  private void addFormEditor(YoungAndroidFormNode formNode) {
    final YaFormEditor yaFormEditor = new YaFormEditor(this, formNode);
    yaFormEditor.loadFile(new Command() {
      @Override
      public void execute() {
        if (yaFormEditor.isScreen1()) {
          insertFileEditor(yaFormEditor, 0);
          selectFileEditor(yaFormEditor);
        } else {
          addFileEditor(yaFormEditor);
        }
      }
    });
  }
}
