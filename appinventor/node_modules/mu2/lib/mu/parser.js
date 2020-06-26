var util   = require('util'),
    Buffer = require('buffer').Buffer,
    
    carriage = '__MU_CARRIAGE__',
    carriageRegExp = new RegExp(carriage, 'g'),
    newline = '__MU_NEWLINE__',
    newlineRegExp = new RegExp(newline, 'g');

exports.parse = function (template, options) {
  var parser = new Parser(template, options);
  return parser.tokenize();
}

function Parser(template, options) {
  this.template = template.replace(/\r\n/g, carriage)
                          .replace(/\n/g, newline);
  this.options  = options || {};
  
  this.sections = [];
  this.tokens   = ['multi'];
  this.partials = [];
  this.buffer   = this.template;
  this.state    = 'static'; // 'static' or 'tag'
  this.currentLine = '';
  
  this.setTag(['{{', '}}']);
}

Parser.prototype = {
  tokenize: function () {
    while (this.buffer) {
      this.state === 'static' ? this.scanText() : this.scanTag();
    }
    
    if (this.sections.length) {
      throw new Error('Encountered an unclosed section.');
    }
    
    return {partials: this.partials, tokens: this.tokens};
  },

  appendMultiContent: function (content) {
    for (var i = 0, len = this.sections.length; i < len; i++) {
      var multi = this.sections[i][1];
      multi = multi[multi.length - 1][3] += content;
    }
  },
  
  setTag: function (tags) {
    this.otag = tags[0] || '{{';
    this.ctag = tags[1] || '}}';
  },
  
  scanText: function () {
    var index = this.buffer.indexOf(this.otag);
    
    if (index === -1) {
      index = this.buffer.length;
    }
    
    var content = this.buffer.substring(0, index)
                             .replace(carriageRegExp, '\r\n')
                             .replace(newlineRegExp, '\n'),
        buffer  = new Buffer(Buffer.byteLength(content));
    
    if (content !== '') {
      buffer.write(content);
      this.appendMultiContent(content);
      this.tokens.push(['static', content, buffer]);
    }
   
    var line = this.currentLine + content;

    this.currentLine = line.substring(line.lastIndexOf('\n') + 1, line.length);
    // console.log('line:', this.buffer.lastIndexOf(newline) + newline.length, index, '>', this.currentLine, '/end');
    this.buffer = this.buffer.substring(index + this.otag.length);
    this.state  = 'tag';
  },
  
  scanTag: function () {
    var ctag    = this.ctag,
        matcher = 
      "^" +
      "\\s*" +                           // Skip any whitespace
                                         
      "(#|\\^|/|=|!|<|>|&|\\{)?" +       // Check for a tag type and capture it
      "\\s*" +                           // Skip any whitespace
      "(.+?)" +                          // Capture the text inside of the tag : non-greedy regex
      "\\s*" +                           // Skip any whitespace
      "=?\\}?" +                         // Skip balancing '}' or '=' if it exists
      e(ctag) +                          // Find the close of the tag
                                         
      "(.*)$"                            // Capture the rest of the string
      ;
    matcher = new RegExp(matcher);
    
    var match = this.buffer.match(matcher);
    
    if (!match) {
      throw new Error('Encountered an unclosed tag: "' + this.otag + this.buffer + '"');
    }
    
    var sigil     = match[1],
        content   = match[2].trim(),
        remainder = match[3],
        tagText   = this.otag + this.buffer.substring(0, this.buffer.length - remainder.length);

    
    switch (sigil) {
    case undefined:
      this.tokens.push(['mustache', 'etag', content]);
    this.appendMultiContent(tagText);
      break;
      
    case '>':
    case '<':
      this.tokens.push(['mustache', 'partial', content]);
      this.partials.push(content);
    this.appendMultiContent(tagText);
      break;
      
    case '{':
    case '&':
      this.tokens.push(['mustache', 'utag', content]);
    this.appendMultiContent(tagText);
      break;
    
    case '!':
      // Ignore comments
      break;
    
    case '=':
      util.puts("Changing tag: " + content)
      this.setTag(content.split(' '));
    this.appendMultiContent(tagText);
      break;
    
    case '#':
    case '^':
    this.appendMultiContent(tagText);
      var type = sigil === '#' ? 'section' : 'inverted_section',
          block = ['multi'];
      
      this.tokens.push(['mustache', type, content, '', block]);
      this.sections.push([content, this.tokens]);
      this.tokens = block;
      break;
    
    case '/':
      var res    = this.sections.pop() || [],
          name   = res[0],
          tokens = res[1];
      
      this.tokens = tokens;
      if (!name) {
        throw new Error('Closing unopened ' + name);
      } else if (name !== content) {
        throw new Error("Unclosed section " + name);
      }
    this.appendMultiContent(tagText);
      break;
    }
    
    this.buffer = remainder;
    this.state  = 'static';
  }
}


//
// Used to escape RegExp strings
//
function e(text) {
  // thank you Simon Willison
  if(!arguments.callee.sRE) {
    var specials = [
      '/', '.', '*', '+', '?', '|',
      '(', ')', '[', ']', '{', '}', '\\'
    ];
    arguments.callee.sRE = new RegExp(
      '(\\' + specials.join('|\\') + ')', 'g'
    );
  }
  
  return text.replace(arguments.callee.sRE, '\\$1');
}
