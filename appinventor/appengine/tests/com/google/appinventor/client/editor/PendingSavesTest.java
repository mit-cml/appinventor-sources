// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests what a save sends and what is left outstanding.
 *
 * <p>This is what Submit to LMS rests on. The menu item saves the open editors and sends the
 * submission only once nothing is outstanding, so anything wrong here hands in work that is
 * not what the learner is looking at, or waits for something that is never coming.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class PendingSavesTest extends TestCase {

  /** Two notes of one file, so a test can tell the newer edit from the older one. */
  private static final class Note {
    private final String fileId;
    private final int version;

    Note(String fileId, int version) {
      this.fileId = fileId;
      this.version = version;
    }
  }

  /** Names a note by its file, which is what the file side does. */
  private static PendingSaves<Note> noteQueue() {
    return new PendingSaves<Note>(new PendingSaves.Key<Note>() {
      @Override
      public Object of(Note note) {
        return note.fileId;
      }
    });
  }

  /** Names each item by itself, which is what the settings side does. */
  private static PendingSaves<String> queue() {
    return new PendingSaves<String>(new PendingSaves.Key<String>() {
      @Override
      public Object of(String item) {
        return item;
      }
    });
  }

  /** Nothing to do means nothing outstanding, which is what lets a submission go out. */
  public void testAnEmptyQueueHasNothingOutstanding() {
    assertTrue(queue().isEmpty());
    assertFalse(queue().heldBack());
  }

  /** Something waiting to be saved is outstanding, so a submission must not go out. */
  public void testSomethingWaitingIsOutstanding() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");

    assertFalse("a file waiting to be saved is unfinished work", q.isEmpty());
  }

  /** Something sent and not answered is still outstanding, even though nothing is waiting. */
  public void testSomethingOnItsWayIsStillOutstanding() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    assertEquals(1, q.take().size());

    assertFalse("a save that has not answered is still unfinished work", q.isEmpty());
    assertFalse("nothing is waiting, it is on its way", q.heldBack());
    assertEquals(1, q.inFlightCount());
  }

  /** Once every save has answered there is nothing outstanding. */
  public void testAnsweringClearsIt() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();

    q.answered("Screen1.bky");

    assertTrue(q.isEmpty());
    assertEquals(0, q.inFlightCount());
  }

  /**
   * Two saves of one file must not run at the same time. They are separate requests and can
   * land in either order, and the older one landing last would undo the newer one.
   */
  public void testTheSameFileIsNeverSentTwiceAtOnce() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    assertEquals(1, q.take().size());

    q.add("Screen1.bky");
    List<String> second = q.take();

    assertTrue("the second save has to wait for the first to answer", second.isEmpty());
    assertTrue("and it is held back rather than forgotten", q.heldBack());
  }

  /** The held back save goes out as soon as the one before it has answered. */
  public void testTheHeldBackSaveGoesOutNext() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();
    q.add("Screen1.bky");
    q.take();

    q.answered("Screen1.bky");
    List<String> third = q.take();

    assertEquals(1, third.size());
    assertEquals("Screen1.bky", third.get(0));
  }

  /** A file with nothing outstanding is sent straight away even while another one is waiting. */
  public void testAnotherFileIsNotHeldUpByTheFirst() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();
    q.add("Screen1.bky");
    q.add("Screen2.bky");

    List<String> sending = q.take();

    assertEquals(1, sending.size());
    assertEquals("Screen2.bky", sending.get(0));
    assertTrue("Screen1 is still held back", q.heldBack());
  }

  /** A save that failed goes back to waiting, so the next save tries it again. */
  public void testAFailedSaveGoesBackToWaiting() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();

    q.failed("Screen1.bky");

    assertFalse("a file whose save failed is unfinished work", q.isEmpty());
    assertEquals(1, q.take().size());
  }

  /**
   * A failure must not undo an edit made while the save was on its way. What is waiting is
   * newer than what was sent, so it is what goes next.
   */
  public void testAFailureDoesNotUndoANewerEdit() {
    PendingSaves<Note> q = noteQueue();
    Note older = new Note("Screen1.bky", 1);
    Note newer = new Note("Screen1.bky", 2);
    q.add(older);
    q.take();
    q.add(newer);

    q.failed(older);

    List<Note> sending = q.take();
    assertEquals(1, sending.size());
    assertEquals("the newer edit goes, not the one whose save failed", 2, sending.get(0).version);
  }

  /**
   * One save answering must not clear another save of a different file, which is what a map
   * keyed only by file cannot express when both are outstanding.
   */
  public void testOneAnswerDoesNotClearAnother() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.add("Screen2.bky");
    assertEquals(2, q.take().size());

    q.answered("Screen1.bky");

    assertFalse("Screen2 is still on its way", q.isEmpty());
    assertEquals(1, q.inFlightCount());
  }

  /** Something being closed rather than saved is forgotten entirely. */
  public void testDiscardForgetsIt() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();
    q.add("Screen1.bky");

    q.discard("Screen1.bky");

    assertTrue(q.isEmpty());
    assertFalse(q.heldBack());
  }

  /**
   * The wrapped completion marks its save answered before anything else runs. A completion
   * that did not would leave the save counting as on its way forever, and a submission would
   * wait on it until its deadline.
   */
  public void testAnsweringMarksTheSaveAnsweredFirst() {
    final PendingSaves<String> q = queue();
    q.add("settings");
    q.take();
    final boolean[] sawItAnswered = {false};

    PendingSaves.answering(q, "settings", new com.google.gwt.user.client.Command() {
      @Override
      public void execute() {
        sawItAnswered[0] = q.isEmpty();
      }
    }).execute();

    assertTrue("the completion runs", sawItAnswered[0]);
    assertTrue("and nothing is left outstanding", q.isEmpty());
  }

  /**
   * A late answer from a save that was discarded must not clear a newer save of the same
   * thing. The newer save owns the slot, and losing it would let a second save of that thing
   * go out while the newer one is still on its way.
   */
  public void testAStaleAnswerCannotClearANewerSave() {
    PendingSaves<Note> q = noteQueue();
    Note older = new Note("Screen1.bky", 1);
    Note newer = new Note("Screen1.bky", 2);
    q.add(older);
    q.take();
    q.discard(older);
    q.add(newer);
    assertEquals(1, q.take().size());

    q.answered(older);

    assertEquals("the newer save still owns its slot", 1, q.inFlightCount());
    q.failed(older);
    assertEquals("a stale failure changes nothing either", 1, q.inFlightCount());
    assertFalse("and the discarded save is not requeued", q.heldBack());
  }

  /** Noting the same thing twice before it is sent sends it once. */
  public void testNotingTwiceSendsOnce() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.add("Screen1.bky");

    assertEquals(1, q.take().size());
  }

  /**
   * A closing window sends held back work instead of dropping it. No later save could come
   * for it there, so takeEvenInFlight hands out everything waiting, and the newer send takes
   * the slot so a late answer from the older save cannot clear it.
   */
  public void testAClosingWindowSendsWhatWasHeldBack() {
    PendingSaves<Note> q = noteQueue();
    Note older = new Note("Screen1.bky", 1);
    Note newer = new Note("Screen1.bky", 2);
    q.add(older);
    q.take();
    q.add(newer);
    assertEquals("an ordinary save holds the newer one back", 0, q.take().size());

    List<Note> sent = q.takeEvenInFlight();

    assertEquals("the closing window sends it anyway", 1, sent.size());
    assertSame(newer, sent.get(0));
    assertFalse("nothing is left waiting", q.heldBack());
    q.answered(older);
    assertEquals("the newer send owns the slot, so a late answer cannot clear it",
        1, q.inFlightCount());
    q.answered(newer);
    assertTrue("its own answer clears it", q.isEmpty());
  }

  /** With nothing waiting, a closing window has nothing to send. */
  public void testTakeEvenInFlightWithNothingWaitingSendsNothing() {
    PendingSaves<String> q = queue();
    q.add("Screen1.bky");
    q.take();

    assertEquals(0, q.takeEvenInFlight().size());
    assertEquals("the earlier save still owns its slot", 1, q.inFlightCount());
  }
}
