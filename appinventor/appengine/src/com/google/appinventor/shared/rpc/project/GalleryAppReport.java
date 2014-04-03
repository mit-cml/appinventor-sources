package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.logging.Logger;
import com.google.appinventor.shared.rpc.user.User;

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



  public GalleryAppReport(long reportId, String reportText, GalleryApp app, User offender, User reporter, long timeStamp, boolean resolved ) {
    super();
    this.reportId=reportId;
    this.reportText = reportText;
    this.app = app;
    this.offender=offender;
    this.reporter=reporter;
    this.timeStamp = timeStamp;
    this.resolved = resolved;

  }

  public long getReportId() {
    return reportId;
  }

  public void setReportID(long reportId) {
    this.reportId = reportId;
  }

  public GalleryApp getApp() {
    return app;
  }

  public void setApp(GalleryApp app) {
    this.app = app;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getReportText() {
    return reportText;
  }
  public void setReportText(String reportText) {
    this.reportText = reportText;
  }

  public User getOffender() {
    return offender;
  }

  public void setOffender(User offender) {
    this.offender = offender;
  }
  public User getReporter() {
    return reporter;
  }

  public void setReporter(User reporter) {
    this.reporter = reporter;
  }

  public User getResolved() {
    return reporter;
  }

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
