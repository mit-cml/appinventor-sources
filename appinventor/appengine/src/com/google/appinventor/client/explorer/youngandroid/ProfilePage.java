// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import java.util.logging.Level;
import java.util.logging.Logger;

//import com.google.appengine.api.memcache.*;
//import com.google.appengine.tools.cloudstorage.GcsFilename;
//import com.google.appengine.tools.cloudstorage.GcsService;
//import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
//import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
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

  public static final int PRIVATE = 0;
  public static final int PUBLIC = 1;

  String userId = "-1";  

  final FileUpload imageUpload = new FileUpload();
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
  Label userContentHeader = new Label();
  Label usernameLabel = new Label();
  Label userLinkLabel = new Label();
  final TextBox userNameBox = new TextBox();
  final TextBox userLinkBox = new TextBox();
  final Label userNameDisplay = new Label();
  Anchor userLinkDisplay = new Anchor();
  final Button profileSummit = new Button("Update Profile");


  private static final Logger LOG = Logger.getLogger(ProfilePage.class.getName());

  /**
   * Creates a new GalleryPage, must take in parameters
   *
   * @param user  the string ID of user that we are about to render
   * @param editStatus  the edit status (0 is private, 1 is public)
   *
   */
  public ProfilePage(String user, final int editStatus) {
    LOG.log(Level.WARNING, "#### userid of profile page " + user);
    LOG.log(Level.WARNING, "#### editstatus of profile page " + editStatus);

    // Replace the global variable
    userId = user;

    // setup panel
    panel.setWidth("100%");
    panel.addStyleName("ode-UserProfileWrapper");
    
    if (editStatus == PRIVATE) {
      // USER PROFILE IN PRIVATE (EDITABLE) STATE
      // setup upload stuff
      imageUploadPrompt.setText("Upload your profile image!");
      // Set the correct handler for servlet side capture
      imageUpload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
      imageUpload.addChangeHandler(new ChangeHandler (){
        public void onChange(ChangeEvent event) {
          uploadImage();
        }
      });
      appCardWrapper.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          // The correct way to trigger click event on FileUpload
          imageUpload.getElement().<InputElement>cast().click();
        }
      });
      imageUploadBoxInner.add(imageUploadPrompt);
      imageUploadBoxInner.add(imageUpload);
      imageUploadBoxInner.add(userAvatar);

      // set up the user info stuff
      userContentHeader.setText("Edit your profile");
      usernameLabel.setText("Your display name");
      userLinkLabel.setText("More info link");

      final Ode ode = Ode.getInstance();
      profileSummit.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

          // Store the name value of user, modify database
          final OdeAsyncCallback<Void> userNameUpdateCallback = new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryError()) {
                @Override
                public void onSuccess(Void arg0) {
                }
            };
           ode.getUserInfoService().storeUserName(userNameBox.getText(), userNameUpdateCallback);

          // Store the link value of user, modify database
          final OdeAsyncCallback<Void> userLinkUpdateCallback = new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryError()) {
                @Override
                public void onSuccess(Void arg0) {
                }
            };
          if (userLinkBox.getText().isEmpty()) {
            Ode.getInstance().getUserInfoService().storeUserLink(
                "", userLinkUpdateCallback);
          } else {
            Ode.getInstance().getUserInfoService().storeUserLink(
                userLinkBox.getText(), userLinkUpdateCallback);
          }

        }
      });

      majorContentCard.add(userContentHeader);
      majorContentCard.add(usernameLabel);
      majorContentCard.add(userNameBox);
      majorContentCard.add(userLinkLabel);
      majorContentCard.add(userLinkBox);
      majorContentCard.add(profileSummit);

    } else {
      panel.addStyleName("ode-Public");
      // USER PROFILE IN PUBLIC (NON-EDITABLE) STATE
      // set up the user info stuff
      imageUploadBoxInner.clear();

      // set up the user link
      userLinkLabel.setText("More info link:");

      majorContentCard.add(userContentHeader);
      majorContentCard.add(userLinkLabel);
      majorContentCard.add(userLinkDisplay);
    }

    // Add styling
    cardContainer.addStyleName("gallery-app-collection");
    imageUploadBox.addStyleName("gallery-card");

    userAvatar.addStyleName("gallery-card-cover");
    userAvatar.addStyleName("status-updating");
    imageUpload.addStyleName("app-image-upload");
    imageUploadPrompt.addStyleName("gallery-editprompt");

    // add styling for user info stuff
    majorContentCard.addStyleName("gallery-content-card");
    userContentHeader.addStyleName("app-title");
    usernameLabel.addStyleName("profile-textlabel");
    userNameBox.addStyleName("profile-textbox");
    userNameDisplay.addStyleName("profile-textdisplay");
    userLinkLabel.addStyleName("profile-textlabel");
    userLinkBox.addStyleName("profile-textbox");
    userLinkDisplay.addStyleName("profile-textdisplay");

    profileSummit.addStyleName("profile-submit");
    imageUpload.addStyleName("app-image-upload");


    // Add all the GUI layers up at the end
    panel.add(cardContainer);

    cardContainer.add(appCardWrapper);
    appCardWrapper.add(imageUploadBox);
    imageUploadBox.add(imageUploadBoxInner);
    cardContainer.add(majorContentCard);

    initWidget(panel);
    
    // Retrieve user info right after GUI is initialized
    final OdeAsyncCallback<User> userInformationCallback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(User user) {
            // Set associate GUI components
            if (editStatus == PRIVATE) {
              // In this case it'll return the current user
              userId = user.getUserId();
              userNameBox.setText(user.getUserName());
              userLinkBox.setText(user.getUserLink());
            } else {
              // In this case it'll return the user of [userId]
              userContentHeader.setText("Public Profile of " + user.getUserName());
              String link = user.getUserLink();
              if (link == null) {
                userLinkDisplay.setText("N/A");
              } else {
                if (link.isEmpty()) {
                  userLinkDisplay.setText("N/A");
                } else {
                  link = link.toLowerCase();
                  // Validate link format, fill in http part
                  if (!link.startsWith("http")) {
                    link = "http://" + link;
                  }
                  userLinkDisplay.setText(link);
                  userLinkDisplay.setHref(link);
                }
              }
            }
            // once we get the user info and id we can show the right image
            updateUserImage(GalleryApp.getUserImageUrl(userId), imageUploadBoxInner);

         }
    };
    if (editStatus == PRIVATE) {
      Ode.getInstance().getUserInfoService().getUserInformation(userInformationCallback);
    } else {
      Ode.getInstance().getUserInfoService().getUserInformation(userId, userInformationCallback);
    }

  } 

    
  private void uploadImage() {
        
    String uploadFilename = imageUpload.getFilename();
    if (!uploadFilename.isEmpty()) {
      String filename = makeValidFilename(uploadFilename);
      // Forge the request URL for gallery servlet
      String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.GALLERY_SERVLET + 
          "/user/" + userId + "/" + filename;
      Uploader.getInstance().upload(imageUpload, uploadUrl,
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
