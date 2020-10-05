// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview french strings.
 * @author Gabrielė Stupurienė
 */
'use strict';

goog.provide('AI.Blockly.Msg.lt');

goog.require('Blockly.Msg.lt');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.lt.switch_language_to_lithuanian = {
  // Switch language to Lithuanian.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.UNDO = 'Atšaukti';
    Blockly.Msg.REDO = 'Grąžinti';
    Blockly.Msg.CLEAN_UP = 'Išvalyti blokus';
    Blockly.Msg.HIDE = 'Slėpti darbo vietos valdiklius';
    Blockly.Msg.SHOW = 'Rodyti darbo vietos valdiklius';
    Blockly.Msg.DUPLICATE_BLOCK = 'Dubliuoti';
    Blockly.Msg.REMOVE_COMMENT = 'Pašalinti komentarą';
    Blockly.Msg.ADD_COMMENT = 'Pridėti komentarą';
    Blockly.Msg.EXTERNAL_INPUTS = 'Išorinės įvestys';
    Blockly.Msg.INLINE_INPUTS = 'Tiesioginės įvestys';
    Blockly.Msg.HORIZONTAL_PARAMETERS = 'Parametrus išdėstyti horizontaliai';
    Blockly.Msg.VERTICAL_PARAMETERS = 'Parametrus išdėstyti vertikaliai';
    Blockly.Msg.CONFIRM_DELETE = 'Patvirtinti pašalinimą';
    Blockly.Msg.DELETE_ALL_BLOCKS = "Pašalinti visus %1 blokus (-ų)?";
    Blockly.Msg.DELETE_BLOCK = 'Pašalinti bloką';
    Blockly.Msg.DELETE_X_BLOCKS = 'Pašalinti %1 blokus (-ų)';
    Blockly.Msg.COLLAPSE_BLOCK = 'Sutraukti bloką';
    Blockly.Msg.EXPAND_BLOCK = 'Išskleisti bloką';
    Blockly.Msg.DISABLE_BLOCK = 'Išjungti bloką';
    Blockly.Msg.ENABLE_BLOCK = 'Įgalinti bloką';
    Blockly.Msg.HELP = 'Pagalba';
    Blockly.Msg.EXPORT_IMAGE = 'Parsisiųsti blokus kaip paveikslą';
    Blockly.Msg.COLLAPSE_ALL = 'Sutraukti blokus';
    Blockly.Msg.EXPAND_ALL = 'Išskleisti blokus';
    Blockly.Msg.ARRANGE_H = 'Blokus išdėstyti horizontaliai';
    Blockly.Msg.ARRANGE_V = 'Blokus išdėstyti vertikaliai';
    Blockly.Msg.ARRANGE_S = 'Blokus išdėstyti įstrižai';
    Blockly.Msg.SORT_W = 'Rikiuoti blokus pagal plotį';
    Blockly.Msg.SORT_H = 'Rikiuoti blokus pagal aukštį';
    Blockly.Msg.SORT_C = 'Rikiuoti blokus pagal kategoriją';
    Blockly.Msg.COPY_TO_BACKPACK = 'Dėti į kuprinę';
    Blockly.Msg.COPY_ALLBLOCKS = 'Visus blokus nukopijuoti į kuprinę';
    Blockly.Msg.REMOVE_FROM_BACKPACK = 'Pašalinti iš kuprinės';
    Blockly.Msg.BACKPACK_GET = 'Visus blokelius įklijuoti iš kuprinės';
    Blockly.Msg.BACKPACK_EMPTY = 'Ištuštinti kuprinę';
    Blockly.Msg.BACKPACK_CONFIRM_EMPTY = 'Ar tikrai norite ištuštinti kuprinę?';
    Blockly.Msg.BACKPACK_DOC_TITLE = "Kuprinės informacija";
    Blockly.Msg.SHOW_BACKPACK_DOCUMENTATION = "Rodyti kuprinės dokumentaciją";
    Blockly.Msg.BACKPACK_DOCUMENTATION = "Kuprinė yra kopijavimo / įklijavimo funkcija. Tai leidžia kopijuoti blokus iš vieno projekto ar ekrano ir įklijuoti juos į kitą projektą ar ekraną."
        + " Norėdami nukopijuoti, galite nuvilkti blokus į kuprinę. Norėdami įklijuoti, spustelėkite ant kuprinės piktogramos ir nuvilkite blokus į darbo vietą." +
   "</p><p>Jei atsijungsite iš „App Inventor“ su kuprinėje paliktais blokais, jie bus ten, kai kitą kartą prisijungsite." +
   "</p><p><a href='/reference/other/backpack.html' target='_blank'>Spustelėkite čia</a> dėl detalesnės informacijos.";
    Blockly.Msg.ENABLE_GRID = 'Įgalinti darbo vietos tinklelį';
    Blockly.Msg.DISABLE_GRID = 'Išjungti darbo vietos tinklelį';
    Blockly.Msg.ENABLE_SNAPPING = 'Įgalinti pritraukimą į tinklelį';
    Blockly.Msg.DISABLE_SNAPPING = 'Išjungti pritraukimą į tinklelį';
    Blockly.Msg.DISABLE_ALL_BLOCKS = 'Išjungti visus blokus';
    Blockly.Msg.ENABLE_ALL_BLOCKS = 'Įgalinti visus blokus';
    Blockly.Msg.HIDE_ALL_COMMENTS = 'Slėpti visus komentarus';
    Blockly.Msg.SHOW_ALL_COMMENTS = 'Rodyti visus komentarus';
    Blockly.Msg.GENERICIZE_BLOCK = 'Padaryti bendriniu';
    Blockly.Msg.UNGENERICIZE_BLOCK = 'Padaryti specifiniu';
    Blockly.Msg.DOWNLOAD_BLOCKS_AS_PNG = 'Parsisiųsti blokus kaip PNG';

// Variable renaming.
    Blockly.Msg.CHANGE_VALUE_TITLE = 'Keisti reikšmę:';
    Blockly.Msg.NEW_VARIABLE = 'Naujas kintamasis...';
    Blockly.Msg.NEW_VARIABLE_TITLE = 'Naujas kintamojo pavadinimas:';
    Blockly.Msg.RENAME_VARIABLE = 'Pervadinti kintamąjį...';
    Blockly.Msg.RENAME_VARIABLE_TITLE = 'Pervadinti visus „%1“ kintamuosius į:';

// Toolbox.
    Blockly.Msg.VARIABLE_CATEGORY = 'Kintamieji';
    Blockly.Msg.PROCEDURE_CATEGORY = 'Procedūros';

// Warnings/Errors
    Blockly.Msg.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = "Šis blokas negali būti apibrėžtas";
    Blockly.Msg.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = "Išskleidžiamajame meniu pasirinkite tinkamą elementą.";
    Blockly.Msg.ERROR_DUPLICATE_EVENT_HANDLER = "Tai yra šio komponento įvykių tvarkyklės kopija.";
    Blockly.Msg.ERROR_COMPONENT_DOES_NOT_EXIST = "Komponentas neegzistuoja";
    Blockly.Msg.ERROR_BLOCK_IS_NOT_DEFINED = "Šis blokas nėra apibrėžtas. Ištrinkite šį bloką!";
    Blockly.Msg.ERROR_BREAK_ONLY_IN_LOOP = "Lūžio blokas turėtų būti naudojamas tik cikluose";

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = '/reference/blocks/colors.html#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = 'Spustelėkite kvadratą, kad pasirinktumėte spalvą.';
    Blockly.Msg.LANG_COLOUR_BLACK = 'juoda';
    Blockly.Msg.LANG_COLOUR_WHITE = 'balta';
    Blockly.Msg.LANG_COLOUR_RED = 'raudona';
    Blockly.Msg.LANG_COLOUR_PINK = 'rožinė';
    Blockly.Msg.LANG_COLOUR_ORANGE = 'oranžinė';
    Blockly.Msg.LANG_COLOUR_YELLOW = 'geltona';
    Blockly.Msg.LANG_COLOUR_GREEN = 'žalia';
    Blockly.Msg.LANG_COLOUR_CYAN = 'ciano';
    Blockly.Msg.LANG_COLOUR_BLUE = 'mėlyna';
    Blockly.Msg.LANG_COLOUR_MAGENTA = 'magenta';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = 'šviesiai pilka';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = 'tamsiai pilka';
    Blockly.Msg.LANG_COLOUR_GRAY = 'pilka';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = 'padalinti spalvą';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = '/reference/blocks/colors.html#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = "Keturių elementų sąrašas (reikšmės nuo 0 iki 255), nurodantis raudoną, žalią, mėlyną ir alfa komponentus.";
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = 'sukurti spalvą';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = '/reference/blocks/colors.html#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = "Spalva su nurodytais raudonos, žalios, mėlynos ir pasirinktinai alfa komponentais";

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = 'Valdymas';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = '/reference/blocks/control.html#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = 'Jei reikšmė teisinga, tai atliekami keli sakiniai';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = 'Jei reikšmė teisinga, tai atliekamas pirmas sakinių blokas. Priešingu atveju, atliekamas antras sakinių blokas.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = 'Jei pirma reikšmė teisinga, tai atliekamas pirmas sakinių blokas. Priešingu atveju, jei antroji reikšmė yra teisinga, tai atliekamas antras sakinių blokas.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = 'Jei pirma reikšmė teisinga, tai atliekamas pirmas sakinių blokas. Priešingu atveju, jei antroji reikšmė yra teisinga, tai atliekamas antras sakinių blokas.\n' +
        'Jei nė viena iš reikšmių nėra teisinga, atliekamas paskutinis sakinių blokas.';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = 'jei';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = 'priešingu atveju jei';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = 'priešingu atveju';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = 'tai';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = 'jei';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = 'Pridėti, pašalinti arba perrikiuoti skyrius norint iš naujo sukonfigūruoti šį bloką.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'priešingu atveju jei';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Pridėti sąlygas į bloką „Jei“.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'priešingu atveju';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Prdėti galutinę sąlygą į bloką „Jei“.';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = '/reference/blocks/control.html#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'kartoti';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'Kol';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'Tol, kol';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Kol reikšmė teisinga, atliekama keleta sakinių.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Tol, kol reikšmė teisinga, atliekami keli sakiniai.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = 'Veikia blokus, esančius skyriuje „atlikti“, kol testas yra teisingas.';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = 'skaičiuok su';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = 'nuo';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = 'iki';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = 'atlikti';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = 'Suskaičiuoja nuo pradžios iki pabaigos.\n' +
        'Kiekvienam skaičiui nustato dabartinį skaičiaus numerį į\n' +
        'kintamojo „%1“, ir tada atlieka kelis sakinius.';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = '/reference/blocks/control.html#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'kiekvienam';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'skaičiui';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = 'nuo';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = 'iki';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'kas kiek';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = 'skaičiui intervale';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = 'dėl ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' intervale';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Veikia blokus, esančius skyriuje „atlikti“, ir kiekvienai skaitinei reikšmei '
        + '(intervale nuo pradžios iki pabaigos) kiekvieną kartą padidina reikšmę. Panaudoja duotą '
        + 'kintamojo pavadinimą, kuris nurodo esamą reikšmę.';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = '/reference/blocks/control.html#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'kiekvienam';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = 'elementui';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'sąraše';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = 'elementui sąraše';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = 'dėl ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' sąraše';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = 'Veikia blokus, esančius skyriuje „atlikti“, kiekvienam elementui sąraše. '
        + ' Norėdami nurodyti dabartinį sąrašo elementą, naudokite duotą kintamojo pavadinimą.';

    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_HELPURL = '/reference/blocks/control.html#foreachdict';
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT = 'kiekvienam %1 su %2 žodyne %3';
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_KEY = 'raktas';
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_INPUT_VALUE = 'reikšmė';
    // Used by the typeblock system.
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_TITLE = 'kiekvienam žodyne';
    Blockly.Msg.LANG_CONTROLS_FOREACH_DICT_TOOLTIP = 'Veikia blokus, esančius „atlikti“, kiekvienam rakto reikšmės įrašui žodyne.'
        + ' Nurodykite dabartinio žodyno elemento raktą / reikšmę naudodami duotus kintamųjų pavadinimus.';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = '/reference/blocks/control.html#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'ciklo';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'nutraukti';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'tęsti su kita iteracija';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Nutraukia besitęsiantį ciklą.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Praleidžia likusią šio ciklo dalį ir \n' +
        'tęsia su kita iteracija.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Įspėjimas:\n' +
        'Šis blokas gali būti naudojamas tik cikle.';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = '/reference/blocks/control.html#while';
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = 'kol';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = 'testas';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = 'kol';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = 'Veikia blokus, esančius skyriuje „atlikti“, kol testas yra teisingas.';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = '/reference/blocks/control.html#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = 'jei';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'tai';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'priešingu atveju';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = 'jei';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = 'Jei tikrinama sąlyga yra tiesa, grąžinamas išraiškos, pridėtos prie sakinio „tai-grąžinti“, įvertinimo rezultatas.'
        + 'Kitu atveju, grąžinamas išraiškos, pridėtos prie sakinio „priešingu atveju-gražinti“ įvertinimo rezultatas.'
        + 'Bus įvertinta viena iš grąžinimo sakinių išraiškų.';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = '/reference/blocks/control.html#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'atlikti';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = 'rezultatas';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = 'Veikia blokus, esančius „atlikti“, ir grąžina sakinį. Naudinga, jei prieš pradedant grąžinti reikšmę į kintamąjį reikia atlikti procedūrą.';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = 'atlikti/rezultatas';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = 'atlikti rezultatą';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'įvertinti, bet nepaisyti rezultato';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = '/reference/blocks/control.html#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'įvertinti, bet nepaisyti';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = 'Paleidžia prijungtą kodo bloką ir nepaiso grąžinimo reikšmės (jei yra). Naudinga, jei reikia iškviesti procedūrą su grąžinimo reikšme, bet jos nereikia.';

    /* [lyn, 10/14/13] Removed for now. May come back some day.
     Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = 'nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = '/reference/blocks/control.html#nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = 'Returns nothing. Used to initialize variables or can be plugged into a return socket if no value needed to return. this is equivalent to null or None.';
     */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = '/reference/blocks/control.html#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'atveria kitą ekraną';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'ekranoPavadinimas';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = 'atverti ekraną';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Kelių ekranų programoje atidaromas naujas ekranas.';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = '/reference/blocks/control.html#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'atveria kitą ekraną su pradine reikšme';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'ekranoPavadinimas';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'pradinėReikšmė';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = 'atverti ekraną su reikšme';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Kelių ekranų programoje atidaromas naujas ekranas '
        + 'ir perduodama pradinė reikšmė į tą ekraną.';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = '/reference/blocks/control.html#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = 'gauti pradinę reikšmę';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'ekranoPavadinimas';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'pradinėReikšmė';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = 'gauti pradinę reikšmę';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Grąžina reikšmę, kuri buvo perduota šiam ekranui, kai jis buvo atidaromas kito ekrano kelių ekranų programoje. Jei reikšmė nebuvo perduota, grąžinamas tuščias tekstas.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = '/reference/blocks/control.html#closescreen';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = 'užverti ekraną';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = 'užverti ekraną';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Užveria dabartinį ekraną';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = '/reference/blocks/control.html#closescreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = 'uždaryti ekraną su reikšme';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'rezultatas';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = 'uždaryti ekraną su reikšme';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Uždaromas dabartinis ekranas ir grąžinamas rezultatas į ekraną, kuris jį atidarė.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = '/reference/blocks/control.html#closeapp';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = 'uždaryti programą';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = 'uždaryti programą';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Uždaromi visi šios programos ekranai ir programa sustabdoma.';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = '/reference/blocks/control.html#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = 'gauti grynąjį pradžios tekstą';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = 'gauti grynąjį pradžios tekstą';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'Grąžina grynąjį tekstą, kuris buvo perduotas šiam ekranui, kai jį pradėjo kita programa.'
        + 'Jei reikšmė nebuvo perduota, grąžinamas tuščias tekstas.'
        + 'Jei norite naudoti kelių ekranų programas, naudokite pradžios reikšmės gavimą, o ne grynojo pradžios teksto gavimą.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = '/reference/blocks/control.html#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = 'uždaryti ekraną su grynuoju tekstu';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = 'uždaryti ekraną su grynuoju tekstu';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Uždaromas dabartinis ekranas ir grąžinamas tekstas programai, kuri jį atidarė.'
        + 'Ši komanda skirta grąžinti tekstą ne „App Inventor“ veiklai, o ne „App Inventor“ ekranams.';

    Blockly.Msg.LANG_CONTROLS_BREAK_HELPURL = '/reference/blocks/control.html#break';
    Blockly.Msg.LANG_CONTROLS_BREAK_TITLE = 'lūžis';
    Blockly.Msg.LANG_CONTROLS_BREAK_INPUT_TEXT = 'reikšmė';
    Blockly.Msg.LANG_CONTROLS_BREAK_COLLAPSED_TEXT = 'lūžis';

// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = 'Logika';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = '/reference/blocks/logic.html#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = '/reference/blocks/logic.html#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Testuoja ar du dalykai yra lygūs. Lyginami dalykai gali būti bet kokie dalykai, ne tik skaičiai. \n' +
        'Skaičiai laikomi lygiais jų spausdintai formai kaip eilutės, pavyzdžiui, skaičius 0 yra lygus tekstui „0“.\n' +
        'Be to, dvi eilutės, kurios žymi skaičius, yra lygios, jei skaičiai yra lygūs, pavyzdžiui, „1“ yra lygus „01“.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Grąžina reikšmę „Tiesa“, jei abi įvestys nėra lygios viena kitai.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = 'loginė lygybė';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = '/reference/blocks/logic.html#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = '/reference/blocks/logic.html#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = 'ir';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = 'arba';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Grąžina reikšmę „Tiesa“, jei visos įvestys yra teisingos.';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Grąžina reikšmę „Tiesa“, jei kuri nors įvestis yra teisinga.';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = '/reference/blocks/logic.html#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = 'ne';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = 'Grąžina reikšmę „Tiesa“, jei įvestis yra neteisinga.\n' +
        'Grąžina reikšmę „Netiesa“, jei įvestis yra teisinga.';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = '/reference/blocks/logic.html#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = '/reference/blocks/logic.html#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'tiesa';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'netiesa';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Grąžina loginę reikšmę „Tiesa“.';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Grąžina loginę reikšmę „Netiesa“.';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = 'Matematika';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = '/reference/blocks/math.html#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = 'Informuoja apie rodomą skaičių.';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'skaičius';

    Blockly.Msg.LANG_MATH_DECIMAL_FORMAT = 'dešimtainis';
    Blockly.Msg.LANG_MATH_BINARY_FORMAT = 'dvejetainis';
    Blockly.Msg.LANG_MATH_OCTAL_FORMAT = 'aštuntainis';
    Blockly.Msg.LANG_MATH_HEXADECIMAL_FORMAT = 'šešioliktainis';
    Blockly.Msg.LANG_MATH_NUMBER_RADIX_HELPURL = '/reference/blocks/math.html#number-radix';
    Blockly.Msg.LANG_MATH_NUMBER_RADIX_TOOLTIP = 'Pateikite skaičių dešimtainiu formatu.';
    Blockly.Msg.LANG_MATH_NUMBER_RADIX_TITLE = 'skaičiaus šaknis';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = '/reference/blocks/math.html#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = '/reference/blocks/math.html#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = '/reference/blocks/math.html#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = '/reference/blocks/math.html#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = '/reference/blocks/math.html#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = '/reference/blocks/math.html#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Grąžina reikšmę „Tiesa“, jei abu skaičiai yra lygūs vienas kitam.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Grąžina reikšmę „Tiesa“, jei abu skaičiai nėra lygūs vienas kitam.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = 'Grąžina reikšmę „Tiesa“, jei pirmas skaičius yra mažesnis už antrą.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Grąžina reikšmę „Tiesa“, jei pirmas skaičius yra mažesnis arba lygus antram skaičiui.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = 'Grąžina reikšmę „Tiesa“, jei pirmas skaičius yra didesnis už antrą.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Grąžina reikšmę „Tiesa“, jei pirmas skaičius yra didesnis arba lygus antram skaičiui.';
    Blockly.Msg.LANG_MATH_COMPARE_EQ = '=';
    Blockly.Msg.LANG_MATH_COMPARE_NEQ = '\u2260';
    Blockly.Msg.LANG_MATH_COMPARE_LT = '<';
    Blockly.Msg.LANG_MATH_COMPARE_LTE = '\u2264';
    Blockly.Msg.LANG_MATH_COMPARE_GT = '>';
    Blockly.Msg.LANG_MATH_COMPARE_GTE = '\u2265';

    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_ADD = '/reference/blocks/math.html#add';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MINUS = '/reference/blocks/math.html#subtract';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_MULTIPLY = '/reference/blocks/math.html#multiply';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_DIVIDE = '/reference/blocks/math.html#divide';
    Blockly.Msg.LANG_MATH_ARITHMETIC_HELPURL_POWER = '/reference/blocks/math.html#exponent';
    Blockly.Msg.LANG_MATH_BITWISE_HELPURL_AND = '/reference/blocks/math.html#bitwise_and';
    Blockly.Msg.LANG_MATH_BITWISE_HELPURL_IOR = '/reference/blocks/math.html#bitwise_ior';
    Blockly.Msg.LANG_MATH_BITWISE_HELPURL_XOR = '/reference/blocks/math.html#bitwise_xor';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Grąžina dviejų skaičių sumą.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Grąžina dviejų skaičių skirtumą.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Grąžina dviejų skaičių sandaugą.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Grąžina dviejų skaičių dalmenį.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Grąžina reikšmę, kai pirmas skaičius pakeltas antro skaičiaus laipniu.';
    Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_AND = 'Grąžina dviejų skaičių bitinį IR.';
    Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_IOR = 'Grąžina dviejų skaičių bitinę neišskirtinę disjunkciją (ARBA).';
    Blockly.Msg.LANG_MATH_BITWISE_TOOLTIP_XOR = 'Grąžina dviejų skaičių bitinę griežtąją disjunkciją (XOR).';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    Blockly.Msg.LANG_MATH_BITWISE_AND = 'bitinis IR';
    Blockly.Msg.LANG_MATH_BITWISE_IOR = 'bitinis ARBA';
    Blockly.Msg.LANG_MATH_BITWISE_XOR = 'bitinis XOR';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = 'change';
     Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = 'item';
     Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = 'by';
     Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = 'Add a number to variable "%1".';*/


    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = 'kvadratinė šaknis';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = 'absoliutus';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = 'neigimas';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'log';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Grąžina skaičiaus kvadratinę šaknį.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = '/reference/blocks/math.html#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Grąžina absoliučią skaičiaus reikšmę.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = '/reference/blocks/math.html#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Grąžina skaičiaus neigimą.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = '/reference/blocks/math.html#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = 'Grąžina natūralų logaritmą, t.y. logaritmą, kurio pagrindas e (2.71828 ...).';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN ='/reference/blocks/math.html#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Grąžina e (2.71828...) kaip skaičiaus laipsnį.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP ='/reference/blocks/math.html#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Return 10 to the power of a number.';*/

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Suapvalina įvestį iki artimiausio sveikojo skaičiaus. ' +
        'Reikšmės, kurių dešimtainė trupmena yra 0,5, suapvalinamos iki artimiausio sveikojo skaičiaus.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = '/reference/blocks/math.html#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Suapvalina įvestį iki mažiausio skaičiaus ne mažesnio nei įvestis.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING =  '/reference/blocks/math.html#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Suapvalina įvestį iki didžiausio skaičiaus, bet ne didesnio už įvestį.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR =  '/reference/blocks/math.html#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = 'apvalinimas';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = 'apvalinimas į mažesnę pusę';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = 'apvalinimas į didesnę pusę';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sin';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asin';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = 'Pateikia nurodyto kampo sinusą laipsniais.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = '/reference/blocks/math.html#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = 'Pateikia nurodyto kampo kosinusą laipsniais.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = '/reference/blocks/math.html#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = 'Pateikia nurodyto kampo tangentą laipsniais.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = '/reference/blocks/math.html#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Pateikia kampo laipsnius intervale (-90, + 90) su nurodyta sinuso reikšme.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = '/reference/blocks/math.html#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Pateikia kampo laipsnius intervale [0, 180) su nurodyta kosinuso reikšme.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = '/reference/blocks/math.html#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Pateikia kampo laipsnius intervale (-90, +90) su nurodyta tangento reikšme.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = '/reference/blocks/math.html#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Pateikia kampo laipsnius intervale (-180, +180] su nurodytomis koordinatėmis.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = '/reference/blocks/math.html#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = 'mažiausias';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = 'didžiausias';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Grąžina mažiausią iš argumentų.';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Grąžina didžiausią iš argumentų.';
    Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#min';
    Blockly.Msg.LANG_MATH_ONLIST_HELPURL_MAX = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#max';

    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'modulis';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'liekana';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'dalmuo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Grąžina modulį.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = '/reference/blocks/math.html#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Grąžina liekaną.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = '/reference/blocks/math.html#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Grąžina dalmenį.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = '/reference/blocks/math.html#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = '/reference/blocks/math.html#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'atsitiktinis sveikasis skaičius';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = 'nuo';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = 'iki';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = 'atsitiktinis sveikasis skaičius nuo %1 iki %2';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = 'Grąžina atsitiktinį sveikąjį skaičių tarp viršutinio ir apatinio rėžio. Ribos bus sumažintos, kad būtų mažiau nei 2**30.';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = '/reference/blocks/math.html#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'atsitiktinė trupmena';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Grąžina atsitiktinį skaičių nuo 0 iki 1.';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = '/reference/blocks/math.html#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'atsitiktinai nustatyta sėkla';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = 'į';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = 'nurodo skaitinę sėklą atsitiktinių skaičių generatoriui';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'konvertuoti';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'radianus į laipsnius';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'laipsnius į radianus';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Grąžina laipsnio reikšmę (intervale [0, 360)) atitinkančią jo radianų argumentą.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = '/reference/blocks/math.html#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Grąžina radiano reikšmę (intervale [-\u03C0, +\u03C0)) atitinkančią jos laipsnių argumentą.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = '/reference/blocks/math.html#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = '/reference/blocks/math.html#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'formatuoti kaip dešimtainį';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'skaičius';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'skiltys';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = 'formatuoti kaip dešimtainį %1 skiltys %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Grąžina skaičių, suformatuotą kaip dešimtainį, su nurodytu skilčių skaičiumi.';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = '/reference/blocks/math.html#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = 'yra skaičius?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Testuoja ar tai yra skaičius.';

    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = 'yra dešimtainis?';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = 'Tikrina, ar kažkas yra eilutė, kuri reiškia teigiamą dešimtainį skaičių.';

    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = '/reference/blocks/math.html#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = 'yra šešioliktainis?';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = 'Tikrina, ar kažkas yra eilutė, kuri reiškia šešioliktainį skaičių.';

    Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = '/reference/blocks/math.html#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = 'yra dvejetainis?';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = 'Tikrina, ar kažkas yra eilutė, kuri reiškia dvejetainį skaičių.';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = 'konvertuoti skaičių';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = 'dešimtainį į šešioliktainį';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertnumber';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = 'Paima teigiamą sveikąjį dešimtainį skaičių ir grąžina eilutę, kuri žymi šešioliktainį skaičių.';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = 'šešioliktainį į dešimtainį';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertnumber';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = 'Paima eilutę, kuri žymi šešioliktainį skaičių, ir grąžina eilutę, kuri nurodo dešimtainį skaičių.';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = 'dešimtainį į dvejetainį';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertnumber';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = 'Paima teigiamą sveikąjį dešimtainį skaičių ir grąžina eilutę, kuri žymi dvejetainį skaičių.';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = 'dvejetainį į dešimtainį';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertnumber';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = 'Paima eilutę, kuri žymi dvejetainį skaičių, ir grąžina eilutę, kuri nurodo dešimtainį skaičių.';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = 'Tekstas';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = '/reference/blocks/text.html#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = 'Teksto eilutė.';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = '/reference/blocks/text.html#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'kurti tekstą su';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = 'Prideda visas įvestis ir sudaro vieną teksto eilutę. Jei nėra įvesčių, sukuria tuščią tekstą.';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = 'prijungti';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'eilutė';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = 'į';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = 'pridėti tekstą';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = 'elementas';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = 'Prideda tekstą prie kintamojo „%1“.';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = '/reference/blocks/text.html#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = 'ilgis';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = 'Grąžinamas pateiktame tekste esantis raidžių (įskaitant tarpus) skaičius.';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = '/reference/blocks/text.html#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'yra tuščias';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = 'Grąžina reikšmę „Tiesa“, jei teksto ilgis yra 0, kitaip - klaidinga.';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_NEQ = ' ≠';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = '/reference/blocks/text.html#compare';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = 'palyginti tekstus';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = 'Tikrina ar tekstas1 yra leksikografiškai mažesnis nei tekstas2.\n'
        + 'Jei vienas tekstas yra kito priešdėlis, trumpesnis tekstas laikomas mažesniu. Didžiosios raidės rašomos prieš mažąsias raides.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = 'Testuoja, ar teksto eilutės yra tapačios, t.y., turi tuos pačius simbolius ta pačia tvarka.\n'
        + 'Skiriasi nuo įprasto „=“ tuo atveju, kai teksto eilutės yra skaitinės: 123 ir 0123 yra lygios, bet ne teksto atžvilgiu.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_NEQ = 'Testuoja, ar teksto eilutės yra skirtingos, t.y., neturi tų pačių simbolių ta pačia tvarka.\n'
        + 'Skiriasi nuo įprasto „≠“ tuo atveju, kai teksto eilutės yra skaitinės: 123 ir 0123 yra nelygios teksto atžvilgiu, bet matematiškai lygios.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = 'Tikrina ar tekstas1 yra leksikografiškai didesnis nei tekstas2.\n'
        + 'Jei vienas tekstas yra kito priešdėlis, trumpesnis tekstas laikomas mažesniu.\n'
        + 'Didžiosios raidės rašomos prieš mažąsias raides.';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_TOOLTIP = "Kuria tekstą, kaip teksto bloką. Skirtumas tas, kad tekstas nėra lengvai aptinkamas nagrinėjant programos APK. \n"
        + "Naudokite tai kurdami programas, kad platintumėte konfidencialią informaciją, pavyzdžiui, API raktus. \n"
        + "Įspėjimas: tai užtikrina tik labai žemą apsaugą.";
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE = 'Neteisingas tekstas';
    Blockly.Msg.LANG_TEXT_TEXT_OBFUSCATE_HELPURL = '/reference/blocks/text.html#obfuscatetext';

    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = 'letters in text';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = 'Returns specified number of letters at the beginning or end of the text.';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'first';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'last';*/

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = 'find';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'occurrence of text';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'in text';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = 'Returns the index of the first/last occurrence\n' +
     'of first text in the second text.\n' +
     'Returns 0 if text is not found.';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'first';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'last';*/

    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = 'letter at';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = 'in text';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = 'Returns the letter at the specified position.';*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'didžiosios raidės';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'mažosios raidės';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Gauna teksto eilutės argumento, konvertuoto į didžiąsias raides, kopiją.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = '/reference/blocks/text.html#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Gauna teksto eilutės argumento, konvertuoto į mažąsias raides, kopiją.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = '/reference/blocks/text.html#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = '/reference/blocks/text.html#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = 'apkarpyti';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = 'Grąžina teksto eilutės argumentų kopiją su pašalintais tarpais priekyje ir gale.';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = '/reference/blocks/text.html#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'prasideda';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'dalis';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = 'prasideda tekste %1 dalyje %2';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = 'Grąžina tekste esančios dalies pradžios indeksą.\n'
        + 'Indeksas 1 žymi teksto pradžią. Grąžina 0, jei dalies nėra tekste.';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = '/reference/blocks/text.html#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'turi';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = 'dalis';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = 'turi tekstą %1 dalį %2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = 'Tikrina ar dalis yra tekste.';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = 'esantis';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = 'esantis (sąrašas)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'skaidyti iš pradžių';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'skaidyti iš pradžių bet kurį';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'skaidyti';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'skaidyti bet kurį';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = 'Pateiktą tekstą skaido į dvi dalis naudojant skaidymo tašką, ir grąžina dviejų elementų sąrašą, susidedantį iš dalių prieš ir po skaidymo taško. \n'
        + 'Tekste „obuolys,bananas,vyšnia,persikas“ skaidymo tašku laikant kablelį bus grąžinamas dviejų elementų sąrašas: pirmoje dalyje tekstas „obuolys“, antroje dalyje „bananas,vyšnia,persikas“. \n'
        + 'Pastaba, kad kablelio po „obuolys“ nebus, kadangi jis buvo skaidymo taškas.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = '/reference/blocks/text.html#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Pateiktą tekstą skaido į dvi dalis naudojant pirmąją bet kurio elemento vietą kaip skaidymo tašką. \n'
        + 'Skaidant pagal sąrašą „(ba,ob)“ tekstą „Man patinka obuolys ir bananas.“ bus grąžinamas \n'
        + 'dviejų elementų sąrašas: pirmoje dalyje bus tekstas „Man patinka“, antroje dalyje bus „uolys ir bananas.“';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = '/reference/blocks/text.html#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Skaido tekstą į dalis, naudodamas skaidymo tašką nurodytą „esantis“ ir pateikia rezultatų sąrašą. \n'
        + 'Skaidant tekstą „vienas,du,trys,keturi“ ir skaidymo tašku naudojant kablelį bus grąžinamas sąrašas „(vienas du trys keturi)“. \n'
        + 'Skaidant tekstą „vienas-bulvė,du-bulvė,trys-bulvė,keturi“ ir skaidymo tašku naudojant "-bulvė,", bus grąžinamas sąrašas „(vienas du trys keturi)“.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = '/reference/blocks/text.html#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Pateiktas tekstas skaidomas į sąrašą, naudojant bet kurį elementą kaip skaidymo tašką, ir grąžinamas rezultatų sąrašas. \n'
        + 'Skaidant „nesimokyti,mokytis,tingėti“ pagal sąrašą „(,moky)“ pirmas yra kablelis, antras yra „moky“, bus grąžinamas trijų elementų sąrašas: „(nesii tis tingėti)“.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = '/reference/blocks/text.html#splitatany';

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = 'print';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = 'Print the specified text, number or other value.';*/

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'prompt for';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'with message';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = 'Prompt for user input with the specified text.';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = 'text';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = 'number';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = '/reference/blocks/text.html#splitspaces';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'skaidyti tarpais';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Skaido tekstą į dalis ir atskiria tarpais.';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = '/reference/blocks/text.html#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'segmentas';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = 'pradžia';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'ilgis';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = 'segmento tekstas %1 pradžia %2 ilgis %3';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Iš duoto teksto ištraukia nurodyto ilgio segmentą pradedant nuo pateikto teksto, pradedant nuo nurodytos pozicijos.\n'
        + 'Pozicija 1 žymi teksto pradžią.';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = '/reference/blocks/text.html#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'segmentas';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'pakeisti visus';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'pakeitimas';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = 'pakeisti visą tekstą %1 segmentu %2 pakeičiant %3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Grąžina naują tekstą, gautą pakeičiant segmentus.';

    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_HELPURL = '/reference/blocks/text.html#isstring';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TITLE = 'yra eilutė?';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_INPUT_THING = 'dalykas';
    Blockly.Msg.LANG_TEXT_TEXT_IS_STRING_TOOLTIP = 'Grąžina reikšmę „Tiesa“, jei <code>dalykas</code> yra eilutė.';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_HELPURL = '';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_TEXT = 'tekste';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER_PREFIX = 'teikiant pirmenybę';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_INPUT_ORDER = 'tvarkai';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_LONGEST_STRING_FIRST = 'pirmiausia ilgiausia eilutė';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_OPERATOR_DICTIONARY_ORDER = 'žodynas';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TITLE = 'pakeisti visus atvaizdavimus';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_HELPURL_LONGEST_STRING_FIRST = '/reference/blocks/text.html#replaceallmappingslongeststring';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TOOLTIP_LONGEST_STRING_FIRST = 'Pateikus atvaizdavimo žodyną kaip įvestį, pagrindiniai teksto įrašai n\'' +
        + 'pakeičiami atitinkamomis žodyno reikšmėmis. Grąžina tekstą su pritaikytais atvaizdavimais.\n'
        + 'Jei pasirenkamas vienas ar kitas raktas, pirmiausia pakeičiamas ilgiausias raktas.';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_HELPURL_DICTIONARY_ORDER = '/reference/blocks/text.html#replaceallmappingsdictionary';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_MAPPINGS_TOOLTIP_DICTIONARY_ORDER = 'Grąžina naują tekstą, kuris gaunamas pakeičiant visus atvejus apibrėžtais įvesties žodyno raktais su atitinkamomis raktų reikšmėmis. \n'
        + 'Jei reikia pasirinkti pakeisti vieną ar kitą raktą, elementas, kuris pirmas atsiranda žodyne, yra pakeičiamas pirmas.';

    Blockly.Msg.LANG_TEXT_REVERSE_HELPURL = '/reference/blocks/text.html#reverse';
    Blockly.Msg.LANG_TEXT_REVERSE_INPUT = 'atvirkščiai';
    Blockly.Msg.LANG_TEXT_REVERSE_TOOLTIP = 'Duotą tekstą išveda atvirkščia tvarka.';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = 'Sąrašai';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list.html#Empty_lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = 'sukurti tuščią sąrašą';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Returns a list, of length 0, containing no data records';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = '/reference/blocks/lists.html#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'sukurti sąrašą';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Sukuria sąrašą su bet kokiu elementų skaičiumi.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'sąrašas';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Prideda, pašalina arba pertvarko skyrius, kad iš naujo sukonfigūruotų šį sąrašo bloką.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'elementas';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Prideda elementą į sąrašą.';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = 'elementas';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = 'Prideda elementą į sąrašą.';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = '/reference/blocks/lists.html#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = '/reference/blocks/lists.html#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'pasirinkti sąrašo elementą';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'indeksas';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = 'pasirinkti sąrašo elementą: sąrašas %1 indeksas %2';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Grąžina elementą, kuris yra sąraše, tam tikroje pozicijoje, kurią nurodo indeksas.';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = '/reference/blocks/lists.html#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = 'yra sąraše?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = 'Dalykas';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = 'Sąrašas';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = 'yra sąraše? Dalykas %1 sąrašas %2';
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = 'Grąžina „Tiesa“, jei dalykas yra sąrašo elementas, priešingu atveju grąžina „Netiesa“.';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = '/reference/blocks/lists.html#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'indeksas sąraše';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = 'dalykas';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = 'indeksas sąraše: dalykas %1 sąrašas %2';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = 'Suranda dalyko poziciją sąraše. Jei jo nėra sąraše grąžina 0.';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = '/reference/blocks/lists.html#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'parinkti atsitiktinį elementą';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Iš sąrašo atsitiktinai parinkti elementą.';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = '/reference/blocks/lists.html#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'pakeisti sąrašo elementą';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'indeksas';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'pakeitimas';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = 'pakeisti sąrašo elementą:  sąrašas %1 indeksas %2 pakeitimas %3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Pakeičia n-tąjį sąrašo elementą.';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = '/reference/blocks/lists.html#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'pašalinti sąrašo elementą';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'indeksas';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = 'pašalinti sąrašo elementą: sąrašas %1 indeksas %2';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Iš sąrašo pašalinamas nurodytoje vietoje esantis elementas.';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = 'create list with item';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = 'repeated';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = 'times';
     Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = 'Creates a list consisting of the given value\n' +
     'repeated the specified number of times.';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = '/reference/blocks/lists.html#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = 'sąrašo ilgis';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = 'sąrašo ilgis: sąrašas %1';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = 'Suskaičiuoja sąrašo elementų skaičių.';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = '/reference/blocks/lists.html#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'pridėti prie sąrašo';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'sąrašas1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'sąrašas2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = 'pridėti prie sąrašo  sąrašas1 %1 sąrašas2 %2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Prideda visus elementus esančius sąrašas2 į sąrašas1 pabaigą. '
        + 'Po pridėjimo, sąrašė sąrašas1 bus papildomi elementai, tačiau sąrašas2 nebus keičiamas.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = '/reference/blocks/lists.html#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'įtraukti elementus į sąrašą';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = ' sąrašas';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'elementas';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = 'įtraukti elementus į sąrašą: sąrašas %1 elementas %2';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Įtraukia elementus į sąrašo pabaigą.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = 'sąrašas';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = 'Įtraukti, pašalinti ar perrikiuoti skyrius siekiant pertvarkyti šį sąrašo bloką.';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = '/reference/blocks/lists.html#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = 'kopijuoti sąrašą';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = 'Padaro sąrašo kopiją.';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = '/reference/blocks/lists.html#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = 'yra sąrašas?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = 'dalykas';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = 'Tikrinti ar tai yra sąrašas.';

    Blockly.Msg.LANG_LISTS_REVERSE_HELPURL = '/reference/blocks/lists.html#reverse';
    Blockly.Msg.LANG_LISTS_REVERSE_TITLE_REVERSE = 'perrikiuoti sąrašą';
    Blockly.Msg.LANG_LISTS_REVERSE_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_REVERSE_TOOLTIP = 'Pakeičia įvesties sąrašo tvarką ir grąžina kaip naują sąrašą';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = '/reference/blocks/lists.html#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'sąrašą į CSV eilutę';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Sąrašą interpretuoja kaip lentelės eilutę ir grąžina CSV (kableliais atskirtos reikšmės) tekstą atvaizduojantį eilutes. '
        + 'Kiekvienas eilučių sąrašo elementas laikomas lauku, o gautame CSV tekste cituojamas dvigubomis kabutėmis.'
        + 'Elementai yra atskirti kableliais. Grąžintos eilutės teksto pabaigoje nėra eilučių skirtuko.';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = '/reference/blocks/lists.html#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'sąrašas iš CSV eilutės';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Analizuoja tekstą kaip CSV (kableliais atskirta reikšmė) suformatuota eilutė, kad būtų pateiktas laukų sąrašas. '
        + ' Klaida, kai eilutės tekste yra neištrintos naujos eilutės (kelios eilutės). '
        + ' Eilutės tekstas turi pasibaigti viena nauja eilute.';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = '/reference/blocks/lists.html#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'sąrašas į CSV lentelę';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interpretuoja sąrašą kaip lentelę su didžiausių eilučių formatu ir grąžina CSV (kableliais atskirtos reikšmės) tekstą atvaizduojančia lentelę. '
        + ' Kiekvienas elementas sąraše pats turėtų būti CSV lentelės eilutę atvaizduojantis sąrašas. '
        + ' Kiekvienas elementas eilučių sąraše laikomas lauku ir pateiktame CSV tekste cituojamas dvigubomis kabutėmis. '
        + ' Grąžintame tekste eilutės elementai yra atskiriami kableliais, o eilutės yra atskiriamos naujomis eilutėmis.';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = '/reference/blocks/lists.html#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'sąrašas iš CSV lentelės';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'tekstas';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Analizuoja tekstą kaip CSV (kableliais atskirta reikšmė) suformatuota lentelę, kad būtų pateiktas eilučių sąrašas, iš kurių kiekvienas yra laukų sąrašas.'
        + 'Eilutes galima atskirti naujomis eilutėmis.';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = '/reference/blocks/lists.html#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'įterpti sąrašo elementą';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = 'indeksas';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = 'elementas';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = 'įterpti sąrašo elementą: sąrašas %1 indeksas %2 elementas %3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = 'Įterpia elementą į sąrašą nurodytoje vietoje.';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = '/reference/blocks/lists.html#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = 'sąrašas tuščias?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = 'Grąžina reikšmę „Tiesa“, jei sąrašas tuščias.';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = '/reference/blocks/lists.html#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = 'ieškoti porose';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = 'raktas';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = 'poros';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = 'nerasta';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = 'ieškoti porose: raktas %1 poros %2 nerasta %3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = 'Grąžina reikšmę, susietą su raktu porų sąraše.';

    // Join With Separator block
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_HELPURL = '/reference/blocks/lists.html#joinwithseparator';
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_TITLE = 'sujungti su skirtuku';
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_SEPARATOR = 'skirtukas';
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_LIST = 'sąrašas';
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_INPUT = 'sujungti elementus naudojant skirtuką %1 sąrašas %2';
    Blockly.Msg.LANG_LISTS_JOIN_WITH_SEPARATOR_TOOLTIP = 'Grąžina tekstą su sąrašo elementais, sujungtais su skirtuku.';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = 'find';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'occurrence of item';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'in list';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = 'Returns the index of the first/last occurrence\n' +
     'of the item in the list.\n' +
     'Returns 0 if text is not found.';
     Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = 'first';
     Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = 'last';

     Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = 'get item at';
     Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'in list';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = 'Returns the value at the specified position in a list.';

     Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = 'set item at';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'in list';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = 'to';
     Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = 'Sets the value at the specified position in a list.';*/

    // Dictionaries Blocks
    Blockly.Msg.LANG_CATEGORY_DICTIONARIES = 'Žodynai';
    Blockly.Msg.LANG_DICTIONARIES_CREATE_EMPTY_TITLE = 'sukurti tuščią žodyną';
    Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_EMPTY_HELPURL = '/reference/blocks/dictionaries.html#create-empty-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TITLE = 'sukurti žodyną';
    Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_TOOLTIP = 'Sukuriamas žodynas.';
    Blockly.Msg.LANG_DICTIONARIES_MAKE_DICTIONARY_HELPURL = '/reference/blocks/dictionaries.html#make-a-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_CONTAINER_TITLE_ADD = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_CREATE_WITH_CONTAINER_TOOLTIP = 'Įtraukti, pašalinti ar perrikiuoti skyrius siekiant pertvarkyti šį žodyno bloką.';

    Blockly.Msg.LANG_DICTIONARIES_PAIR_TITLE = 'pora';
    Blockly.Msg.LANG_DICTIONARIES_PAIR_TOOLTIP = 'Įtraukti porą į žodyną.';
    Blockly.Msg.LANG_DICTIONARIES_PAIR_HELPURL = '/reference/blocks/dictionaries.html#pair';

    Blockly.Msg.LANG_DICTIONARIES_PAIR_INPUT = 'raktas %1 reikšmė %2';
    Blockly.Msg.LANG_DICTIONARIES_PAIR_TOOLTIP = 'Sukuriama pora su pateiktu raktu ir reikšme.';
    Blockly.Msg.LANG_DICTIONARIES_MAKE_PAIR_TITLE = 'sukurti porą';

    Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_INPUT = 'nustatyti reikšmę raktui %1 esančiam žodyne %2 į %3';
    Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_TITLE = 'nusatyti porą';
    Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_TOOLTIP = 'Nustato porą žodyne.';
    Blockly.Msg.LANG_DICTIONARIES_SET_PAIR_HELPURL = '/reference/blocks/dictionaries.html#set-value-for-key';

    Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_INPUT = 'pašalinti įrašą raktui %2 iš žodyno %1';
    Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_TITLE = 'pašalinti raktui įrašą iš žodyno %1';
    Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_TOOLTIP = 'Pašalina porą žodyne, atsižvelgiant į jos raktą.';
    Blockly.Msg.LANG_DICTIONARIES_DELETE_PAIR_HELPURL = '/reference/blocks/dictionaries.html#delete-entry-for-key';

    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_INPUT = 'gauti reikšmę raktui %1 žodyne %2 arba jei nerastas %3';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_TOOLTIP = 'Grąžina reikšmę žodyne, susijusiame su raktu.';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_TITLE = 'pažiūrėti žodyną';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_LOOKUP_HELPURL = '/reference/blocks/dictionaries.html#get-value-for-key';

    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_INPUT = 'gauti reikšmę kelyje %1 žodyne %2 arba jei nerasta %3';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_TOOLTIP = 'Grąžina įdėto žodyno reikšmę.';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_TITLE = 'rekursinė paieška žodyne';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_LOOKUP_HELPURL = '/reference/blocks/dictionaries.html#get-value-at-key-path';

    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_INPUT = 'nustatyti reikšmę kelyje %1 žodyne %2 į %3';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_TOOLTIP = 'Nustato reikšmę medyje, pradedant nuo duoto žodyno.';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_TITLE = 'nustatyti reikšmę pagrindiniame žodyno kelyje';
    Blockly.Msg.LANG_DICTIONARIES_DICTIONARY_RECURSIVE_SET_HELPURL = '/reference/blocks/dictionaries.html#set-value-for-key-path';

    Blockly.Msg.LANG_DICTIONARIES_GETTERS_TITLE = 'gauti';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TITLE = 'raktai';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_INPUT = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TOOLTIP = 'Grąžina visų žodyno raktų sąrašą.';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_HELPURL = '/reference/blocks/dictionaries.html#get-keys';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TYPEBLOCK = 'gauna raktus';

    Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_TITLE = 'reikšmės';
    Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_INPUT = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_TOOLTIP = 'Grąžina visų žodyno reikšmių sąrašą.';
    Blockly.Msg.LANG_DICTIONARIES_GET_VALUES_HELPURL = '/reference/blocks/dictionaries.html#get-values';
    Blockly.Msg.LANG_DICTIONARIES_GET_KEYS_TYPEBLOCK = 'gauna raktus';

    Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_INPUT = 'yra raktas žodyne? raktas %1 žodynas %2';
    Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_TOOLTIP = 'Patikrina, ar raktas yra žodyne.';
    Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_TITLE = 'ar raktas yra žodyne?';
    Blockly.Msg.LANG_DICTIONARIES_IS_KEY_IN_HELPURL = '/reference/blocks/dictionaries.html#is-key-in-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_LENGTH_TITLE = 'žodyno dydis';
    Blockly.Msg.LANG_DICTIONARIES_LENGTH_INPUT = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_LENGTH_TOOLTIP = 'Grąžina reikšmingų porų skaičių žodyne.';
    Blockly.Msg.LANG_DICTIONARIES_LENGTH_HELPURL = '/reference/blocks/dictionaries.html#size-of-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_TITLE = 'porų sąrašas žodyne';
    Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_INPUT = 'poros';
    Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_TOOLTIP = 'Porų sąrašą paverčia žodynu.';
    Blockly.Msg.LANG_DICTIONARIES_ALIST_TO_DICT_HELPURL = '/reference/blocks/dictionaries.html#list-of-pairs-to-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_TITLE = 'žodynas į porų sąrašą';
    Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_INPUT = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_TOOLTIP = 'Žodyną paverčia porų sąrašu.';
    Blockly.Msg.LANG_DICTIONARIES_DICT_TO_ALIST_HELPURL = '/reference/blocks/dictionaries.html#dictionary-to-list-of-pairs';

    Blockly.Msg.LANG_DICTIONARIES_COPY_TITLE = 'kopijuoti žodyną';
    Blockly.Msg.LANG_DICTIONARIES_COPY_INPUT = 'žodynas';
    Blockly.Msg.LANG_DICTIONARIES_COPY_TOOLTIP = 'Grąžina žodyno paviršinę kopiją';
    Blockly.Msg.LANG_DICTIONARIES_COPY_HELPURL = '/reference/blocks/dictionaries.html#copy-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_INPUT = 'sujungti į žodyną %1 iš žodyno %2';
    Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_TOOLTIP = 'Nukopijuoja žodyno poras „Iš“ į žodyną „Į“.';
    Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_TITLE = 'sujungti žodynus';
    Blockly.Msg.LANG_DICTIONARIES_COMBINE_DICTS_HELPURL = '/reference/blocks/dictionaries.html#merge-into-dictionary';

    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_TITLE = 'sąrašas einant rakto keliu %1 žodyne arba sąraše %2';
    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_TOOLTIP = 'Pradeda nuo duoto žodyno ir seka jį ir jo raktus, atsižvelgiant į nurodytą kelią, grąžina rastų mazgų sąrašą.';
    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_HELPURL = '/reference/blocks/dictionaries.html#list-by-walking-key-path';

    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_TITLE = 'eiti visuose lygiuose';
    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_TOOLTIP = 'Naudojamas sąraše, einant rakto kelio bloku, tiria kiekvieną mazgą tam tikrame lygyje.';
    Blockly.Msg.LANG_DICTIONARIES_WALK_TREE_ALL_HELPURL = '/reference/blocks/dictionaries.html#walk-all-at-level';

    Blockly.Msg.LANG_DICTIONARIES_IS_DICT_TITLE = 'yra žodynas? %1';
    Blockly.Msg.LANG_DICTIONARIES_IS_DICT_TOOLTIP = 'Tikrina, ar kažkas yra žodynas.';
    Blockly.Msg.LANG_DICTIONARIES_IS_DICT_HELPURL = '/reference/blocks/dictionaries.html#is-a-dictionary';

// Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = '/reference/blocks/variables.html#global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'inicijuoti globalų';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'pavadinimas';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'į';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = 'globalūs';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Sukuria globalų kintamąjį ir suteikia jam pridedamų blokų reikšmę.';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_PREFIX = 'globalūs';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = '/reference/blocks/variables.html#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = 'gauti';
    /* Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = 'item'; */ // [lyn, 10/14/13] unused
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = 'gauti';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = 'Grąžina šio kintamojo reikšmę.';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = '/reference/blocks/variables.html#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = 'nustatyti';
    /* Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = 'item'; */ // [lyn, 10/14/13] unused
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = 'į';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = 'nustatyti';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = 'Nustato, kad šis kintamasis bus lygus įvesčiai.';
    Blockly.Msg.LANG_VARIABLES_VARIABLE = ' kintamasis';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = '/reference/blocks/variables.html#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'inicijuoti vietinį';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = 'pavadinimas';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'į';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'esantis';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = 'vietinis';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = 'Leidžia sukurti kintamuosius, kurie pasiekiami tik šio bloko dalyje „atlikti“.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = 'inicijuoja vietinį';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = '/reference/blocks/variables.html#return';
    /* // These don't differ between the statement and expression
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'initialize local';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'name';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'to';
     */
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'esantis';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = 'vietinis';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = 'Leidžia kurti kintamuosius, kurie pasiekiami tik šio bloko grįžtamojoje dalyje.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = 'inicijuoja vietinį grąžinime';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'vietiniai pavadinimai';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = 'pavadinimas';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = '/reference/blocks/procedures.html#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = 'į';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'procedūra';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = 'atlikti';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = 'į ';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Procedūra, kuri negrąžina reikšmės.';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = '/reference/blocks/procedures.html#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'rezultatas';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = 'atlikti';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = 'rezultatas';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = 'Veikia blokai, esantys „atlikti“, ir grąžina sakinį. Naudinga, jei prieš pradedant grąžinti reikšmę į kintamąjį reikia atlikti procedūrą.';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = 'atlikti/rezultatas';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = '/reference/blocks/procedures.html#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = 'į';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = 'rezultatas';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = 'į ';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Rezultato reikšmės grąžinimo procedūra.';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Įspėjimas:\n' +
        'Ši procedūra turi pasikartojančias įvestis.';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = '/reference/blocks/procedures.html#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = '/reference/blocks/procedures.html#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = 'iškviesti ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'procedūra';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = 'iškviesti ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Kviečia procedūrą be grąžinimo reikšmės.';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = 'iškviesti be grąžinimo';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = '/reference/blocks/procedures.html#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = 'iškviesti ';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Kviečia procedūrą su grąžinimo reikšme.';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = 'iškviesti su grąžinimu';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'įvestys';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = 'įvestis:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Paryškinti procedūrą';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.UNDEFINED_BLOCK_TOOLTIP = "Šis blokas nėra apibrėžtas. Ištrinkite šį bloką!";

    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = 'kai ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = 'atlikti';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_EVENT_TITLE = 'kai bet kuris ';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = 'iškviesti ';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = 'iškviesti ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = 'komponentui';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = 'komponento';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = 'nustatyti ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = ' į';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = 'nustatyti ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = ' į';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_OF_COMPONENT = 'komponento';

///////////////////
    /* HelpURLs for Component Blocks */

//User Interface Components
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_HELPURL = '/reference/components/userinterface.html#Button';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Button';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BUTTON_EVENTS_HELPURL = '/reference/components/userinterface.html#Button';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_HELPURL = '/reference/components/userinterface.html#CheckBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#CheckBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CHECKBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#CheckBox';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_HELPURL = '/reference/components/sensors.html#Clock';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_PROPERTIES_HELPURL = '/reference/components/sensors.html#Clock';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_EVENTS_HELPURL = '/reference/components/sensors.html#Clock';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOCK_METHODS_HELPURL = '/reference/components/sensors.html#Clock';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_HELPURL = '/reference/components/userinterface.html#Image';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Image';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_EVENTS_HELPURL = '/reference/components/userinterface.html#Image';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGE_METHODS_HELPURL = '/reference/components/userinterface.html#Image';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_HELPURL = '/reference/components/userinterface.html#Label';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Label';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_EVENTS_HELPURL = '/reference/components/userinterface.html#Label';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LABEL_METHODS_HELPURL = '/reference/components/userinterface.html#Label';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_HELPURL = '/reference/components/userinterface.html#ListPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#ListPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_EVENTS_HELPURL = '/reference/components/userinterface.html#ListPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTPICKER_METHODS_HELPURL = '/reference/components/userinterface.html#ListPicker';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SWITCH_HELPURL = '/reference/components/userinterface.html#Switch';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TIMEPICKER_HELPURL = '/reference/components/userinterface.html#TimePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_DATEPICKER_HELPURL = '/reference/components/userinterface.html#DatePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LISTVIEW_HELPURL = '/reference/components/userinterface.html#ListView';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_HELPURL = "/reference/components/userinterface.html#Notifier";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Notifier';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_EVENTS_HELPURL = '/reference/components/userinterface.html#Notifier';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NOTIFIER_METHODS_HELPURL = '/reference/components/userinterface.html#Notifier';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PASSWORDTEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#PasswordTextBox';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_HELPURL = '/reference/components/userinterface.html#Screen';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Screen';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_EVENTS_HELPURL = '/reference/components/userinterface.html#Screen';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SCREEN_METHODS_HELPURL = '/reference/components/userinterface.html#Screen';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_HELPURL = '/reference/components/userinterface.html#Slider';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#Slider';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_EVENTS_HELPURL = '/reference/components/userinterface.html#Slider';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SLIDER_METHODS_HELPURL = '/reference/components/userinterface.html#Slider';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SPINNER_HELPURL = '/reference/components/userinterface.html#Spinner';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_HELPURL = '/reference/components/userinterface.html#TextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_PROPERTIES_HELPURL = '/reference/components/userinterface.html#TextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_EVENTS_HELPURL = '/reference/components/userinterface.html#TextBox';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTBOX_METHODS_HELPURL = '/reference/components/userinterface.html#TextBox';

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_HELPURL = "/reference/components/userinterface.html#WebViewer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_PROPERTIES_HELPURL = '/reference/components/userinterface.html#WebViewer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_EVENTS_HELPURL = '/reference/components/userinterface.html#WebViewer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEBVIEWER_METHODS_HELPURL = '/reference/components/userinterface.html#WebViewer';

//Layout components
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_HELPURL = "/reference/components/layout.html#HorizontalArrangement";
    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#HorizontalArrangement';

    Blockly.Msg.LANG_COMPONENT_BLOCK_HORIZSCROLLARRANGE_HELPURL = "/reference/components/layout.html#HorizontalScrollArrangement";

    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_HELPURL = "/reference/components/layout.html#VerticalArrangement";
    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#VerticalArrangement';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VERTSCROLLARRANGE_HELPURL = "/reference/components/layout.html#VerticalScrollArrangement";

    Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_HELPURL = "/reference/components/layout.html#TableArrangement";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TABLEARRANGE_PROPERTIES_HELPURL = '/reference/components/layout.html#TableArrangement';

//Media components
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_HELPURL = '/reference/components/media.html#Camcorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_PROPERTIES_HELPURL = '/reference/components/media.html#Camcorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_EVENTS_HELPURL = '/reference/components/media.html#Camcorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMCORDER_METHODS_HELPURL = '/reference/components/media.html#Camcorder';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_HELPURL = '/reference/components/media.html#Camera';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_PROPERTIES_HELPURL = '/reference/components/media.html#Camera';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_EVENTS_HELPURL = '/reference/components/media.html#Camera';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CAMERA_METHODS_HELPURL = '/reference/components/media.html#Camera';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_HELPURL = '/reference/components/media.html#ImagePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_PROPERTIES_HELPURL = '/reference/components/media.html#ImagePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_EVENTS_HELPURL = '/reference/components/media.html#ImagePicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGEPICKER_METHODS_HELPURL = '/reference/components/media.html#ImagePicker';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_HELPURL = '/reference/components/media.html#Player';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#Player';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_EVENTS_HELPURL = '/reference/components/media.html#Player';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PLAYER_METHODS_HELPURL = '/reference/components/media.html#Player';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_HELPURL = '/reference/components/media.html#Sound';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_PROPERTIES_HELPURL = '/reference/components/media.html#Sound';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_EVENTS_HELPURL = '/reference/components/media.html#Sound';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUND_METHODS_HELPURL = '/reference/components/media.html#Sound';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_HELPURL = "/reference/components/media.html#SoundRecorder";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_PROPERTIES_HELPURL = '/reference/components/media.html#SoundRecorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_EVENTS_HELPURL = '/reference/components/media.html#SoundRecorder';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SOUNDRECORDER_METHODS_HELPURL = '/reference/components/media.html#SoundRecorder';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_HELPURL = "/reference/components/media.html#SpeechRecognizer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_PROPERTIES_HELPURL = '/reference/components/media.html#SpeechRecognizer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_EVENTS_HELPURL = '/reference/components/media.html#SpeechRecognizer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SPEECHRECOGNIZER_METHODS_HELPURL = '/reference/components/media.html#SpeechRecognizer';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_HELPURL = "/reference/components/media.html#TextToSpeech";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_PROPERTIES_HELPURL = '/reference/components/media.html#TextToSpeech';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_EVENTS_HELPURL = '/reference/components/media.html#TextToSpeech';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTTOSPEECH_METHODS_HELPURL = '/reference/components/media.html#TextToSpeech';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_HELPURL = '/reference/components/media.html#VideoPlayer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_PROPERTIES_HELPURL = '/reference/components/media.html#VideoPlayer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_EVENTS_HELPURL = '/reference/components/media.html#VideoPlayer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VIDEOPLAYER_METHODS_HELPURL = '/reference/components/media.html#VideoPlayer';

// Drawing and Animation components
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_HELPURL = "/reference/components/animation.html#Ball";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_PROPERTIES_HELPURL = '/reference/components/animation.html#Ball';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_EVENTS_HELPURL = '/reference/components/animation.html#Ball';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BALL_METHODS_HELPURL = '/reference/components/animation.html#Ball';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_HELPURL = '/reference/components/animation.html#Canvas';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_PROPERTIES_HELPURL = '/reference/components/animation.html#Canvas';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_EVENTS_HELPURL = '/reference/components/animation.html#Canvas';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CANVAS_METHODS_HELPURL = '/reference/components/animation.html#Canvas';

    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_HELPURL = "/reference/components/animation.html#ImageSprite";
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_PROPERTIES_HELPURL = '/reference/components/animation.html#ImageSprite';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_EVENTS_HELPURL = '/reference/components/animation.html#ImageSprite';
    Blockly.Msg.LANG_COMPONENT_BLOCK_IMAGESPRITE_METHODS_HELPURL = '/reference/components/animation.html#ImageSprite';

// Maps components
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_HELPURL = "/reference/components/maps.html#Map";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_CIRCLE_HELPURL = "/reference/components/maps.html#Circle";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_FEATURECOLLECTION_HELPURL = "/reference/components/maps.html#FeatureCollection";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_LINESTRING_HELPURL = "/reference/components/maps.html#LineString";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_MARKER_HELPURL = "/reference/components/maps.html#Marker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_NAVIGATION_HELPURL = "/reference/components/maps.html#Navigation";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_POLYGON_HELPURL = "/reference/components/maps.html#Polygon";
    Blockly.Msg.LANG_COMPONENT_BLOCK_MAPS_RECTANGLE_HELPURL = "/reference/components/maps.html#Rectangle";

//Sensor components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_HELPURL = "/reference/components/sensors.html#AccelerometerSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#AccelerometerSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#AccelerometerSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACCELEROMETERSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#AccelerometerSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_HELPURL = "/reference/components/sensors.html#BarcodeScanner";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_PROPERTIES_HELPURL = '/reference/components/sensors.html#BarcodeScanner';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_EVENTS_HELPURL = '/reference/components/sensors.html#BarcodeScanner';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BARCODESCANNER_METHODS_HELPURL = '/reference/components/sensors.html#BarcodeScanner';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BAROMETER_HELPURL = '/reference/components/sensors.html#Barometer';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_HELPURL = "/reference/components/sensors.html#GyroscopeSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#GyroscopeSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#GyroscopeSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GYROSCOPESENSOR_METHODS_HELPURL = '/reference/components/sensors.html#GyroscopeSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_HYGROMETER_HELPURL = '/reference/components/sensors.html#Hygrometer';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LIGHTSENSOR_HELPURL = '/reference/components/sensors.html#LightSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_HELPURL = "/reference/components/sensors.html#LocationSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#LocationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#LocationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_LOCATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#LocationSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NEARFIELDSENSOR_HELPURL = "/reference/components/sensors.html#NearField";

    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_HELPURL = "/reference/components/sensors.html#OrientationSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_PROPERTIES_HELPURL = '/reference/components/sensors.html#OrientationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_EVENTS_HELPURL = '/reference/components/sensors.html#OrientationSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ORIENTATIONSENSOR_METHODS_HELPURL = '/reference/components/sensors.html#OrientationSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PEDOMETERSENSOR_HELPURL = "/reference/components/sensors.html#Pedometer";

    Blockly.Msg.LANG_COMPONENT_BLOCK_PROXIMITYSENSOR_HELPURL = "/reference/components/sensors.html#ProximitySensor";

    Blockly.Msg.LANG_COMPONENT_BLOCK_THERMOMETER_HELPURL = '/reference/components/sensors.html#Thermometer';

//Social components
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_HELPURL = "/reference/components/social.html#ContactPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#ContactPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_EVENTS_HELPURL = '/reference/components/social.html#ContactPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_CONTACTPICKER_METHODS_HELPURL = '/reference/components/social.html#ContactPicker';

    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_HELPURL = "/reference/components/social.html#EmailPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#EmailPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_EVENTS_HELPURL = '/reference/components/social.html#EmailPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_EMAILPICKER_METHODS_HELPURL = '/reference/components/social.html#EmailPicker';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_HELPURL = "/reference/components/social.html#PhoneCall";
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_PROPERTIES_HELPURL = '/reference/components/social.html#PhoneCall';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_EVENTS_HELPURL = '/reference/components/social.html#PhoneCall';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONECALL_METHODS_HELPURL = '/reference/components/social.html#PhoneCall';

    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_HELPURL = "/reference/components/social.html#PhoneNumberPicker";
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_PROPERTIES_HELPURL = '/reference/components/social.html#PhoneNumberPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_EVENTS_HELPURL = '/reference/components/social.html#PhoneNumberPicker';
    Blockly.Msg.LANG_COMPONENT_BLOCK_PHONENUMBERPICKER_METHODS_HELPURL = '/reference/components/social.html#PhoneNumberPicker';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_HELPURL = "/reference/components/social.html#Texting";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_PROPERTIES_HELPURL = '/reference/components/social.html#Texting';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_EVENTS_HELPURL = '/reference/components/social.html#Texting';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TEXTING_METHODS_HELPURL = '/reference/components/social.html#Texting';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SHARING_HELPURL = "/reference/components/social.html#Sharing";

    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_HELPURL = "/reference/components/social.html#Twitter";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_PROPERTIES_HELPURL = '/reference/components/social.html#Twitter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_EVENTS_HELPURL = '/reference/components/social.html#Twitter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TWITTER_METHODS_HELPURL = '/reference/components/social.html#Twitter';

//Storage Components
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_HELPURL = "/reference/components/storage.html#FusionTablesControl";
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_PROPERTIES_HELPURL = '/reference/components/storage.html#FusionTablesControl';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_EVENTS_HELPURL = '/reference/components/storage.html#FusionTablesControl';
    Blockly.Msg.LANG_COMPONENT_BLOCK_FUSIONTABLESCONTROL_METHODS_HELPURL = '/reference/components/storage.html#FusionTablesControl';

    Blockly.Msg.LANG_COMPONENT_BLOCK_FILE_HELPURL = "/reference/components/storage.html#File";

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_HELPURL = '/reference/components/storage.html#TinyDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_PROPERTIES_HELPURL = '/reference/components/storage.html#TinyDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_EVENTS_HELPURL = '/reference/components/storage.html#TinyDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYDB_METHODS_HELPURL = '/reference/components/storage.html#TinyDB';

    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_HELPURL = "/reference/components/storage.html#TinyWebDB";
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_PROPERTIES_HELPURL = '/reference/components/storage.html#TinyWebDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_EVENTS_HELPURL = '/reference/components/storage.html#TinyWebDB';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TINYWEBDB_METHODS_HELPURL = '/reference/components/storage.html#TinyWebDB';

    Blockly.Msg.LANG_COMPONENT_BLOCK_CLOUDDB_HELPURL = "/reference/components/storage.html#CloudDB";

//Connectivity components
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_HELPURL = "/reference/components/connectivity.html#ActivityStarter";
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#ActivityStarter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_EVENTS_HELPURL = '/reference/components/connectivity.html#ActivityStarter';
    Blockly.Msg.LANG_COMPONENT_BLOCK_ACTIVITYSTARTER_METHODS_HELPURL = '/reference/components/connectivity.html#ActivityStarter';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_HELPURL = "/reference/components/connectivity.html#BluetoothClient";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_PROPERTIES_HELPURL = '/reference/components/connectivity.html#BluetoothClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_EVENTS_HELPURL = '/reference/components/connectivity.html#BluetoothClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHCLIENT_METHODS_HELPURL = '/reference/components/connectivity.html#BluetoothClient';

    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_HELPURL = "/reference/components/connectivity.html#BluetoothServer";
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_PROPERTIES_HELPURL = '/reference/components/connectivity.html#BluetoothServer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_EVENTS_HELPURL = '/reference/components/connectivity.html#BluetoothServer';
    Blockly.Msg.LANG_COMPONENT_BLOCK_BLUETOOTHSERVER_METHODS_HELPURL = '/reference/components/connectivity.html#BluetoothServer';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_HELPURL = "/reference/components/connectivity.html#Serial";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_PROPERTIES_HELPURL = "/reference/components/connectivity.html#Serial";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_EVENTS_HELPURL = "/reference/components/connectivity.html#Serial";
    Blockly.Msg.LANG_COMPONENT_BLOCK_SERIAL_METHODS_HELPURL = "/reference/components/connectivity.html#Serial";

    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_HELPURL = "/reference/components/connectivity.html#Web";
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_PROPERTIES_HELPURL = '/reference/components/connectivity.html#Web';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_EVENTS_HELPURL = '/reference/components/connectivity.html#Web';
    Blockly.Msg.LANG_COMPONENT_BLOCK_WEB_METHODS_HELPURL = '/reference/components/connectivity.html#Web';

//Lego mindstorms components
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_HELPURL = "/reference/components/legomindstorms.html#NxtDirectCommands";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtDirectCommands';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDIRECT_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtDirectCommands';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_HELPURL = "/reference/components/legomindstorms.html#NxtColorSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtColorSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_EVENTS_HELPURL = '/reference/components/legomindstorms.html#NxtColorSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTCOLOR_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtColorSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_HELPURL = "/reference/components/legomindstorms.html#NxtLightSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtLightSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_EVENTS_HELPURL = '/reference/components/legomindstorms.html#NxtLightSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTLIGHT_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtLightSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_HELPURL = "/reference/components/legomindstorms.html#NxtSoundSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtSoundSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_EVENTS_HELPURL = '/reference/components/legomindstorms.html#NxtSoundSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTSOUND_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtSoundSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_HELPURL = "/reference/components/legomindstorms.html#NxtTouchSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtTouchSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_EVENTS_HELPURL = '/reference/components/legomindstorms.html#NxtTouchSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTTOUCH_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtTouchSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_HELPURL = "/reference/components/legomindstorms.html#NxtUltrasonicSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtUltrasonicSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_EVENTS_HELPURL = '/reference/components/legomindstorms.html#NxtUltrasonicSensor';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTULTRASONIC_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtUltrasonicSensor';

    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_HELPURL = "/reference/components/legomindstorms.html#NxtDrive";
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_PROPERTIES_HELPURL = '/reference/components/legomindstorms.html#NxtDrive';
    Blockly.Msg.LANG_COMPONENT_BLOCK_NXTDRIVE_METHODS_HELPURL = '/reference/components/legomindstorms.html#NxtDrive';

    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3MOTORS_HELPURL = "/reference/components/legomindstorms.html#Ev3Motors";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COLORSENSOR_HELPURL = "/reference/components/legomindstorms.html#Ev3ColorSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3GYROSENSOR_HELPURL = "/reference/components/legomindstorms.html#Ev3GyroSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3TOUCHSENSOR_HELPURL = "/reference/components/legomindstorms.html#Ev3TouchSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3ULTRASONICSENSOR_HELPURL = "/reference/components/legomindstorms.html#Ev3UltrasonicSensor";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3SOUND_HELPURL = "/reference/components/legomindstorms.html#Ev3Sound";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3UI_HELPURL = "/reference/components/legomindstorms.html#Ev3UI";
    Blockly.Msg.LANG_COMPONENT_BLOCK_EV3COMMANDS_HELPURL = "/reference/components/legomindstorms.html#Ev3Commands";


//Experimental components
    // FirebaseDB
    Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_HELPURL = "/reference/components/experimental.html#FirebaseDB";
    Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_PROPERTIES_HELPURL = "/reference/components/experimental.html#FirebaseDB";
    Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_EVENTS_HELPURL = "/reference/components/experimental.html#FirebaseDB";
    Blockly.Msg.LANG_COMPONENT_BLOCK_FIREBASE_METHODS_HELPURL = "/reference/components/experimental.html#FirebaseDB";

//Internal components
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_HELPURL = "/reference/components/internal.html#GameClient";
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_PROPERTIES_HELPURL = '/reference/components/internal.html#GameClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_EVENTS_HELPURL = '/reference/components/internal.html#GameClient';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GAMECLIENT_METHODS_HELPURL = '/reference/components/internal.html#GameClient';

    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_HELPURL = "/reference/components/internal.html#Voting";
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_PROPERTIES_HELPURL = '/reference/components/internal.html#votingproperties';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_EVENTS_HELPURL = '/reference/components/internal.html#votingevents';
    Blockly.Msg.LANG_COMPONENT_BLOCK_VOTING_METHODS_HELPURL = '/reference/components/internal.html#votingmethods';

//Misc
    Blockly.Msg.SHOW_WARNINGS = "Rodyti įspėjimus";
    Blockly.Msg.HIDE_WARNINGS = "Slėpti įspėjimus";
    Blockly.Msg.MISSING_SOCKETS_WARNINGS = "Visus lizdus turėtumėte užpildyti blokais";
    Blockly.Msg.WRONG_TYPE_BLOCK_WARINGS = "Šis blokas turėtų būti sujungtas su įvykių bloku arba procedūros apibrėžimu.";
    Blockly.Msg.ERROR_PROPERTY_SETTER_NEEDS_VALUE = 'Šiam blokui reikia reikšmės bloko, prijungto prie jo lizdo.';
    Blockly.Msg.ERROR_GENERIC_NEEDS_COMPONENT = 'Turite pateikti galiojantį komponentą bloko „%1“ lizdui.';

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION = "Klaida iš „AI2 Companion“";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "Tinklo ryšio klaida";
    Blockly.Msg.REPL_NETWORK_ERROR = "Tinklo klaida";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART = "Tinklo klaida jungiantis su „AI2 Companion“. Pabandykite iš naujo paleisti ir vėl prisijungti.";
    Blockly.Msg.REPL_OK = "Gerai";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK = "„AI2 Companion“ versijos tikrinimas";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'Jūsų „AI2 Companion“ programa neatnaujinta. Spustelėkite „Gerai“ norėdami atnaujinti. ';
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE2 = 'Jūsų „AI2 Companion“ programa neatnaujinta. Iš naujo paleiskite „AI2 Companion“ ir naudokite jį nuskaitant QR kodą, kad galėtumėte atnaujinti';
    Blockly.Msg.REPL_EMULATORS = "emuliatoriaus";
    Blockly.Msg.REPL_DEVICES = "įrenginio";
    Blockly.Msg.REPL_APPROVE_UPDATE = " ekraną, nes bus paprašyta patvirtinti atnaujinimą.";
    Blockly.Msg.REPL_MORE_INFORMATION = "Daugiau informacijos";
    Blockly.Msg.REPL_SECURE_CONNECTION = "Pastaba: esate prisijungę prie saugaus ryšio, senasis „AI2 Companion“ režimas neveiks";
    Blockly.Msg.REPL_NOT_NOW = "Ne dabar";
    Blockly.Msg.REPL_NO_LEGACY = "Senas ryšio režimas neveikia, kai „App Inventor“ įkeliamas per HTTPS (saugus).";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 = "Naudojama „AI2 Companion“ yra neatnaujinta. Ši „App Inventor“ versija turėtų būti naudojama kartu su papildoma versija.";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE = "Naudojate neatnaujintą „AI2 Companion“. Kaip įmanoma greičiau turėtumėte atnaujinti. Jei parduotuvėje turite automatinio atnaujinimo sąranką, atnaujinimas netrukus įvyks pats.";
    Blockly.Msg.REPL_COMPANION_WRONG_PACKAGE = "„AI2 Companion“ kurį naudojate, buvo sukurtas skirtingam „App Inventor“. Norėdami gauti tinkamą „AI2 Companion“, žiūrėkite „App Inventor“ ekrane esantį meniu Pagalba -> „AI2 Companion“ informacija.";
    Blockly.Msg.REPL_DISMISS = "Atmesti";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "Programinės įrangos atnaujinimas";
    Blockly.Msg.REPL_OK_LOWER = "Gerai";
    Blockly.Msg.REPL_GOT_IT = "Supratau";
    Blockly.Msg.REPL_UPDATE_INFO = 'Atnaujinimas dabar įdiegtas jūsų įrenginyje. Stebėkite savo įrenginio (arba emuliatoriaus) ekraną ir patvirtinkite programinės įrangos diegimą, kai to bus paprašyta. SVARBU: kai atnaujinimas baigsis, pasirinkite „Atlikta“. Tada eikite į „App Inventor“ savo interneto naršyklėje, spustelėkite meniu „Prijungti“ ir pasirinkite „Atkurti ryšį“. Tada vėl prijunkite įrenginį.';
    Blockly.Msg.REPL_UPDATE_NO_UPDATE = "Nėra atnaujinimo";
    Blockly.Msg.REPL_UPDATE_NO_CONNECTION = "Norėdami atnaujinti, turite būti prisijungę prie „AI2 Companion“";
    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "Neįmanoma nusiųsti atnaujinimo į įrenginį / emuliatorių";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "Neįmanoma įkelti atnaujinimo iš „App Inventor“ serverio";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "Neįmanoma įkelti atnaujinimo iš „App Inventor“ serverio (serveris neatsako)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "Dabar siunčiame naujinimą iš „App Inventor“ serverio, prašome palaukti.";
    Blockly.Msg.REPL_RUNTIME_ERROR = "Vykdymo klaida";
    Blockly.Msg.REPL_NO_ERROR_FIVE_SECONDS = "Pastaba: Nematysite kitos klaidos pranešimo 5 sekundes.";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "Prijungimas per USB kabelį";
    Blockly.Msg.REPL_STARTING_EMULATOR = "Paleidžiamas „Android“ emuliatorius. Prašome palaukti: tai gali užtrukti minutę ar dvi.";
    Blockly.Msg.REPL_CONNECTING = "Jungiamasi...";
    Blockly.Msg.REPL_CANCEL = "Atšaukti";
    Blockly.Msg.REPL_GIVE_UP = "Atsisakyti";
    Blockly.Msg.REPL_KEEP_TRYING = "Bandykite toliau";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "Sujungimo klaida";
    Blockly.Msg.REPL_NO_START_EMULATOR = "Nepavyko paleisti „AI2 Companion“ emuliatoriuje";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "Prijungtas?";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE = "„App Inventor“ nemato jūsų įrenginio, įsitikinkite, kad kabelis prijungtas ir tvarkyklės yra teisingos.";
    Blockly.Msg.REPL_HELPER_Q = "Pagelbiklis?";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'Neatrodo, kad yra paleistas „aiStarter“ pagelbiklis <a href="http://appinventor.mit.edu" target="_blank">Reikia pagalbos?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB prijungtas, laukite ";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = " sekundes (-džių), kad įsitikintumėte, jog viskas veikia.";
    Blockly.Msg.REPL_EMULATOR_STARTED = "Emuliatorius paleistas, laukite ";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE = "Paleiskite „AI2 Companion“ programą prijungtame telefone.";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR = "Paleiskite programą „AI2 Companion“ emuliatoriuje.";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING = "„AI2 Companion“ paleistas, laukite ";
    Blockly.Msg.REPL_VERIFYING_COMPANION = "Patikrinti, ar „AI2 Companion“ pradėjo veikti...";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION = "Prisijungti prie „AI2 Companion“";
    Blockly.Msg.REPL_TRY_AGAIN1 = "Nepavyko prisijungti prie „AI2 Companion“, bandykite dar kartą.";
    Blockly.Msg.REPL_YOUR_CODE_IS = "Jūsų kodas yra ";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "Ar tikrai?";
    Blockly.Msg.REPL_FACTORY_RESET = 'Bus bandoma atkurti emuliatoriaus „gamyklinę“ būseną. Jei anksčiau atnaujinote emuliatoriuje įdiegtą „AI2 Companion“, greičiausiai turėsite tai padaryti dar kartą.';
    Blockly.Msg.REPL_WEBRTC_CONNECTION_ERROR = "„AI2 Companion“ ryšio klaida";
    Blockly.Msg.REPL_WEBRTC_CONNECTION_CLOSED = "„AI2 Companion“ atsijungė";
    Blockly.Msg.REPL_EMULATOR_ONLY = 'Šią parinktį galite naudoti tik norėdami atnaujinti emuliatorių.';

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "Ar tikrai norite ištrinti visus %1 blokus (-ų)?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "Generuoti YAIL";
    Blockly.Msg.DO_IT = "Atlikti tai";
    Blockly.Msg.DO_IT_DISCONNECTED = 'Atlikti tai („AI2 Companion“ neprisijungęs)';
    Blockly.Msg.CLEAR_DO_IT_ERROR = "Išvalyti klaidą";
    Blockly.Msg.CAN_NOT_DO_IT = "Negalima atlikti";
    Blockly.Msg.CONNECT_TO_DO_IT = 'Norėdami naudoti „Atlikti tai“, turite būti prisijungę prie „AI2 Companion“ ar emuliatoriaus.';

// Clock Component Menu Items
    Blockly.Msg.TIME_YEARS = "Metai";
    Blockly.Msg.TIME_MONTHS = "Mėnesiai";
    Blockly.Msg.TIME_WEEKS = "Savaitės";
    Blockly.Msg.TIME_DAYS = "Dienos";
    Blockly.Msg.TIME_HOURS = "Valandos";
    Blockly.Msg.TIME_MINUTES = "Minutės";
    Blockly.Msg.TIME_SECONDS = "Sekundės";
    Blockly.Msg.TIME_DURATION = "Trukmė";

// Connection Dialog Messages
    Blockly.Msg.DIALOG_RENDEZVOUS_NEGOTIATING = "Prašome palaukti";
    Blockly.Msg.DIALOG_SECURE_ESTABLISHING = "Saugaus ryšio užmezgimas";
    Blockly.Msg.DIALOG_SECURE_ESTABLISHED = "Saugus ryšys užmegztas";
    Blockly.Msg.DIALOG_FOUND_COMPANION = "Aptikti „AI2 Companion“";

//Blockly.Util.Dialog Messages
    Blockly.Msg.DIALOG_UNBOUND_VAR = 'Nepriklausomos reikšmės';
    Blockly.Msg.DIALOG_SUBMIT = 'Pateikti';
    Blockly.Msg.DIALOG_ENTER_VALUES = 'Įveskite reikšmes:';
  }
};

// Initialize language definition to Lithuanian
Blockly.Msg.lt.switch_blockly_language_to_lt.init();
Blockly.Msg.lt.switch_language_to_lithuanian.init();
