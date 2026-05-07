# karma-firefox-launcher

[![js-standard-style](https://img.shields.io/badge/code%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/karma-runner/karma-firefox-launcher)
[![npm version](https://img.shields.io/npm/v/karma-firefox-launcher.svg?style=flat-square)](https://www.npmjs.com/package/karma-firefox-launcher) [![npm downloads](https://img.shields.io/npm/dm/karma-firefox-launcher.svg?style=flat-square)](https://www.npmjs.com/package/karma-firefox-launcher)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

[![Build Status](https://img.shields.io/travis/karma-runner/karma-firefox-launcher/master.svg?style=flat-square)](https://travis-ci.org/karma-runner/karma-firefox-launcher) [![Dependency Status](https://img.shields.io/david/karma-runner/karma-firefox-launcher.svg?style=flat-square)](https://david-dm.org/karma-runner/karma-firefox-launcher) [![devDependency Status](https://img.shields.io/david/dev/karma-runner/karma-firefox-launcher.svg?style=flat-square)](https://david-dm.org/karma-runner/karma-firefox-launcher#info=devDependencies)

> Launcher for Mozilla Firefox.

## `karma-firefox-launcher` is deprecated and is not accepting new features or general bug fixes.

See [deprecation notice for `karma`](https://github.com/karma-runner/karma#karma-is-deprecated-and-is-not-accepting-new-features-or-general-bug-fixes).

[Web Test Runner](https://modern-web.dev/docs/test-runner/overview/),
[`jasmine-browser-runner`](https://github.com/jasmine/jasmine-browser-runner),
and [`playwright-test`](https://github.com/hugomrdias/playwright-test) provide
browser-based unit testing solutions which can be used as a direct alternative.

## Installation

The easiest way is to keep `karma-firefox-launcher` as a devDependency in your `package.json`.

You can simple do it by:

```bash
npm install karma-firefox-launcher --save-dev
```

## Configuration

```js
// karma.conf.js
module.exports = function (config) {
  config.set({
    plugins: [require("karma-firefox-launcher")],
    browsers: [
      "Firefox",
      "FirefoxDeveloper",
      "FirefoxAurora",
      "FirefoxNightly",
    ],
  });
};
```

You can pass list of browsers as a CLI argument too:

```bash
karma start --browsers Firefox,Chrome
```

To run Firefox in headless mode, append `Headless` to the version name, e.g. `FirefoxHeadless`, `FirefoxNightlyHeadless`.

### Environment variables

You can specify the location of the Firefox executable using the following
environment variables:

- `FIREFOX_BIN` (for browser `Firefox` or `FirefoxHeadless`)
- `FIREFOX_DEVELOPER_BIN` (for browser `FirefoxDeveloper` or
  `FirefoxDeveloperHeadless`)
- `FIREFOX_AURORA_BIN` (for browser `FirefoxAurora` or `FirefoxAuroraHeadless`)
- `FIREFOX_NIGHTLY_BIN` (for browser `FirefoxNightly` or
  `FirefoxNightlyHeadless`)

### Custom Firefox location

In addition to Environment variables you can specify location of the Firefox executable in a custom launcher:

```js
browsers: ['Firefox68', 'Firefox78'],

customLaunchers: {
    Firefox68: {
        base: 'Firefox',
        name: 'Firefox68',
        command: '<path to FF68>/firefox.exe'
    },
    Firefox78: {
        base: 'Firefox',
        name: 'Firefox78',
        command: '<path to FF78>/firefox.exe'
    }
}
```

### Custom Preferences

To configure preferences for the Firefox instance that is loaded, you can specify a custom launcher in your Karma
config with the preferences under the `prefs` key:

```js
browsers: ['FirefoxAutoAllowGUM'],

customLaunchers: {
    FirefoxAutoAllowGUM: {
        base: 'Firefox',
        prefs: {
            'media.navigator.permission.disabled': true
        }
    }
}
```

### Loading Firefox Extensions

If you have extensions that you want loaded into the browser on startup, you can specify the full path to each
extension in the `extensions` key:

```js
browsers: ['FirefoxWithMyExtension'],

customLaunchers: {
    FirefoxWithMyExtension: {
        base: 'Firefox',
        extensions: [
          path.resolve(__dirname, 'helpers/extensions/myCustomExt@suchandsuch.xpi'),
          path.resolve(__dirname, 'helpers/extensions/myOtherExt@soandso.xpi')
        ]
    }
}
```

**Please note**: the extension name must exactly match the 'id' of the extension. You can discover the 'id' of your
extension by extracting the .xpi (i.e. `unzip XXX.xpi`) and opening the install.RDF file with a text editor, then look
for the `em:id` tag under the `Description` tag. If your extension manifest looks something like this:

```xml
<?xml version="1.0" encoding="utf-8"?>
   <RDF xmlns="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:em="http://www.mozilla.org/2004/em-rdf#">
  <Description about="urn:mozilla:install-manifest">
    <em:id>myCustomExt@suchandsuch</em:id>
    <em:version>1.0</em:version>
    <em:type>2</em:type>
    <em:bootstrap>true</em:bootstrap>
    <em:unpack>false</em:unpack>

    [...]
  </Description>
</RDF>
```

Then you should name your extension `myCustomExt@suchandsuch.xpi`.

---

For more information on Karma see the [homepage].

[homepage]: https://karma-runner.github.io
