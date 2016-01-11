// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.Date;
import java.util.List;

import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

public class GalleryGuiFactory implements GalleryRequestListener {
  GalleryClient gallery = null;

  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);

  private final String PERSON_URL = "/images/person.png";
  private final String HOLLOW_HEART_ICON_URL = "/images/numLikeHollow.png";
  private final String RED_HEART_ICON_URL = "/images/numLike.png";
  private final String DOWNLOAD_ICON_URL = "/images/numDownload.png";
  private final String NUM_VIEW_ICON_URL = "/images/numView.png";
  private final String NUM_COMMENT_ICON_URL = "/images/numComment.png";

  /**
   * Generates a new GalleryGuiFactory instance.
   */
  public GalleryGuiFactory() {
    gallery = GalleryClient.getInstance();
    gallery.addListener(this);
  }

  /**
   * Class representing the GUI of a single gallery app.
   */
  private class GalleryAppWidget {
    final Label nameLabel;
    final Label authorLabel;
    final Label numDownloadsLabel;
    final Label numCommentsLabel;
    final Label numViewsLabel;
    final Label numLikesLabel;
    final Image image;

    private GalleryAppWidget(final GalleryApp app) {
      nameLabel = new Label(app.getTitle());
      authorLabel = new Label(app.getDeveloperName());
      numDownloadsLabel = new Label(Integer.toString(app.getDownloads()));
      numLikesLabel = new Label(Integer.toString(app.getLikes()));
      numViewsLabel = new Label(Integer.toString(app.getViews()));
      numCommentsLabel = new Label(Integer.toString(app.getComments()));
      image = new Image();
      image.addErrorHandler(new ErrorHandler() {
        public void onError(ErrorEvent event) {
          image.setUrl(GalleryApp.DEFAULTGALLERYIMAGE);
        }
      });
      String url = gallery.getCloudImageURL(app.getGalleryAppId());
      image.setUrl(url);

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
  }

  /**
   * Loads the proper tab GUI with gallery's app data.
   * @param apps: list of returned gallery apps from callback.
   * @param container: the GUI panel where apps will reside.
   * @param refreshable: if true then the GUI can be reloaded later.
   */
  public void generateHorizontalAppList(List<GalleryApp> apps,
      FlowPanel container, Boolean refreshable) {
    if (refreshable) {
      // Flush the panel's content if we knew new stuff is coming in!
      container.clear();
    }
    for (final GalleryApp app : apps) {
      // Create the associated GUI object for app
      GalleryAppWidget gaw = new GalleryAppWidget(app);

      // Create necessary GUI wrappers and components
      FlowPanel appCard = new FlowPanel();
      FlowPanel appCardContent = new FlowPanel();
      FlowPanel appCardMeta = new FlowPanel();

      // Special processing for the app title, mainly for fade-out effect
      HTML appTitle = new HTML("" +
        "<div class='gallery-title'>" + gaw.nameLabel.getText() +
        "<span class='paragraph-end-block'></span></div>");

      // Special processing for the app author, mainly for fade-out effect
      HTML appAuthor = new HTML("" +
        "<div class='gallery-subtitle'>" + gaw.authorLabel.getText() +
        "<span class='paragraph-end-block'></span></div>");

      gaw.image.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
        }
      });

      appTitle.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
        }
      });

      // Add everything to the top-level stuff
      appCard.add(gaw.image);
      appCard.add(appCardContent);
      appCardContent.add(appTitle);
      appCardContent.add(appAuthor);
      appCardContent.add(appCardMeta);

      // Set helper icons
      Image numViews = new Image();
      numViews.setUrl("/images/numView.png");
      Image numDownloads = new Image();
      numDownloads.setUrl("/images/numDownload.png");
      Image numLikes = new Image();
      numLikes.setUrl("/images/numLikeHollow.png");
    // For generic cards, do not show comment
//    Image numComments = new Image();
//    numComments.setUrl("/image/numComment.png");

//      appCardMeta.add(numViews);
//      appCardMeta.add(gaw.numViewsLabel);
      appCardMeta.add(numDownloads);
      appCardMeta.add(gaw.numDownloadsLabel);
      appCardMeta.add(numLikes);
      appCardMeta.add(gaw.numLikesLabel);
      // For generic cards, do not show comment
//      appCardMeta.add(numComments);
//      appCardMeta.add(gaw.numCommentsLabel);

      // Add associated styling
      appCard.addStyleName("gallery-card");
      gaw.image.addStyleName("gallery-card-cover");
//      gaw.nameLabel.addStyleName("gallery-title");
//      gaw.authorLabel.addStyleName("gallery-subtitle");
      appCardContent.addStyleName("gallery-card-content");
      gaw.numViewsLabel.addStyleName("gallery-meta");
      gaw.numDownloadsLabel.addStyleName("gallery-meta");
      gaw.numLikesLabel.addStyleName("gallery-meta");
//      gaw.numCommentsLabel.addStyleName("gallery-meta");

      container.add(appCard);
    }
    container.addStyleName("gallery-app-collection");
    container.addStyleName("clearfix"); /* For redesigned navigation buttons */

  }

  /**
   * Creates list of comments in the app page.
   * @param comments: list of returned gallery comments from callback.
   * @param container: the GUI panel where comments will reside.
   */
  public void generateAppPageComments(List<GalleryComment> comments, FlowPanel container) {
    container.clear();  // so don't show previous listing
    if (comments == null) {
      Label noComments = new Label("This app does not have any comments yet.");
      noComments.addStyleName("comment-nope");
      container.add(noComments);
    }

    for ( GalleryComment c : comments) {
      FlowPanel commentItem = new FlowPanel();
      FlowPanel commentPerson = new FlowPanel();
      FlowPanel commentMeta = new FlowPanel();
      FlowPanel commentContent = new FlowPanel();

      // Add commentPerson, default avatar for now
      Image cPerson = new Image();
      cPerson.setUrl(PERSON_URL);
      commentPerson.add(cPerson);
      commentPerson.addStyleName("comment-person");
      commentItem.add(commentPerson);

      // Add commentContent
      Label cAuthor = new Label(c.getUserName());
      cAuthor.addStyleName("comment-author");
      commentMeta.add(cAuthor);

      Date commentDate = new Date(c.getTimeStamp());
      DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd hh:mm:ss a");
      Label cDate = new Label(" on " + dateFormat.format(commentDate));
      cDate.addStyleName("comment-date");
      commentMeta.add(cDate);

      commentMeta.addStyleName("comment-meta");
      commentContent.add(commentMeta);

      Label cText = new Label(c.getComment());
      cText.addStyleName("comment-text");
      commentContent.add(cText);

      commentContent.addStyleName("comment-content");
      commentItem.add(commentContent);

      commentItem.addStyleName("comment-item");
      commentItem.addStyleName("clearfix");
      container.add(commentItem);
    }
  }

  /**
   * Creates a sidebar showcasing apps; the CSS name will be the same as the
   * passed-in container's name. This sidebar shows up as a tab under parent.
   *
   * @param apps: list of returned gallery apps from callback.
   *
   * @param parent: the parent TabPanel that this panel will reside in.
   *
   * @param container: the panel containing this particular sidebar.
   *
   * @param name: the name or title of this particular sidebar.
   *
   * @param desc: the short description of this particular sidebar.
   *
   * @param refreshable: if true then this sidebar can be reloaded later.
   *
   * @param isDefault: if true then this sidebar is the default tab showing.
   */
  public void generateSidebar(List<GalleryApp> apps, TabPanel parent,
      FlowPanel container, String name, String desc,
      Boolean refreshable, Boolean isDefault) {
    if (refreshable) {
      // Flush the panel's content if we knew new stuff is coming in!
      container.clear();
    }
    parent.add(container, name);
    if (isDefault) {
      parent.selectTab(0);  //TODO: fix order
    }

    Label descLabel = new Label(desc);
    descLabel.addStyleName("gallery-showcase-desc");
    container.add(descLabel);

    for (final GalleryApp app : apps) {
      // Create the associated GUI object for app
      GalleryAppWidget gaw = new GalleryAppWidget(app);

      // Create necessary GUI wrappers and components
      FlowPanel appCard = new FlowPanel();
      FlowPanel appCardContent = new FlowPanel();
      FlowPanel appCardMeta = new FlowPanel();

      // Special processing for the app title, mainly for fade-out effect
      HTML appTitle = new HTML("" +
        "<div class='gallery-title'>" + gaw.nameLabel.getText() +
        "<span class='paragraph-end-block'></span></div>");

      // Special processing for the app author, mainly for fade-out effect
      HTML appAuthor = new HTML("" +
        "<div class='gallery-subtitle'>" + gaw.authorLabel.getText() +
        "<span class='paragraph-end-block'></span></div>");

      gaw.image.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
        }
      });

      appTitle.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
        }
      });

      // Add everything to the top-level stuff
      appCard.add(gaw.image);
      appCard.add(appCardContent);
      appCardContent.add(appTitle);
      appCardContent.add(appAuthor);
      appCardContent.add(appCardMeta);

      // Set helper icons
//      Image numViews = new Image();
//      numViews.setUrl(NUM_VIEW_ICON_URL);
      Image numDownloads = new Image();
      numDownloads.setUrl(DOWNLOAD_ICON_URL);
      Image numLikes = new Image();
      numLikes.setUrl(HOLLOW_HEART_ICON_URL);
    // For generic cards, do not show comment
//    Image numComments = new Image();
//    numComments.setUrl(NUM_COMMENT_ICON_URL);

//      appCardMeta.add(numViews);
//      appCardMeta.add(gaw.numViewsLabel);
      appCardMeta.add(numDownloads);
      appCardMeta.add(gaw.numDownloadsLabel);
      appCardMeta.add(numLikes);
      appCardMeta.add(gaw.numLikesLabel);
      // For generic cards, do not show comment
//      appCardMeta.add(numComments);
//      appCardMeta.add(gaw.numCommentsLabel);

      // Add associated styling
      appCard.addStyleName("gallery-card");
      appCard.addStyleName("clearfix");
      gaw.image.addStyleName("gallery-card-cover");
//      gaw.nameLabel.addStyleName("gallery-title");
//      gaw.authorLabel.addStyleName("gallery-subtitle");
      appCardContent.addStyleName("gallery-card-content");
      gaw.numViewsLabel.addStyleName("gallery-meta");
      gaw.numDownloadsLabel.addStyleName("gallery-meta");
      gaw.numLikesLabel.addStyleName("gallery-meta");
//      gaw.numCommentsLabel.addStyleName("gallery-meta");

      container.add(appCard);
    }

  }

  @Override
  public void onAppListRequestCompleted(GalleryAppListResult appsResult, int requestID, boolean refreshable) {
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
