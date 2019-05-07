// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2018 MIT, All rights reserved
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

goog.provide('AI.Blockly.Blocks.components');
goog.provide('AI.Blockly.ComponentBlock');
goog.require('Blockly.Blocks.Utilities');

Blockly.Blocks.components = {};
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

/**
 * Add a menu option to the context menu for {@code block} to swap between
 * the generic and specific versions of the block.
 *
 * @param {Blockly.BlockSvg} block the block to manipulate
 * @param {Array.<{enabled,text,callback}>} options the menu options
 */
Blockly.ComponentBlock.addGenericOption = function(block, options) {
  if (block.type === 'component_event' && block.isGeneric) {
    return;  // Cannot make a generic component_event specific for now...
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
    Blockly.Events.fire(new Blockly.Events.Change(
      block, 'mutation', null, oldMutation, newMutation));
    if (block.type === 'component_event') opt_replacementDom = false;
    if (opt_replacementDom !== false) {
      if (opt_replacementDom === undefined) {
        var compBlockXml = '<xml><block type="component_component_block">' +
          '<mutation component_type="' + block.typeName + '" instance_name="' + instanceName + '"></mutation>' +
          '<field name="COMPONENT_SELECTOR">' + instanceName + '</field>' +
          '</block></xml>';
        opt_replacementDom = Blockly.Xml.textToDom(compBlockXml).firstElementChild;
      }
      var replacement = Blockly.Xml.domToBlock(opt_replacementDom, block.workspace);
      replacement.initSvg();
      block.getInput('COMPONENT').connection.connect(replacement.outputConnection);
    }
    var group = Blockly.Events.getGroup();
    setTimeout(function() {
      Blockly.Events.setGroup(group);
      // noinspection JSAccessibilityCheck
      block.bumpNeighbours_();
      Blockly.Events.setGroup(false);
    }, Blockly.BUMP_DELAY);
  }

  var item = { enabled: false };
  if (block.isGeneric) {
    var compBlock = block.getInputTargetBlock('COMPONENT');
    item.enabled = compBlock && compBlock.type === 'component_component_block';
    item.text = Blockly.Msg.UNGENERICIZE_BLOCK;
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
        Blockly.Events.fire(new Blockly.Events.Change(
          block, 'mutation', null, oldMutation, newMutation));
        var group = Blockly.Events.getGroup();
        setTimeout(function () {
          Blockly.Events.setGroup(group);
          // noinspection JSAccessibilityCheck
          block.bumpNeighbours_();
          Blockly.Events.setGroup(false);
        }, Blockly.BUMP_DELAY);
      } finally {
        Blockly.Events.setGroup(false);
      }
    };
  } else if (block.type === 'component_event') {
    item.enabled = true;
    item.text = Blockly.Msg.GENERICIZE_BLOCK;
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
        var varBlockDom = Blockly.Xml.textToDom(varBlockXml).firstElementChild;
        makeGeneric(block);  // Do this first so 'component' is defined.
        block.walk(function(block) {
            if ((block.type === 'component_method' || block.type === 'component_set_get') &&
              block.instanceName === instanceName) {
            makeGeneric(/** @type Blockly.BlockSvg */ block, varBlockDom);
          }
        });
      } finally {
        Blockly.Events.setGroup(false);
      }
    };
  } else {
    item.enabled = true;
    item.text = Blockly.Msg.GENERICIZE_BLOCK;
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
    return container;
  },

  domToMutation : function(xmlElement) {
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
      tooltipDescription = eventType.description;
    }
    else {
      tooltipDescription = Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP;
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

    if (eventType && eventType.deprecated === "true" && this.workspace === Blockly.mainWorkspace) {
      this.badBlock();
      this.setDisabled(true);
    }

    this.verify(); // verify the block and mark it accordingly

    this.rendered = oldRendered;
  },
  // [lyn, 10/24/13] Allow switching between horizontal and vertical display of arguments
  // Also must create flydown params and DO input if they don't exist.

  // To-DO: consider using top.BlocklyPanel... instead of window.parent.BlocklyPanel

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

        // .. and insert new ones:
        if (params.length > 0) {
          var paramInput = this.appendDummyInput('PARAMETERS')
                               .appendField(" ")
                               .setAlign(Blockly.ALIGN_LEFT);
          for (i = 0; param = params[i]; i++) {
            paramInput.appendField(new Blockly.FieldParameterFlydown(componentDb.getInternationalizedParameterName(param.name), false, null, null, param.name), // false means not editable
                                   'VAR' + i)
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

        // .. and insert new ones:

        // Vertically aligned parameters
        for (i = 0; param = params[i]; i++) {
          this.appendDummyInput('VAR' + i)
              .appendField(new Blockly.FieldParameterFlydown(componentDb.getInternationalizedParameterName(param.name), false),
                           'VAR' + i)
              .setAlign(Blockly.ALIGN_RIGHT);
        }
        newDoInput = this.appendStatementInput("DO")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO);
        if (bodyConnection) {
          newDoInput.connection.connect(bodyConnection);
        }
      }
      if (Blockly.Events.isEnabled()) {
        // Trigger a Blockly UI change event
        Blockly.Events.fire(new Blockly.Events.Ui(this, 'parameter_orientation',
          (!this.horizontalParameters).toString(), this.horizontalParameters.toString()))
      }
    }
  },
  // Return a list of parameter names
  getParameters: function () {
    /** @type {EventDescriptor} */
    var eventType = this.getEventTypeObject();
    if (this.isGeneric) {
      return [
          {name:'component', type:'component'},
          {name:'notAlreadyHandled', type: 'boolean'}
        ].concat((eventType && eventType.parameters) || []);
    }
    return eventType && eventType.parameters;
  },
  // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      this.componentDropDown.setValue(this.instanceName);
      Blockly.Blocks.Utilities.renameCollapsed(this, 0);
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
    var mode = this.typeName === "Form" ? "Screen" : this.typeName;
    return Blockly.ComponentBlock.EVENTS_HELPURLS[mode];
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
    return this.getVars();
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
    var componentDb = Blockly.mainWorkspace.getComponentDatabase();
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

    Object.keys(types).forEach(function(typeName) {
      componentDb.forEventInType(typeName, function(_, eventName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_EVENT_TITLE +
            componentDb.getInternationalizedComponentType(typeName),
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
      // No need to check event return type, events do not return.
      return true; // passed all our tests! block is defined!
    };
    var isDefined = validate.call(this);

    if (isDefined) {
      this.notBadBlock();
      if (this.getEventTypeObject()) {
        this.setTooltip(this.getEventTypeObject().description); // update the tooltipDescription, if block is defined
      }
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
      var mode = this.typeName === "Form" ? "Screen" : this.typeName;
      return Blockly.ComponentBlock.METHODS_HELPURLS[mode];
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
    if (!this.isGeneric && this.typeName == "Clock" && Blockly.ComponentBlock.isClockMethodName(this.methodName)) {
      var timeUnit = this.getFieldValue('TIME_UNIT');
      container.setAttribute('method_name', 'Add' + timeUnit);
      container.setAttribute('timeUnit', timeUnit);
    }
    return container;
  },

  domToMutation : function(xmlElement) {
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
        .setAlign(Blockly.ALIGN_RIGHT);
    }

    var tooltipDescription;
    if (methodTypeObject) {
      tooltipDescription = methodTypeObject.description;
    } else {
      tooltipDescription = Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP;
    }
    this.setTooltip(tooltipDescription);

    var params = [];
    if (methodTypeObject) {
      params = methodTypeObject.parameters;
    }
    oldInputValues.splice(0, oldInputValues.length - params.length);
    for (var i = 0, param; param = params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendField(componentDb.getInternationalizedParameterName(param.name));
      newInput.setAlign(Blockly.ALIGN_RIGHT);
      var blockyType = Blockly.Blocks.Utilities.YailTypeToBlocklyType(param.type,Blockly.Blocks.Utilities.INPUT);
      newInput.connection.setCheck(blockyType);
      if (oldInputValues[i] && newInput.connection) {
        Blockly.Mutator.reconnect(oldInputValues[i].outputConnection, this, 'ARG' + i);
      }
    }

    for (var i = 0, input; input = this.inputList[i]; i++) {
      input.init();
    }

    if (!methodTypeObject) {
      this.setOutput(false);
      this.setPreviousStatement(false);
      this.setNextStatement(false);
    } // methodType.returnType is a Yail type
    else if (methodTypeObject.returnType) {
      this.setOutput(true, Blockly.Blocks.Utilities.YailTypeToBlocklyType(methodTypeObject.returnType,Blockly.Blocks.Utilities.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }

    this.errors = [{name:"checkIfUndefinedBlock"},{name:"checkIsInDefinition"},{name:"checkComponentNotExistsError"}];

    // mark the block bad if the method isn't defined or is marked deprecated
    var method = this.getMethodTypeObject();
    if ((!method || method.deprecated === true || method.deprecated === 'true') &&
        this.workspace === Blockly.mainWorkspace) {
      this.badBlock();
      this.setDisabled(true);
    }

    this.verify(); // verify the block and mark it accordingly

    this.rendered = oldRendered;
  },
  // Rename the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText('call ' + this.instanceName + '.' + this.methodType.name);
      this.componentDropDown.setValue(this.instanceName);
      Blockly.Blocks.Utilities.renameCollapsed(this, 0);
      return true;
    }
    return false;
  },
  /**
   * Get the underlying method descriptor for the block.
   * @returns {(MethodDescriptor|undefined)}
   */
  getMethodTypeObject : function() {
    return this.getTopWorkspace().getComponentDatabase().getMethodForType(this.typeName, this.methodName);
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
    var componentDb = Blockly.mainWorkspace.getComponentDatabase();
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
    goog.object.forEach(typeNameDict, function(componentType) {
      componentDb.forMethodInType(componentType, function(_, methodName) {
        tb.push({
          translatedName: Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL +
          componentDb.getInternationalizedComponentType(componentType) + '.' +
          componentDb.getInternationalizedMethodName(methodName),
          mutatorAttributes: {
            component_type: componentType,
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
            var blockyType = Blockly.Blocks.Utilities.YailTypeToBlocklyType(param.type,Blockly.Blocks.Utilities.INPUT);
            input.connection.setCheck(blockyType); // correct type
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
          this.outputConnection.setCheck(Blockly.Blocks.Utilities.YailTypeToBlocklyType(method.returnType,Blockly.Blocks.Utilities.OUTPUT));
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
      if (this.getMethodTypeObject()) {
        this.setTooltip(this.getMethodTypeObject().description); // update the tooltipDescription, if block is defined
      }
    } else {
      this.badBlock();
    }
  },

  customContextMenu: function(options) {
    Blockly.ComponentBlock.addGenericOption(this, options);
    Blockly.Block.prototype.customContextMenu.call(this, options);
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
    var mode = this.typeName === "Form" ? "Screen" : this.typeName;
    return Blockly.ComponentBlock.PROPERTIES_HELPURLS[mode];
  },

  init: function() {
    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
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
    if (this.propertyObject) {
      tooltipDescription = this.propertyObject.description;
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
        this.setValue(selection);
        thisBlock.propertyName = selection;
        thisBlock.propertyObject = thisBlock.getPropertyObject(selection);
        thisBlock.setTypeCheck();
        if (thisBlock.propertyObject) {
          thisBlock.setTooltip(thisBlock.propertyObject.description);
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
          .setAlign(Blockly.ALIGN_RIGHT);
      }
    } else { //this.setOrGet == "set"
      //a notches for set block
      this.setPreviousStatement(true);
      this.setNextStatement(true);
      if(!this.isGeneric) {
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
          .setAlign(Blockly.ALIGN_RIGHT);

        this.appendValueInput("VALUE")
          .appendField(Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO)
          .setAlign(Blockly.ALIGN_RIGHT);
      }
    }

    if (oldInput) {
      this.getInput('VALUE').init();
      Blockly.Mutator.reconnect(oldInput.outputConnection, this, 'VALUE');
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

    this.errors = [{name:"checkIfUndefinedBlock"},{name:"checkIsInDefinition"},{name:"checkComponentNotExistsError"}];

    if (thisBlock.propertyObject && this.propertyObject.deprecated === "true" && this.workspace === Blockly.mainWorkspace) {
      // [lyn, 2015/12/27] mark deprecated properties as bad
      this.badBlock();
      this.setDisabled(true);
    }

    this.verify();

    for (var i = 0, input; input = this.inputList[i]; i++) {
      input.init();
    }

    this.rendered = oldRendered;
  },

  setTypeCheck : function() {

    var inputOrOutput = Blockly.Blocks.Utilities.OUTPUT;
    if(this.setOrGet == "set") {
      inputOrOutput = Blockly.Blocks.Utilities.INPUT;
    }

    var newType = this.getPropertyBlocklyType(this.propertyName,inputOrOutput);
    // this will disconnect the block if the new outputType doesn't match the
    // socket the block is plugged into
    if(this.setOrGet == "get") {
      this.outputConnection.setCheck(newType);
    } else {
      this.getInput("VALUE").connection.setCheck(newType);
    }
  },

  getPropertyBlocklyType : function(propertyName,inputOrOutput) {
    var yailType = "any"; // necessary for undefined propertyObject
    if (this.getPropertyObject(propertyName)) {
      yailType = this.getPropertyObject(propertyName).type;
    }
    return Blockly.Blocks.Utilities.YailTypeToBlocklyType(yailType,inputOrOutput);
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
      Blockly.Blocks.Utilities.renameCollapsed(this, 0);
      return true;
    }
    return false;
  },
  typeblock : function(){
    var componentDb = Blockly.mainWorkspace.getComponentDatabase();
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
      var setters = componentDb.getSetterNamesForType(component.typeName),
          getters = componentDb.getGetterNamesForType(component.typeName),
          k;
      for(k=0;k<setters.length;k++) {
        pushBlock(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET, 'set', setters[k],
          component.typeName, component.name, false);
      }
      for(k=0;k<getters.length;k++) {
        pushBlock('', 'get', getters[k], component.typeName, component.name, false);
      }
      for(k=0;k<setters.length;k++) {
        pushGenericBlock(Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET, 'set', setters[k],
          component.typeName);
      }
      for(k=0;k<getters.length;k++) {
        pushGenericBlock('', 'get', getters[k], component.typeName);
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
      if (this.propertyObject) {
        this.setTooltip(this.propertyObject.description); // update the tooltipDescription, if block is defined
      }
    } else {
      this.badBlock(true);
    }

  },

  customContextMenu: function(options) {
    Blockly.ComponentBlock.addGenericOption(this, options);
    Blockly.Block.prototype.customContextMenu.call(this, options);
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
    var mode = this.typeName === "Form" ? "Screen" : this.typeName;
    return Blockly.ComponentBlock.HELPURLS[mode];
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
    this.setOutput(true, [this.typeName,"COMPONENT"]);
    this.errors = [{name:"checkIfUndefinedBlock"},{name:"checkComponentNotExistsError"}];
  },
  // Renames the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText(this.instanceName);
      this.componentDropDown.setValue(this.instanceName);
      Blockly.Blocks.Utilities.renameCollapsed(this, 0);
      return true;
    }
    return false;
  },

  typeblock : function(){
    var componentDb = Blockly.mainWorkspace.getComponentDatabase();
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
  var componentDropDown = new Blockly.FieldDropdown([["",""]]);
  componentDropDown.menuGenerator_ = function(){ return block.getTopWorkspace().getComponentDatabase().getComponentNamesByType(block.typeName); };
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
  "Polygon": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL,
  "Rectangle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL,
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
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
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
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL,
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
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
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
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_PROPERTIES_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
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
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL,
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
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
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
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_EVENTS_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
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
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL,
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
  "Circle": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL,
  "FeatureCollection": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL,
  "LineString": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL,
  "Marker": Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL,
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
  "GyroscopeSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_METHODS_HELPURL,
  "LocationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL,
  "OrientationSensor": Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL,
  "NearField": Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL,
  "Pedometer": Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL,
  "ProximitySensor": Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL,
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
  "SpeechRecognizer": Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL,
  "TextToSpeech": Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL,
  "TinyWebDB": Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL,
  "Web": Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL,
  "File": Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL,
  "FusiontablesControl": Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL,
  "GameClient": Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL,
  "SoundRecorder": Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL,
  "Voting": Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL,
  "WebViewer": Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL
};
