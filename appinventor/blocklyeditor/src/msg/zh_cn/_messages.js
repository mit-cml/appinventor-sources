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

goog.provide('Blockly.Msg.zh_cn');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.zh_cn.switch_language_to_chinese_cn = {
  // Switch language to Simplified Chinese.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = '复制代码块';
    Blockly.Msg.REMOVE_COMMENT = '删除注释';
    Blockly.Msg.ADD_COMMENT = '添加注释';
    Blockly.Msg.EXTERNAL_INPUTS = '外挂输入项';
    Blockly.Msg.INLINE_INPUTS = '内嵌输入项';
    Blockly.Msg.HORIZONTAL_PARAMETERS = '横向排列参数项';
    Blockly.Msg.VERTICAL_PARAMETERS = '纵向排列参数项';
    Blockly.Msg.DELETE_BLOCK = '删除代码块';
    Blockly.Msg.DELETE_X_BLOCKS = '删除 %1 个代码块';
    Blockly.Msg.COLLAPSE_BLOCK = '折叠代码块';
    Blockly.Msg.EXPAND_BLOCK = '展开代码块';
    Blockly.Msg.DISABLE_BLOCK = '禁用代码块';
    Blockly.Msg.ENABLE_BLOCK = '启用代码块';
    Blockly.Msg.HELP = '帮助';    
    Blockly.Msg.EXPORT_IMAGE = '下载模块图像';
    Blockly.Msg.COLLAPSE_ALL = '折叠所有块';
    Blockly.Msg.EXPAND_ALL = '展开所有块';
    Blockly.Msg.ARRANGE_H = '横向排列所有块';
    Blockly.Msg.ARRANGE_V = '纵向排列所有块';
    Blockly.Msg.ARRANGE_S = '斜向排列所有块';
    Blockly.Msg.SORT_W = '按宽度对所有块排序';
    Blockly.Msg.SORT_H = '按高度对所有块排序';
    Blockly.Msg.SORT_C = '按类别对所有块排序';

    Blockly.Msg.YAIL_OPTION = '生成Yail代码';
    Blockly.Msg.DOIT_OPTION = '执行该代码块';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = '修改数值:';
    Blockly.MSG_NEW_VARIABLE = '新建变量...';
    Blockly.MSG_NEW_VARIABLE_TITLE = '新建变量名称:';
    Blockly.MSG_RENAME_VARIABLE = '变量重命名...';
    Blockly.MSG_RENAME_VARIABLE_TITLE = '将所有 "%1" 变量重命名为:';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = '变量';
    Blockly.MSG_PROCEDURE_CATEGORY = '过程';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = '该代码块不能被定义';
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = '请从下拉列表中选择合适项';
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = '重复的组件事件处理器';
    Blockly.ERROR_CAN_NOT_DO_IT_TITLE = '无法执行该代码块';
    Blockly.ERROR_CAN_NOT_DO_IT_CONTENT = '只有连接助手或模拟器程序，才能执行';

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = '点击方块选取所需颜色';
    Blockly.Msg.LANG_COLOUR_BLACK = '黑色';
    Blockly.Msg.LANG_COLOUR_WHITE = '白色';
    Blockly.Msg.LANG_COLOUR_RED = '红色';
    Blockly.Msg.LANG_COLOUR_PINK = '粉色';
    Blockly.Msg.LANG_COLOUR_ORANGE = '橙色';
    Blockly.Msg.LANG_COLOUR_YELLOW = '黄色';
    Blockly.Msg.LANG_COLOUR_GREEN = '绿色';
    Blockly.Msg.LANG_COLOUR_CYAN = '青色';
    Blockly.Msg.LANG_COLOUR_BLUE = '蓝色';
    Blockly.Msg.LANG_COLOUR_MAGENTA = '品红';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = '浅灰';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = '深灰';
    Blockly.Msg.LANG_COLOUR_GRAY = '灰色';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = '分解色值';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = '返回含红、绿、蓝色值以及透明度值（0-255）的列表';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = '合成颜色';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = '返回由指定红、绿、蓝色值以及透明度值合成的颜色。';

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = '控制';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = '如果值为真，则执行相关语句块';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = '如果值为真，则执行第一个语句块\n' +
        '否则, 执行第二个语句块';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = '如果第一个值为真，则执行第一个语句块，\n' +
        '否则，如果第二个值为真，则执行第二个语句块';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = '如果第一个值为真，则执行第一个语句块，\n' +
        '否则，如果第二个值为真，则执行第二个语句块，\n' +
        '如果值皆不为真，则执行最后一个语句块';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = '否则，如果';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = '否则';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = '则';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = '如果';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = '添加、移除或重排相关元素，\n' +
        '重新设置该“如果”语句块功能';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = '否则，如果';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = '为“如果”语句块增设条件';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = '否则';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = '设最终条件，当所有条件均不满足时执行';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = '重复';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = '只要';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = '直到';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = '只要值为真，就重复执行相关语句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = '只要值为假，就重复执行相关语句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = '只要条件为真，就执行”执行“区域所包含的语句块';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = '循环取数到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = '范围从';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = '到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = '执行';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = '从一个数字开始取数，到另一个数结束。\n' +
        '每取一个数，都将其值赋给\n' +
        '变量 "%1"，并执行语句块。';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = '循环取';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = '数字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = '范围从';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = '到';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = '间隔为';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = '对一定范围内的数字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = '对于 ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' 范围内的';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = '按指定范围和增量循环取值，每次循环均将数值赋予指定变量，并运行“执行”区域所包含的代码块';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = '循环取';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = '列表项';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = '列表为';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = '对列表中每一项';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = '对于 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' 列表中的';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = '针对列表中的每一项运行“执行”区域所包含的代码块，'
    + ' 采用指定变量名引用当前列表项。';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = '循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = '中断';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = '执行下一个周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = '中断内部循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = '跳转到本循环的其余部分，并且\n' +
    '执行下一个周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = '警告：\n' +
    '本代码块只能用于\n' +
    '循环语句块';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = '当';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '满足条件';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = '满足条件';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = '“执行”区域中代码块被执行的前提是，满足条件的表达式值为'
    + '真。';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = '如果'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = '则';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = '否则';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = '如果';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = '如果条件表达式的检测值为真，' +
      '则将关联的求值表达式运算结果传递给“则-返回”语句槽；' +
      '否则将关联的求值表达式运算结果传递给“否则-返回”语句槽；' +
      '一般只有一个返回槽表达式能被求值。';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = '执行模块';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = '返回结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = '运行“执行”区域中的代码块并返回一条语句，用于在赋值前插入执行某个过程。';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = '执行语句/返回结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = '执行并返回';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = '求值但忽视结果'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = '求值但不返回';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = '运行所连接的代码块但不返回运算值，用于调用求值过程但不需要其运算值。';

    /* [林恩 13/10/14] 现在删除。可能回来的某一天。
    Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = '什么';
    Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
    Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = ' 返回 nothing。用来初始化变量或可以插入到返回的插槽中，如果没有价值需要返回。这是相当于为空或没有.';
    */
    
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = '打开屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = '打开屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = '在多屏应用中打开一个新屏幕。';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = '打开屏幕并传值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = '打开屏幕并传值'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = '在多屏应用中开启一个新屏幕，并'
    + '向其传入初始值';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = '获取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = '获取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = '屏幕开启时返回其传入值，'
    + '在多屏应用中开启动作一般由其他屏幕引发。如没有内容传入，'
    + '则返回空文本。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = '关闭当前屏幕';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = '关闭屏幕并返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = '返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = '关闭屏幕返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = ' 关闭当前屏幕并向打开此屏幕者返回结果';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = '关闭所有屏幕并终止程序运行';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = '获取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = '获取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = ' 当屏幕被其他应用启动时返回所传入的文本值，'
    + '如没有内容传入，则返回空文本值。'
    + '对于多屏应用，一般更多采用获取初始值的方式，而非获取纯文本值。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = '关闭屏幕并返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = '文本值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = '关闭屏幕返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = '关闭当前屏幕，并向打开此屏幕的应用返回文本。'
    + '对于多屏应用，则多采用关闭屏幕返回值的方式，'
    + '而不采用关闭屏幕返回文本。';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = '逻辑';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality _(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = '判断二对象是否相等，\n' +
    '对象可为任意类型，不限于数字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = '判断二对象是否互不相等，对象可为任意类型，不限于数字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = '比较';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '等于';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '不等于';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = '并且';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = '或者';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = '如所有输入项皆为真则返回真值。';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = '只要有输入项为真则返回真值。';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = '否定';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = '如输入项为假则返回真值，\n' +
    '如输入项为真则返回假值。';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = '返回true值';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = '返回false值';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = '数学';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = '报告所显示的数字 ';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = '数字';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = '如二数字相等则返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = '如二数字不等则返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = '如第一个数字小于第二个数字，\n' +
    '则返回true值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = '如第一个数字小于或等于第二个数字，\n' +
    '则返回false值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = '如第一个数字大于第二个数字，\n' +
    '则返回true值。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = '如果第一个数大于或等于第二个数，\n' +
    '则返回false值。';
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
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = '求二数之和';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = '求二数之差';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = '求二数乘积';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = '求二数之商';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = '求第一个数的第二个数次方';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = '改变';
    Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = '项目';
    Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = '由';
    Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = '添加号码到变量"%1"。';*/

    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = '平方根';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = '绝对值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = '相反值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = '自然对方';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e的乘方';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = '求平方根';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = '求绝对值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = '求相反值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = '求自然对数值';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = '求e的乘方';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = '返回 10 数的力量'; */

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = '就高或或就低取整';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = '将输入项取整为不低于其的最小数值';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = '将输入项取整为不大于其的最大数值';
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
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x坐标';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y坐标';
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
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = '由直角坐标求角度(-180, +180]';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = '最小值';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = '最大值';

    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Return the smallest of its arguments..';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Return the largest of its arguments..';

    Blockly.Msg.LANG_MATH_DIVIDE = '除以';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = '模数';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = '余数';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = '商数';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = '求模数';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = '求余数';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = '求商数';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = '随机整数';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = '范围从';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = '随机整数从 %1 到 %2 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = '返回位于上下边界之间的随机整数，\n' +
    '限于2的30次方范围内';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = '随机小数';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = '返回0和1之间的随机数值';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = '随机数种子设定';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = '为';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = '为随机数生成器指定种子数';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = '角度变换';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = '弧度转角度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = '角度转弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = '求弧度参数对应的角度值[0, 360)';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = '求角度参数对应的弧度值[-\u03C0, +\u03C0)';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = '求小数值';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = '数字';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = '位数';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = '将数字 %1设为小数形式 位置 %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = '以指定位数返回该数值的小数形式';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '是否为数字?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = '判断该对象是否为数字类型';
    
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = 'is base 10?';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = 'Tests if something is decimal.';

    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = 'is hexadecimal?';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = 'Tests if something is hexadecimal.';

    // Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = 'is binary?';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = 'Tests if something is binary.';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = 'convert number';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = 'base 10 to hex';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = 'Returns the conversion from decimal to hexadecimal';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = 'hex to base 10';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = 'Returns the conversion from hexadecimal to decimal';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = 'base 10 to binary';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = 'Returns the conversion from decimal to binary';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = 'binary to base 10';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = 'Returns the conversion from binary to decimal';  
    
// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = '输入字符串文本';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = '创建字符串文本';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = '将所有输入项合并为一个单独的字符串文本，\n'
    + '如没有输入项，则生成空文本。';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = '合并文本';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = '字符串';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = '到';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = '追加文本';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = '变量';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = '将文本追加到变量 "%1"';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = '求长度';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = '求该文本中所包含的字母数量(包括空格)';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = '是否为空';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = '如文本长度为0则返回真值，否则返回假值';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = '比较文本';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = '检测text1的首字母顺序是否低于text2，\n'
    + '如果首字母相同，则长度较短的文本顺序靠前，\n'
    + '大写字符顺序优于小写字符。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = '检测文本字串内容是否相同，即，\n'
    + '是否由同一组相同顺序的字符组成，与通常的相等概念不同的是，\n'
    + '当文本字串为数字时，如123和0123，尽管数字相等，\n'
    + '但其文本不等。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = '告之text1的字首顺序是否高于text2，\n'
    + '如首字母相同，则长度较短的文本顺序靠前，\n'
    + '大写字符顺序优于小写字符。';

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
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = '将字符串参数复制并转为大写后返回';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = '将字符串参数复制并转为小写后返回';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = '删除空格';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = '将字符串参数复制并删除首尾处的空格后返回';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = '子串位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = '求子串%2在文本%1中的起始位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = '求子串在文本中的起始位置，\n'
    + '其中1表示文本的起始处，\n '
    + '而如子串不在文本中则返回0。';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = '包含子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = '检查文本%1中是否包含子串%2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = '检查文本中是否包含该子串';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = '分隔符';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = '分隔符 (列表)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = '分解首项';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = '分解任意首项';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = '分解';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = '任意分解';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '在首次出现分隔符的位置将给定文本分解为两部分，\n'
    + '并返回包含分隔点前和分隔点后两部分内容的列表，\n'
    + '如分解字符串"苹果,香蕉,樱桃,狗粮"，以逗号作为分隔符，\n'
    + '将返回一个包含两项的列表，其中第一项内容为"苹果"，第二项内容则为\n'
    + '"香蕉,樱桃,狗粮"。\n'
    + '注意，"苹果"后面的逗号不在结果中出现，\n'
    + '因为它起到分隔符的作用。';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = '以列表中的任意项作为分隔符，\n'
    + '在首次出现分隔符的位置将给定文本分解为一个两项列表。\n\n'
    + '如以"(稥,苹)"作为分隔符分解"我喜欢苹果香蕉苹果葡萄"，\n'
    + '将返回一个两项列表，其第一项为"我喜欢"，第二项为\n'
    + '"果香蕉苹果葡萄"';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = '以指定文本作为分隔符，将字符串分解为不同片段，并生成一个列表作为返回结果。\n'
    + ' 如以","(逗号)分解"一,二,三,四"，将返回列表"(一 二 三 四)"，\n'
    + ' 而以"-土豆"作为分隔符分解字符串"一-土豆,二-土豆,三-土豆,四"，则返回列表"(一 二 三 四)"。'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY  = '以分隔符列表中的任意一项作为分隔符，将给定文本分解为列表，\n'
    + '并将列表作为处理结果返回。\n'
    + '如分解字符串"蓝莓,香蕉,草莓,狗粮"，以一个含两元素的列表作为分隔符，\n'
    + '其中第一项为逗号，第二项为"莓"，则返回列表：\n'
    + '"(蓝 香蕉 草 狗粮)"'
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
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = '以空格作为分隔符，将文本分解为若干部分。';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = '提取子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = '提取位置';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = '提取长度';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = '从文本%1第%2位置提取长度为%3的子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = '以指定长度、指定位置从指定文本中提取文本片段，\n'
    + '位置1表示被提取文本的起始处。';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = '替换项';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = '原始文本';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = '全部替换';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = '替换为';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = '将文本%2中所有%1全部替换为%3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = '返回一个新文本字符串，其中所包含的替换项内容\n'
    + '均被替换为指定的字串。';

    Blockly.Msg.LANG_CATEGORY_LISTS = '列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty _lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = '创建空列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = '返回一个项数为零的列表对象';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = '创建列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = '创建一个可包含任意项数的列表';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = '重新配置该列表块，为其增加、删除或重新排列所包含的区间。';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = '列表项';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = '增加一个列表项';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = '列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = '增加一个列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = '选择列表项';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = '选择列表%1中索引值为%2的列表项';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = '求指定位置的列表项';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '是否在列表中?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = '检查查列表%2中是否含列表项%1'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = '如该对象为列表中某一项则返回真值，'
    + '否则为假。';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = '列表项索引值';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = '求列表项%1在列表%2中的位置';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = '求对象在该列表中的位置，'
    + '如不在该列表中，则返回0。';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = '随机选取列表项';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = '从列表中随机选取一项';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = '替换列表项';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = '替换为';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = '将列表%1中索引值为%2的列表项替换为%3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = '替换列表中第n项内容';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = '删除列表项';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = '删除列表%1中第%2项';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = '删除指定位置的列表项';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = '列表 ' 创建与项目 ';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = '重复';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = '时代';
    Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = '创建一个列表组成的给定 value\n' +
    '重复指定的次数的';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = '求列表长度 ';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = '计算列表%1长度';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = '计算列表项数';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = '追加列表';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = '列表1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = '列表2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = '将列表%2中所有项追加到列表%1中';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = '将list2中所有项添加到list1的末尾。添加后，'
    + 'list1中将包括所有新加入的元素，而list2则不发生变化。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = '添加列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = '列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = '将列表项%2加入列表%1中';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = '在列表末尾增加列表项';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = '重新配置该列表块，增加、删除或重新排序其中包含的区间';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = '复制列表';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = '复制列表，包括复制其中包含的所有子列表';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '是否为列表?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = '检测该对象是否为列表类型';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = '列表转CSV行';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = '将列表转换为表格中的一行数据，'
    + '并返回表示行数据的CSV（逗号分隔数值）字符串，数据行中的每一项被当作一个字段，'
    + '在CSV字符串中以双引号方式标识，'
    + '各数据项以逗号分隔，且每行末尾'
    + '均不带换行符。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'CSV行转列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'CSV字符串';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = '对CSV（逗号分隔数值）格式字符串进行解析，'
    + '生成一个包含各字段数据的列表。对于文本行而言，如字段中出现非转义的换行符则会出错'
    + '（实际是指多行字段的情况），而只在整行文本的末端才出现换行符或CRLF则是正确的。';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = '列表转CSV';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = '将列表转换为带标题行的表格形式，'
    + '且返回表示该表格的CSV（逗号分隔数值）字符串文本，列表中的每一项本身'
    + '还可以作为表示CSV表格行的列表，列表行中的每一项'
    + '都可看成是一个字段，在CSV字符串文本中以双引号方式进行标识。'
    + '在返回字符串文本中，数据行中的各项以逗号进行分隔，'
    + '而各数据行则以CRLF \(\\r\\n\)进行分隔。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'CSV转列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'CSV字符串';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = '对CSV（逗号分隔数值）格式字符串进行解析，'
    + '并生成数据行列表，其中的每一项又都是一个字段列表，'
    + '各数据行间分别以换行符\(\\n\)或CRLF \(\\r\\n\)方式分隔。';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = '插入列表项';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = '插入位置';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = '插入项';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = '在列表%1的第%2项处插入列表项%3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = '在指定位置插入列表项';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '列表是否为空?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = '如果列表为空则返回真';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = '键值对查询';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = '关键字';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = '键值对';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = '无果则返回';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = '在键值对%2中查找关键字%1 如未找到则返回%3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = '返回键值对列表中与关键字关联的数值';

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
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = '初始化全局变量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = '我的变量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = '为';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = '全局变量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = '创建全局变量，并通过挂接的代码块赋值';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = '取';
    // Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = '取变量值';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = '取变量值';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = '设';
    // Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = '为';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = '设变量值';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = '设变量值等于输入项';

    Blockly.Msg.LANG_VARIABLES_VARIABLE = '变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = '初始化局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = '我的变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = '为';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = '作用范围';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = '局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '创建指定范围内语句块所使用的变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = '初始化局部变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    // 这些别不同之间的语句和表达式
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = '初始化表达式变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = '我的变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = '为';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = '作用范围';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = '表达式变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '创建指定表达式所使用的变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = '初始化表达式变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = '输入项';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = '参数';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = '我的过程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = '执行语句';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = '语句执行完成后，不返回结果';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = '然后返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = '执行语句';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = '“执行”其中包含的块并返回一条语句， 可以实现在过程执行前将返回数据赋值给相关变量';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = '执行/返回';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = '语句执行完成后，会返回结果';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = '警告:\n' +
    '此过程的输入项\n' +
    '出现重复';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = '过程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = '调用无返回值过程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = '调用无返回值过程';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = '调用有返回值过程';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = '调用有返回值过程';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = '输入项';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = '输入:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = '预览代码块功能';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = '当';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = '执行';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = '调用';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = '调用';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = '组件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = '组件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = '设';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = '为';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = '设';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = '为';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = '组件';

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
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL = '/reference/components/sensors.html#AccelerometerSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#accelerometersensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#accelerometersensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#accelerometersensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL = '/reference/components/sensors.html#BarcodeScanner';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL = '/reference/components/sensors.html#barcodescannerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL = '/reference/components/sensors.html#barcodescannerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL = '/reference/components/sensors.html#barcodescannermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL = '/reference/components/sensors.html#LocationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#locationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#locationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#locationsensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL = '/reference/components/sensors.html#OrientationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#orientationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#orientationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#orientationsensormethods';

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
    Blockly.Msg.HIDE_WARNINGS = "隐藏警告";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "你应该为模块的每个端口都填上模块";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "这个模块应该连上事件或者过程模块";
    
//Replmgr.js messages
    Blockly.Msg.REPL_ERROR_FROM_COMPANION ="AI伴侣出现错误";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR ="发生网络连接故障";
    Blockly.Msg.REPL_NETWORK_ERROR ="网络故障";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART ="与AI伴侣通信故障，<br />请尝试重启伴侣程序并重新连接";
    Blockly.Msg.REPL_OK ="确定";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK ="检查伴侣程序版本";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = '伴侣程序已过期，点击确定键升级。';
    Blockly.Msg.REPL_EMULATORS ="查看模拟器";
    Blockly.Msg.REPL_DEVICES ="设备";
    Blockly.Msg.REPL_APPROVE_UPDATE ="屏幕，确认升级";
    Blockly.Msg.REPL_NOT_NOW ="现在不";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 ="你使用的伴侣程序已经过期，<br/><br/>本版App Inventor适用的伴侣程序版本为";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE ="你正在使用一个过期版本的伴侣程序，请尽快升级";
    Blockly.Msg.REPL_DISMISS ="放弃";
    Blockly.Msg.REPL_SOFTWARE_UPDATE ="软件升级";
    Blockly.Msg.REPL_OK_LOWER ="确定";
    Blockly.Msg.REPL_GOT_IT ="升级完成";
    Blockly.Msg.REPL_UPDATE_INFO = '更新正在安装在你的设备上。请查看你设备(或模拟器)屏幕出现的提示并批准软件安装。<br /><br />重要:当更新完成,选择“完成”(不要点击“开放”)。然后再次浏览 App Inventor 网页,点击“连接”菜单,选择“重置连接”。然后重新连接设备。';
    Blockly.Msg.REPL_UNABLE_TO_UPDATE ="无法将升级包发送给设备或模拟器";
    Blockly.Msg.REPL_UNABLE_TO_LOAD ="无法从App Inventor服务器下载升级包";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND ="无法App Inventor服务器(服务器没有响应)加载更新信息";
    Blockly.Msg.REPL_NOW_DOWNLOADING ="正在从App Inventor服务器下载升级包，请耐心等待。";
    Blockly.Msg.REPL_RUNTIME_ERROR ="运行故障";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS ="<br/><i>注意：</i>&nbsp;5秒钟后将显示另一条错误信息。";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE ="正在通过USB电缆连接";
    Blockly.Msg.REPL_STARTING_EMULATOR ="正在启动Android模拟器<br/>请等待：可能需要一至两分钟";
    Blockly.Msg.REPL_CONNECTING ="连接中...";
    Blockly.Msg.REPL_CANCEL ="取消";
    Blockly.Msg.REPL_GIVE_UP ="放弃";
    Blockly.Msg.REPL_KEEP_TRYING ="重试";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 ="连接失败";
    Blockly.Msg.REPL_NO_START_EMULATOR ="无法在模拟器中启动伴侣程序";
    Blockly.Msg.REPL_PLUGGED_IN_Q ="是否已插入电缆？";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE ="AI2没有看到你的设备，请确认电缆连接以及驱动程序安装是否正常。";
    Blockly.Msg.REPL_HELPER_Q ="是否运行助手？";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'aiStarter助手程序不在执行状态中，<br />是否需要<a href="http://appinventor.mit.edu" target="_blank">帮助?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT ="已连接USB，请等待";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING ="秒，确保相关资源全部加载。";
    Blockly.Msg.REPL_EMULATOR_STARTED ="模拟器已运行，请等待";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE ="正在所连接电话设备中启动伴侣程序";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR ="正在模拟器中启动伴侣程序";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING ="伴侣程序启动中，请等待";
    Blockly.Msg.REPL_VERIFYING_COMPANION ="检查伴侣程序启动状态....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION ="连接伴侣程序";
    Blockly.Msg.REPL_TRY_AGAIN1 ="无法连接伴侣程序，请重新连接。";
    Blockly.Msg.REPL_YOUR_CODE_IS ="编码为：";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q ="你真的要这么做吗？";
    Blockly.Msg.REPL_FACTORY_RESET = "这将使模拟器重置为出厂模式，如果此前升级过伴侣程序，则需要重新升级。";

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "你确定要全部删除 %1 个这些模块吗?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "生成 Yail";
    Blockly.Msg.DO_IT = "预览代码块功能";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "清除錯誤";
    Blockly.Msg.CAN_NOT_DO_IT = "不能预览代码块功能";
    Blockly.Msg.CONNECT_TO_DO_IT = '你必须要连接AI伴侣或者模拟器才能使用"预览代码块功能"';
  }
};

// Initalize language definition to English
Blockly.Msg.zh_cn.switch_language_to_chinese_cn.init();
