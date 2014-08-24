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
 * @fileoverview Traditional English strings.
 * @author mckinney@gmail.com (Andrew F. McKinney)
 */
'use strict';

goog.provide('Blockly.Msg.zh_cn');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.zh_cn.switch_language_to_chinese_cn = {
  // Switch language to Chinese (Taiwan).
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = '复制';
    Blockly.Msg.REMOVE_COMMENT = '去除注解';
    Blockly.Msg.ADD_COMMENT = '加注解';
    Blockly.Msg.EXTERNAL_INPUTS = '多行输入';
    Blockly.Msg.INLINE_INPUTS = '单行输入';
    Blockly.Msg.HORIZONTAL_PARAMETERS = '横式形参';
    Blockly.Msg.VERTICAL_PARAMETERS = '竖式形参';
    Blockly.Msg.DELETE_BLOCK = '删除积木';
    Blockly.Msg.DELETE_X_BLOCKS = '删除 %1 块积木';
    Blockly.Msg.COLLAPSE_BLOCK = '折迭积木';
    Blockly.Msg.EXPAND_BLOCK = '展开积木';
    Blockly.Msg.DISABLE_BLOCK = '禁用积木';
    Blockly.Msg.ENABLE_BLOCK = '启用积木';
    Blockly.Msg.HELP = '说明';
    Blockly.Msg.COLLAPSE_ALL = '折迭所有积木';
    Blockly.Msg.EXPAND_ALL = '展开所有积木';
    Blockly.Msg.ARRANGE_H = '横式积木';
    Blockly.Msg.ARRANGE_V = '竖式积木';
    Blockly.Msg.ARRANGE_S = '斜式积木';
    Blockly.Msg.SORT_W = '以宽度分类';
    Blockly.Msg.SORT_H = '以高度分类';
    Blockly.Msg.SORT_C = '以类型分类';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = '修改值：';
    Blockly.MSG_NEW_VARIABLE = '新变量。。。';
    Blockly.MSG_NEW_VARIABLE_TITLE = '新变量名称：';
    Blockly.MSG_RENAME_VARIABLE = '重新命名变量。。。';
    Blockly.MSG_RENAME_VARIABLE_TITLE = '将所有 “%1” 变量重新命名为。。。';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = '变量';
    Blockly.MSG_PROCEDURE_CATEGORY = '过程';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = '此积木不可出现在定义里';
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = '请从下拉式列表选择有效的项目。';
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = '此组件已有事件处理器。';

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = '点击方块以便选择颜色。';
    Blockly.Msg.LANG_COLOUR_BLACK = '黑色';
    Blockly.Msg.LANG_COLOUR_WHITE = '白色';
    Blockly.Msg.LANG_COLOUR_RED = '红色';
    Blockly.Msg.LANG_COLOUR_PINK = '粉色';
    Blockly.Msg.LANG_COLOUR_ORANGE = '橙色';
    Blockly.Msg.LANG_COLOUR_YELLOW = '黄色';
    Blockly.Msg.LANG_COLOUR_GREEN = '绿色';
    Blockly.Msg.LANG_COLOUR_CYAN = '青色';
    Blockly.Msg.LANG_COLOUR_BLUE = '蓝色';
    Blockly.Msg.LANG_COLOUR_MAGENTA = '紫红';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = '浅灰色';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = '深灰色';
    Blockly.Msg.LANG_COLOUR_GRAY = '灰色';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = '分裂颜色';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = '返回值是含有红，绿，蓝，和透明度数（0-255）的列表.';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = '制造颜色';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = '返回值是根据参数制造的颜色。';

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = '语句';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = '如果真值，执行语句。';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = '如果真值，执行第一位语句。\n' +
        '否则，执行第二位语句。';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = '如果第一位是真值，执行第一位语句。\n' +
        '否色如果第二位是真值，执行第二位语句。';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = '如果第一位是真值，执行第一位语句。\n' +
        '否色如果第二位是真值，执行第二位语句。\n' +
        '在否则，执行第三位语句。';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = '如';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = '不然';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = '否则';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = '执行';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = '如';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = '添加, 消除, 或重排部件们\n' +
        '以致重新设置积木.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = '不然';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = '为判别式积木添加条件。';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = '否则';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = '为判别式积木添加总受条件。';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = '重复';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = '只要';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = '直至';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = '只要真值，执行语句。';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = '只要假值，执行语句。';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = '只要测试为真值，执行“执行”部件里的积木。';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = '迭代器为';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = '从';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = '至';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = '执行';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = '从起点数迭代至结尾数。\n' +
        '每次回圈，设迭代为\n' +
        '变量 “%1”, 和执行语句。';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = '取每个';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = '数字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = '从';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = '至';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = '由';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = '迭代范围';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = '取 ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' 范围';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = '每次回圈执行”执行“部件里的积木并增量变量。变量代表当前的数字';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = '为每个';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = '项目';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = '在列表中 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = '做';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = ' 列表中的项目 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = '为';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = '在列表中';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = '运行在 \'do\' 块部分中的每项 '
    + ' 列表。使用给定的变量名称来引用当前列表项.';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = '的循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = '爆发';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = '继续下一次迭代与';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = '打破包含它的循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = '跳过其余的这种循环，和 \n' +
    '继续下一个迭代。';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = '警告： \n' +
    '此块可能 only\n' +
    '用于在一个循环内';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = '而';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '测试 ';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = '做';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = '而';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = '\'do\' 在运行块 部分，测试时 '
    + '真。';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = '如果'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = '然后';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = '其他';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = '如果';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = '如果被测试的条件为 真，' +
      '返回附加到 \'then-return\'，表达式的计算结果 的插槽 ' +
      '否则返回附加到 \'else-return\'，表达式的计算结果 的插槽 ' +
      '在大多数返回插槽表达式之一将会评估。';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = '做';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = '结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = '\'do\' 在运行块 并返回语句。有用的如果您需要返回一个值给一个变量之前运行一个程序.';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = '做/结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = '做导致';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = '评价但忽略结果'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'eval，但忽略';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = ' 运行连接的代码块，并忽略返回值 （如果有）。有用如果需要调用带有返回值，但不要不需要值的过程。 ';

    /* [林恩 13/10/14] 现在删除。可能回来的某一天。
    Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = '什么';
    Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
    Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = ' 返回 nothing。用来初始化变量或可以插入到返回的插槽中，如果没有价值需要返回。这是相当于为空或没有.';
    */
    
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = '打开另一个屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = '的屏显名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = '打开屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = '将打开一个新的屏幕在应用程序中多个屏幕';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = '打开另一个屏幕与起始值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = '的屏显名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = '开始价值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = '打开屏幕与价值'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = ' 在多个屏幕应用程序中打开一个新的屏幕，并将传递 '
    + '开始对该屏幕的价值 ';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = '得到的起始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = '的屏显名称';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'startValue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = '得到的起始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = ' 返回的值被传递给这屏幕时它 '
    + ' 被打开，通常由一个多屏幕的应用程序在另一个屏幕。如果没有价值了 '
    + ' 传递，返回空文本。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = '关闭当前屏幕';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = '关闭屏幕与价值 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = '结果';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = '关闭屏幕与价值 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = ' 关闭当前屏幕，并返回一个结果到 '
    + '屏幕，打开了这一个 ';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = '关闭应用程序 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = '关闭应用程序 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = '关闭所有屏幕在此应用程序，停止该应用程序。';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = '得到平原开始文本';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = '得到平原开始文本';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = ' 返回纯文本传递给这屏幕时 '
    + ' 就开始于另一个应用程序。如果传递了没有价值，返回空文本。为 '
    + '多个屏幕的应用程序，使用 get 起始值而不是得到平原开始文本。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = '关闭屏幕与纯文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = '关闭屏幕与纯文本 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = '关闭当前屏幕和应用程序返回的文本'
    + ' 开了这一个。对于多个屏幕的应用程序，关闭屏幕使用价值而不是 '
    + '关闭屏幕与纯文本';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = '逻辑 ';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality _(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = ' 测试是否两个都是平等的。\n' +
    '被比较的东西可以是任何东西，不只是数字';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = '返回 真 如果两个输入都不彼此相等。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = '逻辑平等 ';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = '和';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = '或';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = '返回 真 如果所有输入都都真实。';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = '返回 真，则任何输入是真实';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = '不';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = '返回 真 如果输入是假 \n' +
    '如果返回 假 的输入是真实';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = '真 ';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = '假 ';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = '返回布尔值 真';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = '返回布尔值 假';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = '数学';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = '报告显示的数字 ';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = '数字';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = '返回 真 如果两个数字都彼此相等';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = '返回 真 如果两个数字都不彼此相等。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = '返回 真，则第一个数字是 smaller\n' +
    ' 比第二个数字';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = '返回 真，则第一个数字是 smaller\n' +
    '或等于第二个数字';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = '返回 真，则第一个数字是 greater\n' +
    ' 比第二个数字';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = '返回 真，则第一个数字是 greater\n' +
    '或等于第二个数字';
    Blockly.Msg.LANG_MATH_COMPARE_EQ = '=';
    Blockly.Msg.LANG_MATH_COMPARE_NEQ = '\u2260';
    Blockly.Msg.LANG_MATH_COMPARE_LT = ' <';
    Blockly.Msg.LANG_MATH_COMPARE_LTE = '\u2264';
    Blockly.Msg.LANG_MATH_COMPARE_GT = ' >';
    Blockly.Msg.LANG_MATH_COMPARE_GTE = '\u2265';

    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_ADD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#add';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MINUS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#subtract';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#multiply';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#divide';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_POWER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#exponent';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = '返回两个数字的总和 ';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = '返回两个数字的区别';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = '返回两个数字的乘积';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = '返回两个数字的商';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = '返回的第一个数字提出 \n' +
    '力量的第二个数字';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = ' *';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = ' ^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = '改变';
    Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = '项目';
    Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = '由';
    Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = '添加号码到变量"%1"。';*/


    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = '广场根';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = '绝对';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = '复';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = '登录 ';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = ' e ^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = '返回的数字的平方根';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = '返回数的绝对值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = '返回的一些否定';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = '返回一个数的自然对数';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = '返回 e 权的数量';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = '返回 10 数的力量'; */

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = '轮数向上或向下';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = '轮的输入，smallest\n' +
    '不是更少数量然后输入';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = '轮的输入，largest\n' +
    '不是更大的数量然后输入';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = '圆';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = '天花板';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = '楼';

    Blockly.Msg.LANG_MATH_TRIG_SIN = '罪恶';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = '谭';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = '阿信';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = ' 很小的时辰';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = '提供以度为单位给定角度的正弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = '提供以度为单位给定角度的余弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = '提供以度为单位给定角度的正切值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = ' 提供的角度范围内 （-90、 + 90] \n' +
    '度与给定的正弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = ' 提供的角度范围内 [0, 180) \n' +
    '度与给定的余弦值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = '提供的角度范围 （-90、 + 90） \n' +
    '度与给定的正切值';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = ' 提供的角度范围内 （-180，+ 180] \n' +
    '度与给定矩形坐标';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = '民';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = '最大';
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

    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = '模的';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = '的其余部分 ';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = '的商 ';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = ' 返回模.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = '返回余数';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = '返回商数';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = '随机整数 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = '从';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = '随机整数从 %1 到 %2 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = '返回一个随机整数之间上部 bound\n' +
    ' 和的下限。将剪切边界是 smaller\n' +
    ' 比 2 * * 30';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = '随机分数';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = '返回一个随机数字 0 和 1 之间';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = '随机集的种子';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = '指定数值的 seed\n' +
    ' 为随机数字生成器';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = '转换 ';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = '弧度表示度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = '度为弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = '回报程度值在 range\n' +
    ' [0, 360) 对应于其弧度参数.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = '返回弧度值在 range\n' +
    ' [-\u03C0，+ \u03C0） 对应于其度参数.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = '的格式设置为十进制数';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = '数字';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = '地方';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = '设置格式作为十进制数 %1 %2 ';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = '返回格式化的数字为 decimal\n' +
    ' 与指定的数量的地方';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '是一个数字吗?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = '测试是否有一些东西是一个数字。';
    
// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = '文本字符串';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = '创建带有文本 ';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = '追加所有的投入，形成一个单一的文本字符串 \n'
    + '如果不有任何输入，使空文本。';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = '加入';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = '字符串';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = '到';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = '追加文本';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = '项目';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = ' 一些将文本追加到变量"%1"。';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = '长度 ';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = '返回数字的字母 （包括空格） \n' +
    ' 在提供的文本';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = '是空';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = '返回 真 如果长度的 \n' + '文本是 0，虚假否则。';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = '=';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = '比较文本';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = '测试是否 text1 是字典序小于 text2.\n'
    + '如果一个文本是其他，较短的文本 is\n 前缀'
    + ' 被认为较小。大写字符前面的小写字符.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = ' 测试文本字符串是否完全相同，即.，有 same\n'
    + ' 中的相同顺序的字符。这是不同于普通 = \n'
    + ' 中的情况下文本字符串号码： 123 和 0123年 = \n'
    + ' 而不是文本 =.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = '报告是否 text1 是字典序大于 text2.\n'
    + ' 如果一个文本的其他的前缀，较短的文本被认为是较小.\n'
    + '大写字符前面的小写字符。';

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
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = '大写';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = '小写';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = '返回其文本字符串参数的副本转换为大写';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = '返回其文本字符串参数的副本转换为小写字母';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = '剪 ';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = '返回其文本的副本的字符串参数与 任何\n'
    + '前导或尾随空格删除。';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = '开始';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = '片';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = '开始在 %1 的文本片断 %2';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = '返回文本 \n 中的片断的起始索引'
    + '，其中索引为 1 表示文本的开头。如果返回 0 \n '
    + '片不是在文本中';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = '包含';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = '片';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = '包含一块文本 %1 %2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = '测试是否在文本中包含片断';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = '在';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = '在 （列表）';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = '在第一次分裂';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = '在任何第一次分裂';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = '分裂';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = '在任何分裂';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '分为两块使用 \n 第一次出现的位置给定的文本'
    + '文本 \'at\' 作为划分点，并返回一个两个项目的列表组成的一块 \n'
    + ' 之前的分界点和后的分界点的片断。\n'
    + '分裂"苹果，香蕉，樱桃，变的一文不值"用逗号作为噼裂点 \n'
    + ' 返回列表的两个项目： 第一是"苹果"的文本，第二个是文本 \n'
    + '"香蕉，樱桃，狗食"。\n'
    + ' 注意"苹果"后面的逗号并不出现在结果中，\n'
    + '因为这是的分界点';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = '将给定的文本分为两项列表中，使用的第一个位置的任何项目 \n'
    + '在列表 \'at\' 作为划分点。\n\n'
    + '分裂"我爱苹果香蕉苹果葡萄"的列表中"广管局 ap）"返回 \n'
    + '的两个项的列表，第一是"我爱"和第二个是 \n'
    + '"平差香蕉苹果葡萄"';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = '划分成块的文本使用文本 \'at\' 的分界点，产生的结果列表。\n'
    + ' 分裂"一、 二、 三、 四"at""，（逗号），返回的列表"（一个两个三个四）"。\n'
    + ' 分裂"一个马铃薯、 土豆两个、 三-马铃薯、 四个"在"-马铃薯"，返回的列表"（一个两个三四）"。 '
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY  = '将给定的文本列表，在列表中 \'at\' 中使用的任何项目分为'
    + ' 分界点，并返回结果的列表。\n'
    + '分裂"appleberry，香蕉，樱桃，变的一文不值"与 \'at\' 两个元素的列表作为其 \n'
    + ' 的第一个项目是一个逗号和其第二个项目是"懊悔"返回四个项目的列表： \n'
    + '"（applebe 香蕉车变的一文不值）"'
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
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = '分裂在空间';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = '将文本拆分成块由空格分隔。';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = '段';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = '启动';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = '长度 ';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = '段文本 %1 启动 %2 长度为 %3';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = '给定长度的线段提取给定 text\n'
    + ' 从给定的文本从给定位置开始开始。Position\n'
    + '1 表示文本的开头';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = '段';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = '替换所有';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = '替换 ';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = '替换所有文本 %1 %2 段替换 %3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = '返回一个新的文本代替所有 occurrences\n 所得到的'
    + '部分的更换';


    Blockly.Msg.LANG_CATEGORY_LISTS = '列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty _lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = '创建空的列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = '返回一个列表，包含了没有数据记录的长度为 0，';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = '，使一个列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = '创建一个列表与任意数量的项目';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = '添加、 删除或重新排列稿件重新配置此列表块';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = '项目';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = '添加项目到列表中 ';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = '项目';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = '添加项目到列表中 ';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = '选择列表项 ';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = '指标';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = '选择列表项列表 %1 索引 %2';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = '返回位置索引处的项的列表中';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '是在列表中?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = '事';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = ' 是在列表中吗？%1 的事情列表 %2 '
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = ' 返回 真 如果事情是列表中的项和 '
    + '假的如果不是。';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = '索引列表中';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = '事';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = '索引目录 %1 的事情列表 %2 中';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = ' 找到的东西在列表中的位置。如果它不在 '
    + '列表中，返回值 0';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = '挑选一个随机项';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = '挑选随机从列表中的项目';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = '替换列表项';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = '指标';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = '替换 ';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = '替换列表项列表 %1 的索引 %2 更换 %3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = '替换列表中的第 n 项';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = '删除列表项 ';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = '指标';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = '删除列表中的项列表 %1 索引 %2 ';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = '位于指定位置的项从列表中删除 ';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = '列表 ' 创建与项目 ';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = '重复';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = '时代';
    Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = '创建一个列表组成的给定 value\n' +
    '重复指定的次数的';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = '列表的长度 ';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = '列表列表 %1 的长度 ';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = '计算的列表中的项目数 ';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = '将追加到列表 ';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = '列表1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = '列表2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = '将追加到列表 列表1 %1 列表2 %2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = ' 追加上月底 列表1 列表2 中的所有项。后 '
    + '追加、 列表1 将包括这些额外的元素，但 列表2 将保持不变';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = '添加列表的项目 ';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = '添加项列表列表 %1 到 %2';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = '添加项目到列表的末尾。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = '添加、 删除或重新排列稿件重新配置此列表块';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = '副本列表中 ';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = '使列表的副本，包括复制所有子列表 ';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '是一个列表?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = '事';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = '测试如果东西是一个列表 ';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = '列表到 csv 行';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = '将列表解释为表中的行并返回一个 CSV'
    + ' \ 代表行 （以逗号分隔 value\） 文本。每个行列表中的项是 '
    + ' 被认为是一个字段，并引用用双引号中生成的 CSV 文本。'
    + ' 项由逗号分隔。返回的行文本没有在行分隔符 '
    + '结束';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = '名单从 csv 行';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = ' 分析作为一个 CSV 文本 \ （以逗号分隔 value\） 格式化 '
    + ' 行生产的字段的列表。它是一个错误的行文本包含转义 '
    + ' 里面的字段换行 \ (有效，多个 lines\）。这是一行文本到好是 '
    + '端在单个换行符或 CRLF。';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = '到 csv 表列出';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = ' 将列表解释为一个表中的行主要格式和 '
    + ' 返回一个 CSV \ （以逗号分隔 value\) 文本表示的表。中的每一项 '
    + ' 列表本身应代表行的 CSV 表的列表。行中的每个项 '
    + '列表中被认为是一个领域，和用双引号中生成 CSV 引述'
    + ' 文本。在返回的文本，在行中的项目之间用逗号分隔，行 '
    + '隔开 CRLF \(\\r\\n\)';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = '名单从 csv 表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = ' 分析作为一个 CSV 文本 \ （以逗号分隔 value\） 格式化 '
    + ' 表产生的行，其中每个是字段的列表的列表。行可以是 '
    + '由换行符 \(\\n\) 或分隔 CRLF \(\\r\\n\)';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = '插入列表项目 ';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = '指标';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = '插入列表项目列表 %1 的索引 %2 项目 %3 ';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = ' 一项插入列表中的指定位置';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '是列表为空?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = '返回 真 如果列表是空的';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = '查找成对';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = '关键 ';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = '对';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = '初一';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = '在查双键 %1 %2 初一成对 %3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = '回报与对的列表中的键关联的值 ';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = '找到';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = '的项目发生';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = '在列表中 ';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = '返回的索引的第一个/最后一个 occurrence\n' +
    '的项目这个列表 \n' +
    '返回 0，如果找不到文本。';
    Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = '第一';
    Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = '最后';
    
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
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = '初始化全局';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = '名字 ';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = '到';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = '全球';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = '创建一个全局变量，给它的附加块价值。';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = '得到 ';
    // Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = '得到 ';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = '返回此变量的值 ';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = '设置 ';
    // Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = '到';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = '设置 ';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = '设置此变量，以将等于输入';
    
    Blockly.Msg.LANG_VARIABLES_VARIABLE = ' 变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = '初始化本地';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = '名字 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = '到';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = '中';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = '本地 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = ' 允许您创建的变量，只在做可访问此块的一部分.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = '初始化本地在做';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    // 这些别不同之间的语句和表达式
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = '初始化本地';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = '名字 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = '到';
    
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = '中';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = '本地 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '允许您创建的变量，只可在此块的返回部分中访问。 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = '初始化本地的回报 ';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = '本地名称';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = '名字 ';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = '到';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = '程序 ';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = '做';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = '到';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = '过程不返回一个值';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = '结果';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = '做';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = '结果';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = '\'do\' 在运行块 并返回语句。有用的如果您需要返回一个值给一个变量之前运行一个程序.';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = '做/结果';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = '到';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = '结果';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = '到';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = '过程返回一个结果值';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = '警告： \n' +
    '这个程序 has\n' +
    '投入重复';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = '打电话';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = '程序 ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = '打电话';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = '调用没有返回值的过程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = '打电话没有回报';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = '打电话';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = '调用带一个返回值的过程';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = '打电话返回';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = '投入';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = '输入：';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = '亮点程序';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    	Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = '当前';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = '执行';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = '打电话';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = '打电话';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = '为组件 ';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = '的组件';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = '设置 ';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = '到';

    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = '设置 ';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = '到';
    		Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = '的组件';


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
    Blockly.Msg.SHOW_WARNINGS = "显示警告";
    
//Replmgr.js messages
    Blockly.Msg.REPL_ERROR_FROM_COMPANION ="从同伴的错误";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR ="网络连接错误";
    Blockly.Msg.REPL_NETWORK_ERROR ="网络错误";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART ="网络错误传播与同伴。 < br / > 请尝试重新启动的同伴，然后重新连接";
    Blockly.Msg.REPL_OK ="OK";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK ="伴侣版本检查";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = ' 您的同伴 App 是过时。单击"确定"开始更新。手表你 ';
    Blockly.Msg.REPL_EMULATORS ="模拟器";
    Blockly.Msg.REPL_DEVICES ="设备";
    Blockly.Msg.REPL_APPROVE_UPDATE ="屏幕因为你将被要求批准更新";
    Blockly.Msg.REPL_NOT_NOW ="不是现在";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 ="您正在使用的伴侣是过时。 <br/> <br/>此版本的应用程序发明家应使用与同伴版本";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE ="您正在使用一个过期的同伴。你不需要立即更新的同伴，但应考虑更新很快.";
    Blockly.Msg.REPL_DISMISS ="解雇";
    Blockly.Msg.REPL_SOFTWARE_UPDATE ="软件更新";
    Blockly.Msg.REPL_OK_LOWER ="Ok";
    Blockly.Msg.REPL_GOT_IT ="得到了它";
    Blockly.Msg.REPL_UPDATE_INFO = ' 现在正在您的设备上安装更新。观看您的设备 （或仿真程序） 屏幕和批准软件安装提示时。 < br / > < br / > 重要提示： 当更新完成后时，选择"DONE" (不要不要单击"打开")。然后转到应用程序发明家在您的 web 浏览器中，单击"连接"菜单，然后选择"重置连接"';

    Blockly.Msg.REPL_UNABLE_TO_UPDATE ="无法将更新发送到设备/模拟器";
    Blockly.Msg.REPL_UNABLE_TO_LOAD ="无法从应用程序发明家服务器加载更新";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND ="无法从应用程序发明家服务器 （服务器没有响应） 加载更新";
    Blockly.Msg.REPL_NOW_DOWNLOADING ="我们现正从应用程序发明家服务器，下载更新请待机状态";
    Blockly.Msg.REPL_RUNTIME_ERROR ="运行时错误";
    Blockly.Msg.REPL_NO_ERROR_FIVE_MINUTES ="<br/> <i>注：</i> 你不会看到另一个错误报告 5 秒钟.";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE ="通过 USB 电缆连接";
    Blockly.Msg.REPL_STARTING_EMULATOR ="启动 Android 模拟器 <br/> 请等待： 这可能要花一、 两分钟.";
    Blockly.Msg.REPL_CONNECTING ="连接......";
    Blockly.Msg.REPL_CANCEL ="取消";
    Blockly.Msg.REPL_GIVE_UP ="放弃";
    Blockly.Msg.REPL_KEEP_TRYING ="继续努力";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 ="连接失败";
    Blockly.Msg.REPL_NO_START_EMULATOR ="我们不能开始仿真程序内的麻省理工 AI 同伴";
    Blockly.Msg.REPL_PLUGGED_IN_Q ="插入吗?";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE ="AI2 并没有看到您的设备，请确保插入电缆和驱动程序是否正确";
    Blockly.Msg.REPL_HELPER_Q ="帮手吗?";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = ' aiStarter 帮手似乎并不会运行 < br / > <a href="http://appinventor.mit.edu"target="_blank"> 需要帮助吗?</a>' ;
    Blockly.Msg.REPL_USB_CONNECTED_WAIT ="USB 已连接，正在等待";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING ="秒，以确保所有正在运行";
    Blockly.Msg.REPL_EMULATOR_STARTED ="仿真程序开始，在等待";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE ="开始的伴侣应用程序上连接的电话";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR ="开始的伴侣应用程序在仿真器中";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING ="同伴开始，等";
    Blockly.Msg.REPL_VERIFYING_COMPANION ="验证，同伴开始......";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION ="连接到同伴";
    Blockly.Msg.REPL_TRY_AGAIN1 ="未能连接到麻省理工学院 AI2 同伴，再试一次";
    Blockly.Msg.REPL_YOUR_CODE_IS ="您的代码是";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q ="真的吗?";
    Blockly.Msg.REPL_FACTORY_RESET = "这将会尝试将仿真程序重置为其\"工厂\"状态。如果你以前已经更新安装在模拟器中的同伴，你可能会再一次做到这一点.";
  }
};

// Initalize language definition to English
Blockly.Msg.zh_cn.switch_language_to_chinese_cn.init();
