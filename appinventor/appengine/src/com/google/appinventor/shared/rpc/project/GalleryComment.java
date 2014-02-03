package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.logging.Logger;
/**
 * GalleryComment is a shared object. The db puts comments in it and slings it back
 * to the view
 */
public class GalleryComment implements IsSerializable{
	
  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryComment() {
  }

  /**
   * This constructor was used in the old system, in conjunction with
   * the old gallery api. I believe we can abolish this.
   */
  public GalleryComment(long appId, long timeStamp, String comment,
      int numCurFlags, String userId, String treeId, int numChildren) {
    super();
    this.appId = appId;
    this.timeStamp = timeStamp;
    this.comment = comment;
    this.numCurFlags = numCurFlags;
    this.userId = userId;
    this.treeId = treeId;
    this.numChildren = numChildren;
  }

  public GalleryComment(long appId, String userId, String comment,long timeStamp ) {
    super();
    this.appId = appId;
    this.timeStamp = timeStamp;
    this.comment = comment;
    this.userId = userId;
    this.numCurFlags = 0;
    this.treeId = "treeId";
    this.numChildren = 0;
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

  public String getComment() {
    return comment;
  }
  public void setText(String comment) {
    this.comment = comment;
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


  public int getNumCurFlags() {
    return numCurFlags;
  }

  public void setNumCurFlags(int numCurFlags) {
    this.numCurFlags = numCurFlags;
  }

// we are implementing a flat comment list to start. If we add replies we may want
// some tree structure as below (this came from old gallery api client)
 
  public String getTreeId() {
    return treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  public int getNumChildren() {
    return numChildren;
  }

  public void setNumChildren(int numChildren) {
    this.numChildren = numChildren;
  }
  
  // Here are the data members
  private long appId;
  private long timeStamp;
  private String comment;
  private String userId;
  private String userName;
  private int numCurFlags;

  private String treeId;
  private int numChildren;
  	
	
  @Override
  public String toString() {
    return comment; 
  }

}
