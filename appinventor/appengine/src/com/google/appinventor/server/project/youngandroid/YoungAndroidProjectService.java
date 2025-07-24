// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.CODEBLOCKS_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.PROJECT_DIRECTORY;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.YAIL_FILE_EXTENSION;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.FileImporter;
import com.google.appinventor.server.FileImporterException;
import com.google.appinventor.server.FileImporterImpl;
import com.google.appinventor.server.GalleryExtensionException;
import com.google.appinventor.server.Server;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.util.UriBuilder;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceFolderNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Provides support for Young Android projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author markf@google.com (Mark Friedman)
 */
public final class YoungAndroidProjectService extends CommonProjectService {

  private static final Logger LOG = Logger.getLogger(YoungAndroidProjectService.class.getName());
  private static final int MB = 1024 * 1024;

  // The value of this flag can be changed in appengine-web.xml
  private static final Flag<Boolean> sendGitVersion =
    Flag.createFlag("build.send.git.version", true);

  private static final Flag<Integer> MAX_PROJECT_SIZE =
      Flag.createFlag("project.maxsize", 30);

  private static final String EXTERNAL_COMPS_FOLDER = ASSETS_FOLDER + "/external_comps";

  public static final String PROJECT_PROPERTIES_FILE_NAME = PROJECT_DIRECTORY + "/" +
      "project.properties";

  private static final JSONParser JSON_PARSER = new ServerJsonParser();

  // Build folder path
  private static final String BUILD_FOLDER = "build";

  // host[:port] to use for connecting to the build server
  private static final Flag<String> buildServerHost =
      Flag.createFlag("build.server.host", "localhost:9990");
  // host[:port] to use for connecting to the second build server
  private static final Flag<String> buildServerHost2 =
      Flag.createFlag("build2.server.host", "");
  // host[:port] to tell build server app host url
  private static final Flag<String> appengineHost =
      Flag.createFlag("appengine.host", "");
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  private static final String galleryLocation = Flag.createFlag("gallery.location", "http://localhost:9001").get();
  private static final String galleryId = Flag.createFlag("gallery.id", "").get();

  public YoungAndroidProjectService(StorageIo storageIo) {
    super(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, storageIo);
  }

  /**
   * Returns the contents of a new Young Android form file.
   * @param qualifiedName the qualified name of the form.
   * @return the contents of a new Young Android form file.
   */
  @VisibleForTesting
  public static String getInitialFormPropertiesFileContents(String qualifiedName) {
    final int lastDotPos = qualifiedName.lastIndexOf('.');
    String packageName = qualifiedName.split("\\.")[2];
    String formName = qualifiedName.substring(lastDotPos + 1);
    // The initial Uuid is set to zero here since (as far as we know) we can't get random numbers
    // in ode.shared.  This shouldn't actually matter since all Uuid's are random int's anyway (and
    // 0 was randomly chosen, I promise).  The TODO(user) in MockComponent.java indicates that
    // there will someday be assurance that these random Uuid's are unique.  Once that happens
    // this will be perfectly acceptable.  Until that happens, choosing 0 is just as safe as
    // allowing a random number to be chosen when the MockComponent is first created.
    return "#|\n$JSON\n" +
        "{\"authURL\":[]," +
        "\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
        "\"Title\":\"" + formName + "\",\"AppName\":\"" + packageName +"\"}}\n|#";
  }

  //when new project is created, checks for theme and toolkit
  public static String getInitialFormPropertiesFileContents(String qualifiedName, NewYoungAndroidProjectParameters youngAndroidParams) {
    final int lastDotPos = qualifiedName.lastIndexOf('.');
    String packageName = qualifiedName.split("\\.")[2];
    String formName = qualifiedName.substring(lastDotPos + 1);
    String themeName = youngAndroidParams.getThemeName();
    String blocksToolkit = youngAndroidParams.getBlocksToolkit();

    String newString = "#|\n$JSON\n" +
        "{\"authURL\":[]," +
        "\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
        "\"Title\":\"" + formName + "\",\"AppName\":\"" + packageName +"\",\"Theme\":\"" + 
        themeName + "\"}}\n|#";
    if (!blocksToolkit.isEmpty()){
        newString = "#|\n$JSON\n" +
        "{\"authURL\":[]," +
        "\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
        "\"Title\":\"" + formName + "\",\"AppName\":\"" + packageName +"\",\"Theme\":\"" + 
        themeName +  "\",\"BlocksToolkit\":" + JSONUtil.toJson(blocksToolkit) +"}}\n|#";
    }
    return newString;
  }

  /**
   * Returns the initial contents of a Young Android blockly blocks file.
   */
  private static String getInitialBlocklySourceFileContents(String qualifiedName) {
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
    YoungAndroidSettingsBuilder newProperties = new YoungAndroidSettingsBuilder(settings);

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
    YoungAndroidSettingsBuilder oldProperties = new YoungAndroidSettingsBuilder(properties);


    // Project settings do not include the name and package (main). So we add them
    // here so the comparison below is accurate. Before this change, we always write out the
    // project properties file, even when there are no changes to it.
    String projectName = properties.getProperty("name");
    String qualifiedName = properties.getProperty("main");
    newProperties.setProjectName(projectName)
      .setQualifiedFormName(qualifiedName);

    if (!oldProperties.equals(newProperties)) {
      // Recreate the project.properties and upload it to storageIo.
      String newContent = newProperties.toProperties();
      storageIo.uploadFileForce(projectId, PROJECT_PROPERTIES_FILE_NAME, userId,
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

    YoungAndroidSettingsBuilder builder = new YoungAndroidSettingsBuilder()
        .setProjectName(projectName)
        .setQualifiedFormName(qualifiedFormName);
    String propertiesFileContents = builder.toProperties();

    String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
    String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName, youngAndroidParams);

    String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
    String blocklyFileContents = getInitialBlocklySourceFileContents(qualifiedFormName);

    String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
    String yailFileContents = "";

    Project project = new Project(projectName);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    // Project history not supported in legacy ode new project wizard
    project.addTextFile(new TextFile(PROJECT_PROPERTIES_FILE_NAME, propertiesFileContents));
    project.addTextFile(new TextFile(formFileName, formFileContents));
    project.addTextFile(new TextFile(blocklyFileName, blocklyFileContents));
    project.addTextFile(new TextFile(yailFileName, yailFileContents));

    // Create new project
    return storageIo.createProject(userId, project, builder.build());
  }


  @Override
  public long copyProject(String userId, long oldProjectId, String newName) {
    String oldName = storageIo.getProjectName(userId, oldProjectId);
    String oldProjectSettings = storageIo.loadProjectSettings(userId, oldProjectId);
    String oldProjectHistory = storageIo.getProjectHistory(userId, oldProjectId);
    YoungAndroidSettingsBuilder builder = new YoungAndroidSettingsBuilder(
        new Settings(JSON_PARSER, oldProjectSettings));

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
        builder.setProjectName(newName).setQualifiedFormName(qualifiedFormName);
        newContents = builder.toProperties();
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
    return storageIo.createProject(userId, newProject, builder.build());
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
    ProjectNode compsNode = new YoungAndroidComponentsFolder(EXTERNAL_COMPS_FOLDER);

    rootNode.addChild(assetsNode);
    rootNode.addChild(sourcesNode);
    rootNode.addChild(compsNode);

    // Sources contains nested folders that are interpreted as packages
    Map<String, ProjectNode> packagesMap = Maps.newHashMap();

    // Retrieve project information
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
    for (String fileId : sourceFiles) {
      if (fileId.startsWith(ASSETS_FOLDER + '/')) {
        if (fileId.startsWith(EXTERNAL_COMPS_FOLDER + '/')) {
          compsNode.addChild(new YoungAndroidComponentNode(StorageUtil.basename(fileId), fileId));
        }
        else {
          assetsNode.addChild(new YoungAndroidAssetNode(StorageUtil.basename(fileId), fileId));
        }
      } else if (fileId.startsWith(SRC_FOLDER + '/')) {
        // We send form (.scm), blocks (.blk), and yail (.yail) nodes to the ODE client.
        YoungAndroidSourceNode sourceNode = null;
        if (fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
          sourceNode = new YoungAndroidFormNode(fileId);
        } else if (fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
          sourceNode = new YoungAndroidBlocksNode(fileId);
        } else if (fileId.endsWith(CODEBLOCKS_SOURCE_EXTENSION)) {
          String blocklyFileName =
              fileId.substring(0, fileId.lastIndexOf(CODEBLOCKS_SOURCE_EXTENSION))
              + BLOCKLY_SOURCE_EXTENSION;
          if (!sourceFiles.contains(blocklyFileName)) {
            // This is an old project that hasn't been converted yet. Convert
            // the blocks file to Blockly format and name. Leave the old
            // codeblocks file around for now (for debugging) but don't send it to the client.
            String blocklyFileContents = convertCodeblocksToBlockly(userId, projectId, fileId);
            storageIo.addSourceFilesToProject(userId, projectId, false, blocklyFileName);
            storageIo.uploadFileForce(projectId, blocklyFileName, userId, blocklyFileContents,
                StorageUtil.DEFAULT_CHARSET);
            sourceNode = new YoungAndroidBlocksNode(blocklyFileName);
          }
        } else if (fileId.endsWith(YAIL_FILE_EXTENSION)) {
          sourceNode = new YoungAndroidYailNode(fileId);
        }
        if (sourceNode != null) {
          String packageName = StorageUtil.getPackageName(sourceNode.getQualifiedName());
          ProjectNode packageNode = packagesMap.get(packageName);
          if (packageNode == null) {
            packageNode = new YoungAndroidPackageNode(packageName, packageNameToPath(packageName));
            packagesMap.put(packageName, packageNode);
            sourcesNode.addChild(packageNode);
          }
          packageNode.addChild(sourceNode);
        }
      }
    }

    return rootNode;
  }

  /*
   * Convert the contents of the codeblocks file named codeblocksFileId
   * to blockly format and return the blockly contents.
   */
  private String convertCodeblocksToBlockly(String userId, long projectId,
      String codeblocksFileId) {
    // TODO(sharon): implement this!
    return "";
  }

  @Override
  public long addFile(String userId, long projectId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
        fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
      // If the file to be added is a form file or a blocks file, add a new form file, a new
      // blocks file, and a new yail file (as a placeholder for later code generation)
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(fileId);
      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);

      List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
      if (!sourceFiles.contains(formFileName) &&
          !sourceFiles.contains(blocklyFileName) &&
          !sourceFiles.contains(yailFileName)) {

        String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, formFileName);
        storageIo.uploadFileForce(projectId, formFileName, userId, formFileContents,
            StorageUtil.DEFAULT_CHARSET);

        String blocklyFileContents = getInitialBlocklySourceFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, blocklyFileName);
        storageIo.uploadFileForce(projectId, blocklyFileName, userId, blocklyFileContents,
            StorageUtil.DEFAULT_CHARSET);

        String yailFileContents = "";  // start empty
        storageIo.addSourceFilesToProject(userId, projectId, false, yailFileName);
        return storageIo.uploadFileForce(projectId, yailFileName, userId, yailFileContents,
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
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
        fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
      // If the file to be deleted is a form file or a blocks file, delete both the form file
      // and the blocks file. Also, if there was a codeblocks file laying around
      // for that same form, delete it too (if it doesn't exist the delete
      // for it will be a no-op).
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(fileId);
      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String codeblocksFileName = YoungAndroidBlocksNode.getCodeblocksFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
      storageIo.deleteFile(userId, projectId, formFileName);
      storageIo.deleteFile(userId, projectId, blocklyFileName);
      storageIo.deleteFile(userId, projectId, codeblocksFileName);
      storageIo.deleteFile(userId, projectId, yailFileName);
      storageIo.removeSourceFilesFromProject(userId, projectId, true,
          formFileName, blocklyFileName, codeblocksFileName, yailFileName);
      return storageIo.getProjectDateModified(userId, projectId);

    } else {
      return super.deleteFile(userId, projectId, fileId);
    }
  }

  /**
   * Constructs a RpcResult object that indicates that a file was too big to send.
   *
   * @param size size of the aia
   * @return a new RpcResult with information for rendering an error in the client
   */
  private RpcResult fileTooBigResult(double size) {
    return new RpcResult(413, "", String.format(Locale.getDefault(),
        "{\"maxSize\":%d,\"aiaSize\":%f}", MAX_PROJECT_SIZE.get(), size / MB));
  }

  /**
   * Make a request to the Build Server to build a project.  The Build Server will asynchronously
   * post the results of the build via the {@link com.google.appinventor.server.ReceiveBuildServlet}
   * A later call will need to be made by the client in order to get those results.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param nonce random string used to find resulting APK from unauth context
   * @param target  build target (optional, implementation dependent)
   *
   * @return an RpcResult reflecting the call to the Build Server
   */
  @Override
  public RpcResult build(User user, long projectId, String nonce, String target,
      boolean secondBuildserver, boolean isAab) {
    String userId = user.getUserId();
    String projectName = storageIo.getProjectName(userId, projectId);
    String outputFileDir = BUILD_FOLDER + '/' + target;

    // Store the userId and projectId based on the nonce

    storageIo.storeNonce(nonce, userId, projectId);
    List<String> buildOutputFiles = storageIo.getProjectOutputFiles(userId, projectId);

    // Delete the existing build output files, if any, so that future attempts to get it won't get
    // old versions.
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
          secondBuildserver,
          outputFileDir,
          isAab));
      HttpURLConnection connection = (HttpURLConnection) buildServerUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
      FileExporter fileExporter = new FileExporterImpl();
      zipFile = fileExporter.exportProjectSourceZip(userId, projectId, false,
          /* includeAndroidKeystore */ true,
        projectName + ".aia", true, false, true, false, false, false);
      // The code below tests the size of the compressed project before
      // we send it off to the buildserver. When using URLFetch we know that
      // this size is limited to 10MB based on Google's documentation.
      // It isn't clear if this is also enforced in the Java 8 environment
      // when not using URLFetch. However we are being conservative for now.
      // Keep in mind that large projects can lead to large APK files which
      // may not be loadable into many memory restricted devices, so we
      // may not want to encourage large projects...
      if (zipFile.getContent().length > MAX_PROJECT_SIZE.get() * MB) {
        return fileTooBigResult(zipFile.getContent().length);
      }
      bufferedOutputStream.write(zipFile.getContent());
      bufferedOutputStream.flush();
      bufferedOutputStream.close();

      int responseCode = 0;
      responseCode = connection.getResponseCode();
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
      } else {
        // We get here if all went well and we sent the job to the
        // buildserver. Below we read the response, but throw it away.
        // We don't really care what was said. But we need to empty out
        // the TCP Stream or App Engine will abort the connection by
        // sending a RST packet instead of re-using it or closing it
        // cleanly (by sending a FIN packet). Aborting connections can
        // have a negative effect on some buildserver infrastructures,
        // particularly those based on docker swarm (as of 2018).
        readContent(connection.getInputStream());
      }
    } catch (MalformedURLException e) {
      CrashReport.createAndLogError(LOG, null,
          buildErrorMsg("MalformedURLException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (IOException e) {
      // As of App Engine 1.9.0 we get these when UrlFetch is asked to send too much data
      int zipFileLength = zipFile == null ? -1 : zipFile.getContent().length;
      if (zipFileLength >= MAX_PROJECT_SIZE.get() * MB) {
        return fileTooBigResult(zipFileLength);
      } else {
        return new RpcResult(false, "", e.getMessage());
      }
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
        if (zipFileLength >= MAX_PROJECT_SIZE.get() * MB) {
          return fileTooBigResult(zipFileLength);
        } else {
          wrappedException = new IllegalArgumentException(
              "Sorry, project was too large to package (" + zipFileLength + " bytes)");
        }
      } else {
        // Unexpected runtime error
        CrashReport.createAndLogError(LOG, null,
            buildErrorMsg("RuntimeException", buildServerUrl, userId, projectId), wrappedException);
      }
      return new RpcResult(false, "", wrappedException.getMessage());
    }
    return new RpcResult(true, "Building " + projectName, "");
  }

  public RpcResult loginToGallery(String userId) {
    String token = GalleryToken.makeToken(userId, 0, "");
    if (galleryId.isEmpty()) {
      return new RpcResult(-1, "", "Gallery Not Properly Configured");
    } else {
      return new RpcResult(0, galleryLocation + "/loginfromappinventor?token=" + token + "&id=" + galleryId, "");
    }
  }

  /*
   * Send a project to the new Gallery
   *
   * @param userId the user id
   * @param projectId the project ID to send
   */

  @Override
  public RpcResult sendToGallery(String userId, long projectId) {
    if (DEBUG) {
      LOG.info("sendToGallery userId = " + userId + " projectId = " + projectId);
    }
    if (galleryId.isEmpty()) {
      return new RpcResult(-1, "", "Gallery Not Properly Configured");
    }
    String projectName = storageIo.getProjectName(userId, projectId);
    URL newGalleryUrl = null;
    ProjectSourceZip zipFile = null;
    try {
      FileExporter fileExporter = new FileExporterImpl();
      zipFile = fileExporter.exportProjectSourceZip(userId, projectId, false,
        false, projectName + ".aia", false, false, true, true, false, false);
      String token = GalleryToken.makeToken(userId, projectId, projectName);
      newGalleryUrl = new URL(galleryLocation + "/fromappinventor?token=" +
        token + "&id=" + galleryId);
      HttpURLConnection connection = (HttpURLConnection) newGalleryUrl.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
      bufferedOutputStream.write(zipFile.getContent());
      bufferedOutputStream.flush();
      bufferedOutputStream.close();
      int responseCode = 0;
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        String error = "Got response code " + responseCode + ".";
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
        LOG.severe("SendToGallery: " + error);
        return new RpcResult(-1, "", error);
      } else {
        String returl = readContent(connection.getInputStream()); // Need to drain any response
        return new RpcResult(0, returl, "");
      }
    } catch (GalleryExtensionException e) {
      return new RpcResult(RpcResult.GALLERY_HAS_EXTENSION, "", "");
    } catch (Exception e) {
      throw CrashReport.createAndLogError(LOG, null, e.getMessage(), e);
    }
  }

  /**
   * Load a project from the new Gallery. This code will reach out and fetch
   * a project from the Gallery. We then store it with the user's projects and
   * return a UserProject object back to the user's browser so it can load the
   * newly stored project into the App Inventor UI.
   *
   * JIS: We send a GET request to the gallery which returns a
   * protocol buffer. This buffer contains the meta data we need for
   * the project (at this point, just its name). It may contain the
   * content itself (as a ZIP blob) or it may indicate that the ZIP
   * blob is at a different URL which we will then fetch (when we
   * implement it :-) ). This permits us to diversify the storage of
   * project AIA files. In fact it will let us leave projects from the
   * older gallery implementation in place in Google Cloud Storage
   * provided that we make those AIA files publicly readable (which I
   * believe they are)
   */

  @Override
  public UserProject loadFromGallery(String userId, String aGalleryId) throws IOException {
    if (DEBUG) {
      LOG.info("Before getURLContents (meta)");
    }
    final byte [] responseContent = getURLContents(galleryLocation + "/aia/" + aGalleryId);
    if (DEBUG) {
      LOG.info("After getURLContents (meta)");
    }
    byte[] aiaContents;
    if (responseContent == null) {
      throw new IOException("Cannot contact the Gallery, Try again later");
    }
    GalleryProtobuf.content content = GalleryProtobuf.content.parseFrom(responseContent);

    if (content.getCtype() == GalleryProtobuf.content.ContentType.DIRECT) {
      aiaContents = content.getContent().toByteArray();
    } else if (content.getCtype() == GalleryProtobuf.content.ContentType.URL) {
      LOG.info("Before getURLContents (data)");
      aiaContents = getURLContents(content.getUrlcontent());
      LOG.info("After getURLContents (data)");
    } else {
      throw new IOException("Unknown storage format for project.");
    }
    FileImporter fileImporter = new FileImporterImpl();
    // Generate a unique project name (only if conflict)
    LOG.info("Before checking project names");
    String newProjectName = verifyProjectName(userId, content.getProjectname());
    LOG.info("After checking project names");
    try {
      UserProject retval = fileImporter.importProject(userId, newProjectName,
        new ByteArrayInputStream(aiaContents));
      LOG.info("After fileImporter");
      return retval;
    } catch (FileImporterException e) {
      throw new IOException("Unable to import project");
    }
  }

  String buildErrorMsg(String exceptionName, URL buildURL, String userId, long projectId) {
    return "Request to build failed with " + exceptionName
      + ", user=" + userId + ", project=" + projectId
      + ", build URL is " + (buildURL != null ? buildURL : "null") + " ["
      + (buildURL != null ? buildURL.toString().length() : "n/a") + "]";
  }

  // Note that this is a function rather than just a constant because we assume it will get
  // a little more complicated when we want to get the URL from an App Engine config file or
  // command line argument.
  private String getBuildServerUrlStr(String userName, String userId,
    long projectId, boolean secondBuildserver, String fileName, boolean isAab)
      throws EncryptionException {
    UriBuilder uriBuilder = new UriBuilder(
        "http://"
            + (secondBuildserver ? buildServerHost2.get() : buildServerHost.get())
            + "/buildserver/build-all-from-zip-async")
        .add("uname", userName)
        .add("callback", "http://" + getCurrentHost() + ServerLayout.ODE_BASEURL_NOAUTH +
            ServerLayout.RECEIVE_BUILD_SERVLET + "/" +
            Security.encryptUserAndProjectId(userId, projectId) + "/" +
            fileName)
        .add("ext", isAab ? "aab" : "apk");
    if (sendGitVersion.get()) {
      uriBuilder.add("gitBuildVersion", GitBuildId.getVersion());
    }
    return uriBuilder.build();
  }

  private String getCurrentHost() {
    if (Server.isProductionServer()) {
      if (StringUtils.isNullOrEmpty(appengineHost.get())) {
        String applicationVersionId = SystemProperty.applicationVersion.get();
        String applicationId = SystemProperty.applicationId.get();
        return applicationVersionId + "." + applicationId + ".appspot.com";
      } else {
        return appengineHost.get();
      }
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
    RpcResult buildResult = new RpcResult(-1,
        Integer.toString(getCurrentProgress(user, projectId, target)),
        ""); // Build not finished
    for (String outputFile : outputFiles) {
      if (buildOutputFileName.equals(outputFile)) {
        String outputStr = storageIo.downloadFile(userId, projectId, outputFile, "UTF-8");
        try {
          JSONObject buildResultJsonObj = new JSONObject(outputStr);
          buildResult = new RpcResult(buildResultJsonObj.getInt("result"),
                                      buildResultJsonObj.getString("output"),
                                      buildResultJsonObj.getString("error"),
                                      outputStr);
          if (buildResultJsonObj.getInt("result") == 0) {
            storageIo.updateProjectBuiltDate(userId, projectId, System.currentTimeMillis());
          }
        } catch (JSONException e) {
          buildResult = new RpcResult(1, "", "");
        }
        break;
      }
    }
    return buildResult;
  }

  /**
   * Check if there are any build progress available for the given user's project
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent)
   */
  public int getCurrentProgress(User user, long projectId, String target) {
    return storageIo.getBuildStatus(user.getUserId(), projectId);
  }

  /**
   * This method reads from a stream based on the passed connection. It reads
   * the content as bytes, so it can deal with binary files
   *
   * @param connection the connection to read from
   * @return the contents of the stream
   * @throws IOException if it cannot read from the http connection
   */
  private static byte[] getResponseBytes(HttpURLConnection connection) throws IOException {
    // Use the content encoding to convert bytes to characters.
    InputStream input = connection.getInputStream();
    int bytesRead = 0;
    int contentLength = connection.getContentLength();
    LOG.info("contentLength = " + contentLength);
    byte buffer[] = new byte[contentLength];
    while (true) {
      int i = input.read(buffer, bytesRead, contentLength - bytesRead);
      if (i < 0) {
        break;
      }
      bytesRead += i;
    }
    LOG.info("Done, contentLenght = " + contentLength + " bytesRead = " + bytesRead);
    return buffer;
  }

  /*
   * Verify that the input projectName is unique among the user's
   * projects.
   */

  private String verifyProjectName(String userId, String projectName) {
    projectName = projectName.replace(" ", "_");
    int count = 0;
    List<Long> projectIds = storageIo.getProjects(userId);
    List<UserProject> projects = storageIo.getUserProjects(userId, projectIds);
    TreeSet<String> projectNames = new TreeSet();
    for (UserProject project : projects) {
      projectNames.add(project.getProjectName());
    }
    String baseProjectName = projectName;
    while (true) {
      if (count > 100) {
        throw CrashReport.createAndLogError(LOG, null, "Count exceeded in verifyProjectName", null);
      }
      if (!projectNames.contains(projectName)) {
        return projectName;
      }
      count += 1;
      projectName = baseProjectName + "_" + count;
    }
  }

  private static byte [] getURLContents(String url) throws IOException {
    try {
      URL Url = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) Url.openConnection();
      if (connection != null) {
        try {
          connection.setRequestMethod("GET");
          connection.setFollowRedirects(true);
          int responseCode = connection.getResponseCode();
          if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Got bad response code on read: " + responseCode);
          }
          return getResponseBytes(connection);
        } catch (ConnectException e) {
          throw new IOException("Connection Failure: " + e.getMessage());
        } catch (FileNotFoundException e) {
          throw new IOException("No Such Object: " + url);
        } finally {
          if (connection != null) {
            try {
              LOG.info("Before CLOSE");
              connection.disconnect();
              LOG.info("After CLOSE");
            } catch (Exception e) {
              // XXX
            }
          }
        }
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new IOException("Unable to read content: " + e.getMessage());
    }

  }

}
