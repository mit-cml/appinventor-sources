// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import java.util.List;
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
import com.google.appinventor.shared.rpc.project.GalleryComment;
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
import com.google.gwt.user.client.Window;
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

/* profileGUI has:

  profileSingle
   mainContent -- like app Info
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

/**
 * The profile page shows a single user's profile information
 *
 * It has different modes for public viewing or when user is editing privately
 *
 * @author vincentaths@gmail.com (Vincent Zhang)
 */
public class ProfilePage extends Composite/* implements GalleryRequestListener*/ {

  public static final int PRIVATE = 0;
  public static final int PUBLIC = 1;

  String userId = "-1";  
  final int profileStatus;

  final FileUpload imageUpload = new FileUpload();
  // Create GUI wrappers and components

  // The abstract top-level GUI container
  VerticalPanel profileGUI = new VerticalPanel();
  // The actual container that components go in
  VerticalPanel profileSingle = new VerticalPanel();
  // The main profile container, same as appDetails in GalleryPage
  FlowPanel mainContent = new FlowPanel();
  // The sidebar showing a list of apps by this author, same as GalleryPage
  FlowPanel appsByAuthor = new FlowPanel();

  // Wrapper for primary profile content (image + userinfo)
  FlowPanel profilePrimaryWrapper = new FlowPanel();
  // Header in this case is basically image-related components
  FlowPanel profileHeader = new FlowPanel();
  FocusPanel profileHeaderWrapper = new FocusPanel();
  // Other basic user profile information
  FlowPanel profileInfo = new FlowPanel();

  FocusPanel appCardWrapper = new FocusPanel();
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
  private static final Ode ode = Ode.getInstance();

  GalleryClient gallery = null;
  GalleryGuiFactory galleryGF = new GalleryGuiFactory();

  /**
   * Creates a new ProfilePage, must take in parameters
   *
   * @param incomingUserId  the string ID of user that we are about to render
   * @param editStatus  the edit status (0 is private, 1 is public)
   *
   */
  public ProfilePage(String incomingUserId, final int editStatus) {
    LOG.log(Level.WARNING, "#### userid of profile page " + incomingUserId + "#####" + userId);
    LOG.log(Level.WARNING, "#### editstatus of profile page " + editStatus);

    // Replace the global variable
    userId = incomingUserId;
    profileStatus = editStatus;

    // If we're editing or updating, add input form for image
    if (editStatus == PRIVATE) {
      initImageComponents();
    } else  { // we are just viewing this page so setup the image
      initReadOnlyImage();
    }

//      // setup upload stuff
//      imageUploadPrompt.setText("Upload your profile image!");
//      // Set the correct handler for servlet side capture
//      imageUpload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
//      imageUpload.addChangeHandler(new ChangeHandler (){
//        public void onChange(ChangeEvent event) {
//          uploadImage();
//        }
//      });
//      appCardWrapper.addClickHandler(new ClickHandler() {
//        @Override
//        public void onClick(ClickEvent event) {
//          // The correct way to trigger click event on FileUpload
//          imageUpload.getElement().<InputElement>cast().click();
//        }
//      });
//      imageUploadBoxInner.add(imageUploadPrompt);
//      imageUploadBoxInner.add(imageUpload);
//      imageUploadBoxInner.add(userAvatar);

    if (editStatus == PRIVATE) {
      userContentHeader.setText("Edit your profile");
      usernameLabel.setText("Your display name");
      userLinkLabel.setText("More info link");

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

      profileInfo.add(userContentHeader);
      profileInfo.add(usernameLabel);
      profileInfo.add(userNameBox);
      profileInfo.add(userLinkLabel);
      profileInfo.add(userLinkBox);
      profileInfo.add(profileSummit);

    } else {
      profileSingle.addStyleName("ode-Public");
      // USER PROFILE IN PUBLIC (NON-EDITABLE) STATE
      imageUploadBoxInner.clear();
      // Set up the user info stuff
      userLinkLabel.setText("More info link:");
      profileInfo.add(userContentHeader);
      profileInfo.add(userLinkLabel);
      profileInfo.add(userLinkDisplay);
    }

    // Add GUI layers in the "main content" container
    profileHeader.addStyleName("app-header"); //TODO: change a more contextual style name
    profilePrimaryWrapper.add(profileHeader); // profileImage
    profileInfo.addStyleName("app-info-container");
    profilePrimaryWrapper.add(profileInfo);
    profilePrimaryWrapper.addStyleName("clearfix");
    mainContent.add(profilePrimaryWrapper);

    // Add styling for user info detail components
    mainContent.addStyleName("gallery-container");
    mainContent.addStyleName("gallery-content-details");
    userContentHeader.addStyleName("app-title");
    usernameLabel.addStyleName("profile-textlabel");
    userNameBox.addStyleName("profile-textbox");
    userNameDisplay.addStyleName("profile-textdisplay");
    userLinkLabel.addStyleName("profile-textlabel");
    userLinkBox.addStyleName("profile-textbox");
    userLinkDisplay.addStyleName("profile-textdisplay");

    profileSummit.addStyleName("profile-submit");
    imageUpload.addStyleName("app-image-upload");


    // Setup top level containers
    // profileGUI is just the abstract top-level GUI container
    profileGUI.addStyleName("ode-UserProfileWrapper");
    // profileSingle is the actual container that components go in
    profileSingle.addStyleName("gallery-page-single");


    // Add containers to the top-tier GUI, initialize
    profileSingle.add(mainContent);
    profileGUI.add(profileSingle);
    profileSingle.add(appsByAuthor);
    initWidget(profileGUI);


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
            } else if (editStatus == PUBLIC) {
              // In this case it'll return the user of [userId]
              userContentHeader.setText(user.getUserName());
              makeValidLink(userLinkDisplay, user.getUserLink());


            }
            // once we get the user info and id we can show the right image
            updateUserImage(GalleryApp.getUserImageUrl(userId), imageUploadBoxInner);

         }
    };
    if (editStatus == PRIVATE) {
      Ode.getInstance().getUserInfoService().getUserInformation(userInformationCallback);
    } else {
      Ode.getInstance().getUserInfoService().getUserInformation(userId, userInformationCallback);
      LOG.warning("###### PROFILEPAGE GOT IN return success, ready to grab appsByDev");
      // Retrieve apps by this author for sidebar
      gallery.GetAppsByDeveloper(0, 5, userId);
    }
  }


  /**
   * Helper method to validify a hyperlink
   * @param link    the GWT anchor object to validify
   * @param linktext    the actual http link that the anchor should point to
   */
  private void makeValidLink(Anchor link, String linktext) {
    if (linktext == null) {
      link.setText("N/A");
    } else {
      if (linktext.isEmpty()) {
        link.setText("N/A");
      } else {
        linktext = linktext.toLowerCase();
        // Validate link format, fill in http part
        if (!linktext.startsWith("http")) {
          linktext = "http://" + linktext;
        }
        link.setText(linktext);
        link.setHref(linktext);
      }
    }
  }


  /**
   * Helper method called by constructor to initialize image upload components
   */
  private void initImageComponents() {
    imageUploadBox = new FlowPanel();
    imageUploadBox.addStyleName("app-image-uploadbox");
    imageUploadBox.addStyleName("gallery-editbox");
    imageUploadBoxInner = new FlowPanel();
    imageUploadPrompt = new Label("Upload your profile image!");
    imageUploadPrompt.addStyleName("gallery-editprompt");

    updateUserImage(GalleryApp.getUserImageUrl(userId), imageUploadBoxInner);
    imageUploadPrompt.addStyleName("app-image-uploadprompt");
    imageUploadBoxInner.add(imageUploadPrompt);

    final FileUpload upload = new FileUpload();
    upload.addStyleName("app-image-upload");
    // Set the correct handler for servlet side capture
    upload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
    upload.addChangeHandler(new ChangeHandler (){
      public void onChange(ChangeEvent event) {
        uploadImage();
      }
    });
    imageUploadBoxInner.add(upload);
    imageUploadBox.add(imageUploadBoxInner);
    profileHeaderWrapper.add(imageUploadBox);
    profileHeaderWrapper.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        upload.getElement().<InputElement>cast().click();
      }
    });
    profileHeader.add(profileHeaderWrapper);
  }


  /**
   * Helper method called by constructor to create the app image for display
   */
  private void initReadOnlyImage() {
    updateUserImage(GalleryApp.getUserImageUrl(userId), profileHeader);
  }


  /**
   * Main method to validify and upload the app image
   */
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
              // The user can resolve the problem by uploading a smaller file.
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


  /**
   * Helper method to validify file name, used in uploadImage()
   * @param uploadFilename  The full filename of the file
   */
  private String makeValidFilename(String uploadFilename) {
    // Strip leading path off filename.
    // We need to support both Unix ('/') and Windows ('\\') separators.
    String filename = uploadFilename.substring(
        Math.max(uploadFilename.lastIndexOf('/'), uploadFilename.lastIndexOf('\\')) + 1);
    // We need to strip out whitespace from the filename.
    filename = filename.replaceAll("\\s", "");
    return filename;
  }


  /**
   * Helper method to update the user's image
   * @param url  The URL of the image to show
   * @param container  The container that image widget resides
   */
  private void updateUserImage(String url, Panel container) {
    userAvatar = new Image();
    userAvatar.setUrl(url);
    userAvatar.addStyleName("app-image");
    if (profileStatus == PRIVATE) {
      userAvatar.addStyleName("status-updating");
    }
    // if the user has provided a gallery app image, we'll load it. But if not
    // the error will occur and we'll load default image
    userAvatar.addErrorHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        userAvatar.setUrl(GalleryApp.DEFAULTUSERIMAGE);
      }
    });
    container.add(userAvatar);   
  }


  /**
   * Loads the proper tab GUI with gallery's app data.
   * @param apps: list of returned gallery apps from callback.
   * @param requestId: determines the specific type of app data.
   */
  /*  private void refreshApps(List<GalleryApp> apps, int requestId) {
    switch (requestId) {
      case GalleryClient.REQUEST_BYDEVELOPER:
        LOG.warning("###### PROFILEPAGE GOT IN refreshapps");
//        galleryGF.generateSidebar(apps, appsByAuthor, MESSAGES.galleryAppsByAuthorSidebar(), false);
        break;
    }
  }


  @Override
  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestId) {
    if (apps != null) {
      LOG.warning("###### PROFILEPAGE GOT IN onAppListRequestCompleted");
      refreshApps(apps, requestId);
    } else {
      Window.alert("app list returned null");
    }
  }


  @Override
  public void onCommentsRequestCompleted(List<GalleryComment> comments) {
    // TODO Auto-generated method stub
  }


  @Override
  public void onSourceLoadCompleted(UserProject projectInfo) {
    // TODO Auto-generated method stub
  }
*/


}
