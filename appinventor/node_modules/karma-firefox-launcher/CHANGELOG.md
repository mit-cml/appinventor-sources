## [2.1.3](https://github.com/karma-runner/karma-firefox-launcher/compare/v2.1.2...v2.1.3) (2024-03-03)

## [2.1.2](https://github.com/karma-runner/karma-firefox-launcher/compare/v2.1.1...v2.1.2) (2021-11-02)


### Bug Fixes

* launcher does not kill firefox.exe on WSL ([3954ad4](https://github.com/karma-runner/karma-firefox-launcher/commit/3954ad4a23bbc1b5886d33c2c9bf39161a9c5f3c)), closes [/github.com/karma-runner/karma-firefox-launcher/issues/101#issuecomment-891850143](https://github.com//github.com/karma-runner/karma-firefox-launcher/issues/101/issues/issuecomment-891850143)

## [2.1.1](https://github.com/karma-runner/karma-firefox-launcher/compare/v2.1.0...v2.1.1) (2021-06-02)


### Bug Fixes

* update name of Developer Edition on macOS ([a0b5e58](https://github.com/karma-runner/karma-firefox-launcher/commit/a0b5e5858a2503c54a429904b133849f90514d6d))

# [2.1.0](https://github.com/karma-runner/karma-firefox-launcher/compare/v2.0.0...v2.1.0) (2020-11-03)


### Features

* provide a way to configure location of executable in customLauncher (fix [#125](https://github.com/karma-runner/karma-firefox-launcher/issues/125)) ([c53efcc](https://github.com/karma-runner/karma-firefox-launcher/commit/c53efcc5f0abf72c0213f826d2b147d01241a39c))

# [2.0.0](https://github.com/karma-runner/karma-firefox-launcher/compare/v1.3.0...v2.0.0) (2020-10-20)


### Bug Fixes

* Bypass WSL check if Firefox is present in WSL environment ([23a5d10](https://github.com/karma-runner/karma-firefox-launcher/commit/23a5d10baeba016d4c30a7378a795de4561f1160)), closes [#107](https://github.com/karma-runner/karma-firefox-launcher/issues/107)
* Update is-wsl to v2.2.0 to detect docker under WSL ([#116](https://github.com/karma-runner/karma-firefox-launcher/issues/116)) ([c585393](https://github.com/karma-runner/karma-firefox-launcher/commit/c58539341897ebbaf9ada80f3fb9a8818046b1b3))


### Features

* Make Node 10 minimum required version ([480dafd](https://github.com/karma-runner/karma-firefox-launcher/commit/480dafd7d0055ed55af211af301754a9e3972ab5)), closes [#118](https://github.com/karma-runner/karma-firefox-launcher/issues/118)


### BREAKING CHANGES

* Changed minimum required version of node.js from 8 to 10.

Node 8 EOL was 2019-12-31.

# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## [1.3.0](https://github.com/karma-runner/karma-firefox-launcher/compare/v1.2.0...v1.3.0) (2020-01-08)


### Bug Fixes

* Check that wsl path exists before using wslpath to convert it ([#105](https://github.com/karma-runner/karma-firefox-launcher/issues/105)) ([1eb7e1b](https://github.com/karma-runner/karma-firefox-launcher/commit/1eb7e1b))

## [1.2.0](https://github.com/karma-runner/karma-firefox-launcher/compare/v1.1.0...v1.2.0) (2019-08-09)


### Bug Fixes

* Add -wait-for-browser ([540c1dd](https://github.com/karma-runner/karma-firefox-launcher/commit/540c1dd))
* Look for other paths for Firefox Nightly on Windows and Mac ([6377ee3](https://github.com/karma-runner/karma-firefox-launcher/commit/6377ee3))


### Features

* **headless:** add enable remote debugging by default ([0e37f76](https://github.com/karma-runner/karma-firefox-launcher/commit/0e37f76))
* Add support for running Windows Firefox from WSL ([b4e260e](https://github.com/karma-runner/karma-firefox-launcher/commit/b4e260e))

<a name="1.1.0"></a>
# [1.1.0](https://github.com/karma-runner/karma-firefox-launcher/compare/v1.0.1...v1.1.0) (2017-12-07)


### Bug Fixes

* safe handling of missing env variables ([98a4ada](https://github.com/karma-runner/karma-firefox-launcher/commit/98a4ada)), closes [#67](https://github.com/karma-runner/karma-firefox-launcher/issues/67)


### Features

* support Firefox headless ([a1fc1c8](https://github.com/karma-runner/karma-firefox-launcher/commit/a1fc1c8))



<a name="1.0.1"></a>
## [1.0.1](https://github.com/karma-runner/karma-firefox-launcher/compare/v1.0.0...v1.0.1) (2017-03-04)


### Bug Fixes

* **windows:** change getFirefoxExe function to allow running on win64 ([a332915](https://github.com/karma-runner/karma-firefox-launcher/commit/a332915))
* **windows:** change getFirefoxExe function to find exe on other drive ([3322a61](https://github.com/karma-runner/karma-firefox-launcher/commit/3322a61))
* disable multi-process firefox ([9f28aa9](https://github.com/karma-runner/karma-firefox-launcher/commit/9f28aa9))
* only use $HOME environmental variable if it exists ([3ffa514](https://github.com/karma-runner/karma-firefox-launcher/commit/3ffa514))
