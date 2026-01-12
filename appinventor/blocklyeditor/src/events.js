// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2016-2025 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// noinspection ES6ConvertVarToLetConst

'use strict';

goog.provide('AI.Events');

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
 * Type identifier used for programmatic workspace shifts.
 * @type {string}
 */
AI.Events.WORKSPACE_VIEWPORT_MOVE = "blocks.workspace.move";

/**
 * Type identifier used for forcing a workspace save (required after upgrades).
 * @type {string}
 */
AI.Events.FORCE_SAVE = 'blocks.save.force';

/**
 * Abstract class for all App Inventor events.
 * @constructor
 */
AI.Events.Abstract = class {
  /**
   * Set realtime to true if the event is a realtime event that should be processed by the
   * collaboration runtime. Certain classes of events are marked realtime=true if they and their
   * subclasses are meant to be realtime.
   *
   * @type {boolean}
   */
  realtime = false;

  /**
   * If true, the event is transient and should not trigger a save action (e.g., companion connected)
   * @type {boolean}
   */
  isTransient = false;

  /**
   * The project id that the event is associated.
   *
   * @type {?string}
   */
  projectId = null;

  /**
   * The user id of the user that generated the event.
   *
   * @type {?string}
   */
  userId = null;

  constructor() {
    this.group = Blockly.Events.getGroup();
    this.recordUndo = Blockly.Events.getRecordUndo();
  }

  isNull() {
    return false;
  }

  toJson() {
    let json = {};
    json['type'] = this.type;
    json['group'] = this.group;
    json['recordUndo'] = this.recordUndo;
    return json;
  }

  fromJson(json) {
    this.type = json['type'];
    this.group = json['group'];
    this.recordUndo = json['recordUndo'];
  }
};

// Make AI.Events.Abstract a superclass of Blockly.Events.Abstract
Blockly.Events.Abstract.prototype = new AI.Events.Abstract();
// Blockly Events are real-time.
Blockly.Events.Abstract.prototype.realtime = true;

/**
 * Base class of the event hierarchy regarding companion events.
 *
 * @constructor
 */
AI.Events.CompanionEvent = class extends AI.Events.Abstract {
  isTransient = true;
}

/**
 * Event raised when a connection has been established between the Companion and the ReplMgr.
 *
 * @constructor
 */
AI.Events.CompanionConnect = class extends AI.Events.CompanionEvent {
  type = AI.Events.COMPANION_CONNECT;
}

/**
 * Event raised when an existing connection between the ReplMgr and the Companion has been dropped.
 *
 * @constructor
 */
AI.Events.CompanionDisconnect = class extends AI.Events.CompanionEvent {
  type = AI.Events.COMPANION_DISCONNECT;
}

/**
 * Base class of the event hierarchy regarding screen events.
 *
 * @param {number} projectId Project ID of the currently active project editor.
 * @param {string} screenName Name of the screen being switched to.
 * @constructor
 */
AI.Events.ScreenEvent = class extends AI.Events.Abstract {
  isTransient = true;

  constructor(projectId, screenName) {
    super();
    this.projectId = projectId;
    this.screenName = screenName;
  }
}

/**
 * Event raised when a screen switch occurs in a project editor.
 *
 * @param {number} projectId
 * @param {string} screenName
 * @constructor
 */
AI.Events.ScreenSwitch = class extends AI.Events.ScreenEvent {
  type = AI.Events.SCREEN_SWITCH;

  constructor(projectId, screenName) {
    super(projectId, screenName);
    this.recordUndo = false;
  }
}

/**
 * Event raised when a screen is pushed on the view stack in the Companion.
 *
 * @param {number} projectId
 * @param {string} screenName
 * @constructor
 */
AI.Events.ScreenPush = class extends AI.Events.ScreenEvent {
  type = AI.Events.SCREEN_PUSH;

  constructor(projectId, screenName) {
    super(projectId, screenName);
  }
};

/**
 * Event raised when a screen is popped from the view stack in the Companion.
 *
 * @param {number} projectId
 * @constructor
 */
AI.Events.ScreenPop = class extends AI.Events.ScreenEvent {
  type = AI.Events.SCREEN_POP;

  constructor(projectId) {
    super(projectId);
  }
};

/**
 * Base class for component-related events.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The newly created Component object.
 * @extends {AI.Events.Abstract}
 * @constructor
 */
AI.Events.ComponentEvent = class extends AI.Events.Abstract {
  realtime = true;

  constructor(projectId, component) {
    super();
    this.projectId = projectId;
    this.component = component;
  }
};

/**
 * Event raised when a new Component has been dragged from the palette and dropped in the
 * Designer view.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The newly created Component object.
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentAdd = class extends AI.Events.ComponentEvent {
  type = AI.Events.COMPONENT_ADD;

  constructor(projectId, component) {
    super(projectId, component);
  };
}

/**
 * Event raised when a Component has been removed from the screen.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The deleted Component object.
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentRemove = class extends AI.Events.ComponentEvent {
  type = AI.Events.COMPONENT_REMOVE;

  constructor(projectId, component) {
    super(projectId, component);
  }
}

/**
 * Event raised when a Component has been moved in the component hierarchy.
 *
 * @param {number} projectId Project ID of the project the designer editor is editing.
 * @param {{}} component The moved Component
 * @extends {AI.Events.ComponentEvent}
 * @constructor
 */
AI.Events.ComponentMove = class extends AI.Events.ComponentEvent {
  type = AI.Events.COMPONENT_MOVE;

  constructor(projectId, component) {
    super(projectId, component);
    let location = this.currentLocation_();
    this.oldParentUuid = location.parentUuid;
    this.oldIndex = location.index;
  };
}

/**
 *
 * @param projectId
 * @param component
 * @param property
 * @param oldValue
 * @param newValue
 * @constructor
 */
AI.Events.PropertyChange = class extends AI.Events.ComponentEvent {
  constructor(projectId, component, property, oldValue, newValue) {
    super(projectId, component);
    this.property = property;
    this.oldValue = oldValue;
    this.newValue = newValue;
  };
}

/**
 * StartArrangeBlocks is an event placed at the start of an event group created during an
 * arrangement operation. Its purpose is to reset the Workspace.arranged* flags during an
 * undo operation so that they reflect the state immediately preceding the arrangement.
 * @param workspaceId The identifier of the workspace the event occurred on
 * @constructor
 */
AI.Events.StartArrangeBlocks = class extends Blockly.Events.Abstract {
  type = AI.Events.BLOCKS_ARRANGE_START;

  constructor(workspaceId) {
    super(workspaceId);
    const workspace = Blockly.Workspace.getById(workspaceId);
    this.old_arranged_type = workspace.arranged_type_;
    this.old_arranged_position = workspace.arranged_position_;
    this.old_arranged_latest_position = workspace.arranged_latest_position_;
    this.recordUndo = Blockly.Events.getRecordUndo();
    this.workspaceId = workspaceId;
  };

  toJson() {
    let json = super.toJson();
    json['old_arranged_type'] = this.old_arranged_type;
    json['old_arranged_position'] = this.old_arranged_position;
    json['old_arranged_latest_position'] = this.old_arranged_latest_position;
    return json;
  }

  fromJson(json) {
    super.fromJson(json);
    this.old_arranged_type = json['old_arranged_type'];
    this.old_arranged_position = json['old_arranged_position'];
    this.old_arranged_latest_position = json['old_arranged_latest_position'];
  }

  run(forward) {
    if (!forward) {
      // TODO(ewpatton): Determine if this is still needed and how to do it with Blockly 10
      // Blockly.Events.FIRE_QUEUE_.length = 0;
      setTimeout(function() {
        const workspace = Blockly.Workspace.getById(this.workspaceId);
        workspace.arranged_type_ = this.old_arranged_type;
        workspace.arranged_position_ = this.old_arranged_position;
        workspace.arranged_latest_position_ = this.old_arranged_latest_position;
      }.bind(this));
    }
  }
}

/**
 * EndArrangeBlocks is an event placed at the end of an event group created during an
 * arrangement operation. Its purpose is to set the Workspace.arranged* flags to the
 * appropriate values since they are reset by {@Blockly.WorkspaceSvg#fireChangeListener}.
 * @param type The type of arrangement (either null or Blockly.BLKS_CATEGORY)
 * @param layout The layout to be applied (either Blockly.BLKS_VERTICAL or Blockly.BLKS_HORIZONTAL)
 * @constructor
 */
AI.Events.EndArrangeBlocks = class extends Blockly.Events.Abstract {
  type = AI.Events.BLOCKS_ARRANGE_END;

  constructor(workspaceId, type, layout) {
    super(workspaceId);
    this.new_type = type;
    this.new_layout = layout;
    this.recordUndo = Blockly.Events.getRecordUndo();
    this.workspaceId = workspaceId;
  };

  toJson() {
    let json = super.toJson();
    json['new_type'] = this.new_type;
    json['new_layout'] = this.new_layout;
    return json;
  }

  fromJson(json) {
    super.fromJson(json);
    this.new_type = json['new_type'];
    this.new_layout = json['new_layout'];
  }

  run(forward) {
    if (forward) {
      // TODO(ewpatton): Determine if this is still needed and how to do it with Blockly 10
      // Blockly.Events.FIRE_QUEUE_.length = 0;
      setTimeout(function() {
        const workspace = Blockly.Workspace.getById(this.workspaceId);
        workspace.arranged_type_ = this.new_type;
        workspace.arranged_position_ = this.new_layout;
        workspace.arranged_latest_position_ = this.new_layout;
      }.bind(this), 1000);
    }
  }
}

/**
 * Class for capturing when the workspace is moved programmatically, to allow undoing by the user.
 * @param {string} workspaceId The workspace that is being moved.
 * @constructor
 */
AI.Events.WorkspaceMove = class extends AI.Events.Abstract {
  type = AI.Events.WORKSPACE_VIEWPORT_MOVE;
  isTransient = true;

  constructor(workspaceId) {
    super();
    this.workspaceId = workspaceId;
    let metrics = Blockly.Workspace.getById(workspaceId).getMetrics();
    this.oldX = metrics.viewLeft - metrics.scrollLeft;
    this.oldY = metrics.viewTop - metrics.scrollTop;
    this.newX = null;
    this.newY = null;
  }

  /**
   * Record the new state of the workspace after the operation has occurred.
   */
  recordNew() {
    var metrics = Blockly.Workspace.getById(this.workspaceId).getMetrics();
    this.newX = metrics.viewLeft - metrics.scrollLeft;
    this.newY = metrics.viewTop - metrics.scrollTop;
  };

  /**
   * Check whether the event is null. For workspace moves, this is true if and only if that the new
   * and old coordinates are the same.
   * @returns {boolean}
   */
  isNull() {
    return this.oldX === this.newX && this.oldY === this.newY;
  };

  toJson() {
    let json = super.toJson();
    json['workspaceId'] = this.workspaceId;
    if (this.newX) {
      json['newX'] = this.newX;
    }
    if (this.newY) {
      json['newY'] = this.newY;
    }
    return json;
  }

  fromJson(json) {
    super.fromJson(json);
    this.workspaceId = json['workspaceId'];
    this.newX = json['newX'];
    this.newY = json['newY'];
  }

  /**
   * Run a workspace move event.
   * @param {boolean} forward True if run forward, false if run backward (undo).
   */
  run(forward) {
    let workspace = Blockly.Workspace.getById(this.workspaceId);
    let x = forward ? this.newX : this.oldX;
    let y = forward ? this.newY : this.oldY;
    workspace.scrollbar.set(x, y, true);
  }
};

/**
 * An event used to trigger a save of the blocks workspace.
 * @param {Blockly.Workspace=} workspace The workspace to be saved.
 * @constructor
 */
AI.Events.ForceSave = class extends AI.Events.Abstract {
  /**
   * The type of the event.
   * @type {string}
   */
  type = AI.Events.FORCE_SAVE;

  /**
   * ForceSave must not be transient. The isTransient flag is used to determine whether or not to
   * save the workspace, so if ForceSave were transient the workspace would not save.
   * @type {boolean}
   */
  isTransient = false;

  constructor(workspace) {
    super();
    if (workspace) {
      this.workspaceId = workspace.id;
    }
    this.recordUndo = false;
  };

  /**
   * Serialize the ForceSave event as a JSON object.
   * @returns {Object}
   */
  toJson() {
    let json = super.toJson();
    json['workspaceId'] = this.workspaceId;
    return json;
  }

  /**
   * Deserialize the ForceSave event form a JSON object.
   * @param {Object} json A JSON object previously created by {@link #toJson()}
   */
  fromJson(json) {
    super.fromJson(json);
    this.workspaceId = json['workspaceId'];
  }
}
