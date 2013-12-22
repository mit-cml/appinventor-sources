// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import java.util.List;

import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProfilePage extends Composite implements GalleryRequestListener {

  public ProfilePage() {
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    
    // Create necessary GUI wrappers and components
    FlowPanel cardContainer = new FlowPanel();
    FlowPanel appCard = new FlowPanel();
    FlowPanel majorContentCard = new FlowPanel();
    
    Image userAvatar = new Image();
    userAvatar.setUrl("http://storage.googleapis.com/galleryai2/5201690726760448/image");
    Label imageUploadPrompt = new Label();
    imageUploadPrompt.setText("Upload your profile image!");
    final FileUpload upload = new FileUpload();
    upload.addStyleName("app-image-upload");
    
    FocusPanel appCardWrapper = new FocusPanel();
    appCardWrapper.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        upload.getElement().<InputElement>cast().click(); 
      }
    });
    
    Label userContentTitle = new Label();
    userContentTitle.setText("Edit your profile");
    Label usernameLabel = new Label();
    usernameLabel.setText("Your display name");
    TextBox usernameBox = new TextBox();

    Label userLocationLabel = new Label();
    userLocationLabel.setText("Your another datafield");
    TextBox userLocationBox = new TextBox();

    
    Button userProfileEditSubmit = new Button();
    userProfileEditSubmit.setText("Update");
    
    // Add associated styling
    panel.addStyleName("ode-UserProfileWrapper");
    cardContainer.addStyleName("gallery-app-collection");
    
    appCard.addStyleName("gallery-card");
    userAvatar.addStyleName("gallery-card-cover");
    userAvatar.addStyleName("status-updating");
    imageUploadPrompt.addStyleName("gallery-editprompt");
    
    majorContentCard.addStyleName("gallery-content-card");
    userContentTitle.addStyleName("app-title");
    usernameLabel.addStyleName("profile-textlabel");
    usernameBox.addStyleName("profile-textbox");
    userLocationLabel.addStyleName("profile-textlabel");
    userLocationBox.addStyleName("profile-textbox");
    userProfileEditSubmit.addStyleName("profile-submit");
    
    
    // Add all the GUI layers up at the end
    appCard.add(userAvatar);
    appCard.add(imageUploadPrompt);
    appCard.add(upload);
    appCardWrapper.add(appCard);
    cardContainer.add(appCardWrapper);
    
    majorContentCard.add(userContentTitle);
    majorContentCard.add(usernameLabel);
    majorContentCard.add(usernameBox);
    majorContentCard.add(userLocationLabel);
    majorContentCard.add(userLocationBox);
    majorContentCard.add(userProfileEditSubmit);
    cardContainer.add(majorContentCard);
    
    panel.add(cardContainer);
    initWidget(panel);
  }
  
  
  @Override
  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestID) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onCommentsRequestCompleted(List<GalleryComment> comments) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onSourceLoadCompleted(UserProject projectInfo) {
    // TODO Auto-generated method stub
    
  }

}
