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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

/**
 * The gallery list shows apps from the gallery in a table.
 *
 * <p> The project name and date created will be shown in the table.
 *
 * @author wolberd@google.com (Dave Wolber)
 */
public class GalleryPage extends Composite implements GalleryRequestListener {
  
  GalleryClient gallery = new GalleryClient(this);
  GalleryGuiFactory galleryGF = new GalleryGuiFactory();
  GalleryApp app = null;
  String projectName = null;
  Project project;

  private final FlowPanel galleryGUI;
  private final FlowPanel appSingle;
  private final FlowPanel appsByAuthor;
  private final FlowPanel appsByTags;
  private final FlowPanel appDetails;
  private final FlowPanel appHeader;
  private final FlowPanel appInfo;
  private final FlowPanel appAction;
  private final FlowPanel appMeta;
  private final FlowPanel appDates;
  private final FlowPanel appDescription;
  private final FlowPanel appComments;
  private final FlowPanel appCommentsList;
  private String tagSelected;

  public static final int VIEWAPP = 0;
  public static final int NEWAPP = 1;
  public static final int UPDATEAPP = 2;  
  private int editStatus;

  /* Publish & edit state components */
  private Image image;
  private FileUpload upload;
  private FlowPanel imageUploadBoxInner;
  private Label creation;
  private Label update;
  private CellList<String> titleCellList;
  private CellList<String> descCellList;
  
  

  public GalleryPage() {
    // Initialize UI
    VerticalPanel panel = new VerticalPanel();
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
  }
  
  /**
   * Creates a new GalleryPage
   */
  public GalleryPage(final GalleryApp app, final int editStatus) {

    this.app = app;
    this.editStatus = editStatus;

    // Initialize UI
    VerticalPanel panel = new VerticalPanel();
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

    // App header - image
    appHeader.addStyleName("app-header");
    
    // If we're editing, add input form for image
    if (newOrUpdateApp()) {
      FlowPanel imageUploadBox = new FlowPanel();
      imageUploadBox.addStyleName("app-image-uploadbox");
      imageUploadBox.addStyleName("gallery-editbox");
      imageUploadBoxInner = new FlowPanel();
      
      if (editStatus == UPDATEAPP) {
        updateAppImage(app.getCloudImageURL(), imageUploadBoxInner);  
      } else {
        Label imageUploadPrompt = new Label("Upload your project image!");
        imageUploadPrompt.addStyleName("app-image-uploadprompt");
        imageUploadPrompt.addStyleName("gallery-editprompt");
        imageUploadBoxInner.add(imageUploadPrompt);        
      }
      upload = new FileUpload();
      upload.addStyleName("app-image-upload");
      // Set the correct handler for servlet side capture
      upload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
      imageUploadBoxInner.add(upload);
      
      imageUploadBox.add(imageUploadBoxInner);
      
      FocusPanel wrapper = new FocusPanel();
      wrapper.add(imageUploadBox);
      wrapper.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          // The correct way to trigger click event on FileUpload
          upload.getElement().<InputElement>cast().click(); 
        }
      });
      appHeader.add(wrapper);
      
    } else  { // we are just viewing this page 
      image = new Image();
      // Vincent note: some strange cache issue preventing newest from showing up
      image.setUrl(app.getCloudImageURL());
      image.addStyleName("app-image");
      appHeader.add(image);      
    }

    // App header - action button
    appHeader.add(appAction);

    if (!newOrUpdateApp()) {
      Button openAppButton = new Button("Try this app");    
      openAppButton.addClickHandler(new ClickHandler() {
        // Open up source file if clicked the action button
        public void onClick(ClickEvent event) {
          //gallery.loadSourceFile(app.getProjectName(),app.getSourceURL());
          gallery.loadSourceFile(app);
        }
      });
      openAppButton.addStyleName("app-action");
      appAction.add(openAppButton);
    } else {
    
      Button publishButton = null;
      if (editStatus == NEWAPP) {
        publishButton = new Button("Publish");
      } else {
        publishButton = new Button("Update");
      }

      publishButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
              
          // Callback for when the server returns us the apps
          final Ode ode = Ode.getInstance();
          final OdeAsyncCallback<Long> callback = new OdeAsyncCallback<Long>(
            // failure message
            MESSAGES.galleryError()) {
            @Override
            // 2. When publish call returns
            public void onSuccess(Long galleryId) {
              // the server has returned us something
              updateAppDates();

              if (editStatus == NEWAPP) {
                final OdeAsyncCallback<Void> projectCallback = new OdeAsyncCallback<Void>(
                  // failure message
                  MESSAGES.galleryError()) {
                    @Override
                    //4. When setGalleryId call returns, which we don't need to do anything
                    public void onSuccess(Void result) {
                    }
                };
                // 3. Set galleryId of the project once it's published
                ode.getProjectService().setGalleryId(app.getProjectId(), 
                    galleryId, projectCallback);
                app.setGalleryAppId(galleryId);
              } 
              
              // 4. Process the app image upload
              String uploadFilename = upload.getFilename();
              if (!uploadFilename.isEmpty()) {
                // Grab and validify the filename
                final String filename = makeValidFilename(uploadFilename);

                // Forge the request URL for gallery servlet
                String uploadUrl = GWT.getModuleBaseURL() + 
                    ServerLayout.GALLERY_SERVLET + "/" + String.valueOf(app.getGalleryAppId()) + "/"
                    + filename;
                Uploader.getInstance().upload(upload, uploadUrl,
                    new OdeAsyncCallback<UploadResponse>(MESSAGES.fileUploadError()) {
                  @Override
                  public void onSuccess(UploadResponse uploadResponse) {
                    switch (uploadResponse.getStatus()) {
                    case SUCCESS:
                      // Update the app image preview after a success upload
                      imageUploadBoxInner.clear();
                      updateAppImage(app.getCloudImageURL(), imageUploadBoxInner);  
                      ErrorReporter.hide();
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
                
              } else {
                Window.alert(MESSAGES.noFileSelected());
              }
            }
          };
        // Prepare the title and description from user inputs
        app.setTitle(sanitizeEditedValue(titleCellList));
        app.setDescription(sanitizeEditedValue(descCellList));

        // ok, this is below the call back, but of course it is done first 
        if (editStatus == NEWAPP) {
          ode.getGalleryService().publishApp(app.getProjectId(), 
              app.getTitle(), app.getProjectName(), app.getDescription(), 
              callback);
        } else {
          ode.getGalleryService().updateApp(app.getGalleryAppId(), app.getProjectId(), 
              app.getTitle(), app.getProjectName(), app.getDescription(), 
              callback);
        }
        }
      });    
      publishButton.addStyleName("app-action");
      appAction.add(publishButton);    
    }
    
    // App details - header title
    if (newOrUpdateApp()) {
      // GUI for editable title container
      FlowPanel titleBox = new FlowPanel();
      titleBox.addStyleName("app-titlebox");
      titleBox.addStyleName("gallery-editbox");
      
      // Create an editable text cell to render values
      EditTextCell titlePrompt = new EditTextCell();
      // Create a cell list that uses this cell
      titleCellList = new CellList<String>(titlePrompt);
      
      // Forge the temporary prefilled title, place it in cell list
      String t = app.getTitle();
      List<String> titleList = Arrays.asList(t);
      titleCellList.setRowData(0, titleList);
      titleCellList.addStyleName("app-titleprompt");
      titleCellList.addStyleName("gallery-editprompt");
      titleBox.add(titleCellList);
      appInfo.add(titleBox);
      // Event handler for editing
    } else {
      Label title = new Label(app.getTitle());
      appInfo.add(title);
      title.addStyleName("app-title");   
    }
    
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
    if (editStatus == NEWAPP) {
      creation.setText("This app is not created in the Gallery yet.");
      update.setText("This app is not updated in the Gallery yet.");
    } else {
      updateAppDates();
    }
    appDates.add(creation);
    appDates.add(update);
    appDates.addStyleName("app-dates");

    // App details - description
    if (newOrUpdateApp()) {
      FlowPanel descBox = new FlowPanel();
      descBox.addStyleName("app-descbox");
      descBox.addStyleName("gallery-editbox");

      // Create an editable text cell to render values
      EditTextCell descPrompt = new EditTextCell();
      // Create a cell list that uses this cell
      descCellList = new CellList<String>(descPrompt);
      // Forge the temporary prefilled description, place it in cell list
      String t = "Please describe your project here! \r\r " +
      		"Tell us what your project is about in a few sentences.";
      if (app.getDescription().length() > 1) {
        t = app.getDescription();
      } else {
        t = "Please describe your project here! \r\r Tell us what your project is about in a few sentences.";
      }
      List<String> descList = Arrays.asList(t);
      descCellList.setRowData(0, descList);
      
      descCellList.addStyleName("app-descprompt");
      descCellList.addStyleName("gallery-editprompt");
      descBox.add(descCellList);
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
                OdeLog.log("###### PUBLISHED COMMENT #########");
              }
          };
        Ode.getInstance().getGalleryService().publishComment(app.getGalleryAppId(), 
            commentTextArea.getText(), commentPublishCallback);
        
      }
    });
    appComments.add(commentSubmit);
    

    // Add list of comments
    gallery.GetComments(String.valueOf(app.getGalleryAppId()), 0, 100);
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
  
  @Override
  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestId)   {
    if (apps != null)
      refreshApps(apps, requestId);
    else
      Window.alert("apps was null");
  }
  
  
  @Override
  public void onCommentsRequestCompleted(List<GalleryComment> comments) {
      galleryGF.generateAppPageComments(comments, appCommentsList);
      if (comments == null) 
        Window.alert("comment list was null");    	
  }
  
  @Override
  public void onSourceLoadCompleted(UserProject projectInfo) {
      final NewProjectCommand onSuccessCommand = new NewProjectCommand() {
          @Override
          public void execute(Project project) {
               Ode.getInstance().openYoungAndroidProjectInDesigner(project);
          }
       };
       // Update project explorer -- i.e., display in project view
       final Ode ode = Ode.getInstance();
       if (projectInfo == null) {
         Window.alert("Unable to create project from Gallery source"); 
       }
       else {
         Project project = ode.getProjectManager().addProject(projectInfo);
         if (onSuccessCommand != null) {
           onSuccessCommand.execute(project);
         }
       }
  	
  }
  
  /*
   * Helper method providing easier way to grab value from GWT's CellList,
   * it also sanitizes the input in the process.
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
  
  /**
   * Helper method to update the app image
   * @param url  The URL of the image to show
   * @param container  The container that image widget resides
   */
  private void updateAppImage(String url, Panel container) {
    image = new Image();
    image.setUrl(url);
    image.addStyleName("app-image");
    container.add(image);   
  }
  
  private void updateAppDates() {
    Date creationDate = new Date(app.getCreationDate());
    Date updateDate = new Date(app.getUpdateDate());
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd hh:mm:ss a");
    creation.setText("Created on " + dateFormat.format(creationDate));
    update.setText("Updated on " + dateFormat.format(updateDate));
  }
 
}	