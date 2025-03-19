// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.version.AppInventorFeatures;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.CsvParser;

import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.InvalidSessionException;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.util.Base64Util;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */

public class ProjectServiceImpl extends OdeRemoteServiceServlet implements ProjectService {

  private static final Logger LOG = Logger.getLogger(ProjectServiceImpl.class.getName());

  private static final long serialVersionUID = -8316312003804169166L;

  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  // RPC implementation for YoungAndroid projects
  private final transient YoungAndroidProjectService youngAndroidProject =
      new YoungAndroidProjectService(storageIo);

  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  /**
   * Creates a new project.
   * @param projectType  type of new project
   * @param projectName  name of new project
   * @param params  optional parameter (project type dependent)
   *
   * @return  a {@link UserProject} for new project
   */
  @Override
  public UserProject newProject(String projectType, String projectName,
                                NewProjectParameters params) {
    final String userId = userInfoProvider.getUserId();
    long projectId = getProjectRpcImpl(userId, projectType).
        newProject(userId, projectName, params);
    return makeUserProject(userId, projectId);
  }

  /**
   * Creates a new project from a zip file that is already stored
   *  on the server.
   * @param projectName  name of new project
   * @param pathToZip path the to template's zip file
   *
   * @return  a {@link UserProject} for new project
   */
  @Override
  public UserProject newProjectFromTemplate(String projectName, String pathToZip) {

    //Window.alert("newProjectFromTemplate " + host + pathToZip);
    //   System.out.println("newProjectFromTemplate = " +  host + pathToZip);
    UserProject userProject = null;
    try {
      FileInputStream fis = new FileInputStream(pathToZip);
      FileImporter fileImporter = new FileImporterImpl();
      userProject = fileImporter.importProject(userInfoProvider.getUserId(), projectName, fis);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "I/O Error importing from template project", e);
    } catch (FileImporterException e) {
      LOG.log(Level.SEVERE, "FileImporterException Error importing from template project", e);
    }

    return userProject;
  }

  /**
   * This service is passed a base64 encoded string representing the Zip file.
   * It converts it to a byte array and imports the project using FileImporter.
   *
   * @see http://stackoverflow.com/questions/6409587/
   *   generating-an-inline-image-with-java-gwt/6495356#6495356
   */
  @Override
  public UserProject newProjectFromExternalTemplate(String projectName, String zipData) {

    System.out.println(">>>>> ProjectService newProjectFromExternalTemplate name = " + projectName);
    UserProject userProject = null;

    // Convert base64 string to byte[]
    // NOTE: GWT's Base64Utils uses a non-standard algorithm.
    // @see:  https://code.google.com/p/google-web-toolkit/issues/detail?id=3880
    byte[] binData = null;
    binData = Base64Util.decodeLines(zipData);

    // Import the project
    ByteArrayInputStream bais = null;
    FileImporter fileImporter = new FileImporterImpl();
    try {
      bais = new ByteArrayInputStream(binData);
      userProject = fileImporter.importProject(userInfoProvider.getUserId(),
        projectName, bais);
    } catch (FileNotFoundException e) {  // Create a new empty project if no Zip
      LOG.log(Level.SEVERE, "File Not Found importing from template project (external)", e);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "I/O Error importing from template project (external)", e);
    } catch (FileImporterException e) {
      LOG.log(Level.SEVERE, "FileImporterException Error importing from template project (external)", e);
    }
    return userProject;
  }


  /**
   * Reads the template data from a JSON File
   * @param pathToTemplatesDir pathname of the templates directory which may contain
   *  0 or more template instances, each of which consists of a JSON file describing
   *  the template, plus a zip file and image files.
   *
   * @return A json-formatted String consisting of an array of template objects
   */
  @Override
  public String retrieveTemplateData(String pathToTemplatesDir) {
    String json = "[";
    File templatesRepository = new File(pathToTemplatesDir);
    File templateFolder[] = templatesRepository.listFiles();
    for (File file: templateFolder) {
      String templateName = file.getName();
      if (file.isDirectory()) {  // Should be a template folder
        File templateFiles[] = file.listFiles();
        for (File f: templateFiles) {
          if (f.isFile() && f.getName().equals(templateName + ".json")) {
            try {
              BufferedReader in = new BufferedReader(
                new FileReader(pathToTemplatesDir + "/" + templateName + "/" + templateName + ".json"));
              json += in.readLine() +  ", ";
            } catch (IOException e) {
              LOG.log(Level.SEVERE, "I/O Exception reading template json file: " + templateName, e);
              throw CrashReport.createAndLogError(LOG, getThreadLocalRequest(), null,
                new IllegalArgumentException("Cannot Read Internal Project Template"));
            }
          }
        }
      }
    }
    return json + "]";
  }

  /**
   * Copies a project with a new name.
   * @param oldProjectId  old project ID
   * @param newName new project name
   *
   * @return  a {@link UserProject} for new project
   */
  @Override
  public UserProject copyProject(long oldProjectId, String newName){
    final String userId = userInfoProvider.getUserId();
    long projectId = getProjectRpcImpl(userId, oldProjectId).
        copyProject(userId, oldProjectId, newName);
    return makeUserProject(userId, projectId);
  }

  /**
   * Deletes a project.
   * @param projectId  project ID
   */
  @Override
  public void deleteProject(long projectId) {
    final String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).deleteProject(userId, projectId);
  }

  /**
   * Moves the project to trash.
   * @param projectId  project ID
   */
  @Override
  public UserProject moveToTrash(long projectId) {
      String userId = userInfoProvider.getUserId();
      storageIo.setMoveToTrashFlag(userId,projectId,true);
      return storageIo.getUserProject(userId,projectId);
  }

  /**
   * Moves the project back to My Projects Tab.
   * @param projectId  project ID
   */
  @Override
  public UserProject restoreProject(long projectId) {
      String userId = userInfoProvider.getUserId();
      storageIo.setMoveToTrashFlag(userId,projectId,false);
      return storageIo.getUserProject(userId,projectId);
  }

  /**
   * Login to the new Gallery
   *
   * Generate a token used to login to the new gallery
   * @return RPC Result which contains URL to open a window on the Gallery
   */

  @Override
  public RpcResult loginToGallery() {
    final String userId = userInfoProvider.getUserId();
    return youngAndroidProject.loginToGallery(userId);
  }

  /**
   * Send project to new Gallery
   * @param projectId project ID
   * @return RPC Result which contains URL to open a window on the Gallery
   */

  @Override
  public RpcResult sendToGallery(long projectId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).sendToGallery(userId, projectId);
  }

  /**
   * Load a project from the Gallery
   * We take the galleryId, fetch the project from the (remote) Gallery
   * store it with the user's projects and return a UserProject object
   * to the client, which will then load the project into the UI
   * @param galleryId The unique id for the project in the gallery
   * @return UserProject Info for the UI to load the project
   *
   * Note: The server loads the project directly from the gallery
   *       it is *not* routed via the user's browser
   *
   */

  @Override
  public UserProject loadFromGallery(String galleryId) throws IOException {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId,
      YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE).loadFromGallery(userId, galleryId);
  }

  /**
   * Returns an array with project IDs.
   *
   * @return  IDs of projects found by the back-end
   */
  @Override
  public long[] getProjects() {
    List<Long> projects = storageIo.getProjects(userInfoProvider.getUserId());
    long[] projectIds = new long[projects.size()];
    int i = 0;
    for (Long project : projects) {
      projectIds[i++] = project;
    }
    return projectIds;
  }

  /**
   * Returns a list with pairs of project id and name.
   *
   * @return list of pairs of project IDs names found by backend
   */
  @Override
  public List<UserProject> getProjectInfos() {
    String userId = userInfoProvider.getUserId();
    List<Long> projectIds = storageIo.getProjects(userId);
    return makeUserProjects(userId, projectIds);
  }

  /**
   * Returns the root node for the given project.
   * @param projectId  project ID as received by {@link #getProjects()}
   *
   * @return  root node of project
   */
  @Override
  public ProjectRootNode getProject(long projectId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).getRootNode(userId, projectId);
  }

  /**
   * Returns a string with the project settings.
   * @param projectId  project ID
   *
   * @return  settings
   */
  @Override
  public String loadProjectSettings(long projectId) {
    String userId = userInfoProvider.getUserId();
    return storageIo.loadProjectSettings(userId, projectId);
  }

  /**
   * Stores a string with the project settings.
   * @param sessionId session id
   * @param projectId  project ID
   * @param settings  project settings
   */
  @Override
  public void storeProjectSettings(String sessionId, long projectId, String settings) throws InvalidSessionException {
    validateSessionId(sessionId);
    String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).storeProjectSettings(userId, projectId, settings);
  }

  /**
   * Deletes a file in the given project.
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  ID of file to delete
   * @return modification date for project
   */
  @Override
  public long deleteFile(String sessionId, long projectId, String fileId) throws InvalidSessionException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).deleteFile(userId, projectId, fileId);
  }

  /**
   * Deletes all files that are contained directly in the given directory. Files
   * in subdirectories are not deleted.
   * @param sessionId session id
   * @param projectId project ID
   * @param directory path of the directory
   * @return modification date for project
   */
  @Override
  public long deleteFiles(String sessionId, long projectId, String directory) throws InvalidSessionException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).deleteFiles(userId, projectId,
            directory);
  }

    /**
     * Deletes all files and folders that are contained inside the given directory. The given directory itself is deleted.
     * @param sessionId session id
     * @param projectId project ID
     * @param directory path of the directory
     * @return modification date for project
     */
  @Override
  public long deleteFolder(String sessionId, long projectId, String directory) throws InvalidSessionException {
      validateSessionId(sessionId);
      final String userId = userInfoProvider.getUserId();
      return getProjectRpcImpl(userId, projectId).deleteFolder(userId, projectId,
              directory);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  implementation dependent
   */
  @Override
  public String load(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).load(userId, projectId, fileId);
  }

  /**
   * Loads the file information associated with a node in the project tree. After
   * loading the file, the contents of it are parsed.
   *
   * <p>Expected format is either JSON or CSV. If the first character of the
   * file's contents is a left curly bracket ( { ), then JSON parsing is
   * attempted. Otherwise, CSV parsing is done.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  List of parsed columns (each column is a List of Strings)
   */
  @Override
  public List<List<String>> loadDataFile(long projectId, String fileId) {
    final int maxRows = 10; // Parse a maximum of 10 rows

    // Load the contents of the specified file
    String result = load(projectId, fileId);

    // If the contents of the file start with a curly bracket, assume JSON
    // and attempt parsing the contents as JSON. Otherwise, attempt to parse
    // the contents as a CSV file.
    if (result.startsWith("{")) {
      try {
        return parseJsonColumns(result, maxRows);
      } catch (JSONException e) {
        // JSON parsing failed; Attempt CSV parsing instead
        return parseCsvColumns(result, maxRows);
      }
    } else {
      return parseCsvColumns(result, maxRows);
    }
  }

  /**
   * Parses and returns columns from the specified String formatted
   * in CSV.
   *
   * @param source  Source String to parse CSV columns from
   * @param rows  Number of rows to parse
   * @return  List representing the columns (each column is a List of Strings)
   */
  private List<List<String>> parseCsvColumns(String source, int rows) {
    List<List<String>> columns = new ArrayList<List<String>>();

    // Construct an InputStream and a CSVParser for the contents of the file
    ByteArrayInputStream inputStream = new ByteArrayInputStream(source.getBytes());
    CsvParser csvParser = new CsvParser(inputStream);

    for (int i = 0; i <= rows && csvParser.hasNext(); ++i) {
      // Parse next row
      List<String> row = csvParser.next();

      // Add row entries to columns
      for (int j = 0; j < row.size(); ++j) {
        // A List for the column did not exist before; Create one
        if (columns.size() <= j) {
          columns.add(new ArrayList<String>());
        }

        // Add the j-th element of the row to the j-th column.
        // E.G. consider the CSV row 1,2,3,4
        // 1 goes into the 1st column, 2 goes into the 2nd one, and so on.
        // So the indexes for both the column and the row match.
        columns.get(j).add(row.get(j));
      }
    }

    return columns;
  }

  /**
   * Parses and returns columns from the specified String formatted
   * in JSON.
   *
   * @param source  Source String to parse JSON columns from
   * @param rows  Number of rows to parse
   * @return  List representing the columns (each column is a List of Strings)
   */
  private List<List<String>> parseJsonColumns(String source, int rows) throws JSONException {
    List<List<String>> columns = new ArrayList<List<String>>();

    // Parse a JSON value from the specified source String
    ServerJsonParser jsonParser = new ServerJsonParser();
    JSONValue value = jsonParser.parse(source);

    // Value must be a JSONObject for the parsing to be valid. If
    // that is not the case, skip column parsing.
    if (value instanceof JSONObject) {
      // Get the value as a JSONObject and retrieve the properties (key-value pairs)
      Map<String, JSONValue> properties = value.asObject().getProperties();

      // Iterate over all the entries (one entry is interpreted as a single column)
      for (final Map.Entry<String, JSONValue> entry : properties.entrySet()) {
        List<String> column = new ArrayList<String>();

        // Add the key as the first entry in the column
        column.add(entry.getKey());

        // Get the actual value of the column
        JSONValue entryValue = entry.getValue();

        // JSONArrays require different handling
        if (entryValue instanceof JSONArray) {
          // Retrieve the value as an Array, and get it's elements
          JSONArray entryArray = entryValue.asArray();
          List<JSONValue> jsonElements = entryArray.getElements();

          // A maximum of the specified rows should be parsed.
          int entries = Math.min(jsonElements.size(), rows);

          // Add all the entries from the array to the column
          for (int i = 0; i < entries; ++i) {
            JSONValue arrayValue = jsonElements.get(i);
            column.add(arrayValue.toString()); // Value has to be converted to String
          }
        } else {
          // Add the value as a String to the elements of the column
          column.add(entryValue.toString());
        }

        // Add the constructed column to the resulting columns List
        columns.add(column);
      }
    }

    return columns;
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * This version returns a ChecksumedLoadFile which contains the file content
   * and a MD5 checksum.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  implementation dependent
   */
  @Override
  public ChecksumedLoadFile load2(long projectId, String fileId) throws ChecksumedFileException {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).load2(userId, projectId, fileId);
  }

  /**
   * Attempt to record the project Id and error message when we detect a corruption
   * while loading a project.
   *
   * @param projectId project id
   * @param message Error message from the thrown exception
   *
   */
  @Override
  public void recordCorruption(long projectId, String fileId, String message) {
    final String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).recordCorruption(userId, projectId, fileId, message);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value is the raw file contents.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  raw file content
   */
  @Override
  public byte [] loadraw(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).loadraw(userId, projectId, fileId);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value is the raw file contents encoded as base64.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  raw file content as base 64
   */
  @Override
  public String loadraw2(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).loadraw2(userId, projectId, fileId);
  }

  /**
   * Loads the contents of multiple files.
   *
   * @param files  list containing file descriptor of files to be loaded
   * @return  list containing file descriptors and their associated content
   */
  @Override
  public List<FileDescriptorWithContent> load(List<FileDescriptor> files) {
    List<FileDescriptorWithContent> result = Lists.newArrayList();
    final String userId = userInfoProvider.getUserId();
    for (FileDescriptor file : files) {
      long projectId = file.getProjectId();
      String fileId = file.getFileId();
      result.add(new FileDescriptorWithContent(
          projectId, fileId,
          getProjectRpcImpl(userId, projectId).load(userId, projectId, fileId)));
    }
    return result;
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   *
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  project node whose source should be saved
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see #load(long, String)
   */
  @Override
  public long save(String sessionId, long projectId, String fileId, String content) throws InvalidSessionException {
    validateSessionId(sessionId);
    // Log parameters except for content
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).save(userId, projectId, fileId,
        content);
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   * This version takes a "force" argument which if false will result in an
   * exception of a trivial (empty) blocks workspace is attempted to be saved
   *
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  project node whose source should be saved
   * @param force whether to write an empty blocks workspace
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see #load(long, String)
   */
  @Override
  public long save2(String sessionId, long projectId, String fileId, boolean force, String content) throws InvalidSessionException,
      BlocksTruncatedException {
    validateSessionId(sessionId);
    // Log parameters except for content
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).save2(userId, projectId, fileId, force,
        content);
  }

  /**
   * Saves the contents of multiple files.
   *
   * @param sessionId session id
   * @param filesAndContent  list containing file descriptors and their
   *                         associated content
   * @return modification date for last modified project of list
   */
  @Override
  public long save(String sessionId, List<FileDescriptorWithContent> filesAndContent) throws InvalidSessionException,
      BlocksTruncatedException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    long date = 0;
    for (FileDescriptorWithContent fileAndContent : filesAndContent) {
     long projectId = fileAndContent.getProjectId();
     date = getProjectRpcImpl(userId, projectId).
         save(userId, projectId, fileAndContent.getFileId(), fileAndContent.getContent());
    }
    return date;
  }

  @Override
  public RpcResult screenshot(String sessionId, long projectId, String fileId, String content)
    throws InvalidSessionException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).screenshot(userId, projectId, fileId,
      content);
  }

  /**
   * Invokes a build command for the project on the back-end.
   *
   * @param projectId  project ID
   * @param target  build target (optional, implementation dependent)
   *
   * @return  results of build
   */
  @Override
  public RpcResult build(long projectId, String nonce, String target, boolean secondBuildserver, boolean isAab) {
    // Dispatch
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).build(
      userInfoProvider.getUser(), projectId, nonce, target, secondBuildserver, isAab);
  }

  /**
   * Gets the result of a build command for the project.
   *
   * @param projectId  project ID
   * @param target  build target (optional, implementation dependent)
   *
   * @return  results of build. The following values may be in RpcResult.result:
   *            0: Build is done and was successful
   *            1: Build is done and was unsuccessful
   *           -1: Build is not yet done.
   */
  @Override
  public RpcResult getBuildResult(long projectId, String target) {
    // Dispatch
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).getBuildResult(
      userInfoProvider.getUser(), projectId, target);
  }

  /*
   * Write the serialized response out to stdout. This is a very unusual thing
   * to do, but it allows us to create a static file version of the response
   * without deploying a servlet.
   *
   * Commented out by JIS 11/12/13
   */
  @Override
  protected void onAfterResponseSerialized(String serializedResponse) {
    // System.out.println(serializedResponse);  // COV_NF_LINE
  }

  private UserProject makeUserProject(String userId, long projectId) {
    return storageIo.getUserProject(userId, projectId);
  }

  // Bulk fetch UserProjects -- efficiently get all project infos asked for
  // using a minimum number of datastore API calls
  private List<UserProject> makeUserProjects(String userId, List<Long> projectIds) {
    return storageIo.getUserProjects(userId, projectIds);
  }

  /*
   * Returns the RPC implementation for the given project type.
   */
  private CommonProjectService getProjectRpcImpl(final String userId, long projectId) {
    String projectType = storageIo.getProjectType(userId, projectId);
    if (!projectType.isEmpty()) {
      return getProjectRpcImpl(userId, projectType);
    } else {
      throw CrashReport.createAndLogError(LOG, getThreadLocalRequest(),
          "user=" + userId + ", project=" + projectId,
          new IllegalArgumentException("Can't find project " + projectId));
    }
  }

  private CommonProjectService getProjectRpcImpl(final String userId, String projectType) {
    if (projectType.equals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE)) {
      return youngAndroidProject;
    } else {
      throw CrashReport.createAndLogError(LOG, getThreadLocalRequest(), null,
          new IllegalArgumentException("Unknown project type:" + projectType));
    }
  }

  @Override
  public long addFile(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).addFile(userId, projectId, fileId);
  }

  @Override
  public TextFile importMedia(String sessionId, long projectId, String url, boolean save)
      throws InvalidSessionException, IOException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).importMedia(userId, projectId, url, save);
  }

  @Override
  public void log(String message) {
    LOG.warning(message);
  }

  private void validateSessionId(String sessionId) throws InvalidSessionException {
    String storedSessionId = userInfoProvider.getSessionId();
    if (DEBUG) {
      if (storedSessionId == null) {
        LOG.info("storedSessionId is null");
      } else {
        LOG.info("storedSessionId = " + storedSessionId);
      }
    }
    if (sessionId.equals("force")) { // If we are forcing our way -- no check
      return;
    }
    if (!storedSessionId.equals(sessionId))
      if (AppInventorFeatures.requireOneLogin()) {
        throw new InvalidSessionException("A more recent login has occurred since we started. No further changes will be saved.");
      }
  }

}
