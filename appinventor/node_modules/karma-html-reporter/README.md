# karma-html-reporter

> Reporter that formats results in HTML similar to jasmine.

## Installation

The easiest way is to keep `karma-html-reporter` as a devDependency in your `package.json`.
```json
{
  "devDependencies": {
    "karma": "~0.10",
    "karma-html-reporter": "~0.1"
  }
}
```

You can simply do it by:
```bash
npm install karma-html-reporter --save-dev
```

## Configuration
```js
// karma.conf.js
module.exports = function(config) {
  config.set({
    reporters: ['progress', 'html'],

    // the default configuration
    htmlReporter: {
      outputDir: 'karma_html', // where to put the reports 
      templatePath: null, // set if you moved jasmine_template.html
      focusOnFailures: true, // reports show failures on start
      namedFiles: false, // name files instead of creating sub-directories
      pageTitle: null, // page title for reports; browser info by default
      urlFriendlyName: false, // simply replaces spaces with _ for files/dirs
      reportName: 'report-summary-filename', // report summary filename; browser info by default
      
      
      // experimental
      preserveDescribeNesting: false, // folded suites stay folded 
      foldAll: false, // reports start folded (only with preserveDescribeNesting)
    },
  });
};
```

You can pass list of reporters as a CLI argument too:
```bash
karma start --reporters html,dots
```

## Keyboard Controls

`1` and `2` - switch between Spec List and Failures.
`F` - fold/unfold all suites in `preserveDescribeNesting` mode.

----

For more information on Karma see the [homepage].


[homepage]: http://karma-runner.github.com
