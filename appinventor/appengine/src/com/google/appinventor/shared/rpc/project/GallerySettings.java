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

  /**
   * default constructor
   */
  public GallerySettings() {
    this.enabled = false;
    this.bucket = "";
  }

  /**
   * constructor with given parameters
   * @param enabled
   * @param bucket
   */
  public GallerySettings(boolean enabled, String bucket) {
    this.enabled = enabled;
    this.bucket = bucket;
  }

  @Override
  public String toString() {
    return this.enabled + ","+this.bucket;
  }

  /**
   * get the gallery bucket
   * @return
   */
  public String getBucket() {
    return bucket;
  }
  /**
   * checck if gallery is enabled
   * @return
   */
  public boolean galleryEnabled() {
    return enabled;
  }

  /**
   * get the source url based on given galleryid
   * @param galleryId gallery id
   * @return url of the source
   */
  public String getSourceURL(long galleryId) {
    String url = GCSSERVERURLSTART + getBucket() +  "/" + getSourceKey(galleryId);
    return url;
  }

  /**
   * get the source key based on given galleryid
   * @param galleryId gallery id
   * @return source key
   */
  public String getSourceKey(long galleryId) {
    String key = APPSDIRFIXED + galleryId + SOURCEDIR;
    return key;
  }

  /**
   * get the cloud image url based on given galleryid
   * should be of form: http://storage.googleapis.com/galleryai2/gallery/apps/4796462844084224/image
   * @param galleryId
   * @return the url of cloud image
   */
  public String getCloudImageURL(long galleryId) {
    String url = GCSCLIENTURLSTART + getBucket() +
        APPSDIR + galleryId + IMAGEDIR;
    return url;
  }

  /**
   * get the image url based on given galleryid
   * @param galleryId gallery id
   * @return image url
   */
  public String getImageURL(long galleryId) {
    return GCSSERVERURLSTART + getBucket()  +getImageKey(galleryId);
  }

  /**
   * get the image key based on given galleryid
   * @param galleryId gallery id
   * @return image key
   */
  public String getImageKey(long galleryId) {
    return APPSDIRFIXED + galleryId + IMAGEDIR;
  }

  /**
   * get the project image url based on given projectId
   * @param projectId project id
   * @return url of project image
   */
  public String getProjectImageURL(long projectId) {
    String url = GCSCLIENTURLSTART+ getBucket() +
        GALLERYPROJECTDIR+ projectId + IMAGEDIR;
    return url;
  }

  /**
   * get the project image path based on given projectId
   * @param projectId project id
   * @return path of the project image
   */
  public String getProjectImagePath(long projectId) {
    String url = GCSSERVERURLSTART + this.getBucket() + GALLERYPROJECTDIR
       + projectId + "/image";
    return url;
  }

  /**
   * get the project image key based on given projectId
   * @param projectId project id
   * @return project image key
   */
  public String getProjectImageKey(long projectId) {
    String url = GALLERYPROJECTDIRFIXED + projectId + IMAGEDIR;
    return url;
  }
  // http://storage.googleapis.com/gallerai2/user/xxxx/image
  /**
   * get the user image url based on given userid
   * @param userid user id
   * @return url of user image
   */
  public String getUserImageURL(String userid) {
    String url = GCSCLIENTURLSTART + getBucket() + USERDIR +
          userid + IMAGEDIR;
    return url;
  }
}