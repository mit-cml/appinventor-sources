package com.google.appinventor.client.explorer.youngandroid;

/*import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
*/

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.ArrayList;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;


public class NewUserGetStarted {
public static void displayDialog(){
    String content="Welcome to App Inventor!";
    int browserHeight = Window.getClientHeight();
    int browserWidth = Window.getClientWidth();
    int left=browserWidth/2;
    int top=browserHeight/2;
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(content);
    dialogBox.setStylePrimaryName("ode-DialogBox-Tutorial");
    dialogBox.getElement().setId("tutorialDialog");

    //setting position of dialog box

    dialogBox.setPopupPosition(left, top);

    dialogBox.setAnimationEnabled(true);

    //close button
    Button closeButton = new Button("close");
    VerticalPanel dialogVPanel = new VerticalPanel();
    dialogVPanel.setWidth("300px");
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    dialogVPanel.add(closeButton);
    closeButton.getElement().setId("closeButton");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    //next button
    Button nextButton = new Button("next");
    nextButton.getElement().setId("nextButton");
    dialogVPanel.add(nextButton);
    nextButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        //TODO: how to switch to designer screen
        OdeLog.log("Before call");
        BlocklyPanel.callNextStep();
        OdeLog.log("After call");
      }
    });

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);
    dialogBox.setAutoHideEnabled(false);
    //dialogBox.setWidget(holder);
    // Set the contents of the Widget
    dialogBox.setWidget(dialogVPanel);
    dialogBox.show();
  }
}