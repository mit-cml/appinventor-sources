// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

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
 * @fileoverview Italian English strings.
 * @author gabryk91@gmail.com (Gabriele Cozzolino)#
 * @author zaffardi@tiscali.it (Andrea Zaffardi)#
 */
'use strict';

goog.provide('Blockly.Msg.it_it');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.it_it.switch_language_to_italian = {
  // Switch language to Italian.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
//Start updating all values to right of equal sign
    Blockly.Msg.DUPLICATE_BLOCK = 'Duplica';
    Blockly.Msg.REMOVE_COMMENT = 'Rimuovi commento';
    Blockly.Msg.ADD_COMMENT = 'Inserisci commento';
    Blockly.Msg.EXTERNAL_INPUTS = 'Parametri esterni';
    Blockly.Msg.INLINE_INPUTS = 'Parametri interni';
    Blockly.Msg.HORIZONTAL_PARAMETERS = 'Disponi parametri orizzontalmente';
    Blockly.Msg.VERTICAL_PARAMETERS = 'Disponi parametri verticalmente';
    Blockly.Msg.DELETE_BLOCK = 'Elimina blocco';

    Blockly.Msg.DELETE_X_BLOCKS = 'Elimina %1 blocchi';
    Blockly.Msg.COLLAPSE_BLOCK = 'Collassa blocco';
    Blockly.Msg.EXPAND_BLOCK = 'Espandi blocco';
    Blockly.Msg.DISABLE_BLOCK = 'Disabilita blocco';
    Blockly.Msg.ENABLE_BLOCK = 'Abilita blocco';
    Blockly.Msg.HELP = 'Aiuto';
    Blockly.Msg.EXPORT_IMAGE = 'Esporta come immagine';
    Blockly.Msg.COLLAPSE_ALL = 'Collassa blocchi';
    Blockly.Msg.EXPAND_ALL = 'Espandi blocchi';
    Blockly.Msg.ARRANGE_H = 'Disponi blocchi orizzontalmente';
    Blockly.Msg.ARRANGE_V = 'Disponi blocchi verticalmente';
    Blockly.Msg.ARRANGE_S = 'Disponi blocchi diagonalmente';
    Blockly.Msg.SORT_W = 'Disponi blocchi per larghezza';
    Blockly.Msg.SORT_H = 'Disponi blocchi per altezza';
    Blockly.Msg.SORT_C = 'Disponi blocchi per categoria';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = 'Modifica valore:';
    Blockly.MSG_NEW_VARIABLE = 'Nuova variabile...';
    Blockly.MSG_NEW_VARIABLE_TITLE = 'Nuovo nome della variabile:';
    Blockly.MSG_RENAME_VARIABLE = 'Rinomina variabile...';
    Blockly.MSG_RENAME_VARIABLE_TITLE = 'Rinomina "%1" variabili a:';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = 'Variabili';
    Blockly.MSG_PROCEDURE_CATEGORY = 'Procedure';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = "Questo blocco non può stare in una definizione";
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = "Seleziona un oggetto valido dalla tendina";
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = "Evento duplicato per lo stesso componente.";

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = 'Premi il riquadro per scegliere un colore.';
    Blockly.Msg.LANG_COLOUR_BLACK = 'nero';
    Blockly.Msg.LANG_COLOUR_WHITE = 'bianco';
    Blockly.Msg.LANG_COLOUR_RED = 'rosso';
    Blockly.Msg.LANG_COLOUR_PINK = 'rosa';
    Blockly.Msg.LANG_COLOUR_ORANGE = 'arancione';
    Blockly.Msg.LANG_COLOUR_YELLOW = 'giallo';
    Blockly.Msg.LANG_COLOUR_GREEN = 'verde';
    Blockly.Msg.LANG_COLOUR_CYAN = 'azzurro';
    Blockly.Msg.LANG_COLOUR_BLUE = 'blu';
    Blockly.Msg.LANG_COLOUR_MAGENTA = 'magenta';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = 'grigio chiaro';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = 'grigio scuro';
    Blockly.Msg.LANG_COLOUR_GRAY = 'grigio';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = 'dividi colore';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = "Una lista di quattro elementi che rappresentano i componenti rosso, verde, blu e alfa. Possono assumere un valore tra 0 e 255";
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = 'crea colore';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = "Crea un colore a partire dai valori forniti per i componenti rosso, verde, blu e opzionalmente alfa";

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = 'Controlli';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = 'Se il valore è vero, esegui il codice seguente.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = 'Se il valore è vero, esegui il codice del primo blocco.\n' +
        'Altrimenti, esegui il codice del secondo blocco.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = 'Se il primo valore è vero, esegui il codice del primo blocco.\n' +
        'Altrimenti, Se il secondo valore è vero, esegui il codice del secondo blocco.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = 'Se il primo valore è vero, esegui il codice del primo blocco.\n' +
        'Altrimenti, se il secondo valore è vero, esegui il codice del secondo blocco.\n' +
        'Se nessuno dei valori è vero, esegui il codice dell’ultimo blocco.';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = 'se';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = 'altrimenti se';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = 'altrimenti';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = 'allora';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = 'se';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = 'Aggiungi, rimuovi, o riordina le selezioni\n' +
        'per riconfigurare questo blocco se.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'altrimenti se';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Aggiungi una condizione al blocco se.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'altrimenti';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Aggiungi in fondo una condizione polivalente al blocco if.';


    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'ripeti';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'fai';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'finchè';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'fino a';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Finchè il valore è vero, esegui il codice seguente.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Finchè il valore è falso, esegui il codice seguente.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = 'Esegui i blocchi compresi tra \'fai\' e finchè il test è '
        + 'vero.';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = 'conta fino a';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = 'da';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = 'a';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = 'fai';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = 'Conta dal numero di partenza al numero finale.\n' +
        'Ad ogni giro, imposta il numero corrente del contatore a\n' +
        'variabile "%1", e dopo esegui quanto segue.';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'per ogni';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'numero';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = 'da';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = 'a';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'per';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = 'fai';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = 'per il range di numeri';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = 'per ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' nel range';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Esegui i blocchi tra \'fai\' sezione per ogni numerica '
        + 'valore nel renge dall\’inizio alla fine, sommando il valore ogni volta.  Usa quanto segue '
        + 'nome della variabile riferita al valore corrente.';



    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'per ogni';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = 'elemento';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'nella lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = 'fai';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = 'per l’elemento in lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = 'per ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' nella lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = 'Esegui i blocchi tra \'fai\'  sezione per ogni elemento contenuto '
        + 'Lista.  Usa questo nome di variabile per riferirti agli elementi della lista corrente.';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'del ciclo';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'interrompi';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'continua con la prossima interazione';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'esci da questo ciclo.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Salta il resto di questo ciclo, e\n' +
        'continua con la prossima interazione.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Attenzione:\n' +
        'Questo blocco può solamente\n' +
        'essere utilizzato all\'interno di un ciclo.';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';;
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = 'finchè';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = 'test';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = 'fai';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = 'finchè';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = 'Esegui i blocchi tra \'fai\' finchè il test è '
        + 'vero.';

        Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = 'se'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'poi';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'altrimenti';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = 'se';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = 'Se la condizione che stai testando è vera,'
        + 'allega il risultato dell\’espressione allo \'then-return\' slot;'
        + 'altrimenti allega il risultato dell’espressione allo \'else-return\' slot;'
        + 'almeno una delle espressioni nello slot saranno processate.';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'fai';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = 'risultato';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = 'esegui i blocchi in \'fai\' e ristituisci il risultato. Utile se hai bisogno di eseguire una procedura prima di salvare il valore sulla variabile.';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = 'fai/risultato';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = 'esegui il risultato';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'valuta ma ignora il risultato'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'valuta ma ignora'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = 'Esegue i blocchi connessi e ignora il risultato (se presente). Utile se necessiti di chiamare una procedura con un valore di ritorno che non ti serve salvare';

    /* [lyn, 10/14/13] Funzione rimossa per ora ma potrà tornare un giorno.
     Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = 'niente';
     Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = 'Non ritorna niente. Usato per inizializzare una variabile o collegato nello slot di ritorno se nessun valore di ritorno è necessario. equivale a nullo o nessuno.';
     */

        Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'apri un altro schermo';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'nomeSchermo';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = 'apri schermo';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Apri un nuovo schermo in un’app multi schermo.';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'apri un altro schermo con un valore in avvio';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'nomeSchermo';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'ValoreAvvio';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = 'apri uno schermo con valore'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Apri un nuovo schermo in un\’app multi schermo e passa il '
        + 'valore di inizio a quello schermo.';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = 'ottieni il valore di avvio';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'nomeSchermo';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'valoreAvvio';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = 'ottieni il valore di avvio';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Restituisce il valore che è stato passato a questo schermo quando quest\’ultimo '
        + 'è stato aperto, di solito da un altro schermo in un\’app multi schermo. Se nessun valore è stato '
        + 'passato, verrà riportato come testo vuoto.';
        Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = 'chiudi schermo';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = 'chiudi schermo';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Chiudi questo schermo';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = 'chiudi lo schermo con un valore';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'risultato';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = 'chiudi lo schermo con un valore';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Chiude lo schermo corrente e passa il valore allo '
        + 'schermo aperto da quest\’ultimo.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = 'chiudi applicazione';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = 'chiudi applicazione';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Chiude tutti gli schermi di questa app e termina il programma.';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = 'ottieni il testo di inizio';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = 'ottieni il testo di inizio';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'ottieni il testo di inizio che è stato passato a questo schermo quando '
        + 'viene avviata un’altra app. Se nessun valore è stato passato, riporta del testo vuoto. Per '
        + 'app multi schermo, usare ottieni ottieni valore di avvio anzichè testo di inizio.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = 'chiudi schermo con testo';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = 'chiudi schermo con testo';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Chiude questo schermo e restituisce il testo all\’app '
        + 'precedente. Per app multi schermo, usa chiudi schermo con valore anzichè '
        + 'chiudi schermo con testo.';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = 'Logici';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Verifica se i due campi sono uguali. \n' +
        'Possono essere comparati sia numeri che stringhe di testo.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Riporta vero se entrambi i campi non sono uguali fra loro.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = 'uguaglianze logiche';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = 'anche';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = 'oppure';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Riporta vero se tutti i campi sono vberi.';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Riporta vero se ogni campo è vero.';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = 'non';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = 'Riporta vero se il campo inserito è falso.\n' +
        'Riporta falso se il campo inserito è vero.';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'vero';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'falso';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Riporta il valore booleano vero.';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Riporta il valore booleano falso.';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = 'Matematici';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = 'Riporta il numero indicato.';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'numero';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Riporta vero se entrambi i numeri sono uguali fra di loro.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Riporta vero se entrambi i numeri non sono uguali fra di loro.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = 'Riporta vero se il primo numero è più piccolo\n' +
        'del secondo numero.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Riporta vero se il primo numero è più piccolo\n' +
        'oppure uguale al secondo numero.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = 'Riporta vero se il primo numero è più grande\n' +
        'del secondo numero.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Riporta vero se il primo numero è più grande\n' +
        'oppure uguale al secondo numero.';
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
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Riporta la somma di due numeri.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Riporta la differenza tra i due numeri.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Riporta la moltiplicazione tra i due numeri.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Riporta il risultato della divisione tra i due numeri.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Riporta il primo numero elevato\n' +
        'alla potenza del secondo numero.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';


  /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = 'cambia';
     Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = 'elemento';
     Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = 'tramite';
     Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = 'Aggiunge un numero alla variabile "%1".';*/


    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = 'radice quadrata';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = 'assoluto';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = 'neg';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'log';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';

    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Riporta la radice quadrata di un numero.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Riporta il valore assoluto di un numero.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Riporta il valore negativo di un numero.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = 'Riporta il logaritmo naturale di un numero.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Riporta e alla potenza di un numero.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Riporta 10 alla potenza di un numero.';*/

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Arrotonda un numero per eccesso o difetto.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Arrotonda il numero inserito\n' +
        'per eccesso';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Arrotonda il numero inserito \n' +
        'per difetto';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = 'arrotonda';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = 'ceiling';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = 'floor';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = 'Riporta il seno dell\'angolo inserito in gradi.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = 'Riporta il coseno dell\'angolo inserito in gradi.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = 'Riporta la tangente dell\'angolo inserito in gradi.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Riporta l\'angolo tra un intervallo di (-90,+90]\n' +
        'gradi col valore del seno ottenuto.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Riporta l\'angolo tra un intervallo di [0, 180)\n' +
        'gradi col valore del coseno ottenuto.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Riporta l\'angolo tra un intervallo di (-90, +90)\n' +
        'gradi col valore della tangente ottenuta.';
    ATAN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Riporta l\'angolo tra un intervallo di (-180, +180]\n' +
        'gradi col valore delle coordinate rettangolari ottenute.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = 'min';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = 'max';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Riporta il più piccolo fra loro..';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Riporta il più grande fra loro..';

    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'modulo di';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'rimanenza di';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'quoziente di';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Riporta il modulo.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Riporta il resto.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Riporta il quoziente.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'intero casuale';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = 'da';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = 'a';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = 'intero casuale tra %1 e %2';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = 'Riporta un numero intero casuale compreso tra il limite massimo\n' +
        'ed il limite minimo. I limiti saranno tagliati per risultare minori\n' +
        'di 2**30.';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'frazione casuale';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Riporta un numero casuale tra 0 and 1.';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'genera set di numeri casuali ripetibili';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = 'valore';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = 'specifica il valore di riferimento\n' +
        'per il generatore di numeri casuali ripetibili';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'converti';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'da radianti a gradi';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'da gradi a radianti';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Restituisce il valore in gradi compreso tra\n' +
        '[0, 360) corrispondente al suo valore in radianti.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Restituisce il valore in radianti compreso tra\n' +
        '[-\u03C0, +\u03C0) corrispondente al suo valore in gradi.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'converti in decimale';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'numero';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'decimali';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = 'converti in numero decimale %1 decimali %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Converte il numero con tanti decimali\n' +
        'quanto specificato.';


    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = 'è un numero?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Controlla se il blocco collegato è numerico.';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = 'Testo';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = 'riga di testo.';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'crea testo con';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = 'Unisce i blocchi in una singola riga di testo.\n'
        + 'se nulla è connesso, crea una riga vuota.';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = 'unire';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'testo';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = 'a';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = 'aggancia testo';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = 'elemento';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = 'Aggancia i testi alla variabile "%1".';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = 'lunghezza';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = 'Riporta il numero di caratteri (spazi inclusi)\n' +
        'nel testo allegato.';
    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'è vuoto';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = 'Restituisce vero se la lunghezza del\n' + 'testo è 0, altrimenti restitusce falso.';
    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = 'confronta testi';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = 'Verifica se il testo1 è lessicograficamente precedente al testo 2.\n'
        + 'se un testo è il prefisso dell\'altro, il testo più breve è\n'
        + 'considerato più piccolo. I caratteri maiuscoli precedono i caratteri minuscoli.';
   Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = 'Verifica se le righe di testo sono identiche, cioè., se hanno gli stessi\n'
        + 'caratteri nello stesso ordine. Questo non è molto comune =\n'
        + 'qualora le righe di testo siano numeri: 123 e 0123 sono =\n'
        + 'ma non testo =.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = 'Verifica se il testo1 è lessicograficamente successivo al al testo 2.\n'
        + 'se un testo è il prefisso dell\'altro, il testo più breve è considerato più piccolo.\n'
        + 'I caratteri maiuscoli precedono i caratteri minuscoli.';
    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = 'lettere nel testo';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = 'Riporta il numero di lettere all\'inizio od alla fine del testo.';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'iniziali';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'finali';*/

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = 'trova';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'occorrenze nel testo';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'nel testo';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = 'Riporta l’indice della prima/ultima occorrenza\n' +
     'del primo testo all\'interno del secondo testo.\n' +
     'Riporta 0 se nessuna occorrenza viene trovata.';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'primo';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'ultimo';*/



    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = 'posizione lettera';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = 'nel testo';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = 'Riporta la lettera alla posizione specificata.';*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'maiuscolo';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'minuscolo';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Trasforma in maiuscolo.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Trasforma in minuscolo.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = 'taglia';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = 'Restituisce una copia del testo con tutti\n'
        + 'gli spazi iniziali e finali rimossi.';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'inizia da';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'segmento';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = 'inizia dal testo %1 segmento %2';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = 'Riporta da che carattere inizia il testo specificato.\n'
        + '1 denota che il segmento viene rilevato all\'inizio. 0 se il\n'
        + 'segmento non viene rilevato nel testo.';
            Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'contiene';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = 'segmento';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = 'contiene il testo %1 segmento %2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = 'Controlla se il segmento è contenuto nel testo.';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = 'a';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = 'a (lista)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'dividi all\'inizio';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'dividi all\'inizio di ogni';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'dividi';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'dividi ogni';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = 'Divide il testo in due pezzi in base alla prima corrispondenza rilevata \n'
        + 'del testo \'a\' come punto di divisione, e crea una lista contenente i due tronconi di testo \n'
        + 'prima e dopo il punto di divisione. \n'
        + 'Dividere "mela,banana,ciliegia,cibo per gatti" con una virgola come punto di divisione \n'
        + 'crea una lista di due elementi: il primo testo sarà "mela" ed il secondo testo sarà \n'
        + '"banana,ciliegia,cibo per gatti". \n'
        + 'da notare che la virgola dopo "mela" non apparirà nel risultato, \n'
        + 'perchè rappresenta il punto di divisione.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Divide il testo in due elementi di una lista, usando la prima ricorrenza di ogni elemento \n'
        + 'nella lista \'lista\' come punto di divisione. \n\n'
        + 'Dividendo "Io amo le mele banane angurie arance." con la lista “(ba,me)” risulterà \n'
        + 'una lista di due elementi, come primo "Io amo le" e come secondo \n'
        + '"nane angurie arance."';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Divide il testo un più pezzi usando il testo \'ogni\' come punto di divisione e produce una lista con i risultati. \n'
        + 'Dividendo "uno,due,tre,quattro" ogni "," (virgola) risulterà una lista “(uno due tre quattro)”. \n'
        + 'Dividendo "una-patata,due-patate,tre-patate,quattro" ogni "-patata", verrà prodotta la lista “(uno due tre quattro)”.'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Divide il testo in una lista, usando ogni elemento della lista \'lista\' come \n'
        + 'punto di divisione, e riporta una lista con i risultati. \n'
        + 'Dividendo "appleberry,banana,cherry,dogfood" con \'lista\' con due elemnti \n'
        + 'virgola e "rry" restituisce una lista di quattro elementi: \n'
        + '"(applebe banana che dogfood)".'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatany';

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = 'stampa';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = 'Stampa il testo specificato, numero o altro valore.';*/

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'chiedi';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'col messaggio';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = 'Chiede all’utente di inserire un testo.';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = 'testo';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = 'numero';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitspaces';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'dividi in spazi';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Divide il testo in pezzi separati da spazi.';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'segmento';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = 'inizio';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'lunghezza';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = 'segmento  testo %1 inizio %2 lunghezza %3';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Estrae il segmento in base ai parametri inseriti\n'
        + 'partendo dal testo specificato dalla posizione specificata. La posizione\n'
        + '1 indica l\'inizio del testo.';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'segmento';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'sostituisci tutto';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'rimpiazzo';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = 'sostituisce tutto il testo %1 segmento %2 rimpiazzo %3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Riporta un nuovo testo ottenuto dalla sostituzione\n'
        + 'del segmento con il rimpiazzo.';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = 'Liste';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = 'crea una lista vuota';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Restituisce una lista, di lunghezza 0, senza elementi';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'crea lista';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Crea una lista con ogni quantità di elementi.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'lista';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Aggiungere, rimuovere, o riordinare gli elementi di questo blocco.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'elemento';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Aggiungi un elemento alla lista.';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = 'elemento';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = 'Aggiungi un elemento alla lista.';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'seleziona un elemento dalla lista';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'indice';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = 'seleziona un elemento della lista  lista %1 indice %2';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Riporta l\'elemento della lista nella posizione dell\'indice.';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = 'è nella lista?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = 'contenuto';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = 'è nella lista? contenuto %1 lista %2'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = 'Riporta vero se il contenuto è un elemento della lista, e '
        + 'falso se non lo è.';

   Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'indice nella lista';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = 'contenuto';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = 'indice nella lista  contenuto %1 lista %2';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = 'Trova la posizione del contenuto nella lista. se non presente '
        + 'nella lista, riporta 0.';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'scegli un elemento casuale';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Scegli un elemento casuale dalla lista.';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'rimpiazza un elemento nella lista';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'indice';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'rimpiazzo';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = 'rimpiazza un elemento nella lista  lista %1 indice %2 rimpiazzo %3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Rimpiazza un elemento nella lista alla posizione specificata.';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'rimuovi un elemento dalla lista';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'indice';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = 'rimuovi un elemento dalla lista  lista %1 indice %2';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Rimuove l\'elemento nella lista alla posizione specificata.';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = 'crea una lista con elementi';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = 'ripetuti';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = 'volte';
     Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = 'Crea una lista con il valore immesso\n' +
     'ripetuto per uno specifico numero di volte.';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = 'lunghezza della lista';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = 'lunghezza della lista lista %1';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = 'Conta il numero di elementi presenti nella lista.';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'attacca alla lista';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'lista1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'lista2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = 'attacca alla lista  lista1 %1 lista2 %2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Aggiunge tutti gli elementi della lista2 alla lista1. Dopo '
        + 'la lista1 includerà tutti gli elementi aggiunti della lista2, ma la lista2 rimarrà invariata.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'aggiungi elementi alla lista';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = ' lista';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'elementi';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = 'aggiungi elementi alla lista lista %1 elementi %2';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Aggiunge elementi in coda alla lista.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = 'lista';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = 'Add, remove, or reorder sections to reconfigure this list block.';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = 'copia lista';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = 'Crea la copia di una lista, comprese eventuali sotto-liste';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = 'è una lista?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = 'contenuto';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = 'Controlla se un elemento è una lista.';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'da lista a riga csv';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Interpreta la lista come la riga di una tabella e restituisce un testo'
        + '\(comma-separated value\) CSV. Ogni elemento della lista riga è considerato un campo,'
        + 'ed è messo tra virgolette nel testo CSV risultante. '
        + 'Gli elementi sono separati da virgole. Il testo risultante non dispone di un separatore di riga '
        + 'alla fine.';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'lista da riga csv';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Analizza il testo come fosse una riga CSV \(comma-separated value\) '
        + 'per produrre una lista di campi. Esiste un errore per le righe di testo contenenti ritorni a capo '
        + '\(linee multiple\). Ok per le righe di testo che '
        + 'terminano in un singolo carattere or CRLF.';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'lista a tabella csv';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interpreta la lista come una tabella e  '
        + 'converte in CSV \(comma-separated value\) il testo. Ogni elemento nalla '
        + 'lista dovrebbe essere di per sé un elenco che rappresenta una riga della tabella CSV. Ogni elemento nella riga '
        + 'lista è considerato come campo, e sarà racchiuso tra doppi apici nel CSV risultante'
        + 'Nel testo risultante, gli elementi saranno separati da una virgola e le righe saranno '
        + 'separate dal CRLF \(\\r\\n\).';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'lista da una tabella csv';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'testo';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Analizza il testo come una tabella CSV \(comma-separated value\) '
        + 'per produrre una lista di righe. Le righe possono essere '
        + 'separate da nuove linee \(\\n\) o CRLF \(\\r\\n\).';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'inserisci elemento nella lista';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = 'indirizzo';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = 'elemento';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = 'inserisce un elemento  lista %1 indice %2 elemento %3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = 'Inserisce un elemento nella lista alla posizione specificata.';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = 'la lista è vuota?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = 'Riporta vero se la lista è vuota.';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = 'cerca coppie';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = 'chiave';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = 'coppie';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = 'nonTrovate';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = 'cerca coppie  chiave %1 coppie %2 nonTrovate %3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = 'Riporta il valore associato alla chiave nella lista di coppie';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = 'trova';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'corrispondenza';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'nella lista';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = 'Riporta l’indice della prima/ultima corrispondenza\n' +
     'trovata dell’elemento nella lista.\n' +
     'Riporta 0 se nulla viene trovato.';
     Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = 'prima';
     Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = 'ultima';

     Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = 'cerca elemento';
     Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'nella lista';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = 'Riporta il valore nella posizione specificata in una lista.';

     Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = 'imposta elemento come';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'nella lista';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = 'a';
     Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = 'Imposta il valore nella posizione specificata in una lista.';*/

 // Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'inizializza variabile globale';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'nome';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'valore';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = 'globale';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Crea una variabile e gli assegna il valore contenuto nei blocchi collegati.';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = 'richiama';
    /* Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = 'elemento'; */ // [lyn, 10/14/13] non usato
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = 'richiama';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = 'Riporta il valore di questa variabile.';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = 'imposta';
    /* Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = 'elemento'; */ // [lyn, 10/14/13] non usato
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = 'valore';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = 'imposta';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = 'Imposta questa variabile in base a quanto inserito.';
    Blockly.Msg.LANG_VARIABLES_VARIABLE = ' variabile';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'initialize variabile locale';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = 'nome';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'valore';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'dentro';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = 'locale';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = 'Ti permette di creare variabili accessibili solamente all\'interno di questi blocchi.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = 'inizializza variabile locale dentro';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    /* // These don't differ between the statement and expression
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'inizializza variabile locale';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'nome';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'valore';
     */
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'dentro';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = 'locale';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = 'Consente di creare variabili che sono accessibili solo nella parte di ritorno di questo blocco.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = 'inizializza variabile locale in ritorno';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'nomi locali';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = 'nome';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = 'crea';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'procedura';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = 'esegui';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = 'crea ';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Procedura senza valore di ritorno.';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'risultato';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = 'crea';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = 'risultato';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = 'Esegue i blocchi in \'esegui\' e riporta il risultato. Utile se è necessario eseguire una procedura prima di applicare il valore ad una variabile.';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = 'crea/risultato';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = 'crea';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = 'risultato';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = 'crea ';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Procedura con risultato di ritorno.';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Attenzione:\n' +
        'Questa procedura\n' +
        'risulta duplicata.';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = 'richiama ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'procedura';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = 'richiama ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Richiama una procedura senza risultati di ritorno.';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = 'richiama senza risultato di ritorno';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = 'richiama ';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Richiama una procedura con risultati di ritorno.';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = 'richiama con risultato di ritorno';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'immetti';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = 'immetti:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Evidenzia Procedura';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = 'quando ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = 'esegui';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = 'richiama ';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = 'richiama ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = 'per il componente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = 'del componente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = 'imposta ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = ' a';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = 'imposta ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = ' a';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = 'del componente';

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

    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL = "/reference/components/userinterface.html#Notifier";
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

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL = "/reference/components/userinterface.html#WebViewer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#webviewerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL = '/reference/components/userinterface.html#webviewerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL = '/reference/components/userinterface.html#webviewermethods';

//Layout components
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL = "/reference/components/layout.html#HorizontalArrangement";
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#horizarrangeproperties';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL = "/reference/components/layout.html#VerticalArrangement";
    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#vertarrangeproperties';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL = "/reference/components/layout.html#TableArrangement";
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

    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL = "/reference/components/media.html#SoundRecorder";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL = '/reference/components/media.html#soundrecorderproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL = '/reference/components/media.html#soundrecorderevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL = '/reference/components/media.html#soundrecordermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL = "/reference/components/media.html#SpeechRecognizer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL = '/reference/components/media.html#speechrecognizerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL = '/reference/components/media.html#speechrecognizerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL = '/reference/components/media.html#speechrecognizermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL = "/reference/components/media.html#TextToSpeech";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL = '/reference/components/media.html#texttospeechproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL = '/reference/components/media.html#texttospeechevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL = '/reference/components/media.html#texttospeechmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL = '/reference/components/media.html#VideoPlayer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#videoplayerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL = '/reference/components/media.html#videoplayerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL = '/reference/components/media.html#videoplayermethods';

// Drawing and Animation components
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_HELPURL = "/reference/components/animation.html#Ball";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL = '/reference/components/animation.html#ballproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL = '/reference/components/animation.html#ballevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL = '/reference/components/animation.html#ballmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_HELPURL = '/reference/components/animation.html#Canvas';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL = '/reference/components/animation.html#canvasproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL = '/reference/components/animation.html#canvasevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL = '/reference/components/animation.html#canvasmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL = "/reference/components/animation.html#ImageSprite";
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL = '/reference/components/animation.html#imagespriteproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL = '/reference/components/animation.html#imagespriteevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL = '/reference/components/animation.html#imagespritemethods';

//Sensor components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL = "/reference/components/sensors.html#AccelerometerSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#accelerometersensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#accelerometersensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#accelerometersensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL = "/reference/components/sensors.html#BarcodeScanner";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL = '/reference/components/sensors.html#barcodescannerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL = '/reference/components/sensors.html#barcodescannerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL = '/reference/components/sensors.html#barcodescannermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL = "/reference/components/sensors.html#LocationSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#locationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#locationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#locationsensormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL = "/reference/components/sensors.html#OrientationSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#orientationsensorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#orientationsensorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#orientationsensormethods';

//Social components
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL = "/reference/components/social.html#ContactPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#contactpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL = '/reference/components/social.html#contactpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL = '/reference/components/social.html#contactpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL = "/reference/components/social.html#EmailPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#emailpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL = '/reference/components/social.html#emailpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL = '/reference/components/social.html#emailpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL = "/reference/components/social.html#PhoneCall";
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL = '/reference/components/social.html#phonecallproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL = '/reference/components/social.html#phonecallevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL = '/reference/components/social.html#phonecallmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL = "/reference/components/social.html#PhoneNumberPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#phonenumberpickerproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL = '/reference/components/social.html#phonenumberpickerevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL = '/reference/components/social.html#phonenumberpickermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_HELPURL = "/reference/components/social.html#Texting";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL = '/reference/components/social.html#textingproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL = '/reference/components/social.html#textingevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL = '/reference/components/social.html#textingmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_HELPURL = "/reference/components/social.html#Twitter";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL = '/reference/components/social.html#twitterproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL = '/reference/components/social.html#twitterevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL = '/reference/components/social.html#twittermethods';

//Storage Components
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL = "/reference/components/storage.html#FusiontablesControl";
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL = '/reference/components/storage.html#fusiontablescontrolproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL = '/reference/components/storage.html#fusiontablescontrolevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL = '/reference/components/storage.html#fusiontablescontrolmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_HELPURL = '/reference/components/storage.html#TinyDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinydbproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL = '/reference/components/storage.html#tinydbevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL = '/reference/components/storage.html#tinydbmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL = "/reference/components/storage.html#TinyWebDB";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL = '/reference/components/storage.html#tinywebdbproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL = '/reference/components/storage.html#tinywebdbevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL = '/reference/components/storage.html#tinywebdbmethods';

//Connectivity components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL = "/reference/components/connectivity.html#ActivityStarter";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#activitystarterproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL = '/reference/components/connectivity.html#activitystarterevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL = '/reference/components/connectivity.html#activitystartermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL = "/reference/components/connectivity.html#BluetoothClient";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothclientproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothclientevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothclientmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL = "/reference/components/connectivity.html#BluetoothServer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#bluetoothserverproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL = '/reference/components/connectivity.html#bluetoothserverevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL = '/reference/components/connectivity.html#bluetoothservermethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_HELPURL = "/reference/components/connectivity.html#Web";
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL = '/reference/components/connectivity.html#webproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL = '/reference/components/connectivity.html#webevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL = '/reference/components/connectivity.html#webmethods';

//Lego mindstorms components
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL = "/reference/components/legomindstorms.html#NxtDirectCommands";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdirectproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdirectmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL = "/reference/components/legomindstorms.html#NxtColorSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtcolorproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtcolorevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtcolormethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL = "/reference/components/legomindstorms.html#NxtLightSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtlightproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtlightevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtlightmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL = "/reference/components/legomindstorms.html#NxtSoundSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtsoundproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtsoundmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL = "/reference/components/legomindstorms.html#NxtTouchSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxttouchproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxttouchevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxttouchmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL = "/reference/components/legomindstorms.html#NxtUltrasonicSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtultrasonicmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL = "/reference/components/legomindstorms.html#NxtDrive";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#nxtdriveproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL = '/reference/components/legomindstorms.html#nxtdrivemethods';

//Internal components
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL = "/reference/components/internal.html#GameClient";
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL = '/reference/components/internal.html#gameclientproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL = '/reference/components/internal.html#gameclientevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL = '/reference/components/internal.html#gameclientmethods';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_HELPURL = "/reference/components/internal.html#Voting";
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL = '/reference/components/internal.html#votingproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL = '/reference/components/internal.html#votingevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL = '/reference/components/internal.html#votingmethods';

//Misc
    Blockly.Msg.SHOW_WARNINGS = "Mostra Avvertimenti";
    Blockly.Msg.HIDE_WARNINGS = "Nascondi Avvertimenti";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "Devi inserire un blocco in ogni slot";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "Questo blocco può essere connesso solamente ad un blocco evento o ad una procedura definita";

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION = "Errore dall\'app Companion";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "Errore di connessione rete";
    Blockly.Msg.REPL_NETWORK_ERROR = "Errore di rete";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART = "Errore di rete nel comunicare con l\'app Companion.<br />Prova a riavviare l\'app Companion e a riconnetterti";
    Blockly.Msg.REPL_OK = "OK";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK = "Controllo versione app Companion";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'Stai usando una vecchia versione dell\'app Companion. Clicca "OK" per avviare l\'aggiornamento. Controlla il tuo ';
    Blockly.Msg.REPL_EMULATORS = "emulatore";
    Blockly.Msg.REPL_DEVICES = "dispositivo";
    Blockly.Msg.REPL_APPROVE_UPDATE = " lo schermo perchè ti sarà chiesto di approvare l\'aggiornamento.";
    Blockly.Msg.REPL_NOT_NOW = "Non adesso";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 = "Stai usando una vecchia versione dell'app Companion.<br/><br/>Questa versione di App Inventor deve essere usata con l\'app Companion versione";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE = "Stai usando una vecchia versione dell'app Companion. Non è necessario aggiornarla subito ma ricordati di farlo al più presto.";
    Blockly.Msg.REPL_DISMISS = "Respingi";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "Aggiornamento Software";
    Blockly.Msg.REPL_OK_LOWER = "Ok";
    Blockly.Msg.REPL_GOT_IT = "Fatto";
    Blockly.Msg.REPL_UPDATE_INFO = 'L\'aggiornamento è ora installato sul tuo dispositivo. Controlla lo schermo del tuo dispositivo (o emulatore) e approva l\'installazione quando richiesto.<br /><br />IMPORTANTE: Ad aggiornamento completato, Scegli "DONE" (non cliccare su "apri"). Dopo apri App Inventor nel browser, clicca su "Connetti" e selezione "Reset Connessione".';

    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "Impossibile inviare l\'aggiornamento al tuo dispositivo/emulatore";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "Impossibile caricare l\'aggiornamento dal derver di App Inventor";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "Impossibile caricare l\'aggiornamento dal derver di App Inventor (il server non risponde)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "Stiamo scaricando l\'aggiornamento dal server di App Inventor, si prega di attendere";
    Blockly.Msg.REPL_RUNTIME_ERROR = "Runtime Error";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS = "<br/><i>Nota:</i>&nbsp;Non verrà riportato nessun errore per 5 secondi.";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "Connessione via cavo USB";
    Blockly.Msg.REPL_STARTING_EMULATOR = "Avvio dell\'emulatore<br/>Attendere: Potrebbero essere necessari uno o due minuti.";
    Blockly.Msg.REPL_CONNECTING = "Connecting...";
    Blockly.Msg.REPL_CANCEL = "Annulla";
    Blockly.Msg.REPL_GIVE_UP = "Abbandona";
    Blockly.Msg.REPL_KEEP_TRYING = "Continua a provare";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "Connessione fallita";
    Blockly.Msg.REPL_NO_START_EMULATOR = "Non è stato possibile avviare l\'app Companion dall\'emulatore";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "Siete collegati?";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE = "AI2 non rileva il tuo dispositivo, controlla che il cavo sia collegato ed i driver siano quelli corretti.";
    Blockly.Msg.REPL_HELPER_Q = "Aiuto?";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'L\'aiutante pare non essere in esecuzione<br /><a href="http://appinventor.mit.edu" target="_blank">Necessiti di aiuto?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB connesso, attendere ";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = " qualche secondo affinchè sia tutto avviato.";
    Blockly.Msg.REPL_EMULATOR_STARTED = "Emulatore avviato, attendere ";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE = "Avvio dell\'app Companion sul telefono collegato.";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR = "Avvio dell\'app Companion sull'emulatore.";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING = "Avvio dell\'app Companion, attendere ";
    Blockly.Msg.REPL_VERIFYING_COMPANION = "verifica che l\'app Companion sia avviata....";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION = "Connessione all\'app Companion";
    Blockly.Msg.REPL_TRY_AGAIN1 = "Tentativo fallito di connessione all\'app Companion, riprovare.";
    Blockly.Msg.REPL_YOUR_CODE_IS = "Il tuo codice è";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "Sei sicuro?";
    Blockly.Msg.REPL_FACTORY_RESET = 'Questo resetterà l\'Emulatore riportandolo allo stato di "fabbrica". Sarà necessario installare nuovamente l\'app Companion.';

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "Sei sicuro di voler cancellare tutti i %1 blocchi?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "Generazione Yail";
    Blockly.Msg.DO_IT = "Esegui";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "Rimuovi Errore";
    Blockly.Msg.CAN_NOT_DO_IT = "Impossibile eseguire";
    Blockly.Msg.CONNECT_TO_DO_IT = 'Devi essere connesso all\'app companion o all\'emulatore per usare "Esegui"';

  }
};

// Initalize language definition to Italian
Blockly.Msg.it_it.switch_language_to_italian.init();

