# SwiftyBase64
SwiftyBase64 provides base64url and base64 encoders as defined by
[RFC 4648](https://tools.ietf.org/html/rfc4648).

## Usage

### Standard Base64 Encoding to String
    
    import SwiftyBase64
    ...
    let bytesToEncode : [UInt8] = [1,2,3]
    let base64EncodedString = SwiftyBase64.EncodeString(bytesToEncode)
    
### URL and Filename Safe Base64 Encoding to String

    import SwiftyBase64
    ...
    let bytesToEncode : [UInt8] = [1,2,3]
    let base64EncodedString = SwiftyBase64.EncodeString(bytesToEncode, alphabet:.URLAndFilenameSafe)

### Standard Base64 Encoding to [UInt8] of ASCII bytes
    
    import SwiftyBase64
    ...
    let bytesToEncode : [UInt8] = [1,2,3]
    let base64EncodedASCIIBytes = SwiftyBase64.Encode(bytesToEncode)
    
### URL and Filename Safe Base64 Encoding to [UInt8] of ASCII bytes

    import SwiftyBase64
    ...
    let bytesToEncode : [UInt8] = [1,2,3]
    let base64EncodedASCIIBytes = SwiftyBase64.Encode(bytesToEncode, alphabet:.URLAndFilenameSafe)


## CocoaPods Installation

[CocoaPods](http://cocoapods.org) is a dependency manager for Cocoa projects.

CocoaPods 0.36 adds supports for Swift and embedded frameworks. You can install it with the following command:

```bash
$ gem install cocoapods
```

To integrate SwiftyBase64 into your Xcode project using CocoaPods, specify it in your `Podfile`:

```ruby
use_frameworks!
pod 'SwiftyBase64', '~> 1.0'
```

Then, run the following command:

```bash
$ pod install
```
