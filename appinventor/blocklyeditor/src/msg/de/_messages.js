// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2012-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * Visual Blocks Language
 *
 * Copyright © 2012 Google Inc.
 * Copyright © 2012-2017 Massachusetts Institute of Technology
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
 * @fileoverview Traditional German strings.
 * @author mckinney@gmail.com (Andrew F. McKinney)
 */
'use strict';

goog.provide('AI.Blockly.Msg.de');

goog.require('Blockly.Msg.de');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.de.switch_language_to_german = {
  // Switch language to German.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.UNDO = 'Rückgängig machen';
    Blockly.Msg.REDO = 'Wiederherstellen';
    Blockly.Msg.CLEAN_UP = 'Blöcke aufräumen';
    Blockly.Msg.DUPLICATE_BLOCK = 'Duplizieren';
    Blockly.Msg.REMOVE_COMMENT = 'Kommentar entfernen';
    Blockly.Msg.ADD_COMMENT = 'Kommentar hinzufügen';
    Blockly.Msg.EXTERNAL_INPUTS = 'Externe Eingänge';
    Blockly.Msg.INLINE_INPUTS = 'Inzeilige Eingänge';
    Blockly.Msg.HORIZONTAL_PARAMETERS = 'Parameter horizontal anordnen';
    Blockly.Msg.VERTICAL_PARAMETERS = 'Parameter vertikal anordnen';
    Blockly.Msg.CONFIRM_DELETE = 'Löschen bestätigen';
    Blockly.Msg.DELETE_ALL_BLOCKS = "Alle %1 Blöcke löschen?";
    Blockly.Msg.DELETE_BLOCK = 'Block löschen';
    Blockly.Msg.DELETE_X_BLOCKS = '%1 Blöcke löschen';
    Blockly.Msg.COLLAPSE_BLOCK = 'Block zuklappen';
    Blockly.Msg.EXPAND_BLOCK = 'Block aufklappen';
    Blockly.Msg.DISABLE_BLOCK = 'Block deaktivieren';
    Blockly.Msg.ENABLE_BLOCK = 'Block aktivieren';
    Blockly.Msg.HELP = 'Hilfe';
    Blockly.Msg.EXPORT_IMAGE = 'Blöcke als Bild herunterladen';
    Blockly.Msg.COLLAPSE_ALL = 'Blöcke zuklappen';
    Blockly.Msg.EXPAND_ALL = 'Blöcke aufklappen';
    Blockly.Msg.ARRANGE_H = 'Blöcke horizontal anordnen';
    Blockly.Msg.ARRANGE_V = 'Blöcke vertikal anordnen';
    Blockly.Msg.ARRANGE_S = 'Blöcke diagonal anordnen';
    Blockly.Msg.SORT_W = 'Blöcke nach Breite sortieren';
    Blockly.Msg.SORT_H = 'Blöcke nach Höhe sortieren';
    Blockly.Msg.SORT_C = 'Blöcke nach Kategorie sortieren';
    Blockly.Msg.COPY_TO_BACKPACK = 'Zum Rucksack hinzufügen';
    Blockly.Msg.COPY_ALLBLOCKS = 'Alle Blöcke in den Rucksack kopieren';
    Blockly.Msg.REMOVE_FROM_BACKPACK = 'Vom Rucksack entfernen';
    Blockly.Msg.BACKPACK_GET = 'Alle Blöcke vom Rucksack einfügen';
    Blockly.Msg.BACKPACK_EMPTY = 'Rucksack leeren';
    Blockly.Msg.BACKPACK_CONFIRM_EMPTY = 'Bist Du dir sicher, dass Du den Rucksack leeren möchtest?';
    Blockly.Msg.BACKPACK_DOC_TITLE = "Rucksackinformationen";
    Blockly.Msg.SHOW_BACKPACK_DOCUMENTATION = "Rucksack Dokumentation anzeigen"; 
    Blockly.Msg.BACKPACK_DOCUMENTATION = "Der Rucksack ist eine Kopieren-/Einfügen-Funktion. Sie erlaubt es, Blöcke von einem Projekt oder einem Screen " +
        " zu kopieren und in ein anderes Projekt oder einem anderen Screen einzufügen. " +
        " Um zu kopieren, kannst Du per Drag und Drop die Blöcke in den Rucksack ablegen. Um einzufügen, " +
        " klicke auf das Rucksacksymbol und lege die Blöcke in den Arbeitsbereich per Drag und Drop ab." +
        "</p><p>Falls Du den MIT App Inventor verlässt und sich Blöcke in Deinem Rucksack befinden, " +
        " werden sie noch dort sein, wenn Du dich das nächste Mal einloggst." +
        "</p><p>Für weitere Dokumentation und einem Anleitungsvideo, besuche:" +
        '</p><p><a href="http://ai2.appinventor.mit.edu/reference/other/backpack.html" target="_blank">http://ai2.appinventor.mit.edu/reference/other/backpack.html</a>';
    Blockly.Msg.ENABLE_GRID = 'Gitter im Arbeitsbereich einblenden'; 
    Blockly.Msg.DISABLE_GRID = 'Gitter im Arbeitsbereich ausblenden';
    Blockly.Msg.ENABLE_SNAPPING = 'Einrasten in Gitter aktivieren'; 
    Blockly.Msg.DISABLE_SNAPPING = 'Einrasten in Gitter deaktivieren';

// Variable renaming.
    Blockly.Msg.CHANGE_VALUE_TITLE = 'Wert ändern:'; 
    Blockly.Msg.NEW_VARIABLE = 'Neue Variable...';
    Blockly.Msg.NEW_VARIABLE_TITLE = 'Neuer Variablenname:';
    Blockly.Msg.RENAME_VARIABLE = 'Variable umbenennen...';
    Blockly.Msg.RENAME_VARIABLE_TITLE = 'Alle "%1" Variablen umbenennen in:';

// Toolbox.
    Blockly.Msg.VARIABLE_CATEGORY = 'Variablen';
    Blockly.Msg.PROCEDURE_CATEGORY = 'Prozeduren';

// Warnings/Errors
    Blockly.Msg.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = "Dieser Block darf nicht in einer Definition sein"; 
    Blockly.Msg.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = "Wählen Sie einen gültigen Eintrag im Dropdown."; 
    Blockly.Msg.ERROR_DUPLICATE_EVENT_HANDLER = "Das ist ein duplizierter Ereignisbehandler für diese Komponente."; 
    Blockly.Msg.ERROR_COMPONENT_DOES_NOT_EXIST = "Komponente existiert nicht";
    Blockly.Msg.ERROR_BLOCK_IS_NOT_DEFINED = "Dieser Block ist nicht definiert. Diesen Block löschen!";
    Blockly.Msg.ERROR_BREAK_ONLY_IN_LOOP = "Der Break-Block sollte nur innerhalb von Schleifen verwendet werden"; 

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = 'Auf das Quadrat klicken, um eine Farbe zu wählen.';
    Blockly.Msg.LANG_COLOUR_BLACK = 'schwarz';
    Blockly.Msg.LANG_COLOUR_WHITE = 'weiss';
    Blockly.Msg.LANG_COLOUR_RED = 'rot';
    Blockly.Msg.LANG_COLOUR_PINK = 'rosa';
    Blockly.Msg.LANG_COLOUR_ORANGE = 'orange';
    Blockly.Msg.LANG_COLOUR_YELLOW = 'gelb';
    Blockly.Msg.LANG_COLOUR_GREEN = 'grün';
    Blockly.Msg.LANG_COLOUR_CYAN = 'cyan';
    Blockly.Msg.LANG_COLOUR_BLUE = 'blau';
    Blockly.Msg.LANG_COLOUR_MAGENTA = 'magenta';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = 'hellgrau';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = 'dunkelgrau';
    Blockly.Msg.LANG_COLOUR_GRAY = 'grau';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = 'Farbe aufteilen';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = "Eine Liste mit vier Elementen, je im Bereich 0 bis 255, die die Rot-, Grün-, Blau- und Alpha-Komponenten repräsentieren.";
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = 'Farbe erstellen';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = "Eine Farbe mit den gegebenen Rot-, Grün-, Blau- und optionalen Alpha-Komponenten.";

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = 'Steuerung';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = 'Wenn eine Bedingung wahr (true) ist, dann führe einige Anweisungen aus.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = 'Wenn eine Bedingung wahr (true) ist, dann führe den ersten Block Anweisungen aus.\n' +
        'Ansonsten führe den zweiten Block Anweisungen aus.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = 'Wenn die erste Bedingung wahr (true) ist, dann führe den ersten Block Anweisungen aus.\n' +
        'Ansonsten, wenn die zweite Bedingung wahr (true) ist, führe den zweiten Block Anweisungen aus.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = 'Wenn die erste Bedingung wahr (true) ist, dann führe den ersten Block Anweisungen aus.\n' +
        'Oder, wenn die zweite Bedingung wahr (true) ist, führe den zweiten Block Anweisungen aus.\n' +
        'Falls keine der beiden Bedingungen wahr (true) ist, dann führe den letzten Block Anweisungen aus.';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = 'wenn';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = 'sonst wenn';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = 'sonst';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = 'dann';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = 'wenn';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = 'Sektionen hinzufügen, entfernen oder neu ordnen,\n' +
        'um diesen wenn-Block neu zu konfigurieren.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'sonst wenn';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Eine Bedingung dem wenn-Block hinzufügen.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'sonst';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Eine sonst-Bedingung hinzufügen. Führt einen Block\n' +
        'Anweisungen aus, falls keine Bedingung zutrifft.';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'wiederhole';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'mache';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'solange';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'bis';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Führe einen Block Anweisungen solange die Bedingung wahr (true) ist aus.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Führe einen Block Anweisungen solange wie die Bedingung falsch (false) ist aus.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = 'Führt den Block Anweisungen in der \'mache\'-Sektion aus, solange der Test '
        + 'wahr (true) ist.';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = 'Zähle';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = 'von';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = 'bis';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = 'mache';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = 'Zähle von einem Startwert bis zu einem Zielwert.\n' +
        'Setze pro Zählung die Variable "%1" auf die gezählte Zahl\n' +
        'und führe dann Anweisungen aus.';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'für jede';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'Zahl';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = 'von';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = 'bis';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'um';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = 'mache';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = 'für eine Zahl im Bereich';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = 'für ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' im Bereich';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Führt die Anweisungen in der \'mache\'-Sektion für jeden numerischen Wert '
        + 'im Bereich von Start bis Ende aus und erhöht den Wert jedes Mal. Benutze den gegebenen '
        + 'Variablennamen, um auf den aktuellen Wert zu verweisen.';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'für jedes';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = 'Element';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'in Liste';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = 'mache';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = 'für Element in Liste';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = 'für ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' in Liste';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = 'Führt die Anweisungen in der \'mache\'-Sektion  für jedes Element in '
        + 'der Liste aus. Verwende den gegebenen Variablennamen, um auf das aktuelle Listenelement zu verweisen.';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'Schleife';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'abbrechen';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'fahre mit der nächsten Iteration fort';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Die aktuelle Schleife abbrechen.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Ignoriere die restlichen Anweisungen dieser Schleife\n' +
        'und fahre mit der nächsten Iteration fort.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Warnung:\n' +
        'Dieser Block darf nur innerhalb\n' +
        'einer Schleife verwendet werden.';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';;
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = 'während';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = 'mache';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = 'während';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = 'Führt die Anweisungen der \'mache\'-Sektion aus, solange der test '
        + 'wahr (true) ist.';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = 'wenn'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'dann';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'sonst';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = 'wenn';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = 'Wenn die getestete Bedingung wahr (true) ist,'
        + ' gebe das Resultat der Auswertung des Ausdrucks, der im \'dann\'-Sektion eingefügt ist, zurück; '
        + 'ansonsten gebe das Resultat der Auswertung des Ausdrucks, der im \'sonst\'-Sektion eingefügt ist, zurück; '
        + 'es wird maximal ein Ausdruck der Rückgabesektionen ausgewertet.';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'mache';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = 'Resultat';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = 'Führt die Blöcke in \'mache\' aus und gibt eine Anweisung zurück. Nützlich, wenn eine Prozedur ausgeführt werden soll bevor ein Wert einer Variable zurückgegeben wird.';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = 'mache/Resultat';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = 'mache Resultat';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'auswerten aber Resultat ignorieren'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'auswerten aber ignorieren'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = 'Führt den verbundenen Codeblock aus und ignoriert den zurückgegebenen Wert (falls vorhanden). Nützlich, wenn eine Prozedur aufgerufen werden soll, die einen Rückgabewert hat, der nicht benötigt wird.';

    /* [lyn, 10/14/13] Removed for now. May come back some day.
     Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = 'nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = 'Returns nothing. Used to initialize variables or can be plugged into a return socket if no value needed to return. this is equivalent to null or None.';
     */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'ein weiterer Bildschirm öffnen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'bildschirmName';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = 'Bildschirm öffnen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Öffnet einen neuen Bildschirm in einer Multi-Bildschirm-App.';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'öffne einen weiteren Bildschirm mit startwert';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'bildschirmName';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'startWert';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = 'öffne Bildschirm mit Wert'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Öffnet einen neuen Bildschirm in einer Multi-Bildschirm-App und gibt den '
        + 'Startwert diesem Bildschirm weiter.';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = 'gib Startwert';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'bildschirmName';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'startwert';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = 'gib Startwert';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Gibt den Startwert, der diesem Bildschirm beim Öffnen weitergegeben wurde, '
        + 'zurück. Normalerweise wird der Startwert durch einen anderen Bildschirm des Multi-Bildschirm-App weitergegeben. Wenn kein Wert '
        + 'weitergegeben wurde, wird ein leerer Text zurückgegeben.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = 'Bildschirm schliessen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = 'Bildschirm schliessen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Schliesst den aktuellen Bildschirm';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = 'Schliesse Bildschirm mit Wert';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'Resultat';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = 'Schliesse Bildschirm mit Wert';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Schliesst den aktuellen Bildschirm und gibt ein Resultat dem Bildschirm, der '
        + 'diesen Bildschirm geöffnet hat, zurück.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = 'Applikation schliessen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = 'Applikation schliessen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Schliesst alle Bildschirme in dieser App und stoppt die App.';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = 'gib Starttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = 'gib Starttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'Gibt den Klartext zurück, der diesem Bildschirm weitergegeben wurde, '
        + 'als er von einer anderen App gestartet wurde. Falls kein Wert weitergegeben wurde, wird ein leerer Text zurückgegeben. '
        + 'Für Multi-Bildschirm-Apps sollte gib Startwert statt gib Starttext verwendet werden.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = 'schliesse Bildschirm mit Klartext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = 'schliesse Bildschirm mit Klartext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Schliesst den aktuellen Bildschirm und gibt den Text der App zurück, die diesen Bildschirm '
        + 'geöffnet hat.   Dieser Befehl ist für das Zurückgeben von Text an Aktivitäten, die nicht im App Inventor sind. Der Befehl wird nicht für die Rückgabe '
        + 'an App Inventor Bildschirme verwendet. Für App Inventor Bildschirme, wie Multi-Bildschirm-Apps, sollte schliesse Bildschirm mit Wert statt schliesse'
        + 'Screen mit Klartext verwendet werden.';

    Blockly.Msg.LANG_CONTROLS_BREAK_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#break';
    Blockly.Msg.LANG_CONTROLS_BREAK_TITLE = 'break';
    Blockly.Msg.LANG_CONTROLS_BREAK_INPUT_TEXT = 'Wert';
    Blockly.Msg.LANG_CONTROLS_BREAK_COLLAPSED_TEXT = 'break';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = 'Logik';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Testet, ob zwei Dinge gleich sind. \n' +
        'Die Dinge, die verglichen werden, können irgendwelche Dinge sein, nicht nur Zahlen. \n' +
        'Zahlen werden gleich wie ihre gedruckte Form als Zeichenkette betrachtet. \n' +
        'Zum Beispiel ist die Zahl 0 gleich wie der Text \"0\". Zudem sind zwei Zeichenketten, \n' +
        'die Zahlen repräsentieren, gleich, wenn die Zahlen gleich sind, zum Beispiel \n' +
        '\"1\" ist gleich wie \"01\".';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Gibt wahr (true) zurück, wenn die Eingaben nicht gleich sind.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = 'logik gleich';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = 'und';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = 'oder';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Gibt wahr (true) zurück, wenn alle Eingaben wahr sind.';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Gibt wahr (true) zurück, wenn irgendeine Eingabe wahr (true) ist.';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = 'nicht';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = 'Gibt wahr (true) zurück, wenn die Eingabe falsch (false) ist.\n' +
        'Gibt falsch (false) zurück, wenn die Eingabe wahr (true) ist.';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'wahr';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'falsch';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Gibt die Boolean wahr (true) zurück.';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Gibt die Boolean falsch (false) zurück.';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = 'Mathematik';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = 'Eine Zahl.';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'Zahl';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Gibt wahr (true) zurück, wenn beide Zahlen gleich sind.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Gibt wahr (true) zurück, wenn beide Zahlen nicht gleich sind.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = 'Gibt wahr (true) zurück, wenn die erste Zahl kleiner als\n' +
        'die zweite Zahl ist.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Gibt wahr (true) zurück, wenn die erste Zahl kleiner\n' +
        'oder gleich die zweite Zahl ist.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = 'Gibt wahr (true) zurück, wenn die erste Zahl grösser\n' +
        'als die zweite Zahl ist.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Gibt wahr (true) zurück, wenn die erste Zahl grösser\n' +
        'oder gleich die zweite Zahl ist.';
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
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Gibt die Summe der beiden Zahlen zurück.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Gibt die Differenz der beiden Zahlen zurück.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Gibt das Produkt der beiden Zahlen zurück.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Gibt den Quotient der beiden Zahlen zurück.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Gibt das Resultat der ersten Zahl potenziert\n' +
        'mit der zweiten Zahl zurück.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = 'change';
     Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = 'item';
     Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = 'by';
     Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = 'Add a number to variable "%1".';*/


    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = 'Quadratwurzel';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = 'Absolutwert';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = 'neg';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'log';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Gibt die Quadratwurzel einer Zahl zurück.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Gibt den Absolutwert einer Zahl zurück.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Gibt die Negation einer Zahl zurück.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = 'Gibt den natürlichen Logarithmus zurück, d.h. den Logarithmus zur Basis e (2.71828...)';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Gibt e (2.71828...) potenziert mit einer Zahl zurück.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Return 10 to the power of a number.';*/

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Eine Zahl auf- oder abrunden.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Rundet die Eingabe auf die kleinste Ganzzahl,\n' +
        'die nicht kleiner als die Eingabe ist';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Rundet die eingabe auf die grösste Ganzzahl,\n' +
        'die nicht grösser als die Eingabe ist';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = 'runden';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = 'aufrunden';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = 'abrunden';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = 'Gibt den Sinus eines gegebenen Winkels in Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = 'Gibt den Kosinus eines gegebenen Winkels in Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = 'Gibt den Tangens eines gegebenen Winkels in Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Gibt den Winkel anhand eines gegebenen Sinuswertes im\n' +
        'Bereich (-90,+90] Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Gibt den Winkel anhand eines gegebenen Kosinuswertes im\n' +
        'Bereich [0, 180) Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Gibt den Winkel anhand eines gegebenen Tangenswertes im\n' +
        'Bereich (-90, +90) Grad an.';
    ATAN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Gibt den Winkel anhand der gegebenen kartesischen Koordinaten im\n' +
        'Bereich (-180, +180] Grad an.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = 'min';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = 'max';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Gibt das Kleinste der Argumente zurück..';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Gibt das Grösste der Argumente zurück..';

    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'Modulo von';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'Rest von';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'Quotient von';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Gibt den Modulo zurück.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Gibt den Rest zurück.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Gibt den Quotient zurück.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'zufällige Zahl';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = 'zwischen';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = 'bis';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = 'zufällige Zahl zwischen %1 bis %2';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = 'Gibt eine zufällige Ganzzahl zwischen der oberen Grenze\n' +
        'und der unteren Grenzen zurück. Die Grenzen werden zu einem Wert kleiner\n' +
        'als 2**30 abgeschnitten.';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'zufälliger Bruch';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Gibt eine zufällige Zahl zwischen 0 und 1 zurück.';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'setze einen zufälligen Anfangswert';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = 'auf';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = 'Definiert ein numerischer Anfangswert\n' +
        'für den Zufallszahlengenerator';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'umwandeln';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'Radiant in Grad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'Grad in Radiant';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Gibt den Wert in Grad im Bereich \n' +
        '[0, 360) entsprechend dem Argument in Radiant zurück.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Gibt den Wert in Radiant im Bereich\n' + 
        '[-\u03C0, +\u03C0) entsprechend dem Argument in Grad zurück.'; 
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'als Dezimalzahl formatieren'; //format as decimal
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'Zahl';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'Stellen';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = 'als Dezimalzahl formatieren Zahl %1 Stellen %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Gibt die Zahl als Dezimalzahl formatiert zurück, mit\n' +
        'der definierten Anzahl Stellen.';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = 'ist Zahl?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Testet, ob etwas eine Zahl ist.';

    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = 'verwendet 10 als Basis?';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = 'Testet, ob etwas eine Zeichenkette ist, die eine positive Zahl zur Basis 10 repräsentiert.';

    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = 'ist hexadezimal?';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = 'Testet, ob etwas eine Zeichenkette ist, die eine Hexadezimalzahl repräsentiert.';

    Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = 'ist binär?';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = 'Testet, ob etwas eine Zeichenkette ist, die eine Binärzahl repräsentiert.';


    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = 'Zahl konvertieren';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = 'von Basis 10 zu hexadezimal';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = 'Nimmt eine positive Ganzzahl zur Basis 10 und gibt eine Zeichenkette zurück, die der Zahl in hexadezimal entspricht';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = 'von hexadezimal zu Basis 10';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = 'Nimmt eine Zeichenkette, die eine Hexadezimalzahl repräsentiert, und gibt eine Zeichenkette zurück, die der Zahl zur Basis 10 entspricht'; 

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = 'von Basis 10 zu binär';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = 'Nimmt eine positive Ganzzahl zur Basis 10 und gibt eine Zeichenkette zurück, die der Zahl in binär entspricht'; 

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = 'von binär zu Basis 10';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = 'Nimmt eine Zeichenkette, die eine Binärzahl repräsentiert, und gibt eine Zeichenkette zurück, die der Zahl zur Basis 10 entspricht';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = 'Ein Text.';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'erstelle einen Text mit';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = 'Fügt alle Eingaben an, um einen einzigen Text zu bilden.\n'
        + 'Falls es keine Eingaben gibt, wird ein leerer Text erstellt.';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = 'verbinde';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'Zeichenkette';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = 'an';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = 'Text anfügen';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = 'Element';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = 'Text der Variable "%1" anfügen.';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = 'Länge';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = 'Gibt eine Anzahl Buchstaben (inklusive Leerzeichen)\n' +
        'im gegebenen Text zurück.';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'ist leer';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = 'Gibt wahr (true) zurück, wenn die Länge des Textes\n' + 
        '0 ist, ansonsten falsch (false).';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_NEQ = ' ≠';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = 'Texte vergleichen';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = 'Testet, ob text1 lexikographisch kleiner als text2 ist.\n'
        + 'Wenn ein Text dem Präfix des anderen entspricht, wird der kürzere Text als\n'
        + 'kleiner betrachtet. Grossbuchstaben haben Vorrang über Kleinbuchstaben.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = 'Testet, ob zwei Texte identisch sind, d.h. die gleichen\n'
        + 'Buchstaben in der gleichen Reihenfolge haben. Das unterscheidet sich vom normalen =,\n'
        + 'wenn die Texte Zahlen sind: 123 und 0123 sind als Zahlen =\n'
        + 'aber nicht Text =.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = 'Gibt an, ob text1 lexikographisch grösser als text2 ist.\n'
        + 'Wenn ein Text dem Präfix des anderen entspricht, wird der kürzere Text als\n'
        + 'kleiner betrachtet. Grossbuchstaben haben Vorrang über Kleinbuchstaben.';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP = "Produziert Text wie ein Text Block. Der Unterschied ist, dass der Text\n"
        + "nicht einfach aufgedeckt werden kann, indem die APK der App untersucht wird. Dann verwenden, wenn Apps erstellt werden,\n"
        + "die vertrauliche Informationen, zum Beispiel API Schlüssel, enthalten.\n"
        + "Warnung: Diese Lösung bietet nur sehr geringen Schutz gegen erfahrene Angreifer.";
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE = 'Verschleierter Text';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#obfuscatetext';

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'grossschreiben';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'kleinschreiben';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Gibt eine Kopie der Text Argumente, umgewandelt in Grossbuchstaben, zurück.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Gibt eine Kopie des Text Arguments, umgewandelt in Kleinbuchstaben, zurück.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = 'kürzen';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = 'Gibt eine Kopie der Text Argumente zurück, wo alle\n'
        + 'führende oder folgende Leerzeichen entfernt worden sind.';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'beginnt bei';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'Stück';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = 'beginnt bei  Text %1 Stück %2';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = 'Gibt den Startindex des Stücks im Text zurück.\n'
        + 'Index 1 kennzeichnet den Anfang des Textes. Gibt 0 zurück, wenn\n'
        + 'das Stück nicht im Text ist.';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'enthält';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = 'Stück';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = 'enthält  Text %1 Stück %2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = 'Testet, ob das Stück im Text enthalten ist.';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = 'bei';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = 'bei (Liste)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'teilen beim ersten';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'teilen beim ersten von irgendeinem';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'teilen';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'teilen bei allen';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = 'Teilt den gegebenen Text in zwei Teile anhand der Position des ersten Auftretens des\n'
        + 'Textes \'bei\' als den Trennungspunkt. Gibt dann eine Liste mit zwei Elementen zurück, die dem Stück \n'
        + 'vor dem Trennungspunkt und dem Stück nach dem Trennungspunkt entsprechen. \n'
        + 'Das Teilen von "apple,banana,cherry,dogfood" mit einem Komma als Trennungspunkt \n'
        + 'gibt eine Liste mit zwei Elementen zurück: Das erste Element entspricht dem Text "apple" und das zweite entspricht dem Text \n'
        + '"banana,cherry,dogfood". \n'
        + 'Zu beachten ist, dass das Komma nach "apple" nicht im Resultat vorkommt, \n'
        + 'da es dem Trennungspunkt entspricht.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Teilt den gegebenen Text in eine Liste mit zwei Elementen anhand der Position des\n'
        + 'ersten Auftretens eines beliebigen Elementes der Liste \'bei (Liste)\' als Trennungspunkt. \n\n'
        + 'Das Teilen von "I love apples bananas apples grapes" mittels der Liste "(ba,ap)" gibt \n'
        + 'eine Liste mit zwei Elementen zurück. Das erste Element entspricht "I love" und das zweite Element entspricht \n'
        + '"ples bananas apples grapes."';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Teilt den Text in Stücke anhand des Textes \'bei\' als Trennungspunkt und erzeugt eine Liste der Resultate.  \n'
        + 'Das Teilen von "one,two,three,four" bei "," (Komma) gibt die Liste "(one two three four)" zurück. \n'
        + 'Das Teilen von "one-potato,two-potato,three-potato,four" bei "-potato" gibt die Liste "(one two three four)" zurück.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Teilt den gegebenen Text in eine Liste anhand eines beliebigen Elementes der Liste \'bei (Liste)\' als \n'
        + 'Trennungspunkt und gibt eine Liste der Resultate zurück. \n'
        + 'Das Teilen von "appleberry,banana,cherry,dogfood" anhand der Liste in \'bei (Liste)\', die zwei Elemente enthält, \n'
        + 'wo das erste Element einem Komma und das zweite Element "rry" entspricht, gibt eine Liste mit vier Elementen zurück: \n'
        + '"(applebe banana che dogfood)".';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatany';

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = 'print';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = 'Print the specified text, number or other value.';*/

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'prompt for';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'with message';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = 'Prompt for user input with the specified text.';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = 'text';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = 'number';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitspaces';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'bei Leerzeichen teilen';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Teile den Text in Stücke, die von Leerzeichen getrennt sind.';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'Segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = 'Start';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'Länge';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = 'Segment  Text %1 Start %2 Länge %3';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Extrahiert das Segment der gegebenen Länge vom gegebenen Text,\n' 
        + 'beginnend von der gegebenen Position im gegebenen Text. Position\n' 
        + '1 kennzeichnet den Anfang des Textes.';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'Segment';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'alle ersetzen';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'Ersatz';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = 'ersetze alle Text %1 Segment %2 Ersatz %3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Gibt einen neuen Text zurück, der erhalten wird, indem alle Vorkomnisse des Segments\n'
        + 'mit dem Ersatz ersetzt werden.';

    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isstring';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TITLE = 'ist eine Zeichenkette?';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_INPUT_THING = 'Ding';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TOOLTIP = 'Gibt wahr (true) zurück, wenn <code>Ding</code> eine Zeichenkette ist.';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = 'Listen';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = 'leere Liste erstellen';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Returns a list, of length 0, containing no data records';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'Liste erstellen';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Eine Liste mit einer beliebigen Anzahl Elemente erstellen.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'Liste';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Sektionen hinzufügen, entfernen oder neu ordnen, um diesen Listenblock neu zu konfigurieren.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'Element';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Ein Element der Liste hinzufügen.';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = 'Element';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = 'Ein Element der Liste hinzufügen.';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'Listenelement wählen';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'Index';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = 'Listenelement wählen  Liste %1 Index %2';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Gibt das Element der Liste an der Position Index zurück.';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = 'ist in der Liste?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = 'Ding';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = 'ist in der Liste? Ding %1 Liste %2'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = 'Gibt wahr (true) zurück, wenn Ding ein Element der Liste ist, und '
        + 'falsch (false) falls nicht.';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'Index in Liste';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = 'Ding';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = 'Index in Liste  Ding %1 Liste %2';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = 'Finde die Position von Ding in der Liste. Falls Ding nicht in '
        + 'der Liste ist, 0 zurückgeben.';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'ein zufälliges Element wählen';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Ein zufälliges Element der Liste wählen.';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'Listenelement ersetzen';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'Index';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'Ersatz';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = 'Listenelement ersetzen  Liste %1 Index %2 Ersatz %3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Das nte Element einer Liste ersetzen.';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'Listenelement entfernen';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'Index';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = 'Listenelement entfernen  Liste %1 Index %2';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Entfernt das Element an der gegebenen Position in der Liste.';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = 'create list with item';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = 'repeated';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = 'times';
     Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = 'Creates a list consisting of the given value\n' +
     'repeated the specified number of times.';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = 'Länge der Liste';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = 'Länge der Liste Liste %1';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = 'Anzahl der Listenelemente zählen.';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'der Liste hinzufügen';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'Liste1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'Liste2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = 'der Liste hinzufügen  Liste1 %1 Liste2 %2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Fügt alle Elemente der Liste2 ans Ende der Liste1 hinzu. Nach dem '
        + 'Hinzufügen enthält Liste1 diese zusätzlichen Elemente, aber Liste2 bleibt unverändert.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'Elemente der Liste hinzufügen';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = ' Liste';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'Element';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = 'Elemente der Liste hinzufügen Liste %1 Element %2';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Fügt Elemente ans Ende einer Liste hinzu.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = 'Liste';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = 'Sektionen hinzufügen, entfernen oder neu ordnen, um diesen Listenblock neu zu konfigurieren.';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = 'Liste kopieren';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = 'Erstellt eine Kopie einer Liste. Kopiert auch alle Teillisten.';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = 'ist eine Liste?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = 'Ding';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = 'Testet, ob etwas eine Liste ist.';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'Liste in csv Zeile';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Interpretiert die Liste als eine Zeile in einer Tabelle und gibt ein CSV '
        + '\(mit Komma getrennte Werte\) Text zurück, der der Zeile entspricht. Jedes Element in der Liste wird '
        + 'als Feld betrachtet und ist mit Anführungszeichen im resultierenden CSV Text zitiert. '
        + 'Elemente sind mittels Komma getrennt. Der zurückgegebene Zeilentext hat keine Zeilentrennung '
        + 'am Ende.';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'Liste aus csv Zeile';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Konvertiert eine CSV formattierte Textzeile \(mit Komma getrennte Werte\), '
        + 'um eine Liste von Feldern zu erstellen. Enthält ein Zeilentext innerhalb eines Feldes Zeilenumbrüche  '
        + '\(d.h. mehrere Zeilen\) ohne Escape-Zeichen, wird er als Fehler gekennzeichnet. Ein einziger Zeilenumbruch '
        + 'oder CRLF am Ende eines Zeilentextes ist zugelassen.';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'Liste in csv Tabelle';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interpretiert die Liste als Tabelle in zeilendominierter Formatierung und ' 
        + 'gibt ein CSV \(mit Komma getrennte Werte\) Text zurück, der der Tabelle entspricht. Jedes Element in der '
        + 'Liste sollte selbst eine Liste sein, die einer Zeile innerhalb der CSV Tabelle entspricht. Jedes Element in der '
        + 'Zeilenliste wird als Feld betrachtet und ist mit Anführungszeichen im resultierenden CSV Text zitiert. '
        + 'Im zurückgegebenen Text sind Elemente in Zeilen mittels Komma getrennt und Zeilen sind mittels '
        + 'CRLF getrennt \(\\r\\n\).';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'Liste aus csv Tabelle';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'Text';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Konvertiert ein Text, der einer CSV \(mit Komma getrennte Werte\) formattierten '
        + 'Tabelle entspricht, um eine Liste von Zeilen zu erstellen. Jede Zeile entspricht einer Liste von Feldern. Zeilen können '
        + 'mittels Zeilenumbrüche \(\\n\) oder CRLF \(\\r\\n\) getrennt sein.';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'Listenelement einfügen';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = 'Index';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = 'Element';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = 'Listenelement einfügen  Liste %1 Index %2 Element %3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = 'Ein Element in eine Liste an der definierten Position (Index) einfügen.';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = 'ist Liste leer?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = 'Liste';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = 'Gibt wahr (true) zurück, wenn die Liste leer ist.';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = 'Paare nachschlagen';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = 'Schlüssel';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = 'Paare';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = 'nichtGefunden';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = 'Paare nachschlagen  Schlüssel %1 Paare %2 nichtGefunden %3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = 'Gibt aus der Liste von Paare den Wert zurück, der dem Schlüssel zugeordnet wird';

// Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'global initialisieren';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'Name';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'auf';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = 'global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Erstellt eine globale Variable und weist ihr den Wert der eingefügten Blöcke zu.';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = 'hole';
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = 'hole';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = 'Gibt den Wert dieser Variable zurück.';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = 'setze';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = 'auf';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = 'setze';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = 'Setze diese Variable so dass ihr Wert der Eingabe entspricht.';
    Blockly.Msg.LANG_VARIABLES_VARIABLE = ' Variable';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'lokal initialisieren';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = 'Name';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'auf';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'in';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = 'lokal';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = 'Erlaubt das Erstellen von Variablen, die nur innerhalb des mache-Teil dieses Blockes erreichbar sind.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = 'initialisiere lokal in mache';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'in';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = 'lokal';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = 'Erlaubt das Erstellen von Variablen, die nur innerhalb der Rückgabe dieses Blockes erreichbar sind.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = 'initialisiere lokal in Rückgabe';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'lokale Namen';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = 'Name';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = 'zu';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'Prozedur';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = 'mache';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = 'zu ';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Eine Prozedur, die keinen Wert zurückgibt.';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'Resultat';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = 'mache';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = 'Resultat';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = 'Führt die Blöcke in \'mache\' aus und gibt eine Anweisung zurück. Nützlich wenn eine Prozedur ausgeführt werden soll, bevor einer Variable ein Wert zurückgegeben wird.';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = 'mache/Resultat';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = 'zu';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = 'Resultat';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = 'zu ';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Eine Prozedur, die ein Resultat zurückgibt.';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Warnung:\n' +
        'Diese Prozedur hat\n' +
        'duplizierte Eingaben.';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = 'aufrufen ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'Prozedur';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = 'aufrufen ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Eine Prozedur ohne Rückgabewert aufrufen.';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = 'aufrufen ohne Rückgabe';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = 'aufrufen ';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Eine Prozedur mit einem Rückgabewert aufrufen.';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = 'aufrufen mit Rückgabe';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'Eingaben';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = 'Eingabe:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Prozedur hervorheben';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP = "Dieser Block ist nicht definiert. Diesen Block löschen!";

    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = 'wenn ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = 'mache';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = 'aufrufen ';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = 'aufrufen ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = 'für Komponente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = 'von Komponente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = 'setze ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = 'auf';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = 'setze ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = 'auf';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = 'von Komponente';


//Misc
    Blockly.Msg.SHOW_WARNINGS = "Warnungen einblenden";
    Blockly.Msg.HIDE_WARNINGS = "Warnungen ausblenden";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "Alle Steckplätze sollten mit Blöcken befüllt werden";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "Dieser Block sollte mit einem Ereignis Block oder einer Prozedur Definition verbunden sein";

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION = "Fehler vom Companion";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "Netzwerkverbindungsfehler";
    Blockly.Msg.REPL_NETWORK_ERROR = "Netzwerkfehler";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART = "Netzwerkfehler in der Kommunikation mit dem Companion.<br />Versuche, den Companion neuzustarten und neu zu verbinden";
    Blockly.Msg.REPL_OK = "OK";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK = "Companion Version Check";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'Deine Companion App ist veraltet. Klicke "OK", um das Update zu starten. Achte auf Deine ';
    Blockly.Msg.REPL_EMULATORS = "Emulator";
    Blockly.Msg.REPL_DEVICES = "Geräte";
    Blockly.Msg.REPL_APPROVE_UPDATE = " Bildschirm, da Du gebeten wirst, den Update zu bestätigen.";
    Blockly.Msg.REPL_NOT_NOW = "Nicht jetzt";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 = "Der Companion den Du nutzt ist veraltet.<br/><br/>Diese Version des App Inventor sollte mit der folgenden Companion Version verwendet werden:";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE = "Du verwendest einen veralteten Companion. Du solltest den MIT AI2 Companion baldmöglichst aktualisieren. Falls Du Auto-Updates im Store aktiviert hast, wird der Update bald selbstständig erfolgen.";
    Blockly.Msg.REPL_COMPANION_WRONG_PACKAGE = "Der Companion den Du verwendest wurde für eine andere Instanz des App Inventor gebaut. Um den korrekten Companion zu erhalten, schaue im App Inventor Bildschirm im Menü unter Hilfe->Companion Information.";
    Blockly.Msg.REPL_DISMISS = "Schliessen";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "Software Update";
    Blockly.Msg.REPL_OK_LOWER = "Ok";
    Blockly.Msg.REPL_GOT_IT = "Verstanden";
    Blockly.Msg.REPL_UPDATE_INFO = 'Das Update wird nun auf Deinem Gerät installiert. Achte auf Deinen Geräte- oder Emulatorbildschirm und, wenn aufgefordert, bestätige die Software Installation.<br /><br />WICHTIG: Wenn das Update fertig ist, wähle "FERTIG" \(nicht auf "öffnen" klicken\). Gehe dann zum App Inventor in Deinem Browser, klicke auf das "Verbinden"-Menü und wähle "Verbindung zurücksetzen". Dann verbinde das Gerät erneut.';

    Blockly.Msg.REPL_UPDATE_NO_UPDATE = "Kein Update ist verfügbar";
    Blockly.Msg.REPL_UPDATE_NO_CONNECTION = "Du musst mit einem Companion verbunden sein, um ihn zu aktualisieren";
    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "Update kann nicht an Gerät/Emulator gesendet werden";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "Update kann nicht vom App Inventor Server geladen werden";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "Update kann nicht vom App Inventor Server geladen werden (Server antwortet nicht)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "Das Update wird nun vom App Inventor Server heruntergeladen, bitte warten";
    Blockly.Msg.REPL_RUNTIME_ERROR = "Laufzeitfehler";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS = "<br/><i>Bemerkung:</i>&nbsp;Du wirst in den nächsten 5 Sekunden keinen weiteren Fehlerbericht sehen.";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "Verbindung wird mittels USB Kabel hergestellt";
    Blockly.Msg.REPL_STARTING_EMULATOR = "Android Emulator wird gestartet<br/>Bitte warten: Das könnte ein oder zwei Minuten dauern.";
    Blockly.Msg.REPL_CONNECTING = "Verbindung wird hergestellt...";
    Blockly.Msg.REPL_CANCEL = "Abbrechen";
    Blockly.Msg.REPL_GIVE_UP = "Aufgeben";
    Blockly.Msg.REPL_KEEP_TRYING = "Weiter versuchen";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "Verbindungsfehler";
    Blockly.Msg.REPL_NO_START_EMULATOR = "Der MIT AI Companion konnte innerhalb des Emulators nicht gestartet werden";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "Angeschlossen?";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE = "AI2 kann Dein Gerät nicht sehen. Bitte versichere, dass das Kabel eingesteckt ist und alle Treiber richtig installiert sind.";
    Blockly.Msg.REPL_HELPER_Q = "Helper?";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'Der aiStarter helper scheint nicht zu laufen<br /><a href="http://appinventor.mit.edu" target="_blank">Hilfe benötigt?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB verbunden, wartet ";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = " Sekunden um sicherzustellen, dass alles läuft.";
    Blockly.Msg.REPL_EMULATOR_STARTED = "Emulator gestartet, wartet ";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE = "Companion App wird auf dem verbundenen Smartphone gestartet.";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR = "Companion App wird im Emulator gestartet.";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING = "Companion wird gestartet, wartet ";
    Blockly.Msg.REPL_VERIFYING_COMPANION = "Überprüfung, dass der Companion gestartet ist....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION = "Mit Companion verbinden";
    Blockly.Msg.REPL_TRY_AGAIN1 = "Verbindung zum MIT AI2 Companion fehlgeschlagen, bitte versuche es erneut.";
    Blockly.Msg.REPL_YOUR_CODE_IS = "Dein Code ist";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "Möchtest Du wirklich?";
    Blockly.Msg.REPL_FACTORY_RESET = 'Es wird versucht, Deinen Emulator auf den Werkzustand zurückzusetzen. Wenn Du zuvor den Companion im Emulator aktualisiert hast, wirst Du dies wahrscheinlich erneut machen müssen.';

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "Bist Du dir sicher, dass Du alle %1 dieser Blöcke löschen möchtest?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "Erstelle YAIL";
    Blockly.Msg.DO_IT = "Mache es";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "Fehler beheben";
    Blockly.Msg.CAN_NOT_DO_IT = "Kann es nicht machen";
    Blockly.Msg.CONNECT_TO_DO_IT = 'Du musst mit dem Companion oder Emulator verbunden sein, um "Mache es" zu nutzen';

// Clock Component Menu Items
    Blockly.Msg.TIME_YEARS = "Jahre";
    Blockly.Msg.TIME_MONTHS = "Monate";
    Blockly.Msg.TIME_WEEKS = "Wochen";
    Blockly.Msg.TIME_DAYS = "Tage";
    Blockly.Msg.TIME_HOURS = "Stunden";
    Blockly.Msg.TIME_MINUTES = "Minuten";
    Blockly.Msg.TIME_SECONDS = "Sekunden";
    Blockly.Msg.TIME_DURATION = "Dauer";
  }
};

// Initalize language definition to German
Blockly.Msg.de.switch_blockly_language_to_de.init();
Blockly.Msg.de.switch_language_to_german.init();
