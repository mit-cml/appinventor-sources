// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse.Status;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ComponentServiceImpl extends OdeRemoteServiceServlet
    implements ComponentService {

  private static final String CANNOT_UPGRADE_MESSAGE = "An extension containing %s already exists" +
      " on the server but could not be upgraded. The new extension was not loaded.";
  private static final Logger LOG =
      Logger.getLogger(ComponentServiceImpl.class.getName());

  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  private final FileImporter fileImporter = new FileImporterImpl();

  /**
   * ExtensionDowngradeException is thrown when ComponentServiceImpl detects that an extension
   * bundle upgrade may result in an existing component disappearing from the set of known
   * components. The user should be warned when such a situation occurs and we will consider it
   * the fault of the extension developer for deleting extensions from a previously published
   * bundle.
   */
  public static class ExtensionDowngradeException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new ExtensionDowngradeException.
     *
     * @param typeName The typeName of the extension that caused the exception.
     */
    public ExtensionDowngradeException(String typeName) {
      super("Cannot downgrade to extension missing previously defined type " + typeName);
    }
  }

  @Override
  public ComponentImportResponse importComponentToProject(String fileOrUrl, long projectId,
      String folderPath) {
    ComponentImportResponse response = new ComponentImportResponse(ComponentImportResponse.Status.FAILED);
    response.setProjectId(projectId);

    if (isUnknownSource(fileOrUrl)) {
      response.setStatus(ComponentImportResponse.Status.UNKNOWN_URL);
      return response;
    }

    Map<String, byte[]> contents;
    String fileNameToDelete = null;
    try {
      if (fileOrUrl.startsWith("__TEMP__")) {
        fileNameToDelete = fileOrUrl;
        contents = extractContents(storageIo.openTempFile(fileOrUrl));
      } else {
        URL compUrl = new URL(fileOrUrl);
        contents = extractContents(compUrl.openStream());
      }
      importToProject(contents, projectId, folderPath, response);
      return response;
    } catch (FileImporterException | IOException | JSONException | IllegalArgumentException e) {
      response.setStatus(Status.FAILED);
      response.setMessage(e.getMessage());
      return response;
    } finally {
      if (fileNameToDelete != null) {
        try {
          storageIo.deleteTempFile(fileNameToDelete);
        } catch (Exception e) {
          throw CrashReport.createAndLogError(LOG, null,
            collectImportErrorInfo(fileOrUrl, projectId), e);
        }
      }
    }
  }

  @Override
  public void renameImportedComponent(String fullyQualifiedName, String newName,
      long projectId) {
    String fileName = "assets/external_comps/" + fullyQualifiedName + "/component.json";

    JSONObject compJson = new JSONObject(storageIo.downloadFile(
        userInfoProvider.getUserId(), projectId, fileName, StorageUtil.DEFAULT_CHARSET));
    compJson.put("name", newName);

    try {
      storageIo.uploadFile(projectId, fileName, userInfoProvider.getUserId(),
          compJson.toString(2), StorageUtil.DEFAULT_CHARSET);
    } catch (BlocksTruncatedException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "Error renaming the short name of " + fullyQualifiedName + " to " +
          newName + " in project " + projectId, e);
    }
  }

  @Override
  public void deleteImportedComponent(String fullyQualifiedName, long projectId) {
    String directory = "assets/external_comps/" + fullyQualifiedName + "/";
    for (String fileId : storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId)) {
      if (fileId.startsWith(directory)) {
        storageIo.deleteFile(userInfoProvider.getUserId(), projectId, fileId);
        storageIo.removeSourceFilesFromProject(userInfoProvider.getUserId(), projectId, false, fileId);
      }
    }
  }

  private Map<String, byte[]> extractContents(InputStream inputStream)
      throws IOException {
    Map<String, byte[]> contents = new HashMap<String, byte[]>();

    // assumption: the zip is non-empty
    ZipInputStream zip = new ZipInputStream(inputStream);
    ZipEntry entry;
    while ((entry = zip.getNextEntry()) != null) {
      if (entry.isDirectory())  continue;
      ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
      ByteStreams.copy(zip, contentStream);
      contents.put(entry.getName(), contentStream.toByteArray());
    }
    zip.close();

    return contents;
  }

  private void importToProject(Map<String, byte[]> contents, long projectId,
      String folderPath, ComponentImportResponse response) throws FileImporterException, IOException {
    Status status = Status.IMPORTED;
    final String userId = userInfoProvider.getUserId();
    final String basepath = folderPath + "/external_comps/";
    Set<String> sourceFiles = new HashSet<>(storageIo.getProjectSourceFiles(userInfoProvider.getUserId(), projectId));
    Map<String, String> nameMap = buildExtensionPathnameMap(contents.keySet());

    // Does the extension contain a file that could be a component descriptor file?
    if (!nameMap.containsKey("component.json") && !nameMap.containsKey("components.json")) {
      response.setMessage("Uploaded file does not contain any component definition files.");
      return;
    }

    // Rename/upgrade component.json to components.json
    JSONArray newComponents = readExtensionComponents(contents, nameMap);
    if (newComponents == null || newComponents.length() == 0) {
      response.setMessage("No valid component descriptors found in the extension.");
      return;
    }

    // Upgrade old extensions, if any
    NavigableMap<String, Set<String>> existingExtensions = findExtensions(basepath, sourceFiles);
    Set<String> oldTypes = matchExtensions(existingExtensions, getExtensionClasses(newComponents));
    if (oldTypes.size() > 0) {
      // upgrade of one or more existing extensions
      try {
        Map<String, JSONObject> componentMap = makeComponentMap(newComponents);
        boolean willCollide = sourceFiles.contains(basepath + nameMap.get("classes.jar"));
        Iterator<String> i = oldTypes.iterator();
        while (i.hasNext()) {
          String extension = i.next();
          if (upgradeOldExtension(userId, projectId, basepath, extension,
              existingExtensions.get(extension), componentMap)) {
            status = Status.UPGRADED;
          } else if (willCollide) {
            // collision but we are not upgrading an existing extension? abort!
            response.setStatus(Status.FAILED);
            response.setMessage(String.format(CANNOT_UPGRADE_MESSAGE, extension));
            return;
          } else {
            // no overlap between the old and new extensions, so don't delete!
            i.remove();
          }
        }
        // save extension renames
        if (Status.UPGRADED.equals(status)) {
          contents.put(nameMap.get("components.json"),
              newComponents.toString().getBytes(StorageUtil.DEFAULT_CHARSET));
        }
      } catch(ExtensionDowngradeException e) {
        response.setStatus(Status.BUNDLE_DOWNGRADE);
        response.setMessage(e.getMessage());
        return;
      }
    }

    // Write new extension files
    List<ProjectNode> compNodes = new ArrayList<>();
    for (Map.Entry<String, byte[]> entry : contents.entrySet()) {
      String dest = basepath + entry.getKey();
      FileNode fileNode = new YoungAndroidComponentNode(StorageUtil.basename(entry.getKey()), dest);
      fileImporter.importFile(userId, projectId, dest, new ByteArrayInputStream(entry.getValue()));
      compNodes.add(fileNode);
    }

    // Delete old extension files
    // NB: If an exception kills us here the project will be in an inconsistent state since Google
    // doesn't guarantee atomicity of writes to both the data store and cloud store.
    for (String extension : oldTypes) {
      for (String file : existingExtensions.get(extension)) {
        if (!contents.containsKey(file.replace(basepath, ""))) {  // don't delete new files
          storageIo.deleteFile(userId, projectId, file);
          storageIo.removeSourceFilesFromProject(userId, projectId, false, file);
        }
      }
    }

    // Extract type map to send to clients
    Map<String, String> types = new TreeMap<>();
    for (int i = 0; i < newComponents.length(); i++) {
      JSONObject desc = newComponents.getJSONObject(i);
      types.put(desc.getString("type"), desc.getString("name"));
    }

    response.setStatus(status);
    response.setComponentTypes(types);
    response.setNodes(compNodes);
  }

  /**
   * Upgrade an existing extension in the project with a new extension. If the old extension had a
   * custom name the name will be copied to the new extension.
   *
   * @param userId The id of the user who owns the project.
   * @param projectId The id of the project.
   * @param basepath The base path of the extension components directory in the project assets.
   * @param extension The type name or package name of the extension.
   * @param files A set of files associated with the extension
   * @param newExtensionDescriptors Descriptors read from the new extension bundle.
   * @return true if this operation is going to result in an upgrade, otherwise false (new import)
   * @throws UnsupportedEncodingException if the platform for some reason does not support UTF-8
   * @throws ExtensionDowngradeException if the method detects that there is a possibility that an
   * existing extension will collide with the new extension bundle. This indicates that the
   * extension author did not properly maintain the extensions so some collision has occurred.
   */
  private boolean upgradeOldExtension(String userId, long projectId, String basepath,
      String extension, Set<String> files, Map<String, JSONObject> newExtensionDescriptors)
          throws UnsupportedEncodingException, ExtensionDowngradeException {
    boolean result = false;
    String descriptorFilename = basepath + extension + "/components.json";
    if (!files.contains(descriptorFilename)) {
      descriptorFilename = basepath + extension + "/component.json";
      if (!files.contains(descriptorFilename)) {
        // missing component descriptors?
        // assume an upgrade even though the project is in an illegal state or corrupted.
        return true;
      }
    }
    JSONArray oldExtensionDescriptors = readComponents(storageIo.downloadRawFile(userId, projectId,
        descriptorFilename));
    int overlapSize = 0;
    String potentialDowngrade = null;
    for (int i = 0; i < oldExtensionDescriptors.length(); i++) {
      JSONObject oldDesc = oldExtensionDescriptors.getJSONObject(i);
      JSONObject newDesc = newExtensionDescriptors.get(oldDesc.getString("type"));
      if (newDesc != null) {
        result = true;
        newDesc.put("name", oldDesc.getString("name"));  // copy user-applied renaming
        overlapSize++;
      } else {
        potentialDowngrade = oldDesc.getString("type");
      }
    }
    if (overlapSize > 0 && potentialDowngrade != null) {
      throw new ExtensionDowngradeException(potentialDowngrade);
    }
    return result;
  }

  /**
   * Finds extensions in the project sources and returns a mapping from the extension type name (for
   * old style extensions) or package name (for new style extensions) to the set of file names
   * included in that extension.
   *
   * @param extensionDir Extension asset directory in the project
   * @param files Set of all file names in the project
   * @return A map from the type name or package name of an extension to a set of all files in that
   * extension. We return a NavigableMap to aid in searching for related extensions during an
   * upgrade when the two extensions might share the same package but have been packaged by the
   * old extension system, which was per-class rather than per-package.
   */
  private static NavigableMap<String, Set<String>> findExtensions(String extensionDir,
      Set<String> files) {
    NavigableMap<String, Set<String>> extensions = new TreeMap<>();
    for (String s : files) {
      if (s.startsWith(extensionDir)) {
        String[] parts = s.split("/");
        String extensionName = parts[2];
        Set<String> extFiles = extensions.get(extensionName);
        if (extFiles == null) {
          extFiles = new HashSet<>();
          extensions.put(extensionName, extFiles);
        }
        extFiles.add(s);
      }
    }
    return extensions;
  }

  private static JSONArray readComponents(String content) {
    content = content.trim();  // remove extraneous whitespace
    if (content.startsWith("{") && content.endsWith("}")) {
      return new JSONArray("[" + content + "]");
    } else if (content.startsWith("[") && content.endsWith("]")) {
      return new JSONArray(content);
    } else {
      // content is neither a JSONObject {...} nor a JSONArray [...]. This is an error state.
      throw new IllegalArgumentException("Content was not a valid component descriptor file");
    }
  }

  /**
   * Read the components from the new extension. This method also upgrades component.json and
   * component_build_info.json so that they will be consistently named in the future using the
   * plural versions and containing arrays of descriptors.
   *
   * @param contents Mapping of path names to file content from an extension.
   * @param nameMap Mapping of file names to path names.
   * @return A JSONArray of parsed component descriptors.
   * @throws UnsupportedEncodingException if the platform for some reason doesn't support UTF-8
   */
  private static JSONArray readExtensionComponents(Map<String, byte[]> contents,
      Map<String, String> nameMap) throws UnsupportedEncodingException {
    upgradeAndRenameFile(contents, nameMap, "component_build_info.json",
        "component_build_infos.json");
    return upgradeAndRenameFile(contents, nameMap, "component.json", "components.json");
  }

  /**
   * Upgrades a file containing a JSONObject or JSONArray serialization to a JSONArray serialization
   * and renames the file from oldName to newName. This is used to handle the transitional files
   * for extension bundles from a file containing a single component descriptor to one containing
   * an array of one or more descriptors.
   *
   * @param contents The mapping of filenames to contents for the extension being imported
   * @param nameMap A mapping of base names to full path names for files
   * @param oldName The base name for the old version of the file, e.g. component.json
   * @param newName The base name for the new version of the file, e.g. components.json
   * @return The contents of the file as a JSONArray
   * @throws UnsupportedEncodingException if the platform for some reason doesn't support UTF-8
   */
  private static JSONArray upgradeAndRenameFile(Map<String, byte[]> contents,
      Map<String, String> nameMap, String oldName, String newName)
          throws UnsupportedEncodingException {
    JSONArray content = null;
    if (nameMap.containsKey(newName)) {
      // remove transitional old file, if it exists
      if (nameMap.containsKey(oldName)) {
        contents.remove(nameMap.remove(oldName));
      }
      content = readComponents(contents.get(nameMap.get(newName)));
    } else if (nameMap.containsKey(oldName)) {
      // rename to new name
      String oldPath = nameMap.remove(oldName);
      nameMap.put(newName, oldPath.replace(oldName, newName));
      content = readComponents(contents.remove(oldPath));
      contents.put(nameMap.get(newName), content.toString().getBytes(StorageUtil.DEFAULT_CHARSET));
    }
    return content;
  }

  /**
   * Make a mapping between a fully qualified class name of an extension and its component
   * descriptor.
   *
   * @param components An array of simple component descriptors in JSON format
   * @return A mapping between the FQCN of each extension in components and its descriptor
   */
  private static Map<String, JSONObject> makeComponentMap(JSONArray components) {
    Map<String, JSONObject> result = new HashMap<>();
    for (int i = 0; i < components.length(); i++) {
      JSONObject desc = components.getJSONObject(i);
      result.put(desc.getString("type"), desc);
    }
    return result;
  }

  private static Map<String, String> buildExtensionPathnameMap(Set<String> paths) {
    Map<String, String> result = new HashMap<>();
    for (String name : paths) {
      result.put(StorageUtil.basename(name), name);
    }
    return result;
  }

  private static Set<String> getExtensionClasses(JSONArray components) {
    Set<String> types = new HashSet<>();
    for (int i = 0; i < components.length(); i++) {
      types.add(components.getJSONObject(i).getString("type"));
    }
    return types;
  }

  /**
   * Match a set of strings of new types to be imported against the set of existing extensions
   * based on package names and type names. This method returns a set of old extensions that
   * are a subset of the new types based on the type names. For example, if you have an old
   * extension com.foo.Bar and you import a new extension that is package name com.foo, then we
   * will need to check that com.foo.Bar is inside com.foo and, if so, perform an upgrade.
   *
   * @param existing Existing set of extensions in the project
   * @param newTypes New type(s) defined by the extension being imported
   * @return A subset of the existing extensions that will need to be checked against the new
   * extension to determine whether we are performing an upgrade or adding a fresh extension.
   */
  private static Set<String> matchExtensions(NavigableMap<String, Set<String>> existing,
      Set<String> newTypes) {
    Set<String> results = new HashSet<>();
    String packageName = getPackageName(newTypes.iterator().next());
    if (existing.containsKey(packageName)) {
      results.add(packageName);
    }
    NavigableMap<String, Set<String>> related = existing.tailMap(packageName, true);
    for (String k : related.navigableKeySet()) {
      if (!k.startsWith(packageName)) {
        break;  // no longer in the same package
      }
      results.add(k);
    }
    return results;
  }

  private static String getPackageName(String className) {
    return className.substring(0, className.lastIndexOf('.'));
  }

  private static JSONArray readComponents(byte[] content) throws UnsupportedEncodingException {
    return readComponents(new String(content, StorageUtil.DEFAULT_CHARSET));
  }

  private String collectImportErrorInfo(String path, long projectId) {
    return "Error importing " + path + " to project " + projectId;
  }

  private static boolean isUnknownSource(String url) {
    // TODO: check if the url is from the market place
    return false;
  }
}
