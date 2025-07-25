// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import java.util.List;
import com.google.appinventor.shared.rpc.RestJSONObject;
import jsinterop.annotations.JsProperty;

/**
 * Data Transfer Object representing user data.
 *
 */
public class Config extends RestJSONObject implements IsSerializable, Serializable {
  // Unique identifier for the user
  private User user;

  private String rendezvousServer = null;

  private SplashConfig splashConfig;

  private String libraryUrl;
  private String getStartedUrl;
  private String tutorialsUrl;
  private String extensionsUrl;
  private String troubleshootingUrl;
  private String forumsUrl;
  private String feedbackUrl;
  private String releaseNotesUrl;
  private String tosUrl;
  private String logoUrl;
  private String guideUrl;
  private String referenceComponentsUrl;
  private String firebaseURL;   // Default Firebase URL
  private String defaultCloudDBserver;
  private int noop;            // No-op interval
  private boolean secondBuildserver; // Whether or not we have a second
                                     // buildserver (used for a different
                                     // target SDK).
  private boolean galleryEnabled;
  private String galleryLocation;
  private boolean galleryReadOnly;
  private List<String> tutorialUrlAllowed;
  private boolean serverExpired;
  private boolean deleteAccountAllowed;
  private String iosExtensions;
  private String surveyUrl;

  public Config() {
  }

  public Config(Object json) {
    super(json);
    setUser(new User(getUser()));
    setSplashConfig(new SplashConfig(getSplashConfig()));
  }

  @JsProperty
  public User getUser() {
    return this.user;
  }

  @JsProperty
  public void setUser(User user) {
    this.user = user;
  }

  @JsProperty
  public String getRendezvousServer() {
    return this.rendezvousServer; // might be null
  }

  @JsProperty
  public void setRendezvousServer(String value) {
    this.rendezvousServer = value;
  }

  @JsProperty
  public SplashConfig getSplashConfig() {
    return this.splashConfig;
  }

  @JsProperty
  public void setSplashConfig(SplashConfig config) {
    this.splashConfig = config;
  }

  @JsProperty
  public String getLibraryUrl() {
    return libraryUrl;
  }

  @JsProperty
  public void setLibraryUrl(String libraryUrl) {
    this.libraryUrl = libraryUrl;
  }

  @JsProperty
  public String getGetStartedUrl() {
    return getStartedUrl;
  }

  @JsProperty
  public void setGetStartedUrl(String getStartedUrl) {
    this.getStartedUrl = getStartedUrl;
  }

  @JsProperty
  public String getExtensionsUrl() {
    return extensionsUrl;
  }

  @JsProperty
  public String getTutorialsUrl() {
    return tutorialsUrl;
  }

  @JsProperty
  public void setExtensionsUrl(String extensionsUrl) {
    this.extensionsUrl = extensionsUrl;
  }

  @JsProperty
  public void setTutorialsUrl(String tutorialsUrl) {
    this.tutorialsUrl = tutorialsUrl;
  }

  @JsProperty
  public String getTroubleshootingUrl() {
    return troubleshootingUrl;
  }

  @JsProperty
  public void setTroubleshootingUrl(String troubleshootingUrl) {
    this.troubleshootingUrl = troubleshootingUrl;
  }

  @JsProperty
  public String getForumsUrl() {
    return forumsUrl;
  }

  @JsProperty
  public void setForumsUrl(String forumsUrl) {
    this.forumsUrl = forumsUrl;
  }

  @JsProperty
  public String getFeedbackUrl() {
    return feedbackUrl;
  }

  @JsProperty
  public void setFeedbackUrl(String feedbackUrl) {
    this.feedbackUrl = feedbackUrl;
  }

  @JsProperty
  public String getReleaseNotesUrl() {
    return releaseNotesUrl;
  }

  @JsProperty
  public void setReleaseNotesUrl(String releaseNotesUrl) {
    this.releaseNotesUrl = releaseNotesUrl;
  }

  @JsProperty
  public String getTosUrl() {
    return tosUrl;
  }

  @JsProperty
  public void setTosUrl(String tosUrl) {
    this.tosUrl = tosUrl;
  }

  @JsProperty
  public String getLogoUrl() {
    return logoUrl;
  }

  @JsProperty
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  @JsProperty
  public String getGuideUrl() {
    return guideUrl;
  }

  @JsProperty
  public void setGuideUrl(String guideUrl) {
    this.guideUrl = guideUrl;
  }

  @JsProperty
  public String getReferenceComponentsUrl() {
    return referenceComponentsUrl;
  }

  @JsProperty
  public void setReferenceComponentsUrl(String referenceComponentsUrl) {
    this.referenceComponentsUrl = referenceComponentsUrl;
  }

  @JsProperty
  public String getFirebaseURL() {
    return firebaseURL;
  }

  @JsProperty
  public void setFirebaseURL(String url) {
    firebaseURL = url;
  }

  @JsProperty
  public void setDefaultCloudDBserver(String server) {
    defaultCloudDBserver = server;
  }

  @JsProperty
  public String getDefaultCloudDBserver() {
    return defaultCloudDBserver;
  }

  @JsProperty
  public int getNoop() {
    return noop;
  }

  @JsProperty
  public void setNoop(int noop) {
    this.noop = noop;
  }

  @JsProperty
  public boolean getSecondBuildserver() {
    return secondBuildserver;
  }

  @JsProperty
  public void setSecondBuildserver(boolean value) {
    secondBuildserver = value;
  }

  @JsProperty
  public boolean getGalleryEnabled() {
    return galleryEnabled;
  }

  @JsProperty
  public void setGalleryEnabled(boolean value) {
    galleryEnabled = value;
  }

  @JsProperty
  public String getGalleryLocation() {
    return galleryLocation;
  }

  @JsProperty
  public void setGalleryLocation(String value) {
    galleryLocation = value;
  }

  @JsProperty
  public boolean getGalleryReadOnly() {
    return galleryReadOnly;
  }

  @JsProperty
  public void setGalleryReadOnly(boolean value) {
    galleryReadOnly = value;
  }

  @JsProperty
  public void setTutorialUrlAllowed(List<String> value) {
    this.tutorialUrlAllowed = value;
  }

  @JsProperty(name = "tutorialUrlAllowed")
  public List<String> getTutorialsUrlAllowed() {
    return tutorialUrlAllowed;
  }

  @JsProperty
  public boolean getServerExpired() {
    return serverExpired;
  }

  @JsProperty
  public void setServerExpired(boolean value) {
    serverExpired = value;
  }

  @JsProperty
  public boolean getDeleteAccountAllowed() {
    return deleteAccountAllowed;
  }

  @JsProperty
  public void setDeleteAccountAllowed(boolean value) {
    deleteAccountAllowed = value;
  }

  @JsProperty
  public String getIosExtensions() {
    return iosExtensions;
  }

  @JsProperty
  public void setIosExtensions(String value) {
    iosExtensions = value;
  }

  @JsProperty
  public String getSurveyUrl() {
    return surveyUrl;
  }

  @JsProperty
  public void setSurveyUrl(String value) {
    surveyUrl = value;
  }

}
