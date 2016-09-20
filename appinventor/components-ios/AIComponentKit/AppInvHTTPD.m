//
//  AppInvHTTPD.m
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "AppInvHTTPD.h"
#include <CoreFoundation/CoreFoundation.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <ifaddrs.h>
#include <arpa/inet.h>

@interface AppInvHTTPD() {
 @private
  int _port;
  NSString *_wwwroot;
  BOOL _secure;
  ReplForm *_form;
  NSThread *_listenThread;
  CFSocketRef _ip4sock;
  CFSocketRef _ip6sock;
}

- (void)acceptConnection:(CFSocketNativeHandle)handle;

@end

static void handleConnect(CFSocketRef s, CFSocketCallBackType type, CFDataRef address,
                          const void *data, void *info) {
  if (type != kCFSocketAcceptCallBack) {
    return;
  }
  AppInvHTTPD *server = (__bridge_transfer AppInvHTTPD *) info;
  [server acceptConnection:*((const CFSocketNativeHandle *) data)];
}

static NSString *_hmacKey = nil;
static int _hmacSeq = 1;
static const NSUInteger BUFSIZE = 4096;

@interface AppInvHTTPD_Session : NSThread<NSStreamDelegate> {
 @private
  __weak AppInvHTTPD *_owner;
  NSInputStream *_reader;
  NSOutputStream *_writer;
  uint8_t buffer[BUFSIZE];
}
@end

@implementation AppInvHTTPD_Session

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
  if (eventCode == NSStreamEventEndEncountered || eventCode == NSStreamEventErrorOccurred) {
    [self cancel];
  } else if(eventCode == NSStreamEventHasBytesAvailable) {
    NSInteger bytesRead;
    while ((bytesRead = [_reader read:buffer maxLength:BUFSIZE]) > 0) {
      
    }
  }
}

- (void)main {
  @autoreleasepool {
    [[NSRunLoop currentRunLoop] run];
    _reader.delegate = nil;
    CFRelease((__bridge CFReadStreamRef) _reader);
    CFRelease((__bridge CFWriteStreamRef) _writer);
    _reader = nil;
    _writer = nil;
  }
}

- (id)initWithServer:(AppInvHTTPD *)server socket:(CFSocketNativeHandle)socket {
  if (self = [super init]) {
    _owner = server;
    CFReadStreamRef reader;
    CFWriteStreamRef writer;
    CFStreamCreatePairWithSocket(kCFAllocatorDefault, socket, &reader, &writer);
    _reader = (__bridge NSInputStream *)reader;
    _writer = (__bridge NSOutputStream *)writer;
    _reader.delegate = self;
  }
  return self;
}

@end

@implementation AppInvHTTPD

+ (void)setHmacKey:(NSString *)key {
  _hmacKey = [key copy];
  _hmacSeq = 1;
}

+ (void)resetSeq {
  _hmacSeq = 1;
}

- (void)acceptConnection:(CFSocketNativeHandle)handle {
  CFReadStreamRef _read;
  CFWriteStreamRef _write;
  CFStreamCreatePairWithSocket(kCFAllocatorDefault, handle, &_read, &_write);
  NSInputStream *input = (__bridge NSInputStream *) _read;
  NSOutputStream *output = (__bridge NSOutputStream *) _write;
  NSInteger read;
  const NSUInteger BUFSIZE = 4096;
  uint8_t *buffer = (uint8_t *)malloc(BUFSIZE * sizeof(uint8_t));
  while ((read = [input read:buffer maxLength:BUFSIZE]) > 0) {
    
  }
  input = nil;
  output = nil;
  CFRelease(_read);
  CFRelease(_write);
}

- (void)listen {
  struct sockaddr_in sin;
  struct sockaddr_in6 sin6;

  CFSocketContext context = {0, (__bridge_retained void *) self, NULL, NULL, NULL};

  _ip4sock = CFSocketCreate(kCFAllocatorDefault, PF_INET, SOCK_STREAM, IPPROTO_TCP,
                            kCFSocketAcceptCallBack, handleConnect, &context);
  _ip6sock = CFSocketCreate(kCFAllocatorDefault, PF_INET6, SOCK_STREAM, IPPROTO_TCP,
                            kCFSocketAcceptCallBack, handleConnect, &context);

  memset(&sin, 0, sizeof(sin));
  sin.sin_len = sizeof(sin);
  sin.sin_family = AF_INET;
  sin.sin_port = htons(_port);
  sin.sin_addr.s_addr= INADDR_ANY;

  CFDataRef sincfd = CFDataCreate(kCFAllocatorDefault, (UInt8 *)&sin, sizeof(sin));

  CFSocketSetAddress(_ip4sock, sincfd);
  CFRelease(sincfd);

  memset(&sin6, 0, sizeof(sin6));
  sin6.sin6_len = sizeof(sin6);
  sin6.sin6_family = AF_INET6; /* Address family */
  sin6.sin6_port = htons(_port); /* Or a specific port */
  sin6.sin6_addr = in6addr_any;

  CFDataRef sin6cfd = CFDataCreate(kCFAllocatorDefault, (UInt8 *)&sin6, sizeof(sin6));

  CFSocketSetAddress(_ip6sock, sin6cfd);
  CFRelease(sin6cfd);

  // configure run loops for listening
  CFRunLoopSourceRef socketsource = CFSocketCreateRunLoopSource(kCFAllocatorDefault, _ip4sock, 0);
  CFRunLoopAddSource(CFRunLoopGetCurrent(), socketsource, kCFRunLoopDefaultMode);
  CFRunLoopSourceRef socketsource6 = CFSocketCreateRunLoopSource(kCFAllocatorDefault, _ip6sock, 0);
  CFRunLoopAddSource(CFRunLoopGetCurrent(), socketsource6, kCFRunLoopDefaultMode);

  // loop indefinitely
  [[NSRunLoop currentRunLoop] run];
}

- (id)initWithPort:(int)port rootDirectory:(NSString *)wwwroot secure:(BOOL)secure
  form:(ReplForm *)form {
  if (self = [super init]) {
    _port = port;
    _wwwroot = [wwwroot copy];
    _secure = secure;
    _form = form;
    _listenThread = [[NSThread alloc] initWithTarget:self selector:@selector(listen) object:nil];
    [_listenThread start];
  }
  return self;
}

- (Response *)serveUri:(NSString *)uri method:(NSString *)method headers:(NSDictionary *)headers
            parameters:(NSDictionary *)params files:(NSDictionary *)files
                socket:(CFSocketRef)socket {
  return nil;
}

@end
