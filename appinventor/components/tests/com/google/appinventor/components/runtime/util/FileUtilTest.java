package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Environment;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.RobolectricTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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
}
