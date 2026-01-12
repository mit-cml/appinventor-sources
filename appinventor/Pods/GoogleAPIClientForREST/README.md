# Google APIs Client Library for Objective-C for REST

**Project site** <https://github.com/google/google-api-objectivec-client-for-rest><br>
**Discussion group** <http://groups.google.com/group/google-api-objectivec-client>

[![CocoaPods](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/cocoapods.yml/badge.svg?branch=main)](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/cocoapods.yml)
[![SwiftPM](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/swiftpm.yml/badge.svg?branch=main)](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/swiftpm.yml)
[![ServiceGenerator](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/service_generator.yml/badge.svg?branch=main)](https://github.com/google/google-api-objectivec-client-for-rest/actions/workflows/service_generator.yml)

Written by Google, this library is a flexible and efficient Objective-C
framework for accessing JSON APIs.

This is the recommended library for accessing JSON-based Google APIs for iOS,
macOS, tvOS, and watchOS applications.

**To get started** with Google APIs and the Objective-C client library, please
read [USING.md](USING.md) for detailed information. The
[example applications](https://github.com/google/google-api-objectivec-client-for-rest/tree/main/Examples)
can also help answer some questions, but there isn't an example for every
service as there are just too many services.

Generated interfaces for Google APIs are in the
[GeneratedServices folder](https://github.com/google/google-api-objectivec-client-for-rest/tree/main/Sources/GeneratedServices).

In addition to the pre generated classes included with the library, you can
generate your own source for other services that have a
[discovery document](https://developers.google.com/discovery/v1/reference/apis#resource-representations)
by using the
[ServiceGenerator](https://github.com/google/google-api-objectivec-client-for-rest/tree/main/Tools/ServiceGenerator).

**If you have a problem** or want a new feature to be included in the library,
please join the
[discussion group](http://groups.google.com/group/google-api-objectivec-client).
Be sure to include [http logs](USING.md#logging-http-server-traffic) for
requests and responses when posting questions. Bugs may also be submitted on the
[issues list](https://github.com/google/google-api-objectivec-client-for-rest/issues).

**Externally-included projects**: The library is built on top of code from the separate
project [GTM Session Fetcher](https://github.com/google/gtm-session-fetcher). To work
with some remote services, it also needs
[Authentication/Authorization](USING.md#authentication-and-authorization).

**Google Data APIs**: The much older library for XML-based APIs is
[still available](https://github.com/google/gdata-objectivec-client).

Other useful classes for Mac and iOS developers are available in the
[Google Toolbox for Mac](https://github.com/google/google-toolbox-for-mac).
