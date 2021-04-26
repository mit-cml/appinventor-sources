// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

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

/**
 * Wizard for renaming projects.
 *
 */
public class RequestNewProjectNameWizard extends Wizard {

  private LabeledTextBox projectNameTextBox;
  private RequestProjectNewNameInterface newName;

  /**
   * Shows dialog box to enter a new project name. 
   * @param newName entered new project name
   * @param defaultText text shown in textbox by default
   * @param title title of the dialog box
   */
  public RequestNewProjectNameWizard(RequestProjectNewNameInterface newName,
      String defaultText, String title) {
    super(MESSAGES.requestNewProjectNameCaption(), true, false); 
    this.newName = newName;
    
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
       
    projectNameTextBox.setText(defaultText);
    
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
   * When user enters new project name it checks whether it is valid or not
   * and then Callbacks with new Project name when it is valid.
   * @param db Dialogbox shown to user
   */
  public void handleOkClick(DialogBox db) {
    String newEnteredName = projectNameTextBox.getText();
    newEnteredName = newEnteredName.replaceAll("( )+", " ").replace(" ","_");
    if (TextValidators.checkNewProjectName(newEnteredName, true)) {
      db.hide();
      newName.getNewName(newEnteredName);
    } else {
      projectNameTextBox.setFocus(true);
      projectNameTextBox.selectAll();
      db.setText(TextValidators.getProjectNameStatus() + " : " + newEnteredName);
    }
  }
}
