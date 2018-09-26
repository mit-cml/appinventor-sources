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
 * @author joechuang01.tw@gmail.com (Chuang Kai Chiao)
 * @author nissin@cavedu.com (David Tseng) 
 * @author tank@mail.chsh.ntct.edu.tw (Tank Tsai) 
 */

'use strict';

goog.provide('AI.Blockly.Msg.zh_tw');

goog.require('Blockly.Msg.zh.hans');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.zh.switch_language_to_chinese_tw = {
  // Switch language to Traditional Chinese.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = '複製程式方塊';
    Blockly.Msg.REMOVE_COMMENT = '刪除註解';
    Blockly.Msg.ADD_COMMENT = '增加註解';
    Blockly.Msg.EXTERNAL_INPUTS = '外接輸入項';
    Blockly.Msg.INLINE_INPUTS = '內嵌輸入項';
    Blockly.Msg.HORIZONTAL_PARAMETERS = '橫向排列參數項';
    Blockly.Msg.VERTICAL_PARAMETERS = '縱向排列參數項';
    Blockly.Msg.DELETE_BLOCK = '刪除程式方塊';
    Blockly.Msg.DELETE_X_BLOCKS = '刪除 %1 個程式方塊';
    Blockly.Msg.COLLAPSE_BLOCK = '摺疊程式方塊';
    Blockly.Msg.EXPAND_BLOCK = '展開程式方塊';
    Blockly.Msg.DISABLE_BLOCK = '停用程式方塊';
    Blockly.Msg.ENABLE_BLOCK = '啟用程式方塊';
    Blockly.Msg.HELP = '求助';
    Blockly.Msg.EXPORT_IMAGE = '匯出程式方塊圖片';
    Blockly.Msg.COLLAPSE_ALL = '摺疊所有方塊';
    Blockly.Msg.EXPAND_ALL = '展開所有方塊';
    Blockly.Msg.ARRANGE_H = '橫向排列所有方塊';
    Blockly.Msg.ARRANGE_V = '縱向排列所有方塊';
    Blockly.Msg.ARRANGE_S = '斜向排列所有方塊';
    Blockly.Msg.SORT_W = '按寬度排序所有方塊';
    Blockly.Msg.SORT_H = '按高度排序所有方塊';
    Blockly.Msg.SORT_C = '按類別排序所有方塊';

    Blockly.Msg.YAIL_OPTION = '生成Yail程式碼';
    Blockly.Msg.DOIT_OPTION = '執行該程式方塊';

    Blockly.Msg.COPY_TO_BACKPACK = '增加至背包';
    Blockly.Msg.COPY_ALLBLOCKS = '複製所有程式方塊到背包';
    Blockly.Msg.BACKPACK_GET = '拿出背包中所有程式方塊';
    Blockly.Msg.BACKPACK_EMPTY = '清空背包';
    Blockly.Msg.BACKPACK_CONFIRM_EMPTY = '您確定要清空背包嗎？';
    Blockly.Msg.BACKPACK_DOC_TITLE = "背包介紹";
    Blockly.Msg.SHOW_BACKPACK_DOCUMENTATION = "顯示背包說明";
    Blockly.Msg.BACKPACK_DOCUMENTATION = "背包具有備份功能。它允許您從專案或螢幕中複製程式方塊到另一個專案或螢幕。複製時，將程式方塊從工作區域拖放入背包；接著另一個專案或畫面，點選背包圖標將其中的程式方塊拖放入工作區域。"
    + "</p><p>退出MIT App Inventor時，程式方塊將留在背包中，並保存到您下次登入。"
    + "</p><p>想要了解更多有關背包的介紹，請參考："
    + '</p><p><a href="/reference/other/backpack.html" target="_blank">http://ai2.appinventor.mit.edu/reference/other/backpack.html</a>';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = '修改數值:';
    Blockly.MSG_NEW_VARIABLE = '新增變數...';
    Blockly.MSG_NEW_VARIABLE_TITLE = '新增變數名稱:';
    Blockly.MSG_RENAME_VARIABLE = '變數重新命名...';
    Blockly.MSG_RENAME_VARIABLE_TITLE = '將所有 "%1" 變數重新命名為:';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = '變數';
    Blockly.MSG_PROCEDURE_CATEGORY = '程序';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = '該程式方塊無法定義';
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = '請從下拉式選單中選取有效項目';
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = '已存在相同名稱的事件處理器';
    Blockly.ERROR_COMPONENT_DOES_NOT_EXIST = "元件不存在";
    Blockly.ERROR_BLOCK_IS_NOT_DEFINED = "該程式方塊未定義。刪除該程式方塊！";

    Blockly.ERROR_CAN_NOT_DO_IT_CONTENT = '只有連接「AI Companion」或「模擬器」才能執行';
    Blockly.ERROR_CAN_NOT_DO_IT_TITLE = '無法執行該程式方塊';

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = '點擊方形區域來選取顏色';
    Blockly.Msg.LANG_COLOUR_BLACK = '黑色';
    Blockly.Msg.LANG_COLOUR_WHITE = '白色';
    Blockly.Msg.LANG_COLOUR_RED = '紅色';
    Blockly.Msg.LANG_COLOUR_PINK = '粉色';
    Blockly.Msg.LANG_COLOUR_ORANGE = '橙色';
    Blockly.Msg.LANG_COLOUR_YELLOW = '黃色';
    Blockly.Msg.LANG_COLOUR_GREEN = '綠色';
    Blockly.Msg.LANG_COLOUR_CYAN = '青色';
    Blockly.Msg.LANG_COLOUR_BLUE = '藍色';
    Blockly.Msg.LANG_COLOUR_MAGENTA = '洋紅';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = '淺灰';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = '深灰';
    Blockly.Msg.LANG_COLOUR_GRAY = '灰色';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = '分解顏色值';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = '回傳包含紅、綠、藍色以及透明度值（0-255）的清單';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = '合成顏色';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = '回傳由指定紅、綠、藍色值以及透明度值合成的顏色。';

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = '控制';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = '如果值為真，則執行「則」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = '如果值為真，則執行「則」內的程式方塊\n' +
        '否則, 執行「否則」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = '如果第一個值為真，則執行「則」內的程式方塊，\n' +
        '否則，如果第二個值為True，則執行第二個「則」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = '如果第一個值為真，則執行「則」內的程式方塊，\n' +
        '否則，如果第二個值為True，則執行第二個「則」內的程式方塊，\n' +
        '如果值皆不為真，則執行最後一個「否則」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = '否則，如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = '否則';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = '則';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = '增加、移除或重排相關元素，\n' +
        '重新設置該「如果」程式方塊功能';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = '否則，如果';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = '為「如果」程式方塊增設條件';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = '否則';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = '最終條件，當所有條件均不滿足時執行本項';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = '當滿足條件…執行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = '當';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = '直到';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = '只要值為真，就重複執行「當滿足條件…執行」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = '只要值為假，就重複執行「直到滿足條件…執行」內的程式方塊';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = '只要條件為真，就執行「當滿足條件…執行」內的程式方塊';

    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = '依序取數到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = '數字';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = '範圍從';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = '到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = '執行';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = '從一個數字開始取數，到另一個數結束。\n' +
        '每取一個數，都將其值賦值予\n' +
        '變數 "%1"，並執行程式方塊。';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = '對於任意';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = '數字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = '範圍從';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = '到';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = '每次增加';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = '對一定範圍內的數字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = '對於 ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' 範圍內的';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = '按指定範圍和增量依序取值';
	+ '每次循環均將數值賦予指定變數，'
	+ '並執行「執行」區段中的程式方塊';

    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = '對於任意';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = '清單項目';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = '清單';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = '對清單中每一項';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = '對於 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' 清單中的';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = '針對清單中的每一項執行「對於任意」區域所包含的程式方塊，'
    + ' 採用指定變數名來引用目前的清單項。';

    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = '迴圈';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = '中斷';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = '執行下一個周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = '中斷內部迴圈';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = '跳過本迴圈的其餘部分，並且\n' +
    '進入下一次迴圈周期。';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = '警告：\n' +
    '本程式方塊只能於\n' +
    '循序(Flow)程式方塊中。';

    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = '當';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '滿足條件';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = '執行';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = '滿足條件';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = '當檢查條件結果為真時，執行「當」內的程式方塊。';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = '如果'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = '則';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = '否則';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = '如果';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = '當檢查條件結果為真時，' +
      '回傳「則」後的程式方塊結果；' +
      '否則的話回傳「否則」後的程式方塊結果；' +
      '一般狀況下每次只會回傳一個結果。';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = '執行方塊';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = '回傳結果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = '執行「執行方塊」內的程式方塊並傳回一個結果，用在設定值前插入執行某個程式方塊。';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = '執行方塊/傳回結果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = '執行並回傳';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = '求值但忽略結果'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = '求值但不回傳';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = '執行所連接的程式方塊但不傳回運算值，用於呼叫求值程序但不需要其運算值。';

    /* [lyn 13/10/14] Removed for now. May come back some day.
    Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = '空值';
    Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = '/reference/blocks/control.html#nothing';
    Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = ' 回傳空值。可用於初始化變數或插入到回傳槽中（如果沒有值需要回傳，相當於為空）。';
    */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = '開啟另一螢幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = '螢幕名稱';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = '開啟螢幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = '在多重畫面應用中開啟其他螢幕。';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = '開啟其他畫面並傳值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = '螢幕名稱';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = '開啟螢幕並傳值'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = '在多重畫面應用中開啟其他螢幕，並'
    + '傳送初始值過去。';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = '取得初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = '螢幕名稱';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = '取得初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = '畫面開啟時取得其他螢幕傳過來的內容，'
    + '此螢幕通常是由具備多個螢幕之App的另一個螢幕開啟。如沒有內容傳過來，'
    + '則回傳空字串。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = '關閉螢幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = '關閉螢幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = '關閉目前螢幕';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = '關閉螢幕並回傳值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = '回傳值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = '關閉螢幕並回傳值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = ' 關閉目前螢幕並將值傳送給新的螢幕。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = '退出程式';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = '退出程式';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = '關閉所有螢幕並終止程式。';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = '取得初始文字';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = '取得初始文字';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = ' 當本螢幕被其他app啟動時取來所傳來的文字，'
    + '如沒有內容傳入，則回傳空字串。'
    + '對於多螢幕的app，通常使用取得初始值的方式而非取得純文字(plain text)值。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = '關閉螢幕並回傳文字';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = '關閉螢幕並回傳文字';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = '關閉目前螢幕並將純文字傳送給新的螢幕。'
    + '對於多螢幕的app，通常使用關閉螢幕並送出值(close screen with value)這個指令，'
    + '而非單純回傳文字。';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = '邏輯';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = '判斷兩者是否相等，\n' +
    '對象可為任意類型，不限於數字。\n' +
	'判斷數字是否相等的依據是它們的字串形式是否相等。' +
	'例如：數字0等同於字串「0」；' +
	'代表數字的字串當它們代表的數字相等時，判斷結果也相等，\n' +
	'例如「1」等於「01」';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = '判斷兩者是否互相等，對象可為任意類型，不限於數字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = '邏輯相等';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_AND = '與';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = '或';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = '如所有輸入項皆為真，則回傳真值。';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = '只要任一輸入項為真，則回傳真值。';

    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = '非';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = '如輸入項為假，則回傳真值，\n' +
    '如輸入項為真則回傳假值。';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = '真';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = '假';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = '回傳真值';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = '回傳假值';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = '數學';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = '回傳所顯示的數字 ';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = '數字';

    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = '如兩個數字相等，則回傳真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = '如兩個數字不等，則回傳真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = '如第一個數字小於第二個數字，\n' +
    '則回傳真值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = '如第一個數字小於或等於第二個數字，\n' +
    '則回傳真值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = '如第一個數字大於第二個數字，\n' +
    '則回傳真值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = '如果第一個數大於或等於第二個數，\n' +
    '則回傳真值。';
    Blockly.Msg.LANG_MATH_COMPARE_EQ = '=';
    Blockly.Msg.LANG_MATH_COMPARE_NEQ = '\u2260';
    Blockly.Msg.LANG_MATH_COMPARE_LT = '<';
    Blockly.Msg.LANG_MATH_COMPARE_LTE = '\u2264';
    Blockly.Msg.LANG_MATH_COMPARE_GT = '>';
    Blockly.Msg.LANG_MATH_COMPARE_GTE = '\u2265';

    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = '回傳兩數之和';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = '回傳兩數之差';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = '回傳兩數相乘之計算結果';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = '回傳兩數相除之計算結果';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = '回傳第一個數的第二個數次方之計算結果';

    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = '改變';
    Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = '專案';
    Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = '由';
    Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = '變數%1增加一個值"。';*/

    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = '平方根';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = '絕對值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = '相反數';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'ln';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = '回傳x的平方根。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = '回傳x的絕對值。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = '回傳x的相反數。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = '回傳ln(x)。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = '回傳e^x。';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = '回傳10的指定次方之計算結果'; */

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = '進位或捨去來取整數';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = '回傳大於等於輸入項的最小整數(無條件進入)';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = '回傳小於等於輸入項的最大整數(無條件捨去)';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = '四捨五入';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = '進位後取整數';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = '捨去後取整數';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = '求正弦值（x單位為度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = '求餘弦值（x單位為度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = '求正切值（x單位為度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = '求反正弦值（x單位為度,範圍(-90,+90]）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = '求反餘弦值（x單位為度,範圍[0, 180)）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = '求反正切值（x單位為度,範圍(-90, +90)）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = '求y/x反正切值（x單位為度,範圍(-180, +180]）';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = '最小值';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = '最大值';

    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = '回傳最小值。';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = '回傳最大值。';

    Blockly.Msg.LANG_MATH_DIVIDE = '除以';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = '模數';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = '餘數';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = '商數';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = '回傳a/b的模數。';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = '回傳a/b的餘數。';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = '回傳a/b的商數。';

    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = '隨機整數';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = '範圍從';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = '隨機整數從 %1 到 %2 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = '回傳指定範圍內的隨機整數，\n' +
    '範圍限於2^30之內。';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = '隨機小數';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = '回傳0和1之間的隨機小數值。';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = '設定隨機數種子';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = '為';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = '為隨機數產成器指定種子。';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = '角度<——>弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = '弧度轉為角度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = '角度轉為弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = '回傳輸入弧度對應的角度值[0, 360)。';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = '回傳輸入角度對應的弧度值[-\u03C0, +\u03C0)。';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = '求小數值';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = '數字';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = '位數';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = '將數字 %1設為小數形式 位數 %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = '以指定位數回傳該數值的小數形式。';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '是否為數字?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = '判斷該對象是否為數字。';

    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = '是否為10進位數?';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = '判斷該對象是否為10進位數。';

    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = '是否為16進位?';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = '判斷該對象是否為16進位數。';

    // Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = '是否為2進位?';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = '判斷該對象是否為2進位數。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = '進位轉換';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = '10進位轉16進位';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = '回傳輸入10進位數的16進位形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = '16進位轉10進位';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = '回傳輸入16進位數的10進位形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = '10進位轉2進位';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = '回傳輸入10進位數的2進位形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = '2進位轉10進位';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = '回傳輸入2進位數的10進位形式。';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = '文字';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = '輸入文字內容';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = '建立文字';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = '合併所有輸入項為同一個文字，\n'
    + '如沒有輸入項，則生成空文字。';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = '合併文字';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = '文字';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_TO = '到';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = '追加文字';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = '變數';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = '將文字追加到原有文字 "%1"之後。';

    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = '求長度';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = '回傳該文字的字元數(包括空格)。';

    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = '是否為空';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = '如文字長度為0則回傳真，否則回傳假。';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = '文字比較';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = '判斷左邊文字的首字母順序是否低於右邊文字，\n'
    + '如果text1與text2開頭部分相同，則長度較短的字串為較小值，\n'
    + '大寫字元順序優於小寫字元，例如 A<a。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = '判斷文字內容是否相同，即：\n'
    + '是否由同一組相同順序的字元組成，與一般所謂相等概念不同之處再於，\n'
    + '當文字內容為數字時，如123和0123，儘管數值相等，\n'
    + '但文字判斷結果不相等。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = '判斷左邊文字的字首順序是否高於右邊文字，\n'
    + '如首字母相同，則長度較短的文字順序較高，\n'
    + '大寫字元順序優於小寫字元。(以ASCII順序比較)';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP = "用法如一般的文字常數。模糊文字的不同之處在於：\n"
    + "模糊後的文字後續不容易藉由解析APK檔來取得內容。模糊文字通常是在儲存機密內容（例如API密鑰）\n"
    + "時使用。\n"
    + "警告：對於專家來說，模糊文字的安全性還是非常低。";
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE = '模糊文字';

    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = '文字字母數';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = '回傳文字指定開頭和結尾間的字元數。';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = '開頭';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = '結尾';*/

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = '查找';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = '查找目標';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = '待查找文字';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = '回傳查找目標在待查找字串中第一次/最後一次出現的下標\n' +
     '如果未發現查找目標，回傳0。';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = '第一次';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = '最後一次';*/

    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = '字元下標';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = '文字';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = '回傳指定下標的字元。';*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = '大寫';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = '小寫';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = '回傳轉為大寫的文字。';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = '回傳轉為大寫的文字。';

    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = '刪除空格';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = '回傳刪除首尾處空格後的文字';

    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = '字串位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = '字串';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = '求字串%2在文字%1中的起始位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = '求字串在文字中的起始位置，\n'
    + '其中1表示文字的起始處，\n '
    + '如找不到該字串則傳回0。';

    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = '包含字串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = '字串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = '檢查文字%1中是否包含字串%2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = '檢查文字中是否包含該字串';

    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = '分隔符號';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = '分隔符號(清單)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = '分解首項';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = '分解任意首項';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = '分解';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = '任意分解';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '在首次出現分隔符號的位置將指定文字分解為兩部分，\n'
    + '並回傳包含分隔點前和分隔點後兩部分內容的清單，\n'
    + '如分解字元串"蘋果,香蕉,櫻桃,西瓜"，以逗號作為分隔符號，\n'
    + '將傳回一個包含兩個元素的清單，其中第一個元素為"蘋果"，第二個元素則為\n'
    + '"香蕉,櫻桃,西瓜"。\n'
    + '注意，"蘋果"後面的逗號不會出現，\n'
    + '因為它就是分隔符號。';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = '以文字中的任意項作為分隔符號，\n'
    + '在首次出現分隔符號的位置將給定文字分解為一個二元素清單。\n\n'
    + '如以"(香蕉,蘋)"作為分隔符號分解"我喜歡蘋果香蕉蘋果葡萄"這段文字，\n'
    + '將回傳一個包含兩個元素的清單，第一個元素為"我喜歡"，第二個元素為\n'
    + '"果香蕉蘋果葡萄"';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = '以指定內容作為分隔符號來分解文字，並回傳包含分解後結果的清單。\n'
    + ' 如以","(逗號)分解"一,二,三,四"，將傳回清單"(一 二 三 四)"，\n'
    +' 而以"-土豆"作為分隔符號分解字串"一-土豆,二-土豆,三-土豆,四"，則傳回清單"(一 二 三 四)"。'
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = '以分隔符號清單中的任意一項作為分隔符號，將指定文字分解為清單，\n'
    + '並回傳一個包含處理結果的清單。\n'
    + '如分解"藍莓,香蕉,草莓,西瓜"，以一個含兩元素的清單作為分隔符號，\n'
    + '其中第一項為逗號，第二項為"莓"，則回傳清單：\n'
    + '"(藍 香蕉 草 西瓜)"'

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = '輸出';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = '輸出文字，數字或指定值。';

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = '提示';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = '信息';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = '提示用戶輸入特定的文字。';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = '文字';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = '指定數字';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = '用空格分解';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = '以空格作為分隔符號，將文字分解為若干部分。';

    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = '提取字串';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = '提取位置';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = '提取長度';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = '從文字%1第%2位置提取長度為%3的字串';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = '以指定長度、指定位置從指定文字中提取文字片段，\n'
    + '位置1表示被提取文字的起始處。';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = '取代項';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = '原始文字';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = '全部取代';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = '取代為';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = '將文字%1中所有%2全部取代為%3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = '傳回一段新的文字，其中所包含的取代項內容\n'
    + '均已被取代為指定字串。';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = '清單 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = '建立空清單';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = '建立一個空的清單';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = '建立清單';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = '建立一個可包含任意項數的清單';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = '清單';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = '編輯該清單，可增加、刪除或重新排列。';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = '清單項';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = '增加一個清單項。';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = '清單項';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = '增加一個清單項。';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = '選擇清單項';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = '選擇清單%1中索引值為%2的清單項';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = '回傳指定索引值的清單項';

    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '對象是否在清單中?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = '檢查清單%2中是否含對象%1'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = '如該對象為清單中某一項則回傳真值，'
    + '否則為假。';

    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = '清單項索引值';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = '求對象%1在清單%2中的位置';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = '求對象在該清單中的位置，'
    + '如不在該清單中，則回傳0。';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = '隨機選取清單項';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = '從清單中隨機選取一項';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = '取代清單項';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = '取代為';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = '將清單%1中索引值為%2的清單項取代為%3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = '取代清單中第n項內容';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = '刪除清單項';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = '刪除清單%1中第%2項';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = '刪除指定索引值的清單項';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = '建立擁有專案清單';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = '重複';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = '次數';
    Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = '建立一個擁有給定專案的清單\n' +
    '並且重複給定的次數';*/

    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = '求清單長度';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = '計算清單%1的長度';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = '計算清單項數';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = '附加清單';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = '清單1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = '清單2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = '將清單%2中的所有項附加到清單%1中';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = '將清單2中所有項附加到清單1的末尾。附加後，'
    + '清單1中將包括所有新加入的元素，而清單2則不發生變化。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = '增加清單項目';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = '清單項目';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = '將清單項目%2加入清單%1中';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = '在清單末尾增加清單項目';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = '清單';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = '編輯該清單，增加、刪除或重新排列';

    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = '複製清單';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = '複製清單，包括其中所有的子清單。';

    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '對象是否為清單? ';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = '對象';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = '判斷該對象是否為清單類型。';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = '清單轉CSV格式';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = '將清單轉換為表格中的一列資料，'
    + '並回傳表示行資料的CSV（逗號分隔數值）字元串，資料中的每一項皆視為一個欄位，'
    + '在CSV格式文字中以雙引號來標示，'
    + '各資料項以逗號分隔，且每行末尾'
    + '均不帶換行符號。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'CSV列轉清單';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = '文字';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = '將文字按CSV格式進行解析，'
    + '生成一個包含各欄位資料的清單。對於CSV格式文字而言，欄位中如果有未轉譯的換行符號將導致錯誤'
    + '（在有多行欄位的情況下），而只在整行文字的末端才出現換行符或CRLF則是正確的。';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = '清單轉CSV表格';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = '將清單轉換為帶標題行的表格形式，'
    + '且回傳代表該表格的CSV（逗號分隔數值）字元串，清單中的每一項本身'
    + '還可以作為表示CSV表格行的清單，清單行中的每一項'
    + '都可看成是一個欄位，在CSV字元串中以雙引號方式進行標識。'
    + '在回傳的CSV文字中，資料中的各項皆是以逗號分隔，'
    + '而各列則以CRLF \(\\r\\n\)進行分隔。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'CSV表格轉清單';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'CSV文字';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = '對CSV（逗號分隔數值）格式的文字進行解析，'
    + '並產生一筆一筆的記錄，其中的每一欄位都是一個清單，'
    + '各記錄間分別以換行符號\(\\n\)或CRLF \(\\r\\n\)方式分隔。';

    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = '插入清單項';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = '插入項';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = '在清單%1的第%2項處插入清單項%3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = '在指定索引值(位置)處插入清單項。';

    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '清單是否為空?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = '清單';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = '如果清單為空則回傳真。';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = '鍵值對查詢';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = '關鍵字';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = '鍵值對';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = '查詢無果';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = '在鍵值對%2中查找關鍵字%1，如未找到則回傳%3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = '回傳鍵值對清單中與關鍵字關聯的值';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = '查詢';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = '專案';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = '待查詢清單';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = '回傳的索引的第一個/最後一個 occurrence\n' +
    '的專案這個清單 \n' +
    '如果找不到文字則回傳。';
    Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = '第一次';
    Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = '最後一次';

    Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = '值的索引為';
    Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = '在清單中';
    Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = '回傳清單中特定位置的值。';
    Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = '被修改的項的索引;
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = '在清單中';
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = '修改為';
    Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = '修改清單中制定位置的值。';*/

// Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = '初始化全域變數';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = '變數名';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = '為';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = '全域變數';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = '建立全域變數，並透過後方的程式方塊來設定初始值';

    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = '取';
    // Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = '專案';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = '取得變數值';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = '回傳變數的值。';

    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = '設置';
    // Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = '專案';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = '為';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = '設變數值';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = '設變數值等於輸入項';
    Blockly.Msg.LANG_VARIABLES_VARIABLE = '變數';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = '初始化區域變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = '變數名';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = '為';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = '作用範圍';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = '區域變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '建立指定範圍內程式方塊所使用的變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = '初始化區域變數';

    // These don't differ between the statement and expression
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = '初始化區域變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = '變數名';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = '為';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = '作用範圍';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = '初始化區域變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '建立指定範圍內程式方塊所使用的變數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = '初始化區域變數';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = '區域變數名稱';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = '參數';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = '定義程序';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = '程序名';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = '執行';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = '定義程序';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = '執行完成後不回傳結果。';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = '然後回傳';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = '執行';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = '回傳';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = '“執行”其中包含的方塊並傳回一條敘述，可以在程序執行前將傳回賦值給相關變數';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = '執行/回傳';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = '定義程序';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = '回傳';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = '定義程序';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = '執行完成後回傳結果。';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = '警告:\n' +
    '此程序的輸入項\n' +
    '出現重複';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = '呼叫';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = '程序';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = '呼叫';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = '呼叫一個無回傳值的程序。';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = '呼叫無回傳值的程序';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = '呼叫';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = '呼叫一個有回傳值的程序。';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = '呼叫有回傳值的程序';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = '輸入項';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = '輸入:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = '反白標示程序';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP = "該程式方塊未定義，刪除此程式方塊!";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = '當';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = '執行';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = '呼叫';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = '呼叫';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = '元件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = '元件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = '設';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = '為';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = '設';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = '為';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = '元件';

//Misc
    Blockly.Msg.SHOW_WARNINGS = "顯示警告";
    Blockly.Msg.HIDE_WARNINGS = "隱藏警告";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "程式方塊沒有連接好";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "本程式方塊應該與某個事件或程序連接";

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION ="AI Companion出現錯誤";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "網路連線故障";
    Blockly.Msg.REPL_NETWORK_ERROR = "網路故障";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART ="與AI Companion通訊故障，<br />請嘗試重啟AI Companion程序並重新連接";
    Blockly.Msg.REPL_OK = "確定";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK ="檢查AI Companion程式版本";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'AI Companion程式已過期，點選「確定」升級。';
    Blockly.Msg.REPL_EMULATORS = "查看模擬器";
    Blockly.Msg.REPL_DEVICES = "裝置";
    Blockly.Msg.REPL_APPROVE_UPDATE = "系統將詢問您是否允許更新。";
    Blockly.Msg.REPL_NOT_NOW = "現在不";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 ="您使用的AI Companion程式已經過期，<br/><br/>本版App Inventor適用的AI Companion程式版本為";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE ="您正在使用一個過期版本的AI Companion程式，請盡快升級";
    Blockly.Msg.REPL_COMPANION_WRONG_PACKAGE = "您現在使用的AI Companion是針對不同版本的App Inventor建立的。請前往功能表的「求助——>AI Companion訊息」獲取正確的AI Companion版本。";
    Blockly.Msg.REPL_DISMISS = "放棄";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "軟體升級";
    Blockly.Msg.REPL_OK_LOWER = "確定";
    Blockly.Msg.REPL_GOT_IT = "升級完成";
    Blockly.Msg.REPL_UPDATE_INFO = '正在安裝更新套件，請在裝置（或模擬器）上檢查確認。<br /><br />注意：升級完成後，請選擇“完成”（不要選開啟）。然後在瀏覽器中開啟並進入App Inventor，點選“連接裝置”並選擇“重置連線”項。';

    Blockly.Msg.REPL_UPDATE_NO_UPDATE = "無可用更新";
    Blockly.Msg.REPL_UPDATE_NO_CONNECTION = "請先與AI Companion連線才能開始更新";
    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "無法將升級套件發送給裝置或模擬器";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "無法從App Inventor伺服器下載更新套件";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "無法從App Inventor伺服器取得更新信息(伺服器無回應)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "正在從App Inventor伺服器下載更新套件，請耐心等待。";
    Blockly.Msg.REPL_RUNTIME_ERROR = "執行錯誤";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS = "<br/><i>注意：</i>&nbsp;5秒鐘之內不會再次顯示錯誤訊息。";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "正在通過USB傳輸線連接";
    Blockly.Msg.REPL_STARTING_EMULATOR = "正在啟動Android模擬器<br/>請等待：可能需要一至兩分鐘";
    Blockly.Msg.REPL_CONNECTING = "連接中...";
    Blockly.Msg.REPL_CANCEL = "取消";
    Blockly.Msg.REPL_GIVE_UP = "放棄";
    Blockly.Msg.REPL_KEEP_TRYING = "重試";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "連接失敗";
    Blockly.Msg.REPL_NO_START_EMULATOR ="無法在模擬器中啟動AI Companion程式";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "是否已插入USB線？";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE ="AI2沒有偵測到裝置，請確認傳輸線連接以及驅動程式安裝是否正常。";
    Blockly.Msg.REPL_HELPER_Q = "是否已執行aiStarter程式？";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'aiStarter程式不在執行狀態中，<br />是否需要<a href="http://appinventor.mit.edu" target="_blank">說明?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB已連接，請等待";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = "秒，確保相關資源全部載入。";
    Blockly.Msg.REPL_EMULATOR_STARTED = "模擬器已啟動，請等待";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE ="正在所連接裝置中啟動AI Companion程式";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR ="正在模擬器中啟動AI Companion程式";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING ="AI Companion程式啟動中，請等待";
    Blockly.Msg.REPL_VERIFYING_COMPANION ="檢查AI Companion程式啟動狀態....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION ="連接AI Companion程式";
    Blockly.Msg.REPL_TRY_AGAIN1 ="無法連接AI Companion程式，請再試一次。";
    Blockly.Msg.REPL_YOUR_CODE_IS ="編碼為：";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "您真的要這麼做嗎？";
    Blockly.Msg.REPL_FACTORY_RESET = "這將使模擬器重置為出廠模式，如果此前升級過AI Companion程式，則需要重新升級。";

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "確定要刪除 %1 個這些程式方塊嗎?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "產生Yail碼";
    Blockly.Msg.DO_IT = "執行";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "清除錯誤";
    Blockly.Msg.CAN_NOT_DO_IT = "無法執行";
    Blockly.Msg.CONNECT_TO_DO_IT = '請先連接AI Companion或者模擬器才能使用"執行"功能';

// Clock Component Menu Items
    Blockly.Msg.TIME_YEARS = "年";
    Blockly.Msg.TIME_MONTHS = "月";
    Blockly.Msg.TIME_WEEKS = "周";
    Blockly.Msg.TIME_DAYS = "日";
    Blockly.Msg.TIME_HOURS = "時";
    Blockly.Msg.TIME_MINUTES = "分";
    Blockly.Msg.TIME_SECONDS = "秒";
    Blockly.Msg.TIME_DURATION = "持續時間";
  }
};

// Initalize language definition to English
Blockly.Msg.zh.hans.switch_blockly_language_to_zh_hans.init();
Blockly.Msg.zh.switch_language_to_chinese_tw.init();
