/**
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * http://blockly.googlecode.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * @fileoverview Vietnamese strings.
 * @author fraser@google.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.messages.vn');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

if (!Blockly.Language) Blockly.Language = {};
Blockly.Language.switch_language_to_vietnamese = {
    // Switch language to Vietnamese.
    category: '',  
    helpUrl: '',
    init: function() {  
        // Context menus.
        Blockly.MSG_DUPLICATE_BLOCK = 'Tạo Bản Sao';
        Blockly.MSG_REMOVE_COMMENT = 'Xóa Chú Giải';
        Blockly.MSG_ADD_COMMENT = 'Thêm Chú Giải';
        Blockly.MSG_EXTERNAL_INPUTS = 'Chỗ Gắn Bên Ngoài';
        Blockly.MSG_INLINE_INPUTS = 'Chỗ Gắn Cùng Dòng';
        Blockly.MSG_DELETE_BLOCK = 'Xóa Mảnh Này';
        Blockly.MSG_DELETE_X_BLOCKS = 'Xóa %1 Mảnh';
        Blockly.MSG_COLLAPSE_BLOCK = 'Thu Gọn';
        Blockly.MSG_EXPAND_BLOCK = 'Mở Lớn';
        Blockly.MSG_DISABLE_BLOCK = 'Ngưng Tác Dụng';
        Blockly.MSG_ENABLE_BLOCK = 'Phục Hồi Tác Dụng';
        Blockly.MSG_HELP = 'Trợ Giúp';

        // Variable renaming.
        Blockly.MSG_CHANGE_VALUE_TITLE = 'Thay giá trị thành:';
        Blockly.MSG_NEW_VARIABLE = 'Biến mới...';
        Blockly.MSG_NEW_VARIABLE_TITLE = 'Tên của biến mới:';
        Blockly.MSG_RENAME_VARIABLE = 'Thay tên biến...';
        Blockly.MSG_RENAME_VARIABLE_TITLE = 'Thay tên tất cả "%1" biến này thành:';

        // Toolbox.
        Blockly.MSG_VARIABLE_CATEGORY = 'Variables';
        Blockly.MSG_PROCEDURE_CATEGORY = 'Procedures';

        // Colour Blocks.
        Blockly.LANG_COLOUR_PICKER_HELPURL = 'http://en.wikipedia.org/wiki/Color';
        Blockly.LANG_COLOUR_PICKER_TOOLTIP = 'Click the square to pick a color.';

        // Control Blocks.
        Blockly.LANG_CATEGORY_CONTROLS = 'Control';
        Blockly.LANG_CONTROLS_IF_HELPURL = 'http://code.google.com/p/blockly/wiki/If_Then';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_1 = 'Nếu điều kiện đúng, thực hiện các lệnh.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_2 = 'Nếu điều kiện đúng, thực hiện các lệnh đầu.\n' +
            'Nếu sai, thực hiện các lệnh sau.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_3 = 'Nếu điều kiện đúng, thực hiện các lệnh đầu.\n' +
            'Nếu không, nếu điều kiện thứ hai đúng, thực hiện các lệnh thứ hai.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_4 = 'Nếu điều kiện đúng, thực hiện các lệnh đầu.\n' +
            'Nếu không, nếu điều kiện thứ hai đúng, thực hiện các lệnh thứ hai.\n' +
            'Nếu không điều kiện nào đúng, thực hiện các lệnh cuối cùng.';
        Blockly.LANG_CONTROLS_IF_MSG_IF = 'nếu';
        Blockly.LANG_CONTROLS_IF_MSG_ELSEIF = 'nếu không nếu';
        Blockly.LANG_CONTROLS_IF_MSG_ELSE = 'nếu không';
        Blockly.LANG_CONTROLS_IF_MSG_THEN = 'thực hiện';

        Blockly.LANG_CONTROLS_IF_IF_TITLE_IF = 'nếu';
        Blockly.LANG_CONTROLS_IF_IF_TOOLTIP = 'Thêm, bỏ, hoặc đổi thứ tự các mảnh con\n' +
            'để tạo cấu trúc mới cho mảnh nếu.';

        Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'nếu không nếu';
        Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Thêm một điều kiện vào mảnh nếu.';

        Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'nếu không';
        Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Cuối cùng, khi không điều kiện nào đúng.';

        Blockly.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://code.google.com/p/blockly/wiki/Repeat';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'lặp lại';
        Blockly.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'thực hiện';
        Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'trong khi';
        Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'cho đến khi';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Miễn là điều kiện còn đúng, thì thực hiện các lệnh.';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Miễn là điều kiện còn sai, thì thực hiện các lệnh\n' +
            'Khi điều kiện đúng thì ngưng.';

        Blockly.LANG_CONTROLS_FOR_HELPURL = 'http://en.wikipedia.org/wiki/For_loop';
        Blockly.LANG_CONTROLS_FOR_INPUT_WITH = 'đếm theo';
        Blockly.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
        Blockly.LANG_CONTROLS_FOR_INPUT_FROM = 'từ';
        Blockly.LANG_CONTROLS_FOR_INPUT_TO = 'đến';
        Blockly.LANG_CONTROLS_FOR_INPUT_DO = 'thực hiện';
        Blockly.LANG_CONTROLS_FOR_TOOLTIP = 'Đếm từ số đầu đến số cuối.\n' +
            'Khi đến mỗi số, gán số vào biến "%1"\n' +
            'rồi thực hiện các lệnh.';

        Blockly.LANG_CONTROLS_FORRANGE_HELPURL = 'http://en.wikipedia.org/wiki/For_loop';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'for range';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'i';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_START = 'start';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_END = 'end';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'step';
        Blockly.LANG_CONTROLS_FORRANGE_INPUT_DO = 'do';
        Blockly.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Runs the blocks in the \'do\' section for each numeric '
                + 'value in the range from start to end, stepping the value each time.  Use the given '
                + 'variable name to refer to the current value.';

        Blockly.LANG_CONTROLS_FOREACH_HELPURL = 'http://en.wikipedia.org/wiki/For_loop';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'với mỗi thành phần';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_VAR = 'x';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'trong danh sách';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_DO = 'thực hiện';
        Blockly.LANG_CONTROLS_FOREACH_TOOLTIP = 'Trong một danh sách, lấy từng thành phần, gán vào\n' +
            'biến "%1", rồi thực hiện một số lệnh.';

        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'vòng lặp';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'thoát';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'sang lần lặp tiếp theo';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Thoát khỏi vòng lặp hiện tại.';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Bỏ qua phần còn lại trong vòng lặp này,\n' +
            'và sang lần lặp tiếp theo.';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_WARNING ='Cẩn thận:\n' +
            'Mảnh này chỉ có thế dùng\n' +
            'trong các vòng lặp.';

        Blockly.LANG_CONTROLS_WHILE_HELPURL = '';
        Blockly.LANG_CONTROLS_WHILE_TITLE = 'while';
        Blockly.LANG_CONTROLS_WHILE_INPUT_TEST = 'test';
        Blockly.LANG_CONTROLS_WHILE_INPUT_DO = 'do';
        Blockly.LANG_CONTROLS_WHILE_TOOLTIP = 'Runs the blocks in the \'do\' section while the test is '
                + 'true.';

        Blockly.LANG_CONTROLS_CHOOSE_HELPURL = '';
        Blockly.LANG_CONTROLS_CHOOSE_TITLE = 'choose';
        Blockly.LANG_CONTROLS_CHOOSE_INPUT_TEST = 'test';
        Blockly.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'then-return';
        Blockly.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'else-return';
        Blockly.LANG_CONTROLS_CHOOSE_TOOLTIP = 'If the condition being tested is true,'
               + 'return the result of evaluating the expression attached to the \'then-return\' slot;'
               + 'otherwise return the result of evaluating the expression attached to the \'else-return\' slot;'
               + 'at most one of the return slot expressions will be evaluated.';

        Blockly.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'do';
        Blockly.LANG_CONTROLS_DO_THEN_RETURN_INPUT_THEN_RETURN = 'then-return';

        Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'evaluate';
        Blockly.LANG_CONTROLS_NOTHING_TITLE = 'nothing';

        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = '';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'open another screen';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'screenName';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Opens a new screen in a multiple screen app.';

        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = '';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'open another screen with start value';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'screenName';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'startValue';
        Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Opens a new screen in a multiple screen app and passes the '
                + 'start value to that screen.';

        Blockly.LANG_CONTROLS_GET_START_VALUE_HELPURL = '';
        Blockly.LANG_CONTROLS_GET_START_VALUE_TITLE = 'get start value';
        Blockly.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'screenName';
        Blockly.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'startValue';
        Blockly.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Returns the value that was passed to this screen when it '
                + 'was opened, typically by another screen in a multiple-screen app. If no value was '
                + 'passed, returns the empty text.';

        Blockly.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = '';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_TITLE_CLOSE = 'close screen';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Close the current screen';

        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = '';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE_CLOSE = 'close screen with value';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'result';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Closes the current screen and returns a result to the '
                + 'screen that opened this one.';

        Blockly.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = '';
        Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TITLE_CLOSE = 'close application';
        Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Closes all screens in this app and stops the app.';

        Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = '';
        Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_INPUT_GET = 'get plain start text';
        Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'Returns the plain text that was passed to this screen when '
                + 'it was started by another app. If no value was passed, returns the empty text. For '
                + 'multiple screen apps, use get start value rather than get plain start text.';
                
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = '';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE_CLOSE = 'close screen with plain text';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'text';
        Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Closes the current screen and returns text to the app that '
                + 'opened this one. For multiple screen apps, use close screen with value rather than '
                + 'close screen with plain text.';

        // Logic Blocks.
        Blockly.LANG_CATEGORY_LOGIC = 'Logic';
        Blockly.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Tests whether two things are equal. \n' +
                'The things being compared can be any thing, not only numbers.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Hoàn trả giá trị "đúng" (true) nếu giá trị hai đầu vào không bằng nhau.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LT = 'Hoàn trả giá trị "đúng" (true)\n' +
            'nếu đầu vào thứ nhất nhỏ hơn đầu vào thứ hai.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LTE = 'Hoàn trả giá trị "đúng" (true)\n' +
            'nếu đầu vào thứ nhất nhỏ hơn hoặc bằng đầu vào thứ hai.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GT = 'Hoàn trả giá trị "đúng" (true)\n' +
            'nếu đầu vào thứ nhất lớn hơn đầu vào thứ hai.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GTE = 'Hoàn trả giá trị "đúng" (true)\n' +
            'nếu đầu vào thứ nhất lớn hơn hoặc bằng đầu vào thứ hai.';

        Blockly.LANG_LOGIC_OPERATION_HELPURL = 'http://code.google.com/p/blockly/wiki/And_Or';
        Blockly.LANG_LOGIC_OPERATION_AND = 'và';
        Blockly.LANG_LOGIC_OPERATION_OR = 'hoặc';
        Blockly.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Returns true if all inputs are true.';
        Blockly.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Returns true if any input is true.';

        Blockly.LANG_LOGIC_NEGATE_HELPURL = 'http://code.google.com/p/blockly/wiki/Not';
        Blockly.LANG_LOGIC_NEGATE_INPUT_NOT = 'không';
        Blockly.LANG_LOGIC_NEGATE_TOOLTIP = 'Hoàn trả "đúng" (true) nếu đầu vào sai.\n' +
            'Hoàn trả "sai" (false) nếu đầu vào đúng.';

        Blockly.LANG_LOGIC_BOOLEAN_HELPURL = 'http://code.google.com/p/blockly/wiki/True_False';
        Blockly.LANG_LOGIC_BOOLEAN_TRUE = 'đúng';
        Blockly.LANG_LOGIC_BOOLEAN_FALSE = 'sai';
        Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Reports the boolean true.';
        Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Reports the boolean false.';

        // Math Blocks.
        Blockly.LANG_CATEGORY_MATH = 'Math';
        Blockly.LANG_MATH_NUMBER_HELPURL = 'http://en.wikipedia.org/wiki/Number';
        Blockly.LANG_MATH_NUMBER_TOOLTIP = 'Một con số.';
        Blockly.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'number';

        Blockly.LANG_MATH_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Return true if both numbers are equal to each other.';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Return true if both numbers are not equal to each other.';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_LT = 'Return true if the first numbers is smaller\n' +
            'than the second numbers.';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Return true if the first numbers is smaller\n' +
            'than or equal to the second numbers.';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_GT = 'Return true if the first numbers is greater\n' +
            'than the second numbers.';
        Blockly.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Return true if the first numbers is greater\n' +
            'than or equal to the second numbers.';

        Blockly.LANG_MATH_ARITHMETIC_HELPURL = 'http://en.wikipedia.org/wiki/Arithmetic';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Hoàn trả tổng của hai con số.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Hoàn trả hiệu của hai con số.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Hoàn trả tích của hai con số.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Hoàn trả thương của hai con số.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Hoàn trả số lũy thừa với số thứ nhất là cơ số\n' +
            'và số thứ hai là số mũ.';

        Blockly.LANG_MATH_CHANGE_HELPURL = 'http://en.wikipedia.org/wiki/Programming_idiom#Incrementing_a_counter';
        Blockly.LANG_MATH_CHANGE_TITLE_CHANGE = 'cộng vào';
        Blockly.LANG_MATH_CHANGE_TITLE_ITEM = 'vật';
        Blockly.LANG_MATH_CHANGE_INPUT_BY = 'giá trị';
        Blockly.LANG_MATH_CHANGE_TOOLTIP = 'Cộng số đầu vào vào biến "%1".';

        Blockly.LANG_MATH_SINGLE_HELPURL = 'http://en.wikipedia.org/wiki/Square_root';
        Blockly.LANG_MATH_SINGLE_OP_ROOT = 'square root';
        Blockly.LANG_MATH_SINGLE_OP_ABSOLUTE = 'absolute';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Return the square root of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Return the absolute value of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Return the negation of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_LN = 'Return the natural logarithm of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_LOG10 = 'Return the base 10 logarithm of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Return e to the power of a number.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Return 10 to the power of a number.';

        Blockly.LANG_MATH_ROUND_HELPURL = 'http://en.wikipedia.org/wiki/Rounding';
        Blockly.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Round a number up or down.';
        Blockly.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Rounds the input to the smallest\n' +
              'number not less then the input';
        Blockly.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Rounds the input to the largest\n' +
              'number not greater then the input';
        Blockly.LANG_MATH_ROUND_OPERATOR_ROUND = 'round';
        Blockly.LANG_MATH_ROUND_OPERATOR_CEILING = 'ceiling';
        Blockly.LANG_MATH_ROUND_OPERATOR_FLOOR = 'floor';

        Blockly.LANG_MATH_TRIG_HELPURL = 'http://en.wikipedia.org/wiki/Trigonometric_functions';
        Blockly.LANG_MATH_TRIG_TOOLTIP_SIN = 'Hoàn trả Sin của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_COS = 'Hoàn trả Cos của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_TAN = 'Hoàn trả Tang của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Hoàn trả Arcsin của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Hoàn trả Arccos của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Hoàn trả Arctang của một góc (theo độ).';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Provides the angle in the range (-180, +180]\n' +
                'degrees with the given rectangular coordinates.';

        Blockly.LANG_MATH_ONLIST_HELPURL = '';
        Blockly.LANG_MATH_ONLIST_INPUT_OFLIST = 'của một danh sách';
        Blockly.LANG_MATH_ONLIST_OPERATOR_SUM = 'tổng';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MIN = 'số nhỏ nhất';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MAX = 'số lớn nhât';
        Blockly.LANG_MATH_ONLIST_OPERATOR_AVERAGE = 'giá trị trung bình';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MEDIAN = 'số trung vị';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MODE = 'các mode';
        Blockly.LANG_MATH_ONLIST_OPERATOR_STD_DEV = 'độ lệch chuẩn';
        Blockly.LANG_MATH_ONLIST_OPERATOR_RANDOM = 'một số bất kỳ';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_SUM = 'Hoàn trả tổng số của tất cả các số trong danh sách.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Hoàn trả số nhỏ nhất trong tất cả các số trong danh sách.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Hoàn trả số lớn nhất trong tất cả các số trong danh sách.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_AVERAGE = 'Hoàn trả giá trị trung bình từ của danh sách số.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MEDIAN = 'Hoàn trả số trung vị của danh sách số.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MODE = 'Hoàn trả các số có mặt nhiều nhất trong danh sách.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_STD_DEV = 'Hoàn trả độ lệch chuẩn của danh sách số.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_RANDOM = 'Hoàn trả một số bất kỳ từ các số trong danh sách.';

        Blockly.LANG_MATH_CONSTRAIN_HELPURL = 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN = 'giới hạn';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_LOW = 'không dưới';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_HIGH = 'không hơn';
        Blockly.LANG_MATH_CONSTRAIN_TOOLTIP = 'Giới hạn số đầu vào để không dưới số thứ nhất và không hơn số thứ hai.';

        Blockly.LANG_MATH_DIVIDE_HELPURL = 'http://en.wikipedia.org/wiki/Modulo_operation';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'modulo of';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'remainder of';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'quotient of';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Return the modulo.';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Return the remainder.';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Return the quotient.';

        Blockly.LANG_MATH_RANDOM_INT_HELPURL = 'http://en.wikipedia.org/wiki/Random_number_generation';
        Blockly.LANG_MATH_RANDOM_INT_INPUT_RANDOM = 'random integer from';
        Blockly.LANG_MATH_RANDOM_INT_INPUT_FROM = 'Một số nguyên bất kỳ từ';
        Blockly.LANG_MATH_RANDOM_INT_INPUT_TO = 'đến';
        Blockly.LANG_MATH_RANDOM_INT_TOOLTIP = 'Hoàn trả một số nguyên bất kỳ\n' +
            'lớn hơn hoặc bằng số đầu và nhỏ hơn hoặc bằng số sau.';

        Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://en.wikipedia.org/wiki/Random_number_generation';
        Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'phân số bất kỳ';
        Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Hoàn trả một phân số bất kỳ\n' +
            'không nhỏ hơn 0.0 và không lớn hơn 1.0.';

        Blockly.LANG_MATH_RANDOM_SEED_HELPURL = 'http://en.wikipedia.org/wiki/Random_number_generation';
        Blockly.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'random set seed';
        Blockly.LANG_MATH_RANDOM_SEED_INPUT_TO = 'to';
        Blockly.LANG_MATH_RANDOM_SEED_TOOLTIP = 'specifies a numeric seed\n' +
                'for the random number generator';

        Blockly.LANG_MATH_CONVERT_ANGLES_HELPURL = '';
        Blockly.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'convert';
        Blockly.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'radians to degrees';
        Blockly.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'degrees to radians';
        Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Returns the degree value in the range\n' +
              '[0, 360) corresponding to its radians argument.';
        Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Returns the radian value in the range\n' +
              '[-\u03C0, +\u03C0) corresponding to its degrees argument.';
                
        Blockly.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = '';
        Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'format as decimal';
        Blockly.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'number';
        Blockly.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'places';
        Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Returns the number formatted as a decimal\n' +
                'with a specified number of places.';

        Blockly.LANG_MATH_IS_A_NUMBER_HELPURL = '';
        Blockly.LANG_MATH_IS_A_NUMBER_INPUT_NUM = 'is a number?';
        Blockly.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Tests if something is a number.';

        // Text Blocks.
        Blockly.LANG_CATEGORY_TEXT = 'Text';
        Blockly.LANG_TEXT_TEXT_HELPURL = 'http://en.wikipedia.org/wiki/String_(computer_science)';
        Blockly.LANG_TEXT_TEXT_TOOLTIP = 'Một ký tự, một từ, hay một dòng.';

        Blockly.LANG_TEXT_JOIN_HELPURL = '';
        Blockly.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'tạo văn bản từ';
        Blockly.LANG_TEXT_JOIN_TOOLTIP = 'Tạo một văn bản từ các thành phần.';
        Blockly.LANG_TEXT_JOIN_TITLE_JOIN = 'kết nối';

        Blockly.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'string';
        Blockly.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

        Blockly.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_APPEND_TO = 'ở ';
        Blockly.LANG_TEXT_APPEND_APPENDTEXT = 'thêm văn bản';
        Blockly.LANG_TEXT_APPEND_VARIABLE = 'vật';
        Blockly.LANG_TEXT_APPEND_TOOLTIP = 'Thêm một mảng văn bản vào biến "%1".';

        Blockly.LANG_TEXT_LENGTH_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH = 'độ dài của';
        Blockly.LANG_TEXT_LENGTH_TOOLTIP = 'Hoàn trả số lượng ký tự (kể cả khoảng trắng)\n' +
            'trong văn bản đầu vào.';

        Blockly.LANG_TEXT_ISEMPTY_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'trống không';
        Blockly.LANG_TEXT_ISEMPTY_TOOLTIP = 'Hoàn trả “đúng nếu văn bản không có ký tự nào.';

        Blockly.LANG_TEXT_COMPARE_HELPURL = '';
        Blockly.LANG_TEXT_COMPARE_INPUT_COMPARE = 'compare texts';
        Blockly.LANG_TEXT_COMPARE_TOOLTIP_LT = 'Tests whether text1 is lexicographically less than text2.\n'
              + 'if one text is the prefix of the other, the shorter text is\n'
              + 'considered smaller. Uppercase characters precede lowercase characters.';
        Blockly.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = 'Tests whether text strings are identical, ie., have the same\n'
              + 'characters in the same order. This is different from ordinary =\n'
              + 'in the case where the text strings are numbers: 123 and 0123 are =\n' 
              + 'but not text =.';
        Blockly.LANG_TEXT_COMPARE_TOOLTIP_GT = 'Reports whether text1 is lexicographically greater than text2.\n'
              + 'if one text is the prefix of the other, the shorter text is considered smaller.\n'
              + 'Uppercase characters precede lowercase characters.';

        Blockly.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_TEXT_ENDSTRING_INPUT = 'ký tự trong văn bản';
        Blockly.LANG_TEXT_ENDSTRING_TOOLTIP = 'Hoàn trả một số ký tự ở đầu hay cuối văn bản, theo độ dài nêu ra.';
        Blockly.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'lấy từ đầu';
        Blockly.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'lấy từ cuối';

        Blockly.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_TEXT_INDEXOF_TITLE_FIND = 'tìm sự có mặt';
        Blockly.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'của';
        Blockly.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'trong văn bản';
        Blockly.LANG_TEXT_INDEXOF_TOOLTIP = 'Hoàn trả vị trí xuất hiện đầu/cuối\n' +
            'của văn bản thứ nhất trong văn bản thứ hai.\n' +
            'Nếu không tìm thấy thì hoàn trả số 0.';
        Blockly.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'đầu tiên';
        Blockly.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'cuối cùng';

        Blockly.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_TEXT_CHARAT_INPUT_AT = 'letter at';
        Blockly.LANG_TEXT_CHARAT_INPUT_INTEXT = 'trong văn bản';
        Blockly.LANG_TEXT_CHARAT_TOOLTIP = 'Hoàn trả ký tự ở vị trí đặt ra.';

        Blockly.LANG_TEXT_CHANGECASE_HELPURL = '';
        Blockly.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'upcase';
        Blockly.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'downcase';
        Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Returns a copy of its text string argument converted to uppercase.';
        Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Returns a copy of its text string argument converted to lowercase.';

        Blockly.LANG_TEXT_TRIM_HELPURL = '';
        Blockly.LANG_TEXT_TRIM_TITLE_TRIM = 'trim';
        Blockly.LANG_TEXT_TRIM_TOOLTIP = 'Returns a copy of it text string arguments with any\n'
                + 'leading or trailing spaces removed.';

        Blockly.LANG_TEXT_STARTS_AT_HELPURL = '';
        Blockly.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'starts at';
        Blockly.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'text';
        Blockly.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'piece';
        Blockly.LANG_TEXT_STARTS_AT_TOOLTIP = 'Returns the starting index of the piece in the text.\n'
                + 'where index 1 denotes the beginning of the text. Returns 0 if the\n'
                + 'piece is not in the text.';

        Blockly.LANG_TEXT_CONTAINS_HELPURL = '';
        Blockly.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'contains';
        Blockly.LANG_TEXT_CONTAINS_INPUT_TEXT = 'text';
        Blockly.LANG_TEXT_CONTAINS_INPUT_PIECE = 'piece';
        Blockly.LANG_TEXT_CONTAINS_TOOLTIP = 'Tests whether the piece is contained in the text.';

        Blockly.LANG_TEXT_SPLIT_HELPURL = '';
        Blockly.LANG_TEXT_SPLIT_INPUT_TEXT = 'text';
        Blockly.LANG_TEXT_SPLIT_INPUT_AT = 'at';
        Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'split at first';
        Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'split at first of any';
        Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'split';
        Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'split at any';
        Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = '';
        Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Splits the text into two pieces separated by the first\n'
              + 'occurrence of any of the elements in the list \'at\'\n'
              + 'and returns these pieces. Returns a one-element list with original\n'
              + 'text if \'at\' is not contained in the text.';
        Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Split the text into pieces separated by the\n'
              + 'occurrences of \'at\' and return the list of these pieces.\n'
              + 'Returns a one-element list with the original\n'
              + 'text if \'at\' is not contained in the text.';
        Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Split the text into pieces separated by the\n'
              + 'occurrences of any of the elements in the list \'at\' and\n'
              + 'return the list of these pieces. Returns a one-element list\n'
              + 'with the original text if \'at\' is not contained in the text.';

        Blockly.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_PRINT_TITLE_PRINT = 'in lên màng hình';
        Blockly.LANG_TEXT_PRINT_TOOLTIP = 'In ra màng hình một văn bản, con số, hay một giá trị đầu vào khác.';

        Blockly.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
        Blockly.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'Xin người dùng nhập vào';
        Blockly.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'với dòng hướng dẫn';
        Blockly.LANG_TEXT_PROMPT_TOOLTIP_NUMBER = 'Xin người dùng nhập vào một con số.\n';
        Blockly.LANG_TEXT_PROMPT_TOOLTIP_TEXT = 'Xin người dùng nhập vào một văn bản.\n';
        Blockly.LANG_TEXT_PROMPT_TYPE_TEXT = 'văn bản';
        Blockly.LANG_TEXT_PROMPT_TYPE_NUMBER = 'con số';

        Blockly.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = '';
        Blockly.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'split at spaces';
        Blockly.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Split the text into pieces separated by spaces.';

        Blockly.LANG_TEXT_SEGMENT_HELPURL = '';
        Blockly.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'segment';
        Blockly.LANG_TEXT_SEGMENT_INPUT_START = 'start';
        Blockly.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'length';
        Blockly.LANG_TEXT_SEGMENT_INPUT_TEXT = 'text';
        Blockly.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Extracts the segment of the given length from the given text\n'
                + 'starting from the given text starting from the given position. Position\n'
                + '1 denotes the beginning of the text.';

        Blockly.LANG_TEXT_REPLACE_ALL_HELPURL = '';
        Blockly.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'segment';
        Blockly.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'text';
        Blockly.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'replace all';
        Blockly.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'replacement';
        Blockly.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Returns a new text obtained by replacing all occurrences\n'
                + 'of the segment with the replacement.';
                
        // Lists Blocks.
        Blockly.LANG_CATEGORY_LISTS = 'Lists';
        Blockly.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
        Blockly.LANG_LISTS_CREATE_EMPTY_TITLE = 'tạo danh sách trống';
        Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Hoàn trả một danh sách, với độ dài 0, không có thành tố nào cả';

        Blockly.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = '';
        Blockly.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'make a list';
        Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Create a list with any number of items.';

        Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_INPUT_LIST = 'danh sách';
        Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Thêm, bỏ, hoặc sắp xếp lại các thành phần để tạo dựng mảnh danh sách này.';

        Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'vật';
        Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Thêm vật vào danh sách.';

        Blockly.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = '';
        Blockly.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'select list item';
        Blockly.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'index';
        Blockly.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Get the nth item from a list.';

        Blockly.LANG_LISTS_IS_IN_HELPURL = '';
        Blockly.LANG_LISTS_IS_IN_TITLE_IS_IN = 'is in list?';
        Blockly.LANG_LISTS_IS_IN_INPUT_THING = 'thing';
        Blockly.LANG_LISTS_IS_IN_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_IS_IN_TOOLTIP = 'Retuns true if the the thing is an item in the list, and '
                + 'false if not.';
                
        Blockly.LANG_LISTS_POSITION_IN_HELPURL = '';
        Blockly.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'position in list';
        Blockly.LANG_LISTS_POSITION_IN_INPUT_THING = 'thing';
        Blockly.LANG_LISTS_POSITION_IN_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_POSITION_IN_TOOLTIP = 'Find the position of the thing in the list. If it\'s not in '
                + 'the list, return 0.';

        Blockly.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = '';
        Blockly.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'pick a random item';
        Blockly.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Pick an item at random from the list.';
                
        Blockly.LANG_LISTS_REPLACE_ITEM_HELPURL = '';
        Blockly.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'replace list item';
        Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'index';
        Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'replacement';
        Blockly.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Replaces the nth item in a list.';

        Blockly.LANG_LISTS_REMOVE_ITEM_HELPURL = '';
        Blockly.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'remove list item';
        Blockly.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'index';
        Blockly.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Removes the item at the specified position from the list.';

        Blockly.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_REPEAT_TITLE_CREATE = 'tạo danh sách gồm một vật';
        Blockly.LANG_LISTS_REPEAT_INPUT_REPEATED = 'lặp lại';
        Blockly.LANG_LISTS_REPEAT_INPUT_TIMES = 'lần';
        Blockly.LANG_LISTS_REPEAT_TOOLTIP = 'Tạo danh sách gồm một số lượng vật nhất định\n' +
            'với mỗi vật đều giống nhau.';

        Blockly.LANG_LISTS_LENGTH_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_LISTS_LENGTH_INPUT_LENGTH = 'length of list';
        Blockly.LANG_LISTS_LENGTH_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_LENGTH_TOOLTIP = 'Counts the number of items in a list.';

        Blockly.LANG_LISTS_APPEND_LIST_HELPURL = '';
        Blockly.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'append to list';
        Blockly.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'list1';
        Blockly.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'list2';
        Blockly.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Appends all the items in list2 onto the end of list1. After '
                + 'the append, list1 will include these additional elements, but list2 will be unchanged.';

        Blockly.LANG_LISTS_ADD_ITEMS_HELPURL = '';
        Blockly.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'add items to list';
        Blockly.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'item';
        Blockly.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Adds items to the end of a list.';

        Blockly.LANG_LISTS_COPY_HELPURL = '';
        Blockly.LANG_LISTS_COPY_TITLE_COPY = 'copy list';
        Blockly.LANG_LISTS_COPY_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_COPY_TOOLTIP = 'Makes a copy of a list, including copying all sublists';

        Blockly.LANG_LISTS_IS_LIST_HELPURL = '';
        Blockly.LANG_LISTS_IS_LIST_TITLE_IS_LIST = 'is a list?';
        Blockly.LANG_LISTS_IS_LIST_INPUT_THING = 'thing';
        Blockly.LANG_LISTS_IS_LIST_TOOLTIP = 'Tests if something is a list.';

        Blockly.LANG_LISTS_TO_CSV_ROW_HELPURL = '';
        Blockly.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'list to csv row';
        Blockly.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Interprets the list as a row of a table and returns a CSV '
                + '\(comma-separated value\) text representing the row. Each item in the row list is '
                + 'considered to be a field, and is quoted with double-quotes in the resulting CSV text. '
                + 'Items are separated by commas. The returned row text does not have a line separator at '
                + 'the end.';

        Blockly.LANG_LISTS_FROM_CSV_ROW_HELPURL = '';
        Blockly.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'list from csv row';
        Blockly.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'text';
        Blockly.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Parses a text as a CSV \(comma-separated value\) formatted '
                + 'row to produce a list of fields. It is an error for the row text to contain unescaped '
                + 'newlines inside fields \(effectively, multiple lines\). It is okay for the row text to '
                + 'end in a single newline or CRLF.';
                
        Blockly.LANG_LISTS_TO_CSV_TABLE_HELPURL = '';
        Blockly.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'list to csv table';
        Blockly.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interprets the list as a table in row-major format and '
                + 'returns a CSV \(comma-separated value\) text representing the table. Each item in the '
                + 'list should itself be a list representing a row of the CSV table. Each item in the row '
                + 'list is considered to be a field, and is quoted with double-quotes in the resulting CSV '
                + 'text. In the returned text, items in rows are separated by commas and rows are '
                + 'separated by CRLF \(\\r\\n\).';

        Blockly.LANG_LISTS_FROM_CSV_TABLE_HELPURL = '';
        Blockly.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'list from csv table';
        Blockly.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'text';
        Blockly.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Parses a text as a CSV \(comma-separated value\) formatted '
                + 'table to produce a list of rows, each of which is a list of fields. Rows can be '
                + 'separated by newlines \(\\n\) or CRLF \(\\r\\n\).';
                
        Blockly.LANG_LISTS_INSERT_ITEM_HELPURL = '';
        Blockly.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'insert list item';
        Blockly.LANG_LISTS_INSERT_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_INSERT_INPUT_INDEX = 'index';
        Blockly.LANG_LISTS_INSERT_INPUT_ITEM = 'item';
        Blockly.LANG_LISTS_INSERT_TOOLTIP = 'Insert an item into a list at the specified position.';

        Blockly.LANG_LISTS_IS_EMPTY_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_LISTS_TITLE_IS_EMPTY = 'trống rỗng';
        Blockly.LANG_LISTS_INPUT_LIST = 'list';
        Blockly.LANG_LISTS_TOOLTIP = 'Hoàn trả “đúng" nếu danh sách không có thành tử nào.';

        Blockly.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND = 'tìm sự có mặt';
        Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'của vật';
        Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'trong danh sách';
        Blockly.LANG_LISTS_INDEX_OF_TOOLTIP = 'Hoàn trả vị trí xuất hiện đầu/cuối\n' +
            'của vật trong danh sách.\n' +
            'Nếu không tìm thấy thì hoàn trả số 0.';
        Blockly.LANG_LISTS_INDEX_OF_FIRST = 'đầu tiên';
        Blockly.LANG_LISTS_INDEX_OF_LAST = 'cuối cùng';

        Blockly.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_GET_INDEX_TITLE_GET = 'get item at';
        Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'trong danh sách';
        Blockly.LANG_LISTS_GET_INDEX_TOOLTIP = 'Returns the value at the specified position in a list.';

        Blockly.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_SET = 'set item at';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_TO = 'thành';
        Blockly.LANG_LISTS_SET_INDEX_TOOLTIP = 'Sets the value at the specified position in a list.';

        // Variables Blocks.
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = '';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'initialize global';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'tên';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'to';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Returns the value of this variable.';

        Blockly.LANG_VARIABLES_GET_HELPURL = 'http://en.wikipedia.org/wiki/Variable_(computer_science)';
        Blockly.LANG_VARIABLES_GET_TITLE_GET = 'get';
        Blockly.LANG_VARIABLES_GET_INPUT_ITEM = 'mục';
        Blockly.LANG_VARIABLES_GET_TOOLTIP = 'Returns the value of this variable.';

        Blockly.LANG_VARIABLES_SET_HELPURL = '';
        Blockly.LANG_VARIABLES_SET_TITLE_SET = 'set';
        Blockly.LANG_VARIABLES_SET_TO = 'to';
        Blockly.LANG_VARIABLES_SET_TOOLTIP = 'Sets this variable to be equal to the input.';

        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = '';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'initialize local';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_NAME = 'tên';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'to';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'in do';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_IN_RETURN = 'in return';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '';

        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = '';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'initialize local';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'tên';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'to';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'in return';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '';

        Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'local names';
        Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

        // Procedures Blocks.
        Blockly.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'thủ tục';
        Blockly.LANG_PROCEDURES_DEFNORETURN_DO = 'thực hiện';
        Blockly.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Một thủ tục không có giá trị hoàn trả.';
        
        Blockly.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'then-return';
        Blockly.LANG_PROCEDURES_DOTHENRETURN_DO = 'do';

        Blockly.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
        Blockly.LANG_PROCEDURES_DEFRETURN_DO = Blockly.LANG_PROCEDURES_DEFNORETURN_DO;
        Blockly.LANG_PROCEDURES_DEFRETURN_RETURN = 'hoàn trả';
        Blockly.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Một thủ tục có giá trị hoàn trả.';

        Blockly.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Cẩn thận:\n' +
            'Thủ tục này có lặp lại\n' +
            'tên các tham số.';

        Blockly.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_CALLNORETURN_CALL = 'thực hiện';
        Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'thủ tục';
        Blockly.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Chạy một thủ tục không có giá trị hoàn trả.';

        Blockly.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.LANG_PROCEDURES_CALLNORETURN_CALL;
        Blockly.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
        Blockly.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Chạy một thủ tục có giá trị hoàn trả.';

        Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'các tham số';
        Blockly.LANG_PROCEDURES_MUTATORARG_TITLE = 'biến:';

        Blockly.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Làm nổi bật thủ tục';

        Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
        Blockly.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';
        
        // Components Blocks.
        Blockly.LANG_COMPONENT_BLOCK_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_TITLE_WHEN = 'when ';
        Blockly.LANG_COMPONENT_BLOCK_TITLE_DO = 'do';
        
        Blockly.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = 'call ';
        
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = 'call';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = 'for component';
        
        Blockly.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';
        
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = 'of component';
        
        Blockly.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = 'set ';
        Blockly.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = ' to';
        
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = 'set ';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = ' to';
        Blockly.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = 'of component';
    }
};