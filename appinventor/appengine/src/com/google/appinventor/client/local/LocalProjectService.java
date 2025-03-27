// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.jzip.GenerateOptions;
import com.google.appinventor.client.jzip.JSZip;
import com.google.appinventor.client.jzip.LoadOptions;
import com.google.appinventor.client.jzip.TextDecoder;
import com.google.appinventor.client.jzip.TextEncoder;
import com.google.appinventor.client.jzip.Type;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.client.settings.project.YoungAndroidSettings;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
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
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalProjectService implements ProjectServiceAsync {
  private final Map<String, UserProject> projects = new HashMap<>();
  private final Map<Long, ProjectRootNode> projectData = new HashMap<>();
  private final Map<String, ArrayBuffer> contents = new HashMap<>();

  String getProjectName(String projectId) {
    long hash = Long.parseLong(projectId);
    for (Map.Entry<String, UserProject> entry : projects.entrySet()) {
      if (entry.getValue().getProjectId() == hash) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Override
  public void newProject(String projectType, String projectName, NewProjectParameters params,
      AsyncCallback<UserProject> callback) {
    long hash = projectName.hashCode();
    UserProject project = new UserProject(hash, projectName, projectType,
        System.currentTimeMillis(), System.currentTimeMillis(), false);
    projects.put(projectName, project);
    YoungAndroidProjectNode root = new YoungAndroidProjectNode(projectName, hash);
    ProjectNode assetsNode = new YoungAndroidAssetsFolder("assets");
    YoungAndroidSourceFolderNode sourcesNode = new YoungAndroidSourceFolderNode("src");
    ProjectNode compsNode = new YoungAndroidComponentsFolder("assets/external_comps");
    root.addChild(assetsNode);
    root.addChild(sourcesNode);
    root.addChild(compsNode);
    projectData.put(hash, root);
    NewYoungAndroidProjectParameters youngAndroidParams = (NewYoungAndroidProjectParameters) params;
    String packageName = youngAndroidParams.getPackageName();
    YoungAndroidPackageNode packageNode = new YoungAndroidPackageNode(packageName,
        packageNameToPath(packageName));
    sourcesNode.addChild(packageNode);

    String qualifiedName = youngAndroidParams.getQualifiedFormName();
    String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedName);
    String formFileContents = getInitialFormPropertiesFileContents(qualifiedName, youngAndroidParams);
    contents.put(hash + ":" + formFileName, new TextEncoder("utf-8").encode(formFileContents));
    packageNode.addChild(new YoungAndroidFormNode(formFileName));

    String blocksFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedName);
    contents.put(hash + ":" + blocksFileName, new TextEncoder("utf-8").encode(""));
    packageNode.addChild(new YoungAndroidBlocksNode(blocksFileName));

    String projectPropertiesFileName = "youngandroidproject/project.properties";
    String projectPropertiesFileContents = "sizing=Responsive\n" +
        "color.primary.dark=&HFF303F9F\n" +
        "color.primary=&HFF3F51B5\n" +
        "color.accent=&HFFFF4081\n" +
        "aname=rl_maze\n" +
        "defaultfilescope=App\n" +
        "main=" + qualifiedName + ".Screen1\n" +
        "source=../src\n" +
        "actionbar=True\n" +
        "useslocation=False\n" +
        "assets=../assets\n" +
        "build=../build\n" +
        "name=" + projectName + "\n" +
        "showlistsasjson=True\n" +
        "theme=AppTheme.Light.DarkActionBar\n" +
        "versioncode=1\n" +
        "versionname=1.0\n";
    contents.put(hash + ":" + projectPropertiesFileName,
        new TextEncoder("utf-8").encode(projectPropertiesFileContents));

    callback.onSuccess(project);
  }

  /*
   * Note that this is copied from
   * {@link com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService}.
   * We should refactor this into a common class under the rpc package.
   */
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

  @Override
  public void newProjectFromTemplate(String projectName, String pathToZip,
      AsyncCallback<UserProject> callback) {

  }

  private static String packageNameToPath(String packageName) {
    return "src/" + packageName.replace('.', '/');
  }

  @Override
  public void newProjectFromExternalTemplate(String projectName, String zipData,
      AsyncCallback<UserProject> callback) {
    UserProject project = new UserProject(projectName.hashCode(), projectName, "YoungAndroid", System.currentTimeMillis(), System.currentTimeMillis(), false);
    projects.put(projectName, project);
    long hash = projectName.hashCode();
    YoungAndroidProjectNode root = new YoungAndroidProjectNode(projectName, hash);
    ProjectNode assetsNode = new YoungAndroidAssetsFolder("assets");
    ProjectNode sourcesNode = new YoungAndroidSourceFolderNode("src");
    ProjectNode compsNode = new YoungAndroidComponentsFolder("assets/external_comps");
    root.addChild(assetsNode);
    root.addChild(sourcesNode);
    root.addChild(compsNode);
    projectData.put(hash, root);

    // Process the zip data
    final JSZip zip = new JSZip();
    zip.loadAsync(zipData, LoadOptions.create(true))
        .then0(() -> {
          final Map<String, ProjectNode> packagesMap = new HashMap<>();
          final List<Promise> promises = new ArrayList<>();
          zip.forEach((name, zipObject) -> {
            promises.add(
              zipObject.get(Type.ARRAY_BUFFER).then((buffer) -> {
                contents.put(hash + ":" + name, buffer);
                log(contents);
                return Promise.resolve(buffer);
              })
            );
            if (name.startsWith("assets/")) {
              if (name.startsWith("assets/external_comps/")) {
                // This is a file in the external components directory
                compsNode.addChild(new YoungAndroidComponentNode(StorageUtil.basename(name), name));
              } else {
                // This is a file in the assets directory
                assetsNode.addChild(new YoungAndroidAssetNode(StorageUtil.basename(name), name));
              }
            } else if (name.startsWith("src/")) {
              YoungAndroidSourceNode sourceNode = null;
              if (name.endsWith(".scm")) {
                sourceNode = new YoungAndroidFormNode(name);
              } else if (name.endsWith(".bky")) {
                sourceNode = new YoungAndroidBlocksNode(name);
              } else if (name.endsWith(".yail")) {
                sourceNode = new YoungAndroidYailNode(name);
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
          });
          Promise[] promisesArray = promises.toArray(new Promise[0]);
          return Promise.allOf(promisesArray).then0(() -> {
            callback.onSuccess(project);
            return Promise.resolve(null);
          });
        })
        .error(err -> {
          callback.onFailure(err);
          return null;
        });
  }

  @Override
  public void retrieveTemplateData(String pathToTemplates, AsyncCallback<String> callback) {
    callback.onSuccess("[]");
  }

  @Override
  public void copyProject(long oldProjectId, String newName, AsyncCallback<UserProject> callback) {

  }

  @Override
  public void moveToTrash(long projectId, AsyncCallback<UserProject> callback) {

  }

  @Override
  public void restoreProject(long projectId, AsyncCallback<UserProject> callback) {

  }

  public void renameProjects(List<Long> projectIds, List<String> projectNames,
      AsyncCallback<Void> callback) {

  }

  @Override
  public void loginToGallery(AsyncCallback<RpcResult> callback) {

  }

  @Override
  public void sendToGallery(long projectId, AsyncCallback<RpcResult> callback) {

  }

  @Override
  public void loadFromGallery(String galleryId, AsyncCallback<UserProject> callback) {

  }

  @Override
  public void deleteProject(long projectId, AsyncCallback<Void> callback) {

  }

  @Override
  public void getProjects(AsyncCallback<long[]> callback) {
  }

  @Override
  public void getProjectInfos(AsyncCallback<List<UserProject>> callback) {
    callback.onSuccess(new ArrayList<>());
  }

  @Override
  public void getProject(long projectId, AsyncCallback<ProjectRootNode> callback) {
    callback.onSuccess(projectData.get(projectId));
  }

  @Override
  public void loadProjectSettings(long projectId, AsyncCallback<String> callback) {
    ArrayBuffer contents = this.contents.get(projectId + ":youngandroidproject/project.properties");
    if (contents != null) {
      TextDecoder decoder = new TextDecoder("utf-8");
      String content = decoder.decode(contents);
      try {
        String[] lines = content.split("\n");
        Map<String, String> properties = new HashMap<>();
        for (String line : lines) {
          String[] parts = line.split("=");
          if (parts.length == 2) {
            properties.put(parts[0].trim(), parts[1].trim());
          }
        }
        ProjectSettings settings = new ProjectSettings(
            Ode.getInstance().getProjectManager().getProject(projectId));
        YoungAndroidSettings child = (YoungAndroidSettings) settings.getSettings(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_ICON,
            properties.getOrDefault("icon", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_VERSION_CODE,
            properties.getOrDefault("versioncode", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_VERSION_NAME,
            properties.getOrDefault("versionname", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_USES_LOCATION,
            properties.getOrDefault("useslocation", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_APP_NAME,
            properties.getOrDefault("aname", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_SIZING,
            properties.getOrDefault("sizing", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON,
            properties.getOrDefault("showlistsasjson", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_TUTORIAL_URL,
            properties.getOrDefault("tutorialurl", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET,
            properties.getOrDefault("subsetjson", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_ACTIONBAR,
            properties.getOrDefault("actionbar", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_THEME,
            properties.getOrDefault("theme", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR,
            properties.getOrDefault("color.primary", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK,
            properties.getOrDefault("color.primary.dark", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_ACCENT_COLOR,
            properties.getOrDefault("color.accent", ""));
        child.changePropertyValue(YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE,
            properties.getOrDefault("defaultfilescope", ""));
        callback.onSuccess(settings.encodeSettings());
      } catch (Exception e) {
        callback.onFailure(e);
      }
    } else {
      callback.onFailure(new Exception("File not found"));
    }
  }

  @Override
  public void storeProjectSettings(String sessionId, long projectId, String settings,
      AsyncCallback<Void> callback) {
    callback.onSuccess(null);
  }

  @Override
  public void deleteFile(String sessionId, long projectId, String fileId,
      AsyncCallback<Long> callback) {

  }

  @Override
  public void deleteFiles(String sessionId, long projectId, String directory,
      AsyncCallback<Long> callback) {

  }

  @Override
  public void deleteFolder(String sessionId, long projectId, String directory,
      AsyncCallback<Long> callback) {

  }

  @Override
  public void load(long projectId, String fileId, AsyncCallback<String> callback) {

  }

  @Override
  public void loadDataFile(long projectId, String fileId,
      AsyncCallback<List<List<String>>> callback) {

  }

  @Override
  public void load2(long projectId, String fileId, final AsyncCallback<ChecksumedLoadFile> callback) {
    ArrayBuffer buffer = contents.get(projectId + ":" + fileId);
    if (buffer != null) {
      TextDecoder decoder = new TextDecoder("utf-8");
      String content = decoder.decode(buffer);
      final ChecksumedLoadFile file = new ChecksumedLoadFile();
      try {
        file.setContent(content);
      } catch (Exception e) {
        callback.onFailure(e);
        return;
      }
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          callback.onSuccess(file);
        }
      });
    } else {
      callback.onFailure(new Exception("File not found"));
    }
  }

  @Override
  public void recordCorruption(long ProjectId, String fileId, String message,
      AsyncCallback<Void> callback) {

  }

  @Override
  public void loadraw(long projectId, String fileId, AsyncCallback<byte[]> callback) {

  }

  @Override
  public void loadraw2(long projectId, String fileId, AsyncCallback<String> callback) {

  }

  @Override
  public void load(List<FileDescriptor> files,
      AsyncCallback<List<FileDescriptorWithContent>> callback) {

  }

  @Override
  public void save(String sessionId, long projectId, String fileId, String source,
      AsyncCallback<Long> callback) {
    save2(sessionId, projectId, fileId, false, source, callback);
  }

  @Override
  public void save2(String sessionId, long projectId, String fileId, boolean force, String source,
      AsyncCallback<Long> callback) {
    TextEncoder encoder = new TextEncoder("utf-8");
    contents.put(projectId + ":" + fileId, encoder.encode(source));
    callback.onSuccess(System.currentTimeMillis());
  }

  @Override
  public void save(String sessionId, List<FileDescriptorWithContent> filesAndContent,
      AsyncCallback<Long> callback) {
    for (FileDescriptorWithContent file : filesAndContent) {
      TextEncoder encoder = new TextEncoder("utf-8");
      contents.put(file.getProjectId() + ":" + file.getFileId(), encoder.encode(file.getContent()));
    }
    callback.onSuccess(System.currentTimeMillis());
  }

  @Override
  public void screenshot(String sessionId, long projectId, String fileId, String content,
      AsyncCallback<RpcResult> callback) {

  }

  @Override
  public void build(long projectId, String nonce, String target, boolean secondBuildserver,
      boolean isAab, AsyncCallback<RpcResult> callback) {

  }

  @Override
  public void getBuildResult(long projectId, String target, AsyncCallback<RpcResult> callback) {

  }

  @Override
  public void addFile(long projectId, String fileId, AsyncCallback<Long> callback) {

  }

  @Override
  public void importMedia(String sessionId, long projectId, String url, boolean save,
      AsyncCallback<TextFile> odeAsyncCallback) {

  }

  @Override
  public void log(String message, AsyncCallback<Void> callback) {

  }

  public Promise<String> exportProject(long projectId) {
    JSZip zip = new JSZip();
    for (String key : contents.keySet()) {
      if (key.startsWith(projectId + ":")) {
        String name = key.split(":")[1];
        zip.putFile(name, contents.get(key));
      }
    }
    return zip.generateAsync(GenerateOptions.create(Type.BASE64));
  }

  private static native void log(Object msg)/*-{
    console.log(msg);
  }-*/;
}
