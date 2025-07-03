// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object representing user data.
 *
 */
public class Config implements IsSerializable, Serializable {
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

  public Config() {
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getRendezvousServer() {
    return this.rendezvousServer; // might be null
  }

  public void setRendezvousServer(String value) {
    this.rendezvousServer = value;
  }

  public SplashConfig getSplashConfig() {
    return this.splashConfig;
  }

  public void setSplashConfig(SplashConfig config) {
    this.splashConfig = config;
  }

  public String getLibraryUrl() {
    return libraryUrl;
  }

  public void setLibraryUrl(String libraryUrl) {
    this.libraryUrl = libraryUrl;
  }

  public String getGetStartedUrl() {
    return getStartedUrl;
  }

  public void setGetStartedUrl(String getStartedUrl) {
    this.getStartedUrl = getStartedUrl;
  }

  public String getExtensionsUrl() {
    return extensionsUrl;
  }

  public String getTutorialsUrl() {
    return tutorialsUrl;
  }

  public void setExtensionsUrl(String extensionsUrl) {
    this.extensionsUrl = extensionsUrl;
  }

  public void setTutorialsUrl(String tutorialsUrl) {
    this.tutorialsUrl = tutorialsUrl;
  }

  public String getTroubleshootingUrl() {
    return troubleshootingUrl;
  }

  public void setTroubleshootingUrl(String troubleshootingUrl) {
    this.troubleshootingUrl = troubleshootingUrl;
  }

  public String getForumsUrl() {
    return forumsUrl;
  }

  public void setForumsUrl(String forumsUrl) {
    this.forumsUrl = forumsUrl;
  }

  public String getFeedbackUrl() {
    return feedbackUrl;
  }

  public void setFeedbackUrl(String feedbackUrl) {
    this.feedbackUrl = feedbackUrl;
  }

  public String getReleaseNotesUrl() {
    return releaseNotesUrl;
  }

  public void setReleaseNotesUrl(String releaseNotesUrl) {
    this.releaseNotesUrl = releaseNotesUrl;
  }

  public String getTosUrl() {
    return tosUrl;
  }

  public void setTosUrl(String tosUrl) {
    this.tosUrl = tosUrl;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public String getGuideUrl() {
    return guideUrl;
  }

  public void setGuideUrl(String guideUrl) {
    this.guideUrl = guideUrl;
  }

  public String getReferenceComponentsUrl() {
    return referenceComponentsUrl;
  }

  public void setReferenceComponentsUrl(String referenceComponentsUrl) {
    this.referenceComponentsUrl = referenceComponentsUrl;
  }

  public String getFirebaseURL() {
    return firebaseURL;
  }

  public void setFirebaseURL(String url) {
    firebaseURL = url;
  }

  public void setDefaultCloudDBserver(String server) {
    defaultCloudDBserver = server;
  }

  public String getDefaultCloudDBserver() {
    return defaultCloudDBserver;
  }

  public int getNoop() {
    return noop;
  }

  public void setNoop(int noop) {
    this.noop = noop;
  }

  public boolean getSecondBuildserver() {
    return secondBuildserver;
  }

  public void setSecondBuildserver(boolean value) {
    secondBuildserver = value;
  }

  public boolean getGalleryEnabled() {
    return galleryEnabled;
  }

  public void setGalleryEnabled(boolean value) {
    galleryEnabled = value;
  }

  public String getGalleryLocation() {
    return galleryLocation;
  }

  public void setGalleryLocation(String value) {
    galleryLocation = value;
  }

  public boolean getGalleryReadOnly() {
    return galleryReadOnly;
  }

  public void setGalleryReadOnly(boolean value) {
    galleryReadOnly = value;
  }

  public void setTutorialUrlAllowed(List<String> value) {
    this.tutorialUrlAllowed = value;
  }

  public List<String> getTutorialsUrlAllowed() {
    return tutorialUrlAllowed;
  }

  public boolean getServerExpired() {
    return serverExpired;
  }

  public void setServerExpired(boolean value) {
    serverExpired = value;
  }

  public boolean getDeleteAccountAllowed() {
    return deleteAccountAllowed;
  }

  public void setDeleteAccountAllowed(boolean value) {
    deleteAccountAllowed = value;
  }

  public String getIosExtensions() {
    return iosExtensions;
  }

  public void setIosExtensions(String value) {
    iosExtensions = value;
  }

}
