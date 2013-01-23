var fs = require('fs');
var path = fs.absolute('.');

if (endsWith(path, 'appinventor')) {
  path = path + "/blocklyeditor";
}

var formJson = fs.read(path + '/tests/com/google/appinventor/blocklyeditor/data/ifThenBlock/Screen1.scm');
formJson = formJson.substring(9, formJson.length-2);
var blocks = fs.read(path + '/tests/com/google/appinventor/blocklyeditor/data/ifThenBlock/Screen1.bky');

var page = require('webpage').create();
page.onConsoleMessage = function (msg) { console.log(msg); };
page.onError = function (msg, trace) {
  console.log(msg);
  trace.forEach(function(item) {
    console.log('  ', item.file, ':', item.line);
  })
}
page.open(path + '/src/demos/yail/testing_index.html', function(status) {
  var passed = page.evaluate(function(){

    processForm(arguments[0]);
    processBlocks(arguments[1]);

    var generatedYail = toAppYail();
    generatedYail = generatedYail.replace(/\s/g, '');

    var expected = '(define-event Screen1 Initialize()  (set-this-form)  (if #t (begin (call-yail-primitive random-set-seed (*list-for-runtime* 0)  \'( number)  \"random set seed\")  ) )  )';
    expected = expected.replace(/\s/g, '');

    return !!~('' + generatedYail).indexOf(expected);

  }, formJson, blocks);
  
  console.log(passed);

  phantom.exit();
});

function endsWith(str, suffix) {
  return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

