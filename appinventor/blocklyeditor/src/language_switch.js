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
goog.require('Blockly.Msg.zh_tw');


Blockly.language_switch = {
  // Switch between languages
  switchLanguage: function (language) {
    var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
    Blockly.mainWorkspace.clear();
    switch (language) {
      case 'zh_TW':
        Blockly.Msg.zh_tw.switch_language_to_chinese_tw.init();
        console.log('zh_TW');
        break;
      case 'en_US':
        Blockly.Msg.en.switch_language_to_english.init();
        console.log('en_US');
        break;
//      case 'de':
//        Blockly.Msg.de.switch_language_to_german.init();
//        break;
//      case 'vi':
//        Blockly.Msg.vn.switch_language_to_vietnamese.init();
//        break;
      case 'en':
        console.log('en');
      default:
        Blockly.Msg.en.switch_language_to_english.init();
        console.log('default');
        break;
    }
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
  }
};

