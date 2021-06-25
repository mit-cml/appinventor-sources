# Logic Blocks

---

### True

<div id = "logic_boolean" type = "ai-2-block"></div>   

Represents the constant value true. Use it for setting boolean property values of components, or as the value of a variable that represents a condition.

### False

<div id = "logic_false" type = "ai-2-block"></div>   

Represents the constant value false. Use it for setting boolean property values of components, or as the value of a variable that represents a condition.

### Not

<div id = "logic_negate" type = "ai-2-block"></div>   

Performs logical negation, returning false if the input is true, and true if the input is false.

### Equal To

<div id = "logic_compare" type = "ai-2-block"></div>   

Tests whether its arguments are equal.   

### Not Equal To

<div id = "logic_compare_notEqualTo" type = "ai-2-block"></div>   

Tests to see whether two arguments are not equal.

### And

<div id = "logic_operation" type = "ai-2-block"></div>   

Tests whether all of a set of logical conditions are true. The result is true if and only if all the tested conditions are true. The number of tests can be expanded using the mutator. The conditions are tested left to right, and the testing stops as soon as one of the conditions is false. If there are no conditions to test, then the result is true. You can consider this to be a logician’s joke.

### Or

<div id = "logic_or" type = "ai-2-block"></div>   

Tests whether any of a set of logical conditions are true. The result is true if one or more of the tested conditions are true. The number of tests can be expanded using the mutator. The conditions are tested left to right, and the testing stops as soon as one of the conditions is true. If there are no conditions to test, then the result is false.