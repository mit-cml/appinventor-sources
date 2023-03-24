![GEOSwift](/README-images/GEOSwift-header.png)

[![Build Status](https://travis-ci.org/GEOSwift/GEOSwift.svg?branch=develop)](https://travis-ci.org/GEOSwift/GEOSwift.svg?branch=develop)
[![CocoaPods Compatible](https://img.shields.io/cocoapods/v/GEOSwift.svg)](https://img.shields.io/cocoapods/v/GEOSwift.svg)
[![codecov](https://codecov.io/gh/GEOSwift/GEOSwift/branch/develop/graph/badge.svg)](https://codecov.io/gh/GEOSwift/GEOSwift)

Easily handle a geographical object model (points, linestrings, polygons etc.) and related topographical operations (intersections, overlapping etc.).  
A type-safe, MIT-licensed Swift interface to the OSGeo's GEOS library routines, nicely integrated with MapKit and Quicklook.

> **For *MapboxGL* integration visit: https://github.com/GEOSwift/GEOSwiftMapboxGL**

## Features

* A pure-Swift, type-safe, optional-aware programming interface
* Automatically-typed geometry deserialization from WKT and WKB representations
* *MapKit* and *MapboxGL* integration
* *Quicklook* integration
* A lightweight *GEOJSON* parser
* Extensively tested

## Requirements

* iOS 8.0+
* Xcode 10
* Swift 4.2
* CocoaPods 1.4.0+ (if developing for iOS)
* Swift Package Manager 4.2+ (if developing Linux/Mac binaries)

## Installation

### CocoaPods

1. Install autotools: `$ brew install autoconf automake libtool`
2. Update your `Podfile` to include:

```
use_frameworks!
pod 'GEOSwift'
```

3. Run `$ pod install`

> GEOS is a configure/install project licensed under LGPL 2.1: it is difficult to build for iOS and its compatibility with static linking is at least controversial. Use of GEOSwift without dynamic-framework-based CocoaPods and with a project targeting iOS 7, even if possible, is advised against.

### Swift Package Manager

SPM's [System Library Targets](https://github.com/apple/swift-evolution/blob/master/proposals/0208-package-manager-system-library-targets.md) allows linking against any library installed on the machine.

1. Ensure that the geos package is installed on your machine. For Mac, Homebrew is suggested: `$ brew install geos`
2. Update `Package.swift` to include GEOSwift as a dependency:

```swift
// swift-tools-version:4.2
let package = Package(
  name: "your-package-name",
  dependencies: [
    .package(url: "https://github.com/GEOSwift/GEOSwift.git", from: "x.x.x") // Reccommend latest release version, e.g. "3.1.0"
  ],
  targets: [
    .target("your-target-name", dependencies: ["GEOSwift"])
  ]
)
```

3.  Ensure that your system has a pkg-config file available for `libgeos_c`:
```
$ pkg-config --validate geos_c
```
> NOTE: Homebrew's current version of geos does not seem to generate a `.pc` file on install.  See the next section for options.

### Providing geos_c library location under Homebrew

Because Homebrew does not generate a pkg-config file for geos, one is provided in this repository: [geos_c.pc](CLibGeosC/geos_c.pc).  You can choose to install it manually, amend your PKG_CONFIG_PATH, or provide the search paths to swiftc by hand.

* Option 1: Copy the sample pkg-config file to your system:
```
$ cp CLibGeosC/geos_c.pc /usr/local/lib/pkgconfig/
$ pkg-config --validate geos_c # should return 0
```

* Option 2: Amend the PKG_CONFIG_PATH to allow pkg-config to find the configuration file:
```
$ export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/path/to/GEOSwift/CLibGeosC
$ pkg-config --validate geos_c # should succeed with no error
```

* Option 3: Pass the homebrew paths for geos to swift build every time (you may want to put this into a Makefile):
```
$ swift build -Xlinker \
              -L$(brew --prefix geos)/lib \
              -Xcc \
              -I$(brew --prefix geos)/include
```

## Usage

### Geometry creation

```swift
// 1. From Well Known Text (WKT) representation
let point = Waypoint(WKT: "POINT(10 45)")
let polygon = Geometry.create("POLYGON((35 10, 45 45.5, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))")

// 2. From a Well Known Binary (WKB)
let WKB: NSData = geometryWKB()
let geometry2 = Geometry.create(WKB.bytes, size: WKB.length)

// 3. From a GeoJSON file:
if let geoJSONURL = Bundle.main.url(forResource: "multipolygon", withExtension: "geojson"),
    let features = try! Features.fromGeoJSON(geoJSONURL),
    let italy = features.first?.geometries?.first as? MultiPolygon
{
    italy
}
```

### MapKit and MapboxGL integration

GEOSwift makes it easy generate annotations to display on a mapview using Apple MapKit and [MapboxGL](https://github.com/mapbox/mapbox-gl-native/).
On each Geometry instance you can call one of the related convenience func `mapShape()` or `mapboxShape()`, that will return an annotation object ready to be added as annotations to a `MKMapView` (for MapKit) or `MGLMapView` (for MapboxGL):

Example for MapKit:

```swift
let shape1 = point.mapShape() // will return a MKPointAnnotation
```

Example for MapboxGL:

For *MapboxGL* integration visit: https://github.com/GEOSwift/GEOSwiftMapboxGL

In this table you can find which annotation class you should expect when calling `mapShape()` or `mapboxShape()` on a geometry:

| WKT Feature | GEOSwift class | MapKit | MapboxGL |
|:------------------:|:-------------:|:-----------------:|:-----------------:|
| `POINT` | `WayPoint` | `MKPointAnnotation` | `MGLPointAnnotation` |
| `LINESTRING` | `LineString` | `MKPolyline` | `MGLPolyline` |
| `POLYGON` | `Polygon` | `MKPolygon` | `MGLPolygon` |
| `MULTIPOINT` | `MultiPoint` | `MKShapesCollection` | `not supported` |
| `MULTILINESTRING` | `MultiLineString` | `MKShapesCollection` | `not supported` |
| `MULTIPOLYGON` | `MultiPolygon` | `MKShapesCollection` | `not supported` |
| `GEOMETRYCOLLECTION` | `GeometryCollection` | `MKShapesCollection` | `not supported` |

Of course you should provide your implementation of the mapview delegate protocol (`MKMapViewDelegate` or `MGLMapViewDelegate`).
In MapKit, when dealing with geometry collections you have to define your own `MKOverlayRenderer` subclass.
Currently geometry collections are not supported when using `MapboxGL`.

### Topological operations

Let's say we have two geometries:

![Example geometries](/README-images/geometries.png)

GEOSwift let you perform a set of operations on these two geometries:

![Topological operations](/README-images/topological-operations.png)

### Predicates:

* _equals_: returns true if this geometric object is “spatially equal” to another geometry.
* _disjoint_: returns true if this geometric object is “spatially disjoint” from another geometry.
* _intersects_: returns true if this geometric object “spatially intersects” another geometry.
* _touches_: returns true if this geometric object “spatially touches” another geometry.
* _crosses_: returns true if this geometric object “spatially crosses’ another geometry.
* _within_: returns true if this geometric object is “spatially within” another geometry.
* _contains_: returns true if this geometric object “spatially contains” another geometry.
* _overlaps_: returns true if this geometric object “spatially overlaps” another geometry.
* _relate_: returns true if this geometric object is spatially related to another geometry by testing for intersections between the interior, boundary and exterior of the two geometric objects as specified by the values in the intersectionPatternMatrix.

### Playground

Explore more, interactively, from the Xcode project’s playground. It can be found inside `GEOSwift` workspace. Open the workspace on Xcode, build the `GEOSwift` framework and open the playground file.

![Playground](/README-images/playground.png)

## Contributing

To make a contribution:

* Fork the repo
* Start from the develop branch and create a branch with a name that describes your contribution
* Sign in to travis-ci.org (if you've never signed in before, CI won't run to verify your pull request)
* Push your branch and create a pull request to develop
* One of the maintainers will review your code and may request changes
* If your pull request is accepted, one of the maintainers should update the changelog before merging it

## Creator

Andrea Cremaschi ([@andreacremaschi](https://twitter.com/andreacremaschi))

## License

* GEOSwift was released by Andrea Cremaschi ([@andreacremaschi](https://twitter.com/andreacremaschi)) under a MIT license. See LICENSE for more information.
* [GEOS](http://trac.osgeo.org/geos/) stands for Geometry Engine - Open Source, and is a C++ library, ported from the [Java Topology Suite](http://sourceforge.net/projects/jts-topo-suite/). GEOS implements the OpenGIS [Simple Features for SQL](http://www.opengeospatial.org/standards/sfs) spatial predicate functions and spatial operators. GEOS, now an OSGeo project, was initially developed and maintained by [Refractions Research](http://www.refractions.net/) of Victoria, Canada.
