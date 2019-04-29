/* -*- mode: javascript; js-indent-level 2; -*- */
/**
 * @license
 * Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.
 */

/**
 * @fileoverview A database for tracking component and component types
 * within the Blockly workspace.
 * @author Evan W. Patton <ewpatton@mit.edu>
 */

'use strict';

goog.provide('AI.Blockly.ComponentDatabase');

Blockly.PROPERTY_READABLE = 1;
Blockly.PROPERTY_WRITEABLE = 2;
Blockly.PROPERTY_READWRITEABLE = 3;

/**
 * @typedef ComponentInfo
 * @type {object}
 * @property {string} type
 * @property {string} name
 * @property {string} external
 * @property {string} version
 * @property {string} categoryString
 * @property {string} helpString
 * @property {string} showOnPalette
 * @property {string} nonVisible
 * @property {string} iconName
 * @property {Object.<string, EventDescriptor>} events
 * @property {Object.<string, PropertyDescriptor>} properties
 * @property {Object.<string, Object<string, string>>} blockProperties
 * @property {Object.<string, MethodDescriptor>} methods
 */
ComponentInfo = function() {};

/**
 * @typedef ParameterDescriptor
 * @type {object}
 * @property {!string} name
 * @property {!type} type
 */
ParameterDescriptor = function() {};

/**
 * @typedef {{name: !string, description: !string, deprecated: ?boolean, parameters: !ParameterDescriptor[]}}
 */
EventDescriptor = function() {};

/**
 * @typedef {{name: !string, description: !string, deprecated: ?boolean, parameters: !ParameterDescriptor[], returnType: ?string}}
 */
MethodDescriptor = function() {};

/**
 * @typedef PropertyDescriptor
 * @type {object}
 * @property {!string} name
 * @property {!string} description
 * @property {!string} type
 * @property {!string} rw
 * @property {?boolean} deprecated
 */
PropertyDescriptor = function() {};

/**
 * @typedef ComponentTypeDescriptor
 * @type {object}
 * @property {!string} type
 * @property {!string} external
 * @property {!ComponentInfo} componentInfo
 * @property {!Object.<string, EventDescriptor>} eventDictionary
 * @property {!Object.<string, MethodDescriptor>} methodDictionary
 * @property {!Object.<string, PropertyDescriptor>} properties
 * @property {!string[]} setPropertyList
 * @property {!string[]} getPropertyList
 */
ComponentTypeDescriptor = function() {};

/**
 * @typedef ComponentInstanceDescriptor
 * @type {object}
 * @property {!string} name
 * @property {!string} typeName
 */
ComponentInstanceDescriptor = function() {};

/**
 * Database for component type information and instances.
 * @constructor
 */
Blockly.ComponentDatabase = function() {
  /** @type {Object.<string, ComponentInstanceDescriptor>} */
  this.instances_ = {};
  /** @type {Object.<string, ComponentTypeDescriptor>} */
  this.types_ = {};
  // For migration of old projects that are name based rather than uid based.
  /** @type {Object.<string, string>} */
  this.instanceNameUid_ = {};

  // Internationalization support
  this.i18nComponentTypes_ = {};
  this.i18nEventNames_ = {};
  this.i18nMethodNames_ = {};
  this.i18nParamNames_ = {};
  this.i18nPropertyNames_ = {};
};

/**
 * Add a new instance to the ComponentDatabase.
 * @param {!string} uid UUID of the component instance
 * @param {!string} name Name of the component instance
 * @param {!string} typeName Type of the component instance
 * @returns {boolean} true if the component was added, false if a component exists with the given
 * UUID.
 */
Blockly.ComponentDatabase.prototype.addInstance = function(uid, name, typeName) {
  if (this.hasInstance(uid)) {
    return false;
  }
  this.instances_[uid] = {name: name, typeName: typeName};
  this.instanceNameUid_[name] = uid;
  return true;
};

/**
 * Check whether an instance exists with the given UUID.
 * @param {!string} uid UUID to look up in the database
 * @returns {boolean} true if a component exists with the UUID, otherwise false.
 */
Blockly.ComponentDatabase.prototype.hasInstance = function(uid) {
  return uid in this.instances_;
};

/**
 * Get a component instance for the given UUID or Name.
 * @param {!string} uidOrName UUID for the component. This method also takes a Name for backwards
 * compatibility with methods that do not yet refer to a component by UUID.
 * @returns {{typeName: !string, name: !string}|ComponentInstanceDescriptor} An internal descriptor
 * of a component, otherwise undefined.
 */
Blockly.ComponentDatabase.prototype.getInstance = function(uidOrName) {
  return this.instances_[uidOrName] || this.instances_[this.instanceNameUid_[uidOrName]];
};

/**
 * Rename a component instance in the ComponentDatabase.
 * @param {!string} uid UUID of the component to be renamed
 * @param {!string} oldName Old name of the component
 * @param {!string} newName New name for the component
 * @returns {boolean} true if the component was successfully renamed, otherwise false.
 */
Blockly.ComponentDatabase.prototype.renameInstance = function(uid, oldName, newName) {
  if (!this.hasInstance(uid)) {
    return false;
  }
  if (oldName === newName) {  // oldName is the same as newName... don't waste time
    return false;
  }
  this.instances_[uid].name = newName;
  delete this.instanceNameUid_[oldName];
  this.instanceNameUid_[newName] = uid;
  return true;
};

/**
 * Remove a component instance in the ComponentDatabase.
 * @param {!string} uid UUID of the component to be removed
 * @returns {boolean} true if the component was removed, otherwise false.
 */
Blockly.ComponentDatabase.prototype.removeInstance = function(uid) {
  if (!this.hasInstance(uid)) {
    return false;
  }
  delete this.instances_[uid];
  return true;
};

/**
 * Iterate over all component instances calling the callback function with the
 * instance and its UUID.
 *
 * @param {function(!ComponentInstanceDescriptor, !string)} callback
 */
Blockly.ComponentDatabase.prototype.forEachInstance = function(callback) {
  goog.object.forEach(this.instances_, callback);
};

/**
 * Check whether the ComponentDatabase has a type identified by typeName.
 *
 * @param {!string} typeName String identifying a component type
 * @returns {boolean} true if the type is known to the ComponentDatabase, otherwise false.
 */
Blockly.ComponentDatabase.prototype.hasType = function(typeName) {
  return typeName in this.types_;
};

/**
 * Get the ComponentTypeDescriptor associated with the given typeName.
 * @param {!string} typeName String identifying a component type.
 * @returns {ComponentTypeDescriptor} The ComponentTypeDescriptor for the type, or undefined if no
 * type is registered with the supplied typeName.
 */
Blockly.ComponentDatabase.prototype.getType = function(typeName) {
  return this.types_[typeName];
};

/**
 * Get the names of the component instances in the database.
 * @returns {Array.<string>} An array of user-provided names for components.
 */
Blockly.ComponentDatabase.prototype.getInstanceNames = function() {
  var instanceNames = [];
  for (var uid in this.instances_) {
    if (this.instances_.hasOwnProperty(uid) && typeof this.instances_[uid] == 'object') {
      instanceNames.push(this.instances_[uid].name);
    }
  }
  return instanceNames;
};

/**
 * Get the name of the type for the given component instance name.
 * @param {!string} instanceName The name of a component instance (e.g., Button1)
 * @returns {string|boolean} The name of the component's type if it exists, otherwise false.
 */
Blockly.ComponentDatabase.prototype.instanceNameToTypeName = function(instanceName) {
  if (instanceName in this.instanceNameUid_) {
    return this.instances_[this.instanceNameUid_[instanceName]].typeName;
  }
  return false;
};

/**
 * Obtain an array of (name, uuid) pairs for displaying components in
 * a dropdown list.
 *
 * @returns {!Array.<!Array<string>>} An array of pairs containing a
 * text value to display for the name of a component and a UUID
 * identifying the component.
 */
// TODO(ewpatton): Profile on larger projects to see if an index by
// type is appropriate
Blockly.ComponentDatabase.prototype.getComponentUidNameMapByType = function(componentType) {
  var componentNameArray = [];
  for (var uid in this.instances_) {
    if (this.instances_.hasOwnProperty(uid) && this.instances_[uid].typeName == componentType) {
      componentNameArray.push([this.instances_[uid].name, uid]);
    }
  }
  return componentNameArray;
};

/**
 * Obtain names of known components for presentation in dropdown fields.
 *
 * @param {!string} componentType The untranslated component type (e.g., button)
 * @returns {Array.<Array.<string>>} An array of 2-tuples containing the name of each component
 *   of the given componentType. If no components are declared, a single element list is returned
 *   with the pair (' ', 'none').
 */
Blockly.ComponentDatabase.prototype.getComponentNamesByType = function(componentType) {
  var componentNameArray = [];
  for (var uid in this.instances_) {
    if (this.instances_.hasOwnProperty(uid) && this.instances_[uid].typeName == componentType) {
      var name = this.instances_[uid].name;
      componentNameArray.push([name, name]);
    }
  }
  if (componentNameArray.length == 0) {
    return [[' ', 'none']]
  } else {
    // Sort the components by name
    componentNameArray.sort(function(a, b) {
      if (a[0] < b[0]) {
        return -1;
      } else if (a[0] > b[0]) {
        return 1;
      } else {
        return 0;
      }
    });
    return componentNameArray;
  }
};

/**
 * Populate the types database.
 *
 * @param {ComponentInfo[]} componentInfos
 */
Blockly.ComponentDatabase.prototype.populateTypes = function(componentInfos) {
  var j, event, method, property;
  for (var i = 0, componentInfo; componentInfo = componentInfos[i]; ++i) {
    var info = this.types_[componentInfo.name] = {
      type: componentInfo.type,
      external: componentInfo.external,
      componentInfo: componentInfo,
      eventDictionary: {},
      methodDictionary: {},
      properties: {},
      setPropertyList: [],
      getPropertyList: []
    };
    // parse type description and fill in all of the fields
    for (j = 0; event = componentInfo.events[j]; ++j) {
      if (typeof event['deprecated'] === 'string') {
        event['deprecated'] = JSON.parse(event['deprecated']);
      }
      if (event['parameters'] === undefined) {
        event['parameters'] = event['params'];
        delete event['params'];
      }
      info.eventDictionary[event.name] = event;
    }
    for (j = 0; method = componentInfo.methods[j]; ++j) {
      if (typeof method['deprecated'] === 'string') {
        method['deprecated'] = JSON.parse(method['deprecated']);
      }
      if (method['parameters'] === undefined) {
        method['parameters'] = method['params'];
        delete method['params'];
      }
      info.methodDictionary[method.name] = method;
    }
    for (j = 0; property = componentInfo.blockProperties[j]; ++j) {
      info.properties[property.name] = property;
      if (typeof property['deprecated'] === 'string') {
        property['deprecated'] = JSON.parse(property['deprecated']);
        if (property['deprecated']) continue;
      }
      if (property['rw'] == 'read-write') {
        property.mutability = Blockly.PROPERTY_READWRITEABLE;
        info.getPropertyList.push(property.name);
        info.setPropertyList.push(property.name);
      } else if (property['rw'] == 'read-only') {
        property.mutability = Blockly.PROPERTY_READABLE;
        info.getPropertyList.push(property.name);
      } else if (property['rw'] == 'write-only') {
        property.mutability = Blockly.PROPERTY_WRITEABLE;
        info.setPropertyList.push(property.name);
      }
    }
    // Copy the designer property information to the block information
    for (j = 0; property = componentInfo.properties[j]; ++j) {
      var target = info.properties[property['name']];
      // All designer properties should have setters, but if not...
      if (!target) continue;
      Object.keys(property).forEach(function(k) {
        target[k] = property[k];
      });
    }
  }
};

/**
 * Populate the tranlsations for components.
 * @param translations
 */
Blockly.ComponentDatabase.prototype.populateTranslations = function(translations) {
  for (var key in translations) {
    if (translations.hasOwnProperty(key)) {
      var parts = key.split('-', 2);
      if (parts[0] == 'COMPONENT') {
        this.i18nComponentTypes_[parts[1]] = translations[key];
      } else if (parts[0] == 'PROPERTY') {
        this.i18nPropertyNames_[parts[1]] = translations[key];
      } else if (parts[0] == 'EVENT') {
        this.i18nEventNames_[parts[1]] = translations[key];
      } else if (parts[0] == 'METHOD') {
        this.i18nMethodNames_[parts[1]] = translations[key];
      } else if (parts[0] == 'PARAM') {
        this.i18nParamNames_[parts[1]] = translations[key];
      }
    }
  }
};

/**
 * Get the event type descriptor for a given type, event pair.
 *
 * @param {!string} typeName
 * @param {!string} eventName
 * @returns {EventDescriptor}
 */
Blockly.ComponentDatabase.prototype.getEventForType = function(typeName, eventName) {
  if (typeName in this.types_) {
    return this.types_[typeName].eventDictionary[eventName];
  }
  return undefined;
};

/**
 * @callback EventIterationCallback
 * @param {!EventDescriptor} eventDesc
 * @param {!string} eventName
 */

/**
 * Iterate over the events declared in typeName calling the provided callback.
 *
 * @param {!string} typeName
 * @param {!EventIterationCallback} callback
 */
Blockly.ComponentDatabase.prototype.forEventInType = function(typeName, callback) {
  if (typeName in this.types_) {
    goog.object.map(this.types_[typeName].eventDictionary, callback);
  }
};

/**
 * Get the method type descriptor for a given type, method pair.
 *
 * @param {!string} typeName
 * @param {!string} methodName
 * @returns {(MethodDescriptor|undefined)}
 */
Blockly.ComponentDatabase.prototype.getMethodForType = function(typeName, methodName) {
  if (typeName in this.types_) {
    return this.types_[typeName].methodDictionary[methodName];
  }
  return undefined;
};

/**
 * @callback MethodIterationCallback
 * @param {!MethodDescriptor} methodDef
 * @param {!string} methodName
 */

/**
 * Iterate over the methods declared in typeName calling the provided callback.
 *
 * @param {!string} typeName
 * @param {!MethodIterationCallback} callback
 */
Blockly.ComponentDatabase.prototype.forMethodInType = function(typeName, callback) {
  if (typeName in this.types_) {
    goog.object.map(this.types_[typeName].methodDictionary, callback);
  }
};

/**
 * Get the property descriptor for a given typeName named by propertyName.
 * @param {!string} typeName String naming a component type
 * @param {!string} propertyName String naming a property defined on typeName
 * @returns {?PropertyDescriptor} The PropertyDescriptor for the property, or null if no such
 * property or type is defined.
 */
Blockly.ComponentDatabase.prototype.getPropertyForType = function(typeName, propertyName) {
  if (this.types_[typeName]) {
    if (this.types_[typeName].properties[propertyName]) {
      return this.types_[typeName].properties[propertyName];
    }
  }
  return null;
};

/**
 * Get a list of setter property names for a type.
 * @param {!string} typeName String naming a component type
 * @returns {?string[]} An array of property names that are writable, otherwise null if the type
 * does not exist.
 */
Blockly.ComponentDatabase.prototype.getSetterNamesForType = function(typeName) {
  if (typeName in this.types_) {
    return this.types_[typeName].setPropertyList;
  }
  return null;
};

/**
 * Get a list of the getter property names for a type.
 * @param {!string} typeName String naming a component type
 * @returns {?string[]} An array of property names that are readable, otherwise null if the type
 * does not exist.
 */
Blockly.ComponentDatabase.prototype.getGetterNamesForType = function(typeName) {
  if (typeName in this.types_) {
    return this.types_[typeName].getPropertyList;
  }
  return null;
};

/**
 * Get the internationalized string for the given component type.
 * @param {!string} name String naming a component type
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedComponentType = function(name) {
  return this.i18nComponentTypes_[name] || name;
};

/**
 * Get the internationalized string for the given event name.
 * @param {!string} name String naming a component event
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedEventName = function(name) {
  return this.i18nEventNames_[name] || name;
};

/**
 * Get the internationalized string for the given method name.
 * @param {!string} name String naming a component method
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedMethodName = function(name) {
  return this.i18nMethodNames_[name] || name;
};

/**
 * Get the internationalized string for the given parameter name.
 * @param {!string} name String naming a component event or method parameter
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedParameterName = function(name) {
  return this.i18nParamNames_[name] || name;
};

/**
 * Get the internationalized string for the given property name.
 * @param {!string} name String naming a component property
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedPropertyName = function(name) {
  return this.i18nPropertyNames_[name] || name;
};
