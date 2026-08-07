// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages undo and redo stacks for the Designer panel.
 *
 * <p>Implements {@link DesignerChangeListener} to automatically record undoable
 * commands as the user modifies the form in the visual designer.
 *
 * <p>One instance exists per {@link YaFormEditor} (i.e., per screen).
 */
public class DesignerUndoManager implements DesignerChangeListener {
  private static final Logger LOG = Logger.getLogger(DesignerUndoManager.class.getName());

  private static final int MAX_UNDO_SIZE = 100;
  private static final int MAX_REDO_SIZE = 50;
  private static final int PROPERTY_CHANGE_DEBOUNCE_MS = 500;

  private final YaFormEditor formEditor;

  private final List<DesignerCommand> undoStack = new ArrayList<DesignerCommand>();
  private final List<DesignerCommand> redoStack = new ArrayList<DesignerCommand>();

  /**
   * When true, form change events are ignored (not recorded as commands).
   * Set during undo/redo execution to prevent recursive recording.
   */
  private boolean suppressRecording = false;

  // --- Property change debouncing ---

  /** Pending property change command, awaiting debounce timer. */
  private PropertyChangeCommand pendingPropertyCommand = null;
  private Timer propertyChangeTimer = null;

  /**
   * Tracks the last-known property values for all components, keyed by
   * "componentUuid:propertyName". Used to determine the old value when
   * a property change event fires (since the value has already changed).
   */
  private final Map<String, String> lastKnownPropertyValues = new HashMap<String, String>();

  // --- Move detection ---

  /** Temporary state for detecting component moves (remove with permanentlyDeleted=false). */
  private PendingMoveInfo pendingMove = null;

  // --- Before-remove capture ---

  /** Captures container/index info before a component is removed from the children list. */
  private BeforeRemoveInfo beforeRemoveInfo = null;

  /** Callback to update toolbar button states. */
  private UndoRedoStateListener stateListener;

  /**
   * Creates a new DesignerUndoManager for the given form editor.
   */
  public DesignerUndoManager(YaFormEditor formEditor) {
    this.formEditor = formEditor;
  }

  // --- Public API ---

  /**
   * Initializes the property value tracking map with the current state of all components.
   * Should be called once after the form is fully loaded.
   */
  public void initializePropertyTracking() {
    lastKnownPropertyValues.clear();
    Map<String, MockComponent> components = formEditor.getComponents();
    for (MockComponent component : components.values()) {
      trackComponentProperties(component);
    }
  }

  /**
   * Performs an undo operation. Pops the top command from the undo stack,
   * pushes it to the redo stack, and calls its undo() method.
   */
  public void undo() {
    flushPendingPropertyCommand();
    if (undoStack.isEmpty()) {
      return;
    }
    DesignerCommand command = undoStack.remove(undoStack.size() - 1);
    if (redoStack.size() >= MAX_REDO_SIZE) {
      redoStack.remove(0);
    }
    redoStack.add(command);

    suppressRecording = true;
    try {
      command.undo();
    } finally {
      suppressRecording = false;
    }

    Ode.getInstance().getEditorManager().scheduleAutoSave(formEditor);
    notifyStateListener();
    LOG.info("Undo: " + command.getDescription());
  }

  /**
   * Performs a redo operation. Pops the top command from the redo stack,
   * pushes it to the undo stack, and calls its execute() method.
   */
  public void redo() {
    flushPendingPropertyCommand();
    if (redoStack.isEmpty()) {
      return;
    }
    DesignerCommand command = redoStack.remove(redoStack.size() - 1);
    if (undoStack.size() >= MAX_UNDO_SIZE) {
      undoStack.remove(0);
    }
    undoStack.add(command);

    suppressRecording = true;
    try {
      command.execute();
    } finally {
      suppressRecording = false;
    }

    Ode.getInstance().getEditorManager().scheduleAutoSave(formEditor);
    notifyStateListener();
    LOG.info("Redo: " + command.getDescription());
  }

  public boolean canUndo() {
    return !undoStack.isEmpty() || pendingPropertyCommand != null;
  }

  public boolean canRedo() {
    return !redoStack.isEmpty();
  }

  /**
   * Clears both undo and redo stacks and resets all internal state.
   */
  public void clear() {
    undoStack.clear();
    redoStack.clear();
    pendingPropertyCommand = null;
    if (propertyChangeTimer != null) {
      propertyChangeTimer.cancel();
      propertyChangeTimer = null;
    }
    pendingMove = null;
    beforeRemoveInfo = null;
    lastKnownPropertyValues.clear();
    notifyStateListener();
  }

  /**
   * Returns the form editor associated with this undo manager.
   */
  public YaFormEditor getFormEditor() {
    return formEditor;
  }

  /**
   * Sets a listener to be notified when the undo/redo state changes
   * (e.g. to enable/disable toolbar buttons).
   */
  public void setStateListener(UndoRedoStateListener listener) {
    this.stateListener = listener;
  }

  /**
   * Returns whether recording is currently suppressed (during undo/redo execution).
   */
  public boolean isSuppressed() {
    return suppressRecording;
  }

  // --- DesignerChangeListener implementation ---

  @Override
  public void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
    if (suppressRecording) {
      return;
    }
    if (!component.isPropertyPersisted(propertyName)) {
      return;
    }

    String key = component.getUuid() + ":" + propertyName;
    String oldValue = lastKnownPropertyValues.get(key);
    if (oldValue == null) {
      oldValue = propertyValue; // fallback if not tracked
    }

    // Update tracking
    lastKnownPropertyValues.put(key, propertyValue);

    // If there's already a pending command for the same component+property, coalesce
    if (pendingPropertyCommand != null
        && pendingPropertyCommand.getComponentUuid().equals(component.getUuid())
        && pendingPropertyCommand.getPropertyName().equals(propertyName)) {
      pendingPropertyCommand.setNewValue(propertyValue);
      restartPropertyTimer();
      return;
    }

    // Flush any existing pending command for a different property
    flushPendingPropertyCommand();

    // Create new pending command
    pendingPropertyCommand = new PropertyChangeCommand(
        formEditor, component.getUuid(), component.getName(),
        propertyName, oldValue, propertyValue);
    restartPropertyTimer();
  }

  @Override
  public void onBeforeComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (suppressRecording) {
      return;
    }
    // Capture the container and index BEFORE the component is removed from the children list
    MockContainer container = component.getContainer();
    if (container != null) {
      int index = container.getChildIndex(component);
      beforeRemoveInfo = new BeforeRemoveInfo(
          container.getUuid(), index, component.isVisibleComponent());
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (suppressRecording) {
      return;
    }

    if (!permanentlyDeleted) {
      // This is the first half of a move operation.
      // Store the source info and wait for onComponentAdded.
      if (beforeRemoveInfo != null) {
        pendingMove = new PendingMoveInfo(
            component.getUuid(),
            beforeRemoveInfo.containerUuid,
            beforeRemoveInfo.childIndex);
        beforeRemoveInfo = null;
      }
      return;
    }

    // Permanent deletion: capture the component's full state.
    flushPendingPropertyCommand();

    String componentJson = formEditor.encodeComponentAsJson(component);
    String containerUuid = beforeRemoveInfo != null ? beforeRemoveInfo.containerUuid : null;
    int childIndex = beforeRemoveInfo != null ? beforeRemoveInfo.childIndex : -1;
    boolean isVisible = beforeRemoveInfo != null ? beforeRemoveInfo.isVisible : component.isVisibleComponent();
    beforeRemoveInfo = null;

    DeleteComponentCommand command = new DeleteComponentCommand(
        formEditor, componentJson, containerUuid, childIndex, isVisible);
    pushUndoCommand(command);

    // Remove tracked property values for deleted component
    removeComponentPropertyTracking(component);
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    if (suppressRecording) {
      // Even when suppressed, update property tracking for new components
      trackComponentProperties(component);
      return;
    }

    // Check if this is the second half of a move operation
    if (pendingMove != null && pendingMove.componentUuid.equals(component.getUuid())) {
      flushPendingPropertyCommand();

      MockContainer newContainer = component.getContainer();
      String dstContainerUuid = newContainer != null ? newContainer.getUuid() : null;
      int dstIndex = newContainer != null ? newContainer.getChildIndex(component) : -1;

      MoveComponentCommand command = new MoveComponentCommand(
          formEditor, component.getUuid(),
          pendingMove.srcContainerUuid, pendingMove.srcChildIndex,
          dstContainerUuid, dstIndex);
      pendingMove = null;
      pushUndoCommand(command);
      return;
    }
    pendingMove = null;

    // Regular add (from palette, paste, duplicate)
    flushPendingPropertyCommand();

    MockContainer container = component.getContainer();
    String containerUuid = container != null ? container.getUuid() : null;
    int childIndex = container != null ? container.getChildIndex(component) : -1;
    String componentJson = formEditor.encodeComponentAsJson(component);

    AddComponentCommand command = new AddComponentCommand(
        formEditor, componentJson, containerUuid, childIndex,
        component.getUuid(), !component.isVisibleComponent());
    pushUndoCommand(command);

    // Track properties for the new component
    trackComponentProperties(component);
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    if (suppressRecording) {
      return;
    }
    flushPendingPropertyCommand();

    RenameComponentCommand command = new RenameComponentCommand(
        formEditor, component.getUuid(), oldName, component.getName());
    pushUndoCommand(command);
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    // Selection changes are not undoable.
  }

  // --- Internal helpers ---

  private void pushUndoCommand(DesignerCommand command) {
    if (undoStack.size() >= MAX_UNDO_SIZE) {
      undoStack.remove(0);
    }
    undoStack.add(command);
    redoStack.clear();
    notifyStateListener();
    LOG.info("Recorded: " + command.getDescription());
  }

  private void flushPendingPropertyCommand() {
    if (propertyChangeTimer != null) {
      propertyChangeTimer.cancel();
      propertyChangeTimer = null;
    }
    if (pendingPropertyCommand != null) {
      // Only push if the value actually changed
      if (!pendingPropertyCommand.getOldValue().equals(pendingPropertyCommand.getNewValue())) {
        if (undoStack.size() >= MAX_UNDO_SIZE) {
          undoStack.remove(0);
        }
        undoStack.add(pendingPropertyCommand);
        redoStack.clear();
        LOG.info("Recorded (flushed): " + pendingPropertyCommand.getDescription());
      }
      pendingPropertyCommand = null;
      notifyStateListener();
    }
  }

  private void restartPropertyTimer() {
    if (propertyChangeTimer != null) {
      propertyChangeTimer.cancel();
    }
    propertyChangeTimer = new Timer() {
      @Override
      public void run() {
        flushPendingPropertyCommand();
      }
    };
    propertyChangeTimer.schedule(PROPERTY_CHANGE_DEBOUNCE_MS);
  }

  private void trackComponentProperties(MockComponent component) {
    String uuid = component.getUuid();
    for (EditableProperty property : component.getProperties()) {
      if (component.isPropertyPersisted(property.getName())) {
        String key = uuid + ":" + property.getName();
        lastKnownPropertyValues.put(key, property.getValue());
      }
    }
  }

  private void removeComponentPropertyTracking(MockComponent component) {
    String uuid = component.getUuid();
    List<String> keysToRemove = new ArrayList<String>();
    for (String key : lastKnownPropertyValues.keySet()) {
      if (key.startsWith(uuid + ":")) {
        keysToRemove.add(key);
      }
    }
    for (String key : keysToRemove) {
      lastKnownPropertyValues.remove(key);
    }
  }

  private void notifyStateListener() {
    if (stateListener != null) {
      stateListener.onUndoRedoStateChanged(canUndo(), canRedo());
    }
  }

  // --- Inner data classes ---

  private static class PendingMoveInfo {
    final String componentUuid;
    final String srcContainerUuid;
    final int srcChildIndex;

    PendingMoveInfo(String componentUuid, String srcContainerUuid, int srcChildIndex) {
      this.componentUuid = componentUuid;
      this.srcContainerUuid = srcContainerUuid;
      this.srcChildIndex = srcChildIndex;
    }
  }

  private static class BeforeRemoveInfo {
    final String containerUuid;
    final int childIndex;
    final boolean isVisible;

    BeforeRemoveInfo(String containerUuid, int childIndex, boolean isVisible) {
      this.containerUuid = containerUuid;
      this.childIndex = childIndex;
      this.isVisible = isVisible;
    }
  }

  /**
   * Listener interface for undo/redo state changes (to update UI buttons).
   */
  public interface UndoRedoStateListener {
    void onUndoRedoStateChanged(boolean canUndo, boolean canRedo);
  }
}
