'use strict'

const fs = require('fs')
const path = require('path')
let isWsl = require('is-wsl')
const which = require('which')
const { execSync } = require('child_process')
const { StringDecoder } = require('string_decoder')

const PREFS = [
  'user_pref("browser.shell.checkDefaultBrowser", false);',
  'user_pref("browser.bookmarks.restore_default_bookmarks", false);',
  'user_pref("dom.disable_open_during_load", false);',
  'user_pref("dom.max_script_run_time", 0);',
  'user_pref("dom.min_background_timeout_value", 10);',
  'user_pref("extensions.autoDisableScopes", 0);',
  'user_pref("browser.tabs.remote.autostart", false);',
  'user_pref("browser.tabs.remote.autostart.2", false);',
  'user_pref("extensions.enabledScopes", 15);'
].join('\n')

// NOTE: add 'config.browsers' to get which browsers are started
const $INJECT_LIST = ['baseBrowserDecorator', 'args', 'logger', 'emitter']

// Check if Firefox is installed on the WSL side and use that if it's available
if (isWsl && which.sync('firefox', { nothrow: true })) {
  isWsl = false
}

/**
 * Takes a string from Windows' tasklist.exe with the following arguments:
 * `/FO CSV /NH /SVC` and returns an array of PIDs.
 * @param {string} tasklist Expected to be in the form of:
 * `'"firefox.exe","14972","Console","1","5.084 K"\r\n"firefox.exe","12204","Console","1","221.656 K"'`
 * @returns {string[]} Array of String PIDs. Can be empty.
 */
const extractPids = tasklist => tasklist
  .split(',')
  .filter(x => /^"\d{3,10}"$/.test(x))
  .map(pid => pid.replace(/"/g, ''))

/**
  * Curried function version of safeExecSync with reference to logger
  * in a closure.
  * @param {function} log An instance of logger.create
  * @returns {{(command:string):string}} A closure with reference to logger
  */
const createSafeExecSync = log => command => {
  let output = ''
  try {
    output = String(execSync(command))
  } catch (err) {
    // Something went wrong but we can usually continue.
    // For Windows kill.exe, one common error is trying to kill a PID
    // that no longer exist, which is fine.
    log.debug(String(err))
  }
  return output
}

// Get all possible Program Files folders even on other drives
// inspect the user's path to find other drives that may contain Program Files folders
const getAllPrefixes = function () {
  const drives = []
  const paden = process.env.Path.split(';')
  const re = /^[A-Z]:\\/i
  let pad
  for (let p = 0; p < paden.length; p++) {
    pad = paden[p]
    if (re.test(pad) && drives.indexOf(pad[0]) === -1) {
      drives.push(pad[0])
    }
  }

  const result = []
  const prefixes = [process.env.PROGRAMFILES, process.env['PROGRAMFILES(X86)']]
  let prefix
  for (let i = 0; i < prefixes.length; i++) {
    if (typeof prefixes[i] !== 'undefined') {
      for (let d = 0; d < drives.length; d += 1) {
        prefix = drives[d] + prefixes[i].slice(1)
        if (result.indexOf(prefix) === -1) {
          result.push(prefix)
        }
      }
    }
  }
  return result
}

// Return location of firefox.exe file for a given Firefox directory
// (available: "Mozilla Firefox", "Aurora", "Nightly").
const getFirefoxExe = function (firefoxDirName) {
  if (process.platform !== 'win32' && process.platform !== 'win64') {
    return null
  }

  const firefoxDirNames = Array.prototype.slice.call(arguments)

  for (const prefix of getAllPrefixes()) {
    for (const dir of firefoxDirNames) {
      const candidate = path.join(prefix, dir, 'firefox.exe')
      if (fs.existsSync(candidate)) {
        return candidate
      }
    }
  }

  return path.join('C:\\Program Files', firefoxDirNames[0], 'firefox.exe')
}

const getAllPrefixesWsl = function () {
  const drives = []
  // Some folks configure their wsl.conf to mount Windows drives without the
  // /mnt prefix (e.g. see https://nickjanetakis.com/blog/setting-up-docker-for-windows-and-wsl-to-work-flawlessly)
  //
  // In fact, they could configure this to be any number of things. So we
  // take each path, convert it to a Windows path, check if it looks like
  // it starts with a drive and then record that.
  const re = /^([A-Z]):\\/i
  for (const pathElem of process.env.PATH.split(':')) {
    if (fs.existsSync(pathElem)) {
      const windowsPath = execSync('wslpath -w "' + pathElem + '"').toString()
      const matches = windowsPath.match(re)
      if (matches !== null && drives.indexOf(matches[1]) === -1) {
        drives.push(matches[1])
      }
    }
  }

  const result = []
  // We don't have the PROGRAMFILES or PROGRAMFILES(X86) environment variables
  // in WSL so we just hard code them.
  const prefixes = ['Program Files', 'Program Files (x86)']
  for (const prefix of prefixes) {
    for (const drive of drives) {
      // We only have the drive, and only wslpath knows exactly what they map to
      // in Linux, so we convert it back here.
      const wslPath =
        execSync('wslpath "' + drive + ':\\' + prefix + '"').toString().trim()
      result.push(wslPath)
    }
  }

  return result
}

const getFirefoxExeWsl = function (firefoxDirName) {
  if (!isWsl) {
    return null
  }

  const firefoxDirNames = Array.prototype.slice.call(arguments)

  for (const prefix of getAllPrefixesWsl()) {
    for (const dir of firefoxDirNames) {
      const candidate = path.join(prefix, dir, 'firefox.exe')
      if (fs.existsSync(candidate)) {
        return candidate
      }
    }
  }

  return path.join('/mnt/c/Program Files/', firefoxDirNames[0], 'firefox.exe')
}

const getFirefoxWithFallbackOnOSX = function () {
  if (process.platform !== 'darwin') {
    return null
  }

  const firefoxDirNames = Array.prototype.slice.call(arguments)
  const prefix = '/Applications/'
  const suffix = '.app/Contents/MacOS/firefox'

  let bin
  let homeBin
  for (let i = 0; i < firefoxDirNames.length; i++) {
    bin = prefix + firefoxDirNames[i] + suffix

    if ('HOME' in process.env) {
      homeBin = path.join(process.env.HOME, bin)

      if (fs.existsSync(homeBin)) {
        return homeBin
      }
    }

    if (fs.existsSync(bin)) {
      return bin
    }
  }
}

const makeHeadlessVersion = function (Browser) {
  const HeadlessBrowser = function () {
    Browser.apply(this, arguments)
    const execCommand = this._execCommand
    this._execCommand = function (command, args) {
      // --start-debugger-server ws:6000 can also be used, since remote debugging protocol also speaks WebSockets
      // https://hacks.mozilla.org/2017/12/using-headless-mode-in-firefox/
      execCommand.call(this, command, args.concat(['-headless', '--start-debugger-server 6000']))
    }
  }

  HeadlessBrowser.prototype = Object.create(Browser.prototype, {
    name: { value: Browser.prototype.name + 'Headless' }
  })
  HeadlessBrowser.$inject = Browser.$inject
  return HeadlessBrowser
}

// https://developer.mozilla.org/en-US/docs/Command_Line_Options
const FirefoxBrowser = function (baseBrowserDecorator, args, logger, emitter) {
  baseBrowserDecorator(this)

  const log = logger.create(this.name + 'Launcher')
  const safeExecSync = createSafeExecSync(log)
  let browserProcessPid
  let browserProcessPidWsl = []

  this._getPrefs = function (prefs) {
    if (typeof prefs !== 'object') {
      return PREFS
    }
    let result = PREFS
    for (const key in prefs) {
      result += 'user_pref("' + key + '", ' + JSON.stringify(prefs[key]) + ');\n'
    }
    return result
  }

  this._start = function (url) {
    const self = this
    const command = args.command || this._getCommand()
    const profilePath = args.profile || self._tempDir
    const flags = args.flags || []
    let extensionsDir

    if (Array.isArray(args.extensions)) {
      extensionsDir = path.resolve(profilePath, 'extensions')
      fs.mkdirSync(extensionsDir)
      args.extensions.forEach(function (ext) {
        const extBuffer = fs.readFileSync(ext)
        const copyDestination = path.resolve(extensionsDir, path.basename(ext))
        fs.writeFileSync(copyDestination, extBuffer)
      })
    }

    fs.writeFileSync(path.join(profilePath, 'prefs.js'), this._getPrefs(args.prefs))
    const translatedProfilePath =
      isWsl ? execSync('wslpath -w ' + profilePath).toString().trim() : profilePath

    if (isWsl) {
      log.warn('WSL environment detected: Please do not open Firefox while running tests as it will be killed after the test!')
      log.warn('WSL environment detected: See https://github.com/karma-runner/karma-firefox-launcher/issues/101#issuecomment-891850143')

      browserProcessPidWsl = extractPids(safeExecSync('tasklist.exe /FI "IMAGENAME eq firefox.exe" /FO CSV /NH /SVC'))
      log.debug('Recorded PIDs not to kill:', browserProcessPidWsl)
    }

    // If we are using the launcher process, make it print the child process ID
    // to stderr so we can capture it. Does not work in WSL.
    //
    // https://wiki.mozilla.org/Platform/Integration/InjectEject/Launcher_Process/
    process.env.MOZ_DEBUG_BROWSER_PAUSE = 0
    browserProcessPid = undefined
    self._execCommand(
      command,
      [url, '-profile', translatedProfilePath, '-no-remote', '-wait-for-browser'].concat(flags)
    )

    self._process.stderr.on('data', errBuff => {
      let errString
      if (typeof errBuff === 'string') {
        errString = errBuff
      } else {
        const decoder = new StringDecoder('utf8')
        errString = decoder.write(errBuff)
      }
      const matches = errString.match(/BROWSERBROWSERBROWSERBROWSER\s+debug me @ (\d+)/)
      if (matches) {
        browserProcessPid = parseInt(matches[1], 10)
      }
    })
  }

  if (isWsl) {
    // exit: will run for each browser when all tests has finished
    emitter.on('exit', (done) => {
      const tasklist = extractPids(safeExecSync('tasklist.exe /FI "IMAGENAME eq firefox.exe" /FO CSV /NH /SVC'))
        .filter(pid => browserProcessPidWsl.indexOf(pid) === -1)

      // if this is not the first time 'exit' is called then tasklist is probably empty
      if (tasklist.length > 0) {
        log.debug('Killing the following PIDs:', tasklist)
        const killResult = safeExecSync('taskkill.exe /F ' + tasklist.map(pid => `/PID ${pid}`).join(' ') + ' 2>&1')
        log.debug(killResult)
      }

      return process.nextTick(done)
    })
  }

  this.on('kill', function (done) {
    // If we have a separate browser process PID, try killing it.
    if (browserProcessPid) {
      try {
        process.kill(browserProcessPid)
      } catch (e) {
        // Ignore failure -- the browser process might have already been
        // terminated.
      }
    }

    return process.nextTick(done)
  })
}

FirefoxBrowser.prototype = {
  name: 'Firefox',

  DEFAULT_CMD: {
    linux: isWsl ? getFirefoxExeWsl('Mozilla Firefox') : 'firefox',
    freebsd: 'firefox',
    darwin: getFirefoxWithFallbackOnOSX('Firefox'),
    win32: getFirefoxExe('Mozilla Firefox')
  },
  ENV_CMD: 'FIREFOX_BIN'
}

FirefoxBrowser.$inject = $INJECT_LIST

const FirefoxHeadlessBrowser = makeHeadlessVersion(FirefoxBrowser)

const FirefoxDeveloperBrowser = function () {
  FirefoxBrowser.apply(this, arguments)
}

FirefoxDeveloperBrowser.prototype = {
  name: 'FirefoxDeveloper',
  DEFAULT_CMD: {
    linux: isWsl ? getFirefoxExeWsl('Firefox Developer Edition') : 'firefox',
    darwin: getFirefoxWithFallbackOnOSX('Firefox Developer Edition', 'FirefoxDeveloperEdition', 'FirefoxAurora'),
    win32: getFirefoxExe('Firefox Developer Edition')
  },
  ENV_CMD: 'FIREFOX_DEVELOPER_BIN'
}

FirefoxDeveloperBrowser.$inject = $INJECT_LIST

const FirefoxDeveloperHeadlessBrowser = makeHeadlessVersion(FirefoxDeveloperBrowser)

const FirefoxAuroraBrowser = function () {
  FirefoxBrowser.apply(this, arguments)
}

FirefoxAuroraBrowser.prototype = {
  name: 'FirefoxAurora',
  DEFAULT_CMD: {
    linux: isWsl ? getFirefoxExeWsl('Aurora') : 'firefox',
    darwin: getFirefoxWithFallbackOnOSX('FirefoxAurora'),
    win32: getFirefoxExe('Aurora')
  },
  ENV_CMD: 'FIREFOX_AURORA_BIN'
}

FirefoxAuroraBrowser.$inject = $INJECT_LIST

const FirefoxAuroraHeadlessBrowser = makeHeadlessVersion(FirefoxAuroraBrowser)

const FirefoxNightlyBrowser = function () {
  FirefoxBrowser.apply(this, arguments)
}

FirefoxNightlyBrowser.prototype = {
  name: 'FirefoxNightly',

  DEFAULT_CMD: {
    linux: isWsl ? getFirefoxExeWsl('Nightly', 'Firefox Nightly') : 'firefox',
    darwin: getFirefoxWithFallbackOnOSX('FirefoxNightly', 'Firefox Nightly'),
    win32: getFirefoxExe('Nightly', 'Firefox Nightly')
  },
  ENV_CMD: 'FIREFOX_NIGHTLY_BIN'
}

FirefoxNightlyBrowser.$inject = $INJECT_LIST

const FirefoxNightlyHeadlessBrowser = makeHeadlessVersion(FirefoxNightlyBrowser)

// PUBLISH DI MODULE
module.exports = {
  'launcher:Firefox': ['type', FirefoxBrowser],
  'launcher:FirefoxHeadless': ['type', FirefoxHeadlessBrowser],
  'launcher:FirefoxDeveloper': ['type', FirefoxDeveloperBrowser],
  'launcher:FirefoxDeveloperHeadless': ['type', FirefoxDeveloperHeadlessBrowser],
  'launcher:FirefoxAurora': ['type', FirefoxAuroraBrowser],
  'launcher:FirefoxAuroraHeadless': ['type', FirefoxAuroraHeadlessBrowser],
  'launcher:FirefoxNightly': ['type', FirefoxNightlyBrowser],
  'launcher:FirefoxNightlyHeadless': ['type', FirefoxNightlyHeadlessBrowser]
}
