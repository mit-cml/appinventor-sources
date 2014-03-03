package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * GalleryApp is the shared class holding app info. It is used by server to
 * hold apps returned from database, and its also used to hold default info
 * for an app before its published.
 */
public class GalleryApp implements IsSerializable {

  /**
   * Default constructor. This constructor is required by GWT.
   * note: don't think this is being used, can remove code within?
   */
  public GalleryApp() {
    this.title = "no title";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.comments = 0;
    this.projectId = 0L;
    this.attributionId = 0L;
    this.developerId = "fred";
    this.creationDate = 0L;
    this.updateDate = 0L;
  }


  public static final String GALLERYBUCKET = "galleryai2";
  public static final String DEFAULTGALLERYIMAGE="images/genericApp.png";
  public static final String DEFAULTUSERIMAGE="images/android_icon_.png";

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
    // NOTE: i think the following was for old gallery and we can eliminate? Maybe
    // even this entire constructor?
    if (projectName.contains(".")) {
      String[] splitName = projectName.split("\\.");
      projectName = splitName[0];
    }
    this.downloads = downloads;
    this.views = views;
    this.likes = likes;
    this.comments = comments;
    this.imageBlobId = imageBlobId;
    this.sourceBlobId = sourceBlobId;
    this.galleryAppId = galleryAppId;
    this.tags = tags;
  }
  
  /* this constructor is called when we are creating a new gallery app but don't have
     the stuff yet */
  public GalleryApp(String title, long projectId, String projectName, long galleryAppId, long attributionId) {
  super();
    this.title = title;
    this.description = projectName +" description";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.comments = 0;
    this.projectId = projectId;
    this.attributionId = attributionId;
    this.projectName=projectName;
    this.galleryAppId=galleryAppId;

    this.developerId = "none";
    this.developerName= "none";
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageBlobId = "SFKJF";
  }

  private String title;
  private String developerId;
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
  private long galleryAppId;
  private ArrayList<String> tags;
  private long projectId;  // when we edit a newly published app, we need the ai proj id.
  private long attributionId;  // the gallery id which this app inherits from.
                               // it takes from the project which inherits from a gellery app.

  public long getProjectAttributionId() {
    return attributionId;
  }
  public void setProjectAttributionId(long attributionId) {
    this.attributionId=attributionId;
  }

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
  // I believe we can eliminate the following until next comment
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
  // end of eliminate stuff

  public void setGalleryAppId(long galleryAppId) {
    this.galleryAppId = galleryAppId;
  }
  public long getGalleryAppId() {
    return this.galleryAppId;
  }
  public String getDeveloperName() {
    return developerName;
  }
  public void setDeveloperName(String name) {
    this.developerName = name;
  }

  /* URL is in GCS, of form: /gs/galleryai2/gallery/apps/6046115656892416/aia
   * Here is what is in gcs docs:
   * Your object is now visible to App Engine with the file name 
   * /gs/my_bucket/my_object.
   * If you set your object to be publicly accessible, your object can be 
   * accessed using the URL http://storage.googleapis.com/my_bucket/my_object.
  */
  public String getSourceURL() {
    String url = "/gs/" + this.GALLERYBUCKET + "/" + getSourceKey();
    return url;
  }
  public static String getSourceURL(long galleryId) {
    String url = "/gs/" + GALLERYBUCKET + "/" + getSourceKey(galleryId);
    return url;
  }
  public String getSourceKey() {
    String key = "gallery/apps/" + getGalleryAppId() + "/aia";
    return key;
  }
  // this static one is called by galleryService when an app is first published
  static public String getSourceKey(long galleryId) {
    String key = "gallery/apps/" + galleryId + "/aia";
    return key;
  }

  public String getCloudImageURL() {
    String url = "http://storage.googleapis.com/" + this.GALLERYBUCKET + 
        "/gallery/apps/" + getGalleryAppId() + "/image";
    return url;
  }
  public static String getImageURL(long galleryId) {
    return "/gs/" + GALLERYBUCKET + "/" +getImageKey(galleryId);
  }

  public String getImageKey() {
    return "gallery/apps/" + getGalleryAppId() + "/image";
  } 

  public static String getImageKey(long galleryId) {
    return "gallery/apps/" + galleryId + "/image";
  } 

  public String getProjectImageURL() {
    String url = "http://storage.googleapis.com/" + this.GALLERYBUCKET + 
        "/gallery/projects/" + getProjectId() + "/image";
    return url;
  }


  public String getProjectImagePath() {
    String url = "/gs/" + this.GALLERYBUCKET + "/gallery/projects/"
       + getProjectId() + "/image";
    return url;
  }

  public String getProjectImageKey() {
    String url = "gallery/projects/" + getProjectId() + "/image";
    return url;
  }

  static public String getUserImageUrl(String userid) {
    String url = "http://storage.googleapis.com/" + GALLERYBUCKET + "/user/" +
          userid + "/image";
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