/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License")';
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Simplified Chinese strings.
 * @author weihuali0509@gmail.com (Weihua Li)
 * @author helen.nie94@gmail.com (Helen Nie)
 * @author moyehan@gmail.com (Morton Mok)
 * @author roadlabs@gmail.com (roadlabs)
 * @author jcjzhl@gmail.com (Congjun Jin)
 */

'use strict';

goog.provide('Blockly.Msg.zh_tw');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.zh_tw.switch_language_to_chinese_tw = {
  // Switch language to Simplified Chinese.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = '復制代碼塊';
    Blockly.Msg.REMOVE_COMMENT = '刪除註釋';
    Blockly.Msg.ADD_COMMENT = '添加註釋';
    Blockly.Msg.EXTERNAL_INPUTS = '外掛輸入項';
    Blockly.Msg.INLINE_INPUTS = '內嵌輸入項';
    Blockly.Msg.HORIZONTAL_PARAMETERS = '橫向排列參數項';
    Blockly.Msg.VERTICAL_PARAMETERS = '縱向排列參數項';
    Blockly.Msg.DELETE_BLOCK = '刪除代碼塊';
    Blockly.Msg.DELETE_X_BLOCKS = '刪除 %1 個代碼塊';
    Blockly.Msg.COLLAPSE_BLOCK = '折疊代碼塊';
    Blockly.Msg.EXPAND_BLOCK = '展開代碼塊';
    Blockly.Msg.DISABLE_BLOCK = '禁用代碼塊';
    Blockly.Msg.ENABLE_BLOCK = '啟用代碼塊';
    Blockly.Msg.HELP = '幫助';
    Blockly.Msg.COLLAPSE_ALL = '折疊所有塊';
    Blockly.Msg.EXPAND_ALL = '展開所有塊';
    Blockly.Msg.ARRANGE_H = '橫向排列所有塊';
    Blockly.Msg.ARRANGE_V = '縱向排列所有塊';
    Blockly.Msg.ARRANGE_S = '斜向排列所有塊';
    Blockly.Msg.SORT_W = '按寬度對所有塊排序';
    Blockly.Msg.SORT_H = '按高度對所有塊排序';
    Blockly.Msg.SORT_C = '按類別對所有塊排序';

    Blockly.Msg.YAIL_OPTION = '生成Yail代碼';
    Blockly.Msg.DOIT_OPTION = '執行該代碼塊';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = '修改數值:';
    Blockly.MSG_NEW_VARIABLE = '新建變量...';
    Blockly.MSG_NEW_VARIABLE_TITLE = '新建變量名稱:';
    Blockly.MSG_RENAME_VARIABLE = '變量重命名...';
    Blockly.MSG_RENAME_VARIABLE_TITLE = '將所有 "%1" 變量重命名為:';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = '變量';
    Blockly.MSG_PROCEDURE_CATEGORY = '過程';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = '該代碼塊不能被定義';
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = '請從下拉列表中選擇合適項';
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = '重復的組件事件處理器';
    Blockly.ERROR_CAN_NOT_DO_IT_TITLE = '無法執行該代碼塊';
    Blockly.ERROR_CAN_NOT_DO_IT_CONTENT = '只有連接助手或模擬器程序，才能執行';

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = '點擊方塊選取所需顏色';
    Blockly.Msg.LANG_COLOUR_BLACK = '黑色';
    Blockly.Msg.LANG_COLOUR_WHITE = '白色';
    Blockly.Msg.LANG_COLOUR_RED = '紅色';
    Blockly.Msg.LANG_COLOUR_PINK = '粉色';
    Blockly.Msg.LANG_COLOUR_ORANGE = '橙色';
    Blockly.Msg.LANG_COLOUR_YELLOW = '黃色';
    Blockly.Msg.LANG_COLOUR_GREEN = '綠色';
    Blockly.Msg.LANG_COLOUR_CYAN = '青色';
    Blockly.Msg.LANG_COLOUR_BLUE = '藍色';
    Blockly.Msg.LANG_COLOUR_MAGENTA = '品紅';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = '淺灰';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = '深灰';
    Blockly.Msg.LANG_COLOUR_GRAY = '灰色';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = '分解色值';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = '返回含紅、綠、藍色值以及透明度值（0-255）的列表';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = '合成顏色';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = '返回由指定紅、綠、藍色值以及透明度值合成的顏色。';

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = '控制';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = '如果值為真，則執行相關語句塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = '如果值為真，則執行第壹個語句塊\n' +
        '否則, 執行第二個語句塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = '如果第壹個值為真，則執行第壹個語句塊，\n' +
        '否則，如果第二個值為真，則執行第二個語句塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = '如果第壹個值為真，則執行第壹個語句塊，\n' +
        '否則，如果第二個值為真，則執行第二個語句塊，\n' +
        '如果值皆不為真，則執行最後壹個語句塊';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = '否則，如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = '否則';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = '則';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = '添加、移除或重排相關元素，\n' +
        '重新設置該“如果”語句塊功能';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = '否則，如果';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = '為“如果”語句塊增設條件';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = '否則';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = '設最終條件，當所有條件均不滿足時執行';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = '重復';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = '只要';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = '直到';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = '只要值為真，就重復執行相關語句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = '只要值為假，就重復執行相關語句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = '只要條件為真，就執行”執行“區域所包含的語句塊';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = '循環取數到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = '範圍從';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = '到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = '執行';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = '從壹個數字開始取數，到另壹個數結束。\n' +
        '每取壹個數，都將其值賦給\n' +
        '變量 "%1"，並執行語句塊。';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = '循環取';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = '數字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = '範圍從';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = '到';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = '間隔為';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = '對壹定範圍內的數字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = '對於 ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' 範圍內的';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = '按指定範圍和增量循環取值，每次循環均將數值賦予指定變量，並運行“執行”區域所包含的代碼塊';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = '循環取';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = '列表項';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = '列表為';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = '對列表中每壹項';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = '對於 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' 列表中的';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = '針對列表中的每壹項運行“執行”區域所包含的代碼塊，'
    + ' 采用指定變量名引用當前列表項。';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = '循環';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = '中斷';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = '執行下壹個周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = '中斷內部循環';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = '跳轉到本循環的其余部分，並且\n' +
    '執行下壹個周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = '警告：\n' +
    '本代碼塊只能用於\n' +
    '循環語句塊';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = '當';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '滿足條件';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = '滿足條件';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = '“執行”區域中代碼塊被執行的前提是，滿足條件的表達式值為'
    + '真。';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = '如果'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = '則';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = '否則';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = '如果';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = '如果條件表達式的檢測值為真，' +
      '則將關聯的求值表達式運算結果傳遞給“則-返回”語句槽；' +
      '否則將關聯的求值表達式運算結果傳遞給“否則-返回”語句槽；' +
      '壹般只有壹個返回槽表達式能被求值。';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = '執行模塊';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = '返回結果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = '運行“執行”區域中的代碼塊並返回壹條語句，用於在賦值前插入執行某個過程。';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = '執行語句/返回結果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = '執行並返回';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = '求值但忽視結果'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = '求值但不返回';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = '運行所連接的代碼塊但不返回運算值，用於調用求值過程但不需要其運算值。';

    /* [林恩 13/10/14] 現在刪除。可能回來的某壹天。
    Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = '什麽';
    Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
    Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = ' 返回 nothing。用來初始化變量或可以插入到返回的插槽中，如果沒有價值需要返回。這是相當於為空或沒有.';
    */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = '打開屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = '屏幕名稱';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = '打開屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = '在多屏應用中打開壹個新屏幕。';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = '打開屏幕並傳值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = '屏幕名稱';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = '打開屏幕並傳值'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = '在多屏應用中開啟壹個新屏幕，並'
    + '向其傳入初始值';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = '獲取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = '屏幕名稱';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = '獲取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = '屏幕開啟時返回其傳入值，'
    + '在多屏應用中開啟動作壹般由其他屏幕引發。如沒有內容傳入，'
    + '則返回空文本。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = '關閉屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = '關閉屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = '關閉當前屏幕';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = '關閉屏幕並返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = '返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = '關閉屏幕返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = ' 關閉當前屏幕並向打開此屏幕者返回結果';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = '關閉所有屏幕並終止程序運行';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = '獲取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = '獲取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = ' 當屏幕被其他應用啟動時返回所傳入的文本值，'
    + '如沒有內容傳入，則返回空文本值。'
    + '對於多屏應用，壹般更多采用獲取初始值的方式，而非獲取純文本值。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = '關閉屏幕並返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = '文本值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = '關閉屏幕返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = '關閉當前屏幕，並向打開此屏幕的應用返回文本。'
    + '對於多屏應用，則多采用關閉屏幕返回值的方式，'
    + '而不采用關閉屏幕返回文本。';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = '邏輯';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality _(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = '判斷二對象是否相等，\n' +
    '對象可為任意類型，不限於數字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = '判斷二對象是否互不相等，對象可為任意類型，不限於數字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = '比較';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '等於';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '不等於';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = '並且';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = '或者';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = '如所有輸入項皆為真則返回真值。';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = '只要有輸入項為真則返回真值。';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = '否定';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = '如輸入項為假則返回真值，\n' +
    '如輸入項為真則返回假值。';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = '返回true值';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = '返回false值';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = '數學';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = '報告所顯示的數字 ';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = '數字';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = '如二數字相等則返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = '如二數字不等則返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = '如第壹個數字小於第二個數字，\n' +
    '則返回true值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = '如第壹個數字小於或等於第二個數字，\n' +
    '則返回false值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = '如第壹個數字大於第二個數字，\n' +
    '則返回true值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = '如果第壹個數大於或等於第二個數，\n' +
    '則返回false值。';
    Blockly.Msg.LANG_MATH_COMPARE_EQ = '=';
    Blockly.Msg.LANG_MATH_COMPARE_NEQ = '\u2260';
    Blockly.Msg.LANG_MATH_COMPARE_LT = '<';
    Blockly.Msg.LANG_MATH_COMPARE_LTE = '\u2264';
    Blockly.Msg.LANG_MATH_COMPARE_GT = '>';
    Blockly.Msg.LANG_MATH_COMPARE_GTE = '\u2265';

    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_ADD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#add';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MINUS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#subtract';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#multiply';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#divide';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_POWER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#exponent';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = '求二數之和';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = '求二數之差';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = '求二數乘積';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = '求二數之商';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = '求第壹個數的第二個數次方';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = '改變';
    Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = '項目';
    Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = '由';
    Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = '添加號碼到變量"%1"。';*/

    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = '平方根';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = '絕對值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = '相反值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = '自然對方';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e的乘方';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = '求平方根';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = '求絕對值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = '求相反值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = '求自然對數值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = '求e的乘方';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = '返回 10 數的力量'; */

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = '就高或或就低取整';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = '將輸入項取整為不低於其的最小數值';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = '將輸入項取整為不大於其的最大數值';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = '四舍五入';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = '就高取整';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = '就低取整';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x坐標';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y坐標';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = '求正弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = '求余弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = '求正切值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = '由正弦值求角度(-90,+90]';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = '由余弦值求角度[0, 180)';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = '由正切值求角度(-90, +90)';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = '由直角坐標求角度(-180, +180]';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = '最小值';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = '最大值';

//TODO: I don't think any of this is useful anymore...Delete?
    /*Blockly.Msg.LANG_MATH_ONLIST_HELPURL = '';
     Blockly.Msg.LANG_MATH_ONLIST_INPUT_OFLIST = 'of list';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_SUM = 'sum';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_AVERAGE = 'average';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MEDIAN = 'median';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MODE = 'modes';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_STD_DEV = 'standard deviation';
     Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_RANDOM = 'random item';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_SUM = 'Return the sum of all the numbers in the list.';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Return the smallest of its arguments..';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Return the largest of its arguments..';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_AVERAGE = 'Return the arithmetic mean of the list.';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MEDIAN = 'Return the median number in the list.';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MODE = 'Return a list of the most common item(s) in the list.';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_STD_DEV = 'Return the standard deviation of the list.';
     Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_RANDOM = 'Return a random element from the list.';

     Blockly.Msg.LANG_MATH_CONSTRAIN_HELPURL = 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29';
     Blockly.Msg.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN = 'constrain';
     Blockly.Msg.LANG_MATH_CONSTRAIN_INPUT_LOW = 'between (low)';
     Blockly.Msg.LANG_MATH_CONSTRAIN_INPUT_HIGH = 'and (high)';
     Blockly.Msg.LANG_MATH_CONSTRAIN_TOOLTIP = 'Constrain a number to be between the specified limits (inclusive).';
*/

    Blockly.Msg.LANG_MATH_DIVIDE = '除以';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = '模數';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = '余數';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = '商數';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = '求模數';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = '求余數';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = '求商數';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = '隨機整數';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = '範圍從';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = '隨機整數從 %1 到 %2 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = '返回位於上下邊界之間的隨機整數，\n' +
    '限於2的30次方範圍內';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = '隨機小數';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = '返回0和1之間的隨機數值';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = '隨機數種子設定';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = '為';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = '為隨機數生成器指定種子數';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = '角度變換';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = '弧度轉角度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = '角度轉弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = '求弧度參數對應的角度值[0, 360)';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = '求角度參數對應的弧度值[-\u03C0, +\u03C0)';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = '求小數值';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = '數字';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = '位數';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = '將數字 %1設為小數形式 位置 %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = '以指定位數返回該數值的小數形式';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '是否為數字?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = '判斷該對象是否為數字類型';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = '輸入字符串文本';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = '創建字符串文本';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = '將所有輸入項合並為壹個單獨的字符串文本，\n'
    + '如沒有輸入項，則生成空文本。';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = '合並文本';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = '字符串';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = '到';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = '追加文本';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = '變量';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = '將文本追加到變量 "%1"';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = '求長度';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = '求該文本中所包含的字母數量(包括空格)';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = '是否為空';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = '如文本長度為0則返回真值，否則返回假值';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = '比較文本';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = '檢測text1的首字母順序是否低於text2，\n'
    + '如果首字母相同，則長度較短的文本順序靠前，\n'
    + '大寫字符順序優於小寫字符。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = '檢測文本字串內容是否相同，即，\n'
    + '是否由同壹組相同順序的字符組成，與通常的相等概念不同的是，\n'
    + '當文本字串為數字時，如123和0123，盡管數字相等，\n'
    + '但其文本不等。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = '告之text1的字首順序是否高於text2，\n'
    + '如首字母相同，則長度較短的文本順序靠前，\n'
    + '大寫字符順序優於小寫字符。';

    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = 'letters in text';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = 'Returns specified number of letters at the beginning or end of the text.';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'first';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'last';

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = 'find';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'occurrence of text';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'in text';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = 'Returns the index of the first/last occurrence\n' +
     'of first text in the second text.\n' +
     'Returns 0 if text is not found.';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'first';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'last';

    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = 'letter at';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = 'in text';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = 'Returns the letter at the specified position.';
*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = '大寫';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = '小寫';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = '將字符串參數復制並轉為大寫後返回';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = '將字符串參數復制並轉為小寫後返回';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = '刪除空格';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = '將字符串參數復制並刪除首尾處的空格後返回';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = '子串位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = '求子串%2在文本%1中的起始位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = '求子串在文本中的起始位置，\n'
    + '其中1表示文本的起始處，\n '
    + '而如子串不在文本中則返回0。';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = '包含子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = '檢查文本%1中是否包含子串%2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = '檢查文本中是否包含該子串';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = '分隔符';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = '分隔符 (列表)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = '分解首項';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = '分解任意首項';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = '分解';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = '任意分解';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '在首次出現分隔符的位置將給定文本分解為兩部分，\n'
    + '並返回包含分隔點前和分隔點後兩部分內容的列表，\n'
    + '如分解字符串"蘋果,香蕉,櫻桃,狗糧"，以逗號作為分隔符，\n'
    + '將返回壹個包含兩項的列表，其中第壹項內容為"蘋果"，第二項內容則為\n'
    + '"香蕉,櫻桃,狗糧"。\n'
    + '註意，"蘋果"後面的逗號不在結果中出現，\n'
    + '因為它起到分隔符的作用。';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = '以列表中的任意項作為分隔符，\n'
    + '在首次出現分隔符的位置將給定文本分解為壹個兩項列表。\n\n'
    + '如以"(稥,蘋)"作為分隔符分解"我喜歡蘋果香蕉蘋果葡萄"，\n'
    + '將返回壹個兩項列表，其第壹項為"我喜歡"，第二項為\n'
    + '"果香蕉蘋果葡萄"';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = '以指定文本作為分隔符，將字符串分解為不同片段，並生成壹個列表作為返回結果。\n'
    + ' 如以","(逗號)分解"壹,二,三,四"，將返回列表"(壹 二 三 四)"，\n'
    + ' 而以"-土豆"作為分隔符分解字符串"壹-土豆,二-土豆,三-土豆,四"，則返回列表"(壹 二 三 四)"。'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY  = '以分隔符列表中的任意壹項作為分隔符，將給定文本分解為列表，\n'
    + '並將列表作為處理結果返回。\n'
    + '如分解字符串"藍莓,香蕉,草莓,狗糧"，以壹個含兩元素的列表作為分隔符，\n'
    + '其中第壹項為逗號，第二項為"莓"，則返回列表：\n'
    + '"(藍 香蕉 草 狗糧)"'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatany';

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = 'print';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = 'Print the specified text, number or other value.';

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'prompt for';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'with message';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = 'Prompt for user input with the specified text.';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = 'text';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = 'number';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitspaces';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = '用空格分解';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = '以空格作為分隔符，將文本分解為若幹部分。';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = '提取子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = '提取位置';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = '提取長度';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = '從文本%1第%2位置提取長度為%3的子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = '以指定長度、指定位置從指定文本中提取文本片段，\n'
    + '位置1表示被提取文本的起始處。';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = '替換項';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = '原始文本';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = '全部替換';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = '替換為';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = '將文本%2中所有%1全部替換為%3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = '返回壹個新文本字符串，其中所包含的替換項內容\n'
    + '均被替換為指定的字串。';

    Blockly.Msg.LANG_CATEGORY_LISTS = '列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty _lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = '創建空列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = '返回壹個項數為零的列表對象';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = '創建列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = '創建壹個可包含任意項數的列表';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = '重新配置該列表塊，為其增加、刪除或重新排列所包含的區間。';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = '列表項';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = '增加壹個列表項';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = '列表項';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = '增加壹個列表項';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = '選擇列表項';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = '選擇列表%1中索引值為%2的列表項';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = '求指定位置的列表項';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '是否在列表中?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = '檢查查列表%2中是否含列表項%1'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = '如該對象為列表中某壹項則返回真值，'
    + '否則為假。';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = '列表項索引值';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = '求列表項%1在列表%2中的位置';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = '求對象在該列表中的位置，'
    + '如不在該列表中，則返回0。';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = '隨機選取列表項';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = '從列表中隨機選取壹項';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = '替換列表項';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = '替換為';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = '將列表%1中索引值為%2的列表項替換為%3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = '替換列表中第n項內容';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = '刪除列表項';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = '刪除列表%1中第%2項';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = '刪除指定位置的列表項';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = '列表 ' 創建與項目 ';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = '重復';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = '時代';
    Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = '創建壹個列表組成的給定 value\n' +
    '重復指定的次數的';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = '求列表長度 ';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = '計算列表%1長度';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = '計算列表項數';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = '追加列表';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = '列表1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = '列表2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = '將列表%2中所有項追加到列表%1中';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = '將list2中所有項添加到list1的末尾。添加後，'
    + 'list1中將包括所有新加入的元素，而list2則不發生變化。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = '添加列表項';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = '列表項';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = '將列表項%2加入列表%1中';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = '在列表末尾增加列表項';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = '重新配置該列表塊，增加、刪除或重新排序其中包含的區間';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = '復制列表';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = '復制列表，包括復制其中包含的所有子列表';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '是否為列表?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = '檢測該對象是否為列表類型';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = '列表轉CSV行';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = '將列表轉換為表格中的壹行數據，'
    + '並返回表示行數據的CSV（逗號分隔數值）字符串，數據行中的每壹項被當作壹個字段，'
    + '在CSV字符串中以雙引號方式標識，'
    + '各數據項以逗號分隔，且每行末尾'
    + '均不帶換行符。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'CSV行轉列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'CSV字符串';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = '對CSV（逗號分隔數值）格式字符串進行解析，'
    + '生成壹個包含各字段數據的列表。對於文本行而言，如字段中出現非轉義的換行符則會出錯'
    + '（實際是指多行字段的情況），而只在整行文本的末端才出現換行符或CRLF則是正確的。';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = '列表轉CSV';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = '將列表轉換為帶標題行的表格形式，'
    + '且返回表示該表格的CSV（逗號分隔數值）字符串文本，列表中的每壹項本身'
    + '還可以作為表示CSV表格行的列表，列表行中的每壹項'
    + '都可看成是壹個字段，在CSV字符串文本中以雙引號方式進行標識。'
    + '在返回字符串文本中，數據行中的各項以逗號進行分隔，'
    + '而各數據行則以CRLF \(\\r\\n\)進行分隔。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'CSV轉列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'CSV字符串';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = '對CSV（逗號分隔數值）格式字符串進行解析，'
    + '並生成數據行列表，其中的每壹項又都是壹個字段列表，'
    + '各數據行間分別以換行符\(\\n\)或CRLF \(\\r\\n\)方式分隔。';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = '插入列表項';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = '插入位置';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = '插入項';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = '在列表%1的第%2項處插入列表項%3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = '在指定位置插入列表項';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '列表是否為空?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = '如果列表為空則返回真';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = '鍵值對查詢';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = '關鍵字';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = '鍵值對';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = '無果則返回';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = '在鍵值對%2中查找關鍵字%1 如未找到則返回%3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = '返回鍵值對列表中與關鍵字關聯的數值';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = '找到';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = '的項目發生';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = '在列表中 ';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = '返回的索引的第壹個/最後壹個 occurrence\n' +
    '的項目這個列表 \n' +
    '返回 0，如果找不到文本。';
    Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = '第壹';
    Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = '最後';

     Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = 'get item at';
     Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'in list';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = 'Returns the value at the specified position in a list.';

     Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = 'set item at';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = 'to';
     Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = 'Sets the value at the specified position in a list.';
     */
// Variables Blocks.

    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = '初始化全局變量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = '我的變量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = '為';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = '全局變量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = '創建全局變量，並通過掛接的代碼塊賦值';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = '求';
    // Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = '項目';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = '求變量值';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = '求變量值';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = '設';
    // Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = '項目';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = '為';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = '設變量值';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = '設變量值等於輸入項';

    Blockly.Msg.LANG_VARIABLES_VARIABLE = '變量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = '初始化局部變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = '我的變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = '為';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = '作用範圍';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = '局部變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '創建指定範圍內語句塊所使用的變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = '初始化局部變量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    // 這些別不同之間的語句和表達式
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = '初始化表達式變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = '我的變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = '為';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = '作用範圍';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = '表達式變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '創建指定表達式所使用的變量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = '初始化表達式變量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = '輸入項';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = '參數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = '定義過程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = '我的過程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = '執行語句';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = '定義過程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = '語句執行完成後，不返回結果';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = '然後返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = '執行語句';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = '“執行”其中包含的塊並返回壹條語句， 可以實現在過程執行前將返回數據賦值給相關變量';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = '執行/返回';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = '定義過程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = '定義過程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = '語句執行完成後，會返回結果';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = '警告:\n' +
    '此過程的輸入項\n' +
    '出現重復';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = '調用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = '過程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = '調用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = '調用無返回值過程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = '調用無返回值過程';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = '調用';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = '調用有返回值過程';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = '調用有返回值過程';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = '輸入項';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = '輸入:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = '突出顯示過程';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = '當';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = '執行';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = '調用';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = '調用';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = '組件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = '組件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = '設';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = '為';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = '設';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = '為';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = '組件';

///////////////////
    /* HelpURLs for Component Blocks */

//User Interface Components
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_HELPURL = '/reference/components/userinterface.html#Button';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_PROPERTIES_HELPURL = '/reference/components/userinterface.html#buttonproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_EVENTS_HELPURL = '/reference/components/userinterface.html#buttonevents';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL = '/reference/components/userinterface.html#CheckBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#checkboxproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#checkboxevents';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_HELPURL = '/reference/components/userinterface.html#Clock';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_PROPERTIES_HELPURL = '/reference/components/userinterface.html#clockproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_EVENTS_HELPURL = '/reference/components/userinterface.html#clockevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_METHODS_HELPURL = '/reference/components/userinterface.html#clockmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_HELPURL = '/reference/components/userinterface.html#Image';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_PROPERTIES_HELPURL = '/reference/components/userinterface.html#imageproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_EVENTS_HELPURL = '/reference/components/userinterface.html#imageevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_METHODS_HELPURL = '/reference/components/userinterface.html#imagemethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_HELPURL = '/reference/components/userinterface.html#Label';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_PROPERTIES_HELPURL = '/reference/components/userinterface.html#labelproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_EVENTS_HELPURL = '/reference/components/userinterface.html#labelevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_METHODS_HELPURL = '/reference/components/userinterface.html#labelmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_HELPURL = '/reference/components/userinterface.html#ListPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#listpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_EVENTS_HELPURL = '/reference/components/userinterface.html#listpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_METHODS_HELPURL = '/reference/components/userinterface.html#listpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL = '/reference/components/userinterface.html#Notifier';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#notifierproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL = '/reference/components/userinterface.html#notifierevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_METHODS_HELPURL = '/reference/components/userinterface.html#notifiermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#pwdboxproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#pwdboxevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#pwdboxmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_HELPURL = '/reference/components/userinterface.html#Screen';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_PROPERTIES_HELPURL = '/reference/components/userinterface.html#screenproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_EVENTS_HELPURL = '/reference/components/userinterface.html#screenevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_METHODS_HELPURL = '/reference/components/userinterface.html#screenmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_HELPURL = '/reference/components/userinterface.html#Slider';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#sliderproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_EVENTS_HELPURL = '/reference/components/userinterface.html#sliderevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_METHODS_HELPURL = '/reference/components/userinterface.html#slidermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_HELPURL = '/reference/components/userinterface.html#TextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#textboxproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#textboxevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#textboxmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL = '/reference/components/userinterface.html#WebViewer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#webviewerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL = '/reference/components/userinterface.html#webviewerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL = '/reference/components/userinterface.html#webviewermethods';

//Layout components
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL = '/reference/components/layout.html#HorizontalArrangement';
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#horizarrangeproperties';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL = '/reference/components/layout.html#VerticalArrangement';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#vertarrangeproperties';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL = '/reference/components/layout.html#TableArrangement';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#tablearrangeproperties';

//Media components
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_HELPURL = '/reference/components/media.html#Camcorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_PROPERTIES_HELPURL = '/reference/components/media.html#camcorderproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_EVENTS_HELPURL = '/reference/components/media.html#camcorderevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_METHODS_HELPURL = '/reference/components/media.html#camcordermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_HELPURL = '/reference/components/media.html#Camera';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_PROPERTIES_HELPURL = '/reference/components/media.html#cameraproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_EVENTS_HELPURL = '/reference/components/media.html#cameraevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_METHODS_HELPURL = '/reference/components/media.html#cameramethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_HELPURL = '/reference/components/media.html#ImagePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_PROPERTIES_HELPURL = '/reference/components/media.html#imagepickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_EVENTS_HELPURL = '/reference/components/media.html#imagepickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_METHODS_HELPURL = '/reference/components/media.html#imagepickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_HELPURL = '/reference/components/media.html#Player';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#playerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_EVENTS_HELPURL = '/reference/components/media.html#playerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_METHODS_HELPURL = '/reference/components/media.html#playermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_HELPURL = '/reference/components/media.html#Sound';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_PROPERTIES_HELPURL = '/reference/components/media.html#soundproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_EVENTS_HELPURL = '/reference/components/media.html#soundevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_METHODS_HELPURL = '/reference/components/media.html#soundmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL = '/reference/components/media.html#SoundRecorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL = '/reference/components/media.html#soundrecorderproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL = '/reference/components/media.html#soundrecorderevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL = '/reference/components/media.html#soundrecordermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL = '/reference/components/media.html#SpeechRecognizer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL = '/reference/components/media.html#speechrecognizerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL = '/reference/components/media.html#speechrecognizerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL = '/reference/components/media.html#speechrecognizermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL = '/reference/components/media.html#TextToSpeech';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL = '/reference/components/media.html#texttospeechproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL = '/reference/components/media.html#texttospeechevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL = '/reference/components/media.html#texttospeechmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL = '/reference/components/media.html#VideoPlayer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#videoplayerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL = '/reference/components/media.html#videoplayerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL = '/reference/components/media.html#videoplayermethods';

// Drawing and Animation components
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_HELPURL = '/reference/components/animation.html#Ball';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL = '/reference/components/animation.html#ballproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL = '/reference/components/animation.html#ballevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL = '/reference/components/animation.html#ballmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_HELPURL = '/reference/components/animation.html#Canvas';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL = '/reference/components/animation.html#canvasproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL = '/reference/components/animation.html#canvasevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL = '/reference/components/animation.html#canvasmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL = '/reference/components/animation.html#ImageSprite';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL = '/reference/components/animation.html#imagespriteproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL = '/reference/components/animation.html#imagespriteevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL = '/reference/components/animation.html#imagespritemethods';

//Sensor components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL = '/reference/components/sensor.html#AccelerometerSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL = '/reference/components/sensor.html#accelerometersensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL = '/reference/components/sensor.html#accelerometersensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL = '/reference/components/sensor.html#accelerometersensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL = '/reference/components/sensor.html#BarcodeScanner';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL = '/reference/components/sensor.html#barcodescannerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL = '/reference/components/sensor.html#barcodescannerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL = '/reference/components/sensor.html#barcodescannermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL = '/reference/components/sensor.html#LocationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensor.html#locationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensor.html#locationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL = '/reference/components/sensor.html#locationsensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL = '/reference/components/sensor.html#OrientationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensor.html#orientationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensor.html#orientationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL = '/reference/components/sensor.html#orientationsensormethods';

//Social components
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL = '/reference/components/social.html#ContactPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#contactpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL = '/reference/components/social.html#contactpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL = '/reference/components/social.html#contactpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL = '/reference/components/social.html#EmailPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#emailpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL = '/reference/components/social.html#emailpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL = '/reference/components/social.html#emailpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL = '/reference/components/social.html#PhoneCall';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL = '/reference/components/social.html#phonecallproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL = '/reference/components/social.html#phonecallevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL = '/reference/components/social.html#phonecallmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL = '/reference/components/social.html#PhoneNumberPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#phonenumberpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL = '/reference/components/social.html#phonenumberpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL = '/reference/components/social.html#phonenumberpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_HELPURL = '/reference/components/social.html#Texting';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL = '/reference/components/social.html#textingproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL = '/reference/components/social.html#textingevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL = '/reference/components/social.html#textingmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_HELPURL = '/reference/components/social.html#Twitter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL = '/reference/components/social.html#twitterproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL = '/reference/components/social.html#twitterevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL = '/reference/components/social.html#twittermethods';

//Storage Components
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL = '/reference/components/storage.html#FusiontablesControl';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL = '/reference/components/storage.html#fusiontablescontrolproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL = '/reference/components/storage.html#fusiontablescontrolevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL = '/reference/components/storage.html#fusiontablescontrolmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_HELPURL = '/reference/components/storage.html#TinyDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinydbproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL = '/reference/components/storage.html#tinydbevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL = '/reference/components/storage.html#tinydbmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL = '/reference/components/storage.html#TinyWebDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinywebdbproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL = '/reference/components/storage.html#tinywebdbevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL = '/reference/components/storage.html#tinywebdbmethods';

//Connectivity components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL = '/reference/components/connectivity.html#ActivityStarter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#activitystarterproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL = '/reference/components/connectivity.html#activitystarterevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL = '/reference/components/connectivity.html#activitystartermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL = '/reference/components/connectivity.html#BluetoothClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothclientproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothclientevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothclientmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL = '/reference/components/connectivity.html#BluetoothServer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothserverproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothserverevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothservermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_HELPURL = '/reference/components/connectivity.html#Web';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL = '/reference/components/connectivity.html#webproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL = '/reference/components/connectivity.html#webevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL = '/reference/components/connectivity.html#webmethods';

//Lego mindstorms components
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL = '/reference/components/legomindstorms.html#NxtDirectCommands';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdirectproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdirectmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL = '/reference/components/legomindstorms.html#NxtColorSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtcolorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtcolorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtcolormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL = '/reference/components/legomindstorms.html#NxtLightSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtlightproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtlightevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtlightmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL = '/reference/components/legomindstorms.html#NxtSoundSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtsoundproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL = '/reference/components/legomindstorms.html#NxtTouchSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxttouchproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxttouchevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxttouchmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL = '/reference/components/legomindstorms.html#NxtUltrasonicSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL = '/reference/components/legomindstorms.html#NxtDrive';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdriveproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdrivemethods';

//Internal components
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL = '/reference/components/internal.html#GameClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL = '/reference/components/internal.html#gameclientproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL = '/reference/components/internal.html#gameclientevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL = '/reference/components/internal.html#gameclientmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_HELPURL = '/reference/components/internal.html#Voting';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL = '/reference/components/internal.html#votingproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL = '/reference/components/internal.html#votingevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL = '/reference/components/internal.html#votingmethods';

//Misc
    Blockly.Msg.SHOW_WARNINGS = "顯示警告";
    Blockly.Msg.HIDE_WARNINGS = "隱藏警告";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "妳應該為模塊的每個端口都填上模塊";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "這個模塊應該連上事件或者過程模塊";

//Replmgr.js messages
    Blockly.Msg.REPL_ERROR_FROM_COMPANION ="AI伴侶出現錯誤";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR ="發生網絡連接故障";
    Blockly.Msg.REPL_NETWORK_ERROR ="網絡故障";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART ="與AI伴侶通信故障，<br />請嘗試重啟伴侶程序並重新連接";
    Blockly.Msg.REPL_OK ="確定";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK ="檢查伴侶程序版本";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = '伴侶程序已過期，點擊確定鍵升級。';
    Blockly.Msg.REPL_EMULATORS ="查看模擬器";
    Blockly.Msg.REPL_DEVICES ="設備";
    Blockly.Msg.REPL_APPROVE_UPDATE ="屏幕，確認升級";
    Blockly.Msg.REPL_NOT_NOW ="現在不";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 ="妳使用的伴侶程序已經過期，<br/><br/>本版App Inventor適用的伴侶程序版本為";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE ="妳正在使用壹個過期版本的伴侶程序，請盡快升級";
    Blockly.Msg.REPL_DISMISS ="放棄";
    Blockly.Msg.REPL_SOFTWARE_UPDATE ="軟件升級";
    Blockly.Msg.REPL_OK_LOWER ="確定";
    Blockly.Msg.REPL_GOT_IT ="升級完成";
    Blockly.Msg.REPL_UPDATE_INFO = '更新正在安裝在你的設備上。請查看你設備(或模擬器)屏幕出現的提示並批準軟件安裝。<br /><br />重要:當更新完成,選擇“完成”(不要點擊“開放”)。然後再次瀏覽 App Inventor 網頁,點擊“連接”菜單,選擇“重置連接”。然後重新連接設備。';
    Blockly.Msg.REPL_UNABLE_TO_UPDATE ="無法將升級包發送給設備或模擬器";
    Blockly.Msg.REPL_UNABLE_TO_LOAD ="無法從App Inventor服務器下載升級包";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND ="無法App Inventor服務器(服務器沒有響應)加載更新信息";
    Blockly.Msg.REPL_NOW_DOWNLOADING ="正在從App Inventor服務器下載升級包，請耐心等待。";
    Blockly.Msg.REPL_RUNTIME_ERROR ="運行故障";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS ="<br/><i>註意：</i>&nbsp;5秒鐘後將顯示另壹條錯誤信息。";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE ="正在通過USB電纜連接";
    Blockly.Msg.REPL_STARTING_EMULATOR ="正在啟動Android模擬器<br/>請等待：可能需要壹至兩分鐘";
    Blockly.Msg.REPL_CONNECTING ="連接中...";
    Blockly.Msg.REPL_CANCEL ="取消";
    Blockly.Msg.REPL_GIVE_UP ="放棄";
    Blockly.Msg.REPL_KEEP_TRYING ="重試";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 ="連接失敗";
    Blockly.Msg.REPL_NO_START_EMULATOR ="無法在模擬器中啟動伴侶程序";
    Blockly.Msg.REPL_PLUGGED_IN_Q ="是否已插入電纜？";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE ="AI2沒有看到妳的設備，請確認電纜連接以及驅動程序安裝是否正常。";
    Blockly.Msg.REPL_HELPER_Q ="是否運行助手？";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'aiStarter助手程序不在執行狀態中，<br />是否需要<a href="http://appinventor.mit.edu" target="_blank">幫助?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT ="已連接USB，請等待";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING ="秒，確保相關資源全部加載。";
    Blockly.Msg.REPL_EMULATOR_STARTED ="模擬器已運行，請等待";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE ="正在所連接電話設備中啟動伴侶程序";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR ="正在模擬器中啟動伴侶程序";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING ="伴侶程序啟動中，請等待";
    Blockly.Msg.REPL_VERIFYING_COMPANION ="檢查伴侶程序啟動狀態....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION ="連接伴侶程序";
    Blockly.Msg.REPL_TRY_AGAIN1 ="無法連接伴侶程序，請重新連接。";
    Blockly.Msg.REPL_YOUR_CODE_IS ="編碼為：";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q ="妳真的要這麽做嗎？";
    Blockly.Msg.REPL_FACTORY_RESET = "這將使模擬器重置為出廠模式，如果此前升級過伴侶程序，則需要重新升級。";

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "你確定要全部刪除 %1 個這些模塊嗎?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "生成 Yail";
    Blockly.Msg.DO_IT = "實現功能";
    Blockly.Msg.CAN_NOT_DO_IT = "不能實現功能";
    Blockly.Msg.CONNECT_TO_DO_IT = '你必須要連接AI伴侶或者模擬器才能使用"實現功能"';
  }
};

// Initalize language definition to English
Blockly.Msg.zh_tw.switch_language_to_chinese_tw.init();