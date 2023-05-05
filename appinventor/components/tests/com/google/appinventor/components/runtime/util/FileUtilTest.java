// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.os.Environment;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.RobolectricTestBase;
import com.google.appinventor.components.runtime.errors.PermissionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowEnvironment;

@RunWith(RobolectricTestRunner.class)
public class FileUtilTest extends RobolectricTestBase {

  @Test
  public void testResolveFilenameSlashes() {
    final String baseAssetPath = getForm().getAssetPath("");
    final String externPath = "file://" + QUtil.getExternalStoragePath(getForm());
    final String privatePath = getForm().getPrivatePath("private.txt");
    assertEquals(baseAssetPath + "asset.txt",
        FileUtil.resolveFileName(getForm(), "//asset.txt", null));
    assertEquals(externPath + "/legacy.txt",
        FileUtil.resolveFileName(getForm(), "/legacy.txt", null));
    assertEquals(privatePath,
        FileUtil.resolveFileName(getForm(), "private.txt", null));
  }

  @Test
  public void testResolveFilenameApp() {
    final String appDir = "file://" + getForm().getExternalFilesDir("").getAbsolutePath();
    assertEquals(appDir + "/app.txt",
        FileUtil.resolveFileName(getForm(), "app.txt", FileScope.App));
  }

  @Test
  public void testResolveFilenameAsset() {
    String baseAssetPath = getForm().getAssetPath("");
    assertEquals(baseAssetPath + "asset.txt",
        FileUtil.resolveFileName(getForm(), "asset.txt", FileScope.Asset));
  }

  @Test
  public void testResolveFilenameCache() {
    String cachePath = "file://" + getForm().getCacheDir().getAbsolutePath();
    assertEquals(cachePath + "/temp.txt",
        FileUtil.resolveFileName(getForm(), "temp.txt", FileScope.Cache));
  }

  @Test
  public void testResolveFilenameLegacy() {
    assertEquals(getForm().getPrivatePath("legacy.txt"),
        FileUtil.resolveFileName(getForm(), "legacy.txt", FileScope.Legacy));
  }

  @Test
  public void testResolveFilenamePrivate() {
    final String privatePath = getForm().getPrivatePath("");
    assertEquals(privatePath + "/private.txt",
        FileUtil.resolveFileName(getForm(), "private.txt", FileScope.Private));
  }

  @Test
  public void testResolveFilenameShared() {
  }

  @Test
  public void testIsExternalStorage() {
    assertTrue(FileUtil.isExternalStorageUri(getForm(), "file:///sdcard/test.txt"));
    assertTrue(FileUtil.isExternalStorageUri(getForm(), "file:///storage/emulator/0/test.txt"));
    assertTrue(FileUtil.isExternalStorageUri(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.App)));
    assertFalse(FileUtil.isExternalStorageUri(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Private)));
  }

  @Test
  public void testIsAppSpecificExternalUri() {
    assertTrue(FileUtil.isAppSpecificExternalUri(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.App)));
    assertFalse(FileUtil.isAppSpecificExternalUri(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Private)));
  }

  @Test
  public void testGetScopedPicture() {
    ScopedFile result;

    // Legacy mode puts images in /sdcard/My Documents/Pictures/
    result = FileUtil.getScopedPictureFile(getForm(), "png");
    assertEquals(FileScope.Legacy, result.getScope());
    assertTrue(result.getFileName().startsWith("/My Documents/Pictures/"));
    assertTrue(result.getFileName().endsWith(".png"));

    // Asset mode gets switched to private mode for writing
    getForm().DefaultFileScope(FileScope.Asset);
    result = FileUtil.getScopedPictureFile(getForm(), "png");
    assertEquals(FileScope.Private, result.getScope());
    assertTrue(result.getFileName().startsWith("Pictures/"));
    assertTrue(result.getFileName().endsWith(".png"));

    // All other modes drop the "My Documents" folder
    FileScope[] scopes = new FileScope[] {
        FileScope.App, FileScope.Cache, FileScope.Private, FileScope.Shared
    };
    for (FileScope scope : scopes) {
      getForm().DefaultFileScope(scope);
      result = FileUtil.getScopedPictureFile(getForm(), "png");
      assertEquals(scope, result.getScope());
      assertTrue(result.getFileName().startsWith("Pictures/"));
      assertTrue(result.getFileName().endsWith(".png"));
    }
  }

  @Test
  public void testGetExternalFile() {
    getForm().DefaultFileScope(FileScope.Legacy);
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

    // getExternalFile will throw an error if the target is in the external storage but not in
    // app-specific storage (e.g., Legacy mode) and the WRITE_EXTERNAL_STORAGE permission has not
    // been granted by the user.
    try {
      FileUtil.getExternalFile(getForm(), "app.txt");
      fail();
    } catch (PermissionException e) {
      assertEquals(Manifest.permission.WRITE_EXTERNAL_STORAGE, e.getPermissionNeeded());
    }

    // Grant permissions for the remainder of the test
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    // When the external storage is available, everything should proceed normally when getting
    // a reference to an external file.
    try {
      assertNotNull(FileUtil.getExternalFile(getForm(), "app.txt"));
    } catch (FileUtil.FileException e) {
      fail("Unexpected exception: " + e);
    }

    // When the default scope is Legacy, getExternalFile should resolve paths beginning with "/"
    // the same as those without (both resolve to external files).
    assertEquals(FileUtil.getExternalFile(getForm(), "app.txt"),
        FileUtil.getExternalFile(getForm(), "/app.txt"));

    // When the external storage is unmounted, getExternalFile should throw an error to indicate
    // that external storage is unavailable.
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
    try {
      FileUtil.getExternalFile(getForm(), "app.txt");
      fail();
    } catch (FileUtil.FileException e) {
      assertEquals(ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_NOT_AVAILABLE,
          e.getErrorMessageNumber());
    }

    // When the external storage is mounted, but read only, getExternalFile should throw an
    // error to indicate that external storage is read only.
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED_READ_ONLY);
    try {
      FileUtil.getExternalFile(getForm(), "app.txt");
      fail();
    } catch (FileUtil.FileException e) {
      assertEquals(ErrorMessages.ERROR_MEDIA_EXTERNAL_STORAGE_READONLY, e.getErrorMessageNumber());
    }

    // Private files are always writeable
    getForm().DefaultFileScope(FileScope.Private);
    assertNotNull(FileUtil.getExternalFile(getForm(), "private.txt"));
  }

  @Test
  public void testResolveFileNameScoped() {
    final String baseAssetPath = getForm().getAssetPath("");
    final String externPath = "file://" + QUtil.getExternalStoragePath(getForm());
    final String privatePath = getForm().getPrivatePath("");

    assertEquals(baseAssetPath + "asset.txt",
        FileUtil.resolveFileName(getForm(), new ScopedFile(FileScope.Legacy, "//asset.txt")));
    assertEquals(externPath + "/legacy.txt",
        FileUtil.resolveFileName(getForm(), new ScopedFile(FileScope.Legacy, "/legacy.txt")));
    assertEquals(privatePath + "/private.txt",
        FileUtil.resolveFileName(getForm(), new ScopedFile(FileScope.Legacy, "private.txt")));
  }

  @Test
  public void testNeedsReadPermission() {
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.App, "test.txt")));
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.Asset, "test.txt")));
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.Cache, "test.txt")));
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.Legacy, "test.txt")));
    assertTrue(FileUtil.needsReadPermission(new ScopedFile(FileScope.Legacy, "/test.txt")));
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.Legacy, "//test.txt")));
    assertFalse(FileUtil.needsReadPermission(new ScopedFile(FileScope.Private, "test.txt")));
    assertTrue(FileUtil.needsReadPermission(new ScopedFile(FileScope.Shared, "test.txt")));
  }

  @Test
  public void testNeedsReadPermissionByUri() {
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.App)));
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Asset)));
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Cache)));
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "text.txt", FileScope.Legacy)));
    assertTrue(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "/test.txt", FileScope.Legacy)));
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "//test.txt", FileScope.Legacy)));
    assertFalse(FileUtil.needsReadPermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Private)));
  }

  @Test
  public void testNeedsWritePermission() {
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.App, "test.txt")));
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.Asset, "test.txt")));
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.Cache, "test.txt")));
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.Legacy, "test.txt")));
    assertTrue(FileUtil.needsWritePermission(new ScopedFile(FileScope.Legacy, "/test.txt")));
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.Legacy, "//test.txt")));
    assertFalse(FileUtil.needsWritePermission(new ScopedFile(FileScope.Private, "test.txt")));
    assertTrue(FileUtil.needsWritePermission(new ScopedFile(FileScope.Shared, "test.txt")));
  }

  @Test
  public void testNeedsWritePermissionByUri() {
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.App)));
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Asset)));
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Cache)));
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Legacy)));
    assertTrue(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "/test.txt", FileScope.Legacy)));
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "//test.txt", FileScope.Legacy)));
    assertFalse(FileUtil.needsWritePermission(getForm(),
        FileUtil.resolveFileName(getForm(), "test.txt", FileScope.Private)));
  }

  @Test
  public void testNeedsExternalStorage() {
    assertTrue(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.App, "test.txt")));
    assertFalse(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Asset, "test.txt")));
    assertFalse(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Cache, "test.txt")));
    assertFalse(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Legacy, "test.txt")));
    assertTrue(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Legacy, "/test.txt")));
    assertFalse(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Legacy, "//test.txt")));
    assertFalse(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Private, "test.txt")));
    assertTrue(FileUtil.needsExternalStorage(getForm(),
        new ScopedFile(FileScope.Shared, "test.txt")));
  }

  @Test
  public void testReadAsset() throws IOException {
    String result = new String(FileUtil.readFile(getForm(), "/android_asset/test.txt"),
        StandardCharsets.UTF_8).trim();
    assertEquals("Hello, world!", result);
  }

  @Test
  public void testReadFile() throws IOException {
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    ScopedFile target = new ScopedFile(FileScope.App, "testReadFile.txt");
    String filename = FileUtil.resolveFileName(getForm(), target);
    try (OutputStream out = FileUtil.openForWriting(getForm(), target)) {
      out.write("success".getBytes(StandardCharsets.UTF_8));
    }
    String result = new String(FileUtil.readFile(getForm(), filename),
        StandardCharsets.UTF_8);
    assertEquals("success", result);
  }

  @Test
  public void testReadFileFails() throws IOException {
    ScopedFile target = new ScopedFile(FileScope.Private, "testReadFileFails.txt");
    try {
      FileUtil.readFile(getForm(), FileUtil.resolveFileName(getForm(), target));
      fail("Should not have found testReadFileFails.txt");
    } catch (FileNotFoundException e) {
      // This is expected behavior
    }
  }

  @Test(expected = FileNotFoundException.class)
  public void testMoveFileThrowsIfSourceMissing() throws Exception {
    ScopedFile source = new ScopedFile(FileScope.Private, "neverMakeThisFile.txt");
    ScopedFile target = new ScopedFile(FileScope.Private, "target.txt");
    FileUtil.moveFile(getForm(), source, target);
  }

  @Test
  public void testMoveFileOverwrites() throws Exception {
    ScopedFile source = new ScopedFile(FileScope.Private, "testMoveFileOverwrittenSource.txt");
    ScopedFile target = new ScopedFile(FileScope.Private, "testMoveFileOverwrittenTarget.txt");
    try (OutputStream out = FileUtil.openForWriting(getForm(), source)) {
      out.write("source".getBytes(StandardCharsets.UTF_8));
    }
    try (OutputStream out = FileUtil.openForWriting(getForm(), target)) {
      out.write("target".getBytes(StandardCharsets.UTF_8));
    }
    FileUtil.moveFile(getForm(), source, target);

    // source file should be gone
    assertFalse(new java.io.File(new URI(FileUtil.resolveFileName(getForm(), source))).exists());

    // target file should now be source
    String content = new String(FileUtil.readFile(getForm(),
        FileUtil.resolveFileName(getForm(), target)));
    assertEquals("source", content);
  }
}
