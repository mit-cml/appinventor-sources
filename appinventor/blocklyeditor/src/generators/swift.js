// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Helper functions for generating Swift for blocks.
 * @author Evan W. Patton <ewpatton@mit.edu>
 */
goog.provide('AI.Swift')

AI.Swift.SWIFT_DEFFORM_PRELUDE = "// $Source $Yail\n// automatically generated source file\n// any edits will be overwritten\n\nimport Foundation\nimport AIComponentKit\n\nclass ";
AI.Swift.SWIFT_DEFFORM_POSTLUDE = ": Form {\n";
AI.Swift.SWIFT_INIT = [
  "\n",
  "init?(_ coder: NSCoder? = nil) {\n",
  "  if let coder = coder {\n",
  "    super.init(coder: coder)\n",
  "  } else {\n",
  "    super.init(nibName: nil, bundle: nil)\n",
  "  }\n",
  "  application = UIApplication.shared.delegate as? Application\n",
  "}\n\n",
  "required convenience init?(coder aCoder: NSCoder) {\n",
  "  self.init(aCoder)\n",
  "}\n\n",
];
AI.Swift.SWIFT_FORM_POSTLUDE = "}\n";

AI.Swift.SwiftWriter = function() {
  AI.Swift.SwiftWriter.superClass_.constructor.call(this, 'Swift');
  this.indent_ = 0;
  this.pretty = true;
  this.code = [];
};
goog.inherits(AI.Swift.SwiftWriter, Blockly.Generator);

AI.Swift.SwiftWriter.prototype.indent = function() {
  this.indent_ += 2;
  return this;
};

AI.Swift.SwiftWriter.prototype.unindent = function() {
  this.indent_ -= 2;
  return this;
};

/**
 *
 * @param {string|string[]} code
 * @returns {AI.Swift.SwiftWriter}
 */
AI.Swift.SwiftWriter.prototype.push = function(code) {
  if (typeof code === 'string') {
    code = [code];
  }
  if (this.pretty) {
    var indent = ' '.repeat(this.indent_);
    for (var i = 0; i < code.length; i++) {
      this.code.push(indent + code[i]);
    }
  } else {
    Array.prototype.push.apply(this.code, code);
  }
  return this;
};

AI.Swift.SwiftWriter.prototype.writeProgram = function(form, blocks) {
  // reset
  this.indent_ = 0;
  this.code = [];

  // aggregate objects
  var componentMap = this.buildComponentMap(blocks, [], [], false, false).components,
      componentNames = Object.keys(componentMap),
      globals = [],
      procs = [],
      events = [],
      initEvent = null;
  for (var blockId in blocks) {
    if (blocks.hasOwnProperty(blockId)) {
      var block = blocks[blockId];
      if (block.type == 'component_event') {
        if (block.typeName == 'Form' && block.eventName == 'Initialize') {
          initEvent = block;
        } else {
          events.push(block);
        }
      } else if (block.type == 'procedures_defreturn' ||
                 block.type == 'procedures_defnoreturn') {
        procs.push(block);
      } else if (block.type == 'global_declaration') {
        globals.push(block);
      }
    }
  }

  return this.writePrelude(form.Properties.$Name)
    .indent()
    .writeGlobalDecls(globals)
    .writeComponentDecls(form.Properties)
    .writeSwiftInit()
    .writeViewDidLoad(form)
    .writeInitialize(initEvent)
    .writeProcedures(procs)
    .writeEventHandlers(events)
    .writeDispatchEvent(events)
    .unindent()
    .writePostlude()
    .toString();
};

AI.Swift.SwiftWriter.prototype.writePrelude = function(formName) {
  this.push(AI.Swift.SWIFT_DEFFORM_PRELUDE + formName + AI.Swift.SWIFT_DEFFORM_POSTLUDE);
  return this;
};

AI.Swift.SwiftWriter.prototype.writeGlobalDecls = function(global_blocks) {
  for (var i = 0; i < global_blocks.length; i++) {
    var block = global_blocks[i];
    var name = 'g$' + block.getFieldValue('NAME');
    this.push('var ' + name + ' = ' + (this.valueToCode(block, 'VALUE', 99) || '0') + '\n');
  }
  this.push('\n');
  return this;
};

AI.Swift.SwiftWriter.prototype.writeComponentDecls = function(component) {
  var name = component.$Name, type = component.$Type;
  if (type != 'Form') {
    this.push('var ' + name + ': ' + type + '!\n');
  }
  if (component.$Components) {
    for (var i = 0; i < component.$Components.length; i++) {
      this.writeComponentDecls(component.$Components[i]);
    }
  }
  return this;
};

AI.Swift.SwiftWriter.prototype.writeSwiftInit = function() {
  this.push('\n');
  this.push(AI.Swift.SWIFT_INIT);
  return this;
};

AI.Swift.SwiftWriter.prototype.writeViewDidLoad = function(formJson) {
  function valueToSwift(value) {
    if (value == 'True') return true;
    else if (value == 'False') return false;
    else if (/^-?[1-9]?[0-9]+$/.test(value)) return window.parseInt(value);
    else if (/^-?[1-9]?[0-9]+(\.?[0-9]+)?$/.test(value)) return window.parseFloat(value);
    else if (value[0] == '&' && value[1] == 'H') {
      return 'Int32(bitPattern: 0x' + value.substr(2) + ')';
    } else return this.quote_(value);
  }
  function writeComponentProperties(component, parent) {
    if (this.pretty) {
      this.push('// ' + component.$Name + '\n');
    }
    var init = component.$Name + ' = ' + component.$Type + '(';
    init += parent.$Type == 'Form' ? 'self' : parent.$Name;
    init += ')\n';
    this.push(init);
    for (var key in component) {
      if (component.hasOwnProperty(key) &&
          key[0] != '$' && key != 'Uuid') {
        var value = valueToSwift.call(this, component[key]);
        this.push(component.$Name + '.' + key + ' = ' + value + '\n');
      }
    }
    if (component.$Components && component.$Components.length > 0) {
      for (var i = 0; i <component.$Components.length; i++) {
        this.push('\n');
        writeComponentProperties.call(this, component.$Components[i], component);
      }
    }
  }
  this.push('override func viewDidLoad() {\n');
  this.indent();
  if (this.pretty) {
    this.push('// do-after-form-creation\n');
  }
  for (var key in formJson.Properties) {
    if (formJson.Properties.hasOwnProperty(key) && key[0] != '$' &&
        key != 'Uuid') {
      var value = valueToSwift.call(this, formJson.Properties[key]);
      this.push(key + ' = ' + value + '\n');
    }
  }
  for (var i = 0; i < formJson.Properties.$Components.length; i++) {
    this.push('\n');
    writeComponentProperties.call(this, formJson.Properties.$Components[i], formJson.Properties);
  }
  this.push('\n');
  this.push('Initialize()\n');
  this.unindent();
  this.push('}\n\n');
  return this;
};

AI.Swift.SwiftWriter.prototype.writeInitialize = function(initBlock) {
  if (!initBlock) {
    return this;  // no custom Initialize() needed
  }
  this.push('override func Initialize() {\n');
  this.indent();
  this.push('super.Initialize()\n');
  this['component_event'].call(this, initBlock, true);
  this.unindent();
  this.push('}\n\n');
  return this;
};

AI.Swift.SwiftWriter.prototype.writeProcedures = function(blocks) {
  for (var i = 0; i < blocks.length; i++) {
    var block = blocks[i];
    this[block.type].call(this, block);
  }
  return this;
};

AI.Swift.SwiftWriter.prototype.writeEventHandlers = function(blocks) {
  for (var i = 0; i < blocks.length; i++) {
    var block = blocks[i];
    this['component_event'].call(this, block);
  }
  return this;
};

AI.Swift.SwiftWriter.prototype.writeDispatchEvent = function(blocks) {
  this.push('override func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool{\n');
  this.indent();
  if (blocks.length > 0) {
    var block = blocks[0],
        componentName = block.getFieldValue('COMPONENT_SELECTOR'),
        eventName = block.eventName;
    this.push('if componentName == \"' + componentName + '\" && eventName == \"' + eventName + '\" {\n');
    this.indent();
    var funcall = componentName + '$' + eventName + '(';
    var argList = [];
    var params = blocks[0].getEventTypeObject().params;
    for (var j = 0; j < params.length; j++) {
      if (params[j].type == 'text') {
        argList.push('args[' + j + '] as! String');
      } else if (params[j].type == 'any') {
        argList.push('args[' + j + '] as! AnyObject');
      } else if (params[j].type == 'number') {
        // TODO(ewpatton): Cannot assume double representation for numbers
        argList.push('args[' + j + '] as! Double');
      } else {
        throw 'Unexpected type ' + params[j].type;
      }
    }
    funcall += argList.join(', ') + ')\n';
    this.push(funcall);
    for (var i = 1; i < blocks.length; i++) {
      block = blocks[i];
      componentName = block.getFieldValue('COMPONENT_SELECTOR');
      eventName = block.eventName;
      this.unindent();
      this.push('} else if componentName == \"' + componentName + '\" && eventName == \"' + eventName + '\" {\n');
      this.indent();
      var funcall = componentName + '$' + eventName + '(';
      var argList = [];
      var params = blocks[i].getEventTypeObject().params;
      for (var j = 0; j < params.length; j++) {
        if (params[j].type == 'text') {
          argList.push('args[' + j + '] as! String');
        } else if (params[j].type == 'any') {
          argList.push('args[' + j + '] as! AnyObject');
        } else if (params[j].type == 'number') {
          // TODO(ewpatton): Cannot assume double representation for numbers
          argList.push('args[' + j + '] as! Double');
        } else {
          throw 'Unexpected type ' + params[j].type;
        }
      }
      funcall += argList.join(', ') + ')\n';
      this.push(funcall);
    }
    this.unindent();
    this.push('}\n');
  }
  this.unindent();
  this.push('}\n\n');
  return this;
};

AI.Swift.SwiftWriter.prototype.writePostlude = function() {
  this.push(AI.Swift.SWIFT_FORM_POSTLUDE);
  return this;
};

AI.Swift.SwiftWriter.prototype.toString = function() {
  return this.code.join('');
};

AI.Swift.SwiftWriter.prototype.buildComponentMap = function(blocks, warnings, errors, forRepl, compileUnattachedBlocks) {
  var map = {};
  map.components = {};
  map.globals = [];

  for (var x = 0, block; block = blocks[x]; x++) {

    // TODO: deal with unattached blocks that are not valid top-level definitions. Valid blocks
    // are events, variable definitions, or procedure definitions.

    if (!block.category) {
      continue;
    }
    if (block.type == 'procedures_defnoreturn' || block.type == 'procedures_defreturn' || block.type == 'global_declaration') {
      map.globals.push(block);
      // TODO: eventually deal with variable declarations, once we have them
    } else if (block.category == 'Component') {
      var instanceName = block.instanceName;
      if(block.blockType != "event") {
        continue;
      }
      if (!map.components[instanceName]) {
        map.components[instanceName] = [];  // first block we've found for this component
      }

      // TODO: check for duplicate top-level blocks (e.g., two event handlers with same name) -
      // or better yet, prevent these from happening!

      map.components[instanceName].push(block);
    }
  }
  return map;
};

AI.Swift.getFormSwift = function(formJson, packageName) {
  var jsonObject = JSON.parse(formJson),
      componentNames = [],
      formProperties,
      formName,
      propertyNameConverter = function(input) { return input; },
      writer = new AI.Swift.SwiftWriter();
  if (jsonObject.Properties) {
    formProperties = jsonObject.Properties;
    formName = formProperties.$Name;
  } else {
    throw 'Cannot find form properties';
  }
  if (!formName) {
    throw 'Unable to determine form name';
  }
  writer.push(AI.Swift.getSwiftPrelude(packageName, formName));

  var componentMap = Blockly.Component.buildComponentMap([], [], false, false);

  for (var comp in componentMap.globals) {
    componentNames.push(comp);
  }

  if (formProperties) {
    var sourceType = jsonObject.Source;
    if (sourceType == 'Form') {
      Array.prototype.push.apply(code, AI.Swift.getComponentLines(formName, formProperties, null /*parent*/, componentMap, false /*forRepl*/, propertyNameConverter));
    } else {
      throw 'Source type ' + sourceType + ' is invalid.';
    }
  }

  writer.push(AI.Swift.SWIFT_FORM_POSTLUDE);
  return writer.toString();
};

AI.Swift.getSwiftPrelude = function(packageName, formName) {
  return "/*\n$Source $Swift\n*/\n\n" +
    AI.Swift.SWIFT_DEFFORM_PRELUDE +
    formName +
    AI.Swift.SWIFT_DEFFORM_POSTLUDE;
};

AI.Swift.SwiftWriter.prototype['color'] = function(block) {
  var code = -1 * (window.Math.pow(16,6) - window.parseInt('0x' + this.getFieldValue('COLOR').substr(1)));
  return [code, 0];
};

AI.Swift.SwiftWriter.prototype['color_light_gray'] = function(block) {
  return this['color'].call(this, block);
};

AI.Swift.SwiftWriter.prototype['color_make_color'] = function(block) {
  var blackList = "[0, 0, 0]";
  var arg0 = this.valueToCode(block, 'COLORLIST', 99) || blackList;
  var code = 'rgbArrayToInt32(' + arg0 + ')'
  return [code, 99];
};

AI.Swift.SwiftWriter.prototype['component_component_block'] = function(block) {
  return [block.getFieldValue('COMPONENT_SELECTOR'), 0];
};

AI.Swift.SwiftWriter.prototype['component_event'] = function(block, isInit) {
  var componentName = block.getFieldValue('COMPONENT_SELECTOR');
  var eventName = block.eventName;
  var argList = [];
  var paramList = block.getParameters();
  var typemap = {'any': 'AnyObject', 'text': 'String', 'number': 'Double'};
  for (var i = 0; i < paramList.length; i++) {
    argList.push('_ ' + paramList[i].name + ': ' + ((paramList[i].type && typemap[paramList[i].type]) || 'Any'));
  }
  if (!isInit) {
    this.push('func ' + componentName + '$' + eventName + '(' +
              argList.join(', ') + ') {\n');
    this.indent();
  }
  var body = this.statementToCode(block, 'DO', 99);
  this.push(body + '\n');
  if (!isInit) {
    this.unindent();
    this.push('}\n\n');
  }
  return null;
};

AI.Swift.SwiftWriter.prototype['component_method'] = function(block) {
  var code = null;
  if (block.isGeneric) {
    code = '((' + block.typeName + ') ' +
      this.valueToCode(block, 'COMPONENT', 99) + ')'
  } else {
    code = block.getFieldValue('COMPONENT_SELECTOR');
  }
  code += '.' + block.methodName + '(';
  var method = block.getMethodTypeObject();
  var args = [];
  for (var i = 0; i < method.params.length; i++) {
    args[i] = this.valueToCode(block, 'ARG' + i, 99);
  }
  code += args.join(', ') + ')';
  if (method.returnType) {
    return [code, 19];
  } else {
    return code + '\n';
  }
};

AI.Swift.SwiftWriter.prototype['component_set_get'] = function(block) {
  var code = null;
  if (block.isGeneric) {
    code = '((' + block.typeName + ') ' + this.valueToCode(block, 'COMPONENT', 99) + ')';
  } else {
    code = block.getFieldValue('COMPONENT_SELECTOR');
  }
  code += '.' + block.getFieldValue('PROP');
  if (block.setOrGet == 'set') {
    code += ' = ' + this.valueToCode(block, 'VALUE', 99) + '\n';
    return code;
  } else {
    return [code, 0];
  }
};

AI.Swift.SwiftWriter.prototype['controls_choose'] = function(block) {
  var test = this.valueToCode(block, 'TEST', 99) || 'false';
  this.push('if (' + test + ') {\n');
  this.indent();
  var code = this.statementToCode(block, 'THENRETURN', 99) || '';
  if (code != '') {
    this.push(code + '\n');
  }
  this.unindent();
  this.push('} else {\n');
  this.indent();
  var code = this.statementToCode(block, 'ELSERETURN', 99) || '';
  if (code != '') {
    this.push(code + '\n');
  }
  this.unindent();
  this.push('}\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['controls_closeScreen'] = function(block) {
  this.push('closeScreen()\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['controls_forEach'] = function(block) {
  this.push('for ' + block.getFieldValue('VAR') + ' in ' +
            this.valueToCode(block, 'LIST', 99) + '{\n');
  this.indent();
  var code = this.statementToCode(block, 'DO', 99) || null;
  if (code != null) {
    this.push(code + '\n');
  }
  this.unindent();
  this.push('}\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['controls_if'] = function(block) {
  var argument = this.valueToCode(block, 'IF0', 99) || 'false';
  this.push('if (' + argument + ') {\n');
  this.indent();
  var branch = this.statementToCode(block, 'DO0', 99) || null;
  if (branch != null) {
    this.push(branch + '\n');
  }
  this.unindent();
  for (var i = 1; i < this.elseifCount_ + 1; i++) {
    argument = this.valueToCode(block, 'IF' + i, 99) || 'false';
    this.push('} else if (' + argument + ') {\n');
    this.indent();
    branch = this.statementToCode(block, 'DO' + i, 99) || null;
    if (branch != null) {
      this.push(branch + '\n');
    }
    this.unindent();
  }
  this.push('}\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['controls_openAnotherScreen'] = function(block) {
  var screen = this.valueToCode(block, 'SCREEN', 99) || '\"\"';
  this.push('openAnotherScreen(named: ' + screen + ')\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['global_declaration'] = function(block) {
  var name = block.getFieldValue('NAME');
  var argument0 = this.valueToCode(block, 'VALUE', 99) || '0';
  this.push('var ' + name + ' = ' + argument0 + '\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['lexical_variable_get'] = function(block) {
  var pair = Blockly.unprefixName(block.getFieldValue('VAR'));
  if (pair[0] == Blockly.globalNamePrefix) {
    return ['g$' + pair[1], 0];
  } else {
    return [pair[1], 0];
  }
};

AI.Swift.SwiftWriter.prototype['lexical_variable_set'] = function(block) {
  var argument0 = this.valueToCode(block, 'VALUE', 99) || '0';
  var name = block.getFieldValue('VAR');
  if (block.eventparam) {
    name = block.eventparam;
  } else {
    Blockly.LexicalVariable.getEventParam(block);
    if (block.eventparam) {
      name = block.eventparam;
    }
  }
  var pair = Blockly.unprefixName(name);
  if (pair[0] == Blockly.globalNamePrefix) {
    name = 'g$' + pair[1];
  } else {
    name = pair[1];
  }
  this.push(name + ' = ' + argument0 + '\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['lists_add_items'] = function(block) {
  var code = this.valueToCode(block, 'LIST', 19) || '[]';
  if (block.itemCount_ == 1) {
    code += '.append(' + this.valueToCode(block, 'ITEM0', 18) + ')\n'
  } else {
    code += '.append(contentsOf: [';
    code += this.valueToCode(block, 'ITEM0', 19);
    for (var i = 1; i < block.itemCount_; i++) {
      code += ', ' + this.valueToCode(block, 'ITEM' + i, 19);
    }
    code += '])\n';
  }
  return code;
};

AI.Swift.SwiftWriter.prototype['lists_create_with'] = function(block) {
  var code = ['[']
  if (block.itemCount_ > 0) {
    code.push(this.valueToCode(block, 'ADD0', 19) || 'nil');
    for (var i = 1; i < block.itemCount_; i++) {
      code.push(', ');
      code.push(this.valueToCode(block, 'ADD' + i, 19) || 'nil');
    }
  }
  code.push(']')
  return [code.join(''), 19];
};

AI.Swift.SwiftWriter.prototype['lists_length'] = function(block) {
  var code = this.valueToCode(block, 'LIST', 19) + '.count';
  return [code, 19];
};

AI.Swift.SwiftWriter.prototype['lists_pick_random_item'] = function(block) {
  var list = this.valueToCode(block, 'LIST', 19);
  var code = list + '[Int(arc4random_uniform(UInt32(' + list + '.count)))]'
  return [code, 19];
};

AI.Swift.SwiftWriter.prototype['lists_position_in'] = function(block) {
  var list = this.valueToCode(block, 'LIST', 18);
  var code = list + '.index(of: ' + this.valueToCode(block, 'ITEM', 18) + ')'
  return [code, 18];
};

AI.Swift.SwiftWriter.prototype['lists_remove_item'] = function(block) {
  var list = this.valueToCode(block, 'LIST', 18);
  var code = list + '.remove(at: Int(' + this.valueToCode(block, 'INDEX', 18) + '))';
  return [code, 18];
};

AI.Swift.SwiftWriter.prototype['lists_select_item'] = function(block) {
  var list = this.valueToCode(block, 'LIST', 19);
  var code = list + '[Int(' + this.valueToCode(block, 'INDEX', 19) + ')]';
  return [code, 19];
};

AI.Swift.SwiftWriter.prototype['logic_boolean'] = function(block) {
  var code = (block.getFieldValue('BOOL') == 'TRUE') ? 'true' : 'false';
  return [code, 0];
};

AI.Swift.SwiftWriter.prototype['logic_operation'] = function(block) {
  // 10
  var mode = block.getFieldValue('OP');
  var op = mode == 'AND' ? ' && ' : ' || ';
  var order = mode == 'AND' ? 6 : 5;
  var arg0 = this.valueToCode(block, 'A', order) || 'false';
  var arg1 = this.valueToCode(block, 'B', order) || 'false';
  var code = arg0 + op + arg1;
  return [code, order];
};

AI.Swift.SwiftWriter.prototype['math_add'] = function(block) {
  var operands = new Array(block.itemCount_);
  for (var i = 0; i < block.itemCount_; i++) {
    operands[i] = this.valueToCode(block, 'ARG' + i, 13);
  }
  return [operands.join(' + '), 13];
};

AI.Swift.SwiftWriter.prototype['math_compare'] = function(block) {
  var ops = {
    'EQ': [' == ', 10],
    'NEQ': [' != ', 10],
    'LT': [' < ', 11],
    'LTE': [' <= ', 11],
    'GT': [' > ', 11],
    'GTE': [' >= ', 11]
  };
  var mode = block.getFieldValue('OP'),
      op = ops[mode][0],
      order = ops[mode][1],
      arg0 = this.valueToCode(block, 'A', order) || '0',
      arg1 = this.valueToCode(block, 'B', order) || '0',
      code = arg0 + op + arg1;
  return [code, order];
};

AI.Swift.SwiftWriter.prototype['math_division'] = function(block) {
  var arg0 = this.valueToCode(block, 'A', 14) || '0',
      arg1 = this.valueToCode(block, 'B', 14) || '1';
  return [arg0 + ' / ' + arg1, 14];
};

AI.Swift.SwiftWriter.prototype['math_format_as_decimal'] = function(block) {
  var code = 'formatAsDecimal(' + this.valueToCode(block, 'NUM', 19) + ', ' +
      this.valueToCode(block, 'PLACES', 19) + ')';
  return [code, 19];
};

AI.Swift.SwiftWriter.prototype['math_multiply'] = function(block) {
  var operands = new Array(block.itemCount_);
  for (var i = 0; i < block.itemCount_; i++) {
    operands[i] = this.valueToCode(block, 'ARG' + i, 14);
  }
  return [operands.join(' * '), 14];
};

AI.Swift.SwiftWriter.prototype['math_number'] = function(block) {
  var code = window.parseFloat(block.getFieldValue('NUM'));
  return [code, 0];
};

AI.Swift.SwiftWriter.prototype['procedures_callnoreturn'] = function(block) {
  var procName = 'p$' + block.getFieldValue('PROCNAME');
  var argCode = [];
  for (var i = 0; block.getInput('ARG' + i); i++) {
    argCode[i] = this.valueToCode(block, 'ARG' + i, 99) || 'false';
  }
  var code = procName + '(' + argCode.join(', ') + ')\n';
  return code;
};

AI.Swift.SwiftWriter.prototype['procedures_callreturn'] = function(block) {
  var procName = 'p$' + block.getFieldValue('PROCNAME');
  var argCode = [];
  for (var i = 0; block.getInput('ARG' + i); i++) {
    argCode[i] = this.valueToCode(block, 'ARG' + i, 99) || 'false';
  }
  var code = procName + '(' + argCode.join(', ') + ')';
  return [code, 0];  
};

AI.Swift.SwiftWriter.prototype['procedures_defnoreturn'] = function(block) {
  var defun = 'func p$' + block.getFieldValue('NAME') + '(';
  var args = new Array(block.arguments_.length);
  for (var i = 0; i < block.arguments_.length; i++) {
    args[i] = '_ ' + block.arguments_[i] + ': Any';
  }
  defun += args.join(', ');
  defun += ') {\n';
  this.push(defun);
  this.indent();
  var body = this.statementToCode(block, 'STACK', 99) || '';
  if (body != '') {
    this.push(body + '\n');
  }
  this.unindent();
  this.push('}\n\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['procedures_defreturn'] = function(block) {
  // TODO(ewpatton): Statically analyze call graph to determine type
  // For now, we [incorrectly] assume a Double return value
  var defun = 'func p$' + block.getFieldValue('NAME') + '(';
  var args = new Array(block.arguments_.length);
  for (var i = 0; i < block.arguments_.length; i++) {
    args[i] = '_ ' + block.arguments_[i] + ': Any';
  }
  defun += args.join(', ');
  defun += ') -> Double {\n';
  this.push(defun);
  this.indent();
  var body = this.statementToCode(block, 'STACK', 99) || '';
  if (body != '') {
    this.push(body + '\n');
  }
  this.unindent();
  this.push('}\n\n');
  return null;
};

AI.Swift.SwiftWriter.prototype['text'] = function(block) {
  var code = this.quote_(block.getFieldValue('TEXT'));
  return [code, 0];
};

AI.Swift.SwiftWriter.prototype['text_join'] = function(block) {
  var code = '';
  if (block.itemCount_ > 0) {
    var code = new Array(block.itemCount_);
    for (var i = 0; i < block.itemCount_; i++) {
      code[i] = this.valueToCode(block, 'ADD' + i, 99) || '""';
    }
    return [code.join(' + '), 13];
  } else {
    return ['', 0];
  }
};

AI.Swift.SwiftWriter.prototype.blockToCode = function(block) {
  if (!block) {
    return '';
  }
  if (block.disabled) {
    // Skip past this block if it is disabled.
    return this.blockToCode(block.getNextBlock());
  }

  var func = this[block.type];
  if (!func) {
    throw 'Language "' + this.name_ + '" does not know how to generate code ' +
        'for block type "' + block.type + '".';
  }
  // First argument to func.call is the value of 'this' in the generator.
  // Prior to 24 September 2013 'this' was the only way to access the block.
  // The current prefered method of accessing the block is through the second
  // argument to func.call, which becomes the first parameter to the generator.
  var code = func.call(this, block);
  if (goog.isArray(code)) {
    // Value blocks return tuples of code and operator order.
    return [this.scrub_(block, code[0]), code[1]];
  } else if (goog.isString(code)) {
    if (this.STATEMENT_PREFIX) {
      code = this.STATEMENT_PREFIX.replace(/%1/g, '\'' + block.id + '\'') +
          code;
    }
    return this.scrub_(block, code);
  } else if (code === null) {
    // Block has handled code generation itself.
    return '';
  } else {
    throw 'Invalid code generated: ' + code;
  }
};

AI.Swift.SwiftWriter.prototype.scrub_ = function(block, code, thisOnly) {
  if (code === null) {
    return '';
  }
  var commentCode = '';
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = thisOnly? "" : this.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};

AI.Swift.SwiftWriter.prototype.quote_ = function(string) {
  string = this.quotifyForREPL(string);
  if (!string) {
    string = '""';
  }
  return string;
};

AI.Swift.SwiftWriter.prototype.quotifyForREPL = function(s) {
  if (!s) {
    return null;
  }
  var sb = [];
  sb.push('"');
  var len = s.length,
      lastIndex = len - 1,
      c;
  for (var i = 0; i < len; i++) {
    c = s.charAt(i);
    if (c == '\\') {
      if (!(i == lastIndex) && s.charAt(i + 1) == 'n') {
        sb.push(c)
        sb.push(s.charAt(i + 1));
        i++;
      } else {
        sb.push('\\\\');
      }
    } else if (c == '"') {
      sb.push('\\"');
    } else {
      var u = s.charCodeAt(i);
      if (u < ' '.charCodeAt(0) || u > '~'.charCodeAt(0)) {
        var hex = '000' + u.toString(16);
        hex = hex.substring(hex.length - 4);
        sb.push('\\u{' + hex + '}');
      } else {
        sb.push(c);
      }
    }
  }
  sb.push('"');
  return sb.join('');
};
