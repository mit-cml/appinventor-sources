// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;


import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.shared.rpc.project.Email;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.shared.rpc.project.GalleryModerationAction;
import com.google.appinventor.shared.rpc.project.GalleryReportListResult;
import com.google.appinventor.shared.rpc.user.User;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The report list shows all reports in a table.
 *
 * <p> The report text, date created, user reported on and user reporting will be shown in the table.
 *
 * @author wolberd@gmail.com, based on ProjectList.java, lizlooney@google.com (Liz Looney),
 * @author blu2@dons.usfca.edu (Bin Lu)
 */
public class ReportList extends Composite  {
  /**
   * The number of RPCs that will be made when the ReportList is initialized. This is used in
   * {@link Ode#initializeUi()} to determine when to hide the Loading message.
   */
  public static final int INITIAL_RPCS = 1;
  public static final int MAX_EMAIL_PREVIEW_LENGTH = 40;
  private final CheckBox checkBox;
  private final VerticalPanel panel;
  private List<GalleryAppReport> reports;
  private List<GalleryAppReport> selectedReports;
  private final List<GalleryAppReport> selectedGalleryAppReports;
  private final Map<GalleryAppReport, ReportWidgets> ReportWidgets;
  private DropDownButton templateButton;
  private GalleryClient galleryClient;

  // UI elements
  private final Grid table;
  private final Label buttonNext;

  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);

  public static final int EMAIL_INAPPROPRIATE_APP_CONTENT_REMOVE = 1;
  public static final int EMAIL_INAPPROPRIATE_APP_CONTENT = 2;
  public static final int EMAIL_INAPPROPRIATE_USER_PROFILE_CONTENT = 3;

  public static final int NUMREPORTSSHOW = 10;
  private int reportRecentCounter = 0;
  private int reportAllRecentCounter = 0;

  /**
   * Creates a new ProjectList
   */
  public ReportList() {
    galleryClient = GalleryClient.getInstance();
    // Initialize UI
    panel = new VerticalPanel();
    panel.setWidth("100%");

    HorizontalPanel checkBoxPanel = new HorizontalPanel();
    checkBoxPanel.addStyleName("all-reports");
    checkBox = new CheckBox();
    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
        //reset start position
        reportAllRecentCounter = 0;
        reportRecentCounter = 0;
        buttonNext.setVisible(true);
        if (isChecked) {
          initializeAllReports();
        } else {
          initializeReports();
        }
      }
    });
    checkBoxPanel.add(checkBox);
    Label checkBoxText = new Label(MESSAGES.moderationShowResolvedReports());
    checkBoxPanel.add(checkBoxText);
    panel.add(checkBoxPanel);

    selectedGalleryAppReports = new ArrayList<GalleryAppReport>();
    ReportWidgets = new HashMap<GalleryAppReport, ReportWidgets>();

    table = new Grid(1, 9); // The table initially contains just the header row.
    table.addStyleName("ode-ModerationTable");
    table.setWidth("100%");
    table.setCellSpacing(0);

    buttonNext = new Label();
    buttonNext.setText(MESSAGES.galleryMoreReports());

    buttonNext.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<GalleryReportListResult> callback = new OdeAsyncCallback<GalleryReportListResult>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(GalleryReportListResult reportListResult) {
                List<GalleryAppReport> reportList = reportListResult.getReports();
                reports.addAll(reportList);
                for (GalleryAppReport report : reportList) {
                  ReportWidgets.put(report, new ReportWidgets(report));
                }
                refreshTable(reportListResult, false);
              }
          };
          if(checkBox.isChecked()){
            reportAllRecentCounter += NUMREPORTSSHOW;
            Ode.getInstance().getGalleryService().getAllAppReports(reportAllRecentCounter,NUMREPORTSSHOW,callback);
          }else{
            reportRecentCounter += NUMREPORTSSHOW;
            Ode.getInstance().getGalleryService().getRecentReports(reportRecentCounter,NUMREPORTSSHOW,callback);
          }
      }
    });

    setHeaderRow();

    panel.add(table);
    FlowPanel next = new FlowPanel();
    buttonNext.addStyleName("active");
    next.add(buttonNext);
    next.addStyleName("gallery-report-next");
    panel.add(next);
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

  /**
   * initialize reports, only including solved reports
   */
  private void initializeReports() {
    final OdeAsyncCallback<GalleryReportListResult> callback = new OdeAsyncCallback<GalleryReportListResult>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
        public void onSuccess(GalleryReportListResult reportListResult) {
          List<GalleryAppReport> reportList = reportListResult.getReports();
          reports=reportList;
          ReportWidgets.clear();
          for (GalleryAppReport report : reports) {
            ReportWidgets.put(report, new ReportWidgets(report));
          }
          refreshTable(reportListResult, true);
        }
    };
    Ode.getInstance().getGalleryService().getRecentReports(reportRecentCounter,NUMREPORTSSHOW,callback);
  }

  /**
   * initialize all reports, including both solved and unsolved reports
   */
  private void initializeAllReports() {
    final OdeAsyncCallback<GalleryReportListResult> callback = new OdeAsyncCallback<GalleryReportListResult>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
        public void onSuccess(GalleryReportListResult reportListResult) {
          List<GalleryAppReport> reportList = reportListResult.getReports();
          reports=reportList;
          ReportWidgets.clear();
          for (GalleryAppReport report : reports) {
            ReportWidgets.put(report, new ReportWidgets(report));
          }
          refreshTable(reportListResult, true);
        }
      };
    Ode.getInstance().getGalleryService().getAllAppReports(reportAllRecentCounter,NUMREPORTSSHOW,callback);
  }
  /**
   * Helper wrapper Class of Report Widgets
   */
  private class ReportWidgets {
    final Label reportTextLabel;
    final Label appLabel;
    final Label dateCreatedLabel;
    final Label appAuthorlabel;
    final Label reporterLabel;
    final Button sendEmailButton;
    final Button deactiveAppButton;
    final Button markAsResolvedButton;
    final Button seeAllActions;
    boolean appActive;
    boolean appResolved;
    /**
     * Constructor of ReportWidgets
     * @param report GalleryAppReport
     */
    private ReportWidgets(final GalleryAppReport report) {

      reportTextLabel = new Label(report.getReportText());
      reportTextLabel.addStyleName("ode-ProjectNameLabel");
      reportTextLabel.setWordWrap(true);
      reportTextLabel.setWidth("200px");

      appLabel = new Label(report.getApp().getTitle());
      appLabel.addStyleName("primary-link");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      Date dateCreated = new Date(report.getTimeStamp());
      dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));

      appAuthorlabel = new Label(report.getOffender().getUserName());
      appAuthorlabel.addStyleName("primary-link");

      reporterLabel = new Label(report.getReporter().getUserName());
      reporterLabel.addStyleName("primary-link");

      sendEmailButton = new Button(MESSAGES.buttonSendEmail());

      deactiveAppButton = new Button(MESSAGES.labelDeactivateApp());

      markAsResolvedButton = new Button(MESSAGES.labelmarkAsResolved());

      seeAllActions = new Button(MESSAGES.labelSeeAllActions());
    }
  }

  /**
   * refresh report list table
   * Update the information of reports
   */
  private void refreshTable(GalleryReportListResult reportListResult, boolean refreshable) {
    int row;
    List<GalleryAppReport> incomingReports = reportListResult.getReports();
    if(refreshable){
      table.clear();
      table.resize(1+incomingReports.size(), 9);
      setHeaderRow();
      row = 1;
    }else{
      int nextRow = table.getRowCount();
      table.resize(1+reports.size(), 9);
      row = nextRow;
    }

    // Refill the table.
    for (GalleryAppReport report : incomingReports) {
      ReportWidgets rw = ReportWidgets.get(report);
      table.setWidget(row, 0, rw.reportTextLabel);
      table.setWidget(row, 1, rw.appLabel);
      table.setWidget(row, 2, rw.dateCreatedLabel);
      table.setWidget(row, 3, rw.appAuthorlabel);
      table.setWidget(row, 4, rw.reporterLabel);
      table.setWidget(row, 5, rw.sendEmailButton);
      table.setWidget(row, 6, rw.deactiveAppButton);
      table.setWidget(row, 7, rw.markAsResolvedButton);
      table.setWidget(row, 8, rw.seeAllActions);
      prepareGalleryAppReport(report, rw);
      row++;
    }
    //if the total num of row - 1(head row) == total count of reports, there are no more results
    if(table.getRowCount()-1 == reportListResult.getTotalCount()){
      buttonNext.setVisible(false);
    }
  }

  /**
   * Prepare gallery app report based on given GalleryAppReport
   * Setup the functionality of UI components.
   * @param r GalleryAppReport gallery app report
   * @param rw ReportWidgets report widgets
   */
  private void prepareGalleryAppReport(final GalleryAppReport r, final ReportWidgets rw) {
    rw.reportTextLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

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

    rw.sendEmailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendEmailPopup(r);
      }
    });

    final OdeAsyncCallback<Boolean> isActivatedCallback = new OdeAsyncCallback<Boolean>(
    // failure message
    MESSAGES.galleryError()) {
      @Override
      public void onSuccess(Boolean active) {
        if(active){
          rw.deactiveAppButton.setText(MESSAGES.labelDeactivateApp());
          rw.appActive = true;
        }
        else {
          rw.deactiveAppButton.setText(MESSAGES.labelReactivateApp());
          rw.appActive = false;
        }
      }
    };
    Ode.getInstance().getGalleryService().isGalleryAppActivated(r.getApp().getGalleryAppId(), isActivatedCallback);

    rw.deactiveAppButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if(rw.appActive == true){
              deactivateAppPopup(r, rw);
          }else{
            final OdeAsyncCallback<Long> emailCallback = new OdeAsyncCallback<Long>(
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(Long emailId) {
                if(emailId == Email.NOTRECORDED){
                  Window.alert(MESSAGES.moderationErrorFailToSendEmail());
                }else{
                  final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
                      MESSAGES.galleryError()) {
                        @Override
                        public void onSuccess(Boolean success) {
                          if(!success)
                            return;
                          rw.deactiveAppButton.setText(MESSAGES.labelDeactivateApp());//revert button
                          rw.appActive = true;
                          storeModerationAction(r.getReportId(), r.getApp().getGalleryAppId(), GalleryModerationAction.NOTAVAILABLE,
                              GalleryModerationAction.REACTIVATEAPP, null);
                          //update gallery list
                          galleryClient.appWasChanged();
                        }
                      };
                  Ode.getInstance().getGalleryService().deactivateGalleryApp(r.getApp().getGalleryAppId(), callback);
                }
              }
            };
            String emailBody = MESSAGES.moderationAppReactivateBody(r.getApp().getTitle()) +
                MESSAGES.galleryVisitGalleryAppLinkLabel(Window.Location.getHost(), r.getApp().getGalleryAppId());
            Ode.getInstance().getGalleryService().sendEmail(
                Ode.getInstance().getUser().getUserId(), r.getOffender().getUserId(),
                r.getOffender().getUserEmail(), MESSAGES.moderationAppReactivatedTitle(),
                emailBody, emailCallback);
          }
        }
    });

    if(r.getResolved()){                                                //report was unresolved, now resolved
      rw.markAsResolvedButton.setText(MESSAGES.labelmarkAsUnresolved());//revert button
      rw.appResolved = true;
    }else{                                                              //report was resolved, now unresolved
      rw.markAsResolvedButton.setText(MESSAGES.labelmarkAsResolved());  //revert button
      rw.appResolved = false;
    }
    rw.markAsResolvedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
          // failure message
          MESSAGES.galleryError()) {
            @Override
            public void onSuccess(Boolean success) {
              if(success){
                if(r.getResolved()){//current status was resolved
                  r.setResolved(false);
                  rw.markAsResolvedButton.setText(MESSAGES.labelmarkAsResolved());//revert button
                  rw.appResolved = false;
                  storeModerationAction(r.getReportId(), r.getApp().getGalleryAppId(), GalleryModerationAction.NOTAVAILABLE,
                      GalleryModerationAction.MARKASUNRESOLVED, null);
                }else{//current status was unResolved
                  r.setResolved(true);
                  rw.markAsResolvedButton.setText(MESSAGES.labelmarkAsUnresolved());//revert button
                  rw.appResolved = true;
                  storeModerationAction(r.getReportId(), r.getApp().getGalleryAppId(), GalleryModerationAction.NOTAVAILABLE,
                      GalleryModerationAction.MARKASRESOLVED, null);
                }
                if(checkBox.getValue() == false){//only unresolved reports, remove directly.
                  onReportRemoved(r);
                }
              }
            }
          };
        Ode.getInstance().getGalleryService().markReportAsResolved(r.getReportId(), r.getApp().getGalleryAppId(), callback);
      }
    });

    rw.seeAllActions.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        //show actions
        seeAllActionsPopup(r);
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
  /**
   * Method when added gallery app report
   * @param report GalleryAppReport galleryapp report
   */
  public void onReportAdded(GalleryAppReport report) {
    reports.add(report);
    ReportWidgets.put(report, new ReportWidgets(report));
    refreshTable(new GalleryReportListResult(reports, reports.size()), true);
  }
  /**
   * Method when removed gallery app report
   * @param report GalleryAppReport galleryapp report
   */
  public void onReportRemoved(GalleryAppReport report) {
    reports.remove(report);
    ReportWidgets.remove(report);
    refreshTable(new GalleryReportListResult(reports, reports.size()), true);
    selectedGalleryAppReports.remove(report);
  }
  /**
   * Helper method of creating a sending email popup
   * @param report
   */
  private void sendEmailPopup(final GalleryAppReport report){
      // Create a PopUpPanel with a button to close it
      final PopupPanel popup = new PopupPanel(true);
      popup.setStyleName("ode-InboxContainer");
      final FlowPanel content = new FlowPanel();
      content.addStyleName("ode-Inbox");
      Label title = new Label(MESSAGES.emailSendTitle());
      title.addStyleName("InboxTitle");
      content.add(title);

      Button closeButton = new Button(MESSAGES.symbolX());
      closeButton.addStyleName("CloseButton");
      closeButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      content.add(closeButton);

      final FlowPanel emailPanel = new FlowPanel();
      emailPanel.addStyleName("app-actions");
      final Label sentFrom = new Label(MESSAGES.emailSentFrom());
      final Label sentTo = new Label(MESSAGES.emailSentTo() + report.getOffender().getUserName());
      final TextArea emailBodyText = new TextArea();
      emailBodyText.addStyleName("action-textarea");
      final Button sendEmail = new Button(MESSAGES.buttonSendEmail());
      sendEmail.addStyleName("action-button");

      // Account Drop Down Button
      List<DropDownItem> templateItems = Lists.newArrayList();
      // Email Template 1
      templateItems.add(new DropDownItem("template1", MESSAGES.inappropriateAppContentRemoveTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT_REMOVE, report.getApp().getTitle())));
      templateItems.add(new DropDownItem("template2", MESSAGES.inappropriateAppContentTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT, report.getApp().getTitle())));
      templateItems.add(new DropDownItem("template3", MESSAGES.inappropriateUserProfileContentTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_USER_PROFILE_CONTENT, null)));

      templateButton = new DropDownButton("template", MESSAGES.labelChooseTemplate(), templateItems, true);
      templateButton.setStyleName("ode-TopPanelButton");

      new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT, report.getApp().getTitle()).execute();

      emailPanel.add(templateButton);
      emailPanel.add(sentFrom);
      emailPanel.add(sentTo);
      emailPanel.add(emailBodyText);
      emailPanel.add(sendEmail);

      content.add(emailPanel);
      popup.setWidget(content);
      // Center and show the popup
      popup.center();

      final User currentUser = Ode.getInstance().getUser();
      sentFrom.setText(MESSAGES.emailSentFrom() + currentUser.getUserName());
      sendEmail.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          final OdeAsyncCallback<Long> emailCallBack = new OdeAsyncCallback<Long>(
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(final Long emailId) {
                if(emailId == Email.NOTRECORDED){
                  Window.alert(MESSAGES.moderationErrorFailToSendEmail());
                  popup.hide();
                }else{
                  popup.hide();
                  storeModerationAction(report.getReportId(), report.getApp().getGalleryAppId(), emailId,
                      GalleryModerationAction.SENDEMAIL, getEmailPreview(emailBodyText.getText()));
                }
              }
            };
            String emailBody = emailBodyText.getText() + MESSAGES.galleryVisitGalleryAppLinkLabel(Window.Location.getHost(),
                report.getApp().getGalleryAppId());
            Ode.getInstance().getGalleryService().sendEmail(
                currentUser.getUserId(), report.getOffender().getUserId(),
                report.getOffender().getUserEmail(), MESSAGES.moderationSendEmailTitle(),
                emailBody, emailCallBack);
        }
      });
  }
  /**
   * Helper method for deactivating App Popup
   * @param report GalleryAppReport Gallery App Report
   * @param rw ReportWidgets Report Widgets
   */
  private void deactivateAppPopup(final GalleryAppReport report, final ReportWidgets rw){
      // Create a PopUpPanel with a button to close it
      final PopupPanel popup = new PopupPanel(true);
      popup.setStyleName("ode-InboxContainer");
      final FlowPanel content = new FlowPanel();
      content.addStyleName("ode-Inbox");
      Label title = new Label(MESSAGES.emailSendTitle());
      title.addStyleName("InboxTitle");
      content.add(title);

      Button closeButton = new Button(MESSAGES.symbolX());
      closeButton.addStyleName("CloseButton");
      closeButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          popup.hide();
        }
      });
      content.add(closeButton);

      final FlowPanel emailPanel = new FlowPanel();
      emailPanel.addStyleName("app-actions");
      final Label sentFrom = new Label(MESSAGES.emailSentFrom());
      final Label sentTo = new Label(MESSAGES.emailSentTo() + report.getOffender().getUserName());
      final TextArea emailBodyText = new TextArea();
      emailBodyText.addStyleName("action-textarea");
      final Button sendEmailAndDeactivateApp = new Button(MESSAGES.labelDeactivateAppAndSendEmail());
      sendEmailAndDeactivateApp.addStyleName("action-button");
      final Button cancel = new Button(MESSAGES.labelCancel());
      cancel.addStyleName("action-button");

      // Account Drop Down Button
      List<DropDownItem> templateItems = Lists.newArrayList();
      // Email Template 1
      templateItems.add(new DropDownItem("template1", MESSAGES.inappropriateAppContentRemoveTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT_REMOVE, report.getApp().getTitle())));
      templateItems.add(new DropDownItem("template2", MESSAGES.inappropriateAppContentTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT, report.getApp().getTitle())));
      templateItems.add(new DropDownItem("template3", MESSAGES.inappropriateUserProfileContentTitle(), new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_USER_PROFILE_CONTENT, null)));
      templateButton = new DropDownButton("template", MESSAGES.labelChooseTemplate(), templateItems, true);
      templateButton.setStyleName("ode-TopPanelButton");

      // automatically choose first template
      new TemplateAction(emailBodyText, EMAIL_INAPPROPRIATE_APP_CONTENT_REMOVE, report.getApp().getTitle()).execute();

      emailPanel.add(templateButton);
      emailPanel.add(sentFrom);
      emailPanel.add(sentTo);
      emailPanel.add(emailBodyText);
      emailPanel.add(sendEmailAndDeactivateApp);
      emailPanel.add(cancel);

      content.add(emailPanel);
      popup.setWidget(content);
      // Center and show the popup
      popup.center();

      cancel.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            popup.hide();
          }
      });

      final User currentUser = Ode.getInstance().getUser();
      sentFrom.setText(MESSAGES.emailSentFrom() + currentUser.getUserName());
      sendEmailAndDeactivateApp.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          final OdeAsyncCallback<Long> emailCallback = new OdeAsyncCallback<Long>(
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(final Long emailId) {
                if(emailId == Email.NOTRECORDED){
                  Window.alert(MESSAGES.moderationErrorFailToSendEmail());
                  popup.hide();
                }else{
                  popup.hide();
                  final OdeAsyncCallback<Boolean> callback = new OdeAsyncCallback<Boolean>(
                      // failure message
                      MESSAGES.galleryError()) {
                        @Override
                          public void onSuccess(Boolean success) {
                            if(!success)
                              return;
                            if(rw.appActive == true){                                     //app was active, now is deactive
                              rw.deactiveAppButton.setText(MESSAGES.labelReactivateApp());//revert button
                              rw.appActive = false;
                              storeModerationAction(report.getReportId(), report.getApp().getGalleryAppId(), emailId,
                                  GalleryModerationAction.DEACTIVATEAPP, getEmailPreview(emailBodyText.getText()));
                            }else{                                                        //app was deactive, now is active
                              /*This should not be reached, just in case*/
                              rw.deactiveAppButton.setText(MESSAGES.labelDeactivateApp());//revert button
                              rw.appActive = true;
                              storeModerationAction(report.getReportId(), report.getApp().getGalleryAppId(), emailId,
                                  GalleryModerationAction.REACTIVATEAPP, getEmailPreview(emailBodyText.getText()));
                            }
                            //update gallery list
                            galleryClient.appWasChanged();
                          }
                       };
                    Ode.getInstance().getGalleryService().deactivateGalleryApp(report.getApp().getGalleryAppId(), callback);
                }
              }
            };
            String emailBody = emailBodyText.getText() + MESSAGES.galleryVisitGalleryAppLinkLabel(Window.Location.getHost(),
                report.getApp().getGalleryAppId());
            Ode.getInstance().getGalleryService().sendEmail(currentUser.getUserId(),
                      report.getOffender().getUserId(),
                      report.getOffender().getUserEmail(), MESSAGES.moderationAppDeactivatedTitle(), emailBody,
                      emailCallback);
        }
      });
    }

  /**
   * Helper method of creating popup window to show all associated moderation actions.
   * @param report GalleryAppReport gallery app report
   */
  private void seeAllActionsPopup(GalleryAppReport report){
    // Create a PopUpPanel with a button to close it
    final PopupPanel popup = new PopupPanel(true);
    popup.setStyleName("ode-InboxContainer");
    final FlowPanel content = new FlowPanel();
    content.addStyleName("ode-Inbox");
    Label title = new Label(MESSAGES.titleSeeAllActionsPopup());
    title.addStyleName("InboxTitle");
    content.add(title);

    Button closeButton = new Button(MESSAGES.symbolX());
    closeButton.addStyleName("CloseButton");
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        popup.hide();
      }
    });
    content.add(closeButton);

    final FlowPanel actionPanel = new FlowPanel();
    actionPanel.addStyleName("app-actions");

    final OdeAsyncCallback<List<GalleryModerationAction>> callback = new OdeAsyncCallback<List<GalleryModerationAction>>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
          public void onSuccess(List<GalleryModerationAction> moderationActions) {
            for(final GalleryModerationAction moderationAction : moderationActions){
              FlowPanel record = new FlowPanel();
              Label time = new Label();
              Date createdDate = new Date(moderationAction.getDate());
              DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy/MM/dd HH:mm:ss");
              time.setText(dateFormat.format(createdDate));
              time.addStyleName("time-label");
              record.add(time);
              Label moderatorLabel = new Label();
              moderatorLabel.setText(moderationAction.getModeratorName());
              moderatorLabel.addStyleName("moderator-link");
              moderatorLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  Ode.getInstance().switchToUserProfileView(moderationAction.getModeratorId(), 1 /* 1 for public view*/ );
                  popup.hide();
                }
              });
              record.add(moderatorLabel);
              final Label actionLabel = new Label();
              actionLabel.addStyleName("inline-label");
              record.add(actionLabel);
              int actionType= moderationAction.getActonType();
              switch(actionType){
                case GalleryModerationAction.SENDEMAIL:
                  actionLabel.setText(MESSAGES.moderationActionSendAnEmail());
                  createEmailCollapse(record, moderationAction.getMesaageId(),  moderationAction.getEmailPreview());
                  break;
                case GalleryModerationAction.DEACTIVATEAPP:
                  actionLabel.setText(MESSAGES.moderationActionDeactivateThisAppWithEmail());
                  createEmailCollapse(record, moderationAction.getMesaageId(),  moderationAction.getEmailPreview());
                  break;
                case GalleryModerationAction.REACTIVATEAPP:
                  actionLabel.setText(MESSAGES.moderationActionReactivateThisApp());
                  break;
                case GalleryModerationAction.MARKASRESOLVED:
                  actionLabel.setText(MESSAGES.moderationActionMarkThisReportAsResolved());
                  break;
                case GalleryModerationAction.MARKASUNRESOLVED:
                  actionLabel.setText(MESSAGES.moderationActionMarkThisReportAsUnresolved());
                  break;
                default:
                  break;
              }
              actionPanel.add(record);
            }
          }
       };
    Ode.getInstance().getGalleryService().getModerationActions(report.getReportId(), callback);

    content.add(actionPanel);
    popup.setWidget(content);
    // Center and show the popup
    popup.center();
  }
  /**
   * Helper class for email template action
   * Choose Email Template based on given type
   */
  private class TemplateAction implements Command {
    TextArea emailText;
    int type;
    String customText;
    /**
     *
     * @param emailText emamil textArea UI
     * @param type default email type
     * @param customText moderator custom text
     */
    TemplateAction(TextArea emailText, int type, String customText){
      this.emailText = emailText;
      this.type = type;
      this.customText = customText;
    }
    @Override
    public void execute() {
      if(type == EMAIL_INAPPROPRIATE_APP_CONTENT_REMOVE){
        emailText.setText(MESSAGES.inappropriateAppContentRemoveEmail(customText));
        templateButton.setCaption(MESSAGES.inappropriateAppContentRemoveTitle());
      }else if(type == EMAIL_INAPPROPRIATE_APP_CONTENT){
         emailText.setText(MESSAGES.inappropriateAppContentEmail(customText));
        templateButton.setCaption(MESSAGES.inappropriateAppContentTitle());
      }else if(type == EMAIL_INAPPROPRIATE_USER_PROFILE_CONTENT){
        emailText.setText(MESSAGES.inappropriateUserProfileContentEmail());
        templateButton.setCaption(MESSAGES.inappropriateUserProfileContentTitle());
      }
    }
  }
  /**
   * Store Moderation Action into database
   * @param reportId report id
   * @param galleryId gallery id
   * @param emailId email id
   * @param actionType action type
   * @param emailPreview email preview
   */
  void storeModerationAction(final long reportId, final long galleryId, final long emailId, final int actionType, final String emailPreview){
    final User currentUser = Ode.getInstance().getUser();
    final OdeAsyncCallback<Void> moderationActionCallback = new OdeAsyncCallback<Void>(
      // failure message
      MESSAGES.galleryError()) {
        @Override
        public void onSuccess(Void result) {

        }
    };
    Ode.getInstance().getGalleryService().storeModerationAction(reportId, galleryId, emailId, currentUser.getUserId(),
        actionType, currentUser.getUserName(), emailPreview, moderationActionCallback);
  }
  /**
   * Help method for Email Collapse Function
   * When the button(see more) is clicked, it will retrieve the whole email from database.
   * @param parent the parent container
   * @param emailId email id
   * @param preview email preview
   */
  void createEmailCollapse(final FlowPanel parent, final long emailId, final String preview){
    final Label emailContent = new Label();
    emailContent.setText(preview);
    emailContent.addStyleName("inline-label");
    parent.add(emailContent);
    final Label actionButton = new Label();
    actionButton.setText(MESSAGES.seeMoreLink());
    actionButton.addStyleName("seemore-link");
    parent.add(actionButton);
    if(preview.length() <= MAX_EMAIL_PREVIEW_LENGTH){
      actionButton.setVisible(false);
    }
    actionButton.addClickHandler(new ClickHandler() {
      boolean ifPreview = true;
      @Override
      public void onClick(ClickEvent event) {
        if(ifPreview == true){
          OdeAsyncCallback<Email> callback = new OdeAsyncCallback<Email>(
              // failure message
              MESSAGES.serverUnavailable()) {
                @Override
                public void onSuccess(final Email email) {
                  emailContent.setText(email.getBody());
                  emailContent.addStyleName("inline");
                  actionButton.setText(MESSAGES.hideLink());
                  ifPreview = false;
                }
              };
          Ode.getInstance().getGalleryService().getEmail(emailId, callback);
        }else{
          emailContent.setText(preview);
          actionButton.setText(MESSAGES.seeMoreLink());
          ifPreview = true;
        }
      }
    });
  }
  /**
   * prune the email based on MAX_EMAIL_PREVIEW_LENGTH.
   * If the email is longer than MAX_EMAIL_PREVIEW_LENGTH,
   * The rest of it will save as "..."
   * @param email the origin email body
   * @return an email preview
   */
  String getEmailPreview(String email){
    if(email != null && email.length() > MAX_EMAIL_PREVIEW_LENGTH){
      return email.substring(0, MAX_EMAIL_PREVIEW_LENGTH) + MESSAGES.moderationDotDotDot();
    }else{
      return email;
    }
  }
}
