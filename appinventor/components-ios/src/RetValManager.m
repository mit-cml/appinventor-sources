// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "RetValManager.h"
#import <AIComponentKit/AIComponentKit-Swift.h>

#define TENSECONDS 10.0

static RetValManager *_manager = nil;

@interface RetValManager () {
 @private
  NSMutableArray *_results;
  NSLock *_semaphore;
  NSCondition *_waitLock;
}

@end

@implementation RetValManager

@synthesize usingWebRTC;

- (instancetype)init {
  if (self = [super init]) {
    _results = [[NSMutableArray alloc] initWithCapacity:10];
    _semaphore = [[NSLock alloc] init];
    _waitLock = [[NSCondition alloc] init];
    usingWebRTC = NO;
  }
  return self;
}

+ (instancetype)sharedManager {
  if (!_manager) {
    _manager = [[RetValManager alloc] init];
  }
  return _manager;
}

- (void)addResult:(NSDictionary *)result {
  [_waitLock lock];
  @try {
    BOOL sendNotify = _results.count == 0;
    [_results addObject:result];
    if (usingWebRTC) {
      NSDictionary *output = @{
        @"status": @"OK",
        @"values": [_results copy]
      };
      [_results removeAllObjects];
      [ReplForm returnRetvals:[NSJSONSerialization dataWithJSONObject:output options:0 error:nil]];
    } else if (sendNotify) {
      [_waitLock broadcast];
    }
  } @catch (NSException *exception) {
    NSLog(@"[RetValManager appendReturnValue:forBlock:withStatus:] Caught exception %@", exception);
  } @finally {
    [_waitLock unlock];
  }
}

- (void)appendReturnValue:(NSString *)item forBlock:(NSString *)blockId
  withStatus:(NSString *)status {
  if (!item) item = @"";
  NSDictionary *output = @{@"status": status, @"type": @"return", @"value": item, @"blockid": blockId};
  [self addResult:output];
}

- (void)appendLogValue:(NSString *)item forBlock:(NSString *)blockId 
  withStatus:(NSString *)status withLevel:(NSString *)level {
    if (!item) item = @"";
    NSDictionary *output = @{@"status": status, @"level": level, @"type": @"log", @"item": item, @"blockid": blockId};
    [self addResult:output];
  }

- (void)sendError:(NSString *)error {
  NSDictionary *output = @{@"status": @"OK", @"type": @"error", @"value": error};
  [self addResult:output];
}

- (void)pushScreen:(NSString *)screenName withValue:(NSObject *)value {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"pushScreen", @"screen": screenName}];
  if (value) {
    output[@"value"] = [value description];
  }
  [self addResult:output];
}

- (void)popScreen:(NSString *)value {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"popScreen"}];
  if (value) {
    output[@"value"] = value;
  }
  [self addResult:output];
}

- (void)assetTransferred:(NSString *)name {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"assetTransferred"}];
  if (name) {
    output[@"value"] = name;
  }
  [self addResult:output];
}

- (void)extensionsLoaded {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"extensionsLoaded"}];
  [self addResult:output];
}

- (NSString *)fetch:(BOOL)block {
  NSDate *finishBy = [NSDate dateWithTimeIntervalSinceNow:TENSECONDS];
  NSDate *exitBy = [NSDate dateWithTimeIntervalSinceNow:TENSECONDS - 0.1];
  [_waitLock lock];
  @try {
    while (_results.count == 0 && block) {
      if ([exitBy timeIntervalSinceNow] < 0) {
        break;
      }
      [_waitLock waitUntilDate:finishBy];
    }
    NSDictionary *output = @{@"status": @"OK", @"values": _results};
    NSError *err = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:output options:0 error:&err];
    [_results removeAllObjects];
    if (!err) {
      return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
  } @catch (NSException *exception) {
    NSLog(@"Got an exception %@", exception);
  } @finally {
    [_waitLock unlock];
  }
  return @"{\"status\":\"BAD\",\"message\":\"Failure in RetValManager\"}";
}

- (id)copyWithZone:(NSZone *)zone {
  return self;  // we don't want copies of a singleton!
}

@end
