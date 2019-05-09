// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class TutorialPanel extends Frame {
  static {
    exportMethodsToJavascript();
  }

  /**
   * Creates video on page!
   */
  private static void createVideoDialog(String tutorialId) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(true, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText("Tutorial Video");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    VerticalPanel DialogBoxContents = new VerticalPanel();
    // Adds Youtube Video
    HTML message = new HTML("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/" + tutorialId + "?rel=0&autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>");
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button ok = new Button("Close");
    ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
        }
      });
    ok.setStyleName("DialogBox-button");
    holder.add(ok);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.center();
    dialogBox.show();
  }

  /**
   * Enlarges image on page
   */
  private static void createImageDialog(String img) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(true, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    VerticalPanel DialogBoxContents = new VerticalPanel();
    FlowPanel holder = new FlowPanel();
    Button ok = new Button("Close");
    ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
        }
      });
    ok.setStyleName("DialogBox-button");

    // Adds Image
    final Image image = new Image(img);
    image.addLoadHandler(new LoadHandler() {
        public void onLoad(LoadEvent evt) {
          final int imageWidth = image.getWidth();
          final int imageHeight = image.getHeight();
          final int windowWidth = (int) ((float) Window.getClientWidth() * 0.8);
          final int windowHeight = (int) ((float) Window.getClientHeight() * 0.9);
          int effectiveWidth = imageWidth;
          int effectiveHeight = imageHeight;

          if (imageWidth > windowWidth) {
            effectiveWidth = windowWidth;
            effectiveHeight = (int)(imageHeight * ((float)effectiveWidth / imageWidth));
          }

          if (effectiveHeight > windowHeight) {
            effectiveHeight = windowHeight;
            effectiveWidth = (int)(imageWidth * ((float)effectiveHeight / imageHeight));
          }

          image.setPixelSize(effectiveWidth, effectiveHeight);
          dialogBox.center();
        }
      });

    image.setStyleName("DialogBox-image");
    holder.add(ok);
    DialogBoxContents.add(image);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.center();
    dialogBox.show();
  }

  public static void getTutorialDialog(String tutorialId) {
    createVideoDialog(tutorialId);
  }

  public static void getImageDialog(String img) {
    createImageDialog(img);
  }

  private static native void exportMethodsToJavascript() /*-{
    $wnd.TutorialPanel_createTutorialDialog =
    $entry(@com.google.appinventor.client.editor.youngandroid.TutorialPanel::getTutorialDialog(Ljava/lang/String;));
    $wnd.TutorialPanel_createImageDialog =
    $entry(@com.google.appinventor.client.editor.youngandroid.TutorialPanel::getImageDialog(Ljava/lang/String;));
    $wnd.recieveMessage=function(event){
      if (event.data.type == "video") {
        $wnd.TutorialPanel_createTutorialDialog(event.data.youtubeId);
      }
    };
    $wnd.addEventListener("message", $wnd.recieveMessage, false);
    $wnd.recieveMessage=function(event){
      if (event.data.type == "img") {
        $wnd.TutorialPanel_createImageDialog(event.data.imageId);
      }
    };
    $wnd.addEventListener("message", $wnd.recieveMessage, false);
  }-*/;
}
