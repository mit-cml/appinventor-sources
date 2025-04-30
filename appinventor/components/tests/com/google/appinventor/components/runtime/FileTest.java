// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018-2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertErrorOccurred;
import static com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher.assertEventFired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;

import android.os.Environment;

import com.google.appinventor.components.common.FileScope;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import com.google.appinventor.components.runtime.shadows.ShadowActivityCompat;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;

import com.google.appinventor.components.runtime.util.Continuation;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.QUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;

import java.nio.charset.StandardCharsets;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.robolectric.Shadows;

import org.robolectric.annotation.Config;

import org.robolectric.shadows.ShadowEnvironment;

/**
 * Tests for the File component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@Config(shadows = {ShadowActivityCompat.class})
public class FileTest extends FileTestBase {
  private static final String DATA = "test data";
  protected static final String TARGET_FILE = "test.txt";
  protected File file;

  /**
   * Creates a Continuation that always fails the test.
   *
   * @param <T> the received type of the continuation
   * @return a failing continuation
   */
  @SuppressWarnings("unused")
  public static <T> Continuation<T> failTest(Class<T> clazz) {
    return new Continuation<T>() {
      @Override
      public void call(T value) {
        fail();
      }
    };
  }

  /**
   * Set up a new File component for testing.
   */
  @Before
  public void setUp() {
    super.setUp();
    file = new File(getForm());
  }

  /**
   * Tests that the File component can read files when given the special file
   * path "//". In a compiled app, this reads from the app's compiled assets.
   * For the REPL, this reads from a special directory AppInventor/assets/
   * on the external storage.
   */
  @Test
  public void testFileDoubleSlash() {
    file.DefaultScope(FileScope.Legacy);
    grantFilePermissions();
    file.ReadFrom("//" + TARGET_FILE);
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
    ShadowEventDispatcher.assertEventFired(file, "GotText", "Hello, world!\n");
  }

  /**
   * Tests that the File component can read files when given a relative file
   * name. In a compiled app, this reads from the app's private data directory.
   * For the REPL, this reads from a special directory AppInventor/data/
   * on the external storage.
   */
  @Test
  public void testFileRelative() {
    file.DefaultScope(FileScope.Legacy);
    grantFilePermissions();
    writeTempFile(TARGET_FILE, DATA, false);
    testReadFile(TARGET_FILE, DATA);
  }

  /**
   * Tests that the File component can read files when given an absolute file
   * name. In both compiled apps and the REPL, this reads from the given file
   * from the root of the external storage.
   */
  @Test
  public void testFileAbsolute() {
    file.DefaultScope(FileScope.Legacy);
    grantFilePermissions();
    writeTempFile(TARGET_FILE, DATA, true);
    testReadFile("/" + TARGET_FILE, DATA);
  }

  /**
   * Tests that the file component will report a PermissionDenied event when
   * the user denies a request for READ_EXTERNAL_STORAGE.
   */
  @Test
  public void testFilePermissionDenied() {
    file.DefaultScope(FileScope.Legacy);
    getForm().DefaultFileScope(FileScope.Legacy);
    denyFilePermissions();
    file.ReadFrom("/" + TARGET_FILE);
    ShadowActivityCompat.denyLastRequestedPermissions();
    runAllEvents();
    ShadowEventDispatcher.assertPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE);
  }

  @Test
  public void testFileOperations() {
    file.Exists(FileScope.App, "test.txt", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertFalse(value);
        file.SaveFile("Test", "test.txt");
        runAllAsynchronousCommandsAndEvents();
        file.AppendToFile(" Content", "test.txt");
        runAllAsynchronousCommandsAndEvents();
        file.ReadFrom("test.txt");
        runAllAsynchronousCommandsAndEvents();
        ShadowEventDispatcher.assertEventFired(file, "GotText", "Test Content");
        file.Delete("test.txt");
        file.Exists(FileScope.App, "test.txt", new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertFalse(value);
          }
        });
      }
    });
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testLegacyMode() {
    file.LegacyMode(true);
    assertTrue(file.LegacyMode());
    assertEquals(FileScope.Legacy, file.Scope());
    file.LegacyMode(false);
    assertFalse(file.LegacyMode());
    assertEquals(FileScope.App, file.Scope());
  }

  @Test
  public void testCannotDeleteAsset() {
    file.Delete("//foobar.txt");
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_DELETE_ASSET);
  }

  @Test
  public void testCannotDeleteReadonlyFile() throws IOException {
    // Create a read-only directory so its child cannot be deleted
    java.io.File tmp = new java.io.File(URI.create(FileUtil.resolveFileName(
        getForm(), "tempdir", FileScope.App)));
    tmp.mkdirs();
    new java.io.File(tmp, "readonly.txt").createNewFile();
    tmp.setReadOnly();
    tmp.deleteOnExit();

    // Attempt to delete the file, which should fail
    file.Scope(FileScope.App);
    file.Delete("/tempdir/readonly.txt");
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_DELETE_FILE);
  }

  @Test
  public void testMakeDirectory() {
    file.Exists(FileScope.App, "testdir", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertFalse(value);
        file.MakeDirectory(FileScope.App, "testdir", new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertTrue(value);
            file.Exists(FileScope.App, "testdir", new Continuation<Boolean>() {
              @Override
              public void call(Boolean value) {
                assertTrue(value);
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testMakeDirectoryDouble() {
    file.MakeDirectory(FileScope.App, "testdir", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
        file.MakeDirectory(FileScope.App, "testdir", new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertTrue(value);
          }
        });
      }
    });
  }

  @Test
  public void testMakeDirectoryThrows() {
    file.MakeDirectory(FileScope.Asset, "testdir", failTest(Boolean.class));
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_MAKE_DIRECTORY);
  }

  @Test
  public void testMakeDirectoryOverFileFails() {
    file.DefaultScope(FileScope.App);
    file.SaveFile("test", "test.txt");
    runAllAsynchronousCommandsAndEvents();
    file.MakeDirectory(FileScope.App, "test.txt", failTest(Boolean.class));
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_FILE_EXISTS_AT_PATH);
  }

  @Test
  public void testMakeDirectoryFails() {
    java.io.File tmp = new java.io.File(URI.create(FileUtil.resolveFileName(getForm(), "readonly", FileScope.App)));
    tmp.mkdirs();
    tmp.deleteOnExit();
    tmp.setReadOnly();
    grantFilePermissions();
    file.MakeDirectory(FileScope.App, "readonly/subdir", failTest(Boolean.class));
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_MAKE_DIRECTORY);
  }

  @Test
  public void testRemoveDirectory() {
    file.MakeDirectory(FileScope.App, "test", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
        file.RemoveDirectory(FileScope.App, "test", false, new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertTrue(value);
          }
        });
      }
    });
  }

  @Test
  public void testRemoveDirectoryRecursive() {
    file.MakeDirectory(FileScope.App, "test", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        writeFileForTest(FileScope.App, "test/a.txt", "file 1");
        writeFileForTest(FileScope.App, "test/b.txt", "file 2");
        file.RemoveDirectory(FileScope.App, "test", true, new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertTrue(value);
          }
        });
      }
    });
  }

  @Test
  public void testRemoveDirectoryRecursiveFails() {
    file.MakeDirectory(FileScope.App, "test", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        writeFileForTest(FileScope.App, "test/a.txt", "file 1");
        writeFileForTest(FileScope.App, "test/b.txt", "File 2");
        file.RemoveDirectory(FileScope.App, "test", false, new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertFalse(false);
          }
        });
      }
    });
  }

  @Test
  public void testRemoveDirectoryThrows() {
    file.RemoveDirectory(FileScope.Asset, "assets", true, failTest(Boolean.class));
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_REMOVE_DIRECTORY);
  }

  @Test
  public void testListDirectoryEmpty() {
    file.MakeDirectory(FileScope.App, "test", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        file.ListDirectory(FileScope.App, "test", new Continuation<List<String>>() {
          @Override
          public void call(List<String> value) {
            assertTrue(value.isEmpty());
          }
        });
      }
    });
  }

  @Test
  public void testListDirectory() {
    file.MakeDirectory(FileScope.App, "test", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        writeFileForTest(FileScope.App, "test/a.txt", "file 1");
        writeFileForTest(FileScope.App, "test/b.txt", "file 2");
        file.ListDirectory(FileScope.App, "test", new Continuation<List<String>>() {
          @Override
          public void call(List<String> value) {
            assertTrue(value.contains("a.txt"));
            assertTrue(value.contains("b.txt"));
          }
        });
      }
    });
  }

  @Test
  public void testListDirectoryAssets() {
    file.ListDirectory(FileScope.Asset, "", new Continuation<List<String>>() {
      @Override
      public void call(List<String> value) {
        assertTrue(value.contains("test.txt"));
      }
    });
  }

  @Test
  public void testScopeChanges() {
    assertEquals(FileScope.App, file.Scope());
    file.DefaultScope(FileScope.Legacy);
    assertEquals(FileScope.Legacy, file.Scope());
    file.Scope(FileScope.Private);
    assertEquals(FileScope.Private, file.Scope());
  }

  @Test
  public void testForCompletion() {
    file.ReadPermission(true);
    file.WritePermission(true);
  }

  @Test
  public void testIsDirectory() {
    file.IsDirectory(FileScope.App, "", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
      }
    });
    runAllAsynchronousCommandsAndEvents();
  }

  @Test
  public void testIsDirectoryAsset() {
    file.IsDirectory(FileScope.Asset, "", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
      }
    });
    runAllAsynchronousCommandsAndEvents();
  }

  @Test
  public void testIsDirectoryBad() {
    file.IsDirectory(FileScope.App, "dont_make_this_directory", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertFalse(value);
      }
    });
    runAllAsynchronousCommandsAndEvents();
  }

  @Test
  public void testCopyFile() {
    file.Scope(FileScope.App);
    file.CopyFile(FileScope.Asset, "test.txt", FileScope.App, "test.txt", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
        file.ReadFrom("/test.txt");
      }
    });
    runAllAsynchronousCommandsAndEvents();
    assertEventFired(file, "GotText", "Hello, world!\n");
  }

  @Test
  public void testCopyFileFails() {
    try {
      file.CopyFile(FileScope.App, "test.txt", FileScope.Asset, "bad.txt", failTest(Boolean.class));
      fail();
    } catch (StopBlocksExecution e) {
      // expected
    }
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_WRITE_ASSET);
  }

  @Test
  public void testMoveFile() {
    writeFileForTest(FileScope.App, "original.txt", "original content");
    file.MoveFile(FileScope.App, "original.txt", FileScope.App, "renamed.txt", new Continuation<Boolean>() {
      @Override
      public void call(Boolean value) {
        assertTrue(value);
        file.Exists(FileScope.App, "original.txt", new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertFalse(value);
          }
        });
        file.Exists(FileScope.App, "renamed.txt", new Continuation<Boolean>() {
          @Override
          public void call(Boolean value) {
            assertTrue(value);
          }
        });
      }
    });
    runAllAsynchronousCommandsAndEvents();
  }

  @Test
  public void testCannotWriteAsset() {
    file.SaveFile("bad test", "//test.txt");
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_WRITE_ASSET);
  }

  @Test
  public void testCannotWriteReadonlyFile() throws IOException {
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    java.io.File target = new java.io.File(URI.create(FileUtil.resolveFileName(getForm(), "readonly.txt", FileScope.App)));
    target.createNewFile();
    target.setReadOnly();
    target.deleteOnExit();
    file.Scope(FileScope.App);
    file.SaveFile("bad test", "/readonly.txt");
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE);
  }

  @Test
  public void testCannotWriteFileToReadonlyDir() throws IOException {
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    java.io.File target = new java.io.File(URI.create(FileUtil.resolveFileName(getForm(), "readonly/test.txt", FileScope.App)));
    target.getParentFile().mkdirs();
    target.getParentFile().setReadOnly();
    target.getParentFile().deleteOnExit();
    file.SaveFile("bad test", "/readonly/test.txt");
    runAllAsynchronousCommandsAndEvents();
    assertErrorOccurred(ErrorMessages.ERROR_CANNOT_CREATE_FILE);
  }

  @Test
  public void testMakeFullPath() {
    assertEquals(getForm().getAssetPath("asset.txt"),
        file.MakeFullPath(FileScope.Legacy, "//asset.txt"));
  }

  /// Helper functions

  /**
   * Helper function to grant read/write permissions to the app.
   */
  public void grantFilePermissions() {
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }

  /**
   * Helper function to deny read/write permissions to the app.
   */
  public void denyFilePermissions() {
    Shadows.shadowOf(getForm()).denyPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }

  /**
   * Write a temporary file to the file of the given {@code name} with the given
   * {@code content}.
   *
   * @param name the name of the file to write
   * @param content the content of the file
   * @param external true if the file should be written to external storage,
   *                 otherwise false.
   * @return the absolute path of the file
   */
  public String writeTempFile(String name, String content, boolean external) {
    String target;
    if (external) {
      target = QUtil.getExternalStoragePath(getForm());
    } else if (getForm().isRepl()) {
      target = QUtil.getReplDataPath(getForm(), false);
    } else {
      target = getForm().getFilesDir().getAbsolutePath();
    }
    target += "/" + name;
    FileOutputStream out = null;
    try {
      java.io.File targetFile = new java.io.File(target);
      targetFile.deleteOnExit();
      if (!targetFile.getParentFile().exists()) {
        if (!targetFile.getParentFile().mkdirs()) {
          throw new IOException();
        }
      }
      out = new FileOutputStream(target);
      out.write(content.getBytes(StandardCharsets.UTF_8));
      return targetFile.getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to prepare test", e);
    } finally {
      IOUtils.closeQuietly(TAG, out);
    }
  }

  private void testReadFile(String filename, String expectedData) {
    file.ReadFrom(filename);
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
    ShadowEventDispatcher.assertEventFired(file, "GotText", expectedData);
  }

  private void writeFileForTest(FileScope scope, String name, String content) {
    java.io.File file = new java.io.File(URI.create(FileUtil.resolveFileName(
        getForm(), name, scope)));
    OutputStream out = null;
    try {
      out = new FileOutputStream(file);
      out.write(content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException("Unexpected IOException in test", e);
    } finally {
      IOUtils.closeQuietly(TAG, out);
    }
  }

}
