package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.logging.Logger;

public class GalleryAppReport implements IsSerializable{

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryAppReport() {
  }


  public GalleryAppReport(long appId, String userId, String report,long timeStamp ) {
    super();
    this.appId = appId;
    this.timeStamp = timeStamp;
    this.report = report;
    this.userId = userId;


  }

  public long getAppId() {
    return appId;
  }

  public void setAppID(long appId) {
    this.appId = appId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getReport() {
    return report;
  }
  public void setReport(String report) {
    this.report = report;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String name) {
    this.userName = name;
  }

  // Here are the data members
  private long appId;
  private long timeStamp;
  private String report;
  private String userId;
  private String userName;

  @Override
  public String toString() {
    return report;
  }

}
