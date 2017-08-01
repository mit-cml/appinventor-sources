//
//  RetValManager.m
//  AIComponentKit
//
//  Created by Evan Patton on 9/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "RetValManager.h"

#define TENSECONDS 10000

static RetValManager *_manager = nil;

@interface RetValManager () {
 @private
  NSMutableArray *_results;
  NSLock *_semaphore;
  NSCondition *_waitLock;
}

@end

@implementation RetValManager

- (instancetype)init {
  if (self = [super init]) {
    _results = [[NSMutableArray alloc] initWithCapacity:10];
    _semaphore = [[NSLock alloc] init];
    _waitLock = [[NSCondition alloc] init];
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
    if (sendNotify) {
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

- (void)sendError:(NSString *)error {
  NSDictionary *output = @{@"status": @"OK", @"type": @"error", @"value": error};
  [self addResult:output];
}

- (void)pushScreen:(NSString *)screenName withValue:(NSObject *)value {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"pushScreen", @"screen": screenName}];
  if (value) {
    [output setValue:[value description] forKey:@"value"];
  }
  [self addResult:output];
}

- (void)popScreen:(NSString *)value {
  NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary: @{@"status": @"OK", @"type": @"popScreen"}];
  if (value) {
    [output setValue:value forKey:@"value"];
  }
  [self addResult:output];
}

- (NSString *)fetch:(BOOL)block {
  NSDate *finishBy = [NSDate dateWithTimeIntervalSinceNow:TENSECONDS];
  NSDate *exitBy = [NSDate dateWithTimeIntervalSinceNow:TENSECONDS - 100];
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
