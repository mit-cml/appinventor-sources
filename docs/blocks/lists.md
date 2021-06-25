# List Blocks

---

### Create Empty List

Creates an empty list with no elements.

### Make A List

Creates a list from the given blocks. If you don’t supply any arguments, this creates an empty list, which you can add elements to later. This block is a mutator. Clicking the blue plus sign will allow you to add additional items to your list.

### Add Items To List

Adds the given items to the end of the list. The difference between this and append to list is that append to list takes the items to be appended as a single list while add items to list takes the items as individual arguments. This block is a mutator.

### Is In List

If thing is one of the elements of the list, returns true; otherwise, returns false. Note that if a list contains sublists, the members of the sublists are not themselves members of the list. For example, the members of the list (1 2 (3 4)) are 1, 2, and the list (3 4); 3 and 4 are not themselves members of the list.

### Length Of List

Returns the number of items in the list.

### Is List Empty

If list has no items, returns true; otherwise, returns false.

### Pick A Random Item

Picks an item at random from the list.

### Index In List

Returns the position of the thing in the list. If not in the list, returns 0.

### Select List Item

Selects the item at the given index in the given list. The first list item is at index 1.

### Insert List Item

Inserts an item into the list at the given position.

### Replace List Item

Inserts replacement into the given list at position index. The previous item at that position is removed.

### Remove List Item

Removes the item at the given position.

### Append To List

Adds the items in the second list to the end of the first list.

### Copy List

Makes a copy of a list, including copying all sub-lists.

### Is A List?

If thing is a list, returns true; otherwise, returns false.

### Reverse List

Reverses a copy of the list with items in the reverse order. For example reverse([1,2,3]) returns [3,2,1]

### List To CSV Row

Interprets the list as a row of a table and returns a CSV (comma-separated value) text representing the row. Each item in the row list is considered to be a field, and is quoted with double-quotes in the resulting CSV text. Items are separated by commas. For example, converting the list (a b c d) to a CSV row produces (“a”, “b”, “c”, “d”). The returned row text does not have a line separator at the end.

### List to CSV Table

Interprets the list as a table in row-major format and returns a CSV (comma-separated value) text representing the table. Each item in the list should itself be a list representing a row of the CSV table. Each item in the row list is considered to be a field, and is quoted with double-quotes in the resulting CSV text. In the returned text, items in rows are separated by commas and rows are separated by CRLF (\r\n).

### List from CSV Row

Parses a text as a CSV (comma-separated value) formatted row to produce a list of fields. For example, converting (“a”, “b”, “c”, “d”) to a list produces (a b c d).

### List from CSV Table

Parses a text as a CSV (comma-separated value) formatted table to produce a list of rows, each of which is a list of fields. Rows can be separated by newlines (\n) or CRLF (\r\n).

### Lookup In Pairs

Used for looking up information in a dictionary-like structure represented as a list. This operation takes three inputs, a key, a list pairs, and a notFound result, which by default, is set to “not found”. Here pairs must be a list of pairs, that is, a list where each element is itself a list of two elements. Lookup in pairs finds the first pair in the list whose first element is the key, and returns the second element. For example, if the list is ((a apple) (d dragon) (b boxcar) (cat 100)) then looking up ‘b’ will return ‘boxcar’. If there is no such pair in the list, then the lookup in pairs will return the notFound parameter. If pairs is not a list of pairs, then the operation will signal an error.

### Join With separator

Joins all elements in the specified list by the specified separator, producing text as a result.