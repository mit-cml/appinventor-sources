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
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.client.GalleryClient;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.OdeLog;


import com.google.appinventor.shared.rpc.project.GalleryAppReport;

/**
 * The report list shows all reports in a table.
 *
 * <p> The report text, date created, user reported on and user reporting will be shown in the table.
 *
 * @author wolberd@gmail.com, based on ProjectList.java, lizlooney@google.com (Liz Looney),
 */
public class ReportList extends Composite  {

  private List<GalleryAppReport> reports;
  private List<GalleryAppReport> selectedReports;
  private final List<GalleryAppReport> selectedGalleryAppReports;
  private final Map<GalleryAppReport, ReportWidgets> ReportWidgets;

  // UI elements
  private final Grid table;

  /**
   * Creates a new ProjectList
   */
  public ReportList() {

    selectedGalleryAppReports = new ArrayList<GalleryAppReport>();
    ReportWidgets = new HashMap<GalleryAppReport, ReportWidgets>();

    // Initialize UI
    table = new Grid(1, 8); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);

    setHeaderRow();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);

    initializeReports();

  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    HorizontalPanel reportHeader = new HorizontalPanel();
    final Label reportHeaderLabel = new Label(MESSAGES.moderationReportTextHeader());
    reportHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    reportHeader.add(reportHeaderLabel);
    table.setWidget(0, 0, reportHeader);

    HorizontalPanel appHeader = new HorizontalPanel();
    final Label appHeaderLabel = new Label(MESSAGES.moderationAppHeader());
    appHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    appHeader.add(appHeaderLabel);
    table.setWidget(0, 1, appHeader);

    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
    final Label dateCreatedHeaderLabel = new Label(MESSAGES.moderationReportDateCreatedHeader());
    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedHeaderLabel);
    table.setWidget(0, 2, dateCreatedHeader);

    HorizontalPanel appAuthorHeader = new HorizontalPanel();
    final Label appAuthorHeaderLabel = new Label(MESSAGES.moderationAppAuthorHeader());
    appAuthorHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    appAuthorHeader.add(appAuthorHeaderLabel);
    table.setWidget(0, 3, appAuthorHeader);

    HorizontalPanel reporterHeader = new HorizontalPanel();
    final Label reporterHeaderLabel = new Label(MESSAGES.moderationReporterHeader());
    reporterHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    reporterHeader.add(reporterHeaderLabel);
    table.setWidget(0, 4, reporterHeader);

  }

  private void initializeReports() {
    final OdeAsyncCallback<List<GalleryAppReport>> callback = new OdeAsyncCallback<List<GalleryAppReport>>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(List<GalleryAppReport> reportList) {
                reports=reportList;
                ReportWidgets.clear();
                for (GalleryAppReport report : reports) {
                  ReportWidgets.put(report, new ReportWidgets(report));
                }
                refreshTable();
              }
          };
        Ode.getInstance().getGalleryService().getRecentReports(0,10,callback);
  }

  private class ReportWidgets {
    final Label reportTextLabel;
    final Label appLabel;
    final Label dateCreatedLabel;
    final Label appAuthorlabel;
    final Label reporterLabel;
    final Button sendMessageButton;
    final Button deactiveAppButton;
    final Button markAsResolvedButton;

    private ReportWidgets(final GalleryAppReport report) {

      reportTextLabel = new Label(report.getReportText());
      reportTextLabel.addStyleName("ode-ProjectNameLabel");

      appLabel = new Label(report.getApp().getTitle());
      appLabel.addStyleName("ode-ProjectNameLabel");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      Date dateCreated = new Date(report.getTimeStamp());
      dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));

      appAuthorlabel = new Label(report.getOffender().getUserName());
      appAuthorlabel.addStyleName("ode-ProjectNameLabel");

      reporterLabel = new Label(report.getReporter().getUserName());
      reporterLabel.addStyleName("ode-ProjectNameLabel");

      sendMessageButton = new Button("Send Message");

      deactiveAppButton = new Button("Deactive App");

      markAsResolvedButton = new Button("Mark As Resolved");


    }
  }

  private void refreshTable() {

    // Refill the table.
    table.resize(1 + reports.size(), 8);
    int row = 1;
    for (GalleryAppReport report : reports) {
      ReportWidgets rw = ReportWidgets.get(report);
      table.setWidget(row, 0, rw.reportTextLabel);
      table.setWidget(row, 1, rw.appLabel);
      table.setWidget(row, 2, rw.dateCreatedLabel);
      table.setWidget(row, 3, rw.appAuthorlabel);
      table.setWidget(row, 4, rw.reporterLabel);
      table.setWidget(row, 5, rw.sendMessageButton);
      table.setWidget(row, 6, rw.deactiveAppButton);
      table.setWidget(row, 7, rw.markAsResolvedButton);
      prepareGalleryAppReport(report, rw);
      row++;
    }

    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  /**
   *
   */
  private void prepareGalleryAppReport(final GalleryAppReport r, final ReportWidgets rw) {
    rw.reportTextLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        DialogBox db = new DialogBox();
        db.setText(r.getReportText());
        db.show();
      }
    });

    rw.appLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToGalleryAppView(r.getApp(), GalleryPage.VIEWAPP);
        }
    });

    rw.appAuthorlabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().switchToUserProfileView(r.getOffender().getUserId(), 1 /* 1 for public view*/ );
        }
    });

    rw.reporterLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            Ode.getInstance().switchToUserProfileView(r.getReporter().getUserId(), 1 /* 1 for public view*/ );
        }
    });

    rw.sendMessageButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

      }
    });
    rw.deactiveAppButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

        }
    });
    rw.markAsResolvedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(Boolean success) {
              if(success){
                onReportRemoved(r);
              }
            }
        };
        Ode.getInstance().getGalleryService().markReportAsResolved(r.getReportId(), callback);
      }
    });
  }

  /**
   * Gets the number of reports
   *
   * @return the number of reports
   */
  public int getNumGalleryAppReports() {
    return reports.size();
  }

  /**
   * Gets the number of selected reports
   *
   * @return the number of selected reports
   */
  public int getNumSelectedGalleryAppReports() {
    return selectedGalleryAppReports.size();
  }

  /**
   * Returns the list of selected reports
   *
   * @return the selected reports
   */
  public List<GalleryAppReport> getSelectedGalleryAppReports() {
    return selectedGalleryAppReports;
  }

  // ProjectManagerEventListener implementation

  public void onReportAdded(GalleryAppReport report) {
    reports.add(report);
    ReportWidgets.put(report, new ReportWidgets(report));
    refreshTable();
  }

  public void onReportRemoved(GalleryAppReport report) {
    reports.remove(report);
    ReportWidgets.remove(report);

    refreshTable();

    selectedGalleryAppReports.remove(report);
  }
}