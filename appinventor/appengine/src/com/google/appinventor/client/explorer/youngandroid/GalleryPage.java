// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.OdeAsyncCallback;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Window;

import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

/**
 * The gallery page shows a single app from the gallery 
 *
 * It has different modes for public viewing or when user is publishing for first time
 * or updating a previously published app
 *
 * @author wolberd@gmail.com (Dave Wolber)
 * @author vincentaths@gmail.com(Vincent Zhang)
 */
public class GalleryPage extends Composite implements GalleryRequestListener {


  GalleryClient gallery = null;
  GalleryGuiFactory galleryGF = new GalleryGuiFactory();
  GalleryApp app = null;
  String projectName = null;
  Project project;

  private static final String TRYITBUTTONTEXT="Open the App";
  private static final String PUBLISHBUTTONTEXT="Publish";
  private static final String UPDATEBUTTONTEXT="Update";
  private static final String REMOVEBUTTONTEXT="Remove";
  private boolean imageUploaded = false;

  private VerticalPanel panel;  // the main panel
  private FlowPanel galleryGUI;
  private FlowPanel appSingle;
  private FlowPanel appsByAuthor;
  private FlowPanel appsByTags;
  private FlowPanel appDetails;
  private FlowPanel appHeader;
  private FlowPanel appInfo;
  private FlowPanel appAction;
  private FlowPanel appMeta;
  private FlowPanel appDates;
  private FlowPanel appDescription;
  private FlowPanel appComments;
  private FlowPanel appCommentsList;
  private String tagSelected;

  public static final int VIEWAPP = 0;
  public static final int NEWAPP = 1;
  public static final int UPDATEAPP = 2;  
  private int editStatus;

  /* Publish & edit state components */
  private FlowPanel imageUploadBox;
  private Label imageUploadPrompt;
  private Image image;
  private FileUpload upload;
  private FlowPanel imageUploadBoxInner;
  private FocusPanel wrapper;
  private Label creation;
  private Label update;
  private CellList<String> titleCellList;
  private CellList<String> descCellList;
  private TextArea titleText;
  private TextArea desc;
  private FlowPanel descBox;
  private FlowPanel titleBox;
  private Label likeLabel;
  private Image likeButton;

  private Button actionButton;
  private Button publishButton;
  private Button removeButton;

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
   * Creates a new GalleryPage
   *
   */
  public GalleryPage(final GalleryApp app, final int editStatus) {
  
   
    // get a reference to the Gallery Client which handles the communication to
    //   server to get gallery data
    gallery = GalleryClient.getInstance();
    gallery.addListener(this);
    // We are either publishing a new app, updating, or just reading. If we are publishing
    //   a new app, app has some partial info to be published. Otherwise, it has all
    //   the info for the already published app
    this.app = app;
    this.editStatus = editStatus;
    initComponents();
    
    // If we're editing or updating, add input form for image
    if (newOrUpdateApp()) {
      initEditComponents();
    } else  { // we are just viewing this page so setup the image
      initReadOnlyImage();   
    }

    // Now let's add the button for publishing, updating, or trying
    appHeader.add(appAction);
    initActionButton();
    if (editStatus==UPDATEAPP) {
      initRemoveButton();
    }
    // App details - header title
    if (newOrUpdateApp()) {
      // GUI for editable title container
      titleText.setText(app.getTitle());
      titleText.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          app.setTitle(titleText.getText());   
          
        }

      });
      
      titleText.addStyleName("app-desc-textarea");
      titleBox.add(titleText);
      
      appInfo.add(titleBox);
      
    } else {
      Label title = new Label(app.getTitle());
      appInfo.add(title);
      title.addStyleName("app-title");   
    }
    
   
    // if we are publishing no app name  
    if (editStatus!=NEWAPP) {
      Label devName = new Label("By " + app.getDeveloperName());
      appInfo.add(devName);
      devName.addStyleName("app-subtitle");
    
      // App details - meta
      appInfo.add(appMeta);
      appMeta.addStyleName("app-meta");
    
      // Images for meta data
      Image numViews = new Image();
      numViews.setUrl("http://i.imgur.com/jyTeyCJ.png");
      Image numDownloads = new Image();
      numDownloads.setUrl("http://i.imgur.com/j6IPJX0.png");
      Image numLikes = new Image();
      numLikes.setUrl("http://i.imgur.com/N6Lpeo2.png");
      Image numComments = new Image();
      numComments.setUrl("http://i.imgur.com/GGt7H4c.png");
    
      // Add meta data
      appMeta.add(numViews);
      appMeta.add(new Label(Integer.toString(app.getViews())));
      appMeta.add(numDownloads);
      appMeta.add(new Label(Integer.toString(app.getDownloads())));
      appMeta.add(numLikes);
      appMeta.add(new Label(Integer.toString(app.getLikes())));
      appMeta.add(numComments);
      appMeta.add(new Label(Integer.toString(app.getComments())));

      // Add app dates
      appInfo.add(appDates);
      updateAppDates();
    
      appDates.add(creation);
      appDates.add(update);
      appDates.addStyleName("app-dates");
    }

     // App description
    if (newOrUpdateApp()) {
      
      desc.setText(app.getDescription());
      desc.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          app.setDescription(desc.getText());   
        }

      });
      desc.addStyleName("app-desc-textarea");
      descBox.add(desc);
      
      appInfo.add(descBox);
    } else {
      appInfo.add(appDescription);
      Label description = new Label(app.getDescription());
      appDescription.add(description);
      appDescription.addStyleName("app-description");    
    }
    
    appInfo.addStyleName("app-info-container");

    FlowPanel appClear = new FlowPanel();
    appClear.addStyleName("clearfix");
    appClear.add(appHeader);
    appClear.add(appInfo);
    appDetails.add(appClear);
    
    HTML divider = new HTML("<div class='section-divider'></div>");
    appDetails.add(divider);
    
    // App details - comments
    appDetails.add(appComments);
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
 
    // Add sidebar stuff, only in public state
    if (!newOrUpdateApp()) {
      
      gallery.GetAppsByDeveloper(0, 5, app.getDeveloperId());      
      // By default, load the first tag's apps
    
    }

    // Add everything to top-level containers
    appSingle.add(appDetails);
    appDetails.addStyleName("gallery-container");
    appDetails.addStyleName("gallery-app-details");
    if (!newOrUpdateApp()) {
    
      appSingle.add(appsByAuthor);
    //  appSingle.add(appsByTags);   
       
    }
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
    appMeta = new FlowPanel();
    appDates = new FlowPanel();
    appDescription = new FlowPanel();
    appComments = new FlowPanel();
    appCommentsList = new FlowPanel();
    appsByAuthor = new FlowPanel();
    appsByTags = new FlowPanel();
    tagSelected = "";
    
    creation = new Label();
    update = new Label();
    descBox = new FlowPanel();
    titleBox = new FlowPanel();
    desc = new TextArea();
    titleText = new TextArea();

    // App header - image
    appHeader.addStyleName("app-header");
  }


  /**
   * Helper method called by constructor to initialize editing components
   */
  private void initEditComponents() {
    imageUploadBox = new FlowPanel();
    imageUploadBox.addStyleName("app-image-uploadbox");
    imageUploadBox.addStyleName("gallery-editbox");
    imageUploadBoxInner = new FlowPanel();
    imageUploadPrompt = new Label("Upload your project image!");
    imageUploadPrompt.addStyleName("gallery-editprompt");
    
    updateAppImage(app.getCloudImageURL(), imageUploadBoxInner);  
    image.addStyleName("status-updating");
    imageUploadPrompt.addStyleName("app-image-uploadprompt"); 
    imageUploadBoxInner.add(imageUploadPrompt);        

    upload = new FileUpload();
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
    updateAppImage(app.getCloudImageURL(), appHeader);   
  }
  private void initActionButton () {
    if (editStatus==NEWAPP)
      initPublishButton();
    else 
    if (editStatus == UPDATEAPP)
      initUpdateButton();
    else
      initTryitButton(); 
      initLikeitButton();
  }
  /**
   * Helper method called by constructor to initialize the like area
   */
  private void initLikeitButton() {
    final Image likeButton = new Image();
    likeButton.setUrl("http://i.imgur.com/jyTeyCJ.png");
    likeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Integer> changeLikeCallback = new OdeAsyncCallback<Integer>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Integer num) {
                 //TODO deal with/discuss server data sync later; now is updating locally.
              }
          };
        final OdeAsyncCallback<Boolean> isLikedByUserCallback = new OdeAsyncCallback<Boolean>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Boolean bool) {
                if(bool){//already liked
                  Ode.getInstance().getGalleryService().decreaseLikes(app.getGalleryAppId(),
                      changeLikeCallback);
                  likeLabel.setText(String.valueOf(Integer.valueOf(likeLabel.getText()) - 1));
                  likeButton.setUrl("http://i.imgur.com/N6Lpeo2.png");//unliked
                }else{
                  Ode.getInstance().getGalleryService().increaseLikes(app.getGalleryAppId(),
                      changeLikeCallback);
                  likeLabel.setText(String.valueOf(Integer.valueOf(likeLabel.getText()) + 1));
                  likeButton.setUrl("http://i.imgur.com/HjRfYkk.png");//liked
                }
              }
          };
        Ode.getInstance().getGalleryService().isLikedByUser(app.getGalleryAppId(),
            isLikedByUserCallback);
      }
    });
    appAction.add(likeButton);
    likeLabel = new Label("");
    appAction.add(likeLabel);
    final OdeAsyncCallback<Integer> likeNumCallback = new OdeAsyncCallback<Integer>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Integer num) {
            likeLabel.setText(String.valueOf(num));
          }
      };
    Ode.getInstance().getGalleryService().getNumLikes(app.getGalleryAppId(),
        likeNumCallback);

    final OdeAsyncCallback<Boolean> isLikedCallback = new OdeAsyncCallback<Boolean>(
        // failure message
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean bool) {
            if(!bool){
              likeButton.setUrl("http://i.imgur.com/N6Lpeo2.png");//unliked
            }else{
              likeButton.setUrl("http://i.imgur.com/HjRfYkk.png");//liked
            }
          }
      };
    Ode.getInstance().getGalleryService().isLikedByUser(app.getGalleryAppId(),
        isLikedCallback); 
  }
  /**
   * Helper method called by constructor to initialize the try it button
   */
  private void initTryitButton() {
    actionButton = new Button(TRYITBUTTONTEXT);    
    actionButton.addClickHandler(new ClickHandler() {
      // Open up source file if clicked the action button
      public void onClick(ClickEvent event) {
        //gallery.loadSourceFile(app.getProjectName(),app.getSourceURL());
        gallery.loadSourceFile(app);
      }
    });
    actionButton.addStyleName("app-action");
    appAction.add(actionButton);
  }
  /**
   * Helper method called by constructor to initialize the publish button
   */
  private void initPublishButton() {
    actionButton = new Button(PUBLISHBUTTONTEXT);    
    actionButton.addClickHandler(new ClickHandler() {
      // Open up source file if clicked the action button
      public void onClick(ClickEvent event) {
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
                  // this is called after published and after we've set the galleryid
                  // tell the project list to change project's button to "Update"
                  Ode.getInstance().getProjectManager().publishProject(app.getProjectId(),
                      gApp.getGalleryAppId());
                  Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
                }
              };
            
              Ode.getInstance().getProjectService().setGalleryId(gApp.getProjectId(), 
                  gApp.getGalleryAppId(), projectCallback);
              // we need to update the app object for this gallery page
              gallery.appWasChanged();
             
            }
            
          };
          // call publish with the default app data...
          Ode.getInstance().getGalleryService().publishApp(app.getProjectId(), 
              app.getTitle(), app.getProjectName(), app.getDescription(), callback);
        
      }
    });
    actionButton.addStyleName("app-action");
    appAction.add(actionButton);  
  }
  /**
   * Helper method called by constructor to initialize the publish button
   */
  private void initUpdateButton() {
    actionButton = new Button(UPDATEBUTTONTEXT);    
    actionButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
         final OdeAsyncCallback<Void> updateSourceCallback = new OdeAsyncCallback<Void>(
            MESSAGES.galleryError()) {
            @Override 
            public void onSuccess(Void result) {
              gallery.appWasChanged();  // to update the gallery list and page
              Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
            }
          };
          
          Ode.getInstance().getGalleryService().updateApp(app,imageUploaded,updateSourceCallback);
          
      }
    });
    actionButton.addStyleName("app-action");
    appAction.add(actionButton);  
  }
  /**
   * Helper method called by constructor to initialize the remove button
   */
  private void initRemoveButton() {
    removeButton = new Button(REMOVEBUTTONTEXT);    
    removeButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
         // need a are you sure dialog
         final OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(
            MESSAGES.galleryDeleteError()) {
            @Override 
            public void onSuccess(Void result) {
              // once we have deleted, set the project id back to -1
              final OdeAsyncCallback<Void> projectCallback = new OdeAsyncCallback<Void>(
                  MESSAGES.gallerySetProjectIdError()) {
                @Override 
                public void onSuccess(Void result) {
                  // this is called after deleted and after we've set the galleryid
                  Ode.getInstance().getProjectManager().UnpublishProject(app.getProjectId());
                  Ode.getInstance().switchToProjectsView();
                }
              };
              GalleryClient client = GalleryClient.getInstance();
              client.appWasChanged();  // tell views to update
              Ode.getInstance().getProjectService().setGalleryId(app.getProjectId(), 
                  -1, projectCallback);
            }
          };
          
          Ode.getInstance().getGalleryService().deleteApp(app.getGalleryAppId(),callback);
          
      }
    });
    removeButton.addStyleName("app-action");
    appAction.add(removeButton);  
  }

  /**
   * Loads the proper tab GUI with gallery's app data.
   *
   * @param apps: list of returned gallery apps from callback.
   * 
   * @param requestId: determines the specific type of app data.
   */
  private void refreshApps(List<GalleryApp> apps, int requestId) {
    switch (requestId) {
      case GalleryClient.REQUEST_BYDEVELOPER: 
        galleryGF.generateSidebar(apps, appsByAuthor, "By this developer", false); 
        break;
      case GalleryClient.REQUEST_BYTAG: 
        String tagTitle = "Tagged with " + tagSelected;
        galleryGF.generateSidebar(apps, appsByTags, tagTitle, true); 
        break;
    } 
  }
  /**
   * when the gallery client gets some apps it produces this callback for
   * gallery page to listen to
   */
  @Override
  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestId)   {
   if (apps != null)
      refreshApps(apps, requestId);
    else
      Window.alert("apps was null");

  }
  
  /**
   * When the gallery client gets some comments it fires this callback for
   * gallery page to listen to
   */
  @Override
  public void onCommentsRequestCompleted(List<GalleryComment> comments) {
      galleryGF.generateAppPageComments(comments, appCommentsList);
      if (comments == null) 
        Window.alert("comment list was null");
  }
  
  @Override
  public void onSourceLoadCompleted(UserProject projectInfo) {
  }
  
  /*
   * Helper method providing easier way to grab value from GWT's CellList,
   * it also sanitizes the input in the process.
   * NOTE: not currently being used, title can be anything, but need to 
   * figure out how projectName and title relate to each other
   */
  private String sanitizeEditedValue(CellList l) {
    String text = l.getRowElement(0).getString();
    int greaterThanCount = text.length() - text.replace(">", "").length();
    int lessThanCount = text.length() - text.replace("<", "").length();
    if (text.length() < 1) {
      OdeLog.log("Sorry, your input is too short");
    } else if (lessThanCount > 2 | greaterThanCount > 2) {
      OdeLog.log("Sorry, contains illegal characters");
    } else {
      // Valid state, grab text value
      text = text.substring(text.indexOf('>') + 1, text.indexOf('<', 2));
      // OdeLog.log(text);
    }
    return text;
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

  private boolean newOrUpdateApp() {
    if ((editStatus==NEWAPP) || (editStatus==UPDATEAPP))
      return true;
    else
      return false;
  }

  private void uploadImage () {
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
              updateAppImage(app.getProjectImageURL(),imageUploadBoxInner); 
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
   * Helper method to update the app image
   * @param url  The URL of the image to show
   * @param container  The container that image widget resides
   */
  private void updateAppImage(String url, Panel container) {
    image = new Image();
    image.setUrl(url);
    image.addStyleName("app-image");
    // if the user has provided a gallery app image, we'll load it. But if not
    // the error will occur and we'll load default image
    image.addErrorHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        image.setUrl(GalleryApp.DEFAULTGALLERYIMAGE);
      }
    });
    container.add(image);   
  }
  
  private void updateAppDates() {
    Date creationDate = new Date(app.getCreationDate());
    Date updateDate = new Date(app.getUpdateDate());
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd hh:mm:ss a");
    creation.setText("Created on " + dateFormat.format(creationDate));
    update.setText("Updated on " + dateFormat.format(updateDate));
  }
 
   /* this is admin code that was used to temporarily to get all old apps indexed ...
    final OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Void x) {
                
              }
          };
    Ode.getInstance().getGalleryService().indexAll(100,callback);
    ** end temporary code */
 
 
 
}