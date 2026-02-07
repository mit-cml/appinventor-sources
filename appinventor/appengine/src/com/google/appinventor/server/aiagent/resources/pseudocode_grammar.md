# App Inventor Pseudocode Grammar

This grammar defines the text representation of App Inventor blocks. Each block
is encoded as a single statement or expression using the syntax below.
Indentation (two spaces) denotes nesting. Expressions are composable inline.

---

## Statements (one per line, indentation for nesting)

### Component Operations (instance-specific)

These operate on a named component instance visible in the current screen.

```
set <Component>.<Property> to <expr>
call <Component>.<Method>(<arg1>, <arg2>, ...)
```

### Component Operations (generic / any-component)

These operate on a component reference passed as an expression, using the
component type name rather than the instance name.

```
set <Type>.<Property> of <expr> to <expr>
call <Type>.<Method> of <expr>(<arg1>, <arg2>, ...)
```

### Variable Operations

```
set global <name> to <expr>
set local <name> to <expr>
initialize local <name> to <expr>
  <statements>
```

`initialize local` introduces a block-scoped variable; the indented statements
form its scope.

### Procedure Definitions

```
procedure <name>(<param1>, <param2>, ...)
  <statements>

procedure <name>(<param1>, <param2>, ...) returns
  <statements>
  return <expr>
```

### Procedure Calls

```
call <name>(<arg1>, <arg2>, ...)
```

When used as a statement the return value (if any) is discarded.

### Event Handlers

```
when <Component>.<Event>(<param1>, <param2>, ...) do
  <statements>
```

### Control Flow

```
if <expr> then
  <statements>
else if <expr> then
  <statements>
else
  <statements>

for each <var> in <expr>
  <statements>

for each key <k> value <v> in <expr>
  <statements>

for <var> from <expr> to <expr> by <expr>
  <statements>

while <expr>
  <statements>

break

evaluate but ignore <expr>
```

### Screen Navigation

```
open another screen <name>
open another screen <name> with value <expr>
close screen
close screen with value <expr>
close screen with plain text <expr>
close application
```

### List Mutation

```
add items <expr>, <expr>, ... to <list>
insert item <expr> into <list> at <expr>
replace item <expr> in <list> with <expr>
remove item <expr> from <list>
append <list2> to <list1>
```

### Dictionary Mutation

```
set key <expr> in <dict> to <expr>
delete key <expr> from <dict>
set path <list> in <dict> to <expr>
```

---

## Expressions (inline, composable)

Expressions never stand alone; they appear inside statements or other
expressions. Parentheses are used for grouping where needed.

### Literals

```
"hello"          — text string
123              — integer
3.14             — decimal
true / false     — boolean
0xFF             — hex number
```

### Variables

```
global <name>
local <name>
<param>
get start value
```

`<param>` refers to an event or procedure parameter by bare name.

### Component Expressions

```
<Component>.<Property>
call <Component>.<Method>(<args>)
component <Name>
```

`component <Name>` yields a component reference value.

### Arithmetic

```
(<a> + <b> + ...)
(<a> - <b>)
(<a> * <b> * ...)
(<a> / <b>)
(<a> ^ <b>)
negate <expr>
abs <expr>
sqrt <expr>
log <expr>
e^ <expr>
round <expr>
floor <expr>
ceiling <expr>
modulo <a> <b>
remainder <a> <b>
quotient <a> <b>
bitwise and <a> <b>
bitwise or <a> <b>
bitwise xor <a> <b>
```

### Comparisons

```
(<a> = <b>)
(<a> != <b>)
(<a> < <b>)
(<a> <= <b>)
(<a> > <b>)
(<a> >= <b>)
```

### Logic

```
(<a> and <b> and ...)
(<a> or <b> or ...)
not <expr>
```

### Math Functions

```
sin <expr>
cos <expr>
tan <expr>
asin <expr>
acos <expr>
atan <expr>
atan2 <y> <x>
min(<a>, <b>)
max(<a>, <b>)
avg of list <expr>
sum of list <expr>
min of list <expr>
max of list <expr>
random integer from <expr> to <expr>
random fraction
format as decimal <expr> places <expr>
convert number <expr> from base <expr> to base <expr>
is a number <expr>
is a base10 <expr>
is a hexadecimal <expr>
is a binary <expr>
```

### Text Operations

```
join(<a>, <b>, ...)
length of text <expr>
is text empty <expr>
compare text <a> <b>
trim <expr>
upcase <expr>
downcase <expr>
starts at text <text> piece <piece>
contains text <text> piece <piece>
split text <text> at <at>
split text <text> at first <at>
split text <text> at any <at>
split text <text> at first any <at>
split text <text> at spaces
segment text <text> start <start> length <length>
replace all text <text> segment <seg> replacement <rep>
reverse text <expr>
```

### List Operations

```
list(<a>, <b>, ...)
select item <list> index <n>
index of <list> thing <expr>
pick random <list>
length of list <list>
is list empty <list>
is in list <list> thing <expr>
is a list <expr>
copy list <list>
reverse list <list>
csv row to list <expr>
list to csv row <expr>
csv table to list <expr>
list to csv table <expr>
lookup in pairs <pairs> key <key> notFound <default>
join items <list> separator <sep>
sort <list>
map <var> over <list> <expr>
filter <var> in <list> <test>
reduce <var> over <list> initial <init> <combine>
```

### Dictionary Operations

```
dict(<k1>: <v1>, <k2>: <v2>, ...)
pair(<k>, <v>)
lookup key <key> in <dict> notFound <default>
lookup path <path> in <dict> notFound <default>
keys of <dict>
values of <dict>
is key in <dict> key <key>
length of dict <dict>
alist to dict <expr>
dict to alist <expr>
combine dicts <d1> <d2>
walk tree <dict> all at level <list>
walk tree <dict> path <list>
```

### Colors

```
color black
color white
color red
color pink
color orange
color yellow
color green
color cyan
color blue
color magenta
color light gray
color gray
color dark gray
make color(<r>, <g>, <b>)
make color(<r>, <g>, <b>, <a>)
split color <expr>
```

### References

```
component <Name>
all components of type <Type>
```

### Helper Blocks

```
option <OptionList>.<Value>
screen name <Name>
asset <Name>
```

### Special Composite Expressions

```
if <cond> then <expr> else <expr>
do <statements> then return <expr>
initialize local <name> to <expr> in <expr>
call <name>(<args>)
```

The first form is a ternary conditional expression. The second evaluates
statements for their side effects and yields a value. The third introduces a
local variable scoped to a single expression. The fourth calls a result
procedure and yields the returned value.
