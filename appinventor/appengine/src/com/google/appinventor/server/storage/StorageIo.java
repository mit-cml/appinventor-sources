// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.SplashConfig;

import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * Interface of methods to simplify access to the storage systems.
 *
 * In all of the methods below that take a user id, it should be a string
 * that uniquely identifies the logged-in user and will continue to do so
 * indefinitely. It is up to the caller to choose the source of user ids.
 *
 */
public interface StorageIo {
  /**
   * Constant for an invalid project ID.
   */
  public static final long INVALID_PROJECTID = 0;

  // User management

  /**
   * Returns user data given user id. If the user data for the given id
   * doesn't already exist in the storage, it should be created.
   *
   * @param userId unique user id
   * @return user data
   */
  User getUser(String userId);

  /**
   * Returns user data given user id. If the user data for the given id
   * doesn't already exist in the storage, it should be created. email
   * is the email address currently associated with this user. If it
   * doesn't match the stored email address (or if the user doesn't exist yet)
   * the stored email address will be updated to this one.
   *
   * @param userId unique user id
   * @return user data
   */
  User getUser(String userId, String email);

  /**
   * Returns user data given user email address. If the user data for the given email
   * doesn't already exist in the storage, it should be created. email
   * is the email address currently associated with this user.
   *
   * @param user email address
   * @return user data
   */
  User getUserFromEmail(String email);

  /**
   * Sets the stored email address for user with id userId
   *
   */
  void setUserEmail(String userId, String email);

  /**
   * Sets that the user has accepted the terms of service.
   *
   * @param userId user id
   */
  void setTosAccepted(String userId);

  /**
   * Sets the user's session id value which is used to ensure only
   * one valid session exists for a user
   *
   * @param userId user id
   * @param sessionId the session id (uuid) value
   */
  void setUserSessionId(String userId, String sessionId);

  /**
   * Sets the user's hashed password.
   *
   * @param userId user id
   * @param hashed password
   */
  void setUserPassword(String userId, String password);

  /**
   * Returns a string with the user's settings.
   *
   * @param userId user id
   * @return settings
   */
  String loadSettings(String userId);

  /**
   * Stores a string with the user's settings.
   *
   * @param userId user ID
   * @param settings user's settings
   */
  void storeSettings(String userId, String settings);


  // Project management

  /**
   * Creates a new project and uploads the files.
   *
   * <p>
   * This is an atomic operation.
   *
   * @param userId user id
   * @param project project information
   * @param projectSettings project settings
   * @return project id
   */
  long createProject(String userId, Project project, String projectSettings);

  /**
   * Deletes a project and all its files.
   *
   * @param userId user ID
   * @param projectId project ID
   */
  void deleteProject(String userId, long projectId);

  /**
   * Sets the bit true and moves the project to trash.
   *
   * @param userId user ID
   * @param projectId project ID
   * @param flag
   */
  void setMoveToTrashFlag(final String userId, final long projectId, boolean flag);

  /**
   * Returns an array with the user's projects.
   *
   * @param userId  user ID
   * @return  list of projects
   */
  List<Long> getProjects(String userId);

  /**
   * Returns a string with the project settings.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId project ID
   * @return settings
   */
  String loadProjectSettings(String userId, long projectId);

  /**
   * Stores a string with the project settings.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param settings  project settings
   */
  void storeProjectSettings(String userId, long projectId, String settings);

  /**
   * Returns the project type.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  project type
   */
  String getProjectType(String userId, long projectId);

  /**
   * Returns the ProjectData object complete.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   * @return new UserProject object
   */

  UserProject getUserProject(String userId, long projectId);

  /**
   * Bulk version of getUserProject.
   * @param userId a userId
   * @param projectIds a List of project ids
   * @return new List of UserProject objects
   */

  List<UserProject> getUserProjects(String userId, List<Long> projectIds);

  /**
   * Returns a project name.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   * @return project name
   */
  String getProjectName(String userId, long projectId);

  /**
   * Returns the date the project was last modified.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */
  long getProjectDateModified(String userId, long projectId);

  /**
   * Returns the date the project was last exported.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */
  long getProjectDateBuilt(String userId, long projectId);

  /**
   * Sets the date the project was last exported.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   * @long  builtDate the date to set
   *
   * @return long milliseconds
   */
  long updateProjectBuiltDate(String userId, long projectId, long builtDate);

  /**
   * Returns the specially formatted list of project history.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return String specially formatted history
   */
  String getProjectHistory(String userId, long projectId);

  // JIS XXX
  /**
   * Returns the date the project was created.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */
  long getProjectDateCreated(String userId, long projectId);
 /**
   * Returns the gallery id or -1 if not published.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project id
   *
   * @return long milliseconds
   */

  // Non-project-specific file management

  /**
   * Adds file IDs to the user's list of non-project-specific files.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileIds list of file IDs to add to the projects source file list
   */
  void addFilesToUser(String userId, String... fileIds);

  /**
   * Returns a list of non-project-specific files for a user.
   *
   * @param userId a user Id
   * @return list of source file ID
   */
  List<String> getUserFiles(String userId);

  /**
   * Uploads a non-project-specific file.
   *
   * @param userId user ID
   * @param fileId file ID
   * @param content file content
   * @param encoding encoding of content
   */
  void uploadUserFile(String userId, String fileId, String content, String encoding);

  /**
   * Uploads a non-project-specific file.
   *
   * @param userId user ID
   * @param content file content
   * @param fileName file name
   */
  void uploadRawUserFile(String userId, String fileName, byte[] content);

  /**
   * Downloads text user file data.
   *
   * @param userId a user Id
   * @param fileId file ID
   * @param encoding encoding of text file
   *
   * @return text file content
   */
  String downloadUserFile(String userId, String fileId, String encoding);

  /**
   * Downloads raw user file data.
   *
   * @param userId a user Id
   * @param fileName file name
   *
   * @return file content
   */
  byte[] downloadRawUserFile(String userId, String fileName);

  /**
   * Deletes a user file.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param fileId  file ID
   */
  void deleteUserFile(String userId, String fileId);

  // File management

  /**
   * Returns the maximum allowed job size in bytes.
   *
   * @return int maximum job size in bytes
   */
  int getMaxJobSizeBytes();

  /**
   * Adds file IDs to the project's list of source files, updating the
   * modification date of the project if requested.  Note that no
   * modification date is returned.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param changeModDate  update the modification time for the project
   * @param fileIds  list of file IDs to add to the projects source file list
   */
  void addSourceFilesToProject(String userId, long projectId, boolean changeModDate,
      String...fileIds);

  /**
   * Add file IDs to the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileIds  list of file IDs to add to the projects output file list
   */
  void addOutputFilesToProject(String userId, long projectId, String...fileIds);

  /**
   * Removes file IDs from the project's list of source files, updating the
   * modification date of the project if requested.  Note that no
   * modification date is returned.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param changeModDate  update the modification time for the project
   * @param fileIds  list of file IDs to add to the projects source file list
   */
  void removeSourceFilesFromProject(String userId, long projectId, boolean changeModDate,
      String...fileIds);

  /**
   * Removes file IDs from the project's list of output files.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileIds  list of file IDs to add to the projects source file list
   */
  void removeOutputFilesFromProject(String userId, long projectId, String...fileIds);

  /**
   * Returns a list of source files for a project.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  list of source file ID
   */
  List<String> getProjectSourceFiles(String userId, long projectId);

  /**
   * Returns a list of output files for a project.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   *
   * @return  list of output file ID
   */
  List<String> getProjectOutputFiles(String userId, long projectId);

  /**
   * Uploads a file.
   * @param projectId  project ID
   * @param fileId  file ID
   * @param userId the user who owns the file
   * @param content  file content
   * @param encoding encoding of content
   * @return modification date for project
   */
  long uploadFile(long projectId, String fileId, String userId, String content, String encoding)
      throws BlocksTruncatedException;

  /**
   * Uploads a file. -- This version uses "force" to write even a trivial workspace file
   * @param projectId  project ID
   * @param fileId  file ID
   * @param userId the user who owns the file
   * @param content  file content
   * @param encoding encoding of content
   * @return modification date for project
   */
  long uploadFileForce(long projectId, String fileId, String userId, String content, String encoding);

  /**
   * Uploads a file.
   * @param projectId  project ID
   * @param fileId  file ID
   * @param userId the user who owns the file
   * @param force write file even if it is a trivial workspace
   * @param content  file content
   * @return modification date for project
   */
  long uploadRawFile(long projectId, String fileId, String userId, boolean force, byte[] content)
      throws BlocksTruncatedException;

  /**
   * Uploads a file. -- forces the save even with trivial workspace
   * @param projectId  project ID
   * @param fileId  file ID
   * @param userId the user who owns the file
   * @param content  file content
   * @return modification date for project
   */
  long uploadRawFileForce(long projectId, String fileId, String userId, byte[] content);

  /**
   * Deletes a file.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileId  file ID
   * @return modification date for project
   */
  long deleteFile(String userId, long projectId, String fileId);

  /**
   * Downloads text file data.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileId  file ID
   * @param encoding  encoding of text file
   *
   * @return  text file content
   */
  String downloadFile(String userId, long projectId, String fileId, String encoding);

  /**
   * Records a "corruption" record so we can analyze if corruption is
   * happening.
   *
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param message The message from the exception on the client
   */

  void recordCorruption(String userId, long projectId, String fileId, String message);

  /**
   * Downloads raw file data.
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param fileId  file ID
   *
   * @return  file content
   */
  byte[] downloadRawFile(String userId, long projectId, String fileId);

  /**
   * Creates a temporary file with the given content and returns
   * its file name, which will always begin with __TEMP__
   * @param content the files content (bytes)
   *
   * @return fileName the temporary filename
   */
  String uploadTempFile(byte [] content) throws IOException;

  /**
   * Open an input stream to a temp file.
   * Verifies it is a temp file by making sure the filename
   * begins with __TEMP__
   *
   * @param fileName
   *
   * @return inputstream
   */

  InputStream openTempFile(String fileName) throws IOException;

  /**
   * delete a temporary file.
   * Verify that it is a temporary file by making sure its filename
   * starts with __TEMP__
   *
   * @param fileName
   */

  void deleteTempFile(String fileName) throws IOException;

  // MOTD management

  /**
   * Returns the most recent motd.
   *
   * @return  motd
   */
  Motd getCurrentMotd();

  /**
   *  Exports project files as a zip archive
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param includeProjectHistory  whether or not to include the project history
   * @param includeAndroidKeystore  whether or not to include the Android keystore
   * @param zipName  the name of the zip file, if a specific one is desired
   * @param fatalError set true to cause missing GCS file to throw exception
   *
   * @return  project with the content as requested by params.
   */
  ProjectSourceZip exportProjectSourceZip(String userId, long projectId,
    boolean includeProjectHistory,
    boolean includeAndroidKeystore,
    @Nullable String zipName,
    final boolean includeYail,
    final boolean includeScreenShots,
    final boolean forGallery,
    final boolean fatalError) throws IOException;

  /**
   * Find a user's id given their email address. Note that this query is case
   * sensitive!
   *
   * @param email user's email address
   *
   * @return the user's id if found
   * @throws NoSuchElementException if we can't find a user with that exact
   *    email address
   */
  String findUserByEmail(String email) throws NoSuchElementException;

  /**
   * Find a phone's IP address given the six character key. Used by the
   * RendezvousServlet. This is used only when memcache is unavailable.
   *
   * @param key the six character key
   * @return Ip Address as string or null if not found
   *
   */
  String findIpAddressByKey(String key);

  /**
   * Store a phone's IP address indexed by six character key. Used by the
   * RendezvousServlet. This is used only when memcache is unavailable.
   *
   * Note: Nothing currently cleans up these entries, but we have a
   * timestamp field which we update so a later process can recognize
   * and remove stale entries.
   *
   * @param key the six character key
   * @param ipAddress the IP Address of the phone
   *
   */
  void storeIpAddressByKey(String key, String ipAddress);

  boolean checkWhiteList(String email);

  void storeFeedback(final String notes, final String foundIn, final String faultData,
    final String comments, final String datestamp, final String email, final String projectId);

  Nonce getNoncebyValue(String nonceValue);
  void storeNonce(final String nonceValue, final String userId, final long projectId);

  // Cleanup expired nonces
  void cleanupNonces();

  // Retrieve the current Splash Screen Version
  SplashConfig getSplashConfig();

  StoredData.PWData createPWData(String email);
  StoredData.PWData findPWData(String uid);
  void cleanuppwdata();

  // Routines for user admin interface

  List<AdminUser> searchUsers(String partialEmail);
  void storeUser(AdminUser user) throws AdminInterfaceException;

  /**
   * There are two kinds of backpacks. User backpacks, which are
   * stored with the user's personal files (which today is just the
   * backpack and the android keystore used for signing applications.
   * The second kind of backpack is a shared backpack. It is
   * identified by a uuid.  This code is associated with the shared
   * backpack. Shared backpacks are used when a person is logged in
   * via the SSO mechanism. It can optionally specify a backpack to
   * use. If it doesn't specify a backpack, then the normal user
   * specific version is used.
   *
   * @param backPackId uuid used to idenfity this backpack
   * @return the contents of the backpack as an XML encoded string
   */

  public String downloadBackpack(String backPackId);

  /**
   * Used to upload a shared backpack Note: This code will over-write
   * whatever contents is already stored in the the backpack. It is
   * the responsibility of our caller to merge contents if desired.
   *
   * @param backPackId The uuid of the shared backpack to store
   * @param String content the new contents of the backpack
   */

  public void uploadBackpack(String backPackId, String content);

  /**
   * Store the status of a pending build. We used to poll the buildserver
   * for the progress of a build. However that was never correct as while
   * polling you would likely wind up talking to a different buildserver
   * then you originally started with! So now the buildserver does a callback
   * to the server indicating progress. Here is where we store that
   * progress. The reason we do this in this module is because we have
   * different versions of storageio for our three (so far) backends, the
   * App Engine based version, the stand alone version and the "scale-able"
   * version. Each version will likely want to store this information in
   * a different fashion.
   *
   * Note: The App Engine version uses memcache and if memcache isn't
   * available (yes, it can be down!) then we cheat and just return
   * 50 (for 50%).
   *
   */

  public void storeBuildStatus(String userId, long projectId, int progress);

  public int getBuildStatus(String userId, long projectId);

  /**
   * Checks that the user identified by {@code userId} has a reference to the project identified
   * by {@code projectId}. If a corresponding UserProjectData is not found, this function throws
   * a SecurityException to indicate unauthorized access.
   *
   * @param userId id for the user
   * @param projectId id for the project
   * @throws SecurityException if the user doesn't have access to the project
   */
  void assertUserHasProject(String userId, long projectId);

  List<String> getTutorialsUrlAllowed();

  /**
   * Delete a user account.
   *
   * This requires that all of the user's projects are delete, or at
   * least has the projectMovedToTrashFlag set.  If so, this method
   * will remove all vestiges of the user's account and returns
   * true. Otherwise returns false.
   *
   * Note: If a user attempts to login again after this method is run,
   * a new account will automatically be created.
   *
   * @param userId id for the user
   * @return true on successful account deletion, otherwise false
   */
  boolean deleteAccount(String userId);

  String getIosExtensionsConfig();

}


