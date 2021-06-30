// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
* AI default blocks XML
* @author preetvadaliya.ict18@gmail.com (Preet Vadaliya)
*/

const DEFAULT_BLOCKS = {
  "controls_if_else": `<xml><block type="controls_if"><mutation else="1"></mutation></block></xml>`,

  "controls_if_else_if_else": `<xml><block type="controls_if"><mutation elseif="1" else="1"></mutation></block></xml>`,

  "logic_compare_notEqualTo": `<xml><block type="logic_compare"><field name="OP">NEQ</field></block></xml>`,

  "logic_or": `<xml><block type = "logic_operation"><mutation items="2"></mutation><field name="OP">OR</field></block></xml>`,

  "text_join": `<xml><block type = "text_join"><mutation items="2"></mutation></block></xml>`,

  "text_compare_gt": `<xml><block type="text_compare"><field name="OP">GT</field></block></xml>`,
  "text_compare_eq": `<xml><block type="text_compare"><field name="OP">EQUAL</field></block></xml>`,
  "text_compare_neq": `<xml><block type="text_compare"><field name="OP">NEQ</field></block></xml>`,
  "text_downCase": `<xml><block type="text_changeCase"><field name="OP">DOWNCASE</field></block></xml>`,
  "text_containsAny": `<xml><block type="text_contains"><mutation mode="CONTAINS_ANY"></mutation><field name="OP">CONTAINS_ANY</field></block></xml>`,
  "text_containsAll": `<xml><block type="text_contains"><mutation mode="CONTAINS_ALL"></mutation><field name="OP">CONTAINS_ALL</field></block></xml>`,
  "text_splitAtFirst": `<xml><block type="text_split"><mutation mode="SPLITATFIRST"></mutation><field name="OP">SPLITATFIRST</field></block></xml>`,
  "text_splitAtFirstOfAny": `<xml><block type="text_split"><mutation mode="SPLITATFIRSTOFANY"></mutation><field name="OP">SPLITATFIRSTOFANY</field></block></xml>`,
  "text_splitAtAny": `<xml><block type="text_split"><mutation mode="SPLITATANY"></mutation><field name="OP">SPLITATANY</field></block></xml>`,
  "text_replace_mappings_dictionary_order": `<xml><block type="text_replace_mappings"><field name="OP">DICTIONARY_ORDER</field></block></xml>`,
  "lists_create_empty": `<xml><block type="lists_create_with"><mutation items="0"></mutation></block></xml>`,
  "lists_lookup_in_pairs": `<xml><block type="lists_lookup_in_pairs"><value name="NOTFOUND"><block type="text"><field name="TEXT">not found</field></block></value></block></xml>`,
  "lists_join_with_separator": `<xml><block type="lists_join_with_separator"><value name="SEPARATOR"><block type="text"><field name="TEXT"></field></block></value></block></xml>`,
  "lexical_variable_get": `<xml><block type="lexical_variable_get"><field name="VAR">global name</field></block></xml>`,
  "lexical_variable_set": `<xml><block type="lexical_variable_set"><field name="VAR">global name</field></block></xml>`,
  "color_make_color": `<xml ><block type=\"color_make_color\" id=\"{$(vN{:V1yWQqZYsTjY}\" x=\"-733\" y=\"114\"><value name=\"COLORLIST\"><block type=\"lists_create_with\" id=\"0$(gP_u/bG,-5i(OS_*Q\" inline=\"false\"><mutation items=\"3\"></mutation><value name=\"ADD0\"><block type=\"math_number\" id=\"-C.}LUOOTeZI%^C}3Exs\"><field name=\"NUM\">255</field></block></value><value name=\"ADD1\"><block type=\"math_number\" id=\"P$GHI1I[vrVo?-5llO}V\"><field name=\"NUM\">0</field></block></value><value name=\"ADD2\"><block type=\"math_number\" id=\"(4V]Nb?l(3u,=cRj*Q.C\"><field name=\"NUM\">0</field></block></value></block></value></block></xml>`,
  "color_make_color_with_alpha": `<xml ><block type=\"color_make_color\" id=\"{$(vN{:V1yWQqZYsTjY}\" x=\"-733\" y=\"114\"><value name=\"COLORLIST\"><block type=\"lists_create_with\" id=\"0$(gP_u/bG,-5i(OS_*Q\" inline=\"false\"><mutation items=\"4\"></mutation><value name=\"ADD0\"><block type=\"math_number\" id=\"-C.}LUOOTeZI%^C}3Exs\"><field name=\"NUM\">255</field></block></value><value name=\"ADD1\"><block type=\"math_number\" id=\"P$GHI1I[vrVo?-5llO}V\"><field name=\"NUM\">0</field></block></value><value name=\"ADD2\"><block type=\"math_number\" id=\"(4V]Nb?l(3u,=cRj*Q.C\"><field name=\"NUM\">0</field></block></value><value name=\"ADD3\"><block type=\"math_number\" id=\"Y.Wnej]rS~1gp|=6U~4,\"><field name=\"NUM\">0</field></block></value></block></value></block></xml>`,
  "dict_ex_1": "<xml ><block type=\"dictionaries_create_with\" id=\":U(q3_@FyjLDq7MR+gp#\" x=\"-731\" y=\"70\"><mutation items=\"5\"></mutation><value name=\"ADD0\"><block type=\"pair\" id=\"e(XK$SVMiWI]6*!DZTmP\"><value name=\"KEY\"><block type=\"text\" id=\"^3sE2zAjcXh|V0~wIS4(\"><field name=\"TEXT\">id</field></block></value><value name=\"VALUE\"><block type=\"math_number\" id=\"nC;]dA;WL,,RDl9g(;:0\"><field name=\"NUM\">1</field></block></value></block></value><value name=\"ADD1\"><block type=\"pair\" id=\"YQL|3j+@A-(($Ex22]*-\"><value name=\"KEY\"><block type=\"text\" id=\"PxM,[i`w^+t)u-CK5?!=\"><field name=\"TEXT\">name</field></block></value><value name=\"VALUE\"><block type=\"text\" id=\"8njA+mG|(D41/Mf/iW5m\"><field name=\"TEXT\">Tim the Beaver</field></block></value></block></value><value name=\"ADD2\"><block type=\"pair\" id=\"ZpXILL;y8o$)gq6BmBD6\"><value name=\"KEY\"><block type=\"text\" id=\"fw[zPDYa|b`N)hG)0!=O\"><field name=\"TEXT\">school</field></block></value><value name=\"VALUE\"><block type=\"dictionaries_create_with\" id=\"xSJ}r~Ox%ulfUp,o6Ff`\"><mutation items=\"1\"></mutation><value name=\"ADD0\"><block type=\"pair\" id=\"v|vsYR)%I_lOzzaCS(1j\"><value name=\"KEY\"><block type=\"text\" id=\"W)r/BE1Kaq|-h9za8Rxx\"><field name=\"TEXT\">name</field></block></value><value name=\"VALUE\"><block type=\"text\" id=\"~;|bbK~0Rg%+3iCiz?!u\"><field name=\"TEXT\">Massachusetts Institute of Technology</field></block></value></block></value></block></value></block></value><value name=\"ADD3\"><block type=\"pair\" id=\"gXdVX*5U*]Br8fwFn;Y.\"><value name=\"KEY\"><block type=\"text\" id=\"$$u(_[3P9Pia4!=m`p.4\"><field name=\"TEXT\">enrolled</field></block></value><value name=\"VALUE\"><block type=\"logic_boolean\" id=\"+KrA^h){oE.LWI9+tV-l\"><field name=\"BOOL\">TRUE</field></block></value></block></value><value name=\"ADD4\"><block type=\"pair\" id=\"t-1J_i_P@`|*GPm6+I#1\"><value name=\"KEY\"><block type=\"text\" id=\"H(?NE441C]t6`!t*$W}[\"><field name=\"TEXT\">classes</field></block></value><value name=\"VALUE\"><block type=\"lists_create_with\" id=\"0aiCk(;_]ao`zS:C6w2L\"><mutation items=\"3\"></mutation><value name=\"ADD0\"><block type=\"text\" id=\"Tl^]!SshXEK!9qxtOO]+\"><field name=\"TEXT\">6.001</field></block></value><value name=\"ADD1\"><block type=\"text\" id=\"|^7myi!in%hdfL@Swc(s\"><field name=\"TEXT\">18.01</field></block></value><value name=\"ADD2\"><block type=\"text\" id=\"WF.kh2PXQK+B-Gc+MkqG\"><field name=\"TEXT\">8.01</field></block></value></block></value></block></value></block></xml>",
  "dictionaries_create_with_empty": "<xml ><block type=\"dictionaries_create_with\" id=\"Bvo8J_/RfevC4yJ(s*J~\" x=\"-639\" y=\"105\"><mutation items=\"0\"></mutation></block></xml>",
  "dictionaries_create_with": "<xml ><block type=\"dictionaries_create_with\" id=\"y(#DXKv}OkhV7PM]p|g1\" x=\"-727\" y=\"55\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"pair\" id=\"6=0~2wm2o~A0v,-zV+i-\"></block></value><value name=\"ADD1\"><block type=\"pair\" id=\"TlQiyNtt+H4Nz2?7/UTZ\"></block></value></block></xml>",
  "dictionaries_get_value_for_key": "<xml ><block type=\"dictionaries_lookup\" id=\"Vqrf|S=_+!J]4#O-Wo^?\" x=\"-572\" y=\"-107\"><value name=\"NOTFOUND\"><block type=\"text\" id=\"5pE!Q|w{C6qJo%g$o`)@\"><field name=\"TEXT\">not found</field></block></value></block></xml>",
  "dictionaries_recursive_lookup": "<xml ><block type=\"dictionaries_recursive_lookup\" id=\"8wNq2O[Q/^giLWG/dk(?\" x=\"-147\" y=\"-50\"><value name=\"NOTFOUND\"><block type=\"text\" id=\"cV[E0{v}kL-4Y@yCtHQa\"><field name=\"TEXT\">not found</field></block></value></block></xml>",
  "dict_ex_2": "<xml><block  type=\"logic_compare\" id=\"|:y-(Bt:-N{dNdN_2e6k\" inline=\"false\"><field name=\"OP\">EQ</field><value name=\"A\"><block type=\"dictionaries_dict_to_alist\" id=\"FfYQVlQWIkT5sRZL#u7t\"><value name=\"DICT\"><block type=\"dictionaries_alist_to_dict\" id=\"1Gvnn=[a;0^vE8,CWkpF\"><value name=\"PAIRS\"><block type=\"lists_create_with\" id=\"UnC#u^L6~DG!I;9Q{MM.\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"lists_create_with\" id=\"Jir98jDbfS@FJ%lx%FAz\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"text\" id=\"/;OAWkHrC*6.O=A:pR6q\"><field name=\"TEXT\">key1</field></block></value><value name=\"ADD1\"><block type=\"text\" id=\"7=$$WN]on_g=MGJ$PbO3\"><field name=\"TEXT\">value1</field></block></value></block></value><value name=\"ADD1\"><block type=\"lists_create_with\" id=\"Ajd=/$x%tY?t6k}%a|?R\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"text\" id=\"b00Wal|G^@IhGT4|AD||\"><field name=\"TEXT\">key2</field></block></value><value name=\"ADD1\"><block type=\"text\" id=\"kH~/.TD`z|nsve`n-nLa\"><field name=\"TEXT\">value2</field></block></value></block></value></block></value></block></value></block></value><value name=\"B\"><block type=\"lists_create_with\" id=\"-5/c,#*i491n6eAWz,n`\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"lists_create_with\" id=\"_mrMDH1PMD.8jGY/9%=C\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"text\" id=\"c7hNn;L)u6;L[V!qF(Ok\"><field name=\"TEXT\">key1</field></block></value><value name=\"ADD1\"><block type=\"text\" id=\"e1ozFdKuUOX.TC/nYxCP\"><field name=\"TEXT\">value1</field></block></value></block></value><value name=\"ADD1\"><block type=\"lists_create_with\" id=\"J:le9#fxO(B%X%Ehy-1m\"><mutation items=\"2\"></mutation><value name=\"ADD0\"><block type=\"text\" id=\"u2zG}qOtUIYnoM}}XpOh\"><field name=\"TEXT\">key2</field></block></value><value name=\"ADD1\"><block type=\"text\" id=\"Z^I|jXalN3$*g{)h,~5f\"><field name=\"TEXT\">value2</field></block></value></block></value></block></value></block></xml>",
  "math_number_radix_bin": "<xml><block  type=\"math_number_radix\" id=\"{s:b]JLR~`v.by`%Oky~\"><field name=\"OP\">BIN</field><field name=\"NUM\">0</field></block></xml>",
  "math_number_radix_oct": "<xml><block  type=\"math_number_radix\" id=\"{s:b]JLR~`v.by`%Oky~\"><field name=\"OP\">OCT</field><field name=\"NUM\">0</field></block></xml>",
  "math_number_radix_hax": "<xml><block  type=\"math_number_radix\" id=\"{s:b]JLR~`v.by`%Oky~\"><field name=\"OP\">HEX</field><field name=\"NUM\">0</field></block></xml>",
  "math_compare_neq": "<xml><block  type=\"math_compare\" id=\"vss:cSvnL+,m.4W.LX44\"><field name=\"OP\">NEQ</field></block></xml>",
  "math_compare_gt": "<xml><block  type=\"math_compare\" id=\"vss:cSvnL+,m.4W.LX44\"><field name=\"OP\">GT</field></block></xml>",
  "math_compare_gt_eq": "<xml><block  type=\"math_compare\" id=\"vss:cSvnL+,m.4W.LX44\"><field name=\"OP\">GTE</field></block></xml>",
  "math_compare_lt": "<xml><block  type=\"math_compare\" id=\"vss:cSvnL+,m.4W.LX44\"><field name=\"OP\">LT</field></block></xml>",
  "math_compare_lt_eq": "<xml><block type=\"math_compare\"><field name=\"OP\">LTE</field></block></xml>",
  "math_on_list_max": "<xml><block type=\"math_on_list\" id=\"5%~$u|bOr[8lUHg@_XK#\"><mutation items=\"2\"></mutation><field name=\"OP\">MAX</field></block></xml>",
  "math_single_abs": "<xml><block type=\"math_single\"><field name=\"OP\">ABS</field></block></xml>",
  "math_single_abs": "<xml><block type=\"math_single\"><field name=\"OP\">ABS</field></block></xml>",
  "math_single_neg": "<xml><block type=\"math_single\"><field name=\"OP\">NEG</field></block></xml>",
  "math_single_log": "<xml><block type=\"math_single\"><field name=\"OP\">LN</field></block></xml>",
  "math_single_exp": "<xml><block type=\"math_single\"><field name=\"OP\">EXP</field></block></xml>",
  "math_single_round": "<xml><block type=\"math_single\"><field name=\"OP\">ROUND</field></block></xml>",
  "math_single_cei": "<xml><block type=\"math_single\"><field name=\"OP\">CEILING</field></block></xml>",
  "math_single_floor": "<xml><block type=\"math_single\"><field name=\"OP\">FLOOR</field></block></xml>",
  "math_divide_rem": "<xml><block type=\"math_divide\" id=\"u~DxZ^7n?q4LLU2}(J:/\"><field name=\"OP\">REMAINDER</field></block></xml>",
  "math_divide_que": "<xml><block type=\"math_divide\" id=\"u~DxZ^7n?q4LLU2}(J:/\"><field name=\"OP\">QUOTIENT</field></block></xml>",
  "math_trig_arc_sin": '<xml><block type="math_tan"><field name="OP">ASIN</field></block></xml>',
  "math_trig_arc_cos": '<xml><block type="math_tan"><field name="OP">ACOS</field></block></xml>',
  "math_trig_arc_tan": '<xml><block type="math_tan"><field name="OP">ATAN</field></block></xml>',
  "math_convert_angles_d_r": '<xml>  <block type="math_convert_angles" id="+n7$(am_$[v4HY=mO9N," x="-940" y="-6"><field name="OP">DEGREES_TO_RADIANS</field></block></xml>',
  "math_is_num_1": '<xml><block type="math_is_a_number" id="cskArFDr*~zgK%wZY{(~" x="-940" y="120"><field name="OP">BASE10</field></block></xml>',
  "math_is_num_2": '<xml><block type="math_is_a_number" id="cskArFDr*~zgK%wZY{(~" x="-940" y="120"><field name="OP">HEXADECIMAL</field></block></xml>',
  "math_is_num_3": '<xml>  <block type="math_is_a_number" id="cskArFDr*~zgK%wZY{(~" x="-940" y="120"><field name="OP">BINARY</field></block></xml>',
  "math_con_num_1": '<xml><block type="math_convert_number" id="ic[nev-2.Wg{-;,u28(*" x="-683" y="-261"><field name="OP">HEX_TO_DEC</field></block></xml>',
  "math_con_num_2": '<xml><block type="math_convert_number" id="EdHL5p~$jvoOpG9+YlY=" x="-683" y="-159"><field name="OP">BIN_TO_DEC</field></block></xml>',
  "math_con_num_3": '<xml><block type="math_convert_number" id="$3uL.s,XI_OnFYw~E`KH" x="-683" y="-210"><field name="OP">DEC_TO_BIN</field></block></xml>',
  "math_bitwise_or": '<xml><block xmlns=\"http://www.w3.org/1999/xhtml\" type=\"math_bitwise\" id=\"i4@g+dYn^Dmqj]Ms;EFc\"><mutation items=\"2\"></mutation><field name=\"OP\">BITIOR</field></block></xml>',
  "math_bitwise_exor": '<xml><block xmlns=\"http://www.w3.org/1999/xhtml\" type=\"math_bitwise\" id=\"i4@g+dYn^Dmqj]Ms;EFc\"><mutation items=\"2\"></mutation><field name=\"OP\">BITXOR</field></block></xml>',
}