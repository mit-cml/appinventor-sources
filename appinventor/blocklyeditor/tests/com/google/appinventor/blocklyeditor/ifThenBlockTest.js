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

    var app_generatedYail = toAppYail();
    app_generatedYail = app_generatedYail.replace(/\s/g, '');

    var app_expected = '#| $Source $Yail |# (define-form fakepackagename Screen1) (require <com.google.youngandroid.runtime>) ;;; Screen1 (do-after-form-creation (set-and-coerce-property! \'Screen1 \'Title "Screen1" \'text) ) (define-event Screen1 Initialize()(set-this-form) (if #t (begin (call-yail-primitive random-set-seed (*list-for-runtime* 0) \'(number) "random set seed")))) (init-runtime)';
    app_expected = app_expected.replace(/\s/g, '');

    var repl_generatedYail = toReplYail();
    repl_generatedYail = repl_generatedYail.replace(/\s/g, '');

    var repl_expected = '(begin (clear-current-form) ;;; Screen1 (do-after-form-creation (set-and-coerce-property! \'Screen1 \'Title "Screen1" \'text) ) (define-event Screen1 Initialize()(set-this-form) (if #t (begin (call-yail-primitive random-set-seed (*list-for-runtime* 0) \'(number) "random set seed")))) (init-runtime) (call-Initialize-of-components \'Screen1 \'Screen1) )';

    repl_expected = repl_expected.replace(/\s/g, '');

    return (!!~('' + app_generatedYail).indexOf(app_expected)) && (!!~('' + repl_generatedYail).indexOf(repl_expected)) && (repl_expected.length == repl_generatedYail.length)
      && (app_expected.length == app_generatedYail.length);

  }, formJson, blocks);

  console.log(passed);

  phantom.exit();
});

function endsWith(str, suffix) {
  return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

