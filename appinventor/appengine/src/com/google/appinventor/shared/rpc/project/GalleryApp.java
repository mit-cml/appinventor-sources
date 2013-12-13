package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GalleryApp implements IsSerializable {
  /**
   * Default constructor. This constructor is required by GWT.
   */

  public GalleryApp() {
    this.title="no title";
    this.downloads=0;
    this.views=0;
    this.likes=0;
    this.comments=0;
    this.projectId=0L;
    this.developerName="fred";
    this.creationDate = 0L;
    this.updateDate=0L;
    this.imageBlobId="SFKJF";
    this.imageURL="http://smalltowngeeks.net/wp-content/uploads/2011/02/appInv1-300x227.png";
  }

  public static final String GALLERYURL = "http://gallery.appinventor.mit.edu/rpc?";
	
  public GalleryApp(String title, String developerName, String description,
      long creationDate, long updateDate, String imageURL, String sourceFileName,
      int downloads, int views, int likes, int comments, 
      String imageBlobId, String sourceBlobId, String galleryAppId, 
      ArrayList<String> tags) {
    super();
    this.title = title;
    this.developerName = developerName;
    this.description = description;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageURL = imageURL;
    // the source file name we get from gallery can have some bad characters in it...
    // e.g., name  (2).zip. We need to cleanse this and probably deal with url as
    //   well.

    if (sourceFileName.contains(".")) {
      String[] splitName = sourceFileName.split("\\.");
      projectName = splitName[0];
    } else {
      projectName = sourceFileName;
    }
    this.downloads = downloads;
    this.views = views;
    this.likes = likes;
    this.comments = comments;
    this.imageBlobId= imageBlobId;
    this.sourceBlobId= sourceBlobId;
    this.galleryAppId= galleryAppId;
    this.tags = tags;
  }
  /* this constructor is called when we are creating a new gallery app but don't have
     the stuff yet */
  public GalleryApp(String title, long projectId) {
	super();
    this.title = title;
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.comments = 0;
    this.projectId = projectId;
    this.developerName = "Test user";
    this.creationDate = 0L;
    this.updateDate = 0L;
    this.imageBlobId = "SFKJF";
    this.imageURL = "http://smalltowngeeks.net/wp-content/uploads/2011/02/appInv1-300x227.png";
  }

  private String title;
  private String developerName;
  private String description;
  private long creationDate;
  private long updateDate;

  private String projectName;
  private String imageURL;
  private int downloads;
  private int views;
  private int likes;
  private int comments;
  private String imageBlobId;
  private String sourceBlobId;
  private String galleryAppId;
  private ArrayList<String> tags;
  private long projectId;  // when we edit a newly published app, we need the ai proj id.

  public long getProjectId() {
    return projectId;
  }
  public void setProjectId(long projectId) {
    this.projectId=projectId;
  }

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDeveloperName() {
    return developerName;
  }
  public void setDeveloperName(String developerName) {
    this.developerName = developerName;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public long getCreationDate() {
    return this.creationDate; 
  }
  public void setCreationDate(long creationDate) {
    this.creationDate = creationDate;
  }
  public long getUpdateDate() {
    return this.updateDate;
  }
  public void setUpdateDate(long updateDate) {
    this.updateDate = updateDate;
  }
  public String getImageURL() {
    return imageURL;
  }
  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }
  public String getProjectName() {
    return projectName;
  }
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }
  public int getDownloads() {
    return downloads;
  }
  public void setDownloads(int downloads) {
    this.downloads = downloads;
  }
  public int getViews() {
    return views;
  }
  public void setViews(int views) {
    this.views = views;
  }
  public int getLikes() {
    return likes;
  }
  public void setLikes(int likes) {
    this.likes = likes;
  }
  public int getComments() {
    return comments;
  }
  public void setComments(int comments) {
    this.comments = comments;
  }
  public void setImageBlobId(String imageBlobId) {
    this.imageBlobId = imageBlobId;
  }
  public String getImageBlobId() {
    return this.imageBlobId;
  }
  public void setSourceBlobId(String sourceBlobId) {
    this.sourceBlobId = sourceBlobId;
  }
  public String getSourceBlobId() {
    return this.sourceBlobId;
  }
  public void setGalleryAppId(String galleryAppId) {
    this.galleryAppId = galleryAppId;
  }
  public String getGalleryAppId() {
    return this.galleryAppId;
  }
  
  // url is of form:
  //   gallery.appinventor.mit.edu/rpc?getblob=<sourceBlob>:<appid>
  // http://usf-appinventor-gallery.appspot.com/rpc?getblob=AMIfv96uvxoFUHj_Tsv671z66_Iu9HCsUgGad4_py4oWu2INlFgtvW6M5lUPKZwjBAT6Pi_-31MYIGF2aNji_qGZFxTwHH5ryPToMPumbajW0_I4Pf9XY2INsR-o7h_1z8jou1Ey9dS2ES1KjicqOebmCLMYKRrU5tAANrjTj1Bn3n0uipbWvsQ:48002
  public String getSourceURL() {
     return GALLERYURL+"getblob="+getSourceBlobId()+":"+getGalleryAppId();
  }

  public ArrayList<String> getTags() {
    return this.tags;
  }
  public void setTags(ArrayList<String> tags) {
    this.tags = tags;
  }
  
  @Override
  public String toString() {
    return title + " ||| " + description + " ||| " +  imageURL; 
  }
}