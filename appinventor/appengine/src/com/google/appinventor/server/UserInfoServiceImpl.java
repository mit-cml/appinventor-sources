// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoService;
import com.google.appinventor.shared.storage.StorageUtil;

/**
 * Implementation of the user information service.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class UserInfoServiceImpl extends OdeRemoteServiceServlet implements UserInfoService {

  // Storage of user settings
  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private static final long serialVersionUID = -7316312435338169166L;

  /**
   * Returns System Config, including user information record
   *
   */

  @Override
  public Config getSystemConfig(String sessionId) {
    Config config = new Config();
    User user = userInfoProvider.getUser();
    user.setSessionId(sessionId);
    storageIo.setUserSessionId(userInfoProvider.getUserId(), sessionId);
    Flag<String> rendezvousFlag = Flag.createFlag("use.rendezvousserver", "");
    if (!rendezvousFlag.get().equals("")) {
      config.setRendezvousServer(rendezvousFlag.get());
    }
    config.setUser(user);

    // Fetch the current splash screen version
    config.setSplashConfig(storageIo.getSplashConfig());

    config.setLibraryUrl(Flag.createFlag("library.url", "").get());
    config.setGetStartedUrl(Flag.createFlag("getstarted.url", "").get());
    config.setExtensionsUrl(Flag.createFlag("extensions.url", "").get());
    config.setTutorialsUrl(Flag.createFlag("tutorials.url", "").get());
    config.setTroubleshootingUrl(Flag.createFlag("troubleshooting.url", "").get());
    config.setForumsUrl(Flag.createFlag("forums.url", "").get());
    config.setFeedbackUrl(Flag.createFlag("feedback.url", "").get());
    config.setReleaseNotesUrl(Flag.createFlag("release.notes.url", "").get());
    config.setTosUrl(Flag.createFlag("tos.url", "").get());
    config.setLogoUrl(Flag.createFlag("logo.url", "").get());
    config.setGuideUrl(Flag.createFlag("guide.url", "").get());
    config.setReferenceComponentsUrl(Flag.createFlag("reference.components.url", "").get());
    config.setFirebaseURL(Flag.createFlag("firebase.url", "").get());
    config.setDefaultCloudDBserver(Flag.createFlag("clouddb.server", "").get());
    config.setNoop(Flag.createFlag("session.noop", 0).get());

    if (!Flag.createFlag("build2.server.host", "").get().isEmpty()) {
      config.setSecondBuildserver(true);
    }

    // Check to see if we need to upgrade this user's project to GCS
    storageIo.checkUpgrade(userInfoProvider.getUserId());
    return config;
  }

  /**
   * Returns the user's backpack as an XML string.
   *
   * @return backpack
   */
  @Override
  public String getUserBackpack() {
    if (!hasUserFile(StorageUtil.USER_BACKPACK_FILENAME)) {
      return "[]";
    } else {
      return storageIo.downloadUserFile(userInfoProvider.getUserId(), StorageUtil.USER_BACKPACK_FILENAME, "UTF-8");
    }
  }

  /**
   * Returns user information.
   *
   * (obsoleted by getSystemConfig())
   *
   * @return  user information record
   */

  @Override
  public User getUserInformation(String sessionId) {
    // This is a little evil here. We are fetching the User object
    // *and* side effecting it by storing the sessionId
    // A more pedagotically correct way would be to do the store
    // in a separate RPC. But that would add another round trip.
    User user = userInfoProvider.getUser();
    user.setSessionId(sessionId); // Store local copy
    // Store it in the data store
    storageIo.setUserSessionId(userInfoProvider.getUserId(), sessionId);
    return user;
  }

  /**
   * Returns user information based on userId.
   *
   * @return  user information record
   */
  @Override
  public User getUserInformationByUserId(String userId) {
    return storageIo.getUser(userId);
  }

  /**
   * Retrieves the user's settings.
   *
   * @return  user's settings
   */
  @Override
  public String loadUserSettings() {
    return storageIo.loadSettings(userInfoProvider.getUserId());
  }

  /**
   * Stores the user's backpack as an xml string
   * @param backpack the xml string representing the backpack
   */

  @Override
  public void storeUserBackpack(String backpack) {
    storageIo.uploadUserFile(userInfoProvider.getUserId(), StorageUtil.USER_BACKPACK_FILENAME, backpack, "UTF-8");
  }

  /**
   * Stores the user's settings.
   * @param settings  user's settings
   */
  @Override
  public void storeUserSettings(String settings) {
    storageIo.storeSettings(userInfoProvider.getUserId(), settings);
  }

  /**
   * Stores the user's name.
   * @param name  user's name
   */
  @Override
  public void storeUserName(String name) {
    storageIo.setUserName(userInfoProvider.getUserId(), name);
  }

  /**
   * Stores the user's link.
   * @param link  user's link
   */
  @Override
  public void storeUserLink(String link) {
    storageIo.setUserLink(userInfoProvider.getUserId(), link);
  }

  /**
   * Stores the user's email notification frequency.
   * @param emailFrequency  user's email frequency
   */
  @Override
  public void storeUserEmailFrequency(int emailFrequency) {
    storageIo.setUserEmailFrequency(userInfoProvider.getUserId(), emailFrequency);
  }

  /**
   * Returns true if the current user has a user file with the given file name
   */
  @Override
  public boolean hasUserFile(String fileName) {
    return storageIo.getUserFiles(userInfoProvider.getUserId()).contains(fileName);
  }

  /**
   * Deletes the user file with the given file name
   */
  @Override
  public void deleteUserFile(String fileName) {
    storageIo.deleteUserFile(userInfoProvider.getUserId(), fileName);
  }

  /**
   * No-Op (No Operation). However because we are going through
   * OdeAuthFilter to get this far, a session cookie due for renewal
   * will be renewed.
   */
  @Override
  public void noop() {
  }

  /**
   * fetch the contents of a shared backpack.
   *
   * @param BackPackId the uuid of the backpack
   * @return the backpack's content as an XML string
   */

  @Override
  public String getSharedBackpack(String backPackId) {
    return storageIo.downloadBackpack(backPackId);
  }

  /**
   * store a shared backpack.
   *
   * Note: We overwrite any existing backpack. If merging of contents
   * is desired, our caller has to take care of it.
   *
   * @param BackPackId the uuid of the shared backpack
   * @param the new contents of the backpack
   */

  @Override
  public void storeSharedBackpack(String backPackId, String content) {
    storageIo.uploadBackpack(backPackId, content);
  }

}
