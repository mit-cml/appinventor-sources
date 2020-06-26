# karma-closure [![Build Status](https://travis-ci.org/karma-runner/karma-closure.png?branch=master)](https://travis-ci.org/karma-runner/karma-closure)

> A Karma plugin that resolves [Google Closure](https://developers.google.com/closure/library/) dependencies on the fly.

## Installation

The easiest way is to keep `karma-closure` as a devDependency in your `package.json`.
```json
{
  "devDependencies": {
    "karma": "~0.10",
    "karma-closure": "~0.1"
  }
}
```

## Configuration
```js
// karma.conf.js
module.exports = function(config) {
  config.set({
    frameworks: ['jasmine', 'closure'],
    files: [
      // closure base
      'lib/goog/base.js',

      // included files - tests
      'test/*.js',

      // source files - these are only watched and served
      {pattern: 'js/*.js', included: false},

      // external deps
      {pattern: 'lib/goog/deps.js', included: false, served: false}
    ],

    preprocessors: {
      // tests are preprocessed for dependencies (closure) and for iits
      'test/*.js': ['closure', 'closure-iit'],
      // source files are preprocessed for dependencies
      'js/*.js': ['closure'],
      // external deps
      'lib/goog/deps.js': ['closure-deps']
    }
  });
};
```

For an example project, check out [./test-app/](/tree/master/test-app)


## IIT preprocessor
When using `iit` or `ddescribe`, Jasmine only executes these tests (resp. the patched version that comes with Karma). But still, all the tests (even those that are not actually executed) are shipped to the browser, parsed and the definition of the tests is executed. On a huge project, this can easily take seconds.

The `iit` preprocessor looks for test files that contain `iit` and only ships these tests (and their dependencies), which can significantly speed up running the tests during the development !


----

For more information on Karma see the [homepage].


[homepage]: http://karma-runner.github.com
