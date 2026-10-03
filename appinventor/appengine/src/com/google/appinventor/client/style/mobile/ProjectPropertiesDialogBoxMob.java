package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.dialogs.ProjectPropertiesDialogBox;
import com.google.appinventor.client.wizards.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.concurrent.Flow;

public class ProjectPropertiesDialogBoxMob extends ProjectPropertiesDialogBox {

  interface ProjectPropertiesDialogBoxUIBinder extends UiBinder<Widget, ProjectPropertiesDialogBoxMob> {}


  @UiField
  Dialog projectProperties;

  @UiField
  ListBox categoryList;

  @UiField
  DeckPanel propertiesDeckPanel;

  @UiField
  Button closeDialogBox;

  @UiField
  Button topInvisible;

  @UiField
  Button bottomInvisible;

  public ProjectPropertiesDialogBoxMob(YaProjectEditor projectEditor) {
    super(projectEditor);
  }

  @Override
  public void bindUI() {
    ProjectPropertiesDialogBoxUIBinder uibinder = GWT.create(ProjectPropertiesDialogBoxUIBinder.class);
    uibinder.createAndBindUi(this);

    super.projectProperties = this.projectProperties;
    super.categoryList = this.categoryList;
    super.propertiesDeckPanel = this.propertiesDeckPanel;
    super.closeDialogBox = this.closeDialogBox;
    super.topInvisible = this.topInvisible;
    super.bottomInvisible = this.bottomInvisible;
  }

  @UiHandler("closeDialogBox")
  protected void handleClose(ClickEvent e) {
    super.handleClose(e);
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
    super.FocusLast(event);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
    super.FocusFirst(event);
  }
}
