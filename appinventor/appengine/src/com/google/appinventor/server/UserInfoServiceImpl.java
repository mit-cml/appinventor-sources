// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.survey.Survey;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoService;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.server.tokens.Token;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of the user information service.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class UserInfoServiceImpl extends OdeRemoteServiceServlet implements UserInfoService {

  // Storage of user settings
  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  private static final long serialVersionUID = -7316312435338169166L;

  private static final Logger LOG = Logger.getLogger(UserInfoServiceImpl.class.getName());

  @SuppressWarnings("SimpleDateFormat")
  private static final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

  private static boolean deleteAccountAllowed = Flag.createFlag("auth.deleteaccountallowed", true).get();

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

    String surveyUrl;
    if (AppInventorFeatures.doingSurvey()) {
      surveyUrl = Survey.check(user.getUserEmail());
    } else {
      surveyUrl = null;
    }

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
    config.setGalleryEnabled(Flag.createFlag("gallery.enabled", false).get());
    config.setGalleryReadOnly(Flag.createFlag("gallery.readonly", false).get());
    config.setGalleryLocation(Flag.createFlag("gallery.location", "").get());
    config.setDeleteAccountAllowed(deleteAccountAllowed);
    config.setIosExtensions(storageIo.getIosExtensionsConfig());
    config.setSurveyUrl(surveyUrl);

    if (!Flag.createFlag("build2.server.host", "").get().isEmpty()) {
      config.setSecondBuildserver(true);
    }

    String expirationDate = Flag.createFlag("service.expires.time", "").get();
    if (!expirationDate.isEmpty()) {
      try {
        Date expires = ISO8601.parse(expirationDate);
        if (expires.before(new Date())) {
          config.setServerExpired(true);
        }
      } catch (ParseException e) {
        throw CrashReport.createAndLogError(LOG, null, null, e);
      }
    }

    // Fetch list of allowed tutorial prefixes from the data store
    List<String> urls = storageIo.getTutorialsUrlAllowed();
    config.setTutorialUrlAllowed(urls);

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

  @Override
  public String deleteAccount() {
    if (!deleteAccountAllowed) {
      return ("");
    }
    if (storageIo.deleteAccount(userInfoProvider.getUserId())) {
      String delAccountUrl = Flag.createFlag("deleteaccount.url", "NONE").get();
      if (delAccountUrl.equals("NONE")) {
        return (delAccountUrl);
      } else {
        String token = Token.makeAccountDeletionToken(userInfoProvider.getUserId(),
          userInfoProvider.getUserEmail());
        return (delAccountUrl + "/?token=" + token);
      }
    } else {
      return ("");
    }
  }

}
