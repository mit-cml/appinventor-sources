// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage.database.datastore;

import com.google.appinventor.server.storage.FileDataRoleEnum;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

import javax.persistence.Id;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Classes for the data objects that are stored in the Objectify database.
 *
 * TODO(user): for now I just defined a bunch of classes (parallel
 * to the ones in the old ode.proto). It might be
 * worth considering whether to make these classes extend DAOBasic (does it buy
 * us anything) and whether to add any methods for manipulating the objects.
 *
 * TODO(user): consider separating these out into individual class
 * files - more Java-y?
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
final class StoredData {
  // The UserData class is an entity root, and the parent of UserFileData
  // and UserProjectData
  @Unindexed
  @Cached
  static final class UserData {
    // The Google Account userid
    @Id String id;

    @Indexed String email;
    @Indexed String emaillower;

    // User settings
    String settings;

    // Has user accepted terms of service?
    boolean tosAccepted;
    boolean isAdmin;            // Internal flag for local login administrators

    @Indexed Date visited; // Used to figure out if a user is active. Timestamp when settings are stored.

    String name;
    String link;
    int type;
    String sessionid;           // uuid of active session
    String password;            // Hashed (PBKDF2 hashing) password
  }

  // Project properties
  // The ProjectData class is an entity root, and the parent of FileData
  @Cached
  @Unindexed
  static final class ProjectData {
    // Auto-generated unique project id
    @Id Long id;

    // Verbose project name
    String name;

    // Project type. Currently Simple and YoungAndroid
    // TODO(user): convert to enum
    String type;

    // Global project settings
    String settings;

    // Date project created
    // TODO(): Make required
    long dateCreated;

    // Date project last modified
    // TODO(): Make required
    long dateModified;

    // Date project last built/exported as a runnable binary
    long dateBuilt;

    // The specially formatted project history
    String history;

    //adding a boolean variable to mark deleted project
    boolean projectMovedToTrashFlag;
  }

  // Project properties specific to the user
  @Unindexed
  static final class UserProjectData {
    enum StateEnum {
      CLOSED,
      OPEN,
      DELETED
    }

    // The project id
    @Id long projectId;

    // The user (parent's) key
    @Parent Key<UserData> userKey;

    // State of the project relative to user
    // TODO(user): is this ever used?
    StateEnum state;

    // User specific project settings
    // TODO(user): is this ever used?
    String settings;
  }

  // Non-project-specific files (tied to user)
  @Unindexed
  static final class UserFileData {
    // The file name
    @Id String fileName;

    // The user (parent's) key
    @Parent Key<UserData> userKey;

    // File content, these are raw bytes. Note that Objectify automatically
    // converts byte[] to Blob.
    byte[] content;
  }

  // Project files
  // Note: FileData has to be Serializable so we can put it into
  //       memcache.
  @Cached
  @Unindexed
  static final class FileData implements Serializable {
    // The file name
    @Id String fileName;

    // Key of the project (parent) to which this file belongs
    @Parent Key<ProjectData> projectKey;

    // File role
    FileDataRoleEnum role;

    // File content, these are raw bytes. Note that Objectify automatically
    // converts byte[] to an App Engine Datastore Blob (which is not the same thing as a Blobstore
    // Blob).  Consequently, if isBlob is true, the content field should be ignored and the data
    // should be retrieved from Blobstore.
    byte[] content;

    // Is this file stored in Blobstore.  If it is, the blobstorePath will contain the path to use
    // to retrieve the data from Blobstore.
    boolean isBlob;

    // The Blobstore path to use to get the data from Blobstore
    String blobstorePath;

    // The Blobstore key. This is filled in by a MapReduce job run outside of App Inventor
    String blobKey;

    // Is this file stored in the Google Cloud Store (GCS). If it is the gcsName will contain the
    // GCS file name (sans bucket).
    Boolean isGCS = false;

    // The GCS filename, sans bucket name
    String gcsName;

    // DateTime of last backup only used if GCS is enabled
    long lastBackup;

    String userId;              // The userId which owns this file
                                // if null or the empty string, we haven't initialized
                                // it yet
  }

  @Unindexed
  static final class WhiteListData {
    @Id Long id;
    @Indexed String emailLower;
  }

  @Unindexed
  static final class FeedbackData {
    @Id Long id;
    String notes;
    String foundIn;
    String faultData;
    String comments;
    String datestamp;
    String email;
    String projectId;
  }

  // NonceData -- A unique (and obscure) nonce is used to map between
  // the nonce string and a userId and projectId. This is used to provide
  // for unauthenticated download of an APK file. Nonces are timestamped
  // both to provide a way to clean them up and to expire the APK downloads.

  @Unindexed
  static final class NonceData {
    @Id Long id;
    @Indexed String nonce;
    String userId;
    long projectId;
    @Indexed
    Date timestamp;
  }

  @Unindexed
  static final class CorruptionRecord {
    @Id Long id;
    @Indexed Date timestamp;
    String userId;
    long projectId;
    String fileId;
    String message;
  }

  @Cached(expirationSeconds=60)
  @Unindexed
  static final class SplashData {
    @Id Long id;
    int version;
    String content;
    int height;
    int width;
  }

  // Data Structure to keep track of url's emailed out for password
  // setting and reseting. The Id (which is a UUID) is part of the URL
  // that is mailed out.
  @Unindexed
  static final class PWData {
    @Id String id;              // "Secret" URL part
    @Indexed Date timestamp; // So we know when to expire this objects
    String email;            // Email of account in question
  }

  // A Shared backpack. Shared backpacks are not associated with
  // any one user. Instead they are stored independent of projects
  // and users. At login time a shared backpack may be specified.
  // This requires an SSO Login from an external system to provide
  // it.
  @Cached(expirationSeconds=120)
  @Unindexed
  static final class Backpack {
    @Id String id;
    String content;
  }

  @Cached(expirationSeconds=120)
  @Unindexed
  static final class AllowedTutorialUrls {
    // Unique Id - for now we expect there to be only 1 MotdData object.
    @Id Long id;

    // list of allowed Urls as JSON
    // we use JSON here to make it easier to hand edit via
    // datastore editing tools
    String allowedUrls;

  }

  @Cached(expirationSeconds = 120)
  @Unindexed
  static final class AllowedIosExtensions {
    // Unique Id - for now we expect there to be only 1 AllowedIosExtensions object.
    @Id Long id;

    // list of allowed extension packages as JSON
    String allowedExtensions;
  }

  static final class ProjectNotFoundException extends IOException {
    ProjectNotFoundException(String message) {
      super(message);
    }
  }

}
