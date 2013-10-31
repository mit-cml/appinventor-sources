// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.widgets.TextButton;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
  private static final String LEARN_URL = Ode.APP_INVENTOR_DOCS_URL + "/learn/";
  private static final String KNOWN_ISSUES_LINK_URL =
    Ode.APP_INVENTOR_DOCS_URL + "/knownIssues.html";
  private static final String RELEASE_NOTES_LINK_URL =
    Ode.APP_INVENTOR_DOCS_URL + "/ReleaseNotes.html";
  private static final String KNOWN_ISSUES_LINK_AND_TEXT =
    "<a href=\"" + KNOWN_ISSUES_LINK_URL + "\" target=\"_blank\">known issues</a>" ;
  private static final String RELEASE_NOTES_LINK_AND_TEXT =
    "<a href=\"" + RELEASE_NOTES_LINK_URL + "\" target=\"_blank\">release notes</a>"; 
  private static final String GALLERY_LINK_AND_TEXT =
    "<a href=\"http://gallery.appinventor.mit.edu\" target=\"_blank\">" +
    "Try the App Inventor Community Gallery (Beta)</a>";
  private static final String SIGNOUT_URL = "/ode/_logout";

  private static final String LOGO_IMAGE_URL = "/images/logo.png";
  private static final String MYPROJECTS_IMAGE_URL = "/images/myprojects.png";

  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  private String termsOfServiceText =
    "<a href='" + Ode.APP_INVENTOR_DOCS_URL + "/about/termsofservice.html'" +
    " target=_blank>" + MESSAGES.privacyTermsLink() + "</a>";

  private final HTML welcome = new HTML("Welcome to the App Inventor 2 alpha release.<BR>" + "."
  );

  private HTML divider() {
    return new HTML("<span class='linkdivider'>&nbsp;|&nbsp;</span>");
  }

  private final DropDownButton accountButton;

  final Ode ode = Ode.getInstance();

  // Strings for Drop Down Menus:
  private final String WIDGET_NAME_MY_PROJECTS = "myProjects";
  private final String WIDGET_NAME_FEEDBACK = "Report a problem";
  private final String WIDGET_NAME_SIGN_OUT = "signOut";
  private final String WIDGET_NAME_USER = "user";

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

    TextButton guideLink = new TextButton("Guide");
    guideLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("http://appinventor.mit.edu/explore/ai2/user-guide", "_blank", null);
      }
    });

    guideLink.setStyleName("ode-TopPanelButton");
    links.add(guideLink);

	/*
	// Code on master branch
    // Gallery Link
    if (Ode.getInstance().getUser().getIsAdmin()) {
      TextButton gallery = new TextButton("Gallery");
      gallery.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
          Window.open("http://gallery.appinventor.mit.edu", "_blank", null);
        }
    });

    // Code on gallery2 branch
    gallery.setStyleName("ode-TopPanelButton");
    links.add(gallery);
    }
	Label galleryLabel = new Label(MESSAGES.tabNameGallery());
	galleryLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToGalleryView();
      }
     });
    galleryLabel.setStyleName("gwt-TitleLabel");
    middleLinks.add(galleryLabel);		
	*/
		
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

    // Create the Account Information
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    HorizontalPanel account = new HorizontalPanel();
    account.setStyleName("ode-TopPanelAccount");

    // Account Drop Down Button
    List<DropDownItem> userItems = Lists.newArrayList();

    // My Projects
    userItems.add(new DropDownItem(WIDGET_NAME_MY_PROJECTS, MESSAGES.tabNameProjects(), new SwitchToProjectAction()));

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
    panel.add(logo);
    panel.setCellWidth(logo, "50px");
    Label title = new Label("MIT App Inventor 2");
    Label version = new Label("Beta");
    VerticalPanel titleContainer = new VerticalPanel();
    titleContainer.add(title);
    titleContainer.add(version);
    titleContainer.setCellHorizontalAlignment(version, HorizontalPanel.ALIGN_RIGHT);
    panel.add(titleContainer);
    panel.setCellWidth(titleContainer, "180px");
    title.setStyleName("ode-LogoText");
    version.setStyleName("ode-LogoVersion");
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
   * Adds the MOTD box to the right panel. This should only be called once.
   */
  public void showMotd() {
    addMotd(rightPanel);
  }

  private static String makeSpacesNonBreakable(String s) {
    return s.replace(" ", "&nbsp;");
  }

  private static class SwitchToProjectAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToProjectsView();
    }
  }

  private static class SignOutAction implements Command {
    @Override
    public void execute() {
      Window.Location.replace(SIGNOUT_URL);
    }
  }
}

