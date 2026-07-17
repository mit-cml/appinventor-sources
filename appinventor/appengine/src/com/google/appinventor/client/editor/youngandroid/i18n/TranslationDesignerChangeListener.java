// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;

import java.util.logging.Logger;

public final class TranslationDesignerChangeListener implements DesignerChangeListener {
  private final String formName;
  private final TranslationPanel translationPanel;

  private static final Logger LOG =
      Logger.getLogger(TranslationDesignerChangeListener.class.getName());

  public TranslationDesignerChangeListener(String formName, TranslationPanel translationPanel) {
    this.formName = formName;
    this.translationPanel = translationPanel;
  }

  @Override
  public void onComponentPropertyChanged(MockComponent component, String propertyName,
      String propertyValue) {
    if (isTranslatableProperty(propertyName)) {
      translationPanel.refresh();
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (!permanentlyDeleted) {
      return;
    }

    translationPanel.handleComponentRemoved(formName, component.getName());
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    translationPanel.refresh();
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    translationPanel.handleComponentRenamed(formName, oldName, component.getName());
    LOG.info("i18n rename listener: form=" + formName
        + " oldName=" + oldName
        + " newName=" + component.getPropertyValue(MockComponent.PROPERTY_NAME_NAME));
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
  }

  private boolean isTranslatableProperty(String propertyName) {
    return "Text".equals(propertyName)
        || "Hint".equals(propertyName)
        || "Title".equals(propertyName)
        || "Prompt".equals(propertyName);
  }
}