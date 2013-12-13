// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.OdeAsyncCallback;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.google.gwt.user.client.ui.Image;

import com.google.appinventor.client.explorer.project.Project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Window;

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
  
  /**
   * Creates a new GalleryPage
   */
  public GalleryPage(final GalleryApp app,Boolean editable) {

    this.app = app;
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

    // App header - image
    appHeader.addStyleName("app-header");
    if (editable) {
      FlowPanel imageUploadBox = new FlowPanel();
      imageUploadBox.addStyleName("app-image-uploadbox");
      imageUploadBox.addStyleName("gallery-editbox");
      Label imageUploadPrompt = new Label("Upload your project image!");
      imageUploadPrompt.addStyleName("app-image-uploadprompt");
      imageUploadPrompt.addStyleName("gallery-editprompt");
      imageUploadBox.add(imageUploadPrompt);
      
   // Create a FileUpload widget.
      FileUpload upload = new FileUpload();
      upload.setName("uploadFormElement");
      imageUploadBox.add(upload);
      
      appHeader.add(imageUploadBox);
    } else {
      Image image = new Image();
      image.setUrl(app.getImageURL());
      image.addStyleName("app-image");
      appHeader.add(image);      
    }

    // App header - action button
    appHeader.add(appAction);

    // SHOULD ONLY SHOW THIS IF WE ARE NOT IN EDIT MODE
    Button actionButton = new Button("Try this app");
    
    actionButton.addClickHandler(new ClickHandler() {
      // Open up source file if clicked the action button
      public void onClick(ClickEvent event) {
        OdeLog.log("######## I clicked on actionButton - ");
        gallery.loadSourceFile(app.getProjectName(),app.getSourceURL());
      }
    });
    actionButton.addStyleName("app-action");
    appAction.add(actionButton);     


    Button publishButton = new Button("Publish");
    publishButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          int STATUS_CODE_OK = 200;  
          // Callback for when the server returns us the apps
          final Ode ode = Ode.getInstance();
          final OdeAsyncCallback<Long> callback = new OdeAsyncCallback<Long>(
             // failure message
             MESSAGES.galleryError()) {
             @Override
             public void onSuccess(Long galleryId) {
               // the server has returned us something
             OdeLog.log("we had a successful publish");
               String s = String.valueOf(galleryId);

               final OdeAsyncCallback<Void> projectCallback = new OdeAsyncCallback<Void>(
               // failure message
               MESSAGES.galleryError()) {
               @Override
               public void onSuccess(Void result) {
        
               }
               };
               ode.getProjectService().setGalleryId(app.getProjectId(),galleryId,projectCallback);
             }  
          
          };
        // ok, this is below the call back, but of course it is done first 
        ode.getGalleryService().publishApp(app.getProjectId(),app.getTitle(), app.getDescription(), callback);
        }
      });

    
    publishButton.addStyleName("app-action");
    appAction.add(publishButton);    


    final Button cloudButton = new Button("Test GCS");
    cloudButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          int STATUS_CODE_OK = 200;  
          // Callback for when the server returns us the apps
          final Ode ode = Ode.getInstance();
          final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
             // failure message
             MESSAGES.galleryError()) {
             @Override
             public void onSuccess(Boolean flag) {  // this is for 
               cloudButton.setText("Completed");
               OdeLog.log("################ SUCCESS");
             }  
          
          };
        // ok, this is below the call back, but of course it is done first 
        ode.getGalleryService().storeAIAtoCloud(1, callback);
        }
      });

    
    cloudButton.addStyleName("app-action");
    appAction.add(cloudButton);       
    
    // App details - header title
    if (editable) {
      // GUI for editable title container
      FlowPanel titleBox = new FlowPanel();
      titleBox.addStyleName("app-titlebox");
      titleBox.addStyleName("gallery-editbox");
      
      // Create an editable text cell to render values
      EditTextCell titlePrompt = new EditTextCell();
      // Create a cell list that uses this cell
      CellList<String> titleCellList = new CellList<String>(titlePrompt);
      
      // Forge the temporary prefilled title
      String t = app.getTitle() + " (ProjectId:" + app.getProjectId() + ")";
      List<String> titleList = Arrays.asList(t);
      titleCellList.setRowData(0, titleList);
      /*
      // EditTextCell.
      Column<ContactInfo, String> editTextColumn =
          addColumn(new EditTextCell(), "EditText", new GetValue<String>() {
            @Override
            public String getValue(ContactInfo contact) {
              return contact.getFirstName();
            }
          }, new FieldUpdater<ContactInfo, String>() {
            @Override
            public void update(int index, ContactInfo object, String value) {
              pendingChanges.add(new FirstNameChange(object, value));
            }
          });
      contactList.setColumnWidth(editTextColumn, 16.0, Unit.EM);
      */
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

    // WHEN PUBLISHING/EDITING THESE NEED DEFAULT VALUES
    // Add app dates
    appInfo.add(appDates);
    Date creationDate = new Date(app.getCreationDate());
    Date updateDate = new Date(app.getUpdateDate());
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd hh:mm:ss a");
    Label creation = new Label("Created on " + dateFormat.format(creationDate));
    Label update = new Label("Updated on " + dateFormat.format(updateDate));
    appDates.add(creation);
    appDates.add(update);
    appDates.addStyleName("app-dates");

    // App details - description
    if (editable) {
      FlowPanel descBox = new FlowPanel();
      descBox.addStyleName("app-descbox");
      descBox.addStyleName("gallery-editbox");
      Label descPrompt1 = new Label("Please describe your project here!");
      descPrompt1.addStyleName("app-descprompt");
      descPrompt1.addStyleName("gallery-editprompt");
      descBox.add(descPrompt1);
      Label descPrompt2 = new Label("Tell us what your project is about in a few sentences.");
      descPrompt2.addStyleName("app-descprompt");
      descPrompt2.addStyleName("gallery-editprompt");
      descBox.add(descPrompt2);
      appInfo.add(descBox);
    } else {
      appInfo.add(appDescription);
      Label description = new Label(app.getDescription());
      appDescription.add(description);
      appDescription.addStyleName("app-description");      
    }

    
    // Add app tags
    if (editable) {
      // Editable tag panel here
    } else {
      FlowPanel appTags = new FlowPanel();
      appInfo.add(appTags);
      for (String tag : app.getTags()) {
        final Label t = new Label(tag);
        appTags.add(t);
        t.addClickHandler(new ClickHandler() {
          // Open up source file if clicked the action button
          public void onClick(ClickEvent event) {
            gallery.FindByTag(t.getText(), 0, 5, 0);
            tagSelected = t.getText();
          }
        });
      }
      appTags.addStyleName("app-tags");      
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

    // Add list of comments
    gallery.GetComments(app.getGalleryAppId(), 0, 100);
    appComments.add(appCommentsList);
    appCommentsList.addStyleName("app-comments");
 
    // Add sidebar stuff, only in public published state
    if (!editable) {
      gallery.GetAppsByDeveloper(0, 5, app.getDeveloperName());      
      // By default, load the first tag's apps
      tagSelected = app.getTags().get(0);
      gallery.FindByTag(app.getTags().get(0), 0, 5, 0);
    }

    // Add everything to top-level containers
    appSingle.add(appDetails);
    appDetails.addStyleName("gallery-container");
    appDetails.addStyleName("gallery-app-details");
    if (!editable) {
      appSingle.add(appsByAuthor);
      appSingle.add(appsByTags);      
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
      case 7: 
        galleryGF.generateSidebar(apps, appsByAuthor, "By this developer", false); 
        break;
      case 8: 
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
 
}	