# PSSRedisClient

A simple [Swift](https://developer.apple.com/swift/)-based interface to [Redis](https://redis.io), using [CocoaAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket)

## Introduction

We had a project that required a modern implementation of either an ObjC or Swift-based redis client. After some research, it became clear that most objc or swift libraries either attempt to implement both the socket communication and the redis protocol, or use one-off socket communication libraries. For example, here is one such dual socket and redis protocol implementation, [ObjCHiredis](https://github.com/lp/ObjCHiredis/blob/master/ObjCHiredis/hiredis.c). Such libraries contain an excessive amount of network and pointer logic, and we found this one in particular prone to crashes. Additionally, experience suggests that pointer-heavy C/C++ code not actively maintained is almost certainly vulnerable to exploits.

In order to have maximum confidence in the robustness of our socket communication, we investigated iOS socket libraries. [CocoaAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket) was the choice we made, for these reasons:

1. First result in Google searches for [iOS socket libraries](https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=ios+socket+libraries)
1. It is in the public domain
1. More than 8k stars and 2k forks, which is an order of magnitude more than the [next most popular library](https://github.com/MegaBits/SIOSocket)
1. [Actively and frequently maintained](https://github.com/robbiehanson/CocoaAsyncSocket/commits/master)

Finally, as an additional consideration for our redis client, we wanted a library installable via [Cocoapods](https://www.cocoapods.org).

Numerous Swift-based interfaces to redis exist, including:

1. [RedBird](https://github.com/vapor/redbird) -
This library has a networking dependency on the Vaport Socks library, not the [CocoaAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket) library. It is not available on Cocoapods.
1. [Zewo Redis](https://github.com/Zewo/Redis) -
This library has a dependency on Zewo's own [TCP socket library](https://github.com/Zewo/TCP), again not [CocoaAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket) library. It is not available on Cocoapods.
1. [Swidis](https://github.com/FarhadSaadatpei/Swidis) -
There doesn't seem to be any actual code in this library?. It is not available on Cocoapods.
1. [SwiftRedis](https://github.com/ronp001/SwiftRedis) -
This library appears to implement the networking functions itself, instead of using the [CocoaAsyncSocket](https://github.com/robbiehanson/CocoaAsyncSocket) library. It is not available on Cocoapods.

Because none of these solutions were built atop CocoaAsyncSockets, and none were available on [Cocoapods](https://www.cocoapods.org), we created our own simple class that is able to use CocoaAsyncSockets for the networking component, and that parses the [redis protocol](https://redis.io/topics/protocol).

## Example

To run the example project, clone the repo, and run `pod install` from the Example directory first.

## Requirements

iOS 9+

## Installation

PSSRedisClient is available through [CocoaPods](http://cocoapods.org). To install
it, simply add the following line to your Podfile:

```ruby
pod "PSSRedisClient"
```

## Usage

```swift
override func viewDidLoad() {
    ... 

    self.redisManager = RedisClient(delegate: self)
    self.subscriptionManager = RedisClient(delegate: self)
    
    self.redisManager?.connect(host: "localhost",
                               port: 6379,
                               pwd: "password")
    self.subscriptionManager?.connect(host: "localhost",
                                      port: 6379,
                                      pwd: "password")
}

func socketDidConnect(client: RedisClient) {
    debugPrint("SOCKET: Connected")

    // Setup a subscription after we have connected
    if (redisManager == self.subscriptionManager) {
        self.subscriptionManager?.exec(args: ["subscribe", channel], completion: nil)
    }
}

func socketDidDisconnect(client: RedisClient, error: Error?) {
    debugPrint("Disconnected (Error: \(error?.localizedDescription))")
}

func subscriptionMessageReceived(results: NSArray) {
    if (results.count == 3 && results.firstObject as? String != nil) {
        let message = results.firstObject as! String

        if (message == "message") {
            debugPrint("SOCKET: Sending message of \(results[2])");

            self.results.text = "Subscription heard: \(results[2])"
        } else if (message == "subscribe") {
            debugPrint("SOCKET: Subscription successful");
        } else {
            debugPrint("SOCKET: Unknown message received");
        }
    }
}

func messageReceived(message: NSArray) {
    if (message.firstObject as? NSError != nil) {
        let error = message.firstObject as! NSError
        let userInfo = error.userInfo

        if let possibleMessage = userInfo["message"] {
            if let actualMessage = possibleMessage as? String {
                debugPrint("SOCKETS: Error: \(actualMessage)")
            }
        }
    } else {
        debugPrint("Results: \(message.componentsJoined(by: " "))")
    }
}


```

## Author

Eric Silverberg, @esilverberg

## License

PSSRedisClient is available under the MIT license. See the LICENSE file for more info.
