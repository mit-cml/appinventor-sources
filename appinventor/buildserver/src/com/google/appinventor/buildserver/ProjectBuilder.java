// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import static com.google.appinventor.buildserver.context.Resources.RUNTIME_FILES_DIR;
import static com.google.appinventor.buildserver.util.ProjectUtils.PROJECT_DIRECTORY;

import com.google.appinventor.buildserver.FormPropertiesAnalyzer.BlockXmlAnalyzer;
import com.google.appinventor.buildserver.FormPropertiesAnalyzer.ComponentBlocksExtractor;
import com.google.appinventor.buildserver.FormPropertiesAnalyzer.PermissionBlockExtractor;
import com.google.appinventor.buildserver.FormPropertiesAnalyzer.ScopeBlockExtractor;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.context.Paths;
import com.google.appinventor.buildserver.stats.StatReporter;
import com.google.appinventor.buildserver.tasks.common.BuildFactory;
import com.google.appinventor.buildserver.util.Execution;

import com.google.appinventor.buildserver.util.ProjectUtils;
import com.google.appinventor.common.utils.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Provides support for building Young Android projects.
 *
 * @author markf@google.com (Mark Friedman)
 */
public final class ProjectBuilder {

  private File outputApk;
  private File outputKeystore;
  private boolean saveKeystore;

  // Logging support
  private static final Logger LOG = Logger.getLogger(ProjectBuilder.class.getName());

  private static final int MAX_COMPILER_MESSAGE_LENGTH = 160;
  private static final String SEPARATOR = File.separator;

  // Project folder prefixes
  // TODO(user): These constants are (or should be) also defined in
  // appengine/src/com/google/appinventor/server/project/youngandroid/YoungAndroidProjectService
  // They should probably be in some place shared with the server
  private static final String KEYSTORE_FILE_NAME = YoungAndroidConstants.PROJECT_KEYSTORE_LOCATION;

  private static final String FORM_PROPERTIES_EXTENSION =
      YoungAndroidConstants.FORM_PROPERTIES_EXTENSION;
  private static final String YAIL_EXTENSION = YoungAndroidConstants.YAIL_EXTENSION;

  private static final String CODEBLOCKS_SOURCE_EXTENSION =
      YoungAndroidConstants.CODEBLOCKS_SOURCE_EXTENSION;

  private static final String ALL_COMPONENT_TYPES = RUNTIME_FILES_DIR + "simple_components.txt";

  public File getOutputApk() {
    return outputApk;
  }

  public File getOutputKeystore() {
    return outputKeystore;
  }

  private final StatReporter statReporter;

  public ProjectBuilder(StatReporter statReporter) {
    this.statReporter = statReporter;
  }

  Result build(String userName, ZipFile inputZip, File outputDir, String outputFileName,
      boolean isForCompanion, boolean isForEmulator, boolean includeDangerousPermissions,
      String[] extraExtensions, int childProcessRam, String dexCachePath,
      BuildServer.ProgressReporter reporter, String ext) {
    try {
      // Download project files into a temporary directory
      File projectRoot = ProjectUtils.createNewTempDir();
      LOG.info("temporary project root: " + projectRoot.getAbsolutePath());
      try {
        List<String> sourceFiles;
        try {
          sourceFiles = ProjectUtils.extractProjectFiles(inputZip, projectRoot);
        } catch (IOException e) {
          LOG.severe("unexpected problem extracting project file from zip");
          return Result.createFailingResult("", "Problems processing zip file.");
        }

        File keyStoreFile = new File(projectRoot, KEYSTORE_FILE_NAME);
        String keyStorePath = keyStoreFile.getPath();
        if (!keyStoreFile.exists()) {
          keyStorePath = createKeyStore(userName, projectRoot, KEYSTORE_FILE_NAME);
          saveKeystore = true;
        }

        // Create project object from project properties file.
        Project project = ProjectUtils.getProjectProperties(projectRoot);

        final BuildFactory<?, CompilerContext<Paths>> factory = BuildFactory.get(ext);
        if (factory == null) {
          throw new IllegalStateException("No factory for target: " + ext);
        }
        if (outputFileName == null) {
          outputFileName = project.getProjectName() + "." + factory.getExtension();
        }

        File buildTmpDir = new File(projectRoot, "build/tmp");
        buildTmpDir.mkdirs();

        Set<String> componentTypes = getComponentTypes(sourceFiles, project.getAssetsDirectory());
        if (isForCompanion) {
          componentTypes.addAll(getAllComponentTypes());
        }
        if (extraExtensions != null) {
          System.err.println("Including extension: " + Arrays.toString(extraExtensions));
          Collections.addAll(componentTypes, extraExtensions);
        }
        ComponentBlocksExtractor componentBlocksExtractor = new ComponentBlocksExtractor();
        PermissionBlockExtractor permissionBlockExtractor = new PermissionBlockExtractor();
        ScopeBlockExtractor scopeBlockExtractor = new ScopeBlockExtractor();
        analyzeBlockFiles(sourceFiles, componentBlocksExtractor, permissionBlockExtractor,
            scopeBlockExtractor);
        Map<String, Set<String>> componentBlocks = componentBlocksExtractor.getResult();
        Map<String, Set<String>> componentProperties = getComponentDesignerProperties(sourceFiles);
        mergeMaps(componentBlocks, componentProperties);
        Set<String> extraPermissions = permissionBlockExtractor.getResult();
        Set<String> usedScopes = scopeBlockExtractor.getResult();
        for (String scope : usedScopes) {
          switch (scope) {
            case "Shared":
              extraPermissions.add("android.permission.READ_MEDIA_AUDIO");
              extraPermissions.add("android.permission.READ_MEDIA_IMAGES");
              extraPermissions.add("android.permission.READ_MEDIA_VIDEO");
              extraPermissions.add("android.permission.READ_EXTERNAL_STORAGE");
              extraPermissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
              break;
            case "Legacy":
              extraPermissions.add("android.permission.READ_EXTERNAL_STORAGE");
              extraPermissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
              break;
            default:
              break;
          }
        }
        Map<String, String> formOrientations = getScreenOrientations(sourceFiles);

        // Generate the compiler context
        Reporter r = new Reporter(reporter);
        CompilerContext<Paths> context =
            new CompilerContext.Builder<Paths, CompilerContext<Paths>>(project, ext)
                .withClass(factory.getContextClass())
                .withTypes(componentTypes)
                .withBlocks(componentBlocks)
                .withBlockPermissions(extraPermissions)
                .withFormOrientations(formOrientations)
                .withReporter(r)
                .withStatReporter(statReporter)
                .withCompanion(isForCompanion)
                .withEmulator(isForEmulator)
                .withDangerousPermissions(includeDangerousPermissions)
                .withKeystore(keyStorePath)
                .withRam(childProcessRam)
                .withCache(dexCachePath)
                .withOutput(outputFileName)
                .build();

        // Invoke YoungAndroid compiler
        Compiler<?, ?> compiler = factory.makeCompiler(context);

        Future<Boolean> executor = Executors.newSingleThreadExecutor().submit(compiler);

        boolean success = executor.get();
        statReporter.stopBuild(compiler, success);
        r.close();

        // Retrieve compiler messages and convert to HTML and log
        String srcPath = projectRoot.getAbsolutePath() + SEPARATOR + PROJECT_DIRECTORY + SEPARATOR
            + ".." + SEPARATOR + "src" + SEPARATOR;
        String messages = processCompilerOutput(context.getReporter().getSystemOutput(),
            srcPath);

        if (success) {
          // Locate output file
          String fileName = outputFileName;
          if (fileName == null) {
            fileName = project.getProjectName() + "." + ext;
          }
          File outputFile = new File(projectRoot,
              "build" + SEPARATOR + "deploy" + SEPARATOR + fileName);
          if (!outputFile.exists()) {
            LOG.warning("Young Android build - " + outputFile + " does not exist");
          } else {
            outputApk = new File(outputDir, outputFile.getName());
            Files.copy(outputFile, outputApk);
            if (saveKeystore) {
              outputKeystore = new File(outputDir, KEYSTORE_FILE_NAME);
              Files.copy(keyStoreFile, outputKeystore);
            }
          }
        }
        return new Result(success, messages, context.getReporter().getUserOutput());
      } finally {
        // On some platforms (OS/X), the java.io.tmpdir contains a symlink. We need to use the
        // canonical path here so that Files.deleteRecursively will work.

        // Note (ralph):  deleteRecursively has been removed from the guava-11.0.1 lib
        // Replacing with deleteDirectory, which is supposed to delete the entire directory.
        FileUtils.deleteQuietly(new File(projectRoot.getCanonicalPath()));
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to build project", e);
      return Result.createFailingResult(e.toString(), "Server error performing build");
    }
  }

  private static Set<String> getAllComponentTypes() throws IOException {
    Set<String> compSet = Sets.newHashSet();
    String[] components = Resources.toString(
        ProjectBuilder.class.getResource(ALL_COMPONENT_TYPES), Charsets.UTF_8).split("\n");
    for (String component : components) {
      compSet.add(component);
    }
    return compSet;
  }

  private static Map<String, String> getScreenOrientations(List<String> files)
      throws IOException, JSONException {
    Map<String, String> result = new HashMap<>();
    final int extLength = FORM_PROPERTIES_EXTENSION.length();
    for (String f : files) {
      if (f.endsWith(FORM_PROPERTIES_EXTENSION)) {
        File scmFile = new File(f);
        String scmContent = new String(Files.toByteArray(scmFile),
            StandardCharsets.UTF_8);
        String formName = f.substring(f.lastIndexOf(SEPARATOR) + 1, f.length() - extLength);
        result.put(formName, FormPropertiesAnalyzer.getFormOrientation(scmContent));
      }
    }
    return result;
  }

  private static Set<String> getComponentTypes(List<String> files, File assetsDir)
      throws IOException, JSONException {
    Map<String, String> nameTypeMap = createNameTypeMap(assetsDir);

    Set<String> componentTypes = Sets.newHashSet();
    for (String f : files) {
      if (f.endsWith(".scm")) {
        File scmFile = new File(f);
        String scmContent = new String(Files.toByteArray(scmFile),
            PathUtil.DEFAULT_CHARSET);
        for (String compName : getTypesFromScm(scmContent)) {
          componentTypes.add(nameTypeMap.get(compName));
        }
      }
    }
    return componentTypes;
  }

  private static void analyzeBlockFiles(List<String> files, BlockXmlAnalyzer<?>... analyzers)
      throws IOException {
    for (String f : files) {
      if (f.endsWith(".bky")) {
        File bkyFile = new File(f);
        String bkyContent = Files.toString(bkyFile, StandardCharsets.UTF_8);
        FormPropertiesAnalyzer.analyzeBlocks(bkyContent, analyzers);
      }
    }
  }

  /**
   * Constructs a mapping of component types to the blocks of each type used in
   * the project files. Properties specified in the designer are considered
   * blocks for the purposes of this operation.
   *
   * @param files A list of files contained in the project.
   * @return A mapping of component type names to sets of block names used in
   *         the project
   * @throws IOException if any of the files named in {@code files} cannot be
   *                     read
   */
  private static Map<String, Set<String>> getComponentDesignerProperties(List<String> files)
      throws IOException {
    Map<String, Set<String>> result = new HashMap<>();
    for (String f : files) {
      if (f.endsWith(".scm")) {
        File scmFile = new File(f);
        String scmContent = Files.toString(scmFile, StandardCharsets.UTF_8);
        for (Map.Entry<String, Set<String>> entry :
            FormPropertiesAnalyzer.getComponentBlocksFromSchemeFile(scmContent).entrySet()) {
          if (result.containsKey(entry.getKey())) {
            result.get(entry.getKey()).addAll(entry.getValue());
          } else {
            result.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }
    return result;
  }

  /**
   * In ode code, component names are used to identify a component though the
   * variables storing component names appear to be "type". While there's no
   * harm in ode, here in build server, they need to be separated.
   * This method returns a name-type map, mapping the component names used in
   * ode to the corresponding type, aka fully qualified name. The type will be
   * used to build apk.
   */
  private static Map<String, String> createNameTypeMap(File assetsDir)
      throws IOException, JSONException {
    Map<String, String> nameTypeMap = Maps.newHashMap();

    JSONArray simpleCompsJson = new JSONArray(Resources.toString(ProjectBuilder.
        class.getResource("/files/simple_components.json"), Charsets.UTF_8));
    for (int i = 0; i < simpleCompsJson.length(); ++i) {
      JSONObject simpleCompJson = simpleCompsJson.getJSONObject(i);
      nameTypeMap.put(simpleCompJson.getString("name"),
          simpleCompJson.getString("type"));
    }

    File extCompsDir = new File(assetsDir, "external_comps");
    if (!extCompsDir.exists()) {
      return nameTypeMap;
    }

    for (File extCompDir : extCompsDir.listFiles()) {
      if (!extCompDir.isDirectory()) {
        continue;
      }

      File extCompJsonFile = new File(extCompDir, "component.json");
      if (extCompJsonFile.exists()) {
        JSONObject extCompJson = new JSONObject(Resources.toString(
            extCompJsonFile.toURI().toURL(), Charsets.UTF_8));
        nameTypeMap.put(extCompJson.getString("name"),
            extCompJson.getString("type"));
      } else {  // multi-extension package
        extCompJsonFile = new File(extCompDir, "components.json");
        if (extCompJsonFile.exists()) {
          JSONArray extCompJson = new JSONArray(Resources.toString(
              extCompJsonFile.toURI().toURL(), Charsets.UTF_8));
          for (int i = 0; i < extCompJson.length(); i++) {
            JSONObject extCompDescriptor = extCompJson.getJSONObject(i);
            nameTypeMap.put(extCompDescriptor.getString("name"),
                extCompDescriptor.getString("type"));
          }
        }
      }
    }

    return nameTypeMap;
  }

  static String createKeyStore(String userName, File projectRoot, String keystoreFileName)
      throws IOException {
    File keyStoreFile = new File(projectRoot.getPath(), keystoreFileName);

    /* Note: must expire after October 22, 2033, to be in the Android
     * marketplace.  Android docs recommend "10000" as the expiration # of
     * days.
     *
     * For DNAME, US may not the right country to assign it to.
     */
    String[] keytoolCommandline = {
        System.getProperty("java.home") + SEPARATOR + "bin" + SEPARATOR + "keytool",
        "-genkey",
        "-keystore", keyStoreFile.getAbsolutePath(),
        "-alias", "AndroidKey",
        "-keyalg", "RSA",
        "-dname", "CN=" + quotifyUserName(userName) + ", O=AppInventor for Android, C=US",
        "-validity", "10000",
        "-storepass", "android",
        "-keypass", "android"
    };

    if (Execution.execute(null, keytoolCommandline, System.out, System.err)) {
      if (keyStoreFile.length() > 0) {
        return keyStoreFile.getAbsolutePath();
      }
    }
    return null;
  }

  @VisibleForTesting
  static Set<String> getTypesFromScm(String scm) {
    return FormPropertiesAnalyzer.getComponentTypesFromFormFile(scm);
  }

  @VisibleForTesting
  static String processCompilerOutput(String output, String srcPath) {
    // First, remove references to the temp source directory from the messages.
    String messages = output.replace(srcPath, "");

    // Then, format warnings and errors nicely.
    try {
      // Split the messages by \n and process each line separately.
      String[] lines = messages.split("\n");
      Pattern pattern = Pattern.compile("(.*?):(\\d+):\\d+: (error|warning)?:? ?(.*?)");
      StringBuilder sb = new StringBuilder();
      boolean skippedErrorOrWarning = false;
      for (String line : lines) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          // Determine whether it is an error or warning.
          String kind;
          String spanClass;
          // Scanner messages do not contain either 'error' or 'warning'.
          // I treat them as errors because they prevent compilation.
          if ("warning".equals(matcher.group(3))) {
            kind = "WARNING";
            spanClass = "compiler-WarningMarker";
          } else {
            kind = "ERROR";
            spanClass = "compiler-ErrorMarker";
          }

          // Extract the filename, lineNumber, and message.
          String filename = matcher.group(1);
          String lineNumber = matcher.group(2);
          String text = matcher.group(4);

          // If the error/warning is in a yail file, generate a div and append it to the
          // StringBuilder.
          if (filename.endsWith(YoungAndroidConstants.YAIL_EXTENSION)) {
            skippedErrorOrWarning = false;
            sb.append("<div><span class='" + spanClass + "'>" + kind + "</span>: " +
                StringUtils.escape(filename) + " line " + lineNumber + ": " +
                StringUtils.escape(text) + "</div>");
          } else {
            // The error/warning is in runtime.scm. Don't append it to the StringBuilder.
            skippedErrorOrWarning = true;
          }

          // Log the message, first truncating it if it is too long.
          if (text.length() > MAX_COMPILER_MESSAGE_LENGTH) {
            text = text.substring(0, MAX_COMPILER_MESSAGE_LENGTH);
          }
        } else {
          // The line isn't an error or a warning. This is expected.
          // If the line begins with two spaces, it is a continuation of the previous
          // error/warning.
          if (line.startsWith("  ")) {
            // If we didn't skip the most recent error/warning, append the line to our
            // StringBuilder.
            if (!skippedErrorOrWarning) {
              sb.append(StringUtils.escape(line)).append("<br>");
            }
          } else {
            skippedErrorOrWarning = false;
            // We just append the line to our StringBuilder.
            sb.append(StringUtils.escape(line)).append("<br>");
          }
        }
      }
      messages = sb.toString();
    } catch (Exception e) {
      // Report exceptions that happen during the processing of output, but don't make the
      // whole build fail.
      e.printStackTrace();

      // We were not able to process the output, so we just escape for HTML.
      messages = StringUtils.escape(messages);
    }

    return messages;
  }

  /*
   * Adds quotes around the given userName and encodes embedded quotes as \".
   */
  private static String quotifyUserName(String userName) {
    Preconditions.checkNotNull(userName);
    int length = userName.length();
    StringBuilder sb = new StringBuilder(length + 2);
    sb.append('"');
    for (int i = 0; i < length; i++) {
      char ch = userName.charAt(i);
      if (ch == '"') {
        sb.append('\\').append(ch);
      } else {
        sb.append(ch);
      }
    }
    sb.append('"');
    return sb.toString();
  }

  private static void mergeMaps(Map<String, Set<String>> dest, Map<String, Set<String>> src) {
    for (Map.Entry<String, Set<String>> entry : src.entrySet()) {
      String key = entry.getKey();
      Set<String> value = entry.getValue();
      if (dest.containsKey(key)) {
        dest.get(key).addAll(value);
      } else {
        dest.put(key, value);
      }
    }
  }
}
