package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.appinventor.client.youngandroid.TextValidators;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Wizard to create new folders.
 */
public class NewFolderWizard extends Wizard {

  // UI element for project name
  private LabeledTextBox folderNameTextBox;

  /**
   * Creates a new folder wizard.
   */
  public NewFolderWizard() {
    super(MESSAGES.newFolderWizardCaption(), true, false);

    // Initialize the UI
    setStylePrimaryName("ode-DialogBox");

    folderNameTextBox = new LabeledTextBox(MESSAGES.folderNameLabel(), new Validator() {
      @Override
      public boolean validate(String value) {
        errorMessage = TextValidators.getFolderErrorMessage(value);
        if (errorMessage.length()>0){
          disableOkButton();
          return false;
        }
        enableOkButton();
        return true;
      }

      @Override
      public String getErrorMessage() {
        return errorMessage;
      }
    });

    folderNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          handleCancelClick();
        }
      }
    });

    folderNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) { //Validate the text each time a key is lifted
        folderNameTextBox.validate();
      }
    });

    VerticalPanel page = new VerticalPanel();

    page.add(folderNameTextBox);
    addPage(page);

    // Create finish command (create a new folder)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        Ode ode = Ode.getInstance();
        final String relativeName = folderNameTextBox.getText();
        final String currentFolder = ode.getProjectManager().getCurrentFolder();
        final String folderName = (currentFolder == null)
            ? relativeName
            : currentFolder + "/" + relativeName;

        if (TextValidators.checkNewFolderName(relativeName, folderName)) {
          ode.getProjectManager().addFolder(folderName);
        } else {
          show();
          center();
          return;
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 340;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(85);

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        folderNameTextBox.setFocus(true);
        folderNameTextBox.selectAll();
      }
    });
  }
}
