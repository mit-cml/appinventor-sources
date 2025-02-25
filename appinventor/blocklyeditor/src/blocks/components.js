// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Component blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */

'use strict';

goog.provide('AI.Blocks.components');
goog.require('AI.Blockly.FieldEventFlydown');
goog.require('AI.Blockly.FieldNoCheckDropdown');
goog.require('AI.BlockUtils');

Blockly.ComponentBlock = {};

/*
 * All component blocks have category=='Component'. In addition to the standard blocks fields,
 * All regular component blocks have a field instanceName whose value is the name of their
 * component. For example, the blocks representing a Button1.Click event has
 * instanceName=='Button1'. All generic component blocks have a field typeName whose value is
 * the name of their component type.
 */

/**
 * Block Colors Hues (See blockly.js for Saturation and Value - fixed for
 * all blocks)
 */
Blockly.ComponentBlock.COLOUR_EVENT = Blockly.CONTROL_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_METHOD = Blockly.PROCEDURE_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_GET = '#439970';  // [67, 153, 112]
Blockly.ComponentBlock.COLOUR_SET = '#266643';  // [38, 102, 67]
Blockly.ComponentBlock.COLOUR_COMPONENT = '#439970';  // [67, 153, 112]

Blockly.ComponentBlock.COMPONENT_SELECTOR = "COMPONENT_SELECTOR";
Blockly.ComponentBlock.COMPONENT_TYPE_SELECTOR = "COMPONENT_TYPE_SELECTOR";

/**
 * Add a menu option to the context menu for {@code block} to swap between
 * the generic and specific versions of the block.
 *
 * @param {Blockly.BlockSvg} block the block to manipulate
 * @param {Array.<{enabled,text,callback}>} options the menu options
 */
Blockly.ComponentBlock.addGenericOption = function(block, options) {
  if ((block.type === 'component_event' && block.isGeneric) || block.typeName === 'Form') {
    return;  // Cannot make a generic component_event specific for now...
  }
  if (block.workspace && block.workspace.isFlyout) {
    return;  // Flyouts are not mutable
  }

  /**
   * Helper function used to make component blocks generic.
   *
   * @param {Blockly.BlockSvg} block  the block to be made generic
   * @param {(!Element|boolean)=} opt_replacementDom  DOM tree for a replacement block to use in the
   * substitution, false if no substitution should be made, or undefined if the substitution should
   * be inferred.
   */
  function makeGeneric(block, opt_replacementDom) {
    var instanceName = block.instanceName;
    var mutation = block.mutationToDom();
    var oldMutation = Blockly.Xml.domToText(mutation);
    mutation.setAttribute('is_generic', 'true');
    mutation.removeAttribute('instance_name');
    var newMutation = Blockly.Xml.domToText(mutation);
    block.domToMutation(mutation);
    block.initSvg();  // block shape may have changed
    block.render();
    if (Blockly.Events.isEnabled()) {
      Blockly.Events.fire(new Blockly.Events.BlockChange(
        block, 'mutation', null, oldMutation, newMutation));
    }
    if (block.type === 'component_event') opt_replacementDom = false;
    if (opt_replacementDom !== false) {
      if (opt_replacementDom === undefined) {
        var compBlockXml = '<xml><block type="component_component_block">' +
          '<mutation component_type="' + block.typeName + '" instance_name="' + instanceName + '"></mutation>' +
          '<field name="COMPONENT_SELECTOR">' + instanceName + '</field>' +
          '</block></xml>';
        opt_replacementDom = Blockly.utils.xml.textToDom(compBlockXml).firstElementChild;
      }
      var replacement = Blockly.Xml.domToBlock(opt_replacementDom, block.workspace);
      replacement.initSvg();
      block.getInput('COMPONENT').connection.connect(replacement.outputConnection);
    }
    if (Blockly.Events.isEnabled()) {
      var group = Blockly.Events.getGroup();
      setTimeout(function () {
        Blockly.Events.setGroup(group);
        // noinspection JSAccessibilityCheck
        block.bumpNeighbours();
        Blockly.Events.setGroup(false);
      }, Blockly.BUMP_DELAY);
    }
  }

  var item = { enabled: false };
  if (block.isGeneric) {
    var compBlock = block.getInputTargetBlock('COMPONENT');
    item.enabled = compBlock && compBlock.type === 'component_component_block';
    item.text = Blockly.BlocklyEditor.makeMenuItemWithHelp(Blockly.Msg.UNGENERICIZE_BLOCK,
      '/reference/other/any-component-blocks.html');
    item.callback = function () {
      try {
        Blockly.Events.setGroup(true);
        var instanceName = compBlock.instanceName;
        compBlock.dispose(true);
        var mutation = block.mutationToDom();
        var oldMutation = Blockly.Xml.domToText(mutation);
        mutation.setAttribute('instance_name', instanceName);
        mutation.setAttribute('is_generic', 'false');
        var newMutation = Blockly.Xml.domToText(mutation);
        block.domToMutation(mutation);
        block.initSvg();  // block shape may have changed
        block.render();
        if (Blockly.Events.isEnabled()) {
          Blockly.Events.fire(new Blockly.Events.BlockChange(
            block, 'mutation', null, oldMutation, newMutation));
          var group = Blockly.Events.getGroup();
          setTimeout(function () {
            Blockly.Events.setGroup(group);
            // noinspection JSAccessibilityCheck
            block.bumpNeighbours();
            Blockly.Events.setGroup(false);
          }, Blockly.BUMP_DELAY);
        }
      } finally {
        Blockly.Events.setGroup(false);
      }
    };
  } else if (block.type === 'component_event') {
    item.enabled = true;
    item.text = Blockly.BlocklyEditor.makeMenuItemWithHelp(Blockly.Msg.GENERICIZE_BLOCK,
      '/reference/other/any-component-blocks.html');
    item.callback = function() {
      try {
        Blockly.Events.setGroup(true);
        var instanceName = block.instanceName;
        var intlName = block.workspace.getComponentDatabase()
          .getInternationalizedParameterName('component');

        // Aggregate variables in scope
        var namesInScope = {}, maxNum = 0;
        var regex = new RegExp('^' + intlName + '([0-9]+)$');
        var varDeclsWithIntlName = [];
        block.walk(function(block) {
          if (block.type === 'local_declaration_statement' ||
              block.type === 'local_declaration_expression') {
            var localNames = block.getVars();
            localNames.forEach(function(varname) {
              namesInScope[varname] = true;
              var match = varname.match(regex);
              if (match) {
                maxNum = Math.max(maxNum, parseInt(match[1]));
              }
              if (varname === intlName) {
                varDeclsWithIntlName.push(block);
              }
            });
          }
        });

        // Rename local variable definition of i18n(component) to prevent
        // variable capture
        if (intlName in namesInScope) {
          varDeclsWithIntlName.forEach(function(block) {
            Blockly.LexicalVariable.renameParamFromTo(block, intlName, intlName + (maxNum + 1).toString(), true);
          });
        }

        // Make generic the block and any descendants of the same component instance
        var varBlockXml = '<xml><block type="lexical_variable_get">' +
          '<mutation><eventparam name="component"></eventparam></mutation>' +
          '<field name="VAR">' + intlName + '</field></block></xml>';
        var varBlockDom = Blockly.utils.xml.textToDom(varBlockXml).firstElementChild;
        makeGeneric(block);  // Do this first so 'component' is defined.
        block.walk(function(block) {
            if ((block.type === 'component_method' || block.type === 'component_set_get') &&
              block.instanceName === instanceName) {
            makeGeneric(/** @type {Blockly.BlockSvg} */ (block), varBlockDom);
          }
        });
      } finally {
        Blockly.Events.setGroup(false);
      }
    };
  } else {
    item.enabled = true;
    item.text = Blockly.BlocklyEditor.makeMenuItemWithHelp(Blockly.Msg.GENERICIZE_BLOCK,
      '/reference/other/any-component-blocks.html');
    item.callback = function() {
      try {
        Blockly.Events.setGroup(true);
        makeGeneric(block);
      } finally {
        Blockly.Events.setGroup(false);
      }
    };
  }
  options.splice(options.length - 1, 0, item);
};

/**
 * Marks the passed block as a badBlock() and disables it if the data associated
 * with the block is not defined, or the data is marked as deprecated.
 * @param {Blockly.BlockSvg} block The block to check for deprecation.
 * @param {EventDescriptor|MethodDescriptor|PropertyDescriptor} data The data
 *     associated with the block which is possibly deprecated.
 */
Blockly.ComponentBlock.checkDeprecated = function(block, data) {
  if (data && data.deprecated && block.workspace == Blockly.common.getMainWorkspace()) {
    block.setEnabled(false);
  }
}

/**
 * Create an event block of the given type for a component with the given
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 * @lends {Blockly.BlockSvg}
 * @lends {Blockly.Block}
 */
Blockly.Blocks.component_event = {
  category : 'Component',
  blockType : 'event',

  init: function() {
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    this.lexicalVarPrefix = Blockly.localNamePrefix;
  },

  mutationToDom : function() {

    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    container.setAttribute('is_generic', this.isGeneric ? "true" : "false");
    if (!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);//instance name not needed
    }

    container.setAttribute('event_name', this.eventName);
    if (!this.horizontalParameters) {
      container.setAttribute('vertical_parameters', "true"); // Only store an element for vertical
                                                             // The absence of this attribute means horizontal.
    }

    // Note that this.parameterNames only contains parameter names that have
    // overridden the default event parameter names specified in the component
    // DB
    for (var i = 0; i < this.parameterNames.length; i++) {
      container.setAttribute('param_name' + i, this.parameterNames[i]);
    }

    return container;
  },

  domToMutation : function(xmlElement) {
    // The preexisting component dropdown cannot be reused since it might already been
    // used here due to a previous call to mutationToDom. Reusing the dropdown is not
    // allowed by Blockly, i.e. its sourceBlock is not allowed to be changed.
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    var oldRendered = this.rendered;
    this.rendered = false;
    var oldDo = null;
    for (var i = 0, input; input = this.inputList[i]; i++) {
      if (input.connection) {
        if (input.name === 'DO') {
          oldDo = input.connection.targetBlock();
        }
        var block = input.connection.targetBlock();
        if (block) {
          block.unplug();
        }
      }
      input.dispose();
    }
    this.inputList.length = 0;

    this.typeName = xmlElement.getAttribute('component_type');
    this.eventName = xmlElement.getAttribute('event_name');
    this.isGeneric = xmlElement.getAttribute('is_generic') == 'true';
    if (!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    } else {
      delete this.instanceName;
    }

    // this.parameterNames will be set to a list of names that will override the
    // default names specified in the component DB. Note that some parameter
    // names may be overridden while others may remain their defaults
    this.parameterNames = [];
    var numParams = this.getDefaultParameters_().length
    for (var i = 0; i < numParams; i++) {
      var paramName = xmlElement.getAttribute('param_name' + i);
      // For now, we only allow explicit parameter names starting at the beginning
      // of the parameter list.  Some day we may allow an arbitrary subset of the
      // event params to be explicitly specified.
      if (!paramName) break;
      this.parameterNames.push(paramName);
    }

    // Orient parameters horizontally by default
    var horizParams = xmlElement.getAttribute('vertical_parameters') !== "true";

    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);

    var localizedEventName;
    var eventType = this.getEventTypeObject();
    var componentDb = this.getTopWorkspace().getComponentDatabase();
    if (eventType) {
      localizedEventName = componentDb.getInternationalizedEventName(eventType.name);
    }
    else {
      localizedEventName = componentDb.getInternationalizedEventName(this.eventName);
    }

    if (!this.isGeneric) {
      this.appendDummyInput('WHENTITLE').appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN)
        .appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR)
        .appendField('.' + localizedEventName);
      this.componentDropDown.setValue(this.instanceName);
    } else {
      this.appendDummyInput('WHENTITLE').appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_EVENT_TITLE
        + componentDb.getInternationalizedComponentType(this.typeName) + '.' + localizedEventName);
    }
    this.setParameterOrientation(horizParams);
    var tooltipDescription;
    if (eventType) {
      tooltipDescription = componentDb.getInternationalizedEventDescription(this.getTypeName(), eventType.name,
          eventType.description);
    }
    else {
      tooltipDescription = componentDb.getInternationalizedEventDescription(this.getTypeName(), this.eventName);
    }
    this.setTooltip(tooltipDescription);
    this.setPreviousStatement(false, null);
    this.setNextStatement(false, null);

    if (oldDo) {
      this.getInput('DO').connection.connect(oldDo.previousConnection);
    }

    for (var i = 0, input; input = this.inputList[i]; i++) {
      input.init();
    }

    // Set as badBlock if it doesn't exist.
    this.verify();
    // Disable it if it does exist and is deprecated.
    Blockly.ComponentBlock.checkDeprecated(this, eventType);

    this.rendered = oldRendered;
  },

  getTypeName: function() {
    return this.typeName === 'Form' ? 'Screen' : this.typeName;
  },
  // [lyn, 10/24/13] Allow switching between horizontal and vertical display of arguments
  // Also must create flydown params and DO input if they don't exist.

  // TODO: consider using top.BlocklyPanel... instead of window.parent.BlocklyPanel

  setParameterOrientation: function(isHorizontal) {
    var params = this.getParameters();
    if (!params)  {
      params = [];
    }
    var componentDb = this.getTopWorkspace().getComponentDatabase();
    var oldDoInput = this.getInput("DO");
    if (!oldDoInput || (isHorizontal !== this.horizontalParameters && params.length > 0)) {
      this.horizontalParameters = isHorizontal;

      var bodyConnection = null, i, param, newDoInput;
      if (oldDoInput) {
        bodyConnection = oldDoInput.connection.targetConnection; // Remember any body connection
      }
      if (this.horizontalParameters) { // Replace vertical by horizontal parameters

        if (oldDoInput) {
          // Remove inputs after title ...
          for (i = 0; i < params.length; i++) {
            this.removeInput('VAR' + i); // vertical parameters
          }
          this.removeInput('DO');
        }

        // ... and insert new ones:
        if (params.length > 0) {
          var paramInput = this.appendDummyInput('PARAMETERS')
                               .appendField(" ")
                               .setAlign(Blockly.inputs.Align.LEFT);
          for (i = 0; param = params[i]; i++) {
            var field = new Blockly.FieldEventFlydown(
                param, componentDb, Blockly.FieldFlydown.DISPLAY_BELOW);
            paramInput.appendField(field, 'VAR' + i)
                .appendField(" ");
          }
        }

        newDoInput = this.appendStatementInput("DO")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO); // Hey, I like your new do!
        if (bodyConnection) {
          newDoInput.connection.connect(bodyConnection);
        }

      } else { // Replace horizontal by vertical parameters

        if (oldDoInput) {
          // Remove inputs after title ...
          this.removeInput('PARAMETERS'); // horizontal parameters
          this.removeInput('DO');
        }

        // ... and insert new ones:

        // Vertically aligned parameters
        for (i = 0; param = params[i]; i++) {
          var field = new Blockly.FieldEventFlydown(param, componentDb);
          this.appendDummyInput('VAR' + i)
              .appendField(field, 'VAR' + i)
              .setAlign(Blockly.inputs.Align.RIGHT);
        }
        newDoInput = this.appendStatementInput("DO")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO);
        if (bodyConnection) {
          newDoInput.connection.connect(bodyConnection);
        }
      }
      if (Blockly.Events.isEnabled()) {
        Blockly.Events.fire(new Blockly.Events.BlockChange(
            this, 'parameter_orientation', null, !this.horizontalParameters, this.horizontalParameters));
      }
    }
  },
  // Return a list of parameter names
  getParameters: function () {
    /** @type {EventDescriptor} */
    var defaultParameters = this.getDefaultParameters_();
    var explicitParameterNames = this.getExplicitParameterNames_();
    var params = [];
    for (var i = 0; i < defaultParameters.length; i++) {
      var paramName = explicitParameterNames[i] || defaultParameters[i].name;
      params.push({name: paramName, type: defaultParameters[i].type});
    }
    return params;
  },
  getDefaultParameters_: function () {
    var eventType = this.getEventTypeObject();
    if (this.isGeneric) {
      return [
          {name:'component', type:'component'},
          {name:'notAlreadyHandled', type: 'boolean'}
        ].concat((eventType && eventType.parameters) || []);
    }
    return (eventType && eventType.parameters) || [];
  },
  getExplicitParameterNames_: function () {
    return this.parameterNames;
  },
  // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      this.componentDropDown.setValue(this.instanceName);
      return true;
    }
    return false;
  },
  renameVar: function(oldName, newName) {
    for (var i = 0, param = 'VAR' + i, input
        ; input = this.getFieldValue(param)
        ; i++, param = 'VAR' + i) {
      if (Blockly.Names.equals(oldName, input)) {
        this.setFieldValue(param, newName);
      }
    }
  },
  helpUrl : function() {
    var url = Blockly.ComponentBlock.EVENTS_HELPURLS[this.getTypeName()];
    if (url && url[0] == '/') {
      var parts = url.split('#');
      parts[1] = this.getTypeName() + '.' + this.eventName;
      url = parts.join('#');
    }
    return url;
  },

  getVars: function() {
    var varList = [];
    for (var i = 0, input; input = this.getFieldValue('VAR' + i); i++) {
      varList.push(input);
    }
    return varList;
  },

  getVarString: function() {
    var varString = "";
    for (var i = 0, param; param = this.getFieldValue('VAR' + i); i++) {
      // [lyn, 10/13/13] get current name from block, not from underlying event (may have changed)
      if(i != 0){
        varString += " ";
      }
      varString += param;
    }
    return varString;
  },

  declaredNames: function() { // [lyn, 10/13/13] Interface with Blockly.LexicalVariable.renameParam
    var names = [];
    for (var i = 0, param; param = this.getField('VAR' + i); i++) {
      names.push(param.getValue());
      if (param.eventparam && param.eventparam != param.getValue()) {
        names.push(param.eventparam);
      }
    }
    return names;
  },

  declaredVariables: function() {
    var names = [];
    for (var i = 0, param; param = this.getField('VAR' + i); i++) {
      names.push(param.getValue());
    }
    return names;
  },

  withLexicalVarsAndPrefix: function(_, proc) {
    const params = this.getParameters().map(function(param) {
      return param.name;
    });
    // not arguments_ instance var
    for (let i = 0; i < params.length; i++) {
      proc(params[i], this.lexicalVarPrefix, this.workspace.getComponentDatabase().getInternationalizedParameterName(params[i]));
    }
  },

  blocksInScope: function() { // [lyn, 10/13/13] Interface with Blockly.LexicalVariable.renameParam
    var doBlock = this.getInputTargetBlock('DO');
    if (doBlock) {
      return [doBlock];
    } else {
      return [];
    }
  },

  /**
   * Get the underlying event descriptor for the block.
   * @returns {EventDescriptor}
   */
  getEventTypeObject : function() {
    return this.getTopWorkspace().getComponentDatabase().getEventForType(this.typeName, this.eventName);
  },

  typeblock : function(){
    var componentDb = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];
    var types = {};

    componentDb.forEachInstance(function(instance) {
      types[instance.typeName] = true;
      componentDb.forEventInType(instance.typeName, function(_, eventName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN + instance.name + '.' +
            componentDb.getInternationalizedEventName(eventName),
          mutatorAttributes: {
            component_type: instance.typeName,
            instance_name: instance.name,
            event_name: eventName
          }
        });
      });
    });

    delete types['Form'];

    Object.keys(types).forEach(function(typeName) {
      componentDb.forEventInType(typeName, function(_, eventName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_EVENT_TITLE +
            componentDb.getInternationalizedComponentType(typeName) +  '.' +
            componentDb.getInternationalizedEventName(eventName),
          mutatorAttributes: {
            component_type: typeName,
            is_generic: true,
            event_name: eventName
          }
        });
      });
    });

    return tb;
  },
  customContextMenu: function (options) {
    // Remove "do it" option
    const doiItIndex = options.findIndex(function(option) {
      return option.text === Blockly.Msg['DO_IT']
    });
    if (doiItIndex > -1) {
      options.splice(doiItIndex, 1);
    }

    if (this.workspace && this.workspace.isFlyout) {
      return;  // Flyouts are not mutable
    }
    Blockly.FieldParameterFlydown.addHorizontalVerticalOption(this, options);
    Blockly.ComponentBlock.addGenericOption(this, options);
   },

  // check if the block corresponds to an event inside componentTypes[typeName].eventDictionary
  verify : function () {

    var validate = function() {
      // check component type
      var componentDb = this.getTopWorkspace().getComponentDatabase();
      var componentType = componentDb.getType(this.typeName);
      if (!componentType) {
        return false; // component does NOT exist! should not happen!
      }
      var eventDictionary = componentType.eventDictionary;
      /** @type {EventDescriptor} */
      var event = eventDictionary[this.eventName];
      // check event name
      if (!event) {
        return false; // no such event : this event was for another version!  block is undefined!
      }
      // check parameters
      var varList = this.getVars();
      var params = event.parameters;
      if (this.isGeneric) {
        varList.splice(0, 2);  // remove component and wasDefined parameters
                               // since we know they are well-defined
      }
      if (varList.length != params.length) {
        return false; // parameters have changed
      }
      if ("true" === componentType.external) {
        for (var x = 0; x < varList.length; ++x) {
          var found = false;
          for (var i = 0, param; param = params[i]; ++i) {
            if (componentDb.getInternationalizedParameterName(param.name) == varList[x]) {
              found = true;
              break;
            }
          }
          if (!found)  {
            return false; // parameter name changed
          }
        }
      }
      // No need to check event return type, events do not return.
      return true; // passed all our tests! block is defined!
    };
    var isDefined = validate.call(this);

    if (isDefined) {
      this.notBadBlock();
    } else {
      this.badBlock();
    }

  },

  // [lyn, 12/31/2013] Next two fields used to check for duplicate component event handlers
  errors: [{name:"checkIfUndefinedBlock"},{name:"checkIfIAmADuplicateEventHandler"}, {name:"checkComponentNotExistsError"}],
  onchange: function(e) {
    if (e.isTransient) {
      return false;  // don't trigger error check on transient actions.
    }
    return this.workspace.getWarningHandler() && this.workspace.getWarningHandler().checkErrors(this);
  }
};

/**
 * Create a method block of the given type for a component with the given instance name. methodType
 * is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 * @lends {Blockly.BlockSvg}
 * @lends {Blockly.Block}
 */
Blockly.Blocks.component_method = {
  category : 'Component',
  helpUrl : function() {
      var url = Blockly.ComponentBlock.METHODS_HELPURLS[this.getTypeName()];
      if (url && url[0] == '/') {
        var parts = url.split('#');
        parts[1] = this.getTypeName() + '.' + this.methodName;
        url = parts.join('#');
      }
      return url;
  },

  mutationToDom : function() {

    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    container.setAttribute('method_name', this.methodName);
    var isGenericString = "false";
    if(this.isGeneric){
      isGenericString = "true";
    }
    container.setAttribute('is_generic', isGenericString);
    if(!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);//instance name not needed
    }
    if (!this.isGeneric && this.typeName == "Clock" &&
        Blockly.ComponentBlock.isClockMethodName(this.methodName)) {
      var timeUnit = this.getFieldValue('TIME_UNIT');
      container.setAttribute('method_name', 'Add' + timeUnit);
      container.setAttribute('timeUnit', timeUnit);
    }
    return container;
  },

  domToMutation : function(xmlElement) {
    // The preexisting component dropdown cannot be reused since it might already been
    // used here due to a previous call to mutationToDom. Reusing the dropdown is not
    // allowed by Blockly, i.e. its sourceBlock is not allowed to be changed.
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    var oldRendered = this.rendered;
    this.rendered = false;
    var oldInputValues = [];
    for (var i = 0, input; input = this.inputList[i]; i++) {
      if (input.connection) {
        var block = input.connection.targetBlock();
        if (block) {
          block.unplug();
        }
        oldInputValues.push(block);
      } else {
        oldInputValues.push(null);
      }
      input.dispose();
    }
    this.inputList.length = 0;

    this.typeName = xmlElement.getAttribute('component_type');
    this.methodName = xmlElement.getAttribute('method_name');
    this.shape = xmlElement.getAttribute('shape');
    var isGenericString = xmlElement.getAttribute('is_generic');
    this.isGeneric = isGenericString == 'true';
    if(!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    } else {
      delete this.instanceName;
    }

    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);

    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    //for non-generic blocks, set the value of the component drop down
    if(!this.isGeneric) {
      this.componentDropDown.setValue(this.instanceName);
    }
    var componentDb = this.getTopWorkspace().getComponentDatabase();
    /** @type {MethodDescriptor} */
    var methodTypeObject = this.getMethodTypeObject();
    var localizedMethodName;
    if (methodTypeObject) {
      localizedMethodName = componentDb.getInternationalizedMethodName(methodTypeObject.name);
    } else {
      localizedMethodName = this.methodName;
    }
    if(!this.isGeneric) {
      if (this.typeName == "Clock" && Blockly.ComponentBlock.isClockMethodName(this.methodName)) {
        var timeUnitDropDown = Blockly.ComponentBlock.createClockAddDropDown();
        this.appendDummyInput()
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL)
          .appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR)
          .appendField('.Add')
          .appendField(timeUnitDropDown, "TIME_UNIT");
        switch (this.methodName){
          case "AddYears":
            this.setFieldValue('Years', "TIME_UNIT");
            break;
          case "AddMonths":
            this.setFieldValue('Months', "TIME_UNIT");
            break;
          case "AddWeeks":
            this.setFieldValue('Weeks', "TIME_UNIT");
            break;
          case "AddDays":
            this.setFieldValue('Days', "TIME_UNIT");
            break;
          case "AddHours":
            this.setFieldValue('Hours', "TIME_UNIT");
            break;
          case "AddMinutes":
            this.setFieldValue('Minutes', "TIME_UNIT");
            break;
          case "AddSeconds":
            this.setFieldValue('Seconds', "TIME_UNIT");
            break;
          case "AddDuration":
            this.setFieldValue('Duration', "TIME_UNIT");
            break;
        }
      } else {
        this.appendDummyInput()
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL)
          .appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR)
          .appendField('.' + localizedMethodName);
      }
      this.componentDropDown.setValue(this.instanceName);
    } else {
      this.appendDummyInput()
        .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL + componentDb.getInternationalizedComponentType(this.typeName) + '.' + localizedMethodName);
      this.appendValueInput("COMPONENT")
        .setCheck(this.typeName).appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT)
        .setAlign(Blockly.inputs.Align.RIGHT);
    }

    var tooltipDescription;
    if (methodTypeObject) {
      tooltipDescription = componentDb.getInternationalizedMethodDescription(this.getTypeName(), methodTypeObject.name,
          methodTypeObject.description);
    } else {
      tooltipDescription = componentDb.getInternationalizedMethodDescription(this.getTypeName(), this.methodName);
    }
    this.setTooltip(tooltipDescription);

    var params = [];
    if (methodTypeObject) {
      params = methodTypeObject.parameters;
    }
    oldInputValues.splice(0, oldInputValues.length - params.length);
    for (var i = 0, param; param = params[i]; i++) {
      var name = componentDb.getInternationalizedParameterName(param.name);
      var check = this.getParamBlocklyType(param);

      var input = this.appendValueInput("ARG" + i)
          .appendField(name)
          .setAlign(Blockly.inputs.Align.RIGHT)
          .setCheck(check);

      if (oldInputValues[i] && input.connection) {
        Blockly.icons.MutatorIcon.reconnect(oldInputValues[i].outputConnection, this, 'ARG' + i);
      }
    }

    for (var i = 0, input; input = this.inputList[i]; i++) {
      input.init();
    }

    if (!methodTypeObject) {
      if (this.shape === 'statement') {
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setOutput(false);
      } else if (this.shape === 'value') {
        this.setOutput(true);
        this.setPreviousStatement(false);
        this.setNextStatement(false);
      } else {
        // In theory this shouldn't happen unless there's a new input type added to Blockly
        this.setOutput(false);
        this.setPreviousStatement(false);
        this.setNextStatement(false);
      }
    } // methodType.returnType is a Yail type
    else if (methodTypeObject.returnType) {
      this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType(methodTypeObject.returnType,AI.BlockUtils.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }

    this.errors = [{name:"checkIfUndefinedBlock"}, {name:"checkIsInDefinition"},
      {name:"checkComponentNotExistsError"}, {name: "checkGenericComponentSocket"}];

    // Set as badBlock if it doesn't exist.
    this.verify();
    // Disable it if it does exist and is deprecated.
    Blockly.ComponentBlock.checkDeprecated(this, this.getMethodTypeObject());

    this.rendered = oldRendered;
  },

  getTypeName: function() {
    return this.typeName === 'Form' ? 'Screen' : this.typeName;
  },
  // Rename the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText('call ' + this.instanceName + '.' + this.methodType.name);
      this.componentDropDown.setValue(this.instanceName);
      return true;
    }
    return false;
  },
  /**
   * Get the underlying method descriptor for the block.
   * @returns {(MethodDescriptor|undefined)}
   */
  getMethodTypeObject : function() {
    return this.getTopWorkspace().getComponentDatabase()
        .getMethodForType(this.typeName, this.methodName);
  },

  getParamBlocklyType : function(param) {
    var check = [];

    var blocklyType = AI.BlockUtils.YailTypeToBlocklyType(
        param.type, AI.BlockUtils.INPUT);
    if (blocklyType) {
      if (Array.isArray(blocklyType)) {
        // Clone array.
        check = blocklyType.slice();
      } else {
        check.push(blocklyType);
      }
    }

    var helperType = AI.BlockUtils
        .helperKeyToBlocklyType(param.helperKey, this);
    if (helperType && helperType != blocklyType) {
      check.push(helperType);
    }
    return !check.length ? null : check;
  },

  getReturnBlocklyType : function(methodObj) {
    var check = [];
    var blocklyType = AI.BlockUtils.YailTypeToBlocklyType(
        methodObj.returnType, AI.BlockUtils.OUTPUT);
    if (blocklyType) {
      if (Array.isArray(blocklyType)) {
        // Clone array.
        check = blocklyType.slice();
      } else {
        check.push(blocklyType);
      }
    }

    var helperType = AI.BlockUtils
        .helperKeyToBlocklyType(methodObj.returnHelperKey, this);
    if (helperType && helperType != blocklyType) {
      check.push(helperType);
    }

    return !check.length ? null : check;
  },

  /**
   * Get a mapping from input names to {@link Blockly.Input}s.
   * @returns {Object.<string, !Blockly.Input>}}
   */
  getArgInputs: function() {
    var argList = {};
    for (var i = 0, input; input = this.getInput('ARG' + i); i++) {
      if (input.fieldRow.length == 1) {  // should only be 0 or 1
        argList[input.fieldRow[0].getValue()] = input;
      }
    }
    return argList;
  },

  /**
   * Get an array of argument names in the block.
   * @returns {Array.<string>}
   */
  getArgs: function() {
    var argList = [];
    for (var i = 0, input; input = this.getInput('ARG' + i); i++) {
      if (input.fieldRow.length == 1) {  // should only be 0 or 1
        argList.push(input.fieldRow[0].getValue());
      }
    }
    return argList;
  },

  typeblock : function(){
    var componentDb = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];
    var typeNameDict = {};
    componentDb.forEachInstance(function(instance) {
      typeNameDict[instance.typeName] = true;
      componentDb.forMethodInType(instance.typeName, function(_, methodName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL + instance.name +
          '.' + componentDb.getInternationalizedMethodName(methodName),
          mutatorAttributes: {
            component_type: instance.typeName,
            instance_name: instance.name,
            method_name: methodName,
            is_generic: 'false'
          }
        });
      });
    });

    delete typeNameDict['Form'];

    Object.keys(typeNameDict).forEach(function (typeName) {
      componentDb.forMethodInType(typeName, function (_, methodName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL +
              componentDb.getInternationalizedComponentType(typeName) + '.' +
              componentDb.getInternationalizedMethodName(methodName),
          mutatorAttributes: {
            component_type: typeName,
            method_name: methodName,
            is_generic: 'true'
          }
        });
      });
    });

    return tb;
  },

  // check if block corresponds to a method inside componentTypes[typeName].methodDictionary
  verify : function() {

    var validate = function() {
      // check component type
      var componentDb = this.getTopWorkspace().getComponentDatabase();
      var componentType = componentDb.getType(this.typeName);
      if (!componentType) {
        return false; // component does NOT exist! should not happen!
      }
      /** @type {MethodDescriptor} */
      var method = componentDb.getMethodForType(this.typeName, this.methodName);
      // check method name
      if (!method) {
        return false; // no such method : this method was for another version! block is undefined!
      }
      // check parameters
      var argList = this.getArgs();
      var argInputList = this.getArgInputs();
      var params = method.parameters;
      var modifiedParameters = false;
      if (argList.length != params.length) {
        modifiedParameters = true; // parameters have changed
      }
      for (var x = 0; x < argList.length; ++x) {
        var found = false;
        for (var i = 0, param; param = params[i]; ++i) {
          if (componentDb.getInternationalizedParameterName(param.name) == argList[x]) {
            var input = argInputList[argList[x]];
            if (!input || !input.connection) {
              modifiedParameters = true;
              break; // invalid input or connection
            }
            var check = this.getParamBlocklyType(param);
            input.setCheck(check);
            found = true;
            break;
          }
        }
        if (!found) {
          modifiedParameters = true;
        }
      }
      // check return type
      var modifiedReturnType = false;
      if (method.returnType) {
        if (!this.outputConnection) {
          modifiedReturnType = true; // missing return type
        }
        else {
          this.outputConnection.setCheck(this.getReturnBlocklyType(method));
        }
      }
      else if (!method.returnType) {
        if (this.outputConnection) {
          modifiedReturnType = true; // unexpected return type
        }
      }

      return !(modifiedParameters || modifiedReturnType);
       // passed all our tests! block is defined!
    };

    var isDefined = validate.call(this);
    if (isDefined) {
      this.notBadBlock();
    } else {
      this.badBlock();
    }
  },

  customContextMenu: function(options) {
    Blockly.ComponentBlock.addGenericOption(this, options);
  }

};


/**
 * Create a property getter or setter block for a component with the given
 * instance name. Blocks can also be generic or not, depending on the
 * values of the attribute in the mutators.
 * @lends {Blockly.BlockSvg}
 * @lends {Blockly.Block}
 */
Blockly.Blocks.component_set_get = {
  category : 'Component',
  //this.blockType = 'getter',
  helpUrl : function() {
    var url = Blockly.ComponentBlock.PROPERTIES_HELPURLS[this.getTypeName()];
    if (url && url[0] == '/') {
      var parts = url.split('#');
      parts[1] = this.getTypeName() + '.' + this.propertyName;
      url = parts.join('#');
    }
    return url;
  },

  init: function() {
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    this.genericComponentInput = Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT;
  },

  mutationToDom : function() {

    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    container.setAttribute('set_or_get', this.setOrGet);
    container.setAttribute('property_name', this.propertyName);
    var isGenericString = "false";
    if(this.isGeneric){
      isGenericString = "true";
    }
    container.setAttribute('is_generic', isGenericString);
    if(!this.isGeneric) {
      container.setAttribute('instance_name', this.instanceName);//instance name not needed
    }
    return container;
  },

  domToMutation : function(xmlElement) {
    var oldRendered = this.rendered;
    this.rendered = false;
    var oldInput = this.setOrGet == "set" && this.getInputTargetBlock('VALUE');
    for (var i = 0, input; input = this.inputList[i]; i++) {
      if (input.connection) {
        var block = input.connection.targetBlock();
        if (block) {
          if (block.isShadow()) {
            block.dispose();
          } else {
            block.unplug();
          }
        }
      }
      input.dispose();
    }
    this.inputList.length = 0;
    var componentDb = this.getTopWorkspace().getComponentDatabase();
    this.typeName = xmlElement.getAttribute('component_type');
    this.setOrGet = xmlElement.getAttribute('set_or_get');
    this.propertyName = xmlElement.getAttribute('property_name');
    this.propertyObject = this.getPropertyObject(this.propertyName);
    var isGenericString = xmlElement.getAttribute('is_generic');
    this.isGeneric = isGenericString == "true";
    if(!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    } else {
      delete this.instanceName;
    }
    if(this.setOrGet == "set"){
      this.setColour(Blockly.ComponentBlock.COLOUR_SET);
    } else {
      this.setColour(Blockly.ComponentBlock.COLOUR_GET);
    }
    var tooltipDescription;
    if (this.propertyName && this.propertyObject) {
      tooltipDescription = componentDb.getInternationalizedPropertyDescription(
        this.getTypeName(), this.propertyName, this.propertyObject.description);
    } else {
      tooltipDescription = Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP;
    }
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
      function() {
        return thisBlock.getPropertyDropDownList();
      },
      // change the output type and tooltip to match the new selection
      function(selection) {
        thisBlock.propertyName = selection;
        thisBlock.propertyObject = thisBlock.getPropertyObject(selection);
        thisBlock.setTypeCheck();
        if (thisBlock.propertyName && thisBlock.propertyObject) {
          thisBlock.setTooltip(componentDb.getInternationalizedPropertyDescription(thisBlock.getTypeName(),
              thisBlock.propertyName, thisBlock.propertyObject.description));
        } else {
          thisBlock.setTooltip(Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP);
        }
      }
    );

    if(this.setOrGet == "get") {
      //add output plug for get blocks
      this.setOutput(true);

      if(!this.isGeneric) {
        //non-generic get

        // The preexisting component dropdown cannot be reused since it might already been
        // used here due to a previous call to mutationToDom. Reusing the dropdown is not
        // allowed by Blockly, i.e. its sourceBlock is not allowed to be changed.
        this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
        this.appendDummyInput()
          .appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR)
          .appendField('.')
          .appendField(dropdown, "PROP");
      } else {
        //generic get
        this.appendDummyInput()
          .appendField(componentDb.getInternationalizedComponentType(this.typeName) + '.')
          .appendField(dropdown, "PROP");

        this.appendValueInput("COMPONENT")
          .setCheck(this.typeName)
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT)
          .setAlign(Blockly.inputs.Align.RIGHT);
      }
    } else { //this.setOrGet == "set"
      //a notches for set block
      this.setPreviousStatement(true);
      this.setNextStatement(true);
      if(!this.isGeneric) {
        // The preexisting component dropdown cannot be reused since it might already been
        // used here due to a previous call to mutationToDom. Reusing the dropdown is not
        // allowed by Blockly, i.e. its sourceBlock is not allowed to be changed.
        this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
        this.appendValueInput("VALUE")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET)
          .appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR)
          .appendField('.')
          .appendField(dropdown, "PROP")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO);
      } else {
        //generic set
        this.appendDummyInput()
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET +
                       componentDb.getInternationalizedComponentType(this.typeName) + '.')
          .appendField(dropdown, "PROP");

        this.appendValueInput("COMPONENT")
          .setCheck(this.typeName)
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT)
          .setAlign(Blockly.inputs.Align.RIGHT);

        this.appendValueInput("VALUE")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO)
          .setAlign(Blockly.inputs.Align.RIGHT);
      }
    }

    if (oldInput) {
      this.getInput('VALUE').init();
      Blockly.icons.MutatorIcon.reconnect(oldInput.outputConnection, this, 'VALUE');
    }

    //for non-generic blocks, set the value of the component drop down
    if(!this.isGeneric) {
      this.componentDropDown.setValue(this.instanceName);
    }
    //set value of property drop down
    this.setFieldValue(this.propertyName,"PROP");

    //add appropriate type checking to block
    this.setTypeCheck();

    this.setTooltip(tooltipDescription);

    this.errors = [{name:"checkIfUndefinedBlock"}, {name:"checkIsInDefinition"},
      {name:"checkComponentNotExistsError"}, {name: 'checkGenericComponentSocket'},
      {name: 'checkEmptySetterSocket'}];

    // Set as badBlock if it doesn't exist.
    this.verify();
    // Disable it if it does exist and is deprecated.
    Blockly.ComponentBlock.checkDeprecated(this, this.propertyObject);

    for (var i = 0, input; input = this.inputList[i]; i++) {
      input.init();
    }

    this.rendered = oldRendered;
  },

  getTypeName: function() {
    return this.typeName === 'Form' ? 'Screen' : this.typeName;
  },

  setTypeCheck : function() {
    var inputOrOutput = AI.BlockUtils.OUTPUT;
    if(this.setOrGet == "set") {
      inputOrOutput = AI.BlockUtils.INPUT;
    }

    var newType = this.getPropertyBlocklyType(this.propertyName,inputOrOutput);
    // This will disconnect the block if the new outputType doesn't match the
    // socket the block is plugged into.
    if(this.setOrGet == "get") {
      this.outputConnection.setCheck(newType);
    } else {
      this.getInput("VALUE").connection.setCheck(newType);
    }
  },

  getPropertyBlocklyType : function(propertyName,inputOrOutput) {
    var check = [];

    var yailType = "any"; // Necessary for undefined propertyObject.
    var property = this.getPropertyObject(propertyName);
    if (property) {
      yailType = property.type;
    }
    var blocklyType = AI.BlockUtils
        .YailTypeToBlocklyType(yailType, inputOrOutput);
    if (blocklyType) {
      if (Array.isArray(blocklyType)) {
        // Clone array.
        check = blocklyType.slice();
      } else {
        check.push(blocklyType);
      }
    }

    if (property) {
      var helperType = AI.BlockUtils
        .helperKeyToBlocklyType(property.helperKey, this);
      if (helperType && helperType != blocklyType) {
        check.push(helperType);
      }
    }

    return !check.length ? null : check;
  },

  getPropertyDropDownList : function() {
    var componentDb = this.getTopWorkspace().getComponentDatabase();
    var dropDownList = [];
    var propertyNames = [this.propertyName];
    if (this.propertyObject) {
      if (this.propertyObject.deprecated == "true") { // [lyn, 2015/12/27] Handle deprecated properties specially
        propertyNames = [this.propertyObject.name]; // Only list the deprecated property name and no others
      } else if(this.setOrGet == "set") {
        propertyNames = componentDb.getSetterNamesForType(this.typeName);
      } else {
        propertyNames = componentDb.getGetterNamesForType(this.typeName);
      }
    }

    for(var i=0;i<propertyNames.length;i++) {
      dropDownList.push([componentDb.getInternationalizedPropertyName(propertyNames[i]), propertyNames[i]]);
    }
    return dropDownList;
  },
  getPropertyObject : function(propertyName) {
    return this.getTopWorkspace().getComponentDatabase().getPropertyForType(this.typeName, propertyName);
  },
  // Rename the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText(this.instanceName + '.');
      this.componentDropDown.setValue(this.instanceName);
      return true;
    }
    return false;
  },
  typeblock : function(){
    var componentDb = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];

    function pushBlock(prefix, mode, property, typeName, instanceName) {
      tb.push({
        translatedName: prefix + instanceName + '.' +
          componentDb.getInternationalizedPropertyName(property),
        mutatorAttributes: {
          set_or_get: mode,
          component_type: typeName,
          instance_name: instanceName,
          property_name: property,
          is_generic: 'false'
        }
      });
    }

    function pushGenericBlock(prefix, mode, property, typeName) {
      if (typeName === 'Form') {
        return;  // Skip generation of 'any Form' blocks
      }
      tb.push({
        translatedName: prefix + componentDb.getInternationalizedComponentType(typeName) + '.' +
          componentDb.getInternationalizedPropertyName(property),
        mutatorAttributes: {
          set_or_get: mode,
          component_type: typeName,
          property_name: property,
          is_generic: true
        }
      })
    }

    componentDb.forEachInstance(function(component) {

      // Filter out all deprecated properties for both get and set
      var deprecatedProperties = []
      var allCurrentProperties = componentDb.types_[component.typeName].properties;
      for (var prop in allCurrentProperties) {
        if (allCurrentProperties[prop].deprecated) {
          deprecatedProperties.push(allCurrentProperties[prop].name);
        }
      }

      var setters = componentDb.getSetterNamesForType(component.typeName),
          getters = componentDb.getGetterNamesForType(component.typeName),
          k;

      var filteredSetters = setters.filter(function (prop) { return !deprecatedProperties.includes(prop)})
      var filteredGetters = getters.filter(function (prop) { return !deprecatedProperties.includes(prop)})

      for(k=0; k<filteredSetters.length; k++) {
        pushBlock(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET, 'set', filteredSetters[k],
          component.typeName, component.name, false);
      }
      for(k=0; k<filteredGetters.length; k++) {
        pushBlock('', 'get', filteredGetters[k], component.typeName, component.name, false);
      }
      for(k=0; k<filteredSetters.length; k++) {
        pushGenericBlock(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET, 'set', filteredSetters[k],
          component.typeName);
      }
      for(k=0; k<filteredGetters.length; k++) {
        pushGenericBlock('', 'get', filteredGetters[k], component.typeName);
      }
    });

    return tb;
  },

  verify : function() {

    var validate = function() {
      // check component type
      var componentType = this.getTopWorkspace().getComponentDatabase().getType(this.typeName);
      if (!componentType) {
        return false; // component does NOT exist! should not happen!
      }
      var properties = componentType.properties;
      var property = properties[this.propertyName];
      // check property name
      if (!property) {
        return false; // no such event : this property was for another version!  block is undefined!
      }
      // check permissions
      if (this.setOrGet == "get") {
        if (componentType.getPropertyList.indexOf(this.propertyName) == -1) {
          return false;
        }
      } else if (this.setOrGet == "set") {
        if (componentType.setPropertyList.indexOf(this.propertyName) == -1) {
          return false;
        }
      }
      this.setTypeCheck(); // correct the type
      return true; // passed all our tests! block is defined
    };

    var isDefined = validate.call(this);
    if (isDefined) {
      this.notBadBlock();
    } else {
      this.badBlock(true);
    }

  },

  customContextMenu: function(options) {
    Blockly.ComponentBlock.addGenericOption(this, options);
  }

};

/**
 * Create a component (object) block for a component with the given
 * instance name.
 * @lends {Blockly.BlockSvg}
 * @lends {Blockly.Block}
 */
Blockly.Blocks.component_component_block = {
  category : 'Component',

  helpUrl : function() {
    return Blockly.ComponentBlock.HELPURLS[this.getTypeName()];
  },  // TODO: fix

  mutationToDom : function() {
    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    container.setAttribute('instance_name', this.instanceName);
    return container;
  },

  domToMutation : function(xmlElement) {

    this.typeName = xmlElement.getAttribute('component_type');
    this.instanceName = xmlElement.getAttribute('instance_name');

    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    this.componentDropDown.setValue(this.instanceName);

    this.appendDummyInput().appendField(this.componentDropDown, Blockly.ComponentBlock.COMPONENT_SELECTOR);
    //this.componentDropDown.setValue(this.instanceName);
    this.setOutput(true, [this.typeName,"COMPONENT","Key"]);
    this.errors = [{name:"checkIfUndefinedBlock"},{name:"checkComponentNotExistsError"}];
  },

  getTypeName: function() {
    return this.typeName === 'Form' ? 'Screen' : this.typeName;
  },

  // Renames the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText(this.instanceName);
      this.componentDropDown.setValue(this.instanceName);
      return true;
    }
    return false;
  },

  typeblock : function(){
    var componentDb = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];

    componentDb.forEachInstance(function(instance) {
      tb.push({
        translatedName: instance.name,
        mutatorAttributes: {
          component_type: instance.typeName,
          instance_name: instance.name
        }
      });
    });
    return tb;
  },

  verify : function() {
    // TODO(ewpatton): Logic assumes that components cannot be removed (e.g., editing AIA)
    if (this.getTopWorkspace().getComponentDatabase().hasType(this.typeName)) {
      this.notBadBlock();
    } else {
      this.badBlock();
    }
  }

};

/**
 * Create a component list block for a given component type.
 * @lends {Blockly.BlockSvg}
 * @lends {Blockly.Block}
 */
Blockly.Blocks['component_all_component_block'] = {
  category : 'Component',

  helpUrl : function() {
    var mode = this.typeName === "Form" ? "Screen" : this.typeName;
    return Blockly.ComponentBlock.HELPURLS[mode];
  },

  mutationToDom : function() {
    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    return container;
  },

  domToMutation : function(xmlElement) {

    this.typeName = xmlElement.getAttribute('component_type');
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.componentTypeDropDown = Blockly.ComponentBlock.createComponentTypeDropDown(this);
    this.componentTypeDropDown.setValue(this.typeName);

    this.appendDummyInput()
      .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_EVERY_COMPONENT_TITLE_EVERY)
      .appendField(this.componentTypeDropDown, Blockly.ComponentBlock.COMPONENT_TYPE_SELECTOR);
    this.setOutput(true, AI.BlockUtils.YailTypeToBlocklyType("list", AI.BlockUtils.OUTPUT));
    this.errors = [{name:"checkIfUndefinedBlock"}, {name:"checkComponentTypeNotExistsError"}];
  },
  // Renames the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    return true;
  },

  typeblock : function() {
    var componentDb = Blockly.common.getMainWorkspace().getComponentDatabase();
    var tb = [];

    componentDb.forEachInstance(function(instance) {
      if (instance.typeName != "Form") {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_EVERY_COMPONENT_TITLE_EVERY +
              " " + componentDb.getInternationalizedComponentType(instance.typeName),
          mutatorAttributes: {
            component_type: instance.typeName,
          }
        });
      }
    });

    goog.array.removeDuplicates(tb, null, function(t) {
      return t.mutatorAttributes.component_type;
    });
    return tb;
  },

  verify : function() {
    // TODO(ewpatton): Logic assumes that components cannot be removed (e.g., editing AIA)
    if (this.getTopWorkspace().getComponentDatabase().hasType(this.typeName)) {
      this.notBadBlock();
    } else {
      this.badBlock();
    }
  }

};

Blockly.ComponentBlock.timeUnits = ["Years", "Months", "Weeks", "Days", "Hours", "Minutes", "Seconds", "Duration"];
Blockly.ComponentBlock.timeUnitsMenu =
  [[ Blockly.Msg.TIME_YEARS, "Years"],
   [ Blockly.Msg.TIME_MONTHS, "Months"],
   [ Blockly.Msg.TIME_WEEKS, "Weeks"],
   [ Blockly.Msg.TIME_DAYS, "Days"],
   [ Blockly.Msg.TIME_HOURS, "Hours"],
   [ Blockly.Msg.TIME_MINUTES, "Minutes"],
   [ Blockly.Msg.TIME_SECONDS, "Seconds"],
   [ Blockly.Msg.TIME_DURATION, "Duration"]
   ];

Blockly.ComponentBlock.clockMethodNames = ["AddYears", "AddMonths","AddWeeks", "AddDays",
  "AddHours", "AddMinutes", "AddSeconds", "AddDuration"];
Blockly.ComponentBlock.isClockMethodName =  function  (name) {
    return Blockly.ComponentBlock.clockMethodNames.indexOf(name) != -1;
};

Blockly.ComponentBlock.createComponentDropDown = function(block){
  var componentDropDown = new AI.Blockly.FieldNoCheckDropdown([["",""]]);
  componentDropDown.menuGenerator_ = function(){ return block.getTopWorkspace().getComponentDatabase().getComponentNamesByType(block.typeName); };
  return componentDropDown;
};

Blockly.ComponentBlock.createComponentTypeDropDown = function(block) {
  var componentDropDown = new Blockly.FieldDropdown([["",""]]);
  componentDropDown.menuGenerator_ = function() { return block.getTopWorkspace().getComponentDatabase().getComponentTypes(); };
  return componentDropDown;
};

Blockly.ComponentBlock.createClockAddDropDown = function(/*block*/){
  var componentDropDown = new Blockly.FieldDropdown([["",""]]);
  componentDropDown.menuGenerator_ = function(){ return Blockly.ComponentBlock.timeUnitsMenu; };
  return componentDropDown;
};

Blockly.ComponentBlock.HELPURLS = {
  "Button": Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_HELPURL,
  "Canvas": Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_HELPURL,
  "CheckBox": Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL,
  "Switch": Blockly.Msg.LANG_COMPONENT_BLOCK_SWITCH_HELPURL,
  "Clock": Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_HELPURL,
  "Image": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_HELPURL,
  "Label": Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_HELPURL,
  "ListPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_HELPURL,
  "DatePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_DATEPICKER_HELPURL,
  "TimePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_TIMEPICKER_HELPURL,
  "ListView": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTVIEW_HELPURL,
  "PasswordTextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_HELPURL,
  "Screen": Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_HELPURL,
  "Slider": Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_HELPURL,
  "TextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_HELPURL,
  "TinyDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_HELPURL,
  "Camcorder": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_HELPURL,
  "Camera": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_HELPURL,
  "ImagePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_HELPURL,
  "Player": Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_HELPURL,
  "Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_HELPURL,
  "VideoPlayer": Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL,
  "Ball": Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_HELPURL,
  "ImageSprite": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL,
  "Map": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_HELPURL,
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
  "Navigation": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_NAVIGATION_HELPURL,
  "Polygon": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL,
  "Rectangle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL,
  "Chart": Blockly.Msg.LANG_COMPONENT_BLOCK_CHART_HELPURL,
  "ChartData2D": Blockly.Msg.LANG_COMPONENT_BLOCK_CHARTDATA2D_HELPURL,
  "AnomalyDetection": Blockly.Msg.LANG_COMPONENT_BLOCK_ANOMALYDETECTION_HELPURL,
  "Regression": Blockly.Msg.LANG_COMPONENT_BLOCK_REGRESSION_HELPURL,
  "ContactPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL,
  "EmailPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL,
  "CloudDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_CLOUDDB_HELPURL,
  "FirebaseDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_HELPURL,
  "PhoneCall": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL,
  "PhoneNumberPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL,
  "Sharing": Blockly.Msg.LANG_COMPONENT_BLOCK_SHARING_HELPURL,
  "Texting": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_HELPURL,
  "Twitter": Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_HELPURL,
  "AccelerometerSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL,
  "Barometer": Blockly.Msg.LANG_COMPONENT_BLOCK_BAROMETER_HELPURL,
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_HELPURL,
  "Hygrometer": Blockly.Msg.LANG_COMPONENT_BLOCK_HYGROMETER_HELPURL,
  "LightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LIGHTSENSOR_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
  "Thermometer": Blockly.Msg.LANG_COMPONENT_BLOCK_THERMOMETER_HELPURL,
  "HorizontalArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL,
  "HorizontalScrollArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZSCROLLARRANGE_HELPURL,
  "TableArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL,
  "VerticalArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL,
  "VerticalScrollArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_VERTSCROLLARRANGE_HELPURL,
  "NxtColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL,
  "NxtDirectCommands": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL,
  "NxtDrive": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL,
  "NxtLightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL,
  "NxtSoundSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL,
  "NxtTouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL,
  "NxtUltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL,
  "Ev3Motors": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3MOTORS_HELPURL,
  "Ev3ColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COLORSENSOR_HELPURL,
  "Ev3GyroSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3GYROSENSOR_HELPURL,
  "Ev3TouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3TOUCHSENSOR_HELPURL,
  "Ev3UltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3ULTRASONICSENSOR_HELPURL,
  "Ev3UI": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3UI_HELPURL,
  "Ev3Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3SOUND_HELPURL,
  "Ev3Commands": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COMMANDS_HELPURL,
  "ActivityStarter": Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL,
  "BarcodeScanner": Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL,
  "BluetoothClient": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL,
  "BluetoothServer": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL,
  "Notifier": Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL,
  "Serial": Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_HELPURL,
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL,
  "Spreadsheet": Blockly.Msg.LANG_COMPONENT_BLOCK_SPREADSHEET_HELPURL,
  "GameClient": Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL,
  "SoundRecorder": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL,
  "Voting": Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_HELPURL,
  "WebViewer": Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL
};

Blockly.ComponentBlock.PROPERTIES_HELPURLS = {
  "Button": Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_PROPERTIES_HELPURL,
  "Canvas": Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL,
  "CheckBox": Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_PROPERTIES_HELPURL,
  "Switch": Blockly.Msg.LANG_COMPONENT_BLOCK_SWITCH_HELPURL,
  "Clock": Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_PROPERTIES_HELPURL,
  "Image": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_PROPERTIES_HELPURL,
  "Label": Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_PROPERTIES_HELPURL,
  "ListPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_PROPERTIES_HELPURL,
  "DatePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_DATEPICKER_HELPURL,
  "TimePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_TIMEPICKER_HELPURL,
  "ListView": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTVIEW_HELPURL,
  "PasswordTextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_PROPERTIES_HELPURL,
  "Screen": Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_PROPERTIES_HELPURL,
  "Slider": Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_PROPERTIES_HELPURL,
  "Spinner": Blockly.Msg.LANG_COMPONENT_BLOCK_SPINNER_HELPURL,
  "TextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_PROPERTIES_HELPURL,
  "TinyDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL,
  "Camcorder": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_PROPERTIES_HELPURL,
  "Camera": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_PROPERTIES_HELPURL,
  "ImagePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_PROPERTIES_HELPURL,
  "Player": Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_PROPERTIES_HELPURL,
  "Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_PROPERTIES_HELPURL,
  "VideoPlayer": Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL,
  "Ball": Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL,
  "ImageSprite": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL,
  "Map": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_HELPURL,
  "Chart": Blockly.Msg.LANG_COMPONENT_BLOCK_CHART_HELPURL,
  "ChartData2D": Blockly.Msg.LANG_COMPONENT_BLOCK_CHARTDATA2D_HELPURL,
  "AnomalyDetection": Blockly.Msg.LANG_COMPONENT_BLOCK_ANOMALYDETECTION_HELPURL,
  "Regression": Blockly.Msg.LANG_COMPONENT_BLOCK_REGRESSION_HELPURL,
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
  "Navigation": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_NAVIGATION_HELPURL,
  "Polygon": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL,
  "Rectangle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL,
  "ContactPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL,
  "EmailPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL,
  "CloudDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_CLOUDDB_HELPURL,
  "FirebaseDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_PROPERTIES_HELPURL,
  "PhoneCall": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL,
  "PhoneNumberPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL,
  "Sharing": Blockly.Msg.LANG_COMPONENT_BLOCK_SHARING_HELPURL,
  "Texting": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL,
  "Twitter": Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL,
  "AccelerometerSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL,
  "Barometer": Blockly.Msg.LANG_COMPONENT_BLOCK_BAROMETER_HELPURL,
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_PROPERTIES_HELPURL,
  "Hygrometer": Blockly.Msg.LANG_COMPONENT_BLOCK_HYGROMETER_HELPURL,
  "LightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LIGHTSENSOR_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
  "Thermometer": Blockly.Msg.LANG_COMPONENT_BLOCK_THERMOMETER_HELPURL,
  "HorizontalArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL,
  "HorizontalScrollArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZSCROLLARRANGE_HELPURL,
  "TableArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_PROPERTIES_HELPURL,
  "VerticalArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL,
  "VerticalScrollArrangement": Blockly.Msg.LANG_COMPONENT_BLOCK_VERTSCROLLARRANGE_HELPURL,
  "NxtColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL,
  "NxtDirectCommands": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL,
  "NxtDrive": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL,
  "NxtLightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL,
  "NxtSoundSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL,
  "NxtTouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL,
  "NxtUltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL,
  "Ev3Motors": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3MOTORS_HELPURL,
  "Ev3ColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COLORSENSOR_HELPURL,
  "Ev3GyroSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3GYROSENSOR_HELPURL,
  "Ev3TouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3TOUCHSENSOR_HELPURL,
  "Ev3UltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3ULTRASONICSENSOR_HELPURL,
  "Ev3UI": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3UI_HELPURL,
  "Ev3Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3SOUND_HELPURL,
  "Ev3Commands": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COMMANDS_HELPURL,
  "ActivityStarter": Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL,
  "BarcodeScanner": Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL,
  "BluetoothClient": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL,
  "BluetoothServer": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL,
  "Notifier": Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_PROPERTIES_HELPURL,
  "Serial": Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_PROPERTIES_HELPURL,
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL,
  "Spreadsheet": Blockly.Msg.LANG_COMPONENT_BLOCK_SPREADSHEET_PROPERTIES_HELPURL,
  "GameClient": Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL,
  "SoundRecorder": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL,
  "Voting": Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL,
  "WebViewer": Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL
};

Blockly.ComponentBlock.EVENTS_HELPURLS = {
  "Button": Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_EVENTS_HELPURL,
  "Canvas": Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL,
  "Clock": Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_EVENTS_HELPURL,
  "CheckBox": Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_EVENTS_HELPURL,
  "Switch": Blockly.Msg.LANG_COMPONENT_BLOCK_SWITCH_HELPURL,
  "Image": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_EVENTS_HELPURL,
  "Label": Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_EVENTS_HELPURL,
  "ListPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_EVENTS_HELPURL,
  "DatePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_DATEPICKER_HELPURL,
  "TimePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_TIMEPICKER_HELPURL,
  "ListView": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTVIEW_HELPURL,
  "PasswordTextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_EVENTS_HELPURL,
  "Screen": Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_EVENTS_HELPURL,
  "Slider": Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_EVENTS_HELPURL,
  "Spinner": Blockly.Msg.LANG_COMPONENT_BLOCK_SPINNER_HELPURL,
  "TextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_EVENTS_HELPURL,
  "TinyDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL,
  "Camcorder": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_EVENTS_HELPURL,
  "Camera": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_EVENTS_HELPURL,
  "ImagePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_EVENTS_HELPURL,
  "Player": Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_EVENTS_HELPURL,
  "Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_EVENTS_HELPURL,
  "VideoPlayer": Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL,
  "Ball": Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL,
  "ImageSprite": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL,
  "Map": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_HELPURL,
  "Chart": Blockly.Msg.LANG_COMPONENT_BLOCK_CHART_HELPURL,
  "ChartData2D": Blockly.Msg.LANG_COMPONENT_BLOCK_CHARTDATA2D_HELPURL,
  "AnomalyDetection": Blockly.Msg.LANG_COMPONENT_BLOCK_ANOMALYDETECTION_HELPURL,
  "Regression": Blockly.Msg.LANG_COMPONENT_BLOCK_REGRESSION_HELPURL,
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
  "Navigation": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_NAVIGATION_HELPURL,
  "Polygon": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL,
  "Rectangle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL,
  "ContactPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL,
  "EmailPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL,
  "CloudDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_CLOUDDB_HELPURL,
  "FirebaseDB" : Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_EVENTS_HELPURL,
  "PhoneCall": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL,
  "PhoneNumberPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL,
  "Sharing": Blockly.Msg.LANG_COMPONENT_BLOCK_SHARING_HELPURL,
  "Texting": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL,
  "Twitter": Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL,
  "AccelerometerSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL,
  "Barometer": Blockly.Msg.LANG_COMPONENT_BLOCK_BAROMETER_HELPURL,
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_EVENTS_HELPURL,
  "Hygrometer": Blockly.Msg.LANG_COMPONENT_BLOCK_HYGROMETER_HELPURL,
  "LightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LIGHTSENSOR_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
  "Thermometer": Blockly.Msg.LANG_COMPONENT_BLOCK_THERMOMETER_HELPURL,
  "NxtColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL,
  "NxtLightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL,
  "NxtSoundSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL,
  "NxtTouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL,
  "NxtUltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL,
  "Ev3Motors": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3MOTORS_HELPURL,
  "Ev3ColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COLORSENSOR_HELPURL,
  "Ev3GyroSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3GYROSENSOR_HELPURL,
  "Ev3TouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3TOUCHSENSOR_HELPURL,
  "Ev3UltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3ULTRASONICSENSOR_HELPURL,
  "Ev3UI": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3UI_HELPURL,
  "Ev3Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3SOUND_HELPURL,
  "Ev3Commands": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COMMANDS_HELPURL,
  "ActivityStarter": Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL,
  "BarcodeScanner": Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL,
  "BluetoothClient": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL,
  "BluetoothServer": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL,
  "Notifier": Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL,
  "Serial": Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_EVENTS_HELPURL,
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL,
  "Spreadsheet": Blockly.Msg.LANG_COMPONENT_BLOCK_SPREADSHEET_EVENTS_HELPURL,
  "GameClient": Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL,
  "SoundRecorder": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL,
  "Voting": Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL,
  "WebViewer": Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL
};

Blockly.ComponentBlock.METHODS_HELPURLS = {
  "Canvas": Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL,
  "Clock": Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_METHODS_HELPURL,
  "Image": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_METHODS_HELPURL,
  "Label": Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_METHODS_HELPURL,
  "ListPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_METHODS_HELPURL,
  "DatePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_DATEPICKER_HELPURL,
  "TimePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_TIMEPICKER_HELPURL,
  "ListView": Blockly.Msg.LANG_COMPONENT_BLOCK_LISTVIEW_HELPURL,
  "PasswordTextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_METHODS_HELPURL,
  "Screen": Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_METHODS_HELPURL,
  "Slider": Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_METHODS_HELPURL,
  "Spinner": Blockly.Msg.LANG_COMPONENT_BLOCK_SPINNER_HELPURL,
  "TextBox": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_METHODS_HELPURL,
  "TinyDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL,
  "Camcorder": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_METHODS_HELPURL,
  "Camera": Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_METHODS_HELPURL,
  "ImagePicker": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_METHODS_HELPURL,
  "Player": Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_METHODS_HELPURL,
  "Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_METHODS_HELPURL,
  "VideoPlayer": Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL,
  "Ball": Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL,
  "ImageSprite": Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL,
  "Map": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_HELPURL,
  "Chart": Blockly.Msg.LANG_COMPONENT_BLOCK_CHART_HELPURL,
  "ChartData2D": Blockly.Msg.LANG_COMPONENT_BLOCK_CHARTDATA2D_HELPURL,
  "AnomalyDetection": Blockly.Msg.LANG_COMPONENT_BLOCK_ANOMALYDETECTION_HELPURL,
  "Regression": Blockly.Msg.LANG_COMPONENT_BLOCK_REGRESSION_HELPURL,
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
  "Navigation": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_NAVIGATION_HELPURL,
  "Polygon": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL,
  "Rectangle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL,
  "ContactPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL,
  "EmailPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL,
  "CloudDB": Blockly.Msg.LANG_COMPONENT_BLOCK_CLOUDDB_HELPURL,
  "FirebaseDB": Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_METHODS_HELPURL,
  "PhoneCall": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL,
  "PhoneNumberPicker": Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL,
  "Sharing": Blockly.Msg.LANG_COMPONENT_BLOCK_SHARING_HELPURL,
  "Texting": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL,
  "Twitter": Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL,
  "AccelerometerSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL,
  "Barometer": Blockly.Msg.LANG_COMPONENT_BLOCK_BAROMETER_HELPURL,
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_METHODS_HELPURL,
  "Hygrometer": Blockly.Msg.LANG_COMPONENT_BLOCK_HYGROMETER_HELPURL,
  "LightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LIGHTSENSOR_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
  "Thermometer": Blockly.Msg.LANG_COMPONENT_BLOCK_THERMOMETER_HELPURL,
  "NxtColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL,
  "NxtDirectCommands": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL,
  "NxtDrive": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL,
  "NxtLightSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL,
  "NxtSoundSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL,
  "NxtTouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL,
  "NxtUltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL,
  "Ev3Motors": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3MOTORS_HELPURL,
  "Ev3ColorSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COLORSENSOR_HELPURL,
  "Ev3GyroSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3GYROSENSOR_HELPURL,
  "Ev3TouchSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3TOUCHSENSOR_HELPURL,
  "Ev3UltrasonicSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3ULTRASONICSENSOR_HELPURL,
  "Ev3UI": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3UI_HELPURL,
  "Ev3Sound": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3SOUND_HELPURL,
  "Ev3Commands": Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COMMANDS_HELPURL,
  "ActivityStarter": Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL,
  "BarcodeScanner": Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL,
  "BluetoothClient": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL,
  "BluetoothServer": Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL,
  "Notifier": Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_METHODS_HELPURL,
  "Serial": Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_METHODS_HELPURL,
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL,
  "Spreadsheet": Blockly.Msg.LANG_COMPONENT_BLOCK_SPREADSHEET_METHODS_HELPURL,
  "GameClient": Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL,
  "SoundRecorder": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL,
  "Voting": Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL,
  "WebViewer": Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL
};
