//Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

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
Blockly.ComponentBlock.COLOUR_EVENT = Blockly.CONTROL_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_METHOD = Blockly.PROCEDURE_CATEGORY_HUE;
Blockly.ComponentBlock.COLOUR_GETSET = [67, 153, 112];
Blockly.ComponentBlock.COLOUR_COMPONENT = [67, 153, 112];

//TODO(): add I18N

/**
 * Create an event block of the given type for a component with the given
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.event = function(eventType, instanceName, typeName) {
  this.category = 'Component',
  this.blockType = 'event',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.EVENTS_HELPURLS[mode];
    },
  this.instanceName = instanceName;
  this.eventType = eventType;
  this.typeName = typeName;

  // Initializes an event BlocklyBlock
  // This is called by the BlocklyBlock constructor where its type field is set to, e.g., 'Button1_Click'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        var oldValue = this.getValue();
        this.block.rename(oldValue, value);
      }

    };

    this.appendDummyInput().appendTitle('when ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.' + this.eventType.name);
    this.componentDropDown.setValue(this.instanceName);
    
    if(eventType.params.length != 0){
      var paramInput = this.appendDummyInput();
    }

    // TODO: implement event callback parameters.  Need to figure out how to do procedures and 
    // make callback parameters consistent with that.
    var paramLength = eventType.params.length;
    for (var i = 0, param; param = eventType.params[i]; i++) {
      paramInput.appendTitle(param.name + (i != paramLength -1 ? "," : ""));
    }
    
    this.appendStatementInput("DO").appendTitle('do');
    Blockly.Language.setTooltip(this, eventType.description);
    this.setPreviousStatement(false);
    this.setNextStatement(false);

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        this.componentDropDown.setValue(this.instanceName);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.eventType.name, 'COLLAPSED_TEXT');
  };

  this.getVars = function() {
    var varList = [];
    for (var i = 0, input; input = this.getInputVariable(this.paramName(i)); i++) {
      varList.push(input);
    }
    return varList;
  };

  this.getVarString = function() {
    var varString = "";
    for (var i = 0, param; param = this.eventType.params[i]; i++) {
      if(i != 0){
        varString += " ";
      }
      varString += param.name;
    }
    return varString;
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
  this.typeblock = [{ translatedName: this.instanceName + '.' + this.eventType.name }];
  this.prepareCollapsedText = function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName + '.' + this.eventType.name);
  };
};

/**
 * Create a method block of the given type for a component with the given instance name. methodType
 * is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.method = function(methodType, instanceName, typeName) {
  this.category = 'Component';
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.METHODS_HELPURLS[mode];
    },
  this.instanceName = instanceName;
  this.methodType = methodType;
  this.typeName = typeName;

  if (methodType.returnType) {
    this.blockType = 'methodwithreturn';
  } else {
    this.blockType = 'methodnoreturn';
  }

  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  paramTypes = [];
  this.yailTypes = [];
  for (var i = 0, param; param = methodType.params[i]; i++) {
    this.params.push(param.name);
    // param.type is a yail type, and so must be converted to a Blockly type
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type,Blockly.Language.INPUT));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        this.setValue(value);
        this.block.instanceName = value;
      }

    };

    this.appendDummyInput().appendTitle('call ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.' + this.methodType.name);
    this.componentDropDown.setValue(this.instanceName);

    Blockly.Language.setTooltip(this, this.methodType.description);
    for (var i = 0, param; param = this.params[i]; i++) {
        var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.setAlign(Blockly.ALIGN_RIGHT);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType,Blockly.Language.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText('call ' + this.instanceName + '.' + this.methodType.name);
        this.componentDropDown.setValue(this.instanceName);
        this.getTitle_('COLLAPSED_TEXT').setText(newname + '.' + this.methodType.name);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.methodType.name, 'COLLAPSED_TEXT');
  };
  this.typeblock = [{ translatedName: 'call ' + this.instanceName + '.' + this.methodType.name }];
  this.prepareCollapsedText = function(){
    //If the block was copy+pasted from another block, instanaceName is set to the original block
    if (this.getTitleValue('COMPONENT_SELECTOR') !== this.instanceName)
      this.instanceName = this.getTitleValue('COMPONENT_SELECTOR');
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName + '.' + this.methodType.name);
  };
};

/**
 * Create a generic method block of the given type for a component type with the given name.
 * methodType is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.ComponentBlock.genericMethod = function(methodType, typeName) {
  this.category = 'Component';
  this.blockType = 'genericmethod',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.HELPURLS[mode];
    },  // TODO: fix
  this.typeName = typeName;
  this.methodType = methodType;

  // Note: the params and paramTypes arrays are initialized for the language block,
  // and these will be shared with each instance of this block type in the workspace.
  this.params = [];
  paramTypes = [];
  this.yailTypes = [];
  for (var i = 0, param; param = this.methodType.params[i]; i++) {
    this.params.push(param.name);
    // param.type is a yail type, and so must be converted to a Blockly type
    paramTypes.push(Blockly.Language.YailTypeToBlocklyType(param.type,Blockly.Language.INPUT));
    this.yailTypes.push(param.type);
  }
  this.paramTypes = paramTypes;

  // Initializes a generic method BlocklyBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);
    this.appendDummyInput().appendTitle('call');
    this.appendDummyInput().appendTitle(this.typeName + '.' + this.methodType.name);

    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('for component');

    for (var i = 0, param; param = this.params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param);
      newInput.connection.setCheck(this.paramTypes[i]);
    }
    // methodType.returnType is a Yail type
    if (this.methodType.returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.methodType.returnType,Blockly.Language.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's typeNam, and revises its title
    this.rename = function(oldname, newname) {
      if (this.typeName == oldname) {
        this.typeName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('call' + this.typeName + '.' + this.methodType.name);
      }
    };
    this.appendCollapsedInput().appendTitle(this.typeName + '.' + this.methodType.name, 'COLLAPSED_TEXT');
  };
  this.typeblock = [{ translatedName: 'call ' + this.typeName + '.' + this.methodType.name}];
};


/**
 * Create a property getter block for a component with the given
 * instance name. propNames is the list of property names to appear in the
 * dropdown menu for the getter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.getter = function(propNames, propYailTypes, propTooltips, instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'getter',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.PROPERTIES_HELPURLS[mode];
    },  // TODO: fix
  this.instanceName = instanceName;
  this.typeName = typeName;

  // Initializes a getter BlocklyBlock
  // Called by the BlocklyBlock constructor where its type field is set to, e.g., 'TextBox1_getproperty'
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    this.propYailTypes = propYailTypes;
    var dropdown = new Blockly.FieldDropdown(

        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection) {this.setValue(selection);

        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.OUTPUT);
        // this will disconnect the block if the new outputType doesn't match the
        // socket the block is plugged into

        thisBlock.outputConnection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        var oldValue = this.getValue();
        this.block.rename(oldValue, value);
      }

    };


    this.appendDummyInput().appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.').appendTitle(dropdown, "PROP");
    this.componentDropDown.setValue(this.instanceName);
    // Set the initial output type and tooltip since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.OUTPUT);
    if(newType == Number || newType == String){
      newType = [Number,String];
    }
    thisBlock.setOutput(true, newType);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText(this.instanceName + '.');
        this.componentDropDown.setValue(this.instanceName);
        this.getTitle_('COLLAPSED_TEXT').setText(newname + '.' + this.getTitleValue('PROP'));
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');
  };
  var that = this;
  this.typeblock = createTypeBlock();
  function createTypeBlock(){
    var tb = [];
    goog.array.forEach(propNames, function(title){
      tb.push({
        translatedName: 'get ' + that.instanceName + '.' + title[0],
        dropDown: {
          titleName: 'PROP',
          value: title[0]
        }
      });
    });
    return tb;
  }
  this.prepareCollapsedText = function(){
    //If the block was copy+pasted from another block, instanaceName is set to the original block
    if (this.getTitleValue('COMPONENT_SELECTOR') !== this.instanceName)
      this.instanceName = this.getTitleValue('COMPONENT_SELECTOR');
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName + '.' + this.getTitleValue('PROP'));
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
  this.blockType = 'genericgetter',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.HELPURLS[mode];
    },  // TODO: fix
  this.instanceName = typeName;
  this.propYailTypes = propYailTypes;
  this.typeName = typeName;

  // Initializes a genericGetter BlocklyBlock
  // Its type field is set in the BlocklyBlock constructor
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    //    this.appendDummyInput().appendTitle(this.instanceName + '.');
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(

        function() {return propNames; },
        // change the output type and tooltip to match the new selection
        function(selection)
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.OUTPUT);
        // this will disconnect the block if the new outputType doesn't match the
        // socket the block is plugged into

        thisBlock.outputConnection.setCheck(newType);
        thisBlock.setOutput(true, newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    ); 

    this.appendDummyInput().appendTitle(this.instanceName + '.').appendTitle(dropdown, "PROP");

    // the argument input type on the COMPONENT socket is COMPONENT
    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);
    // Set the initial and tooltip type since they won't be set in the dropdown callback
    var newType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.OUTPUT);

    thisBlock.setOutput(true, newType);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Rename the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText(this.instanceName + '.');
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');
  };
  var that = this;
  this.typeblock = createTypeBlock();
  function createTypeBlock(){
    var tb = [];
    goog.array.forEach(propNames, function(title){
      tb.push({
        translatedName: 'get ' + that.typeName + '.' + title[0],
        dropDown: {
          titleName: 'PROP',
          value: title[0]
        }
      });
    });
    return tb;
  }
  this.prepareCollapsedText = function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.typeName + '.' + this.getTitleValue('PROP'));
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

Blockly.ComponentBlock.setter = function(propNames, propYailTypes, propTooltips, instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'setter',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.PROPERTIES_HELPURLS[mode];
    },  // TODO: fix
  this.instanceName = instanceName;
  this.propYailTypes = propYailTypes;
  this.propTooltips = propTooltips;
  this.typeName = typeName;

  // Initializes a setter BlocklyBlock
  // The type field is set in BlocklyBlock constructor to, e.g., Label1_setproperty
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
        // change the input type to match the new selection
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.INPUT);
        // this will set the socket arg type and also disconnect any plugged in block
        // where the type doesn't match the socket type

        thisBlock.getInput("VALUE").connection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        var oldValue = this.getValue();
        this.block.rename(oldValue, value);
      }

    };

    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.INPUT);
    var valueInput = this.appendValueInput("VALUE", initialArgType)
    .appendTitle('set ')
    .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
    .appendTitle('.').appendTitle(dropdown, "PROP").appendTitle(' to');
    valueInput.connection.setCheck(initialArgType);
    this.componentDropDown.setValue(this.instanceName);
    Blockly.Language.setTooltip(
        thisBlock,
        Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;

    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName =  newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText('set ' + this.instanceName + '.');
        this.componentDropDown.setValue(this.instanceName);
        this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName+ '.' + this.getTitleValue('PROP'));
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');
  };
  var that = this;
  this.typeblock = createTypeBlock();
  function createTypeBlock(){
    var tb = [];
    goog.array.forEach(propNames, function(title){
      tb.push({
        translatedName: 'set ' + that.instanceName + '.' + title[0],
        dropDown: {
          titleName: 'PROP',
          value: title[0]
        }
      });
    });
    return tb;
  }
  this.prepareCollapsedText = function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName + '.' + this.getTitleValue('PROP'));
  };
};

/**
 * Create a generic property setter block for a component with the given
 * type name. propNames is the list of property names to appear in the
 * dropdown menu for the setter block.
 * propYailTypes is a table that maps each property name to the corresponding
 * Yail type of the property
 * propTooltips is a table  that maps each property name to the corresponding
 * tooltip for the the property
 */

Blockly.ComponentBlock.genericSetter = function(propNames, propYailTypes, propTooltips, typeName) {
  this.category = 'Component';
  this.blockType = 'genericsetter',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.HELPURLS[mode];
    },  // TODO: fix
  this.typeName = typeName;
  this.propYailTypes = propYailTypes;

  // Initializes a generic setter BlocklyBlock
  this.init = function() {
    var thisBlock = this;
    this.setColour(Blockly.ComponentBlock.COLOUR_GETSET);
    var dropdown = new Blockly.FieldDropdown(
        function() {return propNames; },
        function(selection)
        // change the input type to match the new selection
        {this.setValue(selection);
        var newType = Blockly.ComponentBlock.getCurrentArgType(this, propNames, propYailTypes,Blockly.Language.INPUT);
        // this will set the socket arg type and also disconnect any plugged in block
        // where the type doesn't match the socket type

        thisBlock.getInput("VALUE").connection.setCheck(newType);
        Blockly.Language.setTooltip(
            thisBlock,
            Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
        }
    );

    this.appendDummyInput().appendTitle('set ' +  this.typeName + '.').appendTitle(dropdown, "PROP");
    // the argument input type on the COMPONENT socket is COMPONENT
    var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('of component').setAlign(Blockly.ALIGN_RIGHT);

    var initialArgType = Blockly.ComponentBlock.getCurrentArgType(dropdown, propNames, propYailTypes,Blockly.Language.INPUT);
    var valueInput = this.appendValueInput("VALUE").appendTitle('to').setAlign(Blockly.ALIGN_RIGHT);
    valueInput.connection.setCheck(initialArgType);

    Blockly.Language.setTooltip(thisBlock, Blockly.ComponentBlock.getCurrentTooltip(dropdown, propNames, propTooltips));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    this.mutationToDom = Blockly.ComponentBlock.componentPropertyMutationToDom;
    this.domToMutation = Blockly.ComponentBlock.componentPropertyDomToMutation;
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's typeName, and revises its title
    this.rename = function(oldname, newname) {
      if (this.typeName == oldname) {
        this.typeName = newname;
        var title = this.inputList[0].titleRow[0];
        title.setText('set ' + this.typeName + '.');
      }
    };
    this.appendCollapsedInput().appendTitle(this.typeName + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');
  };
  var that = this;
  this.typeblock = createTypeBlock();
  function createTypeBlock(){
    var tb = [];
    goog.array.forEach(propNames, function(title){
      tb.push({
        translatedName: 'set ' + that.typeName + '.' + title[0],
        dropDown: {
          titleName: 'PROP',
          value: title[0]
        }
      });
    });
    return tb;
  }
  this.prepareCollapsedText = function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.typeName + '.' + this.getTitleValue('PROP'));
  };
};

//Get the Blockly type of the argument for the property name that is
//selected with the dropdown
Blockly.ComponentBlock.getCurrentArgType = function (fieldDropdown, propNames, propYailTypes,inputOrOutput) {
  var propertyName = fieldDropdown.getValue();
  var yailType = propYailTypes[propertyName];
  var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType,inputOrOutput);
  return blocklyType;
}


// Save the yail type of the property socket
Blockly.ComponentBlock.componentPropertyMutationToDom = function() {
  var propertyName = this.getTitleValue("PROP");
  var yailType = this.propYailTypes[propertyName]
  var container = document.createElement('mutation');
  container.setAttribute('yailtype', yailType);
  return container;
}

// Restore the blockly type of the property socket from the yail type.
Blockly.ComponentBlock.componentPropertyDomToMutation = function(xmlElement) {
  var yailType = xmlElement.getAttribute('yailtype');
  if(yailType) {
    var inputOrOutput = (this.blockType == "setter" || this.blockType == "genericsetter" ? Blockly.Language.INPUT : Blockly.Language.OUTPUT);

    var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType,inputOrOutput);
    if(inputOrOutput == Blockly.Language.OUTPUT){
      this.outputConnection.setCheck(blocklyType);
    } else {
      this.getInput("VALUE").connection.setCheck(blocklyType);
    }
  }
}

//Get the tooltip for the property name that is
//selected with the dropdown
Blockly.ComponentBlock.getCurrentTooltip = function (fieldDropdown, propNames, propTooltips) {
  return propTooltips[fieldDropdown.getValue()];
}

/**
 * Create a component (object) block for a component with the given
 * instance name.
 */
Blockly.ComponentBlock.component = function(instanceName, typeName) {
  this.category = 'Component';
  this.blockType = 'component',
  this.helpUrl = function() {
      var mode = this.typeName;
      return Blockly.ComponentBlock.HELPURLS[mode];
    },  // TODO: fix
  this.instanceName = instanceName;
  this.typeName = typeName;

  // Initialize this type of ComponentBlock
  this.init = function() {
    this.setColour(Blockly.ComponentBlock.COLOUR_COMPONENT);
    this.componentDropDown = new Blockly.FieldDropdown([["",""]]);
    this.componentDropDown.block = this;
    this.componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
    this.componentDropDown.changeHandler_ = function(value){
      if (value !== null && value != "") {
        var oldValue = this.getValue();
        this.block.rename(oldValue, value);
      }

    };
    this.appendDummyInput().appendTitle(this.componentDropDown, "COMPONENT_SELECTOR");
    this.componentDropDown.setValue(this.instanceName);
    this.setOutput(true, [this.typeName,"COMPONENT"]);
    this.errors = [{name:"checkIsInDefinition"}];
    this.onchange = Blockly.WarningHandler.checkErrors;
    // Renames the block's instanceName, type, and reset its title
    this.rename = function(oldname, newname) {
      if (this.instanceName == oldname) {
        this.instanceName = newname;
        //var title = this.inputList[0].titleRow[0];
        //title.setText(this.instanceName);
        this.componentDropDown.setValue(this.instanceName);
        this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName);
        if (this.type.indexOf(oldname) != -1) {
          this.type = this.type.replace(oldname, newname);
        }
      }
    };
    this.appendCollapsedInput().appendTitle(this.instanceName, 'COLLAPSED_TEXT');
  };
  this.typeblock = [{ translatedName: this.instanceName }];
  this.prepareCollapsedText = function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName);
  };
};

Blockly.ComponentBlock.HELPURLS = {
  "Button": Blockly.LANG_COMPONENT_BLOCK_BUTTON_HELPURL,
  "Canvas": Blockly.LANG_COMPONENT_BLOCK_CANVAS_HELPURL,
  "CheckBox": Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL,
  "Clock": Blockly.LANG_COMPONENT_BLOCK_CLOCK_HELPURL,
  "Image": Blockly.LANG_COMPONENT_BLOCK_IMAGE_HELPURL,
  "Label": Blockly.LANG_COMPONENT_BLOCK_LABEL_HELPURL,
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKET_HELPURL,
  "PasswordTextBox": Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_HELPURL,
  "Screen": Blockly.LANG_COMPONENT_BLOCK_SCREEN_HELPURL,
  "Slider": Blockly.LANG_COMPONENT_BLOCK_SLIDER_HELPURL,
  "TextBox": Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_HELPURL,
  "TinyDB": Blockly.LANG_COMPONENT_BLOCK_TINYDB_HELPURL,
  "Camcorder": Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_HELPURL,
  "Camera": Blockly.LANG_COMPONENT_BLOCK_CAMERA_HELPURL,
  "ImagePicker": Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_HELPURL,
  "Player": Blockly.LANG_COMPONENT_BLOCK_PLAYER_HELPURL,
  "Sound": Blockly.LANG_COMPONENT_BLOCK_SOUND_HELPURL,
  "VideoPlayer": Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL,
  "Ball": Blockly.LANG_COMPONENT_BLOCK_BALL_HELPURL,
  "ImageSprite": Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL,
  "ContactPicker": Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL,
  "EmailPicker": Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL,
  "PhoneCall": Blockly.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL,
  "PhoneNumberPicker": Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL,
  "Texting": Blockly.LANG_COMPONENT_BLOCK_TEXTING_HELPURL,
  "Twitter": Blockly.LANG_COMPONENT_BLOCK_TWITTER_HELPURL,
  "AccelerometerSensor": Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL,
  "LocationSensor": Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL,
  "OrientationSensor": Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL,
  "HorizontalArrangment": Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL,
  "TableArrangement": Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL,
  "VerticalArrangement": Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL,
  "NxtColorSensor": Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL,
  "NxtDirectCommands": Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL,
  "NxtDrive": Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL,
  "NxtLightSensor": Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL,
  "NxtSoundSensor": Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL,
  "NxtTouchSensor": Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL,
  "NxtUltrasonicSensor": Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL,
  "ActivityStarter": Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL,
  "BarcodeScanner": Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL,
  "BluetoothClient": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL,
  "BluetoothServer": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL,
  "Notifier": Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL,
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNITION_HELPURL,
  "TextToSpeech": Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL,
  "TinyWebDB": Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL,
  "Web": Blockly.LANG_COMPONENT_BLOCK_WEB_HELPURL,
  "FusiontablesControl": Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL,
  "GameClient": Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL,
  "SoundRecorder": Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL,
  "Voting": Blockly.LANG_COMPONENT_BLOCK_VOTING_HELPURL,
  "WebViewer": Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL
};

Blockly.ComponentBlock.PROPERTIES_HELPURLS = {
  "Button": Blockly.LANG_COMPONENT_BLOCK_BUTTON_PROPERTIES_HELPURL,
  "Canvas": Blockly.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL,
  "CheckBox": Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_PROPERTIES_HELPURL,
  "Clock": Blockly.LANG_COMPONENT_BLOCK_CLOCK_PROPERTIES_HELPURL,
  "Image": Blockly.LANG_COMPONENT_BLOCK_IMAGE_PROPERTIES_HELPURL,
  "Label": Blockly.LANG_COMPONENT_BLOCK_LABEL_PROPERTIES_HELPURL,
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKET_PROPERTIES_HELPURL,
  "PasswordTextBox": Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_PROPERTIES_HELPURL,
  "Screen": Blockly.LANG_COMPONENT_BLOCK_SCREEN_PROPERTIES_HELPURL,
  "Slider": Blockly.LANG_COMPONENT_BLOCK_SLIDER_PROPERTIES_HELPURL,
  "TextBox": Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_PROPERTIES_HELPURL,
  "TinyDB": Blockly.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL,
  "Camcorder": Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_PROPERTIES_HELPURL,
  "Camera": Blockly.LANG_COMPONENT_BLOCK_CAMERA_PROPERTIES_HELPURL,
  "ImagePicker": Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_PROPERTIES_HELPURL,
  "Player": Blockly.LANG_COMPONENT_BLOCK_PLAYER_PROPERTIES_HELPURL,
  "Sound": Blockly.LANG_COMPONENT_BLOCK_SOUND_PROPERTIES_HELPURL,
  "VideoPlayer": Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL,
  "Ball": Blockly.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL,
  "ImageSprite": Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL,
  "ContactPicker": Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL,
  "EmailPicker": Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL,
  "PhoneCall": Blockly.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL,
  "PhoneNumberPicker": Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL,
  "Texting": Blockly.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL,
  "Twitter": Blockly.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL,
  "AccelerometerSensor": Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL,
  "LocationSensor": Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL,
  "OrientationSensor": Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL,
  "HorizontalArrangment": Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_PROPERTIES_HELPURL,
  "TableArrangement": Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_PROPERTIES_HELPURL,
  "VerticalArrangement": Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL,
  "NxtColorSensor": Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL,
  "NxtDirectCommands": Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL,
  "NxtDrive": Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL,
  "NxtLightSensor": Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL,
  "NxtSoundSensor": Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL,
  "NxtTouchSensor": Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL,
  "NxtUltrasonicSensor": Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL,
  "ActivityStarter": Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL,
  "BarcodeScanner": Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL,
  "BluetoothClient": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL,
  "BluetoothServer": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL,
  "Notifier": Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_PROPERTIES_HELPURL,
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNITION_PROPERTIES_HELPURL,
  "TextToSpeech": Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL,
  "TinyWebDB": Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL,
  "Web": Blockly.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL,
  "FusiontablesControl": Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL,
  "GameClient": Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL,
  "SoundRecorder": Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL,
  "Voting": Blockly.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL,
  "WebViewer": Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL
};

Blockly.ComponentBlock.EVENTS_HELPURLS = {
  "Button": Blockly.LANG_COMPONENT_BLOCK_BUTTON_EVENTS_HELPURL,
  "Canvas": Blockly.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL,
  "Clock": Blockly.LANG_COMPONENT_BLOCK_CLOCK_EVENTS_HELPURL,
  "CheckBox": Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_EVENTS_HELPURL,
  "Image": Blockly.LANG_COMPONENT_BLOCK_IMAGE_EVENTS_HELPURL,
  "Label": Blockly.LANG_COMPONENT_BLOCK_LABEL_EVENTS_HELPURL,
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKET_EVENTS_HELPURL,
  "PasswordTextBox": Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_EVENTS_HELPURL,
  "Screen": Blockly.LANG_COMPONENT_BLOCK_SCREEN_EVENTS_HELPURL,
  "Slider": Blockly.LANG_COMPONENT_BLOCK_SLIDER_EVENTS_HELPURL,
  "TextBox": Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_EVENTS_HELPURL,
  "TinyDB": Blockly.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL,
  "Camcorder": Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_EVENTS_HELPURL,
  "Camera": Blockly.LANG_COMPONENT_BLOCK_CAMERA_EVENTS_HELPURL,
  "ImagePicker": Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_EVENTS_HELPURL,
  "Player": Blockly.LANG_COMPONENT_BLOCK_PLAYER_EVENTS_HELPURL,
  "Sound": Blockly.LANG_COMPONENT_BLOCK_SOUND_EVENTS_HELPURL,
  "VideoPlayer": Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL,
  "Ball": Blockly.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL,
  "ImageSprite": Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL,
  "ContactPicker": Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL,
  "EmailPicker": Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL,
  "PhoneCall": Blockly.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL,
  "PhoneNumberPicker": Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL,
  "Texting": Blockly.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL,
  "Twitter": Blockly.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL,
  "AccelerometerSensor": Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL,
  "LocationSensor": Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL,
  "OrientationSensor": Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL,
  "HorizontalArrangment": Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_EVENTS_HELPURL,
  "TableArrangement": Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_EVENTS_HELPURL,
  "VerticalArrangement": Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_EVENTS_HELPURL,
  "NxtColorSensor": Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL,
  "NxtDirectCommands": Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_EVENTS_HELPURL,
  "NxtDrive": Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_EVENTS_HELPURL,
  "NxtLightSensor": Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL,
  "NxtSoundSensor": Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL,
  "NxtTouchSensor": Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL,
  "NxtUltrasonicSensor": Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL,
  "ActivityStarter": Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL,
  "BarcodeScanner": Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL,
  "BluetoothClient": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL,
  "BluetoothServer": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL,
  "Notifier": Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL,
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNITION_EVENTS_HELPURL,
  "TextToSpeech": Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL,
  "TinyWebDB": Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL,
  "Web": Blockly.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL,
  "FusiontablesControl": Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL,
  "GameClient": Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL,
  "SoundRecorder": Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL,
  "Voting": Blockly.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL,
  "WebViewer": Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL
};

Blockly.ComponentBlock.METHODS_HELPURLS = {
   "Canvas": Blockly.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL,
   "Clock": Blockly.LANG_COMPONENT_BLOCK_CLOCK_METHODS_HELPURL,
   "Image": Blockly.LANG_COMPONENT_BLOCK_IMAGE_METHODS_HELPURL,
   "Label": Blockly.LANG_COMPONENT_BLOCK_LABEL_METHODS_HELPURL,
   "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_METHODS_HELPURL,
   "PasswordTextBox": Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_METHODS_HELPURL,
   "Screen": Blockly.LANG_COMPONENT_BLOCK_SCREEN_METHODS_HELPURL,
   "Slider": Blockly.LANG_COMPONENT_BLOCK_SLIDER_METHODS_HELPURL,
   "TextBox": Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_METHODS_HELPURL,
   "TinyDB": Blockly.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL,
   "Camcorder": Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_METHODS_HELPURL,
   "Camera": Blockly.LANG_COMPONENT_BLOCK_CAMERA_METHODS_HELPURL,
   "ImagePicker": Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_METHODS_HELPURL,
   "Player": Blockly.LANG_COMPONENT_BLOCK_PLAYER_METHODS_HELPURL,
   "Sound": Blockly.LANG_COMPONENT_BLOCK_SOUND_METHODS_HELPURL,
   "VideoPlayer": Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL,
   "Ball": Blockly.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL,
   "ImageSprite": Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL,
   "ContactPicker": Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL,
   "EmailPicker": Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL,
   "PhoneCall": Blockly.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL,
   "PhoneNumberPicker": Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL,
   "Texting": Blockly.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL,
   "Twitter": Blockly.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL,
   "AccelerometerSensor": Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL,
   "LocationSensor": Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL,
   "OrientationSensor": Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL,
   "HorizontalArrangment": Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_METHODS_HELPURL,
   "TableArrangement": Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_METHODS_HELPURL,
   "VerticalArrangement": Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_METHODS_HELPURL,
   "NxtColorSensor": Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL,
   "NxtDirectCommands": Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL,
   "NxtDrive": Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL,
   "NxtLightSensor": Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL,
   "NxtSoundSensor": Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL,
   "NxtTouchSensor": Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL,
   "NxtUltrasonicSensor": Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL,
   "ActivityStarter": Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL,
   "BarcodeScanner": Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL,
   "BluetoothClient": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL,
   "BluetoothServer": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL,
   "Notifier": Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_METHODS_HELPURL,
   "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNITION_METHODS_HELPURL,
   "TextToSpeech": Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL,
   "TinyWebDB": Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL,
   "Web": Blockly.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL,
   "FusiontablesControl": Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL,
   "GameClient": Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL,
   "SoundRecorder": Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL,
   "Voting": Blockly.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL,
   "WebViewer": Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL
};
