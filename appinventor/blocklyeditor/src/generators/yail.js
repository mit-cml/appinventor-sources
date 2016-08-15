// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Helper functions for generating Yail for blocks.
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * @author sharon@google.com (Sharon Perl)
 */

'use strict';

goog.provide('Blockly.Yail');

goog.require('Blockly.Generator');

Blockly.Yail = new Blockly.Generator('Yail');

/**
 * List of illegal variable names. This is not intended to be a security feature.  Blockly is 
 * 100% client-side, so bypassing this list is trivial.  This is intended to prevent users from
 * accidentally clobbering a built-in object or function.
 * 
 * TODO: fill this in or remove it.
 * @private
 */
Blockly.Yail.RESERVED_WORDS_ = '';

/**
 * Order of operation ENUMs.
 * https://developer.mozilla.org/en/Yail/Reference/Operators/Operator_Precedence
 */
Blockly.Yail.ORDER_ATOMIC = 0;         // 0 "" ...
Blockly.Yail.ORDER_NONE = 99;          // (...)

Blockly.Yail.YAIL_ADD_COMPONENT = "(add-component ";
Blockly.Yail.YAIL_ADD_TO_LIST = "(add-to-list ";
Blockly.Yail.YAIL_BEGIN = "(begin ";
Blockly.Yail.YAIL_CALL_COMPONENT_METHOD = "(call-component-method ";
Blockly.Yail.YAIL_CALL_COMPONENT_TYPE_METHOD = "(call-component-type-method ";
Blockly.Yail.YAIL_CALL_YAIL_PRIMITIVE = "(call-yail-primitive ";
Blockly.Yail.YAIL_CLEAR_FORM = "(clear-current-form)";
// The lines below are complicated because we want to support versions of the
// Companion older then 2.20ai2 which do not have set-form-name defined
Blockly.Yail.YAIL_SET_FORM_NAME_BEGIN = "(try-catch (let ((attempt (delay (set-form-name \"";
Blockly.Yail.YAIL_SET_FORM_NAME_END = "\")))) (force attempt)) (exception java.lang.Throwable 'notfound))";
Blockly.Yail.YAIL_CLOSE_COMBINATION = ")";
Blockly.Yail.YAIL_CLOSE_BLOCK = ")\n";
Blockly.Yail.YAIL_COMMENT_MAJOR = ";;; ";
Blockly.Yail.YAIL_COMPONENT_REMOVE = "(remove-component ";
Blockly.Yail.YAIL_COMPONENT_TYPE = "component";
Blockly.Yail.YAIL_DEFINE = "(def ";
Blockly.Yail.YAIL_DEFINE_EVENT = "(define-event ";
Blockly.Yail.YAIL_DEFINE_FORM = "(define-form ";
Blockly.Yail.YAIL_DO_AFTER_FORM_CREATION = "(do-after-form-creation ";
Blockly.Yail.YAIL_DOUBLE_QUOTE = "\"";
Blockly.Yail.YAIL_FALSE = "#f";
Blockly.Yail.YAIL_FOREACH = "(foreach ";
Blockly.Yail.YAIL_FORRANGE = "(forrange ";
Blockly.Yail.YAIL_GET_COMPONENT = "(get-component ";
Blockly.Yail.YAIL_GET_PROPERTY = "(get-property ";
Blockly.Yail.YAIL_GET_COMPONENT_TYPE_PROPERTY = "(get-property-and-check  ";
Blockly.Yail.YAIL_GET_VARIABLE = "(get-var ";
Blockly.Yail.YAIL_AND_DELAYED = "(and-delayed ";
Blockly.Yail.YAIL_OR_DELAYED = "(or-delayed ";
Blockly.Yail.YAIL_IF = "(if ";
Blockly.Yail.YAIL_INIT_RUNTIME = "(init-runtime)";
Blockly.Yail.YAIL_INITIALIZE_COMPONENTS = "(call-Initialize-of-components";
Blockly.Yail.YAIL_LET = "(let ";
Blockly.Yail.YAIL_LEXICAL_VALUE = "(lexical-value ";
Blockly.Yail.YAIL_SET_LEXICAL_VALUE = "(set-lexical! ";
Blockly.Yail.YAIL_LINE_FEED = "\n";
Blockly.Yail.YAIL_NULL = "(get-var *the-null-value*)";
Blockly.Yail.YAIL_EMPTY_LIST = "'()";
Blockly.Yail.YAIL_OPEN_BLOCK = "(";
Blockly.Yail.YAIL_OPEN_COMBINATION = "(";
Blockly.Yail.YAIL_QUOTE = "'";
Blockly.Yail.YAIL_RENAME_COMPONENT = "(rename-component ";
Blockly.Yail.YAIL_SET_AND_COERCE_PROPERTY = "(set-and-coerce-property! ";
Blockly.Yail.YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY = "(set-and-coerce-property-and-check! ";
Blockly.Yail.YAIL_SET_SUBFORM_LAYOUT_PROPERTY = "(%set-subform-layout-property! ";
Blockly.Yail.YAIL_SET_VARIABLE = "(set-var! ";
Blockly.Yail.YAIL_SET_THIS_FORM = "(set-this-form)\n ";
Blockly.Yail.YAIL_SPACER = " ";
Blockly.Yail.YAIL_TRUE = "#t";
Blockly.Yail.YAIL_WHILE = "(while ";
Blockly.Yail.YAIL_LIST_CONSTRUCTOR = "*list-for-runtime*";

Blockly.Yail.SIMPLE_HEX_PREFIX = "&H";
Blockly.Yail.YAIL_HEX_PREFIX = "#x";

// permit leading and trailing whitespace for checking that strings are numbers
Blockly.Yail.INTEGER_REGEXP = "^[\\s]*[-+]?[0-9]+[\\s]*$";
Blockly.Yail.FLONUM_REGEXP = "^[\\s]*[-+]?([0-9]*)((\\.[0-9]+)|[0-9]\\.)[\\s]*$";


/**
 * Generate the Yail code for this blocks workspace, given its associated form specification.
 * 
 * @param {String} formJson JSON string describing the contents of the form. This is the JSON
 *    content from the ".scm" file for this form.
 * @param {String} packageName the name of the package (to put in the define-form call)
 * @param {Boolean} forRepl  true if the code is being generated for the REPL, false if for an apk
 * @returns {String} the generated code if there were no errors.
 */
Blockly.Yail.getFormYail = function(formJson, packageName, forRepl) {
  var jsonObject = JSON.parse(formJson); 
  // TODO: check for JSON parse error
  var componentNames = [];
  var formProperties; 
  var formName;
  var code = [];
  var propertyNameConverter = function(input) {
    return input;
  };
  if (jsonObject.Properties) {
    formProperties = jsonObject.Properties;
    formName = formProperties.$Name;
  } else {
    throw "Cannot find form properties";
  }
  if (!formName) {
    throw "Unable to determine form name";
  }

  if (!forRepl) {
    code.push(Blockly.Yail.getYailPrelude(packageName, formName));
  }
    
  var componentMap = Blockly.Component.buildComponentMap([], [], false, false);
  
  for (var comp in componentMap.components)
    componentNames.push(comp);

  var globalBlocks = componentMap.globals;
  for (var i = 0, block; block = globalBlocks[i]; i++) {
    code.push(Blockly.Yail.blockToCode(block));
  }
  
  if (formProperties) {
    var sourceType = jsonObject.Source;
    if (sourceType == "Form") {
      code = code.concat(Blockly.Yail.getComponentLines(formName, formProperties, null /*parent*/, 
          componentMap, false /*forRepl*/, propertyNameConverter));
    } else {
      throw "Source type " + sourceType + " is invalid.";
    }
  
    // Fetch all of the components in the form, this may result in duplicates
    componentNames = Blockly.Yail.getDeepNames(formProperties, componentNames);
    // Remove the duplicates
    var uniqueNames = componentNames.filter(function(elem, pos) {
        return componentNames.indexOf(elem) == pos});
    componentNames = uniqueNames;

    // Add runtime initializations
    code.push(Blockly.Yail.YAIL_INIT_RUNTIME);
  
    if (forRepl) {
      code = Blockly.Yail.wrapForRepl(formName, code, componentNames);
    }

    // TODO?: get rid of empty property assignments? I'm not convinced this is necessary.
    // The original code in YABlockCompiler.java attempts to do this, but it matches on 
    // "set-property" rather than "set-and-coerce-property" so I'm not sure it is actually
    // doing anything. If we do need this, something like the call below might work.
    // 
    // finalCode = code.join('\n').replace(/\\(set-property.*\"\"\\)\\n*/mg, "");
  }
  
  return code.join('\n');  // Blank line between each section.
};

Blockly.Yail.getDeepNames = function(componentJson, componentNames) {
  if (componentJson.$Components) {
    var children = componentJson.$Components;
    for (var i = 0, child; child = children[i]; i++) {
      componentNames.push(child.$Name);
      componentNames = Blockly.Yail.getDeepNames(child, componentNames);
    }
  }
  return componentNames;
}

/**
 * Generate the beginning Yail code for an APK compilation (i.e., not the REPL)
 * 
 * @param {String} packageName  the name of the package for the app
 *     (e.g. "appinventor.ai_somebody.myproject.Screen1")
 * @param {String} formName  (e.g., "Screen1")
 * @returns {String} Yail code
 * @private
*/
Blockly.Yail.getYailPrelude = function(packageName, formName) {
 return "#|\n$Source $Yail\n|#\n\n"
     + Blockly.Yail.YAIL_DEFINE_FORM
     + packageName
     + Blockly.Yail.YAIL_SPACER
     + formName
     + Blockly.Yail.YAIL_CLOSE_BLOCK
     + "(require <com.google.youngandroid.runtime>)\n";
}

/**
 * Wraps Yail code for use in the REPL and returns the new code as an array of strings
 * 
 * @param {String} formName 
 * @param {Array} code  code strings to be wrapped
 * @param {Array} componentNames array of component names
 * @returns {Array} wrapped code strings
 * @private
 */
Blockly.Yail.wrapForRepl = function(formName, code, componentNames) {
  var replCode = [];
  replCode.push(Blockly.Yail.YAIL_BEGIN);
  replCode.push(Blockly.Yail.YAIL_CLEAR_FORM);
  if (formName != "Screen1") {
    // If this form is not named Screen1, then the REPL won't be able to resolve any references
    // to it or to any properties on the form itself (such as Title, BackgroundColor, etc) unless
    // we tell it that "Screen1" has been renamed to formName.
    // By generating a call to rename-component here, the REPL will rename "Screen1" to formName
    // in the current environment. See rename-component in runtime.scm.
    replCode.push(Blockly.Yail.getComponentRenameString("Screen1", formName));
  }
  replCode = replCode.concat(code);
  replCode.push(Blockly.Yail.getComponentInitializationString(formName, componentNames));
  replCode.push(Blockly.Yail.YAIL_CLOSE_BLOCK);
  return replCode;
}

/**
 * Return code to initialize all components in componentMap.
 * 
 * @param {Array} componentNames array of names of components in the workspace
 * @returns {Array} code strings
 * @private
 */
Blockly.Yail.getComponentInitializationString = function(formName, componentNames) {
  var code = Blockly.Yail.YAIL_INITIALIZE_COMPONENTS;
  code += " " + Blockly.Yail.YAIL_QUOTE + formName;
  for (var i = 0, cName; cName = componentNames[i]; i++) {  // TODO: will we get non-component fields this way?
    if (cName != formName)                                  // Avoid duplicate initialization of the form
      code = code + " " + Blockly.Yail.YAIL_QUOTE + cName;
  }
  code = code + ")";
  return code;
}

/**
 * Generate Yail code for the component described by componentJson, and all of its child
 * components (if it has any). componentJson may describe a Form or a regular component. The
 * generated code includes adding each component to the form, as well as generating code for
 * the top-level blocks for that component.
 *
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {String} parentName  the name of the component that contains this component (which may be
 *    its Form, for top-level components).
 * @param {Object} componentMap map from component names to the top-level blocks for that component
 *    in the workspace. See the Blockly.Component.buildComponentMap description for the structure.
 * @param {Boolean} forRepl true iff we're generating code for the REPL rather than an apk.
 * @returns {Array} code strings
 * @private
 */
Blockly.Yail.getComponentLines = function(formName, componentJson, parentName, componentMap, 
  forRepl, propertyNameConverter) {
  var code = [];
  var componentName = componentJson.$Name;
  if (componentJson.$Type == 'Form') {
    code = Blockly.Yail.getFormPropertiesLines(formName, componentJson, !forRepl);
  } else {
    code = Blockly.Yail.getComponentPropertiesLines(formName, componentJson, parentName, !forRepl,
      propertyNameConverter);
  }

  if (!forRepl) {
    // Generate code for all top-level blocks related to this component
    if (componentMap.components && componentMap.components[componentName]) {
      var componentBlocks = componentMap.components[componentName];
      for (var i = 0, block; block = componentBlocks[i]; i++) {
        code.push(Blockly.Yail.blockToCode(block));
      }
    }
  }

  // Generate code for child components of this component
  if (componentJson.$Components) {
    var children = componentJson.$Components;
    for (var i = 0, child; child = children[i]; i++) {
      code = code.concat(Blockly.Yail.getComponentLines(formName, child, componentName,
          componentMap, forRepl, propertyNameConverter));
    }
  }
  return code;  
};

/**
 * Generate Yail to add the component described by componentJson to its parent, followed by
 * the code that sets each property of the component (for all its properties listed in
 * componentJson).
 * 
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {String} parentName  the name of the component that contains this component (which may be
 *    its Form, for top-level components).
 * @param {Boolean} whether to include comments in the generated code
 * @returns {Array} code strings
 * @private
 */
Blockly.Yail.getComponentPropertiesLines = function(formName, componentJson, parentName, 
  includeComments, propertyNameConverter) {
  var code = [];
  var componentName = componentJson.$Name;
  var componentType = componentJson.$Type;
  // generate the yail code that adds the component to its parent, followed by the code that
  // sets each property of the component
  if (includeComments) {
    code.push(Blockly.Yail.YAIL_COMMENT_MAJOR + componentName + Blockly.Yail.YAIL_LINE_FEED);
  }
  // Send Blockly.ComponentTypes[componentType].type for full class path
  // but first feed it to propertyNameConverter function passed in. This may
  // trim it back to just the simple component name if we are sending this yail
  // to an older companion
  code.push(Blockly.Yail.YAIL_ADD_COMPONENT + parentName + Blockly.Yail.YAIL_SPACER +
    propertyNameConverter(Blockly.ComponentTypes[componentType].type) +
    Blockly.Yail.YAIL_SPACER + componentName + Blockly.Yail.YAIL_SPACER);
  code = code.concat(Blockly.Yail.getPropertySettersLines(componentJson, componentName));
  code.push(Blockly.Yail.YAIL_CLOSE_BLOCK);
  return code;
}

/**
 * Generate Yail to set the properties for the Form described by componentJson.
 * 
 * @param {String} formName
 * @param {String} componentJson JSON string describing the component
 * @param {Boolean} whether to include comments in the generated code
 * @returns {Array} code strings
 * @private
 */
Blockly.Yail.getFormPropertiesLines = function(formName, componentJson, includeComments) {
  var code = [];
  if (includeComments) {
    code.push(Blockly.Yail.YAIL_COMMENT_MAJOR + formName + Blockly.Yail.YAIL_LINE_FEED);
  }
  var yailForComponentProperties = Blockly.Yail.getPropertySettersLines(componentJson, formName);
  if (yailForComponentProperties.length > 0) {
    // getPropertySettersLine returns an array of lines.  So we need to 
    // concatenate them (using join) before pushing them onto the Yail expression.
    // WARNING:  There may be other type errors of this sort in this file, which
    // (hopefully) will be uncovered in testing. Please
    // be alert for these errors and check carefully.
    code.push(Blockly.Yail.YAIL_DO_AFTER_FORM_CREATION + yailForComponentProperties.join(" ") + 
      Blockly.Yail.YAIL_CLOSE_BLOCK);
  }
  return code;
}

/**
 * Generate the code to set property values for the specifed component.
 *
 * @param {Object} componentJson JSON String describing the component
 * @param {String} componentName the name of the component (also present in the $Name field in
 *    componentJson)
 * @returns {Array} code strings
 * @private
 */
Blockly.Yail.getPropertySettersLines = function(componentJson, componentName) {
  var code = [];
  for (var prop in componentJson) {
    if (prop.charAt(0) != "$" && prop != "Uuid") {
      code.push(Blockly.Yail.getPropertySetterString(componentName, componentJson.$Type, prop, 
        componentJson[prop]));
    }
  }
  return code;
}

/**
 * Generate the code to set a single property value.
 *
 * @param {String} componentName
 * @param {String} propertyName
 * @param {String} propertyValue
 * @returns code string
 * @private
 */
Blockly.Yail.getPropertySetterString = function(componentName, componentType, propertyName, 
  propertyValue) {
  var code = Blockly.Yail.YAIL_SET_AND_COERCE_PROPERTY + Blockly.Yail.YAIL_QUOTE + 
    componentName + Blockly.Yail.YAIL_SPACER + Blockly.Yail.YAIL_QUOTE + propertyName + 
    Blockly.Yail.YAIL_SPACER;
  var propType = Blockly.Yail.YAIL_QUOTE + 
    Blockly.ComponentTypes[componentType].properties[propertyName].type;
  var value = Blockly.Yail.getPropertyValueString(propertyValue, propType);
  code = code.concat(value + Blockly.Yail.YAIL_SPACER + propType + Blockly.Yail.YAIL_CLOSE_BLOCK);
  return code;
}

/**
 * Generate the Yail code for a property value. Special case handling when propertyType is
 * "'number", "'boolean", "'component", the empty string, or null. For all other property values
 * it returns the value as converted by Blockly.Yail.quotifyForREPL().
 *
 * @param {String} propertyValue
 * @param {String} propertyType
 * @returns code string
 * @private
 */
Blockly.Yail.getPropertyValueString = function(propertyValue, propertyType) {
  if (propertyType == "'number") {
    if (propertyValue.match(Blockly.Yail.INTEGER_REGEXP) 
            || propertyValue.match(Blockly.Yail.FLONUM_REGEXP)) { // integer
      return propertyValue;
    } else if (propertyValue.match(Blockly.Yail.SIMPLE_HEX_PREFIX + "[0-9A-F]+")) { // hex
      return Blockly.Yail.YAIL_HEX_PREFIX + 
        propertyValue.substring(Blockly.Yail.SIMPLE_HEX_PREFIX.length);
    }
  } else if (propertyType == "'boolean") {
    if (-1 != propertyValue.indexOf("False")) {
      return "#f";
    } else if (-1 != propertyValue.indexOf("True")) {
      return "#t";
    }
  } else if (propertyType == "'component") {
    if (propertyValue == "") {
      return "\"\"";
    } else {
      return Blockly.Yail.YAIL_GET_COMPONENT + propertyValue + ")";
    }
  }

  if (propertyValue == "" || propertyValue == "null") {  // empty string
    return "\"\"";
  }
  return Blockly.Yail.quotifyForREPL(propertyValue);
}

/**
 * Generate the code to rename a component
 *
 * @param {String} oldName
 * @param {String} newName
 * @returns {String} code
 * @private
 */
Blockly.Yail.getComponentRenameString = function(oldName, newName) {
  return Blockly.Yail.YAIL_RENAME_COMPONENT + Blockly.Yail.quotifyForREPL(oldName)
    + Blockly.Yail.YAIL_SPACER + Blockly.Yail.quotifyForREPL(newName)
    + Blockly.Yail.YAIL_CLOSE_BLOCK;
}

/**
 * Transform a string to the Kawa input representation of the string, for sending to
 * the REPL, by using backslash to escape quotes and backslashes. But do not escape a backslash
 * if it is part of \n. Then enclose the result in quotes.
 * TODO: Extend this to a complete transformation that deals with the full set of formatting
 * characters.
 *
 * @param {String} s string to be quotified
 * @returns {String}
 * @private
 */
Blockly.Yail.quotifyForREPL = function(s) {
  if (!s) {
    return null;
  } else {
    var sb = [];
    sb.push('"');
    var len = s.length;
    var lastIndex = len - 1;
    for (var i = 0; i < len; i++) {
      c = s.charAt(i);
      if (c == '\\') {
        // If this is \n don't slashify the backslash
        // TODO(user): Make this cleaner and more general
        if (!(i == lastIndex) && s.charAt(i + 1) == 'n') {
          sb.push(c);
          sb.push(s.charAt(i + 1));
          i = i + 1;
        } else {
          sb.push('\\');
          sb.push(c);
        }
      } else if (c == '"') {
        sb.push('\\');
        sb.push(c);
      } else {
        var u = s.charCodeAt(i);  // unicode of c
        if (u < ' '.charCodeAt(0) || u > '~'.charCodeAt(0)) {
          // Replace any special chars with \u1234 unicode
          var hex = "000" + u.toString(16);
          hex = hex.substring(hex.length - 4);
          sb.push("\\u" + hex);
        } else {
          sb.push(c);
        }
      }
    }
    sb.push('"');
    return sb.join("");
  }
}

/**
 * Encode a string as a properly escaped Yail string, complete with quotes.
 * @param {String} string Text to encode.
 * @return {String} Yail string.
 * @private
 */

Blockly.Yail.quote_ = function(string) {
  string = Blockly.Yail.quotifyForREPL(string);
  if (!string) {                // quotifyForREPL can return null for
    string = '""';              // empty string
  }
  return string;
};

/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
Blockly.Yail.scrubNakedValue = function(line) {
  return line;
};

/**
 * Handles comments for the specified block and any connected value blocks.
 * Calls any statements following this block.
 * @param {!Blockly.Block} block The current block.
 * @param {string} code The Yail code created for this block.
 * @param {thisOnly} if true, only return code for this block and not any following statements
 *   note that calls of scrub_ with no 3rd parameter are equivalent to thisOnly=false, which
 *   was the behavior before this parameter was added.
 * @return {string} Yail code with comments and subsequent blocks added.
 * @private
 */
Blockly.Yail.scrub_ = function(block, code, thisOnly) {
  if (code === null) {
    // Block has handled code generation itself.
    return '';
  }
  var commentCode = '';
  /* TODO: fix for Yail comments?
  // Only collect comments for blocks that aren't inline.
  if (!block.outputConnection || !block.outputConnection.targetConnection) {
    // Collect comment for this block.
    var comment = block.getCommentText();
    if (comment) {
      commentCode += Blockly.Generator.prefixLines(comment, '// ') + '\n';
    }
    // Collect comments for all value arguments.
    // Don't collect comments for nested statements.
    for (var x = 0; x < block.inputList.length; x++) {
      if (block.inputList[x].type == Blockly.INPUT_VALUE) {
        var childBlock = block.inputList[x].targetBlock();
        if (childBlock) {
          var comment = Blockly.Generator.allNestedComments(childBlock);
          if (comment) {
            commentCode += Blockly.Generator.prefixLines(comment, '// ');
          }
        }
      }
    }
  }*/
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = thisOnly ? "" : this.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};

Blockly.Yail.getDebuggingYail = function() {
  var code = [];
  var componentMap = Blockly.Component.buildComponentMap([], [], false, false);
  
  var globalBlocks = componentMap.globals;
  for (var i = 0, block; block = globalBlocks[i]; i++) {
    code.push(Blockly.Yail.blockToCode(block));
  }
  
  var blocks = Blockly.mainWorkspace.getTopBlocks(true);
  for (var x = 0, block; block = blocks[x]; x++) {
    
    // generate Yail for each top-level language block
    if (!block.category) {
      continue;
    }
    code.push(Blockly.Yail.blockToCode(block));
  }
  return code.join('\n\n');
};

/**
 * Generate code for the specified block but *not* attached blocks.
 * @param {Blockly.Block} block The block to generate code for.
 * @return {string|!Array} For statement blocks, the generated code.
 *     For value blocks, an array containing the generated code and an
 *     operator order value.  Returns '' if block is null.
 */
Blockly.Yail.blockToCode1 = function(block) {
  if (!block) {
    return '';
  }
  var func = this[block.type];
  if (!func) {
    throw 'Language "' + name + '" does not know how to generate code ' +
        'for block type "' + block.type + '".';
  }
  var code = func.call(block);
  if (code instanceof Array) {
    // Value blocks return tuples of code and operator order.
    if (block.disabled || block.isBadBlock()) {
      code[0] = '';
    }
    return [this.scrub_(block, code[0], true), code[1]];
  } else {
    if (block.disabled || block.isBadBlock()) {
      code = '';
    }
    return this.scrub_(block, code, true);
  }
};
