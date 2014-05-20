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
Blockly.ComponentBlock.COLOUR_GET = [67, 153, 112];
Blockly.ComponentBlock.COLOUR_SET = [38, 102, 67];
Blockly.ComponentBlock.COLOUR_COMPONENT = [67, 153, 112];

//TODO(): add I18N

/**
 * Create an event block of the given type for a component with the given
 * instance name. eventType is one of the "events" objects in a typeJsonString
 * passed to Blockly.Component.add.
 */
Blockly.Language.component_event = {
  category : 'Component',
  blockType : 'event',

  mutationToDom : function() {

    var container = document.createElement('mutation');
    container.setAttribute('component_type', this.typeName);
    container.setAttribute('instance_name', this.instanceName);//instance name not needed
    container.setAttribute('event_name', this.eventName);
    if (!this.horizontalParameters) {
      container.setAttribute('vertical_parameters', "true"); // Only store an element for vertical
                                                             // The absence of this attribute means horizontal.
    }
    return container;
  },

  domToMutation : function(xmlElement) {

    this.typeName = xmlElement.getAttribute('component_type');
    this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    this.eventName = xmlElement.getAttribute('event_name');
    var horizParams = xmlElement.getAttribute('vertical_parameters') !== "true";

     // Orient parameters horizontally by default

    this.setColour(Blockly.ComponentBlock.COLOUR_EVENT);

    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    this.componentDropDown.setValue(this.instanceName);

    this.appendDummyInput('WHENTITLE').appendTitle('when ')
        .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendTitle('.' + this.eventName);
    this.componentDropDown.setValue(this.instanceName);

    this.setParameterOrientation(horizParams);

    Blockly.Language.setTooltip(this, this.getEventTypeObject().description);

    this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.getEventTypeObject().name, 'COLLAPSED_TEXT');

    this.setPreviousStatement(false);
    this.setNextStatement(false);

    // [lyn, 12/23/2013] checkIsInDefinition is bogus check that can never happen!
    // this.errors = [{name:"checkIsInDefinition"}];

    // [lyn, 12/23/2013] Move this out of domToMutation into top-level component_event
    // this.onchange = Blockly.WarningHandler.checkErrors;

  },
  // [lyn, 10/24/13] Allow switching between horizontal and vertical display of arguments
  // Also must create flydown params and DO input if they don't exist.
  setParameterOrientation: function(isHorizontal) {
    var params = this.getParameters();
    var oldDoInput = this.getInput("DO");
    if (!oldDoInput || (isHorizontal !== this.horizontalParameters && params.length > 0)) {
      this.horizontalParameters = isHorizontal;

      var bodyConnection = null;
      if (oldDoInput) {
        var bodyConnection = oldDoInput.connection.targetConnection; // Remember any body connection
      }
      if (this.horizontalParameters) { // Replace vertical by horizontal parameters

        if (oldDoInput) {
          // Remove inputs after title ...
          for (var i = 0; i < params.length; i++) {
            this.removeInput('VAR' + i); // vertical parameters
          }
          this.removeInput('DO');
        }

        // .. and insert new ones:
        if (params.length > 0) {
          var paramInput = this.appendDummyInput('PARAMETERS')
                               .appendTitle(" ")
                               .setAlign(Blockly.ALIGN_LEFT);
          for (var i = 0, param; param = params[i]; i++) {
            paramInput.appendTitle(new Blockly.FieldParameterFlydown(param.name, false), // false means not editable
                                   'VAR' + i)
                      .appendTitle(" ");
          }
        }

        var newDoInput = this.appendStatementInput("DO").appendTitle('do'); // Hey, I like your new do!
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
        for (var i = 0, param; param = params[i]; i++) {
          this.appendDummyInput('VAR' + i)
              .appendTitle(new Blockly.FieldParameterFlydown(param.name, false),
                           'VAR' + i)
              .setAlign(Blockly.ALIGN_RIGHT);
        }
        var newDoInput = this.appendStatementInput("DO").appendTitle('do');
        if (bodyConnection) {
          newDoInput.connection.connect(bodyConnection);
        }
      }
    }
  },
  // Return a list of parameter names
  getParameters: function () {
    return this.getEventTypeObject().params;
  },
  // Renames the block's instanceName and type (set in BlocklyBlock constructor), and revises its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      this.componentDropDown.setValue(this.instanceName);
      this.prepareCollapsedText();
    }
  },
  renameVar: function(oldName, newName) {
    for (var i = 0, param = 'VAR' + i, input
        ; input = this.getTitleValue(param)
        ; i++, param = 'VAR' + i) {
      if (Blockly.Names.equals(oldName, input)) {
        this.setTitleValue(param, newName);
      }
    }
  },
  helpUrl : function() {
    var mode = this.typeName;
    return Blockly.ComponentBlock.EVENTS_HELPURLS[mode];
  },

  getVars: function() {
    var varList = [];
    for (var i = 0, input; input = this.getTitleValue('VAR' + i); i++) {
      varList.push(input);
    }
    return varList;
  },

  getVarString: function() {
    var varString = "";
    for (var i = 0, param; param = this.getTitleValue('VAR' + i); i++) {
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

  getEventTypeObject : function() {
    return Blockly.ComponentTypes[this.typeName].eventDictionary[this.eventName];
  },
  typeblock : function(){
    var tb = [];
    var instanceNames = Blockly.ComponentInstances.getInstanceNames();
    var typeName;
    var eventObjects;

    for(var i=0;i<instanceNames.length;i++) {
      typeName = Blockly.ComponentInstances[instanceNames[i]].typeName;

      eventObjects = Blockly.ComponentTypes[typeName].componentInfo.events;
      for(var k=0;k<eventObjects.length;k++) {
        tb.push({
          translatedName: 'when ' + instanceNames[i] + '.' + eventObjects[k].name,
          mutatorAttributes: {
            component_type: typeName,
            instance_name: instanceNames[i],
            event_name: eventObjects[k].name
          }
        });
      }

    }

    return tb;
  },
  prepareCollapsedText : function(){
    this.getTitle_('COLLAPSED_TEXT').setText(this.instanceName + '.' + this.getEventTypeObject().name);
  },
  customContextMenu: function (options) {
    Blockly.FieldParameterFlydown.addHorizontalVerticalOption(this, options);
  },
  // [lyn, 12/31/2013] Next two fields used to check for duplicate component event handlers
  errors: [{name:"checkIfIAmADuplicateEventHandler"}],
  onchange: Blockly.WarningHandler.checkErrors
};

/**
 * Create a method block of the given type for a component with the given instance name. methodType
 * is one of the "methods" objects in a typeJsonString passed to Blockly.Component.add.
 */
Blockly.Language.component_method = {
  category : 'Component',
  helpUrl : function() {
      var mode = this.typeName;
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
    return container;
  },

  domToMutation : function(xmlElement) {

    this.typeName = xmlElement.getAttribute('component_type');
    this.methodName = xmlElement.getAttribute('method_name');
    var isGenericString = xmlElement.getAttribute('is_generic');
    this.isGeneric = (isGenericString == "true" ? true : false);
    if(!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    }

    this.setColour(Blockly.ComponentBlock.COLOUR_METHOD);

    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);
    //for non-generic blocks, set the value of the component drop down
    if(!this.isGeneric) {
      this.componentDropDown.setValue(this.instanceName);
    }

    if(!this.isGeneric) {
      this.appendDummyInput().appendTitle('call ')
        .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
        .appendTitle('.' + this.getMethodTypeObject().name);
      this.componentDropDown.setValue(this.instanceName);
    } else {
      this.appendDummyInput().appendTitle('call ' + this.typeName + '.' + this.getMethodTypeObject().name);
      var compInput = this.appendValueInput("COMPONENT").setCheck(this.typeName).appendTitle('for component').setAlign(Blockly.ALIGN_RIGHT);;
    }
    Blockly.Language.setTooltip(this, this.getMethodTypeObject().description);
    for (var i = 0, param; param = this.getMethodTypeObject().params[i]; i++) {
      var newInput = this.appendValueInput("ARG" + i).appendTitle(param.name);
      newInput.setAlign(Blockly.ALIGN_RIGHT);
      var blockyType = Blockly.Language.YailTypeToBlocklyType(param.type,Blockly.Language.INPUT)
      newInput.connection.setCheck(blockyType);
    }
    // methodType.returnType is a Yail type
    if (this.getMethodTypeObject().returnType) {
      this.setOutput(true, Blockly.Language.YailTypeToBlocklyType(this.getMethodTypeObject().returnType,Blockly.Language.OUTPUT));
    } else {
      this.setPreviousStatement(true);
      this.setNextStatement(true);
    }
    this.errors = [{name:"checkIsInDefinition"}];

    if(!this.isGeneric) {
      this.appendCollapsedInput().appendTitle(this.instanceName + '.' + this.getMethodTypeObject().name, 'COLLAPSED_TEXT');
    } else {
      this.appendCollapsedInput().appendTitle(this.typeName + '.' + this.getMethodTypeObject().name, 'COLLAPSED_TEXT');
    }

  },
  // Rename the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText('call ' + this.instanceName + '.' + this.methodType.name);
      this.componentDropDown.setValue(this.instanceName);
      this.getTitle_('COLLAPSED_TEXT').setText(newname + '.' + this.methodName);
      if (this.type.indexOf(oldname) != -1) {
        this.type = this.type.replace(oldname, newname);
      }
    }
  },
  getMethodTypeObject : function() {
    return Blockly.ComponentTypes[this.typeName].methodDictionary[this.methodName];
  },

  //this.typeblock = [{ translatedName: 'call ' + this.instanceName + '.' + this.methodType.name }];
  typeblock : function(){
    var tb = [];
    var instanceNames = Blockly.ComponentInstances.getInstanceNames();
    var typeName;
    var methodObjects;
    var typeNameDict = {};
    for(var i=0;i<instanceNames.length;i++) {
      typeName = Blockly.ComponentInstances[instanceNames[i]].typeName;
      typeNameDict[typeName] = true;
      methodObjects = Blockly.ComponentTypes[typeName].componentInfo.methods;
      for(var k=0;k<methodObjects.length;k++) {
        tb.push({
          translatedName: 'call ' + instanceNames[i] + '.' + methodObjects[k].name,
          mutatorAttributes: {
            component_type: typeName,
            instance_name: instanceNames[i],
            method_name: methodObjects[k].name,
            is_generic: "false"
          }
        });
      }

    }
    for(var componentType in typeNameDict) {
      methodObjects = Blockly.ComponentTypes[componentType].componentInfo.methods;
      for(var k=0;k<methodObjects.length;k++) {
        tb.push({
          translatedName: 'call ' + componentType + '.' + methodObjects[k].name,
          mutatorAttributes: {
            component_type: componentType,
            method_name: methodObjects[k].name,
            is_generic: "true"
          }
        });
      }
    }
    return tb;
  },
  prepareCollapsedText : function(){
    //If the block was copy+pasted from another block, instanaceName is set to the original block
    if (this.getTitleValue('COMPONENT_SELECTOR') !== this.instanceName)
      this.instanceName = this.getTitleValue('COMPONENT_SELECTOR');
    this.getTitle_('COLLAPSED_TEXT').setText(this.typeName + '.' + this.methodName);
  }
};


/**
 * Create a property getter or setter block for a component with the given
 * instance name. Blocks can also be generic or not, depending on the
 * values of the attribute in the mutators.
 */

Blockly.Language.component_set_get = {
  category : 'Component',
  //this.blockType = 'getter',
  helpUrl : function() {
    var mode = this.typeName;
    return Blockly.ComponentBlock.PROPERTIES_HELPURLS[mode];
  },  // TODO: fix

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

    this.typeName = xmlElement.getAttribute('component_type');
    this.setOrGet = xmlElement.getAttribute('set_or_get');
    this.propertyName = xmlElement.getAttribute('property_name');
    var isGenericString = xmlElement.getAttribute('is_generic');
    this.isGeneric = (isGenericString == "true" ? true : false);
    if(!this.isGeneric) {
      this.instanceName = xmlElement.getAttribute('instance_name');//instance name not needed
    }
    if(this.setOrGet == "set"){
      this.setColour(Blockly.ComponentBlock.COLOUR_SET);
    } else {
      this.setColour(Blockly.ComponentBlock.COLOUR_GET);
    }

    var thisBlock = this;
    var dropdown = new Blockly.FieldDropdown(

      function() {return thisBlock.getPropertyDropDownList(); },
      // change the output type and tooltip to match the new selection
      function(selection) {
        this.setValue(selection);
        thisBlock.propertyName = selection;
        thisBlock.setTypeCheck();

        Blockly.Language.setTooltip(
          thisBlock,
          thisBlock.getPropertyObject(thisBlock.propertyName).description);
      }
    );

    this.componentDropDown = Blockly.ComponentBlock.createComponentDropDown(this);

    if(this.setOrGet == "get") {
      //add output plug for get blocks
      this.setOutput(true);

      if(!this.isGeneric) {
        //non-generic get
        this.appendDummyInput()
          .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
          .appendTitle('.')
          .appendTitle(dropdown, "PROP");
      } else {
        //generic get
        this.appendDummyInput()
          .appendTitle(this.typeName + '.')
          .appendTitle(dropdown, "PROP");

        this.appendValueInput("COMPONENT")
          .setCheck(this.typeName)
          .appendTitle('of component')
          .setAlign(Blockly.ALIGN_RIGHT);
      }
    } else { //this.setOrGet == "set"
      //a notches for set block
      this.setPreviousStatement(true);
      this.setNextStatement(true);
      if(!this.isGeneric) {
        this.appendValueInput("VALUE")
          .appendTitle('set ')
          .appendTitle(this.componentDropDown, "COMPONENT_SELECTOR")
          .appendTitle('.')
          .appendTitle(dropdown, "PROP")
          .appendTitle(' to');
      } else {
        //generic set
        this.appendDummyInput()
          .appendTitle('set ' +  this.typeName + '.')
          .appendTitle(dropdown, "PROP");

        this.appendValueInput("COMPONENT")
          .setCheck(this.typeName)
          .appendTitle('of component')
          .setAlign(Blockly.ALIGN_RIGHT);

        this.appendValueInput("VALUE")
          .appendTitle('to')
          .setAlign(Blockly.ALIGN_RIGHT);
      }
    }

    //for non-generic blocks, set the value of the component drop down
    if(!this.isGeneric) {
      this.componentDropDown.setValue(this.instanceName);
    }
    //set value of property drop down
    this.setTitleValue(this.propertyName,"PROP");

    //add appropriate type checking to block
    this.setTypeCheck();

    Blockly.Language.setTooltip(
        this,
        this.getPropertyObject(this.propertyName).description);

    this.errors = [{name:"checkIsInDefinition"}];

    this.appendCollapsedInput().appendTitle( (this.isGeneric ? this.typeName : this.instanceName) + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');

    //this.typeblock = this.createTypeBlock();
  },

  setTypeCheck : function() {

    var inputOrOutput = Blockly.Language.OUTPUT;
    if(this.setOrGet == "set") {
      inputOrOutput = Blockly.Language.INPUT;
    }

    var newType = this.getPropertyBlocklyType(this.propertyName,inputOrOutput)
    // this will disconnect the block if the new outputType doesn't match the
    // socket the block is plugged into
    if(this.setOrGet == "get") {
      this.outputConnection.setCheck(newType);
    } else {
      this.getInput("VALUE").connection.setCheck(newType);
    }
  },

  getPropertyBlocklyType : function(propertyName,inputOrOutput) {
    var yailType = this.getPropertyObject(propertyName).type;
    var blocklyType = Blockly.Language.YailTypeToBlocklyType(yailType,inputOrOutput);
    return blocklyType;
  },
  getPropertyDropDownList : function() {
    var dropDownList = [];
    var propertyNames = [];
    if(this.setOrGet == "set") {
      propertyNames = Blockly.ComponentTypes[this.typeName].setPropertyList;
    } else {
      propertyNames = Blockly.ComponentTypes[this.typeName].getPropertyList;
    }

    for(var i=0;i<propertyNames.length;i++) {
      dropDownList.push([propertyNames[i],propertyNames[i]]);
    }
    return dropDownList;
  },
  getPropertyObject : function(propertyName) {
    return Blockly.ComponentTypes[this.typeName].properties[propertyName];
  },
  // Rename the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
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
  },
  typeblock : function(){
    var tb = [];
    var instanceNames = Blockly.ComponentInstances.getInstanceNames();
    var typeName;
    var propertyNames;
    var typeNameDict = {};
    for(var i=0;i<instanceNames.length;i++) {
      typeName = Blockly.ComponentInstances[instanceNames[i]].typeName;
      typeNameDict[typeName] = true;
      propertyNames = Blockly.ComponentTypes[typeName].setPropertyList;
      for(var k=0;k<propertyNames.length;k++) {
        tb.push({
          translatedName: 'set ' + instanceNames[i] + '.' + propertyNames[k],
          mutatorAttributes: {
            set_or_get: 'set',
            component_type: typeName,
            instance_name: instanceNames[i],
            property_name: propertyNames[k],
            is_generic: "false"
          }
        });
      }
      propertyNames = Blockly.ComponentTypes[typeName].getPropertyList;
      for(var k=0;k<propertyNames.length;k++) {
        tb.push({
          translatedName: instanceNames[i] + '.' + propertyNames[k],
          mutatorAttributes: {
            set_or_get: 'get',
            component_type: typeName,
            instance_name: instanceNames[i],
            property_name: propertyNames[k],
            is_generic: "false"
          }
        });
      }

    }
    for(var componentType in typeNameDict) {
      propertyNames = Blockly.ComponentTypes[componentType].setPropertyList;
      for(var k=0;k<propertyNames.length;k++) {
        tb.push({
          translatedName: 'set ' + componentType + '.' + propertyNames[k],
          mutatorAttributes: {
            set_or_get: 'set',
            component_type: componentType,
            property_name: propertyNames[k],
            is_generic: "true"
          }
        });
      }
      propertyNames = Blockly.ComponentTypes[componentType].getPropertyList;
      for(var k=0;k<propertyNames.length;k++) {
        tb.push({
          translatedName: componentType + '.' + propertyNames[k],
          mutatorAttributes: {
            set_or_get: 'get',
            component_type: componentType,
            property_name: propertyNames[k],
            is_generic: "true"
          }
        });
      }

    }

    return tb;
  },
  prepareCollapsedText : function(){
    //If the block was copy+pasted from another block, instanaceName is set to the original block
    if (this.getTitleValue('COMPONENT_SELECTOR') !== this.instanceName) {
      this.instanceName = this.getTitleValue('COMPONENT_SELECTOR');
    }
    this.setTitleValue( (this.isGeneric ? this.typeName : this.instanceName) + '.' + this.getTitleValue('PROP'), 'COLLAPSED_TEXT');
  }
};

/**
 * Create a component (object) block for a component with the given
 * instance name.
 */
Blockly.Language.component_component_block = {
  category : 'Component',

  helpUrl : function() {
    var mode = this.typeName;
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

    this.appendDummyInput().appendTitle(this.componentDropDown, "COMPONENT_SELECTOR");
    //this.componentDropDown.setValue(this.instanceName);
    this.setOutput(true, [this.typeName,"COMPONENT"]);
    this.errors = [{name:"checkIsInDefinition"}];

    this.appendCollapsedInput().appendTitle(this.instanceName, 'COLLAPSED_TEXT');
  },
  // Renames the block's instanceName, type, and reset its title
  rename : function(oldname, newname) {
    if (this.instanceName == oldname) {
      this.instanceName = newname;
      //var title = this.inputList[0].titleRow[0];
      //title.setText(this.instanceName);
      this.componentDropDown.setValue(this.instanceName);
      this.setTitleValue(this.instanceName, 'COLLAPSED_TEXT');
      if (this.type.indexOf(oldname) != -1) {
        this.type = this.type.replace(oldname, newname);
      }
    }
  },

  typeblock : function(){
    var tb = [];
    var instanceNames = Blockly.ComponentInstances.getInstanceNames();
    var typeName;

    for(var i=0;i<instanceNames.length;i++) {
      typeName = Blockly.ComponentInstances[instanceNames[i]].typeName;

      tb.push({
        translatedName: instanceNames[i],
        mutatorAttributes: {
          component_type: typeName,
          instance_name: instanceNames[i]
        }
      });

    }

    return tb;
  },
  prepareCollapsedText : function(){
    this.setTitleValue(this.instanceName, 'COLLAPSED_TEXT');
  }
};

Blockly.ComponentBlock.createComponentDropDown = function(block){
  var componentDropDown = new Blockly.FieldDropdown([["",""]]);
  componentDropDown.block = block;
  componentDropDown.menuGenerator_ = function(){ return Blockly.Component.getComponentNamesByType(this.block.typeName);};
  componentDropDown.changeHandler_ = function(value){
    if (value !== null && value != "") {
      var oldValue = this.getValue();
      this.block.rename(oldValue, value);
    }
  };
  return componentDropDown;
}

Blockly.ComponentBlock.HELPURLS = {
  "Button": Blockly.LANG_COMPONENT_BLOCK_BUTTON_HELPURL,
  "Canvas": Blockly.LANG_COMPONENT_BLOCK_CANVAS_HELPURL,
  "CheckBox": Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL,
  "Clock": Blockly.LANG_COMPONENT_BLOCK_CLOCK_HELPURL,
  "Image": Blockly.LANG_COMPONENT_BLOCK_IMAGE_HELPURL,
  "Label": Blockly.LANG_COMPONENT_BLOCK_LABEL_HELPURL,
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_HELPURL,
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
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL,
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
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_PROPERTIES_HELPURL,
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
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL,
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
  "ListPicker": Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_EVENTS_HELPURL,
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
  "NxtColorSensor": Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL,
  "NxtLightSensor": Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL,
  "NxtSoundSensor": Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL,
  "NxtTouchSensor": Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL,
  "NxtUltrasonicSensor": Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL,
  "ActivityStarter": Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL,
  "BarcodeScanner": Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL,
  "BluetoothClient": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL,
  "BluetoothServer": Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL,
  "Notifier": Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL,
  "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL,
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
   "SpeechRecognizer": Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL,
   "TextToSpeech": Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL,
   "TinyWebDB": Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL,
   "Web": Blockly.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL,
   "FusiontablesControl": Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL,
   "GameClient": Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL,
   "SoundRecorder": Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL,
   "Voting": Blockly.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL,
   "WebViewer": Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL
};
