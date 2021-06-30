<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Math Blocks

--- 

### Basic Number Block

<div id = "math_number" type = "ai-2-block"></div>

Can be used as any positive or negative number. Clicking on the “0” in the block will allow you to change the number.
The block supports normal base-10 numbers (for example: 2, 12, and 2.12), as well as C-like prefixes for other number bases. It supports:

* Base-2 (binary) numbers, e.g. 0b10 (decimal 2)
* Base-8 (octal) numbers, e.g. 0o14 (decimal 12)
* Base-16 (hexadecimal) numbers, e.g. 0xd4 (decimal 212)

### Radix Number Block

#### Decimal Number

<div id = "math_number_radix" type = "ai-2-block"></div>

#### Binary Number

<div id = "math_number_radix_bin" type = "ai-2-default-block"></div>

#### Octal Number

<div id = "math_number_radix_oct" type = "ai-2-default-block"></div>

#### Hexadecimal Number

<div id = "math_number_radix_hax" type = "ai-2-default-block"></div>

Represents a base-10 number. Clicking on the “0” will allow you to change the number.
Clicking the dropdown will allow you to input a number in a different number base (aka radix). The number will then be “translated” into decimal (aka base-10).
For example, these three blocks are equivalent:
The dropdown supports: decimal (base-10), binary (base-2), octal (base-8), and hexadecimal (base-16) input formats.
Decimal mode allows you to input any positive or negative number (e.g. 2, -12, 2.12). The other modes only allow you to input a whole number (aka any positive number, or zero).

### Equal (=)

<div id = "math_compare" type = "ai-2-block"></div>

Tests whether two numbers are equal and returns true or false.

### Not Equal (≠)

<div id = "math_compare_neq" type = "ai-2-default-block"></div>

Tests whether two numbers are not equal and returns true or false.

### Greater Than (>)

<div id = "math_compare_gt" type = "ai-2-default-block"></div>

Tests whether the first number is greater than the second number and returns true or false.

### Greater Than or Equal (≥)

<div id = "math_compare_gt_eq" type = "ai-2-default-block"></div>

Tests whether the first number is greater than or equal to the second number and returns true or false.

### Less Than (<)

<div id = "math_compare_lt" type = "ai-2-default-block"></div>

Tests whether the first number is less than the second number and returns true or false.

### Less Than or Equal (≤)

<div id = "math_compare_lt_eq" type = "ai-2-default-block"></div>

Tests whether the first number is less than or equal to the second number and returns true or false.

### Summation (+)

<div id = "math_add" type = "ai-2-block"></div>

Returns the result of adding any amount of blocks that have a number value together. Blocks with a number value include the basic number block, length of list or text, variables with a number value, etc. This block is a mutator and can be expanded to allow more numbers in the sum.

### Subtraction (-)

<div id = "math_subtract" type = "ai-2-block"></div>

### Multiplication (*)

<div id = "math_multiply" type = "ai-2-block"></div>

Returns the result of multiplying any amount of blocks that have a number value together. It is a mutator block and can be expanded to allow more numbers in the product.

### Division (/)

<div id = "math_division" type = "ai-2-block"></div>

Returns the result of dividing the first number by the second.

### Power (^)

<div id = "math_power" type = "ai-2-block"></div>

Returns the result of the first number raised to the power of the second.

### Random Integer

<div id = "math_random_int" type = "ai-2-block"></div>

Returns a random integer value between the given values, inclusive. The order of the arguments doesn’t matter.

### Random Fraction

<div id = "math_random_float" type = "ai-2-block"></div>

Returns a random value between 0 and 1.

### Random Set Seed To

<div id = "math_random_set_seed" type = "ai-2-block"></div>

Use this block to generate repeatable sequences of random numbers. You can generate the same sequence of random numbers by first calling random set seed with the same value. This is useful for testing programs that involve random values.

### Minimum

<div id = "math_on_list" type = "ai-2-block"></div>

Returns the smallest value of a set of numbers. If there are unplugged sockets in the block, min will also consider 0 in its set of numbers. This block is a mutator and a dropdown.

### Maximum

<div id = "math_on_list_max" type = "ai-2-default-block"></div>

Returns the largest value of a set of numbers. If there are unplugged sockets in the block, max will also consider 0 in its set of numbers. This block is a mutator and a dropdown.

### Square Root

<div id = "math_single" type = "ai-2-block"></div>

Returns the square root of the given number.

### Absolute

<div id = "math_single_abs" type = "ai-2-default-block"></div>

Returns the absolute value of the given number.

### Negative (-ve)

<div id = "math_single_neg" type = "ai-2-default-block"></div>

Returns the negative of a given number.

### Logarithm

<div id = "math_single_log" type = "ai-2-default-block"></div>

Returns the natural logarithm of a given number, that is, the logarithm to the base e (2.71828…).

### e^

<div id = "math_single_exp" type = "ai-2-default-block"></div>

Returns e (2.71828…) raised to the power of the given number.

### Round

<div id = "math_single_round" type = "ai-2-default-block"></div>

Returns the given number rounded to the closest integer. If the fractional part is < .5 it will be rounded down. It it is > .5 it will be rounded up. If it is exactly equal to .5, numbers with an even whole part will be rounded down, and numbers with an odd whole part will be rounded up. (This method is called round to even.)

### Ceiling

<div id = "math_single_cei" type = "ai-2-default-block"></div>

Returns the smallest integer that’s greater than or equal to the given number.

### Floor

<div id = "math_single_floor" type = "ai-2-default-block"></div>

Returns the greatest integer that’s less than or equal to the given number.

### Modulo (%)

<div id = "math_divide" type = "ai-2-block"></div>

Modulo(a,b) is the same as remainder(a,b) when a and b are positive. More generally, modulo(a,b) is defined for any a and b so that (floor(a/b)× b) + modulo(a,b) = a. For example, modulo(11, 5) = 1, modulo(-11, 5) = 4, modulo(11, -5) = -4, modulo(-11, -5) = -1. Modulo(a,b) always has the same sign as b, while remainder(a,b) always has the same sign as a.

### Remainder

<div id = "math_divide_rem" type = "ai-2-default-block"></div>

Remainder(a,b) returns the result of dividing a by b and taking the remainder. The remainder is the fractional part of the result multiplied by b.

For example, remainder(11,5) = 1 because

11 / 5 = 2 1⁄5

In this case, 1⁄5 is the fractional part. We multiply this by b, in this case 5 and we get 1, our remainder.

Other examples are remainder(-11, 5) = -1, remainder(11, -5) = 1, and remainder(-11, -5) = -1.

### Quotient

<div id = "math_divide_que" type = "ai-2-default-block"></div>

Returns the result of dividing the first number by the second and discarding any fractional part of the result.

### sine

<div id = "math_trig" type = "ai-2-block"></div>

Returns the sine of the given number in degrees.

### Cosine

<div id = "math_cos" type = "ai-2-block"></div>

Returns the cosine of the given number in degrees.

### Tangent

<div id = "math_tan" type = "ai-2-block"></div>

Returns the tangent of the given number in degrees.

### Arc Sine

<div id = "math_trig_arc_sin" type = "ai-2-default-block"></div>

Returns the arcsine of the given number in degrees.

### Arc Cosine

<div id = "math_trig_arc_cos" type = "ai-2-default-block"></div>

Returns the arccosine of the given number in degrees.

### Arc Tangent

<div id = "math_trig_arc_tan" type = "ai-2-default-block"></div>

Returns the arctangent of the given number in degrees.

### Arc Tangent2

<div id = "math_atan2" type = "ai-2-block"></div>

Returns the arctangent of y/x, given y and x.

### Convert Radians To Degrees

<div id = "math_convert_angles" type = "ai-2-block"></div>

Returns the value in degrees of the given number in radians. The result will be an angle in the range [0, 360)

### Convert Degrees To Radians

<div id = "math_convert_angles_d_r" type = "ai-2-default-block"></div>

Returns the value in radians of the given number in degrees. The result will be an angle in the range [-π , +π)

### Format As Decimal

<div id = "math_format_as_decimal" type = "ai-2-block"></div>

Formats a number as a decimal with a given number of places after the decimal point. The number of places must be a non-negative integer. The result is produced by rounding the number (if there were too many places) or by adding zeros on the right (if there were too few).

### Is A Number?

<div id = "math_is_a_number" type = "ai-2-block" style = "margin-bottom:8px"></div>

<div id = "math_is_num_1" type = "ai-2-default-block" style = "margin-bottom:8px"></div>

<div id = "math_is_num_2" type = "ai-2-default-block" style = "margin-bottom:8px"></div>

<div id = "math_is_num_3" type = "ai-2-default-block"></div>

Returns true if the given object is a number, and false otherwise.

### Convert Number

<div id = "math_convert_number" type = "ai-2-block" style = "margin-bottom:8px"></div>

<div id = "math_con_num_1" type = "ai-2-default-block" style = "margin-bottom:8px"></div>

<div id = "math_con_num_2" type = "ai-2-default-block" style = "margin-bottom:8px"></div>

<div id = "math_con_num_3" type = "ai-2-default-block"></div>

Takes a text string that represents a positive integer in one base and returns a string that represents the same number is another base. For example, if the input string is 10, then converting from base 10 to binary will produce the string 1010; while if the input string is the same 10, then converting from binary to base 10 will produce the string 2. If the input string is the same 10, then converting from base 10 to hex will produce the string A.

### Bitwise And

<div id = "math_bitwise" type = "ai-2-block"></div>

Takes two numbers and compares each pair of bits. Each bit of the result is 1 only if the corresponding bits of both operands are 1.

### Bitwise Or (Inclusive)

<div id = "math_bitwise_or" type = "ai-2-default-block"></div>

Takes two numbers and compares each pair of bits. Each bit of the result is 1 if either of the corresponding bits in each operand is 1.

### Bitwise Or (Exclusive)

<div id = "math_bitwise_exor" type = "ai-2-default-block"></div>

Takes two numbers and compares each pair of bits. Each bit of the result is 1 only if one corresponding bit in the operands is 1 and the other is 0.
