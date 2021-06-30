<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Variable Blocks

---

### Initialize Global Name To

<div id = "global_declaration" type = "ai-2-block"></div> 

This block is used to create global variables. It takes in any type of value as an argument. Clicking on name will change the name of this global variable. Global variables are used in all procedures or events so this block will stand alone.

Global variables can be changed while an app is running and can be referred to and changed from any part of the app even within procedures and event handlers. You can rename this block at any time and any associated blocks referring to the old name will be updated automatically.

### Get

<div id = "lexical_variable_get" type = "ai-2-default-block"></div> 

This block provides a way to get any variables you may have created.

### Set

<div id = "lexical_variable_set" type = "ai-2-default-block"></div> 

This block follows the same rules as get. Only variables in scope will be available in the dropdown. Once a variable v is selected, you can attach a block to give v a new value.

### Initialize Local Name To - In (do)

<div id = "local_declaration_statement" type = "ai-2-block"></div> 

This block is a mutator that allows you to create new variables that are only used in the procedure you run in the DO part of the block. This way all variables in this procedure will all start with the same value each time the procedure is run. NOTE: This block differs from the block described below because it is a DO block. You can attach statements to it. Statements do things. That is why this block has space inside for statement blocks to be attached.

You can rename the variables in this block at any time and any corresponding blocks elsewhere in your program that refer to the old name will be updated automatically

### Initialize Local Name To - In (return)

<div id = "local_declaration_expression" type = "ai-2-block"></div> 

This block is a mutator that allows you to create new variables that are only used in the procedure you run in the RETURN part of the block. This way all variables in this procedure will all start with the same value each time the procedure is run. NOTE: This block differs from the block described above because it is a RETURN block. You can attach expressions to it. Expressions return a value. That is why this block has a socket for plugging in expressions.

You can rename the variables in this block at any time and any corresponding blocks elsewhere in your program that refer to the old name will be updated automatically
