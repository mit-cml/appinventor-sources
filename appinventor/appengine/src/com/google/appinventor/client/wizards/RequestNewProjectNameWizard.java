// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.appinventor.client.youngandroid.TextValidators;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.logging.Logger;

/**
 * Wizard for renaming projects.
 *
 */
public class RequestNewProjectNameWizard extends Wizard {

  private static final Logger LOG = Logger.getLogger(RequestNewProjectNameWizard.class.getName());

  private LabeledTextBox projectNameTextBox;
  private RequestProjectNewNameInterface newName;
  private String suggestedName = "";
  private String title;

  /**
   * Shows dialog box to enter a new project name.
   * @param newName entered new project name
   * @param filename original project name
   * @param doSuggestName whether to suggest name or not
   */
  public RequestNewProjectNameWizard(RequestProjectNewNameInterface newName,
      String filename, boolean doSuggestName) {
    super(MESSAGES.requestNewProjectNameLabel(), true, false); 
    this.newName = newName;
    
    title = MESSAGES.requestNewProjectNameLabel();
    if (doSuggestName) {
      suggestName(filename);
    }
    final DialogBox db = new DialogBox(false, true);              
    db.setText(title);                                                   // title of the dialog box
    db.setStyleName("ode-DialogBox");
    db.setHeight("40px");
    db.setWidth("360px");
    
    projectNameTextBox = 
        new LabeledTextBox(MESSAGES.requestNewProjectNameLabel(), new Validator() {
          @Override
          public boolean validate(String value) {
            errorMessage = TextValidators.getErrorMessage(value);
            if (errorMessage.length() > 0) {
              disableOkButton();
              return false;
            }
            errorMessage = TextValidators.getWarningMessages(value);
            enableOkButton();
            return true;
          }

          @Override
          public String getErrorMessage() {
            return errorMessage;
          }
        });
       
    projectNameTextBox.setText(suggestedName);
    
    projectNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick(db);
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          //handleCancelClick();
          db.hide();
        }
      }
    });

    projectNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) { //Validate the text each time a key is lifted
        projectNameTextBox.validate();
      }
    });

    Button okButton = new Button(MESSAGES.okButton());
    Button cancelButton = new Button(MESSAGES.cancelButton());
      
    cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          db.hide();
        }
      });
    
    okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick(db);
        }
      });
    VerticalPanel page = new VerticalPanel();
    page.add(projectNameTextBox);   
    HorizontalPanel hp = new HorizontalPanel();
               
    hp.add(cancelButton);
    hp.add(okButton);
    page.add(hp); 
            
    db.setWidget(page);
    db.center();
    db.show();
  }
  
  /**
   * Initialize dialog box title and suggest name.
   * 
   * @param filename existing name of the project
   */
  private void suggestName(String filename) {

    TextValidators.ProjectNameStatus status = TextValidators
            .checkNewProjectName(filename, true);
    title = getErrorMessage(status, filename);
    filename = filename.replace(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
    suggestedName = getSuggestedName(filename);    

    switch (status) {
      case RESERVED :
        suggestedName = "";
        break;
      
      case INVALIDFORMAT :
        if (TextValidators.checkNewProjectName(suggestedName, true) 
                != TextValidators.ProjectNameStatus.SUCCESS) {
          suggestedName = "";
        } else {
          title += MESSAGES.suggestNameTitleCaption(suggestedName);
        }
        break;
      
      case DUPLICATE :
        if (TextValidators.checkNewProjectName(suggestedName, true)
                  != TextValidators.ProjectNameStatus.SUCCESS) {
          suggestedName = "";
        } else {
          title += MESSAGES.suggestNameTitleCaption(suggestedName);
        }
        break;
      
      case DUPLICATEINTRASH :
        if (TextValidators.checkNewProjectName(suggestedName, true)
                != TextValidators.ProjectNameStatus.SUCCESS) {
          suggestedName = "";
        } else {
          title += MESSAGES.suggestNameTitleCaption(suggestedName);
        }
        break;
      
      default :
        break;
    }
  }
  
  private String getErrorMessage(TextValidators.ProjectNameStatus status, String filename) {
    switch (status) {
      case RESERVED :
        return MESSAGES.reservedTitleFormatError(filename);
      case INVALIDFORMAT :
        return MESSAGES.invalidTitleFormatError(filename);
      case DUPLICATE :
        return MESSAGES.duplicateTitleFormatError(filename);
      case DUPLICATEINTRASH :
        return MESSAGES.duplicateTitleInTrashFormatError(filename);
      default :
        return MESSAGES.successfulTitleFormat();
    }
  }

  /**
   * Suggests a project name based on existing project names
   * if hello,hello4 are in project list and user tries to upload
   * another project hello.aia then it suggests hello5 or if user uploads
   * hello4.aia then it suggests hello5.
   * 
   * @param filename initial filename
   * @return suggested name
   */
  private String getSuggestedName(String filename) {
    int max = 0;
    int len = filename.length();
    int lastInt = filename.charAt(len - 1);
    int splitPosition = len;
    if (lastInt <= 57 && lastInt >= 48) {
      splitPosition = len - 1;
      for (int i = len - 2; i >= 0; i--) {
        int cur = filename.charAt(i);
        if (cur <= 57 && cur >= 48) {
          splitPosition--; 
        } else {
          break;
        }
      }
  
      if (splitPosition == 0) {
        return filename;
      }
      filename = filename.substring(0, splitPosition);
    }
    for (Project proj : Ode.getInstance().getProjectManager().getProjects(filename)) {
      String sub = proj.getProjectName().substring(splitPosition);
      if (sub.length() > 0) { 
        try {
          lastInt = Integer.parseInt(sub);
        } catch (Exception e) {
          lastInt = -1;
        }
        if (lastInt > max) {
          max = lastInt;
        }
      } else {
        max++;
      }
    }
    max++;
    return filename + max;
  }
  
  /**
   * When user enters new project name it checks whether it is valid or not
   * and then Callbacks with new Project name when it is valid.
   * 
   * @param db Dialogbox shown to user
   */
  public void handleOkClick(DialogBox db) {
    String newEnteredName = projectNameTextBox.getText();
    newEnteredName = newEnteredName.replaceAll("( )+", " ").replace(" ", "_");
    TextValidators.ProjectNameStatus status 
        = TextValidators.checkNewProjectName(newEnteredName, true);
    if (status == TextValidators.ProjectNameStatus.SUCCESS) {
      db.hide();
      newName.getNewName(newEnteredName);
    } else {
      projectNameTextBox.setFocus(true);
      projectNameTextBox.selectAll();
      db.setText(getErrorMessage(status, newEnteredName));
    }
  }
}
