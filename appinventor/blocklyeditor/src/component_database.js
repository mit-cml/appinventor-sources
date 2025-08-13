/* -*- mode: javascript; js-indent-level 2; -*- */
/**
 * @license
 * Copyright © 2016-2021 Massachusetts Institute of Technology. All rights reserved.
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
 * @typedef HelperKey
 * @type {object}
 * @property {!string} type
 * @property {*} key
 */
HelperKey = function() {};

/**
 * @typedef ParameterDescriptor
 * @type {object}
 * @property {!string} name
 * @property {!type} type
 * @property {HelperKey} helperKey
 */
ParameterDescriptor = function() {};

/**
 * @typedef {{
 *            name: !string,
 *            description: !string,
 *            deprecated: ?boolean,
 *            parameters: !ParameterDescriptor[]
 *          }}
 */
EventDescriptor = function() {};

/**
 * @typedef {{
 *            name: !string,
 *            description: !string,
 *            deprecated: ?boolean,
 *            parameters: !ParameterDescriptor[],
 *            returnType: ?string
 *            returnHelperKey: !HelperKey
 *          }}
 */
MethodDescriptor = function() {};

/**
 * @typedef PropertyDescriptor
 * @type {object}
 * @property {!string} name
 * @property {!string} description
 * @property {!string} type
 * @property {HelperKey} helperKey
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
 * @typedef Option
 * @type {object}
 * @property {!string} name
 * @property {!string} value
 * @property {!string} description
 * @property {?boolean} deprecated
 */
Option = function() {};

/**
 * @typedef OptionList
 * @type {object}
 * @property {!string} className
 * @property {!string} tag
 * @property {!string} defaultOpt
 * @property {!string} underlyingType
 * @property {!Array.<!Option>} options
 */
OptionList = function() {};

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

  /**
   * Maps names of option lists to OptionLists.
   *
   * This map is used to de-duplicate duplicated optionList info provided via
   * the simple_components.json file, so that every option is represented
   * exactly once in the component database. Everything that needs to reference
   * an optionList should do so via the getOptionList function using the option
   * lists's key. This field is used in conjunction with the processOptionList
   * method to achieve this behavior.
   * @type {Object.<string, Option>}
   */
  this.optionLists_ = {};

  /**
   * An array of filters which can be added to connections. This filters will
   * then cause asset blocks attached to the connections to filter their
   * dropdowns.
   * @type {!Array<!Array<string>>}
   */
  this.filters_ = [];

  // Internationalization support
  this.i18nComponentTypes_ = {};
  this.i18nEventNames_ = {};
  this.i18nEventDescriptions_ = {};
  this.i18nMethodNames_ = {};
  this.i18nMethodDescriptions_ = {};
  this.i18nParamNames_ = {};
  this.i18nPropertyNames_ = {};
  this.i18nPropertyDescriptions_ = {};
  this.i18nOptionNames_ = {};
  this.i18nOptionListTags_ = {};
};

/**
 * Regular expression to split a component name into a prefix and suffix where the suffix contains
 * only a numeric value (if any).
 *
 * Examples:
 *
 * "Button1" => ["Button", "1"]
 * "Nonumber" => ["Nonumber"]
 * "Button1Button2" => ["Button1Button", "2"]
 *
 * @type {!RegExp}
 */
Blockly.ComponentDatabase.prototype.SUFFIX_REGEX = new RegExp('^(.*?)([0-9]*)$')

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
  this.instances_[uid] = {uid: uid, name: name, typeName: typeName};
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
 * @returns {{uid: string, typeName: !string, name: !string}|ComponentInstanceDescriptor} An internal descriptor
 * of a component, otherwise undefined.
 */
Blockly.ComponentDatabase.prototype.getInstance = function(uidOrName) {
  return this.instances_[uidOrName] || this.instances_[this.instanceNameUid_[uidOrName]];
};

/**
 * Get the container record for the instance by UUID or Name.
 *
 * @param {!string} formName
 * @param {!string} uidOrName
 * @returns {?{uid: string, typeName: !string, name: !string}}
 */
Blockly.ComponentDatabase.prototype.getContainer = function(formName, uidOrName) {
  var component = this.getInstance(uidOrName);
  var containerUuid = top.BlocklyPanel_getComponentContainerUuid(component.name);
  return this.getInstance(containerUuid);
}

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
      var match = name.match(this.SUFFIX_REGEX) || [name, '0'];
      componentNameArray.push([name, name, match[1], parseInt(match[2] || '0', 10)]);
    }
  }
  if (componentNameArray.length == 0) {
    return [[' ', 'none']]
  } else {
    // Sort the components by name
    componentNameArray.sort(function(a, b) {
      if (a[2] === b[2]) {
        return a[3] - b[3];
      } else if (a[0] < b[0]) {
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
 * Obtain type names of added components for presentation in a drop-down field.
 *
 * @returns {Array.<Array.<string>>} An array of 2-tuples containing the type of each component.
 *   If no components are declared, a single element list is returned with the pair
 *   (' ', 'none').
 */
Blockly.ComponentDatabase.prototype.getComponentTypes = function() {
  var componentTypeArray = [];
  for (var uid in this.instances_) {
    var typeName = this.instances_[uid].typeName;
    if (typeName != "Form")
      componentTypeArray.push([this.i18nComponentTypes_[typeName], typeName]);
  }

  goog.array.removeDuplicates(componentTypeArray, null, function(type) {
    return type[1];
  });

  if (componentTypeArray.length == 0) {
    return [[' ', 'none']]
  } else {
    // Sort the components by type
    componentTypeArray.sort(function(a, b) {
      if (a[0] < b[0]) {
        return -1;
      } else if (a[0] > b[0]) {
        return 1;
      } else {
        return 0;
      }
    });
    return componentTypeArray;
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
        event['parameters'] = this.processParameters(event['params']);
        delete event['params'];
      }
      info.eventDictionary[event.name] = event;
    }
    for (j = 0; method = componentInfo.methods[j]; ++j) {
      if (typeof method['deprecated'] === 'string') {
        method['deprecated'] = JSON.parse(method['deprecated']);
      }
      if (method['parameters'] === undefined) {
        method['parameters'] = this.processParameters(method['params']);
        delete method['params'];
      }
      if (method['helper']) {
        method['returnHelperKey'] = this.processHelper(method['helper']);
      }
      info.methodDictionary[method.name] = method;
    }
    for (j = 0; property = componentInfo.blockProperties[j]; ++j) {
      info.properties[property.name] = property;
      if (typeof property['deprecated'] === 'string') {
        property['deprecated'] = JSON.parse(property['deprecated']);
      }
      if (property['helper']) {
        property['helperKey'] = this.processHelper(property['helper']);
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
 * Processes the given array of parameters (from simple_components.json, not
 * Parameters) and returns an array of Parameters.
 * @param {!Array.<!Object>} paramData An array of data from
 *     simple_components.json defining the parameters.
 * @return {!Array.<!Parameter>} An array of parameters.
 */
Blockly.ComponentDatabase.prototype.processParameters = function(paramData) {
  params = [];
  for (var i = 0, datum; datum = paramData[i]; i++) {
    var param = {};
    param.name = datum.name;
    param.type = datum.type;
    param.helperKey = this.processHelper(datum.helper);
    params.push(param);
  }
  return params;
}

/**
 * Processes a helper (from simple_components.json) and returns a HelperKey if
 * possible.
 * @param {Object} helper The (possibly null) helper definition.
 * @return {HelperKey} The helper key associated with the helper (if it is
 *     possible) to create one.
 */
Blockly.ComponentDatabase.prototype.processHelper = function(helper) {
  if (!helper) {
    return null;
  }
  switch (helper.type) {
    case "OPTION_LIST":
      return this.processOptionList(helper.data);
    case "ASSET":
      return this.processAssetHelper(helper.data);
    case "PROVIDER_MODEL":
      return this.processProviderModelHelper(helper.data);
    case "PROVIDER":
      return this.processProviderHelper(helper.data);
  }
  return null;
}

/**
 * Processes data defining an OptionList (from simple_components.json) and
 * returns a HelperKey associated with the OptionList.
 *
 * This function is used in conjunction with the optionLists_ field to remove
 * duplicate optionList data provided via the simple_components.json file.
 * @param {!Object} data The data defining the OptionList.
 * @return {!HelperKey} The key associated with the OptionList.
 */
Blockly.ComponentDatabase.prototype.processOptionList = function(data) {
  // The first time an optionList is encountered its data is inserted into the
  // optionLists_ map and its data is cached. All future calls to
  // processOptionList do not process the option list, and just return
  // a HelperKey which can be used to retrieve information from this map.
  if (!this.optionLists_[data.key]) {
    var options = [];
    for (var i = 0, option; option = data.options[i]; i++) {
      options[i] = this.processOption(option);
    }

    this.optionLists_[data.key] = {
      className: data.className,
      tag: data.tag,
      defaultOpt: data.defaultOpt,
      underlyingType: data.underlyingType,
      options: options
    };
  }
  return {
    type: "OPTION_LIST",
    key: data.key
  };
}

Blockly.ComponentDatabase.prototype.processOption = function(option) {
  return {
    name: option.name,
    value: option.value,
    description: option.description,
    deprecated: option.deprecated == "true"
  };
}

/**
 * Processes data defining an asset filter (from simple_components.json) and
 * returns a HelperKey pointing to the filter.
 * @param {!Object} data The data defining the filter.
 * @return {!HelperKey} The key associated with the filter.
 */
Blockly.ComponentDatabase.prototype.processAssetHelper = function(data) {
  var filter = data.filter;
  if (!filter || filter.length == 0) {
    return {
      type: "ASSET",
      key: null
    }
  }
  // We have to do this because js is very restrictive with array equality.
  function findIndex(acc, cur, idx) {
    if (acc != -1) {
      return acc;
    }
    var matches = cur.every(function(string) {
      return filter.includes(string);
    });
    if (matches) {
      return idx;
    }
    return -1;
  }

  var index = this.filters_.reduce(findIndex, -1);
  if (index == -1) {
    // TODO: If filters_ was instead a sorted list we could optimize index
    //   finding algorithm using a binary search.
    this.filters_.push(filter);
    index = this.filters_.length - 1;
  }
  return {
    type: "ASSET",
    key: index
  }
}

/**
 * Processes data defining an provider model (from simple_components.json) and
 * returns a HelperKey pointing to the filter.
 * @param {!Object} data The data defining the filter.
 * @return {!HelperKey} The key associated with the filter.
 */
Blockly.ComponentDatabase.prototype.processProviderModelHelper = function(data) {
  var filter = data.filter;
  if (!filter || filter.length == 0) {
    return {
      type: "PROVIDER_MODEL",
      key: null
    }
  }
  // We have to do this because js is very restrictive with array equality.
  function findIndex(acc, cur, idx) {
    if (acc != -1) {
      return acc;
    }
    var matches = cur.every(function(string) {
      return filter.includes(string);
    });
    if (matches) {
      return idx;
    }
    return -1;
  }
}

/**
 * Processes data defining an provider (from simple_components.json) and
 * returns a HelperKey pointing to the filter.
 * @param {!Object} data The data defining the filter.
 * @return {!HelperKey} The key associated with the filter.
 */
Blockly.ComponentDatabase.prototype.processProviderHelper = function(data) {
  var filter = data.filter;
  if (!filter || filter.length == 0) {
    return {
      type: "PROVIDER",
      key: null
    }
  }
  // We have to do this because js is very restrictive with array equality.
  function findIndex(acc, cur, idx) {
    if (acc != -1) {
      return acc;
    }
    var matches = cur.every(function(string) {
      return filter.includes(string);
    });
    if (matches) {
      return idx;
    }
    return -1;
  }

  var index = this.filters_.reduce(findIndex, -1);
  if (index == -1) {
    // TODO: If filters_ was instead a sorted list we could optimize index
    //   finding algorithm using a binary search.
    this.filters_.push(filter);
    index = this.filters_.length - 1;
  }
  return {
    type: "PROVIDER",
    key: index
  }
}

Blockly.ComponentDatabase.PROPDESC = /PropertyDescriptions$/;
Blockly.ComponentDatabase.METHODDESC = /MethodDescrptions$/;
Blockly.ComponentDatabase.EVENTDESC = /EventDescriptions$/;

/**
 * Populate the tranlsations for components.
 * @param translations
 */
Blockly.ComponentDatabase.prototype.populateTranslations = function(translations) {
  var newkey;
  for (var key in translations) {
    if (!translations.hasOwnProperty(key)) {
      continue;
    }
    var parts = key.split('-', 2);
    var type = parts[0];
    var jsKey = parts[1];
    var translation = translations[key];
    switch(type) {
      case 'COMPONENT':
        this.i18nComponentTypes_[jsKey] = translation;
        break;
      case 'PROPERTY':
        this.i18nPropertyNames_[jsKey] = translation;
        break;
      case 'EVENT':
        this.i18nEventNames_[jsKey] = translation;
        break;
      case 'METHOD':
        this.i18nMethodNames_[jsKey] = translation;
        break;
      case 'PARAM':
        this.i18nParamNames_[jsKey] = translation;
        break;
      case 'EVENTDESC':
        this.i18nEventDescriptions_[jsKey] = translation;
        break;
      case 'METHODDESC':
        this.i18nMethodDescriptions_[jsKey] = translation;
        break;
      case 'PROPDESC':
        this.i18nPropertyDescriptions_[jsKey] = translation;
        break;
      case 'OPTION':
        this.i18nOptionNames_[jsKey] = translation;
        break;
      case 'OPTIONLIST':
        this.i18nOptionListTags_[jsKey] = translation;
        break;
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
    var filterDeprecated = goog.object.filter(this.types_[typeName].eventDictionary, function(event) { return !event.deprecated; });
    goog.object.map(filterDeprecated, callback);
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
    var filterDeprecated = goog.object.filter(this.types_[typeName].methodDictionary, function(method) { return !method.deprecated; });
    goog.object.map(filterDeprecated, callback);
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
 * Returns the OptionList associated with the given key.
 * @param {!string} key The dictionary key for the OptionList.
 * @return {OptionList} The associated option list, or undefined if one is not
 *     found.
 */
Blockly.ComponentDatabase.prototype.getOptionList = function(key) {
  return this.optionLists_[key];
}

/**
 * Iterate over all option list definitions calling the callback function with
 * the OptionList
 *
 * @param {function(!OptionList)} callback
 */
Blockly.ComponentDatabase.prototype.forEachOptionList = function(callback) {
  goog.object.forEach(this.optionLists_, callback);
}

/**
 * Returns the asset filter associated with the given key, or undefined.
 * @param {number} key The key associated with a given filter.
 * @return {Array<string>=}
 */
Blockly.ComponentDatabase.prototype.getFilter = function(key) {
  return this.filters_[key];
}

/**
 * Get the internationalized string for the given component type.
 * @param {!string} name String naming a component type
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedComponentType = function(name, opt_default) {
  return this.i18nComponentTypes_[name] || opt_default || name;
};

/**
 * Get the internationalized string for the given event name.
 * @param {!string} name String naming a component event
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedEventName = function(name, opt_default) {
  return this.i18nEventNames_[name] || opt_default || name;
};

/**
 * Get the internationalized string for the given event description tooltip.
 * @param {!string} name String naming a component event
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedEventDescription = function(component, name, opt_default) {
  return this.i18nEventDescriptions_[component + '.' + name + 'EventDescriptions'] || this.i18nEventDescriptions_[name + 'EventDescriptions'] || opt_default || name;
};

/**
 * Get the internationalized string for the given method name.
 * @param {!string} name String naming a component method
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedMethodName = function(name, opt_default) {
  return this.i18nMethodNames_[name] || opt_default || name;
};

/**
 * Get the internationalized string for the given method name.
 * @param {!string} name String naming a component method
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedMethodDescription = function(component, name, opt_default) {
  return this.i18nMethodDescriptions_[component + '.' + name + 'MethodDescriptions'] || this.i18nMethodDescriptions_[name + 'MethodDescriptions'] || opt_default || name;
};

/**
 * Get the internationalized string for the given parameter name.
 * @param {!string} name String naming a component event or method parameter
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedParameterName = function(name, opt_default) {
  return this.i18nParamNames_[name] || opt_default || name;
};

/**
 * Get the internationalized string for the given property name.
 * @param {!string} name String naming a component property
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedPropertyName = function(name, opt_default) {
  return this.i18nPropertyNames_[name] || opt_default || name;
};

/**
 * Get the internationalized string for the given property description tooltip.
 * @param {!string} name String naming a component property
 * @param {?string=name} opt_default Optional default value (default: name parameter)
 * @returns {string} The localized string if available, otherwise the unlocalized name.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedPropertyDescription = function(component, name, opt_default) {
  return this.i18nPropertyDescriptions_[component + '.' + name + 'PropertyDescriptions'] || this.i18nPropertyDescriptions_[name + 'PropertyDescriptions'] || opt_default || name;
};

/**
 * Returns the internationalized string for the given option name.
 * @param {string} key The tag name of the option list + the name of the option.
 *     Used to get the internationalized name.
 * @param {string} opt_default The default name if an internationalized one is
 *     not found.
 * @return {string} The localized string if available, otherwise the unlocalized
 *     one.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedOptionName =
  function(key, opt_default) {
    return this.i18nOptionNames_[key] || opt_default;
  };

/**
 * Returns the internationalized string for the given option list tag.
 * @param {string} name The name of the tag used to get the internationlized
 *     version.
 * @param {string} opt_default The default name if the internationalized name
 *     is not available.
 * @return {string} The localized string if available, otherwise the unlocalized
 *     string.
 */
Blockly.ComponentDatabase.prototype.getInternationalizedOptionListTag =
  function(name, opt_default) {
    return this.i18nOptionListTags_[name] || opt_default || name;
  }
