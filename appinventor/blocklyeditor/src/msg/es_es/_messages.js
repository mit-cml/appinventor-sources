// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Spanish (Spain) strings.
 * @author raulconm@gmail.com (Raul C.)
 * @author josmasflores@gmail.com (Jose Dominguez)
 */
'use strict';

goog.provide('Blockly.Msg.es_es');

/**
 * Due to the frequency of long strings, the 80-column wrap rule need not apply
 * to message files.
 */

Blockly.Msg.es_es.switch_language_to_spanish_es = {
  // Switch language to Spanish.
  category: '',
  helpUrl: '',
  init: function() {
// Context menus.
    Blockly.Msg.DUPLICATE_BLOCK = 'Duplicar';
    Blockly.Msg.REMOVE_COMMENT = 'Borrar Comentario';
    Blockly.Msg.ADD_COMMENT = 'Añadir Commentario';
    Blockly.Msg.EXTERNAL_INPUTS = 'Entradas Externas';
    Blockly.Msg.INLINE_INPUTS = 'Entradas Internas';
    Blockly.Msg.HORIZONTAL_PARAMETERS = 'Colocar Parametros Horizontalmente';
    Blockly.Msg.VERTICAL_PARAMETERS = 'Colocar Parametros Verticalmente';
    Blockly.Msg.DELETE_BLOCK = 'Borrar Bloque';
    Blockly.Msg.DELETE_X_BLOCKS = 'Borrar %1 Bloques';
    Blockly.Msg.COLLAPSE_BLOCK = 'Ocultar Bloque';
    Blockly.Msg.EXPAND_BLOCK = 'Mostrar Bloque';
    Blockly.Msg.DISABLE_BLOCK = 'Inhabilitar Bloque';
    Blockly.Msg.ENABLE_BLOCK = 'Habilitar Bloque';
    Blockly.Msg.HELP = 'Ayuda';
    Blockly.Msg.EXPORT_IMAGE = 'Exportar como Imagen';
    Blockly.Msg.COLLAPSE_ALL = 'Ocultar Bloques';
    Blockly.Msg.EXPAND_ALL = 'Mostrar Bloques';
    Blockly.Msg.ARRANGE_H = 'Ordenar Bloques Horizontalmente';
    Blockly.Msg.ARRANGE_V = 'Ordenar Bloques Verticalmente';
    Blockly.Msg.ARRANGE_S = 'Ordenar Bloques Diagonalmente';
    Blockly.Msg.SORT_W = 'Ordenar Bloques por Anchura';
    Blockly.Msg.SORT_H = 'Ordenar Bloques por Altura';
    Blockly.Msg.SORT_C = 'Ordenar Bloques por Categoría';

// Variable renaming.
    Blockly.MSG_CHANGE_VALUE_TITLE = 'Cambiar valor:';
    Blockly.MSG_NEW_VARIABLE = 'Nueva variable...';
    Blockly.MSG_NEW_VARIABLE_TITLE = 'Nuevo nombre de variable:';
    Blockly.MSG_RENAME_VARIABLE = 'Renombrar variable...';
    Blockly.MSG_RENAME_VARIABLE_TITLE = 'Renombrar todas las "%1" variables:';

// Toolbox.
    Blockly.MSG_VARIABLE_CATEGORY = 'Variables';
    Blockly.MSG_PROCEDURE_CATEGORY = 'Procedimientos';

// Warnings/Errors
    Blockly.ERROR_BLOCK_CANNOT_BE_IN_DEFINTION = "Este bloque no se puede colocar en una definición";
    Blockly.ERROR_SELECT_VALID_ITEM_FROM_DROPDOWN = "Escoja un elemento válido.";
    Blockly.ERROR_DUPLICATE_EVENT_HANDLER = "El evento está duplicado para este componente.";

// Colour Blocks.
    Blockly.Msg.LANG_COLOUR_PICKER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#basic';
    Blockly.Msg.LANG_COLOUR_PICKER_TOOLTIP = 'Pincha el cuadrado para escoger un color.';
    Blockly.Msg.LANG_COLOUR_BLACK = 'negro';
    Blockly.Msg.LANG_COLOUR_WHITE = 'blanco';
    Blockly.Msg.LANG_COLOUR_RED = 'rojo';
    Blockly.Msg.LANG_COLOUR_PINK = 'rosa';
    Blockly.Msg.LANG_COLOUR_ORANGE = 'naranja';
    Blockly.Msg.LANG_COLOUR_YELLOW = 'amarillo';
    Blockly.Msg.LANG_COLOUR_GREEN = 'verde';
    Blockly.Msg.LANG_COLOUR_CYAN = 'cian';
    Blockly.Msg.LANG_COLOUR_BLUE = 'azúl';
    Blockly.Msg.LANG_COLOUR_MAGENTA = 'magenta';
    Blockly.Msg.LANG_COLOUR_LIGHT_GRAY = 'gris claro';
    Blockly.Msg.LANG_COLOUR_DARK_GRAY = 'gris oscuro';
    Blockly.Msg.LANG_COLOUR_GRAY = 'gris';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR = 'separar color';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#split';
    Blockly.Msg.LANG_COLOUR_SPLIT_COLOUR_TOOLTIP = "Una lista de 4 elementos, cada uno en el rango de 0 a 255, que representan los components Rojo, Verde, Azúl y Alpha(transparente).";
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR = 'crear color';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/colors#make';
    Blockly.Msg.LANG_COLOUR_MAKE_COLOUR_TOOLTIP = "Un color con la cantidad dada de rojo, verde, azúl, y opcionalmente, alpha.";

// Control Blocks
    Blockly.Msg.LANG_CATEGORY_CONTROLS = 'Control';
    Blockly.Msg.LANG_CONTROLS_IF_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#if';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_1 = 'Si el valor es cierto, ejecutar las siguientes instrucciones.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_2 = 'Si el valor es cierto, ejecutar el primer bloque de instrucciones. Si no es cierto, ejecutar el segundo bloque de instrucciones.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_3 = 'Si el primer valor es cierto, ejecutar el primer bloque de instrucciones.\n' +
        'Si no, si el segundo valor es cierto, ejecutar el segundo bloque de instrucciones.';
    Blockly.Msg.LANG_CONTROLS_IF_TOOLTIP_4 = 'Si el primer valor es cierto, ejecutar el primer bloque de instrucciones.\n' +
        'Si no, si el segundo valor es cierto, ejecutar el segundo bloque de instrucciones.\n' +
        'Si ninguno de los valores es verdadero, ejecutar el último bloque de instrucciones.';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_IF = 'si';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSEIF = 'si no, si';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_ELSE = 'si no';
    Blockly.Msg.LANG_CONTROLS_IF_MSG_THEN = 'entonces';

    Blockly.Msg.LANG_CONTROLS_IF_IF_TITLE_IF = 'si';
    Blockly.Msg.LANG_CONTROLS_IF_IF_TOOLTIP = 'Añadir, borrar, o reordenar secciones\n' +
        'para re-configurar este bloque si.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TITLE_ELSEIF = 'si no, si';
    Blockly.Msg.LANG_CONTROLS_IF_ELSEIF_TOOLTIP = 'Añade una condición al bloque si.';

    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TITLE_ELSE = 'si no, ';
    Blockly.Msg.LANG_CONTROLS_IF_ELSE_TOOLTIP = 'Añade una condición final al bloque si.';

    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TITLE_REPEAT = 'repetir';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_INPUT_DO = 'ejecuta';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_WHILE = 'mientras';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_OPERATOR_UNTIL = 'hasta';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_WHILE = 'Mientras el valor es verdadero, ejecuta las siguientes instrucciones.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_UNTIL = 'Mientras el valor es falso, ejecuta ciertas instrucciones.';
    Blockly.Msg.LANG_CONTROLS_WHILEUNTIL_TOOLTIP_1 = 'Ejecuta los bloques en la sección \'ejecuta\' mientras el valor es verdadero.';

    Blockly.Msg.LANG_CONTROLS_FOR_HELPURL = '';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_WITH = 'contar con';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_VAR = 'x';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_FROM = 'desde';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_TO = 'hasta';
    Blockly.Msg.LANG_CONTROLS_FOR_INPUT_DO = 'ejecuta';

    Blockly.Msg.LANG_CONTROLS_FOR_TOOLTIP = 'Cuenta desde el número inicial al final.\n' +
        'Por cada número contado, asigna el número del contador a\n' +
        'variable "%1", y ejecuta las instrucciones.';

    Blockly.Msg.LANG_CONTROLS_FORRANGE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#forrange';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_ITEM = 'por cada';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_VAR = 'número';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_START = 'desde';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_END = 'hasta';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_STEP = 'en incrementos de';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_DO = 'ejecuta';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_TEXT = 'para cada número en el rango';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_PREFIX = 'por ';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX = ' en el  rango';
    Blockly.Msg.LANG_CONTROLS_FORRANGE_TOOLTIP = 'Ejecuta los bloques en la sección \'ejecutar\' para cada valor numérico '
        + 'en el rango desde el inicio al final, pasando por cada valor uno a uno. Utiliza el nombre de la '
        + 'variable asignada para referirse al valor actual.';

    Blockly.Msg.LANG_CONTROLS_FOREACH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#foreach';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_ITEM = 'por cada';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_VAR = 'elemento';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_INLIST = 'en la lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_DO = 'ejecutar';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_TEXT = 'por cada elemento en la lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_PREFIX = 'por ';
    Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX = ' en la lista';
    Blockly.Msg.LANG_CONTROLS_FOREACH_TOOLTIP = 'Ejecutar los bloques en la sección \'do\'  por cada elemento de '
        + 'la lista.  Utilizar el nombre de la variable dado para referirse al elemento actual de la lista.';

    Blockly.Msg.LANG_CONTROLS_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#get';


    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_HELPURL = 'http://en.wikipedia.org/wiki/Control_flow';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_INPUT_OFLOOP = 'del bucle';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_BREAK = 'salir';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_OPERATOR_CONTINUE = 'continuar con la siguiente iteración';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_BREAK = 'Salir del bucle.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_TOOLTIP_CONTINUE = 'Saltar al final del bucle, y\n' +
        'continuar con la siguiente iteración.';
    Blockly.Msg.LANG_CONTROLS_FLOW_STATEMENTS_WARNING = 'Aviso:\n' +
        'Este bloque solo puede\n' +
        'utilizarse dentro de un bucle.';

    Blockly.Msg.LANG_CONTROLS_WHILE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#while';;
    Blockly.Msg.LANG_CONTROLS_WHILE_TITLE = 'mientras';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_TEST = 'comprobar';
    Blockly.Msg.LANG_CONTROLS_WHILE_INPUT_DO = 'ejecutar';
    Blockly.Msg.LANG_CONTROLS_WHILE_COLLAPSED_TEXT = 'mientras';
    Blockly.Msg.LANG_CONTROLS_WHILE_TOOLTIP = 'Ejecutar los bloques en la sección \'do\' mientras la comprobación sea '
        + 'cierta.';

    Blockly.Msg.LANG_CONTROLS_CHOOSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#choose';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TITLE = 'si'
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_TEST = '';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_THEN_RETURN = 'entonces';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_INPUT_ELSE_RETURN = 'si no';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_COLLAPSED_TEXT = 'si';
    Blockly.Msg.LANG_CONTROLS_CHOOSE_TOOLTIP = 'Si la condición a comprobar es cierta,'
        + 'devolver el resultado de evaluar la expresión encajada en la pieza \'then-return\';'
        + 'en otro caso devolver el resultado de evaluar la expresión encajada en la pieza \'else-return\' ;'
        + 'como máximo se evaluará una de las expresiones devueltas.';

    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#doreturn';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_DO = 'ejecutar';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_INPUT_RETURN = 'resultado';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TOOLTIP = 'Ejecutar los bloques en \'do\' y devolver una sentencia. Útil cuando se requiere ejecutar un procedimiento antes de devolver un valor a una variable.';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_COLLAPSED_TEXT = 'ejecutar/resultado';
    Blockly.Msg.LANG_CONTROLS_DO_THEN_RETURN_TITLE = 'ejecutar resultado';

    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TITLE = 'evaluar pero ignorar el resultado'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#evaluate';
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_COLLAPSED_TEXT = 'evaluar pero ignorar'
    Blockly.Msg.LANG_CONTROLS_EVAL_BUT_IGNORE_TOOLTIP = 'Ejecutar el bloque de código conectado e ignorar e ignorar el valor devuelto (si existe). Útil cuando se requiere invocar un procedimiento que devuelve un valor pero no se necesita dicho valor.';

    /* [lyn, 10/14/13] Removed for now. May come back some day.
     Blockly.Msg.LANG_CONTROLS_NOTHING_TITLE = 'nada';
     Blockly.Msg.LANG_CONTROLS_NOTHING_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#nothing';
     Blockly.Msg.LANG_CONTROLS_NOTHING_TOOLTIP = 'No devuelve nada. Se utiliza para inicializar variables, es posible encajarlo en una pieza de retorno si no se necesita el valor devuelto. Equivale  a nulo o Ninguno.';
     */

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreen';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TITLE = 'abrir otra pantalla';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_INPUT_SCREENNAME = 'Nombre de la pantalla';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_COLLAPSED_TEXT = 'abrir pantalla';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_TOOLTIP = 'Abre una nueva pantalla en una aplicación de múltiples pantallas.';

    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#openscreenwithvalue';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TITLE = 'abre otra pantalla con un valor inicial';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_SCREENNAME = 'Nombre de la pantalla';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_INPUT_STARTVALUE = 'Valor inicial';
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_COLLAPSED_TEXT = 'abrir pantalla con valor'
    Blockly.Msg.LANG_CONTROLS_OPEN_ANOTHER_SCREEN_WITH_START_VALUE_TOOLTIP = 'Abrir una nueva pantalla en una aplicación de múltiples pantallas y pasar el '
        + 'valor inicial a dicha pantalla.';

    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getstartvalue';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TITLE = 'tomar el valor inicial';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_SCREENNAME = 'Nombre de la pantalla';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_INPUT_STARTVALUE = 'Valor inicial';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_COLLAPSED_TEXT = 'tomar el valor inicial';
    Blockly.Msg.LANG_CONTROLS_GET_START_VALUE_TOOLTIP = 'Devuelve el valor que se pasó a esta pantalla cuando '
        + 'se abrió, normalmente desde otra pantalla de una aplicación multi-pantalla. Si no se pasó ningún valor, '
        + 'devuelve un texto en blanco.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreen';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TITLE = 'cerrar pantalla';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_COLLAPSED_TEXT = 'cerrar pantalla';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_TOOLTIP = 'Cerrar la pantalla actual';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithvalue';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TITLE = 'cerrar la pantalla con un valor';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_INPUT_RESULT = 'resultado';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_COLLAPSED_TEXT = 'cerrar pantalla con un valor';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_VALUE_TOOLTIP = 'Cierra la pantalla actual y devuelve un resultado a la '
        + 'pantalla desde la que se abrió.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closeapp';;
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TITLE = 'cerrar la aplicación';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_COLLAPSED_TEXT = 'cerrar la aplicación';
    Blockly.Msg.LANG_CONTROLS_CLOSE_APPLICATION_TOOLTIP = 'Cierra todas las pantalla de esta aplicación y finaliza la aplicación.';

    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#getplainstarttext';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TITLE = 'tomar el texto inicial';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_COLLAPSED_TEXT = 'tomar el texto inicial';
    Blockly.Msg.LANG_CONTROLS_GET_PLAIN_START_TEXT_TOOLTIP = 'Devuelve el texto que se pasó a esta aplicación cuando '
        + 'fue iniciada desde otra aplicación. Si no se pasó ningún texto, devuelve un texto en blanco. Para '
        + 'aplicaciones de múltiples pantallas, utilícese tomar valor inicial en lugar de tomar texto inicial.';

    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/control#closescreenwithplaintext';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TITLE = 'cerrar pantalla con texto';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_COLLAPSED_TEXT = 'cerrar pantalla con texto';
    Blockly.Msg.LANG_CONTROLS_CLOSE_SCREEN_WITH_PLAIN_TEXT_TOOLTIP = 'Cierra la pantalla actual y devuelve texto a la aplicación '
        + 'desde la que se abrió. Para aplicaciones de múltiples pantallas, utilícese cerrar pantalla con un valor en lugar de '
        + 'cerrar pantalla con texto.';



// Logic Blocks.
    Blockly.Msg.LANG_CATEGORY_LOGIC = 'Lógica';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL = 'http://en.wikipedia.org/wiki/Inequality_(mathematics)';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#=';
    Blockly.Msg.LANG_LOGIC_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not=';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_EQ = 'Comprueba si dos cosas son iguales. \n' +
        'Los elementos a comparar pueden ser cualquier cosa, no solo números.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TOOLTIP_NEQ = 'Devolver cierto si ambos elementos de entrada no son iguales.';
    Blockly.Msg.LANG_LOGIC_COMPARE_TRANSLATED_NAME = 'lógica igual';
    Blockly.Msg.LANG_LOGIC_COMPARE_EQ = '=';
    Blockly.Msg.LANG_LOGIC_COMPARE_NEQ = '\u2260';

    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_AND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#and';
    Blockly.Msg.LANG_LOGIC_OPERATION_HELPURL_OR = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#or';
    Blockly.Msg.LANG_LOGIC_OPERATION_AND = 'y';
    Blockly.Msg.LANG_LOGIC_OPERATION_OR = 'o';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_AND = 'Devolver cierto si todos los elementos de entrada son ciertos.';
    Blockly.Msg.LANG_LOGIC_OPERATION_TOOLTIP_OR = 'Devolver cierto si algún elemento de entrada es cierto.';

    Blockly.Msg.LANG_LOGIC_NEGATE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#not';
    Blockly.Msg.LANG_LOGIC_NEGATE_INPUT_NOT = 'no';
    Blockly.Msg.LANG_LOGIC_NEGATE_TOOLTIP = 'Devuelve cierto si el elemento de entrada es falso.\n' +
        'Devuelve falso si el elemento de entrada es cierto.';

    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#true';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/logic#false';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TRUE = 'cierto';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_FALSE = 'falso';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_TRUE = 'Devuleve el valor booleano cierto.';
    Blockly.Msg.LANG_LOGIC_BOOLEAN_TOOLTIP_FALSE = 'Devuleve el valor booleano falso.';

// Math Blocks.
    Blockly.Msg.LANG_CATEGORY_MATH = 'Matemáticas';
    Blockly.Msg.LANG_MATH_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#number';
    Blockly.Msg.LANG_MATH_NUMBER_TOOLTIP = 'Report the number shown.';
    Blockly.Msg.LANG_MATH_MUTATOR_ITEM_INPUT_NUMBER = 'número';

    Blockly.Msg.LANG_MATH_COMPARE_HELPURL = '';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_EQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_NEQ = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#not=';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_LTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#lte';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gt';
    Blockly.Msg.LANG_MATH_COMPARE_HELPURL_GTE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#gte';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_EQ = 'Devolver cierto si ambos números son iguales.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_NEQ = 'Devolver cierto si ambos números no son iguales.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LT = 'Devolver cierto si el primer número es menor\n' +
        'que el segundo.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_LTE = 'Devolver cierto si el primer número es menor\n' +
        'o igual que el segundo.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GT = 'Devolver cierto si el primer número es mayor\n' +
        'que el segundo.';
    Blockly.Msg.LANG_MATH_COMPARE_TOOLTIP_GTE = 'Devolver cierto si el primer número es mayor\n' +
        'o igual que el segundo.';
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
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_ADD = 'Devolver la suma de dos números.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MINUS = 'Devolver la resta de dos números.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_MULTIPLY = 'Devolver la multiplicación de dos números.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_DIVIDE = 'Devolver el cociente de dos números.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_TOOLTIP_POWER = 'Devolver el primer número elevado\n' +
        'al segundo.';
    Blockly.Msg.LANG_MATH_ARITHMETIC_ADD = '+';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MINUS = '-';
    Blockly.Msg.LANG_MATH_ARITHMETIC_MULTIPLY = '*';
    Blockly.Msg.LANG_MATH_ARITHMETIC_DIVIDE = '/';
    Blockly.Msg.LANG_MATH_ARITHMETIC_POWER = '^';

    /*Blockly.Msg.LANG_MATH_CHANGE_TITLE_CHANGE = 'cambiar';
     Blockly.Msg.LANG_MATH_CHANGE_TITLE_ITEM = 'elemento';
     Blockly.Msg.LANG_MATH_CHANGE_INPUT_BY = 'por';
     Blockly.Msg.LANG_MATH_CHANGE_TOOLTIP = 'Sumar un número a la variable "%1".';*/


    Blockly.Msg.LANG_MATH_SINGLE_OP_ROOT = 'raiz cuadrada';
    Blockly.Msg.LANG_MATH_SINGLE_OP_ABSOLUTE = 'valor absoluto';
    Blockly.Msg.LANG_MATH_SINGLE_OP_NEG = 'neg';
    Blockly.Msg.LANG_MATH_SINGLE_OP_LN = 'log';
    Blockly.Msg.LANG_MATH_SINGLE_OP_EXP = 'e^';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ROOT = 'Devolver la raiz cuadrada de un número.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ROOT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sqrt';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_ABS = 'Devolver el valor absoluto de un número.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_ABS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#abs';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_NEG = 'Devolver el negativo de un número.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_NEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#neg';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_LN = 'Devolver el logaritmo natural de un número.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_LN ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#log';
    Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_EXP = 'Devolver e elevado a un número.';
    Blockly.Msg.LANG_MATH_SINGLE_HELPURL_EXP ='http://appinventor.mit.edu/explore/ai2/support/blocks/math#e';
    /*Blockly.Msg.LANG_MATH_SINGLE_TOOLTIP_POW10 = 'Devolver 10 elevado a un número.';*/

    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_ROUND = 'Redondear al entero más próximo, superior o inferior.';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_ROUND = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#round';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_CEILING = 'Redondea al número entero\n' +
        ' superior más cercano';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_CEILING =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#ceiling';
    Blockly.Msg.LANG_MATH_ROUND_TOOLTIP_FLOOR = 'Redondea al número entero\n' +
        ' inferior más cercano';
    Blockly.Msg.LANG_MATH_ROUND_HELPURL_FLOOR =  'http://appinventor.mit.edu/explore/ai2/support/blocks/math#floor';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_ROUND = 'redondear';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_CEILING = 'superior';
    Blockly.Msg.LANG_MATH_ROUND_OPERATOR_FLOOR = 'inferior';

    Blockly.Msg.LANG_MATH_TRIG_SIN = 'sen';
    Blockly.Msg.LANG_MATH_TRIG_COS = 'cos';
    Blockly.Msg.LANG_MATH_TRIG_TAN = 'tan';
    Blockly.Msg.LANG_MATH_TRIG_ASIN = 'asen';
    Blockly.Msg.LANG_MATH_TRIG_ACOS = 'acos';
    Blockly.Msg.LANG_MATH_TRIG_ATAN = 'atan';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2 = 'atan2';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_X = 'x';
    Blockly.Msg.LANG_MATH_TRIG_ATAN2_Y = 'y';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_SIN = 'Devuelve el seno de un ángulo en grados.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_SIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#sin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_COS = 'Devuelve el coseno de un ángulo en grados.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_COS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#cos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_TAN = 'Devuelve la tangente de un ángulo en grados.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_TAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#tan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ASIN = 'Devuelve el ángulo en grados  (-90,+90]\n' +
        'para un valor seno dado.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ASIN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#asin';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ACOS = 'Devuelve el ángulo en grados [0, 180)\n' +
        'para un valor coseno dado.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ACOS = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#acos';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN = 'Devuelve el ángulo en grados (-90, +90)\n' +
        'para un valor tangente dado.';
    ATAN : Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan';
    Blockly.Msg.LANG_MATH_TRIG_TOOLTIP_ATAN2 = 'Devuelve el ángulo en grados (-180, +180]\n' +
        'para unas coordenadas rectangulares dadas.';
    Blockly.Msg.LANG_MATH_TRIG_HELPURL_ATAN2 = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#atan2';

    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MIN = 'min';
    Blockly.Msg.LANG_MATH_ONLIST_OPERATOR_MAX = 'max';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MIN = 'Devuelve el menor de sus argumentos.';
    Blockly.Msg.LANG_MATH_ONLIST_TOOLTIP_MAX = 'Devuelve el mayor de sus argumentos.';


    Blockly.Msg.LANG_MATH_DIVIDE = '\u00F7';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_MODULO = 'módulo de';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_REMAINDER = 'resto de';
    Blockly.Msg.LANG_MATH_DIVIDE_OPERATOR_QUOTIENT = 'cociente de';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_MODULO = 'Devolver el módulo.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_MODULO = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#modulo';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_REMAINDER = 'Devolver el resto.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_REMAINDER = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#remainder';
    Blockly.Msg.LANG_MATH_DIVIDE_TOOLTIP_QUOTIENT = 'Devolver el cociente.';
    Blockly.Msg.LANG_MATH_DIVIDE_HELPURL_QUOTIENT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#quotient';

    Blockly.Msg.LANG_MATH_RANDOM_INT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomint';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TITLE_RANDOM = 'random integer';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_FROM = 'Entre';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT_TO = 'y';
    Blockly.Msg.LANG_MATH_RANDOM_INT_INPUT = 'entero aleatorio entre %1 y %2';
    Blockly.Msg.LANG_MATH_RANDOM_INT_TOOLTIP = 'Devuelve un número entero aleatorio entre el límite superior\n' +
        'y el límite inferior. Los límites siempre estarán por debajo \n' +
        'de 2**30.';

    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomfrac';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TITLE_RANDOM = 'decimal aleatorio';
    Blockly.Msg.LANG_MATH_RANDOM_FLOAT_TOOLTIP = 'Devolver un número aleatorio entre 0 y 1.';

    Blockly.Msg.LANG_MATH_RANDOM_SEED_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#randomseed';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TITLE_RANDOM = 'semilla aleatoria';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_INPUT_TO = 'es';
    Blockly.Msg.LANG_MATH_RANDOM_SEED_TOOLTIP = 'especifica una semilla numérica\n' +
        'para el generador de números aleatorios';

    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TITLE_CONVERT = 'convertir';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_RAD_TO_DEG = 'radianes a grados';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_OP_DEG_TO_RAD = 'grados a radianes';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_RAD_TO_DEG = 'Devuelve el valor en grados entre\n' +
        '[0, 360) correspondiente a los radianes definidos como argumento.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_RAD_TO_DEG = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertrad';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_TOOLTIP_DEG_TO_RAD = 'Devuelve el valor en radianes entre\n' +
        '[-\u03C0, +\u03C0) correspondiente a los grados definidos como argumento.';
    Blockly.Msg.LANG_MATH_CONVERT_ANGLES_HELPURL_DEG_TO_RAD = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#convertdeg';

    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#format';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TITLE = 'dar formato decimal';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_NUM = 'número';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT_PLACES = 'decimales';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_INPUT = 'dar formato decimal al número %1 decimales %2';
    Blockly.Msg.LANG_MATH_FORMAT_AS_DECIMAL_TOOLTIP = 'Devuelve un número en formato decimal\n' +
        'con un número especificado de cifras decimales.';

    Blockly.Msg.LANG_MATH_IS_A_NUMBER_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_INPUT_NUM = '¿es un número?';
    Blockly.Msg.LANG_MATH_IS_A_NUMBER_TOOLTIP = 'Comprueba si algo es un número.';

    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_INPUT_NUM = 'is base 10?';
    Blockly.Msg.LANG_MATH_IS_A_DECIMAL_TOOLTIP = 'Tests if something is decimal.';

    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_INPUT_NUM = 'is hexadecimal?';
    Blockly.Msg.LANG_MATH_IS_A_HEXADECIMAL_TOOLTIP = 'Tests if something is hexadecimal.';

    Blockly.Msg.LANG_MATH_IS_A_BINARY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/math#isnumber';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_INPUT_NUM = 'is binary?';
    Blockly.Msg.LANG_MATH_IS_A_BINARY_TOOLTIP = 'Tests if something is binary.';


    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TITLE_CONVERT = 'convert number';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_HEX = 'base 10 to hex';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_HEX = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_HEX = 'Returns the conversion from decimal to hexadecimal';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_HEX_TO_DEC = 'hex to base 10';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_HEX_TO_DEC = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_HEX_TO_DEC = 'Returns the conversion from hexadecimal to decimal';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_DEC_TO_BIN = 'base 10 to binary';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_DEC_TO_BIN = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_DEC_TO_BIN = 'Returns the conversion from decimal to binary';

    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_OP_BIN_TO_DEC = 'binary to base 10';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_HELPURL_BIN_TO_DEC = '';
    Blockly.Msg.LANG_MATH_CONVERT_NUMBER_TOOLTIP_BIN_TO_DEC = 'Returns the conversion from binary to decimal';

// Text Blocks.
    Blockly.Msg.LANG_CATEGORY_TEXT = 'Texto';
    Blockly.Msg.LANG_TEXT_TEXT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#string';
    Blockly.Msg.LANG_TEXT_TEXT_TOOLTIP = 'Una cadena de texto.';
    Blockly.Msg.LANG_TEXT_TEXT_LEFT_QUOTE = '\u201C';
    Blockly.Msg.LANG_TEXT_TEXT_RIGHT_QUOTE = '\u201D';

    Blockly.Msg.LANG_TEXT_JOIN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#join';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_CREATEWITH = 'crear texto con';
    Blockly.Msg.LANG_TEXT_JOIN_TOOLTIP = 'Concatena todas las entradas para formar una única cadena de texto.\n'
        + 'Si no hay entradas, crea un texto en blanco.';
    Blockly.Msg.LANG_TEXT_JOIN_TITLE_JOIN = 'unir';

    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TITLE_ITEM = 'cadena';
    Blockly.Msg.LANG_TEXT_JOIN_ITEM_TOOLTIP = '';

    Blockly.Msg.LANG_TEXT_APPEND_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
    Blockly.Msg.LANG_TEXT_APPEND_TO = 'a';
    Blockly.Msg.LANG_TEXT_APPEND_APPENDTEXT = 'concatena texto';
    Blockly.Msg.LANG_TEXT_APPEND_VARIABLE = 'elemento';
    Blockly.Msg.LANG_TEXT_APPEND_TOOLTIP = 'Añade texto a una variable "%1".';

    Blockly.Msg.LANG_TEXT_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#length';
    Blockly.Msg.LANG_TEXT_LENGTH_INPUT_LENGTH = 'longitud';
    Blockly.Msg.LANG_TEXT_LENGTH_TOOLTIP = 'Devuelve el número de letras\n' +
        'que hay en el texto especificado (espacios incluidos).';

    Blockly.Msg.LANG_TEXT_ISEMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#isempty';
    Blockly.Msg.LANG_TEXT_ISEMPTY_INPUT_ISEMPTY = 'está vacío';
    Blockly.Msg.LANG_TEXT_ISEMPTY_TOOLTIP = 'Devuelve cierto si la longitud del\n' + 'texto es 0, y falso en otro caso.';

    Blockly.Msg.LANG_TEXT_COMPARE_LT = ' <';
    Blockly.Msg.LANG_TEXT_COMPARE_EQUAL = ' =';
    Blockly.Msg.LANG_TEXT_COMPARE_GT = ' >';
    Blockly.Msg.LANG_TEXT_COMPARE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#comparar';
    Blockly.Msg.LANG_TEXT_COMPARE_INPUT_COMPARE = 'comparar textos';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_LT = 'Compara si texto1 es lexicográficamente menor que texto2.\n'
        + 'si un texto es el prefijo del otro, el texto más corto\n'
        + 'se considera menor. Los caracteres en mayúsculas preceden a los caracteres en minúsculas.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_EQUAL = 'Comprueba si dos cadenas de caracteres son idénticas, es decir, si tienen los mismos\n'
        + 'caracteres en el mismo orden. Esto es diferente del = normal \n'
        + 'cuando las cadenas de texto son números: 123 y 0123 son =\n'
        + 'pero no son texto =.';
    Blockly.Msg.LANG_TEXT_COMPARE_TOOLTIP_GT = 'Comprueba si texto1 es lexicográficamente mayor que texto2.\n'
        + 'si un texto es prefijo del otro, el texto más corto se considera menor.\n'
        + 'Los cracteres en mayúsculas preceden a los caracteres en minúsculas.';

    /*Blockly.Msg.LANG_TEXT_ENDSTRING_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_ENDSTRING_INPUT = 'letras en texto';
     Blockly.Msg.LANG_TEXT_ENDSTRING_TOOLTIP = 'Devuelve un número especificado de letras al comienzo o al final del texto.';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_FIRST = 'primero';
     Blockly.Msg.LANG_TEXT_ENDSTRING_OPERATOR_LAST = 'último';*/

    /*Blockly.Msg.LANG_TEXT_INDEXOF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_INDEXOF_TITLE_FIND = 'encontrar';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_OCCURRENCE = 'ocurrencia de texto';
     Blockly.Msg.LANG_TEXT_INDEXOF_INPUT_INTEXT = 'en texto';
     Blockly.Msg.LANG_TEXT_INDEXOF_TOOLTIP = 'Devuelve la posición de la primera/última ocurrencia\n' +
     'del primer texto en el segundo texto.\n' +
     'Si no se encuentra el texto devuelve 0.';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_FIRST = 'primero';
     Blockly.Msg.LANG_TEXT_INDEXOF_OPERATOR_LAST = 'último';*/

    /*Blockly.Msg.LANG_TEXT_CHARAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_AT = 'letra en';
     Blockly.Msg.LANG_TEXT_CHARAT_INPUT_INTEXT = 'en texto';
     Blockly.Msg.LANG_TEXT_CHARAT_TOOLTIP = 'Devuelve la letra que ocupa la posición especificada.';*/

    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_UPPERCASE = 'mayúscula';
    Blockly.Msg.LANG_TEXT_CHANGECASE_OPERATOR_DOWNCASE = 'minúscula';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_UPPERCASE = 'Devuelve en mayúsculas una copia de la cadena de texto especificada como argumento.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_UPPERCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#upcase';
    Blockly.Msg.LANG_TEXT_CHANGECASE_TOOLTIP_DOWNCASE = 'Devuelve en minúsculas una copia de la cadena de texto especificada como argumento.';
    Blockly.Msg.LANG_TEXT_CHANGECASE_HELPURL_DOWNCASE = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#downcase';

    Blockly.Msg.LANG_TEXT_TRIM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#trim';
    Blockly.Msg.LANG_TEXT_TRIM_TITLE_TRIM = 'recortar';
    Blockly.Msg.LANG_TEXT_TRIM_TOOLTIP = 'Devuelve una copia de la cadena de texto especificada como argumento\n'
        + 'eliminando los espacios anteriores o posteriores.';

    Blockly.Msg.LANG_TEXT_STARTS_AT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#startsat';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_STARTS_AT = 'comienza en';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT_PIECE = 'cadena';
    Blockly.Msg.LANG_TEXT_STARTS_AT_INPUT = 'comienzo en el texto %1 cadena %2';
    Blockly.Msg.LANG_TEXT_STARTS_AT_TOOLTIP = 'Devuelve la posición de inicio de una cadena de texto dentro de otro texto.\n'
        + 'donde la posición 1 indica el inicio del texto. Devuelve 0 si\n'
        + 'no se ha encontrado la cadena dentro del texto.';

    Blockly.Msg.LANG_TEXT_CONTAINS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#contains';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_CONTAINS = 'contiene';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT_PIECE = 'cadena';
    Blockly.Msg.LANG_TEXT_CONTAINS_INPUT = 'contiene  texto %1 cadena %2';
    Blockly.Msg.LANG_TEXT_CONTAINS_TOOLTIP = 'Comprueba si la cadena de caracteres está contenida dentro del texto.';

    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL = '';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT = 'en';
    Blockly.Msg.LANG_TEXT_SPLIT_INPUT_AT_LIST = 'en (lista)';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST = 'recorta el primero';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_FIRST_OF_ANY = 'recorta el primero de cualquiera';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT = 'recorta';
    Blockly.Msg.LANG_TEXT_SPLIT_OPERATOR_SPLIT_AT_ANY = 'recorta en cualquiera';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST = 'Divide en partes un texto dado, utilizando la posición de la primera ocurrencia \n'
        + 'del texto \'at\' como punto de división, y devuelve una lista de dos elementos, donde el primero es la parte de texto \n'
        + 'anterior al corte y el segundo es la parte posterior. \n'
        + 'Recortar "manaza, plátano, cereza, piña" con una coma como punto de corte \n'
        + 'devuelve una lista de dos elementos: el primero es “manzana” y el segundo es el texto \n'
        + ' “plátano, cereza, piña”. \n'
        + 'Obsérvese que la coma posterior a “manzana” no aparece en el resultado, \n'
        + 'porque es el punto de corte.';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitat';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_FIRST_OF_ANY = 'Divide el texto especificado en una lista de dos elementos, utilizando la primera posición de un elemento \n'
        + 'en la lista \'at\' como punto de corte. \n\n'
        + 'Recortar “Me gustan las manzanas plátanos manzanas uvas”  "I love apples bananas apples grapes" por la lista "(ma,pl)" devuelve \n'
        + 'una lista de dos elementos, siendo el primero "Me gustan las" y el segundo \n'
        + '"nzanas plátanos manzanas uvas."';
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_FIRST_OF_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatfirstofany';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT = 'Divide el texto en partes utilizando el texto \'at\' como puntos de corte y genera una lista con los resultados.  \n'
        + 'Recortar "uno,dos,tres,cuatro" en "," (coma) devuelve la lista "(uno dos tres cuatro)". \n'
        + 'Recortar "uno-patata,dos-patata,tres-patata,cuatro" en "-patata", devuelve la lista"(uno dos tres cuatro)".'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#split';
    Blockly.Msg.LANG_TEXT_SPLIT_TOOLTIP_SPLIT_AT_ANY = 'Divide el texto especificado en una lista, utilizando cualquiera de los elementos de la lista \'at\' como \n'
        + 'punto de corte, y devuelve una lista con los resultados. \n'
        + 'Recortar "manzana, pera, plátano, pescado" con \'at\' como lista de dos elementos cuyo \n'
        + 'primer elemento es una coma y el segundo es “pe” devuelve una lista de cuatro elementos: \n'
        + '"(manzana ra plátano scado)".'
    Blockly.Msg.LANG_TEXT_SPLIT_HELPURL_SPLIT_AT_ANY = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitatany';

    /*.LANG_TEXT_PRINT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode91.html';
     Blockly.Msg.LANG_TEXT_PRINT_TITLE_PRINT = 'escribir';
     Blockly.Msg.LANG_TEXT_PRINT_TOOLTIP = 'Escribir el texto, número u otro valor especificado.';*/

    /*Blockly.Msg.LANG_TEXT_PROMPT_HELPURL = 'http://www.liv.ac.uk/HPC/HTMLF90Course/HTMLF90CourseNotesnode92.html';
     Blockly.Msg.LANG_TEXT_PROMPT_TITLE_PROMPT_FOR = 'solicitar';
     Blockly.Msg.LANG_TEXT_PROMPT_TITILE_WITH_MESSAGE = 'con el mensaje';
     Blockly.Msg.LANG_TEXT_PROMPT_TOOLTIP = 'Solicitar un valor al usuario con el texto especificado.';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_TEXT = 'texto';
     Blockly.Msg.LANG_TEXT_PROMPT_TYPE_NUMBER = 'número';*/

    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#splitspaces';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_SPACES_TITLE = 'dividir por espacios';
    Blockly.Msg.LANG_TEXT_SPLIT_AT_TOOLTIP = 'Divide el texto en parte separadas por espacios.';

    Blockly.Msg.LANG_TEXT_SEGMENT_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#segment';
    Blockly.Msg.LANG_TEXT_SEGMENT_TITLE_SEGMENT = 'segmento';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_START = 'inicio';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_LENGTH = 'longitud';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_TEXT_SEGMENT_INPUT = 'segmento de texto %1 inicio %2 longitud %3';
    Blockly.Msg.LANG_TEXT_SEGMENT_AT_TOOLTIP = 'Extrae de un texto dado un segmento de una longitud especificada\n'
        + 'comenzando el texto desde la posición indicada. La posición\n'
        + '1 indica el principio del texto.';

    Blockly.Msg.LANG_TEXT_REPLACE_ALL_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/text#replaceall';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_SEGMENT = 'segmento';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TITLE_REPLACE_ALL = 'sustituye por';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT_REPLACEMENT = 'sustituto';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_INPUT = 'sustituye en todo el texto %1 segmento %2 sustituto %3';
    Blockly.Msg.LANG_TEXT_REPLACE_ALL_TOOLTIP = 'Devuelve un texto nuevo resultado de sustituir todas las ocurrencias\n'
        + 'del segmento por su sustituto.';

// Lists Blocks.
    Blockly.Msg.LANG_CATEGORY_LISTS = 'Listas';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_HELPURL = 'http://en.wikipedia.org/wiki/Linked_list#Empty_lists';
    Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TITLE = 'crear una lista vacía';
//Blockly.Msg.LANG_LISTS_CREATE_EMPTY_TOOLTIP = 'Devuelve una lista, de longitud 0, que no contiene registros de datos';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#makealist';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TITLE_MAKE_LIST = 'construye una lista';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_TOOLTIP = 'Crea una lista con un número de elementos.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD = 'lista';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_CONTAINER_TOOLTIP = 'Añadir, borrar, o reordenar secciones para reconfigurar este bloque de lista.';

    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TITLE = 'elemento';
    Blockly.Msg.LANG_LISTS_CREATE_WITH_ITEM_TOOLTIP = 'Añadir un elemento a la lista.';

    Blockly.Msg.LANG_LISTS_ADD_ITEM_TITLE = 'elemento';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_TOOLTIP = 'Añadir un elemento a la lista.';
    Blockly.Msg.LANG_LISTS_ADD_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';

    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#selectlistitem';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TITLE_SELECT = 'seleccionar elemento de la lista';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT_INDEX = 'índice';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_INPUT = 'seleccionar elemento de la lista %1 índice %2';
    Blockly.Msg.LANG_LISTS_SELECT_ITEM_TOOLTIP = 'Devuelve el elemento de la lista ubicado en la posición indicada.';

    Blockly.Msg.LANG_LISTS_IS_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#inlist';
    Blockly.Msg.LANG_LISTS_IS_IN_TITLE_IS_IN = '¿Está en la lista?';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_THING = 'cosa';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_IS_IN_INPUT = '¿Está en la lista? cosa %1 lista %2'
    Blockly.Msg.LANG_LISTS_IS_IN_TOOLTIP = 'Devuelve cierto si la cosa es un elemento de la lista, y '
        + 'falso si no lo es.';

    Blockly.Msg.LANG_LISTS_POSITION_IN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#indexinlist';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TITLE_POSITION = 'índice en la lista';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_THING = 'cosa';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_POSITION_IN_INPUT = 'índice en la lista  cosa %1 lista %2';
    Blockly.Msg.LANG_LISTS_POSITION_IN_TOOLTIP = 'Encuentra la posición de algo concreto en la lista. Si no está en'
        + 'la lista, devuelve 0.';

    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#pickrandomitem';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TITLE_PICK_RANDOM = 'toma un elemento al azar';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_PICK_RANDOM_TOOLTIP = 'Tomar de la lista un elemento al azar.';

    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#replace';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TITLE_REPLACE = 'sustituye elemento de la lista';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_INDEX = 'índice';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT_REPLACEMENT = 'sustituto';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_INPUT = 'sustituye elemento de la lista  lista %1 índice %2 sustituto %3';
    Blockly.Msg.LANG_LISTS_REPLACE_ITEM_TOOLTIP = 'Sustituye el elemeno n de una lista.';

    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#removeitem';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TITLE_REMOVE = 'eliminar elemento de la lista';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT_INDEX = 'índice';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_INPUT = 'eliminar elemento de la lista lista %1 índice %2';
    Blockly.Msg.LANG_LISTS_REMOVE_ITEM_TOOLTIP = 'Elimina de la lista el elemento que ocupa una posición especificada.';

    /*Blockly.Msg.LANG_LISTS_REPEAT_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_REPEAT_TITLE_CREATE = 'crear lista con elemento';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_REPEATED = 'repetido';
     Blockly.Msg.LANG_LISTS_REPEAT_INPUT_TIMES = 'veces';
     Blockly.Msg.LANG_LISTS_REPEAT_TOOLTIP = 'Crear una lista que contiene un valor dado\n' +
     'un número especificado de veces.';*/

    Blockly.Msg.LANG_LISTS_LENGTH_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lengthoflist';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LENGTH = 'longitud de la lista';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_LENGTH_INPUT = 'longitud de la lista lista %1';
    Blockly.Msg.LANG_LISTS_LENGTH_TOOLTIP = 'Contar el número de elementos que hay en una lista.';

    Blockly.Msg.LANG_LISTS_APPEND_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#append';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TITLE_APPEND = 'añadir a la lista';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST1 = 'lista1';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT_LIST2 = 'lista2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_INPUT = 'añadir a la lista lista1 %1 lista2 %2';
    Blockly.Msg.LANG_LISTS_APPEND_LIST_TOOLTIP = 'Añade al final de la lista1 todos los elementos  que hay en lista2. Una vez '
        + 'añadidos, lista1 contendrá todos los elementos, y lista2 permanecerá inalterada.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#additems';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TITLE_ADD = 'añadir elementos a la lista ';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_LIST = ' lista';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT_ITEM = 'elemento';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_INPUT = 'añadir elementos a la lista list %1 elemento %2';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_TOOLTIP = 'Añade elementos al final de una lista.';

    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TITLE_ADD = 'lista';
    Blockly.Msg.LANG_LISTS_ADD_ITEMS_CONTAINER_TOOLTIP = 'Añadir, eliminar, o reordenar secciones para reconfigurar este bloque de lista.';

    Blockly.Msg.LANG_LISTS_COPY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#copy';
    Blockly.Msg.LANG_LISTS_COPY_TITLE_COPY = 'copiar lista';
    Blockly.Msg.LANG_LISTS_COPY_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_COPY_TOOLTIP = 'Hace una copia de una lista, incluyendo la copia de todas las sublistas';

    Blockly.Msg.LANG_LISTS_IS_LIST_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#isalist';
    Blockly.Msg.LANG_LISTS_IS_LIST_TITLE_IS_LIST = '¿es una lista?';
    Blockly.Msg.LANG_LISTS_IS_LIST_INPUT_THING = 'cosa';
    Blockly.Msg.LANG_LISTS_IS_LIST_TOOLTIP = 'Comprueba si algo es una lista.';

    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvrow';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TITLE_TO_CSV = 'lista a registro csv';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_TO_CSV_ROW_TOOLTIP = 'Interpreta la lista como un registro de una tabla, y devuelve un texto CSV'
        + '\(comma-separated value\) que representa al registro. Cada elemento de la lista de registros es '
        + 'considerado como un campo, y está delimitado por comillas en el texto CSV resultante. '
        + 'Los elementos están separados por comas. No hay un divisor de línea '
        + 'al final del texto devuelto.';

    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvrow';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TITLE_FROM_CSV = 'lista desde registro csv';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_LISTS_FROM_CSV_ROW_TOOLTIP = 'Analiza un texto como un registro en formato CSV \(valores separados por comas\) '
        + 'para generar una lista de campos. Es erróneo que el texto del registro contenga un carácter de cambio de línea sin código de escape '
        + 'dentro de los campos \(en la práctica, varias líneas\). Es correcto que el texto del registro '
        + 'termine con un carácter de cambio de línea o CRLF.';

    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listtocsvtable';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TITLE_TO_CSV = 'lista a tabla csv';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_TO_CSV_TABLE_TOOLTIP = 'Interpreta la lista como una tabla con formato row-major y '
        + 'devuelve un texto CSV \(valores separados por comas\) que representa a la tabla. Cada elemento de la lista debe ser'
        + ' por sí mismo una lista que representa un registro de la tabla CSV. Cada elemento '
        + 'en la lista de registros es considerado como un campo, y delimitado por comillas en el texto CSV resultante '
        + 'En el texto devuelto, los elementos de los registros están separados por comas,  '
        + 'y los registros están separados por CRLF \(\\r\\n\).';

    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#listfromcsvtable';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TITLE_FROM_CSV = 'lista desde tabla CSV';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_INPUT_TEXT = 'texto';
    Blockly.Msg.LANG_LISTS_FROM_CSV_TABLE_TOOLTIP = 'Analiza un texto como una tabla con formato CSV \(valores separados por comas\) '
        + 'para producir una lista de registros, cada uno de los cuales es una lista de campos. Los registros se pueden separar '
        + 'por caracteres de cambio de línea \(\\n\) o CRLF \(\\r\\n\).';

    Blockly.Msg.LANG_LISTS_INSERT_ITEM_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#insert';
    Blockly.Msg.LANG_LISTS_INSERT_TITLE_INSERT_LIST = 'insertar elemento en lista';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_INDEX = 'índice';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT_ITEM = 'elemento';
    Blockly.Msg.LANG_LISTS_INSERT_INPUT = 'insertar elemento en lista lista %1 índice %2 elemento %3';
    Blockly.Msg.LANG_LISTS_INSERT_TOOLTIP = 'Inserta un elemento en una lista en una posición especificada.';

    Blockly.Msg.LANG_LISTS_IS_EMPTY_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#islistempty';
    Blockly.Msg.LANG_LISTS_TITLE_IS_EMPTY = '¿está vacía la lista?';
    Blockly.Msg.LANG_LISTS_INPUT_LIST = 'lista';
    Blockly.Msg.LANG_LISTS_IS_EMPTY_TOOLTIP = 'Devuelve cierto si la lista está vacía.';

    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/lists#lookuppairs';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TITLE_LOOKUP_IN_PAIRS = 'buscar por parejas';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_KEY = 'clave';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_PAIRS = 'parejas';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT_NOT_FOUND = 'noEncontrado';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_INPUT = 'buscar por parejas  clave %1 parejas %2 noEncontrado %3';
    Blockly.Msg.LANG_LISTS_LOOKUP_IN_PAIRS_TOOLTIP = 'Devuelve el valor asociado con la clave en la lista de parejas';

    /*Blockly.Msg.LANG_LISTS_INDEX_OF_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TITLE_FIND = 'encontrar';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_OCCURRENCE = 'ocurrencia del elemento';
     Blockly.Msg.LANG_LISTS_INDEX_OF_INPUT_IN_LIST = 'en la lista';
     Blockly.Msg.LANG_LISTS_INDEX_OF_TOOLTIP = 'Devuelve la posición de la primera/última ocurrencia\n' +
     'del elemento en la lista.\n' +
     'Devuelve 0 se no ha encontrado el texto.';
     Blockly.Msg.LANG_LISTS_INDEX_OF_FIRST = 'primero';
     Blockly.Msg.LANG_LISTS_INDEX_OF_LAST = 'último';

     Blockly.Msg.LANG_LISTS_GET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TITLE_GET = 'tomar el elemento en';
     Blockly.Msg.LANG_LISTS_GET_INDEX_INPUT_IN_LIST = 'en la lista';
     Blockly.Msg.LANG_LISTS_GET_INDEX_TOOLTIP = 'Devuelve el valor almacenado en una posición especificada de la lista.';

     Blockly.Msg.LANG_LISTS_SET_INDEX_HELPURL = 'http://publib.boulder.ibm.com/infocenter/lnxpcomp/v8v101/index.jsp?topic=%2Fcom.ibm.xlcpp8l.doc%2Flanguage%2Fref%2Farsubex.htm';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_SET = 'Almacena el elemento en';
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_IN_LIST = 'en la lista;
     Blockly.Msg.LANG_LISTS_SET_INDEX_INPUT_TO = 'como';
     Blockly.Msg.LANG_LISTS_SET_INDEX_TOOLTIP = 'Almacena el valor en una posición especificada de una lista.';*/

// Variables Blocks.
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TITLE_INIT = 'inicializar global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_NAME = 'nombre';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TO = 'como';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_COLLAPSED_TEXT = 'global';
    Blockly.Msg.LANG_VARIABLES_GLOBAL_DECLARATION_TOOLTIP = 'Crea una variable global y le asigna el valor de los bloques encajados.';

    Blockly.Msg.LANG_VARIABLES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#get';
    Blockly.Msg.LANG_VARIABLES_GET_TITLE_GET = 'tomar';
    /* Blockly.Msg.LANG_VARIABLES_GET_INPUT_ITEM = 'elemento'; */ // [lyn, 10/14/13] unused
    Blockly.Msg.LANG_VARIABLES_GET_COLLAPSED_TEXT = 'tomar';
    Blockly.Msg.LANG_VARIABLES_GET_TOOLTIP = 'Devuelve el valor de esta variable.';

    Blockly.Msg.LANG_VARIABLES_SET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#set';
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_SET = 'poner';
    /* Blockly.Msg.LANG_VARIABLES_SET_INPUT_ITEM = 'elemento'; */ // [lyn, 10/14/13] unused
    Blockly.Msg.LANG_VARIABLES_SET_TITLE_TO = 'a';
    Blockly.Msg.LANG_VARIABLES_SET_COLLAPSED_TEXT = 'poner';
    Blockly.Msg.LANG_VARIABLES_SET_TOOLTIP = 'Asigna a esta variable el valor especificado como entrada.';

    Blockly.Msg.LANG_VARIABLES_VARIABLE = ' variable';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#do';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TITLE_INIT = 'inicializar local';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_DEFAULT_NAME = 'nombre';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_INPUT_TO = 'como';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_IN_DO = 'en';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_COLLAPSED_TEXT = 'local';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TOOLTIP = 'Permite crear variables que solamente son accesibles en la parte ejecutar de este bloque.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_TRANSLATED_NAME = 'inicializar local en ejecutar';

    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/variables#return';
    /* // These don't differ between the statement and expression
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TITLE_INIT = 'inicializar local';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_NAME = 'nombre';
     Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_INPUT_TO = 'como';
     */
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_IN_RETURN = 'en';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_COLLAPSED_TEXT = 'local';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TOOLTIP = 'Permite crear variables que solamente son accesibles en la sección resultado de este bloque.';
    Blockly.Msg.LANG_VARIABLES_LOCAL_DECLARATION_EXPRESSION_TRANSLATED_NAME = 'inicializar local en resultado';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TITLE_LOCAL_NAMES = 'nombres locales';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_CONTAINER_TOOLTIP = '';

    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_TITLE_NAME = 'nombre';
    Blockly.Msg.LANG_VARIABLES_LOCAL_MUTATOR_ARG_DEFAULT_VARIABLE = 'x';

// Procedures Blocks.
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DEFINE = 'como';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE = 'procedimiento';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO = 'ejecutar';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_COLLAPSED_PREFIX = 'como ';
    Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_TOOLTIP = 'Un procedimiento que o devuelve ningún valor.';

    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#doreturn';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_THEN_RETURN = 'resultado';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_DO = 'ejecutar';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_RETURN = 'resultado';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_TOOLTIP = 'Ejecuta los bloques contenidos \'ejecutar\' y devuelve un estado. Es útil cuando se necesita ejecutar un procedimiento antes de devolver un valor a una variable.';
    Blockly.Msg.LANG_PROCEDURES_DOTHENRETURN_COLLAPSED_TEXT = 'ejecutar/resultado';

    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DEFINE = 'como';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_DO = Blockly.Msg.LANG_PROCEDURES_DEFNORETURN_DO;
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_RETURN = 'resultado';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_COLLAPSED_PREFIX = 'como ';
    Blockly.Msg.LANG_PROCEDURES_DEFRETURN_TOOLTIP = 'Un procedimiento que devuelve un valor como resultado.';

    Blockly.Msg.LANG_PROCEDURES_DEF_DUPLICATE_WARNING = 'Aviso:\n' +
        'Este procedimiento tiene\n' +
        'entradas duplicadas.';

    Blockly.Msg.LANG_PROCEDURES_GET_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#get';

    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#do';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL = 'Llamar ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE = 'procedimiento';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_COLLAPSED_PREFIX = 'invocar ';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TOOLTIP = 'Llamar a un procedimiento que no devuelve un valor.';
    Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_TRANSLATED_NAME = 'llamar sin resultado';

    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_HELPURL = 'http://appinventor.mit.edu/explore/ai2/support/blocks/procedures#return';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_CALL = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_CALL;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_PROCEDURE = Blockly.Msg.LANG_PROCEDURES_CALLNORETURN_PROCEDURE;
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_COLLAPSED_PREFIX = 'llamar ';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TOOLTIP = 'Hacer una llamada a un procedimiento que devuelve un valor.';
    Blockly.Msg.LANG_PROCEDURES_CALLRETURN_TRANSLATED_NAME = 'llamar resultado';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TITLE = 'entradas';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TITLE = 'entrada:';

    Blockly.Msg.LANG_PROCEDURES_HIGHLIGHT_DEF = 'Procedimiento resaltado';

    Blockly.Msg.LANG_PROCEDURES_MUTATORCONTAINER_TOOLTIP = '';
    Blockly.Msg.LANG_PROCEDURES_MUTATORARG_TOOLTIP = '';

// Components Blocks.
    Blockly.Msg.LANG_COMPONENT_BLOCK_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_WHEN = 'cuando ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_TITLE_DO = 'ejecutar';

    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_METHOD_TITLE_CALL = 'llamar ';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_CALL = 'llamar ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_METHOD_TITLE_FOR_COMPONENT = 'para el componente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GETTER_HELPURL = '';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_GETTER_TITLE_OF_COMPONENT = 'del componente';

    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_SET = 'poner ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_SETTER_TITLE_TO = ' como';

    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_HELPURL = '';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_SET = 'poner ';
    Blockly.Msg.LANG_COMPONENT_BLOCK_GENERIC_SETTER_TITLE_TO = ' como';
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
    Blockly.Msg.SHOW_WARNINGS = "Mostrar avisos";

// Messages from replmgr.js
    Blockly.Msg.REPL_ERROR_FROM_COMPANION = "Error de Companion";
    Blockly.Msg.REPL_NETWORK_CONNECTION_ERROR = "Error de conexión de red";
    Blockly.Msg.REPL_NETWORK_ERROR = "Error de red";
    Blockly.Msg.REPL_NETWORK_ERROR_RESTART = "Error de red al comunicarse con Companion.<br />Intenta reinicar Companion y conectar de nuevo con él";
    Blockly.Msg.REPL_OK = "OK";
    Blockly.Msg.REPL_COMPANION_VERSION_CHECK = "Comprobación de la versión de Companion";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE = 'Tu aplicación Companion está anticuada. Haz clic en "OK" para actualizarla. Consulta la pantalla de tu ';
    Blockly.Msg.REPL_EMULATORS = "emulador";
    Blockly.Msg.REPL_DEVICES = "dispositivo";
    Blockly.Msg.REPL_APPROVE_UPDATE = " porque se te solicitará que apruebes la actualización.";
    Blockly.Msg.REPL_NOT_NOW = "Ahora no";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE1 = "La aplicación Companion que estás utilizando está anticuada..<br/><br/>Esta versión de App Inventor debe utilizarse con la versión ";
    Blockly.Msg.REPL_COMPANION_OUT_OF_DATE_IMMEDIATE = "Estás utilizando una versión anticuada de Companion. No necesitas actualizarla inmediatamente, pero debes considerar hacerlo pronto.";
    Blockly.Msg.REPL_DISMISS = "Descartar";
    Blockly.Msg.REPL_SOFTWARE_UPDATE = "Actualización de software";
    Blockly.Msg.REPL_OK_LOWER = "Ok";
    Blockly.Msg.REPL_GOT_IT = "Entendido";
    Blockly.Msg.REPL_UPDATE_INFO = 'Se está instalando la actualización en tu dispositivo. Mira la pantalla de tu dispositivo (o emulador) y autoriza la instalación del software cuando se te solicite.<br /><br />IMPORTANTE: Cuando finalice la actualización, pulsa "HECHO" (no pulses  "abrir"). A continuación abre la página de App Inventor en el navegador, haz clic en el menú “Conectar” y selecciona la opción “Reiniciar conexión”.';

    Blockly.Msg.REPL_UNABLE_TO_UPDATE = "No se ha podido enviar la actualización al dispositivo/emulador.";
    Blockly.Msg.REPL_UNABLE_TO_LOAD = "No se ha podido cargar la actualización desde el servidor de App Inventor";
    Blockly.Msg.REPL_UNABLE_TO_LOAD_NO_RESPOND = "No se ha podido cargar la actualización desde el servidor de App Inventor (el servidor no está respondiendo)";
    Blockly.Msg.REPL_NOW_DOWNLOADING = "Se está descargando la actualización desde el servidor de App Inventor, espera por favor.";
    Blockly.Msg.REPL_RUNTIME_ERROR = "Error de ejecución";
    Blockly.Msg.REPL_NO_ERROR_FIVE_MINUTES = "<br/><i>Nota:</i>&nbsp;No se mostrará ningún otro error en los próximos cinco segundos.";
    Blockly.Msg.REPL_CONNECTING_USB_CABLE = "Estableciendo conexión mediante cable USB";
    Blockly.Msg.REPL_STARTING_EMULATOR = "Iniciándose el emulador de Android<br/>Por favor espera, puede necesitar uno o dos minutos.";
    Blockly.Msg.REPL_CONNECTING = "Conectando...";
    Blockly.Msg.REPL_CANCEL = "Cancelar";
    Blockly.Msg.REPL_GIVE_UP = "Abandonar";
    Blockly.Msg.REPL_KEEP_TRYING = "Intentar de nuevo";
    Blockly.Msg.REPL_CONNECTION_FAILURE1 = "Conexión fallida";
    Blockly.Msg.REPL_NO_START_EMULATOR = "No se pudo iniciar el MIT AI Companion en el emulador";
    Blockly.Msg.REPL_PLUGGED_IN_Q = "¿Está conectado?";
    Blockly.Msg.REPL_AI_NO_SEE_DEVICE = "AI2 no detecta tu dispositivo, comprueba que el cable está conectado y que los controladores son los adecuados.";
    Blockly.Msg.REPL_HELPER_Q = "¿Asistente?";
    Blockly.Msg.REPL_HELPER_NOT_RUNNING = 'Parece que el asistente de aiStarter no se estñá ejecutando<br /><a href="http://appinventor.mit.edu" target="_blank">¿Necesitas ayuda?</a>';
    Blockly.Msg.REPL_USB_CONNECTED_WAIT = "USB conectando, espera ";
    Blockly.Msg.REPL_SECONDS_ENSURE_RUNNING = " segundos para asegurate de que todo está funcionando.";
    Blockly.Msg.REPL_EMULATOR_STARTED = "El emulador se ha iniciado, espera ";
    Blockly.Msg.REPL_STARTING_COMPANION_ON_PHONE = "Iniciando la aplicación Companion en el dispositivo conectado.";
    Blockly.Msg.REPL_STARTING_COMPANION_IN_EMULATOR = "Iniciando la aplicación Companion en el emulador.";
    Blockly.Msg.REPL_COMPANION_STARTED_WAITING = "Iniciando Companion, espera ";
    Blockly.Msg.REPL_VERIFYING_COMPANION = "Verificando que Companion se ha iniciado...";
    Blockly.Msg.REPL_CONNECT_TO_COMPANION = "Conectado a Companion";
    Blockly.Msg.REPL_TRY_AGAIN1 = "La conexión con MIT AI2 Companion ha fallado, inténtalo de nuevo.";
    Blockly.Msg.REPL_YOUR_CODE_IS = "Tu código es";
    Blockly.Msg.REPL_DO_YOU_REALLY_Q = "¿Estás totalmente seguro?";
    Blockly.Msg.REPL_FACTORY_RESET = 'Esto intentará devolver tu emulador a su estado “de fábrica”. Si habías actualizado Companion en el emulador, seguramente tendrás que hacerlo de nuevo.';

// Messages from Blockly.js
    Blockly.Msg.WARNING_DELETE_X_BLOCKS = "Estás seguro de que quieres borrar todos los %1 bloques?";

// Blocklyeditor.js
    Blockly.Msg.GENERATE_YAIL = "Generate Yail";
    Blockly.Msg.DO_IT = "Do It";
    Blockly.Msg.CLEAR_DO_IT_ERROR = "Cerrar Error";
    Blockly.Msg.CAN_NOT_DO_IT = 'El comando "Do it" no está disponible';
    Blockly.Msg.CONNECT_TO_DO_IT = 'Necesitas estar conectado a la Companion or al Emulador para utilizar "Do It"';
  }
};

// Initalize language definition to Spanish
Blockly.Msg.es_es.switch_language_to_spanish_es.init();
