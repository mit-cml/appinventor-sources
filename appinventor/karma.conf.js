// Karma configuration
// Generated on Tue Nov 12 2024 16:22:05 GMT-0500 (Eastern Standard Time)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',


    // frameworks to use
    // available frameworks: https://www.npmjs.com/search?q=keywords:karma-adapter
    frameworks: ['mocha', 'chai'],


    // list of files / patterns to load in the browser
    files: [
        'blocklyeditor/tests/index.js',
      'build/blocklyeditor/msg/messages.nocache.js',
      'lib/closure-library/closure/goog/base.js',
      'build/blocklyeditor/blockly-all.js',
      'build/blocklyeditor/component-types.js',
      'appengine/war/static/js/scroll-options-5.0.11.min.js',
      'appengine/war/static/js/workspace-search.min.js',
      'appengine/war/static/js/block-dynamic-connection-0.6.0.min.js',
      'appengine/war/static/js/workspace-multiselect-0.1.14-beta2.min.js',
      'blocklyeditor/tests/testCommon.js',
      'blocklyeditor/tests/com/google/appinventor/mocha/*.js',
      'blocklyeditor/build/javascript/*.js',
      'blocklyeditor/tests/com/google/appinventor/blocklyeditor/*.js',
    ],


    // list of files / patterns to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://www.npmjs.com/search?q=keywords:karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://www.npmjs.com/search?q=keywords:karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,


    // start these browsers
    // available browser launchers: https://www.npmjs.com/search?q=keywords:karma-launcher
    browsers: ['FirefoxHeadless'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    // singleRun: false,

    // Concurrency level
    // how many browser instances should be started simultaneously
    concurrency: Infinity,

    customLaunchers: {
      FirefoxHeadless: {
        base: 'Firefox',
        flags: ['-headless'],
      },
    },

    client: {
      mocha: {
        ui: 'tdd'
      }
    },
  })
}
