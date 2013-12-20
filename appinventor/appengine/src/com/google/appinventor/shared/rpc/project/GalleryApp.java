package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GalleryApp implements IsSerializable {
  /**
   * Default constructor. This constructor is required by GWT.
   */

  // Dave, are we still using this? -Vincent
  public GalleryApp() {
    this.title = "no title";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.comments = 0;
    this.projectId = 0L;
    this.developerId = "fred";
    this.creationDate = 0L;
    this.updateDate = 0L;
    this.imageBlobId = "SFKJF";
    this.imageURL = "http://smalltowngeeks.net/wp-content/uploads/2011/02/appInv1-300x227.png";
  }

  public static final String GALLERYURL = "http://gallery.appinventor.mit.edu/rpc?";

  public static final String GALLERYBUCKET = "galleryai2";
	
  public GalleryApp(String title, String developerId, String description,
      long creationDate, long updateDate, String imageURL, String projectName,
      int downloads, int views, int likes, int comments, 
      String imageBlobId, String sourceBlobId, long galleryAppId, 
      ArrayList<String> tags) {
    super();
    this.title = title;
    this.developerId = developerId;
    this.description = description;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageURL = imageURL;
    // the source file name we get from gallery can have some bad characters in it...
    // e.g., name  (2).zip. We need to cleanse this and probably deal with url as
    //   well.

    if (projectName.contains(".")) {
      String[] splitName = projectName.split("\\.");
      projectName = splitName[0];
    } else {
      projectName = projectName;
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
  public GalleryApp(String title, long projectId, String projectName, long galleryAppId) {
	super();
    this.title = title;
    this.description = projectName +" description";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.comments = 0;
    this.projectId = projectId;
    this.projectName=projectName;
    this.galleryAppId=galleryAppId;
    this.developerId = "Test user";
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageBlobId = "SFKJF";
    this.imageURL = "http://smalltowngeeks.net/wp-content/uploads/2011/02/appInv1-300x227.png";
  }

  private String title;
  private String developerId;
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
  private long galleryAppId;
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
  public String getDeveloperId() {
    return developerId;
  }
  public void setDeveloperId(String developerId) {
    this.developerId = developerId;
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
  public void setGalleryAppId(long galleryAppId) {
    this.galleryAppId = galleryAppId;
  }
  public long getGalleryAppId() {
    return this.galleryAppId;
  }
  // developerId is the google account name. We need to send the name for the account
  //   or if we add more user info send that...for testing, we're just sending id
  public String getDeveloperName() {
    return developerId;
  }
  /* URL is in GCS. Here is what GCS says:
   *Your object is now visible to App Engine with the file name /gs/my_bucket/my_object. 
   * If you set your object to be publicly accessible, your object can be accessed using the 
   * URL http://storage.googleapis.com/my_bucket/my_object.  
  */
  public String getSourceURL() {
    String url = "/gs/" + this.GALLERYBUCKET + "/" + getGalleryAppId();
    return url;
  }

  public String getCloudImageURL() {
    
    String url = "/gs/" + this.GALLERYBUCKET+"/" + getGalleryAppId()+"/image";
    return url;
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