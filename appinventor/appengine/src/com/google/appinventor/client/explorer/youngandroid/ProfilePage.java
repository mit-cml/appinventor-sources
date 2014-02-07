// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import java.io.IOException;
import java.util.List;

//import com.google.appengine.api.memcache.*;
//import com.google.appengine.tools.cloudstorage.GcsFilename;
//import com.google.appengine.tools.cloudstorage.GcsService;
//import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
//import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

/* panel has:
   
  cardContainer  -- like App Header, make it like appClear
   majorContentCard -- like app Info
     userContentTitle
     userNameLabel
     userNameBox
     userLinkLabel
     userLinkBox
     
   appCardWrapper
     imageUploadBox
       imageUploadBoxInner
         userAvatar
         imageUploadPrompt
         upload


*/
public class ProfilePage extends Composite {

  String userId = "-1";  
  final FileUpload upload= new FileUpload();
  // Create GUI wrappers and components
  // the main panel and its container
  VerticalPanel panel = new VerticalPanel();
  FlowPanel cardContainer = new FlowPanel();
  // cardContainer contains a card for picture (appCardWrapper) and info (majorContentCard)
  FocusPanel appCardWrapper = new FocusPanel();

  FlowPanel majorContentCard = new FlowPanel();
  FlowPanel imageUploadBox = new FlowPanel();
  FlowPanel imageUploadBoxInner = new FlowPanel();  
  Image userAvatar = new Image(); 
  Label imageUploadPrompt = new Label();
  
  

  
  // the majorContentCard has a label and namebox
  Label usernameLabel = new Label();
  Label userLinkLabel = new Label();
  //Label httpLinkLabel = new Label();
  Label userContentTitle = new Label();
  final TextBox userNameBox = new TextBox();
  final Button updateButton = new Button("Update Profile");
  final TextBox userLinkBox = new TextBox();

  
  public ProfilePage() {
    // setup panel
    panel.setWidth("100%");
    panel.addStyleName("ode-UserProfileWrapper");
    

    // setup upload stuff    
    imageUploadPrompt.setText("Upload your profile image!");
    upload.addStyleName("app-image-upload");
    // Set the correct handler for servlet side capture
    upload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
    upload.addChangeHandler(new ChangeHandler (){
      public void onChange(ChangeEvent event) {
        uploadImage();
      }
    });
    appCardWrapper.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        upload.getElement().<InputElement>cast().click(); 
      }
    });
    // set up the user info stuff
    userContentTitle.setText("Edit your profile");
    usernameLabel.setText("Display name");
    // set up the code to modify database when user changes his display name 
    final Ode ode = Ode.getInstance();
    userNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          final OdeAsyncCallback<Void> userUpdateCallback = new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Void arg0) {
              }
          };
         ode.getUserInfoService().storeUserName(userNameBox.getText(), userUpdateCallback);
        }

      });
    
    // set up the user link
    userLinkLabel.setText("More info link");
    //httpLinkLabel.setText("http://");
    // set up the code to modify database when user changes his introduction link
    userLinkBox.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          final OdeAsyncCallback<Void> userUpdateCallback = new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Void arg0) {
              }
          };
         ode.getUserInfoService().storeUserLink(userLinkBox.getText(), userUpdateCallback);
        }

      });

    // Add styling
    cardContainer.addStyleName("gallery-app-collection");
    imageUploadBox.addStyleName("gallery-card");

    userAvatar.addStyleName("gallery-card-cover");
    userAvatar.addStyleName("status-updating");
    imageUploadPrompt.addStyleName("gallery-editprompt");

    // add styling for user info stuff
    majorContentCard.addStyleName("gallery-content-card");
    userContentTitle.addStyleName("app-title");
    usernameLabel.addStyleName("profile-textlabel");
    userNameBox.addStyleName("profile-textbox");
    userLinkLabel.addStyleName("profile-textlabel");
    userLinkBox.addStyleName("profile-textbox");

    upload.addStyleName("app-image-upload");
 
    
    // Add all the GUI layers up at the end
    panel.add(cardContainer);
    
    cardContainer.add(appCardWrapper);
    appCardWrapper.add(imageUploadBox);
    imageUploadBox.add(imageUploadBoxInner);

    imageUploadBoxInner.add(imageUploadPrompt);
    imageUploadBoxInner.add(upload);
    imageUploadBoxInner.add(userAvatar);
    
    cardContainer.add(majorContentCard);
    majorContentCard.add(userContentTitle);
    majorContentCard.add(usernameLabel);
    majorContentCard.add(userNameBox);
    majorContentCard.add(userLinkLabel);
    //majorContentCard.add(httpLinkLabel);
    majorContentCard.add(userLinkBox);
    majorContentCard.add(updateButton);
    initWidget(panel);
    
    // Retrieve user info right after GUI is initialized
    final OdeAsyncCallback<User> userInformationCallback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(User user) {
            // Set associate GUI components
            userNameBox.setText(user.getUserName());
            userLinkBox.setText(user.getUserLink());
            userId = user.getUserId();
            // once we get the user info and id we can show the right image
            updateUserImage(GalleryApp.getUserImageUrl(userId),imageUploadBoxInner);
         }
    };
    ode.getUserInfoService().getUserInformation(userInformationCallback);
      
  } 

    
  private void uploadImage() {
        
    String uploadFilename = upload.getFilename();
    if (!uploadFilename.isEmpty()) {
      String filename = makeValidFilename(uploadFilename);
      // Forge the request URL for gallery servlet
      String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.GALLERY_SERVLET + 
          "/user/" + userId + "/" + filename;
      Uploader.getInstance().upload(upload, uploadUrl,
          new OdeAsyncCallback<UploadResponse>(MESSAGES.fileUploadError()) {
        @Override
        public void onSuccess(UploadResponse uploadResponse) {
          switch (uploadResponse.getStatus()) {
            case SUCCESS:
              ErrorReporter.hide();
              imageUploadBoxInner.clear();
              updateUserImage(GalleryApp.getUserImageUrl(userId), imageUploadBoxInner);
              break;
            case FILE_TOO_LARGE:
              // The user can resolve the problem by
              // uploading a smaller file.
              ErrorReporter.reportInfo(MESSAGES.fileTooLargeError());
              break;
            default:
              ErrorReporter.reportError(MESSAGES.fileUploadError());
              break;
          }
        }
      });
          
    }
        
  }
  
  private String makeValidFilename(String uploadFilename) {
    // Strip leading path off filename.
    // We need to support both Unix ('/') and Windows ('\\') separators.
    String filename = uploadFilename.substring(
        Math.max(uploadFilename.lastIndexOf('/'), uploadFilename.lastIndexOf('\\')) + 1);
    // We need to strip out whitespace from the filename.
    filename = filename.replaceAll("\\s", "");
    return filename;
  }

  private void updateUserImage(String url, Panel container) {
    userAvatar = new Image();
    userAvatar.setUrl(url);
    userAvatar.addStyleName("app-image");
    // if the user has provided a gallery app image, we'll load it. But if not
    // the error will occur and we'll load default image
    userAvatar.addErrorHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        userAvatar.setUrl(GalleryApp.DEFAULTUSERIMAGE);
      }
    });
    container.add(userAvatar);   
  }
  
  
  
}
