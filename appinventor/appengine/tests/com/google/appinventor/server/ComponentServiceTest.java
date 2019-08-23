// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse;
import com.google.appinventor.shared.rpc.component.ComponentImportResponse.Status;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link ComponentService}.
 */
public class ComponentServiceTest {
  private static final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();

  private static final String PACKAGE_BASE = "com.domain.noname.";
  private static final String PROJECT1_NAME = "Project1";
  private static final String EXTENSION1_CLASS = "edu.mit.appinventor.aix_test.AIXTestExtension";
  private static final String EXTENSION2_CLASS = "edu.mit.appinventor.aix_test.AIXTestExtension2";

  private ProjectServiceImpl projectService;
  private ComponentService componentService;
  private long projectId;

  private static final String KEYSTORE_ROOT_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/war/";  // must end with a slash

  private static String resource(String file) {
    return "file://" + TestUtils.APP_INVENTOR_ROOT_DIR +
        "/appengine/tests/com/google/appinventor/server/" + file;
  }

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    LocalUser localUserMock = LocalUser.getInstance();
    localUserMock.set(new User("1", "NonSuch", "NoName", null, 0, false, false, 0, null));
    localUserMock.setSessionId("test-session");
    projectService = new ProjectServiceImpl();
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);

    componentService = new ComponentServiceImpl();
    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(PACKAGE_BASE + PROJECT1_NAME);
    projectId = projectService.newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT1_NAME, params).getProjectId();
  }

  /**
   * Test loading an extension built with an earlier version of the extension system that only
   * allowed one extension per package. The component descriptor was a JSONObject stored in a
   * component.json file within the extension. When loaded, component.json should be upgraded
   * to components.json, which is a JSONArray containing the single component descriptor from the
   * original component.json.
   */
  @Test
  public void testLoadSingleExtension() {
    ComponentImportResponse result = importTestExtension("FCQN-Single-Extension.aix");
    assertEquals(Status.IMPORTED, result.getStatus());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
  }

  @Test
  public void testLoadExtensionBundle() {
    ComponentImportResponse result = importTestExtension("Extension-Bundle.aix");
    assertEquals(Status.IMPORTED, result.getStatus());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
  }

  @Test
  public void testLoadMultipleExtensionBundle() {
    ComponentImportResponse result = importTestExtension("Extension-Bundle-With-2-Extensions.aix");
    assertEquals(Status.IMPORTED, result.getStatus());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertTrue(result.getComponentTypes().containsKey(EXTENSION2_CLASS));
    assertExtensionAssets(result);
  }

  @Test
  public void testUpgradeExtensionSingleToSingle() throws Exception {
    UserProject project = importProject("testUpgradeExtensionSingleToSingle", resource("OldExtensionTest.aia"));
    projectId = project.getProjectId();
    ComponentImportResponse result = importTestExtension("FCQN-Single-Extension.aix");
    assertEquals(Status.UPGRADED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
    assertAssetsWithPrefixRemoved("assets/external_comps/edu.mit.appinventor.aix_test.AIXTestExtension/component.json");
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test.AIXTestExtension/components.json");
  }

  @Test
  public void testUpgradeExtensionSingleToTransition() throws Exception {
    UserProject project = importProject("testUpgradeExtensionSingleToTransition", resource("OldExtensionTest.aia"));
    projectId = project.getProjectId();
    ComponentImportResponse result = importTestExtension("Transition-Extension-Bundle.aix");
    assertEquals("Failed with error: " + result.getMessage(), Status.UPGRADED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
    assertAssetsWithPrefixRemoved("assets/external_comps/edu.mit.appinventor.aix_test.AIXTestExtension/");
  }

  @Test
  public void testUpgradeExtensionSingleToBundle() throws Exception {
    UserProject project = importProject("OldExtensionTest", resource("OldExtensionTest.aia"));
    projectId = project.getProjectId();
    ComponentImportResponse result = importTestExtension("Extension-Bundle.aix");
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
    assertAssetsWithPrefixRemoved("assets/external_comps/edu.mit.appinventor.aix_test.AIXTestExtension/");
  }

  @Test
  public void testDowngradeExtensionBundleToOldExtension() throws Exception {
    importTestExtension("Extension-Bundle-With-2-Extensions.aix");
    ComponentImportResponse result = importTestExtension("FCQN-Single-Extension.aix");
    assertEquals(Status.BUNDLE_DOWNGRADE, result.getStatus());
  }

  @Test
  public void testDowngradeExtensionBundleToBundle() throws Exception {
    importTestExtension("Extension-Bundle-With-2-Extensions.aix");
    ComponentImportResponse result = importTestExtension("Extension-Bundle.aix");
    assertEquals(Status.BUNDLE_DOWNGRADE, result.getStatus());
  }

  @Test
  public void testUpgradeExtensionBundleToBundle() throws Exception {
    importTestExtension("Extension-Bundle.aix");
    ComponentImportResponse result = importTestExtension("Extension-Bundle-With-2-Extensions.aix");
    assertEquals(Status.UPGRADED, result.getStatus());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertTrue(result.getComponentTypes().containsKey(EXTENSION2_CLASS));
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
  }

  @Test
  public void testUpgradeTransitionToTransition() throws Exception {
    importTestExtension("Transition-Extension-Bundle.aix");
    ComponentImportResponse result = importTestExtension("Transition-Extension-Bundle.aix");
    assertEquals("Failed with error: " + result.getMessage(), Status.UPGRADED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
  }

  @Test
  public void testUpgradeTransitionExtensionToBundle() throws Exception {
    importTestExtension("Transition-Extension-Bundle.aix");
    ComponentImportResponse result = importTestExtension("Extension-Bundle-With-2-Extensions.aix");
    assertEquals("Failed with error: " + result.getMessage(), Status.UPGRADED, result.getStatus());
    assertEquals(2, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertTrue(result.getComponentTypes().containsKey(EXTENSION2_CLASS));
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
  }

  @Test
  public void testReversedFilesExtension() throws Exception {
    importTestExtension("Transition-Extension-Bundle.aix");
    ComponentImportResponse result = importTestExtension("Transition-Extension-With-Files-Reversed.aix");
    assertEquals("Failed with error: " + result.getMessage(), Status.UPGRADED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
    assertAssetsOnServer("assets/external_comps/edu.mit.appinventor.aix_test/components.json");
  }

  @Test
  public void testUpgradeIfProjectMissingComponentsJson() throws Exception {
    UserProject project = importProject("testUpgradeIfProjectMissingComponentsJson", resource("CorruptExtensionTestMissingDescriptors.aia"));
    projectId = project.getProjectId();
    ComponentImportResponse result = importTestExtension("Transition-Extension-Bundle.aix");
    assertEquals("Failed with error: " + result.getMessage(), Status.UPGRADED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey(EXTENSION1_CLASS));
    assertExtensionAssets(result);
    assertAssetsWithPrefixRemoved("assets/external_comps/edu.mit.appinventor.aix_test.AIXTestExtension/");
  }

  @Test
  public void testOkayIfPackageCollisionButNoOverlappingTypes() {
    importTestExtension("test.Extension1.aix");
    importTestExtension("test2.Extension2.aix");  // tests escape after package change
    ComponentImportResponse result = importTestExtension("test.Extension3.aix");
    assertEquals(Status.IMPORTED, result.getStatus());
    assertEquals(1, result.getComponentTypes().size());
    assertTrue(result.getComponentTypes().containsKey("test.Extension3"));
    assertAssetsOnServer("assets/external_comps/test.Extension1/components.json");
    assertAssetsOnServer("assets/external_comps/test2.Extension2/components.json");
    assertAssetsOnServer("assets/external_comps/test/components.json");
  }

  /**
   * Tests whether two extensions compiled with the same package name will collide. This won't
   * happen if one adheres to using a single package per set of extension. However, in practice
   * there have been reports of older extensions using the same package name being upgraded to the
   * new extensions model and then one extension overwrites another due to collisions at the
   * package name level. For example, com.example.Bar overwrites com.example.Foo because both
   * packages are com.example. Extension authors should use one package per extension bundle, so
   * com.example.foo.Foo and com.example.bar.Bar would be acceptable, or generate a single package
   * with both extensions, com.example containing com.example.Foo and com.example.Bar.
   */
  @Test
  public void testFailIfPackageCollisionWithDifferingComponents() {
    importTestExtension("test.Extension3.aix");
    ComponentImportResponse result = importTestExtension("test.Extension4.aix");
    assertEquals(Status.FAILED, result.getStatus());
    assertEquals(0, result.getComponentTypes().size());
    assertNotNull(result.getMessage());
    System.err.println(result.getMessage());
  }

  /**
   * Tests to ensure that if multiple (old) extensions share the same package name that after
   * importing a new style extension bundle containing the union of the old extensions will result
   * in the old extension files being removed and replaced with the contents of the single new
   * extension.
   */
  @Test
  public void testUpgradeManyOldExtensionsToOneBundle() {
    importTestExtension("FooOld.aix");
    importTestExtension("BarOld.aix");
    ComponentImportResponse result = importTestExtension("FooBar.aix");
    assertEquals(Status.UPGRADED, result.getStatus());
    assertEquals(2, result.getComponentTypes().size());
    assertNull(result.getMessage());
    assertTrue(result.getComponentTypes().containsKey("com.example.Foo"));
    assertTrue(result.getComponentTypes().containsKey("com.example.Bar"));
    assertAssetsWithPrefixRemoved("assets/external_comps/com.example.Foo/");
    assertAssetsWithPrefixRemoved("assets/external_comps/com.example.Bar/");
    assertAssetsOnServer("assets/external_comps/com.example/components.json");
  }

  @Test
  public void testEmptyComponentDescriptors() {
    ComponentImportResponse result = importTestExtension("test.EmptyComponentDescriptor.aix");
    assertEquals(Status.FAILED, result.getStatus());
  }

  @Test
  public void testNoComponentDescriptor() {
    ComponentImportResponse result = importTestExtension("test.NoComponentDescriptor.aix");
    assertEquals(Status.FAILED, result.getStatus());
  }

  @Test
  public void testBadExtensionFile() {
    ComponentImportResponse result = importTestExtension("Bad-Extension.aix");
    assertEquals(Status.FAILED, result.getStatus());
  }

  @Test
  public void testBadComponentJson() {
    ComponentImportResponse result = importTestExtension("Corrupt-Extension.aix");
    assertEquals(Status.FAILED, result.getStatus());
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  private UserProject importProject(String projectName, String aiaPath) throws Exception {
    FileImporterImpl importer = new FileImporterImpl();
    FileInputStream fis = new FileInputStream(new File(URI.create(aiaPath)));
    UserProject project = importer.importProject("1", projectName, fis);
    fis.close();
    return project;
  }

  private ComponentImportResponse importTestExtension(String name) {
    return componentService.importComponentToProject(resource(name), projectId, "assets");
  }

  private void assertExtensionAssets(ComponentImportResponse result) {
    ProjectNode descriptor = null, buildInfo = null;
    for (ProjectNode node : result.getNodes()) {
      if ("components.json".equals(node.getName())) {
        descriptor = node;
      } else if ("component_build_infos.json".equals(node.getName())) {
        buildInfo = node;
      } else if ("component_build_info.json".equals(node.getName())) {
        fail("Expected the server to remove legacy component_build_info.json but found " + node.getFullName());
      } else if ("component.json".equals(node.getName())) {
        fail("Expected the server to remove legacy component.json but found " + node.getFullName());
      }
    }
    assertNotNull("No components.json found after upgrading extension.", descriptor);
    assertNotNull("No component_build_infos.json found after upgrading extension.", buildInfo);
  }

  private void assertAssetsWithPrefixRemoved(String prefix) {
    List<String> files = StorageIoInstanceHolder.getInstance()
        .getProjectSourceFiles("1", projectId);
    for (String name : files) {
      if (name.startsWith(prefix)) {
        fail("Expected for file " + name + " to be deleted.");
      }
    }
  }

  private void assertAssetsOnServer(String path) {
    List<String> files = StorageIoInstanceHolder.getInstance()
        .getProjectSourceFiles("1", projectId);
    for (String name : files) {
      if (name.equals(path)) {
        return;
      }
    }
    fail("Expected file " + path + " to exist.");
  }
}
