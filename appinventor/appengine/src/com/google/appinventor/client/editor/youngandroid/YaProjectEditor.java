// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.ProjectEditorFactory;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.gwt.user.client.Command;

import java.util.Collections;
import java.util.Comparator;

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
      } else if (source instanceof YoungAndroidBlocksNode) {
        addBlocksEditor((YoungAndroidBlocksNode) source);
      }
    }
  }

  @Override
  protected void onShow() {
    AssetListBox.getAssetListBox().getAssetList().refreshAssetList(projectId);

    // The superclass will call onShow for the selected file editor (a YaFormEditor or a
    // YaBlocksEditor).
    super.onShow();
  }

  @Override
  protected void onHide() {
    AssetListBox.getAssetListBox().getAssetList().refreshAssetList(0);

    // The superclass will call onHide for the selected file editor (a YaFormEditor or a
    // YaBlocksEditor).
    super.onHide();
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
    } else if (node instanceof YoungAndroidBlocksNode) {
      if (getFileEditor(node.getFileId()) == null) {
        addBlocksEditor((YoungAndroidBlocksNode) node);
      }
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
  }

  // Private methods

  private static Comparator getFileIdComparator() {
    // File editors (YaFormEditors and YaBlocksEditors) are sorted so that Screen1 always comes
    // first and others are in alphabetical order. Within each pair, the YaFormEditor is
    // immediately before the YaBlocksEditor.
    return new Comparator<String>() {
      @Override
      public int compare(String fileId1, String fileId2) {
        boolean isForm1 = fileId1.endsWith(YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION);
        boolean isForm2 = fileId2.endsWith(YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION);

        // Give priority to screen1.
        if (YoungAndroidSourceNode.isScreen1(fileId1)) {
          if (YoungAndroidSourceNode.isScreen1(fileId2)) {
            // They are both named screen1. The form editor should come before the blocks editor.
            if (isForm1) {
              return isForm2 ? 0 : -1;
            } else {
              return isForm2 ? 1 : 0;
            }
          } else {
            // Only fileId1 is named screen1.
            return -1;
          }
        } else if (YoungAndroidSourceNode.isScreen1(fileId2)) {
          // Only fileId2 is named screen1.
          return 1;
        }

        String fileId1WithoutExtension = StorageUtil.trimOffExtension(fileId1);
        String fileId2WithoutExtension = StorageUtil.trimOffExtension(fileId2);
        int compare = fileId1WithoutExtension.compareTo(fileId2WithoutExtension);
        if (compare != 0) {
          return compare;
        }
        // They are both the same name without extension. The form editor should come before the
        // blocks editor.
        if (isForm1) {
          return isForm2 ? 0 : -1;
        } else {
          return isForm2 ? 1 : 0;
        }
      }
    };
  }

  private void addFormEditor(YoungAndroidFormNode formNode) {
    final YaFormEditor newFormEditor = new YaFormEditor(this, formNode);
    newFormEditor.loadFile(new Command() {
      @Override
      public void execute() {
        int pos = Collections.binarySearch(fileIds, newFormEditor.getFileId(),
            getFileIdComparator());
        if (pos < 0) {
          pos = -pos - 1;
        }
        insertFileEditor(newFormEditor, pos);

        // Automatically select the Screen1 form editor when it is added.
        if (newFormEditor.isScreen1()) {
          selectFileEditor(newFormEditor);
        }
      }
    });
  }

  private void addBlocksEditor(YoungAndroidBlocksNode blocksNode) {
    final YaBlocksEditor newBlocksEditor = new YaBlocksEditor(this, blocksNode);
    newBlocksEditor.loadFile(new Command() {
      @Override
      public void execute() {
        int pos = Collections.binarySearch(fileIds, newBlocksEditor.getFileId(),
            getFileIdComparator());
        if (pos < 0) {
          pos = -pos - 1;
        }
        insertFileEditor(newBlocksEditor, pos);
      }
    });
  }
}
