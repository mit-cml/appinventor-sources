// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.language_switch');

goog.require('Blockly.Msg.en');
goog.require('Blockly.Msg.zh_cn');
goog.require('Blockly.Msg.zh_tw');
goog.require('Blockly.Msg.es_es');
goog.require('Blockly.Msg.it_it');
Blockly.language_switch = {
  // Switch between languages
  switchLanguage: function (language) {
    if (Blockly.mainWorkspace) {
      var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
      Blockly.mainWorkspace.clear();
    }
    switch (language) {
      case 'es_ES':
          Blockly.Msg.es_es.switch_language_to_spanish_es.init();
          break;
      case 'zh_TW':
        Blockly.Msg.zh_tw.switch_language_to_chinese_tw.init();
        break;
      case 'zh_CN':
        Blockly.Msg.zh_cn.switch_language_to_chinese_cn.init();
        break;
      case 'it_IT':
        Blockly.Msg.it_it.switch_language_to_italian.init()
        break;
      case 'en_US':
        Blockly.Msg.en.switch_language_to_english.init();
        break;
      case 'en':
      default:
        Blockly.Msg.en.switch_language_to_english.init();
        break;
    }
    if (Blockly.mainWorkspace) {
      Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
    }
  }
};

//switch language before blocks are generated
var language = window.parent.__gwt_Locale;
// console.log("Language = " + language);
Blockly.language_switch.switchLanguage(language);
