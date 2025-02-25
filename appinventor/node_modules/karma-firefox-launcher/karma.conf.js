module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['mocha'],
    files: [
      'test/*.spec.js'
    ],
    exclude: [],
    preprocessors: {},
    reporters: ['dots'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['Firefox', 'FirefoxHeadless'],
    concurrency: Infinity,
    plugins: [
      require.resolve('./'),
      'karma-mocha'
    ]
  })
}
