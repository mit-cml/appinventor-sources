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

goog.provide('AI.Blockly.Msg.zh_cn');

goog.require('Blockly.Msg.zh.hans');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.zh.switch_language_to_chinese_cn = {
  // Switch language to Simplified Chinese.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = '复制代码块';
    Blockly.Msg.REMOVE_COMMENT = '删除注释';
    Blockly.Msg.ADD_COMMENT = '添加注释';
    Blockly.Msg.EXTERNAL_INPUTS = '外接输入项';
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

    Blockly.Msg.COPY_TO_BACKPACK = '增加至背包';
    Blockly.Msg.COPY_ALLBLOCKS = '复制所有代码块到背包';
    Blockly.Msg.BACKPACK_GET = '提取背包中所有代码块';
    Blockly.Msg.BACKPACK_EMPTY = '清空背包';
    Blockly.Msg.BACKPACK_CONFIRM_EMPTY = '你确定要清空背包吗？';
    Blockly.Msg.BACKPACK_DOC_TITLE = "背包介绍";
    Blockly.Msg.SHOW_BACKPACK_DOCUMENTATION = "显示背包介绍";
    Blockly.Msg.BACKPACK_DOCUMENTATION = "背包具有备份功能。它允许你从项目或屏幕中复制代码块到另一个项目或屏幕。复制时，将代码块从工作区域拖放入背包；粘贴时，单击背包图标将背包中的代码块拖放入工作区域。"
    + "</p><p>如果你退出 MIT App Inventor 时将代码块留在背包中，背包会保存代码块直到你下次登录。"
    + "</p><p>想要了解更多有关背包的介绍，请前往："
    + '</p><p><a href="/reference/other/backpack.html" target="_blank">/reference/other/backpack.html</a>';

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
    Blockly.ERROR_COMPONENT_DOES_NOT_EXIST = "组件不存在";
    Blockly.ERROR_BLOCK_IS_NOT_DEFINED = "该代码块未定义。删除该代码块！";

    Blockly.ERROR_CAN_NOT_DO_IT_CONTENT = '只有连接AI伴侣或模拟器，才能执行该代码块';
    Blockly.ERROR_CAN_NOT_DO_IT_TITLE = '无法执行该代码块';

// Colour Blocks.
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
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = '返回含红、绿、蓝色值以及透明度值（0-255）的列表';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = '合成颜色';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = '返回由指定红、绿、蓝色值以及透明度值合成的颜色。';

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = '控制';
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
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = '设最终条件，当所有条件均不满足时则执行最终条件';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = '重复';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = '只要';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = '直到';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = '只要值为真，就重复执行相关语句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = '只要值为假，就重复执行相关语句';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = '只要条件为真，就执行“执行”区域所包含的语句块';

    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = '循环取数到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = '范围从';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = '到';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = '执行';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = '从一个数字开始取数，到另一个数结束。\n' +
        '每取一个数，都将其值赋给\n' +
        '变量 "%1"，并执行语句块。';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = '对于任意';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = '变量名';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = '范围从';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = '到';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = '每次增加';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = '对一定范围内的数字';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = '对于 ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' 范围内的';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = '按指定范围和增量循环取值，'
	+ '每次循环均将数值赋予指定变量，'
	+ '并运行“执行”区域所包含的代码块';

    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = '对于任意';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = '列表项目名';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = '于列表';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = '对列表中每一项';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = '对于 ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' 列表中的';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = '针对列表中的每一项运行“执行”区域所包含的代码块，'
    + ' 采用指定变量名引用当前列表项。';

    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = '循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = '中断';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = '执行下一个周期';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = '跳出内部循环';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = '跳过本循环的其余部分，并且\n' +
    '进入下一循环周期。';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = '警告：\n' +
    '本代码块只能于\n' +
    '循环语句块中使用。';

    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = '当';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '满足条件';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = '执行';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = '满足条件';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = '当条件的表达式值为真时，执行“执行”区域中的代码块。';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = '如果'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = '则';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = '否则';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = '如果';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = '如果条件表达式的检测值为真，' +
      '则将关联的求值表达式运算结果传递给“则-返回”语句槽；' +
      '否则将关联的求值表达式运算结果传递给“否则-返回”语句槽；' +
      '一般只有一个返回槽表达式能被求值。';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = '执行模块';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = '返回结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = '运行“执行”区域中的代码块并返回一条语句，用于在赋值前插入执行某个过程。';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = '执行语句/返回结果';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = '执行并返回';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = '求值但忽略结果'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = '求值但不返回';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = '运行所连接的代码块但不返回运算值，用于调用求值过程但不需要其运算值。';

    /* [lyn 13/10/14] Removed for now. May come back some day.
    Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = '空值';
    Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = '/reference/blocks/control.html#nothing';
    Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = ' 返回空值。可用于初始化变量或插入到返回槽中（如果没有值需要返回，相当于为空）。';
    */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = '打开另一屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = '打开屏幕';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = '在多屏应用中打开一个新屏幕。';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = '打开另一屏幕并传值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = '打开屏幕并传值'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = '在多屏应用中开启一个新屏幕，并'
    + '传入初始值。';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = '获取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = '屏幕名称';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = '初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = '获取初始值';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = '在屏幕打开时返回传入的值。'
    + '此屏幕通常由多屏应用程序中的另一个屏幕打开。如没有内容传入，'
    + '则返回空文本。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = '关闭屏幕 ';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = '关闭当前屏幕';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = '关闭屏幕并返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = '返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = '关闭屏幕并返回值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = ' 关闭当前屏幕并向打开此屏幕者返回值。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = '退出程序';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = '关闭所有屏幕并终止程序。';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = '获取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = '获取初始文本值';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = ' 当屏幕被其他应用启动时返回所传入的文本值，'
    + '如没有内容传入，则返回空文本值。'
    + '对于多屏应用，更多地是采用获取初始值的方式，而非获取文本值。';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = '关闭屏幕并返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = '文本值';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = '关闭屏幕并返回文本';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = '关闭当前屏幕，并向打开此屏幕的应用返回文本。'
    + '对于多屏应用，则多采用关闭屏幕返回值，'
    + '而非返回文本值。';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = '逻辑';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = '判断二对象是否相等，\n' +
    '对象可为任意类型，不限于数字。\n' +
	'判断数字是否相等的依据是它们的字符串形式是否相等。' +
	'例如：数字0等同于字符串“0”；' +
	'代表数字的字符串当它们代表的数字相等时也相等，\n' +
	'例如“1”等于“01”';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = '判断二对象是否互不相等，对象可为任意类型，不限于数字。';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = '逻辑相等';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_AND = '与';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = '或';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = '如所有输入项皆为真则返回真值。';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = '只要任意输入项为真则返回真值。';

    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = '非';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = '如输入项为假则返回真值，\n' +
    '如输入项为真则返回假值。';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = '真';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = '假';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = '返回真值';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = '返回假值';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = '数学';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = '报告所显示的数字 ';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = '数字';

    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = '如两个数字相等则返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = '如两个数字不等则返回真值';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = '如第一个数字小于第二个数字，\n' +
    '则返回真。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = '如第一个数字小于或等于第二个数字，\n' +
    '则返回真。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = '如第一个数字大于第二个数字，\n' +
    '则返回真。';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = '如果第一个数大于或等于第二个数，\n' +
    '则返回真。';
    Blockly.Msg.LANG_MATH_COMPARE_EQ = '=';
    Blockly.Msg.LANG_MATH_COMPARE_NEQ = '\u2260';
    Blockly.Msg.LANG_MATH_COMPARE_LT = '<';
    Blockly.Msg.LANG_MATH_COMPARE_LTE = '\u2264';
    Blockly.Msg.LANG_MATH_COMPARE_GT = '>';
    Blockly.Msg.LANG_MATH_COMPARE_GTE = '\u2265';

    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = '返回a+b';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = '返回a-b';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = '返回a*b';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = '返回a/b';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = '返回a^b';

    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = '改变';
    Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = '项目';
    Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = '由';
    Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = '变量%1增加一个值"。';*/

    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = '平方根';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = '绝对值';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = '相反数';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'ln';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = '返回x的平方根。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = '返回x的绝对值。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = '返回x的相反数。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = '返回ln(x)。';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = '返回e^x。';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = '返回10^x。'; */

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = '上取整或下取整';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = '将x取为不小于x的最小整数。';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = '将x取为不大于x的最大整数。';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = '四舍五入';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = '上取整';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = '下取整';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = '返回sin(x)。（x单位为度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = '返回cos(x)。（x单位为度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = '返回tan(x)。（x单位为度）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = '返回asin(x)。（x单位为度,范围(-90,+90]）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = '返回acos(x)。（x单位为度,范围[0, 180)）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = '返回atan(x)。（x单位为度,范围(-90, +90)）';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = '返回atan2(x)。（x单位为度,范围(-180, +180]）';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = '最小值';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = '最大值';

    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = '返回最小值。';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = '返回最大值。';

    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = '求模';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = '求余数';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = '求商';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = '返回a/b的模。';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = '返回a/b的余数。';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = '返回a/b的商。';

    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = '随机整数';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = '范围从';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = '到';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = '随机整数从 %1 到 %2 ';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = '返回指定范围内的随机整数，\n' +
    '接受的范围限于2^30之内。';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = '随机小数';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = '返回0和1之间的随机小数值。';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = '设定随机数种子';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = '为';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = '为随机数生成器指定种子。';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = '角度<——>弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = '弧度——>角度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = '角度——>弧度';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = '返回输入弧度对应的角度,返回的度数范围[0, 360)。';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = '返回角度对应的弧度值，返回的弧度范围[-\u03C0, +\u03C0)。';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = '求小数值';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = '数字';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = '位数';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = '将数字 %1转变为小数形式 位数 %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = '以指定位数返回该数值的小数形式。';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '是否为数字?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = '判断该对象是否为数字。';

    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = '是否为十进制数?';
    // Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = '判断该对象是否为十进制数。';

    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = '是否为十六进制?';
    // Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = '判断该对象是否为十六进制数。';

    // Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = '/reference/blocks/math.html#isnumber';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = '是否为二进制?';
    // Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = '判断该对象是否为二进制数。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = '进制转换';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = '10进制转16进制';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = '返回一个十进制数的十六进制形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = '16进制转10进制';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = '返回一个十六进制数的十进制形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = '10进制转2进制';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = '返回一个十进制数的二进制形式。';

    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = '2进制转10进制';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = '';
    // Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = '返回一个二进制数的十进制形式。';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = '一个字符串';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = '以...创建字符串';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = '合并所有输入项，为单一的字符串，\n'
    + '如没有输入项，则生成空字符串。';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = '合并字符串';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = '字符串';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_TO = '到';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = '追加字符串';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = '变量';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = '将字符串追加到字符串变量 "%1"之后。';

    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = '求长度';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = '返回该字符串的字符数(包括空格)。';

    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = '是否为空';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = '如字符串长度为0则返回真，否则返回假。';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = '字符串比较';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = '按字典顺序比较text1是否小于text2，\n'
    + '如果text1与text2开头部分相同，则长度较短的字符串为较小值，\n'
    + '大写字符顺序优于小写字符，例如 A<a。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = '检测字符串内容是否相同，即：\n'
    + '是否由同一组顺序相同的字符组成，与通常的相等概念不同的是，\n'
    + '当文本字串为数字，如123和0123，尽管数字相等，\n'
    + '但其字符串不相等。';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = '按字典顺序比较text1是否大于text2，\n'
    + '如果text1与text2开头部分相同，则长度较短的字符串为较小值，\n'
    + '大写字符顺序优于小写字符，例如 A<a。';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP = "生成文本，如文本块。模糊文本的不同之处在于：\n"
    + "检查应用程序的APK时，文本不容易被发现。通常在创建包含机密信息的应用程序（例如API密钥）\n"
    + "时使用此功能。\n"
    + "警告：对于专家来说，模糊文本的安全可靠性非常低。";
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE = '模糊文本';

    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = '文本字母数';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = '返回文本指定开头和结尾间的字符数。';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = '开头';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = '结尾';*/

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = '查找';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = '查找目标';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = '待查找字符串';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = '返回查找目标在待查找字符串中第一次/最后一次出现的下标\n' +
     '如果未发现查找目标，返回0。';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = '第一次';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = '最后一次';*/

    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = '字符下标';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = '字符串';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = '返回指定下标的字符。';*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = '大写';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = '小写';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = '返回转为大写后的字符串副本。';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = '返回转为小写后的字符串副本。';

    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = '删除空格';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = '返回删除空格后的字符串副本。';

    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = '子串在文本中位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = '求子串%2在文本%1中的起始位置';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = '求子串在文本中的起始位置，\n'
    + '其中1表示文本的起始处，\n '
    + '而如子串不在文本中则返回0。';

    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = '文本是否包含子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = '子串';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = '检查文本%1中是否包含子串%2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = '检查文本中是否包含该子串。';

    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = '分隔符';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = '分隔符 (列表)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = '分解首项';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = '分解任意首项';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = '分解';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = '任意分解';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '在首次出现分隔符的位置将给定文本分解为两部分，\n'
    + '并返回包含分隔点前和分隔点后两部分内容的列表，\n'
    + '如分解字符串"苹果,香蕉,樱桃,西瓜"，以逗号作为分隔符，\n'
    + '将返回一个包含两项的列表，其中第一项内容为"苹果"，第二项内容则为\n'
    + '"香蕉,樱桃,西瓜"。\n'
    + '注意，"苹果"后面的逗号不在结果中出现，\n'
    + '因为它起到分隔符的作用。';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = '以列表中的任意项作为分隔符，\n'
    + '在首次出现分隔符的位置将给定文本分解为一个两项列表。\n\n'
    + '如以"(稥,苹)"作为分隔符分解"我喜欢苹果香蕉苹果葡萄"，\n'
    + '将返回一个两项列表，其第一项为"我喜欢"，第二项为\n'
    + '"苹果香蕉苹果葡萄"';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = '以指定文本作为分隔符，将字符串分解为不同片段，并生成一个列表作为返回结果。\n'
    + ' 如以","(逗号)分解"一,二,三,四"，将返回列表"(一 二 三 四)"，\n'
    + ' 而以"-土豆"作为分隔符分解字符串"一-土豆,二-土豆,三-土豆,四"，则返回列表"(一 二 三 四)"。'
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY  = '以分隔符列表中的任意一项作为分隔符，将给定文本分解为列表，\n'
    + '并将列表作为处理结果返回。\n'
    + '如分解字符串"蓝莓,香蕉,草莓,西瓜"，以一个含两元素的列表作为分隔符，\n'
    + '其中第一项为逗号，第二项为"莓"，则返回列表：\n'
    + '"(蓝 香蕉 草 西瓜)"'

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = '输出';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = '输出文本，数字或指定值。';

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = '提示';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = '信息';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = '提示用户输入特定的文本。';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = '文本';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = '指定数字';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = '用空格分解';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = '以空格作为分隔符，将文本分解为若干部分。';

    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = '提取子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = '提取位置';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = '提取长度';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = '从文本%1第%2位置提取长度为%3的子串';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = '以指定长度、指定位置从指定文本中提取文本片段，\n'
    + '位置1表示被提取文本的起始处。';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = '替换项';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = '原始文本';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = '全部替换';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = '替换为';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = '将文本%1中所有%2全部替换为%3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = '返回一个新文本字符串，其中所包含的替换项内容\n'
    + '均被替换为指定的字串。';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = '列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = '创建空列表 ';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = '返回一个项数为零的列表对象';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = '创建列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = '创建一个可包含任意项数的列表';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = '编辑该列表块，包括：增加、删除或重新排列。';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = '列表项';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = '向列表增加一个列表项。';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = '列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = '向列表增加一个列表项。';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = '选择列表项';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = '选择列表%1中索引值为%2的列表项';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = '返回指定索引值的列表项';

    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '对象是否在列表中?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = '检查列表%2中是否含对象%1'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = '如该对象为列表中某一项则返回真值，'
    + '否则为假。';

    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = '列表项索引值';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = '求对象%1在列表%2中的位置';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = '求对象在该列表中的位置，'
    + '如不在该列表中，则返回0。';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = '随机选取列表项';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = '从列表中随机选取一项';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = '替换列表项';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = '替换为';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = '将列表%1中索引值为%2的列表项替换为%3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = '替换列表中第n项内容';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = '删除列表项';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = '删除列表%1中第%2项';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = '删除指定索引值的列表项';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = '创建拥有项目列表';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = '重复';
    Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = '次数';
    Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = '创建一个拥有给定项目的列表\n' +
    '并且重复给定的次数';*/

    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = '求列表长度';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = '计算列表%1的长度';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = '计算列表项数';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = '追加列表';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = '列表1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = '列表2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = '将列表%2中所有项追加到列表%1中';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = '将list2中所有项添加到list1的末尾。添加后，'
    + 'list1中将包括所有新加入的元素，而list2不发生变化。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = '追加列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = '列表项';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = '将列表项%2加入列表%1中';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = '在列表末尾增加列表项。';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = '列表';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = '编辑该列表块，包括：增加、删除或重新排列。';

    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = '复制列表';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = '复制列表，包括其中包含的所有子列表。';

    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '对象是否为列表? ';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = '对象';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = '判断该对象是否为列表类型。';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = '列表转换为CSV行';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = '将列表转换为表格中的一行数据，'
    + '并返回表示行数据的CSV格式文本，数据行中的每一项被当作一个字段，'
    + '在CSV格式文本中以双引号引用，'
    + '各数据项以逗号分隔，返回的CSV格式文本'
    + '末尾没有换行符。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'CSV行转换为列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = '将文本按CSV格式进行解析，'
    + '生成一个包含各字段数据的列表。对于CSV格式文本而言，字段中出现未转义的换行符则会出错'
    + '（在有多行字段的情况下），而只在整行文本的末端才出现换行符或CRLF则是正确的。';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = '列表转换为CSV表';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = '将列表转换为行优先形式的表格，'
    + '且返回表示该表格的CSV格式文本，列表中本身可以代表'
    + 'CSV表中的一行，列表中的每一项都可看成是'
    + '一个字段，在CSV格式文本中以双引号引用，'
    + '在返回的CSV文本中，行中数据以逗号分隔，'
    + '行则以CRLF \(\\r\\n\)分隔。';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'CSV表转换为列表';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = '文本';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = '将文本按CSV格式进行解析，'
    + '并生成多行列表，其中的每一项又都是一个字段列表，'
    + '各行间分别以换行符\(\\n\)或CRLF \(\\r\\n\)分隔。';

    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = '插入列表项';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = '索引值';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = '插入项';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = '在列表%1的第%2项处插入列表项%3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = '在指定索引值(位置)处插入列表项。';

    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '列表是否为空?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = '列表';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = '如果列表为空则返回真。';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = '键值对查询';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = '关键字';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = '键值对';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = '查询无果';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = '在键值对%2中查找关键字%1，如未找到则返回%3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = '返回键值对列表中与关键字关联的值';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = '查询';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = '项目';
    Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = '待查询列表';
    Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = '返回的索引的第一个/最后一个 occurrence\n' +
    '的项目这个列表 \n' +
    '如果找不到文本则返回。';
    Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = '第一次';
    Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = '最后一次';

    Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = '值的索引为';
    Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = '在列表中';
    Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = '返回列表中特定位置的值。';
    Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = '被修改的项的索引;
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = '在列表中';
    Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = '修改为';
    Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = '修改列表中制定位置的值。';*/

// Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = '初始化全局变量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = '变量名';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = '为';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = '全局变量';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = '创建全局变量，并通过挂接的代码块赋值';

    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = '取';
    // Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = '获取变量值';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = '返回变量的值。';

    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = '设置';
    // Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = '项目';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = '为';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = '设置变量值';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = '设置变量值等于输入的值。';
    Blockly.Msg.LANG_VARIABLES_VARIABLE = '变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = '初始化局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = '变量名';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = '为';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = '作用范围';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = '局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '创建只在指定块的执行部分有效的变量。';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = '初始化局部变量';

    // These don't differ between the statement and expression
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = '初始化局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = '变量名';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = '为';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = '作用范围';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = '初始化局部变量';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '创建只在指定块内的返回部分有效的变量。';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = '初始化局部变量';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = '局部变量名称';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = '名称';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = '过程名';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = '执行语句';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = '语句执行完成后，不返回结果。';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = '然后返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = '执行语句';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = '执行其包含的语句块并返回一条语句，可以实现在过程执行后将返回数据赋值给相关变量。';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = '执行/返回';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = '返回';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = '定义过程';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = '执行过程后返回一个结果值。';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = '警告:\n' +
    '此过程的输入项\n' +
    '出现重复';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = '过程';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = '调用一个无返回值的过程。';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = '调用无返回值的过程';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = '调用';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = '调用一个有返回值的过程。';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = '调用有返回值的过程';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = '输入项';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = '输入:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = '转到代码块定义处';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP = "该代码块未定义，删除此代码块!";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = '当';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = '执行';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = '调用';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = '调用';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = '组件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = '组件';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = '设置';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = '为';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = '设置';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = '为';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = '组件';


//Misc
    Blockly.Msg.SHOW_WARNINGS = "显示警告";
    Blockly.Msg.HIDE_WARNINGS = "隐藏警告";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "你应该为所有的槽连接块";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "本代码块应与一个事件块或过程定义连接";

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION = "AI伴侣出现错误";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "网络连接故障";
    Blockly.Msg.REPL_NETWORK_ERROR = "网络故障";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART = "与AI伴侣通信故障，<br />请尝试重新启动AI伴侣并重新连接";
    Blockly.Msg.REPL_OK = "确定";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK = "检查AI伴侣版本";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'AI伴侣已版本过期，点击“确定”升级。';
    Blockly.Msg.REPL_EMULATORS = "查看模拟器";
    Blockly.Msg.REPL_DEVICES = "设备";
    Blockly.Msg.REPL_APPROVE_UPDATE = "您将被请求允许更新。";
    Blockly.Msg.REPL_NOT_NOW = "现在不";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 = "你正使用的AI伴侣已经过期，<br/><br/>本版本App Inventor适用的AI伴侣版本为";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE = "你正在使用的AI伴侣版本已过期，请尽快升级";
    Blockly.Msg.REPL_COMPANION_WRONG_PACKAGE = "你正在使用的AI伴侣是针对不同版本的APP Inventor创建的。请前往菜单栏的“帮助——>AI伴侣信息”获取正确的AI伴侣版本。";
    Blockly.Msg.REPL_DISMISS = "放弃";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "软件升级";
    Blockly.Msg.REPL_OK_LOWER = "确定";
    Blockly.Msg.REPL_GOT_IT = "升级完成";
    Blockly.Msg.REPL_UPDATE_INFO = '正在你的设备上安装更新。请查看移动设备(或模拟器)屏幕上的提示，同意安装软件。<br /><br />注意:更新完成后,请点击“完成”(不要点击“打开”)。然后再次进入App Inventor网页,点击“连接”菜单,选择“重置连接”。然后重新连接设备。';

    Blockly.Msg.REPL_UPDATE_NO_UPDATE = "无可用更新";
    Blockly.Msg.REPL_UPDATE_NO_CONNECTION = "你必须与AI伴侣连接才能开始更新";
    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "无法将升级包发送给设备或模拟器";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "无法从App Inventor服务器下载升级包（服务器无响应）";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "无法从App Inventor服务器获取更新信息(服务器无响应)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "正在从App Inventor服务器下载升级包，请耐心等待。";
    Blockly.Msg.REPL_RUNTIME_ERROR = "运行故障";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS = "<br/><i>注意：</i>&nbsp;5秒钟后将报告另一条错误信息。";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "正在通过USB线连接";
    Blockly.Msg.REPL_STARTING_EMULATOR = "正在启动Android模拟器<br/>请等待：可能需要一至两分钟";
    Blockly.Msg.REPL_CONNECTING = "连接中...";
    Blockly.Msg.REPL_CANCEL = "取消";
    Blockly.Msg.REPL_GIVE_UP = "放弃";
    Blockly.Msg.REPL_KEEP_TRYING = "重试";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "连接失败";
    Blockly.Msg.REPL_NO_START_EMULATOR = "无法在模拟器中启动AI伴侣";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "是否已插入USB线？";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE = "AI2没有查找到设备，请确认USB是否连接以及驱动程序是否正常安装。";
    Blockly.Msg.REPL_HELPER_Q = "是否已运行aiStarter助手程序？";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'aiStarter助手程序未运行，<br />是否需要<a href="http://appinventor.mit.edu" target="_blank">帮助</a>?';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB已连接，请等待";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = "秒，确保相关资源全部加载。";
    Blockly.Msg.REPL_EMULATOR_STARTED = "模拟器已启动，请等待";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE = "正在所连接的设备中启动AI伴侣";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR = "正在模拟器中启动AI伴侣";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING = "AI伴侣启动中，请等待";
    Blockly.Msg.REPL_VERIFYING_COMPANION = "检查AI伴侣启动状态....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION = "连接到AI伴侣";
    Blockly.Msg.REPL_TRY_AGAIN1 = "无法连接AI伴侣，请重试。";
    Blockly.Msg.REPL_YOUR_CODE_IS = "编码为";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "你真的要这么做吗？";
    Blockly.Msg.REPL_FACTORY_RESET = "这将使模拟器恢复出厂模式，如果此前升级过AI伴侣，则需要重新升级。";

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "你确定要删除 %1 个模块吗?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "生成 Yail";
    Blockly.Msg.DO_IT = "预览代码块";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "清除出现錯误";
    Blockly.Msg.CAN_NOT_DO_IT = "无法预览代码块";
    Blockly.Msg.CONNECT_TO_DO_IT = '你必须要连接AI伴侣或者模拟器才能使用"预览代码块"功能';

// Clock Component Menu Items
    Blockly.Msg.TIME_YEARS = "年";
    Blockly.Msg.TIME_MONTHS = "月";
    Blockly.Msg.TIME_WEEKS = "周";
    Blockly.Msg.TIME_DAYS = "日";
    Blockly.Msg.TIME_HOURS = "时";
    Blockly.Msg.TIME_MINUTES = "分";
    Blockly.Msg.TIME_SECONDS = "秒";
    Blockly.Msg.TIME_DURATION = "时段";
  }
};

// Initalize language definition to English
Blockly.Msg.zh.hans.switch_blockly_language_to_zh_hans.init();
Blockly.Msg.zh.switch_language_to_chinese_cn.init();
