<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# List Blocks

---

### Create Empty List

<div id = "lists_create_empty" type = "ai-2-default-block"></div>

Creates an empty list with no elements.

### Make A List

<div id = "lists_create_with" type = "ai-2-block"></div>

Creates a list from the given blocks. If you don’t supply any arguments, this creates an empty list, which you can add elements to later. This block is a mutator. Clicking the blue plus sign will allow you to add additional items to your list.

### Add Items To List

<div id = "lists_add_items" type = "ai-2-block"></div>

Adds the given items to the end of the list. The difference between this and append to list is that append to list takes the items to be appended as a single list while add items to list takes the items as individual arguments. This block is a mutator.

### Is In List

<div id = "lists_is_in" type = "ai-2-block"></div>

If thing is one of the elements of the list, returns true; otherwise, returns false. Note that if a list contains sublists, the members of the sublists are not themselves members of the list. For example, the members of the list (1 2 (3 4)) are 1, 2, and the list (3 4); 3 and 4 are not themselves members of the list.

### Length Of List

<div id = "lists_length" type = "ai-2-block"></div>

Returns the number of items in the list.

### Is List Empty

<div id = "lists_is_empty" type = "ai-2-block"></div>

If list has no items, returns true; otherwise, returns false.

### Pick A Random Item

<div id = "lists_pick_random_item" type = "ai-2-block"></div>

Picks an item at random from the list.

### Index In List

<div id = "lists_position_in" type = "ai-2-block"></div>

Returns the position of the thing in the list. If not in the list, returns 0.

### Select List Item

<div id = "lists_select_item" type = "ai-2-block"></div>

Selects the item at the given index in the given list. The first list item is at index 1.

### Insert List Item

<div id = "lists_insert_item" type = "ai-2-block"></div>

Inserts an item into the list at the given position.

### Replace List Item

<div id = "lists_replace_item" type = "ai-2-block"></div>

Inserts replacement into the given list at position index. The previous item at that position is removed.

### Remove List Item

<div id = "lists_remove_item" type = "ai-2-block"></div>

Removes the item at the given position.

### Append To List

<div id = "lists_append_list" type = "ai-2-block"></div>

Adds the items in the second list to the end of the first list.

### Copy List

<div id = "lists_copy" type = "ai-2-block"></div>

Makes a copy of a list, including copying all sub-lists.

### Is A List?

<div id = "lists_is_list" type = "ai-2-block"></div>

If thing is a list, returns true; otherwise, returns false.

### Reverse List

<div id = "lists_reverse" type = "ai-2-block"></div>

Reverses a copy of the list with items in the reverse order. For example reverse([1,2,3]) returns [3,2,1]

### List To CSV Row

<div id = "lists_to_csv_row" type = "ai-2-block"></div>

Interprets the list as a row of a table and returns a CSV (comma-separated value) text representing the row. Each item in the row list is considered to be a field, and is quoted with double-quotes in the resulting CSV text. Items are separated by commas. For example, converting the list (a b c d) to a CSV row produces (“a”, “b”, “c”, “d”). The returned row text does not have a line separator at the end.

### List to CSV Table

<div id = "lists_to_csv_table" type = "ai-2-block"></div>

Interprets the list as a table in row-major format and returns a CSV (comma-separated value) text representing the table. Each item in the list should itself be a list representing a row of the CSV table. Each item in the row list is considered to be a field, and is quoted with double-quotes in the resulting CSV text. In the returned text, items in rows are separated by commas and rows are separated by CRLF (\r\n).

### List from CSV Row

<div id = "lists_from_csv_row" type = "ai-2-block"></div>

Parses a text as a CSV (comma-separated value) formatted row to produce a list of fields. For example, converting (“a”, “b”, “c”, “d”) to a list produces (a b c d).

### List from CSV Table

<div id = "lists_from_csv_table" type = "ai-2-block"></div>

Parses a text as a CSV (comma-separated value) formatted table to produce a list of rows, each of which is a list of fields. Rows can be separated by newlines (\n) or CRLF (\r\n).

### Lookup In Pairs

<div id = "lists_lookup_in_pairs" type = "ai-2-default-block"></div>

Used for looking up information in a dictionary-like structure represented as a list. This operation takes three inputs, a key, a list pairs, and a notFound result, which by default, is set to “not found”. Here pairs must be a list of pairs, that is, a list where each element is itself a list of two elements. Lookup in pairs finds the first pair in the list whose first element is the key, and returns the second element. For example, if the list is ((a apple) (d dragon) (b boxcar) (cat 100)) then looking up ‘b’ will return ‘boxcar’. If there is no such pair in the list, then the lookup in pairs will return the notFound parameter. If pairs is not a list of pairs, then the operation will signal an error.

### Join With separator

<div id = "lists_join_with_separator" type = "ai-2-default-block"></div>

Joins all elements in the specified list by the specified separator, producing text as a result.
