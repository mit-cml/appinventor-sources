package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;



import java.util.logging.Logger;

/**
 * GallerySettings is the shared class holding gallery settings (enabled, bucket)
 */
public class GallerySettings implements IsSerializable {

  private String APPSDIR="/gallery/apps/";
  private String APPSDIRFIXED="gallery/apps/";
  private String USERDIR="/user/";
  private String GALLERYPROJECTDIR="/gallery/projects/"; // we put images here pre-publish
  private String GALLERYPROJECTDIRFIXED="gallery/projects/"; // we put images here pre-publish
  private String SOURCEDIR="/aia";
  private String IMAGEDIR="/image";
  private String GCSSERVERURLSTART= "/gs/";
  private String GCSCLIENTURLSTART="http://storage.googleapis.com/";

  private boolean enabled;
  private String bucket;

  public GallerySettings() {
    this.enabled=false;
    this.bucket="";
  }

  public GallerySettings(boolean enabled, String bucket) {
    this.enabled = enabled;
    this.bucket = bucket;
  }

  @Override
  public String toString() {
    return this.enabled + ","+this.bucket;
  }

  public String getBucket() {
    return bucket;
  }

  public boolean galleryEnabled() {
    return enabled;
  }

  public String getSourceURL(long galleryId) {
    String url = GCSSERVERURLSTART + getBucket() +  "/" + getSourceKey(galleryId);
    return url;
  }

  public String getSourceKey(long galleryId) {
    String key = APPSDIRFIXED + galleryId + SOURCEDIR;
    return key;
  }

  // should be of form: http://storage.googleapis.com/galleryai2/gallery/apps/4796462844084224/image
  public String getCloudImageURL(long galleryId) {
    String url = GCSCLIENTURLSTART + getBucket() +
        APPSDIR + galleryId + IMAGEDIR;
    return url;
  }
  public String getImageURL(long galleryId) {
    return GCSSERVERURLSTART + getBucket()  +getImageKey(galleryId);
  }

  public String getImageKey(long galleryId) {
    return APPSDIRFIXED + galleryId + IMAGEDIR;
  }

  public String getProjectImageURL(long projectId) {
    String url = GCSCLIENTURLSTART+ getBucket() +
        GALLERYPROJECTDIR+ projectId + IMAGEDIR;
    return url;
  }

  public String getProjectImagePath(long projectId) {
    String url = GCSSERVERURLSTART + this.getBucket() + GALLERYPROJECTDIR
       + projectId + "/image";
    return url;
  }

  public String getProjectImageKey(long projectId) {
    String url = GALLERYPROJECTDIRFIXED + projectId + IMAGEDIR;
    return url;
  }
  // http://storage.googleapis.com/gallerai2/user/xxxx/image
  public String getUserImageURL(String userid) {
    String url = GCSCLIENTURLSTART + getBucket() + USERDIR +
          userid + IMAGEDIR;
    return url;
  }

}