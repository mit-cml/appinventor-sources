// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.user.User;
import com.google.gwt.user.client.rpc.IsSerializable;

public class GalleryAppReport implements IsSerializable{
  private long appId;
  private long timeStamp;
  private String report;
  private String userId;
  private String userName;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryAppReport() {

  }

  /**
   * constructor of GalleryAppReport
   * @param reportId report id
   * @param reportText report text
   * @param app gallery app
   * @param offender offender of the report
   * @param reporter reporter of the report
   * @param timeStamp time
   * @param resolved wheter or not this report is resolved
   */
  public GalleryAppReport(long reportId, String reportText, GalleryApp app, User offender, User reporter, long timeStamp, boolean resolved) {
    super();
    this.reportId=reportId;
    this.reportText = reportText;
    this.app = app;
    this.offender=offender;
    this.reporter=reporter;
    this.timeStamp = timeStamp;
    this.resolved = resolved;
  }

  /**
   * @return reportId report id
   */
  public long getReportId() {
    return reportId;
  }

  /**
   * set report id based on given reportId
   * @param reportId report id
   */
  public void setReportID(long reportId) {
    this.reportId = reportId;
  }

  /**
   * @return app galleryapp
   */
  public GalleryApp getApp() {
    return app;
  }

  /**
   * set app as given gallery app
   * @param app
   */
  public void setApp(GalleryApp app) {
    this.app = app;
  }

  /**
   * @return timeStamp time stamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  /**
   * set time stamp as given timestamp
   * @param timeStamp
   */
  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * @return reportText get report text
   */
  public String getReportText() {
    return reportText;
  }

  /**
   * set report text as given report text
   * @param reportText
   */
  public void setReportText(String reportText) {
    this.reportText = reportText;
  }

  /**
   * @return offender of the report
   */
  public User getOffender() {
    return offender;
  }

  /**
   * set offender as given offender
   * @param offender
   */
  public void setOffender(User offender) {
    this.offender = offender;
  }

  /**
   * @return reporter of the report
   */
  public User getReporter() {
    return reporter;
  }

  /**
   * set reporter as given reporter
   * @param reporter
   */
  public void setReporter(User reporter) {
    this.reporter = reporter;
  }

  /**
   * check if report is resolved
   * @return
   */
  public boolean getResolved() {
    return resolved;
  }

  /**
   * set status of report as given boolean
   * @param resolved
   */
  public void setResolved(boolean resolved) {
    this.resolved = resolved;
  }

  // Here are the data members
  private long reportId;
  private GalleryApp app;
  private String reportText;
  private User offender;
  private User reporter;
  private boolean resolved;

  @Override
  public String toString() {
    return reportText;
  }

}
