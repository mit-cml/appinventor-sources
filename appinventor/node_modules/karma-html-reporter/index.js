var os = require('os');
var path = require('path');
var fs = require('fs');
var _ = require('lodash');
var mu = require('mu2');


var HtmlReporter = function(baseReporterDecorator, config, emitter, logger, helper, formatError) {
	config = config || {};
	var pkgName = config.suite;
	var log = logger.create('reporter.html');

	var browserResults = {};
	var allMessages = [];
	var pendingFileWritings = 0;
	var env = {}; // for preserveDescribeNesting
	var fileWritingFinished = function() {
	};

	baseReporterDecorator(this);

	this.adapters = [function(msg) {
		allMessages.push(msg);
	}];

	this.onRunStart = function() {
		allMessages = [];
	};

	this.onBrowserStart = function (browser){
		var timestamp = (new Date()).toISOString().substr(0, 19);
		browserResults[browser.id] = {
			browserName : browser.name,
			browserFullName : browser.fullName,
			'package' : pkgName,
			timestamp : timestamp,
			hostname : os.hostname(),
			suites : {},
			sections: null, // for preserveDescribeNesting
		};
		
		env[browser.id] = { // preserveDescribeNesting stuff
			currentSuiteName: [], // current set of `describe` names as an array of strings
			currentIndent: 0, // in 'levels'
			sectionIndex: -1, // current top-level `describe` in processing 
		};
	};

	this.onBrowserComplete = function(browser) {
		var browserResult = browserResults[browser.id];
		if(browserResult != undefined){
			browserResult.results = browser.lastResult;
			browserResult.output = allMessages;
		}
	};

	this.onRunComplete = function(browsers) {
		
		pendingFileWritings = browsers.length;
		browsers.forEach(function(browser) {
			var results = browserResults[browser.id];

			if (results == undefined) {
				if (!--pendingFileWritings) {
					fileWritingFinished();
				}
				return;
			}
			prepareResults(results);
			
			// whether report name should go into file name istead of a folder
			var namedFiles = config.namedFiles || false;
			
			// whether to select the Failures tab automatically 
			results.focusOnFailures = config.focusOnFailures !== false && results.results.hasFailed;
			
			var outputDir = config.outputDir || 'karma_html';
			var reportName = config.reportName || config.middlePathDir || results.browserName;
			results.pageTitle = config.pageTitle || reportName; // inject into head 
			if (config.urlFriendlyName) reportName = reportName.replace(/ /g, '_');
			var reportFile = outputDir + '/' + reportName + (namedFiles ? '.html' : '/index.html');
			var writeStream;
			
			results.date = new Date().toDateString();
			
			var templatePath = config.templatePath || __dirname + "/jasmine_template.html";
			var template = mu.compileAndRender(templatePath, results);
			template.pause();
			
			helper.mkdirIfNotExists(path.dirname(reportFile), function() {

				writeStream = fs.createWriteStream(reportFile);

				writeStream.on('error', function(err) {
					log.warn('Cannot write HTML Report\n\t' + err.message);
				});

				writeStream.on('finish', function() {
					log.debug('HTML report written to "%s".', reportFile);
					if (!--pendingFileWritings) {
						fileWritingFinished();
					}
					template = null;
				});

				template.pipe(writeStream);
				template.resume();
			});

		});
	}; //HtmlReporter


	this.specSuccess = this.specSkipped = this.specFailure = function(browser, result) {
		var suite = getOrCreateSuite(browser, result);
		suite.specs.push(result);
	};

	// wait for writing all the xml files, before exiting
	emitter.on('exit', function(done) {
		if (pendingFileWritings) {
			fileWritingFinished = done;
		} else {
			done();
		}
	});
	
	function compareSuiteNames(current, next) { // simple array comparison for preserveDescribeNesting
		if (current.length !== next.length) return false;
		for (var i= 0, l= current.length; i < l; i++) {
			if (current[i] !== next[i]) return false;
		}
		return true;
	}

	function getOrCreateSuite(browser, result) {
		var suites = browserResults[browser.id].suites;
		
		if (config.preserveDescribeNesting) { // generate sections
			if (!browserResults[browser.id].sections) browserResults[browser.id].sections = [];
			var sections = browserResults[browser.id].sections;
			var e = env[browser.id];
			
			var lastIndent = e.currentIndent;
			var describeAdded = false; // whether new `describe` line was added
			
			if (!compareSuiteNames(e.currentSuiteName, result.suite)) { // combined `describe` changed
				e.currentIndent += result.suite.length - e.currentSuiteName.length - 1;
				
				if (result.suite.length > e.currentSuiteName.length ||
						!compareSuiteNames(_.first(e.currentSuiteName, result.suite.length), result.suite)) {
					
					if (!e.currentIndent) e.sectionIndex++; // opening a new section 
					if (!sections[e.sectionIndex]) sections[e.sectionIndex] = { lines: [], 
							passed: 0, failed: 0, skipped: 0, folded: config.foldAll ? ' folded' : '' };
					
					sections[e.sectionIndex].lines.push({ // `describe` line
						className: 'description'+ (!e.currentIndent ? ' section-starter' : ' br'),
						style: 'margin-left:'+ (e.currentIndent * 14) + 'px',
						value: result.suite[e.currentIndent],
					});
					describeAdded = true;
				}
				e.currentSuiteName = result.suite;
				e.currentIndent++;
			}
			
			// in case the list start with iit - wrap it into an anonymous suite
			if (!sections[e.sectionIndex] && e.sectionIndex === -1) {
				e.sectionIndex = 0;
				sections[e.sectionIndex] = { lines: [], 
						passed: 0, failed: 0, skipped: 0, folded: config.foldAll ? ' folded' : '' };
				sections[e.sectionIndex].lines.push({ // `describe` line
					className: 'description section-starter',
					style: 'margin-left:'+ (e.currentIndent * 14) + 'px',
					value: 'Anonymous Suite',
				});
				e.currentIndent = 1;
			}
			sections[e.sectionIndex].lines.push({ // spec line 
				className: 'specSummary '+ 
									 (result.skipped ? 'skipped' : result.success ? 'passed' : 'failed') +
									 (e.currentIndent < lastIndent && !describeAdded ? ' br' : ''),
				style: 'margin-left:'+ (e.currentIndent * 14) + 'px',
				value: result.description,
			});
			sections[e.sectionIndex][result.skipped ? 'skipped' : result.success ? 'passed' : 'failed']++;
		}

		var suiteKey = result.suite.join(" ");
		if (suites[suiteKey] === undefined) {
			return suites[suiteKey] = { specs : [] };
		}
		else {
			return suites[suiteKey];
		}
	}

	function prepareResults(browser) {
		browser.foldAll = (!!config.foldAll).toString(); // pass var to the template 
		if (config.preserveDescribeNesting && browser.sections) { // generate section totals 
			var k = ['passed', 'failed', 'skipped'], i, n;
			for (i = 0; i < browser.sections.length; i++) {
				browser.sections[i].lines[0].totals = [];
				for (n = 0; n < k.length; n++) {
					if (browser.sections[i][ k[n] ]) browser.sections[i].lines[0].totals.push({
						result: k[n],
						count: browser.sections[i][ k[n] ],
					});
				}
			}
		}
		
		browser.suites = suitesToArray(browser.suites);
		var results = browser.results;
		results.hasSuccess = results.success > 0;
		results.hasFailed = results.failed > 0;
		results.hasSkipped = results.skipped > 0;
		browser.failedSuites = getFailedSuites(browser.suites);
		return browser;
	}

	function suitesToArray(suites) {
		return _.map(suites, function(suite, suiteName) {
			var specs = transformSpecs(suite.specs);
			var overallState = getOverallState(specs);
			return { name : suiteName, state : overallState, specs : transformSpecs(suite.specs)};
		});
	}

	function transformSpecs(specs) {
		return _.map(specs, function(spec) {
			var newSpec = _.clone(spec);
			if (spec.skipped) {
				newSpec.state = "skipped";
			}
			else if (spec.success) {
				newSpec.state = "passed";
			}
			else {
				newSpec.state = "failed";
			}
			return newSpec;
		});
	}

	function getOverallState(specs) {
		if (_.any(specs, function(spec) {
			return spec.state === "failed";
		})) {
			return "failed";
		}
		else {
			return "passed";
		}
	}

	function getFailedSuites(suites) {
		return _.filter(suites,function(suite) {
			return suite.state === "failed";
		}).map(function(suite) {
			 var newSuite = _.clone(suite);
			 newSuite.specs = getFailedSpecs(suite.specs);
			 return newSuite;
		 });
	}

	function getFailedSpecs(specs) {
		return _.filter(specs, function(spec) {
			return spec.state === "failed";
		});
	}


};

HtmlReporter.$inject = ['baseReporterDecorator', 'config.htmlReporter', 'emitter', 'logger', 'helper', 'formatError'];

// PUBLISH DI MODULE
module.exports = {
	'reporter:html' : ['type', HtmlReporter]
};
