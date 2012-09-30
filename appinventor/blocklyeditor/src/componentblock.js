// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Methods for creating the different kinds of blocks for App Inventor
 * components: events, methods, property getters and setters, component
 *
 * @author sharon@google.com (Sharon Perl)
 */

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
Blockly.ComponentBlock.COLOUR_EVENT = 31;
Blockly.ComponentBlock.COLOUR_METHOD = 270;
Blockly.ComponentBlock.COLOUR_GETSET = 151;
Blockly.ComponentBlock.COLOUR_COMPONENT = 180;


// TODO(): add I18N

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
    this.appendInput(Blockly.NEXT_STATEMENT, "DO").appendTitle('do');
    this.setPreviousStatement(false);
    this.setNextStatement(false);
  };
  this.getVars = function() {
    var varList = [];
    for (var i = 0, input; input = this.getInputVariable(this.paramName(i)); i++) {
      varList.push(input);
    }
    return varList;
  };
  this.renameVar = function(oldName, newName) {
    for (var i = 0, param = this.paramName(i), input
         ; input = this.getInputVariable(param)
         ; i++, param = this.paramName(i)) {
      if (Blockly.Names.equals(oldName, input)) {
        this.setInputVariable(param, newName);
      }
    }
  };
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
  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  this.paramTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    this.paramTypes.push(param.type);
  }
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendTitle('call ' + instanceName + '.' + methodType.name);
    for (var i = 0, param; param = methodType.params[i]; i++) {
        this.appendInput(Blockly.INPUT_VALUE, "ARG" + i).appendTitle(param.name);
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
 * Create a generic method block of the given type for a component type with the given name.
 * methodType is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.genericMethod = function(methodType, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  this.paramTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    this.paramTypes.push(param.type);
  }
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendTitle('call');
    this.appendTitle(typeName + '.' + methodType.name);
    this.appendInput('component', Blockly.INPUT_VALUE, "COMPONENT");
    for (var i = 0, param; param = methodType.params[i]; i++) {
      this.appendInput(param.name, Blockly.INPUT_VALUE, "ARG" + i);
    }
    if (methodType.returnType) {
      this.setOutput(true);
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
  };
}

// TODO(hal): Handle the case where we change the dropdown for a block already
// plugged in to an unnacceptable type.   Blocky currently throws a warning in
// this case and ends up in a slightly inconsistent state.


/**
 * Create a property getter block for a component with the given
 * instance name. propNames is the list of property names to appear in the
 * dropdown menu for the getter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.getter = function(propNames, propYailTypes, propTooltips, instanceName) {
    this.category = 'Component';
    this.helpUrl = "http://foo";  // TODO: fix
    this.init = function() {
      this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
      this.appendTitle(instanceName + '.');
      var thisBlock = this;
      var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection)
          {this.setText(selection);
          var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
          thisBlock.setOutput(true, newType);
          Blockly.Language.setTooltip(
              thisBlock,
              Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
          }
          );
    this.appendTitle(dropdown, "PROP");
    // Set the initial output type and tooltip since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    thisBlock.setOutput(true, newType);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
  };
};


/**
 * Create a generic property getter block for a component with the given
 * type name. propNames is the list of property names to appear in the
 * dropdown menu for the getter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */
Blockly.ComponentBlock.genericGetter = function(propNames, propYailTypes, propTooltips, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle(typeName + '.');
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection)
          {this.setText(selection);
          var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
          thisBlock.setOutput(true, newType);
          Blockly.Language.setTooltip(
              thisBlock,
              Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
          }
        );
    this.appendTitle(dropdown, "PROP");
    // the argument input type on the COMPONENT socket is COMPONENT
    this.appendInput(Blockly.INPUT_VALUE, "COMPONENT", "COMPONENT").appendTitle('of component');
    // Set the initial and tooltip type since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    thisBlock.setOutput(true, newType);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
  };
};


/**
 * Create a property setter block for a component with the given
 * instance name. propNames is the list of property names to appear in the
 * dropdown menu for the setter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.setter = function(propNames, propYailTypes, propTooltips, instanceName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.instanceName = instanceName;
  this.propYailTypes = propYailTypes;
  this.propTooltips = propTooltips;
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle('set');
    this.appendTitle(instanceName + '.');
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
          // change the input type to match the new selection
          {this.setText(selection);
          var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
          thisBlock.getInput("VALUE").connection.check_[0] = newType;
          Blockly.Language.setTooltip(
              thisBlock,
              Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
         }
        );
    this.appendTitle(dropdown, "PROP");
    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    this.appendInput(Blockly.INPUT_VALUE, "VALUE", initialArgType).appendTitle('to');
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  };
};


/**
 * Create A generic property setter block for a component with the given
 * type name. propNames is the list of property names to appear in the
 * dropdown menu for the setter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.genericSetter = function(propNames, propYailTypes, propTooltips, typeName) {
  this.category = 'Component';
  this.helpUrl = "http://foo";  // TODO: fix
  this.typeName = typeName;
  this.propYailTypes = propYailTypes;
  this.init = function() {
    var thisBlock = this;
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    this.appendTitle('set');
    this.appendTitle(typeName + '.');
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
          // change the input type to match the new selection
          {this.setText(selection);
          var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes);
          thisBlock.getInput("VALUE").connection.check_[0] = newType;
          Blockly.Language.setTooltip(
              thisBlock,
              Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
         }
        );
    this.appendTitle(dropdown, "PROP");
    // the argument input type on the COMPONENT socket is COMPONENT
    this.appendInput(Blockly.INPUT_VALUE, "COMPONENT", "COMPONENT").appendTitle('of component');
    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes);
    this.appendInput(Blockly.INPUT_VALUE, "VALUE", initialArgType).appendTitle('to');
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);
  };
};

// Get the Blockly type of the argument for the property name that is
// selected with the dropdown
Blockly.ComponentBlock.getCurrentArgType = function (fieldDropdown, propNames, propYailTypes) {
    var propertyName = fieldDropdown.getValue();
    var yailType = propYailTypes[propertyName];
    var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType);
    return blocklyType;
}

// Get the tooltip for the property name that is
// selected with the dropdown
Blockly.ComponentBlock.getCurrentTooltip = function (fieldDropdown, propNames, propTooltips) {
    return propTooltips[fieldDropdown.getValue()];
}

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
    this.appendTitle(instanceName);
    this.setOutput(true, "COMPONENT");
  };
};

