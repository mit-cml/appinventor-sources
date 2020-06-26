var assert = require('assert'),
    fs     = require('fs'),
    path   = require('path'),
    mu     = require('../lib/mu');

mu.root = path.join(__dirname, 'examples');

[
  'tenthousand',
  'boolean',
  'carriage_return',
  'comments',
  'complex',
  'date',
  'deep_partial',
  // 'delimiters',
  'error_not_found',
  'escaped',
  'hash_instead_of_array',
  'inverted',
  'partial',
  'recursion_with_same_names',
  'reuse_of_enumerables',
  'simple',
  'dot_notation',
  'twice',
  'two_in_a_row',
  'unescaped'
].forEach(function (name) {
  var js   = fs.readFileSync(path.join(mu.root, name + '.js')).toString(),
      text = fs.readFileSync(path.join(mu.root, name + '.txt')).toString();
  
  js = eval('(' + js + ')');
  
  var buffer = '';

  mu.compileAndRender(name + '.html', js)
    .on('data', function (c) { buffer += c.toString(); })
    .on('end', function () {
      console.log("Testing: " + name);
      assert.equal(buffer, text);
      console.log(name + ' passed\n');
    });
    
  /*
  var js   = fs.readFileSync(path.join(mu.root, name + '.js')).toString(),
      text = fs.readFileSync(path.join(mu.root, name + '.txt')).toString();

  js = eval('(' + js + ')');
  
  mu.compile(name + '.html', function (err, parsed) {
    if (err) {
      throw err;
    }
    
    var buffer = '';
    
    mu.render(parsed, js)
      .on('data', function (c) { buffer += c.toString(); })
      .on('end', function () {
        assert.equal(buffer, text);
        console.log(name + ' passed');
      })
      //.on('error', function (error) {
      //  console.log('Error: ' + error);
      //});
  });
  */
});
