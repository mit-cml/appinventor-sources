goog.provide('AI.Blockly.TypeBlockMatcher');

AI.Blockly.TypeBlockMatcher = function(options, query) {
  const trimmedQuery = query.trim();

  let matchedOptions;
  if (trimmedQuery) {
    const escapedToken = trimmedQuery.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&');
    const matcher = new RegExp(`(^|\\W+)${escapedToken}`, 'i');
    matchedOptions = options.filter(option => option.displayText.match(matcher));

    const numberMatch = /^-?[0-9]\d*(\.\d+)?$/.exec(trimmedQuery);
    if (numberMatch) {
      matchedOptions.unshift({
        blockType: 'math_number',
        displayText: trimmedQuery,
        fieldValues: { NUM: trimmedQuery }
      });
    }

    const textMatch = /^["']+/.exec(trimmedQuery);
    if (textMatch) {
      matchedOptions.push({
        blockType: 'text',
        displayText: trimmedQuery,
        fieldValues: { TEXT: trimmedQuery.replace(/^["']|["']$/g, '') }
      });
    }
  } else {
    matchedOptions = options;
  }

  return matchedOptions;
};
