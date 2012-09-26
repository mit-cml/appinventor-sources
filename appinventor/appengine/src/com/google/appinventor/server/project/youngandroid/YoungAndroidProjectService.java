// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.project.youngandroid;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.Server;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceFolderNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides support for Young Android projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author markf@google.com (Mark Friedman)
 */
public final class YoungAndroidProjectService extends CommonProjectService {

  private static final Logger LOG = Logger.getLogger(YoungAndroidProjectService.class.getName());
  
  // The value of this flag can be changed in appengine-web.xml
  private static final Flag<Boolean> sendGitVersion = 
    Flag.createFlag("build.send.git.version", true);

  // Project folder prefixes
  public static final String SRC_FOLDER = YoungAndroidSourceAnalyzer.SRC_FOLDER;
  protected static final String ASSETS_FOLDER = "assets";
  static final String PROJECT_DIRECTORY = "youngandroidproject";

  // TODO(user) Source these from a common constants library.
  private static final String FORM_PROPERTIES_EXTENSION =
      YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION;
  private static final String CODEBLOCKS_SOURCE_EXTENSION =
      YoungAndroidSourceAnalyzer.CODEBLOCKS_SOURCE_EXTENSION;

  public static final String PROJECT_PROPERTIES_FILE_NAME = PROJECT_DIRECTORY + "/" +
      "project.properties";

  // Maximum size of a generated apk file, in megabytes.
  private static final Flag<Float> maxApkSizeMegs = Flag.createFlag("max.apk.size.megs", 10f);

  private static final JSONParser JSON_PARSER = new ServerJsonParser();

  // Build folder path
  private static final String BUILD_FOLDER = "build";

  public static final String PROJECT_KEYSTORE_LOCATION = "android.keystore";

  private static final String KEYSTORE_FILE_NAME = YoungAndroidProjectService.PROJECT_DIRECTORY +
                                                   "/" + PROJECT_KEYSTORE_LOCATION;

  // host[:port] to use for connecting to the build server
  private static final Flag<String> buildServerHost =
      Flag.createFlag("build.server.host", "localhost:9990");

  public YoungAndroidProjectService(StorageIo storageIo) {
    super(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, storageIo);
  }

  /**
   * Returns project settings that can be used when creating a new project.
   */
  public static String getProjectSettings(String icon, String vCode, String vName) {
    icon = Strings.nullToEmpty(icon);
    vCode = Strings.nullToEmpty(vCode);
    vName = Strings.nullToEmpty(vName);
    return "{\"" + SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS + "\":{" +
        "\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON + "\":\"" +
        icon + "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE +
        "\":\"" + vCode +"\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME +
        "\":\"" + vName + "\"}}";
  }

  /**
   * Returns the contents of the project properties file for a new Young Android
   * project.
   *
   * @param projectName the name of the project
   * @param qualifiedName the qualified name of Screen1 in the project
   * @param icon the name of the asset to use as the application icon
   * @param vcode the version code
   * @param vname the version name
   */
  public static String getProjectPropertiesFileContents(String projectName, String qualifiedName,
      String icon, String vcode, String vname) {
    String contents = "main=" + qualifiedName + "\n" +
        "name=" + projectName + '\n' +
        "assets=../" + ASSETS_FOLDER + "\n" +
        "source=../" + SRC_FOLDER + "\n" +
        "build=../build\n";
    if (icon != null && !icon.isEmpty()) {
      contents += "icon=" + icon + "\n";
    }
    if (vcode != null && !vcode.isEmpty()) {
      contents += "versioncode=" + vcode + "\n";
    }
    if (vname != null && !vname.isEmpty()) {
      contents += "versionname=" + vname + "\n";
    }
    return contents;
  }

  private static String getFormPropertiesFileName(String qualifiedName) {
    return packageNameToPath(qualifiedName) + FORM_PROPERTIES_EXTENSION;
  }

  /**
   * Returns the contents of a new Young Android form file.
   * @param qualifiedName the qualified name of the form.
   * @return the contents of a new Young Android form file.
   */
  @VisibleForTesting
  public static String getInitialFormPropertiesFileContents(String qualifiedName) {
    final int lastDotPos = qualifiedName.lastIndexOf('.');
    String formName = qualifiedName.substring(lastDotPos + 1);
    // The initial Uuid is set to zero here since (as far as we know) we can't get random numbers
    // in ode.shared.  This shouldn't actually matter since all Uuid's are random int's anyway (and
    // 0 was randomly chosen, I promise).  The TODO(user) in MockComponent.java indicates that
    // there will someday be assurance that these random Uuid's are unique.  Once that happens
    // this will be perfectly acceptable.  Until that happens, choosing 0 is just as safe as
    // allowing a random number to be chosen when the MockComponent is first created.
    return "#|\n$JSON\n" +
        "{\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
        "\"Title\":\"" + formName + "\"}}\n|#";
  }

  /**
   * Returns the name of the codeblocks source file given a qualified form name
   */
  private static String getCodeblocksSourceFileName(String qualifiedName) {
    return packageNameToPath(qualifiedName) + CODEBLOCKS_SOURCE_EXTENSION;
  }

  /**
   * Returns the initial contents of a Young Android codeblocks file.
   */
  private static String getInitialCodeblocksSourceFileContents(String qualifiedName) {
    return "";
  }

  private static String packageNameToPath(String packageName) {
    return SRC_FOLDER + '/' + packageName.replace('.', '/');
  }

  public static String getSourceDirectory(String qualifiedName) {
    return StorageUtil.dirname(packageNameToPath(qualifiedName));
  }

  // CommonProjectService implementation

  @Override
  public void storeProjectSettings(String userId, long projectId, String projectSettings) {
    super.storeProjectSettings(userId, projectId, projectSettings);

    // If the icon has been changed, update the project properties file.
    // Extract the new icon from the projectSettings parameter.
    Settings settings = new Settings(JSON_PARSER, projectSettings);
    String newIcon = Strings.nullToEmpty(settings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON));
    String newVCode = Strings.nullToEmpty(settings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE));
    String newVName = Strings.nullToEmpty(settings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME));

    // Extract the old icon from the project.properties file from storageIo.
    String projectProperties = storageIo.downloadFile(userId, projectId,
        PROJECT_PROPERTIES_FILE_NAME, StorageUtil.DEFAULT_CHARSET);
    Properties properties = new Properties();
    try {
      properties.load(new StringReader(projectProperties));
    } catch (IOException e) {
      // Since we are reading from a String, I don't think this exception can actually happen.
      e.printStackTrace();
      return;
    }
    String oldIcon = Strings.nullToEmpty(properties.getProperty("icon"));
    String oldVCode = Strings.nullToEmpty(properties.getProperty("versioncode"));
    String oldVName = Strings.nullToEmpty(properties.getProperty("versionname"));

    if (!newIcon.equals(oldIcon) || !newVCode.equals(oldVCode) || !newVName.equals(oldVName)) {
      // Recreate the project.properties and upload it to storageIo.
      String projectName = properties.getProperty("name");
      String qualifiedName = properties.getProperty("main");
      String newContent = getProjectPropertiesFileContents(projectName, qualifiedName, newIcon, newVCode, newVName);
      storageIo.uploadFile(projectId, PROJECT_PROPERTIES_FILE_NAME, userId,
          newContent, StorageUtil.DEFAULT_CHARSET);
    }
  }

  /**
   * {@inheritDoc}
   *
   * {@code params} needs to be an instance of
   * {@link NewYoungAndroidProjectParameters}.
   */
  @Override
  public long newProject(String userId, String projectName, NewProjectParameters params) {
    NewYoungAndroidProjectParameters youngAndroidParams = (NewYoungAndroidProjectParameters) params;
    String qualifiedFormName = youngAndroidParams.getQualifiedFormName();

    String propertiesFileName = PROJECT_PROPERTIES_FILE_NAME;
    String propertiesFileContents = getProjectPropertiesFileContents(projectName,
        qualifiedFormName, null, null, null);

    String formFileName = getFormPropertiesFileName(qualifiedFormName);
    String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName);

    String codeblocksFileName = getCodeblocksSourceFileName(qualifiedFormName);
    String codeblocksFileContents = getInitialCodeblocksSourceFileContents(qualifiedFormName);

    Project project = new Project(projectName);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    // Project history not supported in legacy ode new project wizard
    project.addTextFile(new TextFile(propertiesFileName, propertiesFileContents));
    project.addTextFile(new TextFile(formFileName, formFileContents));
    project.addTextFile(new TextFile(codeblocksFileName, codeblocksFileContents));

    // Create new project
    return storageIo.createProject(userId, project, getProjectSettings("", "1", "1.0"));
  }

  @Override
  public long copyProject(String userId, long oldProjectId, String newName) {
    String oldName = storageIo.getProjectName(userId, oldProjectId);
    String oldProjectSettings = storageIo.loadProjectSettings(userId, oldProjectId);
    String oldProjectHistory = storageIo.getProjectHistory(userId, oldProjectId);
    Settings oldSettings = new Settings(JSON_PARSER, oldProjectSettings);
    String icon = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON);
    String vcode = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE);
    String vname = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME);

    Project newProject = new Project(newName);
    newProject.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    newProject.setProjectHistory(oldProjectHistory);

    // Get the old project's source files and add them to new project, modifying where necessary.
    for (String oldSourceFileName : storageIo.getProjectSourceFiles(userId, oldProjectId)) {
      String newSourceFileName;

      String newContents = null;
      if (oldSourceFileName.equals(PROJECT_PROPERTIES_FILE_NAME)) {
        // This is the project properties file. The name of the file doesn't contain the old
        // project name.
        newSourceFileName = oldSourceFileName;
        // For the contents of the project properties file, generate the file with the new project
        // name and qualified name.
        String qualifiedFormName = StringUtils.getQualifiedFormName(
            storageIo.getUser(userId).getUserEmail(), newName);
        newContents = getProjectPropertiesFileContents(newName, qualifiedFormName, icon, vcode, vname);
      } else {
        // This is some file other than the project properties file.
        // oldSourceFileName may contain the old project name as a path segment, surrounded by /.
        // Replace the old name with the new name.
        newSourceFileName = StringUtils.replaceLastOccurrence(oldSourceFileName,
            "/" + oldName + "/", "/" + newName + "/");
      }

      if (newContents != null) {
        // We've determined (above) that the contents of the file must change for the new project.
        // Use newContents when adding the file to the new project.
        newProject.addTextFile(new TextFile(newSourceFileName, newContents));
      } else {
        // If we get here, we know that the contents of the file can just be copied from the old
        // project. Since it might be a binary file, we copy it as a raw file (that works for both
        // text and binary files).
        byte[] contents = storageIo.downloadRawFile(userId, oldProjectId, oldSourceFileName);
        newProject.addRawFile(new RawFile(newSourceFileName, contents));
      }
    }

    // Create the new project and return the new project's id.
    return storageIo.createProject(userId, newProject, getProjectSettings(icon, vcode, vname));
  }

  @Override
  public ProjectRootNode getRootNode(String userId, long projectId) {
    // Create root, assets, and source nodes (they are mocked nodes as they don't really
    // have to exist like this on the file system)
    ProjectRootNode rootNode =
        new YoungAndroidProjectNode(storageIo.getProjectName(userId, projectId),
                                    projectId);
    ProjectNode assetsNode = new YoungAndroidAssetsFolder(ASSETS_FOLDER);
    ProjectNode sourcesNode = new YoungAndroidSourceFolderNode(SRC_FOLDER);

    rootNode.addChild(assetsNode);
    rootNode.addChild(sourcesNode);

    // Sources contains nested folders that are interpreted as packages
    Map<String, ProjectNode> packagesMap = Maps.newHashMap();

    // Retrieve project information
    for (String fileId : storageIo.getProjectSourceFiles(userId, projectId)) {
      if (fileId.startsWith(ASSETS_FOLDER + '/')) {
        // Assets is a flat folder
        assetsNode.addChild(new YoungAndroidAssetNode(StorageUtil.basename(fileId), fileId));

      } else if (fileId.startsWith(SRC_FOLDER + '/')) {
        // We only send form (.scm) nodes to the ODE client.
        // We don't send codeblocks source (.blk) nodes.
        if (fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
          YoungAndroidFormNode formNode = new YoungAndroidFormNode(fileId);
          String packageName = StorageUtil.getPackageName(formNode.getQualifiedName());
          ProjectNode packageNode = packagesMap.get(packageName);
          if (packageNode == null) {
            packageNode = new YoungAndroidPackageNode(packageName, packageNameToPath(packageName));
            packagesMap.put(packageName, packageNode);
            sourcesNode.addChild(packageNode);
          }
          packageNode.addChild(formNode);
        }
      }
    }

    return rootNode;
  }

  @Override
  public long addFile(String userId, long projectId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
      // If the file to be added is a new form, add a new form file and a new codeblocks file.
      String qualifiedFormName = YoungAndroidFormNode.getQualifiedName(fileId);
      String formFileName = getFormPropertiesFileName(qualifiedFormName);
      String codeblocksFileName = getCodeblocksSourceFileName(qualifiedFormName);

      List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
      if (!sourceFiles.contains(formFileName) &&
          !sourceFiles.contains(codeblocksFileName)) {

        String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, formFileName);
        storageIo.uploadFile(projectId, formFileName, userId, formFileContents,
            StorageUtil.DEFAULT_CHARSET);

        String codeblocksFileContents = getInitialCodeblocksSourceFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, codeblocksFileName);
        return storageIo.uploadFile(projectId, codeblocksFileName, userId, codeblocksFileContents,
            StorageUtil.DEFAULT_CHARSET);
      } else {
        throw new IllegalStateException("One or more files to be added already exists.");
      }

    } else {
      return super.addFile(userId, projectId, fileId);
    }
  }

  @Override
  public long deleteFile(String userId, long projectId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
      // If the file to be deleted is a form, delete the form file and the codeblocks file.
      String qualifiedFormName = YoungAndroidFormNode.getQualifiedName(fileId);
      String formFileName = getFormPropertiesFileName(qualifiedFormName);
      String codeblocksFileName = getCodeblocksSourceFileName(qualifiedFormName);
      storageIo.deleteFile(userId, projectId, formFileName);
      storageIo.deleteFile(userId, projectId, codeblocksFileName);
      storageIo.removeSourceFilesFromProject(userId, projectId, true,
          formFileName, codeblocksFileName);
      return storageIo.getProjectDateModified(userId, projectId);

    } else {
      return super.deleteFile(userId, projectId, fileId);
    }
  }

  /**
   * Make a request to the Build Server to build a project.  The Build Server will asynchronously
   * post the results of the build via the {@link com.google.appinventor.server.ReceiveBuildServlet}
   * A later call will need to be made by the client in order to get those results.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent)
   *
   * @return an RpcResult reflecting the call to the Build Server
   */
  @Override
  public RpcResult build(User user, long projectId, String target) {
    String userId = user.getUserId();
    String projectName = storageIo.getProjectName(userId, projectId);
    String outputFileDir = BUILD_FOLDER + '/' + target;
    // Delete the existing build output files, if any, so that future attempts to get it won't get
    // old versions.
    List<String> buildOutputFiles = storageIo.getProjectOutputFiles(userId, projectId);
    for (String buildOutputFile : buildOutputFiles) {
      storageIo.deleteFile(userId, projectId, buildOutputFile);
    }
    URL buildServerUrl = null;
    ProjectSourceZip zipFile = null;
    try {
      buildServerUrl = new URL(getBuildServerUrlStr(
          user.getUserEmail(),
          userId,
          projectId,
          outputFileDir));
      HttpURLConnection connection = (HttpURLConnection) buildServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      BufferedOutputStream bufferedOutputStream =
          new BufferedOutputStream(connection.getOutputStream());
      FileExporter fileExporter = new FileExporterImpl();
      zipFile = fileExporter.exportProjectSourceZip(userId, projectId, false,
          /* includeAndroidKeystore */ true,
          projectName + ".zip");
      bufferedOutputStream.write(zipFile.getContent());
      bufferedOutputStream.flush();
      bufferedOutputStream.close();

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        // Put the HTTP response code into the RpcResult so the client code in BuildCommand.java
        // can provide an appropriate error message to the user.
        // NOTE(lizlooney) - There is some weird bug/problem with HttpURLConnection. When the
        // responseCode is 503, connection.getResponseMessage() returns "OK", but it should return
        // "Service Unavailable". If I make the request with curl and look at the headers, they
        // have the expected error message.
        // For now, the moral of the story is: don't use connection.getResponseMessage().
        String error = "Build server responded with response code " + responseCode + ".";
        try {
          String content = readContent(connection.getInputStream());
          if (content != null && !content.isEmpty()) {
            error += "\n" + content;
          }
        } catch (IOException e) {
          // No content. That's ok.
        }
        try {
          String errorContent = readContent(connection.getErrorStream());
          if (errorContent != null && !errorContent.isEmpty()) {
            error += "\n" + errorContent;
          }
        } catch (IOException e) {
          // No error content. That's ok.
        }
        if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
          // The build server is not compatible with this App Inventor instance. Log this as severe
          // so the owner of the app engine instance will know about it.
          LOG.severe(error);
        }

        return new RpcResult(responseCode, "", StringUtils.escape(error));
      }
    } catch (MalformedURLException e) {
      CrashReport.createAndLogError(LOG, null, 
          buildErrorMsg("MalformedURLException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (IOException e) {
      CrashReport.createAndLogError(LOG, null, 
          buildErrorMsg("IOException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (EncryptionException e) {
      CrashReport.createAndLogError(LOG, null, 
          buildErrorMsg("EncryptionException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (RuntimeException e) {  
      // In particular, we often see RequestTooLargeException (if the zip is too 
      // big) and ApiProxyException. There may be others.
      Throwable wrappedException = e;
      if (e instanceof ApiProxy.RequestTooLargeException && zipFile != null) {
        int zipFileLength = zipFile.getContent().length;
        if (zipFileLength >= (5 * 1024 * 1024) /* 5 MB */) {
          wrappedException = new IllegalArgumentException(
              "Sorry, can't package projects larger than 5MB."
              + " Yours is " + zipFileLength + " bytes.", e);
        } else {
          wrappedException = new IllegalArgumentException(
              "Sorry, project was too large to package (" + zipFileLength + " bytes)");
        }
      }
      CrashReport.createAndLogError(LOG, null, 
          buildErrorMsg("RuntimeException", buildServerUrl, userId, projectId), wrappedException);
      return new RpcResult(false, "", wrappedException.getMessage());
    }
    return new RpcResult(true, "Building " + projectName, "");
  }
 
  private String buildErrorMsg(String exceptionName, URL buildURL, String userId, long projectId) {
    return "Request to build failed with " + exceptionName + ", user=" + userId
        + ", project=" + projectId + ", build URL is " + buildURL
        + " [" + buildURL.toString().length() + "]";
  }

  // Note that this is a function rather than just a constant because we assume it will get
  // a little more complicated when we want to get the URL from an App Engine config file or
  // command line argument.
  private String getBuildServerUrlStr(String userName, String userId,
                                      long projectId, String fileName)
      throws UnsupportedEncodingException, EncryptionException {
    return "http://" + buildServerHost.get() + "/buildserver/build-all-from-zip-async"
           + "?uname=" + URLEncoder.encode(userName, "UTF-8")
           + (sendGitVersion.get()
               ? "&gitBuildVersion=" 
                 + URLEncoder.encode(GitBuildId.getVersion(), "UTF-8")
               : "")
           + "&callback="
           + URLEncoder.encode("http://" + getCurrentHost() + ServerLayout.ODE_BASEURL_NOAUTH
                               + ServerLayout.RECEIVE_BUILD_SERVLET + "/"
                               + Security.encryptUserAndProjectId(userId, projectId)
                               + "/" + fileName,
                               "UTF-8");
  }

  private String getCurrentHost() {
    if (Server.isProductionServer()) {
      String applicationVersionId = SystemProperty.applicationVersion.get();
      String applicationId = SystemProperty.applicationId.get();
      return applicationVersionId + "." + applicationId + ".appspot.com";
    } else {
      // TODO(user): Figure out how to make this more generic
      return "localhost:8888";
    }
  }

  /*
   * Reads the UTF-8 content from the given input stream.
   */
  private static String readContent(InputStream stream) throws IOException {
    if (stream != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      try {
        return CharStreams.toString(reader);
      } finally {
        reader.close();
      }
    }
    return null;
  }

  /**
   * Check if there are any build results available for the given user's project
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent)
   * @return an RpcResult reflecting the call to the Build Server. The following values may be in
   *         RpcResult.result:
   *            0:  Build is done and was successful
   *            1:  Build is done and was unsuccessful
   *            2:  Yail generation failed
   *           -1:  Build is not yet done.
   */
  @Override
  public RpcResult getBuildResult(User user, long projectId, String target) {
    String userId = user.getUserId();
    String buildOutputFileName = BUILD_FOLDER + '/' + target + '/' + "build.out";
    List<String> outputFiles = storageIo.getProjectOutputFiles(userId, projectId);
    RpcResult buildResult = new RpcResult(-1, "", ""); // Build not finished yet
    for (String outputFile : outputFiles) {
      if (buildOutputFileName.equals(outputFile)) {
        String outputStr = storageIo.downloadFile(userId, projectId, outputFile, "UTF-8");
        try {
          JSONObject buildResultJsonObj = new JSONObject(outputStr);
          buildResult = new RpcResult(buildResultJsonObj.getInt("result"),
                                      buildResultJsonObj.getString("output"),
                                      buildResultJsonObj.getString("error"),
                                      outputStr);
        } catch (JSONException e) {
          buildResult = new RpcResult(1, "", "");
        }
        break;
      }
    }
    return buildResult;
  }
}
