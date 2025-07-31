// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.dialogs;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;

import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.wizards.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent; 
import com.google.gwt.event.dom.client.ChangeHandler; 
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A dialog for updating project properties that can be open from any screen
 */
public class ProjectPropertiesDialogBox { 

  interface ProjectPropertiesDialogBoxUiBinder extends UiBinder<Widget, ProjectPropertiesDialogBox> {
  }

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

  /**
   * List Of project properties category, which will be used to group properties in the dialog 
   * properties category are : General, Theming, Publishing
   */
  private static final Map<String, String> projectCategories = new LinkedHashMap<>();

  static {
    projectCategories.put("General", MESSAGES.projectPropertyGeneralCategoryTitle());
    projectCategories.put("Theming", MESSAGES.projectPropertyThemingCategoryTitle());
    projectCategories.put("Publishing", MESSAGES.projectPropertyPublishingCategoryTitle());
  }

  /**
   * Maps the project property category to List of EditableProperty which
   * belongs to that particular project property category
   */
  private HashMap<String, List<EditableProperty>> categoryToProperties = new HashMap<>();

  /* Object for storing reference of project editor in which the dialog opened */
  private YaProjectEditor projectEditor;

  /* refers to the screen name in which dialog opened */
  private String currentScreen = "";

  /**
   * Show the project property dialog
   * 
   * @param screenName name of the screen in which dialog needs to open
   */
  public void showDialog(String screenName) {
    currentScreen = screenName;
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
          projectProperties.center();
          categoryList.setFocus(true);
      }
    });   
  }

  public ProjectPropertiesDialogBox(YaProjectEditor projectEditor) {
    ProjectPropertiesDialogBoxUiBinder uibinder = GWT.create(ProjectPropertiesDialogBoxUiBinder.class);
    uibinder.createAndBindUi(this);
    projectProperties.setAutoHideEnabled(false);
    projectProperties.setModal(false);
    projectProperties.setCaption(MESSAGES.projectPropertiesText());

    categoryList.getElement().getStyle().setProperty("height", "400px");

    // Get current instance of YaProjectEditor
    this.projectEditor = projectEditor;

    MockForm form = (MockForm) projectEditor.getFormFileEditor("Screen1").getRoot();

    // Get project properties from the screen1 MockForm
    EditableProperties editableProperties = form.getProperties();
    Iterator<EditableProperty> properties = editableProperties.iterator();

    // Iterate and put the editable property to the corresponding category in categoryToProperties
    while (properties.hasNext()) {
      EditableProperty property = properties.next();

      if (!categoryToProperties.containsKey(property.getCategory())) {
        categoryToProperties.put(property.getCategory(), new ArrayList<EditableProperty>());
      } 

      categoryToProperties.get(property.getCategory()).add(property);
    }

    // Add the Categories to ListBox - categoryList
    for (Map.Entry<String, String> categoryTitle : projectCategories.entrySet()) {
      categoryList.addItem(categoryTitle.getValue());
      propertiesDeckPanel.add(getPanel(categoryTitle.getKey()));
    }

    // When category is changed by the user, display related properties
    categoryList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        propertiesDeckPanel.showWidget(categoryList.getSelectedIndex());
      }
    });

    categoryList.setVisibleItemCount(projectCategories.size());
      
    // When dialog is opened, properties related to the General category is shown
    propertiesDeckPanel.showWidget(0);
  }

  /**
   * Build vertical panel for each categories
   * 
   * @param category indicates the category for which we need to build the vertical panel
   * @return vertical panel which contains the all the Editable Property belongs to the particualt category passed as argument
   */
  private FlowPanel getPanel(String category) {
    // Main 
    FlowPanel propertiesContainer = new FlowPanel();
    propertiesContainer.setStyleName("ode-propertyDialogVerticalPanel");

    List<EditableProperty> properties = categoryToProperties.get(category);

    for (EditableProperty property : properties) {
      // container for displaing one editable property
      FlowPanel propertyContainer = new FlowPanel();
      propertyContainer.setStyleName("ode-propertyDialogPropertyContainer");

      // name of the EditableProperty
      Label name = new Label(ComponentTranslationTable.getPropertyName(property.getName()));
      name.setStyleName("ode-propertyDialogPropertyTitle");

      // Description of the property
      HTML description = new HTML(ComponentTranslationTable.getPropertyDescription(property.getDescription()));
      description.setStyleName("ode-propertyDialogPropertyDescription");

      // editor of the editor
      PropertyEditor editor = property.getEditor();
      editor.setStyleName("ode-propertyDialogPropertyEditor");

      // add to the container
      propertyContainer.add(name);
      propertyContainer.add(description);
      propertyContainer.add(editor);

      // add to the main container
      propertiesContainer.add(propertyContainer);
    }  
    
    return propertiesContainer;
  }

  void applyPropertyChanges() {
    if (!"Screen1".equals(currentScreen)) {
      MockForm currentform = (MockForm) projectEditor.getFormFileEditor(currentScreen).getRoot();
      if (currentform != null) {
        currentform.projectPropertyChanged();
      }
    }
  }

  @UiHandler("closeDialogBox")
  void handleClose(ClickEvent e) {
    projectProperties.hide();
    applyPropertyChanges();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     closeDialogBox.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
     categoryList.setFocus(true);
  }
}
