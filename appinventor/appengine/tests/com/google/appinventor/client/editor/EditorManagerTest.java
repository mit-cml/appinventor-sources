// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import junit.framework.TestCase;

/**
 * Tests how many completions a save waits for before it reports back.
 *
 * <p>This is the arithmetic behind Submit to LMS. The menu item saves the open editors and
 * only sends the submission once nothing is left unsaved, so a count that reports back early
 * would hand in whatever had reached the server by then. Saving a screen's designer and its
 * blocks is two files, which is the ordinary case rather than an unusual one.
 *
 * <p>This covers the arithmetic alone. What is sent, held back, retried, and still
 * outstanding is decided by {@code PendingSaves} and covered by its own tests. Driving the
 * save itself would need {@code Ode}, which cannot load outside a GWT client, so the lines
 * that hand these pieces to each other are covered by reading them and by the end to end run.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class EditorManagerTest extends TestCase {

  /** Each file is answered separately, so each file is one thing to wait for. */
  public void testEachFileIsCounted() {
    assertEquals(2, EditorManager.pendingSaveOperationCount(0, 2));
    assertEquals(5, EditorManager.pendingSaveOperationCount(0, 5));
  }

  /**
   * A screen's designer and its blocks are separate files, so editing both and then
   * submitting has to wait for two answers rather than one.
   */
  public void testASavedScreenWaitsForBothOfItsFiles() {
    assertEquals(2, EditorManager.pendingSaveOperationCount(0, 2));
    assertFalse("counting the files as one operation reports back too early",
        EditorManager.pendingSaveOperationCount(0, 2) == 1);
  }

  /** With nothing to save the file step still answers once, so the count is never zero. */
  public void testNoFilesStillCountsOnce() {
    assertEquals(1, EditorManager.pendingSaveOperationCount(0, 0));
    assertEquals(4, EditorManager.pendingSaveOperationCount(3, 0));
  }

  /** Project settings are saved one at a time alongside the files. */
  public void testSettingsAndFilesAddUp() {
    assertEquals(5, EditorManager.pendingSaveOperationCount(3, 2));
    assertEquals(1, EditorManager.pendingSaveOperationCount(0, 1));
  }
}
