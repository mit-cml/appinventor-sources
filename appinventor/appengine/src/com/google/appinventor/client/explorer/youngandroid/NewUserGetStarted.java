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
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

    //elements
    Button closeButton = new Button("close");
    closeButton.getElement().setId("closeButton");
    Frame frame = new Frame("");
    frame.getElement().setId("Tutorial_frame");
    Button nextButton = new Button("next");
    nextButton.getElement().setId("nextButton");
    Button backButton = new Button("back");
    backButton.getElement().setId("backButton");
    HTML nextStepErrorMsg = new HTML("It looks like you haven't finished this step yet. Try reading through all the instructions again to make sure you've finished the whole step.");
    nextStepErrorMsg.getElement().setId("nextStepErrorMsg");


    VerticalPanel dialogVPanel = new VerticalPanel();
    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    // dialogVPanel.add(frame);
    dialogVPanel.add(nextStepErrorMsg);
    dialogVPanel.add(hPanel);

    dialogVPanel.setWidth("300px");
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    //next button

    nextButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callNextStep();
      }
    });

    backButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callBackStep();
      }
    });

    hPanel.add(closeButton);
    hPanel.add(backButton);
    hPanel.add(nextButton);

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);
    dialogBox.setAutoHideEnabled(false);
    //dialogBox.setWidget(holder);
    // Set the contents of the Widget
    dialogBox.setWidget(dialogVPanel);
    dialogBox.show();
  }
}