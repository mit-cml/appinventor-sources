# AGENTS.md — MIT App Inventor

This is the MIT App Inventor project. It is a web based platform for
people to build mobile phone apps and learn about programming. It uses
a block coding environment built on Blockly and allows for live
testing the applications using a companion app on Android or iOS.

App Inventor is a complex system with many moving parts. Because it is
relied on by millions of users worldwide every month, take care to
make thoughtful, targeted changes. Large changes risk breaking things
other people rely on. The impact of the changes should dictate the
size of the PR. Add tests where necessary to help catch potential
regressions in your implementation.

Contributors are expected to be able to understand and justify the
changes they are making. Work with them to develop their ideas, but
they are responsible for opening their own PRs with the explanation of
their work. Any PR that does not follow the template, or fails tests,
may be closed without review.

## Prerequisites

- **Java 17** (OpenJDK preferred; JRE is not enough)
- **Apache Ant 1.10**
- **Google Cloud SDK** (for local dev server)
- **Node.js 20+** and **Firefox** (for JavaScript tests)
- **Latest Xcode** for iOS development
- Run `./buildtools doctor` first to diagnose environment issues

## Submodules (required before building)

```bash
git submodule update --init
```

One submodule: `appinventor/schemekit/src/picrin`.
Deinit before switching branches that lack them: `git submodule deinit --all`.

Copy `sample-.gitignore` to `.gitignore` before building.

## Build Commands (all run from `appinventor/` directory)

| Command | What it does |
|---|---|
| `ant MakeAuthKey` | Generate auth key (required before first build) |
| `ant` or `ant all` | Full build (all modules) |
| `ant noplay` | Build without companion app (faster, CI default) |
| `ant -Dskip.ios=true` | Skip iOS build (required on macOS if not configured to build for iOS) |
| `ant PlayApp` | Build Android companion APK only |
| `ant extensions` | Build external component extensions (.aix) |
| `ant clean` | Clean all submodule build artifacts |
| `ant -Drelease=true noplay` | Release build with optimizations |
| `ant docs` | Update the documentation pages |

## Running Locally

1. Start GWT Super Dev Mode: `ant devmode` (CodeServer on port 9876)
2. Start App Engine dev server:
   ```bash
   your-google-cloud-SDK-folder/bin/java_dev_appserver.sh --port=8888 --address=0.0.0.0 appengine/build/war/
   ```
3. Optionally start build server (needed for APK builds): `ant RunLocalBuildServer`
4. Open http://localhost:8888, use GWT bookmarklets from http://localhost:9876 for hot-reload

## Testing

- **All tests:** `ant tests` (from `appinventor/`). Runs Java + JavaScript + iOS tests.
- **Single Java test:** `ant -Dtest_name=com.google.appinventor.common.utils.StringUtilsTest CommonUtilsTests`
- **JS tests:** Karma + Mocha + Chai, Firefox headless. Config in `karma.conf.js`. Test files in `blocklyeditor/tests/com/google/appinventor/mocha/*.js`.
- **iOS tests:** Xcode/xcodebuild, iPhone 14 Pro simulator. Schemes: SchemeKit, AIComponentKit, AICompanionApp.
- Test reports: `reports/html/index.html` after `ant tests`

## Module Structure (under `appinventor/`)

| Directory | Purpose |
|---|---|
| `appengine/` | Main server + GWT client (ODE editor). Entry: `Ode.java` |
| `blocklyeditor/` | Blockly blocks editor (JavaScript, YAIL generators) |
| `common/` | Shared Java utilities |
| `components/` | Android component runtime (`com.google.appinventor.components`) |
| `components-ios/` | iOS component implementations (Swift) |
| `buildserver/` | APK/IPA build server |
| `aiplayapp/` | Companion app Android project |
| `schemekit/` | Framework for implementing Scheme on iOS |
| `aicompanionapp/` | iOS Companion App |
| `lib/` | External JAR dependencies |
| `misc/` | Utility scripts (checkstyle, etc.) |

All Java packages use `com.google.appinventor.<module>` naming (server/shared/client/buildserver/components/common).

## Code Style & Quality

- **Checkstyle:** Google Java Style + AI custom rules. Config in `lib/checkstyle/appinventor-checks.xml`.
  - 2-space indentation, 100-char max line length
  - `@Simple*`/`@Design*` annotated methods must be PascalCase
- Run `ant checkstyle` (changed files) or `ant checkstyle-all` (all files)
- JavaScript: Google JavaScript Style Guide

## Important Conventions

- Branch from `ucr` for device/companion changes; `master` for everything else.
- Component API changes require: bump version in `YaVersion.java`, update `YoungAndroidFormUpgrader.java`, update `blocklyeditor/src/versioning.js` to preserve semantics of existing project code, particularly if a default value is changed.
- GWT client code lives under `appengine/src/com/google/appinventor/client/`; server under `server/`; shared under `shared/`.
- Android SDK target version: `36` (set in `components/build.xml`).

## CI

App Inventor uses Jenkins to run its automated test suite, which breaks the build up into two components (Android build and tests with `ant tests` and iOS build and tests with `ant iostests`). All tests must pass before any PR is accepted.
