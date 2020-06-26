var BUFFER_LENGTH = 1024 * 8;
var MAX_STACK_SIZE = 100;

var parser = require('./parser');
var nextTick = (typeof setImmediate == 'function') ? setImmediate : process.nextTick;

exports.render = render;


function render(tokens, context, partials, stream, callback) {
  if (!Array.isArray(context)) {
    context = [context];
  }

  return _render(tokens, context, partials, stream, callback);
}

function _render(tokens, context, partials, stream, callback) {
  if (tokens[0] !== 'multi') {
    throw new Error('Mu - WTF did you give me? I expected mustache tokens.');
  }
  
  var i = 1
    , stackSize = 0;
  
  function next() {
    try {
    
      if (stream.paused) {
        stream.once('resumed', function () {
          nextTick(next);
        });
        return;
      }

      if (++stackSize % MAX_STACK_SIZE == 0) {
        nextTick(next);
        return;
      }
    
      var token = tokens[i++];
    
      if (!token) {
        return callback ? callback() : true;
      }
    
      switch (token[0]) {
      case 'static':
        stream.emit('data', token[2]);
        return next();
    
      case 'mustache':    
        switch (token[1]) {
        case 'utag': // Unescaped Tag
          stream.emit('data', s(normalize(context, token[2]),stream,token[2]));
          return next();
        
        case 'etag': // Escaped Tag
          stream.emit('data', escape(s(normalize(context, token[2]),stream,token[2])));
          return next();
      
        case 'section':
          var res = normalize(context, token[2], token[3]);
          if (res) {
            return section(context, token[2], res, token[4], partials, stream, next);
          } else {
            return next();
          }
        
        case 'inverted_section':
          var res = normalize(context, token[2], token[3]);
          if (!res || res.length === 0) {
            return section(context, token[2], true, token[4], partials, stream, next);
          } else {
            return next();
          }
        
        case 'partial':
          var partial = partials[token[2]];
          // console.log(require('util').inspect(partials));
          if (partial) {
            return render(partial[0].tokens, context, partials, stream, next);
          } else {
            return next();
          }
        }
    
      }
    
    } catch (err) {
      stream.emit('error', err);
      next();
    }
  }
  
  next();
}

function s(val,stream,token) {
  if (val === null || typeof val === 'undefined') {
    if(stream && token){
      stream.emit('warn',{token:token,category:'token.undefined'});
    }
    return '';
  } else {
    return val.toString();
  }
}

function escape(string) {
  return string.replace(/[&<>"]/g, escapeReplace);
}

function normalize(context, name, body) {
  var val = walkToFind(context, name);
  
  if (typeof(val) === 'function') {
    val = val.call(smashContext(context), body);
  }
  
  return val;
}

function walkToFind(context, name) {
  var i = context.length;

  while (i--) {
    var result = contextLevelContains(context[i], name);

    if (result !== undefined) {
      return result;
    }
  }

  return undefined;
}

function contextLevelContains(context, fullPath) {
  var pathParts = fullPath.split('.');
  var obj = context;

  for (var i = 0; i < pathParts.length; i++) {
    var part = pathParts[i];

    if (typeof obj == 'object' && part in obj) {
      obj = obj[part];
    } else {
      obj = undefined;
      break;
    }
  }

  return obj;
}

// TODO: if Proxy, make more efficient
// TODO: cache?
function smashContext(context) {
  var obj = {};

  for (var i = 0; i < context.length; i++) {
    var level = context[i];

    if (level instanceof Date) {
      obj.__date = level;
    } else {
      for (var k in level) {
        obj[k] = level[k];
      }
    }
  }

  return obj;
}

function section(context, name, val, tokens, partials, stream, callback) {
  if (val instanceof Array) {
    var i = 0;
    
    (function next() {
      var item = val[i++];
      
      if (item) {
        context.push(item);
        _render(tokens, context, partials, stream, function () {
          context.pop();

          if (i % MAX_STACK_SIZE == 0) {
            return nextTick(next);
          } else {
            next();
          }
        });
      } else {
        callback();
      }
      
    }());
    
    return;
  }
  
  if (typeof val === 'object') {
    context.push(val);
    _render(tokens, context, partials, stream, function () {
      context.pop();
      callback();
    });
    return;
  }
  
  if (val) {
    return _render(tokens, context, partials, stream, callback);
  }
  
  return callback();
}


//
//
//
function findInContext(context, key) {
  var i = context.length;
  while (i--) {
    if (context[i][key]) {
      return context[i][key];
    }
  }

  return undefined;
}


//
//
//
function escapeReplace(char) {
  switch (char) {
    case '<': return '&lt;';
    case '>': return '&gt;';
    case '&': return '&amp;';
    case '"': return '&quot;';
    default: return char;
  }
}
