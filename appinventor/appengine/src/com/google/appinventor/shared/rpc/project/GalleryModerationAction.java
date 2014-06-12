package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * GalleryModerationAction is the shared class holding moderation action info. It is used by server to
 * hold moderation action returned from database
 */
public class GalleryModerationAction implements IsSerializable {
  public final static long NOTAVAILABLE = -1L;

  private long messageId;
  private long reportId;
  private long galleryId;
  private String moderatorId;
  private int actionType;

  private String moderatorName;
  private String messagePreview;

  public final static int SENDMESSAGE = 1;
  public final static int DEACTIVATEAPP = 2;
  public final static int REACTIVATEAPP = 3;
  public final static int MARKASRESOLVED = 4;
  public final static int MARKASUNRESOLVED = 5;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  public GalleryModerationAction() {

  }

  public GalleryModerationAction(long reportId, long galleryId, long messageId, String moderatorId, int actionType, String moderatorName, String messagePreview) {
    this.reportId = reportId;
    this.galleryId = galleryId;
    this.messageId = messageId;
    this.moderatorId = moderatorId;
    this.actionType = actionType;
    this.moderatorName = moderatorName;
    this.messagePreview = messagePreview;
  }

  public void setReportId(long reportId){
    this.reportId = reportId;
  }

  public long getReportId(){
    return reportId;
  }

  public void setGalleryId(long galleryId){
    this.galleryId = galleryId;
  }

  public long getGalleryId(){
    return galleryId;
  }

  public void setMesaageId(long messageId){
    this.messageId = messageId;
  }

  public long getMesaageId(){
    return messageId;
  }

  public void setModeratorId(String moderatorId){
    this.moderatorId = moderatorId;
  }

  public String getModeratorId(){
    return moderatorId;
  }

  public void setActionType(int actionType){
    this.actionType = actionType;
  }

  public int getActonType(){
    return actionType;
  }

  public void setModeratorName(String moderatorName){
    this.moderatorName = moderatorName;
  }

  public String getModeratorName(){
    return moderatorName;
  }

  public void setMessagePreview(String messagePreview){
    this.messagePreview = messagePreview;
  }

  public String getMessagePreview(){
    return messagePreview;
  }
}