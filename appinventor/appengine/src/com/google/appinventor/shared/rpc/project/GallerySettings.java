// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GallerySettings is the shared class holding gallery settings (enabled, bucket)
 */
public class GallerySettings implements IsSerializable {

  private String APPSDIR="/gallery/apps/";
  private String APPSDIRFIXED="gallery/apps/";
  private String USERDIR="/user/";
  private String USERDIRFIXED="user/";
  private String GALLERYPROJECTDIR="/gallery/projects/"; // we put images here pre-publish
  private String GALLERYPROJECTDIRFIXED="gallery/projects/"; // we put images here pre-publish
  private String SOURCEDIR="/aia";
  private String IMAGEDIR="/image";
  private String GCSSERVERURLSTART= "/gs/";
  private String GCSCLIENTURLSTARTDEVSERVER="/gs/";
  private String GCSCLIENTURLSTART="http://storage.googleapis.com/";

  private boolean enabled;
  private String bucket;
  private String environment;
  private String adminEmail;

  /**
   * default constructor
   */
  public GallerySettings() {
    this.enabled = false;
    this.bucket = "";
    this.environment = "";
    this.adminEmail = "";
  }

  /**
   * constructor with given parameters
   * @param enabled
   * @param bucket
   */
  public GallerySettings(boolean enabled, String bucket, String environment, String adminEmail) {
    this.enabled = enabled;
    this.bucket = bucket;
    this.environment = environment;
    this.adminEmail = adminEmail;
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
   * get the gallery environment
   */
  public String getEnvironment() {
    return environment;
  }

  /**
   * get the gallery admin email
   */
  public String getAdminEmail() {
    return adminEmail;
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
   * get the cloud image url based on given galleryid  (Production Server)
   * should be of form: http://storage.googleapis.com/galleryai2/gallery/apps/4796462844084224/image
   * @param galleryId
   * @return the url of cloud image
   */
  public String getCloudImageURL(long galleryId) {
    String url;
    url = GCSCLIENTURLSTART + getBucket() +
          APPSDIR + galleryId + IMAGEDIR;
    return url;
  }

  /**
   * get the cloud image location based on given galleryid  (Development Server)
   * @param galleryId
   * @return the location of cloud image
   */
  public String getCloudImageLocation(long galleryId) {
    String url;
    url = GCSCLIENTURLSTARTDEVSERVER + getBucket() +
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
   * get the project image url based on given projectId   (Production Server)
   * @param projectId project id
   * @return url of project image
   */
  public String getProjectImageURL(long projectId) {
    String url;
    url = GCSCLIENTURLSTART+ getBucket() +
          GALLERYPROJECTDIR+ projectId + IMAGEDIR;
    return url;
  }

  /**
   * get the project image location based on given projectId (Development Server)
   * @param location location on Development Server
   * @return location of project image
   */
  public String getProjectImageLocation(long projectId) {
    String url;
    url = GCSCLIENTURLSTARTDEVSERVER+ getBucket() +
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

  /**
   * get the user image key based on given userId
   * @param userId user id
   * @return user image key
   */
  public String getUserImageKey(String userId) {
    String url = USERDIRFIXED + userId + IMAGEDIR;
    return url;
  }

  // http://storage.googleapis.com/gallerai2/user/xxxx/image
  /**
   * get the user image url based on given userid   (Production Server)
   * @param userid user id
   * @return url of user image
   */
  public String getUserImageURL(String userid) {
    String url;
    url = GCSCLIENTURLSTART + getBucket() + USERDIR +
          userid + IMAGEDIR;
    return url;
  }

  /**
   * get the user image location based on given userid (Development Server)
   * @param location location of image on development server
   * @return location of user image
   */
  public String getUserImageLocation(String userid) {
    String location;
    location = GCSCLIENTURLSTARTDEVSERVER + getBucket() + USERDIR +
          userid + IMAGEDIR;
    return location;
  }
}