// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Command;

/**
 * Top-level Translation editor shown beside Designer and Blocks.
 */
public final class TranslationEditor extends FileEditor {
  public static final String EDITOR_TYPE = "TranslationEditor";
  public static final String ENTITY_NAME = "Translations";

  private final TranslationPanel translationPanel;

  public TranslationEditor(YaProjectEditor projectEditor, ProjectRootNode projectRootNode) {
    super(projectEditor, new TranslationFileNode(projectRootNode));
    translationPanel = new TranslationPanel(projectEditor);
    initWidget(translationPanel);
  }

  public TranslationPanel getTranslationPanel() {
    return translationPanel;
  }

  @Override
  public DropTargetProvider getDropTargetProvider() {
    return new DropTargetProvider() {
      @Override
      public DropTarget[] getDropTargets() {
        return new DropTarget[0];
      }
    };
  }

  @Override
  public void loadFile(Command afterFileLoaded) {
    if (afterFileLoaded != null) {
      afterFileLoaded.execute();
    }
  }

  @Override
  public String getTabText() {
    return "Translations";
  }

  @Override
  public void onShow() {
    super.onShow();
    translationPanel.refresh();
  }

  @Override
  public String getRawFileContent() {
    return "";
  }

  @Override
  public void onSave() {
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
  }

  @Override
  public String getEditorType() {
    return EDITOR_TYPE;
  }

  @Override
  public String getEntityName() {
    return ENTITY_NAME;
  }
}
