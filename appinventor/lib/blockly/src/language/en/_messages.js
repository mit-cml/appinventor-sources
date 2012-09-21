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
Blockly.MSG_DELETE_BLOCK = 'Delete Block';
Blockly.MSG_DELETE_X_BLOCKS = 'Delete %1 Blocks';
Blockly.MSG_COLLAPSE_BLOCK = 'Collapse Block';
Blockly.MSG_EXPAND_BLOCK = 'Expand Block';
Blockly.MSG_DISABLE_BLOCK = 'Disable Block';
Blockly.MSG_ENABLE_BLOCK = 'Enable Block';
Blockly.MSG_HELP = 'Help';

// Variable renaming.
Blockly.MSG_CHANGE_VALUE_TITLE = 'Change value:';
Blockly.MSG_NEW_VARIABLE = 'New variable...';
Blockly.MSG_NEW_VARIABLE_TITLE = 'New variable name:';
Blockly.MSG_RENAME_VARIABLE = 'Rename variable...';
Blockly.MSG_RENAME_VARIABLE_TITLE = 'Rename all "%1" variables to:';

// Toolbox.
Blockly.MSG_VARIABLE_CATEGORY = 'Variables';
Blockly.MSG_PROCEDURE_CATEGORY = 'Procedures';

// Control Blocks
Blockly.LANG_CATEGORY_CONTROLS = 'Control';
Blockly.LANG_CONTROLS_IF_HELPURL = 'http://code.google.com/p/blockly/wiki/If_Then';
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
Blockly.LANG_CONTROLS_IF_IF_TOOLTIP_1 = 'Add, remove, or reorder sections\n' +
                    'to reconfigure this if block.';

Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'else if';
Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP_1 = 'Add a condition to the if block.';

Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'else';
Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP_1 = 'Add a final, catch-all condition to the if block.';

Blockly.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://code.google.com/p/blockly/wiki/Repeat';
Blockly.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT= 'repeat';
Blockly.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'while';
Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'until';
Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'While a value is true, then do some statements.';
Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'While a value is false, then do some statements.';

Blockly.LANG_CONTROLS_FOR_HELPURL = 'http://en.wikipedia.org/wiki/For_loop';
Blockly.LANG_CONTROLS_FOR_TITLE_COUNT = 'count';
Blockly.LANG_CONTROLS_FOR_INPUT_WITH = 'with';
Blockly.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
Blockly.LANG_CONTROLS_FOR_INPUT_FROM = 'from';
Blockly.LANG_CONTROLS_FOR_INPUT_TO = 'to';
Blockly.LANG_CONTROLS_FOR_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_FOR_TOOLTIP_1 = 'Count from a start number to an end number.\n' +
    'For each count, set the current count number to\n' +
    'variable "%1", and then do some statements.';        

Blockly.LANG_CONTROLS_FOREACH_HELPURL = 'http://en.wikipedia.org/wiki/For_loop';
Blockly.LANG_CONTROLS_FOREACH_TITLE_FOREACH = 'for each';
Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'item'; 
Blockly.LANG_CONTROLS_FOREACH_INPUT_VAR = 'x';
Blockly.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'in list';
Blockly.LANG_CONTROLS_FOREACH_INPUT_DO = 'do';
Blockly.LANG_CONTROLS_FOREACH_TOOLTIP_1 = 'For each item in a list, set the item to\n' +
  'variable "%1", and then do some statements.';

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

// Logic Blocks.
Blockly.LANG_CATEGORY_LOGIC = 'Logic';
Blockly.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Return true if both inputs equal each other.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Return true if both inputs are not equal to each other.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LT = 'Return true if the first input is smaller\n' +
      'than the second input.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LTE = 'Return true if the first input is smaller\n' +
       'than or equal to the second input.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GT = 'Return true if the first input is greater\n' +
      'than the second input.';
Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GTE = 'Return true if the first input is greater\n' +
       'than or equal to the second input.';

Blockly.LANG_LOGIC_OPERATION_HELPURL = 'http://code.google.com/p/blockly/wiki/And_Or';
Blockly.LANG_LOGIC_OPERATION_AND = 'and';
Blockly.LANG_LOGIC_OPERATION_OR = 'or';
Blockly.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Return true if both inputs are true.';
Blockly.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Return true if either inputs are true.';

Blockly.LANG_LOGIC_NEGATE_HELPURL = 'http://code.google.com/p/blockly/wiki/Not';
Blockly.LANG_LOGIC_NEGATE_INPUT_NOT = 'not';
Blockly.LANG_LOGIC_NEGATE_TOOLTIP_1 = 'Returns true if the input is false.\n' +
                    'Returns false if the input is true.';

Blockly.LANG_LOGIC_BOOLEAN_HELPURL = 'http://code.google.com/p/blockly/wiki/True_False';
Blockly.LANG_LOGIC_BOOLEAN_TRUE = 'true';
Blockly.LANG_LOGIC_BOOLEAN_FALSE = 'false';
Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_1 = 'Returns either true or false.';

// Math Blocks.
Blockly.LANG_CATEGORY_MATH = 'Math';
Blockly.LANG_MATH_NUMBER_HELPURL = 'http://en.wikipedia.org/wiki/Number';
Blockly.LANG_MATH_NUMBER_TOOLTIP_1 = 'A number.';

Blockly.LANG_MATH_ARITHMETIC_HELPURL = 'http://en.wikipedia.org/wiki/Arithmetic';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Return the sum of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Return the difference of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Return the product of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Return the quotient of the two numbers.';
Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Return the first number raised to\n' +
    'the power of the second number.';

Blockly.LANG_MATH_CHANGE_HELPURL = 'http://en.wikipedia.org/wiki/Negation';
Blockly.LANG_MATH_CHANGE_TITLE_CHANGE = 'change';
Blockly.LANG_MATH_CHANGE_TITLE_ITEM = 'item';
Blockly.LANG_MATH_CHANGE_INPUT_BY = 'by';
Blockly.LANG_MATH_CHANGE_TOOLTIP_1 = 'Add a number to variable "%1".';

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
Blockly.LANG_MATH_ROUND_TOOLTIP_1 = 'Round a number up or down.';
Blockly.LANG_MATH_ROUND_OPERATOR_ROUND = 'round';
Blockly.LANG_MATH_ROUND_OPERATOR_ROUNDUP = 'round up';
Blockly.LANG_MATH_ROUND_OPERATOR_ROUNDDOWN = 'round down';

Blockly.LANG_MATH_TRIG_HELPURL = 'http://en.wikipedia.org/wiki/Trigonometric_functions';
Blockly.LANG_MATH_TRIG_TOOLTIP_SIN = 'Return the sine of a degree.';
Blockly.LANG_MATH_TRIG_TOOLTIP_COS = 'Return the cosine of a degree.';
Blockly.LANG_MATH_TRIG_TOOLTIP_TAN = 'Return the tangent of a degree.';
Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Return the arcsine of a number.';
Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Return the arccosine of a number.';
Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Return the arctangent of a number.';

Blockly.LANG_MATH_ONLIST_HELPURL = '';
Blockly.LANG_MATH_ONLIST_INPUT_OFLIST = 'of list';
Blockly.LANG_MATH_ONLIST_OPERATOR_SUM = 'sum'; 
Blockly.LANG_MATH_ONLIST_OPERATOR_MIN = 'min';
Blockly.LANG_MATH_ONLIST_OPERATOR_MAX = 'max';
Blockly.LANG_MATH_ONLIST_OPERATOR_AVERAGE = 'average';
Blockly.LANG_MATH_ONLIST_OPERATOR_MEDIAN = 'median';
Blockly.LANG_MATH_ONLIST_OPERATOR_MODE = 'modes';
Blockly.LANG_MATH_ONLIST_OPERATOR_STD_DEV = 'standard deviation';
Blockly.LANG_MATH_ONLIST_OPERATOR_RANDOM = 'random item';
Blockly.LANG_MATH_ONLIST_TOOLTIP_SUM = 'Return the sum of all the numbers in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Return the smallest number in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Return the largest number in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_AVERAGE = 'Return the arithmetic mean of the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MEDIAN = 'Return the median number in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_MODE = 'Return a list of the most common item(s) in the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_STD_DEV = 'Return the standard deviation of the list.';
Blockly.LANG_MATH_ONLIST_TOOLTIP_RANDOM = 'Return a random element from the list.';

Blockly.LANG_MATH_CONSTRAIN_HELPURL = 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29';
Blockly.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN = 'constrain';
Blockly.LANG_MATH_CONSTRAIN_INPUT_LOW = 'between (low)';
Blockly.LANG_MATH_CONSTRAIN_INPUT_HIGH = 'and (high)';
Blockly.LANG_MATH_CONSTRAIN_TOOLTIP_1 = 'Constrain a number to be between the specified limits (inclusive).';

Blockly.LANG_MATH_MODULO_HELPURL = 'http://en.wikipedia.org/wiki/Modulo_operation';
Blockly.LANG_MATH_MODULO_INPUT_DIVIDEND = 'remainder of';
Blockly.LANG_MATH_MODULO_TOOLTIP_1 = 'Return the remainder of dividing both numbers.';

Blockly.LANG_MATH_RANDOM_INT_HELPURL = 'http://en.wikipedia.org/wiki/Random_number_generation';
Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'random integer';
Blockly.LANG_MATH_RANDOM_INT_INPUT_FROM = 'from';
Blockly.LANG_MATH_RANDOM_INT_INPUT_TO = 'to';
Blockly.LANG_MATH_RANDOM_INT_TOOLTIP_1 = 'Return a random integer between the two\n' +
    'specified limits, inclusive.';

Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://en.wikipedia.org/wiki/Random_number_generation';
Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'random fraction';
Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP_1 = 'Return a random fraction between\n' +
    '0.0 (inclusive) and 1.0 (exclusive).';

// Text Blocks.
Blockly.LANG_CATEGORY_TEXT = 'Text';
Blockly.LANG_TEXT_TEXT_HELPURL = 'http://en.wikipedia.org/wiki/String_(computer_science)';
Blockly.LANG_TEXT_TEXT_TOOLTIP_1 = 'A letter, word, or line of text.';

Blockly.LANG_TEXT_JOIN_HELPURL = '';
Blockly.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'create text with';
Blockly.LANG_TEXT_JOIN_TOOLTIP_1 = 'Create a piece of text by joining\n' +
                    'together any number of items.';

Blockly.LANG_TEXT_CREATE_JOIN_TITLE_JOIN = 'join';
Blockly.LANG_TEXT_CREATE_JOIN_TOOLTIP_1 = 'Add, remove, or reorder sections to reconfigure this text block.';

Blockly.LANG_TEXT_CREATE_JOIN_ITEM_TITLE_ITEM = 'item';
Blockly.LANG_TEXT_CREATE_JOIN_ITEM_TOOLTIP_1 = 'Add an item to the text.';

Blockly.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html'; 
Blockly.LANG_TEXT_APPEND_TO = 'to';
Blockly.LANG_TEXT_APPEND_APPENDTEXT = 'append text';
Blockly.LANG_TEXT_APPEND_VARIABLE = 'item';
Blockly.LANG_TEXT_APPEND_TOOLTIP_1 = 'Append some text to variable "%1".';

Blockly.LANG_TEXT_LENGTH_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html'; 
Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH = 'length';
Blockly.LANG_TEXT_LENGTH_TOOLTIP_1 = 'Returns number of letters (including spaces)\n' +
                    'in the provided text.';
                    
Blockly.LANG_TEXT_ISEMPTY_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'is empty';
Blockly.LANG_TEXT_ISEMPTY_TOOLTIP_1 = 'Returns true if the provided text is empty.';

Blockly.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_TEXT_ENDSTRING_INPUT = 'letters in text';
Blockly.LANG_TEXT_ENDSTRING_TOOLTIP_1 = 'Returns specified number of letters at the beginning or end of the text.';
Blockly.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'first';
Blockly.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'last';

Blockly.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_TEXT_INDEXOF_TITLE_FIND = 'find';
Blockly.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'occurrence of text';
Blockly.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'in text';
Blockly.LANG_TEXT_INDEXOF_TOOLTIP_1 = 'Returns the index of the first/last occurrence\n' +
                    'of first text in the second text.\n' +
                    'Returns 0 if text is not found.';
Blockly.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'first';
Blockly.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'last';

Blockly.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm'
Blockly.LANG_TEXT_CHARAT_TITLE_LETTER = 'letter';
Blockly.LANG_TEXT_CHARAT_INPUT_AT = 'at';
Blockly.LANG_TEXT_CHARAT_INPUT_INTEXT = 'in text';
Blockly.LANG_TEXT_CHARAT_TOOLTIP_1 = 'Returns the letter at the specified position.';

Blockly.LANG_TEXT_CHANGECASE_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_CHANGECASE_TITLE_TO = 'to';
Blockly.LANG_TEXT_CHANGECASE_TOOLTIP_1 = 'Return a copy of the text in a different case.';
Blockly.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'UPPER CASE';
Blockly.LANG_TEXT_CHANGECASE_OPERATOR_LOWERCASE = 'lower case';
Blockly.LANG_TEXT_CHANGECASE_OPERATOR_TITLECASE = 'Title Case';

Blockly.LANG_TEXT_TRIM_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_TRIM_TITLE_SPACE = 'trim spaces from';
Blockly.LANG_TEXT_TRIM_TITLE_SIDES = 'sides';
Blockly.LANG_TEXT_TRIM_TOOLTIP_1 = 'Return a copy of the text with spaces\n' +
                    'removed from one or both ends.';
Blockly.LANG_TEXT_TRIM_TITLE_SIDES = 'sides';
Blockly.LANG_TEXT_TRIM_TITLE_SIDE = 'side';
Blockly.LANG_TEXT_TRIM_OPERATOR_BOTH = 'both';
Blockly.LANG_TEXT_TRIM_OPERATOR_LEFT = 'left';
Blockly.LANG_TEXT_TRIM_OPERATOR_RIGHT = 'right';

Blockly.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_TEXT_PRINT_TITLE_PRINT = 'print';
Blockly.LANG_TEXT_PRINT_TOOLTIP_1 = 'Print the specified text, number or other value.';

Blockly.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
Blockly.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'prompt for';
Blockly.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'with message';
Blockly.LANG_TEXT_PROMPT_TOOLTIP_1 = 'Prompt for user input with the specified text.';
Blockly.LANG_TEXT_PROMPT_TYPE_TEXT = 'text';
Blockly.LANG_TEXT_PROMPT_TYPE_NUMBER = 'number';

// Lists Blocks.
Blockly.LANG_CATEGORY_LISTS = 'Lists';
Blockly.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
Blockly.LANG_LISTS_CREATE_EMPTY_TITLE_1 = 'create empty list';
Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP_1 = 'Returns a list, of length 0, containing no data records';

Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH = 'create list with';
Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP_1 = 'Create a list with any number of items.';

Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'list';
Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP_1 = 'Add, remove, or reorder sections to reconfigure this list block.';

Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'item';
Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP_1 = 'Add an item to the list.';

Blockly.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_REPEAT_TITLE_CREATELIST = 'create list';
Blockly.LANG_LISTS_REPEAT_INPUT_WITH = 'with item';
Blockly.LANG_LISTS_REPEAT_INPIT_REPEATED = 'repeated';
Blockly.LANG_LISTS_REPEAT_INPIT_TIMES = 'times';
Blockly.LANG_LISTS_REPEAT_TOOLTIP_1 = 'Creates a list consisting of the given value\n' +
                    'repeated the specified number of times.';

Blockly.LANG_LISTS_LENGTH_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_LISTS_LENGTH_INPUT_LENGTH = 'length';
Blockly.LANG_LISTS_LENGTH_TOOLTIP_1 = 'Returns the length of a list.';

Blockly.LANG_LISTS_IS_EMPTY_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
Blockly.LANG_LISTS_INPUT_IS_EMPTY = 'is empty';
Blockly.LANG_LISTS_TOOLTIP_1 = 'Returns true if the list is empty.';

Blockly.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND = 'find';
Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'occurrence of item';
Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_INDEX_OF_TOOLTIP_1 = 'Returns the index of the first/last occurrence\n' +
                    'of the item in the list.\n' +
                    'Returns 0 if text is not found.';
Blockly.LANG_LISTS_INDEX_OF_FIRST = 'first';
Blockly.LANG_LISTS_INDEX_OF_LAST = 'last';

Blockly.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_GET_INDEX_TITLE = 'get item';
Blockly.LANG_LISTS_GET_INDEX_INPUT_AT = 'at';
Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_GET_INDEX_TOOLTIP_1 = 'Returns the value at the specified position in a list.';

Blockly.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
Blockly.LANG_LISTS_SET_INDEX_TITLE = 'set item';
Blockly.LANG_LISTS_SET_INDEX_INPUT_AT = 'at';
Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
Blockly.LANG_LISTS_SET_INDEX_INPUT_TO = 'to';
Blockly.LANG_LISTS_SET_INDEX_TOOLTIP_1 = 'Sets the value at the specified position in a list.';

// Variables Blocks.
Blockly.LANG_VARIABLES_GET_HELPURL = 'http://en.wikipedia.org/wiki/Variable_(computer_science)';
Blockly.LANG_VARIABLES_GET_TITLE_1 = 'get';
Blockly.LANG_VARIABLES_GET_ITEM = 'item';
Blockly.LANG_VARIABLES_GET_TOOLTIP_1 = 'Returns the value of this variable.';

Blockly.LANG_VARIABLES_SET_HELPURL = 'http://en.wikipedia.org/wiki/Variable_(computer_science)';
Blockly.LANG_VARIABLES_SET_TITLE_1 = 'set';
Blockly.LANG_VARIABLES_SET_ITEM = 'item';
Blockly.LANG_VARIABLES_SET_TOOLTIP_1 = 'Sets this variable to be equal to the input.';

// Procedures Blocks.
Blockly.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'procedure';
Blockly.LANG_PROCEDURES_DEFNORETURN_DO = 'do';
Blockly.LANG_PROCEDURES_DEFNORETURN_TOOLTIP_1 = 'A procedure with no return value.';

Blockly.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
Blockly.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
Blockly.LANG_PROCEDURES_DEFRETURN_DO = Blockly.LANG_PROCEDURES_DEFNORETURN_DO;
Blockly.LANG_PROCEDURES_DEFRETURN_RETURN = 'return';
Blockly.LANG_PROCEDURES_DEFRETURN_TOOLTIP_1 = 'A procedure with a return value.';

Blockly.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Warning:\n' +
    'This procedure has\n' +
    'duplicate parameters.';

Blockly.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
Blockly.LANG_PROCEDURES_CALLNORETURN_CALL = 'call';
Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'procedure';
Blockly.LANG_PROCEDURES_CALLNORETURN_TOOLTIP_1 = 'Call a procedure with no return value.';

Blockly.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
Blockly.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.LANG_PROCEDURES_CALLNORETURN_CALL;
Blockly.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
Blockly.LANG_PROCEDURES_CALLRETURN_TOOLTIP_1 = 'Call a procedure with a return value.';

Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'parameters';
Blockly.LANG_PROCEDURES_MUTATORARG_TITLE = 'variable:';

Blockly.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Highlight Procedure';
