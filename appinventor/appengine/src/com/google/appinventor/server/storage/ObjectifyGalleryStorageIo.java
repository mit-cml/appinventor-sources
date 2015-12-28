// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.GalleryEmail;
import com.google.appinventor.server.GallerySearchIndex;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GalleryCommentReport;
import com.google.appinventor.shared.rpc.project.GalleryModerationAction;
import com.google.appinventor.shared.rpc.project.Email;
import com.google.appinventor.shared.rpc.project.GalleryReportListResult;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.common.annotations.VisibleForTesting;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Implements the GalleryStorageIo interface using Objectify as the underlying data
 * store. This class provides the db support for gallery data, and is modeled after
 * StorageIo which handles the rest of the AI database.
 *
 * @author wolberd@gmail.com (David Wolber)
 * @author vincentaths@gmail.com (Vincent Zhang)
 *
 */
public class ObjectifyGalleryStorageIo implements  GalleryStorageIo {
  static final Flag<Boolean> requireTos = Flag.createFlag("require.tos", false);

  private static final Logger LOG = Logger.getLogger(ObjectifyStorageIo.class.getName());

  private static final String DEFAULT_ENCODING = "UTF-8";

  // TODO(user): need a way to modify this. Also, what is really a good value?
  private static final int MAX_JOB_RETRIES = 10;
  private static final long TWENTYFOURHOURS = 24*3600*1000; // 24 hours in milliseconds

  // Use this class to define the work of a job that can be retried. The
  // "datastore" argument to run() is the Objectify object for this job
  // (created with ObjectifyService.beginTransaction()). Note that all operations
  // on "datastore" should be for objects in the same entity group.
  @VisibleForTesting
  abstract class JobRetryHelper {
    public abstract void run(Objectify datastore) throws ObjectifyException;
    /**
     * Called before retrying the job. Note that the underlying datastore
     * still has the transaction active, so restrictions about operations
     * over multiple entity groups still apply.
     */
    public void onNonFatalError() {
      // Default is to do nothing
    }
  }

  // Create a final object of this class to hold a modifiable result value that
  // can be used in a method of an inner class.
  private class Result<T> {
    T t;
  }

  static {
    // Register the data object classes stored in the database
    ObjectifyService.register(EmailData.class);
    ObjectifyService.register(GalleryAppData.class);
    ObjectifyService.register(GalleryCommentData.class);
    ObjectifyService.register(GalleryAppLikeData.class);
    ObjectifyService.register(GalleryAppFeatureData.class);
    ObjectifyService.register(GalleryAppTutorialData.class);
    ObjectifyService.register(GalleryAppAttributionData.class);
    ObjectifyService.register(GalleryAppReportData.class);
    ObjectifyService.register(GalleryModerationActionData.class);
  }

  // we'll need to talk to the StorageIo to get developer names, so...
  private final transient StorageIo storageIo =
      StorageIoInstanceHolder.INSTANCE;

  /**
   * creates a new gallery app
   * @param title title of new app
   * @param projectName name of new app's aia file
   * @param description description of new app
   * @param projectId id of the project being published to gallery
   * @param userId if of user publishing this app
   * @return a {@link GalleryApp} for gallery App
   */
  @Override
  public GalleryApp createGalleryApp(final String title,
      final String projectName, final String description, final String moreInfo,
      final String credit, final long projectId, final String userId) {

    final Result<GalleryAppData> galleryAppData = new Result<GalleryAppData>();
    try {
      // first job is on the gallery entity, creating the GalleryAppData object
      // and the associated files.
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          datastore = ObjectifyService.begin();
          long date = System.currentTimeMillis();
          GalleryAppData appData = new GalleryAppData();
          appData.id = null;  // let Objectify auto-generate the project id

          appData.dateCreated = date;
          appData.dateModified = date;
          appData.title = title;
          appData.projectName = projectName;
          appData.description = description;
          appData.moreInfo = moreInfo;
          appData.credit = credit;
          appData.projectId = projectId;
          appData.userId = userId;
          appData.active = true;
          appData.lastEmailNotificationTimeStamp = Email.NO_LAST_EMAIL_NOTIFICATION_ACTIVITY;
          datastore.put(appData); // put the appData in the db so that it gets assigned an id

          assert appData.id != null;
          galleryAppData.t = appData;
          // remember id in some way, as in below?
          // projectId.t = pd.id;
          // After the job commits projectId.t should end up with the last value
          // we've gotten for pd.id (i.e. the one that committed if there
          // was no error).
          // Note that while we cannot expect to read back a value that we've
          // written in this job, reading the assigned id from pd should work.

          Key<GalleryAppData> galleryKey = galleryKey(appData.id);
        }
      });

    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "gallery error", e);
    }
    GalleryApp gApp = new GalleryApp();
    makeGalleryApp(galleryAppData.t, gApp);
    return gApp;
  }

  /**
   * Returns total number of GalleryApps
   * @return number of GalleryApps
   */
  @Override
  public Integer getNumGalleryApps() {
    Objectify datastore = ObjectifyService.begin();
    int num = datastore.query(GalleryAppData.class).count();
    return num;
  }
  /**
   * Returns a wrapped class which contains list of most recently
   * updated galleryApps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  @Override
  public GalleryAppListResult getRecentGalleryApps(int start, final int count) {
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    // If I try to run this in runjobwithretries, it tells me can't run
    // non-ancestor query as a transaction. ObjectifyStorageio has some samples
    // of not using transactions (run with) so I grabbed.

    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppData appData:datastore.query(GalleryAppData.class).order("-dateModified").filter("active", true).offset(start).limit(count)) {
      GalleryApp gApp = new GalleryApp();
      makeGalleryApp(appData, gApp);
      apps.add(gApp);
    }
    int totalCount = datastore.query(GalleryAppData.class).order("-dateModified").filter("active", true).count();
    return new GalleryAppListResult(apps, totalCount);
  }
  /**
   * Returns a wrapped class which contains a list of most downloaded
   * gallery apps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  @Override
  public GalleryAppListResult getMostDownloadedApps(int start, final int count) {
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    // If I try to run this in runjobwithretries, it tells me can't run
    // non-ancestor query as a transaction. ObjectifyStorageio has some samples
    // of not using transactions (run with) so I grabbed.

    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppData appData:datastore.query(GalleryAppData.class).order("-numDownloads").filter("active", true).offset(start).limit(count)) {
      GalleryApp gApp = new GalleryApp();
      makeGalleryApp(appData, gApp);
      apps.add(gApp);
    }
    int totalCount = datastore.query(GalleryAppData.class).order("-numDownloads").filter("active", true).count();
    return new GalleryAppListResult(apps, totalCount);
  }

  /**
   * Returns a wrapped class which contains a list of most liked
   * gallery apps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  @Override
  public GalleryAppListResult getMostLikedApps(int start, final int count) {
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    // If I try to run this in runjobwithretries, it tells me can't run
    // non-ancestor query as a transaction. ObjectifyStorageio has some samples
    // of not using transactions (run with) so I grabbed

    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppData appData : datastore.query(GalleryAppData.class)
           .filter("active", true)
           .order("-numLikes")
           .order("-numDownloads")
           .offset(start).limit(count)) {
      GalleryApp gApp = new GalleryApp();
      makeGalleryApp(appData, gApp);
      apps.add(gApp);
    }

    // The line below is a PROBLEM. It is *very expensive*
    // We either need to keep separate track of the number of apps in the
    // Gallery, or we need to cache this value somewhere so we are
    // not computing it all the time!
    int totalCount = datastore.query(GalleryAppData.class).filter("active", true).count();
    return new GalleryAppListResult(apps, totalCount);

  }

  /**
   * Returns a wrapped class which contains a list of featured gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  public GalleryAppListResult getFeaturedApp(int start, int count){
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppFeatureData appFeatureData:datastore.query(GalleryAppFeatureData.class).offset(start).limit(count)) {
      Long galleryId = appFeatureData.galleryKey.getId();
      GalleryApp gApp = new GalleryApp();
      GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
      makeGalleryApp(galleryAppData, gApp);
      apps.add(gApp);
    }

    int totalCount = datastore.query(GalleryAppFeatureData.class).count();
    return new GalleryAppListResult(apps, totalCount);
  }

  /**
   * Returns a wrapped class which contains a list of tutorial gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  public GalleryAppListResult getTutorialApp(int start, int count){
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppTutorialData appTutorialData:datastore.query(GalleryAppTutorialData.class).offset(start).limit(count)) {
      Long galleryId = appTutorialData.galleryKey.getId();
      GalleryApp gApp = new GalleryApp();
      GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
      makeGalleryApp(galleryAppData, gApp);
      apps.add(gApp);
    }

    int totalCount = datastore.query(GalleryAppTutorialData.class).count();
    return new GalleryAppListResult(apps, totalCount);
  }

  /**
   * check if app is featured already
   * @param galleryId gallery id
   * @return true if featured, otherwise false
   */
  public boolean isFeatured(long galleryId){
    final Result<Boolean> result = new Result<Boolean>();
    Objectify datastore = ObjectifyService.begin();
    result.t = false;
    for (GalleryAppFeatureData appFeatureData:datastore.query(GalleryAppFeatureData.class).ancestor(galleryKey(galleryId))) {
      result.t = true;
      break;
    }
    return result.t;
  }

  /**
   * check if app is tutorial already
   * @param galleryId gallery id
   * @return true if tutorial, otherwise false
   */
   public boolean isTutorial(long galleryId){
    final Result<Boolean> result = new Result<Boolean>();
    Objectify datastore = ObjectifyService.begin();
    result.t = false;
    for (GalleryAppTutorialData appTutorialData:datastore.query(GalleryAppTutorialData.class).ancestor(galleryKey(galleryId))) {
      result.t = true;
      break;
    }
    return result.t;
  }

  /**
   * mark an app as featured
   * @param galleryId gallery id
   * @return
   */
  public boolean markAppAsFeatured(long galleryId){
    final Result<Boolean> result = new Result<Boolean>();
    result.t = false;
    boolean find = false;
    Objectify datastore = ObjectifyService.begin();

    for (GalleryAppFeatureData appFeatureData:datastore.query(GalleryAppFeatureData.class).ancestor(galleryKey(galleryId))) {
      find = true;
      datastore.delete(appFeatureData);
      result.t = false;
      break;
    }
    if(!find){
      GalleryAppFeatureData appFeatureData = new GalleryAppFeatureData();
      appFeatureData.galleryKey = galleryKey(galleryId);
      datastore.put(appFeatureData);
      result.t = true;
    }
    return result.t;
  }

  /**
   * mark an app as tutorial
   * @param galleryId gallery id
   * @return
   */
  public boolean markAppAsTutorial(long galleryId){
    final Result<Boolean> result = new Result<Boolean>();
    result.t = false;
    boolean find = false;
    Objectify datastore = ObjectifyService.begin();

    for (GalleryAppTutorialData appTutorialData:datastore.query(GalleryAppTutorialData.class).ancestor(galleryKey(galleryId))) {
      find = true;
      datastore.delete(appTutorialData);
      result.t = false;
      break;
    }
    if(!find){
      GalleryAppTutorialData appTutorialData = new GalleryAppTutorialData();
      appTutorialData.galleryKey = galleryKey(galleryId);
      datastore.put(appTutorialData);
      result.t = true;
    }
    return result.t;
  }

  /**
   * Returns a wrapped class which contains a list of galleryApps
   * by a particular developer and total number of results in database
   * @param userId id of developer
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */  @Override
  public GalleryAppListResult getDeveloperApps(String userId, int start, final int count) {
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    // if i try to run this in runjobwithretries it tells me can't run
    // non-ancestor query as a transaction. ObjectifyStorageio has some samples
    // of not using transactions (run with) so i grabbed

    Objectify datastore = ObjectifyService.begin();
    for (GalleryAppData appData:datastore.query(GalleryAppData.class).filter("userId",userId).filter("active", true).offset(start).limit(count)) {
      GalleryApp gApp = new GalleryApp();
      makeGalleryApp(appData, gApp);
      apps.add(gApp);
    }
    int totalCount = datastore.query(GalleryAppData.class).filter("userId",userId).filter("active", true).count();
    return new GalleryAppListResult(apps, totalCount);
  }

 /**
   * Records that an app has been downloaded
   * @param galleryId the id of gallery app that was downloaded
   */
  @Override
  public void incrementDownloads(final long galleryId) {

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
          if (galleryAppData != null) {
            galleryAppData.numDownloads = galleryAppData.numDownloads + 1;
            galleryAppData.unreadDownloads = galleryAppData.unreadDownloads + 1;
            datastore.put(galleryAppData);
          }
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo", e);
    }
  }

  /**
   * updates gallery app
   * @param galleryId id of app being updated
   * @param title new title of app
   * @param description new description of app
   * @param userId if of user publishing this app
   */
  @Override
  public void updateGalleryApp(final long galleryId, final String title,
      final String description, final String moreInfo, final String credit,
      final String userId) {

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
          if (galleryAppData != null) {
            long date = System.currentTimeMillis();
            galleryAppData.title = title;
            galleryAppData.description = description;
            galleryAppData.moreInfo = moreInfo;
            galleryAppData.credit = credit;
            galleryAppData.dateModified = date;
            datastore.put(galleryAppData);
          }
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo", e);
    }
  }

  /**
   * Returns a gallery app
   * @param galleryId id of gallery app you want
   * @return a {@link GalleryApp} for gallery App
   */
  @Override
  public GalleryApp getGalleryApp(final long galleryId) {
    final GalleryApp gApp = new GalleryApp();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData app = datastore.get(new Key<GalleryAppData>(GalleryAppData.class,galleryId));
          makeGalleryApp(app,gApp);
        }
      });
    }catch (NotFoundException e){
      //galleryId is not in our database
      return null;
    }
    catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,"gallery error", e);
    }
    return (gApp);
  }

  /**
   * deletes an app
   * @param galleryId the id of gallery app to be deleted
   */
  public void deleteApp(final long galleryId) {
    //the commenting code is for deleting app, instead, we are going to deactivate app
	/*
    try {
      // first job deletes the UserProjectData in the user's entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          // delete the GalleryApp
          datastore.delete(galleryKey(galleryId));
        }
      });
      // second job deletes the comments from this app
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryCommentData commentData : datastore.query(GalleryCommentData.class).ancestor(galleryKey).order("-dateCreated")) {
            datastore.delete(commentData);
          }
        }
      });
      //note that in the gallery service we'll change the associated project's gallery id back to 0
      //  and we'll remove the aia and image file
     } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,"gallery remove error", e);
    }
    */
    //for now, we only set app to inactive status.
    try {
        runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            // delete the GalleryApp
            GalleryAppData appData = datastore.find(galleryKey(galleryId));
            if(appData != null){
              appData.active = false;
              datastore.put(appData);
            }
          }
        });
       } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null,"gallery remove error", e);
      }
  }


  /**
   * adds a comment to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param userId id of user who commented
   * @param comment comment
   * @return the id of the new comment
   */
  @Override
  public long addComment(final long galleryId, final String userId, final String comment) {
    final Result<Long> theDate = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryCommentData commentData = new GalleryCommentData();
          long date = System.currentTimeMillis();
          commentData.comment = comment;
          commentData.userId = userId;
          commentData.galleryKey = galleryKey(galleryId);
          commentData.dateCreated = date;
          theDate.t = date;

          datastore.put(commentData);
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.addComment", e);
    }
    return theDate.t;
  }
 /**
   * Returns a list of comments for an app
   * @param galleryId id of gallery app
   * @return list of {@link GalleryComment}
   */
  @Override
  public List<GalleryComment> getComments(final long galleryId) {
   final List<GalleryComment> comments = new ArrayList<GalleryComment>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryCommentData commentData : datastore.query(GalleryCommentData.class).ancestor(galleryKey).order("-dateCreated")) {
            User commenter = storageIo.getUser(commentData.userId);
            String name="unknown";
            if (commenter!= null) {
               name = commenter.getUserName();
            }
            GalleryComment galleryComment = new GalleryComment(galleryId,
                commentData.userId,commentData.comment,commentData.dateCreated);
            galleryComment.setUserName(name);
            comments.add(galleryComment);
          }
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getComments", e);
    }

    return comments;
  }

  /**
   * increase likes to a gallery app
   *
   * @param galleryId
   *          id of gallery app that was like on
   * @param userId
   *          id of user who liked
   * @return the number of the new like
   */
  @Override
  public int increaseLikes(final long galleryId,final String userId) {
    final Result<Integer> numLikes = new Result<Integer>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
          if (galleryAppData != null) {
            Key<GalleryAppData> galleryKey = galleryKey(galleryId);

            // Make sure it isn't already liked (people have subverted the client
            // based checks!)
            for (GalleryAppLikeData likeData : datastore.query(GalleryAppLikeData.class).ancestor(galleryKey)) {
              if(likeData.userId.equals(userId)){
                return;         // We're done, already liked.
              }
            }

            // Forge the like data entry
            GalleryAppLikeData likeData = new GalleryAppLikeData();
            likeData.galleryKey = galleryKey;
            likeData.userId = userId;
            datastore.put(likeData);

            // Retrieve the current number of likes
            numLikes.t = datastore.query(GalleryAppLikeData.class).ancestor(galleryKey).count();

            // Increase app's like/unread like count
            galleryAppData.numLikes = galleryAppData.numLikes + 1;
            galleryAppData.unreadLikes = galleryAppData.unreadLikes + 1;
            datastore.put(galleryAppData);
          }
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.increaseLike", e);
    }
    return numLikes.t;
  }

  /**
   * decrease likes to a gallery app
   *
   * @param galleryId   id of gallery app that was unlike on
   * @param userId    id of user who unliked
   * @return    the number of the new like
   */
  @Override
  public int decreaseLikes(final long galleryId, final String userId) {
    final Result<Integer> numLikes = new Result<Integer>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
          if (galleryAppData != null) {
            Key<GalleryAppData> galleryKey = galleryKey(galleryId);
            for (GalleryAppLikeData likeData : datastore.query(GalleryAppLikeData.class).ancestor(galleryKey)) {
              if(likeData.userId.equals(userId)){
                datastore.delete(likeData);
                // break;
                // We don't break because there might be more then one likeData object for this
                // person
              }
            }
            numLikes.t = datastore.query(GalleryAppLikeData.class).ancestor(galleryKey).count();

            // Increase app's like/unread like count
            if(galleryAppData.numLikes > 0){
              galleryAppData.numLikes = galleryAppData.numLikes - 1;
            }

            if (galleryAppData.unreadLikes > 0) {
              galleryAppData.unreadLikes = galleryAppData.unreadLikes - 1;
              datastore.put(galleryAppData);
            }
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.decreaseLike", e);
    }
    return numLikes.t;
  }

  /**
   * get num likes of a gallery app
   *
   * @param galleryId
   *          id of gallery app
   * @return the num of like
   */
  public int getNumLikes(final long galleryId) {
    final Result<Integer> num = new Result<Integer>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          //num.t = datastore.query(GalleryAppLikeData.class).ancestor(galleryKey).count();
          GalleryAppData galleryAppData = datastore.find(galleryKey);
          num.t = galleryAppData.numLikes;
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.getNumLike", e);
    }
    return num.t;
  }

  /**
   * Check if an app is liked by a user
   *
   * @param galleryId   id of gallery app that was unlike on
   * @param userId    id of user who unliked
   * @return    true if relation exists
   */
  @Override
  public boolean isLikedByUser(final long galleryId, final String userId) {
    final Result<Boolean> bool = new Result<Boolean>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          boolean find = false;
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryAppLikeData likeData : datastore.query(GalleryAppLikeData.class).ancestor(galleryKey)) {
            if(likeData.userId.equals(userId)){
              find = true;
              bool.t = true;
              break;
            }
          }
          if(!find){
            bool.t = false;
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.isLikedByUser", e);
    }
    return bool.t;
  }

  /**
   * salvage the gallery app by given galleryId
   */
  @Override
  public void salvageGalleryApp(final long galleryId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          int num = 0;
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          // We need to extract a unique set of userId's for the Likes of this app
          // Because of past bugs and abuse, a user can Like an app more then once
          // So we fix that here...
          TreeMap<String,Boolean> likeTree = new TreeMap();
          for (GalleryAppLikeData likeData : datastore.query(GalleryAppLikeData.class).ancestor(galleryKey)) {
            likeTree.put(likeData.userId, true);
          }
          for (GalleryAppLikeData likeData : datastore.query(GalleryAppLikeData.class).ancestor(galleryKey)) {
            datastore.delete(likeData);
          }
          for (String treeUserId : likeTree.keySet()) {
            GalleryAppLikeData likeData = new GalleryAppLikeData();
            likeData.userId = treeUserId;
            likeData.galleryKey = galleryKey;
            datastore.put(likeData);
          }

          num = datastore.query(GalleryAppLikeData.class).ancestor(galleryKey).count();
          GalleryAppData galleryAppData = datastore.find(galleryKey);
          galleryAppData.numLikes = num;
          datastore.put(galleryAppData);
          LOG.info("salvage on gallerId:" + galleryId + ", total likes:" + galleryAppData.numLikes);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.salvageGalleryApp", e);
    }
  }

  /**
   * save the attribution of a gallery app
   *
   * @param galleryId
   *          id of gallery app
   * @param attributionId
   *          id of attribution app
   * @return the id of attribution info
   */
  public long saveAttribution(final long galleryId, final long attributionId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
            GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
            Key<GalleryAppData> galleryKey = galleryKey(galleryId);
            GalleryAppAttributionData attributionData = new GalleryAppAttributionData();
            attributionData.galleryKey = galleryKey(galleryId);
            attributionData.attributionId = attributionId;
            attributionData.galleryId = galleryId;
            datastore.put(attributionData);

        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.saveAttribution", e);
    }
    return System.currentTimeMillis();
  }

  /**
   * get the attribution id of a gallery app
   *
   * @param galleryId
   *          id of gallery app
   * @return the attributionId
   */
  public long remixedFrom(final long galleryId) {
    final Result<Long> id = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
              datastore = ObjectifyService.begin();
              Key<GalleryAppData> galleryKey = galleryKey(galleryId);
              boolean find = false;
              //this is a forloop but only one result, or none if app created before this.
              for (GalleryAppAttributionData attributionData : datastore.query(GalleryAppAttributionData.class).ancestor(galleryKey)) {
                if(attributionData.attributionId == UserProject.FROMSCRATCH) continue;
                GalleryAppData appData = datastore.find(galleryKey(attributionData.attributionId));
                if(appData == null || appData.active == false) break;
                id.t = attributionData.attributionId;
                find = true;
              }
              if(!find){
                id.t = UserProject.FROMSCRATCH;
              }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.saveAttribution", e);
    }
    return id.t;
  }

  /**
   * get the list of children Gallery Apps
   *
   * @param galleryId
   *          id of gallery app
   * @return the list of children Gallery Apps
   */
  public List<GalleryApp> remixedTo(final long galleryId) {
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
              datastore = ObjectifyService.begin();
              for (GalleryAppAttributionData attributionData:datastore.query(GalleryAppAttributionData.class).filter("attributionId",galleryId)) {
                GalleryAppData galleryAppData = datastore.find(galleryKey(attributionData.galleryId));
                if(!galleryAppData.active) continue;
                GalleryApp gApp = new GalleryApp();
                makeGalleryApp(galleryAppData, gApp);
                apps.add(gApp);
              }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.saveAttribution", e);
    }
    return apps;
  }
  /**
   * adds a report (flag) to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param offenderId id of user who commented
   * @param reporterId report
   * @return the id of the new report
   */
  @Override
  public long addAppReport(final String reportText, final long galleryId,final String offenderId, final String reporterId) {
    final Result<Long> theDate = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppReportData reportData = new GalleryAppReportData();
          long date = System.currentTimeMillis();
          reportData.id = null;  // let Objectify auto-generate the GalleryAppReportData id
          reportData.reportText = reportText;
          reportData.reporterId = reporterId;
          reportData.offenderId = offenderId;
          reportData.galleryKey = galleryKey(galleryId);
          reportData.dateCreated=date;
          reportData.resolved = false;
          theDate.t=date;
          datastore.put(reportData);
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.addAppReport", e);
    }
    return theDate.t;
  }
  /**
   * check if an app is reported by a user
   * @param galleryId
   *          id of gallery app that was unlike on
   * @param userId
   *          id of user who unliked
   * @return true if relation exists
   */
  @Override
  public boolean isReportedByUser(final long galleryId, final String userId) {
    final Result<Boolean> bool = new Result<Boolean>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          boolean find = false;
//          LOG.log(Level.SEVERE, "$$$$$:" + galleryId);
//          LOG.log(Level.SEVERE, "$$$$$:" + userId);
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryAppReportData appReportData : datastore.query(GalleryAppReportData.class).ancestor(galleryKey)) {
            if(appReportData.reporterId.equals(userId)){
              find = true;
              bool.t = true;
              break;
            }
          }
          if(!find){
            bool.t = false;
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in galleryStorageIo.isReportedByUser", e);
    }
    return bool.t;
  }
  /**
   * Returns a list of reports (flags) for an app
   * @param galleryId id of gallery app
   * @param start start index
   * @param count number to return
   * @return list of {@link GalleryAppReport}
   */
  @Override
  public List<GalleryAppReport> getAppReports(final long galleryId, final int start, final int count) {
   final List<GalleryAppReport> reports = new ArrayList<GalleryAppReport>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryAppReportData reportData : datastore.query(GalleryAppReportData.class).filter("resolved", false).order("-dateCreated").offset(start).limit(count)) {
            User reporter = storageIo.getUser(reportData.reporterId);
            User offender = storageIo.getUser(reportData.offenderId);
            GalleryApp app = getGalleryApp(galleryId);
            GalleryAppReport galleryReport = new GalleryAppReport(reportData.id,reportData.reportText, app, offender, reporter,
                reportData.dateCreated, reportData.resolved);
            reports.add(galleryReport);
          }
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getAppReports", e);
    }
    return reports;
  }

  /**
   * Returns a wrapped class which contains a list of reports (flags) for unresolved app
   * and total number of results in database
   * @param start start index
   * @param count number to return
   * @return list of {@link GalleryAppReport}
   */
  @Override
  public GalleryReportListResult getAppReports(final int start, final int count) {
    final List<GalleryAppReport> reports = new ArrayList<GalleryAppReport>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          datastore = ObjectifyService.begin();
          for (GalleryAppReportData reportData : datastore.query(GalleryAppReportData.class).filter("resolved", false).order("-dateCreated").offset(start).limit(count)) {
            User reporter = storageIo.getUser(reportData.reporterId);
            User offender = storageIo.getUser(reportData.offenderId);
            GalleryApp app = getGalleryApp(reportData.galleryKey.getId());
            GalleryAppReport galleryReport = new GalleryAppReport(reportData.id,reportData.reportText, app, offender, reporter,
                reportData.dateCreated, reportData.resolved);
            reports.add(galleryReport);
          }
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getAppReports (all)", e);
    }
    Objectify datastore = ObjectifyService.begin();
    int totalCount = datastore.query(GalleryAppReportData.class).filter("resolved", false).count();
    return new GalleryReportListResult(reports, totalCount);
  }
  /**
  * Returns a wrapped class which contains a list of reports (flags) for resolved and unresolved app
  * and total number of results in database
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  @Override
  public GalleryReportListResult getAllAppReports(final int start, final int count){
    final List<GalleryAppReport> reports = new ArrayList<GalleryAppReport>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          datastore = ObjectifyService.begin();
          for (GalleryAppReportData reportData : datastore.query(GalleryAppReportData.class).order("-dateCreated").offset(start).limit(count)) {
            User reporter = storageIo.getUser(reportData.reporterId);
            User offender = storageIo.getUser(reportData.offenderId);
            GalleryApp app = getGalleryApp(reportData.galleryKey.getId());
            GalleryAppReport galleryReport = new GalleryAppReport(reportData.id,reportData.reportText, app, offender, reporter,
              reportData.dateCreated, reportData.resolved);
            reports.add(galleryReport);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getExistingAppReports (all)", e);
    }
    Objectify datastore = ObjectifyService.begin();
    int totalCount = datastore.query(GalleryAppReportData.class).count();
    return new GalleryReportListResult(reports, totalCount);
  }
  /**
   * mark an report as resolved
   * @param reportId the id of the app
   */
  @Override
  public boolean markReportAsResolved(final long reportId, final long galleryId){
    final Result<Boolean> success = new Result<Boolean>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          datastore = ObjectifyService.begin();
          success.t = false;
          Key<GalleryAppData> galleryKey = galleryKey(galleryId);
          for (GalleryAppReportData reportData : datastore.query(GalleryAppReportData.class).ancestor(galleryKey)) {
            if(reportData.id == reportId){
              reportData.resolved = !reportData.resolved;
              datastore.put(reportData);
              success.t = true;
              break;
            }
          }
         }
      });
     } catch (ObjectifyException e) {
         throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.markReportAsResolved", e);
     }
     return success.t;
  }
  /**
   * deactivate app
   * @param galleryId the id of the app
   */
  @Override
  public boolean deactivateGalleryApp(final long galleryId) {
    final Result<Boolean> success = new Result<Boolean>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
            datastore = ObjectifyService.begin();
            success.t = false;
            Key<GalleryAppData> galleryKey = galleryKey(galleryId);
            GalleryAppData appData = datastore.find(galleryKey);
            if(appData != null){
              appData.active = !appData.active;
              datastore.put(appData);
              success.t = true;
              if(appData.active){
                GalleryApp gApp = new GalleryApp();
                makeGalleryApp(appData, gApp);
                GallerySearchIndex.getInstance().indexApp(gApp);
              }else{
                GallerySearchIndex.getInstance().unIndexApp(appData.id);
              }
            }
         }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.markReportAsResolved", e);
    }
    return success.t;
  }
  /**
   * check if gallery app is activated
   * @param galleryId the id of the gallery app
   */
  @Override
  public boolean isGalleryAppActivated(final long galleryId){
    final Result<Boolean> success = new Result<Boolean>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
            datastore = ObjectifyService.begin();
            success.t = false;
            Key<GalleryAppData> galleryKey = galleryKey(galleryId);
            GalleryAppData appData = datastore.find(galleryKey);
            if(appData != null){
              if(appData.active){
                success.t = true;
              }
            }
         }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.markReportAsResolved", e);
    }
    return success.t;
  }

  /**
   * adds a report (flag) to a gallery app comment
   * @param commentId id of comment that was reported
   * @param userId id of user who commented
   * @param report report
   * @return the id of the new report
   */
  @Override
  public long addCommentReport(final long commentId, final String userId, final String report) {
    final Result<Long> theDate = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryCommentReportData reportData = new GalleryCommentReportData();
          long date = System.currentTimeMillis();
          reportData.report = report;
          reportData.userId = userId;
          reportData.galleryCommentKey = galleryCommentKey(commentId);
          reportData.dateCreated=date;
          theDate.t=date;
          datastore.put(reportData);
        }
      });
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.addCommentReport", e);
    }
    return theDate.t;
  }
 /**
   * Returns a list of reports (flags) for a comment
   * @param commentId id of comment
   * @return list of {@link GalleryCommentReport}
   */
  @Override
  public List<GalleryCommentReport> getCommentReports(final long commentId) {
   final List<GalleryCommentReport> reports = new ArrayList<GalleryCommentReport>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryCommentData> galleryCommentKey = galleryCommentKey(commentId);
          for (GalleryCommentReportData reportData : datastore.query(GalleryCommentReportData.class).ancestor(galleryCommentKey).order("-dateCreated")) {
            User commenter = storageIo.getUser(reportData.userId);
            String name="unknown";
            if (commenter!= null) {
               name = commenter.getUserName();
            }
            GalleryCommentReport galleryCommentReport = new GalleryCommentReport(commentId,
                reportData.userId,reportData.report,reportData.dateCreated);
            galleryCommentReport.setUserName(name);
            reports.add(galleryCommentReport);
          }
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getCommentReports", e);
    }

    return reports;
  }

  /**
   * Returns a list of reports (flags) for all comments
   * @return list of {@link GalleryCommentReport}
   */
  @Override
  public List<GalleryCommentReport> getCommentReports() {
    final List<GalleryCommentReport> reports = new ArrayList<GalleryCommentReport>();
    Objectify datastore = ObjectifyService.begin();
    for (GalleryCommentReportData reportData : datastore.query(GalleryCommentReportData.class).order("-dateCreated")) {
      User commenter = storageIo.getUser(reportData.userId);
      String name="unknown";
      if (commenter!= null) {
         name = commenter.getUserName();
      }
      GalleryCommentReport galleryCommentReport = new GalleryCommentReport(reportData.galleryCommentKey.getId(),
          reportData.userId,reportData.report,reportData.dateCreated);
      galleryCommentReport.setUserName(name);
      reports.add(galleryCommentReport);
    }
    return reports;
  }

  /**
   * Store moderation actions based on actionType
   * @param reportId
   * @param galleryId
   * @param emailId
   * @param moderatorId
   * @param actionType
   */
  @Override
  public void storeModerationAction(final long reportId, final long galleryId, final long emailId, final String moderatorId,
      final int actionType, final String moderatorName, final String emailPreview){
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryModerationActionData moderationActionData = new GalleryModerationActionData();
          long date = System.currentTimeMillis();
          moderationActionData.date = date;
          moderationActionData.reportId = reportId;
          moderationActionData.galleryId = galleryId;
          moderationActionData.emailId = emailId;
          moderationActionData.moderatorId = moderatorId;
          moderationActionData.actionType = actionType;
          moderationActionData.moderatorName = moderatorName;
          moderationActionData.emailPreview = emailPreview;
          moderationActionData.reportKey = galleryReportKey(reportId);
          datastore.put(moderationActionData);
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.storeModerationAction", e);
    }
  }

  /**
   * get moderation actions associated with given reportId
   * @param reportId
   */
  @Override
  public List<GalleryModerationAction> getModerationActions(final long reportId){
    final List<GalleryModerationAction> moderationActions = new ArrayList<GalleryModerationAction>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<GalleryAppReportData> galleryReportKey = galleryReportKey(reportId);
          for (GalleryModerationActionData moderationActionData : datastore.query(GalleryModerationActionData.class)
              .ancestor(galleryReportKey).order("-date")) {
            GalleryModerationAction moderationAction = new GalleryModerationAction(reportId, moderationActionData.galleryId,
                moderationActionData.emailId, moderationActionData.moderatorId, moderationActionData.actionType,
                moderationActionData.moderatorName, moderationActionData.emailPreview, moderationActionData.date);
            moderationActions.add(moderationAction);
          }
        }
      });
    } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.getCommentReports", e);
    }
    return moderationActions;
  }

  /**
   * Converts a db object GalleryAppData into a shared GalleryApp that can be passed
   * around in client. Create the galleryApp first then send it here to get its data
   *
   */
  private void makeGalleryApp(GalleryAppData appData, GalleryApp galleryApp) {
    galleryApp.setTitle(appData.title);
    galleryApp.setProjectName(appData.projectName);
    galleryApp.setGalleryAppId(appData.id);
    galleryApp.setProjectId(appData.projectId);
    galleryApp.setDescription(appData.description);

    User developer = storageIo.getUser(appData.userId);
    galleryApp.setDeveloperName(developer.getUserName());
    galleryApp.setDeveloperId(appData.userId);
    galleryApp.setDownloads(appData.numDownloads);
    galleryApp.setUnreadDownloads(appData.unreadDownloads);
    galleryApp.setUnreadLikes(appData.unreadLikes);
    galleryApp.setCreationDate(appData.dateCreated);
    galleryApp.setUpdateDate(appData.dateModified);
    galleryApp.setActive(appData.active);
    galleryApp.setMoreInfo(appData.moreInfo);
    galleryApp.setCredit(appData.credit);

    galleryApp.setLikes(getNumLikes(appData.id));
  }

  private static String collectGalleryAppErrorInfo(final String galleryAppId) {
    return "galleryApp=" + galleryAppId;
  }

  private Key<GalleryAppData> galleryKey(long galleryId) {
    return new Key<GalleryAppData>(GalleryAppData.class, galleryId);
  }

  private Key<GalleryAppFeatureData> galleryFeatureKey(long galleryId) {
    return new Key<GalleryAppFeatureData>(GalleryAppFeatureData.class, galleryId);
  }

  private Key<GalleryAppTutorialData> galleryTutorialKey(long galleryId) {
    return new Key<GalleryAppTutorialData>(GalleryAppTutorialData.class, galleryId);
  }

  private Key<GalleryCommentData> galleryCommentKey(long commentId) {
    return new Key<GalleryCommentData>(GalleryCommentData.class, commentId);
  }

  private Key<GalleryAppReportData> galleryReportKey(long appReportId) {
    return new Key<GalleryAppReportData>(GalleryAppReportData.class, appReportId);
  }

  private Key<EmailData> emailKey(long id) {
    return new Key<EmailData>(EmailData.class, id);
  }

  private Key<GalleryModerationActionData> galleryModerationActionKey(long id) {
    return new Key<GalleryModerationActionData>(GalleryModerationActionData.class, id);
  }


  /**
   * Sends an email to a particular user
   * @param senderId id of user sending this email
   * @param receiverId id of user receiving this email
   * @param title title of email
   * @param body body of email
   */
  @Override
  public long sendEmail(final String senderId, final String receiverId,
      final String senderEmail, final String receiverEmail, final String title,
      final String body) {

    final Result<Long> emailId = new Result<Long>();
    final User sender = storageIo.getUser(senderId);

    if(!sender.isModerator()){
      emailId.t = Email.NOTRECORDED;
      return emailId.t;
    }

    //send email
    boolean success = new GalleryEmail().sendEmail(senderEmail, receiverEmail, title, body);
    if(success){
        //record email in database
        try {
          runJobWithRetries(new JobRetryHelper() {
            @Override
            public void run(Objectify datastore) {
              EmailData emailData = new EmailData();
              long date = System.currentTimeMillis();
              emailData.senderId = senderId;
              emailData.receiverId = receiverId;
              emailData.title = title;
              emailData.body = body;
              emailData.datestamp = date;
              datastore.put(emailData);
              emailId.t = emailData.id;
            }
          });
        } catch (ObjectifyException e) {
           emailId.t = null;
           throw CrashReport.createAndLogError(LOG, null, "error in galleryStorageIo.sendEmail", e);
        }
    }else{
      emailId.t = Email.NOTRECORDED;
    }

    return emailId.t;
  }

  /**
   * Returns the email with a particular emailId
   * @param emailId id of the email
   */  @Override
  public Email getEmail(final long emailId) {
    final Result<Email> result = new Result<Email>();
    // if i try to run this in runjobwithretries it tells me can't run
    // non-ancestor query as a transaction. ObjectifyStorageio has some samples
    // of not using transactions (run with) so i grabbed
    Objectify datastore = ObjectifyService.begin();
    for (EmailData emailData : datastore.query(EmailData.class)
        .filter("id", emailId)/*.order("-datestamp")*/) {
      Email email = new Email(emailData.id, emailData.senderId, emailData.receiverId,
          emailData.title, emailData.body, emailData.datestamp);
      result.t = email;
      break;
    }
    return result.t;
  }

  /**
   * Call job.run() in a transaction and commit the transaction if no exceptions
   * occur. If we get a {@link java.util.ConcurrentModificationException}
   * or {@link com.google.appinventor.server.storage.ObjectifyException}
   * we will retry the job (at most {@code MAX_JOB_RETRIES times}).
   * Any other exception will cause the job to fail immediately.
   * @param job
   * @throws ObjectifyException
   */
  @VisibleForTesting
  void runJobWithRetries(JobRetryHelper job) throws ObjectifyException {
    int tries = 0;
    while (tries <= MAX_JOB_RETRIES) {
      Objectify datastore = ObjectifyService.beginTransaction();
      try {
        job.run(datastore);
        datastore.getTxn().commit();
        break;
      } catch (ConcurrentModificationException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Optimistic concurrency failure", ex);
      } catch (ObjectifyException oe) {
        // maybe this should be a fatal error? I think the only thing
        // that creates this exception (other than this method) is uploadToBlobstore
        job.onNonFatalError();
      } finally {
        if (datastore.getTxn().isActive()) {
          try {
            datastore.getTxn().rollback();
          } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Transaction rollback failed", e);
          }
        }
      }
      tries++;
    }
    if (tries > MAX_JOB_RETRIES) {
      throw new ObjectifyException("Couldn't commit job after max retries.");
    }
  }

  /**
   * Check if need to send out email about number of downloads and likes
   * @param userId the id of User
   * @param galleryId  the id of GalleryApp
   * @param adminEmail admin Email
   * @param currentHost current host address from client
   */
  public boolean checkIfSendAppStats(final String userId, final long galleryId, final String adminEmail, final String currentHost) {
    final Result<Boolean> send = new Result<Boolean>();
    final long currentTime = System.currentTimeMillis();
    try {
      final User user = storageIo.getUser(userId);
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          GalleryAppData galleryAppData = datastore.find(galleryKey(galleryId));
          if(currentTime - galleryAppData.lastEmailNotificationTimeStamp > TWENTYFOURHOURS){
            if(galleryAppData.unreadDownloads + galleryAppData.unreadLikes >= user.getUserEmailFrequency()){
              String title = prepareAppStatsEmailTitle(galleryAppData.title);
              String body = prepareAppStatsEmailBody(galleryAppData.title, galleryAppData.numDownloads,
                  getNumLikes(galleryId), currentHost, galleryId);
              boolean success = new GalleryEmail().sendEmail(adminEmail, user.getUserEmail(), title, body);
              if(success){
                send.t = true;
                //clear unread stats
                galleryAppData.unreadDownloads = 0;
                galleryAppData.unreadLikes = 0;
                //update last eamil notification timestamp
                galleryAppData.lastEmailNotificationTimeStamp = currentTime;
                datastore.put(galleryAppData);
              }else{
                /*send email fail*/
                send.t = false;
              }
            }else{
              /*num of (unreaddownloads+unreadlikes) hasn't reach the threshold */
              send.t = false;
            }
          }else{
            /*less than 24 hours of last email notification on this app*/
            send.t = false;
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,"gallery error: checkIfSendAppStats", e);
    }

    return send.t;
  }

  /**
   * generate the email title of app stats
   * @param title
   * @return
   */
  private String prepareAppStatsEmailTitle(String title) {
    return "Congratulations: Your App \"" + title +"\" has been downloaded or liked";
  }

  /**
   * generate the email body of app stats
   * @param title
   * @param numDownloads
   * @param numLikes
   * @param currentHost
   * @param galleryId
   * @return
   */
  private String prepareAppStatsEmailBody(String title, int numDownloads, int numLikes, String currentHost, long galleryId){
    return "Congratulations, your app \"" + title + "\" has been recently downloaded/liked."
        + " You are up to " + numDownloads + " downloads and " + numLikes + " likes. Keep up the good work!\n\n\n"
        + "You can visit your app at " + currentHost + "/?galleryId=" + galleryId
        + " App Inventor will send you an email notification when the apps you have posted are liked or downloaded. "
        + "At most, one notification will be sent every 24 hours for each of your apps. "
        + "To modify the frequency of this notification email, please visit your profile page at "
        + currentHost;
  }
}
