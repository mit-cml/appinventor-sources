// Text strings (factored out to make multi-language easier).

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

// Context menus.
Blockly.MSG_DUPLICATE_BLOCK = 'Duplicate';
Blockly.MSG_REMOVE_COMMENT = 'Remove Comment';
Blockly.MSG_ADD_COMMENT = 'Add Comment';
Blockly.MSG_EXTERNAL_INPUTS = 'External Inputs';
Blockly.MSG_INLINE_INPUTS = 'Inline Inputs';
Blockly.MSG_HORIZONTAL_PARAMETERS = 'Arrange Parameters Horizontally';
Blockly.MSG_VERTICAL_PARAMETERS = 'Arrange Parameters Vertically';
Blockly.MSG_DELETE_BLOCK = 'Delete Block';
Blockly.MSG_DELETE_X_BLOCKS = 'Delete %1 Blocks';
Blockly.MSG_COLLAPSE_BLOCK = 'Collapse Block';
Blockly.MSG_EXPAND_BLOCK = 'Expand Block';
Blockly.MSG_DISABLE_BLOCK = 'Disable Block';
Blockly.MSG_ENABLE_BLOCK = 'Enable Block';
Blockly.MSG_HELP = 'Help';
Blockly.MSG_COLLAPSE_ALL = 'Collapse Blocks';
Blockly.MSG_EXPAND_ALL = 'Expand Blocks';
Blockly.MSG_ARRANGE_H = 'Arrange Blocks Horizontally';
Blockly.MSG_ARRANGE_V = 'Arrange Blocks Vertically';
Blockly.MSG_ARRANGE_S = 'Arrange Blocks Diagonally';
Blockly.MSG_SORT_W = 'Sort Blocks by Width';
Blockly.MSG_SORT_H = 'Sort Blocks by Height';
Blockly.MSG_SORT_C = 'Sort Blocks by Category';

// Variable renaming.
Blockly.MSG_CHANGE_VALUE_TITLE = 'Change value:';
Blockly.MSG_NEW_VARIABLE = 'New variable...';
Blockly.MSG_NEW_VARIABLE_TITLE = 'New variable name:';
Blockly.MSG_RENAME_VARIABLE = 'Rename variable...';
Blockly.MSG_RENAME_VARIABLE_TITLE = 'Rename all "%1" variables to:';

// Toolbox.
Blockly.MSG_VARIABLE_CATEGORY = 'Variables';
Blockly.MSG_PROCEDURE_CATEGORY = 'Procedures';

// Warnings/Errors
Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = "This block cannot be in a definition";
Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = "Select a valid item in the drop down.";
Blockly.ERROR_DUPLICATE_EVENT_HANDLER = "This is a duplicate event handler for this component.";

// Colour Blocks.
Blockly.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
Blockly.LANG_COLOUR_PICKER_TOOLTIP = 'Click the square to pick a color.';
Blockly.LANG_COLOUR_BLACK = 'black';
Blockly.LANG_COLOUR_WHITE = 'white';
Blockly.LANG_COLOUR_RED = 'red';
Blockly.LANG_COLOUR_PINK = 'pink';
Blockly.LANG_COLOUR_ORANGE = 'orange';
Blockly.LANG_COLOUR_YELLOW = 'yellow';
Blockly.LANG_COLOUR_GREEN = 'green';
Blockly.LANG_COLOUR_CYAN = 'cyan';
Blockly.LANG_COLOUR_BLUE = 'blue';
Blockly.LANG_COLOUR_MAGENTA = 'magenta';
Blockly.LANG_COLOUR_LIGHT_GRAY = 'light gray';
Blockly.LANG_COLOUR_DARK_GRAY = 'dark gray';
Blockly.LANG_COLOUR_GRAY = 'gray';
Blockly.LANG_COLOUR_SPLIT_COLOUR = 'split color';
Blockly.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
Blockly.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = "A list of four elements, each in the range 0 to 255, representing the red, green, blue and alpha components.";
Blockly.LANG_COLOUR_MAKE_COLOUR = 'make color';
Blockly.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
Blockly.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = "A color with the given red, green, blue, and optionally alpha components";

// Control Blocks
Blockly.LANG_CATEGORY_CONTROLS = 'Control';
Blockly.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
Blockly.LANG_CONTROLS_IF_TOOLTIP_1 = 'If a value is true, then do some statements.';
Blockly.LANG_CONTROLS_IF_TOOLTIP_2 = 'If a value is true, then do the first block of statements.\n' +
  'Otherwise, do the second block of statements.';
Blockly.LANG_CONTROLS_IF_TOOLTIP_3 = 'If the first value is true, then do the first block of statements.\n' +
  'Otherwise, if the second value is true, do the second block of statements.';
Blockly.LANG_CONTROLS_IF_TOOLTIP_4 = 'If the first value is true, then do the first block of statements.\n' +
  'Otherwise, if the second value is true, do the second block of statements.\n' +
  'If none of the values are true, do the last block of statements.';
Blockly.LANG_CONTROLS_IF_MSG_IF = 'if';
Blockly.LANG_CONTROLS_IF_MSG_ELSEIF = 'else if';
Blockly.LANG_CONTROLS_IF_MSG_ELSE = 'else';
Blockly.LANG_CONTROLS_IF_MSG_THEN = 'then';

Blockly.LANG_CONTROLS_IF_IF_TITLE_IF = 'if';
Blockly.LANG_CONTROLS_IF_IF_TOOLTIP = 'Add, remove, or reorder sections\n' +
  'to reconfigure this if block.';

Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'else if';
Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Add a condition to the if block.';

Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'else';
Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Add a final, catch-all condition to the if block.';

Blockly.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
Blockly.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'repeat';
Blockly.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'while';
Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'until';
Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'While a value is true, then do some statements.';
Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'While a value is false, then do some statements.';
Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = 'Runs the blocks in the \'do\' section while the test is '
        + 'true.';

Blockly.LANG_CONTROLS_FOR_HELPURL = '';
Blockly.LANG_CONTROLS_FOR_INPUT_WITH = 'count with';
Blockly.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
Blockly.LANG_CONTROLS_FOR_INPUT_FROM = 'from';
Blockly.LANG_CONTROLS_FOR_INPUT_TO = 'to';
Blockly.LANG_CONTROLS_FOR_INPUT_DO = 'do';

Blockly.LANG_CONTROLS_FOR_TOOLTIP = 'Count from a start number to an end number.\n' +
  'For each count, set the current count number to\n' +
  'variable "%1", and then do some statements.';

Blockly.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'for each';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'number';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_START = 'from';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_END = 'to';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'by';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = 'for number in range';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = 'for ';
Blockly.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' in range';
Blockly.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Runs the blocks in the \'do\' section for each numeric '
  + 'value in the range from start to end, stepping the value each time.  Use the given '
  + 'variable name to refer to the current value.';

Blockly.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'for each';
Blockly.LANG_CONTROLS_FOREACH_INPUT_VAR = 'item';
Blockly.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'in list';
Blockly.LANG_CONTROLS_FOREACH_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = 'for item in list';
Blockly.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = 'for ';
Blockly.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' in list';
Blockly.LANG_CONTROLS_FOREACH_TOOLTIP = 'Runs the blocks in the \'do\'  section for each item in '
  + 'the list.  Use the given variable name to refer to the current list item.';

Blockly.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


Blockly.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'of loop';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'break out';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'continue with next iteration';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Break out of the containing loop.';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Skip the rest of this loop, and\n' +
  'continue with the next iteration.';
Blockly.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Warning:\n' +
  'This block may only\n' +
  'be used within a loop.';

Blockly.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';;
Blockly.LANG_CONTROLS_WHILE_TITLE = 'while';
Blockly.LANG_CONTROLS_WHILE_INPUT_TEST = 'test';
Blockly.LANG_CONTROLS_WHILE_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = 'while';
Blockly.LANG_CONTROLS_WHILE_TOOLTIP = 'Runs the blocks in the \'do\' section while the test is '
  + 'true.';

Blockly.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
Blockly.LANG_CONTROLS_CHOOSE_TITLE = 'if'
Blockly.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
Blockly.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'then';
Blockly.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'else';
Blockly.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = 'if';
Blockly.LANG_CONTROLS_CHOOSE_TOOLTIP = 'If the condition being tested is true,'
  + 'return the result of evaluating the expression attached to the \'then-return\' slot;'
  + 'otherwise return the result of evaluating the expression attached to the \'else-return\' slot;'
  + 'at most one of the return slot expressions will be evaluated.';

Blockly.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
Blockly.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = 'result';
Blockly.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = 'Runs the blocks in \'do\' and returns a statement. Useful if you need to run a procedure before returning a value to a variable.';
Blockly.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = 'do/result';

Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'evaluate but ignore result'
Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
Blockly.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'eval but ignore'
Blockly.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = 'Runs the connected block of code and ignores the return value (if any). Useful if need to call a procedure with a return value but don\'t need the value.';

/* [lyn, 10/14/13] Removed for now. May come back some day.
Blockly.LANG_CONTROLS_NOTHING_TITLE = 'nothing';
Blockly.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
Blockly.LANG_CONTROLS_NOTHING_TOOLTIP = 'Returns nothing. Used to initialize variables or can be plugged into a return socket if no value needed to return. this is equivalent to null or None.';
*/

Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'open another screen';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'screenName';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = 'open screen';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Opens a new screen in a multiple screen app.';

Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'open another screen with start value';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'screenName';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'startValue';
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = 'open screen with value'
Blockly.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Opens a new screen in a multiple screen app and passes the '
  + 'start value to that screen.';

Blockly.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
Blockly.LANG_CONTROLS_GET_START_VALUE_TITLE = 'get start value';
Blockly.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'screenName';
Blockly.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'startValue';
Blockly.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = 'get start value';
Blockly.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Returns the value that was passed to this screen when it '
  + 'was opened, typically by another screen in a multiple-screen app. If no value was '
  + 'passed, returns the empty text.';

Blockly.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';;
Blockly.LANG_CONTROLS_CLOSE_SCREEN_TITLE = 'close screen';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = 'close screen';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Close the current screen';

Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';;
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = 'close screen with value';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'result';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = 'close screen with value';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Closes the current screen and returns a result to the '
  + 'screen that opened this one.';

Blockly.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';;
Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = 'close application';
Blockly.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = 'close application';
Blockly.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Closes all screens in this app and stops the app.';

Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = 'get plain start text';
Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = 'get plain start text';
Blockly.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'Returns the plain text that was passed to this screen when '
  + 'it was started by another app. If no value was passed, returns the empty text. For '
  + 'multiple screen apps, use get start value rather than get plain start text.';

Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = 'close screen with plain text';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'text';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = 'close screen with plain text';
Blockly.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Closes the current screen and returns text to the app that '
  + 'opened this one. For multiple screen apps, use close screen with value rather than '
  + 'close screen with plain text.';



// Logic Blocks.
Blockly.LANG_CATEGORY_LOGIC = 'Logic';
Blockly.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
Blockly.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
Blockly.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Tests whether two things are equal. \n' +
  'The things being compared can be any thing, not only numbers.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Return true if both inputs are not equal to each other.';

Blockly.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
Blockly.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
Blockly.LANG_LOGIC_OPERATION_AND = 'and';
Blockly.LANG_LOGIC_OPERATION_OR = 'or';
Blockly.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Return true if all inputs are true.';
Blockly.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Return true if any input is true.';

Blockly.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
Blockly.LANG_LOGIC_NEGATE_INPUT_NOT = 'not';
Blockly.LANG_LOGIC_NEGATE_TOOLTIP = 'Returns true if the input is false.\n' +
  'Returns false if the input is true.';

Blockly.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
Blockly.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
Blockly.LANG_LOGIC_BOOLEAN_TRUE = 'true';
Blockly.LANG_LOGIC_BOOLEAN_FALSE = 'false';
Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Returns the boolean true.';
Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Returns the boolean false.';

// Math Blocks.
Blockly.LANG_CATEGORY_MATH = 'Math';
Blockly.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
Blockly.LANG_MATH_NUMBER_TOOLTIP = 'Report the number shown.';
Blockly.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'number';

Blockly.LANG_MATH_COMPARE_HELPURL = '';
Blockly.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
Blockly.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
Blockly.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
Blockly.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
Blockly.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
Blockly.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
Blockly.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Return true if both numbers are equal to each other.';
Blockly.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Return true if both numbers are not equal to each other.';
Blockly.LANG_MATH_COMPARE_TOOLTIP_LT = 'Return true if the first number is smaller\n' +
  'than the second number.';
Blockly.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Return true if the first number is smaller\n' +
  'than or equal to the second number.';
Blockly.LANG_MATH_COMPARE_TOOLTIP_GT = 'Return true if the first number is greater\n' +
  'than the second number.';
Blockly.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Return true if the first number is greater\n' +
  'than or equal to the second number.';

Blockly.LANG_MATH_ARITHMETIC_HELPURL_ADD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#add';
Blockly.LANG_MATH_ARITHMETIC_HELPURL_MINUS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#subtract';
Blockly.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#multiply';
Blockly.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#divide';
Blockly.LANG_MATH_ARITHMETIC_HELPURL_POWER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#exponent';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Return the sum of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Return the difference of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Return the product of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Return the quotient of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Return the first number raised to\n' +
  'the power of the second number.';


/*Blockly.LANG_MATH_CHANGE_TITLE_CHANGE = 'change';
Blockly.LANG_MATH_CHANGE_TITLE_ITEM = 'item';
Blockly.LANG_MATH_CHANGE_INPUT_BY = 'by';
Blockly.LANG_MATH_CHANGE_TOOLTIP = 'Add a number to variable "%1".';*/


Blockly.LANG_MATH_SINGLE_OP_ROOT = 'square root';
Blockly.LANG_MATH_SINGLE_OP_ABSOLUTE = 'absolute';
Blockly.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Return the square root of a number.';
Blockly.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
Blockly.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Return the absolute value of a number.';
Blockly.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
Blockly.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Return the negation of a number.';
Blockly.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
Blockly.LANG_MATH_SINGLE_TOOLTIP_LN = 'Return the natural logarithm of a number.';
Blockly.LANG_MATH_SINGLE_HELPURL_LN ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
Blockly.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Return e to the power of a number.';
Blockly.LANG_MATH_SINGLE_HELPURL_EXP ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
/*Blockly.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Return 10 to the power of a number.';*/

Blockly.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Round a number up or down.';
Blockly.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
Blockly.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Rounds the input to the smallest\n' +
  'number not less then the input';
Blockly.LANG_MATH_ROUND_HELPURL_CEILING =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
Blockly.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Rounds the input to the largest\n' +
  'number not greater then the input';
Blockly.LANG_MATH_ROUND_HELPURL_FLOOR =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
Blockly.LANG_MATH_ROUND_OPERATOR_ROUND = 'round';
Blockly.LANG_MATH_ROUND_OPERATOR_CEILING = 'ceiling';
Blockly.LANG_MATH_ROUND_OPERATOR_FLOOR = 'floor';


Blockly.LANG_MATH_TRIG_TOOLTIP_SIN = 'Provides the sine of the given angle in degrees.';
Blockly.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
Blockly.LANG_MATH_TRIG_TOOLTIP_COS = 'Provides the cosine of the given angle in degrees.';
Blockly.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
Blockly.LANG_MATH_TRIG_TOOLTIP_TAN = 'Provides the tangent of the given angle in degrees.';
Blockly.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Provides the angle in the range (-90,+90]\n' +
  'degrees with the given sine value.';
Blockly.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Provides the angle in the range [0, 180)\n' +
  'degrees with the given cosine value.';
Blockly.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Provides the angle in the range (-90, +90)\n' +
  'degrees with the given tangent value.';
ATAN : Blockly.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Provides the angle in the range (-180, +180]\n' +
  'degrees with the given rectangular coordinates.';
Blockly.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

Blockly.LANG_MATH_ONLIST_OPERATOR_MIN = 'min';
Blockly.LANG_MATH_ONLIST_OPERATOR_MAX = 'max';
//TODO: I don't think any of this is useful anymore...Delete?
/*Blockly.LANG_MATH_ONLIST_HELPURL = '';
Blockly.LANG_MATH_ONLIST_INPUT_OFLIST = 'of list';
Blockly.LANG_MATH_ONLIST_OPERATOR_SUM = 'sum';
Blockly.LANG_MATH_ONLIST_OPERATOR_AVERAGE = 'average';
Blockly.LANG_MATH_ONLIST_OPERATOR_MEDIAN = 'median';
Blockly.LANG_MATH_ONLIST_OPERATOR_MODE = 'modes';
Blockly.LANG_MATH_ONLIST_OPERATOR_STD_DEV = 'standard deviation';
Blockly.LANG_MATH_ONLIST_OPERATOR_RANDOM = 'random item';
Blockly.LANG_MATH_ONLIST_TOOLTIP_SUM = 'Return the sum of all the numbers in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Return the smallest of its arguments..';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Return the largest of its arguments..';
Blockly.LANG_MATH_ONLIST_TOOLTIP_AVERAGE = 'Return the arithmetic mean of the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MEDIAN = 'Return the median number in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MODE = 'Return a list of the most common item(s) in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_STD_DEV = 'Return the standard deviation of the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_RANDOM = 'Return a random element from the list.';

Blockly.LANG_MATH_CONSTRAIN_HELPURL = 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29';
Blockly.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN = 'constrain';
Blockly.LANG_MATH_CONSTRAIN_INPUT_LOW = 'between (low)';
Blockly.LANG_MATH_CONSTRAIN_INPUT_HIGH = 'and (high)';
Blockly.LANG_MATH_CONSTRAIN_TOOLTIP = 'Constrain a number to be between the specified limits (inclusive).';*/


Blockly.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'modulo of';
Blockly.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'remainder of';
Blockly.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'quotient of';
Blockly.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Return the modulo.';
Blockly.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
Blockly.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Return the remainder.';
Blockly.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
Blockly.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Return the quotient.';
Blockly.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

Blockly.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'random integer';
Blockly.LANG_MATH_RANDOM_INT_INPUT_FROM = 'from';
Blockly.LANG_MATH_RANDOM_INT_INPUT_TO = 'to';
Blockly.LANG_MATH_RANDOM_INT_TOOLTIP = 'Returns a random integer between the upper bound\n' +
  'and the lower bound. The bounds will be clipped to be smaller\n' +
  'than 2**30.';

Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'random fraction';
Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Return a random number between 0 and 1.';

Blockly.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
Blockly.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'random set seed';
Blockly.LANG_MATH_RANDOM_SEED_INPUT_TO = 'to';
Blockly.LANG_MATH_RANDOM_SEED_TOOLTIP = 'specifies a numeric seed\n' +
  'for the random number generator';

Blockly.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'convert';
Blockly.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'radians to degrees';
Blockly.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'degrees to radians';
Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Returns the degree value in the range\n' +
  '[0, 360) corresponding to its radians argument.';
Blockly.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
Blockly.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Returns the radian value in the range\n' +
  '[-\u03C0, +\u03C0) corresponding to its degrees argument.';
Blockly.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

Blockly.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'format as decimal';
Blockly.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'number';
Blockly.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'places';
Blockly.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Returns the number formatted as a decimal\n' +
  'with a specified number of places.';

Blockly.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
Blockly.LANG_MATH_IS_A_NUMBER_INPUT_NUM = 'is a number?';
Blockly.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Tests if something is a number.';

// Text Blocks.
Blockly.LANG_CATEGORY_TEXT = 'Text';
Blockly.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
Blockly.LANG_TEXT_TEXT_TOOLTIP = 'A text string.';

Blockly.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
Blockly.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'create text with';
Blockly.LANG_TEXT_JOIN_TOOLTIP = 'Appends all the inputs to form a single text string.\n'
        + 'If there are no inputs, makes an empty text.';
Blockly.LANG_TEXT_JOIN_TITLE_JOIN = 'join';

Blockly.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'string';
Blockly.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

Blockly.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_APPEND_TO = 'to';
Blockly.LANG_TEXT_APPEND_APPENDTEXT = 'append text';
Blockly.LANG_TEXT_APPEND_VARIABLE = 'item';
Blockly.LANG_TEXT_APPEND_TOOLTIP = 'Append some text to variable "%1".';

Blockly.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH = 'length';
Blockly.LANG_TEXT_LENGTH_TOOLTIP = 'Returns number of letters (including spaces)\n' +
  'in the provided text.';

Blockly.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'is empty';
Blockly.LANG_TEXT_ISEMPTY_TOOLTIP = 'Returns true if the length of the\n' + 'text is 0, false otherwise.';

Blockly.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
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

/*Blockly.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_TEXT_ENDSTRING_INPUT = 'letters in text';
        Blockly.LANG_TEXT_ENDSTRING_TOOLTIP = 'Returns specified number of letters at the beginning or end of the text.';
Blockly.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'first';
Blockly.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'last';*/

/*Blockly.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_TEXT_INDEXOF_TITLE_FIND = 'find';
Blockly.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'occurrence of text';
Blockly.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'in text';
Blockly.LANG_TEXT_INDEXOF_TOOLTIP = 'Returns the index of the first/last occurrence\n' +
  'of first text in the second text.\n' +
  'Returns 0 if text is not found.';
Blockly.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'first';
Blockly.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'last';*/

/*Blockly.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_TEXT_CHARAT_INPUT_AT = 'letter at';
Blockly.LANG_TEXT_CHARAT_INPUT_INTEXT = 'in text';
Blockly.LANG_TEXT_CHARAT_TOOLTIP = 'Returns the letter at the specified position.';*/

Blockly.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'upcase';
Blockly.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'downcase';
Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Returns a copy of its text string argument converted to uppercase.';
Blockly.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Returns a copy of its text string argument converted to lowercase.';
Blockly.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

Blockly.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
Blockly.LANG_TEXT_TRIM_TITLE_TRIM = 'trim';
Blockly.LANG_TEXT_TRIM_TOOLTIP = 'Returns a copy of its text string arguments with any\n'
  + 'leading or trailing spaces removed.';

Blockly.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
Blockly.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'starts at';
Blockly.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'text';
Blockly.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'piece';
Blockly.LANG_TEXT_STARTS_AT_TOOLTIP = 'Returns the starting index of the piece in the text.\n'
  + 'where index 1 denotes the beginning of the text. Returns 0 if the\n'
  + 'piece is not in the text.';

Blockly.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
Blockly.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'contains';
Blockly.LANG_TEXT_CONTAINS_INPUT_TEXT = 'text';
Blockly.LANG_TEXT_CONTAINS_INPUT_PIECE = 'piece';
Blockly.LANG_TEXT_CONTAINS_TOOLTIP = 'Tests whether the piece is contained in the text.';

Blockly.LANG_TEXT_SPLIT_HELPURL = '';
Blockly.LANG_TEXT_SPLIT_INPUT_TEXT = 'text';
Blockly.LANG_TEXT_SPLIT_INPUT_AT = 'at';
Blockly.LANG_TEXT_SPLIT_INPUT_AT_LIST = 'at (list)';
Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'split at first';
Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'split at first of any';
Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'split';
Blockly.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'split at any';
Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = 'Divides the given text into two pieces using the location of the first occurrence of \n'
      + 'the text \'at\' as the dividing point, and returns a two-item list consisting of the piece \n'
      + 'before the dividing point and the piece after the dividing point. \n'
      + 'Splitting "apple,banana,cherry,dogfood" with a comma as the splitting point \n'
      + 'returns a list of two items: the first is the text "apple" and the second is the text \n'
      + '"banana,cherry,dogfood". \n'
      + 'Notice that the comma after "apple" does not appear in the result, \n'
      + 'because that is the dividing point.';
Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Divides the given text into a two-item list, using the first location of any item \n'
      + 'in the list \'at\' as the dividing point. \n\n'
      + 'Splitting "I love apples bananas apples grapes" by the list "(ba,ap)" returns \n'
      + 'a list of two items, the first being "I love" and the second being \n'
      + '"ples bananas apples grapes."';
Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Divides text into pieces using the text \'at\' as the dividing points and produces a list of the results.  \n'
      + 'Splitting "one,two,three,four" at "," (comma) returns the list "(one two three four)". \n'
      + 'Splitting "one-potato,two-potato,three-potato,four" at "-potato", returns the list "(one two three four)".'
Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
Blockly.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Divides the given text into a list, using any of the items in the list \'at\' as the \n'
      + 'dividing point, and returns a list of the results. \n'
      + 'Splitting "appleberry,banana,cherry,dogfood" with \'at\' as the two-element list whose \n'
      + 'first item is a comma and whose second item is "rry" returns a list of four items: \n'
      + '"(applebe banana che dogfood)".'
Blockly.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatany';

/*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_PRINT_TITLE_PRINT = 'print';
Blockly.LANG_TEXT_PRINT_TOOLTIP = 'Print the specified text, number or other value.';*/

/*Blockly.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
Blockly.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'prompt for';
Blockly.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'with message';
Blockly.LANG_TEXT_PROMPT_TOOLTIP = 'Prompt for user input with the specified text.';
Blockly.LANG_TEXT_PROMPT_TYPE_TEXT = 'text';
Blockly.LANG_TEXT_PROMPT_TYPE_NUMBER = 'number';*/

Blockly.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitspaces';
Blockly.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'split at spaces';
Blockly.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Split the text into pieces separated by spaces.';

Blockly.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
Blockly.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'segment';
Blockly.LANG_TEXT_SEGMENT_INPUT_START = 'start';
Blockly.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'length';
Blockly.LANG_TEXT_SEGMENT_INPUT_TEXT = 'text';
Blockly.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Extracts the segment of the given length from the given text\n'
  + 'starting from the given text starting from the given position. Position\n'
  + '1 denotes the beginning of the text.';

Blockly.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
Blockly.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'segment';
Blockly.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'text';
Blockly.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'replace all';
Blockly.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'replacement';
Blockly.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Returns a new text obtained by replacing all occurrences\n'
  + 'of the segment with the replacement.';

// Lists Blocks.
Blockly.LANG_CATEGORY_LISTS = 'Lists';
//Blockly.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
Blockly.LANG_LISTS_CREATE_EMPTY_TITLE = 'create empty list';
//Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Returns a list, of length 0, containing no data records';

Blockly.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
Blockly.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'make a list';
Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Create a list with any number of items.';

Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'list';
Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Add, remove, or reorder sections to reconfigure this list block.';

Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'item';
Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Add an item to the list.';

Blockly.LANG_LISTS_ADD_ITEM_TITLE = 'item';
Blockly.LANG_LISTS_ADD_ITEM_TOOLTIP = 'Add an item to the list.';
Blockly.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

Blockly.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
Blockly.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'select list item';
Blockly.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'list';
Blockly.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'index';
Blockly.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Returns the item at position index in the list.';

Blockly.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
Blockly.LANG_LISTS_IS_IN_TITLE_IS_IN = 'is in list?';
Blockly.LANG_LISTS_IS_IN_INPUT_THING = 'thing';
Blockly.LANG_LISTS_IS_IN_INPUT_LIST = 'list';
Blockly.LANG_LISTS_IS_IN_TOOLTIP = 'Returns true if the the thing is an item in the list, and '
  + 'false if not.';

Blockly.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
Blockly.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'index in list';
Blockly.LANG_LISTS_POSITION_IN_INPUT_THING = 'thing';
Blockly.LANG_LISTS_POSITION_IN_INPUT_LIST = 'list';
Blockly.LANG_LISTS_POSITION_IN_TOOLTIP = 'Find the position of the thing in the list. If it\'s not in '
  + 'the list, return 0.';

Blockly.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
Blockly.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'pick a random item';
Blockly.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'list';
Blockly.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Pick an item at random from the list.';

Blockly.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
Blockly.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'replace list item';
Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'list';
Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'index';
Blockly.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'replacement';
Blockly.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Replaces the nth item in a list.';

Blockly.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
Blockly.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'remove list item';
Blockly.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'list';
Blockly.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'index';
Blockly.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Removes the item at the specified position from the list.';

/*Blockly.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_REPEAT_TITLE_CREATE = 'create list with item';
Blockly.LANG_LISTS_REPEAT_INPUT_REPEATED = 'repeated';
Blockly.LANG_LISTS_REPEAT_INPUT_TIMES = 'times';
Blockly.LANG_LISTS_REPEAT_TOOLTIP = 'Creates a list consisting of the given value\n' +
  'repeated the specified number of times.';*/

Blockly.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
Blockly.LANG_LISTS_LENGTH_INPUT_LENGTH = 'length of list';
Blockly.LANG_LISTS_LENGTH_INPUT_LIST = 'list';
Blockly.LANG_LISTS_LENGTH_TOOLTIP = 'Counts the number of items in a list.';

Blockly.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
Blockly.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'append to list';
Blockly.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'list1';
Blockly.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'list2';
Blockly.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Appends all the items in list2 onto the end of list1. After '
  + 'the append, list1 will include these additional elements, but list2 will be unchanged.';

Blockly.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
Blockly.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'add items to list';
Blockly.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'item';
Blockly.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Adds items to the end of a list.';

Blockly.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = 'list';
Blockly.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = 'Add, remove, or reorder sections to reconfigure this list block.';

Blockly.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
Blockly.LANG_LISTS_COPY_TITLE_COPY = 'copy list';
Blockly.LANG_LISTS_COPY_INPUT_LIST = 'list';
Blockly.LANG_LISTS_COPY_TOOLTIP = 'Makes a copy of a list, including copying all sublists';

Blockly.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
Blockly.LANG_LISTS_IS_LIST_TITLE_IS_LIST = 'is a list?';
Blockly.LANG_LISTS_IS_LIST_INPUT_THING = 'thing';
Blockly.LANG_LISTS_IS_LIST_TOOLTIP = 'Tests if something is a list.';

Blockly.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
Blockly.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'list to csv row';
Blockly.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'list';
Blockly.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Interprets the list as a row of a table and returns a CSV '
  + '\(comma-separated value\) text representing the row. Each item in the row list is '
  + 'considered to be a field, and is quoted with double-quotes in the resulting CSV text. '
  + 'Items are separated by commas. The returned row text does not have a line separator at '
  + 'the end.';

Blockly.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
Blockly.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'list from csv row';
Blockly.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'text';
Blockly.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Parses a text as a CSV \(comma-separated value\) formatted '
  + 'row to produce a list of fields. It is an error for the row text to contain unescaped '
  + 'newlines inside fields \(effectively, multiple lines\). It is okay for the row text to '
  + 'end in a single newline or CRLF.';

Blockly.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
Blockly.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'list to csv table';
Blockly.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'list';
Blockly.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interprets the list as a table in row-major format and '
  + 'returns a CSV \(comma-separated value\) text representing the table. Each item in the '
  + 'list should itself be a list representing a row of the CSV table. Each item in the row '
  + 'list is considered to be a field, and is quoted with double-quotes in the resulting CSV '
  + 'text. In the returned text, items in rows are separated by commas and rows are '
  + 'separated by CRLF \(\\r\\n\).';

Blockly.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
Blockly.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'list from csv table';
Blockly.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'text';
Blockly.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Parses a text as a CSV \(comma-separated value\) formatted '
  + 'table to produce a list of rows, each of which is a list of fields. Rows can be '
  + 'separated by newlines \(\\n\) or CRLF \(\\r\\n\).';

Blockly.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
Blockly.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'insert list item';
Blockly.LANG_LISTS_INSERT_INPUT_LIST = 'list';
Blockly.LANG_LISTS_INSERT_INPUT_INDEX = 'index';
Blockly.LANG_LISTS_INSERT_INPUT_ITEM = 'item';
Blockly.LANG_LISTS_INSERT_TOOLTIP = 'Insert an item into a list at the specified position.';

Blockly.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
Blockly.LANG_LISTS_TITLE_IS_EMPTY = 'is list empty?';
Blockly.LANG_LISTS_INPUT_LIST = 'list';
Blockly.LANG_LISTS_IS_EMPTY_TOOLTIP = 'Returns true if the list is empty.';

Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = 'look up in pairs';
Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = 'key';
Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = 'pairs';
Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = 'notFound';
Blockly.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = 'Returns the value associated with the key in the list of pairs';

/*Blockly.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND = 'find';
Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'occurrence of item';
Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_INDEX_OF_TOOLTIP = 'Returns the index of the first/last occurrence\n' +
  'of the item in the list.\n' +
  'Returns 0 if text is not found.';
Blockly.LANG_LISTS_INDEX_OF_FIRST = 'first';
Blockly.LANG_LISTS_INDEX_OF_LAST = 'last';

Blockly.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_GET_INDEX_TITLE_GET = 'get item at';
Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_GET_INDEX_TOOLTIP = 'Returns the value at the specified position in a list.';

Blockly.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_SET_INDEX_INPUT_SET = 'set item at';
Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_SET_INDEX_INPUT_TO = 'to';
Blockly.LANG_LISTS_SET_INDEX_TOOLTIP = 'Sets the value at the specified position in a list.';*/

// Variables Blocks.
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#global';
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'initialize global';
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'name';
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'to';
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = 'global';
Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Creates a global variable and gives it the value of the attached blocks.';

Blockly.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
Blockly.LANG_VARIABLES_GET_TITLE_GET = 'get';
/* Blockly.LANG_VARIABLES_GET_INPUT_ITEM = 'item'; */ // [lyn, 10/14/13] unused
Blockly.LANG_VARIABLES_GET_COLLAPSED_TEXT = 'get';
Blockly.LANG_VARIABLES_GET_TOOLTIP = 'Returns the value of this variable.';

Blockly.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
Blockly.LANG_VARIABLES_SET_TITLE_SET = 'set';
/* Blockly.LANG_VARIABLES_SET_INPUT_ITEM = 'item'; */ // [lyn, 10/14/13] unused
Blockly.LANG_VARIABLES_SET_TITLE_TO = 'to';
Blockly.LANG_VARIABLES_SET_COLLAPSED_TEXT = 'set';
Blockly.LANG_VARIABLES_SET_TOOLTIP = 'Sets this variable to be equal to the input.';

Blockly.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'initialize local';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = 'name';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'to';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'in';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = 'local';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = 'Allows you to create variables that are only accessible in the do part of this block.';

Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
/* // These don't differ between the statement and expression
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'initialize local';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'name';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'to';
*/
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'in';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = 'local';
Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = 'Allows you to create variables that are only accessible in the return part of this block.';

Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'local names';
Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

Blockly.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = 'name';
Blockly.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
Blockly.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
Blockly.LANG_PROCEDURES_DEFNORETURN_DEFINE = 'to';
Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'procedure';
Blockly.LANG_PROCEDURES_DEFNORETURN_DO = 'do';
Blockly.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = 'to ';
Blockly.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'A procedure that does not return a value.';

Blockly.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
Blockly.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'result';
Blockly.LANG_PROCEDURES_DOTHENRETURN_DO = 'do';
Blockly.LANG_PROCEDURES_DOTHENRETURN_RETURN = 'result';
Blockly.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = 'Runs the blocks in \'do\' and returns a statement. Useful if you need to run a procedure before returning a value to a variable.';
Blockly.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = 'do/result';

Blockly.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
Blockly.LANG_PROCEDURES_DEFRETURN_DEFINE = 'to';
Blockly.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
Blockly.LANG_PROCEDURES_DEFRETURN_DO = Blockly.LANG_PROCEDURES_DEFNORETURN_DO;
Blockly.LANG_PROCEDURES_DEFRETURN_RETURN = 'result';
Blockly.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = 'to ';
Blockly.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'A procedure returning a result value.';

Blockly.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Warning:\n' +
    'This procedure has\n' +
    'duplicate inputs.';

Blockly.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

Blockly.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
Blockly.LANG_PROCEDURES_CALLNORETURN_CALL = 'call ';
Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'procedure';
Blockly.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = 'call ';
Blockly.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Call a procedure with no return value.';

Blockly.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
Blockly.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.LANG_PROCEDURES_CALLNORETURN_CALL;
Blockly.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
Blockly.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = 'call ';
Blockly.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Call a procedure with a return value.';

Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'inputs';
Blockly.LANG_PROCEDURES_MUTATORARG_TITLE = 'input:';

Blockly.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Highlight Procedure';

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


///////////////////
/* HelpURLs for Component Blocks */

//User Interface Components
Blockly.LANG_COMPONENT_BLOCK_BUTTON_HELPURL = '/reference/components/userinterface.html#Button';
Blockly.LANG_COMPONENT_BLOCK_BUTTON_PROPERTIES_HELPURL = '/reference/components/userinterface.html#buttonproperties';
Blockly.LANG_COMPONENT_BLOCK_BUTTON_EVENTS_HELPURL = '/reference/components/userinterface.html#buttonevents';

Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL = '/reference/components/userinterface.html#CheckBox';
Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#checkboxproperties';
Blockly.LANG_COMPONENT_BLOCK_CHECKBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#checkboxevents';

Blockly.LANG_COMPONENT_BLOCK_CLOCK_HELPURL = '/reference/components/userinterface.html#Clock';
Blockly.LANG_COMPONENT_BLOCK_CLOCK_PROPERTIES_HELPURL = '/reference/components/userinterface.html#clockproperties';
Blockly.LANG_COMPONENT_BLOCK_CLOCK_EVENTS_HELPURL = '/reference/components/userinterface.html#clockevents';
Blockly.LANG_COMPONENT_BLOCK_CLOCK_METHODS_HELPURL = '/reference/components/userinterface.html#clockmethods';

Blockly.LANG_COMPONENT_BLOCK_IMAGE_HELPURL = '/reference/components/userinterface.html#Image';
Blockly.LANG_COMPONENT_BLOCK_IMAGE_PROPERTIES_HELPURL = '/reference/components/userinterface.html#imageproperties';
Blockly.LANG_COMPONENT_BLOCK_IMAGE_EVENTS_HELPURL = '/reference/components/userinterface.html#imageevents';
Blockly.LANG_COMPONENT_BLOCK_IMAGE_METHODS_HELPURL = '/reference/components/userinterface.html#imagemethods';

Blockly.LANG_COMPONENT_BLOCK_LABEL_HELPURL = '/reference/components/userinterface.html#Label';
Blockly.LANG_COMPONENT_BLOCK_LABEL_PROPERTIES_HELPURL = '/reference/components/userinterface.html#labelproperties';
Blockly.LANG_COMPONENT_BLOCK_LABEL_EVENTS_HELPURL = '/reference/components/userinterface.html#labelevents';
Blockly.LANG_COMPONENT_BLOCK_LABEL_METHODS_HELPURL = '/reference/components/userinterface.html#labelmethods';

Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_HELPURL = '/reference/components/userinterface.html#ListPicker';
Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#listpickerproperties';
Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_EVENTS_HELPURL = '/reference/components/userinterface.html#listpickerevents';
Blockly.LANG_COMPONENT_BLOCK_LISTPICKER_METHODS_HELPURL = '/reference/components/userinterface.html#listpickermethods';

Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL = "/reference/components/userinterface.html#Notifier";
Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#notifierproperties';
Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL = '/reference/components/userinterface.html#notifierevents';
Blockly.LANG_COMPONENT_BLOCK_NOTIFIER_METHODS_HELPURL = '/reference/components/userinterface.html#notifiermethods';

Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';
Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#pwdboxproperties';
Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#pwdboxevents';
Blockly.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#pwdboxmethods';

Blockly.LANG_COMPONENT_BLOCK_SCREEN_HELPURL = '/reference/components/userinterface.html#Screen';
Blockly.LANG_COMPONENT_BLOCK_SCREEN_PROPERTIES_HELPURL = '/reference/components/userinterface.html#screenproperties';
Blockly.LANG_COMPONENT_BLOCK_SCREEN_EVENTS_HELPURL = '/reference/components/userinterface.html#screenevents';
Blockly.LANG_COMPONENT_BLOCK_SCREEN_METHODS_HELPURL = '/reference/components/userinterface.html#screenmethods';

Blockly.LANG_COMPONENT_BLOCK_SLIDER_HELPURL = '/reference/components/userinterface.html#Slider';
Blockly.LANG_COMPONENT_BLOCK_SLIDER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#sliderproperties';
Blockly.LANG_COMPONENT_BLOCK_SLIDER_EVENTS_HELPURL = '/reference/components/userinterface.html#sliderevents';
Blockly.LANG_COMPONENT_BLOCK_SLIDER_METHODS_HELPURL = '/reference/components/userinterface.html#slidermethods';

Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_HELPURL = '/reference/components/userinterface.html#TextBox';
Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#textboxproperties';
Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#textboxevents';
Blockly.LANG_COMPONENT_BLOCK_TEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#textboxmethods';

Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL = "/reference/components/userinterface.html#WebViewer";
Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#webviewerproperties';
Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL = '/reference/components/userinterface.html#webviewerevents';
Blockly.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL = '/reference/components/userinterface.html#webviewermethods';

//Layout components
Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL = "/reference/components/layout.html#HorizontalArrangement";
Blockly.LANG_COMPONENT_BLOCK_HORIZARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#horizarrangeproperties';

Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL = "/reference/components/layout.html#VerticalArrangement";
Blockly.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#vertarrangeproperties';

Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL = "/reference/components/layout.html#TableArrangement";
Blockly.LANG_COMPONENT_BLOCK_TABLEARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#tablearrangeproperties';

//Media components
Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_HELPURL = '/reference/components/media.html#Camcorder';
Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_PROPERTIES_HELPURL = '/reference/components/media.html#camcorderproperties';
Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_EVENTS_HELPURL = '/reference/components/media.html#camcorderevents';
Blockly.LANG_COMPONENT_BLOCK_CAMCORDER_METHODS_HELPURL = '/reference/components/media.html#camcordermethods';

Blockly.LANG_COMPONENT_BLOCK_CAMERA_HELPURL = '/reference/components/media.html#Camera';
Blockly.LANG_COMPONENT_BLOCK_CAMERA_PROPERTIES_HELPURL = '/reference/components/media.html#cameraproperties';
Blockly.LANG_COMPONENT_BLOCK_CAMERA_EVENTS_HELPURL = '/reference/components/media.html#cameraevents';
Blockly.LANG_COMPONENT_BLOCK_CAMERA_METHODS_HELPURL = '/reference/components/media.html#cameramethods';

Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_HELPURL = '/reference/components/media.html#ImagePicker';
Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_PROPERTIES_HELPURL = '/reference/components/media.html#imagepickerproperties';
Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_EVENTS_HELPURL = '/reference/components/media.html#imagepickerevents';
Blockly.LANG_COMPONENT_BLOCK_IMAGEPICKER_METHODS_HELPURL = '/reference/components/media.html#imagepickermethods';

Blockly.LANG_COMPONENT_BLOCK_PLAYER_HELPURL = '/reference/components/media.html#Player';
Blockly.LANG_COMPONENT_BLOCK_PLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#playerproperties';
Blockly.LANG_COMPONENT_BLOCK_PLAYER_EVENTS_HELPURL = '/reference/components/media.html#playerevents';
Blockly.LANG_COMPONENT_BLOCK_PLAYER_METHODS_HELPURL = '/reference/components/media.html#playermethods';

Blockly.LANG_COMPONENT_BLOCK_SOUND_HELPURL = '/reference/components/media.html#Sound';
Blockly.LANG_COMPONENT_BLOCK_SOUND_PROPERTIES_HELPURL = '/reference/components/media.html#soundproperties';
Blockly.LANG_COMPONENT_BLOCK_SOUND_EVENTS_HELPURL = '/reference/components/media.html#soundevents';
Blockly.LANG_COMPONENT_BLOCK_SOUND_METHODS_HELPURL = '/reference/components/media.html#soundmethods';

Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL = "/reference/components/media.html#SoundRecorder";
Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL = '/reference/components/media.html#soundrecorderproperties';
Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL = '/reference/components/media.html#soundrecorderevents';
Blockly.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL = '/reference/components/media.html#soundrecordermethods';

Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL = "/reference/components/media.html#SpeechRecognizer";
Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL = '/reference/components/media.html#speechrecognizerproperties';
Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL = '/reference/components/media.html#speechrecognizerevents';
Blockly.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL = '/reference/components/media.html#speechrecognizermethods';

Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL = "/reference/components/media.html#TextToSpeech";
Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL = '/reference/components/media.html#texttospeechproperties';
Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL = '/reference/components/media.html#texttospeechevents';
Blockly.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL = '/reference/components/media.html#texttospeechmethods';

Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL = '/reference/components/media.html#VideoPlayer';
Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#videoplayerproperties';
Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL = '/reference/components/media.html#videoplayerevents';
Blockly.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL = '/reference/components/media.html#videoplayermethods';

// Drawing and Animation components
Blockly.LANG_COMPONENT_BLOCK_BALL_HELPURL = "/reference/components/animation.html#Ball";
Blockly.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL = '/reference/components/animation.html#ballproperties';
Blockly.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL = '/reference/components/animation.html#ballevents';
Blockly.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL = '/reference/components/animation.html#ballmethods';

Blockly.LANG_COMPONENT_BLOCK_CANVAS_HELPURL = '/reference/components/animation.html#Canvas';
Blockly.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL = '/reference/components/animation.html#canvasproperties';
Blockly.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL = '/reference/components/animation.html#canvasevents';
Blockly.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL = '/reference/components/animation.html#canvasmethods';

Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL = "/reference/components/animation.html#ImageSprite";
Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL = '/reference/components/animation.html#imagespriteproperties';
Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL = '/reference/components/animation.html#imagespriteevents';
Blockly.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL = '/reference/components/animation.html#imagespritemethods';

//Sensor components
Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL = "/reference/components/sensors.html#AccelerometerSensor";
Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#accelerometersensorproperties';
Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#accelerometersensorevents';
Blockly.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#accelerometersensormethods';

Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL = "/reference/components/sensors.html#BarcodeScanner";
Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL = '/reference/components/sensors.html#barcodescannerproperties';
Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL = '/reference/components/sensors.html#barcodescannerevents';
Blockly.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL = '/reference/components/sensors.html#barcodescannermethods';

Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL = "/reference/components/sensors.html#LocationSensor";
Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#locationsensorproperties';
Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#locationsensorevents';
Blockly.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#locationsensormethods';

Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL = "/reference/components/sensors.html#OrientationSensor";
Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#orientationsensorproperties';
Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#orientationsensorevents';
Blockly.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#orientationsensormethods';

//Social components
Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL = "/reference/components/social.html#ContactPicker";
Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#contactpickerproperties';
Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL = '/reference/components/social.html#contactpickerevents';
Blockly.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL = '/reference/components/social.html#contactpickermethods';

Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL = "/reference/components/social.html#EmailPicker";
Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#emailpickerproperties';
Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL = '/reference/components/social.html#emailpickerevents';
Blockly.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL = '/reference/components/social.html#emailpickermethods';

Blockly.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL = "/reference/components/social.html#PhoneCall";
Blockly.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL = '/reference/components/social.html#phonecallproperties';
Blockly.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL = '/reference/components/social.html#phonecallevents';
Blockly.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL = '/reference/components/social.html#phonecallmethods';

Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL = "/reference/components/social.html#PhoneNumberPicker";
Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#phonenumberpickerproperties';
Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL = '/reference/components/social.html#phonenumberpickerevents';
Blockly.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL = '/reference/components/social.html#phonenumberpickermethods';

Blockly.LANG_COMPONENT_BLOCK_TEXTING_HELPURL = "/reference/components/social.html#Texting";
Blockly.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL = '/reference/components/social.html#textingproperties';
Blockly.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL = '/reference/components/social.html#textingevents';
Blockly.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL = '/reference/components/social.html#textingmethods';

Blockly.LANG_COMPONENT_BLOCK_TWITTER_HELPURL = "/reference/components/social.html#Twitter";
Blockly.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL = '/reference/components/social.html#twitterproperties';
Blockly.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL = '/reference/components/social.html#twitterevents';
Blockly.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL = '/reference/components/social.html#twittermethods';

//Storage Components
Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL = "/reference/components/storage.html#FusiontablesControl";
Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL = '/reference/components/storage.html#fusiontablescontrolproperties';
Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL = '/reference/components/storage.html#fusiontablescontrolevents';
Blockly.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL = '/reference/components/storage.html#fusiontablescontrolmethods';

Blockly.LANG_COMPONENT_BLOCK_TINYDB_HELPURL = '/reference/components/storage.html#TinyDB';
Blockly.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinydbproperties';
Blockly.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL = '/reference/components/storage.html#tinydbevents';
Blockly.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL = '/reference/components/storage.html#tinydbmethods';

Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL = "/reference/components/storage.html#TinyWebDB";
Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinywebdbproperties';
Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL = '/reference/components/storage.html#tinywebdbevents';
Blockly.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL = '/reference/components/storage.html#tinywebdbmethods';

//Connectivity components
Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL = "/reference/components/connectivity.html#ActivityStarter";
Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#activitystarterproperties';
Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL = '/reference/components/connectivity.html#activitystarterevents';
Blockly.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL = '/reference/components/connectivity.html#activitystartermethods';

Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL = "/reference/components/connectivity.html#BluetoothClient";
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothclientproperties';
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothclientevents';
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothclientmethods';

Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL = "/reference/components/connectivity.html#BluetoothServer";
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothserverproperties';
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothserverevents';
Blockly.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothservermethods';

Blockly.LANG_COMPONENT_BLOCK_WEB_HELPURL = "/reference/components/connectivity.html#Web";
Blockly.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL = '/reference/components/connectivity.html#webproperties';
Blockly.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL = '/reference/components/connectivity.html#webevents';
Blockly.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL = '/reference/components/connectivity.html#webmethods';

//Lego mindstorms components
Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL = "/reference/components/legomindstorms.html#NxtDirectCommands";
Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdirectproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdirectmethods';

Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL = "/reference/components/legomindstorms.html#NxtColorSensor";
Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtcolorproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtcolorevents';
Blockly.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtcolormethods';

Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL = "/reference/components/legomindstorms.html#NxtLightSensor";
Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtlightproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtlightevents';
Blockly.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtlightmethods';

Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL = "/reference/components/legomindstorms.html#NxtSoundSensor";
Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtsoundproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundevents';
Blockly.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundmethods';

Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL = "/reference/components/legomindstorms.html#NxtTouchSensor";
Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxttouchproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxttouchevents';
Blockly.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxttouchmethods';

Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL = "/reference/components/legomindstorms.html#NxtUltrasonicSensor";
Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicevents';
Blockly.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicmethods';

Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL = "/reference/components/legomindstorms.html#NxtDrive";
Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdriveproperties';
Blockly.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdrivemethods';

//Internal components
Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL = "/reference/components/internal.html#GameClient";
Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL = '/reference/components/internal.html#gameclientproperties';
Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL = '/reference/components/internal.html#gameclientevents';
Blockly.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL = '/reference/components/internal.html#gameclientmethods';

Blockly.LANG_COMPONENT_BLOCK_VOTING_HELPURL = "/reference/components/internal.html#Voting";
Blockly.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL = '/reference/components/internal.html#votingproperties';
Blockly.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL = '/reference/components/internal.html#votingevents';
Blockly.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL = '/reference/components/internal.html#votingmethods';



