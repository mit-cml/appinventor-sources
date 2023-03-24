# MIT App Inventor for iOS

## Prerequisites (Mac OS X)

* Xcode 14 or higher
* macOS 12 or higher

(optional) To automatically install the companion app on a connected, authorized iOS device, install ideviceinstaller via Homebrew:

```
brew uninstall ideviceinstaller
brew uninstall libimobiledevice
brew install --HEAD libimobiledevice
brew link --overwrite libimobiledevice
brew install --HEAD  ideviceinstaller
brew link --overwrite ideviceinstaller
sudo chmod -R 777 /var/db/lockdown/
```

## Building

Build iOS:

```shell
ant ios
```

To build components and reinstall the companion:

```shell
ant ioscomps
```

To build only the Android version of the companion (MIT App Inventor open source):

```shell
ant android
```

To build the webapp for Google App Engine:

```shell
ant webapp
```

## Testing

To run tests:

```shell
ant iostests
```
