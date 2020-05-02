#!/usr/bin/env node

var util     = require('util'),
    mu       = require('./mu'),
    parser   = require('./mu/parser');

var stdin = process.openStdin();

   
stdin.on('data', function (template) {
  template = template.toString('utf8');
  var parsed = parser.parse(template);

  if (~process.argv.indexOf('--tokens')) {
    console.log(util.inspect(parsed, false, 20));
    return;
  }

  process.argv.forEach(function (arg) {
    if (arg.indexOf('--view=') === 0) {
      try {
        var view = eval('(' + arg.replace('--view=', '') + ')');
      } catch (e) {
        console.log('\nData: ' + arg.replace('--view=', ''));
        throw e;
      }

      mu.renderText(template, view)
        .on('data', function (d) {
          util.print(d);
        });
    }
  });

});



