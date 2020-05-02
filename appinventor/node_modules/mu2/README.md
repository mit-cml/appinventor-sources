# Mu - a fast, streaming Node.js Mustache engine

Warning: This version is not API compatible with 0.1.

## Install

I have had some issues with my npm auth and got it in a bit of a rut, so for
now you have to:

    npm install mu2

## Issues

Currently mu does not support changing the tag form ({{ }} to say <% %>).

## Usage

There are a few ways to use mu 0.5. Here is the simplest:
```javascript
var mu = require('mu2'); // notice the "2" which matches the npm repo, sorry..

mu.root = __dirname + '/templates'
mu.compileAndRender('index.html', {name: "john"})
  .on('data', function (data) {
    console.log(data.toString());
  });
```
Here is an example mixing it with the http module:
```javascript
var http = require('http')
  , util = require('util')
  , mu   = require('mu2');

mu.root = __dirname + '/templates';

  http.createServer(function (req, res) {

  var stream = mu.compileAndRender('index.html', {name: "john"});
  stream.pipe(res);

}).listen(8000);
```
Taking that last example here is a little trick to always compile the templates
in development mode (so the changes are immediately reflected).
```javascript
var http = require('http')
  , util = require('util')
  , mu   = require('mu2');

mu.root = __dirname + '/templates';

http.createServer(function (req, res) {

  if (process.env.NODE_ENV == 'DEVELOPMENT') {
    mu.clearCache();
  }

  var stream = mu.compileAndRender('index.html', {name: "john"});
  util.pump(stream, res);

}).listen(8000);
```
## API

    mu.root

      A path to lookup templates from. Defaults to the working directory.


    mu.compileAndRender(String templateName, Object view)

      Returns: Stream

      The first time this function is called with a specific template name, the
      template will be compiled and then rendered to the stream. Subsequent
      calls with the same template name will use a cached version of the compiled
      template to improve performance (a lot).


    mu.compile(filename, callback)

      Returns nil
      Callback (Error err, Any CompiledTemplate)

      This function is used to compile a template. Usually you will not use it
      directly but when doing wierd things, this might work for you. Does not
      use the internal cache when called multiple times, though it does add the
      compiled form to the cache.


    mu.compileText(String name, String template, Function callback)

      Returns nil
      Callback (err, CompiledTemplate)

      Similar to mu.compile except it taks in a name and the actual string of the
      template. Does not do disk io. Does not auto-compile partials either.


    mu.render(Mixed filenameOrCompiledTemplate, Object view)

      Returns Stream

      The brother of mu.compile. This function takes either a name of a template
      previously compiled (in the cache) or the result of the mu.compile step.

      This function is responsible for transforming the compiled template into the
      proper output give the input view data.


    mu.renderText(String template, Object view, Object partials)

      Returns Stream

      Like render, except takes a template as a string and an object for the partials.
      This is not a very performant way to use mu, so only use this for dev/testing.


    mu.clearCache(String templateNameOrNull)

      Clears the cache for a specific template. If the name is omitted, clears all cache.



