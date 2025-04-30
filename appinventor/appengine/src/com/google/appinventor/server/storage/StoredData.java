// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

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
public class StoredData {
  // The UserData class is an entity root, and the parent of UserFileData
  // and UserProjectData
  @Unindexed
  @Cached
  public static final class UserData {
    // The Google Account userid
    @Id public String id;

    @Indexed public String email;
    @Indexed public String emaillower;

    // User settings
    public String settings;

    // Has user accepted terms of service?
    boolean tosAccepted;
    boolean isAdmin;            // Internal flag for local login administrators

    @Indexed public Date visited; // Used to figure out if a user is active. Timestamp when settings are stored.

    public String name;
    public String link;
    public int emailFrequency;
    public int type;
    String sessionid;           // uuid of active session
    String password;            // Hashed (PBKDF2 hashing) password

    // Path to template project passed as GET parameter
    String templatePath;
    boolean upgradedGCS;
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

    //introduction link
    String link;

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

    // File settings
    // TODO(user): is this ever used?
    String settings;
  }

  // Project files
  // Note: FileData has to be Serializable so we can put it into
  //       memcache.
  @Cached
  @Unindexed
  static final class FileData implements Serializable {
    // The role that file play: source code, build target or temporary file
    enum RoleEnum {
      SOURCE,
      TARGET,
      TEMPORARY
    }

    // The file name
    @Id String fileName;

    // Key of the project (parent) to which this file belongs
    @Parent Key<ProjectData> projectKey;

    // File role
    RoleEnum role;

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

    // File settings
    String settings;

    // DateTime of last backup only used if GCS is enabled
    long lastBackup;

    String userId;              // The userId which owns this file
                                // if null or the empty string, we haven't initialized
                                // it yet
  }

  // MOTD data.
  @Unindexed
  static final class MotdData {
    // Unique Id - for now we expect there to be only 1 MotdData object.
    @Id Long id;

    // Caption for the MOTD
    String caption;

    // More MOTD detail, if any
    String content;
  }

  // Rendezvous Data -- Only used when memcache is unavailable
  @Unindexed
  static final class RendezvousData {
    @Id Long id;

    // Six character key entered by user (or scanned).
    @Indexed public String key;

    // Ip Address of phone
    public String ipAddress;

    public Date used;           // Used during (manual) cleanup to determine if this entry can be pruned

  }

  @Unindexed
  static final class WhiteListData {
    @Id Long id;
    @Indexed public String emailLower;
  }

  @Unindexed
  static final class FeedbackData {
    @Id Long id;
    public String notes;
    public String foundIn;
    public String faultData;
    public String comments;
    public String datestamp;
    public String email;
    public String projectId;
  }

  // NonceData -- A unique (and obscure) nonce is used to map between
  // the nonce string and a userId and projectId. This is used to provide
  // for unauthenticated download of an APK file. Nonces are timestamped
  // both to provide a way to clean them up and to expire the APK downloads.

  @Unindexed
  static final class NonceData {
    @Id Long id;
    @Indexed public String nonce;
    public String userId;
    public long projectId;
    @Indexed
    public Date timestamp;
  }

  @Unindexed
  static final class CorruptionRecord {
    @Id Long id;
    @Indexed public Date timestamp;
    public String userId;
    public long projectId;
    public String fileId;
    public String message;
  }

  @Cached(expirationSeconds=60)
  @Unindexed
  static final class SplashData {
    @Id Long id;
    public int version;
    public String content;
    public int height;
    public int width;
  }

  // Data Structure to keep track of url's emailed out for password
  // setting and reseting. The Id (which is a UUID) is part of the URL
  // that is mailed out.
  @Unindexed
  public static final class PWData {
    @Id public String id;              // "Secret" URL part
    @Indexed public Date timestamp; // So we know when to expire this objects
    public String email;            // Email of account in question
  }

  // A Shared backpack. Shared backpacks are not associated with
  // any one user. Instead they are stored independent of projects
  // and users. At login time a shared backpack may be specified.
  // This requires an SSO Login from an external system to provide
  // it.
  @Cached(expirationSeconds=120)
  @Unindexed
  public static final class Backpack {
    @Id public String id;
    public String content;
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
  public static final class AllowedIosExtensions {
    // Unique Id - for now we expect there to be only 1 AllowedIosExtensions object.
    @Id Long id;

    // list of allowed extension packages as JSON
    String allowedExtensions;
  }

  public static final class ProjectNotFoundException extends IOException {
    ProjectNotFoundException(String message) {
      super(message);
    }
  }

}
