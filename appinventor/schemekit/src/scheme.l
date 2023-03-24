/*
 *  scheme.l
 *  SchemeKit
 *
 *  Created by Evan Patton on 9/22/16.
 *  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
 */

%top {
#include <stdio.h>
#include <stdint.h>
#include <inttypes.h>
}

LINE_COMMENT    ;.*
BLOCK_COMMENT   #\|([^|]|\|[^#])*\|?\|#
STRING          \"[^\"]*\"
INITIAL         [a-zA-Z!$%&*/:<=>?^_~]

%%

{BLOCK_COMMENT}   //{ printf("Block comment\n"); }
{LINE_COMMENT}    //{ printf("Line comment\n"); }
{STRING}          { printf("String:          %s\n", yytext); }
\(                { printf("Lparens:         (\n"); }
\)                { printf("Rparens:         )\n"); }
#t                { printf("Bool:            true\n"); }
#f                { printf("Bool:            false\n"); }
#'                { printf("Function:        #'\n"); }
#\(               { printf("Vector:          #(\n"); }
'[^ \t\r\n()]+      { printf("Quoted Symbol:   %s\n", yytext); }
(\*|[A-Za-z_0-9\.]+):[A-Za-z0-9_]  {
                    printf("Native Func:     %s\n", yytext); }
[0-9]+\.[0-9]+    { printf("Decimal:         %s\n", yytext); }
[0-9]+            { printf("Digit:           %s\n", yytext); }
[^ \t\r\n()]+       { printf("Symbol:          %s\n", yytext); }
[ \t\r\n]         //{ printf("Whitespace\n"); }
.                 { printf("Unexpected char: %s", yytext); }
