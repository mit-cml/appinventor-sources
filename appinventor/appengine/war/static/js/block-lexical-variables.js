(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory(require("blockly/core"), require("blockly/javascript"));
	else if(typeof define === 'function' && define.amd)
		define(["blockly/core", "blockly/javascript"], factory);
	else {
		var a = typeof exports === 'object' ? factory(require("blockly/core"), require("blockly/javascript")) : factory(root["Blockly"], root["Blockly.JavaScript"]);
		for(var i in a) (typeof exports === 'object' ? exports : root)[i] = a[i];
	}
})(this, (__WEBPACK_EXTERNAL_MODULE__573__, __WEBPACK_EXTERNAL_MODULE__403__) => {
return /******/ (() => { // webpackBootstrap
/******/ 	"use strict";
/******/ 	var __webpack_modules__ = ({

/***/ 573:
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE__573__;

/***/ }),

/***/ 403:
/***/ ((module) => {

module.exports = __WEBPACK_EXTERNAL_MODULE__403__;

/***/ })

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			// no module.id needed
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		__webpack_modules__[moduleId](module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
/******/ 	/* webpack/runtime/define property getters */
/******/ 	(() => {
/******/ 		// define getter functions for harmony exports
/******/ 		__webpack_require__.d = (exports, definition) => {
/******/ 			for(var key in definition) {
/******/ 				if(__webpack_require__.o(definition, key) && !__webpack_require__.o(exports, key)) {
/******/ 					Object.defineProperty(exports, key, { enumerable: true, get: definition[key] });
/******/ 				}
/******/ 			}
/******/ 		};
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/hasOwnProperty shorthand */
/******/ 	(() => {
/******/ 		__webpack_require__.o = (obj, prop) => (Object.prototype.hasOwnProperty.call(obj, prop))
/******/ 	})();
/******/ 	
/******/ 	/* webpack/runtime/make namespace object */
/******/ 	(() => {
/******/ 		// define __esModule on exports
/******/ 		__webpack_require__.r = (exports) => {
/******/ 			if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 				Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 			}
/******/ 			Object.defineProperty(exports, '__esModule', { value: true });
/******/ 		};
/******/ 	})();
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be isolated against other modules in the chunk.
(() => {
// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXPORTS
__webpack_require__.d(__webpack_exports__, {
  "FieldFlydown": () => (/* reexport */ FieldFlydown),
  "FieldGlobalFlydown": () => (/* reexport */ FieldGlobalFlydown),
  "FieldLexicalVariable": () => (/* reexport */ FieldLexicalVariable),
  "FieldNoCheckDropdown": () => (/* reexport */ FieldNoCheckDropdown),
  "FieldParameterFlydown": () => (/* reexport */ FieldParameterFlydown),
  "FieldProcedureName": () => (/* reexport */ FieldProcedureName),
  "Flydown": () => (/* reexport */ Flydown),
  "LexicalVariable": () => (/* reexport */ LexicalVariable),
  "init": () => (/* binding */ init)
});

// NAMESPACE OBJECT: ./src/warningHandler.js
var warningHandler_namespaceObject = {};
__webpack_require__.r(warningHandler_namespaceObject);
__webpack_require__.d(warningHandler_namespaceObject, {
  "checkDropDownContainsValidValue": () => (checkDropDownContainsValidValue),
  "checkErrors": () => (checkErrors),
  "checkIsInDefinition": () => (checkIsInDefinition)
});

// EXTERNAL MODULE: external {"root":"Blockly","commonjs":"blockly/core","commonjs2":"blockly/core","amd":"blockly/core"}
var core_ = __webpack_require__(573);
;// CONCATENATED MODULE: ./src/css.js
/**
 * Array making up the extra CSS content for added Blockly fields.
 */var EXTRA_CSS="\n  .blocklyFieldParameter>rect {\n    fill: rgb(222, 143, 108);\n    fill-opacity: 1.0;\n    stroke-width: 2;\n    stroke: rgb(231, 175, 150);\n  }\n  .blocklyFieldParameter>text {\n    stroke-width: 1;\n    fill: #000;\n  }\n  .blocklyFieldParameter:hover>rect {\n    stroke-width: 2;\n    stroke: rgb(231,175,150);\n    fill: rgb(231,175,150);\n    fill-opacity: 1.0;\n  }\n  /*\n   * [lyn, 10/08/13] Control flydown with the getter/setter blocks.\n   */\n  .blocklyFieldParameterFlydown {\n    fill: rgb(231,175,150);\n    fill-opacity: 0.8;\n  }\n  /*\n   * [lyn, 10/08/13] Control parameter fields with flydown procedure\n   * caller block.\n   */\n  .blocklyFieldProcedure>rect {\n    fill: rgb(215,203,218);\n    fill-opacity: 1.0;\n    stroke-width: 0;\n    stroke: #000;\n  }\n  .blocklyFieldProcedure>text {\n    fill: #000;\n  }\n  .blocklyFieldProcedure:hover>rect {\n    stroke-width: 2;\n    stroke: #fff;\n    fill: rgb(215,203,218);\n    fill-opacity: 1.0;\n  }\n  /*\n   * [lyn, 10/08/13] Control flydown with the procedure caller block.\n   */\n  .blocklyFieldProcedureFlydown {\n    fill: rgb(215,203,218);\n    fill-opacity: 0.8;\n  }\n";/**
 * Register our extra CSS with Blockly.
 */function registerCss(){core_.Css.register(EXTRA_CSS);}
;// CONCATENATED MODULE: ./src/msg.js
/**
 * @license
 * Copyright 2021 Mark Friedman
 * SPDX-License-Identifier: Apache-2.0
 */ /**
 * @fileoverview Translatable messages used in lexical variable code.
 * @author mark.friedman@gmail.com (Mark Friedman)
 * Based on code from MIT App Inventor
 */core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT='initialize global';core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME='name';core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO='to';core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT='global';core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP='Creates a global variable and gives it the value of the attached blocks.';core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX='global';core_.Msg.LANG_VARIABLES_GET_TITLE_GET='get';core_.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT='get';core_.Msg.LANG_VARIABLES_GET_TOOLTIP='Returns the value of this variable.';core_.Msg.LANG_VARIABLES_SET_TITLE_SET='set';core_.Msg.LANG_VARIABLES_SET_TITLE_TO='to';core_.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT='set';core_.Msg.LANG_VARIABLES_SET_TOOLTIP='Sets this variable to be equal to the input.';core_.Msg.LANG_VARIABLES_VARIABLE=' variable';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT='initialize local';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME='name';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO='to';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO='in';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT='local';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP='Allows you to create variables that are only accessible in the do part'+' of this block.';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME='initialize local in do';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN='in';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT='local';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP='Allows you to create variables that are only accessible in the return'+' part of this block.';core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME='initialize local in return';core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES='local names';core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP='';core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME='name';core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE='x';core_.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE='to';core_.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE='procedure';core_.Msg.LANG_PROCEDURES_DEFNORETURN_DO='do';core_.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX='to ';core_.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP='A procedure that does not return a value.';core_.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN='result';core_.Msg.LANG_PROCEDURES_DOTHENRETURN_DO='do';core_.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN='result';core_.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP='Runs the blocks in \'do\' and returns a statement. Useful if you need'+' to run a procedure before returning a value to a variable.';core_.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT='do/result';core_.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE='to';core_.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE='procedure';core_.Msg.LANG_PROCEDURES_DEFRETURN_RETURN='result';core_.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX='to ';core_.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP='A procedure returning a result value.';core_.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING='Warning:\nThis procedure has\nduplicate inputs.';core_.Msg.LANG_PROCEDURES_CALLNORETURN_CALL='call ';core_.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE='procedure';core_.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX='call ';core_.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP='Call a procedure with no return value.';core_.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME='call no return';core_.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX='call ';core_.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP='Call a procedure with a return value.';core_.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME='call return';core_.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE='inputs';core_.Msg.LANG_PROCEDURES_MUTATORARG_TITLE='input:';core_.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF='Highlight Procedure';core_.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP='';core_.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP='';core_.Msg.LANG_CONTROLS_FOR_INPUT_WITH='count with';core_.Msg.LANG_CONTROLS_FOR_INPUT_VAR='x';core_.Msg.LANG_CONTROLS_FOR_INPUT_FROM='from';core_.Msg.LANG_CONTROLS_FOR_INPUT_TO='to';core_.Msg.LANG_CONTROLS_FOR_INPUT_DO='do';core_.Msg.LANG_CONTROLS_FOR_TOOLTIP='Count from a start number to an end number.\nFor each count, set the'+' current count number to\nvariable \'%1\', and then do some statements.';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM='for each';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR='number';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_START='from';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_END='to';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP='by';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO='do';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT='for number in range';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX='for';core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX=' in range';core_.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP='Runs the blocks in the \'do\' section for each numeric value in the'+' range from start to end, stepping the value each time.  Use the given'+' variable name to refer to the current value.';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM='for each';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR='item';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST='in list';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_DO='do';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT='for item in list';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX='for ';core_.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX=' in list';core_.Msg.LANG_CONTROLS_FOREACH_TOOLTIP='Runs the blocks in the \'do\'  section for each item in the list.  Use'+' the given variable name to refer to the current list item.';core_.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT='for each %1 with %2 in dictionary %3';core_.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_DO='do';core_.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_KEY='key';core_.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_VALUE='value';core_.Msg.LANG_CONTROLS_FOREACH_DICT_TITLE='for each in dictionary';core_.Msg.LANG_CONTROLS_FOREACH_DICT_TOOLTIP='Runs the blocks in the \'do\' section for each key-value entry in the'+' dictionary. Use the given variable names to refer to the key/value of'+' the current dictionary item.';core_.Msg.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN='Select a valid item in the drop down.';core_.Msg.ERROR_BLOCK_CANNOT_BE_IN_DEFINITION='This block cannot be in a definition';core_.Msg.HORIZONTAL_PARAMETERS='Arrange Parameters Horizontally';core_.Msg.VERTICAL_PARAMETERS='Arrange Parameters Vertically';core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO='do';core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN='result';core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP='Runs the blocks in \'do\' and returns a statement. Useful if you need to'+' run a procedure before returning a value to a variable.';core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT='do/result';core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE='do result';
;// CONCATENATED MODULE: ./src/utilities.js
/**
 * @fileoverview Block utilities for Blockly, modified for App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author hal@mit.edu (Hal Abelson)
 * @author fraser@google.com (Neil Fraser)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 *//**
 * Checks that the given otherConnection is compatible with an InstantInTime
 * connection. If the workspace is currently loading (eg the blocks are not
 * yet rendered) this always returns true for backwards compatibility.
 * @param {!Blockly.Connection} myConn The parent connection.
 * @param {!Blockly.Connection} otherConn The child connection.
 *
 * @return {boolean}
 */var InstantInTime=function InstantInTime(myConn,otherConn){if(!myConn.sourceBlock_.rendered||!otherConn.sourceBlock_.rendered){if(otherConn.check_&&!otherConn.check_.includes('InstantInTime')){otherConn.sourceBlock_.badBlock();}return true;}return!otherConn.check_||otherConn.check_.includes('InstantInTime');};// Convert Yail types to Blockly types
// Yail types are represented by strings: number, text, list, any, ...
// Blockly types are represented by objects: Number, String, ...
// and by the string "COMPONENT"
// The Yail type 'any' is repsented by Javascript null, to match
// Blockly's convention
var YailTypeToBlocklyTypeMap={'number':{'input':['Number'],'output':['Number','String','Key']},'text':{'input':['String'],'output':['Number','String','Key']},'boolean':{'input':['Boolean'],'output':['Boolean','String']},'list':{'input':['Array'],'output':['Array','String']},'component':{'input':['COMPONENT'],'output':['COMPONENT','Key']},'InstantInTime':{'input':['InstantInTime',InstantInTime],'output':['InstantInTime',InstantInTime]},'any':{'input':null,'output':null},'dictionary':{'input':['Dictionary'],'output':['Dictionary','String','Array']},'pair':{'input':['Pair'],'output':['Pair','String','Array']},'key':{'input':['Key'],'output':['String','Key']}};var OUTPUT='output';var INPUT='input';/**
 * Gets the equivalent Blockly type for a given Yail type.
 * @param {string} yail The Yail type.
 * @param {!string} inputOrOutput Either Utilities.OUTPUT or Utilities.INPUT.
 * @param {Array<string>=} opt_currentType A type array to append, or null.
 *
 * @return {string}
 */var yailTypeToBlocklyType=function yailTypeToBlocklyType(yail,inputOrOutput){var type=YailTypeToBlocklyTypeMap[yail][inputOrOutput];if(type===undefined){throw new Error('Unknown Yail type: '+yail+' -- YailTypeToBlocklyType');}return type;};// Blockly doesn't wrap tooltips, so these can get too wide.  We'll create our
// own tooltip setter that wraps to length 60.
var setTooltip=function setTooltip(block,tooltip){block.setTooltip(wrapSentence(tooltip,60));};// Wrap a string by splitting at spaces. Permit long chunks if there
// are no spaces.
var wrapSentence=function wrapSentence(str,len){str=str.trim();if(str.length<len)return str;var place=str.lastIndexOf(' ',len);if(place==-1){return str.substring(0,len).trim()+wrapSentence(str.substring(len),len);}else{return str.substring(0,place).trim()+'\n'+wrapSentence(str.substring(place),len);}};/**
 * Returns an array containing just the element children of the given element.
 * @param {Element} element The element whose element children we want.
 * @return {!(Array<!Element>|NodeList<!Element>)} An array or array-like list
 *     of just the element children of the given element.
 */var getChildren=function getChildren(element){'use strict';// We check if the children attribute is supported for child elements
// since IE8 misuses the attribute by also including comments.
if(element.children!==undefined){return element.children;}// Fall back to manually filtering the element's child nodes.
return Array.prototype.filter.call(element.childNodes,function(node){return node.nodeType==core_.utils.dom.NodeType.ELEMENT_NODE;});};
;// CONCATENATED MODULE: ./src/warningHandler.js
// -*- mode: javascript; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author mark.friedman@gmail.com (Mark Friedman)
 *//**
 * Takes a block as the context (this), puts the appropriate error or warning
 * on the block.
 * @param {Blockly.Block} block The block to check for errors.
 */var checkErrors=function checkErrors(block){if(block.getSvgRoot&&!block.getSvgRoot()||block.readOnly){// remove from error count
if(block.hasWarning){block.hasWarning=false;}if(block.hasError){block.hasError=false;}return;}// give the block empty arrays of errors and warnings to check if they aren't
// defined.
if(!block.errors){block.errors=[];}if(!block.warnings){block.warnings=[];}// add warnings and errors that are on every block
var errorTestArray=block.errors;var warningTestArray=block.warnings;// check if there are any errors
for(var i=0;i<errorTestArray.length;i++){if(errorTestArray[i].func&&errorTestArray[i].func.call(this,block,errorTestArray[i])){if(!block.hasError){block.hasError=true;}if(block.hasWarning){block.hasWarning=false;}return;}}if(block.hasError){block.hasError=false;}// if there are no errors, check for warnings
for(var _i=0;_i<warningTestArray.length;_i++){if(warningTestArray[_i].func&&warningTestArray[_i].func.call(this,block,warningTestArray[_i])){if(!block.hasWarning){block.hasWarning=true;}return;}}// remove the warning icon, if there is one
if(block.warning){block.setWarningText(null);}if(block.hasWarning){block.hasWarning=false;}};// Errors
// Errors indicate that the project will not run
// Each function returns true if there is an error, and sets the error text on
// the block
// Check if the block is inside of a variable declaration block, if so, create
// an error
var checkIsInDefinition=function checkIsInDefinition(block){// Allow property getters as they should be pure.
var rootBlock=block.getRootBlock();if(rootBlock.type==='global_declaration'){var errorMessage=core_.Msg.ERROR_BLOCK_CANNOT_BE_IN_DEFINITION;block.setWarningText(errorMessage);return true;}else{return false;}};// Check if the block has an invalid drop down value, if so, create an error
var checkDropDownContainsValidValue=function checkDropDownContainsValidValue(block,params){if(block.workspace.isDragging&&block.workspace.isDragging()){return false;// wait until the user is done dragging to check validity.
}for(var i=0;i<params.dropDowns.length;i++){var dropDown=block.getField(params.dropDowns[i]);var dropDownList=dropDown.menuGenerator_();var text=dropDown.getText();var value=dropDown.getValue();var textInDropDown=false;if(dropDown.updateMutation){dropDown.updateMutation();}for(var k=0;k<dropDownList.length;k++){if(dropDownList[k][1]===value&&value!==' '){textInDropDown=true;// A mismatch in the untranslated value and translated text can be
// corrected.
if(dropDownList[k][0]!==text){dropDown.setValue(dropDownList[k][0]);}break;}}if(!textInDropDown){var errorMessage=core_.Msg.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN;block.setWarningText(errorMessage);return true;}}return false;};
;// CONCATENATED MODULE: ./src/instrument.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for MIT App Inventor
 * Instrumentation (timing and statistics) for core blockly functionality
 * that's useful for figuring out where time is being spent.
 * Lyn used this in conjunction with Chrome profiling to speed up
 * loading, dragging, and expanding collapsed blocks in large projecsts.
 *
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */// TODO: Maybe just delete this file and delete all the instrumentation
// booleans since they all are set to true by default.
/** Is instrumentation turned on? */ // isOn = true;
var isOn=false;// [lyn, 04/08/14] Turn off for production
/**
 * Turn instrumentation on/off.
 * @param bool
 */var setOn=function setOn(bool){isOn=bool;};/** The following are global flags to control rendering.
 * The default settings give the best performance.
 * Other settings can be use to show slowdowns for different choices/algorithms.
 */ /**
 * [lyn, 04/01/14] Should we use the Blockly.Block.isRenderingOn flag?
 * Default value = true.
 */var useIsRenderingOn=true;/**
 * [lyn, 04/01/14] Should we avoid workspace render in Blockly.Block.onMouseUp_?
 * Default value = true.
 */var avoidRenderWorkspaceInMouseUp=true;/** [lyn, 04/01/14] Global flag to control rendering algorithm,
 * Used to show that renderDown() is better than render() in many situations.
 * Default value = true.
 */var useRenderDown=true;/** [lyn, 04/01/14] Should we avoid renderDown on subblocks of collapsed blocks
 * Default value = true.
 */var avoidRenderDownOnCollapsedSubblocks=true;/** [lyn, 04/01/14] Use Neil's fix to Blockly.Block.getHeightWidth, which
 * sidesteps the inexplicable quadratic problem with getHeightWidth.
 * Default value = true.
 */var useNeilGetHeightWidthFix=true;/** [lyn, 04/01/14] Use my fix to Blockly.Workspace.prototype.getAllBlocks,
 *  which avoids quadratic behavior in Neil's original version.
 */var useLynGetAllBlocksFix=true;/** [lyn, 04/01/14] Use my fix to Blockly.FieldLexicalVariable.getGlobalNames,
 *  which just looks at top blocks in workspace, and not all blocks.
 */var useLynGetGlobalNamesFix=true;/** [lyn, 04/01/14] In
 * Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors,
 * compute Blockly.FieldLexicalVariable.getGlobalNames only once and cache it
 * so that it needn't be computed again.
 */var useLynCacheGlobalNames=true;/** [lyn, 04/05/14] Stats to track improvements in slow removal */var stats={};var statNames=(/* unused pure expression or super */ null && (['totalTime','topBlockCount','blockCount','domToBlockCalls','domToBlockTime','domToBlockInnerCalls','domToWorkspaceCalls','domToWorkspaceTime','workspaceRenderCalls','workspaceRenderTime','renderCalls',// Hard to track without double counting because of its recursive nature. Use
// renderHereTime instead.
// "renderTime",
'renderDownCalls','renderDownTime','renderHereCalls','renderHereTime','getHeightWidthCalls','getHeightWidthTime','getTopBlocksCalls','getTopBlocksTime','getAllBlocksCalls','getAllBlocksTime','getAllBlocksAllocationCalls','getAllBlocksAllocationSpace','checkAllBlocksForWarningsAndErrorsCalls','checkAllBlocksForWarningsAndErrorsTime','scrollBarResizeCalls','scrollBarResizeTime','trashCanPositionCalls','trashCanPositionTime','expandCollapsedCalls','expandCollapsedTime']));var initializeStats=function initializeStats(name){if(isOn){console.log('Initializing stats for '+name);var names=statNames;var _stats=_stats;for(var i=0,_name;_name=names[i];i++){_stats[_name]=0;}}};var displayStats=function displayStats(name){if(isOn){var names=statNames;var _stats2=_stats2;console.log('Displaying stats for '+name+':');console.log('  Instrument.useRenderDown='+useRenderDown);console.log('  Instrument.useIsRenderingOn='+useIsRenderingOn);console.log('  Instrument.avoidRenderWorkspaceInMouseUp='+avoidRenderWorkspaceInMouseUp);console.log('  Instrument.avoidRenderDownOnCollapsedSubblocks='+avoidRenderDownOnCollapsedSubblocks);console.log('  Instrument.useNeilGetHeightWidthFix='+useNeilGetHeightWidthFix);console.log('  Instrument.useLynGetAllBlocksFix='+useLynGetAllBlocksFix);console.log('  Instrument.useLynGetGlobalNamesFix='+useLynGetGlobalNamesFix);console.log('  Instrument.useLynCacheGlobalNames='+useLynCacheGlobalNames);for(var i=0,_name2;_name2=names[i];i++){console.log('  '+_name2+'='+_stats2[_name2]);}}};var timer=function timer(thunk,callback){if(isOn){var start=new Date().getTime();var result=thunk();var stop=new Date().getTime();return callback(result,stop-start);}else{var _result=thunk();return callback(_result,0);}};
;// CONCATENATED MODULE: ./src/shared.js
/* [Added by paulmw in patch 15]
   There are three ways that you can change how lexical variables
   are handled:

   1. Show prefixes to users, and separate namespace in code
   Blockly.showPrefixToUser = true;
   Blockly.usePrefixInCode = true;

   2. Show prefixes to users, lexical variables share namespace code
   Blockly.showPrefixToUser = true;
   Blockly.usePrefixInCode = false;

   3. Hide prefixes from users, lexical variables share namespace code
   //The default (as of 12/21/12)
   Blockly.showPrefixToUser = false;
   Blockly.usePrefixInCode = false;

   It is not possible to hide the prefix and have separate namespaces
   because Blockly does not allow to items in a list to have the same name
   (plus it would be confusing...)

*/var showPrefixToUser=false;var usePrefixInCode=false;/** ****************************************************************************
 [lyn, 12/23-27/2012, patch 16]
 Prefix labels for parameters, locals, and index variables,
 Might want to experiment with different combintations of these. E.g.,
 maybe all non global parameters have prefix "local" or all have prefix "param".
 Maybe index variables have prefix "index", or maybe instead they are treated as
 "param".
 */ /**
 * The global keyword. Users may be shown a translated keyword instead but this
 * is the internal token used to identify global variables.
 * @type {string}
 * @const
 */ // used internally to identify global variables; not translated
var GLOBAL_KEYWORD='global';// For names introduced by procedure/function declarations
var procedureParameterPrefix='input';// For names introduced by event handlers
var handlerParameterPrefix='input';// For names introduced by local variable declarations
var localNamePrefix='local';// For names introduced by for loops
var loopParameterPrefix='item';// For names introduced by for range loops
var loopRangeParameterPrefix='counter';// Separate prefix from name with this. E.g., space in "param x"
var menuSeparator=' ';// Separate prefix from name with this. E.g., underscore "param_ x"
var prefixSeparator='_';// Curried for convenient use in field_lexical_variable.js
// e.g., "param x" vs "x"
var possiblyPrefixMenuNameWith=function possiblyPrefixMenuNameWith(prefix){return function(name){return(showPrefixToUser?prefix+menuSeparator:'')+name;};};var prefixGlobalMenuName=function prefixGlobalMenuName(name){return core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+menuSeparator+name;};// Return a list of (1) prefix (if it exists, "" if not) and (2) unprefixed name
var unprefixName=function unprefixName(name){if(name.indexOf(core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+menuSeparator)===0){// Globals always have prefix, regardless of flags. Handle these specially
return[core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX,name.substring(core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX.length+menuSeparator.length)];}else if(name.indexOf(GLOBAL_KEYWORD+menuSeparator)===0){return[GLOBAL_KEYWORD,name.substring(6+menuSeparator.length)];}else if(!showPrefixToUser){return['',name];}else{var prefixes=[procedureParameterPrefix,handlerParameterPrefix,localNamePrefix,loopParameterPrefix,loopRangeParameterPrefix];for(var i=0;i<prefixes.length;i++){if(name.indexOf(prefixes[i])===0){// name begins with prefix
return[prefixes[i],name.substring(prefixes[i].length+menuSeparator.length)];}}// Really an error if get here ...
return['',name];}};// Curried for convenient use in generators/lexical-variables.js
var possiblyPrefixGeneratedVarName=function possiblyPrefixGeneratedVarName(prefix){return function(name){// e.g., "param_x" vs "x"
return(usePrefixInCode?prefix+prefixSeparator:'')+name;};};
;// CONCATENATED MODULE: ./src/nameSet.js
/**
 * @fileoverview Represent sets of strings and numbers as JavaScript objects
 *   with an elements field that is itself an object mapping each element to true.
 * Note that ECMAScript 6 supports sets, but we cannot rely on sites using this recent a version
 * TODO(mark-friedman): We may now be safe to rewrite this using ECMAScript 6
 * @author fturbak@wellesley.edu (Lyn Turbak)
 *//**
 * History:
 * [lyn, 06/30/14] added to ai2inter (should also add to master)
 * [lyn, 11/16/13] created
 */ /**
 * Construct a set from a list. If no list is provided, construct the empty set.
 */var nameSet_NameSet=function NameSet(names){if(!names){names=[];}this.elements={};for(var i=0,name;name=names[i];i++){this.elements[name]=true;}};/**
 * Set membership
 * @param x: any value
 * @returns true if x is in set and false otherwise
 */nameSet_NameSet.prototype.isMember=function(x){return!!this.elements[x];// !! converts falsey to false
};/**
 * Set emptiness
 * @returns true if set is empty and false otherwise.
 */nameSet_NameSet.prototype.isEmpty=function(){for(var elt in this.elements){return false;}return true;};/**
 * Set size
 * @returns the number of elements in the set
 */nameSet_NameSet.prototype.size=function(){var size=0;for(var elt in this.elements){size++;}return size;};/**
 * Return a list (i.e. array) of names in this set, in lexicographic order.
 */nameSet_NameSet.prototype.toList=function(){var result=[];for(var elt in this.elements){result.push(elt);}return result.sort();};/**
 * @returns a string representation of this set.
 */nameSet_NameSet.prototype.toString=function(){return"NameSet{"+this.toList().join(",")+"}";};/**
 * Return a copy of this set
 */nameSet_NameSet.prototype.copy=function(){var result=new nameSet_NameSet();for(var elt in this.elements){result.insert(elt);}return result;};/**
 * Change this set to have the same elements as otherSet
 */nameSet_NameSet.prototype.mirror=function(otherSet){var elt;for(elt in this.elements){delete this.elements[elt];}for(elt in otherSet.elements){this.elements[elt]=true;}};/************************************************************
 * DESTRUCTIVE OPERATIONS
 * Change the existing set
 ************************************************************/ /**
 * Destructive set insertion
 * Insert x into the set. Does not complain if x already in the set.
 * @param x: any value
 */nameSet_NameSet.prototype.insert=function(x){this.elements[x]=true;};/**
 * Destructive set deletion.
 * Removes x from the set. Does not complain if x not in the set.
 * Note: This used to be called just "delete" but delete is a reserved
 * word, so we call this deleteName instead
 *
 * @param x: any value
 */nameSet_NameSet.prototype.deleteName=function(x){delete this.elements[x];};/**
 * Destructive set union
 * Change this set to have the union of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */nameSet_NameSet.prototype.unite=function(otherSet){for(var elt in otherSet.elements){this.elements[elt]=true;}};/**
 * Destructive set intersection
 * Change this set to have the intersection of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */nameSet_NameSet.prototype.intersect=function(otherSet){for(var elt in this.elements){if(!otherSet.elements[elt]){delete this.elements[elt];}}};/**
 * Destructive set difference
 * Change this set to have the difference of its elements with the elements of the other set
 * @param otherSet: a NameSet
 */nameSet_NameSet.prototype.subtract=function(otherSet){for(var elt in this.elements){if(otherSet.elements[elt]){delete this.elements[elt];}}};/**
 * Destructive set renaming
 * Modifies existing set to rename those elements that are in the given renaming.
 * Since multiple elements may rename to the same element, this may reduce the
 * size of the set.
 * @param substitution: a substitution mapping old names to new names
 *
 */nameSet_NameSet.prototype.rename=function(substitution){this.mirror(this.renamed(substitution));};/************************************************************
 * NONDESTRUCTIVE OPERATIONS
 * Return new sets/lists/strings
 ************************************************************/ /**
 * Nondestructive set insertion
 * Insert x into the set. Does not complain if x already in the set.
 * @param x: any value
 */nameSet_NameSet.prototype.insertion=function(x){var result=this.copy();result.insert(x);return result;};/**
 * Nondestructive set deletion.
 * Returns a new set containing the elements of this set except for x.
 * * @param x: any value
 */nameSet_NameSet.prototype.deletion=function(x){var result=this.copy();result.deleteName(x);return result;};/**
 * Nondestructive set union
 * @param otherSet: a NameSet
 * @returns a new set that is the union of this set and the other set.
 */nameSet_NameSet.prototype.union=function(otherSet){var result=this.copy();result.unite(otherSet);return result;};/**
 * Nondestructive set intersection
 * @param otherSet: a NameSet
 * @returns a new set that is the intersection of this set and the other set.
 */nameSet_NameSet.prototype.intersection=function(otherSet){var result=this.copy();result.intersect(otherSet);return result;};/**
 * Nondestructive set difference
 * @param otherSet: a NameSet
 * @returns a new set that is the differences of this set and the other set.
 */nameSet_NameSet.prototype.difference=function(otherSet){var result=this.copy();result.subtract(otherSet);return result;};/**
 * @param substitution: a substitution mapping old names to new names
 * @returns a new set that renames the elements of this set using the given renaming.
 * If a name is not in the dictionary, it is inserted unchange in the output set.
 */nameSet_NameSet.prototype.renamed=function(substitution){var result=new nameSet_NameSet();for(var elt in this.elements){var renamedElt=substitution.apply(elt);if(renamedElt){result.insert(renamedElt);}else{result.insert(elt);}}return result;};/**
 * @param setList: an array of NameSets
 * @returns a NameSet that is the union of all the given sets
 */nameSet_NameSet.unionAll=function(setList){var result=new nameSet_NameSet();for(var i=0,oneSet;oneSet=setList[i];i++){result.unite(oneSet);}return result;};/**
 * @param setList: an array of NameSets
 * @returns a NameSet that is the intersection of all the given sets
 */nameSet_NameSet.intersectAll=function(setList){if(setList.length===0){return new nameSet_NameSet();}else{var result=setList[0];for(var i=1,oneSet;oneSet=setList[i];i++){result.intersect(oneSet);}return result;}};
;// CONCATENATED MODULE: ./src/substitution.js
function _typeof(obj){"@babel/helpers - typeof";return _typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},_typeof(obj);}/**
 * @fileoverview A substitution is an abstract set of input/output name pairs used for renaming.
 * The inputs form the domain of the substitution; the outputs form the range.
 * Applying a substitution to a name that's an input in its domain maps it to the associated output;
 * Applying a substitution to a name that's not in its domain returns the name unchanged.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */ /**
 * History:
 * [lyn, 06/30/14] added to ai2inter (should also add to master)
 * [lyn, 11/16-17/13] created
 */ /**
 * If the arguments are two equal-length arrays of strings, construct a substitution
 *    by creating a bindings object that maps input strings from the first array
 *    to corresponding strings in the second.
 * If the argument is a single object describing the input/output bindings,
 *   construct a substitution from that.
 * If all other cases (e.g., no argument is provided)/ construct the empty substitution.
 */var Substitution=function Substitution(arg1,arg2){this.bindings={};// empty substitution is default.
// Test that arg1 and arg2 are equal length arrays of strings
if(Substitution.isAllStringsArray(arg2)&&Substitution.isAllStringsArray(arg1)&&arg1.length===arg2.length){for(var i=0;i<arg1.length;i++){this.bindings[arg1[i]]=arg2[i];}}else if(!arg2&&Substitution.isBindingsObject(arg1)){// Make a copy of the bindings so not sharing binding structure with argument.
this.bindings={};for(var oldName in arg1){this.bindings[oldName]=arg1[oldName];}}};/**
 * @param things
 * @returns true iff things is an array containing only strings. Otherwise returns false.
 */Substitution.isAllStringsArray=function(things){// [lyn, 11/17/13] This fails for things that are obviously arrays. Dunno why
// if (!(things instanceof Array)) {
//  return false;
//}
if(_typeof(things)!=="object"||!things.length){// Say it's not an array if it's not an object with a length field.
return false;}for(var i=0;i<things.length;i++){if(typeof things[i]!=="string"){return false;}}return true;};/**
 * @param obj An object
 * @returns true iff obj is an Object containting only string properties with string values.
 *   Otherwise returns false.
 */Substitution.isBindingsObject=function(thing){// [lyn, 11/17/13] This fails for things that are obviously Objects. Dunno why
// if (!(obj instanceof Object)) {
//  return false;
if(_typeof(thing)!="object"){return false;}else{for(var prop in thing){if(!(typeof prop==="string")||!_typeof(thing[prop]==="string")){return false;}}}return true;};/**
 * @param oldName
 * @param newName
 * @returns {Substitution} A substitution with one pair from oldName to newName
 */Substitution.simpleSubstitution=function(oldName,newName){var bindings={};bindings[oldName]=newName;return new Substitution(bindings);};/**
 * Apply a substitution to a name.
 * @param name: a string
 * @returns if the name is in the domain of the substition, returns the corresponding
 *   element in the range; otherwise, returns name unchanged.
 */Substitution.prototype.apply=function(name){var output=this.bindings[name];if(output){return output;}else{return name;}};/**
 * @param names: A list of strings
 * @returns {Array of strings} the result of applying this substitution to each element of names
 */Substitution.prototype.map=function(names){var thisSubst=this;// Need to name "this" for use in function closure passed to map.
return names.map(function(name){return thisSubst.apply(name);});};/**
 * @returns {string} A string representation of this substitution
 */Substitution.prototype.toString=function(){var bindingStrings=[];for(var oldName in this.bindings){bindingStrings.push(oldName+":"+this.bindings[oldName]);}return"Substitution{"+bindingStrings.sort().join(",")+"}";};/**
 * @returns {Substitution} a new copy of this substitution
 */Substitution.prototype.copy=function(){var newSubst=new Substitution();for(var oldName in this.bindings){newSubst.bindings[oldName]=this.bindings[oldName];}return newSubst;};/**
 * @param names: A list of strings
 * @returns {Substitution} a new substitution whose domain is the intersection of
 *   names and the domain of this substitution.
 */Substitution.prototype.restrictDomain=function(names){var newSubst=new Substitution();for(var i=0;i<names.length;i++){var result=this.bindings[names[i]];if(result){newSubst.bindings[names[i]]=result;}}return newSubst;};/**
 * @param names: A list of strings
 * @returns {Substitution} a new substitution whose domain is the difference of
 *   the domain of this substitution and names
 */Substitution.prototype.remove=function(names){var newSubst=new Substitution();for(var oldName in this.bindings){if(names.indexOf(oldName)==-1){newSubst.bindings[oldName]=this.bindings[oldName];}}return newSubst;};/**
 * @param otherSubst: A substitution
 * @returns {Substitution} a new substitution whose domain is the union of the domains of
 *   this substitution and otherSubst. Any input/output mapping in otherSubst whose
 *   input is in this substitution overrides the input in this substitution.
 */Substitution.prototype.extend=function(otherSubst){var newSubst=this.copy();for(var oldName in otherSubst.bindings){newSubst.bindings[oldName]=otherSubst.bindings[oldName];}return newSubst;};/**
 * @returns {Array of String} a list of all the old names in the domain of this substitution.
 */Substitution.prototype.domain=function(){var oldNames=[];for(var oldName in this.bindings){oldNames.push(oldName);}return oldNames.sort();};/**
 * @returns {Array of String} a copy of the input/output bindings in this substitution.
 */Substitution.prototype.getBindings=function(){var bindings={};for(var oldName in this.bindings){bindings[oldName]=this.bindings[oldName];}return bindings;};
;// CONCATENATED MODULE: ./src/fields/field_lexical_variable.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Drop-down chooser of variables in the current lexical scope
 *     for App Inventor.
 * @author fturbak@wellesley.com (Lyn Turbak)
 */function field_lexical_variable_typeof(obj){"@babel/helpers - typeof";return field_lexical_variable_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_lexical_variable_typeof(obj);}function _toConsumableArray(arr){return _arrayWithoutHoles(arr)||_iterableToArray(arr)||_unsupportedIterableToArray(arr)||_nonIterableSpread();}function _nonIterableSpread(){throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");}function _unsupportedIterableToArray(o,minLen){if(!o)return;if(typeof o==="string")return _arrayLikeToArray(o,minLen);var n=Object.prototype.toString.call(o).slice(8,-1);if(n==="Object"&&o.constructor)n=o.constructor.name;if(n==="Map"||n==="Set")return Array.from(o);if(n==="Arguments"||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n))return _arrayLikeToArray(o,minLen);}function _iterableToArray(iter){if(typeof Symbol!=="undefined"&&iter[Symbol.iterator]!=null||iter["@@iterator"]!=null)return Array.from(iter);}function _arrayWithoutHoles(arr){if(Array.isArray(arr))return _arrayLikeToArray(arr);}function _arrayLikeToArray(arr,len){if(len==null||len>arr.length)len=arr.length;for(var i=0,arr2=new Array(len);i<len;i++)arr2[i]=arr[i];return arr2;}function _defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,_toPropertyKey(descriptor.key),descriptor);}}function _createClass(Constructor,protoProps,staticProps){if(protoProps)_defineProperties(Constructor.prototype,protoProps);if(staticProps)_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function _toPropertyKey(arg){var key=_toPrimitive(arg,"string");return field_lexical_variable_typeof(key)==="symbol"?key:String(key);}function _toPrimitive(input,hint){if(field_lexical_variable_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_lexical_variable_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function _classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function _inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)_setPrototypeOf(subClass,superClass);}function _setPrototypeOf(o,p){_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return _setPrototypeOf(o,p);}function _createSuper(Derived){var hasNativeReflectConstruct=_isNativeReflectConstruct();return function _createSuperInternal(){var Super=_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return _possibleConstructorReturn(this,result);};}function _possibleConstructorReturn(self,call){if(call&&(field_lexical_variable_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return _assertThisInitialized(self);}function _assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function _isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function _getPrototypeOf(o){_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return _getPrototypeOf(o);}/**
 * Lyn's History:
 *  *  [lyn, written 11/15-17/13 but added 07/01/14] Overhauled parameter
 * renaming:
 *    + Refactored FieldLexicalVariable method getNamesInScope to have same
 * named function that works on any block, and a separate function
 * getLexicalNamesInScope that works on any block
 *    + Refactored monolithic renameParam into parts that are useful on their
 * own
 *    + Previously, renaming param from oldName to newName might require
 * renaming newName itself
 *      (adding a number to the end) to avoid renaming inner declarations that
 * might be captured by renaming oldName to newName. Now, there is a choice
 * between this previous behavior and a new behavior in which newName is *not*
 * renamed and capture is avoided by renaming the inner declarations when
 * necessary.
 *    + Created Blockly.Lexical.freeVariables for calculating free variables
 *    + Created Blockly.Lexical.renameBound for renaming of boundVariables in
 * declarations
 *    + Created Blockly.Lexical.renameFree for renaming of freeVariables in
 * declarations
 *    + Created LexicalVariable.stringListsEqual for testing equality
 * of string lists.
 *  [lyn, 06/11/14] Modify checkIdentifier to work for i8n.
 *  [lyn, 04/13/14] Modify calculation of global variable names:
 *    1. Use getTopBlocks rather than getAllBlocks; the latter, in combination
 * with quadratic memory allocation space from Neil's getAllBlocks, was leading
 * to cubic memory allocation space, which led to lots of time wasted due to
 * allocation and GC. This change dramatically reduces allocation times and GC
 *    2. Introduce caching for
 * Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors(). This change
 * reduces allocation times and GC even further.
 *  [lyn, 10/28/13] Made identifier legality check more restrictive by removing
 * arithmetic and logical ops as possible identifier characters
 *  [lyn, 10/27/13] Create legality filter & transformer for AI2 variable names
 *  [lyn, 10/26/13] Fixed renaming of globals and lexical vars involving empty
 * strings and names with internal spaces.
 *  [lyn, 12/23-27/12] Updated to:
 *     (1) handle renaming involving local declaration statements/expressions
 * and
 *     (2) treat name prefixes correctly when they're used.
 *  [lyn, 11/29/12] handle control constructs in getNamesInScope and
 * referenceResult
 *  [lyn, 11/24/12] Sort and remove duplicates from namespaces
 *  [lyn, 11/19/12]
 *    + renameGlobal renames global references and prevents duplicates in
 * global names;
 *    + renameParam is similar for procedure and loop names.
 *    + define referenceResult, which is renaming workhorse
 *  [lyn, 11/18/12] nameNotIn function for renaming by adding number at end
 *  [lyn, 11/10/12] getGlobalNames and getNamesInScope.
 */ // Get all global names
/**
 * Class for a variable's dropdown field.
 * @param {!string} varname The default name for the variable.  If null,
 *     a unique variable name will be generated.
 * @extends Blockly.FieldDropdown
 * @constructor
 */var FieldLexicalVariable=/*#__PURE__*/function(_Blockly$FieldDropdow){_inherits(FieldLexicalVariable,_Blockly$FieldDropdow);var _super=_createSuper(FieldLexicalVariable);function FieldLexicalVariable(varname){var _this;_classCallCheck(this,FieldLexicalVariable);// Call parent's constructor.
_this=_super.call(this,FieldLexicalVariable.dropdownCreate,FieldLexicalVariable.dropdownChange);if(varname){_this.doValueUpdate_(varname);}else{_this.doValueUpdate_(core_.Variables.generateUniqueName());}return _this;}return _createClass(FieldLexicalVariable);}(core_.FieldDropdown);FieldLexicalVariable.prototype.doClassValidation_=function(opt_newValue){return(/** @type {string} */opt_newValue);};/**
 * Get the block holding this drop-down variable chooser.
 * @return {string} Block holding this drop-down variable chooser.
 */FieldLexicalVariable.prototype.getBlock=function(){return this.block_;};/**
 * Set the block holding this drop-down variable chooser.
 * @param {?Blockly.Block} block Block holding this drop-down variable chooser.
 */FieldLexicalVariable.prototype.setBlock=function(block){this.block_=block;};// [lyn, 11/10/12] Returns the names of all global definitions as a list of
// strings [lyn, 11/18/12] * Removed from prototype and stripped off "global"
// prefix (add it elsewhere) * Add optional excluded block argument as in
// Neil's code to avoid global declaration being created
FieldLexicalVariable.getGlobalNames=function(optExcludedBlock){// TODO: Maybe switch to injectable warning/error handling
if(useLynCacheGlobalNames&&core_.common.getMainWorkspace()&&core_.common.getMainWorkspace().getWarningHandler&&core_.common.getMainWorkspace().getWarningHandler().cacheGlobalNames){return core_.common.getMainWorkspace().getWarningHandler().cachedGlobalNames;}var globals=[];if(core_.common.getMainWorkspace()){var blocks=[];if(useLynGetGlobalNamesFix){// [lyn, 04/13/14] Only need top blocks, not all blocks!
blocks=core_.common.getMainWorkspace().getTopBlocks();}else{// [lyn, 11/10/12] Is there a better way to get workspace?
blocks=core_.common.getMainWorkspace().getAllBlocks();}for(var i=0;i<blocks.length;i++){var block=blocks[i];if(block.getGlobalNames&&block!=optExcludedBlock){globals.push.apply(globals,_toConsumableArray(block.getGlobalNames()));}}}return globals;};/**
 * @this A FieldLexicalVariable instance
 * @return {list} A list of all global and lexical names in scope at the point
 *     of the getter/setter block containing this FieldLexicalVariable
 *     instance. Global names are listed in sorted order before lexical names
 *     in sorted order.
 */ // [lyn, 12/24/12] Clean up of name prefixes; most work done earlier by paulmw
// [lyn, 11/29/12] Now handle params in control constructs
// [lyn, 11/18/12] Clarified structure of namespaces
// [lyn, 11/17/12]
// * Commented out loop params because AI doesn't handle loop variables
// correctly yet. [lyn, 11/10/12] Returns the names of all names in lexical
// scope for the block associated with this menu. including global variable
// names. * Each global name is prefixed with "global " * If
// Shared.showPrefixToUser is false, non-global names are not prefixed. * If
// Shared.showPrefixToUser is true, non-global names are prefixed with labels
// specified in blocklyeditor.js
FieldLexicalVariable.prototype.getNamesInScope=function(){return FieldLexicalVariable.getNamesInScope(this.block_);};/**
 * @param block
 * @return {Array.<Array.<string>>} A list of pairs representing the translated
 * and untranslated name of every variable in the scope of the current block.
 */ // [lyn, 11/15/13] Refactored to work on any block
FieldLexicalVariable.getNamesInScope=function(block){var globalNames=FieldLexicalVariable.getGlobalNames();// from
// global
// variable
// declarations
// [lyn, 11/24/12] Sort and remove duplicates from namespaces
globalNames=LexicalVariable.sortAndRemoveDuplicates(globalNames);globalNames=globalNames.map(function(name){return[prefixGlobalMenuName(name),'global '+name];});var allLexicalNames=FieldLexicalVariable.getLexicalNamesInScope(block);// Return a list of all names in scope: global names followed by lexical ones.
return globalNames.concat(allLexicalNames);};/**
 * @param block
 * @return {Array.<Array.<string>>} A list of all lexical names (in sorted
 *     order) in scope at the point of the given block If
 *     Shared.usePrefixInYail is true, returns names prefixed with labels like
 *     "param", "local", "index"; otherwise returns unprefixed names.
 */ // [lyn, 11/15/13] Factored this out from getNamesInScope to work on any block
FieldLexicalVariable.getLexicalNamesInScope=function(block){// const procedureParamNames = []; // from procedure/function declarations
// const loopNames = []; // from for loops
// const rangeNames = []; // from range loops
// const localNames = []; // from local variable declaration
var allLexicalNames=[];// all non-global names
var innermostPrefix={};// paulmw's mechanism for keeping track of
// innermost prefix in case
// where prefix is an annotation rather than a separate namespace
var parent;var child;// let params;
// let i;
// [lyn, 12/24/2012] Abstract over name handling
/**
   * @param name
   * @param list
   * @param prefix
   */function rememberName(name,list,prefix){var fullName;if(!usePrefixInCode){// Only a single namespace
if(!innermostPrefix[name]){// only set this if not already set from an inner scope.
innermostPrefix[name]=prefix;}fullName=possiblyPrefixMenuNameWith(innermostPrefix[name])(name);}else{// multiple namespaces distinguished by prefixes
// note: correctly handles case where some prefixes are the same
fullName=possiblyPrefixMenuNameWith(prefix)(name);}list.push(fullName);}child=block;if(child){parent=child.getParent();if(parent){while(parent){if(parent.withLexicalVarsAndPrefix){parent.withLexicalVarsAndPrefix(child,function(lexVar,prefix){rememberName(lexVar,allLexicalNames,prefix);});}child=parent;parent=parent.getParent();// keep moving up the chain.
}}}allLexicalNames=LexicalVariable.sortAndRemoveDuplicates(allLexicalNames);return allLexicalNames.map(function(name){return[name,name];});};/**
 * Return a sorted list of variable names for variable dropdown menus.
 * @return {!Array.<string>} Array of variable names.
 * @this {!FieldLexicalVariable}
 */FieldLexicalVariable.dropdownCreate=function(){var variableList=this.getNamesInScope();// [lyn, 11/10/12] Get all
// global, parameter, and local
// names
return variableList.length==0?[[' ',' ']]:variableList;};/*
TODO: I'm leaving the following in for now (but commented) because at one point
  it seemed necessary.  It doesn't seem so anymore but I just want to remember
  in case it really was needed.
*/ // Blockly.FieldLexicalVariable.dropdownCreate = function() {
//   var variableList = this.getNamesInScope(); // [lyn, 11/10/12] Get all
// global, parameter, and local names variableList = variableList.length == 0 ?
// [[' ', ' ']] : variableList; const extraNames = []; const block =
// this.getBlock(); if (block) { const flydown =
// block.workspace.getTopWorkspace().getFlydown(); const flydownWorkspace =
// flydown.getWorkspace(); if (flydownWorkspace === block.workspace) { const
// currentVariableName = flydown.field_.getValue();
// extraNames.push([currentVariableName, currentVariableName]); } } return
// variableList.concat(extraNames); };
/**
 * Update the value of this dropdown field.
 * @param {*} newValue The value to be saved.
 * @protected
 */FieldLexicalVariable.prototype.doValueUpdate_=function(newValue){// The original call for the following looked like:
//   Blockly.FieldDropdown.superClass_.doValueUpdate_.call(this, newValue);
// but we can no longer use the Blockly.utils.object.inherits function, which sets the superclass_ property
// Note that if we just want the grandparent version of doValueUpdate_ we could use the following instead:
//   Object.getPrototypeOf(Object.getPrototypeOf(Object.getPrototypeOf(this))).doValueUpdate_(newValue);
// but since the original directly referenced the parent/superclass of Blockly.FieldDropdown, we do the same.
Object.getPrototypeOf(core_.FieldDropdown).prototype.doValueUpdate_.call(this,newValue);function genLocalizedValue(value){return value.startsWith('global ')?value.replace('global ',core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+' '):value;}// Fix for issue #1901. If the variable name contains a space separating two
// words, and the first isn't "global", then replace the first word with
// global. This fixes an issue where the translated "global" keyword was
// being stored instead of the English keyword, resulting in errors when
// moving between languages in the App Inventor UI. NB: This makes an
// assumption that we won't allow for multi-word variables in the future.
// Right now variables identifiers still need to be a sequence of
// non-whitespace characters, so only global variables will split on a space.
if(newValue&&newValue!==' '){var parts=newValue.split(' ');if(parts.length==2&&parts[0]!=='global'){newValue='global '+parts[1];}}this.value_=newValue;// Note that we are asking getOptions to add newValue to the list of available
// options.  We do that essentially to force callers up the chain to accept
// newValue as an option.  This could potentially cause trouble, but it seems
// to be ok for our use case.  It is ugly, though, since it bypasses an aspect
// of the normal dropdown validation.
var options=this.getOptions(true,[[genLocalizedValue(newValue),newValue]]);for(var i=0,option;option=options[i];i++){if(option[1]==this.value_){this.selectedOption_=option;break;}}this.forceRerender();};/**
 * Return a list of the options for this dropdown.
 * @param {boolean=} opt_useCache For dynamic options, whether or not to use the
 *     cached options or to re-generate them.
 * @param {boolean=} opt_extraOption A possible extra option to add.
 * @return {!Array<!Array>} A non-empty array of option tuples:
 *     (human-readable text or image, language-neutral name).
 * @throws {TypeError} If generated options are incorrectly structured.
 */FieldLexicalVariable.prototype.getOptions=function(opt_useCache,opt_extraOption){if(Array.isArray(opt_useCache)){opt_extraOption=opt_useCache;}var extraOption=opt_extraOption||[];if(this.isOptionListDynamic()){if(!this.generatedOptions_||!opt_useCache){this.generatedOptions_=this.menuGenerator_.call(this).concat(extraOption);validateOptions(this.generatedOptions_);}return this.generatedOptions_.concat(extraOption);}return(/** @type {!Array<!Array<string>>} */this.menuGenerator_);};// validateOptions copied from Blockly source since it's not exported from
// field_dropdown.js
/**
 * Validates the data structure to be processed as an options list.
 * @param {?} options The proposed dropdown options.
 * @throws {TypeError} If proposed options are incorrectly structured.
 */var validateOptions=function validateOptions(options){if(!Array.isArray(options)){throw TypeError('FieldDropdown options must be an array.');}if(!options.length){throw TypeError('FieldDropdown options must not be an empty array.');}var foundError=false;for(var i=0;i<options.length;++i){var tuple=options[i];if(!Array.isArray(tuple)){foundError=true;console.error('Invalid option['+i+']: Each FieldDropdown option must be an '+'array. Found: ',tuple);}else if(typeof tuple[1]!='string'){foundError=true;console.error('Invalid option['+i+']: Each FieldDropdown option id must be '+'a string. Found '+tuple[1]+' in: ',tuple);}else if(tuple[0]&&typeof tuple[0]!='string'&&typeof tuple[0].src!='string'){foundError=true;console.error('Invalid option['+i+']: Each FieldDropdown option must have a '+'string label or image description. Found'+tuple[0]+' in: ',tuple);}}if(foundError){throw TypeError('Found invalid FieldDropdown options.');}};/**
 * Event handler for a change in variable name.
 * // [lyn, 11/10/12] *** Not clear this needs to do anything for lexically
 * scoped variables. Special case the 'New variable...' and 'Rename
 * variable...' options. In both of these special cases, prompt the user for a
 * new name.
 * @param {string} text The selected dropdown menu option.
 * @this {!FieldLexicalVariable}
 */FieldLexicalVariable.dropdownChange=function(text){if(text){this.doValueUpdate_(text);var topWorkspace=this.sourceBlock_.workspace.getTopWorkspace();if(topWorkspace.getWarningHandler){topWorkspace.getWarningHandler().checkErrors(this.sourceBlock_);}}// window.setTimeout(Blockly.Variables.refreshFlyoutCategory, 1);
};// [lyn, 11/18/12]
/**
 * Possibly add a digit to name to disintguish it from names in list.
 * Used to guarantee that two names aren't the same in situations that prohibit
 * this.
 * @param {string} name Proposed name.
 * @param {string list} nameList List of names with which name can't conflict.
 * @return {string} Non-colliding name.
 */FieldLexicalVariable.nameNotIn=function(name,nameList){// First find the nonempty digit suffixes of all names in nameList that have
// the same prefix as name e.g. for name "foo3" and nameList = ["foo",
// "bar4", "foo17", "bar" "foo5"] suffixes is ["17", "5"]
var namePrefixSuffix=FieldLexicalVariable.prefixSuffix(name);var namePrefix=namePrefixSuffix[0];var nameSuffix=namePrefixSuffix[1];var emptySuffixUsed=false;// Tracks whether "" is a suffix.
var isConflict=false;// Tracks whether nameSuffix is used
var suffixes=[];for(var i=0;i<nameList.length;i++){var prefixSuffix=FieldLexicalVariable.prefixSuffix(nameList[i]);var prefix=prefixSuffix[0];var suffix=prefixSuffix[1];if(prefix===namePrefix){if(suffix===nameSuffix){isConflict=true;}if(suffix===''){emptySuffixUsed=true;}else{suffixes.push(suffix);}}}if(!isConflict){// There is no conflict; just return name
return name;}else if(!emptySuffixUsed){// There is a conflict, but empty suffix not used, so use that
return namePrefix;}else{// There is a possible conflict and empty suffix is not an option.
// First sort the suffixes as numbers from low to high
var suffixesAsNumbers=suffixes.map(function(elt,i,arr){return parseInt(elt,10);});suffixesAsNumbers.sort(function(a,b){return a-b;});// Now find smallest number >= 2 that is unused
var smallest=2;// Don't allow 0 or 1 an indices
var index=0;while(index<suffixesAsNumbers.length){if(smallest<suffixesAsNumbers[index]){return namePrefix+smallest;}else if(smallest==suffixesAsNumbers[index]){smallest++;index++;}else{// smallest is greater; move on to next one
index++;}}// Only get here if exit loop
return namePrefix+smallest;}};/**
 * Split name into digit suffix and prefix before it.
 * Return two-element list of prefix and suffix strings. Suffix is empty if no
 * digits.
 * @param {string} name Input string.
 * @return {string[]} Two-element list of prefix and suffix.
 */FieldLexicalVariable.prefixSuffix=function(name){var matchResult=name.match(/^(.*?)(\d+)$/);if(matchResult){// List of prefix and suffix
return[matchResult[1],matchResult[2]];}else{return[name,''];}};/**
 * Constructs a FieldLexicalVariable from a JSON arg object.
 * @param {!Object} options A JSON object with options.
 * @return {FieldLexicalVariable} The new field instance.
 * @package
 * @nocollapse
 */FieldLexicalVariable.fromJson=function(options){var name=core_.utils.replaceMessageReferences(options['name']);return new FieldLexicalVariable(name);};core_.fieldRegistry.register('field_lexical_variable',FieldLexicalVariable);var LexicalVariable={};// [lyn, 11/19/12] Rename global to a new name.
//
// [lyn, 10/26/13] Modified to replace sequences of internal spaces by
// underscores (none were allowed before), and to replace empty string by '_'.
// Without special handling of empty string, the connection between a
// declaration field and its references is lots.
LexicalVariable.renameGlobal=function(newName){// this is bound to field_textinput object
var oldName=this.value_;// [lyn, 10/27/13] now check legality of identifiers
newName=LexicalVariable.makeLegalIdentifier(newName);this.sourceBlock_.getField('NAME').doValueUpdate_(newName);var globals=FieldLexicalVariable.getGlobalNames(this.sourceBlock_);// this.sourceBlock excludes block being renamed from consideration
// Potentially rename declaration against other occurrences
newName=FieldLexicalVariable.nameNotIn(newName,globals);if(this.sourceBlock_.rendered){// Rename getters and setters
if(core_.common.getMainWorkspace()){var blocks=core_.common.getMainWorkspace().getAllBlocks();for(var i=0;i<blocks.length;i++){var block=blocks[i];var renamingFunction=block.renameLexicalVar;if(renamingFunction){renamingFunction.call(block,GLOBAL_KEYWORD+menuSeparator+oldName,GLOBAL_KEYWORD+menuSeparator+newName,core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+menuSeparator+oldName,core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+menuSeparator+newName);}}}}return newName;};/**
 * Rename the old name currently in this field to newName in the block assembly
 * rooted at the source block of this field (where "this" names the field of
 * interest). See the documentation for renameParamFromTo for more details.
 * @param newName
 * @return {string} The (possibly changed version of) newName, which may be
 *     changed to avoid variable capture with both external declarations
 *     (declared above the declaration of this name) or internal declarations
 *     (declared inside the scope of this name).
 */ // [lyn, 11/19/12 (revised 10/11/13)]
// Rename procedure parameter, local name, or loop index variable to a new
// name,
// avoiding variable capture in the scope of the param. Consistently renames
// all
// references to the name in getter and setter blocks. The proposed new name
// may be changed (by adding numbers to the end) so that it does not conflict
// with existing names. Returns the (possibly changed) new name.
//
// [lyn, 10/26/13] Modified to replace sequences of internal spaces by
// underscores (none were allowed before), and to replace empty string by '_'.
// Without special handling of empty string, the connection between a
// declaration field and its references is lost.  [lyn, 11/15/13] Refactored
// monolithic renameParam into parts that are useful on their own
LexicalVariable.renameParam=function(newName){var htmlInput=this.htmlInput_;// this is bound to field_textinput object
var oldName=this.getValue()||htmlInput&&htmlInput.defaultValue||this.getText();// name being changed to newName
// [lyn, 10/27/13] now check legality of identifiers
newName=LexicalVariable.makeLegalIdentifier(newName);// Default behavior consistent with previous behavior is to use "false" for
// last argument -- I.e., will not rename inner declarations, but may rename
// newName
return LexicalVariable.renameParamFromTo(this.sourceBlock_,oldName,newName,false);// Default should be false (as above), but can also play with true:
// return LexicalVariable.renameParamFromTo(this.sourceBlock_,
// oldName, newName, true);
};/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Refactored from renameParam and extended.
 * Rename oldName to newName in the block assembly rooted at this block
 * (where "this" names the block of interest). The names may refer to any
 * nonglobal parameter name (procedure parameter, local name, or loop index
 * variable). This function consistently renames all references to oldName by
 * newName in all getter and setter blocks that refer to oldName, correctly
 * handling inner declarations that use oldName. In cases where renaming
 * oldName to newName would result in variable capture of newName by another
 * declaration, such capture is avoided by either:
 *    1. (If renameCapturables is true):  consistently renaming the capturing
 * declarations
 *       (by adding numbers to the end) so that they do not conflict with
 * newName (or each other).
 *    2. (If renameCapturables is false): renaming the proposed newName (by
 * adding numbers to the end) so that it does not conflict with capturing
 * declarations).
 * @param block  the root source block containing the parameter being renamed
 * @param oldName
 * @param newName
 * @param renameCapturables in capture situations, determines whether capturing
 *     declarations are renamed (true) or newName is renamed (false)
 * @return {string} if renameCapturables is true, returns the given newName;
 *     if renameCapturables is false, returns the (possibly renamed version of)
 *     newName, which may be changed to avoid variable capture with both
 *     external declarations (declared above the declaration of this name) or
 *     internal declarations (declared inside the scope of this name).
 */LexicalVariable.renameParamFromTo=function(block,oldName,newName,renameCapturables){// Handle mutator blocks specially
if(block.mustNotRenameCapturables){return LexicalVariable.renameParamWithoutRenamingCapturables(block,oldName,newName,[]);}else if(renameCapturables){LexicalVariable.renameParamRenamingCapturables(block,oldName,newName);return newName;}else{return LexicalVariable.renameParamWithoutRenamingCapturables(block,oldName,newName,[]);}};/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Rename oldName to newName in the block assembly rooted at this block.
 * In the case where newName would be captured by an internal declaration,
 *  consistently rename the declaration and all its uses to avoid variable
 * capture. In the case where newName would be captured by an external
 * declaration, throw an exception.
 * @param sourceBlock  the root source block containing the declaration of
 *     oldName
 * @param oldName
 * @param newName
 */LexicalVariable.renameParamRenamingCapturables=function(sourceBlock,oldName,newName){if(newName!==oldName){// Do nothing if names are the same
var namesDeclaredHere=sourceBlock.declaredNames?sourceBlock.declaredNames():[];if(namesDeclaredHere.indexOf(oldName)==-1){throw Error('LexicalVariable.renamingCapturables: oldName '+oldName+' is not in declarations {'+namesDeclaredHere.join(',')+'}');}var namesDeclaredAbove=[];FieldLexicalVariable.getNamesInScope(sourceBlock).map(function(pair){if(pair[0]==pair[1]){namesDeclaredAbove.push(pair[0]);}else{namesDeclaredAbove.push(pair[0],pair[1]);}});// uses translated param names
var declaredNames=namesDeclaredHere.concat(namesDeclaredAbove);// Should really check which forbidden names are free vars in the body
// of declBlock.
if(declaredNames.indexOf(newName)!=-1){throw Error('LexicalVariable.renameParamRenamingCapturables:'+' newName '+newName+' is in existing declarations {'+declaredNames.join(',')+'}');}else{if(sourceBlock.renameBound){var boundSubstitution=Substitution.simpleSubstitution(oldName,newName);var freeSubstitution=new Substitution();// an empty
// substitution
sourceBlock.renameBound(boundSubstitution,freeSubstitution);}else{throw Error('LexicalVariable.renameParamRenamingCapturables:'+' block '+sourceBlock.type+' is not a declaration block.');}}}};/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Rename all free variables in this block according to the given renaming.
 * @param block: any block
 * @param block
 * @param freeSubstitution
 * @param freeRenaming: a dictionary (i.e., object) mapping old names to new
 *     names
 */LexicalVariable.renameFree=function(block,freeSubstitution){if(block){// If block is falsey, do nothing.
if(block.renameFree){// should be defined on every declaration block
block.renameFree(freeSubstitution);}else{block.getChildren().map(function(blk){LexicalVariable.renameFree(blk,freeSubstitution);});}}};/**
 * [lyn, written 11/15/13, installed 07/01/14]
 * Return a nameSet of all free variables in the given block.
 * @param block
 * @return (NameSet) set of all free names in block
 */LexicalVariable.freeVariables=function(block){var result=[];if(!block){// input and next block slots might not empty
result=new nameSet_NameSet();}else if(block.freeVariables){// should be defined on every declaration block
result=block.freeVariables();}else{var nameSets=block.getChildren().map(function(blk){return LexicalVariable.freeVariables(blk);});result=nameSet_NameSet.unionAll(nameSets);}// console.log("freeVariables(" + (block ? block.type : "*empty-socket*") +
// ") = " + result.toString());
return result;};/**
 * [lyn, written 11/15/13, installed 07/01/14] Refactored from renameParam
 * Rename oldName to newName in the block assembly rooted at this block.
 * In the case where newName would be captured by internal or external
 * declaration, change it to a name (with a number suffix) that would not be
 * captured.
 * @param sourceBlock  the root source block containing the declaration of
 *     oldName
 * @param oldName
 * @param newName
 * @param OKNewNames
 * @return {string} the (possibly renamed version of) newName, which may be
 *     changed to avoid variable capture with both external declarations
 *     (declared above the declaration of this name) or internal declarations
 *     (declared inside the scope of this name).
 */LexicalVariable.renameParamWithoutRenamingCapturables=function(sourceBlock,oldName,newName,OKNewNames){if(oldName===newName){return oldName;}sourceBlock.declaredNames?sourceBlock.declaredNames():[];var sourcePrefix='';if(showPrefixToUser){sourcePrefix=this.lexicalVarPrefix;}var helperInfo=LexicalVariable.renameParamWithoutRenamingCapturablesInfo(sourceBlock,oldName,sourcePrefix);var blocksToRename=helperInfo[0];var capturables=helperInfo[1];var declaredNames=[];// declared names in source block, with which
// newName cannot conflict
if(sourceBlock.declaredNames){declaredNames=sourceBlock.declaredNames();// Remove oldName from list of names. We can rename oldName to itself
// if we desire!
var oldIndex=declaredNames.indexOf(oldName);if(oldIndex!=-1){declaredNames.splice(oldIndex,1);}// Remove newName from list of declared names if it's in OKNewNames.
if(OKNewNames.indexOf(newName)!=-1){var newIndex=declaredNames.indexOf(newName);if(newIndex!=-1){declaredNames.splice(newIndex,1);}}}var conflicts=LexicalVariable.sortAndRemoveDuplicates(capturables.concat(declaredNames));newName=FieldLexicalVariable.nameNotIn(newName,conflicts);// Special case: if newName is oldName, we're done!
if(!(newName===oldName)){// [lyn, 12/27/2012] I don't understand what this code is for.
//  I think it had something to do with locals that has now been
// repaired?
/* var oldNameInDeclaredNames = false;
          for (var i = 0; i < declaredNames.length; i++) {
          if(oldName === declaredNames[i]){
            oldNameInDeclaredNames = true;
          }
        }
        if(!oldNameInDeclaredNames){
        */var oldNameValid=declaredNames.indexOf(oldName)!=-1;if(!oldNameValid){// Rename getters and setters
for(var i=0;i<blocksToRename.length;i++){var block=blocksToRename[i];var renamingFunction=block.renameLexicalVar;if(renamingFunction){renamingFunction.call(block,possiblyPrefixMenuNameWith(sourcePrefix)(oldName),possiblyPrefixMenuNameWith(sourcePrefix)(newName));}}}}return newName;};/**
 * [lyn, written 11/15/13, installed 07/01/14] Refactored from renameParam().
 * @param sourceBlock
 * @param oldName
 * @param sourcePrefix
 * @return {pair} Returns a pair of
 * (1) All getter/setter blocks that reference oldName
 * (2) A list of all non-global names to which oldName cannot be renamed
 *     because doing so would change the reference "wiring diagram" and thus
 *     the meaning of the program. This is the union of:
 * (a) all names declared between the declaration of oldName and a reference to
 *     old name; and
 * (b) all names declared in a parent of the oldName declaration that are
 *     referenced in the scope of oldName. In the case where prefixes are used
 *     (e.g., "param a", "index i, "local x") this is a list of *unprefixed*
 *     names.
 */LexicalVariable.renameParamWithoutRenamingCapturablesInfo=function(sourceBlock,oldName,sourcePrefix){// var sourceBlock = this; // The block containing the declaration of
// oldName sourceBlock is block in which name is being changed. Can be
// one of:
//   * For procedure param: procedures_mutatorarg, procedures_defnoreturn,
//     procedures_defreturn (last two added by lyn on 10/11/13).
//   * For local name: local_mutatorarg, local_declaration_statement,
//     local_declaration_expression
//   * For loop name: controls_forEach, controls_forRange, controls_for
var inScopeBlocks=[];// list of root blocks in scope of oldName and in
// which
// renaming must take place.
if(sourceBlock.blocksInScope){// Find roots of blocks in scope.
inScopeBlocks=sourceBlock.blocksInScope();}// console.log("inScopeBlocksRoots: " + JSON.stringify(inScopeBlocks.map(
// function(elt) { return elt.type; })));
// referenceResult is Array of (0) list of getter/setter blocks refering
// to old name and (1) capturable names = names to which oldName cannot
// be renamed without changing meaning of program.
var referenceResults=inScopeBlocks.map(function(blk){return LexicalVariable.referenceResult(blk,oldName,sourcePrefix,[]);});var blocksToRename=[];// A list of all getter/setter blocks whose that
// reference oldName
// and need to have their name changed to newName
var capturables=[];// A list of all non-global names to which oldName
// cannot be renamed because doing
// so would change the reference "wiring diagram" and thus the meaning
// of the program. This is the union of:
// (1) all names declared between the declaration of oldName and a
// reference to old name; and (2) all names declared in a parent of the
// oldName declaration that are referenced in the scope of oldName. In
// the case where prefixes are used (e.g., "param a", "index i, "local
// x") this is a list of *unprefixed* names.
for(var r=0;r<referenceResults.length;r++){blocksToRename=blocksToRename.concat(referenceResults[r][0]);capturables=capturables.concat(referenceResults[r][1]);}capturables=LexicalVariable.sortAndRemoveDuplicates(capturables);return[blocksToRename,capturables];};/**
 * [lyn, 10/27/13]
 * Checks an identifier for validity. Validity rules are a simplified version
 * of Kawa identifier rules. They assume that the YAIL-generated version of the
 * identifier will be preceded by a legal Kawa prefix:
 *
 * <identifier> = <first><rest>*
 * <first> = letter U charsIn("_$?~@")
 * <rest> = <first> U digit.
 *
 * Note: an earlier version also allowed characters in "!&%.^/+-*>=<",
 * but we decided to remove these because (1) they may be used for arithmetic,
 * logic, and selection infix operators in a future AI text language, and we
 * don't want things like a+b, !c, d.e to be ambiguous between variables and
 * other expressions.
 * (2) using chars in "><&" causes HTML problems with getters/setters in
 * flydown menu.
 *
 * First transforms the name by removing leading and trailing whitespace and
 * converting nonempty sequences of internal whitespace to '_'.
 * Returns a result object of the form {transformed: <string>, isLegal:
 * <bool>}, where: result.transformed is the transformed name and
 * result.isLegal is whether the transformed named satisfies the above rules.
 * @param ident
 * @return {{isLegal: boolean, transformed: string}}
 */LexicalVariable.checkIdentifier=function(ident){var transformed=ident.trim()// Remove leading and trailing whitespace
.replace(/[\s\xa0]+/g,'_');// Replace nonempty sequences of internal
// spaces by underscores
// [lyn, 06/11/14] Previous definition focused on *legal* characters:
//
//    var legalRegexp = /^[a-zA-Z_\$\?~@][\w_\$\?~@]*$/;
//
// Unfortunately this is geared only to English, and prevents i8n names (such
// as Chinese identifiers). In order to handle i8n, focus on avoiding illegal
// chars rather than accepting only legal ones. This is a quick solution.
// Needs more careful thought to work for every language. In particular, need
// to look at results of Java's Character.isJavaIdentifierStart(int) and
// Character.isJavaIdentifierPart(int) Note: to take complement of character
// set, put ^ first. Note: to include '-' in character set, put it first or
// right after ^
var legalStartCharRegExp='^[^-0-9!&%^/>=<`\'"#:;,\\\\*+.()|{}[\\] ]';var legalRestCharsRegExp='[^-!&%^/>=<\'"#:;,\\\\*+.()|{}[\\] ]*$';var legalRegexp=new RegExp(legalStartCharRegExp+legalRestCharsRegExp);// " Make Emacs Happy
var isLegal=transformed.search(legalRegexp)==0;return{isLegal:isLegal,transformed:transformed};};LexicalVariable.makeLegalIdentifier=function(ident){var check=LexicalVariable.checkIdentifier(ident);if(check.isLegal){return check.transformed;}else if(check.transformed===''){return'_';}else{return'name';// Use identifier 'name' to replace illegal name
}};// [lyn, 11/19/12] Given a block, return an Array of
//   (0) all getter/setter blocks referring to name in block and its children
//   (1) all (unprefixed) names within block that would be captured if name
// were renamed to one of those names. If Shared.showPrefixToUser, prefix is
// the prefix associated with name; otherwise prefix is "". env is a list of
// internally declared names in scope at this point; if Shared.usePrefixInYail
// is true, the env names have prefixes, otherwise they do not. [lyn,
// 12/25-27/2012] Updated to (1) add prefix argument, (2) handle local
// declaration statements/expressions, and (3) treat prefixes correctly when
// they're used.
LexicalVariable.referenceResult=function(block,name,prefix,env){if(!block){// special case when block is null
return[[],[]];}var referenceResults=block.referenceResults?block.referenceResults(name,prefix,env):block.getChildren().map(function(blk){return LexicalVariable.referenceResult(blk,name,prefix,env);});var blocksToRename=[];var capturables=[];for(var r=0;r<referenceResults.length;r++){blocksToRename=blocksToRename.concat(referenceResults[r][0]);capturables=capturables.concat(referenceResults[r][1]);}return[blocksToRename,capturables];};LexicalVariable.sortAndRemoveDuplicates=function(strings){var sorted=strings.sort();var nodups=[];if(strings.length>=1){var prev=sorted[0];nodups.push(prev);for(var i=1;i<sorted.length;i++){if(!(sorted[i]===prev)){prev=sorted[i];nodups.push(prev);}}}return nodups;};// [lyn, 11/23/12] Given a block, return the block connected to its next
// connection; If there is no next connection or no block, return null.
LexicalVariable.getNextTargetBlock=function(block){if(block&&block.nextConnection&&block.nextConnection.targetBlock()){return block.nextConnection.targetBlock();}else{return null;}};/**
 * [lyn, 11/16/13] Created.
 * @param strings1: An array of strings.
 * @param strings1
 * @param strings2
 * @param strings2: An array of strings.
 * @return True iff strings1 and strings2 have the same names in the same
 *     order; false otherwise.
 */LexicalVariable.stringListsEqual=function(strings1,strings2){var len1=strings1.length;var len2=strings2.length;if(len1!==len2){return false;}else{for(var i=0;i<len1;i++){if(strings1[i]!==strings2[i]){return false;}}}return true;// get here iff lists are equal
};
;// CONCATENATED MODULE: ./src/procedure_utils.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle serialization of the blocks workspace.
 *
 * @author sharon@google.com (Sharon Perl)
 */var procDefaultValue=['',''];var onChange=function onChange(procedureId){var workspace=this.block.workspace.getTopWorkspace();// [lyn, 10/14/13] .editable is undefined on blocks. Changed to .editable_
if(!this.block.isEditable()){workspace=core_.Drawer.flyout_.workspace_;return;}var def=workspace.getProcedureDatabase().getProcedure(procedureId);// loading but the definition block hasn't been processed yet.
if(!def)return;var text=def.getFieldValue('NAME');if(text==''||text!=this.getValue()){for(var i=0;this.block.getInput('ARG'+i)!=null;i++){this.block.removeInput('ARG'+i);}// return;
}this.doValueUpdate_(text);if(def){// [lyn, 10/27/13] Lyn sez: this causes complications (e.g., might open up
// mutator on collapsed procedure declaration block) and is no longer
// necessary with changes to setProedureParameters.
// if(def.paramIds_ == null){
//  def.mutator.setVisible(true);
//  def.mutator.shouldHide = true;
// }
// It's OK if def.paramIds is null
this.block.setProcedureParameters(def.arguments_,def.paramIds_,true);}};var getProcedureNames=function getProcedureNames(returnValue,opt_workspace){var workspace=opt_workspace||core_.common.getMainWorkspace();var topBlocks=workspace.getTopBlocks();var procNameArray=[procDefaultValue];for(var i=0;i<topBlocks.length;i++){var procName=topBlocks[i].getFieldValue('NAME');if(topBlocks[i].type=='procedures_defnoreturn'&&!returnValue){procNameArray.push([procName,procName]);}else if(topBlocks[i].type=='procedures_defreturn'&&returnValue){procNameArray.push([procName,procName]);}}if(procNameArray.length>1){procNameArray.splice(0,1);}return procNameArray;};// [lyn, 10/22/13] Return a list of all procedure declaration blocks
// If returnValue is false, lists all fruitless procedure declarations
// (defnoreturn) If returnValue is true, lists all fruitful procedure
// declaraations (defreturn)
var getProcedureDeclarationBlocks=function getProcedureDeclarationBlocks(returnValue,opt_workspace){var workspace=opt_workspace||Blockly.common.getMainWorkspace();var topBlocks=workspace.getTopBlocks(false);var blockArray=[];for(var i=0;i<topBlocks.length;i++){if(topBlocks[i].type=='procedures_defnoreturn'&&!returnValue){blockArray.push(topBlocks[i]);}else if(topBlocks[i].type=='procedures_defreturn'&&returnValue){blockArray.push(topBlocks[i]);}}return blockArray;};var getAllProcedureDeclarationBlocksExcept=function getAllProcedureDeclarationBlocksExcept(block){var topBlocks=block.workspace.getTopBlocks(false);var blockArray=[];for(var i=0;i<topBlocks.length;i++){if(topBlocks[i].type==='procedures_defnoreturn'||topBlocks[i].type==='procedures_defreturn'){if(topBlocks[i]!==block){blockArray.push(topBlocks[i]);}}}return blockArray;};var removeProcedureValues=function removeProcedureValues(name,workspace){if(workspace&&// [lyn, 04/13/14] ensure workspace isn't undefined
workspace===core_.common.getMainWorkspace()){var blockArray=workspace.getAllBlocks();for(var i=0;i<blockArray.length;i++){var block=blockArray[i];if(block.type=='procedures_callreturn'||block.type=='procedures_callnoreturn'){if(block.getFieldValue('PROCNAME')==name){block.removeProcedureValue();}}}}};// [lyn, 10/27/13] Defined as a replacement for Blockly.Procedures.rename
/**
 * Rename a procedure definition to a new name.
 *
 * @this FieldProcedureName
 * @param {!string} newName New name for the procedure represented by the
 *     field's source block.
 * @return {string} The new, validated name of the block.
 */var renameProcedure=function renameProcedure(newName){// this is bound to field_textinput object
var oldName=this.oldName_||this.getValue();var originalNewName=newName;// [lyn, 10/27/13] now check legality of identifiers
newName=LexicalVariable.makeLegalIdentifier(newName);// [lyn, 10/28/13] Prevent two procedures from having the same name.
var procBlocks=getAllProcedureDeclarationBlocksExcept(this.sourceBlock_);var procNames=procBlocks.map(function(decl){return decl.getFieldValue('NAME');});newName=FieldLexicalVariable.nameNotIn(newName,procNames);if(newName!==originalNewName){this.doValueUpdate_(newName);}// Rename any callers.
var blocks=this.sourceBlock_.workspace.getAllBlocks();for(var x=0;x<blocks.length;x++){var func=blocks[x].renameProcedure;if(func){func.call(blocks[x],oldName,newName);}}this.oldName_=newName;return newName;};
;// CONCATENATED MODULE: ./src/procedure_database.js
/* -*- mode: javascript; js-indent-level: 2; -*- */ /**
 * @license
 * Copyright © 2016-2017 Massachusetts Institute of Technology. All rights
 *     reserved.
 */ /**
 * @fileoverview A database for tracking user-defined procedures.
 * @author Evan W. Patton <ewpatton@mit.edu>
 *//**
 * ProcedureDatabase provides a per-workspace data store for manipulating
 * procedure definitions in the Blocks editor.
 *
 * @param workspace The workspace containing procedures indexed by the
 *     ProcedureDatabase.
 * @constructor
 */var ProcedureDatabase=function ProcedureDatabase(workspace){/**
   * The source workspace for the ProcedureDatabase.
   * @type {!Blockly.WorkspaceSvg}
   * @private
   */this.workspace_=workspace;/**
   * Procedure definition map from block ID to block. This is a subset of
   * {@link Blockly.Workspace.blockDB_}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */this.procedures_={};/**
   * Procedure definition map for procedures that return values. This is a
   * subset of
   * {@link #procedures_}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */this.returnProcedures_={};/**
   * Procedure definition map for procedures that do not return values. This is
   * a subset of
   * {@link #procedures_}.
   * @type {{string: !Blockly.BlockSvg}}
   * @private
   */this.voidProcedures_={};/**
   * Number of procedures in the database.
   * @type {number}
   */this.length=0;/**
   * Number of procedure definitions in the database that return a value.
   * @type {number}
   */this.returnProcedures=0;/**
   * Number of procedure definitions in the database that do not return a value.
   * @type {number}
   */this.voidProcedures=0;};ProcedureDatabase.defaultValue=['','none'];/**
 * Get a list of names for procedures in the database.
 *
 * @param {Boolean=false} returnValue Return names of procedures with return
 *     values (true) or without return values (false).
 * @return {!string[]}
 */ProcedureDatabase.prototype.getNames=function(returnValue){return getProcedureNames(returnValue,this.workspace_).map(function(v){return v[0];});};/**
 * Get a list of (name, id) tuples for showing procedure names in a dropdown
 * field.
 * @return {!Array.<Array.<string>>}
 * @param returnValue
 */ProcedureDatabase.prototype.getMenuItems=function(returnValue){return getProcedureNames(returnValue,this.workspace_);};/**
 * Get a list of procedure definition blocks.
 *
 * @param {Boolean=false} returnValue Return procedure definition blocks with
 *     return values (true) or without return values (false).
 * @return {!Blockly.Block[]}
 */ProcedureDatabase.prototype.getDeclarationBlocks=function(returnValue){return core_.utils.object.values(returnValue?this.returnProcedures_:this.voidProcedures_);};ProcedureDatabase.prototype.getDeclarationsBlocksExcept=function(block){var blockArray=[];core_.utils.values(this.procedures_).forEach(function(b){if(b!==block)blockArray.push(b);});return blockArray;};ProcedureDatabase.prototype.getAllDeclarationNames=function(){return core_.utils.values(this.procedures_).map(function(block){return block.getFieldValue('NAME');});};/**
 * Add a procedure to the database.
 *
 * @param {!string} name
 * @param {!Blockly.Block} block
 * @return {boolean} True if the definition was added, otherwise false.
 */ProcedureDatabase.prototype.addProcedure=function(name,block){if(block.type!='procedures_defnoreturn'&&block.type!='procedures_defreturn'){// not a procedure block!
console.warn('Attempt to addProcedure with block type '+block.type);return false;}var id=block.id;if(id in this.procedures_){return false;}this.procedures_[id]=block;this.length++;if(block.type=='procedures_defnoreturn'){this.voidProcedures_[id]=block;this.voidProcedures++;}else{this.returnProcedures_[id]=block;this.returnProcedures++;}return true;};/**
 * Remove a procedure from the database.
 *
 * @param {!string} id
 * @return {boolean}
 */ProcedureDatabase.prototype.removeProcedure=function(id){if(id in this.procedures_){var block=this.procedures_[id];if(block.type=='procedures_defnoreturn'){delete this.voidProcedures_[id];this.voidProcedures--;}else{delete this.returnProcedures_[id];this.returnProcedures--;}delete this.procedures_[id];this.length--;}return true;};/**
 * Rename a procedure in the database with the given oldNmae to newName.
 *
 * @param {!string} procId
 * @param {!string} oldName
 * @param {!string} newName
 * @returns {boolean} True if the procedure was renamed in the database,
 *     otherwise false.
 */ProcedureDatabase.prototype.renameProcedure=function(procId,oldName,newName){/*
      if (newName in this.procedures_) {
        return false;
      }
      if (oldName in this.procedures_) {
        var block = this.procedures_[oldName];
        if (block.type == 'procedures_defnoreturn') {
          this.voidProcedures_[newName] = block;
          delete this.voidProcedures_[oldName];
        } else {
          this.returnProcedures_[newName] = block;
          delete this.returnProcedures_[oldName];
        }
        this.procedures_[newName] = block;
        delete this.procedures_[oldName];
        return true;
      } else {
        console.warn('Attempt to renameProcedure "' + oldName +
            '" not in the database.');
        return false;
      }
      */};/**
 * Get the procedure identified by {@link #id}. If the id does not identify a
 * procedure, undefined will be returned.
 *
 * @param {?string} id The procedure's id.
 * @return {?Blockly.BlockSvg} The procedure block defining the procedure
 *     identified by {@link #id}.
 */ProcedureDatabase.prototype.getProcedure=function(id){var proc=this.procedures_[id];return proc?proc:this.getProcedureByName(id);};ProcedureDatabase.prototype.getProcedureByName=function(name){for(var id in this.procedures_){if(this.procedures_[id].getFieldValue('NAME')===name){return this.procedures_[id];}}return undefined;};ProcedureDatabase.prototype.clear=function(){this.procedures_={};this.returnProcedures_={};this.voidProcedures_={};this.length=0;this.returnProcedures=0;this.voidProcedures=0;};
;// CONCATENATED MODULE: ./src/workspace.js
// This ia a bit kludgey and we'll need a better way to make this play nice with
// apps that already define some of these things (e.g. App Inventor).  It's also
// kludgey in that we're defining methods on both Blockly.Workspace and Blockly.WorkspaceSvg.
// That's to cover the cases where we're not clear which class is actually defining the method.
/**
 * Shared flydown for parameters and variables.
 * @type {Flydown}
 * @private
 */core_.Workspace.prototype.flydown_=core_.Workspace.prototype.flydown_||null;core_.Workspace.prototype.getFlydown=core_.Workspace.prototype.getFlydown||function(){return this.flydown_;};/**
 * Obtain the {@link Blockly.ProcedureDatabase} associated with the workspace.
 * @return {!Blockly.ProcedureDatabase}
 */core_.Workspace.prototype.getProcedureDatabase=core_.Workspace.prototype.getProcedureDatabase||function(){if(!this.procedureDb_){this.procedureDb_=new ProcedureDatabase(this);}return this.procedureDb_;};/**
 * Get the topmost workspace in the workspace hierarchy.
 * @return {Blockly.Workspace}
 */core_.Workspace.prototype.getTopWorkspace=core_.Workspace.prototype.getTopWorkspace||function(){var parent=this;while(parent.targetWorkspace){parent=parent.targetWorkspace;}return parent;};core_.Workspace.prototype.getWarningHandler=core_.Workspace.prototype.getWarningHandler||function(){return warningHandler_namespaceObject;};/**
 * Shared flydown for parameters and variables.
 * @type {Flydown}
 * @private
 */core_.WorkspaceSvg.prototype.flydown_=core_.WorkspaceSvg.prototype.flydown_||null;core_.WorkspaceSvg.prototype.getFlydown=core_.WorkspaceSvg.prototype.getFlydown||function(){return this.flydown_;};/**
 * Obtain the {@link Blockly.ProcedureDatabase} associated with the workspace.
 * @return {!Blockly.ProcedureDatabase}
 */core_.WorkspaceSvg.prototype.getProcedureDatabase=core_.WorkspaceSvg.prototype.getProcedureDatabase||function(){if(!this.procedureDb_){this.procedureDb_=new ProcedureDatabase(this);}return this.procedureDb_;};/**
 * Get the topmost workspace in the workspace hierarchy.
 * @return {Blockly.WorkspaceSvg}
 */core_.WorkspaceSvg.prototype.getTopWorkspace=core_.WorkspaceSvg.prototype.getTopWorkspace||function(){var parent=this;while(parent.targetWorkspace){parent=parent.targetWorkspace;}return parent;};core_.WorkspaceSvg.prototype.getWarningHandler=core_.WorkspaceSvg.prototype.getWarningHandler||function(){return warningHandler_namespaceObject;};
;// CONCATENATED MODULE: ./src/fields/field_flydown.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Field in which mouseover displays flyout-like menu of blocks
 * and mouse click edits the field name.
 * Flydowns are used in App Inventor for displaying get/set blocks for
 *     parameter names and callers for procedure declarations.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */function field_flydown_typeof(obj){"@babel/helpers - typeof";return field_flydown_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_flydown_typeof(obj);}function field_flydown_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function field_flydown_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,field_flydown_toPropertyKey(descriptor.key),descriptor);}}function field_flydown_createClass(Constructor,protoProps,staticProps){if(protoProps)field_flydown_defineProperties(Constructor.prototype,protoProps);if(staticProps)field_flydown_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function _get(){if(typeof Reflect!=="undefined"&&Reflect.get){_get=Reflect.get.bind();}else{_get=function _get(target,property,receiver){var base=_superPropBase(target,property);if(!base)return;var desc=Object.getOwnPropertyDescriptor(base,property);if(desc.get){return desc.get.call(arguments.length<3?target:receiver);}return desc.value;};}return _get.apply(this,arguments);}function _superPropBase(object,property){while(!Object.prototype.hasOwnProperty.call(object,property)){object=field_flydown_getPrototypeOf(object);if(object===null)break;}return object;}function field_flydown_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)field_flydown_setPrototypeOf(subClass,superClass);}function field_flydown_setPrototypeOf(o,p){field_flydown_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return field_flydown_setPrototypeOf(o,p);}function field_flydown_createSuper(Derived){var hasNativeReflectConstruct=field_flydown_isNativeReflectConstruct();return function _createSuperInternal(){var Super=field_flydown_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=field_flydown_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return field_flydown_possibleConstructorReturn(this,result);};}function field_flydown_possibleConstructorReturn(self,call){if(call&&(field_flydown_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return field_flydown_assertThisInitialized(self);}function field_flydown_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function field_flydown_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function field_flydown_getPrototypeOf(o){field_flydown_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return field_flydown_getPrototypeOf(o);}function _defineProperty(obj,key,value){key=field_flydown_toPropertyKey(key);if(key in obj){Object.defineProperty(obj,key,{value:value,enumerable:true,configurable:true,writable:true});}else{obj[key]=value;}return obj;}function field_flydown_toPropertyKey(arg){var key=field_flydown_toPrimitive(arg,"string");return field_flydown_typeof(key)==="symbol"?key:String(key);}function field_flydown_toPrimitive(input,hint){if(field_flydown_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_flydown_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}/**
 * Class for a clickable parameter field.
 * @param {string} name The initial parameter name in the field.
 * @param {boolean} isEditable Whether the user is allowed to change the name
 *     of this parameter or not.
 * @param {string=} opt_displayLocation The location to display the flydown at
 *     Either: FieldFlydown.DISPLAY_BELOW,
 *             FieldFlydown.DISPLAY_RIGHT
 *     Defaults to DISPLAY_RIGHT.
 * @param {Function} opt_changeHandler An optional function that is called
 *     to validate any constraints on what the user entered.  Takes the new
 *     text as an argument and returns the accepted text or null to abort
 *     the change. E.g., for an associated getter/setter this could change
 *     references to names in this field.
 * @extends {Blockly.FieldTextInput}
 * @constructor
 */var FieldFlydown=/*#__PURE__*/function(_Blockly$FieldTextInp){field_flydown_inherits(FieldFlydown,_Blockly$FieldTextInp);var _super=field_flydown_createSuper(FieldFlydown);function FieldFlydown(name,isEditable,opt_displayLocation,opt_changeHandler){var _this;field_flydown_classCallCheck(this,FieldFlydown);_this=_super.call(this,name,opt_changeHandler);// This by itself does not control editability
_this.EDITABLE=isEditable;// [lyn, 10/27/13] Make flydown direction an instance variable
_this.displayLocation=opt_displayLocation||FieldFlydown.DISPLAY_RIGHT;return _this;}field_flydown_createClass(FieldFlydown,[{key:"showEditor_",value:// Override FieldTextInput's showEditor_ so it's only called for EDITABLE field.
function showEditor_(){if(!this.EDITABLE){return;}if(FieldFlydown.showPid_){// cancel a pending flydown for editing
clearTimeout(FieldFlydown.showPid_);FieldFlydown.showPid_=0;core_.common.getMainWorkspace().hideChaff();}_get(field_flydown_getPrototypeOf(FieldFlydown.prototype),"showEditor_",this).call(this);}},{key:"init",value:function init(block){_get(field_flydown_getPrototypeOf(FieldFlydown.prototype),"init",this).call(this,block);// Remove inherited field css classes ...
core_.utils.dom.removeClass(/** @type {!Element} */this.fieldGroup_,'blocklyEditableText');core_.utils.dom.removeClass(/** @type {!Element} */this.fieldGroup_,'blocklyNoNEditableText');// ... and add new ones, so that look and feel of flyout fields can be
// customized
core_.utils.dom.addClass(/** @type {!Element} */this.fieldGroup_,this.fieldCSSClassName);this.mouseOverWrapper_=core_.browserEvents.bind(this.fieldGroup_,'mouseover',this,this.onMouseOver_);this.mouseOutWrapper_=core_.browserEvents.bind(this.fieldGroup_,'mouseout',this,this.onMouseOut_);}}]);return FieldFlydown;}(core_.FieldTextInput);/**
 * Default CSS class name for the field itself.
 * @type {string}
 * @const
 */ /**
   * Milliseconds to wait before showing flydown after mouseover event on flydown
   * field.
   * @type {number}
   * @const
   */_defineProperty(FieldFlydown,"timeout",500);/**
   * Process ID for timer event to show flydown (scheduled by mouseover event).
   * @type {number}
   * @const
   */_defineProperty(FieldFlydown,"showPid_",0);/**
   * Which instance of FieldFlydown (or a subclass) is an open flydown attached
   * to?
   * @type {FieldFlydown}
   * @private
   */_defineProperty(FieldFlydown,"openFieldFlydown_",null);// These control the positions of the flydown.
_defineProperty(FieldFlydown,"DISPLAY_BELOW",'BELOW');_defineProperty(FieldFlydown,"DISPLAY_RIGHT",'RIGHT');_defineProperty(FieldFlydown,"DISPLAY_LOCATION",FieldFlydown.DISPLAY_BELOW);FieldFlydown.prototype.fieldCSSClassName='blocklyFieldFlydownField';/**
 * Default CSS class name for the flydown that flies down from the field.
 * @type {string}
 * @const
 */FieldFlydown.prototype.flyoutCSSClassName='blocklyFieldFlydownFlydown';FieldFlydown.prototype.onMouseOver_=function(e){// [lyn, 10/22/13] No flydowns in a flyout!
if(!this.sourceBlock_.isInFlyout&&FieldFlydown.showPid_==0){FieldFlydown.showPid_=window.setTimeout(this.showFlydownMaker_(),FieldFlydown.timeout);}// This event has been handled.  No need to bubble up to the document.
e.stopPropagation();};FieldFlydown.prototype.onMouseOut_=function(e){// Clear any pending timer event to show flydown
window.clearTimeout(FieldFlydown.showPid_);FieldFlydown.showPid_=0;e.stopPropagation();};/**
 * Returns a thunk that creates a Flydown block of the getter and setter blocks
 * for receiver field.
 *  @return A thunk (zero-parameter function).
 */FieldFlydown.prototype.showFlydownMaker_=function(){// Name receiver in variable so can close over this variable in returned thunk
var field=this;return function(){if(FieldFlydown.showPid_!==0&&!field.getSourceBlock().workspace.isDragging()&&!this.htmlInput_){try{field.showFlydown_();}catch(e){console.error('Failed to show flydown',e);}}FieldFlydown.showPid_=0;};};/**
 * Shows the blocks generated by flydownBlocksXML_ in the flydown. Xml should be
 * wrapped in <xml> tags.
 */FieldFlydown.prototype.showFlydown_=function(){core_.common.getMainWorkspace().hideChaff();var flydown=core_.common.getMainWorkspace().getFlydown();// Add flydown to top-level svg, *not* to main workspace svg
// This is essential for correct positioning of flydown via translation
// (If it's in workspace svg, it will be additionally translated by
//  workspace svg translation relative to Blockly.svg.)
core_.common.getMainWorkspace().getParentSvg().appendChild(flydown.svgGroup_);// Adjust scale for current zoom level
var scale=flydown.targetWorkspace.scale;flydown.workspace_.setScale(scale);flydown.setCSSClass(this.flyoutCSSClassName);var blocksXMLText=this.flydownBlocksXML_();var blocksDom=core_.utils.xml.textToDom(blocksXMLText);// [lyn, 11/10/13] Use goog.dom.getChildren rather than .children or
//    .childNodes to make this code work across browsers.
var blocksXMLList=getChildren(blocksDom);var xy=core_.common.getMainWorkspace().getSvgXY(this.borderRect_);var borderBBox=this.borderRect_.getBBox();if(this.displayLocation===FieldFlydown.DISPLAY_BELOW){xy.y+=borderBBox.height*scale;}else{// Display right.
xy.x+=borderBBox.width*scale;}// Set the flydown's current field.  Note that this is subtly differnt than
// FieldFlydown.openFieldFlydown_ because the latter might get reset
// by an iterim hiding of the field and not get set again by an interim call
// to show().
flydown.field_=this;flydown.showAt(blocksXMLList,xy.x,xy.y);FieldFlydown.openFieldFlydown_=this;};/**
 * Hide the flydown menu and squash any timer-scheduled flyout creation.
 */FieldFlydown.hide=function(){// Clear any pending timer event to show flydown.
window.clearTimeout(FieldFlydown.showPid_);// Hide any displayed flydown.
var flydown=core_.common.getMainWorkspace().getFlydown();if(flydown){flydown.hide();}};/**
 * Calls the validation function for this field, as well as all the validation
 * function for the field's class and its parents.
 * @param {Blockly.Field} field The field to validate.
 * @param {string} text Proposed text.
 * @return {?string} Revised text, or null if invalid.
 */function callAllValidators(field,text){var classResult=field.doClassValidation_(text);if(classResult===null){// Class validator rejects value.  Game over.
return null;}else if(classResult!==undefined){text=classResult;}var userValidator=field.getValidator();if(userValidator){var userResult=userValidator.call(field,text);if(userResult===null){// User validator rejects value.  Game over.
return null;}else if(userResult!==undefined){text=userResult;}}return text;}// TODO: Changing how validators work is not very future proof.
// Override Blockly's behavior; they call the validator after setting the text,
// which is incompatible with how our validators work (we expect to be called
// before the change since in order to find the old references to be renamed).
FieldFlydown.prototype.onHtmlInputChange_=function(e){var htmlInput=this.htmlInput_;var text=htmlInput.value;if(text!==htmlInput.oldValue_){htmlInput.oldValue_=text;var valid=true;if(this.sourceBlock_){valid=callAllValidators(this,htmlInput.value);}if(valid===null){core_.utils.dom.addClass(htmlInput,'blocklyInvalidInput');}else{core_.utils.dom.removeClass(htmlInput,'blocklyInvalidInput');this.doValueUpdate_(valid);}}else if(core_.utils.userAgent.WEBKIT){// Cursor key.  Render the source block to show the caret moving.
// Chrome only (version 26, OS X).
this.sourceBlock_.render();}// We need all of the following to cause the field to resize!
this.textContent_.nodeValue=text;this.forceRerender();this.resizeEditor_();core_.svgResize(this.sourceBlock_.workspace);};/**
 * Close the flydown and dispose of all UI.
 */FieldFlydown.prototype.dispose=function(){if(FieldFlydown.openFieldFlydown_==this){FieldFlydown.hide();}// Call parent's destructor.
core_.FieldTextInput.prototype.dispose.call(this);};/**
 * Constructs a FieldFlydown from a JSON arg object.
 * @param {!Object} options A JSON object with options.
 * @return {FieldFlydown} The new field instance.
 * @package
 * @nocollapse
 */FieldFlydown.fromJson=function(options){var name=core_.utils.replaceMessageReferences(options['name']);return new FieldFlydown(name,options['is_editable']);};core_.fieldRegistry.register('field_flydown',FieldFlydown);
;// CONCATENATED MODULE: ./src/fields/flydown.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Flydown is an abstract class for a flyout-like dropdown
 *     containing blocks. Unlike a regular flyout, for simplicity it does not
 *     support scrolling. Any non-abstract subclass must provide a
 *     flydownBlocksXML_ () method that returns an XML element whose children
 *     are blocks that should appear in the flyout.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */function flydown_typeof(obj){"@babel/helpers - typeof";return flydown_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},flydown_typeof(obj);}function flydown_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,flydown_toPropertyKey(descriptor.key),descriptor);}}function flydown_createClass(Constructor,protoProps,staticProps){if(protoProps)flydown_defineProperties(Constructor.prototype,protoProps);if(staticProps)flydown_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function flydown_toPropertyKey(arg){var key=flydown_toPrimitive(arg,"string");return flydown_typeof(key)==="symbol"?key:String(key);}function flydown_toPrimitive(input,hint){if(flydown_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(flydown_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function flydown_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function flydown_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)flydown_setPrototypeOf(subClass,superClass);}function flydown_setPrototypeOf(o,p){flydown_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return flydown_setPrototypeOf(o,p);}function flydown_createSuper(Derived){var hasNativeReflectConstruct=flydown_isNativeReflectConstruct();return function _createSuperInternal(){var Super=flydown_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=flydown_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return flydown_possibleConstructorReturn(this,result);};}function flydown_possibleConstructorReturn(self,call){if(call&&(flydown_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return flydown_assertThisInitialized(self);}function flydown_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function flydown_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function flydown_getPrototypeOf(o){flydown_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return flydown_getPrototypeOf(o);}/**
 * Class for a flydown.
 * @constructor
 * @param workspaceOptions
 */var Flydown=/*#__PURE__*/function(_Blockly$VerticalFlyo){flydown_inherits(Flydown,_Blockly$VerticalFlyo);var _super=flydown_createSuper(Flydown);function Flydown(workspaceOptions){var _this;flydown_classCallCheck(this,Flydown);_this=_super.call(this,workspaceOptions);_this.dragAngleRange_=360;return _this;}return flydown_createClass(Flydown);}(core_.VerticalFlyout);;/**
 * Previous CSS class for this flydown.
 * @type {string}
 */Flydown.prototype.previousCSSClassName_='';/**
 * Override flyout factor to be smaller for flydowns.
 * @type {number}
 * @const
 */Flydown.prototype.VERTICAL_SEPARATION_FACTOR=1;/**
 * Creates the flydown's DOM.  Only needs to be called once.  Overrides the
 * flyout createDom method.
 * @param {!String} cssClassName The name of the CSS class for this flydown.
 * @return {!Element} The flydown's SVG group.
 */Flydown.prototype.createDom=function(cssClassName){/*
  <g>
    <path class={cssClassName}/>
    <g></g>
  </g>
  */this.previousCSSClassName_=cssClassName;// Remember class name for later
this.svgGroup_=core_.utils.dom.createSvgElement('g',{'class':cssClassName},null);this.svgBackground_=core_.utils.dom.createSvgElement('path',{},this.svgGroup_);this.svgGroup_.appendChild(this.workspace_.createDom());return this.svgGroup_;};/**
 * Set the CSS class of the flydown SVG group. Need to remove previous class if
 * there is one.
 * @param {!String} newCSSClassName The name of the new CSS class replacing the
 *     old one.
 */Flydown.prototype.setCSSClass=function(newCSSClassName){if(newCSSClassName!==this.previousCSSClassName_){core_.utils.dom.removeClass(this.svgGroup_,this.previousCSSClassName_);core_.utils.dom.addClass(this.svgGroup_,newCSSClassName);this.previousCSSClassName_=newCSSClassName;}};/**
 * Initializes the Flydown.
 * @param {!Blockly.Workspace} workspace The workspace in which to create new
 *     blocks.
 */Flydown.prototype.init=function(workspace){// Flydowns have no scrollbar
core_.Flyout.prototype.init.call(this,workspace,false);this.workspace_.setTheme(workspace.getTheme());workspace.getComponentManager().addCapability(this.id,core_.ComponentManager.Capability.AUTOHIDEABLE);};/**
 * Override the flyout position method to do nothing instead.
 * @private
 */Flydown.prototype.position=function(){return;};/**
 * Show and populate the flydown.
 * @param {!Array|string} xmlList List of blocks to show.
 * @param {!num} x X-position of upper-left corner of flydown.
 * @param {!num} y Y-position of upper-left corner of flydown.
 */Flydown.prototype.showAt=function(xmlList,x,y){core_.Events.disable();try{// invoke flyout method, which adds blocks to flydown
// and calculates width and height.
this.show(xmlList);}finally{core_.Events.enable();}// this.svgGroup_.setAttribute('transform', 'translate(' + x + ',' + y +
// ')');
// Calculate path around flydown blocks. Based on code in flyout position_
// method.
// Start at bottom of top left arc and proceed clockwise
// Flydown outline shape is symmetric about vertical axis, so no need to
// differentiate LTR and RTL paths.
var margin=this.CORNER_RADIUS*this.workspace_.scale;var edgeWidth=this.width_-2*margin;var edgeHeight=this.height_-2*margin;var path=['M 0,'+margin];path.push('a',margin,margin,0,0,1,margin,-margin);// upper left arc
path.push('h',edgeWidth);// top edge
path.push('a',margin,margin,0,0,1,margin,margin);// upper right arc
path.push('v',edgeHeight);// right edge
path.push('a',margin,margin,0,0,1,-margin,margin);// bottom right arc
path.push('h',-edgeWidth);// bottom edge, drawn backwards
path.push('a',margin,margin,0,0,1,-margin,-margin);// bottom left arc
path.push('z');// complete path by drawing left edge
this.svgBackground_.setAttribute('d',path.join(' '));this.svgGroup_.setAttribute('transform','translate('+x+', '+y+')');};/**
 * Compute width and height of Flydown.  Position button under each block.
 * Overrides the reflow method of flyout
 * For RTL: Lay out the blocks right-aligned.
 */Flydown.prototype.reflow=function(){this.workspace_.scale=this.targetWorkspace.scale;var scale=this.workspace_.scale;var flydownWidth=0;var flydownHeight=0;var margin=this.CORNER_RADIUS*scale;var blocks=this.workspace_.getTopBlocks(false);for(var i=0,block;block=blocks[i];i++){var blockHW=block.getHeightWidth();flydownWidth=Math.max(flydownWidth,blockHW.width*scale);flydownHeight+=blockHW.height*scale;}flydownWidth+=2*margin+this.tabWidth_*scale;// tabWidth is width of
// a plug
var rendererConstants=this.workspace_.getRenderer().getConstants();var startHatHeight=rendererConstants.ADD_START_HATS?rendererConstants.START_HAT_HEIGHT:0;flydownHeight+=3*margin+margin*this.VERTICAL_SEPARATION_FACTOR*blocks.length+startHatHeight*scale/2.0;if(this.width_!=flydownWidth){for(var j=0,_block;_block=blocks[j];j++){var _blockHW=_block.getHeightWidth();var blockXY=_block.getRelativeToSurfaceXY();if(this.RTL){// With the FlydownWidth known, right-align the blocks.
var dx=flydownWidth-margin-scale*(this.tabWidth_-blockXY.x);_block.moveBy(dx,0);blockXY.x+=dx;}if(_block.flyoutRect_){_block.flyoutRect_.setAttribute('width',_blockHW.width);_block.flyoutRect_.setAttribute('height',_blockHW.height);_block.flyoutRect_.setAttribute('x',this.RTL?blockXY.x-_blockHW.width:blockXY.x);_block.flyoutRect_.setAttribute('y',blockXY.y);}}// Record the width for us in showAt method
this.width_=flydownWidth;this.height_=flydownHeight;}};Flydown.prototype.onMouseMove_=function(e){// override Blockly's flyout behavior for moving the flyout.
return;};/**
 * Copy a block from the flyout to the workspace and position it correctly.
 * @param {!Blockly.Block} originBlock The flyout block to copy..
 * @return {!Blockly.Block} The new block in the main workspace.
 * @private
 */Flydown.prototype.placeNewBlock_=function(originBlock){var targetWorkspace=this.targetWorkspace;var svgRootOld=originBlock.getSvgRoot();if(!svgRootOld){throw Error('originBlock is not rendered.');}// Figure out where the original block is on the screen, relative to the upper
// left corner of the main workspace.
var scale=this.workspace_.scale;// const margin = this.CORNER_RADIUS * scale;
var xyOld=this.workspace_.getSvgXY(svgRootOld);// var scrollX = this.svgGroup_.getScreenCTM().e + margin;
var scrollX=xyOld.x;xyOld.x+=scrollX/targetWorkspace.scale-scrollX;// var scrollY = this.svgGroup_.getScreenCTM().f + margin;
var scrollY=xyOld.y;scale=targetWorkspace.scale;xyOld.y+=scrollY/scale-scrollY;// Create the new block by cloning the block in the flyout (via XML).
var xml=core_.Xml.blockToDom(originBlock);var block=core_.Xml.domToBlock(xml,targetWorkspace);var svgRootNew=block.getSvgRoot();if(!svgRootNew){throw Error('block is not rendered.');}// Figure out where the new block got placed on the screen, relative to the
// upper left corner of the workspace.  This may not be the same as the
// original block because the flyout's origin may not be the same as the
// main workspace's origin.
var xyNew=targetWorkspace.getSvgXY(svgRootNew);// Scale the scroll (getSvgXY did not do this).
xyNew.x+=targetWorkspace.scrollX/targetWorkspace.scale-targetWorkspace.scrollX;xyNew.y+=targetWorkspace.scrollY/targetWorkspace.scale-targetWorkspace.scrollY;// If the flyout is collapsible and the workspace can't be scrolled.
if(targetWorkspace.toolbox_&&!targetWorkspace.scrollbar){xyNew.x+=targetWorkspace.toolbox_.getWidth()/targetWorkspace.scale;xyNew.y+=targetWorkspace.toolbox_.getHeight()/targetWorkspace.scale;}// Move the new block to where the old block is.
block.moveBy(xyOld.x-xyNew.x,xyOld.y-xyNew.y);return block;};Flydown.prototype.shouldHide=true;Flydown.prototype.hide=function(){if(this.shouldHide){core_.Flyout.prototype.hide.call(this);FieldFlydown.openFieldFlydown_=null;}this.shouldHide=true;};Flydown.prototype.autoHide=function(){this.hide();};// Note: nothing additional beyond flyout disposal needs to be done to dispose
// of a flydown.
;// CONCATENATED MODULE: ./src/fields/field_global_flydown.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Clickable field with flydown menu of global getter and setter
 *     blocks.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */function field_global_flydown_typeof(obj){"@babel/helpers - typeof";return field_global_flydown_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_global_flydown_typeof(obj);}function field_global_flydown_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,field_global_flydown_toPropertyKey(descriptor.key),descriptor);}}function field_global_flydown_createClass(Constructor,protoProps,staticProps){if(protoProps)field_global_flydown_defineProperties(Constructor.prototype,protoProps);if(staticProps)field_global_flydown_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function field_global_flydown_toPropertyKey(arg){var key=field_global_flydown_toPrimitive(arg,"string");return field_global_flydown_typeof(key)==="symbol"?key:String(key);}function field_global_flydown_toPrimitive(input,hint){if(field_global_flydown_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_global_flydown_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function field_global_flydown_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function field_global_flydown_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)field_global_flydown_setPrototypeOf(subClass,superClass);}function field_global_flydown_setPrototypeOf(o,p){field_global_flydown_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return field_global_flydown_setPrototypeOf(o,p);}function field_global_flydown_createSuper(Derived){var hasNativeReflectConstruct=field_global_flydown_isNativeReflectConstruct();return function _createSuperInternal(){var Super=field_global_flydown_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=field_global_flydown_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return field_global_flydown_possibleConstructorReturn(this,result);};}function field_global_flydown_possibleConstructorReturn(self,call){if(call&&(field_global_flydown_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return field_global_flydown_assertThisInitialized(self);}function field_global_flydown_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function field_global_flydown_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function field_global_flydown_getPrototypeOf(o){field_global_flydown_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return field_global_flydown_getPrototypeOf(o);}/**
 * Class for a clickable global variable declaration field.
 * @param name
 * @param displayLocation
 * @param {string} text The initial parameter name in the field.
 * @extends {Blockly.Field}
 * @constructor
 */var FieldGlobalFlydown=/*#__PURE__*/function(_FieldFlydown){field_global_flydown_inherits(FieldGlobalFlydown,_FieldFlydown);var _super=field_global_flydown_createSuper(FieldGlobalFlydown);function FieldGlobalFlydown(name,displayLocation){field_global_flydown_classCallCheck(this,FieldGlobalFlydown);return _super.call(this,name,true,displayLocation,// rename all references to this global variable
LexicalVariable.renameGlobal);}return field_global_flydown_createClass(FieldGlobalFlydown);}(FieldFlydown);FieldGlobalFlydown.prototype.fieldCSSClassName='blocklyFieldParameter';FieldGlobalFlydown.prototype.flyoutCSSClassName='blocklyFieldParameterFlydown';/**
 * Block creation menu for global variables
 * Returns a list of two XML elements: a getter block for name and a setter
 * block for this parameter field.
 *  @return {!Array.<string>} List of two XML elements.
 **/FieldGlobalFlydown.prototype.flydownBlocksXML_=function(){// global name for this parameter field.
var name=core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX+' '+this.getText();var getterSetterXML='<xml>'+'<block type="lexical_variable_get">'+'<title name="VAR">'+name+'</title>'+'</block>'+'<block type="lexical_variable_set">'+'<title name="VAR">'+name+'</title>'+'</block>'+'</xml>';return getterSetterXML;};/**
 * Constructs a FieldGlobalFlydown from a JSON arg object.
 * @param {!Object} options A JSON object with options.
 * @return {FieldParameterFlydown} The new field instance.
 * @package
 * @nocollapse
 */FieldGlobalFlydown.fromJson=function(options){var name=core_.utils.replaceMessageReferences(options['name']);return new FieldGlobalFlydown(name);};core_.fieldRegistry.register('field_global_flydown',FieldGlobalFlydown);
;// CONCATENATED MODULE: ./src/fields/field_nocheck_dropdown.js
// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @fileoverview Specialization of Blockly's FieldDropdown to allow setting
 * the value even if it's not one of the dynamically generated options.  We use
 * this in situations where we know that the value will eventually be in the
 * generated set.  This can occur, for example, when we are loading from XML and
 * we have procedure call blocks being created before their respective
 * procedure definition block.
 *
 * @author mark.friedman@gmail.com (Mark Friedman)
 */function field_nocheck_dropdown_typeof(obj){"@babel/helpers - typeof";return field_nocheck_dropdown_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_nocheck_dropdown_typeof(obj);}function field_nocheck_dropdown_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,field_nocheck_dropdown_toPropertyKey(descriptor.key),descriptor);}}function field_nocheck_dropdown_createClass(Constructor,protoProps,staticProps){if(protoProps)field_nocheck_dropdown_defineProperties(Constructor.prototype,protoProps);if(staticProps)field_nocheck_dropdown_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function field_nocheck_dropdown_toPropertyKey(arg){var key=field_nocheck_dropdown_toPrimitive(arg,"string");return field_nocheck_dropdown_typeof(key)==="symbol"?key:String(key);}function field_nocheck_dropdown_toPrimitive(input,hint){if(field_nocheck_dropdown_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_nocheck_dropdown_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function field_nocheck_dropdown_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function field_nocheck_dropdown_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)field_nocheck_dropdown_setPrototypeOf(subClass,superClass);}function field_nocheck_dropdown_setPrototypeOf(o,p){field_nocheck_dropdown_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return field_nocheck_dropdown_setPrototypeOf(o,p);}function field_nocheck_dropdown_createSuper(Derived){var hasNativeReflectConstruct=field_nocheck_dropdown_isNativeReflectConstruct();return function _createSuperInternal(){var Super=field_nocheck_dropdown_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=field_nocheck_dropdown_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return field_nocheck_dropdown_possibleConstructorReturn(this,result);};}function field_nocheck_dropdown_possibleConstructorReturn(self,call){if(call&&(field_nocheck_dropdown_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return field_nocheck_dropdown_assertThisInitialized(self);}function field_nocheck_dropdown_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function field_nocheck_dropdown_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function field_nocheck_dropdown_getPrototypeOf(o){field_nocheck_dropdown_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return field_nocheck_dropdown_getPrototypeOf(o);}var FieldNoCheckDropdown=/*#__PURE__*/function(_Blockly$FieldDropdow){field_nocheck_dropdown_inherits(FieldNoCheckDropdown,_Blockly$FieldDropdow);var _super=field_nocheck_dropdown_createSuper(FieldNoCheckDropdown);function FieldNoCheckDropdown(){field_nocheck_dropdown_classCallCheck(this,FieldNoCheckDropdown);for(var _len=arguments.length,args=new Array(_len),_key=0;_key<_len;_key++){args[_key]=arguments[_key];}// Call superclass constructor
return _super.call.apply(_super,[this].concat(args));}return field_nocheck_dropdown_createClass(FieldNoCheckDropdown);}(core_.FieldDropdown);FieldNoCheckDropdown.prototype.doClassValidation_=function(opt_newValue){var isValueValid=false;var options=this.getOptions(true);for(var i=0,option;option=options[i];i++){// Options are tuples of human-readable text and language-neutral values.
if(option[1]===opt_newValue){isValueValid=true;break;}}if(!isValueValid){// Add the value to the cached options array.  Note that this is
// potentially fragile, as it depends on knowledge of the
// Blockly.FieldDropdown implementation.
this.generatedOptions_.push([opt_newValue,opt_newValue]);}return(/** @type {string} */opt_newValue);};/**
 * Construct a FieldNoCheckDropdown from a JSON arg object.
 * @param {!Object} options A JSON object with options (options).
 * @return {!FieldNoCheckDropdown} The new field instance.
 * @package
 * @nocollapse
 */FieldNoCheckDropdown.fromJson=function(options){return new FieldNoCheckDropdown(options['options'],undefined,options);};core_.fieldRegistry.register('field_nocheck_dropdown',FieldNoCheckDropdown);
;// CONCATENATED MODULE: ./src/fields/field_parameter_flydown.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Editable parameter field with flydown menu of a getter and
 *   setter block.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */function field_parameter_flydown_typeof(obj){"@babel/helpers - typeof";return field_parameter_flydown_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_parameter_flydown_typeof(obj);}function field_parameter_flydown_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,field_parameter_flydown_toPropertyKey(descriptor.key),descriptor);}}function field_parameter_flydown_createClass(Constructor,protoProps,staticProps){if(protoProps)field_parameter_flydown_defineProperties(Constructor.prototype,protoProps);if(staticProps)field_parameter_flydown_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function field_parameter_flydown_toPropertyKey(arg){var key=field_parameter_flydown_toPrimitive(arg,"string");return field_parameter_flydown_typeof(key)==="symbol"?key:String(key);}function field_parameter_flydown_toPrimitive(input,hint){if(field_parameter_flydown_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_parameter_flydown_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function field_parameter_flydown_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function field_parameter_flydown_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)field_parameter_flydown_setPrototypeOf(subClass,superClass);}function field_parameter_flydown_setPrototypeOf(o,p){field_parameter_flydown_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return field_parameter_flydown_setPrototypeOf(o,p);}function field_parameter_flydown_createSuper(Derived){var hasNativeReflectConstruct=field_parameter_flydown_isNativeReflectConstruct();return function _createSuperInternal(){var Super=field_parameter_flydown_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=field_parameter_flydown_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return field_parameter_flydown_possibleConstructorReturn(this,result);};}function field_parameter_flydown_possibleConstructorReturn(self,call){if(call&&(field_parameter_flydown_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return field_parameter_flydown_assertThisInitialized(self);}function field_parameter_flydown_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function field_parameter_flydown_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function field_parameter_flydown_getPrototypeOf(o){field_parameter_flydown_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return field_parameter_flydown_getPrototypeOf(o);}/**
 * Class for a parameter declaration field with flyout menu of getter/setter
 * blocks on mouse over.
 * @param {string} name The initial parameter name in the field.
 * @param {boolean} isEditable Indicates whether the the name in the flydown is
 *     editable.
 * @param {?string=} opt_displayLocation The location to display the flydown at
 *     Either: FieldFlydown.DISPLAY_BELOW,
 *             FieldFlydown.DISPLAY_RIGHT
 *     Defaults to DISPLAY_RIGHT.
 * @param {?function=} opt_additionalChangeHandler A one-arg function indicating
 *     what to do in addition to renaming lexical variables. May be
 *     null/undefined to indicate nothing extra to be done.
 * @extends {FieldFlydown}
 * @constructor
 */ // [lyn, 10/26/13] Added opt_additionalChangeHandler to handle propagation of
//    renaming of proc decl params
var FieldParameterFlydown=/*#__PURE__*/function(_FieldFlydown){field_parameter_flydown_inherits(FieldParameterFlydown,_FieldFlydown);var _super=field_parameter_flydown_createSuper(FieldParameterFlydown);function FieldParameterFlydown(name,isEditable,opt_displayLocation,opt_additionalChangeHandler){field_parameter_flydown_classCallCheck(this,FieldParameterFlydown);var changeHandler=function changeHandler(text){if(!FieldParameterFlydown.changeHandlerEnabled){return text;}// Both of these should be called in the context of the field (ie
// 'this').
var possiblyRenamedText=LexicalVariable.renameParam.call(this,text);if(opt_additionalChangeHandler){opt_additionalChangeHandler.call(this,possiblyRenamedText);}return possiblyRenamedText;};return _super.call(this,name,isEditable,opt_displayLocation,changeHandler);}return field_parameter_flydown_createClass(FieldParameterFlydown);}(FieldFlydown);FieldParameterFlydown.prototype.fieldCSSClassName='blocklyFieldParameter';FieldParameterFlydown.prototype.flyoutCSSClassName='blocklyFieldParameterFlydown';// [lyn, 07/02/14] Added this flag to control changeHandler
//   There are several spots where we want to disable the changeHandler to avoid
//   unwanted calls to renameParam, such as when these fields are deleted and
//   then readded in updates to procedures and local variable declarations.
FieldParameterFlydown.changeHandlerEnabled=true;// [lyn, 07/02/14] Execute thunk with changeHandler disabled
FieldParameterFlydown.withChangeHanderDisabled=function(thunk){var oldFlag=FieldParameterFlydown.changeHandlerEnabled;FieldParameterFlydown.changeHandlerEnabled=false;try{thunk();}finally{FieldParameterFlydown.changeHandlerEnabled=oldFlag;}};/**
 * Returns the stringified xml representation of the blocks we want to have in
 * the flydown. In this case a variable getter and a variable setter.
 * @return {string} The stringified XML.
 */FieldParameterFlydown.prototype.flydownBlocksXML_=function(){// TODO: Refactor this to use getValue() instead of getText(). getText()
//   refers to the view, while getValue refers to the model (in MVC terms).
// Name in this parameter field.
var name=this.getText();var getterSetterXML='<xml>'+'<block type="lexical_variable_get">'+'<field name="VAR">'+name+'</field>'+'</block>'+'<block type="lexical_variable_set">'+'<field name="VAR">'+name+'</field>'+'</block>'+'</xml>';return getterSetterXML;};/**
 * [lyn, 10/24/13]
 * Add an option for toggling horizontal vs. Vertical placement of parameter
 * lists on the given block. Put before "Collapse Block in uncollapsed block"
 * [lyn, 10/27/13] Also remove any "Inline Inputs" option, since vertical
 * params
 * doesn't interact well with it (in procedures_defreturn).
 * @param block
 * @param options
 */FieldParameterFlydown.addHorizontalVerticalOption=function(block,options){var numParams=0;if(block.getParameters){numParams=block.getParameters().length;}if(block.isCollapsed()||numParams<=0){return;}var horizVertOption={enabled:true,text:block.horizontalParameters?core_.Msg.VERTICAL_PARAMETERS:core_.Msg.HORIZONTAL_PARAMETERS,callback:function callback(){// TODO: We should force the inputs to be external when we do this.
//   If someone sets the inputs inline and then sets the parameters to
//   vertical we get the same visual bug as 10/27/13.
block.setParameterOrientation(!block.horizontalParameters);}};// Find the index of "Collapse Block" and insert this option before it.
var insertionIndex=0;for(var option;option=options[insertionIndex];insertionIndex++){if(option.text==core_.Msg.COLLAPSE_BLOCK){break;}}// Insert even if we didn't find the option.
options.splice(insertionIndex,0,horizVertOption);// Remove an "Inline Inputs" option (if there is one).
for(var i=0,_option;_option=options[i];i++){if(_option.text==core_.Msg.INLINE_INPUTS){options.splice(i,1);break;}}};/**
 * Constructs a FieldParameterFlydown from a JSON arg object.
 * @param {!Object} options A JSON object with options.
 * @return {FieldParameterFlydown} The new field instance.
 * @package
 * @nocollapse
 */FieldParameterFlydown.fromJson=function(options){var name=core_.utils.replaceMessageReferences(options['name']);return new FieldParameterFlydown(name,options['is_editable']);};core_.fieldRegistry.register('field_parameter_flydown',FieldParameterFlydown);
;// CONCATENATED MODULE: ./src/fields/field_procedurename.js
// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Specialization of Blockly's FieldTextInput to handle logic of
 *     procedure renaming.
 * @author ewpatton@mit.edu (Evan W. Patton)
 */function field_procedurename_typeof(obj){"@babel/helpers - typeof";return field_procedurename_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(obj){return typeof obj;}:function(obj){return obj&&"function"==typeof Symbol&&obj.constructor===Symbol&&obj!==Symbol.prototype?"symbol":typeof obj;},field_procedurename_typeof(obj);}function field_procedurename_classCallCheck(instance,Constructor){if(!(instance instanceof Constructor)){throw new TypeError("Cannot call a class as a function");}}function field_procedurename_defineProperties(target,props){for(var i=0;i<props.length;i++){var descriptor=props[i];descriptor.enumerable=descriptor.enumerable||false;descriptor.configurable=true;if("value"in descriptor)descriptor.writable=true;Object.defineProperty(target,field_procedurename_toPropertyKey(descriptor.key),descriptor);}}function field_procedurename_createClass(Constructor,protoProps,staticProps){if(protoProps)field_procedurename_defineProperties(Constructor.prototype,protoProps);if(staticProps)field_procedurename_defineProperties(Constructor,staticProps);Object.defineProperty(Constructor,"prototype",{writable:false});return Constructor;}function field_procedurename_toPropertyKey(arg){var key=field_procedurename_toPrimitive(arg,"string");return field_procedurename_typeof(key)==="symbol"?key:String(key);}function field_procedurename_toPrimitive(input,hint){if(field_procedurename_typeof(input)!=="object"||input===null)return input;var prim=input[Symbol.toPrimitive];if(prim!==undefined){var res=prim.call(input,hint||"default");if(field_procedurename_typeof(res)!=="object")return res;throw new TypeError("@@toPrimitive must return a primitive value.");}return(hint==="string"?String:Number)(input);}function field_procedurename_get(){if(typeof Reflect!=="undefined"&&Reflect.get){field_procedurename_get=Reflect.get.bind();}else{field_procedurename_get=function _get(target,property,receiver){var base=field_procedurename_superPropBase(target,property);if(!base)return;var desc=Object.getOwnPropertyDescriptor(base,property);if(desc.get){return desc.get.call(arguments.length<3?target:receiver);}return desc.value;};}return field_procedurename_get.apply(this,arguments);}function field_procedurename_superPropBase(object,property){while(!Object.prototype.hasOwnProperty.call(object,property)){object=field_procedurename_getPrototypeOf(object);if(object===null)break;}return object;}function field_procedurename_inherits(subClass,superClass){if(typeof superClass!=="function"&&superClass!==null){throw new TypeError("Super expression must either be null or a function");}subClass.prototype=Object.create(superClass&&superClass.prototype,{constructor:{value:subClass,writable:true,configurable:true}});Object.defineProperty(subClass,"prototype",{writable:false});if(superClass)field_procedurename_setPrototypeOf(subClass,superClass);}function field_procedurename_setPrototypeOf(o,p){field_procedurename_setPrototypeOf=Object.setPrototypeOf?Object.setPrototypeOf.bind():function _setPrototypeOf(o,p){o.__proto__=p;return o;};return field_procedurename_setPrototypeOf(o,p);}function field_procedurename_createSuper(Derived){var hasNativeReflectConstruct=field_procedurename_isNativeReflectConstruct();return function _createSuperInternal(){var Super=field_procedurename_getPrototypeOf(Derived),result;if(hasNativeReflectConstruct){var NewTarget=field_procedurename_getPrototypeOf(this).constructor;result=Reflect.construct(Super,arguments,NewTarget);}else{result=Super.apply(this,arguments);}return field_procedurename_possibleConstructorReturn(this,result);};}function field_procedurename_possibleConstructorReturn(self,call){if(call&&(field_procedurename_typeof(call)==="object"||typeof call==="function")){return call;}else if(call!==void 0){throw new TypeError("Derived constructors may only return object or undefined");}return field_procedurename_assertThisInitialized(self);}function field_procedurename_assertThisInitialized(self){if(self===void 0){throw new ReferenceError("this hasn't been initialised - super() hasn't been called");}return self;}function field_procedurename_isNativeReflectConstruct(){if(typeof Reflect==="undefined"||!Reflect.construct)return false;if(Reflect.construct.sham)return false;if(typeof Proxy==="function")return true;try{Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],function(){}));return true;}catch(e){return false;}}function field_procedurename_getPrototypeOf(o){field_procedurename_getPrototypeOf=Object.setPrototypeOf?Object.getPrototypeOf.bind():function _getPrototypeOf(o){return o.__proto__||Object.getPrototypeOf(o);};return field_procedurename_getPrototypeOf(o);}/**
 * FieldProcedureName is a specialization of {@link Blockly.FieldTextInput}
 * that handles renaming procedures in the {@link ProcedureDatabase}
 * when the procedure's name is changed.
 * @param {?string} text
 * @constructor
 */var FieldProcedureName=/*#__PURE__*/function(_Blockly$FieldTextInp){field_procedurename_inherits(FieldProcedureName,_Blockly$FieldTextInp);var _super=field_procedurename_createSuper(FieldProcedureName);function FieldProcedureName(text){field_procedurename_classCallCheck(this,FieldProcedureName);return _super.call(this,text,renameProcedure);}field_procedurename_createClass(FieldProcedureName,[{key:"setValue",value:/**
   * Set the value of the field.
   *
   * @see Blockly.FieldTextInput.setValue
   * @param {?string} newValue The new value of the field.
   * @override
   */function setValue(newValue){var oldValue=this.getValue();this.oldName_=oldValue;this.doValueUpdate_(newValue);field_procedurename_get(field_procedurename_getPrototypeOf(FieldProcedureName.prototype),"setValue",this).call(this,newValue);newValue=this.getValue();if(typeof newValue==='string'&&this.sourceBlock_){var procDb=this.sourceBlock_.workspace.getProcedureDatabase();if(procDb){if(procDb.getProcedure(this.sourceBlock_.id)){procDb.renameProcedure(this.sourceBlock_.id,oldValue,newValue);}else{procDb.addProcedure(newValue,this.sourceBlock_);}}}this.oldName_=undefined;}}]);return FieldProcedureName;}(core_.FieldTextInput);/*
FieldProcedureName.prototype.onHtmlInputChange_ = function(e) {
  if (e.type == 'keypress') {
    console.log('Suppressed keypress event');
    return;  // suppress change handling on key press
  }
  console.log("input's value is " + Blockly.FieldTextInput.htmlInput_.value);
  FieldProcedureName.superClass_.onHtmlInputChange_.call(this, e);
};
*/ /**
 * Constructs a FieldProcedureName from a JSON arg object.
 * @param {!Object} options A JSON object with options.
 * @return {FieldProcedureName} The new field instance.
 * @package
 * @nocollapse
 */FieldProcedureName.fromJson=function(options){var name=core_.utils.replaceMessageReferences(options['name']);return new FieldProcedureName(name);};core_.fieldRegistry.register('field_procedurename',FieldProcedureName);
;// CONCATENATED MODULE: ./src/blocks/lexical-variables.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Variables blocks for Blockly, modified for MIT App Inventor.
 * @author fturbak@wellesley.edu (Lyn Turbak)
 */ /**
 * Lyn's History:
 *  * [lyn, written 11/16-17/13, added 07/01/14]
 *   + Added freeVariables, renameFree, and renameBound to local declarations
 *   + Added freeVariables and renameFree to getters and setters
 *   + Added renameVar and renameVars to local declaration
 *   + renamed addDeclarationInputs_ to updatedDeclarationInputs_
 * [lyn, 03/04/13]
 *   + Remove notion of collapsed input from local variable declaration
 * statements/expressions, which has been eliminated in updated to Blockly
 * v1636.
 *   + Update appendTitle* to appendField*
 * [lyn, 01/18/13] Remove onchange from lexical_variable_get and
 * lexical_variable_set. This fixes issue 667 (Variable getter/setter names
 * deleted in copied blocks) and improves laggy drag problem.
 * [lyn, 10/27/13]
 *   + Modified local declaration parameter flydowns so editing the name
 * changes corresponding name in an open mutator.
 *   + Changed local declaration compose() to rebuild inputs only if local
 * names have changed.
 *     (essential for getting param flydown name changes reflected in open
 * mutator).
 *   + Fixed local declaration expression compose() to be the same as that for
 * local declaration statements.
 *   + Modified addDeclarationInputs_ to remove existing declarations, add new
 * ones, and keep last two declarations (body and collapsed text) rather than
 * recreating them. This is now used by both domToMutation() and compose(),
 * eliminating duplicated code.
 *   + Eliminated dummy declarations.
 *   + Specify direction of flydowns
 * [lyn, 10/25/13] Made collapsed block labels more sensible.
 * [lyn, 10/10-14/13]
 *   + Installed variable declaration flydowns in global definition and local
 * variable declaration statements and expressions.
 *   + Abstracted over string labels on all blocks using constants defined in
 * en/_messages.js
 *   + Cleaned up code, including refactoring to increase sharing between
 *     local_declaration_statement and local_declaration_expression.
 *   + Fixed bug: Modified onchange for local declarations to keep localNames_
 * instance variable updated when param is edited directly on declaration
 * block.
 *   + In local variable statements/expression, changed both "in do" and "in
 * return" to "scope" (shape distinguishes them). But maybe these should just
 * be empty string?
 * [lyn, 11/18/12] Renaming for globals (still working on renaming of procedure
 * and loop params)
 * [lyn, 11/17/12] Integration of simple naming into App Inventor
 * [lyn, 11/11/12] More work on onchange event. Allow invalid names for
 * untethered getters/setters on workspace, but not when click in to other
 * blocks.
 * [lyn, 11/08-10/12] Get dropdown list of names in scope to work for globals
 * and params
 *                    (including loops) in raw blockly. Pass along to Andrew
 * for integration into AI. Initial work on onchange event to change names when
 * getters/setters copied and moved.
 * [lyn, 11/05-07/12] Add local variable declaration expressions. Get mutator
 * working for local declaration statements and expressions. But these don't
 * save/load properly from XML Helpful 10/7 hangout with Andrew and Paul.
 * [lyn, 11/04/12] Created. Add global declarations. Work on local variable
 * declaration statement.
 */ /*
// For debugging only
function myStringify (obj) {
  var seen = [];
  return JSON.stringify(obj, function(key, val) {
   if (typeof val == "object") {
       if (seen.indexOf(val) >= 0) {
           return undefined;
       }
       seen.push(val);
   }
   return val
   });
}
*/delete core_.Blocks.global_declaration;/**
 * Prototype bindings for a global variable declaration block.
 */core_.Blocks.global_declaration={// Global var defn
category:'Variables',helpUrl:core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL,init:function init(){this.setStyle('variable_blocks');this.appendValueInput('VALUE').appendField(core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT).appendField(new FieldGlobalFlydown(core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME,FieldFlydown.DISPLAY_BELOW),'NAME').appendField(core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO);this.setTooltip(core_.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP);},getVars:function getVars(){var field=this.getField('NAME');return field?[field.getText()]:[];},getGlobalNames:function getGlobalNames(){return this.getVars();},renameVar:function renameVar(oldName,newName){if(core_.Names.equals(oldName,this.getFieldValue('NAME'))){this.setFieldValue(newName,'NAME');}}};/**
 * Prototype bindings for a variable getter block.
 */core_.Blocks.lexical_variable_get={// Variable getter.
category:'Variables',helpUrl:core_.Msg.LANG_VARIABLES_GET_HELPURL,init:function init(){this.setStyle('variable_blocks');this.fieldVar_=new FieldLexicalVariable(' ');this.fieldVar_.setBlock(this);this.appendDummyInput().appendField(core_.Msg.LANG_VARIABLES_GET_TITLE_GET).appendField(this.fieldVar_,'VAR');this.setOutput(true,null);this.setTooltip(core_.Msg.LANG_VARIABLES_GET_TOOLTIP);this.errors=[{func:checkIsInDefinition},{func:checkDropDownContainsValidValue,dropDowns:['VAR']}];this.setOnChange(function(changeEvent){checkErrors(this);});},referenceResults:function referenceResults(name,prefix,env){var childrensReferenceResults=this.getChildren().map(function(blk){return LexicalVariable.referenceResult(blk,name,prefix,env);});var blocksToRename=[];var capturables=[];for(var r=0;r<childrensReferenceResults.length;r++){blocksToRename=blocksToRename.concat(childrensReferenceResults[r][0]);capturables=capturables.concat(childrensReferenceResults[r][1]);}var possiblyPrefixedReferenceName=this.getField('VAR').getText();var unprefixedPair=unprefixName(possiblyPrefixedReferenceName);var referencePrefix=unprefixedPair[0];var referenceName=unprefixedPair[1];var referenceNotInEnv=usePrefixInCode&&env.indexOf(possiblyPrefixedReferenceName)==-1||!usePrefixInCode&&env.indexOf(referenceName)==-1;if(!(referencePrefix===core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX)){if(referenceName===name&&referenceNotInEnv){// if referenceName refers to name and not some intervening
// declaration, it's a reference to be renamed:
blocksToRename.push(this);// Any intervening declared name with the same prefix as the searched
// for name can be captured:
if(usePrefixInCode){for(var i=0;i<env.length;i++){// env is a list of prefixed names.
var unprefixedEntry=unprefixName(env[i]);if(prefix===unprefixedEntry[0]){capturables.push(unprefixedEntry[1]);}}}else{// Shared.usePrefixInCode
capturables=capturables.concat(env);}}else if(referenceNotInEnv&&(!usePrefixInCode||prefix===referencePrefix)){// If reference is not in environment, it's externally declared and
// capturable When Shared.usePrefixInYail is true, only consider names
// with same prefix to be capturable
capturables.push(referenceName);}}return[[blocksToRename,capturables]];},getVars:function getVars(){return[this.getFieldValue('VAR')];},renameLexicalVar:function renameLexicalVar(oldName,newName,oldTranslatedName,newTranslatedName){if(oldTranslatedName===undefined){// Local variables
if(oldName===this.getFieldValue('VAR')){this.setFieldValue(newName,'VAR');}}else if(oldTranslatedName&&oldTranslatedName===this.fieldVar_.getText()){// Global variables
// Force a regeneration of the dropdown options, so the subsequent
// calls to setValue and setFieldValue will work properly.
this.fieldVar_.getOptions(false);this.fieldVar_.setValue(newName);if(oldName===newName){this.setFieldValue(newName,'VAR');}this.fieldVar_.forceRerender();}},renameFree:function renameFree(freeSubstitution){var prefixPair=unprefixName(this.getFieldValue('VAR'));var prefix=prefixPair[0];// Only rename lexical (nonglobal) names
if(prefix!==core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX){var oldName=prefixPair[1];var newName=freeSubstitution.apply(oldName);if(newName!==oldName){this.renameLexicalVar(oldName,newName);}}},freeVariables:function freeVariables(){// return the free lexical variables of this block
var prefixPair=unprefixName(this.getFieldValue('VAR'));var prefix=prefixPair[0];// Only return lexical (nonglobal) names
if(prefix!==core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX){var oldName=prefixPair[1];return new nameSet_NameSet([oldName]);}else{return new nameSet_NameSet();}}};/**
 * Prototype bindings for a variable setter block.
 */core_.Blocks.lexical_variable_set={// Variable setter.
category:'Variables',helpUrl:core_.Msg.LANG_VARIABLES_SET_HELPURL,// *** [lyn, 11/10/12] Fix
// this
init:function init(){this.setStyle('variable_blocks');this.fieldVar_=new FieldLexicalVariable(' ');this.fieldVar_.setBlock(this);this.appendValueInput('VALUE').appendField(core_.Msg.LANG_VARIABLES_SET_TITLE_SET).appendField(this.fieldVar_,'VAR').appendField(core_.Msg.LANG_VARIABLES_SET_TITLE_TO);this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_VARIABLES_SET_TOOLTIP);this.errors=[{func:checkIsInDefinition},{func:checkDropDownContainsValidValue,dropDowns:['VAR']}];this.setOnChange(function(changeEvent){checkErrors(this);});},referenceResults:core_.Blocks.lexical_variable_get.referenceResults,getVars:function getVars(){return[this.getFieldValue('VAR')];},renameLexicalVar:core_.Blocks.lexical_variable_get.renameLexicalVar,renameFree:function renameFree(freeSubstitution){// potentially rename the set variable
var prefixPair=unprefixName(this.getFieldValue('VAR'));var prefix=prefixPair[0];// Only rename lexical (nonglobal) names
if(prefix!==core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX){var oldName=prefixPair[1];var newName=freeSubstitution.apply(oldName);if(newName!==oldName){this.renameLexicalVar(oldName,newName);}}// [lyn, 06/26/2014] Don't forget to rename children!
this.getChildren().map(function(blk){LexicalVariable.renameFree(blk,freeSubstitution);});},freeVariables:function freeVariables(){// return the free lexical variables of this block
// [lyn, 06/27/2014] Find free vars of *all* children, including subsequent
// commands in NEXT slot.
var childrenFreeVars=this.getChildren().map(function(blk){return LexicalVariable.freeVariables(blk);});var result=nameSet_NameSet.unionAll(childrenFreeVars);var prefixPair=unprefixName(this.getFieldValue('VAR'));var prefix=prefixPair[0];// Only return lexical (nonglobal) names
if(prefix!==core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX){var oldName=prefixPair[1];result.insert(oldName);}return result;}};/**
 * Prototype bindings for a statement block that declares local names for use
 * in a statement body.
 * [lyn, 10/13/13] Refactored to share more code with
 * Blockly.Blocks.local_declaration_expression.
 */core_.Blocks.local_declaration_statement={// Define a procedure with no return value.
// category: null,  // Procedures are handled specially.
category:'Variables',// *** [lyn, 11/07/12] Abstract over this
helpUrl:core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL,bodyInputName:'STACK',init:function init(){this.setStyle('variable_blocks');this.initLocals();this.appendStatementInput('STACK').appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO);// Add notch and nub for vertical statement composition
this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP);this.lexicalVarPrefix=localNamePrefix;},referenceResults:function referenceResults(name,prefix,env){// Collect locally declared names ...
var localDeclNames=[];for(var i=0;this.getInput('DECL'+i);i++){var localName=this.getFieldValue('VAR'+i);// Invariant: Shared.showPrefixToUser must also be true!
if(usePrefixInCode){localName=possiblyPrefixMenuNameWith(localNamePrefix)(localName);}localDeclNames.push(localName);}var newEnv=env.concat(localDeclNames);// ... and add to environment
// Collect locally initialization expressions:
var localInits=[];for(var _i=0;this.getInput('DECL'+_i);_i++){var init=this.getInputTargetBlock('DECL'+_i);if(init){localInits.push(init);}}var initResults=localInits.map(function(init){return LexicalVariable.referenceResult(init,name,prefix,env);});var doResults=LexicalVariable.referenceResult(this.getInputTargetBlock('STACK'),name,prefix,newEnv);var nextResults=LexicalVariable.referenceResult(LexicalVariable.getNextTargetBlock(this),name,prefix,env);return initResults.concat([doResults,nextResults]);},withLexicalVarsAndPrefix:function withLexicalVarsAndPrefix(child,proc){if(this.getInputTargetBlock(this.bodyInputName)==child){var localNames=this.declaredNames();// not arguments_ instance var
for(var i=0;i<localNames.length;i++){proc(localNames[i],this.lexicalVarPrefix);}}},initLocals:function initLocals(){// Let the theme determine the color.
// this.setColour(Blockly.VARIABLE_CATEGORY_HUE);
this.setStyle('variable_blocks');this.localNames_=[core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME];var declInput=this.appendValueInput('DECL0');declInput.appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT).appendField(this.parameterFlydown(0),'VAR0').appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO).setAlign(core_.ALIGN_RIGHT);// Add mutator for editing local variable names
this.setMutator(new core_.Mutator(['local_mutatorarg']));},onchange:function onchange(){this.localNames_=this.declaredNames();// ensure localNames_ is in sync
// with paramFlydown fields
},// Store local names in mutation element of XML for block
mutationToDom:function mutationToDom(){var container=core_.utils.xml.createElement('mutation');for(var i=0;i<this.localNames_.length;i++){var parameter=core_.utils.xml.createElement('localname');parameter.setAttribute('name',this.localNames_[i]);container.appendChild(parameter);}return container;},// Retrieve local names from mutation element of XML for block
domToMutation:function domToMutation(xmlElement){// and replace existing declarations
var children=getChildren(xmlElement);if(children.length>0){// Ensure xml element is nonempty
// Else we'll overwrite initial list with "name" for new block
this.localNames_=[];for(var i=0,childNode;childNode=children[i];i++){if(childNode.nodeName.toLowerCase()=='localname'){this.localNames_.push(childNode.getAttribute('name'));}}}this.updateDeclarationInputs_(this.localNames_);// add declarations; inits
// are undefined
},updateDeclarationInputs_:function updateDeclarationInputs_(names,inits){// Modify this block to replace existing initializers by new declaration
// inputs created from names and inits. If inits is undefined, treat all
// initial expressions as undefined. Keep existing body at end of input
// list. [lyn, 03/04/13] As of change to, Blockly 1636, there is no longer
// a collapsed input at end.
// Remember last (= body) input
var bodyInput=this.inputList[this.inputList.length-1];// Body input
// for local
// declaration
var numDecls=this.inputList.length-1;// [lyn, 07/03/14] stop rendering until block is recreated
var savedRendered=this.rendered;this.rendered=false;// Modify this local-in-do block according to arrangement of name blocks in
// mutator editor. Remove all the local declaration inputs ...
var thisBlock=this;// Grab correct object for use in thunk below
FieldParameterFlydown.withChangeHanderDisabled(// [lyn, 07/02/14] Need to disable change handler, else this will try
// to rename params removed fields.
function(){for(var i=0;i<numDecls;i++){thisBlock.removeInput('DECL'+i);}});// Empty the inputList and recreate it, building local initializers from
// mutator
this.inputList=[];this.localNames_=names;for(var i=0;i<names.length;i++){var declInput=this.appendValueInput('DECL'+i);// [lyn, 11/06/12]
//   This was for case where tried to put "local" keyword on same line
// with first local name. But even though alignment set to
// Blockly.ALIGN_RIGHT, the input was left justified and covered the plus
// sign for popping up the mutator. So I put the "local" keyword on it's
// own line even though this wastes vertical space. This should be fixed
// in the future. if (i == 0) { declInput.appendField("local"); // Only
// put keyword "local" on top line. }
declInput.appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT).appendField(this.parameterFlydown(i),'VAR'+i).appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO).setAlign(core_.ALIGN_RIGHT);if(inits&&inits[i]){// If there is an initializer, connect it
declInput.connection.connect(inits[i]);}}// Now put back last (= body) input
this.inputList=this.inputList.concat(bodyInput);this.rendered=savedRendered;if(this.rendered){this.initSvg();this.render();}},// [lyn, 10/27/13] Introduced this to correctly handle renaming of mutatorarg
// in open mutator when procedure parameter flydown name is edited.
// Return a new local variable parameter flydown
parameterFlydown:function parameterFlydown(paramIndex){var initialParamName=this.localNames_[paramIndex];var localDecl=this;// Here, "this" is the local decl block. Name it to
// use in function below
var localParameterChangeHandler=function localParameterChangeHandler(newParamName){// This handler has the same subtleties as
// procedureParameterChangeHandler in language/common/procedures.js, but
// is somewhat simpler since doesn't have associated callers to change.
// See the notes there.
// See Subtleties #1 and #2 in  procedureParameterChangeHandler in
// language/common/procedures.js
var newLocals=localDecl.localNames_;newLocals[paramIndex]=newParamName;// If there's an open mutator, change the name in the corresponding slot.
if(localDecl.mutator&&localDecl.mutator.rootBlock_){// Iterate through mutatorarg param blocks and change name of one at
// paramIndex
var mutatorContainer=localDecl.mutator.rootBlock_;var mutatorargIndex=0;var mutatorarg=mutatorContainer.getInputTargetBlock('STACK');while(mutatorarg&&mutatorargIndex<paramIndex){mutatorarg=mutatorarg.nextConnection&&mutatorarg.nextConnection.targetBlock();mutatorargIndex++;}if(mutatorarg&&mutatorargIndex==paramIndex){// See Subtlety #3 in  procedureParameterChangeHandler in
// language/common/procedures.js
core_.Field.prototype.setValue.call(mutatorarg.getField('NAME'),newParamName);}}};return new FieldParameterFlydown(initialParamName,true,// name is editable
FieldFlydown.DISPLAY_RIGHT,localParameterChangeHandler);},decompose:function decompose(workspace){// Create "mutator" editor populated with name blocks with local variable
// names
var containerBlock=workspace.newBlock('local_mutatorcontainer');containerBlock.initSvg();containerBlock.setDefBlock(this);var connection=containerBlock.getInput('STACK').connection;for(var i=0;i<this.localNames_.length;i++){var localName=this.getFieldValue('VAR'+i);var nameBlock=workspace.newBlock('local_mutatorarg');nameBlock.initSvg();nameBlock.setFieldValue(localName,'NAME');// Store the old location.
nameBlock.oldLocation=i;connection.connect(nameBlock.previousConnection);connection=nameBlock.nextConnection;}return containerBlock;},compose:function compose(containerBlock){// [lyn, 10/27/13] Modified this so that doesn't rebuild block if names
// haven't changed. This is *essential* to handle Subtlety #3 in
// localParameterChangeHandler within parameterFlydown.
var newLocalNames=[];var initializers=[];var mutatorarg=containerBlock.getInputTargetBlock('STACK');while(mutatorarg){newLocalNames.push(mutatorarg.getFieldValue('NAME'));initializers.push(mutatorarg.valueConnection_);// pushes undefined if
// doesn't exist
mutatorarg=mutatorarg.nextConnection&&mutatorarg.nextConnection.targetBlock();}// Reconstruct inputs only if local list has changed
if(!LexicalVariable.stringListsEqual(this.localNames_,newLocalNames)){// Switch off rendering while the block is rebuilt.
// var savedRendered = this.rendered;
// this.rendered = false;
this.updateDeclarationInputs_(newLocalNames,initializers);// Restore rendering and show the changes.
// this.rendered = savedRendered;
// if (this.rendered) {
//  this.render();
// }
}},dispose:function dispose(){for(var _len=arguments.length,args=new Array(_len),_key=0;_key<_len;_key++){args[_key]=arguments[_key];}// *** [lyn, 11/07/12] Dunno if anything needs to be done here.
// Call parent's destructor.
core_.BlockSvg.prototype.dispose.apply(this,args);// [lyn, 11/07/12] In above line, don't know where "arguments" param comes
// from, but if it's remove, there's no clicking sound upon deleting the
// block!
},saveConnections:function saveConnections(containerBlock){// Store child initializer blocks for local name declarations with name
// blocks in mutator editor
var nameBlock=containerBlock.getInputTargetBlock('STACK');var i=0;while(nameBlock){var localDecl=this.getInput('DECL'+i);nameBlock.valueConnection_=localDecl&&localDecl.connection.targetConnection;i++;nameBlock=nameBlock.nextConnection&&nameBlock.nextConnection.targetBlock();}// Store body statement or expression connection
var bodyInput=this.getInput(this.bodyInputName);// 'STACK' or 'RETURN'
if(bodyInput){containerBlock.bodyConnection_=bodyInput.connection.targetConnection;}},getVars:function getVars(){var varList=[];for(var i=0,input;input=this.getField('VAR'+i);i++){varList.push(input.getValue());}return varList;},// Interface with LexicalVariable.renameParam
declaredNames:function declaredNames(){return this.getVars();},declaredVariables:function declaredVariables(){return this.getVars();},// [lyn, 11/16/13 ] Return all the initializer connections
initializerConnections:function initializerConnections(){var connections=[];for(var i=0,input;input=this.getInput('DECL'+i);i++){connections.push(input.connection&&input.connection.targetConnection);}return connections;},// Interface with LexicalVariable.renameParam
blocksInScope:function blocksInScope(){// *** [lyn, 11/24/12]
// This will go away with DO-AND-RETURN block
var doBody=this.getInputTargetBlock(this.bodyInputName);// List of non-null doBody or empty list for null doBody
var doBodyList=doBody&&[doBody]||[];return doBodyList;// List of non-null body elements.
},renameVar:function renameVar(oldName,newName){this.renameVars(Substitution.simpleSubstitution(oldName,newName));},// substitution is a dict (i.e., object) mapping old names to new ones
renameVars:function renameVars(substitution){var localNames=this.declaredNames();var renamedLocalNames=substitution.map(localNames);if(!LexicalVariable.stringListsEqual(renamedLocalNames,localNames)){var initializerConnections=this.initializerConnections();this.updateDeclarationInputs_(renamedLocalNames,initializerConnections);// Update the mutator's variables if the mutator is open.
if(this.mutator&&this.mutator.isVisible()){var blocks=this.mutator.workspace_.getAllBlocks();for(var x=0,block;block=blocks[x];x++){if(block.type=='procedures_mutatorarg'){var oldName=block.getFieldValue('NAME');var newName=substitution.apply(oldName);if(newName!==oldName){block.setFieldValue(newName,'NAME');}}}}}},renameBound:function renameBound(boundSubstitution,freeSubstitution){var oldMutation=core_.Xml.domToText(this.mutationToDom());var localNames=this.declaredNames();for(var i=0;i<localNames.length;i++){// This is LET semantics, not LET* semantics, and needs to change!
LexicalVariable.renameFree(this.getInputTargetBlock('DECL'+i),freeSubstitution);}var paramSubstitution=boundSubstitution.restrictDomain(localNames);this.renameVars(paramSubstitution);var newFreeSubstitution=freeSubstitution.remove(localNames).extend(paramSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock(this.bodyInputName),newFreeSubstitution);var newMutation=core_.Xml.domToText(this.mutationToDom());if(core_.Events.isEnabled()){core_.Events.fire(new core_.Events.BlockChange(this,'mutation',null,oldMutation,newMutation));}if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();LexicalVariable.renameFree(nextBlock,freeSubstitution);}},renameFree:function renameFree(freeSubstitution){// This is LET semantics, not LET* semantics, and needs to change!
var localNames=this.declaredNames();var localNameSet=new nameSet_NameSet(localNames);var bodyFreeVars=LexicalVariable.freeVariables(this.getInputTargetBlock(this.bodyInputName));bodyFreeVars.subtract(localNameSet);var renamedFreeVars=bodyFreeVars.renamed(freeSubstitution);var capturedVars=renamedFreeVars.intersection(localNameSet);if(!capturedVars.isEmpty()){// Case where some names are captured!
// Must consistently rename declarations and uses of capturedFreeVars
// with
// names that do not conflict with renamedFreeVars, localNames, or each
// other.
var forbiddenNames=localNameSet.union(renamedFreeVars).toList();var boundBindings={};var capturedVarList=capturedVars.toList();for(var i=0,capturedVar;capturedVar=capturedVarList[i];i++){var newCapturedVar=FieldLexicalVariable.nameNotIn(capturedVar,forbiddenNames);boundBindings[capturedVar]=newCapturedVar;forbiddenNames.push(newCapturedVar);}this.renameBound(new Substitution(boundBindings),freeSubstitution);}else{this.renameBound(new Substitution(),freeSubstitution);}},freeVariables:function freeVariables(){// return the free lexical variables of this block
var result=LexicalVariable.freeVariables(this.getInputTargetBlock(this.bodyInputName));var localNames=this.declaredNames();result.subtract(new nameSet_NameSet(localNames));// This is LET semantics,
// not LET* semantics,
// but should be changed!
var numDecls=localNames.length;for(var i=0;i<numDecls;i++){result.unite(LexicalVariable.freeVariables(this.getInputTargetBlock('DECL'+i)));}if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();result.unite(LexicalVariable.freeVariables(nextBlock));}return result;}};/**
 * Prototype bindings for an expression block that declares local names for use
 * in an expression body.
 * [lyn, 10/13/13] Refactored to share more code with
 * Blockly.Blocks.local_declaration_statement.
 */core_.Blocks.local_declaration_expression={category:'Variables',// *** [lyn, 11/07/12] Abstract over this
helpUrl:core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL,initLocals:core_.Blocks.local_declaration_statement.initLocals,bodyInputName:'RETURN',init:function init(){this.setStyle('variables_blocks');this.initLocals();// this.appendIndentedValueInput('RETURN')
this.appendValueInput('RETURN').appendField(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN);// Create plug for expression output
this.setOutput(true,null);this.setTooltip(core_.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP);},referenceResults:function referenceResults(name,prefix,env){// Collect locally declared names ...
var localDeclNames=[];for(var i=0;this.getInput('DECL'+i);i++){var localName=this.getFieldValue('VAR'+i);// Invariant: Shared.showPrefixToUser must also be true!
if(usePrefixInCode){localName=possiblyPrefixMenuNameWith(localNamePrefix)(localName);}localDeclNames.push(localName);}var newEnv=env.concat(localDeclNames);// ... and add to environment
// Collect locally initialization expressions:
var localInits=[];for(var _i2=0;this.getInput('DECL'+_i2);_i2++){var init=this.getInputTargetBlock('DECL'+_i2);if(init){localInits.push(init);}}var initResults=localInits.map(function(init){return LexicalVariable.referenceResult(init,name,prefix,env);});var returnResults=LexicalVariable.referenceResult(this.getInputTargetBlock('RETURN'),name,prefix,newEnv);return initResults.concat([returnResults]);},withLexicalVarsAndPrefix:core_.Blocks.local_declaration_statement.withLexicalVarsAndPrefix,onchange:core_.Blocks.local_declaration_statement.onchange,mutationToDom:core_.Blocks.local_declaration_statement.mutationToDom,domToMutation:core_.Blocks.local_declaration_statement.domToMutation,updateDeclarationInputs_:core_.Blocks.local_declaration_statement.updateDeclarationInputs_,parameterFlydown:core_.Blocks.local_declaration_statement.parameterFlydown,blocksInScope:core_.Blocks.local_declaration_statement.blocksInScope,decompose:core_.Blocks.local_declaration_statement.decompose,compose:core_.Blocks.local_declaration_statement.compose,dispose:core_.Blocks.local_declaration_statement.dispose,saveConnections:core_.Blocks.local_declaration_statement.saveConnections,getVars:core_.Blocks.local_declaration_statement.getVars,declaredNames:core_.Blocks.local_declaration_statement.declaredNames,declaredVariables:core_.Blocks.local_declaration_statement.declaredVariables,renameVar:core_.Blocks.local_declaration_statement.renameVar,renameVars:core_.Blocks.local_declaration_statement.renameVars,renameBound:core_.Blocks.local_declaration_statement.renameBound,renameFree:core_.Blocks.local_declaration_statement.renameFree,freeVariables:core_.Blocks.local_declaration_statement.freeVariables};core_.Blocks.local_mutatorcontainer={// Local variable container (for mutator dialog).
init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.VARIABLE_CATEGORY_HUE);
this.setStyle('variable_blocks');this.appendDummyInput().appendField(core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES);this.appendStatementInput('STACK');this.setTooltip(core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP);this.contextMenu=false;this.mustNotRenameCapturables=true;},// [lyn. 11/24/12] Set procBlock associated with this container.
setDefBlock:function setDefBlock(defBlock){this.defBlock_=defBlock;},// [lyn. 11/24/12] Set procBlock associated with this container.
// Invariant: should not be null, since only created as mutator for a
// particular proc block.
getDefBlock:function getDefBlock(){return this.defBlock_;},// [lyn. 11/24/12] Return list of param names in this container
// Invariant: there should be no duplicates!
declaredNames:function declaredNames(){var paramNames=[];var paramBlock=this.getInputTargetBlock('STACK');while(paramBlock){paramNames.push(paramBlock.getFieldValue('NAME'));paramBlock=paramBlock.nextConnection&&paramBlock.nextConnection.targetBlock();}return paramNames;}};core_.Blocks.local_mutatorarg={// Procedure argument (for mutator dialog).
init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.VARIABLE_CATEGORY_HUE);
this.setStyle('variable_blocks');this.appendDummyInput().appendField(core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME).appendField(new core_.FieldTextInput(core_.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE,LexicalVariable.renameParam),'NAME');this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip('');this.contextMenu=false;this.lexicalVarPrefix=localNamePrefix;this.mustNotRenameCapturables=true;},getContainerBlock:function getContainerBlock(){var parent=this.getParent();while(parent&&!(parent.type==='local_mutatorcontainer')){parent=parent.getParent();}// [lyn, 11/24/12] Cache most recent container block so can reference it
// upon removal from mutator arg stack
this.cachedContainerBlock_=parent&&parent.type==='local_mutatorcontainer'&&parent||null;return this.cachedContainerBlock_;},getDefBlock:function getDefBlock(){var container=this.getContainerBlock();return container&&container.getDefBlock()||null;},blocksInScope:function blocksInScope(){var defBlock=this.getDefBlock();return defBlock&&defBlock.blocksInScope()||[];},declaredNames:function declaredNames(){var container=this.getContainerBlock();return container&&container.declaredNames()||[];},// [lyn, 11/24/12] Check for situation in which mutator arg has been removed
// from stack,
onchange:function onchange(){var paramName=this.getFieldValue('NAME');if(paramName){// paramName is null when delete from stack
// console.log("Mutatorarg onchange: " + paramName);
var cachedContainer=this.cachedContainerBlock_;var container=this.getContainerBlock();// Order is important; this
// must come after
// cachedContainer since it
// sets cachedContainerBlock_
// console.log("Mutatorarg onchange: " + paramName
//            + "; cachedContainer = " + JSON.stringify((cachedContainer
// && cachedContainer.type) || null) + "; container = " +
// JSON.stringify((container && container.type) || null));
if(!cachedContainer&&container){// Event: added mutator arg to container stack
// console.log("Mutatorarg onchange ADDED: " + paramName);
var declaredNames=this.declaredNames();var firstIndex=declaredNames.indexOf(paramName);if(firstIndex!=-1){// Assertion: we should get here, since paramName should be among
// names
var secondIndex=declaredNames.indexOf(paramName,firstIndex+1);if(secondIndex!=-1){// If we get here, there is a duplicate on insertion that must be
// resolved
var newName=FieldLexicalVariable.nameNotIn(paramName,declaredNames);this.setFieldValue(newName,'NAME');}}}}}};
;// CONCATENATED MODULE: ./src/blocks/controls.js
/**
 * @fileoverview Control blocks for Blockly, modified for App Inventor.
 * @author fraser@google.com (Neil Fraser)
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to language files.
 */ /**
 * Lyn's History:
 * [lyn, written 11/16-17/13, added 07/01/14] Added freeVariables, renameFree,
 * and renameBound to forRange and forEach loops
 * [lyn, 10/27/13] Specify direction of flydowns
 * [lyn, 10/25/13] Made collapsed block labels more sensible.
 * [lyn, 10/10-14/13]
 *   + Installed flydown index variable declarations in forRange and forEach
 * loops
 *   + Abstracted over string labels on all blocks using constants defined in
 * en/_messages.js
 *   + Renamed "for <i> start [] end [] step []" block to "for each <number>
 * from [] to [] by []"
 *   + Renamed "for each <i> in list []" block to "for each <item> in list []"
 *   + Renamed "choose test [] then-return [] else-return []" to "if [] then []
 * else []"
 *     (TODO: still needs to have a mutator like  the "if" statement blocks).
 *   + Renamed "evaluate" block to "evaluate but ignore result"
 *   + Renamed "do {} then-return []" block to "do {} result []" and re-added
 * this block to the Control drawer (who removed it?)
 *   + Removed get block (still in Variable drawer; no longer needed with
 * parameter flydowns)
 * [lyn, 11/29-30/12]
 *   + Change forEach and forRange loops to take name as input text rather than
 * via plug.
 *   + For these blocks, add extra methods to support renaming.
 */core_.Blocks.controls_forRange={// For range.
category:'Control',helpUrl:core_.Msg.LANG_CONTROLS_FORRANGE_HELPURL,init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.CONTROL_CATEGORY_HUE);
this.setStyle('loop_blocks');// this.setOutput(true, null);
// Need to deal with variables here
// [lyn, 11/30/12] Changed variable to be text input box that does renaming
// right (i.e., avoids variable capture)
this.appendValueInput('START').setCheck(yailTypeToBlocklyType('number',INPUT)).appendField(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM).appendField(new FieldParameterFlydown(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR,true,FieldFlydown.DISPLAY_BELOW),'VAR').appendField(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_START).setAlign(core_.ALIGN_RIGHT);this.appendValueInput('END').setCheck(yailTypeToBlocklyType('number',INPUT)).appendField(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_END).setAlign(core_.ALIGN_RIGHT);this.appendValueInput('STEP').setCheck(yailTypeToBlocklyType('number',INPUT)).appendField(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP).setAlign(core_.ALIGN_RIGHT);this.appendStatementInput('DO').appendField(core_.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO).setAlign(core_.ALIGN_RIGHT);this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP);this.lexicalVarPrefix=loopRangeParameterPrefix;},referenceResults:function referenceResults(name,prefix,env){var loopVar=this.getFieldValue('VAR');// Invariant: Shared.showPrefixToUser must also be true!
if(usePrefixInCode){loopVar=possiblyPrefixMenuNameWith(loopRangeParameterPrefix)(loopVar);}var newEnv=env.concat([loopVar]);var startResults=LexicalVariable.referenceResult(this.getInputTargetBlock('START'),name,prefix,env);var endResults=LexicalVariable.referenceResult(this.getInputTargetBlock('END'),name,prefix,env);var stepResults=LexicalVariable.referenceResult(this.getInputTargetBlock('STEP'),name,prefix,env);var doResults=LexicalVariable.referenceResult(this.getInputTargetBlock('DO'),name,prefix,newEnv);var nextResults=LexicalVariable.referenceResult(LexicalVariable.getNextTargetBlock(this),name,prefix,env);return[startResults,endResults,stepResults,doResults,nextResults];},withLexicalVarsAndPrefix:function withLexicalVarsAndPrefix(child,proc){if(this.getInputTargetBlock('DO')==child){var lexVar=this.getFieldValue('VAR');proc(lexVar,this.lexicalVarPrefix);}},getVars:function getVars(){return[this.getFieldValue('VAR')];},blocksInScope:function blocksInScope(){var doBlock=this.getInputTargetBlock('DO');if(doBlock){return[doBlock];}else{return[];}},declaredNames:function declaredNames(){return[this.getFieldValue('VAR')];},renameVar:function renameVar(oldName,newName){if(core_.Names.equals(oldName,this.getFieldValue('VAR'))){this.setFieldValue(newName,'VAR');}},renameBound:function renameBound(boundSubstitution,freeSubstitution){LexicalVariable.renameFree(this.getInputTargetBlock('START'),freeSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock('END'),freeSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock('STEP'),freeSubstitution);var oldIndexVar=this.getFieldValue('VAR');var newIndexVar=boundSubstitution.apply(oldIndexVar);if(newIndexVar!==oldIndexVar){this.renameVar(oldIndexVar,newIndexVar);var indexSubstitution=Substitution.simpleSubstitution(oldIndexVar,newIndexVar);var extendedFreeSubstitution=freeSubstitution.extend(indexSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock('DO'),extendedFreeSubstitution);}else{var removedFreeSubstitution=freeSubstitution.remove([oldIndexVar]);LexicalVariable.renameFree(this.getInputTargetBlock('DO'),removedFreeSubstitution);}if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();LexicalVariable.renameFree(nextBlock,freeSubstitution);}},renameFree:function renameFree(freeSubstitution){var indexVar=this.getFieldValue('VAR');var bodyFreeVars=LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));bodyFreeVars.deleteName(indexVar);var renamedBodyFreeVars=bodyFreeVars.renamed(freeSubstitution);if(renamedBodyFreeVars.isMember(indexVar)){// Variable capture!
var newIndexVar=FieldLexicalVariable.nameNotIn(indexVar,renamedBodyFreeVars.toList());var boundSubstitution=Substitution.simpleSubstitution(indexVar,newIndexVar);this.renameBound(boundSubstitution,freeSubstitution);}else{this.renameBound(new Substitution(),freeSubstitution);}},freeVariables:function freeVariables(){// return the free variables of this block
var result=LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));// Remove bound index variable from body free vars
result.deleteName(this.getFieldValue('VAR'));result.unite(LexicalVariable.freeVariables(this.getInputTargetBlock('START')));result.unite(LexicalVariable.freeVariables(this.getInputTargetBlock('END')));result.unite(LexicalVariable.freeVariables(this.getInputTargetBlock('STEP')));if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();result.unite(LexicalVariable.freeVariables(nextBlock));}return result;}};// Alias controls_for to controls_forRange We need this because
// we can't use controls_flow_statements within controls_forRange
// due to Blockly checking
delete core_.Blocks.controls_for;core_.Blocks.controls_for=core_.Blocks.controls_forRange;core_.Blocks.controls_forEach={// For each loop.
category:'Control',helpUrl:core_.Msg.LANG_CONTROLS_FOREACH_HELPURL,init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.CONTROL_CATEGORY_HUE);
this.setStyle('loop_blocks');// this.setOutput(true, null);
// [lyn, 10/07/13] Changed default name from "i" to "item"
// [lyn, 11/29/12] Changed variable to be text input box that does renaming
// right (i.e., avoids variable capture)
this.appendValueInput('LIST').setCheck(yailTypeToBlocklyType('list',INPUT)).appendField(core_.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM).appendField(new FieldParameterFlydown(core_.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR,true,FieldFlydown.DISPLAY_BELOW),'VAR').appendField(core_.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST).setAlign(core_.ALIGN_RIGHT);this.appendStatementInput('DO').appendField(core_.Msg.LANG_CONTROLS_FOREACH_INPUT_DO);this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_CONTROLS_FOREACH_TOOLTIP);this.lexicalVarPrefix=loopParameterPrefix;},referenceResults:function referenceResults(name,prefix,env){var loopVar=this.getFieldValue('VAR');// Invariant: Shared.showPrefixToUser must also be true!
if(usePrefixInCode){loopVar=possiblyPrefixMenuNameWith(loopParameterPrefix)(loopVar);}var newEnv=env.concat([loopVar]);var listResults=LexicalVariable.referenceResult(this.getInputTargetBlock('LIST'),name,prefix,env);var doResults=LexicalVariable.referenceResult(this.getInputTargetBlock('DO'),name,prefix,newEnv);var nextResults=LexicalVariable.referenceResult(LexicalVariable.getNextTargetBlock(this),name,prefix,env);return[listResults,doResults,nextResults];},withLexicalVarsAndPrefix:core_.Blocks.controls_forRange.withLexicalVarsAndPrefix,getVars:function getVars(){return[this.getFieldValue('VAR')];},blocksInScope:function blocksInScope(){var doBlock=this.getInputTargetBlock('DO');if(doBlock){return[doBlock];}else{return[];}},declaredNames:function declaredNames(){return[this.getFieldValue('VAR')];},renameVar:function renameVar(oldName,newName){if(core_.Names.equals(oldName,this.getFieldValue('VAR'))){this.setFieldValue(newName,'VAR');}},renameBound:function renameBound(boundSubstitution,freeSubstitution){LexicalVariable.renameFree(this.getInputTargetBlock('LIST'),freeSubstitution);var oldIndexVar=this.getFieldValue('VAR');var newIndexVar=boundSubstitution.apply(oldIndexVar);if(newIndexVar!==oldIndexVar){this.renameVar(oldIndexVar,newIndexVar);var indexSubstitution=Substitution.simpleSubstitution(oldIndexVar,newIndexVar);var extendedFreeSubstitution=freeSubstitution.extend(indexSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock('DO'),extendedFreeSubstitution);}else{var removedFreeSubstitution=freeSubstitution.remove([oldIndexVar]);LexicalVariable.renameFree(this.getInputTargetBlock('DO'),removedFreeSubstitution);}if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();LexicalVariable.renameFree(nextBlock,freeSubstitution);}},renameFree:function renameFree(freeSubstitution){var indexVar=this.getFieldValue('VAR');var bodyFreeVars=LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));bodyFreeVars.deleteName(indexVar);var renamedBodyFreeVars=bodyFreeVars.renamed(freeSubstitution);if(renamedBodyFreeVars.isMember(indexVar)){// Variable capture!
var newIndexVar=FieldLexicalVariable.nameNotIn(indexVar,renamedBodyFreeVars.toList());var boundSubstitution=Substitution.simpleSubstitution(indexVar,newIndexVar);this.renameBound(boundSubstitution,freeSubstitution);}else{this.renameBound(new Substitution(),freeSubstitution);}},freeVariables:function freeVariables(){// return the free variables of this block
var result=LexicalVariable.freeVariables(this.getInputTargetBlock('DO'));// Remove bound index variable from body free vars
result.deleteName(this.getFieldValue('VAR'));result.unite(LexicalVariable.freeVariables(this.getInputTargetBlock('LIST')));if(this.nextConnection){var nextBlock=this.nextConnection.targetBlock();result.unite(LexicalVariable.freeVariables(nextBlock));}return result;}};core_.Blocks.controls_do_then_return={// String length.
category:'Control',helpUrl:core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL,init:function init(){// this.setColour(Blockly.CONTROL_CATEGORY_HUE);
this.setStyle('loop_blocks');this.appendStatementInput('STM').appendField(core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO);this.appendValueInput('VALUE').appendField(core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN).setAlign(core_.ALIGN_RIGHT);this.setOutput(true,null);this.setTooltip(core_.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP);}};
;// CONCATENATED MODULE: ./src/blocks/procedures.js
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Procedure blocks for Blockly, modified for MIT App Inventor.
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */ /**
 * Lyn's Change History:
 * [lyn, written 11/16-17/13, added 07/01/14]
 *   + Added freeVariables, renameFree, and renameBound to procedure
 * declarations
 *   + Added renameVars for procedure declarations, which allows renaming
 * multiple parameters simultaneously
 *   + Modified updateParams_ to accept optional params argument
 *   + Introduced bodyInputName field for procedure declarations ('STACK' for
 * procedures_defnoreturn;
 *     'RETURN' for procedures_return), and use this to share more methods
 * between the two kinds of procedure declarations.
 *   + Replaced inlined string list equality tests by new
 * Blockly.LexicalVariable.stringListsEqual
 * [lyn, 10/28/13]
 *   + Fixed a missing change of Blockly.Procedures.rename by zdure
 *   + I was wrong about re-rendering not being needed in updatedParams_!
 *     Without it, changing horizontal -> vertical params doesn't handle body
 * slot correctly. So added it back.
 * [lyn, 10/27/13]
 *   + Fix bug in list of callers in flyout by simplifying domToMutation for
 * procedure callers. This should never look for associated declaration, but
 * just take arguments from given xml.
 *   + Removed render() call from updateParams_. Seems unnecessary. <== I WAS
 * WRONG. SEE 10/28/13 NOTE
 *   + Specify direction of flydowns
 *   + Replaced Blockly.Procedures.rename by
 * Blockly.AIProcedure.renameProcedure in proc decls
 * [lyn, 10/26/13] Modify procedure parameter changeHandler to propagate name
 * changes to caller arg labels and open mutator labels
 * [lyn, 10/25/13]
 *   + Modified procedures_defnoreturn compose method so that it doesn't call
 * updateParams_ if mutator hasn't changed parameter names. This helps avoid a
 * situation where an attempt is made to update params of a collapsed
 * declaration.
 *   + Modified collapsed decls to have 'to ' prefix and collapsed callers to
 * have 'call ' prefix.
 * [lyn, 10/24/13] Allowed switching between horizontal and vertical display of
 * arguments
 * [lyn, 10/23/13] Fixed bug in domToMutation for callers that was giving wrong
 * args to caller blocks.
 * [lyn, 10/10-14/13]
 *   + Installed variable declaration flydowns in both types of procedure
 * declarations.
 *   + Fixed bug: Modified onchange for procedure declarations to keep
 * arguments_ instance variable updated when param is edited directly on
 * declaration block.
 *   + Removed get block (still in Variable drawer; no longer needed with
 * parameter flydowns)
 *   + Removed "do {} then-return []" block since (1) it's in Control drawer
 * and
 *     (2) it will be superseded in the context by Michael Phox's
 * proc_defnoreturn mutator that allows adding a DO statement.
 *   + TODO: Abstract over string labels on all blocks using constants defined
 * in en/_messages.js
 *   + TODO: Clean up code, including refactoring to increase sharing between
 *     procedures_defnoreturn and procedures_defreturn.
 * [lyn, 11/29/12] Integrated into App Inventor blocks. Known bugs:
 *   + Reordering mutator_args in mutator_container changes references to ???
 * Because it interprets it as removing and inserting rather than moving.
 * [lyn, 11/24/12] Implemented procedure parameter renaming:
 *   + changing a variable name in mutator_arg for procedure changes it
 * immediately in references in body.
 *   + no duplicate names are allowed in mutator_args; alpha-renaming prevents
 * this.
 *   + no variables can be captured by renaming; alpha-renaming prevents this.
 */core_.Blocks.procedures_defnoreturn={// Define a procedure with no return value.
category:'Procedures',// Procedures are handled specially.
helpUrl:core_.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL,bodyInputName:'STACK',init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');var legalName=core_.Procedures.findLegalName(core_.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE,this);this.createHeader(legalName);this.horizontalParameters=true;// horizontal by default
this.appendStatementInput('STACK').appendField(core_.Msg.LANG_PROCEDURES_DEFNORETURN_DO);this.setMutator(new core_.Mutator(['procedures_mutatorarg']));this.setTooltip(core_.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP);// List of declared local variable names; has one
// ("name") initially
this.arguments_=[];// Other methods guarantee the invariant that this variable contains
// the list of names declared in the local declaration block.
this.warnings=[{name:'checkEmptySockets',sockets:['STACK']}];this.lexicalVarPrefix=procedureParameterPrefix;},createHeader:function createHeader(procName){return this.appendDummyInput('HEADER').appendField(core_.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE).appendField(new FieldProcedureName(procName),'NAME');},withLexicalVarsAndPrefix:function withLexicalVarsAndPrefix(_,proc){var params=this.declaredNames();// not arguments_ instance var
for(var i=0;i<params.length;i++){proc(params[i],this.lexicalVarPrefix);}},onchange:function onchange(){// ensure arguments_ is in sync
// with paramFlydown fields
this.arguments_=this.declaredNames();},updateParams_:function updateParams_(opt_params){// make rendered block reflect the parameter names currently in
// this.arguments_
// [lyn, 11/17/13] Added optional opt_params argument:
//    If its falsey (null or undefined), use the existing this.arguments_
// list Otherwise, replace this.arguments_ by opt_params In either case,
// make rendered block reflect the parameter names in this.arguments_
if(opt_params){this.arguments_=opt_params;}// Check for duplicated arguments.
// [lyn 10/10/13] Note that in blocks edited within AI2, duplicate
// parameter names should never occur because parameters are renamed to
// avoid duplication. But duplicates might show up in XML code hand-edited
// by user. console.log("enter procedures_defnoreturn updateParams_()");
var badArg=false;var hash={};for(var x=0;x<this.arguments_.length;x++){if(hash['arg_'+this.arguments_[x].toLowerCase()]){badArg=true;break;}hash['arg_'+this.arguments_[x].toLowerCase()]=true;}if(badArg){this.setWarningText(core_.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING);}else{this.setWarningText(null);}var procName=this.getFieldValue('NAME');// save the first two input lines and the last input line
// to be re added to the block later
// var firstInput = this.inputList[0];
// [lyn, 10/24/13] need to reconstruct first input
// Body of procedure
var bodyInput=this.inputList[this.inputList.length-1];// stop rendering until block is recreated
var savedRendered=this.rendered;this.rendered=false;// remove first input
// console.log("updateParams_: remove input HEADER");
var thisBlock=this;// Grab correct object for use in thunk below
FieldParameterFlydown.withChangeHanderDisabled(// [lyn, 07/02/14] Need to disable change handler, else this will try
// to rename params for horizontal arg fields!
function(){thisBlock.removeInput('HEADER');});// Remove all existing vertical inputs (we will create new ones if
// necessary)
// Only args and body are left
var oldArgCount=this.inputList.length-1;if(oldArgCount>0){var paramInput0=this.getInput('VAR0');if(paramInput0){var _loop=function _loop(i){try{FieldParameterFlydown.withChangeHanderDisabled(// [lyn, 07/02/14] Need to disable change handler, else this
// will try to rename params for vertical arg fields!
function(){thisBlock.removeInput('VAR'+i);});}catch(err){console.log(err);}};// Yes, they were vertical
for(var i=0;i<oldArgCount;i++){_loop(i);}}}// empty the inputList then recreate it
this.inputList=[];// console.log("updateParams_: create input HEADER");
var headerInput=this.createHeader(procName);// const headerInput =
//     this.appendDummyInput('HEADER')
//         .appendField(Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE)
//         .appendField(new FieldProcedureName(procName), 'NAME');
// add an input title for each argument
// name each input after the block and where it appears in the block to
// reference it later
if(this.horizontalParameters){// horizontal case
for(var _i=0;_i<this.arguments_.length;_i++){// [lyn, 10/10/13] Changed to param flydown
// Tag with param tag to make it easy to find later.
headerInput.appendField(' ').appendField(this.parameterFlydown(_i),'VAR'+_i);}}else{// vertical case
for(var _i2=0;_i2<this.arguments_.length;_i2++){this.appendDummyInput('VAR'+_i2).appendField(this.parameterFlydown(_i2),'VAR'+_i2).setAlign(core_.ALIGN_RIGHT);}}// put the last two arguments back
this.inputList=this.inputList.concat(bodyInput);this.rendered=savedRendered;// [lyn, 10/28/13] I thought this rerendering was unnecessary. But I was
// wrong! Without it, get bug noticed by Andrew in which toggling
// horizontal -> vertical params in procedure decl doesn't handle body tag
// appropriately!
for(var _i3=0;_i3<this.inputList.length;_i3++){this.inputList[_i3].init();}if(this.rendered){this.render();}// set in BlocklyPanel.java on successful load
if(this.workspace.loadCompleted){core_.Procedures.mutateCallers(this);}// console.log("exit procedures_defnoreturn updateParams_()");
},// [lyn, 10/26/13] Introduced this to correctly handle renaming of [(1)
// caller arg labels and (2) mutatorarg in open mutator] when procedure
// parameter flydown name is edited.
// Return a new procedure parameter flydown
parameterFlydown:function parameterFlydown(paramIndex){var initialParamName=this.arguments_[paramIndex];// Here, "this" is the proc decl block. Name it to
// use in function below
var procDecl=this;var procedureParameterChangeHandler=function procedureParameterChangeHandler(newParamName){// console.log("enter procedureParameterChangeHandler");
// Extra work that needs to be done when procedure param name is changed,
// in addition to renaming lexical variables: 1. Change all callers so
// label reflects new name 2. If there's an open mutator, change the
// corresponding slot. Note: this handler is invoked as method on field,
// so within the handler body, "this" will be bound to that field and
// *not* the procedure declaration object!
// Subtlety #1: within this changeHandler, procDecl.arguments_ has *not*
// yet been updated to include newParamName. This only happens later. But
// since we know newParamName *and* paramIndex, we know how to update
// procDecl.arguments_ ourselves!
// Subtlety #2: I would have thought we would want to create local copy
// of
// procedure arguments_ list rather than mutate that list, but I'd be
// wrong! Turns out that *not* mutating list here causes trouble below in
// the line
// Blockly.Field.prototype.setText.call(mutatorarg.getTitle_("NAME"),
// newParamName);  The reason is that this fires a change event in
// mutator workspace, which causes a call to the proc decl compose()
// method, and when it detects a difference in the arguments it calls
// proc decl updateParams_. This removes proc decl inputs before adding
// them back, and all hell breaks loose when the procedure name field and
// previous parameter flydown fields are disposed before an attempt is
// made to disposed this field. At this point, the SVG element associated
// with the procedure name is gone but the field is still in the title
// list. Attempting to dispose this field attempts to hide the open HTML
// editor widget, which attempts to re-render the procedure declaration
// block. But the null SVG for the procedure name field raises an
// exception.  It turns out that by mutating proc decl arguments_, when
// compose() is called, updateParams_() is *not* called, and this
// prevents the above scenario. So rather than doing  var newArguments =
// [].concat(procDecl.arguments_)  we instead do:
var newArguments=procDecl.arguments_;newArguments[paramIndex]=newParamName;// 1. Change all callers so label reflects new name
core_.Procedures.mutateCallers(procDecl);// 2. If there's an open mutator, change the name in the corresponding
// slot.
if(procDecl.mutator&&procDecl.mutator.rootBlock_){// Iterate through mutatorarg param blocks and change name of one at
// paramIndex
var mutatorContainer=procDecl.mutator.rootBlock_;var mutatorargIndex=0;var mutatorarg=mutatorContainer.getInputTargetBlock('STACK');while(mutatorarg&&mutatorargIndex<paramIndex){mutatorarg=mutatorarg.nextConnection&&mutatorarg.nextConnection.targetBlock();mutatorargIndex++;}if(mutatorarg&&mutatorargIndex==paramIndex){// Subtlety #3: If call mutatorargs's setValue, its change handler
// will be invoked several times, and on one of those times, it will
// find new param name in the procedures arguments_ instance variable
// and will try to renumber it (e.g. "a" -> "a2"). To avoid this,
// invoke the setText method of its Field s superclass directly.
// I.e., can't do this:
// mutatorarg.getTitle_("NAME").setValue(newParamName); so instead do
// this:
mutatorarg.getField('NAME').setValue(newParamName);// mutatorarg.getField("NAME").doValueUpdate_(newParamName);
//   Blockly.Field.prototype.setText.call(mutatorarg.getField("NAME"),
// newParamName);
}}// console.log("exit procedureParameterChangeHandler");
};return new FieldParameterFlydown(initialParamName,true,// name is editable
// [lyn, 10/27/13] flydown location depends on parameter orientation
this.horizontalParameters?FieldFlydown.DISPLAY_BELOW:FieldFlydown.DISPLAY_RIGHT,procedureParameterChangeHandler);},setParameterOrientation:function setParameterOrientation(isHorizontal){var params=this.getParameters();if(params.length!=0&&isHorizontal!==this.horizontalParameters){this.horizontalParameters=isHorizontal;this.updateParams_();if(core_.Events.isEnabled()){// Trigger a Blockly UI change event
core_.Events.fire(new core_.Events.Ui(this,'parameter_orientation',(!this.horizontalParameters).toString(),this.horizontalParameters.toString()));}}},mutationToDom:function mutationToDom(){var container=core_.utils.xml.createElement('mutation');if(!this.horizontalParameters){container.setAttribute('vertical_parameters','true');// Only store an
// element for
// vertical
// The absence of this attribute means horizontal.
}for(var x=0;x<this.arguments_.length;x++){var parameter=core_.utils.xml.createElement('arg');parameter.setAttribute('name',this.arguments_[x]);container.appendChild(parameter);}return container;},domToMutation:function domToMutation(xmlElement){var params=[];var children=getChildren(xmlElement);for(var x=0,childNode;childNode=children[x];x++){if(childNode.nodeName.toLowerCase()=='arg'){params.push(childNode.getAttribute('name'));}}this.horizontalParameters=xmlElement.getAttribute('vertical_parameters')!=='true';this.updateParams_(params);},decompose:function decompose(workspace){var containerBlock=workspace.newBlock('procedures_mutatorcontainer');containerBlock.initSvg();// [lyn, 11/24/12] Remember the associated procedure, so can
// appropriately change body when update name in param block.
containerBlock.setProcBlock(this);this.paramIds_=[];// [lyn, 10/26/13] Added
var connection=containerBlock.getInput('STACK').connection;for(var x=0;x<this.arguments_.length;x++){var paramBlock=workspace.newBlock('procedures_mutatorarg');this.paramIds_.push(paramBlock.id);// [lyn, 10/26/13] Added
paramBlock.initSvg();paramBlock.setFieldValue(this.arguments_[x],'NAME');// Store the old location.
paramBlock.oldLocation=x;connection.connect(paramBlock.previousConnection);connection=paramBlock.nextConnection;}// [lyn, 10/26/13] Rather than passing null for paramIds, pass actual
// paramIds and use true flag to initialize tracking.
core_.Procedures.mutateCallers(this);return containerBlock;},compose:function compose(containerBlock){var params=[];this.paramIds_=[];var paramBlock=containerBlock.getInputTargetBlock('STACK');while(paramBlock){params.push(paramBlock.getFieldValue('NAME'));this.paramIds_.push(paramBlock.id);paramBlock=paramBlock.nextConnection&&paramBlock.nextConnection.targetBlock();}// console.log("enter procedures_defnoreturn compose(); prevArguments = "
//    + prevArguments.join(',')
//    + "; currentAguments = "
//    + this.arguments_.join(',')
//    + ";"
// );
// [lyn, 11/24/12] Note: update params updates param list in proc
// declaration, but renameParam updates procedure body appropriately.
if(!LexicalVariable.stringListsEqual(params,this.arguments_)){// Only need updates if param list has changed
this.updateParams_(params);core_.Procedures.mutateCallers(this);}// console.log("exit procedures_defnoreturn compose()");
},dispose:function dispose(){var name=this.getFieldValue('NAME');var editable=this.editable_;var workspace=this.workspace;// This needs to happen first so that the Blockly events will be replayed
// in the correct order on undo
if(editable){// Dispose of any callers.
// Blockly.Procedures.disposeCallers(name, workspace);
removeProcedureValues(name,workspace);}// Call parent's destructor.
for(var _len=arguments.length,args=new Array(_len),_key=0;_key<_len;_key++){args[_key]=arguments[_key];}core_.BlockSvg.prototype.dispose.apply(this,args);var procDb=workspace.getProcedureDatabase();if(editable&&procDb){// only true for the top-level workspaces, not flyouts/flydowns
procDb.removeProcedure(this.id);}},getProcedureDef:function getProcedureDef(){// Return the name of the defined procedure,
// a list of all its arguments,
// and that it DOES NOT have a return value.
return[this.getFieldValue('NAME'),this.arguments_,this.bodyInputName==='RETURN'];// true for procedures that return values.
},getVars:function getVars(){var names=[];for(var i=0,param;param=this.getFieldValue('VAR'+i);i++){names.push(param);}return names;},declaredNames:function declaredNames(){// [lyn, 10/11/13] return the names of all parameters of this procedure
return this.getVars();},declaredVariables:function declaredVariables(){return this.getVars();},renameVar:function renameVar(oldName,newName){this.renameVars(Substitution.simpleSubstitution(oldName,newName));},renameVars:function renameVars(substitution){// renaming is a dict (i.e., object) mapping old names to new ones
var oldParams=this.getParameters();var newParams=substitution.map(oldParams);if(!LexicalVariable.stringListsEqual(oldParams,newParams)){this.updateParams_(newParams);// Update the mutator's variables if the mutator is open.
if(this.mutator.isVisible()){var blocks=this.mutator.workspace_.getAllBlocks();for(var x=0,block;block=blocks[x];x++){if(block.type=='procedures_mutatorarg'){var oldName=block.getFieldValue('NAME');var newName=substitution.apply(oldName);if(newName!==oldName){block.setFieldValue(newName,'NAME');}}}}}},renameBound:function renameBound(boundSubstitution,freeSubstitution){var paramSubstitution=boundSubstitution.restrictDomain(this.declaredNames());this.renameVars(paramSubstitution);var newFreeSubstitution=freeSubstitution.extend(paramSubstitution);LexicalVariable.renameFree(this.getInputTargetBlock(this.bodyInputName),newFreeSubstitution);},renameFree:function renameFree(freeSubstitution){// Should have no effect since only top-level procedures.
// Calculate free variables, which
// should be empty,
// throwing exception if not.
// There should be no free variables, and so nothing to rename. Do nothing
// else.
this.freeVariables();},freeVariables:function freeVariables(){// return the free lexical variables of this block
// Should return the empty set: something is wrong if it doesn't!
var result=LexicalVariable.freeVariables(this.getInputTargetBlock(this.bodyInputName));result.subtract(new NameSet(this.declaredNames()));if(result.isEmpty()){return result;}else{throw Error('Violation of invariant: procedure declaration has nonempty free'+' variables: '+result.toString());}},// [lyn, 11/24/12] return list of procedure body (if there is one)
blocksInScope:function blocksInScope(){var body=this.getInputTargetBlock(this.bodyInputName);return body&&[body]||[];},customContextMenu:function customContextMenu(options){FieldParameterFlydown.addHorizontalVerticalOption(this,options);// Blockly.BlocklyEditor.addPngExportOption(this, options);
},getParameters:function getParameters(){return this.arguments_;}};// [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with
// DO-THEN-RETURN)
core_.Blocks.procedures_defreturn={// Define a procedure with a return value.
category:'Procedures',// Procedures are handled specially.
// helpUrl: Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL,
helpUrl:core_.Msg.PROCEDURES_DEFRETURN_HELPURL,bodyInputName:'RETURN',init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');// const name = Blockly.Procedures.findLegalName(
//     Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE, this);
// this.appendDummyInput('HEADER')
//     .appendField(Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE)
//     .appendField(new FieldProcedureName(name), 'NAME');
var legalName=core_.Procedures.findLegalName(core_.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE,this);this.createHeader(legalName);this.horizontalParameters=true;// horizontal by default
// this.appendIndentedValueInput('RETURN')
//     .appendField(Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN);
this.appendValueInput('RETURN').setAlign(core_.ALIGN_RIGHT).appendField(core_.Msg.LANG_PROCEDURES_DEFRETURN_RETURN);this.setMutator(new core_.Mutator(['procedures_mutatorarg']));this.setTooltip(core_.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP);this.arguments_=[];this.warnings=[{name:'checkEmptySockets',sockets:['RETURN']}];},createHeader:function createHeader(procName){return this.appendDummyInput('HEADER').appendField(core_.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE).appendField(new FieldProcedureName(procName),'NAME');},withLexicalVarsAndPrefix:core_.Blocks.procedures_defnoreturn.withLexicalVarsAndPrefix,onchange:core_.Blocks.procedures_defnoreturn.onchange,// [lyn, 11/24/12] return list of procedure body (if there is one)
updateParams_:core_.Blocks.procedures_defnoreturn.updateParams_,parameterFlydown:core_.Blocks.procedures_defnoreturn.parameterFlydown,setParameterOrientation:core_.Blocks.procedures_defnoreturn.setParameterOrientation,mutationToDom:core_.Blocks.procedures_defnoreturn.mutationToDom,domToMutation:core_.Blocks.procedures_defnoreturn.domToMutation,decompose:core_.Blocks.procedures_defnoreturn.decompose,compose:core_.Blocks.procedures_defnoreturn.compose,dispose:core_.Blocks.procedures_defnoreturn.dispose,getProcedureDef:core_.Blocks.procedures_defnoreturn.getProcedureDef,getVars:core_.Blocks.procedures_defnoreturn.getVars,declaredNames:core_.Blocks.procedures_defnoreturn.declaredNames,declaredVariables:core_.Blocks.procedures_defnoreturn.declaredVariables,renameVar:core_.Blocks.procedures_defnoreturn.renameVar,renameVars:core_.Blocks.procedures_defnoreturn.renameVars,renameBound:core_.Blocks.procedures_defnoreturn.renameBound,renameFree:core_.Blocks.procedures_defnoreturn.renameFree,freeVariables:core_.Blocks.procedures_defnoreturn.freeVariables,blocksInScope:core_.Blocks.procedures_defnoreturn.blocksInScope,customContextMenu:core_.Blocks.procedures_defnoreturn.customContextMenu,getParameters:core_.Blocks.procedures_defnoreturn.getParameters};core_.Blocks.procedures_mutatorcontainer={// Procedure container (for mutator dialog).
init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');this.appendDummyInput().appendField(core_.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE);this.appendStatementInput('STACK');this.setTooltip(core_.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP);this.contextMenu=false;this.mustNotRenameCapturables=true;},// [lyn. 11/24/12] Set procBlock associated with this container.
setProcBlock:function setProcBlock(procBlock){this.procBlock_=procBlock;},// [lyn. 11/24/12] Set procBlock associated with this container.
// Invariant: should not be null, since only created as mutator for a
// particular proc block.
getProcBlock:function getProcBlock(){return this.procBlock_;},// [lyn. 11/24/12] Return list of param names in this container
// Invariant: there should be no duplicates!
declaredNames:function declaredNames(){var paramNames=[];var paramBlock=this.getInputTargetBlock('STACK');while(paramBlock){paramNames.push(paramBlock.getFieldValue('NAME'));paramBlock=paramBlock.nextConnection&&paramBlock.nextConnection.targetBlock();}return paramNames;}};core_.Blocks.procedures_mutatorarg={// Procedure argument (for mutator dialog).
init:function init(){//    var mutatorarg = this;
//    var mutatorargChangeHandler = function(newName) {
//      var proc = mutatorarg.getProcBlock();
//      var procArguments = proc ? proc.arguments_ : [];
//      console.log("mutatorargChangeHandler: newName = " + newName
//                  + " and proc argumnets = [" + procArguments.join(',') +
// "]"); return Blockly.LexicalVariable.renameParam.call(this,newName); }
// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');var editor=new core_.FieldTextInput('x',LexicalVariable.renameParam);// 2017 Blockly's text input change breaks our renaming behavior.
// The following is a version we've defined.
editor.onHtmlInputChange_=function(e){var oldValue=this.getValue();FieldFlydown.prototype.onHtmlInputChange_.call(this,e);var newValue=this.getValue();if(newValue&&oldValue!==newValue&&core_.Events.isEnabled()){core_.Events.fire(new core_.Events.BlockChange(this.sourceBlock_,'field',this.name,oldValue,newValue));}};this.appendDummyInput().appendField(core_.Msg.LANG_PROCEDURES_MUTATORARG_TITLE).appendField(editor,'NAME');this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP);this.contextMenu=false;this.lexicalVarPrefix=procedureParameterPrefix;this.mustNotRenameCapturables=true;},// [lyn, 11/24/12] Return the container this mutator arg is in, or null if
// it's not in one. Dynamically calculate this by walking up chain, because
// mutator arg might or might not be in container stack.
getContainerBlock:function getContainerBlock(){var parent=this.getParent();while(parent&&!(parent.type==='procedures_mutatorcontainer')){parent=parent.getParent();}// [lyn, 11/24/12] Cache most recent container block so can reference it
// upon removal from mutator arg stack
this.cachedContainerBlock_=parent&&parent.type==='procedures_mutatorcontainer'&&parent||null;return this.cachedContainerBlock_;},// [lyn, 11/24/12] Return the procedure associated with mutator arg is in, or
// null if there isn't one. Dynamically calculate this by walking up chain,
// because mutator arg might or might not be in container stack.
getProcBlock:function getProcBlock(){var container=this.getContainerBlock();return container&&container.getProcBlock()||null;},// [lyn, 11/24/12] Return the declared names in the procedure associated with
// mutator arg, or the empty list if there isn't one. Dynamically calculate
// this by walking up chain, because mutator arg might or might not be in
// container stack.
declaredNames:function declaredNames(){var container=this.getContainerBlock();return container&&container.declaredNames()||[];},// [lyn, 11/24/12] Return the blocks in scope of proc params in the the
// procedure associated with mutator arg, or the empty list if there isn't
// one. Dynamically calculate this by walking up chain, because mutator arg
// might or might not be in container stack.
blocksInScope:function blocksInScope(){var proc=this.getProcBlock();return proc&&proc.blocksInScope()||[];},// [lyn, 11/24/12] Check for situation in which mutator arg has been removed
// from stack, and change all references to its name to ???.
onchange:function onchange(){var paramName=this.getFieldValue('NAME');if(paramName){// paramName is null when delete from stack
// console.log("Mutatorarg onchange: " + paramName);
var cachedContainer=this.cachedContainerBlock_;var container=this.getContainerBlock();// Order is important; this
// must come after
// cachedContainer
// since it sets cachedContainerBlock_
// console.log("Mutatorarg onchange: " + paramName
//            + "; cachedContainer = " + JSON.stringify((cachedContainer
// && cachedContainer.type) || null) + "; container = " +
// JSON.stringify((container && container.type) || null));
if(!cachedContainer&&container){// Event: added mutator arg to container stack
// console.log("Mutatorarg onchange ADDED: " + paramName);
var declaredNames=this.declaredNames();var firstIndex=declaredNames.indexOf(paramName);if(firstIndex!=-1){// Assertion: we should get here, since paramName should be among
// names
var secondIndex=declaredNames.indexOf(paramName,firstIndex+1);if(secondIndex!=-1){// If we get here, there is a duplicate on insertion that must be
// resolved
var newName=FieldLexicalVariable.nameNotIn(paramName,declaredNames);this.setFieldValue(newName,'NAME');}}}}}};core_.Blocks.procedures_mutatorarg.validator=function(newVar){// Merge runs of whitespace.  Strip leading and trailing whitespace.
// Beyond this, all names are legal.
newVar=newVar.replace(/[\s\xa0]+/g,' ').replace(/^ | $/g,'');return newVar||null;};core_.Blocks.procedures_callnoreturn={// Call a procedure with no return value.
category:'Procedures',// Procedures are handled specially.
helpUrl:core_.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL,init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');var procDb=this.workspace.getTopWorkspace().getProcedureDatabase();this.procNamesFxn=function(){var items=procDb.getMenuItems(false);return items.length>0?items:['',''];};this.procDropDown=new FieldNoCheckDropdown(this.procNamesFxn,onChange);this.procDropDown.block=this;this.appendDummyInput().appendField(core_.Msg.LANG_PROCEDURES_CALLNORETURN_CALL).appendField(this.procDropDown,'PROCNAME');this.setPreviousStatement(true);this.setNextStatement(true);this.setTooltip(core_.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP);this.arguments_=[];this.quarkConnections_=null;this.quarkArguments_=null;this.errors=[{func:checkIsInDefinition},{func:checkDropDownContainsValidValue,dropDowns:['PROCNAME']}];this.setOnChange(function(changeEvent){checkErrors(this);});// Blockly.FieldProcedure.onChange.call(this.getField("PROCNAME"),
//     this.procNamesFxn(false)[0][0]);
onChange.call(this.getField('PROCNAME'),this.getField('PROCNAME').getValue());},getProcedureCall:function getProcedureCall(){return this.getFieldValue('PROCNAME');},renameProcedure:function renameProcedure(oldName,newName){if(!oldName||core_.Names.equals(oldName,this.getFieldValue('PROCNAME'))){var nameField=this.getField('PROCNAME');// Force the options menu to get regenerated since we might be getting
// called because our defining procedure got renamed and
// this.setFieldValue() will fail if it's value isn't in the options set
nameField.getOptions();this.setFieldValue(newName,'PROCNAME');}},// [lyn, 10/27/13] Renamed "fromChange" parameter to "startTracking", because
// it should be true in any situation where we want caller to start tracking
// connections associated with paramIds. This includes when a mutator is
// opened on a procedure declaration.
setProcedureParameters:function setProcedureParameters(paramNames,paramIds,startTracking){// Data structures for parameters on each call block:
// this.arguments = ['x', 'y']
//     Existing param names.
// paramNames = ['x', 'y', 'z']
//     New param names.
// paramIds = ['piua', 'f8b_', 'oi.o']
//     IDs of params (consistent for each parameter through the life of a
//     mutator, regardless of param renaming).
// this.quarkConnections_ {piua: null, f8b_: Blockly.Connection}
//     Look-up of paramIds to connections plugged into the call block.
// this.quarkArguments_ = ['piua', 'f8b_']
//     Existing param IDs.
// Note that quarkConnections_ may include IDs that no longer exist, but
// which might reappear if a param is reattached in the mutator.
var input;var connection;var x;// fixed parameter alignment see ticket 465
if(!paramIds){// Reset the quarks (a mutator is about to open).
this.quarkConnections_={};this.quarkArguments_=null;// return;  // [lyn, 10/27/13] No, don't return yet. We still want to add
// paramNames to block! For now, create dummy list of param ids. This
// needs to be cleaned up further!
paramIds=[].concat(paramNames);// create a dummy list that's a copy of
// paramNames.
}if(paramIds.length!=paramNames.length){throw Error('Error: paramNames and paramIds must be the same length.');}var paramIdToParamName={};for(var i=0;i<paramNames.length;i++){paramIdToParamName[paramIds[i]]=paramNames[i];}if(typeof startTracking=='undefined'){startTracking=null;}if(!this.quarkArguments_||startTracking){// Initialize tracking for this block.
this.quarkConnections_={};if(LexicalVariable.stringListsEqual(paramNames,this.arguments_)||startTracking){// No change to the parameters, allow quarkConnections_ to be
// populated with the existing connections.
this.quarkArguments_=paramIds;}else{this.quarkArguments_=[];}}// Switch off rendering while the block is rebuilt.
var savedRendered=this.rendered;this.rendered=false;// Update the quarkConnections_ with existing connections.
for(x=0;this.getInput('ARG'+x);x++){input=this.getInput('ARG'+x);if(input){connection=input.connection.targetConnection;this.quarkConnections_[this.quarkArguments_[x]]=connection;// Disconnect all argument blocks and remove all inputs.
this.removeInput('ARG'+x);}}// Rebuild the block's arguments.
this.arguments_=[].concat(paramNames);this.quarkArguments_=paramIds;for(x=0;x<this.arguments_.length;x++){input=this.appendValueInput('ARG'+x).setAlign(core_.ALIGN_RIGHT).appendField(this.arguments_[x]);if(this.quarkArguments_){// Reconnect any child blocks.
var quarkName=this.quarkArguments_[x];if(quarkName in this.quarkConnections_){connection=this.quarkConnections_[quarkName];if(!connection||connection.targetConnection||connection.sourceBlock_.workspace!=this.workspace){// Block no longer exists or has been attached elsewhere.
delete this.quarkConnections_[quarkName];}else{input.connection.connect(connection);}}else if(paramIdToParamName[quarkName]){connection=this.quarkConnections_[paramIdToParamName[quarkName]];if(connection){input.connection.connect(connection);}}}}// Restore rendering and show the changes.
this.rendered=savedRendered;if(!this.workspace.rendered){// workspace hasn't been rendered yet, so other connections may
// not yet exist.
return;}// Initialize the new inputs.
for(x=0;x<this.arguments_.length;x++){this.getInput('ARG'+x).init();}if(this.rendered){this.render();}},mutationToDom:function mutationToDom(){// Save the name and arguments (none of which are editable).
var container=core_.utils.xml.createElement('mutation');container.setAttribute('name',this.getFieldValue('PROCNAME'));for(var x=0;this.getInput('ARG'+x);x++){var parameter=core_.utils.xml.createElement('arg');parameter.setAttribute('name',this.getInput('ARG'+x).fieldRow[0].getText());container.appendChild(parameter);}return container;},domToMutation:function domToMutation(xmlElement){// Restore the name and parameters.
var name=xmlElement.getAttribute('name');this.setFieldValue(name,'PROCNAME');// [lyn, 10/27/13] Significantly cleaned up this code. Always take arg
// names from xmlElement. Do not attempt to find definition.
this.arguments_=[];var children=getChildren(xmlElement);for(var x=0,childNode;childNode=children[x];x++){if(childNode.nodeName.toLowerCase()=='arg'){this.arguments_.push(childNode.getAttribute('name'));}}this.setProcedureParameters(this.arguments_,null,true);// [lyn, 10/27/13] Above. set tracking to true in case this is a block with
// argument subblocks. and there's an open mutator.
},renameVar:function renameVar(oldName,newName){for(var x=0;x<this.arguments_.length;x++){if(core_.Names.equals(oldName,this.arguments_[x])){this.arguments_[x]=newName;this.getInput('ARG'+x).fieldRow[0].setValue(newName);}}},procCustomContextMenu:function procCustomContextMenu(options){// Add option to find caller.
var option={enabled:true};option.text=core_.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF;var name=this.getFieldValue('PROCNAME');var workspace=this.workspace;option.callback=function(){var def=core_.Procedures.getDefinition(name,workspace);if(def){def.select();workspace.centerOnBlock(def.id);workspace.getParentSvg().parentElement.focus();}};options.push(option);},removeProcedureValue:function removeProcedureValue(){// Detach inputs before resetting name so that undo/redo operations happen
// in the right order
for(var i=0;this.getInput('ARG'+i)!==null;i++){this.removeInput('ARG'+i);}this.setFieldValue('none','PROCNAME');}};core_.Blocks.procedures_callreturn={// Call a procedure with a return value.
category:'Procedures',// Procedures are handled specially.
helpUrl:core_.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL,init:function init(){// Let the theme determine the color.
// this.setColour(Blockly.PROCEDURE_CATEGORY_HUE);
this.setStyle('procedure_blocks');var procDb=this.workspace.getTopWorkspace().getProcedureDatabase();this.procNamesFxn=function(){var items=procDb.getMenuItems(true);return items.length>0?items:['',''];};this.procDropDown=new FieldNoCheckDropdown(this.procNamesFxn,onChange);this.procDropDown.block=this;this.appendDummyInput().appendField(core_.Msg.LANG_PROCEDURES_CALLRETURN_CALL).appendField(this.procDropDown,'PROCNAME');this.setOutput(true,null);this.setTooltip(core_.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP);this.arguments_=[];this.quarkConnections_=null;this.quarkArguments_=null;this.errors=[{func:checkIsInDefinition},{func:checkDropDownContainsValidValue,dropDowns:['PROCNAME']}];this.setOnChange(function(changeEvent){checkErrors(this);});// Blockly.FieldProcedure.onChange.call(this.getField("PROCNAME"),
//     this.procNamesFxn()[0][0]);
onChange.call(this.getField('PROCNAME'),this.getField('PROCNAME').getValue());},getProcedureCall:core_.Blocks.procedures_callnoreturn.getProcedureCall,renameProcedure:core_.Blocks.procedures_callnoreturn.renameProcedure,setProcedureParameters:core_.Blocks.procedures_callnoreturn.setProcedureParameters,mutationToDom:core_.Blocks.procedures_callnoreturn.mutationToDom,domToMutation:core_.Blocks.procedures_callnoreturn.domToMutation,renameVar:core_.Blocks.procedures_callnoreturn.renameVar,procCustomContextMenu:core_.Blocks.procedures_callnoreturn.procCustomContextMenu,removeProcedureValue:core_.Blocks.procedures_callnoreturn.removeProcedureValue};
// EXTERNAL MODULE: external {"root":"Blockly.JavaScript","commonjs":"blockly/javascript","commonjs2":"blockly/javascript","amd":"blockly/javascript"}
var javascript_ = __webpack_require__(403);
;// CONCATENATED MODULE: ./src/generators/controls.js
if(javascript_){// We might be loaded into an environment that doesn't have Blockly's JavaScript generator.
var javascriptGenerator=javascript_.javascriptGenerator;/**
   * This code is copied from Blockly but the 'var' keyword is replaced by 'let'
   * in the generated code.
   * @param {Blockly.Block} block The block to generate code for.
   * @return {string} The generated code.
   */javascriptGenerator['controls_for']=function(block){// For loop.
var variable0=javascriptGenerator.nameDB_.getName(block.getFieldValue('VAR'),core_.VARIABLE_CATEGORY_NAME);var argument0=javascriptGenerator.valueToCode(block,'START',javascriptGenerator.ORDER_ASSIGNMENT)||'0';var argument1=javascriptGenerator.valueToCode(block,'END',javascriptGenerator.ORDER_ASSIGNMENT)||'0';var increment=javascriptGenerator.valueToCode(block,'STEP',javascriptGenerator.ORDER_ASSIGNMENT)||'1';var branch=javascriptGenerator.statementToCode(block,'DO');branch=javascriptGenerator.addLoopTrap(branch,block);var code;if(core_.utils.string.isNumber(argument0)&&core_.utils.string.isNumber(argument1)&&core_.utils.string.isNumber(increment)){// All arguments are simple numbers.
var up=Number(argument0)<=Number(argument1);code='for (let '+variable0+' = '+argument0+'; '+variable0+(up?' <= ':' >= ')+argument1+'; '+variable0;var step=Math.abs(Number(increment));if(step==1){code+=up?'++':'--';}else{code+=(up?' += ':' -= ')+step;}code+=') {\n'+branch+'}\n';}else{code='';// Cache non-trivial values to variables to prevent repeated look-ups.
var startVar=argument0;if(!argument0.match(/^\w+$/)&&!core_.utils.string.isNumber(argument0)){startVar=javascriptGenerator.nameDB_.getDistinctName(variable0+'_start',core_.VARIABLE_CATEGORY_NAME);code+='let '+startVar+' = '+argument0+';\n';}var endVar=argument1;if(!argument1.match(/^\w+$/)&&!core_.utils.string.isNumber(argument1)){endVar=javascriptGenerator.nameDB_.getDistinctName(variable0+'_end',core_.VARIABLE_CATEGORY_NAME);code+='let '+endVar+' = '+argument1+';\n';}// Determine loop direction at start, in case one of the bounds
// changes during loop execution.
var incVar=javascriptGenerator.nameDB_.getDistinctName(variable0+'_inc',core_.VARIABLE_CATEGORY_NAME);code+='let '+incVar+' = ';if(core_.utils.string.isNumber(increment)){code+=Math.abs(increment)+';\n';}else{code+='Math.abs('+increment+');\n';}code+='if ('+startVar+' > '+endVar+') {\n';code+=javascriptGenerator.INDENT+incVar+' = -'+incVar+';\n';code+='}\n';code+='for ('+variable0+' = '+startVar+'; '+incVar+' >= 0 ? '+variable0+' <= '+endVar+' : '+variable0+' >= '+endVar+'; '+variable0+' += '+incVar+') {\n'+branch+'}\n';}return code;};// controls_forRange and controls_for are aliases.  This is to make the
// controls_statement_flow block work correctly for controls_forRange.
javascriptGenerator['controls_forRange']=javascriptGenerator['controls_for'];/**
   * This code is copied from Blockly but the 'var' keyword is replaced by 'let'
   * or 'const' (as appropriate) in the generated code.
   * @param {Blockly.Block} block The block to generate code for.
   * @return {string} The generated code.
   */javascriptGenerator['controls_forEach']=function(block){// For each loop.
var variable0=javascriptGenerator.nameDB_.getName(block.getFieldValue('VAR'),core_.VARIABLE_CATEGORY_NAME);var argument0=javascriptGenerator.valueToCode(block,'LIST',javascriptGenerator.ORDER_ASSIGNMENT)||'[]';var branch=javascriptGenerator.statementToCode(block,'DO');branch=javascriptGenerator.addLoopTrap(branch,block);var code='';// Cache non-trivial values to variables to prevent repeated look-ups.
var listVar=argument0;if(!argument0.match(/^\w+$/)){listVar=javascriptGenerator.nameDB_.getDistinctName(variable0+'_list',core_.VARIABLE_CATEGORY_NAME);code+='const '+listVar+' = '+argument0+';\n';}var indexVar=javascriptGenerator.nameDB_.getDistinctName(variable0+'_index',core_.VARIABLE_CATEGORY_NAME);branch=javascriptGenerator.INDENT+'const '+variable0+' = '+listVar+'['+indexVar+'];\n'+branch;code+='for (let '+indexVar+' in '+listVar+') {\n'+branch+'}\n';return code;};}
;// CONCATENATED MODULE: ./src/generators/procedures.js
if(javascript_){// We might be loaded into an environment that doesn't have Blockly's JavaScript generator.
var procedures_javascriptGenerator=javascript_.javascriptGenerator;/**
   * This code is copied from Blockly but the 'NAME' field is changed to
   * 'PROCNAME'.
   * @param {Blockly.Block} block The block to generate code for.
   * @return {string} The generated code.
   */procedures_javascriptGenerator['procedures_callreturn']=function(block){// Call a procedure with a return value.
var funcName=procedures_javascriptGenerator.nameDB_.getName(block.getFieldValue('PROCNAME'),core_.PROCEDURE_CATEGORY_NAME);var args=[];var variables=block.arguments_;for(var i=0;i<variables.length;i++){args[i]=procedures_javascriptGenerator.valueToCode(block,'ARG'+i,procedures_javascriptGenerator.ORDER_NONE)||'null';}var code=funcName+'('+args.join(', ')+')';return[code,procedures_javascriptGenerator.ORDER_FUNCTION_CALL];};}
;// CONCATENATED MODULE: ./src/generators/lexical-variables.js
if(javascript_){// We might be loaded into an environment that doesn't have Blockly's JavaScript generator.
var lexical_variables_javascriptGenerator=javascript_.javascriptGenerator;lexical_variables_javascriptGenerator['lexical_variable_get']=function(block){var code=getVariableName(block.getFieldValue('VAR'));return[code,lexical_variables_javascriptGenerator.ORDER_ATOMIC];};/**
   * Generate variable name
   * @param {string} name
   * @return {string}
   */function getVariableName(name){var pair=unprefixName(name);var prefix=pair[0];var unprefixedName=pair[1];if(prefix===core_.Msg.LANG_VARIABLES_GLOBAL_PREFIX||prefix===GLOBAL_KEYWORD){return unprefixedName;}else{return possiblyPrefixGeneratedVarName(prefix)(unprefixedName);}}/**
   * Generate basic variable setting code.
   * @param {Blockly.Block} block
   * @param {string} varFieldName
   * @return {string} The code.
   */function genBasicSetterCode(block,varFieldName){var argument0=lexical_variables_javascriptGenerator.valueToCode(block,'VALUE',lexical_variables_javascriptGenerator.ORDER_ASSIGNMENT)||'0';var varName=getVariableName(block.getFieldValue(varFieldName));return varName+' = '+argument0+';\n';}lexical_variables_javascriptGenerator['lexical_variable_set']=function(block){// Variable setter.
return genBasicSetterCode(block,'VAR');};lexical_variables_javascriptGenerator['global_declaration']=function(block){// Global variable declaration
return'var '+genBasicSetterCode(block,'NAME');};function generateDeclarations(block){var code='{\n  let ';for(var i=0;block.getFieldValue('VAR'+i);i++){code+=(usePrefixInCode?'local_':'')+block.getFieldValue('VAR'+i);code+=' = '+(lexical_variables_javascriptGenerator.valueToCode(block,'DECL'+i,lexical_variables_javascriptGenerator.ORDER_NONE)||'0');code+=', ';}// Get rid of the last comma
code=code.slice(0,-2);code+=';\n';return code;}lexical_variables_javascriptGenerator['local_declaration_statement']=function(){var code=generateDeclarations(this);code+=lexical_variables_javascriptGenerator.statementToCode(this,'STACK',lexical_variables_javascriptGenerator.ORDER_NONE);code+='}\n';return code;};lexical_variables_javascriptGenerator['local_declaration_expression']=function(){// TODO: This can probably be redone to use the variables as parameters to the generated function
// and then call the function with the generated variable values.
var code='(function() {\n';code+=generateDeclarations(this);code+='return '+(lexical_variables_javascriptGenerator.valueToCode(this,'RETURN',lexical_variables_javascriptGenerator.ORDER_NONE)||'null');code+='}})()\n';return[code,lexical_variables_javascriptGenerator.ORDER_NONE];};}
;// CONCATENATED MODULE: ./src/index.js
/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */// TODO: Edit block overview.
/**
 * @fileoverview Block overview.
 */registerCss();// Export Flydown and fields for use by plugin users.
// Note that we might eb exporting too much here, but let's see how it goes.
/**
 * @param workspace
 */function init(workspace){// TODO: Might need the next line
// Blockly.DropDownDiv.createDom();
var flydown=new Flydown(new core_.Options({scrollbars:false,rtl:workspace.RTL,renderer:workspace.options.renderer,rendererOverrides:workspace.options.rendererOverrides}));// ***** [lyn, 10/05/2013] NEED TO WORRY ABOUT MULTIPLE BLOCKLIES! *****
workspace.flydown_=flydown;core_.utils.dom.insertAfter(flydown.createDom('g'),workspace.svgBubbleCanvas_);flydown.init(workspace);flydown.autoClose=true;// Flydown closes after selecting a block
}// Remove before commiting
console.log("Loaded plugin");
})();

/******/ 	return __webpack_exports__;
/******/ })()
;
});
//# sourceMappingURL=index.js.map