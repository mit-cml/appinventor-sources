/*
 *  scheme.y
 *  SchemeKit
 *
 *  Created by Evan Patton on 9/22/16.
 *  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
 */

%start program
%token STRING INTEGER DECIMAL SYMBOL

%%

program: sexps;
sexps: /* empty */
     | sexps sexp
     ;
sexp: '(' operation operands ')';
argument: SYMBOL
        ;
non_empty_arguments: argument non_empty_arguments
                   | argument
                   ;
argument_list: '(' ')'
             | '(' non_empty_arguments ')'
             | '(' non_empty_arguments '.' argument ')'
             | argument
             ;
lambda: '(' 'lambda' argument_list sexps ')';
quote: '(' 'quote' SYMBOL ')';
operation: SYMBOL
         | lambda
         ;
operands: operand
        | operand operands
        ;
function: "#(";
token: identifier
     | boolean
     | number
     | character
     | string
     | '('
     | ')'
     | '#('
     | '#u8('
     | '\''
     | '`'
     | ','
     | ',@'
     | .
     ;
delimiter: whitespace
         | vertical_line
         | '('
         | ')'
         | '\"'
         | ';'
         ;
empty: ;
