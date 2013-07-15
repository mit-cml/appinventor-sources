// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.common.version.GitBuildId;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
    "<a href=\"" + RELEASE_NOTES_LINK_URL + "\" target=\"_blank\">release notes</a>" ;
  private static final String GALLERY_LINK_AND_TEXT =
    "<a href=\"http://gallery.appinventor.mit.edu\" target=\"_blank\">" +
    "Try the App Inventor Community Gallery (Beta)</a>";

  private static final String LOGO_IMAGE_URL = "/images/logo.png";

  private final HTML userEmail = new HTML();
  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  private String termsOfServiceText =
    "<a href='" + Ode.APP_INVENTOR_DOCS_URL + "/about/termsofservice.html'" +
    " target=_blank>" + MESSAGES.privacyTermsLink() + "</a>";

  private final HTML welcome = new HTML("Welcome to the App Inventor 2 alpha release.<BR>" +
      GALLERY_LINK_AND_TEXT + "."
  );

  private HTML divider() {
    return new HTML("<span class='linkdivider'>&nbsp;|&nbsp;</span>");
  }

  /**
   * Initializes and assembles all UI elements shown in the top panel.
   */
  public TopPanel() {
    /*
     * The layout of the top panel is as follows:
     *
     *  +-- topPanel ------------------------------+
     *  |+-- logo --++--middleLinks--++--account--+|
     *  ||          ||               ||            |
     *  |+----------++---------------++-----------+|
     *  +------------------------------------------+
     */
    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // First row - right side is account, report bug, sign out
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    HorizontalPanel account = new HorizontalPanel();
    account.setStyleName("ode-TopPanelAccount");
    account.add(userEmail);
    account.add(divider());

    if (AppInventorFeatures.sendBugReports()) {
      HTML reportBugLink =
        new HTML("<a href='" + BugReport.getBugReportLink() + "' target='_blank'>" +
            makeSpacesNonBreakable(MESSAGES.reportBugLink()) + "</a>");
      account.add(reportBugLink);
      account.add(divider());
    }

    HTML signOutLink =
      new HTML("<a href='/ode/_logout'>" +
          makeSpacesNonBreakable(MESSAGES.signOutLink()) + "</a>");
    account.add(signOutLink);

    rightPanel.add(account);

    topPanel.setWidth("width: 100%");

    addLogo(topPanel);

    HorizontalPanel middleLinks = new HorizontalPanel();
    middleLinks.setStyleName("ode-TopPanelMiddleLinks");
    middleLinks.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    final Ode ode = Ode.getInstance();

    Label myApps = new Label(MESSAGES.tabNameProjects());
    myApps.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToProjectsView();
      }
    }
    );
    myApps.setStyleName("gwt-TitleLabel");
    middleLinks.add(myApps);

    Label aboutButton = new Label(MESSAGES.aboutLink());
    aboutButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final DialogBox db = new DialogBox(false, true);
        db.setText("About MIT App Inventor");
        db.setStyleName("ode-DialogBox");
        db.setHeight("200px");
        db.setWidth("400px");
        db.setGlassEnabled(true);
        db.setAnimationEnabled(true);
        db.center();

        VerticalPanel DialogBoxContents = new VerticalPanel();
        HTML message = new HTML(
            MESSAGES.gitBuildId(GitBuildId.getDate(), GitBuildId.getVersion()) +
            "<BR><BR>Please see " + RELEASE_NOTES_LINK_AND_TEXT +
            " and " + KNOWN_ISSUES_LINK_AND_TEXT  + "." +
            "<BR><BR>" + termsOfServiceText
        );
        message.setStyleName("DialogBox-message");

        SimplePanel holder = new SimplePanel();
        //holder.setStyleName("DialogBox-footer");
        Button ok = new Button("Close");
        ok.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            db.hide();
          }
        });
        holder.add(ok);
        DialogBoxContents.add(message);
        DialogBoxContents.add(holder);
        db.setWidget(DialogBoxContents);
        db.show();
      }
    });
    //    rightMiddleLinks.add(aboutButton);
    aboutButton.setStyleName("gwt-TitleLabel");
    middleLinks.add(aboutButton);

    Label spacer = new Label("|");
    //    spacer.setWidth("50px");
    middleLinks.add(spacer);

    Anchor learn = new Anchor(MESSAGES.tabNameLearn(), LEARN_URL, "_blank");
    learn.setStyleName("gwt-TitleLabel");
    middleLinks.add(learn);

    Anchor gallery = new Anchor("Gallery", "http://gallery.appinventor.mit.edu", "_blank");
    gallery.setStyleName("gwt-TitleLabel");
    middleLinks.add(gallery);

    topPanel.add(middleLinks);
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
    Label version = new Label("alpha");
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
    userEmail.setHTML(email);
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
}
