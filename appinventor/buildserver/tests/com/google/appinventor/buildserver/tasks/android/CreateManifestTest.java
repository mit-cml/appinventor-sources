// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.buildserver.Project;
import com.google.appinventor.buildserver.Reporter;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import com.google.appinventor.buildserver.context.CompilerContext;
import com.google.appinventor.buildserver.tasks.common.LoadComponentInfo;
import com.google.appinventor.buildserver.tasks.common.ReadBuildInfo;
import com.google.appinventor.buildserver.util.ProjectUtils;
import com.google.appinventor.common.testutils.TestUtils;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link CreateManifest} class with and without a custom package name.
 */
public class CreateManifestTest {
  private static final String HELLO_PURR_TEMPLATE =
      TestUtils.windowsToUnix(TestUtils.APP_INVENTOR_ROOT_DIR)
          + "/appengine/war/templates/HelloPurr/HelloPurr.zip";
  private static final String MAIN_CLASS = "appinventor.ai_ram8647.HelloPurr.Screen1";

  private File projectRoot;

  @Before
  public void setUp() throws IOException {
    projectRoot = ProjectUtils.createNewTempDir();
    ProjectUtils.extractProjectFiles(new ZipFile(HELLO_PURR_TEMPLATE), projectRoot);
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(projectRoot);
  }

  @Test
  public void testProjectPackageNames() throws IOException {
    Project project = loadProject(null);
    assertEquals("appinventor.ai_ram8647.HelloPurr", project.getPackageName());
    assertEquals("appinventor.ai_ram8647.HelloPurr", project.getFormPackageName());

    project = loadProject(" com.example.purr ");
    assertEquals("com.example.purr", project.getPackageName());
    assertEquals("appinventor.ai_ram8647.HelloPurr", project.getFormPackageName());
  }

  @Test
  public void testGeneratedPackageName() throws IOException {
    String manifest = createManifest(loadProject(null));
    assertTrue(manifest.contains("package=\"appinventor.ai_ram8647.HelloPurr\""));
    assertTrue(manifest.contains("<activity android:name=\"" + MAIN_CLASS + "\""));
  }

  @Test
  public void testCustomPackageName() throws IOException {
    String manifest = createManifest(loadProject("com.example.purr"));
    assertTrue(manifest.contains("package=\"com.example.purr\""));
    // The form classes stay in the generated package, so the activity must be fully qualified.
    assertTrue(manifest.contains("<activity android:name=\"" + MAIN_CLASS + "\""));
    assertFalse(manifest.contains("android:name=\".Screen1\""));
  }

  @Test
  public void testInvalidPackageName() throws IOException {
    AndroidCompilerContext context = buildContext(loadProject("myapp"));
    assertFalse(new CreateManifest().execute(context).isSuccess());
  }

  private Project loadProject(String packageName) throws IOException {
    File properties = new File(projectRoot, "youngandroidproject/project.properties");
    if (packageName != null) {
      Files.write(properties.toPath(),
          ("\npackagename=" + packageName + "\n").getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
    return ProjectUtils.getProjectProperties(projectRoot);
  }

  private AndroidCompilerContext buildContext(Project project) {
    CompilerContext<?> context = new CompilerContext.Builder<>(project, "apk")
        .withClass(AndroidCompilerContext.class)
        .withBlocks(Collections.<String, Set<String>>emptyMap())
        .withCache(null)
        .withCompanion(false)
        .withDangerousPermissions(false)
        .withEmulator(false)
        .withFormOrientations(ImmutableMap.of("Screen1", "unspecified"))
        .withBlockPermissions(Collections.<String>emptySet())
        .withRam(2048)
        .withReporter(new Reporter(null))
        .withKeystore("test.keystore")
        .withTypes(Collections.<String>emptySet())
        .build();
    new ReadBuildInfo().execute(context);
    new LoadComponentInfo().execute(context);
    return (AndroidCompilerContext) context;
  }

  private String createManifest(Project project) throws IOException {
    AndroidCompilerContext context = buildContext(project);
    TaskResult result = new CreateManifest().execute(context);
    assertTrue(result.isSuccess());
    return new String(Files.readAllBytes(context.getPaths().getManifest().toPath()),
        StandardCharsets.UTF_8);
  }
}
