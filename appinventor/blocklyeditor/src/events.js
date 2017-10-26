'use strict';

goog.provide('AI.Events');

goog.require('Blockly.Events');

/**
 * Type identifier used for serializing CompanionConnect events.
 * @const {string}
 */
AI.Events.COMPANION_CONNECT = 'companion.connect';

/**
 * Type identifier used for serializing CompanionDisconnect events.
 * @const {string}
 */
AI.Events.COMPANION_DISCONNECT = 'companion.disconnect';

/**
 * Type identifier used for serializing ScreenSwitch events.
 * @const {string}
 */
AI.Events.SCREEN_SWITCH = 'screen.switch';

/**
 * Type identifier used for serializing ScreenPush events.
 * @const {string}
 */
AI.Events.SCREEN_PUSH = 'screen.push';

/**
 * Type identifier used for serializing ScreenPop events.
 * @const {string}
 */
AI.Events.SCREEN_POP = 'screen.pop';

/**
 * Type identifier used for serializing ComponentAdd events.
 * @const {string}
 */
AI.Events.COMPONENT_ADD = 'component.add';

/**
 * Type identifier used for serializing ComponentRemove events.
 * @const {string}
 */
AI.Events.COMPONENT_REMOVE = 'component.remove';

/**
 * Type identifier used for serializing ComponentMove events.
 * @const {string}
 */
AI.Events.COMPONENT_MOVE = 'component.move';

/**
 * Type identifier used for serializing PropertyChange events.
 * @const {string}
 */
AI.Events.COMPONENT_PROPERTY_CHANGE = 'property.change';

/**
 * Type identifier used for serializing StartArrangeBlocks events.
 * @type {string}
 */
AI.Events.BLOCKS_ARRANGE_START = 'blocks.arrange.start';

/**
 * Type identifier used for serializing EndArrangeBlocks events.
 * @type {string}
 */
AI.Events.BLOCKS_ARRANGE_END = 'blocks.arrange.end';

/**
 * Abstract class for all App Inventor events.
 * @constructor
 */
AI.Events.Abstract = function() {
  this.group = Blockly.Events.group_;
  this.recordUndo = Blockly.Events.recordUndo;
};

// Make AI.Events.Abstract a superclass of Blockly.Events.Abstract
goog.inherits(Blockly.Events.Abstract, AI.Events.Abstract);

/**
 * Set realtime to true if the event is a realtime event that should be processed by the
 * collaboration runtime. Certain classes of events are marked realtime=true if they and their
 * subclasses are meant to be realtime.
 *
 * @type {boolean}
 */
AI.Events.Abstract.prototype.realtime = false;

// Blockly Events are real-time.
Blockly.Events.Abstract.prototype.realtime = true;

/**
 * If true, the event is transient and should not trigger a save action (e.g., companion connected)
 * @type {boolean}
 */
AI.Events.Abstract.prototype.isTransient = false;

/**
 * The project id that the event is associated.
 *
 * @type {number}
 */
AI.Events.Abstract.prototype.projectId = null;

/**
 * The user id of the user that generated the event.
 *
 * @type {number}
 */
AI.Events.Abstract.prototype.userId = null;

/**
 * Base class of the event hierarchy regarding companion events.
 *
 * @constructor
 */
AI.Events.CompanionEvent = function() {
  AI.Events.CompanionEvent.superClass_.constructor.call(this);
};
goog.inherits(AI.Events.CompanionEvent, AI.Events.Abstract);

AI.Events.CompanionEvent.prototype.isTransient = true;

/**
 * Event raised when a connection has been established between the Companion and the ReplMgr.
 *
 * @constructor
 */
AI.Events.CompanionConnect = function() {
  AI.Events.CompanionConnect.superClass_.constructor.call(this);
};
goog.inherits(AI.Events.CompanionConnect, AI.Events.CompanionEvent);

AI.Events.CompanionConnect.prototype.type = AI.Events.COMPANION_CONNECT;

/**
 * Event raised when an existing connection between the ReplMgr and the Companion has been dropped.
 *
 * @constructor
 */
AI.Events.CompanionDisconnect = function() {
  AI.Events.CompanionDisconnect.superClass_.constructor.call(this);
};
goog.inherits(AI.Events.CompanionDisconnect, AI.Events.CompanionEvent);

AI.Events.CompanionDisconnect.prototype.type = AI.Events.COMPANION_DISCONNECT;

/**
 * Base class of the event hierarchy regarding screen events.
 *
 * @param {number} projectId Project ID of the currently active project editor.
 * @param {string} screenName Name of the screen being switched to.
 * @constructor
 */
AI.Events.ScreenEvent = function(projectId, screenName) {
  AI.Events.ScreenEvent.superClass_.constructor.call(this);
  this.projectId = projectId;
  this.screenName = screenName;
};
goog.inherits(AI.Events.ScreenEvent, AI.Events.Abstract);

// Changing screens is transient behavior.
AI.Events.ScreenEvent.prototype.isTransient = true;

/**
 * Event raised when a screen switch occurs in a project editor.
 *
 * @param {number} projectId
 * @param {string} screenName
 * @constructor
 */
AI.Events.ScreenSwitch = function(projectId, screenName) {
  AI.Events.ScreenSwitch.superClass_.constructor.call(this, projectId, screenName);
  this.recordUndo = false;
};
goog.inherits(AI.Events.ScreenSwitch, AI.Events.ScreenEvent);

AI.Events.ScreenSwitch.prototype.type = AI.Events.SCREEN_SWITCH;

/**
 * Event raised when a screen is pushed on the view stack in the Companion.
 *
 * @param {number} projectId
 * @param {string} screenName
 * @constructor
 */
AI.Events.ScreenPush = function(projectId, screenName) {
  AI.Events.ScreenPush.superClass_.constructor.call(this, projectId, screenName);
};
goog.inherits(AI.Events.ScreenPush, AI.Events.ScreenEvent);

AI.Events.ScreenPush.prototype.type = AI.Events.SCREEN_PUSH;

/**
 * Event raised when a screen is popped from the view stack in the Companion.
 *
 * @param {number} projectId
 * @constructor
 */
AI.Events.ScreenPop = function(projectId) {
  AI.Events.ScreenPop.superClass_.constructor.call(this, projectId);
};
goog.inherits(AI.Events.ScreenPop, AI.Events.ScreenEvent);

AI.Events.ScreenPop.prototype.type = AI.Events.SCREEN_POP;

/**
 * Base class for component-related events.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The newly created Component object.
 * @extends {AI.Events.Abstract}
 * @constructor
 */
AI.Events.ComponentEvent = function(projectId, component) {
  AI.Events.ComponentEvent.superClass_.constructor.call(this);
  this.projectId = projectId;
  this.component = component;
};
goog.inherits(AI.Events.ComponentEvent, AI.Events.Abstract);

// Component events need to be sent while collaborating in real time.
AI.Events.ComponentEvent.prototype.realtime = true;

/**
 * Event raised when a new Component has been dragged from the palette and dropped in the
 * Designer view.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The newly created Component object.
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentAdd = function(projectId, component) {
  if (!component) {
    return;  // Blank event to be populated by fromJson.
  }
  AI.Events.ComponentAdd.superClass_.constructor.call(this, projectId, component);
};
goog.inherits(AI.Events.ComponentAdd, AI.Events.ComponentEvent);

AI.Events.ComponentAdd.prototype.type = AI.Events.COMPONENT_ADD;

/**
 * Event raised when a Component has been removed from the screen.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The deleted Component object.
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentRemove = function(projectId, component) {
  if (!component) {
    return;  // Blank event for deserialization.
  }
  AI.Events.ComponentRemove.superClass_.constructor.call(this, projectId, component);
};
goog.inherits(AI.Events.ComponentRemove, AI.Events.ComponentEvent);

AI.Events.ComponentRemove.prototype.type = AI.Events.COMPONENT_REMOVE;

/**
 * Event raised when a Component has been moved in the component hierarchy.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The moved Component
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentMove = function(projectId, component) {
  if (!component) {
    return;  // Blank event for deserialization.
  }
  AI.Events.ComponentMove.superClass_.constructor.call(this, projectId, component);
  var location = this.currentLocation_();
  this.oldParentUuid = location.parentUuid;
  this.oldIndex = location.index;
};
goog.inherits(AI.Events.ComponentMove, AI.Events.ComponentEvent);

AI.Events.ComponentMove.prototype.type = AI.Events.COMPONENT_MOVE;

/**
 *
 * @param projectId
 * @param component
 * @param property
 * @param oldValue
 * @param newValue
 * @constructor
 */
AI.Events.PropertyChange = function(projectId, component, property, oldValue, newValue) {
  if (!component) {
    return;  // Blank event for deserialization.
  }
  this.property = property;
  this.oldValue = oldValue;
  this.newValue = newValue;
};

/**
 * StartArrangeBlocks is an event placed at the start of an event group created during an
 * arrangement operation. Its purpose is to reset the Blockly.workspace_arranged* flags during an
 * undo operation so that they reflect the state immediately preceding the arrangement.
 * @param workspaceId The identifier of the workspace the event occurred on
 * @constructor
 */
AI.Events.StartArrangeBlocks = function(workspaceId) {
  AI.Events.StartArrangeBlocks.superClass_.constructor.call(this);
  this.old_arranged_type = Blockly.workspace_arranged_type;
  this.old_arranged_position = Blockly.workspace_arranged_position;
  this.old_arranged_latest_position = Blockly.workspace_arranged_latest_position;
  this.recordUndo = Blockly.Events.recordUndo;
  this.workspaceId = workspaceId;
};
goog.inherits(AI.Events.StartArrangeBlocks, Blockly.Events.Ui);

AI.Events.StartArrangeBlocks.prototype.type = AI.Events.BLOCKS_ARRANGE_START;

AI.Events.StartArrangeBlocks.prototype.toJson = function() {
  var json = AI.Events.StartArrangeBlocks.superClass_.toJson.call(this);
  json['old_arranged_type'] = this.old_arranged_type;
  json['old_arranged_position'] = this.old_arranged_position;
  json['old_arranged_latest_position'] = this.old_arranged_latest_position;
  return json;
};

AI.Events.StartArrangeBlocks.prototype.fromJson = function(json) {
  AI.Events.StartArrangeBlocks.superClass_.fromJson.call(this, json);
  this.old_arranged_type = json['old_arranged_type'];
  this.old_arranged_position = json['old_arranged_position'];
  this.old_arranged_latest_position = json['old_arranged_latest_position'];
};

AI.Events.StartArrangeBlocks.prototype.run = function(forward) {
  if (!forward) {
    Blockly.Events.FIRE_QUEUE_.length = 0;
    setTimeout(function() {
      Blockly.workspace_arranged_type = this.old_arranged_type;
      Blockly.workspace_arranged_position = this.old_arranged_position;
      Blockly.workspace_arranged_latest_position = this.old_arranged_latest_position;
    }.bind(this));
  }
};

/**
 * EndArrangeBlocks is an event placed at the end of an event group created during an
 * arrangement operation. Its purpose is to set the Blockly.workspace_arranged* flags to the
 * appropriate values since they are reset by {@Blockly.WorkspaceSvg#fireChangeListener}.
 * @param type The type of arrangement (either null or Blockly.BLKS_CATEGORY)
 * @param layout The layout to be applied (either Blockly.BLKS_VERTICAL or Blockly.BLKS_HORIZONTAL)
 * @constructor
 */
AI.Events.EndArrangeBlocks = function(workspaceId, type, layout) {
  AI.Events.EndArrangeBlocks.superClass_.constructor.call(this);
  this.new_type = type;
  this.new_layout = layout;
  this.recordUndo = Blockly.Events.recordUndo;
  this.workspaceId = workspaceId;
};
goog.inherits(AI.Events.EndArrangeBlocks, Blockly.Events.Ui);

AI.Events.EndArrangeBlocks.prototype.type = AI.Events.BLOCKS_ARRANGE_END;

AI.Events.EndArrangeBlocks.prototype.toJson = function() {
  var json = AI.Events.EndArrangeBlocks.superClass_.toJson.call(this);
  json['new_type'] = this.new_type;
  json['new_layout'] = this.new_layout;
  return json;
};

AI.Events.EndArrangeBlocks.prototype.fromJson = function(json) {
  AI.Events.EndArrangeBlocks.superClass_.fromJson.call(this, json);
  this.new_type = json['new_type'];
  this.new_layout = json['new_layout'];
};

AI.Events.EndArrangeBlocks.prototype.run = function(forward) {
  if (forward) {
    Blockly.Events.FIRE_QUEUE_.length = 0;
    setTimeout(function() {
      Blockly.workspace_arranged_type = this.new_type;
      Blockly.workspace_arranged_position = this.new_layout;
      Blockly.workspace_arranged_latest_position = this.new_layout;
    }.bind(this));
  }
};


/**
 * Filter the queued events and merge duplicates. This version is O(n) versus the implementation
 * provided by Blockly that is O(n^2). This improves performance when people perform or undo
 * operations that create, move, or delete a large number of blocks all at once.
 * @param {!Array.<!Blockly.Events.Abstract>} queueIn Array of events.
 * @param {boolean} forward True if forward (redo), false if backward (undo).
 * @return {!Array.<!Blockly.Events.Abstract>} Array of filtered events.
 */
Blockly.Events.filter = function(queueIn, forward) {
  var queue = goog.array.clone(queueIn);
  if (!forward) {
    // Undo is merged in reverse order.
    queue.reverse();
  }
  var queue2 = [];
  var hash = {};
  // Merge duplicates.
  for (var i = 0, event; event = queue[i]; i++) {
    if (!event.isNull()) {
      var key = [event.type, event.blockId, event.workspaceId].join(' ');
      if (hash[key] === undefined) {
        hash[key] = event;
        queue2.push(event);
      } else if (event.type == Blockly.Events.MOVE) {
        // Merge move events.
        hash[key].newParentId = event.newParentId;
        hash[key].newInputName = event.newInputName;
        hash[key].newCoordinate = event.newCoordinate;
      } else if (event.type == Blockly.Events.CHANGE &&
        event.element == hash[key].element &&
        event.name == hash[key].name) {
        // Merge change events.
        hash[key].newValue = event.newValue;
      } else if (event.type == Blockly.Events.UI &&
        hash[key].element == 'click' &&
        (event.element == 'commentOpen' ||
        event.element == 'mutatorOpen' ||
        event.element == 'warningOpen')) {
        // Merge change events.
        hash[key].newValue = event.newValue;
      } else {
        // Collision, but newer events should merge into this event to maintain order
        hash[key] = event;
        queue2.push(event);
      }
    }
  }
  // After merging, it is possible that the product of merging two events where isNull() returned
  // false now returns true. This is one last pass to remove these null events on the filtered
  // queue.
  queue = queue2.filter(function(e) { return !e.isNull(); });
  if (!forward) {
    // Restore undo order.
    queue.reverse();
  }
  // Move mutation events to the top of the queue.
  // Intentionally skip first event.
  for (var i = 1, event; event = queue[i]; i++) {
    if (event.type == Blockly.Events.CHANGE &&
      event.element == 'mutation') {
      queue.unshift(queue.splice(i, 1)[0]);
    }
  }
  return queue;
};
