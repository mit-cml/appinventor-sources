// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblockutil.CSaveButton;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * Listens to workspace events to coordinate saving operations.
 *
 * An Autosaver will start a java.util TimerTask which will
 * attempt to save the blocks workspace every second as long as no workspace
 * events have occurred in the last five seconds.
 *
 * Autosaver provides a history mechanism that supports undo and redo
 * operations on saved state. It keeps a history, which is a list of
 * state strings, along with a "currentState" pointer that points to the
 * current place in the history (the state from the most recent save or
 * restore). A new save operation will save the state immediately after the
 * currentState, wiping out any following saved states.
 *   - checkpoint: If any user-initiated changes have been made since the last
 *           save or restore, truncate the history list after the currentState,
 *           append the new state, and advance the currentState pointer to be
 *           the new saved state.
 *   - undo: First, do a checkpoint. Then, if there is a state before the
 *           currentState in the history, back up the currentPointer to the
 *           previous state and restore the workspace to that state.
 *   - redo: First, do a checkpoint. Then, if there is a state after the
 *           currentState in the history, move the currentState pointer forward
 *           and restores the workspace to that state.
 *
 * TODO(sharon): consider compressing the saved history strings to avoid
 * consuming too much memory (and possibly allowing a larger number
 * of saved states).
 * @author sharon@google.com (Sharon Perl) - undo/redo
 */
public class AutoSaver implements WorkspaceListener {
  private static final boolean DEBUG = false;
  private static final int MAX_HISTORY_SIZE = 20; // max depth of undo stack

  // Set testingMode to true to prevent the AutoSaver from really trying
  // to save files
  public static boolean testingMode = false;

  private final long minimumEventInterval = 10000000000L; // 10 sec
  private long lastEventTime = System.nanoTime();

  // The blocks workspace could change as a result of user actions (signalled
  // via WorkspaceListener) or as a result of an undo or redo operation.
  // Both of these types of changes should result in writing the changes
  // to the server, but they require different handling of the history so
  // we need to distinguish them.
  private volatile boolean workspaceChangedBySystem = false;
  private volatile boolean workspaceChangedByUser = false;
  private volatile boolean listening = false;
  // alreadySaving prevents auto-save if explicit save is already in progress
  private volatile boolean alreadySaving = false;
  private volatile ArrayList<String> history;
  // if non-null, lastFormProperties should be the form properties string
  // that goes with the blocks state in history
  private volatile String lastFormProperties;
  private volatile int currentState;  // an index into history
  private final Object saverLock = new Object();  // protects volatile fields

  private final WorkspaceController controller;
  private final CSaveButton saveButton;
  private final CSaveButton undoButton;
  private final CSaveButton redoButton;

  // if this many saves fail in a row, warn the user and offer them
  // the option to exit.
  private static final int WARN_SAVE_FAILURES = 10;
  private int successiveSaveFailures = 0;
  public static final String SAVE_FAILURE_MESSAGE =
    "Error saving blocks state. It is probably best to exit and restart the blocks editor "
    + " to avoid losing more modifications to your blocks.";

  public AutoSaver(WorkspaceController controller, CSaveButton saveButton,
      CSaveButton undoButton, CSaveButton redoButton) {

    TimerTask saveTask = new TimerTask() {
      @Override
      public void run() {
        saveIfIdle();
      }
    };

    // Schedules the autosaver to try and save once per second.
    // This is not scheduled on the Event Dispatch thread since it uses
    // java.util.Timer.
    new Timer().schedule(saveTask, 0, 1000);

    this.controller = controller;
    this.saveButton = saveButton;
    this.undoButton = undoButton;
    this.redoButton = redoButton;

    clearHistory();  // initializes undo/redo button state, history list,
                     // and state pointer
    lastFormProperties = null;
  }

  @Override
  public void workspaceEventOccurred(WorkspaceEvent event) {
    if (!event.shouldSaveChanges()) {
      // Certain events don't cause
      // any changes that we care to save.
      return;
    }

    boolean enableSaveButton = false;

    synchronized (saverLock) {
      if (listening) {
        lastEventTime = System.nanoTime();
        if (!workspaceChangedByUser) {
          workspaceChangedByUser = true;
          enableSaveButton = true;
        }
      }
    }

    if (enableSaveButton) {
      enableButton(saveButton);
    }
  }

  /**
   * Causes the autosaver to ignore workspace events.
   */
  public void stopListening(){
    synchronized (saverLock) {
      if (DEBUG) {
        System.out.println("Autosaver stopped listening.");
      }
      listening = false;
    }
  }

  /**
   * Causes the autosaver to start acting on workspace events.
   */
  public void startListening(){
    synchronized (saverLock) {
      if (DEBUG) {
        System.out.println("Autosaver started listening.");
      }
      listening = true;
    }
  }

  /**
   * Returns whether a workspace event has been listened to since
   * the last snapshot occurred or the autosaver was reset.
   * Must call without saverLock held
   * @return true if workspace has changed
   */
  public boolean getWorkspaceChanged(){
    synchronized (saverLock) {
      return workspaceChangedByUser || workspaceChangedBySystem;
    }
  }

  /**
   * Should be called when a save attempt fails after a snapshot was taken.
   * Doing so informs the autosaver that it ought to try and save on its
   * next opportunity.
   */
  public void onSaveFailed() {
    successiveSaveFailures++;
    if (successiveSaveFailures >= WARN_SAVE_FAILURES) {
      FeedbackReporter.showErrorMessageWithExit(SAVE_FAILURE_MESSAGE);
    }
    successiveSaveFailures = 0; // reset since the user must have decided to stay
    enableButton(saveButton);

    synchronized (saverLock) {
      workspaceChangedByUser = true;
      // TODO(sharon): onSnapshot probably already put the state we were
      // trying to save in the history. Unless we do something here (remove
      // the state or somehow remember not to save the same state again) we
      // could end up with multiple copies of the same state in the history,
      // making undo/redo appear to do nothing and be confusing.
    }
  }

  /**
   * Should be called when a save attempt succeeds. Informs the autosaver
   * that any string of successive save failures has been ended.
   */
  public void onSaveSucceeded() {
    successiveSaveFailures = 0;
  }

  /**
   * Should be called whenever the workspace is snapshotted (written
   * to the server). Adds saveString to the history if anything had changed or
   * if this is the initial state to save.
   * Also, creates a baseline point from which to determine whether the
   * workspace has changed since the last save/restore.
   */
  public void onSnapshot(String saveString) {
    if (DEBUG) {
      System.out.println("onSnapshot");
    }
    disableButton(saveButton);

    synchronized (saverLock) {
      if (workspaceChangedByUser || currentState < 0) {
        addToHistory(saveString);
        workspaceChangedByUser = false;
      } else {
        if (DEBUG) {
          System.out.println("onSnapshot called when workspaceChangedUser is false");
        }
      }
      workspaceChangedBySystem = false;
    }
  }

  /**
   * Resets the autosaver for a new project.
   */
  public void reset() {
    disableButton(saveButton);

    synchronized (saverLock) {
      listening = true;
      workspaceChangedByUser = false;
    }
    clearHistory();
    lastFormProperties = null;
  }

  /*
   * Remember the last formProperties string that was loaded, in case
   * we need to reload them on an UNDO or REDO
   */
  public void saveFormProperties(String formProperties) {
    lastFormProperties = formProperties;
  }

  /*
   * Trigger a save of the workspace now.
   */
  public void saveNow() {
    class SaveRunnable implements Runnable {
      public void run() {
        synchronized (saverLock) {
          if (alreadySaving) {
            return;
          }
          alreadySaving = true;
        }
        try {
          if (DEBUG) {
            System.out.println("Saving in response to Save button click...");
          }
          try {
            controller.persistCodeblocksSourceFile(true /* wait for save to complete */);
          } catch (SaveException e) {
            // The save should have happened, but it failed for some reason, so we still want to
            // be able to save.
            onSaveFailed();
            e.printStackTrace();
          }
        } finally {
          synchronized (saverLock) {
            alreadySaving = false;
          }
        }
      }
    }

    if (SwingUtilities.isEventDispatchThread()) {
      new SaveRunnable().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new SaveRunnable());
      } catch (InterruptedException e) {
        onSaveFailed();
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        onSaveFailed();
        e.printStackTrace();
      }
    }
  }

  private void saveIfIdle() {
    if (testingMode) {
      return;
    }
    class SaveRunnable implements Runnable {
      public void run() {
        boolean save = false;

        synchronized (saverLock) {
          save = !alreadySaving && listening &&
            (workspaceChangedByUser || workspaceChangedBySystem) &&
            System.nanoTime() - lastEventTime > minimumEventInterval;
          if (DEBUG && alreadySaving) {
            System.out.println("Not auto-saving because explicit save still in progress");
          }
        }
        if (save) {
          if (DEBUG) {
            System.out.println("Autosaver saving...");
          }
          try {
            controller.persistCodeblocksSourceFile(false /* don't wait for save to complete */);
          } catch (SaveException e) {
            // The save should have happened, but it failed for some reason, so we still want to
            // be able to save.
            onSaveFailed();
            e.printStackTrace();
          }
        }
      }
    }

    if (SwingUtilities.isEventDispatchThread()) {
            new SaveRunnable().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new SaveRunnable());
      } catch (InterruptedException e) {
        onSaveFailed();
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        onSaveFailed();
        e.printStackTrace();
      }
    }
  }

  private void disableButton(CSaveButton button) {
    button.grayOut();
  }

  private void enableButton(CSaveButton button) {
    button.reColor();
  }

  /* ++++++++++++++++++
   * History operations
   * ++++++++++++++++++
   */

  /**
   * checkpoint: If any changes have been made since the last save or restore,
   * truncate the history list after the currentState, append
   * a new snapshot, and advance the currentState pointer to be the
   * new snapshoted state. Sets workspaceChanged to false even though the
   * snapshot is not written to the server. This should be fine because
   * checkpoint is intended to be used in conjunction with undo and redo,
   * which will immediately change the state and note that it needs to
   * be written to the server
   *
   */
  public void checkpoint() throws SaveException {
    if (DEBUG) {
      System.out.println("Checkpoint");
    }
    synchronized (saverLock) {
      if (workspaceChangedByUser) {
        CodeblocksSourceOutput sourceOutput =
          controller.takeSnapshot(false /* don't tell autosaver */, true /* we need to synchronize */);
        addToHistory(sourceOutput.getContents());
        workspaceChangedByUser = false;
      }
    }
  }

  // Add stateString to history just past currentState and increment
  // currentState to point to the newly added state. Correct
  // the undo and redo buttons to reflect what is possible in the current
  // state.
  // Note: must be called with saverLock held
  private void addToHistory(String stateString) {
    if (DEBUG) {
      System.out.println("++addTohistory: current=" + currentState + ", size=" +
          history.size());
    }
    if (currentState >= 0 && stateString.equals(history.get(currentState))) {
      if (DEBUG) {
        System.out.println("stateString==history[currentState]. not adding");
      }
      return;
    }
    if (currentState == history.size() - 1) {
      // common case unless we've been undo'ing
      if (history.size() >= MAX_HISTORY_SIZE) {
        System.out.println("Truncating undo history.");
        history.remove(0);
        currentState -= 1; // adjust for shift in history items
      }
      currentState += 1;
      history.add(stateString);
      fixButtons();
    } else {
      // the new state goes into the history just after the current state
      currentState += 1;
      try {
        history.set(currentState, stateString);
        // clear out the history beyond index currentState. Do it from high
        // to low indexes to avoid having to shift elements.
        for (int i = history.size() - 1; i > currentState; --i) {
          history.remove(i);
        }
      } catch (IndexOutOfBoundsException e) {
        e.printStackTrace();
        if (DEBUG) {
          System.out.println("Unexpected bounds exception on history. " +
              "size = " + history.size() + ", index = " + currentState);
        }
      } finally {
        fixButtons();
      }
    }
    if (DEBUG) {
      System.out.println("--addTohistory: currentState = " + currentState + ", history.size() = "
          + history.size());
    }
  }

  /**
   * undo: First, do a checkpoint. Then, if there is a state before the
   * currentState in the history, back up the currentPointer to the
   * previous state and restore the workspace to that state. Finally, update
   * the phone definitions.
   * @throws UndoRedoException if there is no previous state
   * @throws SaveException if checkpointing fails
   */
  public void undo() throws UndoRedoException, SaveException {
    if (DEBUG) {
      System.out.println("Undo");
    }
    checkpoint();
    synchronized (saverLock) {
      if (currentState > 0) {
        stopListening();
        currentState -= 1;
        fixButtons();
        try {
          controller.doLoadBlocks(history.get(currentState));
          if (lastFormProperties == null) {
            controller.sendCurrentProjectDefinitionsToRepl();
          } else {
            controller.sendCurrentProjectDefinitionsToRepl(lastFormProperties);
          }
        } catch (LoadException e) {
          // TODO(sharon): not really acceptable. figure out something else
          // to do!
          FeedbackReporter.showSystemErrorMessage(
              "An unrecoverable error occurred while redo'ing. " +
              "Please exit and restart the blocks editor to recover your work");
          return;
        }
        workspaceChangedBySystem = true;
        startListening();
      } else {
        throw new UndoRedoException("Can't undo");
      }
    }
  }

  /**
   * redo: First, do a checkpoint. Then, if there is a state after the
   * currentState in the history, move the currentState pointer forward
   * and restores the workspace to that state. Finally, update the phone state.
   * @throws UndoRedoException if there is no next state
   * @throws SaveException if checkpointing fails
   */
  public void redo() throws UndoRedoException, SaveException {
    if (DEBUG) {
      System.out.println("Redo");
    }
    checkpoint();
    synchronized (saverLock) {
      if (currentState < history.size() - 1) {
        stopListening();
        currentState += 1;
        fixButtons();
        try {
          controller.doLoadBlocks(history.get(currentState));
          if (lastFormProperties == null) {
            controller.sendCurrentProjectDefinitionsToRepl();
          } else {
            controller.sendCurrentProjectDefinitionsToRepl(lastFormProperties);
          }
        } catch (LoadException e) {
          // TODO(sharon): not really acceptable. figure out something else
          // to do!
           FeedbackReporter.showSystemErrorMessage(
              "An unrecoverable error occurred while redo'ing. " +
              "Please exit and restart the blocks editor to recover your work");
           return;
        }
        workspaceChangedBySystem = true;
        startListening();
      } else {
        throw new UndoRedoException("Can't redo");
      }
    }
  }

  /**
   * Sets the workspaceChangedBySystem field to true so that the autosaver will
   * recognize that it needs to save the blocks workspace.
   */
  public void workspaceChangedBySystem() {
    lastEventTime = System.nanoTime();
    workspaceChangedBySystem = true;
  }

  /**
   * Clear the undo/redo history and disable the buttons
   * Must not hold saverLock on call
   */
  public void clearHistory() {
    synchronized (saverLock) {
      history = new ArrayList<String>();
      currentState = -1;
      fixButtons();
    }
  }

  // Fix up the enabled/disabled state of the undo and redo buttons based
  // on currentState and history. Call with saverLock held.
   private void fixButtons() {
    if (currentState > 0) {
      enableButton(undoButton);
    } else {
      disableButton(undoButton);
    }
    if (currentState < history.size() - 1)
      enableButton(redoButton);
    else {
      disableButton(redoButton);
    }
  }

}
