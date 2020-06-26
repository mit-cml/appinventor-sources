var fs     = require('fs'),
    path   = require('path'),
    mu     = require('../lib/mu');

mu.root = path.join(__dirname, 'examples');

var js   = fs.readFileSync(path.join(mu.root, 'complex.js')).toString(),
    text = fs.readFileSync(path.join(mu.root, 'complex.txt')).toString();

js = eval('(' + js + ')');

var RUNS = parseInt(process.argv[2] || "1000000");

mu.compile('complex.html', function (err, compiled) {
  if (err) {
    throw err;
  }
  
  //var buffer = '';
  //mu.render('complex.html', js)
  //  .on('data', function (c) { buffer += c.toString(); })
  //  .on('end', function () { console.log(buffer); });
  
  mu.render('complex.html', js).pipe(process.stdout);

  var i = 0, d = new Date();
  
  (function go() {
    if (i++ < RUNS) {
      mu.render('complex.html', js).on('end', function () { go(); });
    }
  }())
  
  process.addListener('exit', function () {
    require('util').debug("Time taken: " + ((new Date() - d) / 1000) + "secs");
  });
});
