/**
 * Visual Blocks Language
 *
 * Copyright 2013 Google Inc.
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
 * @fileoverview German strings.
 * @author henning@mst.ch (Heiko Henning)
 */
'use strict';


goog.provide('Blockly.messages.de');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

if (!Blockly.Language) Blockly.Language = {};
 Blockly.Language.switch_language_to_german = {
    // Switch language to German.
    category: '',  
    helpUrl: '',
    init: function() { 
        // Context menus.
        Blockly.MSG_DUPLICATE_BLOCK = 'Kopieren';
        Blockly.MSG_REMOVE_COMMENT = 'Kommentar entfernen';
        Blockly.MSG_ADD_COMMENT = 'Kommentar hinzufügen';
        Blockly.MSG_EXTERNAL_INPUTS = 'externe Eingänge';
        Blockly.MSG_INLINE_INPUTS = 'interne Eingänge';
        Blockly.MSG_DELETE_BLOCK = 'Block löschen';
        Blockly.MSG_DELETE_X_BLOCKS = 'Block %1 löschen';
        Blockly.MSG_COLLAPSE_BLOCK = 'Block zusammenfalten';
        Blockly.MSG_EXPAND_BLOCK = 'Block entfalten';
        Blockly.MSG_DISABLE_BLOCK = 'Block deaktivieren';
        Blockly.MSG_ENABLE_BLOCK = 'Block aktivieren';
        Blockly.MSG_HELP = 'Hilfe';

        // Variable renaming.
        Blockly.MSG_CHANGE_VALUE_TITLE = 'Wert ändern:';
        Blockly.MSG_NEW_VARIABLE = 'Neue Variable...';
        Blockly.MSG_NEW_VARIABLE_TITLE = 'Name der neuen Variable:';
        Blockly.MSG_RENAME_VARIABLE = 'Variable umbenennen...';
        Blockly.MSG_RENAME_VARIABLE_TITLE = 'Alle "%1" Variablen umbenennen in:';

        // Toolbox.
        Blockly.MSG_VARIABLE_CATEGORY = 'Variables';
        Blockly.MSG_PROCEDURE_CATEGORY = 'Procedures';

        // Colour Blocks.
        Blockly.LANG_COLOUR_PICKER_HELPURL = 'http://en.wikipedia.org/wiki/Color';
        Blockly.LANG_COLOUR_PICKER_TOOLTIP = 'Click the square to pick a color.';

        // Control Blocks.
        Blockly.LANG_CATEGORY_CONTROLS = 'Control';
        Blockly.LANG_CONTROLS_IF_HELPURL = 'http://code.google.com/p/blockly/wiki/If_Then';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_1 = 'Wenn eine Bedingung wahr (true) ist, dann führe eine Anweisung aus.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_2 = 'Wenn eine Bedingung wahr (true) ist, dann führe die erste Anweisung aus.\n' +
            'Ansonsten führe die zweite Anweisung aus.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_3 = 'Wenn der erste Bedingung wahr (true) ist, dann führe die erste Anweisung aus.\n' +
            'Oder wenn die zweite Bedingung wahr (true) ist, dann führe die zweite Anweisung aus.';
        Blockly.LANG_CONTROLS_IF_TOOLTIP_4 = 'Wenn der erste Bedingung wahr (true) ist, dann führe die erste Anweisung aus.\n' +
            'Oder wenn die zweite Bedingung wahr (true) ist, dann führe die zweite Anweisung aus.\n' +
            'Falls keine der beiden Bedingungen wahr (true) ist, dann führe die dritte Anweisung aus.';
        Blockly.LANG_CONTROLS_IF_MSG_IF = 'wenn';
        Blockly.LANG_CONTROLS_IF_MSG_ELSEIF = 'oder wenn';
        Blockly.LANG_CONTROLS_IF_MSG_ELSE = 'oder';
        Blockly.LANG_CONTROLS_IF_MSG_THEN = 'mache';

        Blockly.LANG_CONTROLS_IF_IF_TITLE_IF = 'wenn';
        Blockly.LANG_CONTROLS_IF_IF_TOOLTIP = 'Hinzufügen, entfernen oder sortieren von Sektionen';

        Blockly.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'oder wenn';
        Blockly.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Eine weitere Bedingung hinzufügen.';

        Blockly.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'oder';
        Blockly.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Eine oder Bedingung hinzufügen, führt eine Anweisung aus falls keine Bedingung zutrifft.';

        Blockly.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://de.wikipedia.org/wiki/Schleife_%28Programmierung%29';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'Wiederhole';
        Blockly.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'mache';
        Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'solange';
        Blockly.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'bis';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Führe die Anweisung solange aus wie die Bedingung wahr (true) ist.';
        Blockly.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Führe die Anweisung solange aus wie die Bedingung falsch (false) ist.';

        Blockly.LANG_CONTROLS_FOR_HELPURL = 'http://de.wikipedia.org/wiki/For-Schleif';
        Blockly.LANG_CONTROLS_FOR_INPUT_WITH = 'Zähle';
        Blockly.LANG_CONTROLS_FOR_INPUT_VAR = 'i';
        Blockly.LANG_CONTROLS_FOR_INPUT_FROM = 'von';
        Blockly.LANG_CONTROLS_FOR_INPUT_TO = 'bis';
        Blockly.LANG_CONTROLS_FOR_INPUT_DO = 'mache';
        Blockly.LANG_CONTROLS_FOR_TOOLTIP = 'Zähle die Variable "%1" von einem Startwert\n' +
            'bis zu einem Zielwert und führe für jeden Wert\n' +
            'eine Anweisung aus.';

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

        Blockly.LANG_CONTROLS_FOREACH_HELPURL = 'http://de.wikipedia.org/wiki/For-Schleif';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'Für Wert';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_VAR = 'i';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'aus der Liste';
        Blockly.LANG_CONTROLS_FOREACH_INPUT_DO = 'mache';
        Blockly.LANG_CONTROLS_FOREACH_TOOLTIP = 'Führe eine Anweisung für jeden Wert in der Liste aus\n' +
            'und setzte dabei die Variable "%1" \n' +
            'auf den aktuellen Listen Wert.';

        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://de.wikipedia.org/wiki/Kontrollstruktur';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'aus der Schleife';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'ausbrechen';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'mit der nächsten Iteration fortfahren';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Die umgebene Schleife beenden.';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Diese Anweisung abbrechen\n' +
            'und mit der nächsten Schleifeniteration fortfahren.';
        Blockly.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Warnung:\n' +
            'Diese block sollte\n' +
            'nur in einer Schleife\n'+
            'verwendet werden.';

        Blockly.LANG_CONTROLS_WHILE_HELPURL = '';
        Blockly.LANG_CONTROLS_WHILE_INPUT_WHILE = 'while';
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
        Blockly.LANG_LOGIC_COMPARE_HELPURL = 'http://de.wikipedia.org/wiki/Vergleich_%28Zahlen%29';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Tests whether two things are equal. \n' +
                'The things being compared can be any thing, not only numbers.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Ist wahr (true) wenn beide Werte unterschiedlich sind.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LT = 'Ist wahr (true) wenn der erste Wert kleiner als\n' +
            'der zweite Wert ist.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_LTE = 'Ist wahr (true) wenn der erste Wert kleiner als\n' +
            'oder gleich gross wie zweite Wert ist.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GT = 'Ist wahr (true) wenn der erste Wert grösser als\n' +
            'der zweite Wert ist.';
        Blockly.LANG_LOGIC_COMPARE_TOOLTIP_GTE = 'Ist wahr (true) wenn der erste Wert grösser als\n' +
            'oder gleich gross wie zweite Wert ist.';

        Blockly.LANG_LOGIC_OPERATION_HELPURL = 'http://code.google.com/p/blockly/wiki/And_Or';
        Blockly.LANG_LOGIC_OPERATION_AND = 'und';
        Blockly.LANG_LOGIC_OPERATION_OR = 'oder';
        Blockly.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Returns true if all inputs are true.';
        Blockly.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Returns true if any input is true.';

        Blockly.LANG_LOGIC_NEGATE_HELPURL = 'http://code.google.com/p/blockly/wiki/Not';
        Blockly.LANG_LOGIC_NEGATE_INPUT_NOT = 'nicht';
        Blockly.LANG_LOGIC_NEGATE_TOOLTIP = 'Ist wahr (true) wenn der Eingabewert falsch (false) ist.\n' +
            'Ist falsch (false) wenn der Eingabewert wahr (true) ist.';

        Blockly.LANG_LOGIC_BOOLEAN_HELPURL = 'http://code.google.com/p/blockly/wiki/True_False';
        Blockly.LANG_LOGIC_BOOLEAN_TRUE = 'wahr';
        Blockly.LANG_LOGIC_BOOLEAN_FALSE = 'falsch';
        Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Returns the boolean true.';
        Blockly.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Returns the boolean false.';

        // Math Blocks.
        Blockly.LANG_CATEGORY_MATH = 'Math';
        Blockly.LANG_MATH_NUMBER_HELPURL = 'http://de.wikipedia.org/wiki/Zahl';
        Blockly.LANG_MATH_NUMBER_TOOLTIP = "Report the number shown.";
        Blockly.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'number';

        Blockly.LANG_MATH_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
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

        Blockly.LANG_MATH_ARITHMETIC_HELPURL = 'http://de.wikipedia.org/wiki/Grundrechenart';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Ist die Summe zweier Werte.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Ist die Differenz zweier Werte.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Ist das Produkt zweier Werte.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Ist der Quotient zweier Werte.';
        Blockly.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Ist der erste Wert potenziert mit\n' +
            'dem zweiten Wert.';

        Blockly.LANG_MATH_CHANGE_HELPURL = 'http://de.wikipedia.org/wiki/Inkrement_und_Dekrement';
        Blockly.LANG_MATH_CHANGE_TITLE_CHANGE = 'erhöhe';
        Blockly.LANG_MATH_CHANGE_TITLE_ITEM = 'Variable';
        Blockly.LANG_MATH_CHANGE_INPUT_BY = 'um';
        Blockly.LANG_MATH_CHANGE_TOOLTIP = 'Addiert einen Wert zur Variable "%1" hinzu.';

        Blockly.LANG_MATH_SINGLE_HELPURL = 'http://de.wikipedia.org/wiki/Quadratwurzel';
        Blockly.LANG_MATH_SINGLE_OP_ROOT = 'Quadratwurzel';
        Blockly.LANG_MATH_SINGLE_OP_ABSOLUTE = 'Absolutwert';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Ist die Qudratwurzel eines Wertes.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Ist der Absolutwert eines Wertes.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Negiert einen Wert.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_LN = 'Ist der natürliche Logarithmus eines Wertes.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_LOG10 = 'Ist der dekadische Logarithmus eines Wertes.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Ist Wert der Exponentialfunktion eines Wertes.';
        Blockly.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Rechnet 10 hoch Eingabewert.';

        Blockly.LANG_MATH_ROUND_HELPURL = 'http://en.wikipedia.org/wiki/Rounding';
        Blockly.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Round a number up or down.';
        Blockly.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Rounds the input to the smallest\n' +
              'number not less then the input';
        Blockly.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Rounds the input to the largest\n' +
              'number not greater then the input';
        Blockly.LANG_MATH_ROUND_OPERATOR_ROUND = 'round';
        Blockly.LANG_MATH_ROUND_OPERATOR_CEILING = 'ceiling';
        Blockly.LANG_MATH_ROUND_OPERATOR_FLOOR = 'floor';

        Blockly.LANG_MATH_TRIG_HELPURL = 'http://de.wikipedia.org/wiki/Trigonometrie';
        Blockly.LANG_MATH_TRIG_TOOLTIP_SIN = 'Ist der Sinus eins Winkels.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_COS = 'Ist der Cosinus eins Winkels.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_TAN = 'Ist der Tangens eins Winkels.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Ist der Arcussinus des Eingabewertes.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Ist der Arcuscosinus des Eingabewertes.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Ist der Arcustangens des Eingabewertes.';
        Blockly.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Provides the angle in the range (-180, +180]\n' +
                'degrees with the given rectangular coordinates.';

        Blockly.LANG_MATH_ONLIST_HELPURL = 'http://www.sysplus.ch/einstieg.php?links=menu&seite=4125&grad=Crash&prog=Excel';
        Blockly.LANG_MATH_ONLIST_INPUT_OFLIST = 'einer Liste';
        Blockly.LANG_MATH_ONLIST_OPERATOR_SUM = 'Summme';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MIN = 'Minimalwert';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MAX = 'Maximalwert';
        Blockly.LANG_MATH_ONLIST_OPERATOR_AVERAGE = 'Mittelwert';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MEDIAN = 'Median';
        Blockly.LANG_MATH_ONLIST_OPERATOR_MODE = 'Modulo / Restwert';
        Blockly.LANG_MATH_ONLIST_OPERATOR_STD_DEV = 'Standart Abweichung';
        Blockly.LANG_MATH_ONLIST_OPERATOR_RANDOM = 'Zufallswert';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_SUM = 'Ist die Summe aller Werte in einer Liste.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Ist der kleinste Wert in einer Liste.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Ist der grösste Wert in einer Liste.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_AVERAGE = 'Ist der Durchschnittswert aller Werte in einer Liste.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MEDIAN = 'Ist der Zentralwert aller Werte in einer Liste.';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_MODE = 'Findet den am häufigsten vorkommenden Wert in einer Liste.\n' +
            'Falls kein Wert öfter vorkomme als alle anderen,\n' +
            'wird die originale Liste zurückgegen';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_STD_DEV = 'Ist die standartiesierte Standartabweichung aller Werte in der Liste';
        Blockly.LANG_MATH_ONLIST_TOOLTIP_RANDOM = 'Gebe einen Zufallswert aus der Liste zurück.';

        Blockly.LANG_MATH_CONSTRAIN_HELPURL = 'http://en.wikipedia.org/wiki/Clamping_%28graphics%29';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_CONSTRAIN = 'begrenzen';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_LOW = 'von';
        Blockly.LANG_MATH_CONSTRAIN_INPUT_HIGH = 'bis';
        Blockly.LANG_MATH_CONSTRAIN_TOOLTIP = 'Begrenzt den Wertebereich mittels von / bis Werte. (inklusiv)';

        Blockly.LANG_MATH_DIVIDE_HELPURL = 'http://en.wikipedia.org/wiki/Modulo_operation';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'modulo of';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'remainder of';
        Blockly.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'quotient of';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Return the modulo.';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Return the remainder.';
        Blockly.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Return the quotient.';

        Blockly.LANG_MATH_RANDOM_INT_HELPURL = 'http://de.wikipedia.org/wiki/Zufallszahlen';
        Blockly.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'random integer';
        Blockly.LANG_MATH_RANDOM_INT_INPUT_FROM = 'from';
        Blockly.LANG_MATH_RANDOM_INT_INPUT_TO = 'und';
        Blockly.LANG_MATH_RANDOM_INT_TOOLTIP = 'Erzeuge eine ganzahligen Zufallswert zwischen\n' +
            'zwei Werten (inklusiv).';

        Blockly.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://de.wikipedia.org/wiki/Zufallszahlen';
        Blockly.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'Zufallszahl (0.0 -1.0)';
        Blockly.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Erzeuge eine Zufallszahl\n' +
            'zwischen 0.0 (inklusiv) und 1.0 (exklusiv).';

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
        Blockly.LANG_TEXT_TEXT_HELPURL = 'http://de.wikipedia.org/wiki/Zeichenkette';
        Blockly.LANG_TEXT_TEXT_TOOLTIP = 'Ein Buchstabe, Text oder Satz.';

        Blockly.LANG_TEXT_JOIN_HELPURL = '';
        Blockly.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'Erstelle Text aus';
        Blockly.LANG_TEXT_JOIN_TOOLTIP = 'Erstellt einen Text durch das verbinden\n' +
            'von mehreren Textelementen.';
        Blockly.LANG_TEXT_JOIN_TITLE_JOIN = 'join';

        Blockly.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'string';
        Blockly.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

        Blockly.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_APPEND_TO = 'An';
        Blockly.LANG_TEXT_APPEND_APPENDTEXT = 'Text anhängen';
        Blockly.LANG_TEXT_APPEND_VARIABLE = 'Variable';
        Blockly.LANG_TEXT_APPEND_TOOLTIP = 'Text an die Variable "%1" anhängen.';

        Blockly.LANG_TEXT_LENGTH_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_LENGTH_INPUT_LENGTH = 'länge';
        Blockly.LANG_TEXT_LENGTH_TOOLTIP = 'Die Anzahl von Zeichen in einem Textes. (inkl. Leerzeichen)';

        Blockly.LANG_TEXT_ISEMPTY_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
        Blockly.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'ist leer?';
        Blockly.LANG_TEXT_ISEMPTY_TOOLTIP = 'Ist wahr (true), wenn der Text leer ist.';

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
        Blockly.LANG_TEXT_ENDSTRING_INPUT = 'ten Buchstaben aus';
        Blockly.LANG_TEXT_ENDSTRING_TOOLTIP = 'Extrahiert die erste / letzten X Buchstaben von einem Text.';
        Blockly.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'von vorn';
        Blockly.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'von hinten';

        Blockly.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_TEXT_INDEXOF_TITLE_FIND = 'Suche';
        Blockly.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'Vorkommniss des Begriff';
        Blockly.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'im Text';
        Blockly.LANG_TEXT_INDEXOF_TOOLTIP = 'Findest das erste / letzte Vorkommniss\n' +
            'eines cuchbegriffes in einem Text.\n' +
            'Gibt die Position des Begriffes oder 0 zurück.';
        Blockly.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'erstes';
        Blockly.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'letztes';

        Blockly.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_TEXT_CHARAT_GET = 'Nehme';
        Blockly.LANG_TEXT_CHARAT_FROM_START = 'Buchstabe #ten';
        Blockly.LANG_TEXT_CHARAT_FROM_END = '#te Buchstabe von hinten';
        Blockly.LANG_TEXT_CHARAT_FIRST = 'ersten Buchstabe';
        Blockly.LANG_TEXT_CHARAT_LAST = 'letzten Buchstabe';
        Blockly.LANG_TEXT_CHARAT_RANDOM = 'zufälligen Buchstabe';
        Blockly.LANG_TEXT_CHARAT_INPUT_INTEXT = 'vom Text';
        Blockly.LANG_TEXT_CHARAT_TOOLTIP = 'Extrahiere einen Buchstaben von einer spezifizierten Position.';

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
        Blockly.LANG_TEXT_PRINT_TITLE_PRINT = 'Ausgabe';
        Blockly.LANG_TEXT_PRINT_TOOLTIP = 'Gib den Inhalt einer Variable aus.';

        Blockly.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
        Blockly.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'Fragt nach';
        Blockly.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'mit Hinweis';
        Blockly.LANG_TEXT_PROMPT_TOOLTIP_NUMBER = 'Fragt den Benutzer nach ein Zahl.';
        Blockly.LANG_TEXT_PROMPT_TOOLTIP_TEXT = 'Fragt den Benutzer nach einem Text.';
        Blockly.LANG_TEXT_PROMPT_TYPE_TEXT = 'Text';
        Blockly.LANG_TEXT_PROMPT_TYPE_NUMBER = 'Zahl';

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
        Blockly.LANG_LISTS_CREATE_EMPTY_TITLE = 'Erzeuge eine leere Liste';
        Blockly.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Erzeugt eine leere Liste ohne Inhalt.';

        Blockly.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = '';
        Blockly.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'make a list';
        Blockly.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Create a list with any number of items.';

        Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_INPUT_LIST = 'Liste';
        Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Hinzufügen, entfernen und sortieren von Elementen.';

        Blockly.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'Element';
        Blockly.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Ein Element zur Liste hinzufügen.';

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
        Blockly.LANG_LISTS_REPEAT_TITLE_CREATE = 'Erzeuge Liste mit Element';
        Blockly.LANG_LISTS_REPEAT_INPUT_REPEATED = 'wiederhole es';
        Blockly.LANG_LISTS_REPEAT_INPUT_TIMES = 'mal';
        Blockly.LANG_LISTS_REPEAT_TOOLTIP = 'Erzeugt eine Liste mit einer variablen\n' +
            'Anzahl von Elementen';

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
        Blockly.LANG_LISTS_TITLE_IS_EMPTY = 'ist leer?';
        Blockly.LANG_LISTS_INPUT_LIST = 'liste';
        Blockly.LANG_LISTS_TOOLTIP = 'Ist wahr (true), wenn die Liste leer ist.';

        Blockly.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_INDEX_OF_TITLE_FIND = 'Suche';
        Blockly.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'Vorkommniss';
        Blockly.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'in der Liste';
        Blockly.LANG_LISTS_INDEX_OF_TOOLTIP = 'Sucht die Position (index) eines Elementes in der Liste\n' +
            'Gibt 0 zurück wenn nichts gefunden wurde.';
        Blockly.LANG_LISTS_INDEX_OF_FIRST = 'erstes';
        Blockly.LANG_LISTS_INDEX_OF_LAST = 'letztes';

        Blockly.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_GET_INDEX_TITLE_GET = 'get item at';
        Blockly.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'in list';
        Blockly.LANG_LISTS_GET_INDEX_TOOLTIP = 'Returns the value at the specified position in a list.';

        Blockly.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_SET = 'set item at';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
        Blockly.LANG_LISTS_SET_INDEX_INPUT_TO = 'to';
        Blockly.LANG_LISTS_SET_INDEX_TOOLTIP = 'Sets the value at the specified position in a list.';

        // Variables Blocks.
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = '';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'initialize global';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'name';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'to';
        Blockly.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Returns the value of this variable.';

        Blockly.LANG_VARIABLES_GET_HELPURL = 'http://de.wikipedia.org/wiki/Variable_%28Programmierung%29';
        Blockly.LANG_VARIABLES_GET_TITLE_GET = 'bekommen';
        Blockly.LANG_VARIABLES_GET_INPUT_ITEM = 'Variable';
        Blockly.LANG_VARIABLES_GET_TOOLTIP = 'Gibt den Wert der Variable zurück.';

        Blockly.LANG_VARIABLES_SET_HELPURL = 'http://de.wikipedia.org/wiki/Variable_%28Programmierung%29';
        Blockly.LANG_VARIABLES_SET_TITLE_SET = 'Schreibe';
        Blockly.LANG_VARIABLES_SET_INPUT_TO = 'to';
        Blockly.LANG_VARIABLES_SET_INPUT_ITEM = 'auf';
        Blockly.LANG_VARIABLES_SET_INPUT_TOOLTIP = 'Setzt den Wert einer Variable.';

        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = '';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'initialize local';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_NAME = 'name';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'to';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'in do';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_IN_RETURN = 'in return';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = '';

        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = '';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'initialize local';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'name';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'to';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'in return';
        Blockly.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = '';

        Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'local names';
        Blockly.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

        // Procedures Blocks.
        Blockly.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://de.wikipedia.org/wiki/Prozedur_%28Programmierung%29';
        Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'Funktionsblock';
        Blockly.LANG_PROCEDURES_DEFNORETURN_DO = 'mache';
        Blockly.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Ein Funktionsblock ohne Rückgabewert.';
        
        Blockly.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://en.wikipedia.org/wiki/Procedure_%28computer_science%29';
        Blockly.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'then-return';
        Blockly.LANG_PROCEDURES_DOTHENRETURN_DO = 'do';

        Blockly.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://de.wikipedia.org/wiki/Prozedur_%28Programmierung%29';
        Blockly.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
        Blockly.LANG_PROCEDURES_DEFRETURN_DO = Blockly.LANG_PROCEDURES_DEFNORETURN_DO;
        Blockly.LANG_PROCEDURES_DEFRETURN_RETURN = 'gebe zurück';
        Blockly.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Ein Funktionsblock mit Rückgabewert.';

        Blockly.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Warnung:\n' +
            'dieser Funktionsblock\n' +
            'hat doppelte Parameter.';

        Blockly.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://de.wikipedia.org/wiki/Prozedur_%28Programmierung%29';
        Blockly.LANG_PROCEDURES_CALLNORETURN_CALL = 'Aufruf';
        Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'Funktionsblock';
        Blockly.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Rufe einen Funktionsblock ohne Rückgabewert auf.';

        Blockly.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://de.wikipedia.org/wiki/Prozedur_%28Programmierung%29';
        Blockly.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.LANG_PROCEDURES_CALLNORETURN_CALL;
        Blockly.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
        Blockly.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Rufe einen Funktionsblock mit Rückgabewert auf.';

        Blockly.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'Parameter';
        Blockly.LANG_PROCEDURES_MUTATORARG_TITLE = 'Variable:';

        Blockly.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Markiere Funktionsblock';
        Blockly.LANG_PROCEDURES_CREATE_DO = 'Erzeuge "Aufruf %1"';

        Blockly.LANG_PROCEDURES_IFRETURN_TOOLTIP = 'Wenn der erste Wert wahr (true) ist,\n' +
            'Gebe den zweiten Wert zurück.';
        Blockly.LANG_PROCEDURES_IFRETURN_WARNING = 'Warnung:\n' +
            'Dieser Block darf nur\n' +
            'innerhalb eines Funktionsblock\n' +
            'genutzt werden.';

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