// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.Message;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:
  private final DropDownButton accountButton;
  private final String WIDGET_NAME_MESSAGES = "Messages";
  private final TextButton moderation;
  private final String WIDGET_NAME_SIGN_OUT = "Signout";
  private final String WIDGET_NAME_USER = "User";
  private static final String SIGNOUT_URL = "/ode/_logout";
  private static final String LOGO_IMAGE_URL = "/images/logo.png";

  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  final Ode ode = Ode.getInstance();

  /**
   * Initializes and assembles all UI elements shown in the top panel.
   */
  public TopPanel() {
    /*
     * The layout of the top panel is as follows:
     *
     *  +-- topPanel ------------------------------------+
     *  |+-- logo --++-----tools-----++--links/account--+|
     *  ||          ||               ||                 ||
     *  |+----------++---------------++-----------------+|
     *  +------------------------------------------------+
     */
    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // Create the Tools
    TopToolbar tools = new TopToolbar();
    ode.setTopToolbar(tools);

    // Create the Links
    HorizontalPanel links = new HorizontalPanel();
    links.setStyleName("ode-TopPanelLinks");
    links.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // My Projects Link
    TextButton myProjects = new TextButton(MESSAGES.tabNameProjects());
    myProjects.setStyleName("ode-TopPanelButton");

    myProjects.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToProjectsView();
      }
    });

    myProjects.setStyleName("ode-TopPanelButton");
    links.add(myProjects);

    TextButton guideLink = new TextButton(MESSAGES.guideLink());
    guideLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("http://dev-explore.appinventor.mit.edu/library", "_ai2", null);
      }
    });

    guideLink.setStyleName("ode-TopPanelButton");
    links.add(guideLink);

    // Feedback Link
    TextButton feedbackLink = new TextButton(MESSAGES.feedbackLink());
    feedbackLink.setStyleName("ode-TopPanelButton");

    feedbackLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open("http://something.example.com", "_blank", null);
      }
    });

    feedbackLink.setStyleName("ode-TopPanelButton");
    links.add(feedbackLink);
	
	/*
	// Code on master branch
    // Gallery Link
    if (Ode.getInstance().getUser().getIsAdmin()) {
      TextButton gallery = new TextButton(MESSAGES.galleryLink());
      gallery.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          Window.open("http://gallery.appinventor.mit.edu", "_blank", null);
        }
      });

      gallery.setStyleName("ode-TopPanelButton");
      links.add(gallery);
    }
    */

    // Code on gallerydev branch
    // Gallery Link
    TextButton gallery = new TextButton(MESSAGES.tabNameGallery());
    gallery.setStyleName("ode-TopPanelButton");
    gallery.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToGalleryView();
      }
    });
    links.add(gallery);

    moderation = new TextButton(MESSAGES.tabNameModeration());
    moderation.setStyleName("ode-TopPanelButton");
    moderation.addClickHandler(new ClickHandler() {
    @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToModerationPageView();
      }
    });
    moderation.setVisible(false);
    links.add(moderation);

    // Create the Account Information
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    HorizontalPanel account = new HorizontalPanel();
    account.setStyleName("ode-TopPanelAccount");

    // Account Drop Down Button
    List<DropDownItem> userItems = Lists.newArrayList();

    // Messages
    userItems.add(new DropDownItem(WIDGET_NAME_MESSAGES, MESSAGES.messagesLink(), new MessageAction()));

    // Sign Out
    userItems.add(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));

    accountButton = new DropDownButton(WIDGET_NAME_USER, " " , userItems, true);
    accountButton.setStyleName("ode-TopPanelButton");

    account.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    account.add(links);
    account.add(accountButton);

    rightPanel.add(account);

    //topPanel.setWidth("width: 100%");

    // Add the Logo, Tools, Links to the TopPanel
    addLogo(topPanel);
    topPanel.add(tools);
    topPanel.add(rightPanel);
    topPanel.setCellVerticalAlignment(rightPanel, HorizontalPanel.ALIGN_MIDDLE);
    rightPanel.setCellHorizontalAlignment(account, HorizontalPanel.ALIGN_RIGHT);
    topPanel.setCellHorizontalAlignment(rightPanel, HorizontalPanel.ALIGN_RIGHT);

    initWidget(topPanel);

    setStyleName("ode-TopPanel");
    setWidth("100%");
  }

  private void addLogo(HorizontalPanel panel) {
    // Logo should be a link to App Inv homepage. Currently, after the user
    // has logged in, the top level *is* ODE; so for now don't make it a link.
    // Add timestamp to logo url to get around browsers that agressively cache
    // the image! This same trick is used in StorageUtil.getFilePath().
    Image logo = new Image(LOGO_IMAGE_URL + "?t=" + System.currentTimeMillis());
    logo.setSize("40px", "40px");
    logo.setStyleName("ode-Logo");
    logo.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("http://dev-explore.appinventor.mit.edu", "_ai2", null);
      }
    });
    panel.add(logo);
    panel.setCellWidth(logo, "50px");
    Label title = new Label("MIT App Inventor 2");
    Label version = new Label("Beta");
    title.setStyleName("ode-LogoText");
    version.setStyleName("ode-LogoVersion");
    VerticalPanel titleContainer = new VerticalPanel();
    titleContainer.add(title);
    titleContainer.add(version);
    titleContainer.setCellHorizontalAlignment(version, HorizontalPanel.ALIGN_RIGHT);
    panel.add(titleContainer);
    panel.setCellWidth(titleContainer, "180px");
    panel.setCellHorizontalAlignment(logo, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(logo, HorizontalPanel.ALIGN_MIDDLE);
  }

  private void addMotd(VerticalPanel panel) {
    MotdBox motdBox = MotdBox.getMotdBox();
    panel.add(motdBox);
    panel.setCellHorizontalAlignment(motdBox, HorizontalPanel.ALIGN_RIGHT);
    panel.setCellVerticalAlignment(motdBox, HorizontalPanel.ALIGN_BOTTOM);
  }

  /**
   * Updates the UI to show the user's email address.
   *
   * @param email the email address
   */
  public void showUserEmail(String email) {
    accountButton.setCaption(email);
  }

  /**
   * Updates the UI to show the moderation's link.
   */
  public void showModerationLink() {
    moderation.setVisible(true);
  }

  /**
   * Adds the MOTD box to the right panel. This should only be called once.
   */
  public void showMotd() {
    addMotd(rightPanel);
  }

  private static class SignOutAction implements Command {
    @Override
    public void execute() {
      Window.Location.replace(SIGNOUT_URL);
    }
  }

  private static class MessageAction implements Command {
    @Override
    public void execute() {
      final int[] msgCount = {0};
      // Create a PopUpPanel with a button to close it
      final PopupPanel popup = new PopupPanel(true);
      popup.setStyleName("ode-InboxContainer");
      final FlowPanel content = new FlowPanel();
      content.addStyleName("ode-Inbox");
      Label title = new Label(MESSAGES.messageInboxTitle());
      title.addStyleName("InboxTitle");
      content.add(title);

      Button closeButton = new Button("x");
//      closeButton.addStyleName("ActionButton");
      closeButton.addStyleName("CloseButton");
      closeButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      content.add(closeButton);

      final FlowPanel msgList = new FlowPanel();
      msgList.addStyleName("MsgList");
      ScrollPanel msgPanel = new ScrollPanel(msgList);
      msgPanel.addStyleName("MsgListWrapper");
      content.add(msgPanel);

      // Retrieve list of unread dl and likes from this user's apps
      final OdeAsyncCallback<List<GalleryApp>> appUnreadCallback =
          new OdeAsyncCallback<List<GalleryApp>>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(List<GalleryApp> apps) {
              // get the new unread list so gui updates
              for (final GalleryApp app : apps) {
                // Only add if this app actually has unread data
                if (app.getUnreadDownloads() + app.getUnreadLikes() > 0) {
                  msgCount[0]++;
                  generateUnreadStatsMessage(app, app.getUnreadDownloads(), app.getUnreadLikes(), msgList);
                }
              }
              if (msgCount[0] == 0) { // No new messages, add a prompt
                Label noMsgPrompt = new Label("You have no messages at this moment.");
                noMsgPrompt.addStyleName("MsgNoPrompt");
                msgList.add(noMsgPrompt);
              }
            }
        };
      Ode.getInstance().getGalleryService().getDeveloperApps(Ode.getInstance().getUser().getUserId(), 0, 10, appUnreadCallback);


      // Retrieve list of unread messages of this user
      // We are not using this at the moment because all messages will be system generated ones above
      final OdeAsyncCallback<List<Message>> msgUnreadCallback = new OdeAsyncCallback<List<Message>>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(List<Message> msgs) {
              // get the new comment list so gui updates
              OdeLog.log("### MSGS RETRIEVED SUCCESSFULLY, size = " + msgs.size());
              for (final Message m : msgs) {
//                msgCount++;
                HTML msgBody = new HTML(m.getMessage());
                msgBody.setStyleName("demo-PopUpPanel-message");
                content.add(msgBody);

                if (m.getStatus().equalsIgnoreCase("1")) {
                  Button msgButton = new Button("Confirm read");
                  msgButton.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                      final OdeAsyncCallback<Void> messagesCallback = new OdeAsyncCallback<Void>(
                          MESSAGES.galleryError()) {
                            @Override
                            public void onSuccess(Void result) {
                              OdeLog.log("### MSGS READ SUCCESSFULLY");
                            }
                        };
                      Ode.getInstance().getGalleryService().readMessage(m.getTimetamp(), messagesCallback);
                    }
                  });
                  content.add(msgButton);
                }
              }
            }
        };
      Ode.getInstance().getGalleryService().getMessages(Ode.getInstance().getUser().getUserId(), msgUnreadCallback);



      popup.setWidget(content);
      // Center and show the popup
      popup.center();
    }
  }

  /**
   * Helper method to generate the UI for a single "unread app stats" message.
   * @param app   the app for the unread statistics
   * @param dls   the number of unread downloads of the app
   * @param likes   the number of unread likes of the app
   * @param container   the parent container that this message resides in
   */
  private static void generateUnreadStatsMessage(final GalleryApp app, int dls, int likes, FlowPanel container) {
    Boolean hasDls = true;
    Boolean hasLikes = true;
    if (dls == 0) {
      hasDls = false;
    } else if ( likes == 0) {
      hasLikes = false;
    }
    final FlowPanel msg = new FlowPanel();
    msg.addStyleName("MsgEntry");
    Label msgBody = new Label("Your app \"" + app.getTitle() + "\" has been liked "
        + likes + " times and downloaded " + dls + " times since the last time you check it. Keep up the good work!");
    if (hasDls && !hasLikes) {
      msgBody.setText("Your app \"" + app.getTitle() + "\" has been downloaded "
          + dls + " times" + " since the last time you check it. Keep up the good work!");
    } else if (!hasDls && hasLikes) {
      msgBody.setText("Your app \"" + app.getTitle() + "\" has been liked "
          + likes + " times" + " since the last time you check it. Keep up the good work!");
    }
    msgBody.setStyleName("MsgBody");
    msg.add(msgBody);
    FlowPanel msgMeta = new FlowPanel();
    msgMeta.addStyleName("MsgMeta");
    msgMeta.addStyleName("clearfix");
    Label actionRead = new Label("Mark as read");
    actionRead.addStyleName("primary-link");
    Label actionDelete = new Label("Delete message");
    actionDelete.addStyleName("primary-link");
    Label msgSender = new Label("Sent from System");
    msgSender.addStyleName("MsgSender");
    msgMeta.add(actionRead);
//    msgMeta.add(actionDelete);
//    msgMeta.add(msgSender);
    msg.add(msgMeta);
    container.add(msg);

    actionRead.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Void> messagesCallback = new OdeAsyncCallback<Void>(
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Void result) {
//                OdeLog.log("### appstats READ SUCCESSFULLY");
                msg.removeFromParent();
              }
          };
        Ode.getInstance().getGalleryService().appStatsWasRead(app.getGalleryAppId(), messagesCallback);
      }
    });
  }
}

