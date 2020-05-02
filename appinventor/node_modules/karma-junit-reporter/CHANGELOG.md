<a name="2.0.1"></a>
## [2.0.1](https://github.com/karma-runner/karma-junit-reporter/compare/v2.0.0...v2.0.1) (2019-10-07)



<a name="2.0.0"></a>
# [2.0.0](https://github.com/karma-runner/karma-junit-reporter/compare/v1.2.0...v2.0.0) (2019-10-07)


### Bug Fixes

* allow special characters and emoji in output ([59fd3c6](https://github.com/karma-runner/karma-junit-reporter/commit/59fd3c6))


### Chores

* **travis:** Drop old nodejs version: NO v10 ([#164](https://github.com/karma-runner/karma-junit-reporter/issues/164)) ([bc07177](https://github.com/karma-runner/karma-junit-reporter/commit/bc07177))
* remove support for node 6 ([435cb5e](https://github.com/karma-runner/karma-junit-reporter/commit/435cb5e))


### Features

* support for new XML format (per SonarQube)  ([b0984e0](https://github.com/karma-runner/karma-junit-reporter/commit/b0984e0))


### BREAKING CHANGES

* Drop Support for Node 6, to make it possible to use async/await in karma codebase.
* **travis:** no support for node < 6

* node 10 not supported by libxmljs-mt



<a name="1.2.0"></a>
# [1.2.0](https://github.com/karma-runner/karma-junit-reporter/compare/v1.1.0...v1.2.0) (2016-12-06)


### Bug Fixes

* initialize var suites early  ([e09acb2](https://github.com/karma-runner/karma-junit-reporter/commit/e09acb2))
* release memory held by a testsuite after we're done with it. ([eacf8bb](https://github.com/karma-runner/karma-junit-reporter/commit/eacf8bb))
* remove unnecessary nulling out of `suites` ([4202ee8](https://github.com/karma-runner/karma-junit-reporter/commit/4202ee8)), closes [#99](https://github.com/karma-runner/karma-junit-reporter/issues/99)



<a name="1.1.0"></a>
# [1.1.0](https://github.com/karma-runner/karma-junit-reporter/compare/v1.0.0...v1.1.0) (2016-06-26)


### Bug Fixes

* add defensive checks to safely handle browser disconnects. ([485d87a](https://github.com/karma-runner/karma-junit-reporter/commit/485d87a))



<a name="1.0.0"></a>
# [1.0.0](https://github.com/karma-runner/karma-junit-reporter/compare/v0.4.2...v1.0.0) (2016-05-03)




<a name="0.4.2"></a>
## [0.4.2](https://github.com/karma-runner/karma-junit-reporter/compare/v0.4.1...v0.4.2) (2016-04-08)


### Features

* Add support for additional properties to add to the report section ([b67d234](https://github.com/karma-runner/karma-junit-reporter/commit/b67d234))



<a name="0.4.1"></a>
## [0.4.1](https://github.com/karma-runner/karma-junit-reporter/compare/v0.4.0...v0.4.1) (2016-03-21)


### Bug Fixes

* check if outputFile is set before checking for absolute outputFile ([c8887ac](https://github.com/karma-runner/karma-junit-reporter/commit/c8887ac)), closes [#87](https://github.com/karma-runner/karma-junit-reporter/issues/87)



<a name="0.4.0"></a>
# [0.4.0](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.8...v0.4.0) (2016-03-10)


### Bug Fixes

* Handle absolute outputFile paths in Junit Reporter ([d5dc808](https://github.com/karma-runner/karma-junit-reporter/commit/d5dc808)), closes [#83](https://github.com/karma-runner/karma-junit-reporter/issues/83)
* test name to contain parent suite(s) names ([8e6e202](https://github.com/karma-runner/karma-junit-reporter/commit/8e6e202)), closes [#62](https://github.com/karma-runner/karma-junit-reporter/issues/62)

### Features

* add name and classname formatters ([3f43c51](https://github.com/karma-runner/karma-junit-reporter/commit/3f43c51)), closes [#75](https://github.com/karma-runner/karma-junit-reporter/issues/75)



<a name="0.3.8"></a>
## [0.3.8](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.7...v0.3.8) (2015-10-20)


### Features

* **reporter:** add useBrowserName parameter ([2327234](https://github.com/karma-runner/karma-junit-reporter/commit/2327234))



<a name="0.3.7"></a>
## [0.3.7](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.6...v0.3.7) (2015-10-01)


### Bug Fixes

* **reporter:** bug creating directory when outputFile specified (#67) ([2205f47](https://github.com/karma-runner/karma-junit-reporter/commit/2205f47))



<a name="0.3.6"></a>
## [0.3.6](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.5...v0.3.6) (2015-09-29)


### Bug Fixes

* Bug in building of paths ([1c36509](https://github.com/karma-runner/karma-junit-reporter/commit/1c36509)), closes [#65](https://github.com/karma-runner/karma-junit-reporter/issues/65)



<a name="0.3.5"></a>
## [0.3.5](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.4...v0.3.5) (2015-09-27)


### Bug Fixes

* Add back `outputFile` config ([b4583b5](https://github.com/karma-runner/karma-junit-reporter/commit/b4583b5)), closes [#58](https://github.com/karma-runner/karma-junit-reporter/issues/58)
* Update dependencies ([a1fe8fe](https://github.com/karma-runner/karma-junit-reporter/commit/a1fe8fe))



<a name="0.3.4"></a>
## [0.3.4](https://github.com/karma-runner/karma-junit-reporter/compare/v0.3.3...v0.3.4) (2015-08-28)


### Bug Fixes

* Change how classname is generated ([e4f7ebd](https://github.com/karma-runner/karma-junit-reporter/commit/e4f7ebd))
* **config:** Default `outputDir` to `'.'` ([8fdfb73](https://github.com/karma-runner/karma-junit-reporter/commit/8fdfb73)), closes [#54](https://github.com/karma-runner/karma-junit-reporter/issues/54)

### Features

* Upgrade dependencies to their latest versions ([62b5783](https://github.com/karma-runner/karma-junit-reporter/commit/62b5783))



<a name"0.3.3"></a>
### 0.3.3 (2015-07-23)


#### Bug Fixes

* create browser directory if not exist ([fe14a8d8](https://github.com/karma-runner/karma-junit-reporter/commit/fe14a8d8))


<a name"0.3.2"></a>
### 0.3.2 (2015-07-19)


#### Features

* Allow user to specify filename ([71e0e432](https://github.com/karma-runner/karma-junit-reporter/commit/71e0e432))


<a name"0.3.1"></a>
### 0.3.1 (2015-07-13)


#### Bug Fixes

* handle browser script failure and put the error to browser report ([d5b365ab](https://github.com/karma-runner/karma-junit-reporter/commit/d5b365ab))


<a name"0.3.0"></a>
## 0.3.0 (2015-07-13)


#### Features

* add error tag when browser disconnected ([a1a1e7c4](https://github.com/karma-runner/karma-coverage/commit/a1a1e7c4), closes [#29](https://github.com/karma-runner/karma-coverage/issues/29))


<a name"0.2.2"></a>
### 0.2.2 (2015-07-13)


#### Bug Fixes

* reporter fails when browser times out ([00fbfe4c](https://github.com/karma-runner/karma-coverage/commit/00fbfe4c), closes [#16](https://github.com/karma-runner/karma-coverage/issues/16))


<a name"0.2.1"></a>
### 0.2.1 (2015-07-13)


#### Bug Fixes

* Compatibility with Karma 0.10 ([29d4de18](https://github.com/karma-runner/karma-coverage/commit/29d4de18))
* update to work with Karma 0.11 ([a0676bec](https://github.com/karma-runner/karma-coverage/commit/a0676bec))


#### Features

* normalize the outputFile path ([85a3b871](https://github.com/karma-runner/karma-coverage/commit/85a3b871))
* add a default config ([c6c44dbf](https://github.com/karma-runner/karma-coverage/commit/c6c44dbf))


<a name"0.1.0"></a>
## 0.1.0 (2015-07-13)


<a name"0.0.2"></a>
### 0.0.2 (2015-07-13)


#### Features

* **reporter.junit:** add a 'skipped' tag for skipped testcases ([b552dcb4](https://github.com/karma-runner/karma-coverage/commit/b552dcb4))

