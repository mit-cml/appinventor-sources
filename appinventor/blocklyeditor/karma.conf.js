module.exports = function(config) {
  config.set({
    basePath: '',
    autoWatch: true,
    frameworks: ['closure', 'mocha', 'chai'],
    files: [
      {pattern: '../lib/closure-library/closure/goog/base.js'},
      {pattern: 'tests/com/google/appinventor/mocha/setup.js'},
      {pattern: 'tests/com/google/appinventor/mocha/*.js'},
      {pattern: 'src/**/*.js', included: false},
      {pattern: '../lib/blockly/core/*.js', included: false},
      {pattern: '../lib/blockly/msg/js/*.js', included: false},
      {pattern: '../lib/closure-library/closure/goog/deps.js', included: false, served: false},
      {pattern: '../lib/closure-library/closure/goog/**/*.js', included: false}
      // {pattern: 'tests/deps.js', included: false, served: false}
    ],
    plugins: [
      'karma-coverage',
      'karma-mocha',
      'karma-chai',
      'karma-closure',
      'karma-phantomjs-launcher',
      'karma-junit-reporter',
      'karma-html-reporter'
    ],
    browsers: ['PhantomJS'],
    reporters: ['progress', 'html', 'junit', 'coverage'],
    preprocessors: {
      'tests/com/google/appinventor/mocha/*.js': ['closure'],
      'src/**/*.js': ['closure', 'coverage'],
      '../lib/blockly/core/*.js': ['closure'],
      '../lib/blockly/msg/js/*.js': ['closure'],
      '../lib/closure-library/closure/goog/deps.js': ['closure-deps']
    },
    singleRun: true,
    coverageReporter: {
      dir: 'reports/coverage',
      reporters: [
        { type: 'html', subdir: 'html' },
        { type: 'lcovonly', subdir: 'lcov' },
        { type: 'cobertura', subdir: 'cobertura' }
      ],
      includeAllSources: true
    },
    junitReporter: {
      outputDir: 'reports/raw',
      outputFile: 'TEST-MochaTests.xml',
      useBrowserName: false,
      suite: 'com.google.appinventor.blocklyeditor.MochaTests'
    },
    htmlReporter: {
      outputDir: 'reports/karma_html'
    },
    client: {
      mocha: {
        ui: 'tdd'
      }
    }
  });
};
