// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for creating the different kinds of blocks for App Inventor 
 * components: events, methods, property getters and setters, component
 * 
 * TODO(sharon): add support for generic component blocks (per component type)
 * 
 * @author sharon@google.com (Sharon Perl)
 */

Blockly.ComponentBlock = {};

/* 
 * All component blocks have category=='Component'. In addition to the standard blocks fields,
 * all component blocks have a field instanceName whose value is the name of their component. For
 * example, the blocks representing a Button1.Click event has instanceName=='Button1'.
 */ 

/**
 * Block Colors Hues (See blockly.js for Saturation and Value - fixed for 
 * all blocks)
 */
Blockly.ComponentBlock.COLOUR_EVENT = 31;
Blockly.ComponentBlock.COLOUR_METHOD = 270;
Blockly.ComponentBlock.COLOUR_GETSET = 151;
Blockly.ComponentBlock.COLOUR_COMPONENT = 180;

// TODO(sharon): add tooltips, I18N for labels

/**
 * Create an event block of the given type for a component with the given 
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.event = function(eventType, instanceName) {
  this.category = 'Component',
  this.helpUrl = 'http://foo', // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);
    this.appendTitle('when');
    this.appendTitle(instanceName + '.' + eventType.name); 
    // TODO: implement event parameters
    /*
    for (var i = 0, param; param = eventType.params[i]; i++) {
      this.appendInput('', Blockly.LOCAL_VARIABLE, this.paramName(i)).setText(param.name); 
    }
    */
    this.appendInput('do', Blockly.NEXT_STATEMENT, "DO");
    this.setPreviousStatement(false);
    this.setNextStatement(false);
  },
  this.getVars = function() {
    var varList = [];
    for (var i = 0, input; input = this.getInputVariable(this.paramName(i)); i++) {
      varList.push(input);
    }
    return varList;
  },
  this.renameVar = function(oldName, newName) {
    for (var i = 0, param = this.paramName(i), input 
         ; input = this.getInputVariable(param)
         ; i++, param = this.paramName(i)) {
      if (Blockly.Names.equals(oldName, input)) {
        this.setInputVariable(param, newName);
      }
    }
  },
  this.paramName = function(i) {
    return "PARAM" + i;
  };
};

/**
 * Create a method block of the given type for a component with the given instance name. methodType
 * is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.method = function(methodType, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.params = [];
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendTitle('call');
    this.appendTitle(instanceName + '.' + methodType.name);
    for (var i = 0, param; param = methodType.params[i]; i++) {
      this.appendInput(param.name, Blockly.INPUT_VALUE, "ARG" + i);
      this.params.push(param.name);
    }
    if (methodType.returnType) {
      this.setOutput(true);
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
  };
}

/**
 * Create a property getter block for a component with the given 
 * instance name. propNames is the list of property names to appear in the 
 * dropdown menu for the getter block.
 */
Blockly.ComponentBlock.getter = function(propNames, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle(instanceName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    this.setOutput(true);
  };
};

/**
 * Create a property setter block for a component with the given 
 * instance name. propNames is the list of property names to appear in the 
 * dropdown menu for the setter block.
 */
Blockly.ComponentBlock.setter = function(propNames, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle('set');
    this.appendTitle(instanceName + '.');
    var dropdown = new Blockly.FieldDropdown(function() {
      return propNames;
    });
    this.appendTitle(dropdown, "PROP");
    this.appendTitle('to');
    this.appendInput('', Blockly.INPUT_VALUE, "VALUE");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  };
};

/**
 * Create a component (object) block for a component with the given 
 * instance name. 
 */
Blockly.ComponentBlock.component = function(instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.appendTitle('component');
    this.appendTitle(instanceName);
    this.setOutput(true);
  };
};

