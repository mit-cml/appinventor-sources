// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import java.util.Date;
import java.util.List;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.wizards.youngandroid.RemixedYoungAndroidProjectWizard;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The gallery page shows a single app from the gallery
 *
 * It has different modes for public viewing or when user is publishing for first time
 * or updating a previously published app
 *
 * @author wolberd@gmail.com (Dave Wolber)
 * @author vincentaths@gmail.com (Vincent Zhang)
 */
public class GalleryPage extends Composite implements GalleryRequestListener {
  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);
  final Ode ode = Ode.getInstance();

  GalleryClient gallery = null;
  GalleryGuiFactory galleryGF = new GalleryGuiFactory();
  GalleryApp app = null;
  String projectName = null;
  Project project;

  private final String HOLLOW_HEART_ICON_URL = "/images/numLikeHollow.png";
  private final String RED_HEART_ICON_URL = "/images/numLike.png";
  private final String DOWNLOAD_ICON_URL = "/images/numDownload.png";
  private final String NUM_VIEW_ICON_URL = "/images/numView.png";
  private final String NUM_COMMENT_ICON_URL = "/images/numComment.png";
  private boolean imageUploaded = false;

  private VerticalPanel panel;  // the main panel
  private FlowPanel galleryGUI;
  private FlowPanel appSingle;
  private FlowPanel appsByAuthor;
  private FlowPanel appsByTags;
  private FlowPanel appsRemixes;
  private FlowPanel appDetails;
  private FlowPanel appHeader;
  private FlowPanel appInfo;
  private FlowPanel appAction;
  private FlowPanel appAuthor;
  private FlowPanel appMeta;
  private FlowPanel appDates;
  private FlowPanel appPrimaryWrapper;
  private FlowPanel appSecondaryWrapper;
  private TabPanel appActionTabs;
  private TabPanel sidebarTabs;
  private FlowPanel appDescPanel;
  private FlowPanel appReportPanel;
  private FlowPanel appSharePanel;
  private FlowPanel appComments;
  private FlowPanel appCommentsList;
  private FlowPanel returnToGallery;
//private String tagSelected;

  public static final int VIEWAPP = 0;
  public static final int NEWAPP = 1;
  public static final int UPDATEAPP = 2;
  private int editStatus;
  private static final int MIN_DESC_LENGTH = 40;

  /* Publish & edit state components */
  private FlowPanel imageUploadBox;
  private Label imageUploadPrompt;
  private Image image;
  private FileUpload upload;
  private FlowPanel imageUploadBoxInner;
  private FocusPanel wrapper;
  private Label appCreated;
  private Label appChanged;
  private TextArea titleText;
  private TextArea desc;
  private TextArea moreInfoText;
  private TextArea creditText;
  private FlowPanel descBox;
  private FlowPanel titleBox;
  private Label likeCount;
  private Button actionButton;
  private Button removeButton;
  private Button editButton;
  private Button cancelButton;

  private HTML ccLicenseRef;


/* Here is the organization of this page:
panel
 galleryGUI
  appSingle
   appDetails
    appClear
      appHeader
        wrapper (focus panel)
         imageUploadBox (flow)
          imageUploadBoxInner (flow)
           imageUploadPRompt (label)
           upload
           image (this is put in dynamically)
        appAction (button)
     appInfo
       title or titlebox
       devName
       appMeta
       appDates
       desc/descbox
    appComments
   appsByDev

  divider
*/

  /**
   * Creates a new GalleryPage, must take in parameters
   * @param app GalleryApp
   * @param editStatus edit status
   */
  public GalleryPage(final GalleryApp app, final int editStatus) {
    // Get a reference to the Gallery Client which handles the communication to
    // server to get gallery data
    gallery = GalleryClient.getInstance();
    gallery.addListener(this);

    // We are either publishing a new app, updating, or just reading. If we are publishing
    //   a new app, app has some partial info to be published. Otherwise, it has all
    //   the info for the already published app
    this.app = app;
    this.editStatus = editStatus;
    initComponents();

    // App header - image
    appHeader.addStyleName("app-header");
    // If we're editing or updating, add input form for image
    if (newOrUpdateApp()) {
      initImageComponents();
    } else  { // we are just viewing this page so setup the image
      initReadOnlyImage();
    }

    // Now let's add the button for publishing, updating, or trying
    appHeader.add(appAction);
    initActionButton();
    if (editStatus==NEWAPP) {
      initCancelButton();
      /* Add Creative Commons Publishing Reference */
      appAction.add(ccLicenseRef);
    }
    if (editStatus==UPDATEAPP) {
      initRemoveButton();
      initCancelButton();
      /* Add Creative Commons Updating Reference */
      appAction.add(ccLicenseRef);
    }

    // App details - app title
    appInfo.add(titleBox);
    initAppTitle(titleBox);

    // App details - app author info
    appInfo.add(appAuthor);
    initAppAuthor(appAuthor);

    // Not showing in new app becaus it doesn't have these info
    // App details - meta
    if (!newOrUpdateApp()) {
      appInfo.add(appMeta);
      initAppStats(appMeta);
    }

    // App details - dates
    appInfo.add(appDates);
    initAppMeta(appDates);

     // App details - app description
    appInfo.add(descBox);
    initAppDesc(descBox, appDescPanel);
    /**
     * TODO: I may need to change the code logic here. appDescPanel is actually
     * not added to [appInfo], instead in public state appDescPanel will be
     * added into the [appActionTabs] (not showing in editable states) as a sub
     * tab. So it may not be the best idea to modify appDescPanel in a method
     * that resides in [appInfo]'s code block. - Vincent, 03/28/2014
     */


    // Pass app components to App Detail container
    appInfo.addStyleName("app-info-container");
    appPrimaryWrapper.add(appHeader);
    appPrimaryWrapper.add(appInfo);
    appPrimaryWrapper.addStyleName("clearfix");
    appDetails.add(appPrimaryWrapper);


    // If app is in its public state, add action tabs
    if (!newOrUpdateApp()) {
      // Add a divider
      HTML dividerPrimary = new HTML("<div class='section-divider'></div>");
      appDetails.add(dividerPrimary);
      // Initialize action tabs
      initActionTabs();
      // Initialize app share
      initAppShare();
      // Initialize app action features
      initReportSection();


      // We are not showing comments at initial launch, Such sadness :'[
      /*
      HTML dividerSecondary = new HTML("<div class='section-divider'></div>");
      appDetails.add(dividerSecondary);
      initAppComments();
      */

      // Add sidebar stuff, only in public state
      // By default, load the first tag's apps
      gallery.GetAppsByDeveloper(0, 5, app.getDeveloperId());
    }

    // Add to appSingle
    appSingle.add(appDetails);
    appDetails.addStyleName("gallery-container");
    appDetails.addStyleName("gallery-app-details");

    if (!newOrUpdateApp()) {
      appSingle.add(sidebarTabs);
      sidebarTabs.addStyleName("gallery-container");
      sidebarTabs.addStyleName("gallery-app-showcase");
    }

    // Add everything to top-level containers
    galleryGUI.add(appSingle);
    appSingle.addStyleName("gallery-app-single");
    panel.add(galleryGUI);
    galleryGUI.addStyleName("gallery");
    initWidget(panel);
  }

   /**
   * Helper method called by constructor to initialize ui components
   */
  private void initComponents() {
    // Initialize UI
    panel = new VerticalPanel();
    panel.setWidth("100%");
    galleryGUI = new FlowPanel();
    appSingle = new FlowPanel();
    appDetails = new FlowPanel();
    appHeader = new FlowPanel();
    appInfo = new FlowPanel();
    appAction = new FlowPanel();
    appAuthor = new FlowPanel();
    appMeta = new FlowPanel();
    appDates = new FlowPanel();
    appPrimaryWrapper = new FlowPanel();
    appSecondaryWrapper = new FlowPanel();
    appDescPanel = new FlowPanel();
    appReportPanel = new FlowPanel();
    appSharePanel = new FlowPanel();
    appActionTabs = new TabPanel();
    sidebarTabs = new TabPanel();
    appComments = new FlowPanel();
    appCommentsList = new FlowPanel();
    appsByAuthor = new FlowPanel();
    appsByTags = new FlowPanel();
    appsRemixes = new FlowPanel();
    returnToGallery = new FlowPanel();
//    tagSelected = "";

    appCreated = new Label();
    appChanged = new Label();
    descBox = new FlowPanel();
    titleBox = new FlowPanel();
    desc = new TextArea();
    titleText = new TextArea();
    moreInfoText = new TextArea();
    creditText = new TextArea();
    ccLicenseRef = new HTML(MESSAGES.galleryCcLicenseRef());
    ccLicenseRef.addStyleName("app-action-html");
  }


  /**
   * Helper method to check if the app is in its "editable" state
   * If the app is in its "public" state it should return false
   */
  private boolean newOrUpdateApp() {
    if ((editStatus==NEWAPP) || (editStatus==UPDATEAPP))
      return true;
    else
      return false;
  }


  /**
   * Helper method called by constructor to initialize image upload components
   */
  private void initImageComponents() {
    imageUploadBox = new FlowPanel();
    imageUploadBox.addStyleName("app-image-uploadbox");
    imageUploadBox.addStyleName("gallery-editbox");
    imageUploadBoxInner = new FlowPanel();
    imageUploadPrompt = new Label("Upload your project image!");
    imageUploadPrompt.addStyleName("gallery-editprompt");

    updateAppImage(gallery.getCloudImageURL(app.getGalleryAppId()), imageUploadBoxInner);
    image.addStyleName("status-updating");
    imageUploadPrompt.addStyleName("app-image-uploadprompt");
    imageUploadBoxInner.add(imageUploadPrompt);

    upload = new FileUpload();
    upload.addStyleName("app-image-upload");
    upload.getElement().setAttribute("accept", "image/*");
    // Set the correct handler for servlet side capture
    upload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
    upload.addChangeHandler(new ChangeHandler (){
      public void onChange(ChangeEvent event) {
        uploadImage();
      }
    });
    imageUploadBoxInner.add(upload);
    imageUploadBox.add(imageUploadBoxInner);
    wrapper = new FocusPanel();
    wrapper.add(imageUploadBox);
    wrapper.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // The correct way to trigger click event on FileUpload
        upload.getElement().<InputElement>cast().click();
      }
    });
    appHeader.add(wrapper);
  }


  /**
   * Helper method called by constructor to create the app image for display
   */
  private void initReadOnlyImage() {
    updateAppImage(gallery.getCloudImageURL(app.getGalleryAppId()), appHeader);
  }


  /**
   * Main method to validify and upload the app image
   */
  private void uploadImage() {
    String uploadFilename = upload.getFilename();
    if (!uploadFilename.isEmpty()) {
      // Grab and validify the filename
      final String filename = makeValidFilename(uploadFilename);
      // Forge the request URL for gallery servlet
      // we used to send the gallery id to the servlet, now the project id as
      // the servlet just stores image temporarily before publish
      /* String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.GALLERY_SERVLET +
          "/apps/" + String.valueOf(app.getGalleryAppId()) + "/"+ filename; */
      // send the project id as the id, to store image temporarily until published
      String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.GALLERY_SERVLET +
          "/apps/" + String.valueOf(app.getProjectId()) + "/"+ filename;
      Uploader.getInstance().upload(upload, uploadUrl,
          new OdeAsyncCallback<UploadResponse>(MESSAGES.fileUploadError()) {
          @Override
          public void onSuccess(UploadResponse uploadResponse) {
            switch (uploadResponse.getStatus()) {
            case SUCCESS:
              // Update the app image preview after a success upload
              imageUploadBoxInner.clear();
              // updateAppImage(app.getCloudImageURL(), imageUploadBoxInner);
              updateAppImage(gallery.getProjectImageURL(app.getProjectId()),imageUploadBoxInner);
              imageUploaded=true;
              ErrorReporter.hide();
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
    } else {
      if (editStatus == NEWAPP) {
        Window.alert(MESSAGES.noFileSelected());
      }
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
   * Helper method to update the app image
   * @param url  The URL of the image to show
   * @param container  The container that image widget resides
   */
  private void updateAppImage(String url, final Panel container) {
      image = new Image();
      image.addStyleName("app-image");
      image.setUrl(url);
      // if the user has provided a gallery app image, we'll load it. But if not
      // the error will occur and we'll load default image
      image.addErrorHandler(new ErrorHandler() {
        public void onError(ErrorEvent event) {
          image.setUrl(GalleryApp.DEFAULTGALLERYIMAGE);
        }
      });
      container.add(image);

      if(gallery.getSystemEnvironment() != null &&
          gallery.getSystemEnvironment().toString().equals("Development")){
        final OdeAsyncCallback<String> callback = new OdeAsyncCallback<String>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(String newUrl) {
              image.setUrl(newUrl + "?" + System.currentTimeMillis());
            }
          };
        Ode.getInstance().getGalleryService().getBlobServingUrl(url, callback);
      }
  }

  /**
   * Helper method called by constructor to create the app's main action button
   */
  private void initActionButton () {
    if (editStatus==NEWAPP)
      initPublishButton();
    else if (editStatus == UPDATEAPP)
      initUpdateButton();
    else{ // Public view state
      initTryitButton();
      initEdititButton();
    }
  }


  /**
   * Helper method called by constructor to initialize the app's title section
   * @param container   The container that title resides
   */
  private void initAppTitle(Panel container) {
    if (newOrUpdateApp()) {
      // GUI for editable title container
      if (editStatus==NEWAPP) {
        // If it's new app, give a textual hint telling user this is title
        titleText.setText(app.getTitle());
      } else if (editStatus==UPDATEAPP) {
        // If it's not new, just set whatever's in the data field already
        titleText.setText(app.getTitle());
      }
      titleText.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          app.setTitle(titleText.getText());
        }
      });
      titleText.addStyleName("app-desc-textarea");
      container.add(titleText);
      container.addStyleName("app-title-container");
    } else {
      Label title = new Label(app.getTitle());
      title.addStyleName("app-title");
      container.add(title);
    }
  }


  /**
   * Helper method called by constructor to initialize the author's info
   * @param container   The container that author's info resides
   */
  private void initAppAuthor(Panel container) {


    // Add author's image - not when creating a new app
    if (editStatus != NEWAPP) {
      final Image authorAvatar = new Image();
      authorAvatar.addStyleName("app-userimage");
      authorAvatar.setUrl(gallery.getUserImageURL(app.getDeveloperId()));
      // If the user has provided a gallery app image, we'll load it. But if not
      // the error will occur and we'll load default image
      authorAvatar.addErrorHandler(new ErrorHandler() {
        public void onError(ErrorEvent event) {
          authorAvatar.setUrl(GalleryApp.DEFAULTUSERIMAGE);
        }
      });
      authorAvatar.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToUserProfileView(
              app.getDeveloperId(), 1 /* 1 for public view */ );
        }
      });
      appInfo.add(authorAvatar);
    }

    // Add author's name
    final Label authorName = new Label();
    if (editStatus == NEWAPP) {
      // App doesn't have author info yet, grab current user info
      final User currentUser = Ode.getInstance().getUser();
      authorName.setText(currentUser.getUserName());
    } else {
      authorName.setText(app.getDeveloperName());
      authorName.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToUserProfileView(
              app.getDeveloperId(), 1 /* 1 for public view*/ );
        }
      });
    }
    authorName.addStyleName("app-username");
    authorName.addStyleName("app-subtitle");
    appInfo.add(authorName);
  }

  /**
   * Helper method called by constructor to initialize the app's stats fields
   * @param container   The container that stats fields reside
   */
  private void initAppStats(Panel container) {
    // Images for stats data
    Image numDownloads = new Image();
    numDownloads.setUrl(DOWNLOAD_ICON_URL);
    Image numLikes = new Image();
    numLikes.setUrl(HOLLOW_HEART_ICON_URL);

    // Add stats data
    container.addStyleName("app-stats");
    container.add(numDownloads);
    container.add(new Label(Integer.toString(app.getDownloads())));
    // Adds dynamic like
    initLikeSection(container);
    // Adds dynamic feature
    initFeatureSection(container);
    // Adds dynamic tutorial
    initTutorialSection(container);
    // Adds dynamic salvage
    initSalvageSection(container);

    // We are not using views and comments at initial launch
    /*
    Image numViews = new Image();
    numViews.setUrl(NUM_VIEW_ICON_URL);
    Image numComments = new Image();
    numComments.setUrl(NUM_COMMENT_ICON_URL);
    container.add(numViews);
    container.add(new Label(Integer.toString(app.getViews())));
    container.add(numComments);
    container.add(new Label(Integer.toString(app.getComments())));
    */
  }


  /**
   * Helper method called by constructor to initialize the app's meta fields
   * @param container   The container that date fields reside
   */
  private void initAppMeta(Panel container) {
    Date createdDate = new Date();
    Date changedDate = new Date();
    if (editStatus == NEWAPP) {
    } else {
      createdDate = new Date(app.getCreationDate());
      changedDate = new Date(app.getUpdateDate());
    }
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd");

    Label appCreatedLabel = new Label(MESSAGES.galleryCreatedDateLabel());
    appCreatedLabel.addStyleName("app-meta-label");
    container.add(appCreatedLabel);
    appCreated.setText(dateFormat.format(createdDate));
    container.add(appCreated);

    Label appChangedLabel = new Label(MESSAGES.galleryChangedDateLabel());
    appChangedLabel.addStyleName("app-meta-label");
    container.add(appChangedLabel);
    appChanged.setText(dateFormat.format(changedDate));
    container.add(appChanged);

    if (newOrUpdateApp()) {
      // GUI for editable title container
      // Set the placeholders of textarea
      moreInfoText.getElement().setPropertyString("placeholder", MESSAGES.galleryMoreInfoHint());
      creditText.getElement().setPropertyString("placeholder", MESSAGES.galleryCreditHint());

      if (editStatus==NEWAPP) {
        // If it's a new app, it will show the placeholder hint
      } else if (editStatus==UPDATEAPP) {
        // If it's not new, just set whatever's in the data field already
        moreInfoText.setText(app.getMoreInfo());
        creditText.setText(app.getCredit());
      }

      moreInfoText.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          app.setMoreInfo(moreInfoText.getText());
        }
      });
      creditText.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
         app.setCredit(creditText.getText());
        }
      });

      moreInfoText.addStyleName("app-desc-textarea");
      creditText.addStyleName("app-desc-textarea");
      container.add(moreInfoText);
      container.add(creditText);

    } else { // Public app view
      String linktext = makeValidLink(app.getMoreInfo());
      if(linktext != null){
        Label moreInfoLabel = new Label(MESSAGES.galleryMoreInfoLabel());
        moreInfoLabel.addStyleName("app-meta-label");
        container.add(moreInfoLabel);

        Anchor userLinkDisplay = new Anchor();
        userLinkDisplay.setText(linktext);
        userLinkDisplay.setHref(linktext);
        userLinkDisplay.setTarget("_blank");
        container.add(userLinkDisplay);
      }
      //"remixed from" field
      container.add(initRemixFromButton());

      //"credits" field
      if(app.getCredit() != null && app.getCredit().length() > 0){
        Label creditLabel = new Label(MESSAGES.galleryCreditLabel());
        creditLabel.addStyleName("app-meta-label");
        container.add(creditLabel);

        Label creditText = new Label(app.getCredit());
        container.add(creditText);
      }
    }

    container.addStyleName("app-meta");
  }

  /**
   * Helper method to validify a hyperlink
   * @param linktext    the actual http link that the anchor should point to
   * @return linktext a valid http link or null.
   */
  private String makeValidLink(String linktext) {
    if (linktext == null) {
      return null;
    } else {
      if (linktext.isEmpty()) {
        return null;
      } else {
        // Validate link format, fill in http part
        if (!linktext.toLowerCase().startsWith("http")) {
          linktext = "http://" + linktext;
        }
        return linktext;
      }
    }
  }

  /**
   * Helper method called by constructor to initialize the app's description
   * @param c1   The container that description resides (editable state)
   * @param c2   The container that description resides (public state)
   */
  private void initAppDesc(Panel c1, Panel c2) {
    desc.getElement().setPropertyString("placeholder", MESSAGES.galleryDescriptionHint());
    if (newOrUpdateApp()) {
      desc.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          app.setDescription(desc.getText());
        }
      });
      if(editStatus==UPDATEAPP){
        desc.setText(app.getDescription());
      }
      desc.addStyleName("app-desc-textarea");
      c1.add(desc);
    } else {
      Label description = new Label(app.getDescription());
      c2.add(description);
      c2.addStyleName("app-description");
    }
  }

  /**
   * Helper method called by constructor to initialize the app's comment area
   */
  private void initAppComments() {
    // App details - comments
    appDetails.add(appComments);
    appComments.addStyleName("app-comments-wrapper");
    Label commentsHeader = new Label("Comments and Reviews");
    commentsHeader.addStyleName("app-comments-header");
    appComments.add(commentsHeader);
    final TextArea commentTextArea = new TextArea();
    commentTextArea.addStyleName("app-comments-textarea");
    appComments.add(commentTextArea);
    Button commentSubmit = new Button("Submit my comment");
    commentSubmit.addStyleName("app-comments-submit");
    commentSubmit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Long> commentPublishCallback = new OdeAsyncCallback<Long>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Long date) {
                // get the new comment list so gui updates
                //   note: we might modify the call to publishComment so it returns
                //   the list instead, this would save one server call
                gallery.GetComments(app.getGalleryAppId(), 0, 100);
              }
          };
        Ode.getInstance().getGalleryService().publishComment(app.getGalleryAppId(),
            commentTextArea.getText(), commentPublishCallback);
      }
    });
    appComments.add(commentSubmit);

    // Add list of comments
    gallery.GetComments(app.getGalleryAppId(), 0, 100);
    appComments.add(appCommentsList);
    appCommentsList.addStyleName("app-comments");

  }

  /**
   * Helper method called by constructor to initialize the app action tabs
   */
  private void initActionTabs() {
    // Add a bunch of tabs for executable actions regarding the app
    appSecondaryWrapper.addStyleName("clearfix");
    appSecondaryWrapper.add(appActionTabs);
    appActionTabs.addStyleName("app-actions");
    appActionTabs.add(appDescPanel, "Description");
    appActionTabs.add(appSharePanel, "Share");
    appActionTabs.add(appReportPanel, "Report");
    appActionTabs.selectTab(0);
    appActionTabs.addStyleName("app-actions-tabs");
    appDetails.add(appSecondaryWrapper);
    // Return to Gallery link
    Label returnLabel = new Label("Back to Gallery");
    returnLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToGalleryView();
      }
    });
    returnToGallery.add(returnLabel);
    returnToGallery.addStyleName("gallery-nav-return");
    returnToGallery.addStyleName("primary-link");
    appSecondaryWrapper.add(returnToGallery); //
  }

  /**
   * Helper method called by constructor to initialize the remix button
   */
  private FlowPanel initRemixFromButton(){
    FlowPanel container = new FlowPanel();
    final Label remixedFrom = new Label(MESSAGES.galleryRemixedFrom());
    remixedFrom.addStyleName("app-meta-label");
    final Label parentApp = new Label();
    //gwt-Label use fixed width which will case border-underline-dot
    //be longer than text link.
    //gwt-Label-auto use auto width
    parentApp.removeStyleName("gwt-Label");
    parentApp.addStyleName("gwt-Label-auto");
    parentApp.addStyleName("primary-link");
    container.add(remixedFrom);
    container.add(parentApp);
    remixedFrom.setVisible(false);
    parentApp.setVisible(false);

    final Result<GalleryApp> attributionGalleryApp = new Result<GalleryApp>();
    final OdeAsyncCallback<Long> remixedFromCallback = new OdeAsyncCallback<Long>(
    // failure message
    MESSAGES.galleryError()) {
    @Override
      public void onSuccess(final Long attributionId) {
        if (attributionId != UserProject.FROMSCRATCH) {
          remixedFrom.setVisible(true);
          parentApp.setVisible(true);
          final OdeAsyncCallback<GalleryApp> callback = new OdeAsyncCallback<GalleryApp>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(GalleryApp AppRemixedFrom) {
              parentApp.setText(AppRemixedFrom.getTitle());
              attributionGalleryApp.t = AppRemixedFrom;
            }
          };
          Ode.getInstance().getGalleryService().getApp(attributionId, callback);
        } else {
          attributionGalleryApp.t = null;
        }
      }
    };
    Ode.getInstance().getGalleryService().remixedFrom(app.getGalleryAppId(), remixedFromCallback);

    parentApp.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          if (attributionGalleryApp.t == null) {
          } else {
            Ode.getInstance().switchToGalleryAppView(attributionGalleryApp.t, GalleryPage.VIEWAPP);
          }
        }
    });

    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
        public void onSuccess(final List<GalleryApp> apps) {
          if (apps.size() != 0) {
            // Display remixes at the sidebar on the same page
            galleryGF.generateSidebar(apps, sidebarTabs, appsRemixes, "Remixes",
                MESSAGES.galleryAppsRemixesSidebar() + app.getTitle(), false, false);
          }
        }
      };
    Ode.getInstance().getGalleryService().remixedTo(app.getGalleryAppId(), callback);

    return container;
  }

  /**
   * Helper method called by constructor to initialize the report section
   */
  private void initReportSection() {
    final HTML reportPrompt = new HTML();
    reportPrompt.setHTML(MESSAGES.galleryReportPrompt());
    reportPrompt.addStyleName("primary-prompt");
    final TextArea reportText = new TextArea();
    reportText.addStyleName("action-textarea");
    final Button submitReport = new Button(MESSAGES.galleryReportButton());
    submitReport.addStyleName("action-button");
    final Label descriptionError = new Label();
    descriptionError.setText("Description required");
    descriptionError.setStyleName("ode-ErrorMessage");
    descriptionError.setVisible(false);
    appReportPanel.add(reportPrompt);
    appReportPanel.add(descriptionError);
    appReportPanel.add(reportText);
    appReportPanel.add(submitReport);

    final OdeAsyncCallback<Boolean> isReportdByUserCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean isAlreadyReported) {
            if(isAlreadyReported) { //already reported, cannot report again
              reportPrompt.setHTML(MESSAGES.galleryAlreadyReportedPrompt());
              reportText.setVisible(false);
              submitReport.setVisible(false);
              submitReport.setEnabled(false);
            } else {
              submitReport.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                  final OdeAsyncCallback<Long> reportClickCallback = new OdeAsyncCallback<Long>(
                      // failure message
                      MESSAGES.galleryError()) {
                        @Override
                        public void onSuccess(Long id) {
                          reportPrompt.setHTML(MESSAGES.galleryReportCompletionPrompt());
                          reportText.setVisible(false);
                          submitReport.setVisible(false);
                          submitReport.setEnabled(false);
                        }
                    };
                  if (!reportText.getText().trim().isEmpty()){
                    Ode.getInstance().getGalleryService().addAppReport(app, reportText.getText(),
                      reportClickCallback);
                    descriptionError.setVisible(false);
                  } else {
                    descriptionError.setVisible(true);
                  }
                }
              });
            }
          }
      };
    Ode.getInstance().getGalleryService().isReportedByUser(app.getGalleryAppId(),
        isReportdByUserCallback);
  }

  /**
   * Helper method called by constructor to initialize the report section
   */
  private void initAppShare() {
    final HTML sharePrompt = new HTML();
    sharePrompt.setHTML(MESSAGES.gallerySharePrompt());
    sharePrompt.addStyleName("primary-prompt");
    final TextBox urlText = new TextBox();
    urlText.addStyleName("action-textbox");
    urlText.setText(Window.Location.getHost() + MESSAGES.galleryGalleryIdAction() + app.getGalleryAppId());
    urlText.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        urlText.selectAll();
      }
    });
    appSharePanel.add(sharePrompt);
    appSharePanel.add(urlText);
  }

  /**
   * Helper method called by constructor to initialize the like section
   * @param container   The container that like label & image reside
   */
  private void initLikeSection(Panel container) { //TODO: Update the location of this button
    final Image likeButton = new Image();
    likeButton.setUrl(HOLLOW_HEART_ICON_URL);
    container.add(likeButton);
    likeCount = new Label(MESSAGES.galleryEmptyText());
    container.add(likeCount);
    final Label likePrompt = new Label(MESSAGES.galleryEmptyText());
    likePrompt.addStyleName("primary-link");
    container.add(likePrompt);
    likePrompt.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Integer> changeLikeCallback = new OdeAsyncCallback<Integer>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Integer num) {
                // TODO: deal with/discuss server data sync later; now is updating locally.
                final OdeAsyncCallback<Boolean> checkCallback = new OdeAsyncCallback<Boolean>(
                    MESSAGES.galleryError()) {
                      @Override
                      public void onSuccess(Boolean b) {
                        //email will be send automatically if condition matches (in ObjectifyGalleryStorageIo)
                      }
                };
                Ode.getInstance().getGalleryService().checkIfSendAppStats(app.getDeveloperId(), app.getGalleryAppId(),
                    gallery.getGallerySettings().getAdminEmail(), Window.Location.getHost(), checkCallback);
              }
          };
        final OdeAsyncCallback<Boolean> isLikedByUserCallback = new OdeAsyncCallback<Boolean>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Boolean bool) {
                if (bool) { // If the app is already liked before, and user clicks again, that means unlike
                  Ode.getInstance().getGalleryService().decreaseLikes(app.getGalleryAppId(),
                      changeLikeCallback);
                  likePrompt.setText(MESSAGES.galleryAppsLike());
                  // Old code
                  likeCount.setText(String.valueOf(Integer.valueOf(likeCount.getText()) - 1));
                  likeButton.setUrl(HOLLOW_HEART_ICON_URL); // Unliked
                } else {
                  // If the app is not yet liked, and user clicks like, that means add a like
                  Ode.getInstance().getGalleryService().increaseLikes(app.getGalleryAppId(),
                      changeLikeCallback);
                  likePrompt.setText(MESSAGES.galleryAppsAlreadyLike());
                  // Old code
                  likeCount.setText(String.valueOf(Integer.valueOf(likeCount.getText()) + 1));
                  likeButton.setUrl(RED_HEART_ICON_URL); // Liked
                }
              }
          };
        Ode.getInstance().getGalleryService().isLikedByUser(app.getGalleryAppId(),
            isLikedByUserCallback); // This happens when user click on like, we need to check if it's already liked
      }
    });

    final OdeAsyncCallback<Integer> likeNumCallback = new OdeAsyncCallback<Integer>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Integer num) {
            likeCount.setText(String.valueOf(num));
          }
      };
    Ode.getInstance().getGalleryService().getNumLikes(app.getGalleryAppId(),
        likeNumCallback);

    final OdeAsyncCallback<Boolean> isLikedCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean bool) {
            if (!bool) {
              likePrompt.setText(MESSAGES.galleryAppsLike());
              likeButton.setUrl(HOLLOW_HEART_ICON_URL);//unliked
            } else {
              likePrompt.setText(MESSAGES.galleryAppsAlreadyLike());
              likeButton.setUrl(RED_HEART_ICON_URL);//liked
            }
          }
      };
    Ode.getInstance().getGalleryService().isLikedByUser(app.getGalleryAppId(),
        isLikedCallback);
  }

  /**
   * Helper method called by constructor to initialize the salvage section
   * @param container   The container that salvage label reside
   */
  private void initSalvageSection(Panel container) { //TODO: Update the location of this button
    if (!canSalvage()) {                              // Permitted to salvage?
      return;
    }

    final Label salvagePrompt = new Label("salvage");
    salvagePrompt.addStyleName("primary-link");
    container.add(salvagePrompt);

    salvagePrompt.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Void bool) {
                salvagePrompt.setText("done");
              }
          };
        Ode.getInstance().getGalleryService().salvageGalleryApp(app.getGalleryAppId(), callback);
      }
    });
  }


  /**
   * Helper method called by constructor to initialize the feature section
   * @param container   The container that feature label reside
   */
  private void initFeatureSection(Panel container) { //TODO: Update the location of this button
    final User currentUser = Ode.getInstance().getUser();
    if(currentUser.getType() != User.MODERATOR){     //not admin
      return;
    }

    final Label featurePrompt = new Label(MESSAGES.galleryEmptyText());
    featurePrompt.addStyleName("primary-link");
    container.add(featurePrompt);

    final OdeAsyncCallback<Boolean> isFeaturedCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean bool) {
            if (bool) { // If the app is already featured before, the prompt should show as unfeatured
              featurePrompt.setText(MESSAGES.galleryUnfeaturedText());
            } else {    // otherwise show as featured
              featurePrompt.setText(MESSAGES.galleryFeaturedText());
            }
          }
      };
    Ode.getInstance().getGalleryService().isFeatured(app.getGalleryAppId(),
        isFeaturedCallback); // This happens when user click on like, we need to check if it's already liked

    featurePrompt.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Boolean> markFeaturedCallback = new OdeAsyncCallback<Boolean>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Boolean bool) {
                if (bool) { // If the app is already featured, the prompt should show as unfeatured
                  featurePrompt.setText(MESSAGES.galleryUnfeaturedText());
                } else {    // otherwise show as featured
                  featurePrompt.setText(MESSAGES.galleryFeaturedText());
                }
                //update gallery list
                gallery.appWasChanged();
              }
          };
        Ode.getInstance().getGalleryService().markAppAsFeatured(app.getGalleryAppId(),
            markFeaturedCallback);
      }
    });
  }

  /**
   * Helper method called by constructor to initialize the tutorial section
   * @param container   The container that feature label reside
   */
  private void initTutorialSection(Panel container) { //TODO: Update the location of this button
    final User currentUser = Ode.getInstance().getUser();
    if(currentUser.getType() != User.MODERATOR){     //not admin
      return;
    }

    final Label tutorialPrompt = new Label(MESSAGES.galleryEmptyText());
    tutorialPrompt.addStyleName("primary-link");
    container.add(tutorialPrompt);

    final OdeAsyncCallback<Boolean> isTutorialCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean bool) {
            if (bool) { // If the app is already featured before, the prompt should show as unfeatured
              tutorialPrompt.setText(MESSAGES.galleryUntutorialText());
            } else {    // otherwise show as featured
              tutorialPrompt.setText(MESSAGES.galleryTutorialText());
            }
          }
      };
    Ode.getInstance().getGalleryService().isTutorial(app.getGalleryAppId(),
        isTutorialCallback); // This happens when user click on like, we need to check if it's already liked

    tutorialPrompt.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Boolean> markTutorialCallback = new OdeAsyncCallback<Boolean>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Boolean bool) {
                if (bool) { // If the app is already featured, the prompt should show as unfeatured
                  tutorialPrompt.setText(MESSAGES.galleryUntutorialText());
                } else {    // otherwise show as featured
                  tutorialPrompt.setText(MESSAGES.galleryTutorialText());
                }
                //update gallery list
                gallery.appWasChanged();
              }
          };
        Ode.getInstance().getGalleryService().markAppAsTutorial(app.getGalleryAppId(),
            markTutorialCallback);
      }
    });
  }

  /**
   * Helper method called by constructor to initialize the edit it button
   * Only seen by app owner.
   */
  private void initEdititButton() {
    final User currentUser = Ode.getInstance().getUser();
    if(app.getDeveloperId().equals(currentUser.getUserId())){
      editButton = new Button(MESSAGES.galleryEditText());
      editButton.addClickHandler(new ClickHandler() {
        // Open up source file if clicked the action button
        public void onClick(ClickEvent event) {
          editButton.setEnabled(false);
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.UPDATEAPP);
        }
      });
      editButton.addStyleName("app-action-button");
      appAction.add(editButton);
    }
  }

  /**
   * Helper method called by constructor to initialize the try it button
   */
  private void initTryitButton() {
    actionButton = new Button(MESSAGES.galleryOpenText());
    actionButton.addClickHandler(new ClickHandler() {
      // Open up source file if clicked the action button
      public void onClick(ClickEvent event) {
        actionButton.setEnabled(false);
        /*
         *  open a popup window that will prompt to ask user to enter
         *  a new project name(if "new name" is not valid, user may need to
         *  enter again). After that, "loadSourceFil" and "appWasDownloaded"
         *  will be called.
         */
        new RemixedYoungAndroidProjectWizard(app, actionButton).center();
      }
    });
    actionButton.addStyleName("app-action-button");
    appAction.add(actionButton);
  }

  /**
   * Helper method called by constructor to initialize the publish button
   */
  private void initPublishButton() {
    actionButton = new Button(MESSAGES.galleryPublishText());
    actionButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
         if(!checkIfReadyToPublishOrUpdateApp(app)){
           return;
         }
         actionButton.setEnabled(false);
         actionButton.setText(MESSAGES.galleryAppPublishing());
         final OdeAsyncCallback<GalleryApp> callback = new OdeAsyncCallback<GalleryApp>(
              MESSAGES.galleryError()) {
            @Override
            // When publish or update call returns
            public void onSuccess(final GalleryApp gApp) {
              // we only set the projectId to the gallery app if new app. If we
              // are updating its already set
              final OdeAsyncCallback<Void> projectCallback = new OdeAsyncCallback<Void>(
                  MESSAGES.galleryError()) {
                @Override
                public void onSuccess(Void result) {
                  // this is called after published and after we've set the gallery id
                  // tell the project list to change project's button to "Update"
                  Ode.getInstance().getProjectManager().publishProject(app.getProjectId(),
                      gApp.getGalleryAppId());
                  Ode.getInstance().switchToGalleryAppView(gApp, GalleryPage.VIEWAPP);
                  // above was app, switched to gApp which is the newly published thing
                  final OdeAsyncCallback<Long> attributionCallback = new OdeAsyncCallback<Long>(
                          MESSAGES.galleryError()) {
                        @Override
                        public void onSuccess(Long result) {
                        }
                  };
                  Ode.getInstance().getGalleryService().saveAttribution(gApp.getGalleryAppId(), app.getProjectAttributionId(),
                          attributionCallback);
                }//end of projectCallback#onSuccess
                @Override
                public void onFailure(Throwable caught) {
                  super.onFailure(caught);
                  actionButton.setEnabled(true);
                  actionButton.setText(MESSAGES.galleryPublishText());
                }
              };//end of projectCallback
              Ode.getInstance().getProjectService().setGalleryId(gApp.getProjectId(),
                  gApp.getGalleryAppId(), projectCallback);
              // we need to update the app object for this gallery page
              gallery.appWasChanged();
            }//end of callback#onSuccess
            @Override
            public void onFailure(Throwable caught) {
              Window.alert(MESSAGES.galleryNoExtensionsPlease());
              actionButton.setEnabled(true);
              actionButton.setText(MESSAGES.galleryPublishText());
            }
          };
          // call publish with the default app data...
          Ode.getInstance().getGalleryService().publishApp(app.getProjectId(),
              app.getTitle(), app.getProjectName(), app.getDescription(), app.getMoreInfo(), app.getCredit(), callback);
      }
    });
    actionButton.addStyleName("app-action-button");
    appAction.add(actionButton);
  }

  /**
   * Helper method called by constructor to initialize the publish button
   */
  private void initUpdateButton() {
    actionButton = new Button(MESSAGES.galleryUpdateText());
    actionButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
         if(!checkIfReadyToPublishOrUpdateApp(app)){
           return;
         }
         actionButton.setEnabled(false);
         actionButton.setText(MESSAGES.galleryAppUpdating());
         final OdeAsyncCallback<Void> updateSourceCallback = new OdeAsyncCallback<Void>(
            MESSAGES.galleryError()) {
            @Override
            public void onSuccess(Void result) {
              gallery.appWasChanged();  // to update the gallery list and page
              Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
            }
            @Override
            public void onFailure(Throwable caught) {
              Window.alert(MESSAGES.galleryNoExtensionsPlease());
              actionButton.setEnabled(true);
              actionButton.setText(MESSAGES.galleryUpdateText());
            }
          };
          Ode.getInstance().getGalleryService().updateApp(app,imageUploaded,updateSourceCallback);
      }
    });
    actionButton.addStyleName("app-action-button");
    appAction.add(actionButton);
  }

  /**
   * check if it is ready to publish or update GalleryApp
   * 1.The minimum length of Desc must be at least MIN_DESC_LENGTH
   * 2.User must upload an image first, in order to publish GaleryApp
   * @param app
   * @return
   */
  private boolean checkIfReadyToPublishOrUpdateApp(GalleryApp app){
    if(app.getDescription().length() < MIN_DESC_LENGTH){
      Window.alert(MESSAGES.galleryNotEnoughDescriptionMessage());
      return false;
    }
    if(!imageUploaded && editStatus==NEWAPP){
        /*we only need to check the image on the publish status*/
        Window.alert(MESSAGES.galleryNoScreenShotMessage());
      return false;
    }
    return true;
  }

  /**
   * Helper method called by constructor to initialize the remove button
   */
  private void initRemoveButton() {
    removeButton = new Button(MESSAGES.galleryRemoveText());
    removeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        //popup confrim dialog
        if(!Window.confirm(MESSAGES.galleryRemoveConfirmText())) {
          return;
        }
        removeButton.setEnabled(false);
        removeButton.setText(MESSAGES.galleryAppRemoving());;
         final OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(
            MESSAGES.galleryDeleteError()) {
            @Override
            public void onSuccess(Void result) {
              // once we have deleted, set the project id back to not published
              final OdeAsyncCallback<Void> projectCallback = new OdeAsyncCallback<Void>(
                  MESSAGES.gallerySetProjectIdError()) {
                @Override
                public void onSuccess(Void result) {
                  // this is called after deleted and after we've set the galleryid
                  Ode.getInstance().getProjectManager().UnpublishProject(app.getProjectId());
                  Ode.getInstance().switchToProjectsView();
                }
                @Override
                public void onFailure(Throwable caught) {
                  super.onFailure(caught);
                  removeButton.setEnabled(true);
                  removeButton.setText(MESSAGES.galleryRemoveText());
                }
              };
              GalleryClient client = GalleryClient.getInstance();
              client.appWasChanged();  // tell views to update
              Ode.getInstance().getProjectService().setGalleryId(app.getProjectId(),
                  UserProject.NOTPUBLISHED, projectCallback);
            }
            @Override
            public void onFailure(Throwable caught) {
              super.onFailure(caught);
              removeButton.setEnabled(true);
              removeButton.setText(MESSAGES.galleryRemoveText());
            }
          };
          Ode.getInstance().getGalleryService().deleteApp(app.getGalleryAppId(),callback);
      }
    });
    removeButton.addStyleName("app-action-button");
    appAction.add(removeButton);
  }

  /**
   * Helper method called by constructor to initialize the cancel button
   */
  private void initCancelButton() {
    cancelButton = new Button(MESSAGES.galleryCancelText());
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (editStatus==NEWAPP) {
          Ode.getInstance().switchToProjectsView();
        }else if(editStatus==UPDATEAPP){
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
        }
      }
    });
    cancelButton.addStyleName("app-action-button");
    appAction.add(cancelButton);
  }

  /**
   * Loads the proper tab GUI with gallery's app data.
   * @param appResults: list of returned gallery apps from callback.
   * @param requestId: determines the specific type of app data.
   */
  private void refreshApps(GalleryAppListResult appResults, int requestId, boolean refreshable) {
    switch (requestId) {
      case GalleryClient.REQUEST_BYDEVELOPER:
        galleryGF.generateSidebar(appResults.getApps(), sidebarTabs, appsByAuthor, MESSAGES.galleryByAuthorText(), MESSAGES.galleryAppsByAuthorSidebar() + MESSAGES.gallerySingleSpaceText() + app.getDeveloperName(), true, true);
        break;
//      case GalleryClient.REQUEST_BYTAG: /* We are not implementing tags at initial launch */
//        String tagTitle = "Tagged with " + tagSelected;
//        galleryGF.generateSidebar(apps, appsByTags, tagTitle, true);
//        break;
    }
  }

  /**
   * When the gallery client gets some apps it fires this callback for
   * gallery page to listen to
   */
  @Override
  public boolean onAppListRequestCompleted(GalleryAppListResult appResults, int requestId, boolean refreshable)   {
   if (appResults != null && appResults.getApps() != null)
      refreshApps(appResults, requestId, refreshable);
    else
      OdeLog.log("apps was null");
   return false;
  }

  /**
   * When the gallery client gets some comments it fires this callback for
   * gallery page to listen to
   */
  @Override
  public boolean onCommentsRequestCompleted(List<GalleryComment> comments) {
      galleryGF.generateAppPageComments(comments, appCommentsList);
      if (comments == null)
        OdeLog.log("comment list was null");
      return false;
  }

  @Override
  public boolean onSourceLoadCompleted(UserProject projectInfo) {
    return false;
  }

  /**
   * Routine to determine if this user can salvage likes on a Gallery App
   * Verifies that they are a Gallery Moderator AND a site Admin.
   *
   * @return boolean true if permitted
   */
  private boolean canSalvage() {
    User currentUser = Ode.getInstance().getUser();
    if ((currentUser.getType() == User.MODERATOR)
      && currentUser.getIsAdmin()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Create a final object of this class to hold a modifiable result value that
   * can be used in a method of an inner class
   */
  private class Result<T> {
    T t;
  }

  /**
   * Creates a new null GalleryPage.
   * This is only used for init in GalleryAppBox.java, do not use this normally
   *
   */
  public GalleryPage() {

  }
}
