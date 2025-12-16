goog.provide('AI.Blockly.TypeBlockMatcher');

/**
 * Escapes characters in the string that are not safe to use in a RegExp.
 * @param {*} s The string to escape. If not a string, it will be casted
 *     to one.
 * @return {string} A RegExp safe, escaped copy of `s`.
 *
 * Copied from goog.string.regExpEscape in the Closure Library.
 */
function regExpEscape(s) {
  'use strict';
  return String(s)
      .replace(/([-()\[\]{}+?*.$\^|,:#<!\\])/g, '\\$1')
      .replace(/\x08/g, '\\x08');
}

/**
 * Matches the token against the start of words in the row.
 * @param {string} token Token to match.
 * @param {number} maxMatches Max number of matches to return.
 * @param {!Array<?>} rows Rows to search for matches. Can be objects if they
 * have
 *     a toString method that returns the value to match against.
 * @return {!Array<?>} Rows that match.
 *
 * Copied from goog.ui.ac.ArrayMatcher.getPrefixMatchesForRows in the Closure Library.
 */
function getPrefixMatchesForRows(token, maxMatches, rows) {
  'use strict';
  var matches = [];

  if (token != '') {
    var escapedToken = regExpEscape(token);
    var matcher = new RegExp('(^|\\W+)' + escapedToken, 'i');

    for (var i = 0; i < rows.length && matches.length < maxMatches; i++) {
      var row = rows[i];
      if (String(row).match(matcher)) {
        matches.push(row);
      }
    }
  }
  return matches;
}

/**
 * Matches the token against similar rows, by calculating "distance" between the
 * terms.
 * @param {string} token Token to match.
 * @param {number} maxMatches Max number of matches to return.
 * @param {!Array<?>} rows Rows to search for matches. Can be objects
 *     if they have a toString method that returns the value to
 *     match against.
 * @return {!Array<?>} The best maxMatches rows.
 *
 * Copied from goog.ui.ac.ArrayMatcher.getSimilarMatchesForRows in the Closure Library.
 */
function getSimilarMatchesForRows(token, maxMatches, rows) {
  'use strict';
  var results = [];

  for (var index = 0; index < rows.length; index++) {
    var row = rows[index];
    var str = token.toLowerCase();
    var txt = String(row).toLowerCase();
    var score = 0;

    if (txt.indexOf(str) != -1) {
      score = parseInt((txt.indexOf(str) / 4).toString(), 10);

    } else {
      var arr = str.split('');

      var lastPos = -1;
      var penalty = 10;

      for (var i = 0, c; c = arr[i]; i++) {
        var pos = txt.indexOf(c);

        if (pos > lastPos) {
          var diff = pos - lastPos - 1;

          if (diff > penalty - 5) {
            diff = penalty - 5;
          }

          score += diff;

          lastPos = pos;
        } else {
          score += penalty;
          penalty += 5;
        }
      }
    }

    if (score < str.length * 6) {
      results.push({str: row, score: score, index: index});
    }
  }

  results.sort(function(a, b) {
    'use strict';
    var diff = a.score - b.score;
    if (diff != 0) {
      return diff;
    }
    return a.index - b.index;
  });

  var matches = [];
  for (var i = 0; i < maxMatches && i < results.length; i++) {
    matches.push(results[i].str);
  }

  return matches;
}

AI.Blockly.TypeBlockMatcher = function(options, query) {
  const token = query.trim();
  const rows = options.map(option => ({
    ...option,
    toString: function() { return this.displayText; }
  }));
  const matches = getPrefixMatchesForRows(token, 100, rows);

  // Because we allow for similar matches, Button.Text will always appear before Text
  // So we handle the 'text' case as a special case here
  if (token.toLowerCase() === 'text') {
    const textIndex = matches.findIndex(match => match.displayText === 'Text');
    if (textIndex > 0) {
      const textMatch = matches.splice(textIndex, 1)[0];
      matches.unshift(textMatch);
    }
  }

  // Added code to handle any number typed in the widget (including negatives and decimals)
  const numberMatch = /^-?[0-9]\d*(\.\d+)?$/.exec(token);
  if (numberMatch) {
    matches.unshift({
      blockType: 'math_number',
      displayText: token,
      fieldValues: { NUM: token }
    });
  }

  // Added code to handle default values for text fields (they start with " or ')
  const textMatch = /^["']+/.exec(token);
  if (textMatch) {
    matches.push({
      blockType: 'text',
      displayText: token,
      fieldValues: { TEXT: token.replace(/^["']|["']$/g, '') }
    });
  }

  if (matches.length === 0 && token !== '') {
    return getSimilarMatchesForRows(token, 100, rows);
  }

  return matches;
};
