// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.language_switch');

goog.require('AI.Blockly.Msg.en');
goog.require('AI.Blockly.Msg.zh_cn');
goog.require('AI.Blockly.Msg.zh_tw');
goog.require('AI.Blockly.Msg.es_es');
goog.require('AI.Blockly.Msg.it_it');
goog.require('AI.Blockly.Msg.ru');
goog.require('AI.Blockly.Msg.ko_kr');
goog.require('AI.Blockly.Msg.sv');
goog.require('AI.Blockly.Msg.pt_br');
goog.require('AI.Blockly.Msg.pt');
goog.require('AI.Blockly.Msg.hu');
goog.require('AI.Blockly.Msg.nl');

Blockly.language_switch = {
  // Switch between languages
  switchLanguage: function (language) {
    if (Blockly.mainWorkspace) {
      var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
      Blockly.mainWorkspace.clear();
    }
    switch (language) {
      case 'ko_KR':
        Blockly.Msg.ko.switch_blockly_language_to_ko.init();
        Blockly.Msg.ko.switch_language_to_korean.init();
        break;
      case 'es_ES':
        Blockly.Msg.es.switch_blockly_language_to_es.init();
        Blockly.Msg.es.switch_language_to_spanish_es.init();
        break;
      case 'zh_TW':
        Blockly.Msg.zh.hans.switch_blockly_language_to_zh_hans.init();
        Blockly.Msg.zh.switch_language_to_chinese_tw.init();
        break;
      case 'zh_CN':
        Blockly.Msg.zh.hans.switch_blockly_language_to_zh_hans.init();
        Blockly.Msg.zh.switch_language_to_chinese_cn.init();
        break;
      case 'fr_FR':
        Blockly.Msg.fr.switch_blockly_language_to_fr.init();
        Blockly.Msg.fr.switch_language_to_french.init();
        break;
      case 'it_IT':
        Blockly.Msg.it.switch_blockly_language_to_it.init();
        Blockly.Msg.it.switch_language_to_italian.init();
        break;
      case 'ru':
        Blockly.Msg.ru.switch_blockly_language_to_ru.init();
        Blockly.Msg.ru.switch_language_to_russian.init();
        break;
      case 'sv':
        Blockly.Msg.sv.switch_blockly_language_to_sv.init();
        Blockly.Msg.sv.switch_language_to_swedish.init();
        break;
      case 'pt_BR':
        Blockly.Msg.pt.br.switch_blockly_language_to_pt_br.init();
        Blockly.Msg.pt.br.switch_language_to_portuguese_br.init();
        break;
      case 'pt':
        Blockly.Msg.pt.switch_blockly_language_to_pt.init();
        Blockly.Msg.pt.switch_language_to_portuguese.init();
        break;
      case 'hu':
        Blockly.Msg.hu.switch_blockly_language_to_hu.init();
        Blockly.Msg.hu.switch_language_to_hungarian.init();
        break;
      case 'nl':
        Blockly.Msg.nl.switch_language_to_dutch.init();
        break;
      case 'en_US':
      case 'en':
      default:
        Blockly.Msg.en.switch_blockly_language_to_en.init();
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
