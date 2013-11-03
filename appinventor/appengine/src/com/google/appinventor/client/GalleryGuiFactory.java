package com.google.appinventor.client;

import java.util.Date;
import java.util.List;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class GalleryGuiFactory implements GalleryRequestListener {
	GalleryClient gallery = null;

	/*
	 * Generates a new GalleryGuiFactory instance.
	 */
	public GalleryGuiFactory() {
		gallery = new GalleryClient(this);
	}

	/*
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
      image.setUrl(app.getImageURL());
      
    }
  }
  
  /**
   * Loads the proper tab GUI with gallery's app data.
   *
   * @param apps: list of returned gallery apps from callback.
   *
   * @param request: type of app request, for pagination.
   * 
   * @param container: the GUI panel where apps will reside.
   * 
   * @param refreshable: if true then the GUI can be reloaded later.
   */
	public void generateHorizontalAppList(List<GalleryApp> apps, 
	    final int request, FlowPanel container, Boolean refreshable) {
    if (refreshable) {
      // Flush the panel's content if we knew new stuff is coming in!
      container.clear();
    }
    /*
    Label pagePrev = new Label("Prev");
    container.add(pagePrev);
    */
		for (final GalleryApp app : apps) {
		  // Create the associated GUI object for app
		  GalleryAppWidget gaw = new GalleryAppWidget(app);
		  
		  // Create necessary GUI wrappers and components
      FlowPanel appCard = new FlowPanel();
      FlowPanel appCardContent = new FlowPanel();
      FlowPanel appCardMeta = new FlowPanel();
      
      // Special processing for app title, mainly for fade-out effect
      HTML appTitle = new HTML("" +
        "<div class='gallery-title'>" + gaw.nameLabel.getText() +
        "<span class='paragraph-end-block'></span></div>");

      gaw.image.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app); 
        }
      });
      
      appTitle.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app); 
        }
      });

      // Add everything to the top-level stuff
      appCard.add(gaw.image);
      appCard.add(appCardContent);
      appCardContent.add(appTitle);
      appCardContent.add(gaw.authorLabel);
      appCardContent.add(appCardMeta);

      // Set helper icons
      Image numViews = new Image();
      numViews.setUrl("http://i.imgur.com/jyTeyCJ.png");
      Image numDownloads = new Image();
      numDownloads.setUrl("http://i.imgur.com/j6IPJX0.png");
      Image numLikes = new Image();
      numLikes.setUrl("http://i.imgur.com/N6Lpeo2.png");
    // For generic cards, do not show comment
//    Image numComments = new Image();
//    numComments.setUrl("http://i.imgur.com/GGt7H4c.png");
      
      appCardMeta.add(numViews);
      appCardMeta.add(gaw.numViewsLabel);
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
      gaw.authorLabel.addStyleName("gallery-subtitle");
      appCardContent.addStyleName("gallery-card-content");
      gaw.numViewsLabel.addStyleName("gallery-meta");
      gaw.numDownloadsLabel.addStyleName("gallery-meta");
      gaw.numLikesLabel.addStyleName("gallery-meta");
//      gaw.numCommentsLabel.addStyleName("gallery-meta");
      
      container.add(appCard);
		}

		/*
    Label pageNext = new Label("Next");
    container.add(pageNext);
    pageNext.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        switch (request) {
          case 1: 
            break;   
          case 2: 
            break;  
          case 3:
            break;  
          case 5:
            break;  
        }
      }
    });    
    */
    
		container.addStyleName("gallery-app-collection");
		
	}
  
  /**
   * Creates list of comments in the app page.
   *
   * @param comments: list of returned gallery comments from callback.
   * 
   * @param container: the GUI panel where comments will reside.
   * 
   */
  public void generateAppPageComments(List<GalleryComment> comments, FlowPanel container) {

    for ( GalleryComment c : comments) {
      FlowPanel commentItem = new FlowPanel();
      FlowPanel commentPerson = new FlowPanel();
      FlowPanel commentMeta = new FlowPanel();
      FlowPanel commentContent = new FlowPanel();
      
      // Add commentPerson, default avatar for now
      Image cPerson = new Image();
      cPerson.setUrl("http://i.imgur.com/1h7cUkM.png");
      commentPerson.add(cPerson);
      commentPerson.addStyleName("comment-person");
      commentItem.add(commentPerson);
      
      // Add commentContent
      Label cAuthor = new Label(c.getAuthor());
      cAuthor.addStyleName("comment-author");
      commentMeta.add(cAuthor);

      Date commentDate = new Date(Long.parseLong(c.getTimeStamp()));
      DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd hh:mm:ss a");
      Label cDate = new Label(" on " + dateFormat.format(commentDate));
      cDate.addStyleName("comment-date");
      commentMeta.add(cDate);

      commentMeta.addStyleName("comment-meta");
      commentContent.add(commentMeta);

      Label cText = new Label(c.getText());
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
   * passed-in container's name.
   *
   * @param apps: list of returned gallery apps from callback.
   * 
   * @param container: the GUI panel where the sidebar will reside.
   * 
   * @param name: the name or title of this particular sidebar.
   * 
   * @param refreshable: if true then the GUI can be reloaded later.
   */
  public void generateSidebar(List<GalleryApp> apps, FlowPanel container, String name, Boolean refreshable) {
    if (refreshable) {
      // Flush the panel's content if we knew new stuff is coming in!
      container.clear();
    }
    
    Label title = new Label(name);
    title.addStyleName("gallery-showcase-title");
    container.add(title);
    
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

      gaw.image.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app); 
        }
      });
      
      appTitle.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(app); 
        }
      });

      // Add everything to the top-level stuff
      appCard.add(gaw.image);
      appCard.add(appCardContent);
      appCardContent.add(appTitle);
      appCardContent.add(gaw.authorLabel);
      appCardContent.add(appCardMeta);

      // Set helper icons
      Image numViews = new Image();
      numViews.setUrl("http://i.imgur.com/jyTeyCJ.png");
      Image numDownloads = new Image();
      numDownloads.setUrl("http://i.imgur.com/j6IPJX0.png");
      Image numLikes = new Image();
      numLikes.setUrl("http://i.imgur.com/N6Lpeo2.png");
    // For generic cards, do not show comment
//    Image numComments = new Image();
//    numComments.setUrl("http://i.imgur.com/GGt7H4c.png");
      
      appCardMeta.add(numViews);
      appCardMeta.add(gaw.numViewsLabel);
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
      gaw.authorLabel.addStyleName("gallery-subtitle");
      appCardContent.addStyleName("gallery-card-content");
      gaw.numViewsLabel.addStyleName("gallery-meta");
      gaw.numDownloadsLabel.addStyleName("gallery-meta");
      gaw.numLikesLabel.addStyleName("gallery-meta");
//      gaw.numCommentsLabel.addStyleName("gallery-meta");
      
      container.add(appCard);
    }
    container.addStyleName("gallery-container");
    container.addStyleName("gallery-app-showcase");
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
