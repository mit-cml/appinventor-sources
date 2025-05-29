// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

@interface RetValManager : NSObject

+ (nonnull instancetype)sharedManager NS_SWIFT_NAME(shared());
- (void)appendReturnValue:(nonnull NSString *)item forBlock:(nonnull NSString *)blockId withStatus:(nonnull NSString *)status;
- (void)appendLogValue:(NSString *)item forBlock:(NSString *)blockId withStatus:(NSString *)status withLevel:(NSString *)level;
- (void)sendError:(nonnull NSString *)error;
- (void)pushScreen:(nonnull NSString *)screenName withValue:(nonnull NSObject *)value;
- (void)popScreen:(nonnull NSString *)value;
- (void)assetTransferred:(nonnull NSString *)name;
- (void)extensionsLoaded;
- (nonnull NSString *)fetch:(BOOL)block;

@property BOOL usingWebRTC;

@end
