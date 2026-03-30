# MIT App Inventor for iOS

## Prerequisites (Mac OS X)

* Xcode 14 or higher
* macOS 12 or higher
* valid Apple Developer account and provisioning profile for your device

(Optional) To automatically install the companion app on a connected, authorized iOS device, install ideviceinstaller via Homebrew:

```
brew uninstall ideviceinstaller
brew uninstall libimobiledevice
brew install --HEAD libimobiledevice
brew link --overwrite libimobiledevice
brew install --HEAD ideviceinstaller
brew link --overwrite ideviceinstaller
sudo chmod -R 777 /var/db/lockdown/
```

## Setup

Before building, copy the sample Xcode config and set your development team:

```shell
cp appinventor/AICompanionApp.xcconfig.sample appinventor/AICompanionApp.xcconfig
```

Edit `appinventor/AICompanionApp.xcconfig` and set:

```conf
DEVELOPMENT_TEAM = YOUR_TEAM_ID
BUNDLE_IDENTIFIER = edu.mit.appinventor.aicompanion3
```

Replace `YOUR_TEAM_ID` with the development team ID from your Apple Developer account.

## Building

From the repository root, run:

```shell
ant ios
```

To build components and reinstall the companion app:

```shell
ant ioscomps
```

To build only the Android companion:

```shell
ant android
```

To build the webapp for Google App Engine:

```shell
ant webapp
```

## Testing

To run the iOS tests:

```shell
ant iostests
```
