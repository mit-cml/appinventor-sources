// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ReportList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabPanel;


/**
 * TabPanel implementation for project list.
 *
 */
public final class ModerationPageBox extends TabPanel {

  // Singleton project explorer box instance (only one project explorer allowed)
  private static final ModerationPageBox INSTANCE = new ModerationPageBox();

  // moderation list
  private ReportList reports;

  /**
   * Returns the singleton moderations list box.
   *
   * @return  moderation list box
   */
  public static ModerationPageBox getModerationPageBox() {
    return INSTANCE;
  }

  public static void loadModerationPage() {

    INSTANCE.reports = new ReportList();

    FlowPanel reportsContainer = new FlowPanel();
    reportsContainer.add(INSTANCE.reports);

    INSTANCE.add(reportsContainer, MESSAGES.moderationPageBoxCaption());
    INSTANCE.selectTab(0);

    // Styling options
    INSTANCE.addStyleName("gallery");
  }


  /**
   * Creates new moderation list box.
   */
  private ModerationPageBox() {

    reports = null;

  }

  /**
   * Returns report list associated with moderation page box.
   *
   * @return  report list
   */
  public ReportList getReportList() {
     return reports;
  }
}
