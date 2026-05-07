# GEOS Podspec

GEOS is an open source C++ library for working with geospatial geometry. Learn more on its
[homepage](http://trac.osgeo.org/geos). This repo hosts a [CocoaPods](https://cocoapods.org/)
Podspec that allows you to use GEOS's threadsafe C interface in your Swift or Objective-C project.
It is commonly used via [GEOSwift](https://github.com/GEOSwift/GEOSwift).

## Requirements

* iOS 8.0+
* Xcode 9+
* CocoaPods 1.0.0+

## Installing with CocoaPods

1. Install autotools: `$ brew install autoconf automake libtool`
2. Update your `Podfile` to include:

```
use_frameworks!
pod 'geos'
```

3. Run `$ pod install`

> GEOS is a configure/install project licensed under LGPL 2.1: it is difficult to build for iOS and
its compatibility with static linking is at least controversial. Use of geos without
dynamic-framework-based CocoaPods and with a project targeting iOS 7, even if possible, is advised
against.

## How this Podspec works

When you install this pod, GEOS is integrated into your Xcode project in a few steps:

1. Downloads the GEOS source from the project's [official git
repo](https://git.osgeo.org/gitea/geos/geos) via git submodules.
2. Runs the autotools build system (but doesn't run `make`) to configure the project files for the
system you're developing on.
3. Patches a few files to make things work.
4. The resulting source and header files are built by Xcode when you build your project.

## History

Starting with tag 3.7.0 in this repo, the Podspec here should match the one in the CocoaPods specs
repo. This has not always been the case. For example, the 3.5.0 tag in this repo does not actually
represent what you get if you install 3.5.0 from CocoaPods. Alas, this situation should be remedied
as we move forward.

Version 3.7.0 is based on the actually-published 3.5.0 podspec.

The version numbers in this repo are designed to match the corresponding version numbers in GEOS
itself.
