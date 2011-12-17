// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.help.HelpService;
import com.google.appinventor.shared.rpc.help.HelpServiceAsync;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  private static final String LEARN_URL = Ode.APP_INVENTOR_DOCS_URL + "/learn/";

  private final HTML userEmail = new HTML();
  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  private static final String EXTRA_TEXT_IF_NOT_PRODUCTION = "This is an experimental version of "
      + " App Inventor. IT IS FOR TESTING ONLY, NOT FOR GENERAL USE!"
      + " Note: This App Inventor instance is not being hosted on AppEngine."
      + " As a result, it will not correctly save your projects when you log out."
      + " You'll have to download them if you want them saved.";

  // TODO(user) Remove the expermental warning label below when ready.
  private final Label warning = new Label("This is an experimental version of App Inventor. "
      + "IT IS FOR TESTING ONLY, NOT FOR GENERAL USE! ");

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
     *  ||          ||               ||           ||
     *  |+----------++---------------++-----------+|
     *  +------------------------------------------+
     */
    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // First row - right side is account, report bug, sign out
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
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
        new HTML("<a href='" + ServerLayout.ODE_LOGOUT_URL + "'>" +
        makeSpacesNonBreakable(MESSAGES.signOutLink()) + "</a>");
    account.add(signOutLink);

    rightPanel.add(account);
    addLogo(topPanel);

    HorizontalPanel middleLinks = new HorizontalPanel();
    middleLinks.setStyleName("ode-TopPanelMiddleLinks");
    middleLinks.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    final Ode ode = Ode.getInstance();

    Label myApps = new Label(MESSAGES.tabNameProjects());
    myApps.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          ode.switchToProjectsView();
        }
      }
    );
    middleLinks.add(myApps);

    Label design = new Label(MESSAGES.tabNameDesign());
    design.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          ode.switchToDesignView();
        }
      }
    );
    middleLinks.add(design);

    Anchor learn = new Anchor(MESSAGES.tabNameLearn(), LEARN_URL, "_blank");
    middleLinks.add(learn);

    if (AppInventorFeatures.hasDebuggingView()) {
      Label debugging = new Label(MESSAGES.tabNameDebugging());
      debugging.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            ode.switchToDebuggingView();
          }
        }
      );
      middleLinks.add(debugging);
    }

    setWarningLabelText();
    warning.setStyleName("ode-TopPanelWarningLabel");
    middleLinks.add(warning);

    topPanel.add(middleLinks);
    topPanel.add(rightPanel);

    topPanel.setCellVerticalAlignment(rightPanel, HorizontalPanel.ALIGN_TOP);
    rightPanel.setCellHorizontalAlignment(account, HorizontalPanel.ALIGN_RIGHT);
    topPanel.setCellHorizontalAlignment(rightPanel, HorizontalPanel.ALIGN_RIGHT);

    initWidget(topPanel);

    setStyleName("ode-TopPanel");
    setWidth("100%");
  }

  private void setWarningLabelText() {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
      }
      @Override
      public void onSuccess(Boolean isProduction) {
        if (!isProduction) {
          warning.setText(EXTRA_TEXT_IF_NOT_PRODUCTION);
        }
      }
    };
    Ode.getInstance().getHelpService().isProductionServer(callback);
  }

  private void addLogo(HorizontalPanel panel) {
    // Logo should be a link to App Inv homepage. Currently, after the user
    // has logged in, the top level *is* ODE; so for now don't make it a link.
    Image logo = new Image(Ode.getImageBundle().logoYoungAndroid());
    panel.add(logo);
    panel.setCellHorizontalAlignment(logo, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(logo, HorizontalPanel.ALIGN_MIDDLE);
    panel.setCellWidth(logo, "228px");
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
