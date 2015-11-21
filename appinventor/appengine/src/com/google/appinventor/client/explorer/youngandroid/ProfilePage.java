// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.PrivateUserProfileTabPanel;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/* profileGUI has:

  profileSingle
   mainContent -- like app Info
     userContentTitle
     userNameLabel
     userNameBox
     userLinkLabel
     userLinkBox
     userEmailFrequencyBox
     userEmailFrequencyPrefixLabel
     userEmailFrequencySuffixLabel

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
 * @author mrburaimo@gmail.com (Daniel Buraimo)
 */
public class ProfilePage extends Composite/* implements GalleryRequestListener*/ {
  private  List<GalleryApp> apps;
  private final List<GalleryApp> selectedApps;
  private static final int ZERO = 0;
  public static final int PRIVATE = 0;
  public static final int PUBLIC = 1;
  public static final int REQUEST_BYDEVELOPER = 7;
  private int appCatalogCounter = 0;
  private boolean appCatalogExhausted = false;
  public static final int NUMAPPSTOSHOW = 10;

  String userId = "-1";
  final int profileStatus;

  final FileUpload imageUpload = new FileUpload();
  // Create GUI wrappers and components

  private GalleryAppTab appCatalogTab;
  private final TabPanel appTabs;
  private final FlowPanel appCatalog;
  private final FlowPanel appCatalogContent;

  // The abstract top-level GUI container
  VerticalPanel profileGUI = new VerticalPanel();
  // The actual container that components go in
  VerticalPanel profileSingle = new VerticalPanel();
  // The main profile container, same as appDetails in GalleryPage
  FlowPanel mainContent = new FlowPanel();
  // The sidebar showing a list of apps by this author, same as GalleryPage
  private TabPanel sidebarTabs = new TabPanel();

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
  Label userEmailDescriptionLabel = new Label();
  Label userEmailFrequencyPrefixLabel = new Label();
  Label userEmailFrequencySuffixLabel = new Label();
  Button editProfile = new Button(MESSAGES.buttonEditProfile());
  final TextBox userNameBox = new TextBox();
  final TextBox userLinkBox = new TextBox();
  final TextBox userEmailFrequencyBox = new TextBox();
  final Label userNameDisplay = new Label();
  Anchor userLinkDisplay = new Anchor();
  final Button profileSubmit = new Button(MESSAGES.buttonUpdateProfile());

  private static final Logger LOG = Logger.getLogger(ProfilePage.class.getName());
  private static final Ode ode = Ode.getInstance();

  GalleryClient gallery = GalleryClient.getInstance();
  GalleryGuiFactory galleryGF = new GalleryGuiFactory();

  /**
   * Creates a new ProfilePage, must take in parameters
   *
   * @param incomingUserId  the string ID of user that we are about to render
   * @param editStatus  the edit status (0 is private, 1 is public)
   *
   */
  public ProfilePage(final String incomingUserId, final int editStatus) {

    appCatalog = new FlowPanel();
    appCatalogContent = new FlowPanel();
    selectedApps = new ArrayList<GalleryApp>();
    appTabs = new TabPanel();

    // Replace the global variable
    if (incomingUserId.equalsIgnoreCase("-1")) {
      // this is user checking out own profile, thus we grab current user info
      // Get current user id
      final User currentUser = Ode.getInstance().getUser();
      userId = currentUser.getUserId();
    } else {
      // this is checking out an already existing user's profile...
      userId = incomingUserId;
    }
    profileStatus = editStatus;

    // If we're editing or updating, add input form for image
    if (editStatus == PRIVATE) {
    // This should only set up image after userId is returned above
    } else  { // we are just viewing this page so setup the image
      initReadOnlyImage();
    }

    if (editStatus == PRIVATE) {
      userContentHeader.setText(MESSAGES.labelEditYourProfile());
      usernameLabel.setText(MESSAGES.labelYourDisplayName());
      userLinkLabel.setText(MESSAGES.labelMoreInfoLink());
      userEmailDescriptionLabel.setText(MESSAGES.labelEmailDescription());
      userEmailFrequencyPrefixLabel.setText(MESSAGES.labelEmailFrequencyPrefix());
      userEmailFrequencySuffixLabel.setText(MESSAGES.labelEmailFrequencySuffix());
      editProfile.setVisible(false);

      profileSubmit.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          profileSubmit.setEnabled(false);
          if(!validEmailFrequency(userEmailFrequencyBox.getText())){
            Window.alert(MESSAGES.errorEmailFrequency());
            profileSubmit.setEnabled(true);
            return;
          }

          // Store the name value of user, modify database
          final OdeAsyncCallback<Void> userNameUpdateCallback = new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryError()) {
                @Override
                public void onSuccess(Void arg0) {
                  profileSubmit.setEnabled(true);
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

          // Store the email notification frequency value of user, modofy database
          final OdeAsyncCallback<Void> userEmailFrequencyUpdateCallback = new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryError()) {
                @Override
                public void onSuccess(Void arg0) {
                }
            };
            Ode.getInstance().getUserInfoService().storeUserEmailFrequency(
                Integer.valueOf(userEmailFrequencyBox.getText()), userEmailFrequencyUpdateCallback);

        }
      });

      profileInfo.add(userContentHeader);
      profileInfo.add(usernameLabel);
      profileInfo.add(userNameBox);
      profileInfo.add(userLinkLabel);
      profileInfo.add(userLinkBox);
      profileInfo.add(userEmailDescriptionLabel);
      profileInfo.add(userEmailFrequencyPrefixLabel);
      profileInfo.add(userEmailFrequencyBox);
      profileInfo.add(userEmailFrequencySuffixLabel);
      profileInfo.add(profileSubmit);

    } else {
      profileSingle.addStyleName("ode-Public");
      // USER PROFILE IN PUBLIC (NON-EDITABLE) STATE
      // Set up the user info stuff
      userLinkLabel.setText("More info:");
      profileInfo.add(userContentHeader);
      profileInfo.add(userLinkLabel);
      profileInfo.add(userLinkDisplay);
      profileInfo.add(editProfile);
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
    userEmailDescriptionLabel.addStyleName("profile-textlabel-emaildescription");
    userEmailFrequencyPrefixLabel.addStyleName("profile-textlabel");
    userEmailFrequencySuffixLabel.addStyleName("profile-textlabel");
    userEmailFrequencyBox.addStyleName("profile-textbox-small");
    editProfile.addStyleName("profile-submit");

    profileSubmit.addStyleName("profile-submit");
    imageUpload.addStyleName("app-image-upload");

    // Add sidebar
    if (editStatus == PUBLIC) {
      sidebarTabs.addStyleName("gallery-container");
      sidebarTabs.addStyleName("gallery-app-showcase");
    }
    // Setup top level containers
    // profileSingle is the actual container that components go in
    profileSingle.addStyleName("gallery-page-single");


    // Add containers to the top-tier GUI, initialize
    profileSingle.add(mainContent);
    if (editStatus == PUBLIC) {
      profileSingle.add(appTabs);
      profileSingle.add(sidebarTabs);
    }

    // profileGUI is just the abstract top-level GUI container
    profileGUI.add(profileSingle);
    profileGUI.addStyleName("ode-UserProfileWrapper");
    profileGUI.addStyleName("gallery");
    initWidget(profileGUI);

    // Retrieve other user info right after GUI is initialized
    final OdeAsyncCallback<User> userInformationCallback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(User user) {
            // Set associate GUI components of public states
            // In this case it'll return the user of [userId]
            userContentHeader.setText(user.getUserName());
            makeValidLink(userLinkDisplay, user.getUserLink());
            userEmailFrequencyBox.setText(String.valueOf(user.getUserEmailFrequency()));
         }
    };
    if (editStatus == PRIVATE) {
      User currentUser = Ode.getInstance().getUser();
      // In this case it'll return the current user
      userId = currentUser.getUserId();
      userNameBox.setText(currentUser.getUserName());
      userLinkBox.setText(currentUser.getUserLink());
      userEmailFrequencyBox.setText(String.valueOf(currentUser.getUserEmailFrequency()));
    } else {
      // Public state
      Ode.getInstance().getUserInfoService().getUserInformationByUserId(userId, userInformationCallback);
      sidebarTabs.setVisible(false);
      appCatalogTab = new GalleryAppTab(appCatalog, appCatalogContent,userId);
      appTabs.add(appCatalog,"My Catalog");
      appTabs.selectTab(0);
      appTabs.addStyleName("gallery-app-tabs");
    }

    //TODO this callback should combine with previous ones. Leave it out for now
    final User user = Ode.getInstance().getUser();
    if(incomingUserId.equals(user.getUserId())){
      editProfile.setVisible(true);
      editProfile.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          ode.switchToPrivateUserProfileView();
          PrivateUserProfileTabPanel.getPrivateUserProfileTabPanel().selectTab(0);
        }
      });
    }else{
      editProfile.setVisible(false);
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
        link.setTarget("_blank");
      }
    }
  }


  /**
   * Helper method called by constructor to initialize image upload components
   */
  private void initImageComponents(String userId) {
    imageUploadBox.addStyleName("app-image-uploadbox");
    imageUploadBox.addStyleName("gallery-editbox");
    imageUploadPrompt = new Label("Upload your profile image!");
    imageUploadPrompt.addStyleName("gallery-editprompt");

    if(gallery.getGallerySettings() != null){
      updateUserImage(gallery.getUserImageURL(userId), imageUploadBoxInner);
    }
    imageUploadPrompt.addStyleName("app-image-uploadprompt");
    //imageUploadBoxInner.add(imageUploadPrompt);

    // Set the correct handler for servlet side capture
    imageUpload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
    imageUpload.addChangeHandler(new ChangeHandler (){
      public void onChange(ChangeEvent event) {
        uploadImage();
      }
    });
    imageUploadBoxInner.add(imageUpload);
    imageUploadBox.add(imageUploadBoxInner);
    profileHeaderWrapper.add(imageUploadBox);
    profileHeaderWrapper.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        //imageUpload.getElement().<InputElement>cast().click();
      }
    });
    profileHeader.add(profileHeaderWrapper);
    
    Label uploadPrompt = new Label("Upload your profile image");
    uploadPrompt.addStyleName("primary-link-small");
    uploadPrompt.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        imageUpload.getElement().<InputElement>cast().click();
      }
    });
    profileHeader.add(uploadPrompt);
  }


  /**
   * Helper method called by constructor to create the app image for display
   */
  private void initReadOnlyImage() {
    updateUserImage(gallery.getUserImageURL(userId), profileHeader);
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
              updateUserImage(gallery.getUserImageURL(userId), imageUploadBoxInner);
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
  private void updateUserImage(final String url, Panel container) {
    userAvatar = new Image();
    //setUrl if the new URL is the same one as it was before; an easy workaround is
    //to make the URL unique so it forces the browser to reload
    userAvatar.setUrl(url + "?" + System.currentTimeMillis());
    userAvatar.addStyleName("app-image");
    if (profileStatus == PRIVATE) {
      //userAvatar.addStyleName("status-updating");
    }
    // if the user has provided a gallery app image, we'll load it. But if not
    // the error will occur and we'll load default image
    userAvatar.addErrorHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        userAvatar.setUrl(GalleryApp.DEFAULTUSERIMAGE);
      }
    });
    container.add(userAvatar);

    if(gallery.getSystemEnvironment() != null &&
        gallery.getSystemEnvironment().toString().equals("Development")){
      final OdeAsyncCallback<String> callback = new OdeAsyncCallback<String>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(String newUrl) {
            userAvatar.setUrl(newUrl + "?" + System.currentTimeMillis());
          }
        };
      Ode.getInstance().getGalleryService().getBlobServingUrl(url, callback);
    }
  }

  public void loadImage(){
    initImageComponents(userId);
  }

  private boolean validEmailFrequency(String emailFrequencyString){
    int emailFrequency = 0;
    try {
      emailFrequency = Integer.valueOf(emailFrequencyString);
      if(emailFrequency < 1) return false;
    } catch (NumberFormatException e) {
      return false;
    }

    return true;
  }

  /**
   * A wrapper class of tab, which provides help method to get/set UI components
   */
  private class GalleryAppTab{
    Label buttonNext;
    Label noResultsFound;
    Label keywordTotalResultsLabel;
    Label generalTotalResultsLabel;
    /**
     * @param container: the FlowPanel that this app tab will reside.
     * @param content: the sub-panel that contains the actual app content.
     * @apram incomingUserId: the user id
     */
    GalleryAppTab(FlowPanel container, FlowPanel content, final String incomingUserId){
      addGalleryAppTab(container, content,incomingUserId);
    }

    /**
     * @return Label buttonNext
     */
    public Label getButtonNext(){
      return buttonNext;
    }

    /**
     * @return Label noResultsFound
     */
    public Label getNoResultsFound(){
      return noResultsFound;
    }

    /**
     * @return Label keywordTotalResultsLabel
     */
    public Label getKeywordTotalResultsLabel(){
      return keywordTotalResultsLabel;
    }

    /**
     * Set keywordTotalResultsLabel's text to new text
     * @param keyword the search keyword
     * @param num number of results
     */
    public void setKeywordTotalResultsLabel(String keyword, int num){
       keywordTotalResultsLabel.setText(MESSAGES.gallerySearchResultsPrefix() + keyword + MESSAGES.gallerySearchResultsInfix() + num + MESSAGES.gallerySearchResultsSuffix());
    }

    /**
     * @return Label generalTotalResultsLabel
     */
    public Label getGeneralTotalResultsLabel(){
      return generalTotalResultsLabel;
    }

    /**
     * set generalTotalResultsLabel to new text
     * @param num number of results
     */
    public void setGeneralTotalResultsLabel(int num){
      generalTotalResultsLabel.setText(num + MESSAGES.gallerySearchResultsSuffix());
    }

    /**
     * Creates the GUI components for a regular app tab.
     * This method resides here because it needs access to global variables.
     * @param container: the FlowPanel that this app tab will reside.
     * @param content: the sub-panel that contains the actual app content.
     */
    private void addGalleryAppTab(FlowPanel container, FlowPanel content, final String incomingUserId) {
      // Search specific
      generalTotalResultsLabel = new Label();
      container.add(generalTotalResultsLabel);

      final OdeAsyncCallback<GalleryAppListResult> byAuthorCallback = new OdeAsyncCallback<GalleryAppListResult>(
        // failure message
        MESSAGES.galleryError()) {
        @Override
        public void onSuccess(GalleryAppListResult appsResult) {
          refreshApps(appsResult,false);
        }
      };
      Ode.getInstance().getGalleryService().getDeveloperApps(userId,appCatalogCounter ,NUMAPPSTOSHOW, byAuthorCallback);
      container.add(content);

      buttonNext = new Label();
      buttonNext.setText(MESSAGES.galleryMoreApps());
      buttonNext.addStyleName("active");

      FlowPanel next = new FlowPanel();
      next.add(buttonNext);
      next.addStyleName("gallery-nav-next");
      container.add(next);
      buttonNext.addClickHandler(new ClickHandler() {
        //  @Override
        public void onClick(ClickEvent event) {
           if (!appCatalogExhausted) {
                // If the next page still has apps to retrieve, do it
                appCatalogCounter += NUMAPPSTOSHOW;
                Ode.getInstance().getGalleryService().getDeveloperApps(userId,appCatalogCounter ,NUMAPPSTOSHOW, byAuthorCallback);
              }
        }
      });
    }
  }

  /**
   * Loads the proper tab GUI with gallery's app data.
   * @param apps: list of returned gallery apps from callback.
   */
  private void refreshApps(GalleryAppListResult appsResult, boolean refreshable) {
        appCatalogTab.setGeneralTotalResultsLabel(appsResult.getTotalCount());
        if (appsResult.getTotalCount() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appCatalogExhausted = true;
        } else {
          appCatalogExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appCatalogContent, refreshable);
        if(appsResult.getTotalCount() < NUMAPPSTOSHOW || appCatalogCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appCatalogTab.getButtonNext().setVisible(false);
        }
  }

  /**
   * Gets the number of projects
   *
   * @return the number of projects
   */
  public int getNumProjects() {
    return apps.size();
  }

  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getNumSelectedApps() {
    return selectedApps.size();
  }

  /**
   * Returns the list of selected projects
   *
   * @return the selected projects
   */
  public List<GalleryApp> getSelectedApps() {
    return selectedApps;
  }
  /**
   * select specific tab index based on given index
   * @param index
   */
  public void setSelectTabIndex(int index){
    appTabs.selectTab(index);
  }
  /**
   * Process the results after retrieving GalleryAppListResult
   * @param appsResult GalleryAppList Result
   * @param refreshable whether or not clear container
   * @see GalleryRequestListener
   */
  public void onAppListRequestCompleted(GalleryAppListResult appsResult, boolean refreshable)
  {
    List<GalleryApp> apps = appsResult.getApps();
    if (apps != null)
      refreshApps(appsResult, refreshable);
    else
      OdeLog.log("apps was null");
  }
  /**
   * Process the results after retrieving list of GalleryComment
   * @see GalleryRequestListener
   */
  public void onCommentsRequestCompleted(List<GalleryComment> comments){

  }

  /**
   * Process the results after retrieving list of UserProject
   * @see GalleryRequestListener
   */
  public void onSourceLoadCompleted(UserProject projectInfo) {

  }
}
